package com.incredibles.reclib;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import cern.colt.matrix.impl.SparseDoubleMatrix2D;

import com.incredibles.storage.LogLine;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;
//import com.sun.org.apache.xml.internal.utils.StringVector;


public class Reader {
	
	public static SparseDoubleMatrix2D spd;//sparse matrix ebben tarolom a beolvasott log filet
	
	/*user id and the row of the big matrix is not equal, we need mapping*/
	public static HashMap <String, Integer> user_map;//valodi id - nalam id	// <integer,integer> for website
	public static HashMap <String, Integer> item_map;//valodi id - nalam id	// <integer,integer> for website
	
	public HashSet <String> user_map_row_counter;	// <integer> for website
	public HashSet <String> item_map_row_counter;	// <integer> for website
	
	//public static StringVector new_user;
	//public static StringVector new_item;
//	public static StringVector new_user;	// Vector<Integer> for web
//	public static StringVector new_item;	// Vector<Integer> for web
	
	/**Collect and store the user and event info
	 * if the log data is in the test time period,
	 * which set with T variable*/
	public static HashMap<String,HashMap<String,Integer>> TestUserItemHm = new HashMap<String,HashMap<String,Integer>>();	//userid - (itemid - darab)
	
	/*what do T stands for ?*/
	public int T = 0;
	public Date lastDate;
	public Date lastMinusT;
	
	public String splitString = new String("\t");// \\| volt
	public String fileString = new String("C:/Downloads/lastfm-dataset-1K/userid-timestamp-artid-artname-traid-traname.tsv");
	
	/*Set lastMinusT, lastDate - T*/
	public Date minusT(){		
//		long l=lastDate.getTime()-T*3600000;
//		lastMinusT=new Date();
//		lastMinusT.setTime(l);
//		//System.out.println("inrecall");
//		//System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS").format(lastMinusT));
//		return lastMinusT;
		Date time = new Date();
		time.setTime(1230771661000L);
		return time;
	}
	
