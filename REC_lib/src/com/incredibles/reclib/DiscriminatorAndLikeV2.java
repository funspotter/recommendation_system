package com.incredibles.reclib;

import java.io.IOException;
import java.sql.SQLException;
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

import com.incredibles.data.Like;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator; 
import com.incredibles.data.MetadataFromThirdParty;

/**Refaktoring idea: read everything from database once; not every user.. feature */
public class DiscriminatorAndLikeV2{

	
	/* Facebook like types categorization into discriminators */
    String cinema = "movie,movie general,movie genre,actor/director,movie character,tv show";
	String exhibition = "artist,arts/entertainment/nightlife,arts/humanities website,attractions/things to do,museum/art gallery";
	String festival = "album,club,concert venue,event planning/event services,musician/band,concert tour,concert venue";
	String gastro = "bar,club,food/grocery,restaurant/cafe,food/beverages,chef";
	String kid = "school,board game,games/toys,video game";
	String music = "album,artist,musician/band,concert tour,concert venue,radio station,song";
	String sport = "outdoor gear/sporting goods,sports venue,sports/recreation/activities,tours/sightseeing,amateur sports team,professional sports team,school sports team,sports league,sports venue";
	String theater = "museum/art gallery,comedian";
	String travel = "airport,landmark,spas/beauty/personal care,tours/sightseeing,transport,public places,food/beverages,transport/freight,travel/leisure,concert tour";
	String party = "club,pub/bar,school,university,drugs,musician/band";
	String other = "";
	
	/**UserId <--- FacebookId converter.*/
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
	
