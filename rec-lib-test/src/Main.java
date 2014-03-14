import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

import org.jfree.ui.RefineryUtilities;
import org.json.JSONException;

import com.incredibles.data.Rec;
import com.incredibles.reclib.CheckinFilter;
import com.incredibles.reclib.DavidCos;
import com.incredibles.reclib.DiscriminatorAndLike;
import com.incredibles.reclib.DiscriminatorCategorization;
import com.incredibles.reclib.EventReplacerV1;
import com.incredibles.reclib.LikeWeighting;
import com.incredibles.reclib.Reader;
import com.incredibles.reclib.ReaderFromFile;
import com.incredibles.reclib.RecMaintenance;
import com.incredibles.reclib.Recommender;
import com.incredibles.reclib.SecondStep;
import com.incredibles.reclib.Tester;
import com.incredibles.reclib.MyFilteredClassifier;
import com.incredibles.reclib.UploadFiltersResult;
import com.incredibles.reclib.UploadFiltersResultV2;
import com.incredibles.storage.ClientDbService;
import com.incredibles.storage.ClientDbServiceCreator;
import com.incredibles.storage.LogLine;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;



public class Main {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		
//		Tester t = new Tester();
//		try {
//			t.runAllTests();
//		} catch (SQLException | IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		
//		Recommender ajanlas = new Recommender();
//		ajanlas.newRecFunction();
		
		
		
		
		
//		RecommenderDbService dbService = null;
//		try {
//			dbService = RecommenderDbServiceCreator.createCloud();
//			Date nowDate = new Date();
//			HashMap<Integer, String> eventDiscriminator = dbService.getEventDiscriminatorFromDateV2(nowDate.getTime());
//			HashMap<Integer, HashMap<String, Double>> zeroUserDiscRanks = dbService.getUserDiscriminatorRank(0, true);
//			HashMap<String, Double> discRanks = zeroUserDiscRanks.get(0);
//			HashMap <Integer, Double> eventRank = new HashMap<Integer,Double>();	
//			for(Entry<Integer, String>entry: eventDiscriminator.entrySet()){
//				Integer EventId = entry.getKey();
//				String discriminator = entry.getValue();
//				if(discriminator.equals("simple") || discriminator == null){
//					// fuck it
//					//eventRank.put(EventId, Math.random()/100);
//				}else{
//					String discLow = discriminator.toLowerCase();
//					if(discRanks.containsKey(discLow)){
//						Double rankValue = discRanks.get(discLow)+Math.random()/100;
//						eventRank.put(EventId, rankValue);
//					}
//				}
//			}
//			LinkedHashMap<Integer, Double> sortedMapDesc = sortByValue(eventRank);
//			List<Rec> eventFirstStepRank = new ArrayList<Rec>();					
//			for (Entry<Integer, Double> entry : sortedMapDesc.entrySet()) {
//		        Integer eventid = entry.getKey();
//		        double rank = entry.getValue();   
//		        Rec aRec = new Rec();
//		        aRec.setEventID(eventid);
//		        aRec.setUserID(0);
//		        aRec.setRank(rank);
//		        System.out.println("eventid: "+eventid+" "+" rank: "+rank);
//		        eventFirstStepRank.add(aRec);
//			}
//			dbService.updateNewUserEventCaches(eventFirstStepRank);
//			dbService.insertRecommendationLog("RefreshFirstStepDone", 0);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
	
		
		
