package com.incredibles.reclib;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;

import cern.colt.matrix.impl.SparseDoubleMatrix2D;


public class Xepoch {
	int X_matrix_row=Recommender.X_matrix_row;
	int X_matrix_column=Recommender.Y_matrix_column;
	int Y_matrix_row=Recommender.Y_matrix_row;
	int Y_matrix_column=Recommender.Y_matrix_column;
	double alpha=Recommender.alpha;
	double lambda=Recommender.lambda;
	int K= Recommender.K;
	SparseDoubleMatrix2D spd = Readerv2.ReadedMatrix;	// this is the readed matrix
	DoubleMatrix YT = Recommender.YT;
	
	Xepoch() throws InterruptedException {
	      // Create a new, second thread
	
	   }
	public void startcount() throws InterruptedException{

		/*ez a többszálonfuttatós dolog*/
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
		    int X_matrix_row=Recommender.X_matrix_row;
		      for(int i=0;i<X_matrix_row;i++){
		    	  if(i%5==0){	/*calculate individual rows in different thread with modulo*/
		    		 compute(i); 
		    	  }
		      }
		    
		  }
		};
		Runnable r2 = new Runnable() {
			  public void run() {
			   
			    int X_matrix_row=Recommender.X_matrix_row;
			      for(int i=0;i<X_matrix_row;i++){
			    	  if(i%5==1){
			    		 compute(i); 
			    	  }
			      }
			    
			  }
			};
			Runnable r3 = new Runnable() {
				  public void run() {
				   
				    int X_matrix_row=Recommender.X_matrix_row;
				      for(int i=0;i<X_matrix_row;i++){
				    	  if(i%5==2){
				    		 compute(i); 
				    	  }
				      }
				    
				  }
				};
				Runnable r4 = new Runnable() {
					  public void run() {
					   
					    int X_matrix_row=Recommender.X_matrix_row;
					      for(int i=0;i<X_matrix_row;i++){
					    	  if(i%5==3){
					    		 compute(i); 
					    	  }
					      }
					    
					  }
					};
					Runnable r5 = new Runnable() {
						  public void run() {
						   
						    int X_matrix_row=Recommender.X_matrix_row;
						      for(int i=0;i<X_matrix_row;i++){
						    	  if(i%5==4){
						    		 compute(i); 
						    	  }
						      }
						    
						  }
						};
		
		/*recalculate user factor for u item user row*/
		public void compute(int u) {
			DoubleMatrix Y=Recommender.Y;
			DoubleMatrix yty = Recommender.yty;		
			double vals[]=new double[Y_matrix_row];	/*confidence level vals[]*/
			if(Recommender.i==0){// fix Tester.i
				//lambda=Tester.fix[Tester.j];// atirni
				lambda = Recommender.j;
			}		 
			double supp=0;
			double data[]=new double[Y_matrix_row];	/*binary vector; like;dont like*/
			for(int i=0;i<Y_matrix_row;i++){
				double s=spd.get(u, i);				/*click num for item i;from user u*/
				vals[i]=1+alpha*s; 					/*confidence level vals[]*/
													/*we could use another math to calc...*/
				if(s==0){
					data[i]=0;
				}else{
					data[i]=1;
					supp++;							/*counting how many different item was clicked by user*/
				}
			}
				 
			if(Recommender.i==1){// supp // Tester.i
				//lambda=(1+supp)*Tester.supp[Tester.j];
				lambda = (1+supp)*Recommender.j;
			}
			if(Recommender.i==2){// logsupp Tester.i
				//lambda=Math.log10(1+supp)*Tester.logsupp[Tester.j];
				lambda = Math.log10(1+supp)*Recommender.j;
			}
				
			DoubleMatrix P2 = new DoubleMatrix(Y_matrix_row,1,data);		// vector : row;column data..
			DoubleMatrix LI = DoubleMatrix.eye(K).mul(lambda);
			DoubleMatrix YTY2 = new DoubleMatrix(K,K);
			
			YTY2.fill(0);
			for(int x=0;x<Y_matrix_row;x++){
				if(vals[x]-1!=0){		/*vals[x] always bigger or equal to 1; vals[x]==0 if spd.get(u,i)==0; not clicked event*/
					YTY2=YTY2.add(((YT.getColumn(x)).mmul(Y.getRow(x))).mul(vals[x]-1));//itt csak .mul-ig volt
				}
			}
			
			DoubleMatrix temp2 = yty.add(YTY2).add(LI);
			DoubleMatrix temp3 = Solve.solve(temp2, DoubleMatrix.eye(temp2.columns)); // solving linear equation invertalas

			DoubleMatrix temp4 = new DoubleMatrix(K,Y_matrix_row);
			temp4.fill(0);
			for(int x=0;x<Y_matrix_row;x++){
				if(data[x]!=0){				// where user u clicked..
					temp4.putColumn(x, temp3.mmul(YT.getColumn(x)).mul(vals[x])); // ez mi ???
				}
					
			}

			DoubleMatrix Xu = temp4.mmul(P2);
			for (int col=0;col<K;col++){
				Recommender.X.put(u, col, Xu.get(col, 0));
			}	 
		}
}
