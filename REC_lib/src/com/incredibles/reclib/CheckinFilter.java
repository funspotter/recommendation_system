package com.incredibles.reclib;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;

public class CheckinFilter {

	/*Return users checkin place name*/
	public List<String> getCheckins(int UserId){
		List<String> checkins = null;
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			checkins = dbService.getUserCheckin(UserId);
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
		return checkins;
	}

	/*Returns legit events places based in isIn flag*/
	public HashMap<Integer, List<String>> getLegitEventsPlaceName(){
		RecommenderDbService dbService = null;
		HashMap<Integer, List<String>> eventPlaces = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			eventPlaces = dbService.getLegitEventsPlaceName();
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
		return eventPlaces;
	}

	/*Returns a flag to higher the events rank based on checkin*/
	public HashMap<Integer, Boolean> eventShouldPush(int UserId){
		boolean trigger = false;
		HashMap<Integer, Double> eventValues = new HashMap<Integer, Double>();
		HashMap<Integer, List<String>> eventPlaceNames = getLegitEventsPlaceName();
		List<String> checkins = getCheckins(UserId);
		HashMap<Integer, Boolean> eventFlag = new HashMap<Integer, Boolean>();
		for(Entry<Integer,List<String>>entry: eventPlaceNames.entrySet()){
			trigger = false;
			Integer EventId = entry.getKey();
			List<String> eventPlace = entry.getValue();
			for(int i=0; i<checkins.size(); i++){
				String checkinPlace = checkins.get(i);
				String[] parts = checkinPlace.split(" ");
				for(int j=0; j<parts.length; j++){
					for(int k=0; k<eventPlace.size(); k++){
						String placeName = eventPlace.get(k);
						if(placeName.contains(parts[i])){
							trigger = true;
						}
					}
				}
			}
			eventFlag.put(EventId, trigger);
		}
		return eventFlag;
	}

	/*Set all the things*/
	public HashMap<Integer, Double> setRankValuesWithCheckin(HashMap<Integer, Double> eventRank, int UserId){
		HashMap<Integer, Boolean> eventFlag = eventShouldPush(UserId);
		for(Entry<Integer, Double>entry: eventRank.entrySet()){
			Integer EventId = entry.getKey();
			Double oldRank = entry.getValue();
			if(eventFlag.containsKey(EventId)){
				Boolean flag = eventFlag.get(EventId);
				if(flag==true){
					Double newRank = 1.5*oldRank;
					eventRank.put(EventId, newRank);
				}
			}
		}
		return eventRank;
	}
	

}
