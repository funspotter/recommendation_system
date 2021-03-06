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

import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;

/**Find top 10 facebook events for today; 10-tomorrow; 10- next week;*/
public class TopTenEventFilter {

	/*lek�rni legit f�cse esem�nyeket �s a checkin+ like sz�mukat*/
	
	public final double todayTrendingNum = 5.0;
	
	public final double tomorrowTrendingNum = 4.0;
	
	public final double nextweekTrendingNum = 3.0;
	
	/**Returns events for one day and one day only*/
	public List<Integer> getOneDayEvents(Date date){
		List<Integer> eventList = null;
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			eventList = dbService.getLegitEventsOnDate(date);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(dbService!=null){
				try {
					dbService.close();
				} catch (IOException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return eventList;
	}

	/**Retuns events in a time intervall*/
	public List<Integer> getEventsInTimeIntervall(Date from, Date to){
		List<Integer> eventList = null;
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			eventList = dbService.getLegitEventsIdV2(from, to);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(dbService!=null){
				try {
					dbService.close();
				} catch (IOException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return eventList;
	}

	/**Returns all facebook events page information checkin and like count*/
	public HashMap<Integer, Long> getFacebookEventsPageInfo(){
		HashMap<Integer, Long> eventPageInfo = null;
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			eventPageInfo = dbService.getFacebookEventsPageInformation();
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
		return eventPageInfo;
	}
	
	/**Returns legit Facebook events*/
	public HashMap<Integer, Long> getLegitFacebookEvents(){
		RecommenderDbService dbService = null;
		HashMap<Integer, Long> facebookEvents = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			facebookEvents = dbService.getCategorizedFutureFacebookEvents();
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
		return facebookEvents;
	}
	
	/**Update Recommendation table for all user with topEventsRank hashmap*/
	public void updateREC(HashMap<Integer, Double> topTenEventRank){
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			dbService.updateRecPV5(topTenEventRank);
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
	}
	
	/**Nomen est omen. Needed because of the new database structure*/
	public LinkedHashMap<Integer, Long> sortbyValue(Map<Integer, Long> map) {
        List<Map.Entry<Integer, Long>> list = new LinkedList<Map.Entry<Integer, Long>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Long>>() {
            public int compare(Map.Entry<Integer, Long> m1, Map.Entry<Integer, Long> m2) {
                return (m2.getValue()).compareTo(m1.getValue());
            }
        });
        LinkedHashMap<Integer, Long> result = new LinkedHashMap<Integer, Long>();
        for (Entry<Integer, Long> entry : list) {
        	result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
	
	/**Sets the top 30 events for ONE USER to be the trending ones. use only facebook events.
	 * Today : 5; tomorrow: 4; nextweek: 3 base double rank.*/
	public HashMap<Integer, Double> setTopTenEventRanks(HashMap<Integer, Double> eventRank){
		Date nowDate = new Date();
		Date tomorrowDate = new Date();
		Date nextSevenDays = new Date();
		Calendar c = Calendar.getInstance(); 
		c.setTime(nowDate); 
		c.add(Calendar.DATE, 1);
		tomorrowDate = c.getTime();
		Calendar c2 = Calendar.getInstance(); 
		c2.setTime(tomorrowDate); 
		c2.add(Calendar.DATE, 7);
		nextSevenDays = c2.getTime();
		List<Integer> todayEvents = getOneDayEvents(nowDate);
		List<Integer> tomorrowEvents = getOneDayEvents(tomorrowDate);
		List<Integer> nextWeekEvents = getEventsInTimeIntervall(tomorrowDate, nextSevenDays);
		HashMap<Integer, Long> facebookEventsInfo = getFacebookEventsPageInfo();
		HashMap<Integer, Long> legitFacebookEvents = getLegitFacebookEvents();
		HashMap<Integer, Long> tempData = new HashMap<Integer, Long>();
		for(int i=0; i<todayEvents.size(); i++){
			Integer eventId = todayEvents.get(i);
			if(legitFacebookEvents.containsKey(eventId) && facebookEventsInfo.containsKey(eventId)){
				Long Number = facebookEventsInfo.get(eventId);
				tempData.put(eventId, Number);
			}
		}
		LinkedHashMap<Integer, Long> sortedTemp = sortbyValue(tempData);
		int counter=1;
		for(Entry<Integer, Long>entry: sortedTemp.entrySet()){
			if(counter<=10){
				Integer eventId = entry.getKey();
				Double newRank = todayTrendingNum + Math.random()/100;
				eventRank.put(eventId, newRank);
				counter++;
			}else{
				break;
			}
		}
		tempData.clear();
		sortedTemp.clear();
		for(int i=0; i<tomorrowEvents.size(); i++){
			Integer eventId = tomorrowEvents.get(i);
			if(legitFacebookEvents.containsKey(eventId) && facebookEventsInfo.containsKey(eventId) && !todayEvents.contains(eventId)){
				Long Number = facebookEventsInfo.get(eventId);
				tempData.put(eventId, Number);
			}
		}
		sortedTemp = sortbyValue(tempData);
		counter=1;
		for(Entry<Integer, Long>entry: sortedTemp.entrySet()){
			if(counter<=10){
				Integer eventId = entry.getKey();
				Double newRank = tomorrowTrendingNum + Math.random()/100;
				eventRank.put(eventId, newRank);
				counter++;
			}else{
				break;
			}
		}
		tempData.clear();
		sortedTemp.clear();
		for(int i=0; i<nextWeekEvents.size(); i++){
			Integer eventId = nextWeekEvents.get(i);
			if(legitFacebookEvents.containsKey(eventId) && facebookEventsInfo.containsKey(eventId) && !todayEvents.contains(eventId) && !tomorrowEvents.contains(eventId)){
				Long Number = facebookEventsInfo.get(eventId);
				tempData.put(eventId, Number);
			}
		}
		sortedTemp = sortbyValue(tempData);
		counter=1;
		for(Entry<Integer, Long>entry: sortedTemp.entrySet()){
			if(counter<=10){
				Integer eventId = entry.getKey();
				Double newRank = nextweekTrendingNum + Math.random()/100;
				eventRank.put(eventId, newRank);
				counter++;
			}else{
				break;
			}
		}
		return eventRank;
	}

	/**Based on setTopEventRanks, but changed to update all users ranklist for maintain function*/
	public void setTopEventRanksForAllUser(){
		Date nowDate = new Date();
		Date tomorrowDate = new Date();
		Date nextSevenDays = new Date();
		Calendar c = Calendar.getInstance(); 
		c.setTime(nowDate); 
		c.add(Calendar.DATE, 1);
		tomorrowDate = c.getTime();
		Calendar c2 = Calendar.getInstance(); 
		c2.setTime(tomorrowDate); 
		c2.add(Calendar.DATE, 7);
		nextSevenDays = c2.getTime();
		List<Integer> todayEvents = getOneDayEvents(nowDate);
		List<Integer> tomorrowEvents = getOneDayEvents(tomorrowDate);
		List<Integer> nextWeekEvents = getEventsInTimeIntervall(tomorrowDate, nextSevenDays);
		HashMap<Integer, Long> facebookEventsInfo = getFacebookEventsPageInfo();
		HashMap<Integer, Long> legitFacebookEvents = getLegitFacebookEvents();
		HashMap<Integer, Long> tempData = new HashMap<Integer, Long>();	// for facebook page information
		HashMap<Integer, Double> topEventsToUpdate = new HashMap<Integer, Double>();	// events rank to update for all users
		for(int i=0; i<todayEvents.size(); i++){
			Integer eventId = todayEvents.get(i);
			if(legitFacebookEvents.containsKey(eventId) && facebookEventsInfo.containsKey(eventId)){
				Long Number = facebookEventsInfo.get(eventId);
				tempData.put(eventId, Number);
			}
		}
		LinkedHashMap<Integer, Long> sortedTemp = sortbyValue(tempData);
		int counter=1;
		for(Entry<Integer, Long>entry: sortedTemp.entrySet()){
			if(counter<=10){
				Integer eventId = entry.getKey();
				Double newRank = todayTrendingNum + Math.random()/100;
				topEventsToUpdate.put(eventId, newRank);
				counter++;
			}else{
				break;
			}
		}
		tempData.clear();
		sortedTemp.clear();
		for(int i=0; i<tomorrowEvents.size(); i++){
			Integer eventId = tomorrowEvents.get(i);
			if(legitFacebookEvents.containsKey(eventId) && facebookEventsInfo.containsKey(eventId) && !todayEvents.contains(eventId)){
				Long Number = facebookEventsInfo.get(eventId);
				tempData.put(eventId, Number);
			}
		}
		sortedTemp = sortbyValue(tempData);
		counter=1;
		for(Entry<Integer, Long>entry: sortedTemp.entrySet()){
			if(counter<=10){
				Integer eventId = entry.getKey();
				Double newRank = tomorrowTrendingNum + Math.random()/100;
				topEventsToUpdate.put(eventId, newRank);
				counter++;
			}else{
				break;
			}
		}
		tempData.clear();
		sortedTemp.clear();
		for(int i=0; i<nextWeekEvents.size(); i++){
			Integer eventId = nextWeekEvents.get(i);
			if(legitFacebookEvents.containsKey(eventId) && facebookEventsInfo.containsKey(eventId) && !todayEvents.contains(eventId) && !tomorrowEvents.contains(eventId)){
				Long Number = facebookEventsInfo.get(eventId);
				tempData.put(eventId, Number);
			}
		}
		sortedTemp = sortbyValue(tempData);
		counter=1;
		for(Entry<Integer, Long>entry: sortedTemp.entrySet()){
			if(counter<=10){
				Integer eventId = entry.getKey();
				Double newRank = nextweekTrendingNum + Math.random()/100;
				topEventsToUpdate.put(eventId, newRank);
				counter++;
			}else{
				break;
			}
		}
		updateREC(topEventsToUpdate);
	}
}
