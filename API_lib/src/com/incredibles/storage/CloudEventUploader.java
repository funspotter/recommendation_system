package com.incredibles.storage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import com.incredibles.data.Event;
import com.incredibles.data.EventVisitor;
import com.incredibles.data.FilmEvent;
import com.incredibles.data.InterTicketEvent;
import com.incredibles.data.InterTicketShow;
import com.incredibles.data.PtEvent;
import com.incredibles.data.Show;
import com.incredibles.data.SimpleEvent;
import com.incredibles.geocoder.Geocoder;
import com.incredibles.geocoder.Geocoder.MyLatLng;

public class CloudEventUploader implements EventUploader {

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
	
	/** database connection */
	protected Connection conn;
	/** visitor to upload events */
	protected UploadEventVisitor uploadVisitor;
	
	public CloudEventUploader() {
		uploadVisitor = new UploadEventVisitor();
		
		try {
			conn = createConnection();
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected Connection createConnection() throws SQLException {
		String url = String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf8&characterSetResults=utf8", 
				DatabaseConnectionData.SERVER_NAME, 
				DatabaseConnectionData.PORT_NUMBER, 
				DatabaseConnectionData.DB_NAME);
		
		if (CommonTools.DRIVER_REGISTERED.compareAndSet(false, true)) {
			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		}
		return DriverManager.getConnection(
				url, 
				DatabaseConnectionData.USER_NAME, 
				DatabaseConnectionData.PASSWORD);
	}
	
	@Override
	public void uploadEvents(List<Event> events) throws SQLException {
		try {
			for (Event event : events) {
				event.accept(uploadVisitor);
			}
			conn.commit();
		} catch (SQLException e) {
			conn.rollback();
			throw e;
		}
	}
	
	@Override
	public void uploadOrganizerEvents(List<SimpleEvent> events)
			throws SQLException {
		try {
			for (SimpleEvent event : events) {
				event.accept(uploadVisitor);
			}
			conn.commit();
		} catch (SQLException e) {
			conn.rollback();
			throw e;
		}
		
	}
	
	@Override
	public void uploadInterticketEvents(List<InterTicketEvent> events) throws SQLException {
		try {
			for (InterTicketEvent event : events) {
				event.accept(uploadVisitor);
			}
			conn.commit();
		} catch (SQLException e) {
			conn.rollback();
			throw e;
		}
	}
	

	
	@Override
	public void updateInterTicketEvents(List<InterTicketEvent> events) throws SQLException {
		
		final String selectEvent = 
				"SELECT id FROM TEST_EVENT WHERE name = ?";
		PreparedStatement selectEventStatement = null;
		ResultSet selectEventResultSet = null;
		
		final String updateEvent = 
				"UPDATE TEST_EVENT SET audit_id = ?, venue_id = ? WHERE id = ?";
		PreparedStatement updateEventStatement = null;
		
		final String updateShow =
				"UPDATE TEST_SHOW SET netevent_id = ? WHERE event_id = ?";
		PreparedStatement updateShowStatement = null;
		
		try {
			selectEventStatement = conn.prepareStatement(selectEvent);
			updateEventStatement = conn.prepareStatement(updateEvent);
			updateShowStatement = conn.prepareStatement(updateShow);
			
			for (InterTicketEvent event : events) {
				selectEventStatement.clearParameters();
				selectEventStatement.setString(1, event.getName());
				selectEventResultSet = selectEventStatement.executeQuery();
				
				if (selectEventResultSet.next()) { 
					// this event already exists in the database
					long eventId = selectEventResultSet.getLong("id");
					System.out.println(String.format("Updating event: %s, id = %d", event.getName(), eventId));
					
					// update event
					updateEventStatement.clearParameters();
					updateEventStatement.setInt(1, event.getAuditId());
					updateEventStatement.setInt(2, event.getVenueId());
					updateEventStatement.setLong(3, eventId);
					updateEventStatement.executeUpdate();
					
					// update shows
					for (Show show : event.getShowList()) {
						String NetEvent_id = ((InterTicketShow) show).getNetEvent_id();
						updateShowStatement.clearParameters();
						updateShowStatement.setString(1, NetEvent_id);
						updateShowStatement.setLong(2, eventId);
						updateShowStatement.executeUpdate();
					}
				} else {
					// this event is not in the database yet
					System.out.println(String.format("Event not in database: %s", event.getName()));
				}
			}
			conn.commit();
			
		} catch (SQLException e) {
			
			conn.rollback();
			throw e;
			
		} finally {
			
			if (selectEventResultSet != null) {
				selectEventResultSet.close();
			}
			if (selectEventStatement != null) {
				selectEventStatement.close();
			}
			if (updateEventStatement != null) {
				updateEventStatement.close();
			}
			
		}
	}
	
	@Override
	public void close() throws IOException, SQLException {
		Runnable r = new Runnable(){
			@Override
			public void run() {
				ClientDbService dbServiceR;
				try {
					dbServiceR = ClientDbServiceCreator.createCloud();
					dbServiceR.copyNewEventsToOldUsers();
					dbServiceR.uploadShowsToShowJson();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		};
		Thread t = new Thread(r);
		t.start();
		
		conn.close();
	}
	
	private class UploadEventVisitor implements EventVisitor {
		
		protected final Geocoder geocoder;
		
		public UploadEventVisitor() {
			this.geocoder = new Geocoder();
		}
		
		@Override
		public void visitFilm(FilmEvent event) throws SQLException {
			
			final String selectFilm = 
					"SELECT id FROM TEST_EVENT WHERE name = ?";
			
			final String insertFilm =
					"INSERT INTO TEST_EVENT" +
					"(name, originaltitle, genre, length, rating, director, actors, year, releasedon, description, discriminator, many_shows, lang_code) " +
					"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '" + FilmEvent.DISCRIIMINATOR_VALUE + "', 1, ?)";
			
			PreparedStatement selectFilmStatement = null;
			ResultSet selectFilmResultSet = null;
			PreparedStatement insertFilmStatement = null;
			Statement getNewIdStatement = null;
			ResultSet generatedKeys = null;
			try {
				
				selectFilmStatement = conn.prepareStatement(selectFilm);
				selectFilmStatement.setString(1, event.getName());
				selectFilmResultSet = selectFilmStatement.executeQuery();
				if (selectFilmResultSet.next()) { 
					
					// this event already exists in the database
					long eventId = selectFilmResultSet.getLong("id");
					System.out.println(String.format("%s : film already in database, id = %d", event.getName(), eventId));
					insertShows(eventId, event.getShowList());
					updateShowJson(eventId);
				} else {
					System.out.println(String.format("%s : film not yet in database", event.getName()));
					// the event is not yet in the database, we have to insert it now
					insertFilmStatement = conn.prepareStatement(insertFilm, Statement.RETURN_GENERATED_KEYS);
					insertFilmStatement.setString(1, event.getName());
					insertFilmStatement.setString(2, event.getOriginalTitle());
					insertFilmStatement.setString(3, event.getGenre());
					insertFilmStatement.setInt(4, event.getLengthInMin());
					insertFilmStatement.setInt(5, event.getRating());
					insertFilmStatement.setString(6, event.getDirector());
					
					if(event.getActors().length()<150){
						insertFilmStatement.setString(7, event.getActors());
					}else{
						insertFilmStatement.setString(7, "");
					}
					
					
					insertFilmStatement.setLong(8, 2000l);
					// TODO: year property a FilmEvent-be, db-ben meg ne long legyen
					insertFilmStatement.setInt(9, 2000);
					// TODO: Date - number konverzi�
					String description = event.getDescription();
					if (description != null) {
						if (description.length() > 3000) {
							// TODO: ez nem biztos hogy lefut rendesen
							description = description.substring(0, 2999);
						}
					} else {
						description = "Nincs leiras.";
					}
					insertFilmStatement.setString(10, description);
					insertFilmStatement.setString(11, event.getLangCode());
					insertFilmStatement.executeUpdate();
					generatedKeys = insertFilmStatement.getGeneratedKeys();
					if (generatedKeys.next()) {
						int eventId = generatedKeys.getInt(1); 
						insertShows(eventId, event.getShowList());
					} else {
						System.out.println("Szopo....");
					}
					

//					getNewIdStatement = conn.createStatement();
//					getNewIdStatement.execute("SELECT EVENT_ID_SEQ.currval FROM dual");
//					ResultSet nextIdRs = getNewIdStatement.getResultSet();
//
//					if (nextIdRs.next()) {
//
//						int eventId = nextIdRs.getInt(1);
//						insertShows(eventId, event.getShowList());
//					} else {
//						System.out.println("szopo....");
//					}
				}
				
			} finally {
				
				if (generatedKeys != null) {
					generatedKeys.close();
				}
				
				if (insertFilmStatement != null) {
					insertFilmStatement.close();
				}
				
				if (getNewIdStatement != null) {
					getNewIdStatement.close();
				}
				if (selectFilmResultSet != null) {
					selectFilmResultSet.close();
				}
				if (selectFilmStatement != null) {
					selectFilmStatement.close();
				}
			}
		}
		
		@Override
		public void visitPt(PtEvent event) {
			
			if (event.getShowList().size() == 0) {
				// we do not upload events without shows
				return;
			}
			
			final String insertEvent = 
					"INSERT INTO TEST_EVENT (name, description, discriminator) VALUES(?, ?, ?)";
			
			PreparedStatement eventStatement = null;
			Statement getIdStatement = null;
			try {
				eventStatement = conn.prepareStatement(insertEvent);
				eventStatement.setString(1, event.getName());
				String description = event.getDescription();
				if (description == null || description.equals("")){
					description = "Nincs leiras.";
				}
				if (description.length() > 3000) { // TODO: ez nem biztos hogy lefut rendesen
//					event.setDescription(event.getDescription().substring(0, 2999));
					description = description.substring(0, 2999);
				}
				
				eventStatement.setString(2, description);
				eventStatement.setString(3, event.getPtEventType().name().toLowerCase());
				eventStatement.executeUpdate();
				conn.commit();
				
				getIdStatement = conn.createStatement();
				getIdStatement.execute("SELECT EVENT_ID_SEQ.currval FROM dual");
				ResultSet nextIdRs = getIdStatement.getResultSet();
				
				if (nextIdRs.next()) {

					int eventId = nextIdRs.getInt(1);
					System.out.println(String.format("event ID = %d", eventId));
					insertShows(eventId, event.getShowList());
				} else {
					System.out.println("szopo....");
				}
				
				
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (eventStatement != null) {
					try {
						eventStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				if (getIdStatement != null) {
					try {
						getIdStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	
		@Override
		public void visitSimple(Event event) throws SQLException {
			final String selectFilm = 
					"SELECT id FROM TEST_EVENT WHERE name = ?";
			
			final String insertFilm =
					"INSERT INTO TEST_EVENT" +
					"(name, description, discriminator, many_shows,lang_code, thumbnail) " +
					"VALUES(?, ?, ?, ?, ?, ?)";
			
			PreparedStatement selectFilmStatement = null;
			ResultSet selectFilmResultSet = null;
			PreparedStatement insertFilmStatement = null;
			Statement getNewIdStatement = null;
			ResultSet generatedKeys = null;
			try {
				
				selectFilmStatement = conn.prepareStatement(selectFilm);
				selectFilmStatement.setString(1, event.getName());
				selectFilmResultSet = selectFilmStatement.executeQuery();
				if (selectFilmResultSet.next()) { 
					
					// this event already exists in the database
					long eventId = selectFilmResultSet.getLong("id");
					System.out.println(String.format("%s : film already in database, id = %d", event.getName(), eventId));
					insertShows(eventId, event.getShowList());
					updateShowJson(eventId);
					
				} else {
					System.out.println(String.format("%s : film not yet in database", event.getName()));
					// the event is not yet in the database, we have to insert it now
					insertFilmStatement = conn.prepareStatement(insertFilm, Statement.RETURN_GENERATED_KEYS);
					insertFilmStatement.setString(1, event.getName());

					
					String description = null;
					if (event.getDescription() == null) {
						description = "Nincs leiras.";
					} else {
						description = event.getDescription();
						if (description.length() > 3000) {
							description = description.substring(0, 2999);
						}
					}
					
					insertFilmStatement.setString(2, description); // description
					insertFilmStatement.setString(3, "simple"); // discriminator
					
					insertFilmStatement.setInt(4, event.hasManyShowsOnOneDay() ? 1 : 0); // many _shows 
					insertFilmStatement.setString(5, event.getLangCode());
					insertFilmStatement.setString(6, event.getThumbnail());
					insertFilmStatement.executeUpdate();
					generatedKeys = insertFilmStatement.getGeneratedKeys();
					if (generatedKeys.next()) {
						int eventId = generatedKeys.getInt(1); 
						insertShows(eventId, event.getShowList());
					} else {
						System.out.println("Szopo....");
					}
				}
			} finally {
				
				if (generatedKeys != null) {
					generatedKeys.close();
				}
				
				if (insertFilmStatement != null) {
					insertFilmStatement.close();
				}
				
				if (getNewIdStatement != null) {
					getNewIdStatement.close();
				}
				if (selectFilmResultSet != null) {
					selectFilmResultSet.close();
				}
				if (selectFilmStatement != null) {
					selectFilmStatement.close();
				}
			}
		}
		
		
		@Override
		public void visitSimpleOrganizer(SimpleEvent event) throws SQLException {
			final String selectFilm = 
					"SELECT id FROM TEST_EVENT WHERE name = ?";
			
			final String insertFilm =
					"INSERT INTO TEST_EVENT" +
					"(name, description, discriminator, many_shows,lang_code, thumbnail) " +
					"VALUES(?, ?, ?, ?, ?, ?)";
			
			PreparedStatement selectFilmStatement = null;
			ResultSet selectFilmResultSet = null;
			PreparedStatement insertFilmStatement = null;
			Statement getNewIdStatement = null;
			ResultSet generatedKeys = null;
			try {
				
				selectFilmStatement = conn.prepareStatement(selectFilm);
				selectFilmStatement.setString(1, event.getName());
				selectFilmResultSet = selectFilmStatement.executeQuery();
				if (selectFilmResultSet.next()) { 
					
					// this event already exists in the database
					long eventId = selectFilmResultSet.getLong("id");
					System.out.println(String.format("%s : film already in database, id = %d", event.getName(), eventId));
					insertShows(eventId, event.getShowList());
					updateShowJson(eventId);
					
				} else {
					System.out.println(String.format("%s : film not yet in database", event.getName()));
					// the event is not yet in the database, we have to insert it now
					insertFilmStatement = conn.prepareStatement(insertFilm, Statement.RETURN_GENERATED_KEYS);
					insertFilmStatement.setString(1, event.getName());

					
					String description = null;
					if (event.getDescription() == null) {
						description = "Nincs leiras.";
					} else {
						description = event.getDescription();
						if (description.length() > 3000) {
							description = description.substring(0, 2999);
						}
					}
					
					String discriminator = null;
					if (event.getDiscriminator() == null) {
						discriminator = "simple";
					} else {
						discriminator = event.getDiscriminator();
					}
					
					insertFilmStatement.setString(2, description); // description
					insertFilmStatement.setString(3, discriminator); // discriminator
					
					insertFilmStatement.setInt(4, event.hasManyShowsOnOneDay() ? 1 : 0); // many _shows 
					insertFilmStatement.setString(5, event.getLangCode());
					insertFilmStatement.setString(6, event.getThumbnail());
					insertFilmStatement.executeUpdate();
					generatedKeys = insertFilmStatement.getGeneratedKeys();
					if (generatedKeys.next()) {
						int eventId = generatedKeys.getInt(1); 
						insertShows(eventId, event.getShowList());
					} else {
						System.out.println("Szopo....");
					}
				}
			} finally {
				
				if (generatedKeys != null) {
					generatedKeys.close();
				}
				
				if (insertFilmStatement != null) {
					insertFilmStatement.close();
				}
				
				if (getNewIdStatement != null) {
					getNewIdStatement.close();
				}
				if (selectFilmResultSet != null) {
					selectFilmResultSet.close();
				}
				if (selectFilmStatement != null) {
					selectFilmStatement.close();
				}
			}
		}
		
		@Override
		public void visitInterTicket(InterTicketEvent event) throws SQLException {
			visitSimple(event);
		}
		
		/*
		@Override
		public void visitJegyHu(JegyHuEvent event) throws SQLException {
			final String selectFilm = 
					"SELECT id FROM TEST_EVENT WHERE name = ?";
			
			final String insertFilm =
					"INSERT INTO TEST_EVENT" +
					"(name, description, discriminator, many_shows) " +
					"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '" + FilmEvent.DISCRIIMINATOR_VALUE + "', 1)";
			
			PreparedStatement selectFilmStatement = null;
			ResultSet selectFilmResultSet = null;
			PreparedStatement insertFilmStatement = null;
			Statement getNewIdStatement = null;
			ResultSet generatedKeys = null;
			try {
				
				selectFilmStatement = conn.prepareStatement(selectFilm);
				selectFilmStatement.setString(1, event.getName());
				selectFilmResultSet = selectFilmStatement.executeQuery();
				if (selectFilmResultSet.next()) { 
					
					// this event already exists in the database
					long eventId = selectFilmResultSet.getLong("id");
					System.out.println(String.format("%s : film already in database, id = %d", event.getName(), eventId));
					insertShows(eventId, event.getShowList());
					
				} else {
					System.out.println(String.format("%s : film not yet in database", event.getName()));
					// the event is not yet in the database, we have to insert it now
					insertFilmStatement = conn.prepareStatement(insertFilm, Statement.RETURN_GENERATED_KEYS);
					insertFilmStatement.setString(1, event.getName());

					
					String description = null;
					if (event.getShortDescription() == null && event.getDescription() == null) {
						description = "Nincs leiras.";
					} else {
						description = String.format("%s\n%s", event.getShortDescription(), event.getDescription());
						if (description.length() > 3000) {
							description = description.substring(0, 2999);
						}
					}
					
					insertFilmStatement.setString(2, description); // description
					insertFilmStatement.setString(3, JegyHuEvent.DISCRIMINATOR_VALUE); // discriminator
					
					// TODO: calculate many_shows dynamically
					insertFilmStatement.setInt(4, 1); // many _shows 
					insertFilmStatement.executeUpdate();
					generatedKeys = insertFilmStatement.getGeneratedKeys();
					if (generatedKeys.next()) {
						int eventId = generatedKeys.getInt(1); 
						insertShows(eventId, event.getShowList());
					} else {
						System.out.println("Szopo....");
					}
				}
			} finally {
				
				if (generatedKeys != null) {
					generatedKeys.close();
				}
				
				if (insertFilmStatement != null) {
					insertFilmStatement.close();
				}
				
				if (getNewIdStatement != null) {
					getNewIdStatement.close();
				}
				if (selectFilmResultSet != null) {
					selectFilmResultSet.close();
				}
				if (selectFilmStatement != null) {
					selectFilmStatement.close();
				}
			}
		}
		*/
		
		
		protected void updateShowJson(long eventId){
			ClientDbService dbService = null;
			try {
				dbService = ClientDbServiceCreator.createCloud();
				dbService.updateShowToShowJson(eventId);
				try {
					dbService.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		
		protected void insertShows(long eventId, List<Show> showList) throws SQLException {
			
			//super.insertShows(eventId, showList);
			PreparedStatement selectShowStatement = null;
			ResultSet selectShowResultSet = null;
			PreparedStatement selectDaysStatement = null;
			ResultSet selectDaysResultSet = null;
			PreparedStatement insertDaysStatement = null;
			
			PreparedStatement insertShowStatement = null;
			try {
				System.out.println("event ID = " + eventId);
				final String selectShowStr =
						"SELECT ID FROM TEST_SHOW WHERE GO = ? AND LOCATION = ? AND EVENT_ID = ?";
				
				selectShowStatement = conn.prepareStatement(selectShowStr);
				
				final String insertShowStr = 
						"INSERT INTO TEST_SHOW (GO, END, LOCATION, CATEGORY, EVENT_ID, COORDX, COORDY, DAY) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
				insertShowStatement = conn.prepareStatement(insertShowStr);
				
				for (Show show : showList) {
					
					// check if it is already in the database
					selectShowStatement.clearParameters();
					selectShowStatement.setLong(1, show.getStart().getTime());
					selectShowStatement.setString(2, show.getLocation());
					selectShowStatement.setLong(3, eventId);
					selectShowResultSet = selectShowStatement.executeQuery();
					if (selectShowResultSet.next()) {
						
						long showId = selectShowResultSet.getLong("id");
						System.out.println(
								String.format("show (id = %d) already in database, go = %d, location = %s, eventId = %d", 
										showId, show.getStart().getTime(), show.getLocation(), eventId));
						// already in the database, continue with the next show
						continue;
					}
					
					// not yet in the database
					
					// check if day was added
					final String selectDaysStr = 
							"SELECT * FROM TEST_DAYS WHERE EVENT_ID = ? AND DAY = ?";
					String dayStr = CloudEventUploader.DATE_FORMAT.format(show.getStart());
					selectDaysStatement = conn.prepareStatement(selectDaysStr);
					selectDaysStatement.setLong(1, eventId);
					selectDaysStatement.setString(2, dayStr);
					selectDaysResultSet = selectDaysStatement.executeQuery();
					if (!selectDaysResultSet.next()) {
						// need to add to the days table
						String insertDayStr =
								"INSERT INTO TEST_DAYS (DAY, EVENT_ID) VALUES (?, ?)";
						insertDaysStatement = conn.prepareStatement(insertDayStr);
						insertDaysStatement.setString(1, dayStr);
						insertDaysStatement.setLong(2, eventId);
						insertDaysStatement.executeUpdate();
					}
					
					insertShowStatement.setLong(1, show.getStart().getTime());
					if (show.getEnd() == null) {
						insertShowStatement.setNull(2, Types.DECIMAL);
					} else {
						insertShowStatement.setLong(2, show.getEnd().getTime());
					}

					insertShowStatement.setString(3, show.getLocation());
					insertShowStatement.setString(4, show.getCategory());
					insertShowStatement.setLong(5, eventId);

					MyLatLng latLng = show.getLocation() == null ? null : geocoder.geocode(show.getLocation());
					if (latLng != null) {
						insertShowStatement.setDouble(6, latLng.lat);
						insertShowStatement.setDouble(7, latLng.lon);
					} else {
						
						insertShowStatement.setNull(6, Types.FLOAT);
						insertShowStatement.setNull(7, Types.FLOAT);
					}
					insertShowStatement.setString(8, CloudEventUploader.DATE_FORMAT.format(show.getStart()));
					insertShowStatement.executeUpdate();
					insertShowStatement.clearParameters();
					
				}
				
			} finally {
				if (insertShowStatement != null) {
					insertShowStatement.close();
				}
				if (selectShowResultSet != null) {
					selectShowResultSet.close();
				}
				if (selectShowStatement != null) {
					selectShowStatement.close();
				}
				if (selectDaysResultSet != null) {
					selectDaysResultSet.close();
				}
				if (selectDaysStatement != null) {
					selectDaysStatement.close();
				}
				if (insertDaysStatement != null) {
					insertDaysStatement.close();
				}
				
			}
		}
	}



}
