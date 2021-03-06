package com.incredibles.reclib;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;

public class RecommendationAccuracy {
	
	
	public static void valami(){
		
	}
	
	public static HashMap<Integer,Double> accuracyModOne() throws SQLException{
		
		HashMap<Integer,Double> UserRmsHm = new HashMap<Integer,Double>();
		RecommenderDbService dbService = RecommenderDbServiceCreator.createCloud();
		List<Integer> UserArray = new ArrayList<Integer>();
		UserArray = dbService.getUserIdArray();
		HashMap<Integer,Double> EventRankhm = new HashMap<Integer,Double>();
		HashMap<Integer,Integer> eventNumberInLogtoUser = new HashMap<Integer,Integer>();
		
		
		double rmstoUser = 0;
		
		for(int i=0; i<UserArray.size(); i++){	// usereken megy vegig
			int UserId = UserArray.get(i);
			EventRankhm = dbService.getEventIdArrayForXYUserV2(UserId); // event-rank
			//eventNumberInLogtoUser = dbService.eventNumberInLogtoUser(UserId); // event - log f�jlban megjelenes szama adott usernek
			
			int eventcountermax = 1;			// userhez tartozo max kattintas
			for(Entry<Integer, Integer> entry : eventNumberInLogtoUser.entrySet()){
				 if(entry.getValue()>eventcountermax){
					 eventcountermax=entry.getValue();
				 }
			}
			System.out.println(eventcountermax);
			
			boolean newUser=true;
			double sum = 0;
			double ratio = 0;
			int eventcounter = 0;
			double differential_negyzet = 0;
			
			for (Entry<Integer, Double> entry : EventRankhm.entrySet()) {	// adott user eventjein megy vegig
		 		Integer eventid = entry.getKey();
				Double rank = entry.getValue();	
				try{
					eventcounter = eventNumberInLogtoUser.get(eventid);
					ratio = (double)eventcounter/(double)eventcountermax;
					differential_negyzet = Math.pow(ratio - Math.pow(rank, 2.0), 2.0);	// matek shit
					sum = sum + differential_negyzet;
					RecommendationAccuracy.writeToTxt(rank, ratio, newUser);
					newUser = false;
				}catch(NullPointerException e){}
			}	
			rmstoUser = Math.sqrt(sum/(double)eventNumberInLogtoUser.size());
			UserRmsHm.put(UserId, rmstoUser);
		}
		return UserRmsHm;	
	}
	
