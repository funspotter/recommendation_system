package com.incredibles.storage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gson.JsonArray;
import com.incredibles.data.Like;
import com.incredibles.data.LogTableInfoClass;
import com.incredibles.data.MetadataFromThirdParty;
import com.incredibles.data.Rec;

/**
 	* Interface that is used by the recommender when accessing the persistent store 
 */
	public interface RecommenderDbService extends ClientDbService {

	/**
	 * Get the next line of the log, wich contains that wich user clicked on
	 * wich event and when did that happen
	 * 
	 * @param line
	 *            {@link LogLine} instance in wich the data of the next line
	 *            will be written
	 * 
	 * @throws IOException
	 *             If an error occured while reading the next line
	 * 
	 * @return If there are more lines in the log
	 * @throws SQLException 
	 */
	public boolean getNextLogLine(LogLine line) throws IOException, SQLException;
	
	/**
	 * Upload that wich event is recommended to wich user, and on wich weighted place (rank)
	 * 
	 * @param userID The id of the user to wich the recommendation was made
	 * @param eventID The id of the event wich was recommended to the user
	 * @param rank The weighted rank of the recommendation
	 * @throws SQLException 
	 */

	Integer getUserBirthDate(Long facebookId) throws SQLException;
	
	
	/*Returns Legit events Descriptio, and name*/
	List<HashMap<Integer,String>> getExistEventDescAndName(long from) throws SQLException;

	public void copyFromRecToRecP() throws SQLException;
	
	public void updateRecP(int userid, LinkedHashMap<Integer, Double> uploadableEvents);
	
	HashMap<Integer, String> getEventsGenreFromDate(long fromDate) throws SQLException;
	
	public List<LogTableInfoClass> getInfoFromLog(int type,int userId) throws SQLException;

	HashMap<Integer, Double> getRankforEvent(Integer userid) throws SQLException;
	
	public void deleteInfoFromLog(LogTableInfoClass deleteThis) throws SQLException;

	List<Integer> eventsInShowList(int from) throws SQLException;
	
	public List<Integer> getLegitEventsNumber(Date from, Date to);
	
	/*-----------------------------new db functions------------------------------*/
	
	/**Returns true if has next line, and loadup LogLine object with data*/
	public boolean getNextLogLineV2(LogLine line) throws SQLException;
	
	/**Returns all events id in a string list*/
	public List<Integer> getAllEventsId();
	
	/**Returns all events in hashmap with ISIn value.*/
	public HashMap<Integer, Boolean> getAllEventsWithIsinFlag();
	
	/**Upload isin flag in Events table.*/
	public void updateIsinFlagForEvents(List<Integer> legitEventsIdFromDate);
	
	/**Returns events id witch have future show 
	 * - uses two Date time interval and convert these into integer dates*/
	public List<Integer> getLegitEventsIdV2(Date from, Date to);
	
	/**Returns legit events in int List.*/
	public List<Integer> getLegitEventsIdFromDate(long from);
	
	/**Returns legit events click number from EventLogs Table*/
	public HashMap<Integer, Integer> countLegitEventsClick();
	
	/**Get legit events with isIn=0 flag. In the maintanence class call this first, before uploadIsinFlagForEvents() !!!*/
	public List<Integer> getLegitNotinEventsIdFromDate(long from);
	
	/**Returns a hasmap with event-rank key;value for one user*/
	public HashMap<Integer,Double> getRankforEventV2(Integer userid) throws SQLException;
	
	/**Returns list of hasmaps. 0. - eventName; 1- eventDesc.*/
	public List<HashMap<Integer,String>> getLegitEventDescAndNameFromDate(long from) throws SQLException;
	
	/**Returns all event description in hm (eventid - description string)*/
	public HashMap<Integer,String> getAllEventDescV2() throws SQLException;
	
	/**Returns events description from date. Uses converted long number for date*/
	public HashMap<Integer,String> getEventDescriptionFromDateV2(long fromDate) throws SQLException;
	
	/**Returns all event discriminator in a hasmap*/
	public HashMap<Integer,String> getAllEventDiscV2() throws SQLException;
	
	/**Return events discriminator FROM DATE in HM (eventid - discriminator)*/
	public HashMap<Integer,String> getEventDiscriminatorFromDateV2(long fromDate) throws SQLException;
	
	/**Returns userid list, who has record in the UserDiscriminatorsRanks table*/
	public List<Integer> whoHasDiscRankInTable();
	
	/**Upload/Update user/users discriminator rank values*/
	public void uploadUserDiscriminatorRank(HashMap<Integer, HashMap<String, Double>> discriminatorRank);
	
	/**Returns for ONE or ALL USER their discriminator ranks. For one user onlyoneuser = 1.
	 * Does not return with the 0. user disc. ranks*/
	public HashMap<Integer, HashMap<String, Double>> getUserDiscriminatorRank(int UserId, boolean onlyOneUser);
	
	/**Returns all tag for all event in a hashmap. (eventid - tagList)*/
	public HashMap<Integer, List<String>> getAllEventsTagV2() throws SQLException;
	
	/**Returns all tag for legit events from long date converted into integer date*/
	public HashMap<Integer, List<String>> getEventsTagFromDateV2(long fromDate) throws SQLException;
	
	/**Insert a line into eventlog
	 * - user click
	 * - user event checks
	 * - user logins*/
	public void uploadEventLogV2(int userid, int eventid, long timestamp, int type) throws SQLException;
	
	/**Insert new log line into RecommendationLogs*/
	public void insertRecommendationLog(String comment, Integer UserID);
	
	
	
	/**Insert a line into RecommendationLog
	 * - gravity
	 * - second step
	 * - iALS*/
	public String insertRECLogV2(String comment, Integer UserId) throws SQLException;
	
	/**Insert newly calculated rank values for user event pairs
	 * - Rank values should be descending order.
	 * - Delete old rank values before upload new ones*/
	public void updateRecPV2(int UserId, LinkedHashMap<Integer, Double> EventRanks);
	
	/**Delete, Update, Insert events rank value into UserEventRecommendations table*/
	public void updateRecPV3(int UserId, LinkedHashMap<Integer, Double> newEventRanks);
	
	/**JUST - Delete, Insert events rank value into UserEventRecommendations table*/
	public void insertRecPV4(int UserId, LinkedHashMap<Integer, Double> newEventRanks);
	
	/**Insert into NewUserEventChache table the newly calculated first step data. Create shouldChache flag too.*/
	public void updateNewUserEventCaches(List<Rec> zeroUserRanks) throws SQLException;
	
	/**Returns FirstStep ZERO user eventid and rank values*/
	public HashMap<Integer, Double> getZeroUserRanks();
	
	/**Returns one users recommendation data. hashmap(eventid - double_rank_value)*/
	public HashMap<Integer, Double> getRecPUser (int UserId);
	
	/**Returns the number of users*/
	public int getAllUserNumberV2() throws SQLException;
	
	/**Returns all userid and facebookid in hasmap*/
	public HashMap<Integer, Long> getAllFaceAndUserIdV2() throws SQLException;
	
	/**Return all users ID in a list*/	// for Rec. accuracy getUserIdArrayFromTestUser
	public List<Integer> getUserIdArray() throws SQLException;
	
	/**Returns FacebookId for UserId - Returns 0 if sth. goes wrong*/
	public long getFbUserIDforUserIDV2(int userId) throws SQLException;
	
	/**Returns facebookid for one user. Uses userid*/
	public int getUserIdforFacebookUserId(Long facebookUserId) throws SQLException;
	
	/**Returns all event ranks for one user in hashmap. use userid*/
	public HashMap<Integer,Double> getEventIdArrayForXYUserV2(int UserId) throws SQLException;
	
	/**Returns true for userId if have in the UserEventRecommendations any rows (:= has rec)*/
	public boolean hasRecV2(int userId) throws SQLException;
	
	/**Returns users age; if the age is negative age is 0*/
	public Integer getUserAge(int UserId) throws SQLException;
	
	/**Returns all like for one user in a Like list. (userid - List<Like>)*/
	public List<Like> getAllLikeForUserId(int UserId) throws SQLException;
	
	/**Returns all likes for facebook user; convert fbuserid to userid, !use the other! function to get likes*/
	public List<Like> getAllLikeForFbUserIdV2(long facebookUserId) throws SQLException;
	
	/**Returns all user last visit on site from Users table*/
	public HashMap<Integer, Date> getAllUserLastVisit() throws SQLException;
	
	/**Returns all our facebook like id from LikeImdbTags tabl. USe MEtadataFromthirdparty class*/
	public HashMap<Long, MetadataFromThirdParty> getInfoFromImdbTags() throws SQLException;
	
	/**Returns all our facebook like id from LikeLastfmTags table use Metadatafromthirdparty class*/
	public HashMap<Long, MetadataFromThirdParty> getInfoFromLastfmTags() throws SQLException;
	
	/**Upload facebook music like tags into Lastfm table. Use Metadatafromthirdparty class*/
	public void uploadInfoToLastfmTags(HashMap<Long, MetadataFromThirdParty> allNewLikeTagHm);
	
	/**Upload facebook music like tags into IMDB table. Use MEtadatafromthirdparty class*/
	public void uploadInfoToImdbTags(HashMap<Long, MetadataFromThirdParty> allNewLikeTagHm);

}