package com.incredibles.reclib;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

import org.jblas.DoubleMatrix;

import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;

/**VALAMIT KIKOMMENTEZTEM !!!*/
public class Recall {
	DoubleMatrix user_tanitott;
	DoubleMatrix X;
	DoubleMatrix YT;
	DoubleMatrix Y;
	double index[];
	int user_count;
	HashSet <Integer> tophusz; 
	Vector<Integer> user;
	Vector<Integer> item;
	HashMap<String, Integer> user_map;//eredeti id - nalam id
	int recallcounter=0;
	HashMap<String, Integer> item_map;
	
	HashSet<Integer> coverage;
	
	HashSet<Integer> all_new_user;	// ez mi ?
	
	public void store() throws SQLException{
		user_count=Recommender.X_matrix_row;
		X=Recommender.X;
		Y=Recommender.Y;
		YT=Recommender.Y.transpose();
		tophusz=new HashSet<Integer>();
		user_map=Readerv2.LinkingUserID;
		item_map=Readerv2.LinkingItemID;
		Random rnd = new Random();
		rnd.setSeed(1);
		recallcounter=0;
		coverage = new HashSet<Integer>();
		all_new_user = new HashSet<Integer>();
		
		RecommenderDbService dbService = null;
		dbService = RecommenderDbServiceCreator.createCloud();
		
		
		for(int j=0;j<X.rows;j++){
			 int user_id=j;
			 user_tanitott=X.getRow(user_id).mmul(YT);	// megkapjuk az ials végereményét adott userre
			 double[] user_tanitott_array = user_tanitott.toArray();
			 for(int i=0;i<user_tanitott_array.length;i++){
				 user_tanitott_array[i]+=rnd.nextDouble()*0.000001;
			 }
			 index=new double[user_tanitott_array.length];
			 for(int i=0;i<user_tanitott_array.length;i++){
					index[i]=i;	// 0. helyen van a 0. item
			}
	
			 
			 user_tanitott_array=QuickSort(user_tanitott_array, 0, user_tanitott_array.length-1);	// rendezzük csökkenő sörrendben i guess

			 HashSet<Integer> storedIndex = new HashSet<Integer>();    
			 
			 for(int i=user_tanitott_array.length-1;i>0;i--){//user_tanitott_array.length-6 top 6
				 int uj=0;//j user atmeppelt ID-ja
				 int ui=0;//index[i] item atmeppelt IDJ-ja
				 int ur=0;//i rank
				 for (String key  : item_map.keySet()){
					 if(index[i]==(item_map.get(key))){
						 ui=Integer.parseInt(key);
					 }
				 }
				 for (String key  : user_map.keySet()){
					 if(j==(user_map.get(key))){
						 uj=Integer.parseInt(key);
					 }
				 }
				 //ur=i+6-user_tanitott_array.length;
				 ur=user_tanitott_array.length-i;
				 //dbService.uploadRecommendation(uj, ui, ur);
				 
				 //TODO:mas modon kell eltarolni
				 //dbService.uploadWeightedRecommendation(uj, ui, ur);
			}
			  
		}
	}
	
	public void make_recall(){
		user_count=Recommender.X_matrix_row;
		X=Recommender.X;
		Y=Recommender.Y;
		YT=Recommender.Y.transpose();
//		user=Recommender.r.new_user;
//		item=Recommender.r.new_item;
		user_map=Recommender.r.user_map;
		item_map=Recommender.r.item_map;
		tophusz=new HashSet<Integer>();
		
		Random rnd = new Random();
		rnd.setSeed(1);
		recallcounter=0;
		
		coverage = new HashSet<Integer>();
		
		all_new_user = new HashSet<Integer>();
		
		for(int j=0;j<X.rows;j++){
			 int user_id=j;
			 user_tanitott=X.getRow(user_id).mmul(YT);
			 double[] user_tanitott_array = user_tanitott.toArray();
			 for(int i=0;i<user_tanitott_array.length;i++){
				 user_tanitott_array[i]+=rnd.nextDouble()*0.000001; // sorbarendezés miatt kell hogy különbözőek legyenek
			 }
			 index=new double[user_tanitott_array.length];
			 for(int i=0;i<user_tanitott_array.length;i++){
					index[i]=i;	// sorbarendezls után változik a sorrend
				}
			 user_tanitott_array=QuickSort(user_tanitott_array, 0, user_tanitott_array.length-1);
			 //uccso 5 elem			 
			 HashSet<Integer> storedIndex = new HashSet<Integer>();    
			 
			 for(int i=user_tanitott_array.length-1;i>user_tanitott_array.length-11;i--){	// top 10 a sorbarendezett cuccban			 
				 coverage.add((int)index[i]);	 
			 }
			 for (String key  : item_map.keySet()){
				 if(storedIndex.contains(item_map.get(key))){
					 System.out.println(key);
				 }
			 }  
		}
		
	
		
		//recall megjobb
		for(int j=0;j<user.size();j++){				// user iteration
			Integer userElementAtJ=user.get(j);
			if(all_new_user.add(userElementAtJ)){
				 if(user_map.containsKey(userElementAtJ)){
					 //az ut�bbi 1 h�tben volt olyan user akire tan�tottunk is
					 int user_id=user_map.get(userElementAtJ);
					 user_tanitott=X.getRow(user_id).mmul(YT);
					 double[] user_tanitott_array = user_tanitott.toArray();
					 for(int i=0;i<user_tanitott_array.length;i++){
						 user_tanitott_array[i]+=rnd.nextDouble()*0.000001;
					 }
					 index=new double[user_tanitott_array.length];
					 for(int i=0;i<user_tanitott_array.length;i++){
							index[i]=i;
						}
					 user_tanitott_array=QuickSort(user_tanitott_array, 0, user_tanitott_array.length-1);
						
					 //uccso 5 elem
					 for(int i=user_tanitott_array.length-1;i>user_tanitott_array.length-11;i--){
						//System.out.println("index: "+Double.toString(index[i])+" item: "+Double.toString(user_tanitott_array[i]));
						 tophusz.add((int)index[i]);//adott user top20-a
					} 
				 }
			}
		
			 for(int i=0;i<item.size();i++){
				 if(userElementAtJ.equals(user.elementAt(i))){
					 if(tophusz.contains(item_map.get(item.elementAt(i)))){
						 recallcounter++;
					 }
				 }
			 }
			 
			 
			 
			
			tophusz.clear();
		
			
		 }
		
				
		//System.out.println("RECALL");
		//System.out.println(Integer.toString(recallcounter));
		//System.out.println(Integer.toString(item.size()));
		float f = (float)recallcounter/(float)item.size();
		System.out.println("RECALL VEGEREDMENY: " + Float.toString(f) + " "+Integer.toString(recallcounter)+" "+Integer.toString(item.size()));
		
		//System.out.println("COVERAGE");
		//System.out.println(Integer.toString(coverage.size()));
		//System.out.println(Integer.toString(YT.columns));
		float g = (float)coverage.size()/(float)YT.columns;
		System.out.println("COVERAGE VEGEREDMENY: " + Integer.toString(coverage.size())+" "+YT.columns);

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