	public static HashMap<Integer,Double> accuracyModTwo() throws SQLException{
		
		HashMap<Integer,Double> UserRmsHm = new HashMap<Integer,Double>();
		RecommenderDbService dbService = RecommenderDbServiceCreator.createCloud();
		List<Integer> UserArray = new ArrayList<Integer>();
		UserArray = dbService.getUserIdArray();
		HashMap<Integer,Double> EventRankhm = new HashMap<Integer,Double>();
		HashMap<Integer,Integer> eventNumberInLogtoUser = new HashMap<Integer,Integer>();
		
		double rmstoUser = 0;
		
		for(int i=0; i<UserArray.size(); i++){	// usereken megy vegig
			int UserId = UserArray.get(i);
			EventRankhm = dbService.getEventIdArrayForXYUserV2(UserId); // event-rank
			//eventNumberInLogtoUser = dbService.eventNumberInLogtoUser(UserId); // event - log f�jlban megjelenes szama adott usernek
			
			int eventcountermax = 1;			// userhez tartozo max kattintas
			for(Entry<Integer, Integer> entry : eventNumberInLogtoUser.entrySet()){
				 if(entry.getValue()>eventcountermax){
					 eventcountermax=entry.getValue();
				 }
			}
			
			double sum = 0;
			double ratio = 0;
			int eventcounter = 0;
			double differential_negyzet = 0;
			
			for (Entry<Integer, Double> entry : EventRankhm.entrySet()) {	// adott user eventjein megy vegig
		 		Integer eventid = entry.getKey();
				Double rank = entry.getValue();
				try{
					eventcounter = eventNumberInLogtoUser.get(eventid);
					ratio = (double)eventcounter/(double)eventcountermax;
					differential_negyzet = Math.pow(ratio - Math.pow(rank, 3.0), 2.0);	// matek shit
					sum = sum + differential_negyzet;
				}catch(NullPointerException e){
					
				}
			}
			
			rmstoUser = Math.sqrt(sum/(double)eventNumberInLogtoUser.size());
			UserRmsHm.put(UserId, rmstoUser);
		}
		
		
		return UserRmsHm;
	}

	
	public static void accuracyModThree() throws SQLException {
		
	//	HashMap<Integer,Double> UserRmsHm = new HashMap<Integer,Double>();
		RecommenderDbService dbService = RecommenderDbServiceCreator.createCloud();
		List<Integer> UserArray = new ArrayList<Integer>();
		UserArray = dbService.getUserIdArray();
		HashMap<Integer,Double> EventRankhm = new HashMap<Integer,Double>();
		HashMap<Integer,Integer> eventNumberInLogtoUser = new HashMap<Integer,Integer>();
		
		
	//	double rmstoUser = 0;
		
		for(int i=0; i<UserArray.size(); i++){	// usereken megy vegig
			int UserId = UserArray.get(i);
			EventRankhm = dbService.getEventIdArrayForXYUserV2(UserId); // event-rank
			//eventNumberInLogtoUser = dbService.eventNumberInLogtoUser(UserId); // event - log f�jlban megjelenes szama adott usernek
			
			int summaKattintas = 0;			// userhez tartozo osszes kattintas
			for(Entry<Integer, Integer> entry : eventNumberInLogtoUser.entrySet()){
				summaKattintas = summaKattintas + entry.getValue();
			}						
			
			boolean newUser=true;
			double ratio = 0;
			int eventcounter = 0;
			int eventcountermax=1;			// userhez tartozo max kattintas
			
		if(summaKattintas!=0){			
			for(Entry<Integer, Integer> entry : eventNumberInLogtoUser.entrySet()){
				 if(entry.getValue()>eventcountermax){
					 eventcountermax=entry.getValue();
				 }
			}			
			for (Entry<Integer, Double> entry : EventRankhm.entrySet()) {	// adott user eventjein megy vegig
		 		Integer eventid = entry.getKey();
				Double rank = entry.getValue();	
				try{
					eventcounter = eventNumberInLogtoUser.get(eventid); // eventcounter lehet nulla is, mert ami nincs a log fajlban azt beletesszuk nulla value-vel
					if (rank != 0 && eventcounter!=0){
						ratio = ((double)eventcounter/(double)eventcountermax)/rank;
						RecommendationAccuracy.writeToTxt(summaKattintas, ratio, newUser);
						System.out.println("summakatt: "+summaKattintas+",   ratio: "+ratio);
						newUser = false;
					}
				}catch(NullPointerException e){}
			}
		  }
		}
		
	}
	
	
	 // txt fajlba kiirja a koordinatakat
    public static void writeToTxt(double x, double y, boolean newUser){
//    	BufferedWriter out;
//        try {
//            //replace helloworld.txt with the name of the file
//            out = new BufferedWriter(new FileWriter("C:/Users/Otthon/Desktop/newcoords.txt"));
//            //Write out the specified string to the file
//           if(newUser==false){
//        	   out.write(Double.toString(x));
//        	   out.write(Double.toString(y));
//           }else{
//        	   out.write('n');		// new user
//        	   out.write(Double.toString(x));
//        	   out.write(Double.toString(y));
//           }
//            
//        //flushes and closes the stream
//            out.close();
//        }catch(IOException e){
//            System.out.println("There was a problem:" + e);
//        }

    	BufferedWriter out;
    	        try {
    	            //replace helloworld.txt with the name of the file CHANGE after
    	            //you declare which file to write to add the boolean true which
    	            // will stop it from replacing the helloworld.txt with a new one.
    	        	out = new BufferedWriter(new FileWriter("C:/Users/Otthon/Desktop/newcoords.txt",true));
    	            //out = new BufferedWriter(new FileWriter("helloworld.txt",true));
    	            //Write out a string to the file
    	        	 if(newUser==false){
//    	          	   out.write(Double.toString(x));
//    	          	   out.newLine();
//    	          	   out.write(Double.toString(y));
    	          	 out.write(Double.toString(x) +","+Double.toString(y));
    	          	 out.newLine();
    	             }else{
    	     //     	   out.write('n');		// new user
    	          	  
    	          	   out.write(Double.toString(x) +","+Double.toString(y));
    	          	 out.newLine();
    	             }
    	            //write a new line to the file so the next time you write
    	            //to the file it does it on the next line
    	         //   out.newLine();
    	            //flushes and closes the stream
    	            out.close();
    	        }catch(IOException e){
    	            System.out.println("There was a problem:" + e);
    	        }
    	    }
    	
    }
