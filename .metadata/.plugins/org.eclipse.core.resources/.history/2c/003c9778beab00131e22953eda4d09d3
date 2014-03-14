package com.incredibles.reclib;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.incredibles.data.Like;
import com.incredibles.storage.ClientDbService;
import com.incredibles.storage.ClientDbServiceCreator;


public class SecondStep {
	
	public void makeSecondStep(long fbuserid,int userid){
		List<Like> mp = null;
		ClientDbService dbService = null;
		HashMap<Integer,Integer> hm = new HashMap<Integer,Integer>();
		
		LinkedHashMap<Integer,Integer> lhm = new LinkedHashMap<Integer,Integer>();
		
		try {
			dbService = ClientDbServiceCreator.createCloud();
			mp = dbService.getAllLikeForFbUserId(fbuserid);
			lhm = dbService.getEventTagSwitch();
			
			for(int i = 0;i<mp.size();i++){
				Like l = mp.get(i);
				String tagStr = makeTagForLike(l.getName());
				if(tagStr!=null){
					Integer tag = 0;
					tag = Integer.parseInt(tagStr);
//					try{
//						tag = Integer.parseInt(tagStr);
//					}catch(NumberFormatException e){
//						e.printStackTrace();
//					}
					
					
					if(hm.get(tag) != null){
						Integer tagCount = hm.get(tag);
						tagCount=tagCount+1;
						hm.remove(tag);
						hm.put(tag,tagCount);
						System.out.println("put "+tag+" "+tagCount);
					}else{
						hm.put(tag, 1);
					}
				}

			}
			
			ultimateMuveletek(hm,lhm,userid);
			
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
	
	public void ultimateMuveletek(HashMap<Integer,Integer> hm,LinkedHashMap<Integer,Integer> lhm,int userid){
		//lhm eventid-tagid
		//hm tagid-like darab
		Double szumlike=0.0;
		for (Entry<Integer, Integer> entry : hm.entrySet()) {
	        Integer tag = entry.getKey();
	        Integer likeDB = entry.getValue();
	        szumlike+=likeDB.doubleValue();
		}
		double lepeskoz=0.0;
		if(szumlike!=0.0){
			lepeskoz = 3.0/szumlike;
		}else{
			return;
		}
		
		LinkedHashMap<Integer, Integer> sortedMapDesc = sortByComparator(hm, false);
		
		List<Integer> tags = new ArrayList<Integer>();
		List<Double> likeDBs = new ArrayList<Double>();
		
		int i=0;
		for (Entry<Integer, Integer> entry : sortedMapDesc.entrySet()) {
	        Integer tag = entry.getKey();
	        Integer likeDB = entry.getValue();
	        
	        tags.add(tag);
	        likeDBs.add(likeDB.doubleValue());
		}
	
		
		double elsocsoportsuja = 1;
	    double masodikcsoportsuja = 1;
	    double harmadikcsoportsuja = 1; 
		double negyedikcsoportsuja = 1;
		double otodikcsoportsuja = 1;
		double hatodikcsoportsuja = 1;
		
		
		while(tags.size()!=6){
			tags.add(6);
			likeDBs.add(0.0);
		}
		
		System.out.println("szumlike"+szumlike);
		
		 try{
			 System.out.println("EXP "+String.valueOf(Math.exp(-lepeskoz*likeDBs.get(0))));
			
             elsocsoportsuja = 1.0 - Math.exp(-lepeskoz*likeDBs.get(0));
         masodikcsoportsuja = Math.exp(-lepeskoz*likeDBs.get(0)) - Math.exp(-lepeskoz*(likeDBs.get(0)+likeDBs.get(1)));
         harmadikcsoportsuja = Math.exp(-lepeskoz*(likeDBs.get(0)+likeDBs.get(1))) - Math.exp(-lepeskoz*(likeDBs.get(0)+likeDBs.get(1)+likeDBs.get(2)));
             negyedikcsoportsuja = Math.exp(-lepeskoz*(likeDBs.get(0)+likeDBs.get(1)+likeDBs.get(2))) - Math.exp(-lepeskoz*(likeDBs.get(0)+likeDBs.get(1)+likeDBs.get(2)+likeDBs.get(3)));
             otodikcsoportsuja = Math.exp(-lepeskoz*(likeDBs.get(0)+likeDBs.get(1)+likeDBs.get(2)+likeDBs.get(3))) - Math.exp(-lepeskoz*(likeDBs.get(0)+likeDBs.get(1)+likeDBs.get(2)+likeDBs.get(3)+likeDBs.get(4)));
             hatodikcsoportsuja = Math.exp(-lepeskoz*(likeDBs.get(0)+likeDBs.get(1)+likeDBs.get(2)+likeDBs.get(3)+likeDBs.get(4))) - Math.exp(-lepeskoz*(likeDBs.get(0)+likeDBs.get(1)+likeDBs.get(2)+likeDBs.get(3)+likeDBs.get(4)+likeDBs.get(5)));
     }catch(IndexOutOfBoundsException e){
             e.printStackTrace();
     }

		

		
		ClientDbService dbService = null;
		try {
			dbService = ClientDbServiceCreator.createCloud();
			
			
			for (Entry<Integer, Integer> entry : lhm.entrySet()) {
		        Integer eventid = entry.getKey();
		        Integer tagid = entry.getValue();
		        
		        int index = tags.indexOf(tagid);//0-5-ig
		        double szorzo=0;
		        switch (index) {
	            case 0:  szorzo = elsocsoportsuja;
	                     break;
	            case 1:  szorzo = masodikcsoportsuja;
	                     break;
	            case 2:  szorzo = harmadikcsoportsuja;
	                     break;
	            case 3:  szorzo = negyedikcsoportsuja;
	                     break;
	            case 4:  szorzo = otodikcsoportsuja;
	                     break;
	            case 5:  szorzo = hatodikcsoportsuja;
                		break;
	            default: szorzo = 1;
	                     break;
		        }
		        
		        szorzo = szorzo + Math.random()/100;
		        System.out.println("szorzo"+szorzo+" userid"+userid+" eventid"+eventid);
		//        dbService.updateRank(szorzo,userid,eventid);
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
	
	
	private static LinkedHashMap<Integer, Integer> sortByComparator(Map<Integer, Integer> unsortMap, final boolean order)
    {

        List<Entry<Integer, Integer>> list = new LinkedList<Entry<Integer, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<Integer, Integer>>()
        {
        	@Override
            public int compare(Entry<Integer, Integer> o1,
                    Entry<Integer, Integer> o2)
            {
                if (order)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else
                {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }

        });

        // Maintaining insertion order with the help of LinkedList
        LinkedHashMap<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
        for (Entry<Integer, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
	
	
	public String makeTagForLike(String like){//bejön a like neve
		System.out.println(like);
		DescriptionFromWiki desc = new DescriptionFromWiki();
		String wikiDesc = desc.downloadWikiPlainText(like);
		if(wikiDesc!=null){
			MyFilteredClassifier mfc = new MyFilteredClassifier();
			return mfc.makeClassifier(wikiDesc);//kimegy a tag
		}else{
			return null;
		}
		
		
	}

}
