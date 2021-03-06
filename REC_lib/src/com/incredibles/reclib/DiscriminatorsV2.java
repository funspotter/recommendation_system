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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.incredibles.data.Like;
import com.incredibles.data.MetadataFromThirdParty;

public class DiscriminatorsV2 extends DiscriminatorAndLikeV2 {
	
	
	/*-----CINEMA-----*/
	
	/*Update allCinemaLikeGenre hashmap */
	public static HashMap<String,Double> getAllCinemaLikeGenre(Like oneLike, HashMap<String, Double> allCinemaLikeGenre, HashMap<Long, MetadataFromThirdParty> newTagsforUpload){
		HashMap<String, Double> CinemaLikeGenre = null;
		String oneLikeStr = oneLike.getName();
		CinemaLikeGenre = downloadCinemaLikeGenre(oneLike,newTagsforUpload);	
		if(CinemaLikeGenre != null){								// like does not unknown
			for(Entry<String, Double> entry : CinemaLikeGenre.entrySet()){
				String genre = entry.getKey().toLowerCase();
				Double number = entry.getValue();
				if(allCinemaLikeGenre.containsKey(genre) == false){				// uj genre
					allCinemaLikeGenre.put(genre, number);
				}else{														// letezo genre
					Double regiNumber = allCinemaLikeGenre.get(genre);
					Double ujNumber = regiNumber + number;
					allCinemaLikeGenre.put(genre, ujNumber);
				}
			}
		}
		return allCinemaLikeGenre;
	}
	
	/*Search like on IMDB*/ // le kell t�lteni m�g az imdb id-t hozz� valahogy
	private static HashMap<String, Double> downloadCinemaLikeGenre(Like oneLike, HashMap<Long, MetadataFromThirdParty> newTagsforUpload){
		JSONObject json = null;
		JSONArray jsonArray = null;
		HashMap<String, Double> CinemaLikeGenre = null;
		String genres = null;
		String like = oneLike.getName();
		long ourLikeId = oneLike.getId();
		List<String> movieTags = new ArrayList<String>();
		try {
			String url = "http://www.omdbapi.com/?t=" + URLEncoder.encode(like, "ISO-8859-1"); // Or "UTF-8".
			json = readJsonFromUrl(url);
		} catch (IOException | JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Nincs film: IMDB api"+ like);
			return CinemaLikeGenre;
		}
		try {
			CinemaLikeGenre = new HashMap<String, Double>();
			genres = json.get("Genre").toString();
			String[] gstr = genres.split(", ");
			for(int i = 0; i<gstr.length;i++){
				if(CinemaLikeGenre.containsKey(gstr[i]) == false){	// new genre
					CinemaLikeGenre.put(gstr[i], (double) 1);
					movieTags.add(gstr[i]);
				}else{												// old genre
					Double number = CinemaLikeGenre.get(gstr[i]);
					number = number + (double) 1;
					CinemaLikeGenre.put(gstr[i], number);
				}	
			}
			List<String> tagList = new ArrayList<String>(CinemaLikeGenre.keySet());
			MetadataFromThirdParty metadataInfo = new MetadataFromThirdParty(ourLikeId, "imdbid", tagList);
			newTagsforUpload.put(ourLikeId, metadataInfo);
		} catch (JSONException e) {
			System.out.println("json probl�ma nincs imdbid: "+like);
		}
		return CinemaLikeGenre;
	}
		
