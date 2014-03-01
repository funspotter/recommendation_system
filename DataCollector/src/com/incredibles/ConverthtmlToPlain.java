package com.incredibles;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.incredibles.storage.ClientDbService;
import com.incredibles.storage.ClientDbServiceCreator;

public class ConverthtmlToPlain {
	
	public void convert (){
		ClientDbService dbService = null;
		try {
			dbService = ClientDbServiceCreator.createCloud();
			HashMap <Integer, String> idi = dbService.getAllImage();
			HashMap <Integer, String> idt = dbService.getAllThumbnail();
			
			//image
			HashMap <Integer, String> correctidi = new HashMap<Integer, String>();
			
			// Pattern for recognizing a URL, based off RFC 3986
			final Pattern urlPattern = Pattern.compile(
			        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
			                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
			                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
			        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			
//			for(Entry<Integer, String> entry : idi.entrySet()){
//				List<String> links = new ArrayList<String>();
//				String images = entry.getValue();
//				if(images!=null){
//					Matcher matcher = urlPattern.matcher(images);
//					System.out.println(images);
//					while (matcher.find()) {
//						int matchStart = matcher.start(1);
//						int matchEnd = matcher.end();
//						// now you have the offsets of a URL match
//						
//						String url = images.substring(matchStart, matchEnd);
//						System.out.println(url);
//						if(!links.contains(url)){
//							links.add(url);
//						}
//					}
//				}
//				String json = new Gson().toJson(links );
//				correctidi.put(entry.getKey(), json);
//			}
//			dbService.uploadAllImage(correctidi);
//
//			
//			
//						
//			//thumbnail
//			HashMap <Integer, String> correctidt = new HashMap<Integer, String>();
//			
//			
//			for(Entry<Integer, String> entry : idt.entrySet()){
//				String thumbnail = entry.getValue();
//				
//				if(thumbnail!=null){
//					int startIndex = thumbnail.indexOf("http");
//					int stopIndex = thumbnail.indexOf("jpg");
//					if(startIndex>=0&&stopIndex>=0){
//						thumbnail = thumbnail.substring(startIndex, stopIndex+3);
//						//System.out.println(thumbnail);
//						correctidt.put(entry.getKey(), thumbnail);
//					}
//				}
//				
//			}
//			dbService.uploadAllThumbnail(correctidt);
//
//			
			
			//video
			HashMap <Integer, String> idv = dbService.getAllVideo();
			HashMap <Integer, String> correctidv = new HashMap<Integer, String>();
			
			for(Entry<Integer, String> entry : idv.entrySet()){
				String video = entry.getValue();
				String url = null;
				if(video!=null){
					Matcher matcher = urlPattern.matcher(video);
					System.out.println(video);
					while (matcher.find()) {
						int matchStart = matcher.start(1);
						int matchEnd = matcher.end();
						// now you have the offsets of a URL match
						
						url = video.substring(matchStart, matchEnd);
						
					}
				}
				correctidv.put(entry.getKey(), url);
			}
			
			dbService.uploadAllVideo(correctidv);
			
			
			
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	


}
