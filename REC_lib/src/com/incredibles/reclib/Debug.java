package com.incredibles.reclib;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;

public class Debug {

	
	/*-------------- Facebook Event Classification UnDo --------------------------*/
	
	/**Returns uncategorized events facebook and funspotter id*/
	public static HashMap<Long, Integer> getAllFacebookIds(){
		HashMap<Long, Integer> FacebookEventId = null;
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			FacebookEventId = dbService.getAllFacebookEvents();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}finally {
			if (dbService != null) {
				try {
					dbService.close();
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		return FacebookEventId;
	}
	
	/**Upload the newly categorized events discriminator into database
	 * handle the events with zero facebook info from their places*/
	public static void uploadEventDiscriminators(HashMap<Integer, String> newEventDiscriminator, List<Integer>noInfoFromFacebook){
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			dbService.updateEventsDiscriminator(newEventDiscriminator,noInfoFromFacebook);
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

	/*undo facebook categorization*/
	public static void undoFacebookDiscriminators(){
		HashMap<Long, Integer> FacebookEvents = getAllFacebookIds();
		List<Integer> events = new ArrayList<Integer>();
		for(Entry<Long, Integer> entry: FacebookEvents.entrySet()){
			Integer FunspotterId = entry.getValue();
			events.add(FunspotterId);
		}
		uploadEventDiscriminators(null, events);
	}


}