		DiscriminatorCategorization categor = new DiscriminatorCategorization();
		categor.categorizing();
		
		
		
//		ReaderFromFile uj = new ReaderFromFile();
//		uj.ExecuteReading();

		
	
//		RecommenderDbService dbService2=null;
//		List<Integer> blabla = new ArrayList<Integer>();
//		blabla.add(256);
//		try {
//			dbService2 = RecommenderDbServiceCreator.createCloud();
//			dbService2.updateIsinFlagForEvents(blabla);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}finally {
//			if (dbService2 != null) {
//				try {
//					dbService2.close();
//				} catch (SQLException | IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
		
		
//		
//	RecommenderDbService dbService = null;
//	try {
//		dbService = RecommenderDbServiceCreator.createCloud();
//		HashMap <Integer,Integer> eventClick = dbService.countLegitEventsClick();
//		Date nowDate = new Date();
//		HashMap<Integer, String> eventDiscriminator = dbService.getEventDiscriminatorFromDateV2(nowDate.getTime());
//		HashMap<Integer, HashMap<String, Double>> zeroUserDiscRanks = dbService.getUserDiscriminatorRank(0, true);
//		HashMap<String, Double> discRanks = zeroUserDiscRanks.get(0);
//		HashMap <Integer, Double> eventRank = new HashMap<Integer,Double>();	
//		int maxClick = 1;
//		for(Entry<Integer, Integer> entry : eventClick.entrySet()){
//			 if(entry.getValue()>maxClick){
//				 maxClick=entry.getValue();
//			 }
//		}
//		for (Entry<Integer, Integer> entry : eventClick.entrySet()) {
//	        Integer eventid = entry.getKey();
//	        Integer Click = entry.getValue();
//	        double x = (double) Click;
//	        if(Click!=0){
//	        	 double rank2 = x/(double)maxClick;
//	 	        if(rank2>0.1 && rank2<0.3){
//	 	        	rank2=0.1;
//	 	        }else{
//	 	        	rank2=1-Math.exp(-Math.pow((rank2-0.2), 2.0)/(2.0*Math.pow(0.2, 2.0)));
//	 	        }
//	 	        rank2 = rank2 + Math.random()/100;
//	 	        eventRank.put(eventid, rank2);
//	        }else{
//	        	String discriminator = eventDiscriminator.get(eventid);
//	        	if(discRanks.containsKey(discriminator)){	// mert nem bizunk a sajat adatbazisunkban...
//	        		Double discriminatorRank = discRanks.get(discriminator);
//		        	double newRank = discriminatorRank + Math.random()/100;
//		        	eventRank.put(eventid, newRank);
//	        	}
//	        }
//		}
//		LinkedHashMap<Integer, Double> sortedMapDesc = sortByValue(eventRank);
//		List<Rec> eventFirstStepRank = new ArrayList<Rec>();					
//		for (Entry<Integer, Double> entry : sortedMapDesc.entrySet()) {
//	        Integer eventid = entry.getKey();
//	        double rank = entry.getValue();   
//	        Rec aRec = new Rec();
//	        aRec.setEventID(eventid);
//	        aRec.setUserID(0);
//	        aRec.setRank(rank);
//	        System.out.println("eventid: "+eventid+" "+" rank: "+rank);
//	        eventFirstStepRank.add(aRec);
//		}
//		dbService.updateNewUserEventCaches(eventFirstStepRank);
//	} catch (SQLException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}finally {
//		if (dbService != null) {
//			try {
//				dbService.close();
//			} catch (SQLException | IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//	
//	
//		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//		Date date = new Date();
//		System.out.println(" time: "+dateFormat.format(date));
//		RecMaintenance maintain = new RecMaintenance();
//		maintain.maintainRecTable();
		
//		RecMaintenance maintain = new RecMaintenance();
//		maintain.maintainRecTable();
		
//		int UserId = 30;
//		HashMap<Integer, Double> userRanks = null;
//		RecommenderDbService dbService = null;
//		try {
//			dbService = RecommenderDbServiceCreator.createCloud();
//			userRanks = dbService.getRecPUser(UserId);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}finally {
//			if (dbService != null) {
//				try {
//					dbService.close();
//				} catch (SQLException | IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		
//		CheckinFilter valami = new CheckinFilter();
//		userRanks = valami.setRankValuesWithCheckin(userRanks, UserId);
		
//		HashMap<Integer, Long> userIdFaceId = new HashMap<Integer, Long>();
//		RecommenderDbService dbService = null;	
//		try {
//			dbService = RecommenderDbServiceCreator.createCloud();
//			userIdFaceId = dbService.getAllFaceAndUserIdV2();
//			int i =1;
//			for(Entry<Integer, Long> entry : userIdFaceId.entrySet()){
//				if(i>=741){
//					System.out.println(i+". user"+ "	id:"+ entry.getKey());
//					Long facebookid = entry.getValue();
//					Integer UserId = entry.getKey();
//					UploadFiltersResultV2.filterExecute(UserId);
//				}
//				i++;
//			}		
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}finally {
//			if (dbService != null) {
//				try {
//					dbService.close();
//				} catch (SQLException | IOException e) {
//					e.printStackTrace();
//				}
//			}
//
//		}
		
//		EventReplacerV1 one = new EventReplacerV1();
//		long date = one.getCurrentDate();
//		System.out.println(date);
//		one.executeEventReplace();
		
		
		
		
		
//		RecommenderDbService dbService = null;
//		LogLine line = new LogLine();
//			try {
//				dbService = RecommenderDbServiceCreator.createCloud();
//				while(dbService.getNextLogLine(line)){
//					Date time = line.timeStamp;
//					int userid = line.userId;
//					int eventid = line.eventId;
//					int type = line.type;
//					System.out.println(time);
//					System.out.println(userid);
//					System.out.println(eventid);
//					System.out.println(type);
//					
//				}
//			} catch (SQLException | IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
		
		
		
		
		
		
//		DavidCos dc = new DavidCos();
//		String str=null;
//		try {
//			str = dc.runDavidTest();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("hi"+str);
		
//		ColdStart cs = new ColdStart();
//		try {
//			long l= 1000002091*100000+97406;
//			cs.run(l);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}//100000209197406
		
//		MyFilteredClassifier mfc = new MyFilteredClassifier();
//		mfc.makeClassifier();
		
		
//	    ResultPlot demo = new ResultPlot("Scatter Plot Demo", "Chart title");
//	      demo.pack();
//	      RefineryUtilities.centerFrameOnScreen(demo);
//	      demo.setVisible(true);
//		
//		Reader valami = new Reader();
//		try {
//			valami.readFileWithoutDate();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
		
//		DiscriminatorAndLike filter1 = new DiscriminatorAndLike();
//		filter1.setEventsRank(100001001552981L);
		
		
//		HashMap<String,Double> rankok = new HashMap<String,Double>();
//		rankok = lw.rankSetting(likeCounter);
		
		
//		SecondStep mindenkire = new SecondStep();
//		
//		HashMap<Integer, Long> userIdFaceId = new HashMap<Integer, Long>();
//		ClientDbService dbService = null;
//		
//		try {
//			dbService = RecommenderDbServiceCreator.createCloud();
//			userIdFaceId = dbService.getAllFaceAndUserId();
//			for(Entry<Integer, Long> entry : userIdFaceId.entrySet()){
//				Integer userid = entry.getKey();
//				Long facebookid = entry.getValue();
//				mindenkire.makeSecondStep(facebookid, userid);
//			}
//			
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
		
//		RecommenderDbService dbService = null;		
//		try {
//			dbService = RecommenderDbServiceCreator.createCloud();
//		
//			System.out.println(dbService.getUserBirthDate(100000209197406L));			
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		
		
//		lw.downloadGenreForMovie("Vuk");
		
//		for (Entry<String, Double> entry : rankok.entrySet()) {
//	       String csoport = entry.getKey();
//			Double rank = entry.getValue();
//			System.out.println("cucc: "+csoport+ " rank: " +rank);
//		}
		
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
