package com.incredibles.storage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.incredibles.data.Like;
import com.incredibles.data.Rec;

/**
 * Interface that used by the servlet when accessing the persistent store
 */
public interface ClientDbService {

	/**
	 * Upload the users facebook likes
	 * @throws SQLException 
	 */
	public String uploadUserTag(long fbuserid, String jsonString, String name, String email, String birthdate) throws SQLException;


	/**
	 * Get the events in an interval for a certain user
	 * 
	 * @param from
	 *            Start of the event's show
	 * @param to
	 *            End of the event's show
	 * @throws SQLException 
	 */
	public String getRecommendedEvents(long fbuserid, long from, long to, String lang) throws SQLException;

	/**
	 * Get the events in an interval
	 * 
	 * @param from
	 *            Start of the event's show
	 * @param to
	 *            End of the event's show
	 * @throws SQLException 
	 */
	public String getEvents(long from, long to, String lang) throws SQLException;
	
	/**
	 * Get the shows in an interval for a certain event
	 * 
	 * @param eventId The id of the event for which the shows are queried
	 * @param from The minimum value of the start of the show
	 * @param to The maximum value of the start of the show
	 * 
	 * @return A string that contains the json response
	 * 
	 * @throws SQLException If an error occured when accessing the database
	 * @throws IOException 
	 */
	public String getShows(int eventId, long from, long to) throws SQLException, IOException;

	/**
	 * Get an event and its shows in a certain interval
	 * 
	 * @param eventId
	 *            The id of the event
	 * @param from
	 *            The earliest possible start of a show
	 * @param to
	 *            The latest possible start of a show
	 *            
	 * @return A string that contains the json response with the event and its
	 *         shows
	 * @throws SQLException
	 */
	public String getSingleEventWithShows(int eventId, long from, long to) throws SQLException;
	
	/**
	 * Get the facebookID that belongs to the user id (the id of the row that
	 * contains the facebook id
	 * 
	 * @param userId The id of the user
	 * 
	 * @return The facebookId of the user
	 * @throws SQLException 
	 */
	public long getFbUserIDforUserID(long userId) throws SQLException;
	
	/**
	 * Get the userID that belongs to the user FB id (the id of the row that
	 * contains the facebook id
	 * 
	 * @param userId The id of the user
	 * 
	 * @return The facebookId of the user
	 * @throws SQLException 
	 */
	public int getUserIDforFbUserID(long fbuserId) throws SQLException;

	/**
	 * Release any resources that are used by the implementation
	 * 
	 * @throws IOException
	 *             If an error occured while releaseing resources
	 */
	public void close() throws IOException, SQLException;
	
	public List<Like> getAllLikeForFbUserId(long fbuserid) throws SQLException;

	List<Integer> getEventIdArrayFromLog() throws SQLException;

	public boolean hasRec(long parseLong) throws SQLException;

	void uploadEventTag(List<String> s) throws SQLException;

	HashMap<Integer, String> getAllEventDesc() throws SQLException;


	void uploadAllEventsTag(HashMap<Integer, Integer> felt) throws SQLException;

	int getAllUserNumber() throws SQLException;

	HashMap<Integer, Integer> getEventsCount() throws SQLException;

	LinkedHashMap<Integer, Integer> getEventTagSwitch() throws SQLException;





	void uploadBatchTempRec(List<Rec> rec) throws SQLException;

	void deleteAllTempCount() throws SQLException;

	void copyFromTempToRec(int userid) throws SQLException;

	void copyNewEventsToOldUsers() throws SQLException;

	List<Integer> getUserIdArrayFromTestUser() throws SQLException;

	HashMap<Integer, Double> getEventIdArrayForXYUser(int UserId)
			throws SQLException;

	HashMap<Integer, Integer> eventNumberInLogtoUser(int userId)
			throws SQLException;

	List<String> getAllMovieTitle() throws SQLException;


	public HashMap<Integer, Long> getAllFaceAndUserId() throws SQLException;

	String uploadLog(long fbuserid, int eventid, long timestamp, int type)
			throws SQLException;


	void uploadMoviePoster(String originalTitle, String link, List<String> list)
			throws SQLException;


	HashMap<Integer, String> getEventDiscriminatorFromDate(long fromDate)
			throws SQLException;




	HashMap<Integer, String> getAllThumbnail() throws SQLException;


	HashMap<Integer, String> getAllImage() throws SQLException;


	void uploadAllThumbnail(HashMap<Integer, String> idt) throws SQLException;


	void uploadAllImage(HashMap<Integer, String> idi) throws SQLException;


	HashMap<Integer, String> getAllVideo() throws SQLException;


	void uploadAllVideo(HashMap<Integer, String> idi) throws SQLException;


	void uploadShowsToShowJson();

	void updateShowToShowJson(long eventid);


	void checkUpdateableShowsJson();


	String insertCMSLog(String type) throws SQLException;


	List<Integer> getAllEventsID();


	int getRecPCountForUser(int userid);


	void insertNewLinesInRecP(int lineid);
	
	public String insertRECLog(String type, Integer userID) throws SQLException;

	void uploadShowsToDays(int eventId);


}
