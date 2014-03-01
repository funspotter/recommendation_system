package com.incredibles.reclib;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cern.colt.matrix.impl.SparseDoubleMatrix2D;

/**Read from CSV and TSV predefined files. Set Test(optional) and Train dataset
 * - Need to select the dataset log file
 * - Select the Test dataset function*/
public class ReaderFromFile extends Readerv2 {

	/**Capable to read from different files
	 *LAST_FM_1K_FILE ==0; --> Dataset info : http://www.dtic.upf.edu/~ocelma/MusicRecommendationDataset/lastfm-1K.html*/
	public static final int LAST_FM_1K_FILE = 0;
	
	/*Set Log line splited string infos to different datasets*/
	private int userIdInLogLine;
	private int itemIdInLogLine;
	private int timestampIdInLogLine;
	private String filePath;
	private String splitStringWith;
	private SimpleDateFormat formateDatasetDate;
	
	/**Set Log line splited string infos to different datasets
	 * If have a new dataset add new case to the function and
	 * implement and/or declare new variables
	 * LAST_FM_1K_FILE = 0*/
	public void setInfosForDataset(int fileNumber){
		switch(fileNumber){
		case 0: 
			userIdInLogLine = 0;
			itemIdInLogLine = 3;	/*Artist id here, not song id..*/
			timestampIdInLogLine = 1;
			filePath = new String("C:/Downloads/lastfm-dataset-1K/userid-timestamp-artid-artname-traid-traname.tsv");
			splitStringWith = new String("\t");
			formateDatasetDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			break;
		default:
			System.out.println("NOT DECLARED FILENUMBER");
			break;
		}
	}
	
