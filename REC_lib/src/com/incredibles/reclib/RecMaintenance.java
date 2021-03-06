package com.incredibles.reclib;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;

public class RecMaintenance {

	/*isIn beallitasa. cms ben isOk lett gyorsan be akarjuk vinni a recrank t�bl�ba.
	 * - j�v�beli esem�nyek, melyek isOk-ok voltak - isIn 1.
	 * - multbeli esem�nyek 0;
	 * - isOk=0 - isIn = 0
	 * 
	 * RecRank m�ltbeli esem�nyek t�rtl�se*/
	
	/**Returns all events id*/
	public HashMap<Integer, Boolean> getAllEventsIsinHm(){
		HashMap<Integer, Boolean> eventIdIsinHm = null;
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			eventIdIsinHm = dbService.getAllEventsWithIsinFlag();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (dbService != null) {
				try {
					dbService.close();
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		return eventIdIsinHm;
	}

	/**Returns legit events id*/
	public List<Integer> getLegitEventsId(){
		List<Integer> eventIdArray = null;
		RecommenderDbService dbService = null;
		Date nowDate = new Date();
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			eventIdArray = dbService.getLegitEventsIdFromDate(nowDate.getTime());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (dbService != null) {
				try {
					dbService.close();
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		return eventIdArray;
	}
	
	/**Return existing events discriminator*/
	public HashMap<Integer,String> getEventsDiscriminatorFromDate(){
		HashMap<Integer,String> eventDiscriminator = new HashMap<Integer,String>();
		RecommenderDbService dbService = null;
		Date nowDate = new Date();
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			eventDiscriminator = dbService.getEventDiscriminatorFromDateV2(nowDate.getTime());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (dbService != null) {
				try {
					dbService.close();
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		return eventDiscriminator;
	}
	
	/**Returns legit isIn=0 events. Newly uploaded events, updated events in cms become legit... etc.*/
	public List<Integer> getLegitEventsWithNotinFlag(){
		List<Integer> eventsWithNoinFlag = null;
		RecommenderDbService dbService = null;
		Date nowDate = new Date();
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			eventsWithNoinFlag = dbService.getLegitNotinEventsIdFromDate(nowDate.getTime());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (dbService != null) {
				try {
					dbService.close();
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		return eventsWithNoinFlag;
	}
	
	/**Returns discriminator ranks for all user*/
	public HashMap<Integer, HashMap<String, Double>> getAllUserDiscriminatorRank(){
		RecommenderDbService dbService = null;
		HashMap<Integer, HashMap<String, Double>> userDiscRanks = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			userDiscRanks = dbService.getUserDiscriminatorRank(0, false);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (dbService != null) {
				try {
					dbService.close();
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		return userDiscRanks;
	}
	
	/**Returns discriminator ranks for one user*/
	public HashMap<Integer, HashMap<String, Double>> getOneUserDiscriminatorRank(int UserID){
		RecommenderDbService dbService = null;
		HashMap<Integer, HashMap<String, Double>> userDiscRanks = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			userDiscRanks = dbService.getUserDiscriminatorRank(UserID, true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (dbService != null) {
				try {
					dbService.close();
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		return userDiscRanks;
	}
	
	/**Returns ranks for user from Rec table*/
	public HashMap<Integer, Double> getUserRanks(int UserId){
		HashMap<Integer, Double> UserRanks = null;
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			UserRanks = dbService.getRecPUser(UserId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (dbService != null) {
				try {
					dbService.close();
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		return UserRanks;
	}
	
	/**Upload rec table info in linked hashmap for one user
	 * Uses the insert and delete version.*/
	public void uploadUserRec(LinkedHashMap<Integer, Double> userRanks, int UserId){
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			dbService.insertRecPV4(UserId, userRanks);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (dbService != null) {
				try {
					dbService.close();
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**Maintain events isIn flag. Run after RecTable update.*/
	public void setisinFlagEvents(List<Integer> legitEvents){
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			dbService.updateIsinFlagForEvents(legitEvents);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (dbService != null) {
				try {
					dbService.close();
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**Returns all users ID from Users table*/
	public List<Integer> getAllUserId(){
		RecommenderDbService dbService = null;
		List<Integer> userIdArray = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			userIdArray = dbService.getUserIdArray();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(dbService != null){
				try {
					dbService.close();
				} catch (IOException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return userIdArray;
	}
	
	/**At least one user hasnt got discriminator vector, should reload the discriminator hashmap*/
	public boolean shouldMaintainDiscriminatorVectors(HashMap<Integer, HashMap<String, Double>> allUserDiscriminatorRank){
		boolean flag = false;
		List<Integer> userIdArray = getAllUserId();
		for(int i=0; i< userIdArray.size(); i++){
			Integer UserId = userIdArray.get(i);
			if(!allUserDiscriminatorRank.containsKey(UserId)){
				UploadFiltersResultV2.filterExecute(UserId);
				flag= true;
			}
		}
		return flag;
	}
	
	/**Nomen est omen. Needed because of the new database structure*/
	public static LinkedHashMap<Integer, Double> sortByValue(Map<Integer, Double> map) {
        List<Map.Entry<Integer, Double>> list = new LinkedList<Map.Entry<Integer, Double>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
            public int compare(Map.Entry<Integer, Double> m1, Map.Entry<Integer, Double> m2) {
                return (m2.getValue()).compareTo(m1.getValue());
            }
        });
        LinkedHashMap<Integer, Double> result = new LinkedHashMap<Integer, Double>();
        for (Entry<Integer, Double> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
	
	/**Upload log for one user*/
	public static void uploadComment(String Comment, Integer UserId){
		RecommenderDbService dbService2=null;
		try {
			dbService2 = RecommenderDbServiceCreator.createCloud();
			if(UserId==null){
				dbService2.insertRecommendationLog(Comment, 0);
			}else{
				dbService2.insertRecommendationLog(Comment, UserId);
			}
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
	
	/**ForAllUser: Maintain Rec Table. Upload newly created and delete old events.
	* Update isIn flags in Events table.*/
	public void maintainRecTable(){
		HashMap<Integer, HashMap<String, Double>> allUserDiscriminatorRank = getAllUserDiscriminatorRank();
		boolean shouldReload = shouldMaintainDiscriminatorVectors(allUserDiscriminatorRank);
		if(shouldReload == true){
			allUserDiscriminatorRank = getAllUserDiscriminatorRank();
		}
		List<Integer> notInFlagEvents = getLegitEventsWithNotinFlag();
		List<Integer> legitEvents = getLegitEventsId();
		HashMap<Integer, String> eventDiscriminator = getEventsDiscriminatorFromDate(); // tolowercase!!
		HashMap<Integer, Double> UserRanks = null;
		HashMap<String,Double> discriminatorRank = null;
		uploadComment("UserNum: "+allUserDiscriminatorRank.size()+" EventToProcess: "+notInFlagEvents.size(),null);
		int period = 1;
		for(Entry<Integer, HashMap<String,Double>>entry: allUserDiscriminatorRank.entrySet()){	// run for every user in the system
			Integer UserId = entry.getKey();
			UserRanks = getUserRanks(UserId);
			if(!notInFlagEvents.isEmpty()){
				discriminatorRank = entry.getValue();
				for(int i=0; i<notInFlagEvents.size(); i++){	// legit isIn =0 events	HANDLE NEWLY UPLOADED EVENTS INTO RECTABLE
					Integer EventId = notInFlagEvents.get(i);
					if(!UserRanks.containsKey(EventId)){	// handle case if event is / isnt in rec table. 
						String discriminator = eventDiscriminator.get(EventId).toLowerCase();
						if(!discriminator.equals("simple")){
							Double newEventRank = discriminatorRank.get(discriminator)+Math.random()/100;
							UserRanks.put(EventId, newEventRank);
						}
					}
				}
			}	
			Iterator<Map.Entry<Integer,Double>> iter = UserRanks.entrySet().iterator(); // delete old events
			while (iter.hasNext()) {
			    Map.Entry<Integer,Double> entry1 = iter.next();
			    Integer EventId = entry1.getKey();
			    if(!legitEvents.contains(EventId)){
			        iter.remove();
			    }
			}
			LinkedHashMap<Integer, Double> rankValues = sortByValue(UserRanks);
			uploadUserRec(rankValues, UserId);	// upload new rank values for user
			if(period%40==0){
				uploadComment("40MoreUserDone",null);
			}
			period++;
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			System.out.println("UserID k�sz: "+UserId +"HashMapSize:"+rankValues.size()+" time: "+dateFormat.format(date));
		}
		TopTenEventFilter top = new TopTenEventFilter();
		top.setTopEventRanksForAllUser(); // set top facebook events rank value
		setisinFlagEvents(legitEvents);	// set isIn flag for legit events
	}

	/**Maintain for only ONE USER + SET TOP FacebookEvents + set disc vector if missing*/
	public void maintainRecTableForOneUser(int UserId2){
		HashMap<Integer, HashMap<String, Double>> allUserDiscriminatorRank = getOneUserDiscriminatorRank(UserId2);
		HashMap<Integer, Double> UserRanks = getUserRanks(UserId2);
		if(allUserDiscriminatorRank.isEmpty()){
			UploadFiltersResultV2.filterExecute(UserId2);
			allUserDiscriminatorRank = getOneUserDiscriminatorRank(UserId2);
		}
		List<Integer> notInFlagEvents = getLegitEventsWithNotinFlag();
		List<Integer> legitEvents = getLegitEventsId();
		HashMap<Integer, String> eventDiscriminator = getEventsDiscriminatorFromDate(); // tolowercase!!
		HashMap<String,Double> discriminatorRank = null;
		for(Entry<Integer, HashMap<String,Double>>entry: allUserDiscriminatorRank.entrySet()){	// run for every user in the system
			Integer UserId = entry.getKey();
			if(!notInFlagEvents.isEmpty()){
				discriminatorRank = entry.getValue();
				for(int i=0; i<notInFlagEvents.size(); i++){	// legit isIn =0 events	HANDLE NEWLY UPLOADED EVENTS INTO RECTABLE
					Integer EventId = notInFlagEvents.get(i);
					if(!UserRanks.containsKey(EventId)){	// handle case if event is / isnt in rec table. 
						String discriminator = eventDiscriminator.get(EventId).toLowerCase();
						if(!discriminator.equals("simple")){
							Double newEventRank = discriminatorRank.get(discriminator)+Math.random()/100;
							UserRanks.put(EventId, newEventRank);
						}
					}
				}
			}	
			Iterator<Map.Entry<Integer,Double>> iter = UserRanks.entrySet().iterator(); // delete old events
			while (iter.hasNext()) {
			    Map.Entry<Integer,Double> entry1 = iter.next();
			    Integer EventId = entry1.getKey();
			    if(!legitEvents.contains(EventId)){
			        iter.remove();
			    }
			}
			LinkedHashMap<Integer, Double> rankValues = sortByValue(UserRanks);
			uploadUserRec(rankValues, UserId);	// upload new rank values for user
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			System.out.println("UserID k�sz: "+UserId +"HashMapSize:"+rankValues.size()+" time: "+dateFormat.format(date));
			TopTenEventFilter one = new TopTenEventFilter();
			one.setTopTenEventRanks(rankValues);
		}
	}
}
