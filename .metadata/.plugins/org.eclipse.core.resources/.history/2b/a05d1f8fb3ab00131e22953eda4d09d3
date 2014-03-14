import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.incredibles.data.Rec;
import com.incredibles.storage.ClientDbService;
import com.incredibles.storage.ClientDbServiceCreator;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;
import com.incredibles.storage.Utils;

public class CalculateFirstStep {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public void startAutomaticEventCounting() {
		final Runnable beeper = new Runnable() {
			public void run() {				
				
				ClientDbService dbService = null;
				int u;
				try {
					
					
					
					dbService = ClientDbServiceCreator.createCloud();
					
					u = dbService.getAllUserNumber();					
					HashMap <Integer,Integer> hm = dbService.getEventsCount();
					HashMap <Integer, Double> eventidrank = new HashMap<Integer,Double>();	
					
					// max event-nézettség David mod
					int max = 1;
					for(Entry<Integer, Integer> entry : hm.entrySet()){
						 if(entry.getValue()>max){
							 max=entry.getValue();
						 }
					}
					//end of David mod
					
					for (Entry<Integer, Integer> entry : hm.entrySet()) {
				        Integer eventid = entry.getKey();
				        double x = entry.getValue();
//				        double rank = x/(x+u);
				          
				        
				        //David mod
				        double rank2 = x/max;
				        if(rank2>0.1 && rank2<0.3){
				        	rank2=0.1;
				        }else{
				        	rank2=1-Math.exp(-Math.pow((rank2-0.2), 2.0)/(2.0*Math.pow(0.2, 2.0)));
				        } 
				        //end of David mod
				        
				        //ez itt rank volt
				        rank2 = rank2 + Math.random()/100;
				        eventidrank.put(eventid, rank2);
					}

					LinkedHashMap<Integer, Double> sortedMapDesc = (LinkedHashMap<Integer, Double>) Utils.sortByValues(eventidrank);
		
					List<Rec> r = new ArrayList<Rec>();					
					for (Entry<Integer, Double> entry : sortedMapDesc.entrySet()) {
				        Integer eventid = entry.getKey();
				        double rank = entry.getValue();
				        
				        Rec aRec = new Rec();
				        aRec.setEventID(eventid);
				        aRec.setUserID(0);
				        aRec.setRank(rank);
				        
				        
				        System.out.println("eventid: "+eventid+" "+" rank: "+rank);
				        r.add(aRec);
					}
					
					for(int i = r.size();i<=1000;i++){
						 	Rec aRec = new Rec();
					       	aRec.setEventID(0);
					        aRec.setUserID(0);
					        aRec.setRank(0);
					        
					        System.out.println("eventid: "+0+" "+" rank: "+0);
					        r.add(aRec);
					}
					
					dbService.deleteAllTempCount();
					dbService.uploadBatchTempRec(r);
		        
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
		};
		final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(beeper, 0, 1, TimeUnit.HOURS);
	}

}
