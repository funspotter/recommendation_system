package com.incredibles.storage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.joda.time.DateTimeZone;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.incredibles.data.FacebookPageNumbers;
import com.incredibles.data.FunspotterEvent;
import com.incredibles.data.Like;
import com.incredibles.data.LogTableTypes;
import com.incredibles.data.Rec;
import com.incredibles.data.MetadataFromThirdParty;
import com.incredibles.data.FacebookPlaceTag;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import com.sun.xml.internal.ws.api.ha.StickyFeature;




/**
 * Simple implementation of {@link DbManager} wich download log from CloudDB
 */
class CloudDbManager implements RecommenderDbService {
	
	protected static final boolean DEBUG = true;
	
	protected static final int FIRST_N_ROW = Integer.MAX_VALUE; 
	protected static final int SHOW_LIMIT = Integer.MAX_VALUE;
	protected static final boolean USE_BASE64 = false;
	protected static final boolean GET_EVENTS_FROM_FILE = false;
	protected static final boolean GET_SHOWS_FROM_FILE = false;
	
	protected static class ShowColumn {
		static final String EVENT_ID = "event_id";
		static final String GO = "go";
		static final String END = "end";
		static final String LOCATION = "location";
		static final String X = "coordx";
		static final String Y = "coordy";
		public static final String CATEGORY = "category";
		public static final String IS_STUB = "is_stub";
		public static final String DAY = "day";
	}
	
	protected static class EventColumn {
		static final String ID = "id";
		static final String NAME = "name";
		static final String ORIGINAL_TITLE = "originaltitle";
		static final String GENRE = "genre";
		static final String LENGTH = "length";
		static final String RATING = "rating";
		static final String DIRECTOR = "director";
		static final String ACTORS = "actors";
		static final String YEAR = "year";
		static final String RELEASED_ON = "releasedon";
		static final String DESCRIPTION = "description";
		static final String CATEGORY = "category";
		static final String THUMBNAIL = "thumbnail";
		static final String IMAGE = "image";
		static final String DAY = "day";
		static final String DISCRIMINATOR = "discriminator";
		static final String MANY_SHOWS = "many_shows";
		static final String VIDEO = "video";
		static final String RANK = "rank";
	}
	
	protected static class UserColumn {
		static final String ID = "ID";
		static final String NAME = "NAME";
		static final String FACEBOOK_ID = "FACEBOOKID";
		static final String EMAIL = "EMAIL";
		static final String TAG = "TAG";
	}
	
	protected static class DaysColumn {
		static final String DAY = "day";
		static final String ALIAS_DAY = "the_day";
	}
	
	
	
	/**
	 * Encode a string with Base64
	 * 
	 * @param str
	 *            The string to be encoded
	 * 
	 * @return The Base64 encoded string
	 */
	protected static String toBase64(String str) {
		String ret = null;
		if (str != null) {
			ret = Base64.encode(str.getBytes());
		}
		return ret;
	}
	
	/** database connection */
	protected Connection conn;
	/** resultSet that contains the log */
	protected ResultSet logResultSet;

	/**
	 * Constructor
	 * 
	 * @param uploadRecData
	 *            If we are uploading recommendation data, and we want to
	 *            truncate the recommendation table
	 * @throws SQLException 
	 */
	public CloudDbManager() throws SQLException {	
		conn = createConnection();
	}
	
