package com.incredibles.reclib;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;


/*- what is T ? T: szétválasztani idõ szerint teszt és tanító részre.
 *- regularization lambda value in pdf : 150;500 : csávó mondta
 *- logLine osztályt használni, kibõvítve type-ot
 *- SparseDoubleMatrix2D ami tudja kezelni a ritka mátrixokat
 *- sztem a getNextLogLine nincs megfelelõen megírva, az általam készített osztállyal kell adatot lekérni*/



public class Tester {
	
	public static int fix[]={1,5,10,50,100,500,1000,10000};
	public static double supp[]={0.1,0.5,1,5,10,50,100};
	public static double logsupp[]={0.5,1,5,10,50};
	public static int i;
	public static int j;

	
	
	public static void runAllTests() throws InterruptedException, SQLException, IOException{
		System.out.println(new SimpleDateFormat("HH:mm:ss:SS").format(new Date()));
		for(i=1;i<2;i++){
			//System.out.println("FIX");
			if(i==0){//0==fix
				for(j=0;j<fix.length;j++){
					System.out.println("FIX ARG: "+Integer.toString(fix[j]));
					Recommender.recommend();
				}
			}
			if(i==1){//1==supp
				for(j=3;j<supp.length-3;j++){
					System.out.println("SUPP ARG: "+Double.toString(supp[j]));
					Recommender.recommend();
				}
				
			}
			if(i==2){//2==logsupp
				for(j=0;j<logsupp.length;j++){
					System.out.println("LOGSUPP ARG: "+Double.toString(logsupp[j]));
					Recommender.recommend();
				}
				
			}
			
		}
		System.out.println(new SimpleDateFormat("HH:mm:ss:SS").format(new Date()));
	}
	

}
