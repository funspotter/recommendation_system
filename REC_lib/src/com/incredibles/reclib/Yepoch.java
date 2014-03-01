package com.incredibles.reclib;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;

import cern.colt.matrix.impl.SparseDoubleMatrix2D;


public class Yepoch {
	
	int X_matrix_row=Recommender.X_matrix_row;
	int X_matrix_column=Recommender.Y_matrix_column;
	int Y_matrix_row=Recommender.Y_matrix_row;
	int Y_matrix_column=Recommender.Y_matrix_column;
	double alpha=Recommender.alpha;
	double lambda=Recommender.lambda;
	int K= Recommender.K;
	SparseDoubleMatrix2D spd = Readerv2.ReadedMatrix;	//Recommender.r.spd;
	DoubleMatrix XT = Recommender.XT;

	
	Yepoch() throws InterruptedException {
	      // Create a new, second thread
	
	   }
	public void startcount() throws InterruptedException{
		Thread thr1 = new Thread(r1);
		Thread thr2 = new Thread(r2);
		Thread thr3 = new Thread(r3);
		Thread thr4 = new Thread(r4);
		Thread thr5 = new Thread(r5);
		thr1.start();
		thr2.start();
		thr3.start();
		thr4.start();
		thr5.start();
		
		thr1.join();
		thr2.join();
		thr3.join();
		thr4.join();
		thr5.join();
	}
	
	
	Runnable r1 = new Runnable() {
		  public void run() {
		   
		    int Y_matrix_row=Recommender.Y_matrix_row;
		      for(int i=0;i<Y_matrix_row;i++){
		    	  if(i%5==0){
		    		 compute(i); 
		    	  }
		      }
		    
		  }
		};
		Runnable r2 = new Runnable() {
			  public void run() {
			   
			    int Y_matrix_row=Recommender.Y_matrix_row;
			      for(int i=0;i<Y_matrix_row;i++){
			    	  if(i%5==1){
			    		 compute(i); 
			    	  }
			      }
			    
			  }
			};
			Runnable r3 = new Runnable() {
				  public void run() {
				   
				    int Y_matrix_row=Recommender.Y_matrix_row;
				      for(int i=0;i<Y_matrix_row;i++){
				    	  if(i%5==2){
				    		 compute(i); 
				    	  }
				      }   
				  }
				};			
				Runnable r4 = new Runnable() {
					  public void run() {
					   
					    int Y_matrix_row=Recommender.Y_matrix_row;
					      for(int i=0;i<Y_matrix_row;i++){
					    	  if(i%5==3){
					    		 compute(i); 
					    	  }
					      }
					    
					  }
					};
					Runnable r5 = new Runnable() {
						  public void run() {
						   
						    int Y_matrix_row=Recommender.Y_matrix_row;
						      for(int i=0;i<Y_matrix_row;i++){
						    	  if(i%5==4){
						    		 compute(i); 
						    	  }
						      }
						    
						  }
						};
	
	
	public void compute(int i) {
		DoubleMatrix X=Recommender.X;
		//DoubleMatrix Y=Recommender.Y;
		DoubleMatrix xtx = Recommender.xtx;
		 
		double vals[]=new double[X_matrix_row];
		//for(int u=0;u<X_matrix_row;u++){
		//	vals[u]=1+alpha*spd.get(u, i); 
		//}
		
		if(Recommender.i==0){	// Tester.i
			//lambda=Tester.fix[Tester.j];
			lambda = Recommender.j;
		}

		double supp=0;
		double data[]=new double[X_matrix_row];
		for(int u=0;u<X_matrix_row;u++){
			double s=spd.get(u, i);
			vals[u]=1+alpha*s; 
			if(spd.get(u, i)==0){
				data[u]=0;
			}else{
				data[u]=1;
				supp++;
			}
		}
		if(Recommender.i==1){	// Tester.i
			//lambda=(1+supp)*Tester.supp[Tester.j];
			lambda = (1+supp)*Recommender.j;
		}
		if(Recommender.i==2){	// Tester.i
			//Math.log10(1+supp)*Tester.logsupp[Tester.j];
			lambda = Math.log10(1+supp)*Recommender.j;
		}

		DoubleMatrix P2 = new DoubleMatrix(X_matrix_row,1,data);
			 
		 DoubleMatrix LI = DoubleMatrix.eye(K).mul(lambda);

		 DoubleMatrix XTX2 = new DoubleMatrix(K,K);
		 XTX2.fill(0);
		 for(int x=0;x<X_matrix_row;x++){
			 if(vals[x]-1!=0){
				 XTX2=XTX2.add(XT.getColumn(x).mmul(X.getRow(x)).mul(vals[x]-1));//it csak .mul-ig volt
			 }
		 }

		 DoubleMatrix temp2 = xtx.add(XTX2).add(LI);

		 DoubleMatrix temp3 = Solve.solve(temp2, DoubleMatrix.eye(temp2.columns));

		 DoubleMatrix temp4 = new DoubleMatrix(K,X_matrix_row);
			temp4.fill(0);
			
			for(int x=0;x<X_matrix_row;x++){
				if(data[x]!=0){
					temp4.putColumn(x, temp3.mmul(XT.getColumn(x)).mul(vals[x]));
				}
					
			}

		 DoubleMatrix Yi = temp4.mmul(P2);

		 for (int col=0;col<K;col++){
			 Recommender.Y.put(i, col, Yi.get(col, 0));
		 }
	

	}


}
