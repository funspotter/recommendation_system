import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.incredibles.data.Event;
import com.incredibles.data.Show;
import com.incredibles.data.SimpleEvent;
import com.incredibles.reclib.DavidCos;
import com.incredibles.reclib.DiscriminatorAndLikeV2;
import com.incredibles.reclib.FirstStep;
import com.incredibles.reclib.LikeWeighting;
import com.incredibles.reclib.RecommendationAccuracy;
import com.incredibles.reclib.SecondStep;
import com.incredibles.reclib.TagMaker;
import com.incredibles.reclib.Tester;
import com.incredibles.reclib.UltimateiALS;
import com.incredibles.reclib.UploadFiltersResult;
import com.incredibles.reclib.UploadFiltersResultV2;
import com.incredibles.storage.ClientDbService;
import com.incredibles.storage.ClientDbServiceCreator;
import com.incredibles.storage.EventUploader;
import com.incredibles.storage.EventUploaderCreator;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;

/**
 * Servlet implementation class API
 */
public class API extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final Gson gson;
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public API() {
		super();
		GsonBuilder builder = new GsonBuilder();
		JsonDeserializer<Date> deserializer = new JsonDeserializer<Date>() {

			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				return new Date(json.getAsJsonPrimitive().getAsLong());
			}
		};
		builder.registerTypeAdapter(Date.class, deserializer);
		gson = builder.create();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		processRequest(request, response);
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String ret = null;
		ClientDbService dbService = null;
		try {
			
			if (request.getParameter("cmd").equals("madeoffer")) {
				
				ret = DavidCos.runDavidTest();
				
			}else if(request.getParameter("cmd").equals("startsecondstep")){
				
				Runnable r = new Runnable(){
					@Override
					public void run() {
						ClientDbService dbService = null;
						try {
							dbService = ClientDbServiceCreator.createCloud();
							HashMap<Integer, Long> userIdFaceId = new HashMap<Integer, Long>();
							dbService = RecommenderDbServiceCreator.createCloud();
							userIdFaceId = dbService.getAllFaceAndUserId();
							for(Entry<Integer, Long> entry : userIdFaceId.entrySet()){
								Integer UserId = entry.getKey();
								Long facebookid = entry.getValue();
								UploadFiltersResult.filterExecute(UserId);
								dbService.uploadLog(facebookid, 0, System.currentTimeMillis(), 8);
							}
						
							try {
								dbService= ClientDbServiceCreator.createCloud();
								dbService.insertCMSLog("secondstepend");
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
					}
				};
				Thread t = new Thread(r);
				t.start();		
			}else if(request.getParameter("cmd").equals("newUser")){
				final int UserID = Integer.parseInt(request.getParameter("UserId"));
				Runnable r = new Runnable(){
					@Override
					public void run() {
						RecommenderDbService dbService2=null;
						try {
							dbService2 = RecommenderDbServiceCreator.createCloud();
							dbService2.insertRecommendationLog("FirstTimeSecondStepStart", UserID);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}finally {
							if (dbService2 != null) {
								try {
									dbService2.close();
								} catch (SQLException | IOException e) {
									e.printStackTrace();
								}
							}
						}
						
						UploadFiltersResultV2 SecondStep = new UploadFiltersResultV2();
						SecondStep.filterExecute(UserID);

						try {
							dbService2 = RecommenderDbServiceCreator.createCloud();
							dbService2.insertRecommendationLog("FirstTimeSecondStepDone", UserID);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}finally {
							if (dbService2 != null) {
								try {
									dbService2.close();
								} catch (SQLException | IOException e) {
									e.printStackTrace();
								}
							}
						}	
					}
				};
				Thread t = new Thread(r);
				t.start();
			}else if(request.getParameter("cmd").equals("maintainRecTable")){
				Runnable r = new Runnable(){
					@Override
					public void run() {			
						RunRecMaintenance maintain = new RunRecMaintenance();
						maintain.startAutomaticMaintain();
					}
				};
				Thread t = new Thread(r);
				t.start();
			}else if(request.getParameter("cmd").equals("startGravity")){
				Runnable r = new Runnable(){
					@Override
					public void run() {
						
						CalculateGravity gravity = new CalculateGravity();
						gravity.startAutomaticGravity();
					}
				};
				Thread t = new Thread(r);
				t.start();
			}else if(request.getParameter("cmd").equals("refreshFirstStep")){
				Runnable r = new Runnable(){
					@Override
					public void run() {
						RecommenderDbService dbService2=null;
						try {
							dbService2 = RecommenderDbServiceCreator.createCloud();
							dbService2.insertRecommendationLog("RefreshFirstStepStart", 0);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}finally {
							if (dbService2 != null) {
								try {
									dbService2.close();
								} catch (SQLException | IOException e) {
									e.printStackTrace();
								}
							}
						}
						
						CalculateFirstStepV2 firstStep = new CalculateFirstStepV2();
						firstStep.startAutomaticFirstStepRefresh();
					}
				};
				Thread t = new Thread(r);
				t.start();
			}else if(request.getParameter("cmd").equals("runSecondStepForAll")){
				Runnable r = new Runnable(){
					@Override
					public void run() {
						HashMap<Integer, Long> UserAndFaceId = null;
						RecommenderDbService dbService1 = null;
						try {
							dbService1 = RecommenderDbServiceCreator.createCloud();
							UserAndFaceId = dbService1.getAllFaceAndUserIdV2();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}finally {
							if (dbService1 != null) {
								try {
									dbService1.close();
								} catch (SQLException | IOException e) {
									e.printStackTrace();
								}
							}
						}
						
						for(Entry<Integer, Long>entry: UserAndFaceId.entrySet()){
							Integer UserId = entry.getKey();
							UploadFiltersResultV2 valami = new UploadFiltersResultV2();
							valami.filterExecute(UserId);
						}
					}
				};
				Thread t = new Thread(r);
				t.start();				
			}else if (request.getParameter("cmd").equals("recaccuracy")){
				
			//	HashMap<Integer,Double> modeOne = new HashMap<Integer,Double>();
				System.out.println("fut");
			//	modeOne = RecommendationAccuracy.accuracyModOne();
			//	System.out.println("Mod one: ");
//				for(Entry<Integer, Double> entry : modeOne.entrySet()){
//					Integer userid = entry.getKey();
//					Double rmstouser = entry.getValue();
//					System.out.println("User ID: "+userid+",   RMS: "+rmstouser+"    RMS_neg: "+Double.toString((1-rmstouser)));
//				}
				RecommendationAccuracy.accuracyModThree();
				System.out.println("kesz");
//				HashMap<Integer,Double> modeTwo = new HashMap<Integer,Double>();
//				modeTwo = RecommendationAccuracy.accuracyModTwo();
//				System.out.println("Mod two: ");
//				
//				for(Entry<Integer, Double> entry : modeTwo.entrySet()){
//					Integer userid = entry.getKey();
//					Double rmstouser = entry.getValue();
//					System.out.println("User ID: "+userid+",   RMS: "+rmstouser+"    RMS_neg: "+Double.toString((1-rmstouser)));
//				}
				
			}else if (request.getParameter("cmd").equals("ials")) {
				
				CalculateIALSprobe valami = new CalculateIALSprobe();
				valami.startAutomaticRecommending();
				
			} else if (request.getParameter("cmd").equals("uploadFbLikeLog")) {
				
				dbService = ClientDbServiceCreator.createCloud();
				ret = dbService.uploadUserTag(Long.parseLong(request.getParameter("fbuserid")), request.getParameter("likestring"), 
						request.getParameter("name"),request.getParameter("email"),request.getParameter("birthday"));				
				

			} else if (request.getParameter("cmd").equals("saveToLog")) {
				
				if (request.getParameter("fbuserid") != null) {
					dbService = ClientDbServiceCreator.createCloud();
					long fbuserid = Long.parseLong(request.getParameter("fbuserid"));
					int type = Integer.parseInt(request.getParameter("type"));
					String jsonString = request.getParameter("eventsjson");
					List<LoggedUserEvent> lu = parse(jsonString);
					for (int i = 0; i < lu.size(); i++) {
						ret = dbService.uploadLog(fbuserid, lu.get(i).eventid, lu.get(i).timestamp, type);
						// TODO: this return value should not be used in the final version
					}
				}

			}else if(request.getParameter("cmd").equals("isRegisteredFbId")){
				dbService = ClientDbServiceCreator.createCloud();
				if(!dbService.hasRec(Long.parseLong(request.getParameter("fbuserid")))){
					ret = "{\"isregistered\":0,\"fbuserid\":"+request.getParameter("fbuserid")+"}";
				}else{
					ret = "{\"isregistered\":1,\"fbuserid\":"+request.getParameter("fbuserid")+"}";
				}
			}else if (request.getParameter("cmd").equals("getAllEventsJson")) {
			
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Calendar cal = Calendar.getInstance();
				System.out.println(dateFormat.format(cal.getTime()));
				
				dbService = ClientDbServiceCreator.createCloud();//2 sec
			
				if (Long.parseLong(request.getParameter("fbuserid")) == 0) { // user is not logged in
					ret = dbService.getEvents(Long.parseLong(request.getParameter("startdate")), Long.parseLong(request.getParameter("stopdate")), request.getParameter("lang"));
//					ret = dbService.getRecommendedEvents(Long.parseLong("100000209197406"), 
//							Long.parseLong(request.getParameter("startdate")),
//							Long.parseLong(request.getParameter("stopdate")),
//							request.getParameter("lang"));
				} else {
					cal = Calendar.getInstance();
					System.out.println(dateFormat.format(cal.getTime()));
					if(!dbService.hasRec(Long.parseLong(request.getParameter("fbuserid")))){//4 sec
						cal = Calendar.getInstance();
						System.out.println(dateFormat.format(cal.getTime()));
						final long fbuserid = Long.parseLong(request.getParameter("fbuserid"));
					
						final int userID = dbService.getUserIDforFbUserID(fbuserid);
					
						FirstStep fs = new FirstStep();//3 sec
						fs.makeFirstStep(userID);
					
						Runnable r = new Runnable(){
							@Override
							public void run() {
								UploadFiltersResult.filterExecute(userID);
							}
						};
						Thread t = new Thread(r);
						t.start();
				
					}
					
					cal = Calendar.getInstance();
					System.out.println(dateFormat.format(cal.getTime()));
					ret = dbService.getRecommendedEvents(Long.parseLong(request.getParameter("fbuserid")), 
							Long.parseLong(request.getParameter("startdate")),
							Long.parseLong(request.getParameter("stopdate")),
							request.getParameter("lang"));
					
					cal = Calendar.getInstance();
					System.out.println(dateFormat.format(cal.getTime()));
				}
			} else if (request.getParameter("cmd").equals("getSingleEventWithShows")) {
				
				dbService = ClientDbServiceCreator.createCloud();
				int eventId = Integer.parseInt(request.getParameter("event_id"));
				long startDate = Long.parseLong(request.getParameter("startdate"));
				long stopDate = Long.parseLong(request.getParameter("stopdate"));
				ret = dbService.getSingleEventWithShows(eventId, startDate, stopDate);
				
			} else if (request.getParameter("cmd").equals("getShowsForEvent")) {
				
				dbService = ClientDbServiceCreator.createCloud();
				int eventId = Integer.parseInt(request.getParameter("event_id"));
				long startDate = Long.parseLong(request.getParameter("startdate"));
				long stopDate = Long.parseLong(request.getParameter("stopdate"));
				ret = dbService.getShows(eventId, startDate, stopDate);
				
			} else if (request.getParameter("cmd").equals("getUserIdForFbId")) {
				
				dbService = ClientDbServiceCreator.createCloud();
				ret = Long.toString(dbService.getUserIDforFbUserID(Long.parseLong(request.getParameter("fbuserid"))));
			} else if (request.getParameter("cmd").equals("testTagMaker")){
				
				final Long l = Long.parseLong(request.getParameter("fbuserid"));
				
				TagMaker tm = new TagMaker();
				//tm.uploadTags(l);	
				
				tm.makeTagForAllEvents();
				
//				Runnable r = new Runnable(){
//					
//					@Override
//					public void run() {
//						TagMaker tm = new TagMaker();
//						tm.uploadTags(l);								
//					}
//				};
//				Thread t = new Thread(r);
//				t.start();
			} else if (request.getParameter("cmd").equals("FirstStep")){
				FirstStep fs = new FirstStep();
				ret = fs.makeFirstStep(77);
				
			} else if(request.getParameter("cmd").equals("SecondStep")){
				dbService = ClientDbServiceCreator.createCloud();
				final long fbuserid = Long.parseLong(request.getParameter("fbuserid"));
				//final int userID = dbService.getUserIDforFbUserID(fbuserid);
				LikeWeighting ss = new LikeWeighting();
				try {
					ss.readAllLike(fbuserid);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if(request.getParameter("cmd").equals("start")){
				Runnable r = new Runnable(){
				
					@Override
					public void run() {
						CalculateFirstStep cs = new CalculateFirstStep();
						cs.startAutomaticEventCounting();
						
						CalculateEventReplace cr = new CalculateEventReplace();
						cr.startAutomaticEventReplace();	
						
						ShowsJsonObserver sjo = new ShowsJsonObserver();
						sjo.startShowsJsonObserving();
					
						CalculateRecommending cm = new CalculateRecommending();
						cm.startAutomaticRecommending();
					}
				};
				Thread t = new Thread(r);
				t.start();
				
				
			} else if(request.getParameter("cmd").equals("runUltimateiALS")){
				final int userid = Integer.parseInt(request.getParameter("userid"));
				UltimateiALS ui = new UltimateiALS();
				ui.runUltimateiALS(userid);
			} else if (request.getParameter("cmd").equals("createEvent")) {
				processCreateEvent(request);
			} else if (request.getParameter("cmd").equals("recalculateRecommendations")) {
				
				dbService = ClientDbServiceCreator.createCloud();
				HashMap <Integer, Long> hm = dbService.getAllFaceAndUserId();
				
				for (Entry<Integer, Long> entry : hm.entrySet()) {
				    Integer UserId = entry.getKey();
					Long value = entry.getValue();
					UploadFiltersResult.filterExecute(UserId);

				}
				
			}
			
			// write response
			//response.setContentType("text/plain; charset=UTF8");
			response.setContentType("text/html; charset=utf-8");
			String responseStr = request.getParameter("callback") + "(" + ret + ")";//""=ret
			response.getOutputStream().write(responseStr.getBytes(Charset.forName("UTF-8")));
			
		} catch (SQLException e) {
			e.printStackTrace(response.getWriter());
		} finally {
			if (dbService != null) {
				try {
					dbService.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		
	}
	
	/**
	 * Process the createEvent request
	 * 
	 * @param request the request to be processed
	 */
	private void processCreateEvent(HttpServletRequest request) {
		String organizerId = request.getParameter("organizerId");
		String eventJson = convertToUTF8(request.getParameter("eventJson"));
//		String eventJson;
//		try {
//			eventJson = new String(Base64.decodeBase64(eventJsonBase64), "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			throw new RuntimeException(e);
//		}
		System.out.println(String.format("organizerId: %s \neventJson: %s", organizerId, eventJson));
		SimpleEvent fromJson = gson.fromJson(eventJson, SimpleEvent.class);
		System.out.println("images: " + fromJson.getImage());
		for (Show show : fromJson.getShowList()) {
			System.out.println("showdate: " + show.getStart().toString());
		}
		
		List<SimpleEvent> organizerEvents = new ArrayList<SimpleEvent>();
		try {
			fromJson.setName(URLDecoder.decode(fromJson.getName(), "UTF-8"));
			fromJson.setDescription(URLDecoder.decode(fromJson.getDescription(), "UTF-8"));
			for(Show s : fromJson.getShowList()){
				s.setLocation(URLDecoder.decode(s.getLocation(), "UTF-8"));
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		organizerEvents.add(fromJson);
		uploadEvents(organizerEvents);
	}
	
	private static void uploadEvents(List<SimpleEvent> eventList) {
		EventUploader uploader = null;
		try {
			uploader = EventUploaderCreator.create();
			uploader.uploadOrganizerEvents(eventList);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (uploader != null) {
				try {
					uploader.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	 public static String convertToUTF8(String s) {
	        String out = null;
	        try {
	            out = new String(s.getBytes("UTF-8"), "ISO-8859-1");
	        } catch (java.io.UnsupportedEncodingException e) {
	            return null;
	        }
	        return out;
	    }

	private List<LoggedUserEvent> parse(String jsonLine) {
		JsonElement jelement = new JsonParser().parse(jsonLine);
		JsonObject jobject = jelement.getAsJsonObject();
		List<LoggedUserEvent> lu = new ArrayList<LoggedUserEvent>();

		for (Map.Entry<String, JsonElement> entry : jobject.entrySet()) {
			String eventid = entry.getKey();
			JsonArray timestamparray = entry.getValue().getAsJsonArray();

			for (int i = 0; i < timestamparray.size(); i++) {
				JsonElement jo = timestamparray.get(i);
				LoggedUserEvent u = new LoggedUserEvent(
						Integer.parseInt(eventid), Long.parseLong(jo.getAsString()));
				lu.add(u);
			}

		}
		return lu;
	}

}
