package com.incredibles.reclib;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Vector;

import org.jblas.DoubleMatrix;

import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;


public class Recallv2 {
	
	public class EvaluateValues{
		public double recallvalue;
		public double precision;
	}
	// Most csak azt tudjuk mire kattintott. illetve azon belül még függ a kattintások számától a valós érdeklõdés.
	// Ha lekérnénk azoknak az eseményeknek a számát, amelyek a teszt adatsor kezdetétõl hozzáférhetõek, akkor meglenne minden event száma -> minden - szeretett = nem szeretett
	
	/** Calculate X*YT to get trained event rank values*/
	DoubleMatrix userTrained;
	DoubleMatrix X = Recommender.X;
	DoubleMatrix YT = Recommender.YT;
	DoubleMatrix Y;
	/**Stores the places of the events, after sorted by decrease rank value*/
	double index[];
	/**The top X highest rank*/
	int topXitem;
	/**True positive: condition - true; test outcome - true*/
	double TP;
	/**False Negative: condition - true; test outcome - false*/
	double FN;
	/**False Positive: condition - negative; test outcome - positive*/
	double FP;
	/**True Negative: condition - false; test outcome - false*/
	double TN;
	/**Counts all of the events for one user, how much events click-number is minTPClickNum*/
	double summapositive=0;
	/**Store for all user the recall values*/
	HashMap<Integer, EvaluateValues> UserStats = new HashMap<Integer, EvaluateValues>();
	HashMap<String, HashMap<String, Integer>> TestUserItemHm = Readerv2.TestUserItemHm;	// Test dataset informations
	HashMap <String, Integer> user_map = Readerv2.LinkingUserID;	// user string - matrix number
	HashMap <String, Integer> item_map = Readerv2.LinkingItemID;
	HashMap<Integer,Double> topXHm = new HashMap<Integer, Double>();
	/**Min number of click num. to count event as liked*/
	int minTPClickNum;
	int user_count;
	
	/** Calculate Recall value to evaluate recommendation*/
	public void apiRecallCalculator(){
		//Set properly with the reader from api time interval datas
		List<Integer> legitEventList = legitEventNumber(new Date(1387198467000L), new Date(1209600000L+1387198467000L));
		int legitEventNum = legitEventList.size();
		topXitem = 100;
		minTPClickNum = 1;
		for(Entry<String, Integer> entry: user_map.entrySet()){
			String realUserIDSTRING = entry.getKey();
			Integer realUserID = Integer.parseInt(realUserIDSTRING);
			Integer matrixUserID = entry.getValue();
			if(TestUserItemHm.containsKey(realUserIDSTRING)){
				TP=0;
				FN=0;
				FP=0;
				TN=0;
				summapositive=0;				
				HashMap<String,Integer> ItemNumforUser = TestUserItemHm.get(realUserIDSTRING); // TODO : gyakrabban kattintó userre vizsgálni csak. pl..	
				System.out.println(realUserIDSTRING);
				 // userID is in the test set
				topXHm.clear();
				userTrained = X.getRow(matrixUserID).mmul(YT); // teached values for one user
				double[] userTrainedArray = userTrained.toArray();
				index=new double[userTrainedArray.length];
				
				for(int i=0;i<userTrainedArray.length;i++){
					userTrainedArray[i]+=Math.random()*0.00001;// little random number to handle sorting
					index[i]=i; // i. helyen van az i. item
				}
				
				userTrainedArray=QuickSort(userTrainedArray, 0, userTrainedArray.length-1);// rendezzük növekvõ sorrendbe
				
				for(int i= userTrainedArray.length-1; i >= 0; i--){	// store topXitem in hm.
					int key = (int) index[i];
					if(legitEventList.contains(key)){	// a TOPX elemet a legit eventekbõl kiválasztani (régi eventek ne legyenek benne)
						topXHm.put(key, userTrainedArray[i]);
					}
				}
				
				for(Entry<String, Integer> entry3: ItemNumforUser.entrySet()){	// count all event with positive condition
					String stringEventID = entry3.getKey();
					int eventID = Integer.parseInt(stringEventID);
					int clickNum = entry3.getValue();
					if(clickNum >= minTPClickNum){
						summapositive++;
					}
					if(topXHm.containsKey(eventID)){	// calculate true positive; false negative number for topX
						//Double trainedRank = topXHm.get(eventID);
						if(clickNum>=minTPClickNum){
							TP++;
						}
					}
				}
				FN = summapositive - TP;
				FP = topXitem - TP;
				TN = legitEventNum - summapositive - FP;
				
				
				// After evaluate for each user the TP... FN values
				System.out.println();
				System.out.println("userid: "+realUserID);
				System.out.println();
				System.out.println("TP:"+TP);
				System.out.println("FN:"+FN);
				System.out.println("FP:"+FP);
				System.out.println("TN:"+TN);
				System.out.println("summapositive:"+summapositive);
				System.out.println("legitEventNumber:"+legitEventNum);
				
				Double recallValue = TP/ (TP + FN);
				Double precision = TP/ (TP + FP);
				System.out.println("recall:"+recallValue);
				System.out.print("precision: "+precision);
				EvaluateValues values = new EvaluateValues();
				values.recallvalue = recallValue;
				values.precision = precision; 
				UserStats.put(realUserID, values);
			
			}
		}
		System.out.println("SANYI");
		for(Entry<Integer, EvaluateValues> entry: UserStats.entrySet()){
			Integer userID = entry.getKey();
			EvaluateValues numbers = entry.getValue();
			System.out.println("User "+userID+" - recall:"+numbers.recallvalue+"  precision:"+numbers.precision);
		}
	}
	
	
	public void printMatrix(){
		X=Recommender.X;
		Y=Recommender.Y;
		YT=Recommender.Y.transpose();
		
		//X.print();
		//YT.print();
		
		DoubleMatrix p = X.mmul(YT);
		p.print();
	}
	
	
	public List<Integer> legitEventNumber(Date from, Date to){
		RecommenderDbService dbService = null;
		List<Integer> legitEventList = null;
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			legitEventList = dbService.getLegitEventsIdV2(from, to);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if (dbService != null) {
				try {
					dbService.close();
				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		return legitEventList;
	}
	
	
	
	public double[] QuickSort(double T[], int lo0, int hi0) {
	    int lo = lo0;
	    int hi = hi0;
	    double mid;

	    if ( hi0 > lo0){
	        mid = T[ ( lo0 + hi0 ) / 2 ];
	        while( lo <= hi ) {
	            while( ( lo < hi0 ) && ( T[lo] < mid )) ++lo;
	            while( ( hi > lo0 ) && ( T[hi] > mid )) --hi;
	            if( lo <= hi ){
	                swap(T, lo, hi);
	                swap(index,lo,hi);
	                ++lo;
	                --hi;
	            }
	        }
	        if( lo0 < hi ) QuickSort( T, lo0, hi );
	        if( lo < hi0 ) QuickSort( T, lo, hi0 );
	    }
	    
	    return T;
	}

	public void swap(double T[], int i, int j) {
	    double tmp= T[i]; 
	    T[i] = T[j];
	    T[j] = tmp;
	}
	
}