	/**Returns all like for UserId*/
	public List<Like> getAllLikeForUser(int UserId) {
		RecommenderDbService dbService = null;
		List<Like> likes = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			likes = dbService.getAllLikeForUserId(UserId);
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

	/**Return all ourslikeid in imdbtags TABLE*/
	public HashMap<Long, MetadataFromThirdParty> getAllOurLikeFromImdbTags(){
		RecommenderDbService dbService = null;
		HashMap<Long, MetadataFromThirdParty> ourLikeIds= null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			ourLikeIds = dbService.getInfoFromImdbTags();
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
		return ourLikeIds;
	}
	
	/**Return all ourslikeid in lastfmtag TABLE*/
	public HashMap<Long, MetadataFromThirdParty> getAllOurLikeFromLastfmTags(){
		RecommenderDbService dbService = null;
		HashMap<Long, MetadataFromThirdParty> ourLikeIds= null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			ourLikeIds = dbService.getInfoFromLastfmTags();
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
		return ourLikeIds;
	}
	
	/**Upload tags for new like into imdb table*/
	public static void uploadAllNewLikeTagImdb(HashMap<Long, MetadataFromThirdParty> newLikeTags){
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			dbService.uploadInfoToImdbTags(newLikeTags);
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
	
	/**Upload tags for new like into lastfm table*/
	public static void uploadAllNewLikeTagLastfm(HashMap<Long, MetadataFromThirdParty> newLikeTags){
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			dbService.uploadInfoToLastfmTags(newLikeTags);
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
	
	/**Upload user discriminator rank into usersdiscrank table*/
	public static void uploadUserDiscriminatorRank(HashMap<Integer, HashMap<String, Double>> discriminatorRanks){
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			dbService.uploadUserDiscriminatorRank(discriminatorRanks);
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
	
	/**Return events with legit show from date*/
	public List<Integer> getEventsFromDate(long nowDate) {
		RecommenderDbService dbService = null;
		List<Integer> events = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			events = dbService.getLegitEventsIdFromDate(nowDate);
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

	/**Return existing events discriminator*/
	public static HashMap<Integer,String> getEventsDiscriminatorFromDate(long date){
		HashMap<Integer,String> eventDiscriminator = new HashMap<Integer,String>();
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			eventDiscriminator = dbService.getEventDiscriminatorFromDateV2(date);
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
		return eventDiscriminator;
	}
	
	/**Return all events Genre ---> TODO: update in the database the genre column*/
	public static HashMap<Integer,List<String>> getEventsGenreFromDate(long date){
		HashMap<Integer,List<String>> eventGenre = new HashMap<Integer,List<String>>();
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			eventGenre = dbService.getEventsTagFromDateV2(date);
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
		return eventGenre;
	}

	/**Return Users-s birthdate*/
	public Integer getUserBirtdate(int UserId) {
		RecommenderDbService dbService = null;
		Integer Date = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			Date = dbService.getUserAge(UserId);
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}finally {
			if (dbService != null) {
				try {
					dbService.close();
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		return Date;		
	}

	/**Return current date*/
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
	
	/** Return current year*/
	public static Integer getCurrentYear(){
		long timestamp = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		Integer year = cal.get(Calendar.YEAR);
		return year;
	}
		
	/** Discriminator number to discriminator ranks*/
	public static HashMap<String,Double> convertDiscNumbToDiscRank(HashMap <String,Integer> discNumber){
		HashMap<String,Double> discRanks = new HashMap<String,Double>();
		Integer sum= 0;
		Double rank=null;
		/*Counting elements of discriminators number*/
		for (Entry<String, Integer> entry : discNumber.entrySet()) {
			Integer csopCount = entry.getValue();
			sum = sum + csopCount;		
		}
		for (Entry<String, Integer> entry : discNumber.entrySet()) {
			String discriminator = entry.getKey();
			Integer discCount = entry.getValue();
			if(!discCount.equals(0) && !sum.equals(0)){
				rank = (double)discCount/(double)sum;
			}else if(!sum.equals(0) && discCount.equals(0)){
				rank = 1.0/(double)sum;
			}else{
				rank = (double) 0;
			}
			discRanks.put(discriminator, rank);
			}	
			return discRanks;
		}
	
	/** Counting likes in discriminators*/
	public static void likeCountPlusPlus(HashMap<String, Integer> discNumber, String category) {
		if (discNumber.containsKey(category)) {
			Integer counter = discNumber.get(category);
			Integer newCounter = counter + 1;						// Not new discriminator +1;
			discNumber.put(category, newCounter);
		} else {
			discNumber.put(category, 1);				// new disc =1;
		}
	}
	
	/**Like analisator; create discriminator ranks*/
	public static List<HashMap<String,Double>> convertLiketoNumber(int UserId){
		List<Like> likes = null;
		boolean startFlagSearchImdb = true;					// different flags for different tag tables
		boolean startFlagSearchLastfm = true;
		List<HashMap<String,Double>> hashmaps = new ArrayList<HashMap<String,Double>>();
		HashMap<String, Integer> discNumber = new HashMap<String, Integer>();
		HashMap<String, Double> discRank = new HashMap<String, Double>();
		
		/* Analise smaller set of facebook likes type in discriminators */
		HashMap<String,Double> allCinemaLikeGenre = new HashMap<String,Double>();	/* Cinema: movies genre; */
		HashMap<String,Double> allMusicLikeGenre = new HashMap<String, Double>();   /* Music: artist genre; */
			
		HashMap<Long, MetadataFromThirdParty> imdbLikeTags = null;	// download
		HashMap<Long, MetadataFromThirdParty> lastfmLikeTags = null;	//download
		HashMap<Long, MetadataFromThirdParty> newTagsforUploadImdb = new  HashMap<Long, MetadataFromThirdParty>();	//upload
		HashMap<Long, MetadataFromThirdParty> newTagsforUploadLastfm = new  HashMap<Long, MetadataFromThirdParty>(); //upload
		MetadataFromThirdParty imdbTags = null;
		MetadataFromThirdParty lastfmTags = null;
		
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
		
		DiscriminatorAndLikeV2 startObject = new DiscriminatorAndLikeV2();
		likes = startObject.getAllLikeForUser(UserId);
		
		imdbLikeTags = startObject.getAllOurLikeFromImdbTags();
		lastfmLikeTags = startObject.getAllOurLikeFromLastfmTags();
		
		/* Analise likes */
		/* Create new function if have a way to get apart discriminators -> movie : sci-fi;action;..*/
		for(int i=0; i<likes.size(); i++){
			startFlagSearchLastfm = true;
			startFlagSearchImdb = true;
			Like oneLike = likes.get(i);
			String name = oneLike.getName();
			long ourLikeId = oneLike.getId();
			System.out.println("Like:"+ name);
			if(imdbLikeTags.containsKey(ourLikeId)){
				imdbTags = imdbLikeTags.get(ourLikeId);
				startFlagSearchImdb=false;
			}
			if(lastfmLikeTags.containsKey(ourLikeId)){
				lastfmTags = lastfmLikeTags.get(ourLikeId);
				startFlagSearchLastfm=false;
			}
			String fbCategory = oneLike.getCategory().toLowerCase();
			if(startObject.cinema.contains(fbCategory)){
				likeCountPlusPlus(discNumber,"cinema");
				if(startFlagSearchImdb==true){
					allCinemaLikeGenre = DiscriminatorsV2.getAllCinemaLikeGenre(oneLike,allCinemaLikeGenre,newTagsforUploadImdb);	/*getAllCinemaLikeGenre*/
				}else{
					List<String> tagArray = imdbTags.getTagArray();
					for(int j=0; j < tagArray.size(); j++){
						String oneTag = tagArray.get(j).toLowerCase();
						if(allCinemaLikeGenre.containsKey(oneTag)){
							double genreNumber = allCinemaLikeGenre.get(oneTag);
							double newNumber = genreNumber+1;
							allCinemaLikeGenre.put(oneTag, newNumber);
						}else{
							allCinemaLikeGenre.put(oneTag, 1.0);
						}
					}
				}
			}if(startObject.exhibition.contains(fbCategory)){
				likeCountPlusPlus(discNumber,"exhibition");
				/*TODO*/
			}if(startObject.festival.contains(fbCategory)){
				likeCountPlusPlus(discNumber,"festival");
				/*TODO*/
			}if(startObject.gastro.contains(fbCategory)){
				likeCountPlusPlus(discNumber,"gastro");
				/*TODO*/
			}if(startObject.kid.contains(fbCategory)){
				likeCountPlusPlus(discNumber,"kid");
				/*TODO*/
			}if(startObject.music.contains(fbCategory)){
				likeCountPlusPlus(discNumber,"music");
				if(startFlagSearchLastfm == true){
					DiscriminatorsV2.getAllMusicLikeGenre(oneLike, allMusicLikeGenre, newTagsforUploadLastfm);	/*getAllMusicLikeGenre*/
				}else{
					List<String> tagArray = lastfmTags.getTagArray();
					for(int j=0; j < tagArray.size(); j++){
						String oneTag = tagArray.get(j).toLowerCase();
						if(allMusicLikeGenre.containsKey(oneTag)){
							double genreNumber = allMusicLikeGenre.get(oneTag);
							double newNumber = genreNumber+1;
							allMusicLikeGenre.put(oneTag, newNumber);
						}else{
							allMusicLikeGenre.put(oneTag, 1.0);
						}
					}
				}
			}else if(startObject.sport.contains(fbCategory)){
				likeCountPlusPlus(discNumber,"sport");
				/*TODO*/
			}if(startObject.theater.contains(fbCategory)){
				likeCountPlusPlus(discNumber,"theater");
				/*TODO*/
			}if(startObject.travel.contains(fbCategory)){
				likeCountPlusPlus(discNumber,"travel");
				/*TODO*/
			}if(startObject.other.contains(fbCategory)){
				likeCountPlusPlus(discNumber,"other");
				/*TODO*/
			}if(startObject.party.contains(fbCategory)){
				likeCountPlusPlus(discNumber,"party");
				/*TODO*/
			}
		}
		
		/*upload in new thread the tags*/
		final HashMap<Long, MetadataFromThirdParty> newTagsforUploadImdbFinal = new HashMap<Long, MetadataFromThirdParty>(newTagsforUploadImdb);
		final HashMap<Long, MetadataFromThirdParty> newTagsforUploadLastfmFinal = new HashMap<Long, MetadataFromThirdParty>(newTagsforUploadLastfm);
	 	Runnable r = new Runnable(){
			@Override
			public void run() {
				if(!newTagsforUploadImdbFinal.isEmpty()){
					uploadAllNewLikeTagImdb(newTagsforUploadImdbFinal);
				}
				if(!newTagsforUploadLastfmFinal.isEmpty()){
					uploadAllNewLikeTagLastfm(newTagsforUploadLastfmFinal);
				}
			}
		};
		Thread t = new Thread(r);
		t.start();
		
		/* Discriminator number to discriminator ranks */
		discRank = convertDiscNumbToDiscRank(discNumber);
		hashmaps.add(0, discRank);						/*easier to return hashmaps in one list that is why*/
		hashmaps.add(1, allCinemaLikeGenre);			/* string & double */
		hashmaps.add(2, allMusicLikeGenre);		
		return hashmaps;
	}

	/**Call this function to run this Recommendation filter*/
	public HashMap<Integer, Double> setEventsRank(int UserId) {
		List<HashMap<String,Double>> hashmaps = convertLiketoNumber(UserId);
		HashMap <String,Double> discRank = hashmaps.get(0);					/*the order is very important !*/
		HashMap <String,Double> allCinemaLikeGenre = hashmaps.get(1);
		HashMap <String,Double> allMusicLikeGenre = hashmaps.get(2);
		Date nowDate = new Date();
		List <Integer> eventList = getEventsFromDate(nowDate.getTime());
		HashMap<Integer,String> eventDiscriminator = getEventsDiscriminatorFromDate(nowDate.getTime());
		HashMap<Integer,List<String>> eventGenre = new HashMap<Integer, List<String>>();//getEventsGenreFromDate(nowDate.getTime());
		Integer ageInYears = getUserBirtdate(UserId); /*users age*/
		
		HashMap<Integer, Double> eventsRank = new HashMap<Integer, Double>();
		LinkedHashMap<Integer, Double> eventsRankSorted = new LinkedHashMap<Integer, Double>();
		
		HashMap<Integer, HashMap<String, Double>> userDiscriminatorRanks = new HashMap<Integer, HashMap<String, Double>>();
		userDiscriminatorRanks.put(UserId, discRank);
		System.out.println("discriminatorrank feltoltes ---------"+UserId);
		uploadUserDiscriminatorRank(userDiscriminatorRanks);
		
		for(int i=0; i<eventList.size(); i++){
			boolean legitEvent = true;
			Integer eventId = eventList.get(i);
			String discriminator = "makiverem";
			String valami = eventDiscriminator.get(eventId);
			if(valami!=null){
				discriminator = valami.toLowerCase();
			}
			Double rank = null;						/*Default discriminator rank value*/
			Double ujrank = null;					/*Newly calculated rank value*/			
			/* TODO: new discriminator need new else part + other things :) */
			if(discriminator.equals("cinema")){
				rank = discRank.get(discriminator);
				ujrank = DiscriminatorsV2.helpCinemaGenreRanking(eventGenre,rank,eventId,allCinemaLikeGenre);		/*Movies genre observation*/
			}else if(discriminator.equals("exhibition")){
				rank = discRank.get(discriminator);
				ujrank = rank + (Math.random()/100); 	/*Default setting*/
			}else if(discriminator.equals("festival")){
				rank = discRank.get(discriminator);
				ujrank = rank + (Math.random()/100);	/*Default setting*/
			}else if(discriminator.equals("gastro")){
				rank = discRank.get(discriminator);
				ujrank = rank + (Math.random()/100);	/*Default setting*/
			}else if(discriminator.equals("kid")){
				rank = discRank.get(discriminator);
				ujrank = DiscriminatorsV2.helpKidAgeRanking(ageInYears, rank, maximumValueInHashmap(discRank));		/*Kid age filtering*/
			}else if(discriminator.equals("music")){
				rank = discRank.get(discriminator);
				ujrank = DiscriminatorsV2.helpMusicGenreRanking(eventGenre,rank,eventId,allMusicLikeGenre);		/*Music genre observation*/
			}else if(discriminator.equals("sport")){
				rank = discRank.get(discriminator);
				ujrank = rank + (Math.random()/100);	/*Default setting*/
			}else if(discriminator.equals("theater")){
				rank = discRank.get(discriminator);
				ujrank = rank + (Math.random()/100);	/*Default setting*/
			}else if(discriminator.equals("travel")){
				rank = discRank.get(discriminator);
				ujrank = rank + (Math.random()/100);	/*Default setting*/
			}else if(discriminator.equals("party")){
				rank = discRank.get(discriminator);
				ujrank = DiscriminatorsV2.helpPartyAgeRanking(ageInYears, rank, maximumValueInHashmap(discRank));  /*Party age filtering*/
			}else if(discriminator.equals("other")){
				rank = discRank.get(discriminator);
				ujrank = rank + (Math.random()/100);	/*Default setting*/
			}else if(discriminator.equals("simple")){
				ujrank = (double) 0;					/*Not categorised yet in database*/
				legitEvent = false;
			}else{
				ujrank = (double) 0;					/*Other discriminator problem*/
				legitEvent = false;
			}
			
			/*New database system needs to upload event-rank pairs in Linked HashMap*/
			if(legitEvent == true){
				eventsRank.put(eventId, ujrank);
				System.out.println(" EventId:" +eventId+ " Discriminator:" +discriminator+ " Rank:" +ujrank);
			}
		}	
		System.out.println("V�GZETT AZ EVENTEKKEL");
		return eventsRank;
	}

	/**Nomen est omen. Needed because of the new database structure*/
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

	/**Maximum value in <String, Double> hashmap*/
	public static Double maximumValueInHashmap(HashMap<String, Double> hm){
		Double max = 0.0;
		for(Entry<String, Double> entry : hm.entrySet()){
			Double value = entry.getValue();
			if(value>max){
				max=value;
			}
		}
		return max;
	}
	
	/**Calculate Average value in a string,double hashmap*/
	public static Double averageValueInHashmap(HashMap<String, Double> hm){
		double average = 0.0;
		int nevezo = 1;
		for(Entry<String, Double> entry : hm.entrySet()){
			Double value = entry.getValue();
			average += value;
			if(value != 0.0){
				nevezo++;
			}
		}
		average = average / (double)nevezo;
		return average;
	}

	
	
}