	/*Ranking by movie genre*/
	public static Double helpCinemaGenreRanking(HashMap<Integer,List<String>> eventGenre, Double rank, Integer eventId, HashMap <String,Double> allCinemaLikeGenre){
		List<String> genre = null;
		if(eventGenre.containsKey(eventId)){
			genre = eventGenre.get(eventId);
		}
		Double ujrank = null;
		if(genre != null){		
			Double regiArany = 0.0;
				for(int j = 0; j< genre.size(); j++){
					String engGenre = null;
					engGenre = hunGenreToEngGenre(genre.get(j).toLowerCase());
					if(allCinemaLikeGenre.containsKey(engGenre)){
						Double num = allCinemaLikeGenre.get(engGenre);		 					
						Double arany = num/maximumValueInHashmap(allCinemaLikeGenre);
						if(regiArany<arany){
							if(arany < 0.3){
								ujrank = rank + (Math.random()/100);
							}else if(0.3 <= arany && arany < 0.7){
								ujrank = 1.2*rank + (Math.random()/100);
							}else if(0.7 <= arany && arany <=1){
								ujrank = 1.4*rank + (Math.random()/100);
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
		return ujrank;
	}
	
	/*Translate hungarian movie genre to english*/
	private static String hunGenreToEngGenre(String hunGenre){
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
	
	
	
	/*-----MUSIC-----*/
	
	/*Update allMusicLikeGenre hashmap */
	/*This function calls Last.fm getartistgenre function*/
	public static HashMap<String,Double> getAllMusicLikeGenre(Like oneLike, HashMap<String,Double> allMusicLikeGenre, HashMap<Long, MetadataFromThirdParty> newTagsforUpload){
		List<String> musicLikeGenre = null;
		String oneLikeStr = oneLike.getName();
		try {
			musicLikeGenre = downloadMusicGenreFromLastFm(oneLike, newTagsforUpload);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("ioexeption last.fm lek�rdez�sn�l/dicriminators");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			musicLikeGenre = null;
			System.out.println("Nincs hozz� zene:" +oneLikeStr);
		}	
		if(musicLikeGenre != null){								// like does not unknown
			for(int i = 0; i<musicLikeGenre.size(); i++){
				String genre = musicLikeGenre.get(i).toLowerCase();
				if(allMusicLikeGenre.containsKey(genre) == false){				// new genre
					allMusicLikeGenre.put(genre, (double) 1);					// last.fm returns just one spec. genre (indie;alternative...)
				}else{														// existing genre
					Double regiNumber = allMusicLikeGenre.get(genre);
					Double ujNumber = regiNumber + (double) 1;
					allMusicLikeGenre.put(genre, ujNumber);
				}
			}
		}
		return allMusicLikeGenre;
	}
			
	/*NOT USED*/
	/* Search music on xy webpage*/
	private static HashMap<String, Double> downloadMusicLikeGenre(String artist) throws IOException, JSONException {
		String queryStr = "[{ \"type\": \"/music/artist\", \"name\": \""+artist+"\", \"genre\": [] }]";
		HashMap<String, Double> genres = null;
		//System.out.println(queryStr);
		try {
			String url = "https://www.googleapis.com/freebase/v1/mqlread/?query=" + URLEncoder.encode(queryStr, "ISO-8859-1");
			//System.out.println(url);
			JSONObject json = readJsonFromUrl(url);		
			JSONArray a1 = json.getJSONArray("result");
			try{
				JSONObject o1 = a1.getJSONObject(0);
				JSONArray a2 = o1.getJSONArray("genre");
				genres = new HashMap<String, Double>();
				for(int i=0;i<a2.length();i++){
				//	System.out.println(a2.get(i).toString());
					Double num = (double) 0;
					if(genres.containsKey(a2.get(i).toString()) == true){
						num = genres.get(a2.get(i).toString());
						num = num + 1;
						genres.put(a2.get(i).toString(), num); // mert fel�lirja a regi value erteket
					}else{
						genres.put(a2.get(i).toString(), (double) 1);
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
	
	/*Search music on Last.FM*/
	/*LAST FM API Key: ad9afb49ea6463b08d4a1107f55ff3d1
	        Secret: is b104f833b9f4283a259000290973a241    brought to you by david*/ // qrva lastfm idt is lekell k�rni valahogy a qrva el�ad�hoz.
	public static List<String> downloadMusicGenreFromLastFm(Like oneLike, HashMap<Long, MetadataFromThirdParty> newTagsforUpload) throws IOException, JSONException{
		List<String> genres = new ArrayList<String>();
		String method = "artist.gettoptags";
		String artist = oneLike.getName();
		Long ourLikeId = oneLike.getId();
	 	String artisVar = artist.replaceAll(" ", "+");
	 	String apiKey = "ad9afb49ea6463b08d4a1107f55ff3d1";
	 	Integer minNumber = 15;
	 	JSONObject obj = readJsonFromUrl("http://ws.audioscrobbler.com/2.0/?method="+ method +"&artist="+ artisVar +"&autocorrect=1&api_key="+ apiKey +"&format=json");
	 	JSONObject obj1 = obj.getJSONObject("toptags");
	 	JSONObject obj2 = obj1.getJSONObject("@attr");
	 	String artistName = obj2.getString("artist");
	 	JSONArray tagArray = obj1.getJSONArray("tag");
	 	for(int i = 0; i<tagArray.length(); i++){
	 		JSONObject elements = tagArray.getJSONObject(i);
	 		if(elements.getInt("count")>minNumber){
	 			genres.add(elements.getString("name"));
	 		}else{
	 			break;
	 		}
	 	}
		MetadataFromThirdParty metadataInfo = new MetadataFromThirdParty(ourLikeId, artistName, genres);
		newTagsforUpload.put(ourLikeId, metadataInfo);	
	 	System.out.println("eloado:"+artist+"	darab tag:"+genres.size());
	 	return genres;
	}
	
	/*Ranking by music genre*/
	public static Double helpMusicGenreRanking(HashMap<Integer,List<String>> eventGenre, Double rank, Integer eventId, HashMap <String,Double> allMusicLikeGenre){
		List<String> genre = null;
		if(eventGenre.containsKey(eventId)){
			genre = eventGenre.get(eventId);
		}
		Double ujrank = null;
		if(genre != null){	
			Double regiArany = 0.0;
				for(int j = 0; j< genre.size(); j++){
					String oneGenre = genre.get(j).toLowerCase();
					if(allMusicLikeGenre.containsKey(oneGenre)){
						Double num = allMusicLikeGenre.get(oneGenre);		 					
						Double arany = num/maximumValueInHashmap(allMusicLikeGenre);
						if(regiArany<arany){
							if(arany < 0.3){
								ujrank = rank + (Math.random()/100);
							}else if(0.3 <= arany && arany < 0.7){
								ujrank = 1.2*rank + (Math.random()/100);
							}else if(0.7 <= arany && arany <=1){
								ujrank = 1.4*rank + (Math.random()/100);
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
		return ujrank;
	}

	
	/*-----PARTY-----*/
	
	/* Ranking by user age, 14-28 higher ranks */
	public static Double helpPartyAgeRanking(Integer age, Double rank, Double maximumInDiscRank){
		Double ujrank = null;
		if(age > 14 && age < 28){
			if(maximumInDiscRank <= 1 && maximumInDiscRank > 0){
				ujrank = 0.9*maximumInDiscRank + (Math.random()/100);
			}else if(maximumInDiscRank == 0){
				ujrank = Math.random()/10;
			}else{
				ujrank =  Math.random()/100;
			}
		}else{
			ujrank = rank + (Math.random()/100);
		}
		return ujrank;
	}
	
	
	
	/*-----KID-----*/
	
	/*Ranking by user age, 0-14 higher ranks, 14-inf lower ranks*/
	public static Double helpKidAgeRanking(Integer age, Double rank, Double maximumInDiscRank){
		Double ujrank = null;
		if(age > 0 && age <= 14){ 
			if(maximumInDiscRank <= 1 && maximumInDiscRank > 0){
				ujrank = 0.9*maximumInDiscRank + (Math.random()/100);
			}else if(maximumInDiscRank == 0){
				ujrank = Math.random()/10;
			}else{
				ujrank =  Math.random()/100;
			}
		}else{
			ujrank = 0.5*rank + (Math.random()/100);
		}
		return ujrank;
	}
	
	
	
	/* JSON STUFF */
	
	private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
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

	private static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
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

	
}
