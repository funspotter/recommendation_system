package com.incredibles.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.incredibles.data.Like;
import com.incredibles.data.LogTableInfoClass;
import com.incredibles.data.Rec;

/**
 * Simple implementation of {@link DbManager} wich uses a txt file for the log
 */
class SimpleDbManager{

	/** The default url of the logfile, when no other is specified */
	private static final String DEFAULT_LOG_FILE_URL = "files/TV1.txt";
	/**
	 * The default splitter that is used in the logFile between userId, eventId and
	 * timestamp when no other splitter is specified
	 */
	private static final String DEFAULT_SPLITTER = "\\|";
	
	/** Reader to be used to read the logFile */
	private BufferedReader reader;
	/** The url of the logFile */
	private String logFileUrl;
	/** The dateformat to be used to parse the dates in the logfile */
	private final DateFormat dateFormat;
	/** This contains the current line of the logfile */
	private String currentLine;

	/**
	 * The splitter that is used in the logFile between userId, eventId and
	 * timestamp
	 */
	private String splitter;

	/**
	 * Constructor - the default logFileUrl and splitter will be used
	 * 
	 * @throws IOException
	 *             If an error occured when opening the file
	 */
	public SimpleDbManager() throws IOException {
		this(DEFAULT_LOG_FILE_URL);
	}

	/**
	 * Constructor - the default splitter will be used
	 * 
	 * @param logFileUrl
	 *            The url of the logfile
	 * @throws IOException
	 *             If an error occured when opening the file
	 */
	public SimpleDbManager(String logFileUrl) throws IOException {
		this(logFileUrl, DEFAULT_SPLITTER);
	}

	/**
	 * Constructor
	 * 
	 * @param logFileUrl
	 *            The url of the logfile
	 * @param splitter
	 *            The splitter that is used in the logFile between userId,
	 *            eventId and timestamp
	 * 
	 * @throws IOException
	 *             If an error occured when opening the file
	 */
	public SimpleDbManager(String logFileUrl, String splitter) throws IOException {
		this.logFileUrl = logFileUrl;
		this.splitter = splitter;
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(this.logFileUrl);
		this.reader = new BufferedReader(new InputStreamReader(is));
//		this.reader = new BufferedReader(new FileReader(this.logFileUrl));
		this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	}

	// methods inherited from RecommenderDbService
	
	public boolean getNextLogLine(LogLine line) throws IOException {

		boolean ret;
		currentLine = reader.readLine();
		
		if (currentLine != null) {
			String parts[] = currentLine.split(splitter);
			try {
				line.userId = Integer.parseInt(parts[0]);
				line.eventId = Integer.parseInt(parts[1]);
				line.timeStamp = dateFormat.parse(parts[2]);
			} catch (NumberFormatException e) {
				System.err.println(e.getMessage());
			} catch (ParseException e) {
				System.err.println(e.getMessage());
			}
			ret = true;
		} else {
			ret = false;
		}
		return ret;
		
	}


	
	public void close() throws IOException {
		reader.close();
	}

	// methods inherited from ClientDbService
	

	
	public String getRecommendedEvents(long fbuserid, long from, long to, String lang) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getEvents(long from, long to, String lang) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public String getShows(int eventId, long from, long to) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public int getUserIDforFbUserID(long fbuserId){
		// TODO Auto-generated method stub
		return 0;
	}

	
	public long getFbUserIDforUserID(long userId) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public List<Like> getAllLikeForFbUserId(long fbuserid)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<Integer> getEventIdArrayFromLog() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}


	
	public boolean hasRec(long parseLong) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public void uploadEventTag(List<String> s) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	
	public HashMap<Integer, String> getAllEventDesc() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void uploadAllEventsTag(HashMap<Integer, Integer> felt)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	
	public int getAllUserNumber() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public HashMap<Integer, Integer> getEventsCount() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}


	
	public LinkedHashMap<Integer, Integer> getEventTagSwitch()
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}


	
	public void uploadBatchTempRec(List<Rec> rec) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	
	public void deleteAllTempCount() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	
	public void copyFromTempToRec(int userid) throws SQLException {
		// TODO Auto-generated method stub
		
	}



	
	public void copyNewEventsToOldUsers() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	
	public List<Integer> getUserIdArrayFromTestUser() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public HashMap<Integer, Double> getEventIdArrayForXYUser(int UserId)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public HashMap<Integer, Integer> eventNumberInLogtoUser(int userId)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<String> getAllMovieTitle() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}



	
	public HashMap<Integer, Long> getAllFaceAndUserId() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}


	
	public String getSingleEventWithShows(int eventId, long from, long to) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public String uploadUserTag(long fbuserid, String jsonString, String name,
			String email, String birthdate) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Integer getUserBirthDate(Long facebookId) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public String uploadLog(long fbuserid, int eventid, long timestamp, int type)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<HashMap<Integer,String>> getExistEventDescAndName(long from)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void copyFromRecToRecP() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	
	public void updateRecP(int userid,
			LinkedHashMap<Integer, Double> uploadableEvents) {
		// TODO Auto-generated method stub
		
	}

	
	public void uploadMoviePoster(String originalTitle, String link,
			List<String> list) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	
	public HashMap<Integer, String> getEventDiscriminatorFromDate(long fromDate)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public HashMap<Integer, String> getEventsGenreFromDate(long fromDate)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<LogTableInfoClass> getInfoFromLog(int type, int userId)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public HashMap<Integer, Double> getRankforEvent(Integer userid)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void deleteInfoFromLog(LogTableInfoClass deleteThis)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	
	public HashMap<Integer, String> getAllThumbnail() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public HashMap<Integer, String> getAllImage() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void uploadAllThumbnail(HashMap<Integer, String> idt)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	
	public void uploadAllImage(HashMap<Integer, String> idi)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	
	public HashMap<Integer, String> getAllVideo() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void uploadAllVideo(HashMap<Integer, String> idi)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

	
	public void uploadShowsToShowJson() {
		// TODO Auto-generated method stub
		
	}

	
	public void updateShowToShowJson(long eventid) {
		// TODO Auto-generated method stub
		
	}

	
	public void checkUpdateableShowsJson() {
		// TODO Auto-generated method stub
		
	}

	
	public String insertCMSLog(String type) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<Integer> getAllEventsID() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public int getRecPCountForUser(int userid) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public void insertNewLinesInRecP(int lineid) {
		// TODO Auto-generated method stub
		
	}

	
	public List<Integer> eventsInShowList(int from) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	public String insertRECLog(String type, Integer userID) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void uploadShowsToDays(int eventId) {
		// TODO Auto-generated method stub
		
	}

	
	public List<Integer> getLegitEventsNumber(Date from, Date to) {
		// TODO Auto-generated method stub
		return null;
	}

	
}