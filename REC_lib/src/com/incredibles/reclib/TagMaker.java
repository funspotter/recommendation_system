package com.incredibles.reclib;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.incredibles.data.Like;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;

public class TagMaker {
	
	
	public void makeTagForAllEvents() throws SQLException{
		RecommenderDbService dbService = null;
		HashMap<Integer,String> s = null;
		
		HashMap<Integer, Integer> feltoltendo = new HashMap<Integer, Integer>();
		
		dbService = RecommenderDbServiceCreator.createCloud();
		s = dbService.getAllEventDescV2();
		
	     for (Entry<Integer, String> entry : s.entrySet()) {
	        Integer id = entry.getKey();
	        String desc = entry.getValue();
			MyFilteredClassifier mfc = new MyFilteredClassifier();
			String classNum = mfc.makeClassifier(desc);//kimegy a tag
			feltoltendo.put(id, Integer.valueOf(classNum));
		}
	     
	     //dbService.uploadAllEventsTag(feltoltendo);
		
	}
	
	public void uploadTags(long fbuserid){//feltolti a tageket adott userhez
		List<String> l = getLikeName(fbuserid);
		RecommenderDbService dbService = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			//dbService.uploadEventTag(l);
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
	
	public List<String> getLikeName(long fbuserid){//kiszedi az osszes like nevet
		RecommenderDbService dbService = null;
		List<Like> mp = null;
		List<String> tagArray = new ArrayList<String>();

		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			mp = dbService.getAllLikeForFbUserIdV2(fbuserid);
			
			for(int i = 0;i<mp.size();i++){
				Like l = mp.get(i);
				String tag = makeTagForLike(l.getName());
				System.out.println(tag);
				tagArray.add(tag);
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
		return tagArray;
	}
	
	public String makeTagForLike(String like){//bejön a like neve
		System.out.println(like);
		DescriptionFromWiki desc = new DescriptionFromWiki();
		String wikiDesc = desc.downloadWikiPlainText(like);
		MyFilteredClassifier mfc = new MyFilteredClassifier();
		return mfc.makeClassifier(wikiDesc);//kimegy a tag
	}

}
