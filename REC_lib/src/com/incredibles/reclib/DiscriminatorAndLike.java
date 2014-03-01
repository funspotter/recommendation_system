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
import com.incredibles.storage.ClientDbService;
import com.incredibles.storage.ClientDbServiceCreator;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator; 

/**Refaktoring idea: read everything from database once; not every user.. feature */
public class DiscriminatorAndLike implements FiltersForRec {

	
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
	@Override
	public Integer getUserIdFromFbId(long fbId) {
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
	
	/**Returns all like for Facebook user*/
	@Override
	public List<Like> getAllLikeForUser(long fbId) {
		ClientDbService dbService = null;
		List<Like> likes = null;
		try {
			dbService = ClientDbServiceCreator.createCloud();
			likes = dbService.getAllLikeForFbUserId(fbId);
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

	/**Return events with legit show from date*/
	public List<Integer> getEventsFromDate(long date) {
		RecommenderDbService dbService = null;
		List<Integer> events = new ArrayList<Integer>();
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			events = dbService.eventsInShowList(1); // set date in clouddbmanager
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
			eventDiscriminator = dbService.getEventDiscriminatorFromDate(date);
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
	public static HashMap<Integer,String> getEventsGenreFromDate(long date){
		HashMap<Integer,String> eventGenre = new HashMap<Integer,String>();
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			eventGenre = dbService.getEventsGenreFromDate(date);
			
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
	@Override
	public Integer getUserBirtdate(long fbId) {
		RecommenderDbService dbService = null;
		Integer Date = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			Date = dbService.getUserBirthDate(fbId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
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
		    sum += csopCount;			
		}
		for (Entry<String, Integer> entry : discNumber.entrySet()) {
			String discriminator = entry.getKey();
			Integer discCount = entry.getValue();
			if(discCount != 0 && sum!= 0){
				rank = (double)discCount/(double)sum;
			}else if(sum!=0 && discCount == 0){
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
			counter = counter + 1;						// Not new discriminator +1;
			discNumber.put(category, counter);
		} else {
			discNumber.put(category, 1);				// new disc =1;
		}
	}
	
	/**Like analisator ; create discriminator ranks*/
	public static List<HashMap<String,Double>> convertLiketoNumber(long fbId){
		List<Like> likes = null;
		List<HashMap<String,Double>> hashmaps = new ArrayList<HashMap<String,Double>>();
		HashMap<String, Integer> discNumber = new HashMap<String, Integer>();
		HashMap<String, Double> discRank = new HashMap<String, Double>();
		
		/* Analise smaller set of facebook likes type in discriminators */
		HashMap<String,Double> allCinemaLikeGenre = new HashMap<String,Double>();	/* Cinema: movies genre; */
		HashMap<String,Double> allMusicLikeGenre = new HashMap<String, Double>();   /* Music: artist genre; */
		
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
		
		DiscriminatorAndLike startObject = new DiscriminatorAndLike();
		likes = startObject.getAllLikeForUser(fbId);
		
		
		/* Analise likes */
		/* Create new function if have a way to get apart discriminators -> movie : sci-fi;action;..*/
		for(int i=0; i<likes.size(); i++){
			Like oneLike = likes.get(i);
			String fbCategory = oneLike.getCategory().toLowerCase();
			if(startObject.cinema.contains(fbCategory)){
				likeCountPlusPlus(discNumber,"cinema");
				allCinemaLikeGenre = Discriminators.getAllCinemaLikeGenre(oneLike,allCinemaLikeGenre);	/*getAllCinemaLikeGenre*/
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
				Discriminators.getAllMusicLikeGenre(oneLike, allMusicLikeGenre);	/*getAllMusicLikeGenre*/
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
		
		/* Discriminator number to discriminator ranks */
		discRank = convertDiscNumbToDiscRank(discNumber);
		hashmaps.add(0, discRank);						/*easier to return hashmaps in one list that is why*/
		hashmaps.add(1, allCinemaLikeGenre);			/* string & double */
		hashmaps.add(2, allMusicLikeGenre);		
		return hashmaps;
	}

	/**Call this function to run this Recommendation filter*/
	@Override
	public HashMap<Integer, Double> setEventsRank(long fbId) {
		List<HashMap<String,Double>> hashmaps = convertLiketoNumber(fbId);
		HashMap <String,Double> discRank = hashmaps.get(0);					/*the order is very important !*/
		HashMap <String,Double> allCinemaLikeGenre = hashmaps.get(1);
		HashMap <String,Double> allMusicLikeGenre = hashmaps.get(2);
		//Integer userId = getUserIdFromFbId(fbId);
		Integer Date = getCurrentDate();
		Date nowDate = new Date();
		List <Integer> eventList = getEventsFromDate(Date);	// dont need to give date. handeled in cloudbmanager
		HashMap<Integer,String> eventDiscriminator = getEventsDiscriminatorFromDate(Date);	// TODO: hozzaadni az idointervallumot
		HashMap<Integer,String> eventGenre = getEventsGenreFromDate(Date);	// TODO: hozzaadni az idointervallumot
		Integer ageInYears = getUserBirtdate(fbId); /*users age*/
		
		HashMap<Integer, Double> eventsRank = new HashMap<Integer, Double>();
		LinkedHashMap<Integer, Double> eventsRankSorted = new LinkedHashMap<Integer, Double>();
		
		for(int i=0; i<eventList.size(); i++){
			Integer eventId = eventList.get(i);
			String discriminator = eventDiscriminator.get(eventId).toLowerCase();
			Double rank = null;						/*Default discriminator rank value*/
			Double ujrank = null;					/*Newly calculated rank value*/	
			
			/* TODO: new discriminator need new else part + other things :) */
			if(discriminator.equals("cinema")){
				rank = discRank.get(discriminator);
				ujrank = Discriminators.helpCinemaGenreRanking(eventGenre,rank,eventId,allCinemaLikeGenre);		/*Movies genre observation*/
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
				ujrank = Discriminators.helpKidAgeRanking(ageInYears, rank, maximumValueInHashmap(discRank));		/*Kid age filtering*/
			}else if(discriminator.equals("music")){
				rank = discRank.get(discriminator);
				ujrank = Discriminators.helpMusicGenreRanking(eventGenre,rank,eventId,allMusicLikeGenre);		/*Music genre observation*/
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
				ujrank = Discriminators.helpPartyAgeRanking(ageInYears, rank, maximumValueInHashmap(discRank));  /*Party age filtering*/
			}else if(discriminator.equals("other")){
				rank = discRank.get(discriminator);
				ujrank = rank + (Math.random()/100);	/*Default setting*/
			}else if(discriminator.equals("simple")){
				ujrank = (double) 0;					/*Not categorised yet in database*/
			}else{
				ujrank = (double) 0;					/*Other discriminator problem*/
			}
			
			/*New database system needs to upload event-rank pairs in Linked HashMap*/
			eventsRank.put(eventId, ujrank);
			System.out.println(" EventId:" +eventId+ " Discriminator:" +discriminator+ " Rank:" +ujrank);
		}
		//eventsRankSorted = sortByValue(eventsRank);
		
		for(Entry<Integer,Double> entry : eventsRankSorted.entrySet()){
			System.out.println(entry.getKey()+" es "+ entry.getValue());
		}
		//uploader(ujrank, eventId, userId);			/*Upload calculated rank to database*/
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