	/*Set the user-item matrix*/
//	public void read_from_api() throws SQLException, IOException {
//		user_map = new HashMap<Integer,Integer>();
//		item_map = new HashMap<Integer,Integer>();
//		user_map_row_counter = new HashSet<Integer>();
//		item_map_row_counter = new HashSet<Integer>();
//		//new_user = new StringVector(100);
//		//new_item = new StringVector(100);
//		new_user = new Vector<Integer>();
//		new_item = new Vector<Integer>();
//		
//		int inline=0;
//		lastDate = new Date();
//
//		/*here we get from LOG table info, fill up LASTDATE date, with the latest log timestamp*/
//		/*miért is ? :D*/
//		RecommenderDbService dbService = null;
//		try {
//			//dbService = dbServiceCreator.create("file/TV1.txt");
//			dbService = RecommenderDbServiceCreator.createCloud();
//			LogLine logLine = new LogLine();
//			while (dbService.getNextLogLine(logLine)) {
//				//System.out.println(logLine.toString());
//				if(inline==0){
//					lastDate=logLine.timeStamp;
//				}
//				Date convertedDate = logLine.timeStamp;
//				if(convertedDate.compareTo(lastDate)>0){ /*compareTo értéke nagyobb 1, ha convertedDate > lastDate*/
//					lastDate=convertedDate;
//				}
//				inline++;
//			}
//		} finally {
//			if (dbService != null) {
//				dbService.close();
//			}
//		}
//				
//		/*Here T is zero, what is the difference between lastMinusT and lastDate ?*/
//		Date lastMinusT=minusT();
//		//System.out.println(lastMinusT);
//		
//		
//		RecommenderDbService dbService1 = null;
//		try {
//			//dbService1 = dbServiceCreator.create("file/TV1.txt");
//			dbService1 = RecommenderDbServiceCreator.createCloud();
//			LogLine logLine = new LogLine();
//			while (dbService1.getNextLogLine(logLine)) {
//				Date convertedDate = logLine.timeStamp;
//				if(lastMinusT.compareTo(convertedDate)>=0){	/*enter if convertedDate < lastMinusT or convertedDate = lastMinusT */
//				
//					//System.out.println(lastMinusT.compareTo(convertedDate));
//					if(!user_map_row_counter.contains(logLine.userId)){
//						user_map_row_counter.add(logLine.userId);	/*collect all users id*/
//					}
//					if(!item_map_row_counter.contains(logLine.eventId)){//ha nem volt meg ilyen ertek
//						item_map_row_counter.add(logLine.eventId);/*collect all item id*/
//					}
//				}else{										/*enter if convertedDate > lastMinusT*/
//					new_user.addElement(logLine.userId);
//					new_item.addElement(logLine.eventId);
//				}
//			}
//		} finally {
//			if (dbService != null) {
//				dbService.close();
//			}
//		}
//		
//
//
//		int user_futo=0; /*this shows the row number in the matrix*/
//		int item_futo=0; /*shows the column number in the matrix*/
//
//		int lines=0;
//		
//		/*spd contains the readed data*/
//		spd = new SparseDoubleMatrix2D(user_map_row_counter.size(), item_map_row_counter.size());
//		spd.assign(0); /*set all matrix element to zero*/
//		
//		RecommenderDbService dbService2 = null;
//		try {
//			//dbService2 = dbServiceCreator2.create("file/TV1.txt");
//			dbService2 = RecommenderDbServiceCreator.createCloud();
//			LogLine logLine = new LogLine();
//			while (dbService2.getNextLogLine(logLine)) {
//				Date convertedDate = logLine.timeStamp;
//				//System.out.println(logLine.toString());
//				if(lastMinusT.compareTo(convertedDate)>=0){
//	        		//System.out.println("lastMinusT is after convertedDate");
//					int row=0;
//					int column=0;
//					//System.out.println("beolvasott line "+Integer.toString(logLine.userId));
//					if(!user_map.containsKey(logLine.userId)){//ha m�g nem volt ilyen �rt�k
//						//System.out.println(user_futo);
//						user_map.put(logLine.userId,user_futo);/*set user id and matrix row to hashmap*/
//						row=user_futo;
//						user_futo++;
//					}else{
//						row=user_map.get(logLine.userId); /* if user was already in the matrix, get the row number*/
//					}
//
//					if(!item_map.containsKey(logLine.eventId)){//ha m�g nem volt ilyen �rt�k
//						item_map.put(logLine.eventId,item_futo);//elmentem az eredeti id-t az �n id-mel
//						column=item_futo;
//						item_futo++;
//					}else{
//						column=item_map.get(logLine.eventId);
//					}
//				
//					double i=spd.get(row, column);	/*sets matrix cell x+1*/
//					i=i+1;
//					spd.setQuick(row, column, i);
//					
//					lines++;
//				}
//			}
//		} finally {
//			if (dbService != null) {
//				dbService.close();
//			}
//		}
//		
//		System.out.println(Integer.toString(user_map_row_counter.size())+" "+Integer.toString(item_map_row_counter.size())+" "+Integer.toString(inline));
//
//		System.out.println("olvasas vege");
//	}
	
