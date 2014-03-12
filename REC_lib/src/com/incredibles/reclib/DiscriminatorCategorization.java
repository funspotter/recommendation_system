package com.incredibles.reclib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.joda.time.DateTimeZone;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.JsonObject;
import com.incredibles.data.FacebookPlaceTag;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.exception.FacebookException;
import com.restfb.json.JsonException;
import com.restfb.types.Event;
import com.restfb.types.Place;

public class DiscriminatorCategorization {
	
	String cinemaList = "Cinema";
	String exhibitionList = "Museum/Art Gallery";
	String gastroList = "Food/Grocery, Restaurant/Caf�";
	String musicList = "Concert Venue";
	String sportList = "Attractions/Things to Do, Outdoor Gear/Sporting Goods, Sports Venue, Sports/Recreation/Activities";
	String travelList = "Transport, Tours/Sightseeing, Spas/Beauty/Personal Care, Airport, Hotel, Landmark";
	String partyList = "Club";
	
	/**Returns uncategorized events facebook and funspotter id*/
	public HashMap<Long, Integer> getUncategorizedEventsIds(){
		HashMap<Long, Integer> FacebookEventId = null;
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			FacebookEventId = dbService.getAllEventFacebookAndFunspotterId();
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
	
	/**First categorized based on the returned facebook place category*/
	public String easyCategorizing(String FacebookCategory){
		if(cinemaList.contains(FacebookCategory)){
			return "Cinema";
		}else if(exhibitionList.contains(FacebookCategory)){
			return "Exhibition";
		}else if(gastroList.contains(FacebookCategory)){
			return "Gastro";
		}else if(musicList.contains(FacebookCategory)){
			return "Music";
		}else if(sportList.contains(FacebookCategory)){
			return "Sport";
		}else if(travelList.contains(FacebookCategory)){
			return "Travel";
		}else if(partyList.contains(FacebookCategory)){
			return "Party";
		}else{
			return null;
		}
	}
	
	/**Returns CategoryList elements discriminator numbers*/
	public HashMap<Long, FacebookPlaceTag> getCategoryListNumbers(){
		HashMap<Long, FacebookPlaceTag> categoryDiscriminatorNumber = new HashMap<Long, FacebookPlaceTag>();
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			categoryDiscriminatorNumber = dbService.getFacebookPlaceTagTable();
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
		return categoryDiscriminatorNumber;
	}
	
	/**Updates the discriminator - number hashmaps*/
	public void putDiscriminatorNumbersIntoHashMap(HashMap<String, Integer> discriminatorNumbers, String Discriminator){
		if(discriminatorNumbers.isEmpty()){
			discriminatorNumbers.put("Cinema", 0);
			discriminatorNumbers.put("Exhibition", 0);
			discriminatorNumbers.put("Festival", 0);
			discriminatorNumbers.put("Gastro", 0);
			discriminatorNumbers.put("Kid", 0);
			discriminatorNumbers.put("Music", 0);
			discriminatorNumbers.put("Sport", 0);
			discriminatorNumbers.put("Theater", 0);
			discriminatorNumbers.put("Travel", 0);
			discriminatorNumbers.put("Party", 0);
			discriminatorNumbers.put("Other", 0);
			if(discriminatorNumbers.containsKey(Discriminator)){
				discriminatorNumbers.put(Discriminator, 1);
			}else{
				System.out.println("ProblemInDiscNumberUpdate");
			}
		}else{
			if(discriminatorNumbers.containsKey(Discriminator)){
				Integer number = discriminatorNumbers.get(Discriminator);
				discriminatorNumbers.put(Discriminator, number++);
			}else{
				System.out.println("ProblemInDiscNumberUpdate");
			}
		}
	}
	
	/**Add together the discriminator values*/
	public HashMap<String, Integer> sumDiscriminatorValues(HashMap<String, Integer> oneImportant, HashMap<String, Integer> two){
		if(oneImportant.isEmpty()){
			oneImportant.put("Cinema", two.get("Cinema"));
			oneImportant.put("Exhibition", two.get("Exhibition"));
			oneImportant.put("Festival", two.get("Festival"));
			oneImportant.put("Gastro", two.get("Gastro"));
			oneImportant.put("Kid",  two.get("Kid"));
			oneImportant.put("Music", two.get("Music"));
			oneImportant.put("Sport", two.get("Sport"));
			oneImportant.put("Theater", two.get("Theater"));
			oneImportant.put("Travel", two.get("Travel"));
			oneImportant.put("Party", two.get("Party"));
			oneImportant.put("Other", two.get("Other"));
		}else{
			oneImportant.put("Cinema", two.get("Cinema")+oneImportant.get("Cinema"));
			oneImportant.put("Exhibition", two.get("Exhibition")+oneImportant.get("Exhibition"));
			oneImportant.put("Festival", two.get("Festival")+oneImportant.get("Festival"));
			oneImportant.put("Gastro", two.get("Gastro")+oneImportant.get("Gastro"));
			oneImportant.put("Kid",  two.get("Kid")+oneImportant.get("Kid"));
			oneImportant.put("Music", two.get("Music")+oneImportant.get("Music"));
			oneImportant.put("Sport", two.get("Sport")+oneImportant.get("Sport"));
			oneImportant.put("Theater", two.get("Theater")+oneImportant.get("Theater"));
			oneImportant.put("Travel", two.get("Travel")+oneImportant.get("Travel"));
			oneImportant.put("Party", two.get("Party")+oneImportant.get("Party"));
			oneImportant.put("Other", two.get("Other")+oneImportant.get("Other"));
		}
		return oneImportant;
	}
	
	/**Returns one place category, and fill the onePlaceCategory hashmap with categoryList elements*/
	public String getFacebookData(Long FacebookId, HashMap<Long, String> onePlaceCategoryList){
		Event event = null;
		Place place = null;
		String categoryString = null;
		JSONArray categoryList = null;
		String MY_APP_SECRET = "add4434d3f3f754d29d567d59f285be5";
		String MY_APP_ID = "513927361994826";
		AccessToken accessToken = new DefaultFacebookClient().obtainAppAccessToken(MY_APP_ID, MY_APP_SECRET);
		FacebookClient facebookClient = new DefaultFacebookClient(accessToken.getAccessToken());
		String EventFacebookId = FacebookId.toString();
		try{
			event = facebookClient.fetchObject(EventFacebookId, Event.class, Parameter.with("metadata", 1));
			try{
				place  = facebookClient.fetchObject(event.getVenue().getId(), Place.class, Parameter.with("metadata", 1));
				try{
					JSONObject category = null;
					JSONObject valami = readJsonFromUrl(place.getMetadata().getConnections().getTagged().toString());
					JSONArray tomb = (JSONArray) valami.get("data");
					System.out.println(tomb);
					JSONObject valami2 = (JSONObject) tomb.get(0);
					JSONObject valami3 = (JSONObject) valami2.get("to");
					JSONArray tomb2 = (JSONArray) valami3.get("data");
					category = (JSONObject) tomb2.get(0);
					categoryString = category.get("category").toString();
					categoryList = (JSONArray) category.get("category_list");
					for(int i=0; i< categoryList.size(); i++){
						JsonObject oneCategoryList = (JsonObject) categoryList.get(i);
						Long id = oneCategoryList.get("id").getAsLong();
						String oneCategoryListName= oneCategoryList.get("name").getAsString();
						onePlaceCategoryList.put(id, oneCategoryListName);
					}
				}catch(NullPointerException | IOException | ParseException e){
					System.out.println("problemWithJson");
				}
			}catch(FacebookException e){
				System.out.println("NoPlaceInfo");
			}
		}catch(FacebookException e){
			System.out.println("NoEventInfo");
		}
		return categoryString;
	}
	
	/**Upload the newly categorized events discriminator into database
	 * handle the events with zero facebook info from their places*/
	public void uploadEventDiscriminators(HashMap<Integer, String> newEventDiscriminator, List<Integer>noInfoFromFacebook){
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
	
	/**Upload all of the categoryList elements into table with the new discriminator numbers*/
	public void uploadCategoryListElements(HashMap<Long, FacebookPlaceTag> CategoryListElements, HashMap<Long, FacebookPlaceTag> oldCategoryListElements){
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			dbService.updateFacebookPlaceTagTable(CategoryListElements, oldCategoryListElements);
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
	}
	
	/**Insert one log information to check the categorized evenst percent*/
	public void insertLogInformation(String comment){
		RecommenderDbService dbService2=null;
		try {
			dbService2 = RecommenderDbServiceCreator.createCloud();
			dbService2.insertRecommendationLog(comment, 0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (dbService2 != null) {
				try {
					dbService2.close();
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**Try to categorize all the necessary events*/
	public void categorizing(){
		insertLogInformation("EventCategorizeStart");
		HashMap<Long, Integer> FacebookEventId = getUncategorizedEventsIds();
		HashMap<Long, FacebookPlaceTag> CategoryListNumbers = getCategoryListNumbers();
		HashMap<Long, FacebookPlaceTag> oldCategoryListNumbers = CategoryListNumbers;
		HashMap<Long, String> onePlaceCategoryList = new HashMap<Long, String>();
		HashMap<Integer, String> newEventDiscriminator = new HashMap<Integer, String>();
		List<Long> nextSearchCycleEvents = new ArrayList<Long>();	// collect the not obvious events with discriminator
		List<Integer> noInfoFromFacebook = new ArrayList<Integer>();
		int noDiscriminator = 0;
		int noPlaceCategory = 0;
		int notCategorizedEvents = 0;
		int sumUncatEventNum = FacebookEventId.size();
		for(Entry<Long, Integer>entry: FacebookEventId.entrySet()){
			Long FacebookId = entry.getKey();
			Integer FunspotterId = entry.getValue();
			onePlaceCategoryList.clear();
			String categoryName = getFacebookData(FacebookId, onePlaceCategoryList);
			if(categoryName!=null){	// got info from facebook
				String discriminator = easyCategorizing(categoryName);
				if(discriminator!=null){	// no json problem
					for(Entry<Long, String>entry2: onePlaceCategoryList.entrySet()){
						FacebookPlaceTag discriminatorNumbers = null;
						Long oneCategoryListId = entry2.getKey();
						String oneCategoryListName = entry2.getValue();
						FacebookPlaceTag placeTag = new FacebookPlaceTag();
						placeTag.setId(oneCategoryListId);
						placeTag.setName(oneCategoryListName);
						if(CategoryListNumbers.containsKey(oneCategoryListId)){
							discriminatorNumbers = CategoryListNumbers.get(oneCategoryListId);
							putDiscriminatorNumbersIntoHashMap(discriminatorNumbers.getDiscriminatorNumber(), discriminator);
						}else{
							HashMap<String, Integer> discNumtoFacePlaceTag = new HashMap<String, Integer>();
							putDiscriminatorNumbersIntoHashMap(discNumtoFacePlaceTag, discriminator);
							discriminatorNumbers.setDiscriminatorNumber(discNumtoFacePlaceTag);
						}
						CategoryListNumbers.put(oneCategoryListId, discriminatorNumbers);
					}
					newEventDiscriminator.put(FunspotterId, discriminator);
				}else{
					noDiscriminator++;
					nextSearchCycleEvents.add(FacebookId);
				}
			}else{
				noInfoFromFacebook.add(FunspotterId);
				notCategorizedEvents++;
				noPlaceCategory++;
			}
		}
		System.out.println("Osszes event: "+FacebookEventId.size());
		System.out.println("Bekategorizalt eventek: "+newEventDiscriminator.size());
		System.out.println("Nem talalt discriminatort/ k�veztkez� ciklus: "+noDiscriminator);
		System.out.println("Nem kapott vissza facebooktol place category-t: "+noPlaceCategory);
		
		// lehet p�rhuzamos�tani
		uploadEventDiscriminators(newEventDiscriminator,null);
		uploadCategoryListElements(CategoryListNumbers,oldCategoryListNumbers);
		newEventDiscriminator.clear();
		
		HashMap<String, Integer> eventDiscriminatorNumber = new HashMap<String, Integer>();
		for(Entry<Long, Integer>entry: FacebookEventId.entrySet()){
			Long FacebookId = entry.getKey();
			Integer FunspotterId = entry.getValue();
			if(nextSearchCycleEvents.contains(FacebookId)){
				eventDiscriminatorNumber.clear();
				String categoryName = getFacebookData(FacebookId, onePlaceCategoryList);
				for(Entry<Long, String>entry2: onePlaceCategoryList.entrySet()){
					Long oneCategoryListId = entry2.getKey();
					String oneCategoryListName = entry2.getValue();
					if(CategoryListNumbers.containsKey(oneCategoryListId)){
						HashMap<String, Integer> categoryDiscNum = CategoryListNumbers.get(oneCategoryListId).getDiscriminatorNumber();
						eventDiscriminatorNumber = sumDiscriminatorValues(eventDiscriminatorNumber, categoryDiscNum);
					}
				}
				int max = 0;
				String maxDisc = null;
				for(Entry<String, Integer>entry1: eventDiscriminatorNumber.entrySet()){
					Integer number = entry1.getValue();
					String discriminator = entry1.getKey();
					if(number > max){
						max = number;
						maxDisc = discriminator;
					}
				}
				if(max != 0 && maxDisc!=null){
					newEventDiscriminator.put(FunspotterId, maxDisc);
				}else{
					notCategorizedEvents++;
				}
			}
		}
		Double plusPercent = (double) ((sumUncatEventNum-notCategorizedEvents)/sumUncatEventNum)*100;
		DecimalFormat df = new DecimalFormat("##");
        System.out.print(df.format(plusPercent));
		uploadEventDiscriminators(newEventDiscriminator,noInfoFromFacebook);
		insertLogInformation("EventCategorizeEnd +: "+df.format(plusPercent)+"% startUncat: "+sumUncatEventNum);
	}
	
	private static JSONObject readJsonFromUrl(String url) throws IOException, ParseException {
		InputStream is = new URL(url).openStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
		String jsonText = readAll(rd);
		try {
			JSONParser parser=new JSONParser();
			JSONObject json = new JSONObject();
			Object obj=parser.parse(jsonText);
			JSONObject object=(JSONObject)obj;
			return json;
		}finally {
			is.close();
		}
	}
	
	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}
	
}
