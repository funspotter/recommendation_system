package com.incredibles.reclib;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;

import com.incredibles.data.LogTableInfoClass;
import com.incredibles.data.LogTableTypes;
import com.incredibles.storage.ClientDbService;
import com.incredibles.storage.ClientDbServiceCreator;
import com.incredibles.storage.LogLine;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;

public class EventReplacerV1{
	
	
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
	
	// Return events with legit show from date
	public List<Integer> getEventsFromDate(long date) {
		RecommenderDbService dbService = null;
		List<Integer> events = new ArrayList<Integer>();
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			events = dbService.eventsInShowList(1);
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
	public Long getCurrentDate() {
		long timestamp = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		Long date = (long) (year * 10000 + month * 100 + day);
		return date;
	}
	
	/** Returns the date which is timeSession before from Now*/
	public Date getLastXHourTimestamp() {
		long timestamp = System.currentTimeMillis() - timeSession;
		Date currentTimeDate = new Date(timestamp);
		return currentTimeDate;
	}
	
	/** Get clicked info from log in LOGTABLEINFOCLASS class
	 * NOT USED*/
	public List<LogTableInfoClass> getInformationFromLog(int type, int userId){
		List<LogTableInfoClass> list = null;
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			list = dbService.getInfoFromLog(type, userId);
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
		// first hashmap: eventid - timestamp
		// second hashmap: eventid - number
		return list;
	}
	
	/** Read from Test-Log every line, and store specified log types in specific objects*/
	public void getAllInfoFromLog(HashMap<Integer, HashMap<Integer, OldNewestDate>> displayedEventsAndTimestamp2, HashMap<Integer, HashMap<Integer, Integer>> clickedEventsAndTimestamp2, HashMap<Integer,Date> userOnTheSite2){
		RecommenderDbService dbService = null;
		LogLine oneLine = new LogLine();
		OldNewestDate dates = null;	// parameter for displayedEventsAndTimestamp2
		HashMap<Integer, OldNewestDate> segHm = new HashMap<Integer, OldNewestDate>(); // parameter for displayedEventsAndTimestamp2
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			while(dbService.getNextLogLine(oneLine)){
				if(oneLine.type==LogTableTypes.eventClick){		/*kattint�s: clickedEventsAndTimestamp2*/
					System.out.println("eventclick");
					if(!clickedEventsAndTimestamp2.isEmpty()){	/*not empty hashmap*/
						if(clickedEventsAndTimestamp2.containsKey(oneLine.userId)){	/*user already in the hashmap*/
							if(clickedEventsAndTimestamp2.get(oneLine.userId).containsKey(oneLine.eventId)){	/*user - event already in the hashmap*/
								int clickNum = clickedEventsAndTimestamp2.get(oneLine.userId).get(oneLine.eventId);
								clickedEventsAndTimestamp2.get(oneLine.userId).put(oneLine.eventId, (clickNum+1));
							}else{
								clickedEventsAndTimestamp2.get(oneLine.userId).put(oneLine.eventId, 1);
							}
						}else{
							clickedEventsAndTimestamp2.put(oneLine.userId, new HashMap<Integer, Integer>(oneLine.eventId,1));
						}
					}else{
						clickedEventsAndTimestamp2.put(oneLine.userId, new HashMap<Integer,Integer>(oneLine.eventId, 1));
					}
				}else if(oneLine.type==LogTableTypes.eventShowned){		/*kirakott event: displayedEventsAndTimestamp2*/
					System.out.println("eventshowned");
					if(!displayedEventsAndTimestamp2.isEmpty()){		/*not empty hashmap*/
						if(displayedEventsAndTimestamp2.containsKey(oneLine.userId)){	/*user already in the hashmap*/
							if(displayedEventsAndTimestamp2.get(oneLine.userId).containsKey(oneLine.eventId)){	/*user - event already in the hashmap*/
								dates = displayedEventsAndTimestamp2.get(oneLine.userId).get(oneLine.eventId);	/*oneLine dates newer;older??*/
								if(dates.oldest.compareTo(oneLine.timeStamp)>=0){	// older timestamp
									dates.oldest = oneLine.timeStamp;
								}
								if(dates.newest.compareTo(oneLine.timeStamp)<0){	// newer timestamp
									dates.newest = oneLine.timeStamp;
								}
								segHm.put(oneLine.eventId, dates);
								displayedEventsAndTimestamp2.put(oneLine.userId, segHm);
							}else{
								dates= new OldNewestDate(oneLine.timeStamp, oneLine.timeStamp);
								segHm.put(oneLine.eventId, dates);
								displayedEventsAndTimestamp2.put(oneLine.userId, segHm);
							}
						}else{
							dates= new OldNewestDate(oneLine.timeStamp, oneLine.timeStamp);
							segHm.put(oneLine.eventId, dates);
							displayedEventsAndTimestamp2.put(oneLine.userId, segHm);
						}
					}else{
						dates= new OldNewestDate(oneLine.timeStamp, oneLine.timeStamp);
						segHm.put(oneLine.eventId, dates);
						displayedEventsAndTimestamp2.put(oneLine.userId, segHm);
					}
				}else if(oneLine.type==LogTableTypes.userVisit){		/*user l�togat�s: userOnTheSite2*/
					System.out.println("uservisit");
					if(!userOnTheSite2.isEmpty()){
						if(userOnTheSite2.containsKey(oneLine.userId)){
							if(userOnTheSite2.get(oneLine.userId).compareTo(oneLine.timeStamp)<0){ /*search for the newest login time*/
								userOnTheSite2.put(oneLine.userId, oneLine.timeStamp);
							}
						}else{
							userOnTheSite2.put(oneLine.userId, oneLine.timeStamp);
						}
					}else{
						userOnTheSite2.put(oneLine.userId, oneLine.timeStamp);
					}
				}
			}
		} catch (SQLException | IOException e) {
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
		ClientDbService dbService = null;
		try {
			dbService = ClientDbServiceCreator.createCloud();
			userId = dbService.getAllFaceAndUserId();
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

	/** Returns event rank value for user*/
	public HashMap<Integer,Double> getRankforEvent(Integer userID){
		HashMap<Integer,Double> eventIDRank = null;
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			eventIDRank = dbService.getRankforEvent(userID);
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
			dbService.updateRecP(userId,newRankHm);
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
	
	/** Delete old event log information from log_table, use LogTableInfoClass*/
	public void deleteInformationFromLog(LogTableInfoClass deleteThis){
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			dbService.deleteInfoFromLog(deleteThis);
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
	
	/** Upload gravity log data.. info in logtabletypes class*/
	public void uploadLogInfoforGravity(Integer userID, String info){
		ClientDbService dbService = null;
		try {
			dbService = ClientDbServiceCreator.createCloud();
			dbService.insertRECLog(info, userID);
			//dbService.uploadLog(fbID, 0, Calendar.getInstance().getTimeInMillis(), logtype);
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
		
		getAllInfoFromLog(displayedEventsAndTimestamp2,clickedEventsAndTimestamp2,userOnTheSite2);	/*set log information*/		
		
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
				uploadLogInfoforGravity(userId, info); 	/*To help monitoring the happenings*/		
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
						LinkedHashMap<Integer,Double> newRankHm = sortByValueAndMakeThousandElement(rankforEvent);	/*sort Hashmap*/
						uploadRankValues(userId,newRankHm);															/*Upload new sorted linked hashmap*/
					}
				}
			}else if(timeTrigger == false){	/*user was not on the site*/
				System.out.println("nem jott fel_"+userId);
				String info = "notGravityUserOffline";
				uploadLogInfoforGravity(userId, info); 	/*To help monitoring the happenings*/	
//				Iterator<Map.Entry<Integer,Double>> iter = rankforEvent.entrySet().iterator();
//				while (iter.hasNext()) {
//				    Map.Entry<Integer,Double> entry2 = iter.next();
//				    if(!existingEvents.contains(entry2.getKey())){
//				        iter.remove();
//				    }
//				}
				//uploadLogInfoforGravity(fbID, LogTableTypes.GravityNotOnSite);
			}		
		}			
	}
	
	/** Nomen est omen. Needed because of the new database structure*/
	private LinkedHashMap<Integer, Double> sortByValueAndMakeThousandElement(Map<Integer, Double> map) {
        List<Map.Entry<Integer, Double>> list = new LinkedList<Map.Entry<Integer, Double>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
            public int compare(Map.Entry<Integer, Double> m1, Map.Entry<Integer, Double> m2) {
                return (m2.getValue()).compareTo(m1.getValue());
            }
        });
        LinkedHashMap<Integer, Double> result = new LinkedHashMap<Integer, Double>();
        int count = 0;
        for (Entry<Integer, Double> entry : list) {
            if(count<1000){
            	result.put(entry.getKey(), entry.getValue());
                count ++;
            }
            else{
            	break;
            }
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