package com.incredibles.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.joda.time.DateTimeZone;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.JSONException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.incredibles.data.Event;
import com.incredibles.data.FilmEvent;
import com.incredibles.data.Like;
import com.incredibles.data.LogTableInfoClass;
import com.incredibles.data.LogTableTypes;
import com.incredibles.data.PtEvent;
import com.incredibles.data.PtEvent.PtEventType;
import com.incredibles.data.Rec;
import com.incredibles.data.Show;
import com.incredibles.data.SimpleEvent;
import com.incredibles.data.ToJsonShow;
import com.incredibles.data.MetadataFromThirdParty;
import com.sun.org.apache.xml.internal.security.utils.Base64;


/*AMIT MEG KELLENE CSIN�LNI:
 * - com.incredibles.data mapp�ban rendesen meg�rni az adat oszt�lyokat. pl: Event..
 * - */

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
	
	// methods called depending on mode
//
//	public void uploadRecData() throws SQLException {
//		String selectQuery = "TRUNCATE TABLE TEST_REC";
//		Statement statement = null;
//		try {
//			statement = conn.createStatement();
//			statement.executeUpdate(selectQuery);
//		} finally {
//			if (statement != null) {
//				statement.close();
//			}
//		}
//		
//	}
	
					// methods inherited from RecommenderDbService
	
	/*AZ EventReplacerV1 hivja meg, ami a gravity; meg az ials fogja hasznalni*/
	/*true - has next line & set Logline info*/
	@Override
	public boolean getNextLogLine(LogLine line) throws SQLException {	
		boolean ret = false;	/*default false*/
		Statement getLogStatement = null;
		try {
			System.out.println("try dbmanager");
			if (logResultSet == null) {	/*logResulSet, protected CloudDBManager class variable;*/
			//	String selectQuery = "SELECT TL.ID, TL.TIMESTAMP, TL.EVENTID, TL.USERID, TL.TYPE "+
			//	"FROM TEST_LOG AS TL, TEST_SHOW AS TS "+
			//	"WHERE TL.EVENTID = TS.EVENT_ID "+
			//	"AND TS.GO > UNIX_TIMESTAMP( NOW( ) ) *1000 "+	/*ennek �gy nem sok �rtelme van, ide pont nem kellene n�zni a show d�tum�t*/
			//	"GROUP BY TL.ID, TL.TIMESTAMP, TL.EVENTID, TL.USERID, TL.TYPE";
				String selectQuery = "SELECT ID,TIMESTAMP,EVENTID,USERID,TYPE FROM TEST_LOG";
				getLogStatement = conn.createStatement();
				System.out.println("try lekerdes start");
				logResultSet = getLogStatement.executeQuery(selectQuery);
				System.out.println("try lekerdes end");
			}
			if(logResultSet.next()){/*logResSet not null; info from server recived*/
				ret = true;	/*returns true if have netx line*/
				line.userId = logResultSet.getInt("USERID");
				line.eventId = logResultSet.getInt("EVENTID");
				line.timeStamp = new Date(logResultSet.getLong("TIMESTAMP"));
				line.type = logResultSet.getInt("TYPE");
				//line.timeStamp = new Date(1370532964*1000);
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
	
	/*nem tudom mire haszn�ljuk*/
	@Override
	public void deleteAllTempCount() throws SQLException {
		String selectQuery = "TRUNCATE TABLE TEST_TEMPCOUNT";
		Statement statement = null;
		try {
			statement = conn.createStatement();
			statement.executeUpdate(selectQuery);
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
		
	}
	
	/*FirstStep.makefirststep; kezdeti eventeket atmasoljuk az uj usernek*/
	@Override
	public void copyFromTempToRec(int userid) throws SQLException {
		String selectQuery = "CALL copyFromTempToRecP("+Integer.toString(userid)+");";
		Statement statement = null;
		try {
			statement = conn.createStatement();
			statement.executeUpdate(selectQuery);
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
		
	}
	
	/*CalculateFirstStep*/
	@Override
	public void uploadBatchTempRec(List<Rec> rec) throws SQLException{
		//PreparedStatement prepStmt = conn.prepareStatement("INSERT INTO TEST_TEMPCOUNT (USERID, EVENTID, RANK) VALUES (?, ?, ?)");
		PreparedStatement prepStmt = conn.prepareStatement("CALL updateTempRecTable15(?,?,?)");
		for(Rec r : rec){
			prepStmt.setInt(1,r.getUserID());                 
			prepStmt.setInt(2,r.getEventID());
			prepStmt.setDouble(3, r.getRank());
			prepStmt.addBatch();
			
		}
		
		int [] numUpdates=prepStmt.executeBatch();
		
		
		if (prepStmt != null) {
			prepStmt.close();
		}
	}
	
	/**Returns list of legit events in a time interval
	 * Use integer Date*/
	public List<Integer> getLegitEventsNumber(Date from, Date to){
		PreparedStatement getEventStatement = null;
		ResultSet eventResult = null;
		List<Integer> eventList = new ArrayList<Integer>();
		Integer intDateFrom = getCurrentDate(from.getTime());
		Integer intDateTo = getCurrentDate(to.getTime());
		String queryStr = "SELECT ID FROM TEST_EVENT WHERE LANG_CODE='HU' AND (SELECT EVENT_ID FROM TEST_SHOW WHERE TEST_SHOW.day >= ? AND TEST_SHOW.day <= ? AND TEST_EVENT.ID=EVENT_ID LIMIT 1) ";
		try {
			getEventStatement = conn.prepareStatement(queryStr);
			getEventStatement.setInt(1, intDateFrom);
			getEventStatement.setInt(2, intDateTo);
			eventResult = getEventStatement.executeQuery();
			while(eventResult.next()){
				Integer eventID = eventResult.getInt("ID");
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
		
	@Override
	public void close() throws IOException, SQLException {
		if (conn != null) {
			conn.close();
		} else {
			System.err.println(">> Tried to close the connection, but it was null.");
		}
	}

	/*kiszamolt rankokat adott userre lekeri*/
	/*Returns event rank in TEST_RECP*/
	@Override
	public HashMap<Integer,Double> getRankforEvent(Integer userid) throws SQLException{	
		HashMap<Integer,Double> s = new HashMap<Integer,Double>();
		PreparedStatement getShowsStatement = null;
		ResultSet showResults = null;
		int bottom = userid*1000;
		int top = (userid+1)*1000;
		try {		
			String queryStr = "SELECT EVENTID,RANK FROM TEST_RECP WHERE ID>=? AND ID<?";
			getShowsStatement = conn.prepareStatement(queryStr);
			getShowsStatement.setInt(1, bottom);
			getShowsStatement.setInt(2, top);
			showResults = getShowsStatement.executeQuery();		
			while (showResults.next()) {
				Double rank = showResults.getDouble("RANK");
				Integer id = showResults.getInt("EVENTID");
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
	
	/*Needed because of Recommender filter : Corvinus; ... */
	/**NEM LONG HANEM INTEGER PL: 20131123 !!!!!!!!!!!!!!!!!!!!!*/
	public List<HashMap<Integer,String>> getExistEventDescAndName(long from) throws SQLException{
		HashMap<Integer,String> eventDesc = new HashMap<Integer,String>();
		HashMap<Integer,String> eventName = new HashMap<Integer,String>();
		Integer intDateFrom = getCurrentDate(from);
		List<HashMap<Integer,String>> hashmapList = null;
		PreparedStatement getShowsStatement = null;
		ResultSet showResults = null;	
		try {				
			String queryStr = "SELECT ID,DESCRIPTION,NAME FROM TEST_EVENT WHERE LANG_CODE='HU' AND (SELECT EVENT_ID FROM TEST_SHOW WHERE TEST_SHOW.day >= ? AND TEST_EVENT.ID=EVENT_ID LIMIT 1) ";
			getShowsStatement = conn.prepareStatement(queryStr);
			getShowsStatement.setInt(1, intDateFrom);
			showResults = getShowsStatement.executeQuery();
			while(showResults.next()){
				Integer ot = showResults.getInt("ID");	
				String description = showResults.getString("DESCRIPTION");
				String name = showResults.getString("NAME");
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
			if (showResults != null)  {
				showResults.close();
			}
			if (getShowsStatement != null) {
				getShowsStatement.close();
			}
		}
		return hashmapList;
	}
		
	
	/*Delete from log table with logtableinfoclass class*/
	public void deleteInfoFromLog(LogTableInfoClass deleteThis) throws SQLException{
		Integer userID = deleteThis.getUserID();
		Integer eventID = deleteThis.getEventID();
		Integer type = deleteThis.getLogType();
		List<Long> timeList = deleteThis.getTimestamp();
		String deleteLog = "DELETE FROM TEST_LOG WHERE USERID=? AND EVENTID=? AND TYPE = ? AND TIMESTAMP = ?";
		PreparedStatement deleteLogStatement = null;
		try {
			for(int i=0; i<timeList.size(); i++){
				deleteLogStatement = conn.prepareStatement(deleteLog);
				deleteLogStatement.setInt(1, userID);
				deleteLogStatement.setInt(2, eventID);
				deleteLogStatement.setInt(3, type);
				deleteLogStatement.setLong(4, timeList.get(i));
				deleteLogStatement.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}finally {
			if (deleteLogStatement != null) {
				deleteLogStatement.close();
			}
		}
	}
	
	/*nem hasznaljuk kulon tipust hasznal; logsort kellett volna. log tabla szerkezete kerdeses: user egyedek azon bel�l timestamp eventid log type ?*/
	/*Read from log table with use of logtableinfoclass class*/
	public List<LogTableInfoClass> getInfoFromLog(int type,int userId) throws SQLException{
		List<LogTableInfoClass> infoFromLog = new ArrayList<LogTableInfoClass>();
		PreparedStatement getShowsStatement = null;
		ResultSet showResults = null;
		try {
			String queryStr="SELECT EVENTID,TIMESTAMP FROM TEST_LOG WHERE TYPE = ? AND USERID= ?";
			getShowsStatement = conn.prepareStatement(queryStr);
			getShowsStatement.setInt(1, type);
			getShowsStatement.setInt(2, userId);
			showResults = getShowsStatement.executeQuery();
			while(showResults.next()){
				boolean trigger = false;
				Integer eventId = showResults.getInt("EVENTID");	
				Long timestamp = showResults.getLong("TIMESTAMP");
				if(infoFromLog.isEmpty()){
					LogTableInfoClass object = new LogTableInfoClass(type, userId, eventId, timestamp);
					infoFromLog.add(object);
				}else{
					for(int i = 0; i < (int) infoFromLog.size(); i++){
						int num = infoFromLog.get(i).getEventID();
						if(eventId.equals(infoFromLog.get(i).getEventID())){
							infoFromLog.get(i).addTimestamp(timestamp);
							trigger = true;
							break;
						}
					}
					if(trigger == false){
						infoFromLog.add(new LogTableInfoClass(type, userId, eventId, timestamp));
					}
				}
			}	
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}finally {
			if (getShowsStatement != null) {
				getShowsStatement.close();
			}
		}
		return infoFromLog;
	}
	
	
	
	public List<LogTableInfoClass> getInfoFromLogV2(int type,int userId) throws SQLException{
		List<LogTableInfoClass> infoFromLog = new ArrayList<LogTableInfoClass>();
		PreparedStatement getShowsStatement = null;
		ResultSet showResults = null;
		try {
			String queryStr="SELECT EventId,createdAt FROM EventLogs WHERE type = ? AND UserId= ?";
			getShowsStatement = conn.prepareStatement(queryStr);
			getShowsStatement.setInt(1, type);
			getShowsStatement.setInt(2, userId);
			showResults = getShowsStatement.executeQuery();
			while(showResults.next()){
				boolean trigger = false;
				Integer eventId = showResults.getInt("EventId");	
				Long timestamp = showResults.getLong("createdAt");
				if(infoFromLog.isEmpty()){
					LogTableInfoClass object = new LogTableInfoClass(type, userId, eventId, timestamp);
					infoFromLog.add(object);
				}else{
					for(int i = 0; i < (int) infoFromLog.size(); i++){
						int num = infoFromLog.get(i).getEventID();
						if(eventId.equals(infoFromLog.get(i).getEventID())){
							infoFromLog.get(i).addTimestamp(timestamp);
							trigger = true;
							break;
						}
					}
					if(trigger == false){
						infoFromLog.add(new LogTableInfoClass(type, userId, eventId, timestamp));
					}
				}
			}	
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}finally {
			if (getShowsStatement != null) {
				getShowsStatement.close();
			}
		}
		return infoFromLog;
	}
	
	
	
					// methods inherited from ClientDbService
	
	/*ez valszeg az uj user adatait viszi fel illetve let�lti a likejait; at kell irni*/
	@Override
	public String uploadUserTag(long fbuserid,String jsonString, String name, String email, String birthdate) throws SQLException {
		
		Long.toString(getUserIDforFbUserID(fbuserid));
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		
		java.sql.Date bbdate = null;
		try {
	 
			Date bdate = formatter.parse(birthdate);
			System.out.println(bdate);
			System.out.println(formatter.format(bdate));
			bbdate = new java.sql.Date(bdate.getTime());
	 
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
		
		String updateUserQuery = 
				"UPDATE TEST_USER SET NAME = ?,EMAIL = ?, FACEBOOKID = ?, BIRTHDATE = ? WHERE ID = ?";
		//System.out.println(selectQuery);
		PreparedStatement updateUserStatement = null;
		PreparedStatement insertLikeStatement = null;
		PreparedStatement insertUserlikeStatement = null;
		String insertLikeQuery = "query";
		String insertUserlikeQuery = "query";
		
		int likeJsonSize = 0;
	
		try {
			updateUserStatement = conn.prepareStatement(updateUserQuery);
			updateUserStatement.setString(1, name);
			updateUserStatement.setString(2, email);
			updateUserStatement.setLong(3, fbuserid);
			updateUserStatement.setDate(4, bbdate);
			long userIdTmp = getUserIDforFbUserID(fbuserid);
			updateUserStatement.setLong(5, userIdTmp);
			insertLikeQuery = "INSERT INTO TEST_LIKE (ID,NAME,CATEGORY) VALUES(?, ?, ?)";
			insertLikeStatement = conn.prepareStatement(insertLikeQuery);
			insertUserlikeQuery = "INSERT INTO TEST_USERLIKESWITCH (USERID,LIKEID,FBUSERID,CREATEDTIME) VALUES (?, ?, ?, ?)";
			insertUserlikeStatement = conn.prepareStatement(insertUserlikeQuery);
			
			if (!CloudDbManager.DEBUG) {
				
				updateUserStatement.executeUpdate();
				
			} else { // DEBUG
				try {
					updateUserStatement.executeUpdate();
				} catch (SQLException e) {
					String reason = String.format(
							"Update user params: \n\tname = %s, email = %s, fbuserid = %d, userID = %d\n", name, email, fbuserid, userIdTmp
							);
					throw new SQLException(reason, e);
				}
			}
			
			
			List<LikeJson> lj = parse(jsonString);
			likeJsonSize = lj.size();
			
			for (int i = 0; i < lj.size(); i++) {
				LikeJson clj = lj.get(i);
				try{
					insertLikeStatement.clearParameters();
					insertLikeStatement.setLong(1, clj.id);
					insertLikeStatement.setString(2, clj.name);
					insertLikeStatement.setString(3, clj.category);
					insertLikeStatement.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				try{
					insertUserlikeStatement.clearParameters();
					insertUserlikeStatement.setLong(1, getUserIDforFbUserID(fbuserid));
					insertUserlikeStatement.setLong(2, clj.id);
					insertUserlikeStatement.setLong(3, fbuserid);
					insertUserlikeStatement.setLong(4, clj.createdtime);
					insertUserlikeStatement.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		} finally {
			
			if (updateUserStatement != null) {
				updateUserStatement.close();
			}
			if (insertLikeStatement != null) {
				insertLikeStatement.close();
			}
			if (insertUserlikeStatement != null) {
				insertUserlikeStatement.close();
			}
		}
		
		//return updateUserQuery+"----"+insertLikeQuery+"----"+insertUserlikeQuery+"----"+jsonString+"----"+Integer.toString(likeJsonSize);
		return null;
	}
	
	
	public List<LikeJson> parse(String jsonLine) {
		JsonElement jelement = new JsonParser().parse(jsonLine);
		JsonArray likeArray = jelement.getAsJsonArray();
		List<LikeJson> lu = new ArrayList<LikeJson>();
		for (int i = 0; i < likeArray.size(); i++) {
			JsonObject jo = likeArray.get(i).getAsJsonObject();
			String category = jo.get("category").getAsString();
			String name = jo.get("name").getAsString();
			String created_time = jo.get("created_time").getAsString();

			long id = Long.parseLong(jo.get("id").getAsString());
			created_time = created_time.substring(0, created_time.length() - 6);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			Date date = null;
			try {
				date = formatter.parse(created_time);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			long created_timestamp = date.getTime();
			LikeJson u = new LikeJson(category, name, id, created_timestamp);
			lu.add(u);
		}
		return lu;
	}

	/*log sor feltoltes; valszeg mas strukturaban kell majd elvegezni a feltoltest; getRecommendedEventsFromDb;getRec.EventsWithShowJson...*/
	@Override
	public String uploadLog(long fbuserid, int eventid, long timestamp, int type) throws SQLException{
		String selectQuery = 
				"INSERT INTO TEST_LOG (USERID, EVENTID, TIMESTAMP, TYPE) VALUES (?, ?, ?, ?)";
		PreparedStatement insertLogStatement = null;
		try {
			insertLogStatement = conn.prepareStatement(selectQuery);
			insertLogStatement.setLong(1, getUserIDforFbUserID(fbuserid));
			insertLogStatement.setInt(2, eventid);
			insertLogStatement.setLong(3, timestamp);
			insertLogStatement.setInt(4, type);
			insertLogStatement.executeUpdate();
		} finally {
			if (insertLogStatement != null) {
				insertLogStatement.close();
			}
		}
		return null;
	}
	
	/*Szukseges majd az atiras; uploadTags hasznalja. TAgeket tolt fel userhez: regi 2.nd step*/
	@Override
	public String insertCMSLog(String type) throws SQLException{
		String selectQuery = 
				"INSERT INTO TEST_CMS_LOG (EVENT_ID,USER) VALUES (?,?)";
		PreparedStatement insertLogStatement = null;
		try {
			insertLogStatement = conn.prepareStatement(selectQuery);
			insertLogStatement.setInt(1, 0);
			insertLogStatement.setString(2, type);
			insertLogStatement.executeUpdate();
		} finally {
			if (insertLogStatement != null) {
				insertLogStatement.close();
			}
		}
		return null;
	}
	
	public String insertRECLog(String type, Integer userID) throws SQLException{
		String selectQuery = 
				"INSERT INTO TEST_REC_LOG (USER_ID,COMMENT) VALUES (?,?)";
		PreparedStatement insertLogStatement = null;
		try {
			insertLogStatement = conn.prepareStatement(selectQuery);
			insertLogStatement.setInt(1, userID);
			insertLogStatement.setString(2, type);
			insertLogStatement.executeUpdate();
		} finally {
			if (insertLogStatement != null) {
				insertLogStatement.close();
			}
		}
		return null;
	}
	
	@Override
	public void uploadEventTag(List<String> s) throws SQLException{
		String selectQuery = 
				"INSERT INTO TEST_TAG (NAME) VALUES (?)";
		PreparedStatement insertLogStatement = null;
		try {
			for(int i=0;i<s.size();i++){
				insertLogStatement = conn.prepareStatement(selectQuery);
				insertLogStatement.setString(1, s.get(i));
				insertLogStatement.executeUpdate();
			}
		} finally {
			if (insertLogStatement != null) {
				insertLogStatement.close();
			}
		}
	}
	
	/*ultimate ials; talan az elejen masolja at az eventeket a rank tablaba; cache-elve lesznek az adatok, mas lesz*/
	@Override
	public void copyNewEventsToOldUsers() throws SQLException {
		String selectQuery = "CALL copyFromTestTCtoTestRecWhatNotInTestRec15();";
		Statement statement = null;
		try {
			statement = conn.createStatement();
			statement.executeUpdate(selectQuery);
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}
	
	/*ezzel toltjuk fel a sorrendberendezett rank ertekeket eventid-val; valtozik, ha megvan a logtabla helyettesitoje*/
	@Override
	public void updateRecP(int userid, LinkedHashMap<Integer, Double> topThousandEvent){
		try {
			if(hasRec(getFbUserIDforUserID(userid))){
				String updateQuery = "UPDATE `TEST_RECP` SET `EVENTID`=?,`RANK`=? WHERE ID=?";
				PreparedStatement updateStatement = null;
				try {
					updateStatement = conn.prepareStatement(updateQuery);
					
					int correctid = userid*1000;
					for (Entry<Integer, Double> entry : topThousandEvent.entrySet()) {
						
					    Integer eventid = entry.getKey();
					    Double rank = entry.getValue();
					    
					    updateStatement.setInt(1, eventid);
					    updateStatement.setDouble(2, rank);
					    updateStatement.setInt(3, correctid);
					    updateStatement.addBatch();
						System.out.println("ID:" +correctid+ "eventid:" +eventid+ "rank:" +rank);
						correctid++;
						
					}
					
					for(int i = correctid;i<(userid+1)*1000;i++){
						updateStatement.setInt(1, 0);
					    updateStatement.setDouble(2, 0);
					    updateStatement.setInt(3, correctid);
					    updateStatement.addBatch();
						System.out.println("ID:" +correctid+ "eventid:" +0+ "rank:" +0);
						correctid++;
					}
					
					int [] numUpdates=updateStatement.executeBatch();

				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					if (updateStatement != null) {
						try {
							updateStatement.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
			}else{
				String deleteRec = "DELETE FROM TEST_RECP WHERE ID>=? AND ID<?";
				PreparedStatement deleteRecStatement = null;
				try {
					deleteRecStatement = conn.prepareStatement(deleteRec);
					deleteRecStatement.setInt(1, userid*1000);
					deleteRecStatement.setInt(2, (userid+1)*1000);
					deleteRecStatement.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					if (deleteRecStatement != null) {
						try {
							deleteRecStatement.close();
							
							
							String insertQuery = "INSERT INTO `TEST_RECP`(`ID`, `EVENTID`, `RANK`) VALUES (?, ?, ?)";
							PreparedStatement insertStatement = null;
							try {
								insertStatement = conn.prepareStatement(insertQuery);
								
								int correctid = userid*1000;
								for (Entry<Integer, Double> entry : topThousandEvent.entrySet()) {
								    Integer eventid = entry.getKey();
								    Double rank = entry.getValue();
								    
									insertStatement.setInt(1, correctid);
									insertStatement.setInt(2, eventid);
									insertStatement.setDouble(3, rank);
									insertStatement.addBatch();
									System.out.println("ID:" +correctid+ "eventid:" +eventid+ "rank:" +rank);
									correctid++;
								}
								for (int i = correctid;i<(userid+1)*1000;i++) {
			
								    
									insertStatement.setInt(1, correctid);
									insertStatement.setInt(2, 0);
									insertStatement.setDouble(3, 0);
									insertStatement.addBatch();
									System.out.println("ID:" +correctid+ "eventid:" +0+ "rank:" +0);
									correctid++;
								}
								
								int [] numUpdates=insertStatement.executeBatch();

							} catch (SQLException e) {
								e.printStackTrace();
							} finally {
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
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		

		
	}
		
	
	/*Kell egy olyan rankfelt�lt�, ami az �j DB strukt�r�hoz illik. Nem kell csak 1000 db esem�nyt felt�lteni.*/
	
	/*nem kell; minden event le�r�s�t adja vissza f�ggetlen�l mindentol*/
	@Override
	public HashMap<Integer,String> getAllEventDesc() throws SQLException{
		
		HashMap<Integer,String> s = new HashMap<Integer,String>();

		PreparedStatement getShowsStatement = null;
		ResultSet showResults = null;
		
		try {
			
			String queryStr = "SELECT ID,DESCRIPTION FROM TEST_EVENT";
			getShowsStatement = conn.prepareStatement(queryStr);
			showResults = getShowsStatement.executeQuery();
			
			while (showResults.next()) {
				String description = showResults.getString(EventColumn.DESCRIPTION);
				Integer id = showResults.getInt(EventColumn.ID);
				s.put(id, description);
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
	
	/*nem kell; discriminatort adja vissza; ajanlas*/
	@Override
	public HashMap<Integer,String> getEventDiscriminatorFromDate(long fromDate) throws SQLException{
		
		HashMap<Integer,String> s = new HashMap<Integer,String>();

		PreparedStatement getShowsStatement = null;
		ResultSet showResults = null;
		
		try {
			/*csak a sz�ks�ges eventek adatait kellene let�lteni, bemenetnek kell a nap
			 * + ez a k�d : SELECT ID,DESCRIPTION,NAME FROM TEST_EVENT WHERE (SELECT EVENT_ID FROM TEST_SHOW WHERE TEST_SHOW.go >= ? AND TEST_EVENT.ID=EVENT_ID LIMIT 1) */
			String queryStr = "SELECT ID,DISCRIMINATOR FROM TEST_EVENT WHERE LANG_CODE='HU' AND (SELECT EVENT_ID FROM TEST_SHOW WHERE TEST_SHOW.day >= ? AND TEST_EVENT.ID=EVENT_ID LIMIT 1)  ";
			getShowsStatement = conn.prepareStatement(queryStr);
			getShowsStatement.setLong(1, fromDate);
			showResults = getShowsStatement.executeQuery();		
			while (showResults.next()) {
				String discriminator = showResults.getString(EventColumn.DISCRIMINATOR);
				Integer id = showResults.getInt(EventColumn.ID);
				s.put(id, discriminator);
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
	
	/*nem kell;event genre-jet adja vissza, hasznaljuk ajanlas*/
	public HashMap<Integer, String> getEventsGenreFromDate(long fromDate) throws SQLException{
		HashMap<Integer, String> eventGenre = null;
		PreparedStatement getShowsStatement = null;
		ResultSet showResults = null;		
		try {	
			eventGenre = new HashMap<Integer, String>();
			
			String queryStr = "SELECT ID,GENRE FROM TEST_EVENT WHERE LANG_CODE='HU' AND (SELECT EVENT_ID FROM TEST_SHOW WHERE TEST_SHOW.day >= ? AND TEST_EVENT.ID=EVENT_ID LIMIT 1)  ";		
			getShowsStatement = conn.prepareStatement(queryStr);
			getShowsStatement.setLong(1, fromDate);
			showResults = getShowsStatement.executeQuery();		
			while (showResults.next()) {
				String genre = showResults.getString(EventColumn.GENRE);
				Integer id = showResults.getInt(EventColumn.ID);
				eventGenre.put(id, genre);
			}
		} finally {
			if (showResults != null)  {
				showResults.close();
			}
			if (getShowsStatement != null) {
				getShowsStatement.close();
			}
		}	
		return eventGenre;		
	}
	
	/*maketagforallevents(); */
	@Override
	public void uploadAllEventsTag(HashMap<Integer,Integer> felt) throws SQLException{
		String selectQuery = 
				"INSERT INTO TEST_EVENTTAGSWITCH (EVENTID,TAGID) VALUES (?, ?)";
		PreparedStatement insertLogStatement = null;
		try {
			for (Entry<Integer, Integer> entry : felt.entrySet()) {
		        Integer eventid = entry.getKey();
		        Integer tagid = entry.getValue();
				insertLogStatement = conn.prepareStatement(selectQuery);
				
				insertLogStatement.setInt(1, eventid);
				insertLogStatement.setInt(2, tagid);
				insertLogStatement.executeUpdate();
			}
		} finally {
			if (insertLogStatement != null) {
				insertLogStatement.close();
			}
		}
	}
	
	/*nem kell*/
	@Override
	public LinkedHashMap<Integer,Integer> getEventTagSwitch() throws SQLException{
		
		LinkedHashMap <Integer,Integer> lhm = new LinkedHashMap<Integer,Integer>();
		
		PreparedStatement getShowsStatement = null;
		ResultSet showResults = null;
		
		try {
			
			String queryStr = "SELECT EVENTID,TAGID FROM TEST_EVENTTAGSWITCH";
			getShowsStatement = conn.prepareStatement(queryStr);
			showResults = getShowsStatement.executeQuery();
			
			while (showResults.next()) {
				Integer eventid = showResults.getInt("EVENTID");
				Integer tagid = showResults.getInt("TAGID");
				lhm.put(eventid,tagid);
			}
			
		} finally {
			if (showResults != null)  {
				showResults.close();
			}
			if (getShowsStatement != null) {
				getShowsStatement.close();
			}
		}
		
		return lhm;
	}
	
	/*ujat kell irni: calc frist step*/
	@Override
	public int getAllUserNumber() throws SQLException{
		PreparedStatement getShowsStatement = null;
		ResultSet showResults = null;
		
		int count = 0;
		
		try {
			
			String queryStr = "SELECT COUNT(*) FROM TEST_USER";
			getShowsStatement = conn.prepareStatement(queryStr);
			showResults = getShowsStatement.executeQuery();
			
			
			
			while (showResults.next()) {
				count = showResults.getInt("COUNT(*)");
			}
			
		} finally {
			if (showResults != null)  {
				showResults.close();
			}
			if (getShowsStatement != null) {
				getShowsStatement.close();
			}
		}
		
		return count;
	}
	

	
	/*EZ MIT TUD ?*/
	@Override
	public HashMap<Integer,Integer> getEventsCount() throws SQLException{
		
		HashMap<Integer,Integer> hm = new HashMap<Integer,Integer>();		
		
		PreparedStatement getShowsStatement = null;
		ResultSet showResults = null;
		
		PreparedStatement getShowsStatement2 = null;
		ResultSet showResults2 = null;

		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		int day =Integer.parseInt(dateFormat.format(System.currentTimeMillis()));
		int day7 =Integer.parseInt(dateFormat.format(System.currentTimeMillis()+7*24*60*60*1000));
		
		try {
			String queryStr = "SELECT EVENTID FROM TEST_LOG WHERE (SELECT EVENT_ID FROM TEST_SHOW WHERE TEST_SHOW.day >= ? AND TEST_SHOW.day<= ? AND TEST_LOG.EVENTID=EVENT_ID LIMIT 1)";
			getShowsStatement = conn.prepareStatement(queryStr);
			getShowsStatement.setInt(1, day);
			getShowsStatement.setInt(2, day7);
			showResults = getShowsStatement.executeQuery();
			

			while (showResults.next()) {
				Integer eventid = showResults.getInt("EVENTID");
				
				if(hm.get(eventid) != null){
					Integer eventidCount = hm.get(eventid);
					eventidCount=eventidCount+1;
					hm.remove(eventid);
					hm.put(eventid,eventidCount);
					System.out.println("put "+eventid+" "+eventidCount);
				}else{
					hm.put(eventid, 1);
				}
				
			}
				
		} finally {
			if (showResults != null)  {
				showResults.close();
			}
			
			if (getShowsStatement != null) {
				getShowsStatement.close();
			}
			
		
		}
		
		
		try{
			String queryStr2 = "SELECT ID FROM TEST_EVENT WHERE (SELECT EVENT_ID FROM TEST_SHOW WHERE TEST_SHOW.day >= ? AND TEST_SHOW.day<= ? AND TEST_EVENT.ID=EVENT_ID LIMIT 1)";
			getShowsStatement2 = conn.prepareStatement(queryStr2);
			getShowsStatement2.setInt(1, day);
			getShowsStatement2.setInt(2, day7);
			showResults2 = getShowsStatement2.executeQuery();
			
			while (showResults2.next()) {
				
				Integer eventid = showResults2.getInt("ID");
				System.out.println("eid"+eventid);
				if(hm.get(eventid)==null){
					hm.put(eventid, 1);
				}
			}
		}finally{
			
			if (showResults2 != null)  {
				showResults2.close();
			}
			
			if (getShowsStatement2 != null) {
				getShowsStatement2.close();
			}
		}
		
		
		return hm;
	}

	/*szulinap; at kell irni; kell ajanlas;*/
	@Override
	public Integer getUserBirthDate(Long facebookId) throws SQLException{
		PreparedStatement getBirthDateStatement = null;
		ResultSet bdateResults = null;
		
		Integer kor = 0;
		
		try {
			
			String queryStr = "SELECT BIRTHDATE FROM TEST_USER WHERE FACEBOOKID = ?";
			getBirthDateStatement = conn.prepareStatement(queryStr);
			getBirthDateStatement.setLong(1, facebookId);
			bdateResults = getBirthDateStatement.executeQuery();

			Date today = new Date();
			
			while (bdateResults.next()) {
				Date bdate = bdateResults.getDate("BIRTHDATE");
				try{
					System.out.println(today.getTime()+" "+bdate.getTime());
					kor = (int)((today.getTime()/1000-bdate.getTime()/1000)/60/60/24/365);
				}catch(NullPointerException n){
					
				}
			
			}			
			
			
		} finally {
			if (bdateResults != null)  {
				bdateResults.close();
			}
			if (getBirthDateStatement != null) {
				getBirthDateStatement.close();
			}
		}
		if(kor<0)kor=0;
		return kor;	
	}
	
	/*fb userid lekerdezes; at  kell irni; sec stepben hasznaljuk*/
	public HashMap<Integer, Long> getAllFaceAndUserId() throws SQLException{
		
		HashMap<Integer,Long> lhm = new HashMap<Integer,Long>();	
		PreparedStatement getShowsStatement = null;
		ResultSet showResults = null;
		
		try {
			
			String queryStr = "SELECT ID,FACEBOOKID FROM TEST_USER WHERE 1";
			getShowsStatement = conn.prepareStatement(queryStr);
			showResults = getShowsStatement.executeQuery();
			
			while (showResults.next()) {
				Integer userid = showResults.getInt("ID");
				Long facebookid = showResults.getLong("FACEBOOKID");
				lhm.put(userid,facebookid);
			}			
		} finally {
			if (showResults != null)  {
				showResults.close();
			}
			if (getShowsStatement != null) {
				getShowsStatement.close();
			}
		}		
		return lhm;		
	}
	
	
	
	
	
	@Override	
	public String getEvents(long from, long to, String lang) throws SQLException {
		return GET_EVENTS_FROM_FILE ? getEventsFromFile(from, to) : getEventsFromDbWithShowJson(from, to, lang); 
	}
	
	protected String getEventsFromDb(long from, long to, String lang) throws SQLException {
		String ret = null;
		PreparedStatement statement = null;
		ResultSet eventResults = null;
		
		HashMap<Integer, Integer> hm = getEventsCount();
		//LinkedHashMap<Integer, Integer> lhm = Utils.sortByComparator(hm, true);
		for (Entry<Integer, Integer> entry : hm.entrySet()) {
	        	Integer eventid = entry.getKey();
	        	Integer count = entry.getValue();
	        	
	        	System.out.println("adshgfljadshfjkl" + "eventid: "+eventid+" count: "+count);

	      
		}
		
		
		LinkedHashMap<Integer, Integer> lhm = (LinkedHashMap<Integer, Integer>) Utils.sortByValues(hm);
		LinkedHashMap<Integer, Integer> lhm_top = new LinkedHashMap<Integer, Integer>();
		
		int i = 0;
		for (Entry<Integer, Integer> entry : lhm.entrySet()) {
	        if (i < 120){
	        	Integer eventid = entry.getKey();
	        	Integer count = entry.getValue();
	        	System.out.println("eventid: "+eventid+" count: "+count);
	        	lhm_top.put(eventid, count);
	        	i++;
	        }
		}

		String langVal = lang == null ? "HU" : lang;
		try{
			
			String queryEventInTime =
					/*
					"SELECT event.id, event.name, event.description, event.thumbnail, event.image, event.discriminator, event.many_shows, " +
							"days.day as the_day, show_.id, show_.go, show_.end, " +
							"show_.* " +
					"FROM TEST_EVENT as event, TEST_DAYS as days, TEST_SHOW as show_ " + 
					"WHERE days.day >= ? AND days.day <= ? " +
					  "AND event.id = days.event_id " +  
					  "AND show_.day = days.day " +
					  "AND ((event.many_shows = '1' and show_.is_stub = '1') " +
					  	"or " +
					  	"(event.many_shows = '0' and event.id = show_.event_id)) " +
					"ORDER BY event.name, days.day ";*/
					"SELECT EVENT.id, EVENT.name, EVENT.description, EVENT.thumbnail, EVENT.image, EVENT.discriminator, EVENT.many_shows, EVENT.video, days.day AS the_day, show_.* " + 
					"FROM TEST_EVENT AS EVENT, TEST_DAYS AS days, TEST_SHOW AS show_ " +  
					"WHERE days.day >= ? AND days.day <= ? AND EVENT.id = days.event_id AND EVENT.LANG_CODE = ? " + 
						"AND ((EVENT.`MANY_SHOWS` = 0 AND EVENT.id = show_.event_id AND show_.day = days.day) " +
						"OR (EVENT.many_shows = '1' AND show_.is_stub = '1')) AND (ISOK=1 OR ISOK=2) " + 
					"ORDER BY EVENT.name, days.day";
			
			int dayFrom = getDay(from);
			int dayTo = getDay(to);
			long fromDayStart = getDaysStart(from);
			System.out.println(String.format("\"%s\"", queryEventInTime));
			System.out.println(String.format("day from: %d, day to: %d, from: %d", dayFrom, dayTo, fromDayStart));
			statement = conn.prepareStatement(queryEventInTime);
			statement.setInt(1, dayFrom);
			statement.setInt(2, dayTo);
			statement.setString(3, langVal);
//			statement.setLong(3, fromDayStart);
			eventResults = statement.executeQuery();
			if(langVal.equals("EN")){
				ret = getEventsResponse(eventResults);
			}else{
				ret = getTop120EventsResponse(eventResults,lhm_top);
			}
			
			
		} finally {
			
			if (eventResults != null) {
				eventResults.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return ret;
	}
	
	protected String getEventsFromDbWithShowJson(long from, long to, String lang) throws SQLException {
		String ret = null;
		PreparedStatement statement = null;
		ResultSet eventResults = null;

		String langVal = lang == null ? "HU" : lang;
		try{
			
			String queryEventInTime =
					"SELECT EVENT.id, EVENT.name, EVENT.description, EVENT.thumbnail, EVENT.image, EVENT.discriminator, EVENT.many_shows, EVENT.video, days.day AS the_day, show_.json, EVENT.ISOK " +
					"FROM TEST_EVENT AS EVENT, TEST_DAYS AS days, TEST_SHOW_JSON AS show_ " +
					"WHERE days.day >= ? AND days.day <= ? AND EVENT.LANG_CODE = ? AND EVENT.id = days.event_id " + 
						"AND (" +
							"(EVENT.many_shows = '1' AND show_.is_stub = '1') " + 
							"OR " +
							"(EVENT.many_shows = '0' AND EVENT.id = show_.eventid)" +
						") " +
							"AND (ISOK=1 OR ISOK=2) " +
					"ORDER BY EVENT.ISOK DESC " +
					"LIMIT 1000";
			
			int dayFrom = getDay(from);
			int dayTo = getDay(to);
			long fromDayStart = getDaysStart(from);
			System.out.println(String.format("\"%s\"", queryEventInTime));
			System.out.println(String.format("day from: %d, day to: %d, from: %d", dayFrom, dayTo, fromDayStart));
			statement = conn.prepareStatement(queryEventInTime);
			statement.setInt(1, dayFrom);
			statement.setInt(2, dayTo);
			statement.setString(3, langVal);
//			statement.setLong(3, fromDayStart);
			eventResults = statement.executeQuery();
			ret = getEventsResponseWithShowJson(eventResults);
			
			
		} finally {
			
			if (eventResults != null) {
				eventResults.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return ret;
	}
	
	
	
	protected String getEventsFromFile(long from, long to) {
		InputStream is = null;
		BufferedReader reader = null;
		String ret = "";
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream("files/get_all_events_response");
			reader = new BufferedReader(new InputStreamReader(is));
			String currentLine = null;
			StringBuilder responseBuilder = new StringBuilder();
			while ((currentLine = reader.readLine()) != null) {
				responseBuilder.append(currentLine);
			}
			ret = responseBuilder.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}
	
	@Override
	public String getRecommendedEvents(long fbuserid, long from, long to, String lang) throws SQLException {
		//return getRecommendedEventsFromDb(fbuserid, from, to, lang);
		return getRecommendedEventsWithShowJsonFromDb(fbuserid,from,to,lang);
	}
	
	protected String getRecommendedEventsFromDb(long fbuserid, long from, long to, String lang) throws SQLException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
		Calendar cal = Calendar.getInstance();
		
		String ret = null;
		PreparedStatement statement = null;
		ResultSet eventResults = null;
		String langVal = lang == null ? "HU" : lang;
		try{
			
			String queryEventInTime =
					
//					SELECT recp.ID, EVENT.id, EVENT.name, EVENT.description, EVENT.thumbnail, EVENT.image, EVENT.discriminator, EVENT.many_shows, EVENT.video, days.day AS the_day, show_.id, show_.go, show_.end, show_.* 
//					FROM TEST_EVENT AS EVENT, TEST_DAYS AS days, TEST_SHOW AS show_, TEST_RECP AS recp 
//					WHERE days.day >= 20131009 AND days.day <= 20131016 AND recp.ID >= 258000 AND recp.ID<259000 AND EVENT.LANG_CODE = 'HU' AND EVENT.id = recp.eventid AND EVENT.id = days.event_id AND ((EVENT.many_shows = '1' AND show_.is_stub = '1') OR (EVENT.many_shows = '0' AND EVENT.id = show_.event_id AND show_.day = days.day)) 
//					ORDER BY recp.ID DESC 
//					LIMIT 120
					
					"SELECT recp.ID AS rank, EVENT.id, EVENT.name, EVENT.description, EVENT.thumbnail, EVENT.image, EVENT.discriminator, EVENT.many_shows, EVENT.video, " + 
							"days.day AS the_day, show_.id, show_.go, show_.end, show_.* " +
					"FROM TEST_EVENT AS EVENT, TEST_DAYS AS days, TEST_SHOW AS show_, TEST_RECP AS recp " +
					"WHERE days.day >= ? AND days.day <= ? AND recp.ID >= ? AND recp.ID<? AND EVENT.LANG_CODE = ? AND EVENT.id = recp.eventid AND EVENT.id = days.event_id " + 
						"AND (" +
							"(EVENT.many_shows = '1' AND show_.is_stub = '1') " + 
							"OR " +
							"(EVENT.many_shows = '0' AND EVENT.id = show_.event_id AND show_.day = days.day)" +
						") " +
							"AND (ISOK=1 OR ISOK=2) " +
					"ORDER BY recp.ID ASC " +	
					//"ORDER BY rec.rank, days.day";
					"LIMIT 1000";

			int dayFrom = getDay(from);
			int dayTo = getDay(to);
			long fromDayStart = getDaysStart(from);
			int userid = getUserIDforFbUserID(fbuserid);
			//System.out.println(String.format("\"%s\"", queryEventInTime));
			//System.out.println(String.format("day from: %d, day to: %d, from: %d, language: %s", dayFrom, dayTo, fromDayStart, lang));
			
			cal = Calendar.getInstance();
			System.out.println("prepareWillCall "+dateFormat.format(cal.getTime()));
			
			statement = conn.prepareStatement(queryEventInTime);
			statement.setInt(1, dayFrom);
			statement.setInt(2, dayTo);
			statement.setInt(3, userid*1000);
			statement.setInt(4, (userid+1)*1000);
			statement.setString(5, langVal);
			
			System.out.println(statement.toString());
			
			cal = Calendar.getInstance();
			System.out.println("executeWillCall "+dateFormat.format(cal.getTime()));
			
			eventResults = statement.executeQuery();
			

			
			
			cal = Calendar.getInstance();
			System.out.println("getEventResponseWillCal "+dateFormat.format(cal.getTime()));
			
			ret = getEventsResponse(eventResults);
			
			cal = Calendar.getInstance();
			System.out.println("responseDone "+dateFormat.format(cal.getTime()));
			
			uploadLog(fbuserid, 0, Calendar.getInstance().getTimeInMillis(), 4);
			
			
		} finally {
			
			if (eventResults != null) {
				eventResults.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return ret;
	}

	
	
	protected String getRecommendedEventsWithShowJsonFromDb(long fbuserid, long from, long to, String lang) throws SQLException {
		String ret = null;
		PreparedStatement statement = null;
		ResultSet eventResults = null;
		String langVal = lang == null ? "HU" : lang;
		try{
			String queryEventInTime =
					"SELECT recp.ID AS rank, EVENT.id, EVENT.name, EVENT.description, EVENT.thumbnail, EVENT.image, EVENT.discriminator, EVENT.many_shows, EVENT.video, days.day AS the_day, show_.json, EVENT.ISOK " +
					"FROM TEST_EVENT AS EVENT, TEST_DAYS AS days, TEST_SHOW_JSON AS show_, TEST_RECP AS recp " +
					"WHERE days.day >= ? AND days.day <= ? AND recp.ID >= ? AND recp.ID<? AND EVENT.LANG_CODE = ? AND EVENT.id = recp.eventid AND EVENT.id = days.event_id " + 
						"AND (" +
							"(EVENT.many_shows = '1' AND show_.is_stub = '1') " + 
							"OR " +
							"(EVENT.many_shows = '0' AND EVENT.id = show_.eventid)" +
						") " +
							"AND (ISOK=1 OR ISOK=2) " +
					"ORDER BY EVENT.ISOK DESC, recp.ID ASC " +	
					"LIMIT 1000";

			int dayFrom = getDay(from);
			int dayTo = getDay(to);
			long fromDayStart = getDaysStart(from);
			int userid = getUserIDforFbUserID(fbuserid);

			statement = conn.prepareStatement(queryEventInTime);
			statement.setInt(1, dayFrom);
			statement.setInt(2, dayTo);
			statement.setInt(3, userid*1000);
			statement.setInt(4, (userid+1)*1000);
			statement.setString(5, langVal);
			
			System.out.println(statement.toString());

			eventResults = statement.executeQuery();
			ret = getEventsResponseWithShowJson(eventResults);
			uploadLog(fbuserid, 0, Calendar.getInstance().getTimeInMillis(), 4);
		} finally {
			if (eventResults != null) {
				eventResults.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return ret;
	}
	
	protected String getEventsResponseWithShowJson(ResultSet eventResults) throws SQLException {
		
		Map<Integer, DaysEvents> dayEventMap = new LinkedHashMap<Integer, CloudDbManager.DaysEvents>();
		Map<Integer, EventsShows> eventShowMap = new LinkedHashMap<Integer, CloudDbManager.EventsShows>();
		Map<Integer, Event> eventIdEventMap = new LinkedHashMap<Integer, Event>();
		List<String> showArray = new ArrayList<String>();
		
		HashMap<String, Integer> categoryDateCounter = new HashMap <String, Integer>();
		//check category-date event count
		categoryDateCounter.put("Cinema", 0);
		categoryDateCounter.put("Cinema"+"today", 0);
		categoryDateCounter.put("Cinema"+"tomorrow", 0);
		categoryDateCounter.put("Cinema"+"week", 0);
		
		categoryDateCounter.put("Culture"+"today", 0);
		categoryDateCounter.put("Culture"+"tomorrow", 0);
		categoryDateCounter.put("Culture"+"week", 0);
		
		categoryDateCounter.put("Entertainment"+"today", 0);
		categoryDateCounter.put("Entertainment"+"tomorrow", 0);
		categoryDateCounter.put("Entertainment"+"week", 0);
		
		categoryDateCounter.put("Freetime"+"today", 0);
		categoryDateCounter.put("Freetime"+"tomorrow", 0);
		categoryDateCounter.put("Freetime"+"week", 0);
		
		categoryDateCounter.put("Gastro"+"today", 0);
		categoryDateCounter.put("Gastro"+"tomorrow", 0);
		categoryDateCounter.put("Gastro"+"week", 0);
		
		final Gson gson = new Gson();
		int rowCounter = 0;
		int showCounter = 0;
		boolean first = true;
		FilmEvent filmEvent = new FilmEvent();
		PtEvent ptEvent = new PtEvent();
		
		while (eventResults.next()) {
			Event currentEvent = null;
			rowCounter++;
			if (rowCounter > FIRST_N_ROW) {
				break;
			}
			if (rowCounter % 50 == 0) {
				System.out.println(String.format("%d. row", rowCounter));
			}//eddig volt
			

			
			String discriminatorKey = eventResults.getString(EventColumn.DISCRIMINATOR); 
			int eventDay = eventResults.getInt(DaysColumn.ALIAS_DAY);
			
			DateFormat dateFormat2 = new SimpleDateFormat("yyyyMMdd");
			Calendar cal2 = Calendar.getInstance();
			int todayInt = Integer.parseInt(dateFormat2.format(cal2.getTime()));
			
			String dayStr = null;
			
			if(eventDay==todayInt){
				dayStr = "today";
			}else if(eventDay==todayInt+1){
				dayStr = "tomorrow";
			}else {
				dayStr = "week";
			}
			int threshold  = 20;
			
			if(discriminatorKey==null){
				continue;
			}
			
//			ittt['Cinema'] = ['Cinema'];			
			if(discriminatorKey.equals("Cinema")){
				int counter = categoryDateCounter.get("Cinema"+dayStr);
				counter++;
				categoryDateCounter.put("Cinema"+dayStr, counter);
				if(counter>threshold){
					continue;
				}
			}
//			ittt['Culture'] = ['Theater', 'Exhibition', 'Music', 'Gastro'];			
			if(discriminatorKey.equals("Theater") || discriminatorKey.equals("Exhibition") || discriminatorKey.equals("Music")|| discriminatorKey.equals("Gastro")){
				int counter = categoryDateCounter.get("Culture"+dayStr);
				counter++;
				categoryDateCounter.put("Culture"+dayStr, counter);
				if(counter>threshold){
					continue;
				}
			}
//			ittt['Entertainment'] = ['Cinema', 'Music', 'Festival', 'Party'];
			if(discriminatorKey.equals("Music") ||discriminatorKey.equals("Festival") ||discriminatorKey.equals("Party") ){
				int counter = categoryDateCounter.get("Entertainment"+dayStr);
				counter++;
				categoryDateCounter.put("Entertainment"+dayStr, counter);
				if(counter>threshold){
					continue;
				}
			}
//			ittt['Freetime'] = ['Travel', 'Sport', 'Kid', 'Festival', 'Other', 'Party'];			
			if(discriminatorKey.equals("Travel") ||discriminatorKey.equals("Sport") ||discriminatorKey.equals("Kid") ||discriminatorKey.equals("Festival") ||discriminatorKey.equals("Other") ||discriminatorKey.equals("Party")){
				int counter = categoryDateCounter.get("Freetime"+dayStr);
				counter++;
				categoryDateCounter.put("Freetime"+dayStr, counter);
				if(counter>threshold){
					continue;
				}
			}
//			ittt['Gastro'] = ['Gastro'];			
			if(discriminatorKey.equals("Gastro")){
				int counter = categoryDateCounter.get("Gastro"+dayStr);
				counter++;
				categoryDateCounter.put("Gastro"+dayStr, counter);
				if(counter>threshold){
					continue;
				}
			}
			
			
			//innen kezdodik
			int currentEventId = eventResults.getInt(EventColumn.ID);
			
			if (!eventIdEventMap.containsKey(Integer.valueOf(currentEventId))) { // we reached a new event
				
				String discriminator = eventResults.getString(EventColumn.DISCRIMINATOR); 

				currentEvent = new SimpleEvent();
				((SimpleEvent) currentEvent).setDiscriminator(discriminator);
				
				String name = eventResults.getString(EventColumn.NAME);
				String description = eventResults.getString(EventColumn.DESCRIPTION);
				
				currentEvent.setId(eventResults.getInt(EventColumn.ID));
				currentEvent.setName(USE_BASE64 ? toBase64(name) : name);
				currentEvent.setDescription(USE_BASE64 ? toBase64(description) : description);
				currentEvent.setThumbnail(eventResults.getString(EventColumn.THUMBNAIL));
				currentEvent.setImage(eventResults.getString(EventColumn.IMAGE));
				String manyShowsStr = eventResults.getString(EventColumn.MANY_SHOWS);
				currentEvent.setManyShows(manyShowsStr.equals("1"));
				currentEvent.getShowList().clear();
				currentEvent.setVideo(eventResults.getString(EventColumn.VIDEO));
				try{
					currentEvent.setRank(eventResults.getInt("rank"));
				}catch(SQLException e){
					e.printStackTrace();
				}
				
				
				eventIdEventMap.put(Integer.valueOf(currentEventId), currentEvent);
			} else {
				currentEvent = eventIdEventMap.get(Integer.valueOf(currentEventId));
			}
			
			int day = eventResults.getInt(DaysColumn.ALIAS_DAY);
			DaysEvents daysEvents = dayEventMap.get(Integer.valueOf(day));
			if (daysEvents == null) {
				daysEvents = new DaysEvents(day);
				dayEventMap.put(Integer.valueOf(day), daysEvents);
			}
			daysEvents.events.add(currentEvent);
			
			if (showCounter < SHOW_LIMIT && !currentEvent.hasManyShows()) {

//				// add show to the current event
//				ToJsonShow show = new ToJsonShow();
//				show.setEventId(currentEvent.getId());
//				String location = eventResults.getString(ShowColumn.LOCATION);
//				String category = eventResults.getString(ShowColumn.CATEGORY);
//				long go = eventResults.getLong(ShowColumn.GO);
//				long end = eventResults.getLong(ShowColumn.END);
//				show.setLocation(USE_BASE64 ? toBase64(location) : location);
//				show.setGo(go / 1000L);
//				show.setEnd(end == 0 ? null : new Date(end));
//				show.setCategory(USE_BASE64 ? toBase64(category) : category);
//				show.setX(eventResults.getFloat(ShowColumn.X));
//				show.setY(eventResults.getFloat(ShowColumn.Y));
//				show.setDay(eventResults.getString(ShowColumn.DAY));
//				//innen masoltam ki fentrol
//				EventsShows eventsShows = eventShowMap.get(show.getEventId());
//				if (eventsShows == null) {
//					eventsShows = new EventsShows(show.getEventId());
//					eventShowMap.put(show.getEventId(), eventsShows);
//				}
//				eventsShows.shows.add(show);
//				showCounter++;

				//showArray.add(eventResults.getString("JSON"));
				
				EventsShows eventsShows = (EventsShows)new Gson().fromJson(eventResults.getString("JSON"), EventsShows.class);
				
				Set<ToJsonShow> js = eventsShows.shows;
				Set<ToJsonShow> newJs = new LinkedHashSet<ToJsonShow>();
				
				DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
				Calendar cal = Calendar.getInstance();
				int today = Integer.parseInt(dateFormat.format(cal.getTime()));
				
				
				//get the Iterator
			    Iterator<ToJsonShow> itr = js.iterator();
			   
			    while(itr.hasNext()){
			    	ToJsonShow tjs = itr.next();
					int currentJsonShow = Integer.parseInt(tjs.getDay());
					if(currentJsonShow>=today){
						//eventsShows.shows.remove(tjs);
						newJs.add(tjs);
					}
				}
				
			    eventsShows.shows=newJs;
				
//				for(ToJsonShow tjs : js){
//					int currentJsonShow = Integer.parseInt(tjs.getDay());
//					if(currentJsonShow<today){
//						eventsShows.shows.remove(tjs);
//					}
//				}
				
				
				eventShowMap.put(eventsShows.eventId, eventsShows);
		
				
			}
		}
		
		System.out.println(String.format("summary: %d rows read", rowCounter));

		Collection <DaysEvents> unSorted = dayEventMap.values();

		return gson.toJson(new Response(unSorted, eventShowMap.values()));//here was sorted but we dont need to sort anymore
		//return gson.toJson(new Response(unSorted, showArray));//here was sorted but we dont need to sort anymore

	}
	
	protected Integer getCurrentDate(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		Integer date = year * 10000 + month * 100 + day;
		return date;
	}
	

	
	protected String getRecommendedEventsFromFile(long fbuserid, long from, long to) {
		return null;
	}
	
	@Override
	public String getSingleEventWithShows(int eventId, long from, long to) throws SQLException {

		String ret = null;
		PreparedStatement statement = null;
		ResultSet results = null;
		final String query = 
				"SELECT * FROM TEST_EVENT E, TEST_SHOW S WHERE E.ID = ? AND E.ID = S.EVENT_ID AND S.GO > ? AND S.GO < ?";
		try {
			statement = conn.prepareStatement(query);
			statement.setInt(1, eventId);
			statement.setLong(2, from);
			statement.setLong(3, to);
			results = statement.executeQuery();
			
			// process the results
			
			SimpleEvent event = null;
			while (results.next()) {
				if (event == null) {
					event = new SimpleEvent();
					event.setId(eventId);
					event.setName(results.getString(EventColumn.NAME));
					event.setDescription(results.getString(EventColumn.DESCRIPTION));
					event.setImage(results.getString(EventColumn.IMAGE));
					event.setThumbnail(results.getString(EventColumn.THUMBNAIL));
					event.setManyShows(results.getString(EventColumn.MANY_SHOWS).equals("1"));
					event.setVideo(results.getString(EventColumn.VIDEO));
				}
				ToJsonShow currentShow = new ToJsonShow();
				currentShow.setEventId(eventId);
				String location = results.getString(ShowColumn.LOCATION);
				String category = results.getString(ShowColumn.CATEGORY);
				long go = results.getLong(ShowColumn.GO);
				long end = results.getLong(ShowColumn.END);
				currentShow.setLocation(USE_BASE64 ? toBase64(location) : location);
				currentShow.setGo(go / 1000L);
				currentShow.setEnd(end == 0 ? null : new Date(end));
				currentShow.setCategory(USE_BASE64 ? toBase64(category) : category);
				currentShow.setX(results.getFloat(ShowColumn.X));
				currentShow.setY(results.getFloat(ShowColumn.Y));
				currentShow.setDay(results.getString(ShowColumn.DAY));
				event.getShowList().add(currentShow);
			}
			Gson gson = new Gson();
			ret = gson.toJson(event);
			
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
	
	/**
	 * Get the JSON response from a {@link ResultSet} instance. It does not
	 * close the resultSet! It assumes, that each row has the following columns:
	 * <ul>
	 * <li>rec.rank</li>
	 * <li>days.day</li>
	 * <li>event.id</li>
	 * <li>event.name</li>
	 * <li>event.description</li>
	 * <li>event.discriminator</li>
	 * <li>event.thumbnail</li>
	 * <li>event.image</li>
	 * <li>event.many_shows</li>
	 * <li>show.*</li>
	 * </ul>
	 * 
	 * @param eventResults
	 *            a {@link ResultSet} instance that contains the rows to be used
	 *            to generate the response
	 * 
	 * @return A string that contains the JSON response
	 * 
	 * @throws SQLException
	 *             If something goes wrong when reading the resultSet
	 */
	protected String getEventsResponse(ResultSet eventResults) throws SQLException {
			
			Map<Integer, DaysEvents> dayEventMap = new LinkedHashMap<Integer, CloudDbManager.DaysEvents>();
			Map<Integer, EventsShows> eventShowMap = new LinkedHashMap<Integer, CloudDbManager.EventsShows>();
			Map<Integer, Event> eventIdEventMap = new LinkedHashMap<Integer, Event>();
			
			final Gson gson = new Gson();
			int rowCounter = 0;
			int showCounter = 0;
			boolean first = true;
			FilmEvent filmEvent = new FilmEvent();
			PtEvent ptEvent = new PtEvent();
			
			while (eventResults.next()) {
				
				//System.out.println(eventResults.getString("name"));
			
				Event currentEvent = null;
				rowCounter++;
				if (rowCounter > FIRST_N_ROW) {
					break;
				}
				if (rowCounter % 50 == 0) {
					System.out.println(String.format("%d. row", rowCounter));
				}
				int currentEventId = eventResults.getInt(EventColumn.ID);
				
				if (!eventIdEventMap.containsKey(Integer.valueOf(currentEventId))) { // we reached a new event
					
					String discriminator = eventResults.getString(EventColumn.DISCRIMINATOR); 
	
					currentEvent = new SimpleEvent();
					((SimpleEvent) currentEvent).setDiscriminator(discriminator);
					
					String name = eventResults.getString(EventColumn.NAME);
					String description = eventResults.getString(EventColumn.DESCRIPTION);
					
					currentEvent.setId(eventResults.getInt(EventColumn.ID));
					currentEvent.setName(USE_BASE64 ? toBase64(name) : name);
					currentEvent.setDescription(USE_BASE64 ? toBase64(description) : description);
					currentEvent.setThumbnail(eventResults.getString(EventColumn.THUMBNAIL));
					currentEvent.setImage(eventResults.getString(EventColumn.IMAGE));
					String manyShowsStr = eventResults.getString(EventColumn.MANY_SHOWS);
					currentEvent.setManyShows(manyShowsStr.equals("1"));
					currentEvent.getShowList().clear();
					currentEvent.setVideo(eventResults.getString(EventColumn.VIDEO));
					try{
						currentEvent.setRank(eventResults.getInt("rank"));
					}catch(SQLException e){
						e.printStackTrace();
					}
					
					
					eventIdEventMap.put(Integer.valueOf(currentEventId), currentEvent);
				} else {
					currentEvent = eventIdEventMap.get(Integer.valueOf(currentEventId));
				}
				
				int day = eventResults.getInt(DaysColumn.ALIAS_DAY);
				DaysEvents daysEvents = dayEventMap.get(Integer.valueOf(day));
				if (daysEvents == null) {
					daysEvents = new DaysEvents(day);
					dayEventMap.put(Integer.valueOf(day), daysEvents);
				}
				daysEvents.events.add(currentEvent);
				
				if (showCounter < SHOW_LIMIT && !currentEvent.hasManyShows()) {
	
					// add show to the current event
					ToJsonShow show = new ToJsonShow();
					show.setEventId(currentEvent.getId());
					String location = eventResults.getString(ShowColumn.LOCATION);
					String category = eventResults.getString(ShowColumn.CATEGORY);
					long go = eventResults.getLong(ShowColumn.GO);
					long end = eventResults.getLong(ShowColumn.END);
					show.setLocation(USE_BASE64 ? toBase64(location) : location);
					show.setGo(go / 1000L);
					show.setEnd(end == 0 ? null : new Date(end));
					show.setCategory(USE_BASE64 ? toBase64(category) : category);
					show.setX(eventResults.getFloat(ShowColumn.X));
					show.setY(eventResults.getFloat(ShowColumn.Y));
					show.setDay(eventResults.getString(ShowColumn.DAY));
					EventsShows eventsShows = eventShowMap.get(show.getEventId());
					if (eventsShows == null) {
						eventsShows = new EventsShows(show.getEventId());
						eventShowMap.put(show.getEventId(), eventsShows);
					}
					eventsShows.shows.add(show);
					showCounter++;
				}
			}
			
			System.out.println(String.format("summary: %d rows read", rowCounter));
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			System.out.println("sort elott "+dateFormat.format(cal.getTime()));
			
			
			Collection <DaysEvents> unSorted = dayEventMap.values();
//			List<DaysEvents> sorted = new ArrayList<DaysEvents>();
//			for(DaysEvents d : unSorted){
//				List<Event> eventList = new ArrayList<Event>();
//				eventList.addAll(d.events);
//				Collections.sort(eventList, new Comparator<Event>() {
//					@Override
//					public int compare(Event o1, Event o2) {
//						Float a = o2.getRank();
//						Float b = o1.getRank();
//						return a.compareTo(b);
//					}
//			    });
//				Set<Event> sortedEventsSet = new LinkedHashSet<Event>();
//				for(Event e : eventList){
//					sortedEventsSet.add(e);
//				}
//				DaysEvents curr = new DaysEvents(d.day,sortedEventsSet);
//				sorted.add(curr);
//			}
			
			cal = Calendar.getInstance();
			System.out.println("sort utan "+dateFormat.format(cal.getTime()));
			
			return gson.toJson(new Response(unSorted, eventShowMap.values()));//here was sorted but we dont need to sort anymore
		}
	
	
	protected String getTop120EventsResponse(ResultSet eventResults, LinkedHashMap<Integer, Integer> lhmTop) throws SQLException {
		
		Map<Integer, DaysEvents> dayEventMap = new LinkedHashMap<Integer, CloudDbManager.DaysEvents>();
		Map<Integer, EventsShows> eventShowMap = new LinkedHashMap<Integer, CloudDbManager.EventsShows>();
		Map<Integer, Event> eventIdEventMap = new LinkedHashMap<Integer, Event>();
		
		final Gson gson = new Gson();
		int rowCounter = 0;
		int showCounter = 0;
		boolean first = true;
		FilmEvent filmEvent = new FilmEvent();
		PtEvent ptEvent = new PtEvent();
		
		
		while (eventResults.next()) {
			
			//System.out.println("eventresult next");
			
		  if(lhmTop.containsKey(eventResults.getInt(EventColumn.ID))){
				
			System.out.println(eventResults.getString("name"));
		
			Event currentEvent = null;
			rowCounter++;
			if (rowCounter > FIRST_N_ROW) {
				break;
			}
			if (rowCounter % 50 == 0) {
				System.out.println(String.format("%d. row", rowCounter));
			}
			int currentEventId = eventResults.getInt(EventColumn.ID);
			
			if (!eventIdEventMap.containsKey(Integer.valueOf(currentEventId))) { // we reached a new event
				
				String discriminator = eventResults.getString(EventColumn.DISCRIMINATOR); 

				currentEvent = new SimpleEvent();
				((SimpleEvent) currentEvent).setDiscriminator(discriminator);
				
				String name = eventResults.getString(EventColumn.NAME);
				String description = eventResults.getString(EventColumn.DESCRIPTION);
				
				currentEvent.setId(eventResults.getInt(EventColumn.ID));
				currentEvent.setName(USE_BASE64 ? toBase64(name) : name);
				currentEvent.setDescription(USE_BASE64 ? toBase64(description) : description);
				currentEvent.setThumbnail(eventResults.getString(EventColumn.THUMBNAIL));
				currentEvent.setImage(eventResults.getString(EventColumn.IMAGE));
				String manyShowsStr = eventResults.getString(EventColumn.MANY_SHOWS);
				currentEvent.setManyShows(manyShowsStr.equals("1"));
				currentEvent.getShowList().clear();
				currentEvent.setVideo(eventResults.getString(EventColumn.VIDEO));
					
				eventIdEventMap.put(Integer.valueOf(currentEventId), currentEvent);
			} else {
				currentEvent = eventIdEventMap.get(Integer.valueOf(currentEventId));
			}
			
			int day = eventResults.getInt(DaysColumn.ALIAS_DAY);
			DaysEvents daysEvents = dayEventMap.get(Integer.valueOf(day));
			if (daysEvents == null) {
				daysEvents = new DaysEvents(day);
				dayEventMap.put(Integer.valueOf(day), daysEvents);
			}
			daysEvents.events.add(currentEvent);
			
			if (showCounter < SHOW_LIMIT && !currentEvent.hasManyShows()) {

				// add show to the current event
				ToJsonShow show = new ToJsonShow();
				show.setEventId(currentEvent.getId());
				String location = eventResults.getString(ShowColumn.LOCATION);
				String category = eventResults.getString(ShowColumn.CATEGORY);
				long go = eventResults.getLong(ShowColumn.GO);
				long end = eventResults.getLong(ShowColumn.END);
				show.setLocation(USE_BASE64 ? toBase64(location) : location);
				show.setGo(go / 1000L);
				show.setEnd(end == 0 ? null : new Date(end));
				show.setCategory(USE_BASE64 ? toBase64(category) : category);
				show.setX(eventResults.getFloat(ShowColumn.X));
				show.setY(eventResults.getFloat(ShowColumn.Y));
				show.setDay(eventResults.getString(ShowColumn.DAY));
				EventsShows eventsShows = eventShowMap.get(show.getEventId());
				if (eventsShows == null) {
					eventsShows = new EventsShows(show.getEventId());
					eventShowMap.put(show.getEventId(), eventsShows);
				}
				eventsShows.shows.add(show);
				showCounter++;
			}
		  }
		}
		
		System.out.println(String.format("summary: %d rows read", rowCounter));
		
		return gson.toJson(new Response(dayEventMap.values(), eventShowMap.values()));
	}
	
	
	@Override
	public String getShows(int eventId, long from, long to) throws SQLException, IOException {
		return GET_SHOWS_FROM_FILE ? getShowsFromFile(eventId, from, to) : getShowsFromDb(eventId, from, to);
	}
	
	protected String getShowsFromDb(int eventId, long from, long to) throws SQLException {
		String ret = null;
		PreparedStatement getShowsStatement = null;
		ResultSet showResults = null;
		
		try {
			
			String queryStr = "SELECT * FROM TEST_SHOW WHERE event_id = ? AND go >= ? AND go <= ?";
			getShowsStatement = conn.prepareStatement(queryStr);
			getShowsStatement.setInt(1, eventId);
			getShowsStatement.setLong(2, from);
			getShowsStatement.setLong(3, to);
			showResults = getShowsStatement.executeQuery();
			ret = getShowsResponse(showResults, eventId);
			
		} finally {
			if (showResults != null)  {
				showResults.close();
			}
			if (getShowsStatement != null) {
				getShowsStatement.close();
			}
		}
		return ret;
	}
	
	protected String getShowsResponse(ResultSet showResults, int eventId) throws SQLException {
		final Gson gson = new Gson(); 
		EventsShows eventsShows = new EventsShows(eventId);
		
		while (showResults.next()) {
			ToJsonShow show = new ToJsonShow();
			show.setEventId(eventId);
			String location = showResults.getString(ShowColumn.LOCATION);
			String category = showResults.getString(ShowColumn.CATEGORY);
			long go = showResults.getLong(ShowColumn.GO);
//			long end = showResults.getLong(ShowColumn.END);
			show.setLocation(USE_BASE64 ? toBase64(location) : location);
			show.setGo(go / 1000L);
//			show.setEnd(end == 0 ? null : new Date(end));
			show.setCategory(USE_BASE64 ? toBase64(category) : category);
			show.setX(showResults.getFloat(ShowColumn.X));
			show.setY(showResults.getFloat(ShowColumn.Y));
			show.setDay(showResults.getString(ShowColumn.DAY));
			eventsShows.shows.add(show);
		}
		
		return gson.toJson(eventsShows);
	}
	
	protected String getShowsFromFile(int eventId, long from, long to) {
		InputStream is = null;
		BufferedReader reader = null;
		String ret = "";
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream("files/get_shows_490");
			reader = new BufferedReader(new InputStreamReader(is));
			String currentLine = null;
			StringBuilder responseBuilder = new StringBuilder();
			while ((currentLine = reader.readLine()) != null) {
				responseBuilder.append(currentLine);
			}
			ret = responseBuilder.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}
	
	@Override
	public void uploadShowsToDays(int eventId){
		PreparedStatement getShowsStatement = null;
		ResultSet showResults = null;

		try {			
			String queryStr = "SELECT * FROM TEST_SHOW WHERE TEST_SHOW.EVENT_ID NOT IN (SELECT EVENT_ID FROM TEST_DAYS)";
			getShowsStatement = conn.prepareStatement(queryStr);
			showResults = getShowsStatement.executeQuery();
						
			PreparedStatement insertStatement = null;
			
			while(showResults.next()){
				
				int event_id = showResults.getInt(ShowColumn.EVENT_ID);
				String dayStr = showResults.getString(ShowColumn.DAY);
				
				String insertQuery = "INSERT INTO `TEST_DAYS`(`DAY`, `EVENT_ID`) VALUES (?, ?)";
				
				try {

					insertStatement = conn.prepareStatement(insertQuery);
					insertStatement.setInt(1, Integer.parseInt(dayStr));
					insertStatement.setInt(2, event_id);
					insertStatement.executeUpdate();
					
				} catch (SQLException e) {
					e.printStackTrace();
				} finally{
					if (insertStatement != null) {
						try {
							insertStatement.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}catch(SQLException e ){
			e.printStackTrace();
		}finally {
			if (showResults != null)  {
				try {
					showResults.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (getShowsStatement != null) {
				try {
					getShowsStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	
	@Override
	public void uploadShowsToShowJson(){
		PreparedStatement getShowsStatement = null;
		ResultSet showResults = null;
		Map<Integer, EventsShows> eventShowMap = new LinkedHashMap<Integer, CloudDbManager.EventsShows>();

		try {			
			String queryStr = "SELECT * FROM TEST_SHOW WHERE TEST_SHOW.EVENT_ID NOT IN (SELECT EVENTID FROM TEST_SHOW_JSON)";
			getShowsStatement = conn.prepareStatement(queryStr);
			showResults = getShowsStatement.executeQuery();
						
			while(showResults.next()){
				ToJsonShow show = new ToJsonShow();
				show.setEventId(showResults.getInt(ShowColumn.EVENT_ID));
				String location = showResults.getString(ShowColumn.LOCATION);
				String category = showResults.getString(ShowColumn.CATEGORY);
				long go = showResults.getLong(ShowColumn.GO);
				long end = showResults.getLong(ShowColumn.END);
				show.setLocation(USE_BASE64 ? toBase64(location) : location);
				show.setGo(go / 1000L);
				show.setEnd(end == 0 ? null : new Date(end));
				show.setCategory(USE_BASE64 ? toBase64(category) : category);
				show.setX(showResults.getFloat(ShowColumn.X));
				show.setY(showResults.getFloat(ShowColumn.Y));
				show.setDay(showResults.getString(ShowColumn.DAY));
				EventsShows eventsShows = eventShowMap.get(show.getEventId());
				if (eventsShows == null) {
					eventsShows = new EventsShows(show.getEventId());
					eventShowMap.put(show.getEventId(), eventsShows);
				}
				eventsShows.shows.add(show);
			}
		}catch(SQLException e ){
			e.printStackTrace();
		}finally {
			if (showResults != null)  {
				try {
					showResults.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (getShowsStatement != null) {
				try {
					getShowsStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		final Gson gson = new Gson();
		//System.out.println(gson.toJson(eventShowMap.values()));
		
		PreparedStatement insertStatement = null;
		
		for(Map.Entry<Integer, EventsShows> entry : eventShowMap.entrySet()){
			
			if(entry.getValue().shows.size()<300){
				System.out.println(entry.getKey());//+" "+gson.toJson(entry.getValue())
				String insertQuery = "INSERT INTO `TEST_SHOW_JSON`(`EVENTID`, `JSON`, `SHOULD_CHANGE`) VALUES (?, ?, 0)";
				
				try {
//					try {
//						System.out.println(entry.getKey());
//						System.out.println(String.format("%x", new BigInteger(1, gson.toJson(entry.getValue()).getBytes("UTF8"))));
//					} catch (UnsupportedEncodingException e) {
//						
//						e.printStackTrace();
//					}
					insertStatement = conn.prepareStatement(insertQuery);
					insertStatement.setInt(1, entry.getKey());
					insertStatement.setString(2, gson.toJson(entry.getValue()));
					//insertStatement.addBatch();
					insertStatement.executeUpdate();
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		if (insertStatement != null) {
			try {
				insertStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public List<Integer> getAllEventsID(){
		String query = "SELECT TEST_EVENT.ID FROM TEST_EVENT, TEST_DAYS WHERE TEST_DAYS.DAY>=20131217 AND TEST_DAYS.EVENT_ID = TEST_EVENT.ID GROUP BY TEST_EVENT.ID";
		PreparedStatement getEventsIDStatement = null;
		ResultSet results = null;
		List<Integer> ids = new ArrayList<Integer>();
		try {
			getEventsIDStatement = conn.prepareStatement(query);
			results = getEventsIDStatement.executeQuery();
			
			while(results.next()){
				ids.add(results.getInt("ID"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ids;
	}
	

	
	@Override
	public void updateShowToShowJson(long eventid){
		String insertQuery = "DELETE FROM `TEST_SHOW_JSON` WHERE EVENTID = ?";
		PreparedStatement insertStatement = null;
		
		try {
			insertStatement = conn.prepareStatement(insertQuery);
			insertStatement.setInt(1, safeLongToInt(eventid));			
			insertStatement.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		uploadShowsToShowJson();
	}
	
	@Override
	public void checkUpdateableShowsJson(){
		String queryStr = "SELECT EVENTID FROM TEST_SHOW_JSON WHERE SHOULD_CHANGE = 1";
		
		PreparedStatement getShowsStatement = null;
		ResultSet showResults = null;
		try {
			getShowsStatement = conn.prepareStatement(queryStr);
			showResults = getShowsStatement.executeQuery();
			
			while(showResults.next()){
				updateShowToShowJson(showResults.getInt("EVENTID"));
				//TODO eventid-vel meghívni test_show-test-days szinkronizálót
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public static int safeLongToInt(long l) {
	    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
	        throw new IllegalArgumentException
	            (l + " cannot be cast to int without changing its value.");
	    }
	    return (int) l;
	}
	
	@Override
	public List<Like> getAllLikeForFbUserId(long fbuserid) throws SQLException{
		List<Like> likes = new ArrayList<Like>();

		PreparedStatement selectStatement = null;
		ResultSet resultSet = null;
		int rowcount=0;
		String queryStr = null;
		
		try {
			
			queryStr = 
					"SELECT l.id,l.category,l.name " +
					"FROM TEST_LIKE AS l, TEST_USERLIKESWITCH AS ul " +
					"WHERE ul.fbuserid=? and l.id=ul.likeid";
			selectStatement = conn.prepareStatement(queryStr);
			selectStatement.setLong(1,fbuserid);
	
			resultSet = selectStatement.executeQuery();
			
			while(resultSet.next()){
				rowcount++;
				Like li = new Like();

				li.setId(resultSet.getLong("ID"));
				li.setCategory(resultSet.getString("CATEGORY"));
				li.setName(resultSet.getString("NAME"));
				likes.add(li);
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
	
	
	
	@Override
	public List<String> getAllMovieTitle() throws SQLException{
		PreparedStatement selectStatement = null;
		ResultSet resultSet = null;
		List<String> ret = null;
		
		try {
			
			final String queryStr = "SELECT ORIGINALTITLE FROM TEST_EVENT";
			selectStatement = conn.prepareStatement(queryStr);
			resultSet = selectStatement.executeQuery();
			ret = new ArrayList<String>();
			
			while(resultSet.next()){
				String ot = resultSet.getString("ORIGINALTITLE");
				if(ot!=null){
					ret.add(ot);
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
	
	
	@Override
	public void uploadMoviePoster(String originalTitle, String link, List<String> list) throws SQLException{
		
		String json = new Gson().toJson(list);
		
		PreparedStatement selectStatement = null;
		ResultSet resultSet = null;
		
		PreparedStatement updateStatement = null;
		try {
			
			final String queryStr = "SELECT THUMBNAIL FROM TEST_EVENT WHERE ORIGINALTITLE = ?";
			selectStatement = conn.prepareStatement(queryStr);
			selectStatement.setString(1,originalTitle);
			resultSet = selectStatement.executeQuery();
			
			while(resultSet.next()){
				String ot = resultSet.getString("THUMBNAIL");
				if(ot.contains("funspotter")||ot.length()<5||ot == null){
					try {
						
						final String updateStr = "UPDATE TEST_EVENT SET THUMBNAIL= ?, IMAGE = ? WHERE ORIGINALTITLE = ?";
						updateStatement = conn.prepareStatement(updateStr);
						
						updateStatement.setString(1,link);
						updateStatement.setString(2, json);
						updateStatement.setString(3, originalTitle);
						
						updateStatement.executeUpdate();
						
					} finally {

						if (updateStatement != null) {
							updateStatement.close();
						}
						
					}
					
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
		
	
	
	}
	
	
	@Override
	public boolean hasRec(long fbuserid) throws SQLException{
		boolean ret = false;
		
		PreparedStatement selectStatement = null;
		ResultSet resultSet = null;
		
		//int ouruserid = getUserIDforFbUserID(fbuserid);
		
		try {
			
			final String queryStr = "SELECT ID FROM TEST_RECP WHERE ID = (SELECT ID FROM TEST_USER WHERE FACEBOOKID= ? LIMIT 1)*1000 LIMIT 1";
			selectStatement = conn.prepareStatement(queryStr);
			selectStatement.setLong(1, fbuserid);
			resultSet = selectStatement.executeQuery();
			
			int i=0;
			while(resultSet.next()){
				//int userid = resultSet.getInt("USERID");
				i++;
				System.out.println("hasrec");
			}
			if(998<i){
				ret = true;
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

	
	protected String getEventsResponse_(ResultSet eventResults) throws SQLException {
		final Gson gson = new Gson();
		int rowCounter = 0;
		int showCounter = 0;
		StringBuilder responseBuilder = new StringBuilder("[");
		boolean first = true;
		int previousEventId = -1;
		FilmEvent filmEvent = new FilmEvent();
		PtEvent ptEvent = new PtEvent();
		Event currentEvent = null;
		while (eventResults.next()) {
			
			rowCounter++;
			if (rowCounter > FIRST_N_ROW) {
				break;
			}
			if (rowCounter % 50 == 0) {
				System.out.println(String.format("%d. row", rowCounter));
			}
			int currentEventId = eventResults.getInt(ShowColumn.EVENT_ID);
			
			if (currentEventId != previousEventId) { // we reached a new event
				
				if (previousEventId > 0) { // not the first event
					
					if (currentEvent != null) { // write out the previous event if it is not the first time
						if (!first) {
							responseBuilder.append(", ");
						}
						first = false;
						responseBuilder.append(gson.toJson(currentEvent));
					}
				}
				
				String discriminator = eventResults.getString(EventColumn.DISCRIMINATOR); 

				if (discriminator.equals("film")) {	// cinemacity 
					
					currentEvent = filmEvent;
					// there are some more...
//					filmEvent.setGenre(eventResults.getString(EventColumn.GENRE));
//					filmEvent.setActors(eventResults.getString(EventColumn.ACTORS));
//					filmEvent.setDirector(eventResults.getString(EventColumn.DIRECTOR));
//					filmEvent.setOriginalTitle(eventResults.getString(EventColumn.ORIGINAL_TITLE));
				
				} else { // programturizmus

					currentEvent = ptEvent;
					ptEvent.setPtEventType(PtEventType.valueOf(discriminator.toUpperCase()));
					//ptEvent.setPtEventType(PtEventType.MOVIE);
				}
				// set the attributes that are common to all events
				
				String name = eventResults.getString(EventColumn.NAME);
				String description = eventResults.getString(EventColumn.DESCRIPTION);
				
				currentEvent.setId(eventResults.getInt(EventColumn.ID));
				currentEvent.setName(USE_BASE64 ? toBase64(name) : name);
				currentEvent.setDescription(USE_BASE64 ? toBase64(description) : description);
				currentEvent.setThumbnail(eventResults.getString(EventColumn.THUMBNAIL));
				currentEvent.setImage(eventResults.getString(EventColumn.IMAGE));
				currentEvent.getShowList().clear();
				
			}
			previousEventId = currentEventId;
			
			if (showCounter < SHOW_LIMIT) {

				// add show to the current event
				Show show = new Show();
				String location = eventResults.getString(ShowColumn.LOCATION);
				String category = eventResults.getString(ShowColumn.CATEGORY);
				show.setLocation(USE_BASE64 ? toBase64(location) : location);
				show.setStart(new Date(eventResults.getLong(ShowColumn.GO)));
				show.setEnd(new Date(eventResults.getLong(ShowColumn.END)));
				show.setCategory(USE_BASE64 ? toBase64(category) : category);
				show.setX(eventResults.getFloat(ShowColumn.X));
				show.setY(eventResults.getFloat(ShowColumn.Y));
				currentEvent.getShowList().add(show);
				showCounter++;
			}
//			break;
		}
		if (currentEvent != null) { // write out the previous event if it is not the first time
			if (!first) {
				responseBuilder.append(", ");
			}
			first = false;
			responseBuilder.append(gson.toJson(currentEvent));
		}
		
		responseBuilder.append("]");
		return responseBuilder.toString();
	}
	
	@Override
	public List<Integer> getEventIdArrayFromLog() throws SQLException{
		PreparedStatement statement = null;
		ResultSet eventResults = null;
		List<Integer> l = new ArrayList<Integer>();
		try{
			String queryEventId = "select eventid from TEST_LOG";
			statement = conn.prepareStatement(queryEventId);
			eventResults = statement.executeQuery();
			while(eventResults.next()){
				l.add(eventResults.getInt("EVENTID"));
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

	@Override
	public HashMap<Integer, String> getAllThumbnail() throws SQLException{
		PreparedStatement statement = null;
		ResultSet eventResults = null;
		HashMap<Integer, String> idt = new HashMap<Integer, String>();
		try{
			String queryEventId = "SELECT ID,THUMBNAIL FROM TEST_EVENT";
			statement = conn.prepareStatement(queryEventId);
			eventResults = statement.executeQuery();
			while(eventResults.next()){
				Integer id = eventResults.getInt("ID");
				String t = eventResults.getString("THUMBNAIL");
				idt.put(id, t);
			}
			
		} finally {
			
			if (eventResults != null) {
				eventResults.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		
		return idt;
	}
	
	@Override
	public void uploadAllThumbnail(HashMap<Integer, String> idt) throws SQLException{

		String updateUserQuery = 
				"UPDATE TEST_EVENT SET THUMBNAIL = ? WHERE ID = ?";
		PreparedStatement updateStatement = null;
			
		try {
			for(Entry<Integer, String> act : idt.entrySet()){
				System.out.println(act.getKey());
				updateStatement = conn.prepareStatement(updateUserQuery);
				updateStatement.setString(1, act.getValue());
				updateStatement.setInt(2, act.getKey());
				updateStatement.executeUpdate();
			}
			
		} finally {
			
			if (updateStatement != null) {
				updateStatement.close();
			}
		}
	}
	
	@Override
	public HashMap<Integer, String> getAllImage() throws SQLException{
		PreparedStatement statement = null;
		ResultSet eventResults = null;
		HashMap<Integer, String> idi = new HashMap<Integer, String>();
		try{
			String queryEventId = "SELECT ID,IMAGE FROM TEST_EVENT";
			statement = conn.prepareStatement(queryEventId);
			eventResults = statement.executeQuery();
			while(eventResults.next()){
				Integer id = eventResults.getInt("ID");
				String i = eventResults.getString("IMAGE");
				idi.put(id, i);
			}
			
		} finally {
			
			if (eventResults != null) {
				eventResults.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return idi;
	}
	
	@Override
	public void uploadAllImage(HashMap<Integer, String> idi) throws SQLException{

		String updateUserQuery = 
				"UPDATE TEST_EVENT SET IMAGE = ? WHERE ID = ?";
		PreparedStatement updateStatement = null;
			
		try {
			for(Entry<Integer, String> act : idi.entrySet()){
				System.out.println(act.getKey());
				System.out.println(act.getValue());
				updateStatement = conn.prepareStatement(updateUserQuery);
				updateStatement.setString(1, act.getValue());
				updateStatement.setInt(2, act.getKey());
				updateStatement.executeUpdate();
			}
			
		} finally {
			
			if (updateStatement != null) {
				updateStatement.close();
			}
		}
	}
	
	@Override
	public HashMap<Integer, String> getAllVideo() throws SQLException{
		PreparedStatement statement = null;
		ResultSet eventResults = null;
		HashMap<Integer, String> idi = new HashMap<Integer, String>();
		try{
			String queryEventId = "SELECT ID,VIDEO FROM TEST_EVENT";
			statement = conn.prepareStatement(queryEventId);
			eventResults = statement.executeQuery();
			while(eventResults.next()){
				Integer id = eventResults.getInt("ID");
				String i = eventResults.getString("VIDEO");
				idi.put(id, i);
			}
			
		} finally {
			
			if (eventResults != null) {
				eventResults.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
		return idi;
	}
	
	@Override
	public void uploadAllVideo(HashMap<Integer, String> idi) throws SQLException{

		String updateUserQuery = 
				"UPDATE TEST_EVENT SET VIDEO = ? WHERE ID = ?";
		PreparedStatement updateStatement = null;
			
		try {
			for(Entry<Integer, String> act : idi.entrySet()){
				System.out.println(act.getKey());
				System.out.println(act.getValue());
				updateStatement = conn.prepareStatement(updateUserQuery);
				updateStatement.setString(1, act.getValue());
				updateStatement.setInt(2, act.getKey());
				updateStatement.executeUpdate();
			}
			
		} finally {
			
			if (updateStatement != null) {
				updateStatement.close();
			}
		}
	}
	
	
	@Override
	public void copyFromRecToRecP() throws SQLException {
		PreparedStatement statement = null;
		ResultSet eventResults = null;
		
		PreparedStatement uploadStatement = null;
		ResultSet uploadEventResults = null;
		
		try{
			String queryEventId = "SELECT * FROM TEST_REC WHERE USERID = ? ORDER BY RANK DESC LIMIT 1000";
			statement = conn.prepareStatement(queryEventId);
			
			String upload = "INSERT INTO TEST_RECP (ID, EVENTID, RANK) VALUES (?, ?, ?)";
			uploadStatement = conn.prepareStatement(upload);
			
			for(int i=361;i<400;i++){
				statement.setInt(1, i);
				eventResults = statement.executeQuery();

				int counter = 0;
				while(eventResults.next()){
					
					int mainid = i*1000+counter;
					counter++;
					uploadStatement.setInt(1, mainid);
					uploadStatement.setInt(2, eventResults.getInt("EVENTID"));
					uploadStatement.setDouble(3, eventResults.getDouble("RANK"));
					uploadStatement.executeUpdate();
					
				}
			}
		
			
		} finally {
			
			if (eventResults != null) {
				eventResults.close();
			}
			if (statement != null) {
				statement.close();
			}
		}
	
	}

	
	@Override
	public void insertNewLinesInRecP(int lineid){
		String selectQuery = 
				"INSERT INTO `TEST_RECP`(`ID`, `EVENTID`, `RANK`) VALUES (?, ?, ?)";
		PreparedStatement insertLogStatement = null;
		try {
			insertLogStatement = conn.prepareStatement(selectQuery);
			insertLogStatement.setInt(1, lineid);
			insertLogStatement.setInt(2, 0);
			insertLogStatement.setInt(3, 0);
			
			insertLogStatement.executeUpdate();
		}catch(SQLException e){
			
		}
		finally {
		
			if (insertLogStatement != null) {
				try {
					insertLogStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public int getRecPCountForUser(int userid){
		PreparedStatement statement = null;
		ResultSet eventResults = null;

		int queryUserID = userid*1000;
		int queryUserIDEnd = (userid+1)*1000;
		int ret = 0;
		try{
			String queryEventId = "SELECT COUNT(*) AS COUNT FROM `TEST_RECP` WHERE `ID`>= ? AND `ID`< ?";
			try {
				statement = conn.prepareStatement(queryEventId);
				statement.setInt(1, queryUserID);
				statement.setInt(2, queryUserIDEnd);
				eventResults = statement.executeQuery();
				while(eventResults.next()){
					ret = eventResults.getInt("COUNT");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		
			
		} finally {
			
			if (eventResults != null) {
				try {
					eventResults.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}
	
	//made by dacide  rec. accuracy
	@Override
	public List<Integer> getUserIdArrayFromTestUser() throws SQLException{
		PreparedStatement statement = null;
		ResultSet eventResults = null;
		List<Integer> l = new ArrayList<Integer>();
		try{
			String queryEventId = "SELECT ID FROM TEST_USER";
			statement = conn.prepareStatement(queryEventId);
			eventResults = statement.executeQuery();
			while(eventResults.next()){
				l.add(eventResults.getInt("ID"));
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
	
	//------------------------------------------------------------------
	//made by dacide  rec. accuracy
		@Override
	public HashMap<Integer,Double> getEventIdArrayForXYUser(int UserId) throws SQLException{
			PreparedStatement statement = null;
			ResultSet eventResults = null;
			HashMap<Integer,Double> hm = new HashMap<Integer,Double>();
			try{
				String queryEventId = "SELECT EVENTID, RANK FROM `TEST_RECP` WHERE USERID= ?";
				statement = conn.prepareStatement(queryEventId);
				statement.setInt(1,UserId);
				eventResults = statement.executeQuery();
				while(eventResults.next()){
					hm.put(eventResults.getInt("EVENTID"), eventResults.getDouble("RANK"));
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
	
		
	// made by dacide rec. accuracy
	@Override
	public HashMap<Integer,Integer> eventNumberInLogtoUser(int userId) throws SQLException{
			
			HashMap<Integer,Integer> hm = new HashMap<Integer,Integer>();		
			
			PreparedStatement getShowsStatement = null;
			ResultSet showResults = null;
			
			PreparedStatement getShowsStatement2 = null;
			ResultSet showResults2 = null;

			try {
				
				String queryStr = "SELECT EVENTID FROM TEST_LOG WHERE USERID=?";
				getShowsStatement = conn.prepareStatement(queryStr);
				getShowsStatement.setInt(1,userId);
				showResults = getShowsStatement.executeQuery();

				while (showResults.next()) {
					Integer eventid = showResults.getInt("EVENTID");
					
					if(hm.get(eventid) != null){
						Integer eventidCount = hm.get(eventid);
						eventidCount=eventidCount+1;
						hm.remove(eventid);
						hm.put(eventid,eventidCount);
					//	System.out.println("put "+eventid+" "+eventidCount);
					}else{
						hm.put(eventid, 1);
					}
					
				}
					
			} finally {
				if (showResults != null)  {
					showResults.close();
				}
				
				if (getShowsStatement != null) {
					getShowsStatement.close();
				}
				
			
			}
			
			try{
				String queryStr2 = "SELECT ID FROM TEST_EVENT";
				getShowsStatement2 = conn.prepareStatement(queryStr2);
				showResults2 = getShowsStatement2.executeQuery();
				
				while (showResults2.next()) {
					
					Integer eventid = showResults2.getInt("ID");
				//	System.out.println("eid"+eventid);
					if(hm.get(eventid)==null){
						hm.put(eventid, 0);
					}
				}
			}finally{
				
				if (showResults2 != null)  {
					showResults2.close();
				}
				
				if (getShowsStatement2 != null) {
					getShowsStatement2.close();
				}
			}
			
			
			return hm;
		}
	
	// visszaadja az event listat ha van show-juk
	@Override
	public List<Integer> eventsInShowList(int from) throws SQLException{
			List<Integer> eventIdList = new ArrayList<Integer>();
//			Date nowTime = new Date();
//			Date nowPlusTenDays = new Date();
//			nowPlusTenDays.setTime(nowTime.getTime()+864000000L);
			
			long timestamp = System.currentTimeMillis();
			long tendaystimestamp = timestamp + 864000000L;
			
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(timestamp);
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH) + 1;
			int day = cal.get(Calendar.DAY_OF_MONTH);
			Integer date1 = year * 10000 + month * 100 + day;
			
			cal.setTimeInMillis(tendaystimestamp);
			int year2 = cal.get(Calendar.YEAR);
			int month2 = cal.get(Calendar.MONTH) + 1;
			int day2 = cal.get(Calendar.DAY_OF_MONTH);
			Integer date2 = year2 * 10000 + month2 * 100 + day2;
			
			PreparedStatement getShowsStatement = null;
			ResultSet showResults = null;
			
			try {				
				String queryStr = "SELECT ID FROM TEST_EVENT WHERE LANG_CODE='HU' AND (SELECT EVENT_ID FROM TEST_SHOW WHERE TEST_SHOW.day >= ? AND TEST_SHOW.day <= ?  AND TEST_EVENT.ID=EVENT_ID LIMIT 1)  ";
				getShowsStatement = conn.prepareStatement(queryStr);
				getShowsStatement.setInt(1, date1);
				getShowsStatement.setInt(2, date2);
				showResults = getShowsStatement.executeQuery();
				while(showResults.next()){
					Integer ot = showResults.getInt("ID");	
					if(!eventIdList.contains(ot) && ot != 0){	// because of the 2030 event
						eventIdList.add(ot);
					}
				}
				
			} finally {
				if (showResults != null)  {
					showResults.close();
				}
				if (getShowsStatement != null) {
					getShowsStatement.close();
				}
			}
			return eventIdList;
		}
	
	@Override
	public long getFbUserIDforUserID(long userId) throws SQLException {
		long ret = 0;

		PreparedStatement statement = null;
		ResultSet results = null;
		
		try {
			statement = conn.prepareStatement("SELECT facebookid FROM TEST_USER WHERE id = ?");
			statement.setLong(1, userId);
			results = statement.executeQuery();
			
			if (results.next()) {
				ret = results.getLong(UserColumn.FACEBOOK_ID);
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
	
	@Override
	public int getUserIDforFbUserID(long fbuserId) throws SQLException{
		long ret = 0;

		PreparedStatement selectStatement = null;
		PreparedStatement insertStatement = null;
		ResultSet results1 = null;
		ResultSet results2 = null;
		try {
			selectStatement = conn.prepareStatement("SELECT ID FROM TEST_USER WHERE FACEBOOKID = ?");
			selectStatement.setLong(1, fbuserId);
			results1 = selectStatement.executeQuery();
			boolean egyvolt=true;
			if (results1.next()) {
				egyvolt=false;
				ret = results1.getLong(UserColumn.ID);
			}
			if(egyvolt){
				insertStatement = conn.prepareStatement("INSERT INTO TEST_USER (FACEBOOKID) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
				insertStatement.setLong(1, fbuserId);
				insertStatement.executeUpdate();
				results2 = insertStatement.getGeneratedKeys();
				if (results2.next()) {
					ret = results2.getInt(1);
				}
			}
			
		} finally {
			
			if (results1 != null) {
				results1.close();
			}
			if (selectStatement != null) {
				selectStatement.close();
			}
			if (results2 != null) {
				results2.close();
			}
			if (insertStatement != null) {
				insertStatement.close();
			}
		}
		
		return (int)ret;
	}

	protected int getDay(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		return year * 10000 + month * 100 + day;
	}

	protected long getDaysStart(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 1);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}


	protected class DaysEvents {
		public int day;
		public Set<Event> events;

		public DaysEvents(int day) {
			super();
			this.day = day;
			this.events = new LinkedHashSet<Event>();
		}
		public DaysEvents(int day, Set<Event> sortedEventsSet) {
			super();
			this.day = day;
			this.events = sortedEventsSet;
		}
	}
	
	protected class EventsShows {
		public int eventId;
		public Set<ToJsonShow> shows;
		
		public EventsShows(int eventId) {
			this.eventId = eventId;
			this.shows = new LinkedHashSet<ToJsonShow>();
		}
	}
	
	protected class Response {
		public Collection<DaysEvents> daysEvents;
		public Collection<EventsShows> eventsShows;
		public List<String> jsonShows;
		
		public Response(Collection<DaysEvents> daysEvents, Collection<EventsShows> eventsShows) {
			super();
			this.daysEvents = daysEvents;
			this.eventsShows = eventsShows;
		}
		
		public Response(Collection<DaysEvents> daysEvents, List<String> jsonShows){
			super();
			this.daysEvents = daysEvents;
			this.jsonShows = jsonShows;
		}
	}

	
	

	
	/*-----------------------------new db functions------------------------------*/
	
	/**Returns true if has next line, and loadup LogLine object with data*/
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

	/**Returns all events id in a string list*/
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
	
	/**Returns events id which have future show 
	 * - uses two Date time interval and convert these into integer dates*/
	public List<Integer> getLegitEventsIdV2(Date from, Date to){
		PreparedStatement getEventStatement = null;
		ResultSet eventResult = null;
		List<Integer> eventList = new ArrayList<Integer>();
		Integer intDateFrom = getIntegerDate(from.getTime());
		Integer intDateTo = getIntegerDate(to.getTime());
		String queryStr = "SELECT id FROM Events WHERE ((isOk=1 OR isOk=2) AND (SELECT EventId FROM EventDays WHERE EventDays.day >= ? AND EventDays.day <= ? AND Events.id=EventId LIMIT 1))";
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

	/**Returns legit events in int List.*/
	public List<Integer> getLegitEventsIdFromDate(long from){
		List<Integer> eventIdList = new ArrayList<Integer>();
		Integer intDate = getIntegerDate(from);		
		PreparedStatement getEventStatement = null;
		ResultSet eventResults = null;
		try {				
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
	
	/**Get legit events with isIn=0 flag. In the maintanence class call this first, before uploadIsinFlagForEvents() !!!*/
	public List<Integer> getLegitNotinEventsIdFromDate(long from){
		List<Integer> eventIdList = new ArrayList<Integer>();
		Integer intDate = getIntegerDate(from);		
		PreparedStatement getEventStatement = null;
		ResultSet eventResults = null;
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
	
	/**Returns all events in hashmap with ISIn value.*/
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
	
	/**Returns legit events click number from EventLogs Table*/
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
	
	/**Returns a hasmap with event-rank key;value for one user*/
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
	
	/**Returns list of hasmaps. 0. - eventName; 1- eventDesc.*/
	public List<HashMap<Integer,String>> getLegitEventDescAndNameFromDate(long from) throws SQLException{
		HashMap<Integer,String> eventDesc = new HashMap<Integer,String>();
		HashMap<Integer,String> eventName = new HashMap<Integer,String>();
		Integer intDateFrom = getIntegerDate(from);
		List<HashMap<Integer,String>> hashmapList = null;
		PreparedStatement getEventStatement = null;
		ResultSet eventResults = null;	
		try {				
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

	/**Returns all event description in hm (eventid - description string)*/	//make one for date
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
	
	/**Returns events description from date. Uses converted long number for date*/
	public HashMap<Integer,String> getEventDescriptionFromDateV2(long fromDate) throws SQLException{
		HashMap<Integer,String> s = new HashMap<Integer,String>();
		Integer date = getIntegerDate(fromDate);
		PreparedStatement getEventStatement = null;
		ResultSet eventResults = null;		
		try {
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
	
	/**Returns all event discriminator in a hasmap*/ //make one for date
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
	
	/**Return events discriminator FROM DATE in HM (eventid - discriminator)*/
	public HashMap<Integer,String> getEventDiscriminatorFromDateV2(long fromDate) throws SQLException{
		HashMap<Integer,String> s = new HashMap<Integer,String>();
		Integer date = getIntegerDate(fromDate);
		PreparedStatement getEventStatement = null;
		ResultSet eventResults = null;		
		try {
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
	
	/**Upload isin flag in Events table.*/
	public void updateIsinFlagForEvents(List<Integer> legitEventsIdFromDate){
		PreparedStatement updateStatement = null;
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
					}else{	// if event is legit(future event, and isok=1,3), but isin = false, set isin=true
						updateStatement.setInt(1, 1);
						updateStatement.setTimestamp(2, ts);
						updateStatement.setInt(3, EventId);
						updateStatement.addBatch();
					}
				}else{
					if(isinFlag==false){
						//dont do anything
					}else{ //if event isnt legit (not future and/or isok=0) but isin = true
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
	
	/**Returns userid list, who has record in the UserDiscriminatorsRanks table*/
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
	
	/**Upload/Update user/users discriminator rank values*/
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
	
	/**Returns for ONE or ALL USER their discriminator ranks. For one user onlyoneuser = 1.
	 * Do not returns the 0. user disc. ranks*/
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
	
	/**Returns all tag for all event in a hashmap. (eventid - tagList)*/
	public HashMap<Integer, List<String>> getAllEventsTagV2() throws SQLException{
		HashMap<Integer, List<String>> eventTag = new HashMap<Integer, List<String>>();
		List<String> tagArray = new ArrayList<String>();
		PreparedStatement getEventTagStatement = null;
		ResultSet eventTagResults = null;		
		try {			
			String queryStr = "SELECT EventId,tagJson FROM EventTags";		
			getEventTagStatement = conn.prepareStatement(queryStr);
			eventTagResults = getEventTagStatement.executeQuery();		
			while (eventTagResults.next()) {
				tagArray = parseTagJson(eventTagResults.getString("tagJson"));	// MEG KELL CSIN�LNI A PARSOL�ST !
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

	/**Returns all tag for legit events from long date converted into integer date*/
	public HashMap<Integer, List<String>> getEventsTagFromDateV2(long fromDate) throws SQLException{
		HashMap<Integer, List<String>> eventTag = new HashMap<Integer, List<String>>();
		List<String> tagArray = new ArrayList<String>();
		PreparedStatement getEventTagStatement = null;
		Integer date = getIntegerDate(fromDate);
		ResultSet eventTagResults = null;		
		try {			
			String queryStr = "SELECT EventId,tagJson FROM EventTags WHERE (SELECT EventId FROM EventDays WHERE EventDays.day >= ? AND EventTags.EventId=EventId LIMIT 1)  ";		
			getEventTagStatement = conn.prepareStatement(queryStr);
			getEventTagStatement.setInt(1, date);
			eventTagResults = getEventTagStatement.executeQuery();		
			while (eventTagResults.next()) {
				tagArray = parseTagJson(eventTagResults.getString("tagJson"));	// MEG KELL CSIN�LNI A PARSOL�ST !
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
	
	/**Insert a line into eventlog
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
	
	/**Insert new log line into RecommendationLogs*/
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
	
	/**Insert a line into RecommendationLog
	 * - gravity
	 * - second step
	 * - iALS*/
	public String insertRECLogV2(String comment, Integer UserId) throws SQLException{
		Date local = new Date();
		DateTimeZone zone = DateTimeZone.getDefault();
		long utc = zone.convertLocalToUTC(local.getTime(), false);
		Timestamp ts = new Timestamp(utc);
		String selectQuery = 
				"INSERT INTO RecommendationLogs (comment, createdAt, updatedAt, UserId) VALUES (?,?,?,?)";
		PreparedStatement insertLogStatement = null;
		Date nowDate = new Date();
		try {
			insertLogStatement = conn.prepareStatement(selectQuery);
			insertLogStatement.setString(1, comment);
			insertLogStatement.setTimestamp(2, ts);
			insertLogStatement.setTimestamp(3, ts);
			insertLogStatement.setInt(4, UserId);
			insertLogStatement.executeUpdate();
		} finally {
			if (insertLogStatement != null) {
				insertLogStatement.close();
			}
		}
		return null;
	}
	
	/**NOT USED. USE 3rd VERSION! Insert newly calculated rank values for user event pairs
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
	
	/**Delete, Update, Insert events rank value into UserEventRecommendations table*/
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
	
	/**JUST - Delete, Insert events rank value into UserEventRecommendations table*/
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
	
	/**Insert into NewUserEventChache table the newly calculated first step data. Create shouldChache flag too.*/
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
	
	/**Update caching flag for GetEventsResponseCaches table*/
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
	
	/**Returns one users recommendation data. hashmap(eventid - double_rank_value)*/
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
	
	/**Returns FirstStep ZERO user eventid and rank values*/
	public HashMap<Integer, Double> getZeroUserRanks(){
		HashMap<Integer, Double> rankValues = new HashMap<Integer, Double>();
		PreparedStatement getRecStatement = null;
		ResultSet resultSet = null;
		String insertQuery = "SELECT rank, EventId FROM NewUserEventCaches WHERE 1";
		try {
			getRecStatement = conn.prepareStatement(insertQuery);
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
	
	/**Returns the number of users*/
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
	
	/**Returns all userid and facebookid in hasmap*/
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
	
	/**Return all users ID in a list*/	// for Rec. accuracy getUserIdArrayFromTestUser
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
	
	/**Returns FacebookId for UserId - Returns 0 if sth. goes wrong*/
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
	
	/**Returns facebookid for one user. Uses userid*/
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
	
	/**Returns all user last visit on site from Users table*/
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
	
	/**Returns all event ranks for one user in hashmap. use userid*/
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
	
	/**Returns true for userId if have in the UserEventRecommendations any rows (:= has rec)*/
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
	
	/**Returns users age; if the age is negative age is 0*/
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
	
	/**Returns all like for one user in a Like list. (userid - List<Like>)*/
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

	/**Returns all likes for facebook user; convert fbuserid to userid, !use the other! function to get likes*/
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
	
	/**Returns all our facebook like id from LikeImdbTags tabl. USe MEtadataFromthirdparty class*/
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
	
	/**Returns all our facebook like id from LikeLastfmTags table use Metadatafromthirdparty class*/
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
	
	/**Upload facebook music like tags into Lastfm table. Use Metadatafromthirdparty class*/
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

	/**Upload facebook music like tags into IMDB table*/
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
	
	/**Return timestamp date in integer*/
	protected Integer getIntegerDate(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		Integer date = year * 10000 + month * 100 + day;
		return date;
	}
	
	/**tagJson*/
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

	/**Returns the created json object string*/
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
}
