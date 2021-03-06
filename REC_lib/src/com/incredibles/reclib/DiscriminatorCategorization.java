package com.incredibles.reclib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

import com.incredibles.data.FacebookPlaceTag;
import com.incredibles.data.FunspotterEvent;
import com.incredibles.data.FacebookPageNumbers;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.exception.FacebookException;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.json.JsonException;
import com.restfb.types.Event;
import com.restfb.types.Page;
import com.restfb.types.Place;

public class DiscriminatorCategorization {
	
	public class StringLong{
		Long Number;
		String Words;
	}
	
	static String cinemaList = "Cinema";
	static String exhibitionList = "Museum/Art Gallery";
	static String gastroList = "Food/Grocery, Restaurant/Caf�, Restaurant/Cafe";
	static String musicList = "Concert Venue";
	static String sportList = "Attractions/Things to Do, Outdoor Gear/Sporting Goods, Sports Venue, Sports/Recreation/Activities";
	static String travelList = "Transport, Tours/Sightseeing, Spas/Beauty/Personal Care, Airport, Hotel, Landmark";
	static String partyList = "Club";
	
	static String categorizingStringV2 = "Concert Venue, Club";
	
	
	
	static final Long MIN_NUMBER = 750L;
	
	/**Returns uncategorized events facebook and funspotter id*/
	public static HashMap<Long, Integer> getFutureUncategorizedEventsIds(){
		HashMap<Long, Integer> FacebookEventId = null;
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			FacebookEventId = dbService.getUncategorizedFutureFacebookEvents();
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

	/**Returns uncategorized events facebook and funspotter id*/
	public static HashMap<Long, Integer> getFutureFacebookEventsIds(){
		HashMap<Long, Integer> FacebookEventId = null;
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			FacebookEventId = dbService.getAllFutureFacebookEvents();
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
	
	/**Returns all facebook events facebook and funspotter id*/
	public static HashMap<Long, Integer> getAllFacebookEventsIds(){
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
	
	/**First categorized based on the returned facebook place category*/
	public static String easyCategorizing(String FacebookCategory){
		if(cinemaList.contains(FacebookCategory)){
			return "Cinema";
		}else if(exhibitionList.toLowerCase().contains(FacebookCategory.toLowerCase())){
			return "Exhibition";
		}else if(gastroList.toLowerCase().contains(FacebookCategory.toLowerCase())){
			return "Gastro";
		}else if(musicList.toLowerCase().contains(FacebookCategory.toLowerCase())){
			return "Music";
		}else if(sportList.toLowerCase().contains(FacebookCategory.toLowerCase())){
			return "Sport";
		}else if(travelList.toLowerCase().contains(FacebookCategory.toLowerCase())){
			return "Travel";
		}else if(partyList.toLowerCase().contains(FacebookCategory.toLowerCase())){
			return "Party";
		}else{
			return null;
		}
	}
	
	public static String easyCategorizizingV2(String FacebookCategory){
		if(categorizingStringV2.toLowerCase().contains(FacebookCategory.toLowerCase())){
			return "Party";
		}else{
			return null;
		}
	}
	
	/**Returns CategoryList elements discriminator numbers*/
	public static HashMap<Long, FacebookPlaceTag> getCategoryListNumbers(){
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
	public static void putDiscriminatorNumbersIntoHashMap(HashMap<String, Integer> discriminatorNumbers, String Discriminator){
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
				Integer newNum = number+1;
				discriminatorNumbers.put(Discriminator, newNum);
			}else{
				System.out.println("ProblemInDiscNumberUpdate");
			}
		}
	}
	
	/**Add together the discriminator values*/
	public static HashMap<String, Integer> sumDiscriminatorValues(HashMap<String, Integer> oneImportant, HashMap<String, Integer> two){
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
	public static String getFacebookData(Long FacebookId, HashMap<Long, String> onePlaceCategoryList, AccessToken accessToken){
		Event event = null;
		Place place = null;
		String categoryString = null;
		JSONArray categoryList = null;
		try{
			FacebookClient facebookClient = new DefaultFacebookClient(accessToken.getAccessToken());
			String EventFacebookId = FacebookId.toString();
			try{
				event = facebookClient.fetchObject(EventFacebookId, Event.class, Parameter.with("metadata", 1));
				try{
					place = facebookClient.fetchObject(event.getVenue().getId(), Place.class, Parameter.with("metadata", 1));
					try{
						JSONObject category = null;
						JSONObject valami = readJsonFromUrl(place.getMetadata().getConnections().getTagged().toString());
						JSONArray tomb = (JSONArray) valami.get("data");
						System.out.println(tomb);
						try{
							JSONObject valami2 = (JSONObject) tomb.get(0);
							JSONObject valami3 = (JSONObject) valami2.get("to");
							JSONArray tomb2 = (JSONArray) valami3.get("data");
							category = (JSONObject) tomb2.get(0);
							categoryString = category.get("category").toString();
							categoryList = (JSONArray) category.get("category_list");
							for(int i=0; i< categoryList.size(); i++){
								JSONObject oneCategoryList = (JSONObject) categoryList.get(i);
								Long id = Long.parseLong((String) oneCategoryList.get("id"));
								String oneCategoryListName= (String) oneCategoryList.get("name");
								onePlaceCategoryList.put(id, oneCategoryListName);
							}
						}catch(IndexOutOfBoundsException e){
							System.out.println("dontHaveZeroArray");
						}
					}catch(NullPointerException | IOException | ParseException e){
						System.out.println("problemWithJson");
					}
				}catch(FacebookException | NullPointerException e){
					System.out.println("NoPlaceInfo");
				}
			}catch(FacebookException e){
				System.out.println("NoEventInfo");
			}
		}catch(FacebookOAuthException e){
//			System.out.println("ShouldGetNewToken");
			categoryString = "ShouldGetNewToken";			
		}
		return categoryString;
	}
	
	/**Returns facebook events places main category, and page checkin like numbers in FacebookPageNumbers object*/
	public StringLong getFacebookDataV2(Long FacebookId, Integer FunspotterId, AccessToken accessToken, HashMap<Long, FacebookPageNumbers> pageData){
		List<Long> notWellcomedPlaces = new ArrayList<Long>();
		notWellcomedPlaces.add(335553359801907L);	// place id we dont want to see categorized
		
		Event event = null;
		Page page = null;
		Place place = null;
		String categoryString = null;
		StringLong data = new StringLong();
		data.Words = null;
		try{
			FacebookClient facebookClient = new DefaultFacebookClient(accessToken.getAccessToken());
			String EventFacebookId = FacebookId.toString();
			try{
				event = facebookClient.fetchObject(EventFacebookId, Event.class, Parameter.with("metadata", 1));
				try{
					page  = facebookClient.fetchObject(event.getVenue().getId(), Page.class);
					place = facebookClient.fetchObject(event.getVenue().getId(), Place.class, Parameter.with("metadata", 1));
					System.out.println(place.toString());
					try{
						long likes = 0;
						int checkins = 0;
						try{
							likes = page.getLikes();
							checkins = page.getCheckins();
							try{
								Long pageId = Long.parseLong(page.getId());
								if(!notWellcomedPlaces.contains(pageId)){
									FacebookPageNumbers newData = new FacebookPageNumbers();
									newData.setFacebookEventId(FacebookId);
									newData.setChekinNumber(checkins);
									newData.setFunspotterEventId(FunspotterId);
									newData.setPageId(pageId);
									newData.setLikeNumber(likes);
									pageData.put(pageId, newData);
									JSONObject category = null;
									JSONObject valami = readJsonFromUrl(place.getMetadata().getConnections().getTagged().toString());
									JSONArray tomb = (JSONArray) valami.get("data");
									System.out.println(tomb);
									try{
										JSONObject valami2 = (JSONObject) tomb.get(0);
										JSONObject valami3 = (JSONObject) valami2.get("to");
										JSONArray tomb2 = (JSONArray) valami3.get("data");
										category = (JSONObject) tomb2.get(0);
										categoryString = category.get("category").toString();
										data.Number = pageId;
										data.Words = categoryString;
									}catch(IndexOutOfBoundsException e){
										System.out.println("IndexOutOfBounds");
									}
								}
							}catch(NullPointerException | NumberFormatException e){
								e.printStackTrace();
							}
						}catch(NullPointerException e){
							e.printStackTrace();
						}
					}catch(NullPointerException | IOException | ParseException e){
						System.out.println("problemWithJson");
					}
				}catch(FacebookException | NullPointerException e){
					System.out.println("NoPlaceInfo");
				}
			}catch(FacebookException e){
				System.out.println("NoEventInfo");
			}
		}catch(FacebookOAuthException e){
			System.out.println("ShouldHaveNewToken");
		}
		return data;
	}
	
	/**Event has the minimal properties, not null title, thumbnail, description*/
	public static boolean eventContentOk(HashMap<Integer, FunspotterEvent> eventInfo, Integer FunspotterId){
		boolean ok = false;
		if(eventInfo.containsKey(FunspotterId)){
			FunspotterEvent oneEvent = eventInfo.get(FunspotterId);
			try{
				String title = oneEvent.getTitle();
				String thumbnailUrl = oneEvent.getThumbnailUrl();
				String description = oneEvent.getDescription();
				if(title != null && thumbnailUrl != null && description != null){
					if(thumbnailUrl.length()>15 && description.length()>10){
						ok = true;
					}
				}
			}catch(NullPointerException e){
				return false;
			}
			
		}
		return ok;
	}
	
	/**Returns future events information, id; discriminator; thumbnail; ... see FacebookEvent class*/
	public static HashMap<Integer, FunspotterEvent> getFutureEventInfo(){
		HashMap<Integer, FunspotterEvent> eventInfo = new HashMap<Integer, FunspotterEvent>();
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			eventInfo = dbService.getFutureEventsInformation();
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
		return eventInfo;
	}
	
	/**Returns all events tag to watch witch events was categorized already.*/
	public static HashMap<Integer, List<String>> getAllEventsTag(){
		HashMap<Integer, List<String>> allEventsTag = null;
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			allEventsTag = dbService.getAllEventsTag();
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
		return allEventsTag;
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
	
	/**Update FacebookPages and EventFromFAcebook table with place  like and checkin number and pageid*/
	public static void uploadFacebookPageInformation(HashMap<Long, FacebookPageNumbers> pagInfos){
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			dbService.updateFacebookPageInformation(pagInfos);
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
	
	/**Upload all of the categoryList elements into table with the new discriminator numbers*/
	public static void uploadCategoryListElements(HashMap<Long, FacebookPlaceTag> CategoryListElements, HashMap<Long, FacebookPlaceTag> oldCategoryListElements){
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
	
	/**Insert one log information*/
	public static void insertLogInformation(String comment){
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
	
	public static void faszom(){
		HashMap<Long, String> onePlaceCategoryList = new HashMap<Long, String>();
		String MY_APP_SECRET = "add4434d3f3f754d29d567d59f285be5";
		String MY_APP_ID = "513927361994826";
		AccessToken accessToken = new DefaultFacebookClient().obtainAppAccessToken(MY_APP_ID, MY_APP_SECRET);
		FacebookClient facebookClient = new DefaultFacebookClient(accessToken.getAccessToken());
		int i=0;
		while(true){
			try {
				Thread.sleep(60000);
//				System.out.println("lefut "+i);
				i++;
				String categoryName = getFacebookData(18490L, onePlaceCategoryList, accessToken);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
	}
	
	public static void writeToFile(HashMap<Long, String> onePlaceCategoryList, String discriminator, String categoryName, Long FacebookId){
		BufferedWriter writer = null;
        try {
            //create a temporary file
            File logFile = new File("firsttry");

            // This will output the full path where the file will be written to...
//            System.out.println(logFile.getCanonicalPath());
            String listCategory = new String();
            for(Entry<Long, String>entry: onePlaceCategoryList.entrySet()){
            	listCategory=listCategory+", "+entry.getValue();
            }
            writer = new BufferedWriter(new FileWriter(logFile, true));
            String output = "Fb.Id: "+FacebookId+" Fb.placeCategory: "+categoryName+" Discriminator: "+discriminator+" Fb.placeCategory: "+listCategory;
//            System.out.println(output);
            writer.write(output);
            writer.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
	}
	
	public static void writeToFile2(HashMap<Long, FacebookPlaceTag> CategoryListNumbers){
		BufferedWriter writer = null;
        try {
            //create a temporary file
            File logFile = new File("categoryListNum");
            writer = new BufferedWriter(new FileWriter(logFile, true));
            int max = 0;
            String maxDisc = "semmi";
            int secondMax = 0;
            String secondDisc = "semmi";

            // This will output the full path where the file will be written to...
//            System.out.println(logFile.getCanonicalPath());
            for(Entry<Long, FacebookPlaceTag>entry: CategoryListNumbers.entrySet()){
            	max = 0;
                maxDisc = "semmi";
                secondMax = 0;
                secondDisc = "semmi";
            	Long PlaceId = entry.getKey();
            	FacebookPlaceTag place = entry.getValue();
            	String placeName = place.getName();
            	HashMap<String, Integer> discNum = place.getDiscriminatorNumber();
            	for(Entry<String, Integer>entry2: discNum.entrySet()){
            		String disc = entry2.getKey();
            		Integer num = entry2.getValue();
            		if(num > max){
						secondMax = max;
						max = num;
						secondDisc = maxDisc;
						maxDisc = disc;
					}else if(num > secondMax && num < max){
						secondMax = num;
						secondDisc = disc;
					}
            	}
            	String output = placeName+" "+maxDisc+": "+max+"   "+secondDisc+": "+secondMax;
            	writer.write(output);
                writer.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
	}
	
	/**Try to categorize all the necessary events*/
	public static void categorizing(){
		insertLogInformation("EventCategorizeStart");
		HashMap<Long, Integer> FacebookEventId = getFutureUncategorizedEventsIds();
		HashMap<Long, FacebookPlaceTag> CategoryListNumbers = getCategoryListNumbers();
		HashMap<Integer, FunspotterEvent> eventInfo = getFutureEventInfo();
		//Later problem, not just facebook events could have tags
		HashMap<Integer, List<String>> eventsTag = getAllEventsTag();
		HashMap<Long, FacebookPlaceTag> oldCategoryListNumbers = new HashMap<Long, FacebookPlaceTag>(CategoryListNumbers);
		HashMap<Long, String> onePlaceCategoryList = new HashMap<Long, String>();
		HashMap<Integer, String> newEventDiscriminator = new HashMap<Integer, String>();
		List<Long> nextSearchCycleEvents = new ArrayList<Long>();	// collect the not obvious events with discriminator
		List<Integer> noInfoFromFacebook = new ArrayList<Integer>();
		int noDiscriminator = 0;
		int noPlaceCategory = 0;
		int notCategorizedEvents = 0;
		int sumUncatEventNum = FacebookEventId.size();
		String MY_APP_SECRET = "add4434d3f3f754d29d567d59f285be5";
		String MY_APP_ID = "513927361994826";
		AccessToken accessToken = new DefaultFacebookClient().obtainAppAccessToken(MY_APP_ID, MY_APP_SECRET);
		FacebookClient facebookClient = new DefaultFacebookClient(accessToken.getAccessToken());
		for(Entry<Long, Integer>entry: FacebookEventId.entrySet()){
			Long FacebookId = entry.getKey();
			Integer FunspotterId = entry.getValue();
			onePlaceCategoryList.clear();
			String categoryName = getFacebookData(FacebookId, onePlaceCategoryList, accessToken);
			try{
				if(categoryName.equals("ShouldGetNewToken")){
					accessToken = new DefaultFacebookClient().obtainAppAccessToken(MY_APP_ID, MY_APP_SECRET);
					facebookClient = new DefaultFacebookClient(accessToken.getAccessToken());
					categoryName = getFacebookData(FacebookId, onePlaceCategoryList, accessToken);
				}
			}catch(NullPointerException e){
				System.out.println("categoryNameWasNull");
			}
			if(categoryName!=null){	// got info from facebook
				String discriminator = easyCategorizing(categoryName);
				if(discriminator!=null){	// no json problem
					for(Entry<Long, String>entry2: onePlaceCategoryList.entrySet()){
						FacebookPlaceTag discriminatorNumbers = null;
						Long oneCategoryListId = entry2.getKey();
						String oneCategoryListName = entry2.getValue();
						if(CategoryListNumbers.containsKey(oneCategoryListId)){ // new category is in the database
							discriminatorNumbers = CategoryListNumbers.get(oneCategoryListId);
							HashMap<String, Integer> dbDiscNum = discriminatorNumbers.getDiscriminatorNumber();
							putDiscriminatorNumbersIntoHashMap(dbDiscNum, discriminator);
							discriminatorNumbers.setDiscriminatorNumber(dbDiscNum);
							CategoryListNumbers.put(oneCategoryListId, discriminatorNumbers);
						}else{		//new category isnt in the database
							HashMap<String, Integer> discNumtoFacePlaceTag = new HashMap<String, Integer>();
							putDiscriminatorNumbersIntoHashMap(discNumtoFacePlaceTag, discriminator);
							discriminatorNumbers = new FacebookPlaceTag();
							discriminatorNumbers.setId(oneCategoryListId);
							discriminatorNumbers.setName(oneCategoryListName);
							discriminatorNumbers.setDiscriminatorNumber(discNumtoFacePlaceTag);
							CategoryListNumbers.put(oneCategoryListId, discriminatorNumbers);
						}
					}
//					insertLogInformation("EventCategorized: "+FunspotterId+" disc: "+discriminator);
//					writeToFile(onePlaceCategoryList, discriminator, categoryName, FacebookId);
					if(eventContentOk(eventInfo, FunspotterId)){
						newEventDiscriminator.put(FunspotterId, discriminator);
					}
				}else{
					noDiscriminator++;
					if(eventContentOk(eventInfo, FunspotterId)){
						nextSearchCycleEvents.add(FacebookId);
					}
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
		
		
		insertLogInformation("EventCategorizationUploadDataStart");
//		writeToFile2(CategoryListNumbers);
		uploadEventDiscriminators(newEventDiscriminator,null);
		uploadCategoryListElements(CategoryListNumbers,oldCategoryListNumbers);
		insertLogInformation("EventCategorizationUploadDataEnd");
		newEventDiscriminator.clear();
		
		accessToken = new DefaultFacebookClient().obtainAppAccessToken(MY_APP_ID, MY_APP_SECRET);
		facebookClient = new DefaultFacebookClient(accessToken.getAccessToken());
		
		HashMap<String, Integer> eventDiscriminatorNumber = new HashMap<String, Integer>();
		for(Entry<Long, Integer>entry: FacebookEventId.entrySet()){
			Long FacebookId = entry.getKey();
			Integer FunspotterId = entry.getValue();
			if(nextSearchCycleEvents.contains(FacebookId)){
				eventDiscriminatorNumber.clear();
				String categoryName = getFacebookData(FacebookId, onePlaceCategoryList, accessToken);
				try{
					if(categoryName.equals("ShouldGetNewToken")){
						accessToken = new DefaultFacebookClient().obtainAppAccessToken(MY_APP_ID, MY_APP_SECRET);
						facebookClient = new DefaultFacebookClient(accessToken.getAccessToken());
						categoryName = getFacebookData(FacebookId, onePlaceCategoryList, accessToken);
					}
				}catch(NullPointerException e){
//					System.out.println("categoryNameWasNull");
				}
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
				int secondMax = 0;
				String secondMaxDisc = null;
				for(Entry<String, Integer>entry1: eventDiscriminatorNumber.entrySet()){
					Integer number = entry1.getValue();
					String discriminator = entry1.getKey();
					if(number > max){
						secondMax = max;
						max = number;
						secondMaxDisc = maxDisc;
						maxDisc = discriminator;
					}else if(number > secondMax && number < max){
						secondMax = number;
						secondMaxDisc = discriminator;
					}
				}
				if(max != 0 && maxDisc!=null){
					if(secondMax != 0){
						if(((double)max/(double)secondMax) >= 2.0){
							newEventDiscriminator.put(FunspotterId, maxDisc);
						}
					}else{
						newEventDiscriminator.put(FunspotterId, maxDisc);
					}
//					insertLogInformation("EventCategorized: "+FunspotterId+" disc: "+maxDisc);
				}else{
					notCategorizedEvents++;
				}
			}
		}
		insertLogInformation("PercentageCounting");
		Double plusPercent = 0.0;
		if(sumUncatEventNum != 0){
			plusPercent = ((double)(sumUncatEventNum-notCategorizedEvents)/(double)sumUncatEventNum)*100;
		}else{
			plusPercent = 0.0;
		}
		DecimalFormat df = new DecimalFormat("##");
		System.out.println("EventCategorizeEnd +: "+df.format(plusPercent)+"% startUncat: "+sumUncatEventNum);
		insertLogInformation("EventCategorizationEventUploadStart");
		uploadEventDiscriminators(newEventDiscriminator,noInfoFromFacebook);
		insertLogInformation("EventCategorizeEnd +: "+df.format(plusPercent)+"% startUncat: "+sumUncatEventNum);
	}

	/**2nd version categorization based on main facebook place category: Concert Venue and Club*/
	public void categorizingV2(){
//		insertLogInformation("EventCategorizeStart");
		HashMap<Long, Integer> FacebookEventId = getFutureFacebookEventsIds();
		HashMap<Integer, FunspotterEvent> eventInfo = getFutureEventInfo();
		HashMap<Integer, String> newEventDiscriminator = new HashMap<Integer, String>();
		HashMap<Long, FacebookPageNumbers> pageData = new HashMap<Long, FacebookPageNumbers>();	// contains facebook page info
		/*Facebook Token*/
		String MY_APP_SECRET = "add4434d3f3f754d29d567d59f285be5";
		String MY_APP_ID = "513927361994826";
		AccessToken accessToken = new DefaultFacebookClient().obtainAppAccessToken(MY_APP_ID, MY_APP_SECRET);
		FacebookClient facebookClient = new DefaultFacebookClient(accessToken.getAccessToken());
		Long pageId = 0L;
		/*--------------*/
		for(Entry<Long, Integer>entry: FacebookEventId.entrySet()){
			Long FacebookId = entry.getKey();
			Integer FunspotterId = entry.getValue();
			StringLong data = getFacebookDataV2(FacebookId, FunspotterId, accessToken, pageData);
			if(data.Words != null){
				pageId = data.Number;
				Long sumNumber = pageData.get(pageId).getSumLikeAndCheckin();
				String easyCategory = easyCategorizizingV2(data.Words);
				if(easyCategory != null){
					if(sumNumber > MIN_NUMBER){
						if(eventContentOk(eventInfo, FunspotterId)){
							newEventDiscriminator.put(FunspotterId, "Party");
						}
					}
				}else{
					if(eventInfo.containsKey(FunspotterId)){
						FunspotterEvent oneEvent = eventInfo.get(FunspotterId);
						String Discriminator = "simple";
						try{
							Discriminator = oneEvent.getDiscriminator();
						}catch(NullPointerException e){
							e.printStackTrace();
						}
						if(!Discriminator.equals("Party")){
							if(pageData.containsKey(FunspotterId)){
								pageData.remove(FunspotterId);
							}
						}
					}
				}
			}else{
				if(pageData.containsKey(FunspotterId)){
					pageData.remove(FunspotterId);
				}
			}
		}
		insertLogInformation("CategorizeDiscriminatorUpload");
		uploadEventDiscriminators(newEventDiscriminator, null);
		insertLogInformation("CategorizePageDataUpload");
		uploadFacebookPageInformation(pageData);
		insertLogInformation("EventCategorizeEnd"+" Done: "+newEventDiscriminator.size()+"From: "+FacebookEventId.size());
	}
	
	private static JSONObject readJsonFromUrl(String url) throws IOException, ParseException {
		InputStream is = new URL(url).openStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
		String jsonText = readAll(rd);
		JSONObject json = null;
		try {
			JSONParser parser=new JSONParser();
			json = new JSONObject();
			Object obj=parser.parse(jsonText);
			json=(JSONObject)obj;
		}finally {
			is.close();
		}
		return json;
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