	/**Read from a specific file; creates specific Test set*/
	public void ExecuteReading(){
		BufferedReader readingFromFile= null; 		/*Using this to read from file*/
		/*TODO: CREATE A MENU TO SELECT DATASET TO EXAME*/
		setInfosForDataset(LAST_FM_1K_FILE);		/*Set Last.fm file informations*/
		Date startDate1 = new Date();
		startDate1.setTime(1230829767000L);
		Long intervall = 604800000L; // One WEEK in milisec.
		setTestDateWithTimeIntervall(startDate1, intervall);
		Integer selectTestSetCreator = TEST_WITH_TIME_INTERVALL;
		/*TODO: esetleg beolvasni lehetne a kezdeti dátumot és az intervallumot hogy ne legyen túlságosan statikus a kód*/
		try {
			readingFromFile = new BufferedReader(new FileReader(filePath));	/*Reach file to read*/
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String readedLine;
		int lineNumber = 0;
		try {
			/*TODO: CREATE A MENU, TO SELECT TEST DATA MAKER FUNCTION*/	
			/*FIRST READ : Set Test Dataset and Collect the (1-test_dataset) real userID-s and itemID-s and counting them*/
			if(selectTestSetCreator.equals(TEST_WITH_TIME_INTERVALL)){			/* TEST_WITH_TIME_INTERVALL */
				while ((readedLine = readingFromFile.readLine()) != null){
					String[] str = readedLine.split(splitStringWith);
					String userID = str[userIdInLogLine];
					String timestamp = str[timestampIdInLogLine];
					String itemID = str[itemIdInLogLine];
					Date convertedDate = formateDatasetDate.parse(timestamp.toString().substring(0, 20));
					System.out.println(userID);
					
					if(!userSet.contains(userID)){	/*Collect all real userid which has log */
						userSet.add(userID);		
					}
					if(!itemSet.contains(itemID)){	/*Collect all real itemid which has log */
						itemSet.add(itemID);
					}
					
					if(startDate.compareTo(convertedDate)<0 && endDate.compareTo(convertedDate)>0){	/*if convertedDate is inside of the intervall, so it is in the test part*/
						if(TestUserItemHm.containsKey(userID)){
							HashMap<String, Integer> UserHm = TestUserItemHm.get(userID);
							if(UserHm.containsKey(itemID)){
								Integer number = UserHm.get(itemID);
								UserHm.put(itemID, number+1);
								TestUserItemHm.put(userID, UserHm);
							}else{
								UserHm.put(itemID, 1);
								TestUserItemHm.put(userID, UserHm);
							}
						}else{
							HashMap<String, Integer> UserHm = new HashMap<String,Integer>();
							UserHm.put(itemID, 1);
							TestUserItemHm.put(userID, UserHm);
						}
					}
					lineNumber++;
				}
				readingFromFile.close();
				System.out.println(lineNumber);
			}else if(selectTestSetCreator.equals(TEST_WITHOUT_TEST_DATASET)){		/*Read without Test dataset*/
				while ((readedLine = readingFromFile.readLine()) != null){
					String[] str = readedLine.split(splitStringWith);
					String userID = str[userIdInLogLine];
					String timestamp = str[timestampIdInLogLine];
					String itemID = str[itemIdInLogLine];
					System.out.println(userID);
					if(!userSet.contains(userID)){	/*Collect all real userid which has log in the train dataset*/
						userSet.add(userID);		
					}
					if(!itemSet.contains(itemID)){	/*Collect all real itemid which has log in the train dataset*/
						itemSet.add(itemID);
					}
					lineNumber++;
				}
				readingFromFile.close();
				System.out.println(lineNumber);
			}else if(selectTestSetCreator.equals(null)){
				/*new function...*/
			}else{
				System.out.println("WRONG TEST DATASET MAKER NUMBER");
			}
			System.out.println(lineNumber);
			readingFromFile.close();
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			readingFromFile = new BufferedReader(new FileReader(filePath));	/*Reach file to read*/
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*SECOND READING: SET SPARCEDOUBLEMATRIX*/
		int user_futo=0;
		int item_futo=0;
		ReadedMatrix = new SparseDoubleMatrix2D(userSet.size(), itemSet.size());
		ReadedMatrix.assign(0);
		try {
			/*ReadedMatrix is set with Training dataset, so the test dataset maker function is important*/
			if(selectTestSetCreator.equals(TEST_WITH_TIME_INTERVALL)){
				while ((readedLine = readingFromFile.readLine()) != null){
					String[] str = readedLine.split(splitStringWith);
					String userID = str[userIdInLogLine];
					String timestamp = str[timestampIdInLogLine];
					String itemID = str[itemIdInLogLine];
					Date convertedDate = formateDatasetDate.parse(timestamp.toString().substring(0, 20));
					System.out.println(userID);
					
					int row=0;
					int column=0;			
					if(!LinkingUserID.containsKey(userID)){
						LinkingUserID.put(userID,user_futo);
						row=user_futo;
						user_futo++;
					}else{
						row=LinkingUserID.get(userID);
					}
					if(!LinkingItemID.containsKey(itemID)){
						LinkingItemID.put(itemID,item_futo);
						column=item_futo;
						item_futo++;
					}else{
						column=LinkingItemID.get(itemID);
					}
					
					/*Only the test data matters*/
					if(startDate.compareTo(convertedDate)>0 || endDate.compareTo(convertedDate)<0){	/*Outside of start and end date*/
						double matrixCellValue=ReadedMatrix.get(row, column);
						matrixCellValue += 1;
						ReadedMatrix.setQuick(row, column, matrixCellValue);		
					}
				}
				readingFromFile.close();
			}else if(selectTestSetCreator.equals(TEST_WITHOUT_TEST_DATASET)){
				while ((readedLine = readingFromFile.readLine()) != null){
					String[] str = readedLine.split(splitStringWith);
					String userID = str[userIdInLogLine];
					String timestamp = str[timestampIdInLogLine];
					String itemID = str[itemIdInLogLine];
					Date convertedDate = formateDatasetDate.parse(timestamp.toString().substring(0, 20));
					System.out.println(userID);
					int row=0;
					int column=0;			
					if(!LinkingUserID.containsKey(userID)){
						LinkingUserID.put(userID,user_futo);
						row=user_futo;
						user_futo++;
					}else{
						row=LinkingUserID.get(userID);
					}
					if(!LinkingItemID.containsKey(itemID)){
						LinkingItemID.put(itemID,item_futo);
						column=item_futo;
						item_futo++;
					}else{
						column=LinkingItemID.get(itemID);
					}
					double matrixCellValue=ReadedMatrix.get(row, column);
					matrixCellValue += 1;
					ReadedMatrix.setQuick(row, column, matrixCellValue);
				}
				readingFromFile.close();
			}else if(selectTestSetCreator.equals(null)){
				/*Create ReaderMatrix*/
			}else{
				System.out.println("WRONG TEST DATASET MAKER NUMBER TO MAKE READEDMATRIX");
			}
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}
