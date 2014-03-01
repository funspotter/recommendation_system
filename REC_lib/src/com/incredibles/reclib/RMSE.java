package com.incredibles.reclib;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jblas.DoubleMatrix;

import cern.colt.matrix.impl.SparseDoubleMatrix2D;


/**Calculate error for trained matrix; and test matrix
 * - Needed test matrix
 * - Needed trained matrix*/
public class RMSE {

	public SparseDoubleMatrix2D spd = Readerv2.ReadedMatrix;
	public DoubleMatrix X = Recommender.X; // trained X user factor matrix
	public DoubleMatrix YT = Recommender.YT;	// trained Y transposed item factor matrix
	DoubleMatrix userTrained;
//	StringVector new_user = Reader.new_user;	// Vector<Integer> for web
//	StringVector new_item = Reader.new_item;	// Vector<Integer> for web
	HashMap<String, HashMap<String, Integer>> TestUserItemHm = Readerv2.TestUserItemHm;	// Test dataset informations
	HashMap <String, Integer> user_map = Readerv2.LinkingUserID;	// user string - matrix number
	HashMap <String, Integer> item_map = Readerv2.LinkingItemID;
	
	public void rmseCalculator(){
		for(Entry<String,Integer> entry: user_map.entrySet()){							// user iter�l�s
			Double sumPowError = 0.0;
			Double userNum = 0.0;
			String datasetIDUser = entry.getKey();
			String subString = datasetIDUser.substring(5, 11);
			Double matrixUserNum = Double.parseDouble(subString);
			System.out.println(matrixUserNum);
			Integer matrixIDUser = entry.getValue();
			if(TestUserItemHm.containsKey(datasetIDUser)){	// benne van a test-ben a val�di user id
				userTrained = X.getRow(matrixIDUser).mmul(YT);	// tan�tott m�trix user sora
				double[] userTrainedArray = userTrained.toArray();
				HashMap<String,Integer> ItemNumforUser = TestUserItemHm.get(datasetIDUser);
				for(Entry<String, Integer> entry2: item_map.entrySet()){				// item iter�l�s
					String datasetIDItem = entry2.getKey();
					Integer matrixIDItem = entry2.getValue();
					if(ItemNumforUser.containsKey(datasetIDItem)){
						double trainedValue = userTrainedArray[matrixIDItem];
						Double readedValue = spd.get(matrixIDUser, matrixIDItem);
						Double binaryValue=null;
						if(trainedValue > 1.0){
							trainedValue = 1.0;
						}
						if(!readedValue.equals(0)){
							binaryValue = 1.0;
						}else{
							binaryValue = 0.0;
						}
						Double different = Math.pow(Math.abs(binaryValue-trainedValue), 2.0);	// mikor �s mi�rt j�het ki nan ? pl: user_000473 hiba:NaN
						sumPowError += different;
						userNum++;
					}
				}
			}
			if(userNum != 0){
				System.out.println(datasetIDUser+" hiba:"+ (sumPowError/userNum));
				writeToTxt(matrixUserNum,(Math.sqrt(sumPowError/userNum)));
			}
		}
	}

	public static void writeToTxt(double x, double y){
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(Recommender.path,true));
			 out.write(Double.toString(x) +","+Double.toString(y));
          	 out.newLine();
          	out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
}
