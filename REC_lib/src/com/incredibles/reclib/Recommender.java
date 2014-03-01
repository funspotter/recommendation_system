package com.incredibles.reclib;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

import org.jblas.DoubleMatrix;

/**Class for execute iALS algorithm, with different variables, to make recommendation
 * correct*/
public class Recommender {

	/**Regularization const. calculated with cross correlation*/
	public static double lambda=2;
	/**Set confidence level, how to calculate event clicks*/
	public static double alpha=40;
	/**Item factor; user factor matrix smaller dimension*/
	public static int K=2;
	
	/**User factor matrix [userNum * K]*/
	public static DoubleMatrix X;
	/**Item factor matrix [itemNum * K]*/
	public static DoubleMatrix Y;
	/**Item factor matrix tranpose multiply with item factor matrix [K * K]*/
	public static DoubleMatrix yty;
	/**User factor matrix tranpose multiply with user factor matrix [K * K]*/
	public static DoubleMatrix xtx;
	/**User factor matrix tranpose [K * userNum]*/
	public static DoubleMatrix XT;
	/**Item factor matrix tranpose [K * itemNum]*/
	public static DoubleMatrix YT;

	/**Shows how many items are in the matrix system*/
	public static int Y_matrix_row;
	/**K value*/
	public static int Y_matrix_column;
	
	/**Shows how many users are in the matrix system*/
	public static int X_matrix_row;
	/**K value*/
	public static int X_matrix_column;
	
	/**Select 0=fix; 1=supp; 2=logsupp*/
	public static int i;
	/**Value of fix;supp;logsupp value*/
	public static double j;
	/**where to save the created measure eredmeny*/
	public static String path;
	
	
	
	public static Reader r;

	public static void read(int T) throws SQLException, IOException{
		r = new Reader();
		r.T=T;
		r.read_file();
		//r.readFileWithoutDate();
		//r.read_from_api();	/*set user-item matrix*/
		
		X_matrix_row = r.spd.rows();	/*number of users*/
		X_matrix_column = K;			/*user-factor 2nd dimension*/
		
		Y_matrix_row = r.spd.columns();	/*number of item*/
		Y_matrix_column = K;			/*item-factor 2nd dimension*/
	}
	

	public static void newRecFunction(){
		int fixsupplogsupp[] = {0,1,2};
		int fixertekek[] = {1,5,10,50,100,500,1000,10000};
		double suppertekek[] = {0.1,0.5,1,5,10,50,100};
		double logsuppertekek[] = {0.5,1,5,10,50};
		//ReaderFromFile newRead = new ReaderFromFile();
		//newRead.ExecuteReading();
		ReaderFromApi newRead = new ReaderFromApi();
		newRead.ExecuteReading();
		X_matrix_row = newRead.ReadedMatrix.rows();//r.spd.rows();	/*number of users*/
		X_matrix_column = K;			/*user-factor 2nd dimension*/	
		Y_matrix_row = newRead.ReadedMatrix.columns();//r.spd.columns();	/*number of item*/
		Y_matrix_column = K;			/*item-factor 2nd dimension*/
		X = DoubleMatrix.rand(X_matrix_row, X_matrix_column);
		Y = DoubleMatrix.rand(Y_matrix_row, Y_matrix_column);
		for(int x=0; x< 1/*fixsupplogsupp.length*/; x++){
			i = 2;//fixsupplogsupp[x];	// set what do you want.. fix;supp; logsupp
			if(i==0){
				for(int y=0; y<fixertekek.length; y++){
					j = fixertekek[y];	// set values of fix;supp;logsupp
					String filename = "fixertek_"+j;
					path = "D:"+File.separator+"time_intervall_measure"+File.separator+filename+".txt";
					for(int epochNumber= 0; epochNumber<10; epochNumber++){
						try {
							iALS_epoch();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}		
					//RMSE valami = new RMSE();
					//valami.rmseCalculator();
				}
			}else if(i==1){
				for(int y =0; y<suppertekek.length; y++){
					j = suppertekek[y];
					String filename = "suppertek_"+j;
					path = "D:"+File.separator+"time_intervall_measure"+File.separator+filename+".txt";
					for(int epochNumber= 0; epochNumber<10; epochNumber++){
						try {
							iALS_epoch();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}		
					RMSE valami = new RMSE();
					valami.rmseCalculator();
				}		
			}else if(i==2){
				for(int y =0; y<1;/*logsuppertekek.length;*/ y++){
					j=10;//logsuppertekek[y];
					String filename = "logsupp_"+j;
					path = "D:"+File.separator+"time_intervall_measure"+File.separator+filename+".txt";
					for(int epochNumber= 0; epochNumber<10; epochNumber++){
						try {
							iALS_epoch();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}		
					//RMSE valami = new RMSE();
					//valami.rmseCalculator();	
					//UltimateiALS valami = new UltimateiALS();
					//valami.UploadIALS();
					Recallv2 elem = new Recallv2();
					elem.apiRecallCalculator();
				}
			}
		}
		
	}
	
	
	static void recommend() throws InterruptedException, SQLException, IOException{
		int T = 10000; /*T value is always zero. WHYYYYYY?*/
		
		//System.out.println("RecommendWithTime: "+Integer.toString(T));
		 read(T);
		 Random rnd = new Random();
		 rnd.setSeed(1);	

		 X = DoubleMatrix.rand(X_matrix_row, X_matrix_column);// ha jól emlékszem elég az egyiket feltölteni random számokkal mert a másik kiszámolódik..?
		 Y = DoubleMatrix.rand(Y_matrix_row, Y_matrix_column);

		for (int i = 0; i < 10; i++) {
			//System.out.println("EPOCHNUM: "+Integer.toString(i));
			iALS_epoch();	/*recalculating X;Y matrix*/
		}
		//Recall rec = new Recall();
		//rec.make_recall();
		
		
	
		/*NEW TESTING CLASS: RMSE*/
		RMSE valami = new RMSE();
		valami.rmseCalculator();
		
		
		//TODO:
		//rec.store(); //mas modon kell eltarolni
		
		//rec.printMatrix();
	}
	
	/**Counts one epoch for Y and X matrix*/
	protected static void iALS_epoch() throws InterruptedException{
		yty = Y.transpose().mmul(Y);
		YT = Y.transpose();
		Xepoch x=new Xepoch();
		x.startcount();
 
		xtx = X.transpose().mmul(X);
		XT=X.transpose();
		Yepoch y=new Yepoch();
		y.startcount();
	 }
	
}
