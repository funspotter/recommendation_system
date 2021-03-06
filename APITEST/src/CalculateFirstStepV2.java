import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.incredibles.data.Rec;
import com.incredibles.reclib.TopTenEventFilter;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;

public class CalculateFirstStepV2 {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	public void startAutomaticFirstStepRefresh() {
		final Runnable beeper = new Runnable() {
			public void run() {
				refreshFirstStep();
			}
		};
		final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(beeper, 1, 8, TimeUnit.HOURS);
	}
	
	
	public static void refreshFirstStep(){
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			Date nowDate = new Date();
			HashMap<Integer, String> eventDiscriminator = dbService.getEventDiscriminatorFromDateV2(nowDate.getTime());
			HashMap<Integer, HashMap<String, Double>> zeroUserDiscRanks = dbService.getUserDiscriminatorRank(0, true);
			HashMap<String, Double> discRanks = zeroUserDiscRanks.get(0);
			HashMap <Integer, Double> eventRank = new HashMap<Integer,Double>();	
			for(Entry<Integer, String>entry: eventDiscriminator.entrySet()){
				Integer EventId = entry.getKey();
				String discriminator = entry.getValue();
				if(discriminator.equals("simple") || discriminator == null){
					// fuck it
					//eventRank.put(EventId, Math.random()/100);
				}else{
					String discLow = discriminator.toLowerCase();
					if(discRanks.containsKey(discLow)){
						Double rankValue = discRanks.get(discLow)+Math.random()/100;
						eventRank.put(EventId, rankValue);
					}
				}
			}
			TopTenEventFilter filter = new TopTenEventFilter();
			eventRank = filter.setTopTenEventRanks(eventRank);
			LinkedHashMap<Integer, Double> sortedMapDesc = sortByValue(eventRank);
			List<Rec> eventFirstStepRank = new ArrayList<Rec>();					
			for (Entry<Integer, Double> entry : sortedMapDesc.entrySet()) {
		        Integer eventid = entry.getKey();
		        double rank = entry.getValue();   
		        Rec aRec = new Rec();
		        aRec.setEventID(eventid);
		        aRec.setUserID(0);
		        aRec.setRank(rank);
		        System.out.println("eventid: "+eventid+" "+" rank: "+rank);
		        eventFirstStepRank.add(aRec);
			}
			dbService.updateNewUserEventCaches(eventFirstStepRank);
			dbService.insertRecommendationLog("RefreshFirstStepDone", 0);
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

}
