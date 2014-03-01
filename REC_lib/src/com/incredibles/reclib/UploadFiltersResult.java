package com.incredibles.reclib;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.incredibles.storage.ClientDbService;
import com.incredibles.storage.ClientDbServiceCreator;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;

public class UploadFiltersResult {

	/*Call thie function to execute all filters, and upload for user the top 1000 event*/
	public static void filterExecute(int UserId){
		HashMap<Integer,Double> eventRankValues1 = null;
		HashMap<Integer,Double> eventRankValues2 = null;
		LinkedHashMap<Integer,Double> SumEventRanks = null;
		
		DiscriminatorAndLike one = new DiscriminatorAndLike();
		eventRankValues1 = one.setEventsRank(UserId);
		
//		FilterForCorvinus two = new FilterForCorvinus();
//		eventRankValues2 = two.setEventsRank(fbId);
		
		SumEventRanks = sortByValueAndMakeThousandElement(hashmapPutTogether(eventRankValues1,eventRankValues2));
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			dbService.updateRecP(UserId,SumEventRanks);
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
	
	/*Nomen est omen. Needed because of the new database structure*/
	public static LinkedHashMap<Integer, Double> sortByValueAndMakeThousandElement(Map<Integer, Double> map) {
        List<Map.Entry<Integer, Double>> list = new LinkedList<Map.Entry<Integer, Double>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
            public int compare(Map.Entry<Integer, Double> m1, Map.Entry<Integer, Double> m2) {
                return (m2.getValue()).compareTo(m1.getValue());
            }
        });
        LinkedHashMap<Integer, Double> result = new LinkedHashMap<Integer, Double>();
        int i = 0;
        for (Entry<Integer, Double> entry : list) {
        	if(i<1000){
        		result.put(entry.getKey(), entry.getValue());
        		i++;
        	}else{
        		break;
        	}
        }
        return result;
    }
	
	
	public static HashMap<Integer,Double> hashmapPutTogether(HashMap<Integer,Double> first, HashMap<Integer,Double> second){
		HashMap<Integer,Double> sum = new HashMap<Integer,Double>();
		sum.putAll(first);
		if(second!=null){
			for(Entry<Integer,Double> entry: second.entrySet()){
				Integer eventId = entry.getKey();
				Double rank = entry.getValue();
				if(!sum.containsKey(eventId)){
					sum.put(eventId, rank);
				}
				else{
					Double rankFromSum = sum.get(eventId);
					if(rankFromSum<rank){
						sum.put(eventId, rank);
					}
				}
			}
		}
	
		return sum;	
	}
	
	
	public static Integer getUserIdFromFbId(long fbId) {
		ClientDbService dbService = null;
		Integer userId = null;
		try {
			dbService = ClientDbServiceCreator.createCloud();
			userId = dbService.getUserIDforFbUserID(fbId);
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
	
}
