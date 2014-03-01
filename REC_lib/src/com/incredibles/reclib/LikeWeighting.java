package com.incredibles.reclib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.incredibles.data.Like;
import com.incredibles.storage.ClientDbService;
import com.incredibles.storage.ClientDbServiceCreator;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;

public class LikeWeighting {

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
	
	// bejön a like megnézzük benne van-e valamelyik csoportban és akkor
	// odahúzunk egy strigulát onnan átadom cinema-10->Dava
	public void readAllLike(long fbuserid) throws JSONException {

		List<Like> mp = null;
		ClientDbService dbService = null;
		HashMap<String, Integer> csopLike = new HashMap<String, Integer>();
		csopLike.put("cinema", 0);
		csopLike.put("exhibition", 0);
		csopLike.put("festival", 0);
		csopLike.put("gastro", 0);
		csopLike.put("kid", 0);
		csopLike.put("music", 0);
		csopLike.put("sport", 0);
		csopLike.put("theater", 0);	
		csopLike.put("travel", 0);
		csopLike.put("other", 0);
		csopLike.put("party", 0);
		try {
			dbService = ClientDbServiceCreator.createCloud();
			mp = dbService.getAllLikeForFbUserId(fbuserid);
			HashMap<String, Integer> allArtistGenre = new HashMap<String, Integer>();	// userhez tartozo osszes zenei eloado mufaja
			HashMap<String, Integer> allMovieGenre = new HashMap<String, Integer>();	// userhez tartozo osszes film mufaja			
			for (int i = 0; i < mp.size(); i++) {
				Like l = mp.get(i);
				String cat = l.getCategory().toLowerCase();			
				if (cinema.contains(cat)) {
				    //System.out.println("cinema "+l.getName());
					HashMap<String, Integer> genreMovie = new HashMap<String, Integer>();
					genreMovie=downloadGenreForMovie(l.getName());
					if(genreMovie != null){
						for(Entry<String, Integer> entry : genreMovie.entrySet()){
							String genre = entry.getKey().toLowerCase();
							Integer number = entry.getValue();
							if(allMovieGenre.containsKey(genre) == false){				// uj genre
								allMovieGenre.put(genre, number);
							}else{														// letezo genre
								Integer regiNumber = allMovieGenre.get(genre);
								Integer ujNumber =regiNumber + number;
								allMovieGenre.put(genre, ujNumber);
							}
						}
					}
					likeCountPlusPlus(csopLike, "cinema");
				}
				if (exhibition.contains(cat)) {
					likeCountPlusPlus(csopLike, "exhibition");
				}
				if (festival.contains(cat)) {
					likeCountPlusPlus(csopLike, "festival");
				}
				if (gastro.contains(cat)) {
					likeCountPlusPlus(csopLike, "gastro");
				}
				if (kid.contains(cat)) {
					likeCountPlusPlus(csopLike, "kid");
				}
				if (music.contains(cat)) {
					try {		
						//System.out.println("artist "+l.getName());
						HashMap<String, Integer> genreArtist = new HashMap<String, Integer>();
						genreArtist = downloadGenreForArtist(l.getName());				// egy hashmap-be t�lteni minden genre-t
						if(genreArtist != null){
							for(Entry<String, Integer> entry : genreArtist.entrySet()){
								String genre = entry.getKey().toLowerCase();
								Integer number = entry.getValue();
								if(allArtistGenre.containsKey(genre) == false){				// uj genre
									allArtistGenre.put(genre, number);
								}else{														// letezo genre
									Integer regiNumber = allArtistGenre.get(genre);
									regiNumber =regiNumber + number;
									allArtistGenre.put(genre, regiNumber);
								} 
							}
						}					
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					likeCountPlusPlus(csopLike, "music");
				}
				if (sport.contains(cat)) {
					likeCountPlusPlus(csopLike, "sport");
				}
				if (theater.contains(cat)) {
					likeCountPlusPlus(csopLike, "theater");
				}
				if (travel.contains(cat)) {
					likeCountPlusPlus(csopLike, "travel");
				}
				if (party.contains(cat)) {
					likeCountPlusPlus(csopLike, "party");
				}
			}
			
//			for(Entry<String, Integer> entry : allArtistGenre.entrySet()){
//				System.out.println("artistgenre: "+entry.getKey()+" darab: "+entry.getValue());
//			}
//			for(Entry<String, Integer> entry : allMovieGenre.entrySet()){
//				System.out.println("moviegenre: "+entry.getKey()+" darab: "+entry.getValue());
//			}
			HashMap <String, Double> csopRank = LikeWeighting.LikeWeights(csopLike);
			GenreWeighting.rankSet(allMovieGenre, allArtistGenre, csopRank, fbuserid); // itt t�rtenik a rankfeltoltes
			
			
					
		} catch (SQLException e) {

		}finally {
			if (dbService != null) {
				try {
					dbService.close();
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			}
		}

		
		
		
//		System.out.println("mozi: " + csopLike.get("cinema"));
//		System.out.println("exhibition: " + csopLike.get("exhibition"));
//		System.out.println("festival: " + csopLike.get("festival"));
//		System.out.println("gastro: " + csopLike.get("gastro"));
//		System.out.println("kid: " + csopLike.get("kid"));
//		System.out.println("music: " + csopLike.get("music"));
//		System.out.println("sport: " + csopLike.get("sport"));
//		System.out.println("theater: " + csopLike.get("theater"));
		
	}
	
	public void betaRanker(HashMap<String, Integer> csopLike, long faceUserID){
		RecommenderDbService dbService = null;
		List<Integer> legitEvents = new ArrayList<Integer>();
		HashMap<String, Double> csopRank = LikeWeighting.LikeWeights(csopLike);
		HashMap<Integer, String> eventDiscriminator = null;
		long timestamp = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int date = year * 10000 + month * 100 + day;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			legitEvents = dbService.eventsInShowList(date);
//			eventDiscriminator = dbService.getAllEventDiscriminator();	
			for(int i=0; i<legitEvents.size(); i++ ){	// iteralas a showval rendelkezo eventeken
				String discriminator = null;
				Double rank = null;
				discriminator = eventDiscriminator.get(legitEvents.get(i));	// adott shownak mi a discriminatora
				rank = csopRank.get(discriminator);
				rank = rank + (Math.random()/100);	// ha itt van hiba, akkor Dobule;double k�z�tti valtas lehet a baj
				int userid = dbService.getUserIDforFbUserID(faceUserID);	// ez nem kene ha atirja valaki a rankfeltoltot
				//dbService.updateRankUploader(userid,legitEvents.get(i),rank);	
			}
			
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

	
	public static HashMap<String,Double> LikeWeights(HashMap<String,Integer> csopLike){
		
		HashMap<String,Double> csopRank = new HashMap<String,Double>();
		int sum=0;
		double rank=0;
		// sum kiszamolasa
		for (Entry<String, Integer> entry : csopLike.entrySet()) {
	        Integer csopCount = entry.getValue();
	        sum = sum + csopCount;			
		}
		// rank ertekek feltoltese csopRank-ba
		for (Entry<String, Integer> entry : csopLike.entrySet()) {
	        String csoport = entry.getKey();
			Integer csopCount = entry.getValue();
			if(csopCount != 0){
				rank = (double)csopCount/(double)sum;
			}else{
				rank = 1.0/(double)sum;
			}
			csopRank.put(csoport, rank);
		}
		
		return csopRank;
		
	}
	
	
	// ciname,festival,music,theater-ből genre kiszedni freebase-el, meg
	// eventekből

	public HashMap<String, Integer> downloadGenreForArtist(String artist) throws IOException, JSONException {

		String queryStr = "[{ \"type\": \"/music/artist\", \"name\": \""+artist+"\", \"genre\": [] }]";
		HashMap<String, Integer> genres = null;
		//System.out.println(queryStr);

		try {
			String url = "https://www.googleapis.com/freebase/v1/mqlread/?query=" + URLEncoder.encode(queryStr, "ISO-8859-1");
			//System.out.println(url);

			JSONObject json = readJsonFromUrl(url);
			
			JSONArray a1 = json.getJSONArray("result");
			try{
				JSONObject o1 = a1.getJSONObject(0);
				JSONArray a2 = o1.getJSONArray("genre");
				genres = new HashMap<String, Integer>();
				for(int i=0;i<a2.length();i++){
				//	System.out.println(a2.get(i).toString());
					int num = 0;
					if(genres.containsKey(a2.get(i).toString()) == true){
						num = genres.get(a2.get(i).toString());
						num = num + 1;
						genres.put(a2.get(i).toString(), num); // mert fel�lirja a regi value erteket
					}else{
						genres.put(a2.get(i).toString(), 1);
					}
				}
			}catch(JSONException e){
				
			}		
		//	System.out.println(genres.size());
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Or "UTF-8".
		
		return genres;

	}
	
	
	public HashMap<String, Integer> downloadGenreForMovie(String movie) {
		JSONObject json = null;
		JSONArray jsonArray = null;
		HashMap<String, Integer> movieGenre = null;
		String genres = null;
		
		try {
			String url = "http://www.omdbapi.com/?t=" + URLEncoder.encode(movie, "ISO-8859-1"); // Or "UTF-8".
			json = readJsonFromUrl(url);

		} catch (IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(json.toString());
		try {
			movieGenre = new HashMap<String, Integer>();
			genres = json.get("Genre").toString();
			String[] gstr = genres.split(", ");
			for(int i = 0; i<gstr.length;i++){
			//	System.out.println("movie genres: "+gstr[i]);
				if(movieGenre.containsKey(gstr[i]) == false){	// uj genre
					movieGenre.put(gstr[i], 1);
				}else{											// mar benne volt a genre
					Integer number = movieGenre.get(gstr[i]);
					number = number + 1;
					movieGenre.put(gstr[i], number);
				}
				
			}
		} catch (JSONException e) {

			System.out.println("nincs imdbid: "+movie);
		}
		
		return movieGenre;

	}
	
	
	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(is,
				Charset.forName("UTF-8")));
		String jsonText = readAll(rd);
		
		//System.out.println(jsonText);
		
		try {
			
			JSONObject json = new JSONObject(jsonText);
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

	
	public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(is,
				Charset.forName("UTF-8")));
		String jsonText = readAll(rd);
		
		try {
			
			JSONArray json = new JSONArray(jsonText);
			return json;
		}finally {
			is.close();
		}
	}
	

	public void likeCountPlusPlus(HashMap<String, Integer> csopLike, String cat) {
		if (csopLike.containsKey(cat)) {
			Integer counter = csopLike.get(cat);
			counter = counter + 1;
			csopLike.put(cat, counter);
		} else {
			csopLike.put(cat, 1);
		}
	}

}
