package com.incredibles.reclib;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.incredibles.data.LogTableInfoClass;
import com.incredibles.data.LogTableTypes;
import com.incredibles.storage.LogLine;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;

import de.umass.lastfm.User;

public class EventReplacerV2 {
	
	/** Inside class helps to store two (oldest and newest) dates*/
	class OldNewestDate{
		Date oldest;
		Date newest;
		
		OldNewestDate(Date dateOne, Date dateTwo) {
			this.oldest=dateTwo;
			this.newest=dateOne;
		}

		OldNewestDate() {
			this(new Date(), new Date());
		}
		
	}

	/** Running 4 hours= session in MILIsec.*/
	final long timeSession = 14400000L;
	
	/** Return events with legit show from date*/
	public List<Integer> getEventsFromDate(long date) {
		RecommenderDbService dbService = null;
		List<Integer> events = new ArrayList<Integer>();
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			events = dbService.getLegitEventsIdFromDate(date);
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
		return events;
	}
	
	/** Returns the current date*/
	public Integer getCurrentDate() {
		long timestamp = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int date = (year * 10000 + month * 100 + day);
		return date;
	}
	
	/** Returns the date which is timeSession before from Now*/
	public Date getLastXHourTimestamp() {
		long timestamp = System.currentTimeMillis() - timeSession;
		Date currentTimeDate = new Date(timestamp);
		return currentTimeDate;
	}
		
