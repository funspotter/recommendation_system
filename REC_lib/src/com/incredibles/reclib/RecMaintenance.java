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

	/*isIn beallitasa. cms ben isOk lett gyorsan be akarjuk vinni a recrank táblába.
	 * - jövõbeli események, melyek isOk-ok voltak - isIn 1.
	 * - multbeli események 0;
	 * - isOk=0 - isIn = 0
	 * 
	 * RecRank múltbeli események törtlése*/
	
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
	
	/**ForAllUser: Maintain Rec Table. Upload newly created and delete old events.
	* Update isIn flags in Events table.*/
	public void maintainRecTable(){
		HashMap<Integer, HashMap<String, Double>> allUserDiscriminatorRank = getAllUserDiscriminatorRank();
		List<Integer> notInFlagEvents = getLegitEventsWithNotinFlag();
		List<Integer> legitEvents = getLegitEventsId();
		HashMap<Integer, String> eventDiscriminator = getEventsDiscriminatorFromDate(); // tolowercase!!
		HashMap<Integer, Double> UserRanks = null;
		HashMap<String,Double> discriminatorRank = null;
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
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			System.out.println("UserID kész: "+UserId +"HashMapSize:"+rankValues.size()+" time: "+dateFormat.format(date));
		}
		setisinFlagEvents(legitEvents);	// set isIn flag for legit events
	}
	
}
