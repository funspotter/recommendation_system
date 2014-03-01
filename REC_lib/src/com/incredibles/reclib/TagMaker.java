package com.incredibles.reclib;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.incredibles.data.Like;
import com.incredibles.storage.ClientDbService;
import com.incredibles.storage.ClientDbServiceCreator;

public class TagMaker {
	
	
	public void makeTagForAllEvents() throws SQLException{
		ClientDbService dbService = null;
		HashMap<Integer,String> s = null;
		
		HashMap<Integer, Integer> feltoltendo = new HashMap<Integer, Integer>();
		
		dbService = ClientDbServiceCreator.createCloud();
		s = dbService.getAllEventDesc();
		
	     for (Entry<Integer, String> entry : s.entrySet()) {
	        Integer id = entry.getKey();
	        String desc = entry.getValue();
			MyFilteredClassifier mfc = new MyFilteredClassifier();
			String classNum = mfc.makeClassifier(desc);//kimegy a tag
			feltoltendo.put(id, Integer.valueOf(classNum));
		}
	     
	     dbService.uploadAllEventsTag(feltoltendo);
		
	}
	
	public void uploadTags(long fbuserid){//feltolti a tageket adott userhez
		List<String> l = getLikeName(fbuserid);
		ClientDbService dbService = null;
		try {
			dbService = ClientDbServiceCreator.createCloud();
			dbService.uploadEventTag(l);
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
		ClientDbService dbService = null;
		List<Like> mp = null;
		List<String> tagArray = new ArrayList<String>();

		try {
			dbService = ClientDbServiceCreator.createCloud();
			mp = dbService.getAllLikeForFbUserId(fbuserid);
			
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