	/** Read from Test-Log every line, and store specified log types in specific objects*/
	public void getAllInfoFromLog(HashMap<Integer, HashMap<Integer, OldNewestDate>> displayedEventsAndTimestamp2, HashMap<Integer, HashMap<Integer, Integer>> clickedEventsAndTimestamp2){
		RecommenderDbService dbService = null;
		LogLine oneLine = new LogLine();
		OldNewestDate dates = null;	// parameter for displayedEventsAndTimestamp2
		HashMap<Integer, OldNewestDate> segHm = new HashMap<Integer, OldNewestDate>(); // parameter for displayedEventsAndTimestamp2
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			while(dbService.getNextLogLineV2(oneLine)){
				Integer EventId = oneLine.eventId;
				Integer type = oneLine.type;
				Integer UserId = oneLine.userId;
				Date time = oneLine.timeStamp;
				if(type==LogTableTypes.eventClick){		/*kattintás: clickedEventsAndTimestamp2*/
					System.out.println("eventclick");
					if(!clickedEventsAndTimestamp2.isEmpty()){	/*not empty hashmap*/
						if(clickedEventsAndTimestamp2.containsKey(UserId)){	/*user already in the hashmap*/
							HashMap<Integer,Integer> eventClicks = clickedEventsAndTimestamp2.get(UserId);
							if(eventClicks.containsKey(EventId)){	/*user - event already in the hashmap*/
								int clickNum = eventClicks.get(EventId);
								eventClicks.put(EventId, (clickNum+1));
							}else{
								eventClicks.put(EventId, 1);
							}
						}else{
							clickedEventsAndTimestamp2.put(UserId, new HashMap<Integer, Integer>(EventId,1));
						}
					}else{
						clickedEventsAndTimestamp2.put(UserId, new HashMap<Integer,Integer>(EventId, 1));
					}
				}else if(type==LogTableTypes.eventShowned){		/*kirakott event: displayedEventsAndTimestamp2*/
					System.out.println("eventshowned");
					if(!displayedEventsAndTimestamp2.isEmpty()){		/*not empty hashmap*/
						if(displayedEventsAndTimestamp2.containsKey(UserId)){	/*user already in the hashmap*/
							HashMap<Integer, OldNewestDate> displayedEvent = displayedEventsAndTimestamp2.get(UserId);
							if(displayedEvent.containsKey(EventId)){	/*user - event already in the hashmap*/
								dates = displayedEvent.get(EventId);	/*oneLine dates newer;older??*/
								if(dates.oldest.compareTo(time)>=0){	// older timestamp
									dates.oldest = time;
								}
								if(dates.newest.compareTo(time)<0){	// newer timestamp
									dates.newest = time;
								}
								segHm.put(EventId, dates);
								displayedEventsAndTimestamp2.put(UserId, segHm);
							}else{
								dates= new OldNewestDate(time, time);
								segHm.put(EventId, dates);
								displayedEventsAndTimestamp2.put(UserId, segHm);
							}
						}else{
							dates= new OldNewestDate(time, time);
							segHm.put(EventId, dates);
							displayedEventsAndTimestamp2.put(UserId, segHm);
						}
					}else{
						dates= new OldNewestDate(time, time);
						segHm.put(EventId, dates);
						displayedEventsAndTimestamp2.put(UserId, segHm);
					}
				}else if(oneLine.type==LogTableTypes.userVisit){		/*user látogatás: userOnTheSite2*/
					System.out.println("uservisit");
				}
			}
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
	
	/** Return userID and FBUserID for all users*/
	public HashMap<Integer,Long> getAllUserAndFacebookId(){
		HashMap<Integer,Long> userId = null;
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			userId = dbService.getAllFaceAndUserIdV2();
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
		return userId;
		
	}

	/**Returns all user last visit time on site*/
	public HashMap<Integer,Date> getAllUserLastVisit(){
		RecommenderDbService dbService = null;
		HashMap<Integer,Date> allUserLastVisit = new HashMap<Integer,Date>();
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			allUserLastVisit = dbService.getAllUserLastVisit();
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
		return allUserLastVisit;
	}
	
	/** Returns event rank value for user*/
	public HashMap<Integer,Double> getRankforEvent(Integer userID){
		HashMap<Integer,Double> eventIDRank = null;
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			eventIDRank = dbService.getRankforEventV2(userID);
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
		return eventIDRank;	
	}
	
	/** Upload changed rank values caused by gravity*/
	public void uploadRankValues(Integer userId, LinkedHashMap<Integer,Double> newRankHm){
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			dbService.updateRecPV3(userId,newRankHm);
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
		
	/** Upload gravity log data*/
	public void uploadLogInfoforGravity(Integer userID, String info){
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			dbService.insertRecommendationLog(info, userID);
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
	
	/** Function to replace events for all user*/
	public void executeEventReplace(){
		HashMap <Integer,Long> userAndFacebookId = getAllUserAndFacebookId();	// get all users information
		HashMap<Integer, HashMap<Integer,OldNewestDate>> displayedEventsAndTimestamp2 = new HashMap<Integer, HashMap<Integer, OldNewestDate>>();
		HashMap<Integer, HashMap<Integer, Integer>> clickedEventsAndTimestamp2 = new HashMap<Integer, HashMap<Integer, Integer>>();
		HashMap<Integer, Date> userOnTheSite2 = new HashMap<Integer,Date>();
		
		getAllInfoFromLog(displayedEventsAndTimestamp2,clickedEventsAndTimestamp2);	/*set log information*/		
		userOnTheSite2 = getAllUserLastVisit();	/*last visit for all user on site*/
		
		List<Integer> existingEvents = getEventsFromDate(getCurrentDate()); /*Get still existing events*/
		
		for(Entry<Integer,Long>entry: userAndFacebookId.entrySet()){		// iterate through users
			Integer userId = entry.getKey();
			Long fbID = entry.getValue();
			boolean timeTrigger = false;				// shows the user present in the last time session			
			Date userLastVisitTime = null;
			if(userOnTheSite2.containsKey(userId)){
				Date nowTimeMinusSessionDate = getLastXHourTimestamp();
				userLastVisitTime = userOnTheSite2.get(userId);						// last visit on the site
					if(nowTimeMinusSessionDate.compareTo(userLastVisitTime)<=0){		// if it's true, user was online, get events and other data
						timeTrigger = true;				
						System.out.println("user was online in the last 4 hours");
					}
			}		
			if(timeTrigger == true){			/*IF the user was on the site in the last timeSession, the event replacer starts*/
				String info = "gravityDone";
				//uploadLogInfoforGravity(userId, info); 	/*To help monitoring the happenings*/		
				HashMap<Integer,Double> rankforEvent = getRankforEvent(userId);		/*Get rank values from RECP*/		
				HashMap<Integer, Integer> eventClick = clickedEventsAndTimestamp2.get(userId);	/*User's clicknumber on events*/
				HashMap<Integer,OldNewestDate> shownedEvents = displayedEventsAndTimestamp2.get(userId);
				if(eventClick!= null && shownedEvents != null){			//clicked on event; and seen event
					if(!eventClick.isEmpty() && !shownedEvents.isEmpty()){
						for(Entry <Integer, OldNewestDate> entryUser: shownedEvents.entrySet()){	// iterate on showned events
							Integer eventId = entryUser.getKey();
							OldNewestDate twoDate = entryUser.getValue();
							if(twoDate.newest.compareTo(userLastVisitTime)>=0 && rankforEvent.containsKey(eventId)){		/*if the event showned in the last user visit*/
								if(existingEvents.contains(eventId)){									// event did not happened
									/*COLLECT DATA FOR MATH START*/
									long firstShowned = twoDate.oldest.getTime();
									int sumClickNum = 0;
									if(eventClick.containsKey(eventId)){
										eventClick.get(eventId);							/*sum click for event for user*/
									}
									long shownedTimeInterval = System.currentTimeMillis() - firstShowned;	/*time counted from first appearance*/
									Double rank = rankforEvent.get(eventId);
									/*COLLECT DATA FOR MATH END*/
									Double newRank = doTheMath(rank,sumClickNum,shownedTimeInterval);
									rankforEvent.put(eventId, newRank); /*use MAP properties*/
									System.out.println("math done for"+ userId + "for event: "+eventId);
								}
							}
							else{
								System.out.println("eventet nem latta utolso viewnal vagy nincs recp ben az event"+eventId);
							}
						}
						LinkedHashMap<Integer,Double> newRankHm = sortByValue(rankforEvent);	/*sort Hashmap*/
						uploadRankValues(userId,newRankHm);															/*Upload new sorted linked hashmap*/
					}
				}
			}else if(timeTrigger == false){	/*user was not on the site*/
				System.out.println("nem jott fel_"+userId);
				String info = "notGravityUserOffline";
				//uploadLogInfoforGravity(userId, info); 	/*To help monitoring the happenings*/
			}		
		}			
	}
	
	/** Nomen est omen. Needed because of the new database structure*/
	private LinkedHashMap<Integer, Double> sortByValue(Map<Integer, Double> map) {
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
	
	/** Calculate new rank values from dT; oldRank; clickNumber;*/
	private Double doTheMath(Double rank, Integer sumClick, Long shownedTimeInterval){
		double timePart = 0.0;
		double clikslog = 0.0;
		long oneHour = 3600000L;
		long oneDay = 24 * oneHour;
		
		if(sumClick.equals(0)){
			clikslog = 0.9;
		}else if(sumClick.equals(1)){
			clikslog = 1.5;
		}else{
			clikslog = Math.log(sumClick)/Math.log(2);	/*log2(sumclick)*/
		}
		
		if(shownedTimeInterval < timeSession){
			timePart = 1.0;
		}else if(timeSession <= shownedTimeInterval && shownedTimeInterval < oneDay){
			timePart = 1-((0.1/(oneDay-timeSession))*(shownedTimeInterval - timeSession));
		}else if(shownedTimeInterval >= oneDay && shownedTimeInterval < (4*oneDay)){
			timePart = 0.9-((0.7/(3*oneDay))*(shownedTimeInterval - oneDay));
		}else if(shownedTimeInterval >= (4*oneDay) && shownedTimeInterval <= (7*oneDay)){
			timePart = 0.1;
		}else if(shownedTimeInterval > (7*oneDay)){			/*periodic part*/
			long maradek = shownedTimeInterval%(7*oneDay);
			if(maradek < (3*oneDay)){
				timePart = 0.9 - ((0.7/(3*oneDay))*maradek);
			}else if(maradek >= (3*oneDay)){
				timePart = 0.1;
			}
		}
		
		double newRank = clikslog * timePart * rank;
		if(newRank > 1){
			newRank = 0.9 + Math.random()/100;	// too big rank value
		}else if(newRank<0){
			newRank = -10;						// problem with rank
		}
		return newRank;
	}

}
