package com.incredibles.reclib;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.incredibles.storage.ClientDbService;
import com.incredibles.storage.ClientDbServiceCreator;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;


public class GenreWeighting {

	final static int CURRENT_YEAR = 2013;
	
	public static void rankSet(HashMap<String, Integer> movieGenre, HashMap<String, Integer> artistGenre, HashMap<String, Double> csopRank, long fbuserid){
		HashMap<Integer, Double> eventIdRank = new HashMap<Integer, Double>();
		RecommenderDbService dbService = null;
		List<Integer> eventList = null;
		HashMap<Integer, String> eventDiscriminator = null;
		HashMap<Integer, String> eventGenre = null;
		String genre = null;
		int date = GenreWeighting.getCurrentDate();
		try {
			double averageRank = GenreWeighting.averageValueInHashmap(csopRank);
			eventList = new ArrayList<Integer>();
			eventDiscriminator = new HashMap<Integer, String>();
			dbService = RecommenderDbServiceCreator.createCloud();
			eventList = dbService.eventsInShowList(date);
//		 	eventDiscriminator = dbService.getAllEventDiscriminator();
//		 	eventGenre = dbService.getAllEventGenre();
		 	int userid = dbService.getUserIDforFbUserID(fbuserid);	// ez nem kene ha atirja valaki a rankfeltoltot
		 	Integer birthDate = dbService.getUserBirthDate(fbuserid);
		 	int maxMovieGenreNum = GenreWeighting.maximumValueInHashmap(movieGenre); // Integer - int
		 	int maxArtistGenreNum = GenreWeighting.maximumValueInHashmap(artistGenre);
		 	for(int i=0; i<eventList.size(); i++){
		 		Double rank = null;
		 		Double ujrank = null;
		 		String discriminator = eventDiscriminator.get(eventList.get(i)).toLowerCase();
		 		if(!discriminator.equals("cinema") && !discriminator.equals("music") && !discriminator.equals("simple") && !discriminator.equals("party") && !discriminator.equals("kid")){
		 			rank = csopRank.get(discriminator);
					ujrank = rank + (Math.random()/100);	// ha itt van hiba, akkor Dobule;double k�z�tti valtas lehet a baj
		 		}else if(discriminator.equals("cinema")){
		 			rank = csopRank.get(discriminator);
		 			genre = eventGenre.get(eventList.get(i)); // string szetszedese
		 			if(genre != null){
		 				String[] parts = genre.split(" | ");
		 				Double regiArany = 0.0;
		 				for(int j = 0; j< parts.length; j=j+2){
		 					String engGenre = null;
		 					engGenre = GenreWeighting.hunGenreToEngGenre(parts[j].toLowerCase()); // genre angolositasa
		 					if(movieGenre.containsKey(engGenre)){								  // ha van genre a listaban
		 						Integer num = movieGenre.get(engGenre);		 					
		 						double arany = (double)num/(double)maxMovieGenreNum;
		 						if(regiArany<arany){
		 							if(arany < 0.6){
		 								ujrank = 0.8*rank + (Math.random()/100);
		 							}else if(0.6 <= arany && arany < 0.9){
		 								ujrank = 0.9*rank + (Math.random()/100);
		 							}else if(0.9 <= arany && arany <=1){
		 								ujrank = rank + (Math.random()/100);
		 								if(ujrank>1.0){
		 									ujrank = 0.99 + (Math.random()/1000);
		 								}
		 							}
		 							regiArany = arany;
		 						}
		 					}
		 				}
		 				if(ujrank == null){
		 					ujrank = rank + (Math.random()/100);
		 				}
		 			}else{
		 				ujrank = rank + (Math.random()/100);
		 			}
		 		}else if(discriminator.equals("music")){
		 			rank = csopRank.get(discriminator);
		 			genre = eventGenre.get(eventList.get(i)); // string szetszedese
		 			if(genre != null){
		 				String[] parts = genre.split(",");
		 				Double regiArany = 0.0;
		 				for(int j = 0; j< parts.length; j++){
		 					if(artistGenre.containsKey(parts[j].toLowerCase())){
		 						Integer num = artistGenre.get(parts[j].toLowerCase());
		 						double arany = (double)num/(double)maxArtistGenreNum;
		 						if(regiArany<arany){
		 							if(arany < 0.3){
		 								ujrank = 1.1*rank + (Math.random()/100);
		 							}else if(0.3 <= arany && arany < 0.7){
		 								ujrank = 1.35*rank + (Math.random()/100);
		 							}else if(0.7 <= arany && arany <=1){
		 								ujrank = 1.6*rank + (Math.random()/100);
		 								if(ujrank>1.0){
		 									ujrank = 0.99 + (Math.random()/1000);
		 								}
		 							}
		 							regiArany = arany;
		 						}
		 					}
		 				}
		 				if(ujrank == null){
		 					ujrank = rank + (Math.random()/100);
		 				}
		 			}else{
		 				ujrank = rank + (Math.random()/100);
		 			}
		 		}else if(discriminator.equals("simple")){
		 			ujrank = -1.0;
		 		}else if(discriminator.equals("party")){
		 			rank = csopRank.get(discriminator);
		 			if( CURRENT_YEAR-birthDate > 14 && CURRENT_YEAR-birthDate < 26){
			 			if(averageRank < 0.7 && averageRank != 0){ 	// in this way rank stays under 1.0
		 					ujrank = 1.4 * averageRank + (Math.random()/100);
			 			}else{
			 				ujrank = rank + (Math.random()/100);
			 			}
		 			}else{
		 			rank = csopRank.get(discriminator);
		 			ujrank = rank + (Math.random()/100);
		 			}
		 		}else if(discriminator.equals("kid")){	//we need to watch parents too
		 			rank = csopRank.get(discriminator);
		 			if( CURRENT_YEAR-birthDate < 14 ){
		 				ujrank = 1.2 * averageRank + (Math.random()/100);
		 			}else{
		 				ujrank = 0.8 * rank + (Math.random()/100);
		 			}
		 		}
		 		
		 		
		 		if(ujrank!=-1.0){				// upload new calculated rank
		 			double correctedRank = GenreWeighting.giveaChanceFunction(ujrank,averageRank);
		 			//dbService.uploadRecommendation(userid, eventList.get(i), correctedRank);	// corrected rank to database
		 			System.out.println(" EventId:" +eventList.get(i)+ " Discriminator:" +discriminator+ " Rank:" +ujrank);
		 		}else if(ujrank == -1.0){
		 			//dbService.uploadRecommendation(userid, eventList.get(i), ujrank);			// problem with event, -1 into database
		 			System.out.println(" EventId:" +eventList.get(i)+ " Discriminator:" +discriminator+ " Rank:" +ujrank);
		 		}
		 	}
		 	System.out.println("VÉGZETT AZ EVENTEKKEL");
		}catch (SQLException e) {
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

	public static Integer maximumValueInHashmap(HashMap<String, Integer> hm){
		Integer max = 0;
		for(Entry<String, Integer> entry : hm.entrySet()){
			Integer value = entry.getValue();
			if(value>max){
				max=value;
			}
		}
		return max;
	}
	
	public static double averageValueInHashmap(HashMap<String, Double> hm){
		double average = 0;
		int nevezo = 1;
		for(Entry<String, Double> entry : hm.entrySet()){
			Double value = entry.getValue();
			average += value;
			if(value != 0){
				nevezo++;
			}
		}
		average = average / (double)nevezo;
		return average;
	}
	
	public static String hunGenreToEngGenre(String hunGenre){
		String engGenre = null;
		if(hunGenre.contains("thriller")){
			engGenre = "thriller";
		}else if(hunGenre.contains("akció")){
			engGenre = "action";
		}else if(hunGenre.contains("kaland")){
			engGenre = "adventure";
		}else if(hunGenre.contains("sci-fi")){
			engGenre = "sci-fi";
		}else if(hunGenre.contains("vígjáték")){
			engGenre = "comedy";
		}else if(hunGenre.contains("animációs")){
			engGenre = "animation";
		}else if(hunGenre.contains("bűnügyi")){
			engGenre = "crime";
		}else if(hunGenre.contains("dráma")){
			engGenre = "drama";
		}else if(hunGenre.contains("fantasy")){
			engGenre = "fantasy";
		}else if(hunGenre.contains("romantikus")){
			engGenre = "romantic";
		}else if(hunGenre.contains("misztikus")){
			engGenre = "mystery";
		}else if(hunGenre.contains("horror")){
			engGenre = "horror";
		}else if(hunGenre.contains("dokumentum")){
			engGenre = "document";
		}else if(hunGenre.contains("családi")){
			engGenre = "family";
		}else if(hunGenre.contains("komédia")){
			engGenre = "comedy";
		}else if(hunGenre.contains("zenés vígjáték")){
			engGenre = "comedy";
		}else if(hunGenre.contains("tragédia")){
			engGenre = "drama";
		}
		
		return engGenre;
	}

	public static int getCurrentDate(){
		long timestamp = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int date = year * 10000 + month * 100 + day;
		return date;
	}

	public static double giveaChanceFunction(double helpPercent, double averageRank){
		double randomertek = Math.random();
		double rank = 0;
		if(helpPercent <= 1.1 && helpPercent > 0.5){
			if(randomertek > 0 && randomertek < helpPercent){
				rank = helpPercent*0.8;
			}else if(randomertek <= 1.1 && randomertek > helpPercent){
				rank = helpPercent*0.7;
			}
		}else if(helpPercent >= 0 && helpPercent <= 0.5){
			if(helpPercent > averageRank){
				rank = helpPercent * 0.8;
			}else{
				rank = helpPercent;
			}
		}
		return rank;
	}

}