	/**Dataset info : http://www.dtic.upf.edu/~ocelma/MusicRecommendationDataset/lastfm-1K.html */
	public void read_file() throws IOException{
		
		

		user_map = new HashMap<String,Integer>();
		item_map = new HashMap<String,Integer>();
		user_map_row_counter = new HashSet<String>();
		item_map_row_counter = new HashSet<String>();
//		new_user = new StringVector(100);
//		new_item = new StringVector(100);
		
		//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

		BufferedReader br = new BufferedReader(new FileReader(fileString));
		String line;
		int inline=0;
		lastDate = new Date();
		HashMap<Integer,Integer> listennumondate = new HashMap<Integer,Integer>();
		//kiszedi a legfrissebb datumot
//		while ((line = br.readLine()) != null) {
//			String[] str = line.split(splitString);
//			String userID = str[0];
//			String timestamp = str[1];
//			String artistID = str[3];
//			if(inline==0){
//				try {
//			//		System.out.println(timestamp.toString());
//					lastDate=formatter.parse(timestamp.toString().substring(0, 20));	// str[1] - timestamp
//				} catch (ParseException e) {e.printStackTrace();}
//			}
//		    try {
//				Date convertedDate = formatter.parse(timestamp.toString().substring(0, 20));
////				Calendar cal = Calendar.getInstance();
////				cal.setFirstDayOfWeek(Calendar.MONDAY);
////				 cal.setTimeInMillis(convertedDate.getTime());
////				 Integer time = 10000*cal.get(Calendar.YEAR)+100*(cal.get(Calendar.MONTH)+1)+cal.get(Calendar.DATE);
////				if(listennumondate.containsKey(time)){
////					Integer number = listennumondate.get(time);
////					listennumondate.put(time, number+1);
////				}else{
////					listennumondate.put(time, 1);
////				}
//				if(convertedDate.compareTo(lastDate)>0){
//					lastDate=convertedDate;
//				}
//			} catch (ParseException e) {e.printStackTrace();} 	
//			inline++;
////			for(int i = 0; i<str.length; i++){
////				System.out.print(str[i]+"  ");
////			}
//			
////			System.out.println(userID.toString());
//		}
//		for(Entry<Integer, Integer> entry: listennumondate.entrySet()){
//			double date = (double)entry.getKey();
//			double number = (double)entry.getValue();
//			RecommendationAccuracy.writeToTxt(date, number, true);
//		}
		Date lastMinusT=minusT();	// melyik időponttól van a test és training set
		
		//megn�zem, hogy ezen a d�tumon bel�l mennyi user-item van
		BufferedReader br2 = new BufferedReader(new FileReader(fileString));
		String line2;
		while ((line2 = br2.readLine()) != null) {
			String[] str = line2.split(splitString);	
			String userID = str[0];
			String timestamp = str[1];
			String artistID = str[3];
			System.out.println(userID);
		    try {
				Date convertedDate = formatter.parse(timestamp.toString().substring(0, 20));
				if(lastMinusT.compareTo(convertedDate)>0){	// lastminust előtti bejegyzéseket keressük
					//System.out.println("lastMinusT is after lastDate");
					if(!user_map_row_counter.contains(userID)){	// str[0]- user_xyzk
						user_map_row_counter.add(userID);		
					}
					if(!item_map_row_counter.contains(artistID)){//ha m�g nem volt ilyen �rt�k  		str[3]- artistname
						item_map_row_counter.add(artistID);//elmentem az eredeti id-t az �n id-mel
					}
				}else{
				//	new_user.addElement(userID);	// add user
				//	new_item.addElement(artistID);	// add artist name
					
					if(TestUserItemHm.containsKey(userID)){
						if(TestUserItemHm.get(userID).containsKey(artistID)){
							HashMap<String, Integer> UserHm = TestUserItemHm.get(userID);
							Integer number = UserHm.get(artistID);
							UserHm.put(artistID, number+1);
							TestUserItemHm.put(userID, UserHm);
						}else{
							HashMap<String, Integer> UserHm = TestUserItemHm.get(userID);
							UserHm.put(artistID, 1);
							TestUserItemHm.put(userID, UserHm);
						}
					}else{
						HashMap<String, Integer> UserHm = new HashMap<String,Integer>();
						UserHm.put(artistID, 1);
						TestUserItemHm.put(userID, UserHm);
					}
				}
			} catch (ParseException e) {e.printStackTrace();}
		}

		int user_futo=0;
		int item_futo=0;

		int lines=0;
		
		spd = new SparseDoubleMatrix2D(user_map_row_counter.size(), item_map_row_counter.size());
		spd.assign(0);
		
		BufferedReader br1 = new BufferedReader(new FileReader(fileString));
		String line1;
		while ((line1 = br1.readLine()) != null) {
			
			String[] str = line1.split(splitString);
			//System.out.println (str[0]);		
		    try {
				Date convertedDate = formatter.parse(str[1].toString().substring(0, 20));
				if(lastMinusT.compareTo(convertedDate)>0){	// lastminust előtti bejegyzésekkor
	        		//System.out.println("lastMinusT is after convertedDate");
					int row=0;
					int column=0;			
					if(!user_map.containsKey(str[0])){//ha m�g nem volt ilyen �rt�k
						//System.out.println(user_futo);
						user_map.put(str[0],user_futo);//elmentem az eredeti id-t az �n id-mel
						row=user_futo;
						user_futo++;
					}else{
						row=user_map.get(str[0]);
					}
					if(!item_map.containsKey(str[3])){//ha m�g nem volt ilyen �rt�k
						item_map.put(str[3],item_futo);//elmentem az eredeti id-t az �n id-mel
						column=item_futo;
						item_futo++;
					}else{
						column=item_map.get(str[3]);
					}									// set column and row;
					double i=spd.get(row, column);
					i=i+1;
					spd.setQuick(row, column, i);
					
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			lines++;		
		}
		
		System.out.println(Integer.toString(user_map_row_counter.size())+" "+Integer.toString(item_map_row_counter.size())+" "+Integer.toString(inline));
		//System.out.println("olvasas vege");	
		br.close();
	}
	
	
	/**not needed T variable*/
	public void readFileWithoutDate() throws IOException{
		user_map = new HashMap<String,Integer>();
		item_map = new HashMap<String,Integer>();
		user_map_row_counter = new HashSet<String>();
		item_map_row_counter = new HashSet<String>();
//		new_user = new StringVector(100);	// régen rossz ha itt használjuk ezeket
//		new_item = new StringVector(100);	// - || - -||- -||-
		
		
		int lines=0;
		int inline=0;
		
		int user_futo=0;
		int item_futo=0;
		
		//megn�zem, hogy ezen a d�tumon bel�l mennyi user-item van
		BufferedReader br2 = new BufferedReader(new FileReader(fileString));
		String line2;
		while ((line2 = br2.readLine()) != null) {
			//line2=line2.replaceAll("\"", "");
			String[] str = line2.split(splitString);
			System.out.println(str[0]);
			if(!user_map_row_counter.contains(str[0])){
				user_map_row_counter.add(str[0]);				
			}
			if(!item_map_row_counter.contains(str[3])){//ha m�g nem volt ilyen �rt�k
				item_map_row_counter.add(str[3]);//elmentem az eredeti id-t az �n id-mel
			}				
		}
		br2.close();


		spd = new SparseDoubleMatrix2D(user_map_row_counter.size(), item_map_row_counter.size());
		spd.assign(0);		
		BufferedReader br1 = new BufferedReader(new FileReader(fileString));
		String line1;
		//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		while ((line1 = br1.readLine()) != null) {
			//line1=line1.replaceAll("\"", "");
			String[] str = line1.split(splitString);
			System.out.println(str[0]);
			int row=0;
			int column=0;				
			if(!user_map.containsKey(str[0])){//ha m�g nem volt ilyen �rt�k
				user_map.put(str[0],user_futo);//elmentem az eredeti id-t az �n id-mel
				row=user_futo;
				user_futo++;
			}else{
				row=user_map.get(str[0]);
			}
			if(!item_map.containsKey(str[3])){//ha m�g nem volt ilyen �rt�k
				item_map.put(str[3],item_futo);//elmentem az eredeti id-t az �n id-mel
				column=item_futo;
				item_futo++;
			}else{
				column=item_map.get(str[3]);
			}
				
			double i=spd.get(row, column);
			i=i+1;
			spd.setQuick(row, column, i);
	
			lines++;		
		}	
		System.out.println(Integer.toString(user_map_row_counter.size())+" "+Integer.toString(item_map_row_counter.size())+" "+Integer.toString(lines));
		System.out.println("olvasas vege");
		br1.close();
	}
}
