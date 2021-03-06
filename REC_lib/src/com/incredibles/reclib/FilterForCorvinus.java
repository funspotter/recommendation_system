package com.incredibles.reclib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import com.incredibles.data.Like;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;

/*kisz�moljuk �jra a user fb like kateg�ria rankj�t �s a maximum �rt�kkel adjuk vissza a corvinusos esem�nyeket*/
/*KELL: 
 * - user like analizis, abban "corvinus" keres�se
 * - hova j�r egyetemre
 * - sz�r�s korra 14-28
 * - eventekb�l kisz�rni melyik corvinuszos
 * - kisz�molni ism�t discriminator rankokat
 * - a max disc rankhoz k�pest legyen maxon az �rt�ke az eventnek*/

/*This class highly connected to DiscriminatorAndLike class*/
public class FilterForCorvinus implements FiltersForRec{

	@Override
	public Integer getUserIdFromFbId(long fbId) {
		RecommenderDbService dbService = null;
		Integer userId = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			userId = dbService.getUserIdforFacebookUserId(fbId);
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

	
	@Override
	public List<Like> getAllLikeForUser(long fbId) {
		RecommenderDbService dbService = null;
		List<Like> likes = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			likes = dbService.getAllLikeForFbUserIdV2(fbId);
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
		return likes;
	}

	/*In this filter we use -getLegitEventsDescription- and -getLegitEventsName-*/
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

	/*Return legit event description and name. And legit Events ID too*/
	public static List<HashMap<Integer,String>> getLegitEventsDescriptionName(long from){
		RecommenderDbService dbService = null;
		List<HashMap<Integer,String>> eventDescName = null;
		try {
			eventDescName = new ArrayList <HashMap<Integer,String>>();
			dbService = RecommenderDbServiceCreator.createCloud();
			eventDescName = dbService.getLegitEventDescAndNameFromDate(from);
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
		return eventDescName;
	}
		
	/*Not implemented*/
	@Override
	public Integer getUserBirtdate(long fbId) {
		// TODO Auto-generated method stub
		return null;
	}

	/*Return the correct date*/
	public Integer getCurrentDate() {
		long timestamp = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		Integer date = year * 10000 + month * 100 + day;
		return date;
	}
	
	/* Retrun hashmap with eventid; rank calculated for corvinus filter */
	@Override
	public HashMap<Integer, Double> setEventsRank(long fbId) {
		List<Like> likes = null;							/*List for user likes*/
		HashMap<Integer,String> eventDescription = null;	/*hashmap for events description*/
		HashMap<Integer,String> eventName = null;			/* hashmap for events name*/
		List<Integer> corvinusEvents = new ArrayList<Integer>();		/* list of picked events*/
		List <HashMap<Integer,String>> eventsDescriptionAndName = null; /*quary returns with hashmap list*/
		HashMap<String,Double> discriminatorRank = null;
		HashMap<Integer,Double> eventNewRank = new HashMap<Integer,Double>();
		
		String triggerWord = "corvinus";								/*Set the seeked trigger word*/
		likes = getAllLikeForUser(fbId);								/*Get all likes for Fb user*/	
		Integer date = getCurrentDate();
		
																		/*If his/her likes or school contain any correlation with
																		 * corvinus string, give higher rank value to corvinus events*/
		boolean corvinusFlag = false;									/*Yet, not found corvinus like*/
		for(int i=0; i< likes.size(); i++){
				String apiResult = null;
				long likeID = likes.get(i).getId();
				apiResult = readFromFacebookApi(likeID);
				if(apiResult!=null){
					if(apiResult.toLowerCase().contains(triggerWord)){
						corvinusFlag = true;
						break;											/*In this version, just one triggered like needed*/
					}
				}
				else{
					System.out.println(likes.get(i).getName());			/*graph api returns exeption*/
				}
		}
		if(corvinusFlag == true){												/* we found at least one corvinus content*/
			eventsDescriptionAndName = getLegitEventsDescriptionName(date);
			/*IMPORTANT ORDER*/
			eventName = eventsDescriptionAndName.get(0);
			eventDescription = eventsDescriptionAndName.get(1);
			for(Entry<Integer, String> entry : eventName.entrySet()){			/*Iterate through event's name*/
				Integer eventId = entry.getKey();
				String name = entry.getValue();
				if(name.toLowerCase().contains(triggerWord)){
					corvinusEvents.add(eventId);
				}
			}
			for(Entry<Integer,String> entry : eventDescription.entrySet()){		/*Iterate through event's description*/
				Integer eventId = entry.getKey();
				String description = entry.getValue();
				if(!corvinusEvents.contains(eventId)){
					if(description.toLowerCase().contains(triggerWord)){
						corvinusEvents.add(eventId);							/*New triggerword event found*/
					}
				}
			}		
		}
		if(!corvinusEvents.isEmpty()){
			discriminatorRank = convertLiketoNumber(likes);
			Double rankMax = DiscriminatorAndLikeV2.maximumValueInHashmap(discriminatorRank);
			for(int i = 0; i < corvinusEvents.size(); i++){
				double newRank = rankMax+Math.random()/100;
				if(newRank >= 0 && newRank < 1.0){
					eventNewRank.put(corvinusEvents.get(i), newRank);
				}else if(newRank >= 1.0){
					eventNewRank.put(corvinusEvents.get(i), 0.9+Math.random()/100);
				}
			}
		}else{
			eventNewRank = null;
		}
		return eventNewRank;		/*Problem : events in the database do not consist that they are "corvinus" stuff*/
	}

	
	/*Like to rank, not implemented the movie and music genre searcher*/
	public static HashMap<String,Double> convertLiketoNumber(List<Like> fbLikes){
		HashMap<String, Integer> discNumber = new HashMap<String, Integer>();
		HashMap<String, Double> discRank = new HashMap<String, Double>();
			
		DiscriminatorAndLikeV2 startObject = new DiscriminatorAndLikeV2();
			
		/* Update default values in HashMap */
		discNumber.put("cinema", 0);
		discNumber.put("exhibition", 0);
		discNumber.put("festival", 0);
		discNumber.put("gastro", 0);
		discNumber.put("kid", 0);
		discNumber.put("music", 0);
		discNumber.put("sport", 0);
		discNumber.put("theater", 0);	
		discNumber.put("travel", 0);
		discNumber.put("other", 0);
		discNumber.put("party", 0);		
			
		/* Analise likes, counting, then make the ratio*/
		for(int i=0; i<fbLikes.size(); i++){
			Like oneLike = fbLikes.get(i);
			String fbCategory = oneLike.getCategory().toLowerCase();
			if(startObject.cinema.contains(fbCategory)){
				DiscriminatorAndLikeV2.likeCountPlusPlus(discNumber,"cinema");
			}if(startObject.exhibition.contains(fbCategory)){
				DiscriminatorAndLikeV2.likeCountPlusPlus(discNumber,"exhibition");
			}if(startObject.festival.contains(fbCategory)){
				DiscriminatorAndLikeV2.likeCountPlusPlus(discNumber,"festival");
			}if(startObject.gastro.contains(fbCategory)){
				DiscriminatorAndLikeV2.likeCountPlusPlus(discNumber,"gastro");
			}if(startObject.kid.contains(fbCategory)){
				DiscriminatorAndLikeV2.likeCountPlusPlus(discNumber,"kid");
			}if(startObject.music.contains(fbCategory)){
				DiscriminatorAndLikeV2.likeCountPlusPlus(discNumber,"music");
			}else if(startObject.sport.contains(fbCategory)){
				DiscriminatorAndLikeV2.likeCountPlusPlus(discNumber,"sport");
			}if(startObject.theater.contains(fbCategory)){
				DiscriminatorAndLikeV2.likeCountPlusPlus(discNumber,"theater");
			}if(startObject.travel.contains(fbCategory)){
				DiscriminatorAndLikeV2.likeCountPlusPlus(discNumber,"travel");
			}if(startObject.other.contains(fbCategory)){
				DiscriminatorAndLikeV2.likeCountPlusPlus(discNumber,"other");
			}if(startObject.party.contains(fbCategory)){
				DiscriminatorAndLikeV2.likeCountPlusPlus(discNumber,"party");
			}
		}			
			/* Discriminator number to discriminator ranks */
			discRank = DiscriminatorAndLikeV2.convertDiscNumbToDiscRank(discNumber);
			return discRank;
		}
	
	
	/*Get from graph.facebook the like description*/
	public static String readFromFacebookApi(long fbLikeID){
		JSONObject json = null;
		String string = null;
		try {
			json = readJsonFromUrl("https://graph.facebook.com/"+fbLikeID);
		//	System.out.println(json.get("id"));
		//	System.out.println(json.toString());
			string = json.toString();
		} catch (IOException | JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Nincs hozz� facebook.graph:" + fbLikeID);
			return null;
		}
	    return string;
	}
	
	/*JSON parsing*/
	private static String readAll(BufferedReader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	  }
	
	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
		     BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
		     String jsonText = readAll(rd);
		     JSONObject json = new JSONObject(jsonText);
		     return json;
		    }finally {
		     is.close();
		    }
		  }
}
