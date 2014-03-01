package com.incredibles.reclib;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import cern.colt.matrix.impl.SparseDoubleMatrix2D;

import com.incredibles.data.LogTableTypes;
import com.incredibles.storage.LogLine;
import com.incredibles.storage.RecommenderDbService;
import com.incredibles.storage.RecommenderDbServiceCreator;

/**Read from API, Set Test(optional) and Train dataset
 * - Select the Test dataset function*/
public class ReaderFromApi extends Readerv2{

	/**Read from a DB; creates specific Test set*/
	public void ExecuteReading(){
		Date startDate1 = new Date();
		startDate1.setTime(1387198467000L);
		Long intervall = 1209600000L; // TWO WEEK in milisec.
		setTestDateWithTimeIntervall(startDate1, intervall);
		Integer selectTestSetCreator = TEST_WITH_TIME_INTERVALL;
		Integer dbLine = 0;
		RecommenderDbService dbService = null;
		LogLine logLine = new LogLine();
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			if(selectTestSetCreator.equals(TEST_WITHOUT_TEST_DATASET)){
				while (dbService.getNextLogLine(logLine)) {
					Integer intUser = logLine.userId;
					String userID = intUser.toString();
					Integer intEvent = logLine.eventId;
					String eventID = intEvent.toString();
					Date timestamp = logLine.timeStamp;
					Integer logType = logLine.type;
					
					if(logType.equals(LogTableTypes.eventClick)){
						dbLine++;
						System.out.println(userID);
						if(!userSet.contains(userID)){	/*Collect all real userid which has log in the train dataset*/
							userSet.add(userID);		
						}
						if(!itemSet.contains(eventID)){	/*Collect all real itemid which has log in the train dataset*/
							itemSet.add(eventID);
						}
					}
				}
				System.out.println("ESEMeny_"+dbLine);
			}else if(selectTestSetCreator.equals(TEST_WITH_TIME_INTERVALL)){
				while (dbService.getNextLogLine(logLine)){
					Integer intUser = logLine.userId;
					String userID = intUser.toString();
					Integer intEvent = logLine.eventId;
					String eventID = intEvent.toString();
					Date timestamp = logLine.timeStamp;
					Integer logType = logLine.type;
					
					if(logType.equals(LogTableTypes.eventClick)){
						dbLine++;
						System.out.println(userID);		
						if(!userSet.contains(userID)){	/*Collect all real userid which has log */
							userSet.add(userID);		
						}
						if(!itemSet.contains(eventID)){	/*Collect all real itemid which has log */
							itemSet.add(eventID);
						}
						
						if(startDate.compareTo(timestamp)<0 && endDate.compareTo(timestamp)>0){	/*if timestamp is inside of the intervall, so it is in the test part*/
							if(TestUserItemHm.containsKey(userID)){
								HashMap<String, Integer> UserHm = TestUserItemHm.get(userID);
								if(UserHm.containsKey(eventID)){									
									Integer number = UserHm.get(eventID);
									UserHm.put(eventID, number+1);
									TestUserItemHm.put(userID, UserHm);
								}else{
									UserHm.put(eventID, 1);
									TestUserItemHm.put(userID, UserHm);
								}
							}else{
								HashMap<String, Integer> UserHm = new HashMap<String,Integer>();
								UserHm.put(eventID, 1);
								TestUserItemHm.put(userID, UserHm);
							}
						}
						
					}
				}
			}
			System.out.println(dbLine);
		} catch (SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (dbService != null) {
				try {
					dbService.close();
				} catch (IOException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		/*SECOND READING: SET SPARCEDOUBLEMATRIX*/
		int user_futo=0;
		int item_futo=0;
		ReadedMatrix = new SparseDoubleMatrix2D(userSet.size(), itemSet.size());
		ReadedMatrix.assign(0);
		try {
			dbService = RecommenderDbServiceCreator.createCloud();
			/*ReadedMatrix is set with Training dataset, so the test dataset maker function is important*/
			if(selectTestSetCreator.equals(TEST_WITH_TIME_INTERVALL)){
				while (dbService.getNextLogLine(logLine)){
					Integer intUser = logLine.userId;
					String userID = intUser.toString();
					Integer intEvent = logLine.eventId;
					String eventID = intEvent.toString();
					Date timestamp = logLine.timeStamp;
					Integer logType = logLine.type;
					
					if(logType.equals(LogTableTypes.eventClick)){
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
						if(!LinkingItemID.containsKey(eventID)){
							LinkingItemID.put(eventID,item_futo);
							column=item_futo;
							item_futo++;
						}else{
							column=LinkingItemID.get(eventID);
						}
						/*Only the train data matters*/
						if(startDate.compareTo(timestamp)>0 || endDate.compareTo(timestamp)<0){	/*Outside of start and end date*/
							double matrixCellValue=ReadedMatrix.get(row, column);
							matrixCellValue += 1;
							ReadedMatrix.setQuick(row, column, matrixCellValue);
						}
					}
					
				}				
			}else if(selectTestSetCreator.equals(TEST_WITHOUT_TEST_DATASET)){
				while (dbService.getNextLogLine(logLine)){
					Integer intUser = logLine.userId;
					String userID = intUser.toString();
					Integer intEvent = logLine.eventId;
					String eventID = intEvent.toString();
					Date timestamp = logLine.timeStamp;
					Integer logType = logLine.type;
					
					if(logType.equals(LogTableTypes.eventClick)){
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
						if(!LinkingItemID.containsKey(eventID)){
							LinkingItemID.put(eventID,item_futo);
							column=item_futo;
							item_futo++;
						}else{
							column=LinkingItemID.get(eventID);
						}
						double matrixCellValue=ReadedMatrix.get(row, column);
						matrixCellValue += 1;
						ReadedMatrix.setQuick(row, column, matrixCellValue);
					}
				}
			}else if(selectTestSetCreator.equals(null)){
				/*Create ReaderMatrix*/
			}else{
				System.out.println("WRONG TEST DATASET MAKER NUMBER TO MAKE READEDMATRIX");
			}
		} catch (IOException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (dbService != null) {
				try {
					dbService.close();
				} catch (IOException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
	
	
	
}