	protected Connection createConnection() throws SQLException {
		//?useUnicode=true&characterEncoding=utf8&characterSetResults=utf8
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
	public void close() throws IOException, SQLException {
		if (conn != null) {
			conn.close();
		} else {
			System.err.println(">> Tried to close the connection, but it was null.");
		}
	}

	
	/*-----------------------------new db functions------------------------------*/
	
	
	/*Returns true if has next line, and loadup LogLine object with data*/
	public boolean getNextLogLineV2(LogLine line) throws SQLException {	
		boolean ret = false;	/*default false*/
		Statement getLogStatement = null;
		try {
			System.out.println("try dbmanager");
			if (logResultSet == null) {
				String selectQuery = "SELECT id,type,createdAt,EventId,UserId FROM EventLogs";
				getLogStatement = conn.createStatement();
				logResultSet = getLogStatement.executeQuery(selectQuery);
				System.out.println("LogResultSet OK (next logline func.)");
			}
			if(logResultSet.next()){/*logResSet not null; info from server recived*/
				ret = true;	/*returns true if have netx line*/
				line.userId = logResultSet.getInt("UserId");
				line.eventId = logResultSet.getInt("EventId");
				line.timeStamp = new Date(logResultSet.getLong("createdAt"));
				line.type = logResultSet.getInt("type");
			}
		} finally {
			// if there are no more log lines, we close the logResultSet
			if (ret == false) {
				if (logResultSet != null) {
					logResultSet.close();
				}
				if (getLogStatement != null) {
					getLogStatement.close();
				}
			}
		}
		return ret;
	}

	/*Returns all events id in a string list*/
	public List<Integer> getAllEventsId(){
		PreparedStatement getEventStatement = null;
		ResultSet eventResult = null;
		List<Integer> eventList = new ArrayList<Integer>();
		String queryStr = "SELECT id FROM Events WHERE 1";
		try {
			getEventStatement = conn.prepareStatement(queryStr);
			eventResult = getEventStatement.executeQuery();
			while(eventResult.next()){
				Integer eventID = eventResult.getInt("id");
				eventList.add(eventID);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if (eventResult != null)  {
				try {
					eventResult.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (getEventStatement != null) {
				try {
					getEventStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return eventList;
	}
	
	/*Returns events id which have future show 
	 * - uses two Date time interval and convert these into integer dates*/
	public List<Integer> getLegitEventsIdV2(Date from, Date to){
		PreparedStatement getEventStatement = null;
		ResultSet eventResult = null;
		List<Integer> eventList = new ArrayList<Integer>();
		Integer intDateFrom = getIntegerDate(from.getTime());
		Integer intDateTo = getIntegerDate(to.getTime());
		/*kula*/
		String queryStr = "SELECT id FROM Events WHERE ((isOk=1 OR isOk=2) AND (SELECT EventId FROM EventDays WHERE EventDays.day > ? AND EventDays.day < ? AND Events.id=EventId LIMIT 1))";
		try {
			getEventStatement = conn.prepareStatement(queryStr);
			getEventStatement.setInt(1, intDateFrom);
			getEventStatement.setInt(2, intDateTo);
			eventResult = getEventStatement.executeQuery();
			while(eventResult.next()){
				Integer eventID = eventResult.getInt("id");
				eventList.add(eventID);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if (eventResult != null)  {
				try {
					eventResult.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (getEventStatement != null) {
				try {
					getEventStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return eventList;
	}
	
	/*Returns legit events happening on the predefined DATE*/
	public List<Integer> getLegitEventsOnDate(Date date){
		PreparedStatement getEventStatement = null;
		ResultSet eventResult = null;
		Integer intDate = getIntegerDate(date.getTime());
		List<Integer> eventList = new ArrayList<Integer>();
		/*kula*/
		String queryStr = "SELECT id FROM Events WHERE ((isOk=1 OR isOk=2) AND (SELECT EventId FROM EventDays WHERE EventDays.day = ? AND Events.id=EventId LIMIT 1))";
		try {
			getEventStatement = conn.prepareStatement(queryStr);
			getEventStatement.setInt(1, intDate);
			eventResult = getEventStatement.executeQuery();
			while(eventResult.next()){
				Integer eventID = eventResult.getInt("id");
				eventList.add(eventID);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if (eventResult != null)  {
				try {
					eventResult.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (getEventStatement != null) {
				try {
					getEventStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return eventList;
	}

	/*Returns legit events in int List.*/
	public List<Integer> getLegitEventsIdFromDate(long from){
		List<Integer> eventIdList = new ArrayList<Integer>();
		Integer intDate = getIntegerDate(from);		
		PreparedStatement getEventStatement = null;
		ResultSet eventResults = null;
		try {				
			/*kula*/
			String queryStr = "SELECT id FROM Events WHERE ((isOk=1 OR isOk=2) AND (SELECT EventId FROM EventDays WHERE EventDays.day >= ? AND Events.id = EventId LIMIT 1))";
			try {
				getEventStatement = conn.prepareStatement(queryStr);
				getEventStatement.setInt(1, intDate);
				eventResults = getEventStatement.executeQuery();
				while(eventResults.next()){
					Integer ot = eventResults.getInt("id");	
					if(!eventIdList.contains(ot) && ot != 0){	// because of the 2030 event
						eventIdList.add(ot);
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} finally {
			if (eventResults != null)  {
				try {
					eventResults.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (getEventStatement != null) {
				try {
					getEventStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return eventIdList;
	}
	
	/*Get legit events with isIn=0 flag. In the maintanence class call this first, before uploadIsinFlagForEvents() !!!*/
	public List<Integer> getLegitNotinEventsIdFromDate(long from){
		List<Integer> eventIdList = new ArrayList<Integer>();
		Integer intDate = getIntegerDate(from);		
		PreparedStatement getEventStatement = null;
		ResultSet eventResults = null;
		/*kula*/
		String queryStr = "SELECT id FROM Events WHERE ((isOk=1 OR isOk=2) AND isIn=0 AND (SELECT EventId FROM EventDays WHERE EventDays.day >= ? AND Events.id = EventId LIMIT 1))";
		try {
			getEventStatement = conn.prepareStatement(queryStr);
			getEventStatement.setInt(1, intDate);
			eventResults = getEventStatement.executeQuery();
			while(eventResults.next()){
				Integer ot = eventResults.getInt("id");	
				if(!eventIdList.contains(ot) && ot != 0){	// because of the 2030 event
					eventIdList.add(ot);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (eventResults != null)  {
				try {
					eventResults.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (getEventStatement != null) {
				try {
					getEventStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return eventIdList;
	}
	
	/*Returns all future event but unchecked (isOk =0) events funspotterID*/
	public List<Integer> getAllFutureUncheckedEventId(){
		List<Integer> eventIds = new ArrayList<Integer>();
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Timestamp ts = new Timestamp(utc);
		Integer IntDate = getIntegerDate(utc);
		PreparedStatement statement = null;
		ResultSet result = null;
		String query = "SELECT id FROM Events WHERE isOk=0 AND (SELECT EventId FROM EventDays WHERE EventDays.day >= ? AND Events.id=EventId LIMIT 1)";
		try {
			statement = conn.prepareStatement(query);
			statement.setInt(1, IntDate);
			result = statement.executeQuery();
			while(result.next()){
				Integer FunspotterId = result.getInt("id");
				eventIds.add(FunspotterId);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (result != null)  {
				try {
					result.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return eventIds;
	}
	
	/*Returns all events in hashmap with ISIn value.*/
	public HashMap<Integer, Boolean> getAllEventsWithIsinFlag(){
		PreparedStatement getEventStatement = null;
		ResultSet eventResult = null;
		HashMap<Integer, Boolean> eventIdIsinHm = new HashMap<Integer, Boolean>();
		String queryStr = "SELECT id, isIn FROM Events WHERE 1";
		try {
			getEventStatement = conn.prepareStatement(queryStr);
			eventResult = getEventStatement.executeQuery();
			while(eventResult.next()){
				Integer eventID = eventResult.getInt("id");
				Boolean isIn = eventResult.getBoolean("isIn");
				eventIdIsinHm.put(eventID, isIn);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if (eventResult != null)  {
				try {
					eventResult.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (getEventStatement != null) {
				try {
					getEventStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return eventIdIsinHm;
	}
	
	/*Returns legit events click number from EventLogs Table*/
	public HashMap<Integer, Integer> countLegitEventsClick(){
		PreparedStatement logStatement = null;
		ResultSet resultSet = null;
		HashMap<Integer, Integer> clickNum = new HashMap<Integer, Integer>();
		Date nowDate = new Date();
		List<Integer> legitEvents = getLegitEventsIdFromDate(nowDate.getTime());
		for(int i=0; i<legitEvents.size(); i++){
			Integer EventId = legitEvents.get(i);
			clickNum.put(EventId, 0);
		}
		String queryStr = "SELECT EventId FROM EventLogs WHERE type = ?";
		try {
			logStatement = conn.prepareStatement(queryStr);
			logStatement.setInt(1, LogTableTypes.eventClick);
			resultSet = logStatement.executeQuery();
			while(resultSet.next()){
				Integer EventId = resultSet.getInt("EventId");
				if(legitEvents.contains(EventId)){
					if(clickNum.containsKey(EventId)){
						int click = clickNum.get(EventId);
						clickNum.put(EventId, click+1);
					}else{
						clickNum.put(EventId, 1);
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (resultSet != null)  {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (logStatement != null) {
				try {
					logStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return clickNum;
	}
	
	/*Returns a hasmap with event-rank key;value for one user*/
	public HashMap<Integer,Double> getRankforEventV2(Integer userid) throws SQLException{	
		HashMap<Integer,Double> s = new HashMap<Integer,Double>();
		PreparedStatement getShowsStatement = null;
		ResultSet showResults = null;
		try {		
			String queryStr = "SELECT EventId,rank FROM UserEventRecommendations WHERE UserId=?";
			getShowsStatement = conn.prepareStatement(queryStr);
			getShowsStatement.setInt(1, userid);
			showResults = getShowsStatement.executeQuery();		
			while (showResults.next()) {
				Double rank = showResults.getDouble("rank");
				Integer id = showResults.getInt("EventId");
				s.put(id, rank);
			}		
		} finally {
			if (showResults != null)  {
				showResults.close();
			}
			if (getShowsStatement != null) {
				getShowsStatement.close();
			}
		}	
		return s;
	}
	
	/*Returns list of hasmaps. 0. - eventName; 1- eventDesc.*/
	public List<HashMap<Integer,String>> getLegitEventDescAndNameFromDate(long from) throws SQLException{
		HashMap<Integer,String> eventDesc = new HashMap<Integer,String>();
		HashMap<Integer,String> eventName = new HashMap<Integer,String>();
		Integer intDateFrom = getIntegerDate(from);
		List<HashMap<Integer,String>> hashmapList = null;
		PreparedStatement getEventStatement = null;
		ResultSet eventResults = null;	
		try {		
			/*kula*/
			String queryStr = "SELECT id,title,description FROM Events WHERE ((isOk=1 OR isOk=2) AND (SELECT EventId FROM EventDays WHERE EventDays.day >= ? AND Events.id=EventId LIMIT 1))";
			getEventStatement = conn.prepareStatement(queryStr);
			getEventStatement.setInt(1, intDateFrom);
			eventResults = getEventStatement.executeQuery();
			while(eventResults.next()){
				Integer ot = eventResults.getInt("id");	
				String description = eventResults.getString("description");
				String name = eventResults.getString("title");
				if(!eventDesc.containsKey(ot) && ot != 0){	// because of the 2030 event
					eventDesc.put(ot, description);
				}
				if(!eventName.containsKey(ot) && ot != 0){
					eventName.put(ot, name);
				}
			}
			hashmapList = new ArrayList<HashMap<Integer,String>>();
			hashmapList.add(0, eventName);
			hashmapList.add(1, eventDesc);		
		} finally {
			if (eventResults != null)  {
				eventResults.close();
			}
			if (getEventStatement != null) {
				getEventStatement.close();
			}
		}
		return hashmapList;
	}

	/*Returns all event description in hm (eventid - description string)*/	//make one for date
	public HashMap<Integer,String> getAllEventDescV2() throws SQLException{
		HashMap<Integer,String> s = new HashMap<Integer,String>();
		PreparedStatement getEventStatement = null;
		ResultSet eventResults = null;	
		try {
			String queryStr = "SELECT id,description FROM Events";
			getEventStatement = conn.prepareStatement(queryStr);
			eventResults = getEventStatement.executeQuery();		
			while (eventResults.next()) {
				String description = eventResults.getString("description");
				Integer id = eventResults.getInt("id");
				s.put(id, description);
			}	
		} finally {
			if (eventResults != null)  {
				eventResults.close();
			}
			if (getEventStatement != null) {
				getEventStatement.close();
			}
		}	
		return s;
	}
	
	/*Returns events description from date. Uses converted long number for date*/
	public HashMap<Integer,String> getEventDescriptionFromDateV2(long fromDate) throws SQLException{
		HashMap<Integer,String> s = new HashMap<Integer,String>();
		Integer date = getIntegerDate(fromDate);
		PreparedStatement getEventStatement = null;
		ResultSet eventResults = null;		
		try {
			/*kula*/
			String queryStr = "SELECT id,description FROM Events WHERE ((isOk=1 OR isOk=2) AND (SELECT EventId FROM EventDays WHERE day >= ? AND Events.id=EventId LIMIT 1))";
			getEventStatement = conn.prepareStatement(queryStr);
			getEventStatement.setInt(1, date);
			eventResults = getEventStatement.executeQuery();		
			while (eventResults.next()) {
				String description = eventResults.getString("description");
				Integer id = eventResults.getInt("id");
				s.put(id, description);
			}	
		} finally {
			if (eventResults != null)  {
				eventResults.close();
			}
			if (getEventStatement != null) {
				getEventStatement.close();
			}
		}	
		return s;
	}
	
	/*Returns all event discriminator in a hasmap*/ //make one for date
	public HashMap<Integer,String> getAllEventDiscV2() throws SQLException{
		HashMap<Integer,String> s = new HashMap<Integer,String>();
		PreparedStatement getEventStatement = null;
		ResultSet eventResults = null;	
		try {
			String queryStr = "SELECT id,discriminator FROM Events";
			getEventStatement = conn.prepareStatement(queryStr);
			eventResults = getEventStatement.executeQuery();		
			while (eventResults.next()) {
				String discriminator = eventResults.getString("discriminator");
				Integer id = eventResults.getInt("id");
				s.put(id, discriminator);
			}	
		} finally {
			if (eventResults != null)  {
				eventResults.close();
			}
			if (getEventStatement != null) {
				getEventStatement.close();
			}
		}	
		return s;
	}
	
	/*Return events discriminator FROM DATE in HM (eventid - discriminator)*/
	public HashMap<Integer,String> getEventDiscriminatorFromDateV2(long fromDate) throws SQLException{
		HashMap<Integer,String> s = new HashMap<Integer,String>();
		Integer date = getIntegerDate(fromDate);
		PreparedStatement getEventStatement = null;
		ResultSet eventResults = null;		
		try {
			/*kula*/
			String queryStr = "SELECT id,discriminator FROM Events WHERE ((isOk=1 OR isOk=2) AND (SELECT EventId FROM EventDays WHERE day >= ? AND Events.id=EventId LIMIT 1))";
			getEventStatement = conn.prepareStatement(queryStr);
			getEventStatement.setInt(1, date);
			eventResults = getEventStatement.executeQuery();		
			while (eventResults.next()) {
				String discriminator = eventResults.getString("discriminator").toLowerCase();
				Integer id = eventResults.getInt("id");
				s.put(id, discriminator);
			}	
		} finally {
			if (eventResults != null)  {
				eventResults.close();
			}
			if (getEventStatement != null) {
				getEventStatement.close();
			}
		}	
		return s;
	}
	
	/*Return specified event information in Event object*/
	public HashMap<Integer, FunspotterEvent> getFutureEventsInformation(){
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Integer Date = getIntegerDate(utc);
		HashMap<Integer, FunspotterEvent> eventInfo = new HashMap<Integer, FunspotterEvent>();
		String query = "SELECT id,title,description,thumbnailUrl,imageUrlsJson,discriminator,origin,isOk,isIn FROM Events WHERE (SELECT EventId FROM EventDays WHERE Events.id = EventId AND day >= ? LIMIT 1)";
		try {
			statement = conn.prepareStatement(query);
			statement.setInt(1, Date);
			resultSet = statement.executeQuery();
			while(resultSet.next()){
				Integer funspotterId = resultSet.getInt("id");
				String title = resultSet.getString("title");
				String description = resultSet.getString("description");
				String discriminator = resultSet.getString("discriminator");
				String origin = resultSet.getString("origin");
				String thumbnailUrl = resultSet.getString("thumbnailUrl");
				String imageUrlsJson = resultSet.getString("imageUrlsJson");
				Integer isOk = resultSet.getInt("isOk");
				Integer isIn = resultSet.getInt("isIn");
				if(!funspotterId.equals(0)){
					FunspotterEvent event = new FunspotterEvent();
					event.setFunspotterId(funspotterId);
					event.setTitle(title);
					event.setDescription(description);
					event.setThumbnailUrl(thumbnailUrl);
					event.setImageUrlsJson(imageUrlsJson);
					event.setDiscriminator(discriminator);
					event.setOrigin(origin);
					event.setIsOk(isOk);
					event.setIsIn(isIn);
					eventInfo.put(funspotterId, event);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (resultSet != null)  {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return eventInfo;
	}
	
	/*Update events discriminator and isOk value, handle the noinfofromfacebook case too*/
	public void updateEventsDiscriminator(HashMap<Integer, String> categorizedEvents, List<Integer>noInfoFromFacebook){
		PreparedStatement updateStatement = null;
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Timestamp ts = new Timestamp(utc);
		String queryStrUpdate = "UPDATE Events SET isOk = ?, updatedAt = ?, discriminator = ? WHERE id = ?";
		try {
			updateStatement = conn.prepareStatement(queryStrUpdate);
			if(categorizedEvents != null){
				for(Entry<Integer, String>entry: categorizedEvents.entrySet()){
					Integer funspotterId = entry.getKey();
					String discriminator  = entry.getValue();
					updateStatement.setInt(1, 1);	// 5 means categorized with facebook categorizer
					updateStatement.setTimestamp(2, ts);
					updateStatement.setString(3, discriminator);
					updateStatement.setInt(4, funspotterId);
					updateStatement.addBatch();
				}
			}
			if(noInfoFromFacebook!=null){
				for(int i=0; i<noInfoFromFacebook.size(); i++){
					Integer funspotterId = noInfoFromFacebook.get(i);
					updateStatement.setInt(1, 0);	// no info from categorization means: 4
					updateStatement.setTimestamp(2, ts);
					updateStatement.setString(3, "simple");
					updateStatement.setInt(4, funspotterId);
					updateStatement.addBatch();
				}
			}
			int [] numUpdates1=updateStatement.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (updateStatement != null) {
				try {
					updateStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/*Returns uncategorized facebook events (facebook and funspotter) id from facebook*/
	public HashMap<Long, Integer> getUncategorizedFutureFacebookEvents(){
		HashMap<Long, Integer> FaceAndFunspotterEventId = new HashMap<Long, Integer>();
		List<Integer> futureUncheckedEvents = getAllFutureUncheckedEventId();
		PreparedStatement statement= null;
		ResultSet eventResults = null;
		try {
			String queryStr = "SELECT facebookId, EventId from EventFromFacebook WHERE (SELECT id FROM Events WHERE EventFromFacebook.EventId = id AND isOk = 0)";
			statement = conn.prepareStatement(queryStr);
			eventResults = statement.executeQuery();
			while(eventResults.next()){
				Long FacebookId = eventResults.getLong("facebookId");
				Integer FunspotterId = eventResults.getInt("EventId");
				if(futureUncheckedEvents.contains(FunspotterId)){	// is this a future event?
					if(FacebookId!=null && !FacebookId.equals(0L)){
						if(FunspotterId!=null && !FunspotterId.equals(0)){
							FaceAndFunspotterEventId.put(FacebookId, FunspotterId);
						}
					}
				}	
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (eventResults != null)  {
				try {
					eventResults.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	
		return FaceAndFunspotterEventId;
	}
	
	/*Returns uncategorized facebook events (facebook and funspotter) id from facebook*/
	public HashMap<Integer, Long> getCategorizedFutureFacebookEvents(){
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		HashMap<Integer, Long> FaceAndFunspotterEventId = new HashMap<Integer, Long>();
		List<Integer> futureCheckedEvents = getLegitEventsIdFromDate(utc);
		PreparedStatement statement= null;
		ResultSet eventResults = null;
		try {
			String queryStr = "SELECT facebookId, EventId from EventFromFacebook WHERE (SELECT id FROM Events WHERE EventFromFacebook.EventId = id AND (isOk = 1 OR isOk = 2))";
			statement = conn.prepareStatement(queryStr);
			eventResults = statement.executeQuery();
			while(eventResults.next()){
				Long FacebookId = eventResults.getLong("facebookId");
				Integer FunspotterId = eventResults.getInt("EventId");
				if(futureCheckedEvents.contains(FunspotterId)){	// is this a future event?
					if(FacebookId!=null && !FacebookId.equals(0L)){
						if(FunspotterId!=null && !FunspotterId.equals(0)){
							FaceAndFunspotterEventId.put(FunspotterId,FacebookId);
						}
					}
				}	
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (eventResults != null)  {
				try {
					eventResults.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	
		return FaceAndFunspotterEventId;
	}
	
	/*Returns all future facebook events (facebook and funspotter) id from facebook*/
	public HashMap<Long, Integer> getAllFutureFacebookEvents(){
		HashMap<Long, Integer> FaceAndFunspotterEventId = new HashMap<Long, Integer>();
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Integer Date = getIntegerDate(utc);
		PreparedStatement statement= null;
		ResultSet eventResults = null;
		try {
			String queryStr2 = "SELECT facebookId, EventId from EventFromFacebook WHERE (SELECT EventId FROM EventDays WHERE EventFromFacebook.EventId=EventId AND day >= ? LIMIT 1)";
			statement = conn.prepareStatement(queryStr2);
			statement.setInt(1, Date);
			eventResults = statement.executeQuery();
			while(eventResults.next()){
				Long FacebookId = eventResults.getLong("facebookId");
				Integer FunspotterId = eventResults.getInt("EventId");
				if(FacebookId!=null && !FacebookId.equals(0L)){
					if(FunspotterId!=null && !FunspotterId.equals(0)){
						FaceAndFunspotterEventId.put(FacebookId, FunspotterId);
					}
				}	
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (eventResults != null)  {
				try {
					eventResults.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	
		return FaceAndFunspotterEventId;
	}
	
	/*Returns ALL facebook events (facebook and funspotter) id from facebook*/
	public HashMap<Long, Integer> getAllFacebookEvents(){
		HashMap<Long, Integer> FaceAndFunspotterEventId = new HashMap<Long, Integer>();
		PreparedStatement statement= null;
		ResultSet eventResults = null;
		try {
			String queryStr2 = "SELECT facebookId, EventId from EventFromFacebook WHERE 1";
			statement = conn.prepareStatement(queryStr2);
			eventResults = statement.executeQuery();
			while(eventResults.next()){
				Long FacebookId = eventResults.getLong("facebookId");
				Integer FunspotterId = eventResults.getInt("EventId");
				if(FacebookId!=null && !FacebookId.equals(0L)){
					if(FunspotterId!=null && !FunspotterId.equals(0)){
						FaceAndFunspotterEventId.put(FacebookId, FunspotterId);
					}
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (eventResults != null)  {
				try {
					eventResults.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	
		return FaceAndFunspotterEventId;
	}
	
	/*Returns all facebook events funspotter id from EventFromFacebook table in List<Integer>*/
	public List<Integer> getAllFacebookEventFunspotterId(){
		List<Integer> allFacebookEventFunspotterId = new ArrayList<Integer>();
		PreparedStatement statement= null;
		ResultSet eventResults = null;
		String getQuery = "SELECT EventId FROM EventFromFacebook WHERE 1";
		try {
			statement = conn.prepareStatement(getQuery);
			eventResults = statement.executeQuery();
			while(eventResults.next()){
				Integer FunspotterId = eventResults.getInt("EventId");
				allFacebookEventFunspotterId.add(FunspotterId);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (eventResults != null)  {
				try {
					eventResults.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return allFacebookEventFunspotterId;
	}
	
	/*Returns all PageId from FacebookPages table*/
	public List<Long> getAllFacebookPageId(){
		List<Long> pageIdArray = new ArrayList<Long>();
		PreparedStatement statement = null;
		String query = "SELECT facebookPageId FROM FacebookPages WHERE 1";
		ResultSet result = null;
		try {
			statement = conn.prepareStatement(query);
			result = statement.executeQuery();
			while(result.next()){
				Long FacebookPageId = result.getLong("facebookPageId");
				pageIdArray.add(FacebookPageId);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (result != null)  {
				try {
					result.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return pageIdArray;
	}
	
	/*Returns all facebook events info, need more specified sql query based on time interval.*/
	public HashMap<Integer, Long> getFacebookEventsPageInformation(){
		HashMap<Integer, Long> EventsPageInfo = new HashMap<Integer, Long>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String query = "SELECT EventFromFacebook.EventId, FacebookPages.likeCount, FacebookPages.checkinCount FROM EventFromFacebook INNER JOIN FacebookPages ON (EventFromFacebook.FacebookPageId=FacebookPages.facebookPageID)";
		try {
			statement = conn.prepareStatement(query);
			resultSet = statement.executeQuery();
			while(resultSet.next()){
				Integer EventId = resultSet.getInt("EventId");
				Long likeCount = resultSet.getLong("likeCount");
				Long checkinCount = resultSet.getLong("checkinCount");
				Long sumNum = likeCount + checkinCount;
				EventsPageInfo.put(EventId, sumNum);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (resultSet != null)  {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return EventsPageInfo;
	}
	
	/*Update FacebookPages and EventFromFacebook table, with hashmap<pageId; pageinfo class>*/
	public void updateFacebookPageInformation(HashMap<Long, FacebookPageNumbers> pageInfos){
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Timestamp ts = new Timestamp(utc);
		PreparedStatement statement = null;
		List<Long> allPageId = getAllFacebookPageId();
		List<Long> updatePageId = new ArrayList<Long>();
		List<Long> insertPageId = new ArrayList<Long>();
		for(Entry<Long, FacebookPageNumbers>entry: pageInfos.entrySet()){
			Long pageId = entry.getKey();
			if(allPageId.contains(pageId)){
				updatePageId.add(pageId);
			}else{
				insertPageId.add(pageId);
			}
		}
		String updateQuery = "UPDATE FacebookPages SET updatedAt = ?, likeCount = ?, checkinCount = ? WHERE facebookPageId = ?";
		String insertQuery = "INSERT INTO FacebookPages (createdAt, updatedAt, facebookPageId, likeCount, checkinCount) VALUES (?,?,?,?,?)";
		String updateEventFromFacebook = "UPDATE EventFromFacebook SET FacebookPageId = ?, updatedAt = ? WHERE EventId = ?";
		/*Insert new line into FacebookPages*/
		if(!insertPageId.isEmpty()){
			try {
				statement = conn.prepareStatement(insertQuery);
				for(int i=0; i<insertPageId.size(); i++){
					Long pageId = insertPageId.get(i);
					FacebookPageNumbers OnePageInfo = pageInfos.get(pageId);
					statement.setTimestamp(1, ts);
					statement.setTimestamp(2, ts);
					statement.setLong(3, OnePageInfo.getPageId());
					statement.setLong(4, OnePageInfo.getLikeNumber());
					statement.setLong(5, OnePageInfo.getCheckinNumberLong());
					statement.addBatch();
				}
				int[] num = statement.executeBatch();
			}catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		/*Update old line in FacebookPages*/
		if(!updatePageId.isEmpty()){
			try {
				statement = conn.prepareStatement(updateQuery);
				for(int i=0; i<updatePageId.size(); i++){
					Long pageId = updatePageId.get(i);
					FacebookPageNumbers OnePageInfo = pageInfos.get(pageId);
					statement.setTimestamp(1, ts);
					statement.setLong(2, OnePageInfo.getLikeNumber());
					statement.setLong(3, OnePageInfo.getCheckinNumberLong());
					statement.setLong(4, pageId);
					statement.addBatch();
				}
				int[] num = statement.executeBatch(); 
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		/*Update EventFromFacebook FacebookPageId*/
		utc = zone.convertLocalToUTC(local.getTime(), false);
		ts = new Timestamp(utc);
		try {
			statement = conn.prepareStatement(updateEventFromFacebook);
			for(Entry<Long, FacebookPageNumbers>entry: pageInfos.entrySet()){
				Long pageId = entry.getKey();
				FacebookPageNumbers OnePageInfo = entry.getValue();
				Integer FunspotterId = OnePageInfo.getFunspotterEventId();
				statement.setLong(1, pageId);
				statement.setTimestamp(2, ts);
				statement.setInt(3, FunspotterId);
				statement.addBatch();
			}
			int [] num = statement.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/*Returns checkin places name for one user. In the list one place can be not just one time*/
	public List<String> getUserCheckin(Integer UserId){
		List<String> checkinData = new ArrayList<String>();
		PreparedStatement getCheckinStatement = null;
		ResultSet checkinResults = null;
		String queryStr = "SELECT id, placeName FROM FacebookCheckins WHERE (SELECT UserId FROM FacebookCheckinsUsers WHERE UserId = ? AND FacebookCheckinsUsers.FacebookCheckinId = id LIMIT 1)";
		try {
			getCheckinStatement = conn.prepareStatement(queryStr);
			getCheckinStatement.setInt(1, UserId);
			checkinResults = getCheckinStatement.executeQuery();
			while(checkinResults.next()){
				Integer checkinId = checkinResults.getInt("id");
				String checkinLocationName = checkinResults.getString("placeName");
				System.out.println(checkinId+" neve "+ checkinLocationName);
				checkinData.add(checkinLocationName);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (checkinResults != null)  {
				try {
					checkinResults.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (getCheckinStatement != null) {
				try {
					getCheckinStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	
		return checkinData;
	}
	
	/*Returns events placenames*/
	public HashMap<Integer, List<String>> getLegitEventsPlaceName(){
		HashMap<Integer, List<String>> placeNames = new HashMap<Integer, List<String>>();
		PreparedStatement eventStatement = null;
		ResultSet resultSet = null;
		String eventQuery = "SELECT Shows.EventId, Locations.name FROM Shows AS Shows, Locations AS Locations WHERE Shows.LocationId = Locations.id AND (SELECT id FROM Events WHERE id = Shows.EventId AND isIn=1)";
		try {
			eventStatement = conn.prepareStatement(eventQuery);
			resultSet = eventStatement.executeQuery();		
			while (resultSet.next()) {
				Integer EventId = resultSet.getInt("EventId");
				String name = resultSet.getString("name");
				if(placeNames.containsKey(EventId)){
					List<String> places = placeNames.get(EventId);
					places.add(name);
					placeNames.put(EventId, places);
				}else{
					List<String> names = new ArrayList<String>();
					names.add(name);
					placeNames.put(EventId, names);
				}
			}		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (resultSet != null)  {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (eventStatement != null) {
				try {
					eventStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return placeNames;
	}
	
	/*Upload isin flag in Events table.*/
	public void updateIsinFlagForEvents(List<Integer> legitEventsIdFromDate){
		PreparedStatement updateStatement = null;
		/*kula*/
		HashMap<Integer, Boolean> AllEventsWithIsinFlag = getAllEventsWithIsinFlag();
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Timestamp ts = new Timestamp(utc);
		String queryStrUpdate = "UPDATE Events SET isIn = ?, updatedAt=? WHERE id = ?";
		try{
			updateStatement = conn.prepareStatement(queryStrUpdate);
			for(Entry<Integer, Boolean>entry: AllEventsWithIsinFlag.entrySet()){
				Integer EventId = entry.getKey();
				Boolean isinFlag = entry.getValue();
				if(legitEventsIdFromDate.contains(EventId)){
					if(isinFlag==true){
						//dont do anything
					}else{	// if event is legit(future event, and isok=1,2), but isin = false, set isin=true
						updateStatement.setInt(1, 1);
						updateStatement.setTimestamp(2, ts);
						updateStatement.setInt(3, EventId);
						updateStatement.addBatch();
					}
				}else{
					if(isinFlag==false){
						//dont do anything
					}else{ //if event isnt legit (not future and/or isok=0;3) but isin = true
						updateStatement.setInt(1, 0);
						updateStatement.setTimestamp(2, ts);
						updateStatement.setInt(3, EventId);
						updateStatement.addBatch();
					}
				}
			}
			int [] numUpdates1=updateStatement.executeBatch();
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (updateStatement != null) {
				try {
					updateStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
		
	/*Returns userid list, who has record in the UserDiscriminatorsRanks table*/
	public List<Integer> whoHasDiscRankInTable(){
		List<Integer> usersList = new ArrayList<Integer>();
		PreparedStatement getUserStatement = null;
		ResultSet eventResults = null;
		String queryStr = "SELECT UserId FROM UserDiscriminatorsRanks WHERE 1";
		try {
			getUserStatement = conn.prepareStatement(queryStr);
			eventResults = getUserStatement.executeQuery();
			while (eventResults.next()) {
				Integer UserId = eventResults.getInt("UserId");
				usersList.add(UserId);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (eventResults != null)  {
				try {
					eventResults.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (getUserStatement != null) {
				try {
					getUserStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return usersList;
	}
	
	/*Upload/Update user/users discriminator rank values*/
	public void uploadUserDiscriminatorRank(HashMap<Integer, HashMap<String, Double>> discriminatorRank){
		PreparedStatement insertStatement = null;
		PreparedStatement updateStatement = null;
		boolean insert = false;
		boolean update = false;
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Timestamp ts = new Timestamp(utc);
		List<Integer> UserIds = whoHasDiscRankInTable();
		String queryStrInsert = "INSERT INTO UserDiscriminatorsRanks SET cinema=?, theater=?, festival=?, music=?, exhibition=?, kid=?, gastro=?, travel=?, sport=?, party=?, other=?, updatedAt=?, createdAt=?, UserId=?";
		try{
			insertStatement = conn.prepareStatement(queryStrInsert);
			for(Entry<Integer, HashMap<String, Double>> entry: discriminatorRank.entrySet()){
				Integer UserId = entry.getKey();
				HashMap<String, Double> discRanks = entry.getValue();
				if(!UserIds.contains(UserId)){
					insert=true;
					insertStatement.setDouble(1, discRanks.get("cinema"));
					insertStatement.setDouble(2, discRanks.get("theater"));
					insertStatement.setDouble(3, discRanks.get("festival"));
					insertStatement.setDouble(4, discRanks.get("music"));
					insertStatement.setDouble(5, discRanks.get("exhibition"));
					insertStatement.setDouble(6, discRanks.get("kid"));
					insertStatement.setDouble(7, discRanks.get("gastro"));
					insertStatement.setDouble(8, discRanks.get("travel"));
					insertStatement.setDouble(9, discRanks.get("sport"));
					insertStatement.setDouble(10, discRanks.get("party"));
					insertStatement.setDouble(11, discRanks.get("other"));
					insertStatement.setTimestamp(12, ts);
					insertStatement.setTimestamp(13, ts);
					insertStatement.setInt(14, UserId);
					insertStatement.addBatch();
				}
			}
			if(insert==true){
				int [] numUpdates1=insertStatement.executeBatch();
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (insertStatement != null) {
				try {
					insertStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		String queryStr = "UPDATE UserDiscriminatorsRanks SET cinema=?, theater=?, festival=?, music=?, exhibition=?, kid=?, gastro=?, travel=?, sport=?, party=?, other=?, updatedAt=? WHERE UserId=?";
		try{
			updateStatement = conn.prepareStatement(queryStr);
			for(Entry<Integer, HashMap<String, Double>> entry: discriminatorRank.entrySet()){
				Integer UserId = entry.getKey();
				HashMap<String, Double> discRanks = entry.getValue();
				if(UserIds.contains(UserId)){
					update=true;
					updateStatement.setDouble(1, discRanks.get("cinema"));
					updateStatement.setDouble(2, discRanks.get("theater"));
					updateStatement.setDouble(3, discRanks.get("festival"));
					updateStatement.setDouble(4, discRanks.get("music"));
					updateStatement.setDouble(5, discRanks.get("exhibition"));
					updateStatement.setDouble(6, discRanks.get("kid"));
					updateStatement.setDouble(7, discRanks.get("gastro"));
					updateStatement.setDouble(8, discRanks.get("travel"));
					updateStatement.setDouble(9, discRanks.get("sport"));
					updateStatement.setDouble(10, discRanks.get("party"));
					updateStatement.setDouble(11, discRanks.get("other"));
					updateStatement.setTimestamp(12, ts);
					updateStatement.setInt(13, UserId);
					updateStatement.addBatch();
				}
			}
			if(update==true){
				int [] numUpdates1=updateStatement.executeBatch();
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (updateStatement != null) {
				try {
					updateStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/*Returns for ONE or ALL USER their discriminator ranks. For one user onlyoneuser = 1.
	 * Doesnt return the 0. user disc. ranks*/
	public HashMap<Integer, HashMap<String, Double>> getUserDiscriminatorRank(int UserId, boolean onlyOneUser){
		HashMap<Integer, HashMap<String, Double>> userDiscriminatorRank = new HashMap<Integer, HashMap<String, Double>>();
		HashMap<String, Double> discRank = null;
		PreparedStatement userStatement = null;
		ResultSet resultSet = null;
		if(onlyOneUser){
			String queryStrSelect = "SELECT cinema, theater, festival, music, exhibition, kid, gastro, travel, sport, party, other FROM UserDiscriminatorsRanks WHERE UserId = ?";
			try {
				userStatement = conn.prepareStatement(queryStrSelect);
				userStatement.setInt(1, UserId);
				resultSet = userStatement.executeQuery();
				while(resultSet.next()){
					discRank = new HashMap<String, Double>();
					discRank.put("cinema", resultSet.getDouble("cinema"));
					discRank.put("theater", resultSet.getDouble("theater"));
					discRank.put("festival", resultSet.getDouble("festival"));
					discRank.put("music", resultSet.getDouble("music"));
					discRank.put("exhibition", resultSet.getDouble("exhibition"));
					discRank.put("kid", resultSet.getDouble("kid"));
					discRank.put("gastro", resultSet.getDouble("gastro"));
					discRank.put("travel", resultSet.getDouble("travel"));
					discRank.put("sport", resultSet.getDouble("sport"));
					discRank.put("party", resultSet.getDouble("party"));
					discRank.put("other", resultSet.getDouble("other"));
					userDiscriminatorRank.put(UserId, discRank);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				if (userStatement != null) {
					try {
						userStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}else{
			String queryStrSelect = "SELECT cinema, theater, festival, music, exhibition, kid, gastro, travel, sport, party, other, UserId FROM UserDiscriminatorsRanks WHERE 1";
			try {
				userStatement = conn.prepareStatement(queryStrSelect);
				resultSet = userStatement.executeQuery();
				while(resultSet.next()){
					if(resultSet.getInt("UserId")!=0){	// do not return the 0. user discriminator value
						discRank = new HashMap<String, Double>();
						discRank.put("cinema", resultSet.getDouble("cinema"));
						discRank.put("theater", resultSet.getDouble("theater"));
						discRank.put("festival", resultSet.getDouble("festival"));
						discRank.put("music", resultSet.getDouble("music"));
						discRank.put("exhibition", resultSet.getDouble("exhibition"));
						discRank.put("kid", resultSet.getDouble("kid"));
						discRank.put("gastro", resultSet.getDouble("gastro"));
						discRank.put("travel", resultSet.getDouble("travel"));
						discRank.put("sport", resultSet.getDouble("sport"));
						discRank.put("party", resultSet.getDouble("party"));
						discRank.put("other", resultSet.getDouble("other"));
						userDiscriminatorRank.put(resultSet.getInt("UserId"), discRank);
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				if (userStatement != null) {
					try {
						userStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return userDiscriminatorRank;
	}

	/*Returns all tag for legit events from long date converted into integer date*/
	public HashMap<Integer, List<String>> getEventsTagFromDateV2(long fromDate) throws SQLException{
		HashMap<Integer, List<String>> eventTag = new HashMap<Integer, List<String>>();
		List<String> tagArray = new ArrayList<String>();
		PreparedStatement getEventTagStatement = null;
		Integer date = getIntegerDate(fromDate);
		ResultSet eventTagResults = null;		
		try {			
			String queryStr = "SELECT EventId,tagJson FROM EventTags WHERE (SELECT EventId FROM EventDays WHERE EventDays.day >= ? AND EventTags.EventId=EventId LIMIT 1)";		
			getEventTagStatement = conn.prepareStatement(queryStr);
			getEventTagStatement.setInt(1, date);
			eventTagResults = getEventTagStatement.executeQuery();		
			while (eventTagResults.next()) {
				tagArray = parseTagJson(eventTagResults.getString("tagJson"));	// MEG KELL CSINÁLNI A PARSOLÁST !
				Integer eventId = eventTagResults.getInt("EventId");
				eventTag.put(eventId, tagArray);
			}
		} finally {
			if (eventTagResults != null)  {
				eventTagResults.close();
			}
			if (getEventTagStatement != null) {
				getEventTagStatement.close();
			}
		}
		return eventTag;		
	}
	
	/*Return all events tag from EventTag table.*/
	public HashMap<Integer, List<String>> getAllEventsTag(){
		HashMap<Integer, List<String>> eventTag = new HashMap<Integer, List<String>>();
		List<String> tagArray = new ArrayList<String>();
		PreparedStatement getEventTagStatement = null;
		ResultSet eventTagResults = null;
		String queryStr = "SELECT EventId,tagJson FROM EventTags WHERE 1";		
		try {
			getEventTagStatement = conn.prepareStatement(queryStr);
			eventTagResults = getEventTagStatement.executeQuery();
			while (eventTagResults.next()) {
				tagArray = parseTagJson(eventTagResults.getString("tagJson"));	// MEG KELL CSINÁLNI A PARSOLÁST !
				Integer eventId = eventTagResults.getInt("EventId");
				eventTag.put(eventId, tagArray);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (eventTagResults != null)  {
				try {
					eventTagResults.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (getEventTagStatement != null) {
				try {
					getEventTagStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return eventTag;
	}
	
	/*Update Event Tags in EventsTag table. Works with more event.*/
	public void uploadEventsTag(HashMap<Integer, List<String>> eventTags){
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Timestamp ts = new Timestamp(utc);
		HashMap<Integer, List<String>> allEventsTag = getAllEventsTag();
		HashMap<Integer, List<String>> newEventTags = new HashMap<Integer, List<String>>();
		PreparedStatement Statement = null;
		String insertQuery = "INSERT INTO EventTags (tagJson, createdAt, updatedAt, EventId) VALUES (?,?,?,?)";
		String updateQuery = "UPDATE EventTags SET tagJson =?, updatedAt=? WHERE EventId=?";
		//-----------------UPDATE----------------------
		try {
			for(Entry<Integer, List<String>>entry: eventTags.entrySet()){
				Integer EventId = entry.getKey();
				List<String> newTags = entry.getValue();
				if(allEventsTag.containsKey(EventId)){ //already has this event in tag table
					List<String> oldTags = allEventsTag.get(EventId);
					for(int i=0; i<oldTags.size(); i++){
						String oneTag = oldTags.get(i);
						newTags.add(oneTag);
					}
					newEventTags.put(EventId, newTags);
				}
			}
			Statement = conn.prepareStatement(updateQuery);
			for(Entry<Integer,List<String>>entry2: newEventTags.entrySet()){
				Integer EventId = entry2.getKey();
				List<String> eventTag = entry2.getValue();
				String json = createJson(eventTag);
				Statement.setString(1, json);
				Statement.setTimestamp(2, ts);
				Statement.setInt(3, EventId);
				Statement.addBatch();
			}
			int[] num = Statement.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (Statement != null) {
				try {
					Statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//-------------------INSERT---------------------
		try {
			Statement = conn.prepareStatement(insertQuery);
			for(Entry<Integer, List<String>>entry: eventTags.entrySet()){
				Integer EventId = entry.getKey();
				List<String> tags = entry.getValue();
				if(!allEventsTag.containsKey(EventId)){
					String json = createJson(tags);
					Statement.setString(1, json);
					Statement.setTimestamp(2, ts);
					Statement.setTimestamp(3, ts);
					Statement.setInt(4, EventId);
					Statement.addBatch();
				}
			}
			int[] num = Statement.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (Statement != null) {
				try {
					Statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}		
	}
	
	/*Insert a line into eventlog
	 * - user click
	 * - user event checks
	 * - user logins*/
	public void uploadEventLogV2(int userid, int eventid, long timestamp, int type) throws SQLException{
		Timestamp ts = new Timestamp(timestamp);
		String selectQuery = 
				"INSERT INTO EventLogs (UserId, EventId, createdAt, updatedAt, type) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement insertLogStatement = null;
		try {
			insertLogStatement = conn.prepareStatement(selectQuery);
			insertLogStatement.setLong(1, userid);
			insertLogStatement.setInt(2, eventid);
			insertLogStatement.setTimestamp(3, ts);
			insertLogStatement.setTimestamp(4, ts);
			insertLogStatement.setInt(5, type);
			insertLogStatement.executeUpdate();
		} finally {
			if (insertLogStatement != null) {
				insertLogStatement.close();
			}
		}
	}
	
	/*Insert new log line into RecommendationLogs*/
	public void insertRecommendationLog(String comment, Integer UserID){
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Timestamp ts = new Timestamp(utc);
		PreparedStatement insertStatement = null;
		String insertQuery = "INSERT INTO RecommendationLogs (comment, createdAt, updatedAt, UserId) VALUES (?, ?, ?, ?)";
		try {
			insertStatement = conn.prepareStatement(insertQuery);
			insertStatement.setString(1, comment);
			insertStatement.setTimestamp(2, ts);
			insertStatement.setTimestamp(3, ts);
			insertStatement.setInt(4, UserID);
			insertStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (insertStatement != null) {
				try {
					insertStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/*NOT USED. USE 3rd VERSION! Insert newly calculated rank values for user event pairs
	 * - Rank values should be descending order.
	 * - Delete old rank values before upload new ones*/
	public void updateRecPV2(int UserId, LinkedHashMap<Integer, Double> EventRanks){
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Timestamp ts = new Timestamp(utc);
		String deleteRec = "DELETE FROM UserEventRecommendations WHERE UserId=?";
		PreparedStatement deleteRecStatement = null;
		try {
			deleteRecStatement = conn.prepareStatement(deleteRec);
			deleteRecStatement.setInt(1, UserId);
			deleteRecStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (deleteRecStatement != null) {
				try {
					deleteRecStatement.close();
					String insertQuery = "INSERT INTO UserEventRecommendations (rank, EventId, UserId, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?)";
					PreparedStatement insertStatement = null;
					try {
						insertStatement = conn.prepareStatement(insertQuery);
						for (Entry<Integer, Double> entry : EventRanks.entrySet()) {
						    Integer eventid = entry.getKey();
						    Double rank = entry.getValue(); 
							insertStatement.setDouble(1, rank);
							insertStatement.setInt(2, eventid);
							insertStatement.setInt(3, UserId);
							insertStatement.setTimestamp(4, ts);
							insertStatement.setTimestamp(5, ts);
							insertStatement.addBatch();
							System.out.println("ID:" +UserId+ "eventid:" +eventid+ "rank:" +rank);
						}								
						int [] numUpdates=insertStatement.executeBatch();
					} catch (SQLException e) {
						e.printStackTrace();
					} finally {
						List<Integer> userArray = new ArrayList<Integer>();
						userArray.add(UserId);
						setShouldCache(userArray);	// set cache flag after upload the rec taple
						if (insertStatement != null) {
							try {
								insertStatement.close();
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					}
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/*Delete, Update, Insert events rank value into UserEventRecommendations table*/
	public void updateRecPV3(int UserId, LinkedHashMap<Integer, Double> newEventRanks){
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Timestamp ts = new Timestamp(utc);
		HashMap<Integer, Double> userOldRanks = getRecPUser(UserId);
		List<Integer> deleteThisRanks = new ArrayList<Integer>();
		List<Integer> insertNewEventRanks = new ArrayList<Integer>();
		List<Integer> updateNewEventRanks = new ArrayList<Integer>();
		for(Entry<Integer,Double>entry: userOldRanks.entrySet()){	// filter events for update and delete
			Integer EventId = entry.getKey();
			if(!newEventRanks.containsKey(EventId)){
				deleteThisRanks.add(EventId);
			}else{
				updateNewEventRanks.add(EventId);
			}
		}
		for(Entry<Integer, Double>entry: newEventRanks.entrySet()){	// filter events for insert
			Integer EventId = entry.getKey();
			if(!deleteThisRanks.contains(EventId) && !updateNewEventRanks.contains(EventId)){
				insertNewEventRanks.add(EventId);
			}
		}
		String deleteRec = "DELETE FROM UserEventRecommendations WHERE UserId=? AND EventId=?";
		String insertQuery = "INSERT INTO UserEventRecommendations (rank, EventId, UserId, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?)";
		String updateQuery = "UPDATE UserEventRecommendations SET rank=?, updatedAt=? WHERE EventId=? AND UserId=?";
		PreparedStatement recStatement = null;
		if(!deleteThisRanks.isEmpty()){
			try {
				recStatement = conn.prepareStatement(deleteRec);
				for(int i=0; i< deleteThisRanks.size(); i++){
					Integer EventId = deleteThisRanks.get(i);
					recStatement.setInt(1, UserId);
					recStatement.setInt(2, EventId);
					recStatement.addBatch();
				}
				int [] numUpdates1=recStatement.executeBatch();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				if (recStatement != null) {
					try {
						recStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}		
		}
		if(!updateNewEventRanks.isEmpty()){
			try {
				recStatement = conn.prepareStatement(updateQuery);
				for(int i=0; i<updateNewEventRanks.size(); i++){
					Integer EventId = updateNewEventRanks.get(i);
					recStatement.setDouble(1, newEventRanks.get(EventId));
					recStatement.setTimestamp(2, ts);
					recStatement.setInt(3, EventId);
					recStatement.setInt(4, UserId);
					recStatement.addBatch();
				}
				int [] numUpdates2=recStatement.executeBatch();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				if (recStatement != null) {
					try {
						recStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if(!insertNewEventRanks.isEmpty()){
			try {
				recStatement = conn.prepareStatement(insertQuery);
				for(int i=0; i<insertNewEventRanks.size(); i++){
					Integer EventId = insertNewEventRanks.get(i);
					recStatement.setDouble(1, newEventRanks.get(EventId));
					recStatement.setInt(2, EventId);
					recStatement.setInt(3, UserId);
					recStatement.setTimestamp(4, ts);
					recStatement.setTimestamp(5, ts);
					recStatement.addBatch();	
				}
				int [] numUpdates3=recStatement.executeBatch();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				if (recStatement != null) {
					try {
						recStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/*JUST - Delete, Insert events rank value into UserEventRecommendations table*/
	public void insertRecPV4(int UserId, LinkedHashMap<Integer, Double> newEventRanks){
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Timestamp ts = new Timestamp(utc);
		HashMap<Integer, Double> userOldRanks = getRecPUser(UserId);
		List<Integer> deleteThisRanks = new ArrayList<Integer>();
		List<Integer> insertNewEventRanks = new ArrayList<Integer>();
		List<Integer> updateNewEventRanks = new ArrayList<Integer>();
		for(Entry<Integer,Double>entry: userOldRanks.entrySet()){	// filter events for update and delete
			Integer EventId = entry.getKey();
			if(!newEventRanks.containsKey(EventId)){
				deleteThisRanks.add(EventId);
			}else{
				updateNewEventRanks.add(EventId);
			}
		}
		for(Entry<Integer, Double>entry: newEventRanks.entrySet()){	// filter events for insert
			Integer EventId = entry.getKey();
			if(!deleteThisRanks.contains(EventId) && !updateNewEventRanks.contains(EventId)){
				insertNewEventRanks.add(EventId);
			}
		}
		String deleteRec = "DELETE FROM UserEventRecommendations WHERE UserId=? AND EventId=?";
		String insertQuery = "INSERT INTO UserEventRecommendations (rank, EventId, UserId, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement recStatement = null;
		if(!deleteThisRanks.isEmpty()){
			try {
				recStatement = conn.prepareStatement(deleteRec);
				for(int i=0; i< deleteThisRanks.size(); i++){
					Integer EventId = deleteThisRanks.get(i);
					recStatement.setInt(1, UserId);
					recStatement.setInt(2, EventId);
					recStatement.addBatch();
				}
				int [] numUpdates1=recStatement.executeBatch();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				if (recStatement != null) {
					try {
						recStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}		
		}
		if(!insertNewEventRanks.isEmpty()){
			try {
				recStatement = conn.prepareStatement(insertQuery);
				for(int i=0; i<insertNewEventRanks.size(); i++){
					Integer EventId = insertNewEventRanks.get(i);
					recStatement.setDouble(1, newEventRanks.get(EventId));
					recStatement.setInt(2, EventId);
					recStatement.setInt(3, UserId);
					recStatement.setTimestamp(4, ts);
					recStatement.setTimestamp(5, ts);
					recStatement.addBatch();	
				}
				int [] numUpdates3=recStatement.executeBatch();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				if (recStatement != null) {
					try {
						recStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}	
	}
	
	/*Update specified events for ALL USER.
	 * This function dont handle if one event is not in the recommendation table with one rank value*/
	public void updateRecPV5(HashMap<Integer, Double> newEventRanks){
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Timestamp ts = new Timestamp(utc);
		PreparedStatement statement = null;
		String query = "UPDATE UserEventRecommendations SET rank=?, updatedAt=? WHERE EventId=?";
		if(!newEventRanks.isEmpty()){
			try {
				statement = conn.prepareStatement(query);
				for(Entry<Integer, Double>entry: newEventRanks.entrySet()){
					Integer FunspotterEventId = entry.getKey();
					Double rank = entry.getValue();
					statement.setDouble(1, rank);
					statement.setTimestamp(2, ts);
					statement.setInt(3, FunspotterEventId);
					statement.addBatch();
				}
				int[] num = statement.executeBatch();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/*Insert into NewUserEventChache table the newly calculated first step data. Create shouldChache flag too.*/
	public void updateNewUserEventCaches(List<Rec> zeroUserRanks) throws SQLException{
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Timestamp ts = new Timestamp(utc);
		HashMap<Integer, Double> newEventRanks = new HashMap<Integer, Double>();
		for(int i=0; i<zeroUserRanks.size(); i++){
			Rec faszom = zeroUserRanks.get(i);
			Integer EventID = faszom.getEventID();
			Double rank = faszom.getRank();
			newEventRanks.put(EventID, rank);
		}
		HashMap<Integer, Double> zeroUserOldRanks = getZeroUserRanks();
		List<Integer> deleteThisRanks = new ArrayList<Integer>();
		List<Integer> insertNewEventRanks = new ArrayList<Integer>();
		List<Integer> updateNewEventRanks = new ArrayList<Integer>();
		for(Entry<Integer,Double>entry: zeroUserOldRanks.entrySet()){	// filter events for update and delete
			Integer EventId = entry.getKey();
			if(!newEventRanks.containsKey(EventId)){
				deleteThisRanks.add(EventId);
			}else{
				updateNewEventRanks.add(EventId);
			}
		}
		for(Entry<Integer, Double>entry: newEventRanks.entrySet()){	// filter events for insert
			Integer EventId = entry.getKey();
			if(!deleteThisRanks.contains(EventId) && !updateNewEventRanks.contains(EventId)){
				insertNewEventRanks.add(EventId);
			}
		}
		String deleteRec = "DELETE FROM NewUserEventCaches WHERE EventId=?";
		String insertQuery = "INSERT INTO NewUserEventCaches (rank, EventId, ZoneId, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?)";
		String updateQuery = "UPDATE NewUserEventCaches SET rank=?, updatedAt=? WHERE EventId=?";
		PreparedStatement recStatement = null;
		if(!deleteThisRanks.isEmpty()){
			try {
				recStatement = conn.prepareStatement(deleteRec);
				for(int i=0; i< deleteThisRanks.size(); i++){
					Integer EventId = deleteThisRanks.get(i);
					recStatement.setInt(1, EventId);
					recStatement.addBatch();
				}
				int [] numUpdates1=recStatement.executeBatch();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				if (recStatement != null) {
					try {
						recStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}		
		}
		if(!updateNewEventRanks.isEmpty()){
			try {
				recStatement = conn.prepareStatement(updateQuery);
				for(int i=0; i<updateNewEventRanks.size(); i++){
					Integer EventId = updateNewEventRanks.get(i);
					recStatement.setDouble(1, newEventRanks.get(EventId));
					recStatement.setTimestamp(2, ts);
					recStatement.setInt(3, EventId);
					recStatement.addBatch();
				}
				int [] numUpdates2=recStatement.executeBatch();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				if (recStatement != null) {
					try {
						recStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		if(!insertNewEventRanks.isEmpty()){
			try {
				recStatement = conn.prepareStatement(insertQuery);
				for(int i=0; i<insertNewEventRanks.size(); i++){
					Integer EventId = insertNewEventRanks.get(i);
					recStatement.setDouble(1, newEventRanks.get(EventId));
					recStatement.setInt(2, EventId);
					recStatement.setInt(3, 0);	// zoneid
					recStatement.setTimestamp(4, ts);
					recStatement.setTimestamp(5, ts);
					recStatement.addBatch();	
				}
				int [] numUpdates3=recStatement.executeBatch();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				if (recStatement != null) {
					try {
						recStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/*Update caching flag for GetEventsResponseCaches table*/
	public void setShouldCache(List<Integer>UserIds){
		PreparedStatement setStatement = null;
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Timestamp ts = new Timestamp(utc);
		String updateQuery ="UPDATE GetEventsResponseCaches SET shouldCache=?, updatedAt=? WHERE UserId = ?";
		try {
			for(int i=0; i<UserIds.size(); i++){
				Integer UserId = UserIds.get(i);
				setStatement = conn.prepareStatement(updateQuery);
				setStatement.setInt(1, 1);
				setStatement.setTimestamp(2, ts);
				setStatement.setInt(3, UserId);
				setStatement.addBatch();
			}
			int[] num = setStatement.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (setStatement != null) {
				try {
					setStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/*Returns one users recommendation data. hashmap(eventid - double_rank_value)*/
	public HashMap<Integer, Double> getRecPUser (int UserId){
		HashMap<Integer, Double> rankValues = new HashMap<Integer, Double>();
		PreparedStatement getRecStatement = null;
		ResultSet resultSet = null;
		String insertQuery = "SELECT rank, EventId FROM UserEventRecommendations WHERE UserId = ?";
		try {
			getRecStatement = conn.prepareStatement(insertQuery);
			getRecStatement.setInt(1, UserId);
			resultSet = getRecStatement.executeQuery();
			while(resultSet.next()){
				rankValues.put(resultSet.getInt("EventId"), resultSet.getDouble("rank"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (resultSet != null)  {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (getRecStatement != null) {
				try {
					getRecStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	
		return rankValues;	
	}
	
	/*Returns FirstStep ZERO user eventid and rank values*/
	public HashMap<Integer, Double> getZeroUserRanks(){
		HashMap<Integer, Double> rankValues = new HashMap<Integer, Double>();
		PreparedStatement getRecStatement = null;
		ResultSet resultSet = null;
		String getQuery = "SELECT rank, EventId FROM NewUserEventCaches WHERE 1";
		try {
			getRecStatement = conn.prepareStatement(getQuery);
			resultSet = getRecStatement.executeQuery();
			while(resultSet.next()){
				rankValues.put(resultSet.getInt("EventId"), resultSet.getDouble("rank"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (resultSet != null)  {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (getRecStatement != null) {
				try {
					getRecStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	
		return rankValues;	
	}
	
	/*Returns the number of users*/
	public int getAllUserNumberV2() throws SQLException{
		PreparedStatement getUserStatement = null;
		ResultSet userResults = null;
		int count = 0;
		try {
			String queryStr = "SELECT COUNT(*) AS number FROM Users";
			getUserStatement = conn.prepareStatement(queryStr);
			userResults = getUserStatement.executeQuery();			
			while (userResults.next()) {
				count = userResults.getInt("number");
			}
		} finally {
			if (userResults != null)  {
				userResults.close();
			}
			if (getUserStatement != null) {
				getUserStatement.close();
			}
		}
		return count;
	}
	
	/*Returns all userid and facebookid in hasmap*/
	public HashMap<Integer, Long> getAllFaceAndUserIdV2() throws SQLException{
		HashMap<Integer,Long> lhm = new HashMap<Integer,Long>();	
		PreparedStatement getUserStatement = null;
		ResultSet userResults = null;
		try {	
			String queryStr = "SELECT id,facebookId FROM Users WHERE 1";
			getUserStatement = conn.prepareStatement(queryStr);
			userResults = getUserStatement.executeQuery();
			while (userResults.next()) {
				Integer userid = userResults.getInt("id");
				Long facebookid = userResults.getLong("facebookId");
				lhm.put(userid,facebookid);
			}			
		} finally {
			if (userResults != null)  {
				userResults.close();
			}
			if (getUserStatement != null) {
				getUserStatement.close();
			}
		}		
		return lhm;		
	}
	
	/*Return all users ID in a list*/	// for Rec. accuracy getUserIdArrayFromTestUser
	public List<Integer> getUserIdArray() throws SQLException{
		PreparedStatement statement = null;
		ResultSet eventResults = null;
		List<Integer> l = new ArrayList<Integer>();
		try{
			String queryEventId = "SELECT id FROM Users WHERE 1";
			statement = conn.prepareStatement(queryEventId);
			eventResults = statement.executeQuery();
			while(eventResults.next()){
				l.add(eventResults.getInt("id"));
			}
		} finally {
			if (eventResults != null) {
				eventResults.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return l;	
	}
	
	/*Returns FacebookId for UserId - Returns 0 if sth. goes wrong*/
	public long getFbUserIDforUserIDV2(int userId) throws SQLException {
		long ret = 0;
		PreparedStatement statement = null;
		ResultSet results = null;
		try {
			statement = conn.prepareStatement("SELECT facebookId FROM Users WHERE id = ?");
			statement.setInt(1, userId);
			results = statement.executeQuery();
			if (results.next()) {
				ret = results.getLong("facebookId");
			}
		} finally {
			if (results != null) {
				results.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return ret;
	}
	
	/*Returns facebookid for one user. Uses userid*/
	public int getUserIdforFacebookUserId(Long facebookUserId) throws SQLException {
		int ret = 0;
		PreparedStatement statement = null;
		ResultSet results = null;
		try {
			statement = conn.prepareStatement("SELECT id FROM Users WHERE facebookId = ?");
			statement.setLong(1, facebookUserId);
			results = statement.executeQuery();
			if (results.next()) {
				ret = results.getInt("id");
			}
		} finally {
			if (results != null) {
				results.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return ret;
	}
	
	/*Returns all user last visit on site from Users table*/
	public HashMap<Integer, Date> getAllUserLastVisit() throws SQLException{
		PreparedStatement statement = null;
		ResultSet results = null;
		HashMap<Integer, Date> allUserLastVisit = new HashMap<Integer,Date>();
		try {
			statement = conn.prepareStatement("SELECT id, lastVisitOnSite FROM Users WHERE 1");
			results = statement.executeQuery();
			if(results.next()){
				allUserLastVisit.put(results.getInt("id"), results.getDate("lastVisitOnSite"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (results != null) {
				results.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return allUserLastVisit;
	}
	
	/*Returns all event ranks for one user in hashmap. use userid*/
	public HashMap<Integer,Double> getEventIdArrayForXYUserV2(int UserId) throws SQLException{
		PreparedStatement statement = null;
		ResultSet eventResults = null;
		HashMap<Integer,Double> hm = new HashMap<Integer,Double>();
		try{
			String queryEventId = "SELECT EventId, rank FROM UserEventRecommendations WHERE UserId= ?";
			statement = conn.prepareStatement(queryEventId);
			statement.setInt(1,UserId);
			eventResults = statement.executeQuery();
			while(eventResults.next()){
				Integer eventId = eventResults.getInt("EventId");
				Double rank = eventResults.getDouble("rank");
				hm.put(eventId,rank);
			}
		} finally {
			if (eventResults != null) {
				eventResults.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return hm;	
	}
	
	/*Returns true for userId if have in the UserEventRecommendations any rows (:= has rec)*/
	public boolean hasRecV2(int userId) throws SQLException{
		boolean ret = false;
		PreparedStatement selectStatement = null;
		ResultSet resultSet = null;
		try {
			final String queryStr = "SELECT COUNT(*) AS number FROM UserEventRecommendations WHERE UserId = ?";
			selectStatement = conn.prepareStatement(queryStr);
			selectStatement.setInt(1, userId);
			resultSet = selectStatement.executeQuery();
			if(resultSet.next()){
				int result = resultSet.getInt("number");						// if have at least one line in db, has rec.
				if(result>0){
					ret = true;
				}
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (selectStatement != null) {
				selectStatement.close();
			}
		}
		return ret;
	}
	
	/*Returns users age; if the age is negative age is 0*/
	public Integer getUserAge(int UserId) throws SQLException{
		PreparedStatement getBirthDateStatement = null;
		ResultSet bdateResults = null;
		Integer age = 0;
		try {
			String queryStr = "SELECT birthDate FROM Users WHERE id = ?";
			getBirthDateStatement = conn.prepareStatement(queryStr);
			getBirthDateStatement.setInt(1, UserId);
			bdateResults = getBirthDateStatement.executeQuery();
			Date today = new Date();	
			while (bdateResults.next()) {
				Date bdate = null;
				try{
					bdate = bdateResults.getDate("birthDate");
				}catch(SQLException exp){
					System.out.println("Szulinap rossz");
				}
				try{
					if(bdate==null){
						age = 25;
					}else{
						System.out.println(today.getTime()+" "+bdate.getTime());
						age = (int)((today.getTime()/1000-bdate.getTime()/1000)/60/60/24/365);
					}
				}catch(NullPointerException n){}
			}				
		} finally {
			if (bdateResults != null)  {
				bdateResults.close();
			}
			if (getBirthDateStatement != null) {
				getBirthDateStatement.close();
			}
		}
		if(age<0)age=0;
		return age;	
	}
	
	/*Returns all like for one user in a Like list. (userid - List<Like>)*/
	public List<Like> getAllLikeForUserId(int UserId) throws SQLException{
		List<Like> likes = new ArrayList<Like>();
		PreparedStatement selectStatement = null;
		ResultSet resultSet = null;
		int rowcount=0;
		String queryStr = null;
		try {	
			queryStr = 
					"SELECT l.facebookLikeId, l.id,l.category,l.name " +
					"FROM FacebookLikes AS l, FacebookLikesUsers AS ul " +
					"WHERE ul.UserId=? and l.id=ul.FacebookLikeId";
			selectStatement = conn.prepareStatement(queryStr);
			selectStatement.setInt(1,UserId);
			resultSet = selectStatement.executeQuery();
			while(resultSet.next()){
				rowcount++;
				Like like = new Like();
				like.setFacebooksId(resultSet.getLong("facebookLikeId"));
				like.setId(resultSet.getLong("id"));
				like.setCategory(resultSet.getString("category"));
				like.setName(resultSet.getString("name"));
				likes.add(like);
			}	
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (selectStatement != null) {
				selectStatement.close();
			}	
		}
		if (DatabaseConnectionData.DEBUG) {
			System.out.println(queryStr);
			System.out.println(Integer.toString(rowcount));
			System.out.println(Integer.toString(likes.size()));
		}
		return likes;
	}

	/*Returns all likes for facebook user; convert fbuserid to userid, !use the other! function to get likes*/
	public List<Like> getAllLikeForFbUserIdV2(long facebookUserId) throws SQLException{
		List<Like> likes = new ArrayList<Like>();
		PreparedStatement selectStatement = null;
		ResultSet resultSet = null;
		int rowcount=0;
		String queryStr = null;
		int UserId = getUserIdforFacebookUserId(facebookUserId);
		try {	
			queryStr = 
					"SELECT l.facebookLikeId, l.id,l.category, l.name " +
					"FROM FacebookLikes AS l, FacebookLikesUsers AS ul " +
					"WHERE ul.UserId=? and l.id=ul.FacebookLikeId";
			selectStatement = conn.prepareStatement(queryStr);
			selectStatement.setInt(1,UserId);
			resultSet = selectStatement.executeQuery();
			while(resultSet.next()){
				rowcount++;
				Like like = new Like();
				like.setFacebooksId(resultSet.getLong("facebookLikeId"));
				like.setId(resultSet.getLong("id"));
				like.setCategory(resultSet.getString("category"));
				like.setName(resultSet.getString("name"));
				likes.add(like);
			}	
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (selectStatement != null) {
				selectStatement.close();
			}	
		}
		if (DatabaseConnectionData.DEBUG) {
			System.out.println(queryStr);
			System.out.println(Integer.toString(rowcount));
			System.out.println(Integer.toString(likes.size()));
		}
		return likes;
	}
	
	/*Returns all our facebook like id from LikeImdbTags tabl. USe MEtadataFromthirdparty class*/
	public HashMap<Long, MetadataFromThirdParty> getInfoFromImdbTags() throws SQLException{
		HashMap<Long, MetadataFromThirdParty> likeTagHm = new HashMap<Long, MetadataFromThirdParty>();
		PreparedStatement selectStatement = null;
		ResultSet resultSet = null;
		String queryStr = null;
		try {
			queryStr = "SELECT FacebookLikeId, tagJson, imdbId FROM LikeImdbTags WHERE 1";
			selectStatement = conn.prepareStatement(queryStr);
			resultSet = selectStatement.executeQuery();
			while(resultSet.next()){
				Long ourLikeId = resultSet.getLong("FacebookLikeId");
				List<String> likeTags = parseTagJson(resultSet.getString("tagJson"));
				String imdbId = resultSet.getString("imdbId");
				MetadataFromThirdParty oneLikeTagInfo = new MetadataFromThirdParty(ourLikeId, imdbId, likeTags);
				likeTagHm.put(ourLikeId, oneLikeTagInfo);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (selectStatement != null) {
				selectStatement.close();
			}	
		}
		return likeTagHm;
	}
	
	/*Returns all our facebook like id from LikeLastfmTags table use Metadatafromthirdparty class*/
	public HashMap<Long, MetadataFromThirdParty> getInfoFromLastfmTags() throws SQLException{
		HashMap<Long, MetadataFromThirdParty> likeTagHm = new HashMap<Long, MetadataFromThirdParty>();
		PreparedStatement selectStatement = null;
		ResultSet resultSet = null;
		String queryStr = null;
		try {
			queryStr = "SELECT FacebookLikeId, tagJson, lastfmId FROM LikeLastfmTags WHERE 1";
			selectStatement = conn.prepareStatement(queryStr);
			resultSet = selectStatement.executeQuery();
			while(resultSet.next()){
				Long ourLikeId = resultSet.getLong("FacebookLikeId");
				List<String> likeTags = parseTagJson(resultSet.getString("tagJson"));
				String lastfmId = resultSet.getString("lastfmId");
				MetadataFromThirdParty oneLikeTagInfo = new MetadataFromThirdParty(ourLikeId, lastfmId, likeTags);
				likeTagHm.put(ourLikeId, oneLikeTagInfo);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (selectStatement != null) {
				selectStatement.close();
			}	
		}
		return likeTagHm;
	}
	
	/*Upload facebook music like tags into Lastfm table. Use Metadatafromthirdparty class*/
	public void uploadInfoToLastfmTags(HashMap<Long, MetadataFromThirdParty> allNewLikeTagHm){
		PreparedStatement uploadStatement = null;
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Timestamp ts = new Timestamp(utc);
		String updateQuery = "INSERT INTO LikeLastfmTags (lastfmId, tagJson, createdAt, updatedAt, FacebookLikeId) VALUES (?,?,?,?,?)";
		try {
			uploadStatement = conn.prepareStatement(updateQuery);
			for(Entry<Long, MetadataFromThirdParty> entry: allNewLikeTagHm.entrySet()){
				Long ourLikeId = entry.getKey();
				System.out.println("lastfm table upload; id: "+ ourLikeId);
				MetadataFromThirdParty likeMetadata = entry.getValue();
				uploadStatement.setString(1, likeMetadata.getThirdPartyId());
				uploadStatement.setString(2, likeMetadata.getJsonString());
				uploadStatement.setTimestamp(3, ts);
				uploadStatement.setTimestamp(4, ts);
				uploadStatement.setLong(5, ourLikeId);
				uploadStatement.addBatch();
			}
			int [] returnStuff = uploadStatement.executeBatch();	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (uploadStatement != null) {
				try {
					uploadStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*Upload facebook music like tags into IMDB table*/
	public void uploadInfoToImdbTags(HashMap<Long, MetadataFromThirdParty> allNewLikeTagHm){
		PreparedStatement uploadStatement = null;
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Timestamp ts = new Timestamp(utc);
		String updateQuery = "INSERT INTO LikeImdbTags (imdbId, tagJson, createdAt, updatedAt, FacebookLikeId) VALUES (?,?,?,?,?)";
		try {
			uploadStatement = conn.prepareStatement(updateQuery);
			for(Entry<Long, MetadataFromThirdParty> entry: allNewLikeTagHm.entrySet()){
				Long ourLikeId = entry.getKey();
				System.out.println("imdb table upload; id: "+ ourLikeId);
				MetadataFromThirdParty likeMetadata = entry.getValue();
				uploadStatement.setString(1, likeMetadata.getThirdPartyId());
				uploadStatement.setString(2, likeMetadata.getJsonString());
				uploadStatement.setTimestamp(3, ts);
				uploadStatement.setTimestamp(4, ts);
				uploadStatement.setLong(5, ourLikeId);
				uploadStatement.addBatch();
			}
			int [] returnStuff = uploadStatement.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (uploadStatement != null) {
				try {
					uploadStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/*Update FacebookPlaceTags table, to save place categories discriminator types
	 * and the number of categorized event based on this and other category*/
	public void updateFacebookPlaceTagTable(HashMap<Long, FacebookPlaceTag> newTagDiscNum, HashMap<Long, FacebookPlaceTag> oldTagDiscNum){
		PreparedStatement statement = null;
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Timestamp ts = new Timestamp(utc);
		String updateQuery = "UPDATE FacebookPlaceTags SET discriminatorNumber = ?, updatedAt = ? WHERE facebookCategoryId = ?";
		String insertQuery = "INSERT INTO FacebookPlaceTags (facebookCategoryId, facebookCategoryName, discriminatorNumber, createdAt, updatedAt) VALUES (?,?,?,?,?)";
		/*UPDATE*/
		try {
			statement = conn.prepareStatement(updateQuery);
			for(Entry<Long, FacebookPlaceTag>entry: newTagDiscNum.entrySet()){
				Long categoryId = entry.getKey();
				FacebookPlaceTag categoryInfo = entry.getValue();
				if(oldTagDiscNum.containsKey(categoryId)){	// update part
					statement.setString(1, categoryInfo.getDiscriminatorNumberJson().toString());
					statement.setTimestamp(2, ts);
					statement.setLong(3, categoryInfo.getId());
					statement.addBatch();
				}
			}
			int[] num = statement.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		/*INSERT*/
		try {
			statement = conn.prepareStatement(insertQuery);
			for(Entry<Long, FacebookPlaceTag>entry: newTagDiscNum.entrySet()){
				Long categoryId = entry.getKey();
				FacebookPlaceTag categoryInfo = entry.getValue();
				if(!oldTagDiscNum.containsKey(categoryId)){	// insert part
					statement.setLong(1, categoryInfo.getId());
					statement.setString(2, categoryInfo.getName());
					statement.setString(3, categoryInfo.getDiscriminatorNumberJson().toString());
					statement.setTimestamp(4, ts);
					statement.setTimestamp(5, ts);
					statement.addBatch();
				}
			}
			int[] num = statement.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/*Returns all Facebook place category from this table*/
	public HashMap<Long, FacebookPlaceTag> getFacebookPlaceTagTable(){
		HashMap<Long, FacebookPlaceTag> tagDiscriminatorNumber = new HashMap<Long, FacebookPlaceTag>();
		PreparedStatement selectStatement = null;
		ResultSet resultSet = null;
		String queryStr = null;
		try {
			queryStr = "SELECT facebookCategoryId, facebookCategoryName, discriminatorNumber FROM FacebookPlaceTags WHERE 1";
			selectStatement = conn.prepareStatement(queryStr);
			resultSet = selectStatement.executeQuery();
			while(resultSet.next()){
				Long categoryId = resultSet.getLong("FacebookCategoryId");
				String categoryName = resultSet.getString("FacebookCategoryName");
				String discriminatorNumberJson = resultSet.getString("DiscriminatorNumber");
				FacebookPlaceTag oneCategory = new FacebookPlaceTag();
				oneCategory.setId(categoryId);
				oneCategory.setName(categoryName);
				try {
					oneCategory.setDiscriminatorNumberJson(discriminatorNumberJson);
				} catch (org.json.simple.parser.ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tagDiscriminatorNumber.put(categoryId, oneCategory);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (selectStatement != null) {
				try {
					selectStatement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}
		return tagDiscriminatorNumber;
	}
	
	/*Return timestamp date in integer*/
	protected Integer getIntegerDate(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		Integer date = year * 10000 + month * 100 + day;
		return date;
	}
	
	/*tagJson*/
	protected List<String> parseTagJson(String jsonLine) {
		List<String> tagList = new ArrayList<String>();
		JSONParser parser=new JSONParser();
		Object obj;
		try {
			obj = parser.parse(jsonLine);
			JSONObject object=(JSONObject)obj;		
			JSONArray tagArray = (JSONArray) object.get("tags");
			for(int i=0; i<tagArray.size(); i++){
				tagList.add(tagArray.get(i).toString());
			}
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tagList;
	}

	/*Returns the created json object string*/
	protected String createJson(List<String> tagArray){
		JSONObject obj = new JSONObject();
		JSONArray list = new JSONArray();
		for(int i=0; i<tagArray.size(); i++){
			list.add(tagArray.get(i));
		}
		obj.put("tags", list);
		String jsonString = obj.toString();
		return jsonString;
	}

	public List<Integer> getWrongEvents(){
		List<Integer> eventIds = new ArrayList<Integer>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		String query = "SELECT id FROM Events WHERE updatedAt > '2014-04-02 16:42:07'";
		try {
			statement = conn.prepareStatement(query);
			resultSet = statement.executeQuery();
			while(resultSet.next()){
				Integer FunspotterId = resultSet.getInt("id");
				eventIds.add(FunspotterId);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}
		return eventIds;
	}
	
	public void debugFacebookEventsTable(){
		List<Integer> allFacebookEvent = getWrongEvents();
		PreparedStatement statement = null;
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Timestamp ts = new Timestamp(utc);
		String updateQuery = "UPDATE Events SET isOk = ?, isIn = ?, discriminator = ?, updatedAt = ? WHERE id = ?";
		try {
			statement = conn.prepareStatement(updateQuery);
			for(int i=0; i<allFacebookEvent.size(); i++){
				Integer funspottrerId = allFacebookEvent.get(i);
				statement.setInt(1,0);
				statement.setInt(2,0);
				statement.setString(3, "simple");
				statement.setTimestamp(4, ts);
				statement.setInt(5, funspottrerId);
				statement.addBatch();
			}
			int[] num = statement.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void debugFacebookEventsDELETE(){
		PreparedStatement statement = null;
		List<Integer> facebookEvents = getAllFacebookEventFunspotterId();
		String deleteQuery = "DELETE FROM UserEventRecommendations WHERE EventId = ?";
		try {
			statement = conn.prepareStatement(deleteQuery);
			for(int i=0; i<facebookEvents.size(); i++){
				Integer funspotterId = facebookEvents.get(i);
				statement.setInt(1, funspotterId);
				statement.addBatch();
			}
			int[] num = statement.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
