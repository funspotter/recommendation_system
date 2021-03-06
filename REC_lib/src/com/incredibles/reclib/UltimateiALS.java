package com.incredibles.reclib;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jblas.DoubleMatrix;

import cern.colt.matrix.impl.SparseDoubleMatrix2D;

import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;

public class UltimateiALS {
	
	public SparseDoubleMatrix2D spd = Readerv2.ReadedMatrix;
	public DoubleMatrix X = Recommender.X; // trained X user factor matrix
	public DoubleMatrix YT = Recommender.YT;	// trained Y transposed item factor matrix
	DoubleMatrix userTrained;
//	StringVector new_user = Reader.new_user;	// Vector<Integer> for web
//	StringVector new_item = Reader.new_item;	// Vector<Integer> for web
	HashMap<String, HashMap<String, Integer>> TestUserItemHm = Readerv2.TestUserItemHm;	// Test dataset informations
	HashMap <String, Integer> user_map = Readerv2.LinkingUserID;	// user string - matrix number
	HashMap <String, Integer> item_map = Readerv2.LinkingItemID;
	
	
	public void runUltimateiALS(int userid){
		
//		ClientDbService dbService = null;
//		try {
//			dbService = ClientDbServiceCreator.createCloud();
//			dbService.copyNewEventsToOldUsers();
//		} catch (SQLException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}	finally {
//			if (dbService != null) {
//				try {
//					dbService.close();
//				} catch (SQLException | IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
		
		
		try {
			Tester.runAllTests();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*ellenorizni:
	 * - event legit m�g?
	 * - */
	public void UploadIALS(){
		List<Integer> legitEvents = getEventsFromDate(); // legit eventek lekerese
		HashMap<Integer, Double> rankofUser = null;
		Double weightedRank = 0.0;
		int event_counter=0;
		for(Entry<String,Integer> entry: user_map.entrySet()){	// iterate on users
			String datasetIDUser = entry.getKey();
			Integer userDBID = Integer.parseInt(datasetIDUser);
			System.out.println(userDBID);
			Integer userMatrixID = entry.getValue();
			Double ialsEventRank = 0.0;
			if(userDBID.equals(80) || userDBID.equals(389) || userDBID.equals(294) || userDBID.equals(81) || userDBID.equals(247)){	// ONLY FOR ...
				rankofUser = getRankforEvent(userDBID);
				userTrained = X.getRow(userMatrixID).mmul(YT);
				double[] userTrainedArray = userTrained.toArray();
				for(Entry<String, Integer> entry2: item_map.entrySet()){
					Integer eventDBID = Integer.parseInt(entry2.getKey());
					Integer eventMatrixID = entry2.getValue();
					if(userTrainedArray.length>=eventMatrixID){
						ialsEventRank = userTrainedArray[eventMatrixID];
						if(legitEvents.contains(eventDBID)){
							event_counter++;
							if(rankofUser.containsKey(eventDBID)){
								Double oldRank = rankofUser.get(eventDBID);
								weightedRank = (2.0*oldRank+8.0*ialsEventRank)/10.0;
							}else {
								weightedRank = 0.9 * ialsEventRank;
							}
							if(weightedRank > 1.0){
								weightedRank = 0.8 + Math.random()/100;
							}
							rankofUser.put(eventDBID, weightedRank);
						}
					}		
				}
				System.out.println("Megvaltoztatott es uj eventek:" +event_counter);
				LinkedHashMap<Integer,Double> newRankHm = sortByValueAndMakeThousandElement(rankofUser);	/*sort Hashmap*/
				uploadRankValues(userDBID,newRankHm);															/*Upload new sorted linked hashmap*/
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

	// Return events with legit show from date
		public List<Integer> getEventsFromDate() {
			RecommenderDbService dbService = null;
			List<Integer> events = new ArrayList<Integer>();
			try {
				dbService = RecommenderDbServiceCreator.createCloud();
				//events = dbService.eventsInShowList(1);
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

		
		/** Upload changed rank values caused by ials*/
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

}
