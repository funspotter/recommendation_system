package com.incredibles.reclib;

import java.io.IOException;
import java.sql.SQLException;


public class DavidCos {
	
	public static String runDavidTest () throws SQLException, IOException{
		InputData r = new InputData();
		
		double[][] david_matrix = similarity_function(r.logUserItemMatrix());
		
		return OutputData.doubleMatrixToString(david_matrix);
	}
	
	
	 public static double[][] similarity_function(double user_item[][]){
		    double [][] similarity_matrix=new double [user_item.length][user_item.length];
		    double temp_similarity=0;
		    for(int i=0; i<user_item.length; i++){
		      for(int j=0; j<user_item.length; j++){
		        if(i!=j){
		          double sum_szamlalo=0;
		          double sum_nevezo_i=0;
		          double sum_nevezo_j=0;
		          for(int k=0; k<user_item[0].length; k++){
		            sum_szamlalo=sum_szamlalo+(double)user_item[i][k]*(double)user_item[j][k];
		            sum_nevezo_i=sum_nevezo_i+((double)user_item[i][k]*(double)user_item[i][k]);
		            sum_nevezo_j=sum_nevezo_j+((double)user_item[j][k]*(double)user_item[j][k]);
		          }
		          temp_similarity=sum_szamlalo/(Math.sqrt(sum_nevezo_i)*Math.sqrt(sum_nevezo_j));          
		        }
		        else{
		          temp_similarity=0;
		        }
		        similarity_matrix[i][j]=temp_similarity;
		      }
		    }
		    return similarity_matrix;
		  }
}
