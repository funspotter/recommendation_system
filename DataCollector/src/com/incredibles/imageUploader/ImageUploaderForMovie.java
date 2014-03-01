package com.incredibles.imageUploader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImageUploaderForMovie {

	public HashMap<String,List<String>> getImages(String name) {
		JSONObject json = null;
		JSONObject jsonObject = null;

		String imdbID = null;
		
		String origin = "http://d3gtl9l2a4fn1j.cloudfront.net/t/p/w500";
		
		HashMap<String,List<String>> hm = new HashMap<String,List<String>>();//thumbnail-galeria
		
		if(name.length()>5&&Character.toString(name.charAt(name.length()-1)).equals(")")&&Character.toString(name.charAt(name.length()-4)).equals("(")){
			name = name.substring(0, name.length()-5);
		}
		
		
		int hiba=0;

		try {
			String url = "http://www.omdbapi.com/?t="
					+ URLEncoder.encode(name, "ISO-8859-1"); // Or "UTF-8".
			json = readJsonFromUrl(url);

		} catch (IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(json.toString());
		try {
			imdbID = json.get("imdbID").toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			hiba++;
			System.out.println("nincs imdbid: "+name);
		}
		
		if(imdbID!=null){
			try {
				//String url = "http://api.themoviedb.org/2.1/Movie.getImages/en/json/505516903be6f309e0abb3d7b4ff62ed/" + imdbID;
				String url = "http://api.themoviedb.org/3/movie/"+imdbID+"/images?api_key=505516903be6f309e0abb3d7b4ff62ed";
				System.out.println(url);
				jsonObject = readJsonFromUrl(url);
				
			} catch (IOException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				JSONArray arr = jsonObject.getJSONArray("posters");
				JSONArray barr = jsonObject.getJSONArray("backdrops");
				
				String url = origin + arr.getJSONObject(0).getString("file_path");
				System.out.println(url);
				
				List<String> images = new ArrayList<String>();
				for (int i = 0; i < barr.length() && i < 5; i++) {
					
					String url1 = origin+barr.getJSONObject(i).getString("file_path");
					System.out.println(url1);
					images.add(url1);
				}
				
				hm.put(url, images);
				
				
			} catch (JSONException | NullPointerException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				hiba++;
				System.out.println("nem talalt ilyen filmet id alapjan: "+name);
			}
		}
		
		return hm;
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		try{
			InputStream is = new URL(url).openStream();
			
			if(is!=null){
				BufferedReader rd = new BufferedReader(new InputStreamReader(is,Charset.forName("UTF-8")));
				String jsonText = readAll(rd);
				
				try {
					
					JSONObject json = new JSONObject(jsonText);
					return json;
				}finally {
					is.close();
				}
			}
		} catch(FileNotFoundException f){
			f.printStackTrace();
		}
		
		return null;
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

}
