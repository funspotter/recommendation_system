package com.incredibles.reclib;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


import cern.colt.matrix.impl.SparseDoubleMatrix2D;

/**SuperClass to start reading from API or file;
 * Set Test datasets with different methods*/
public class Readerv2 {
	
	/**Does not create test dataset*/
	public static final int TEST_WITHOUT_TEST_DATASET = 0;
	/**constans to select time intervall test dataset creator*/
	public static final int TEST_WITH_TIME_INTERVALL = 1;
	
	/**Use for setTestDateWithTimeIntervall(Date startDate, Long intervall) reference*/
	protected Date startDate = new Date();
	/**Use for setTestDateWithTimeIntervall(Date startDate, Long intervall) reference*/
	protected Date endDate = new Date();
	
	/**Read from file or API the log information and store in SparseDoubleMatrix2D
	 * protected - just this class could set/get..*/
	protected static SparseDoubleMatrix2D ReadedMatrix;
	
	/**links Dataset UserID and MatrixID protected
	 * linking starts with 0*/
	protected static HashMap<String, Integer> LinkingUserID = new HashMap<String, Integer>();
	
	/**links Dataset ItemID and MatrixID protected
	 * linking starts with 0*/
	protected static HashMap<String, Integer> LinkingItemID = new HashMap<String, Integer>();
	
	/**Collect all real userID for SparceDoubleMatrix*/
	public HashSet<String> userSet = new HashSet<String>();
	
	/**Collect all real itemID for SparceDoubleMatrix*/
	public HashSet <String> itemSet = new HashSet<String>();
	
	/**Set Test dataset; Real UserID -> Real ItemID : click number*/
	public static HashMap<String,HashMap<String,Integer>> TestUserItemHm = new HashMap<String,HashMap<String,Integer>>();
	
	/**Set Test dataset based on StartDate and time intervall
	 * StartDate
	 * EndDate
	 * TEST_WITH_TIME_INTERVALL = 1*/
	public void setTestDateWithTimeIntervall(Date startDate, Long intervall){
		List<Date> timeIntervall = new ArrayList<Date>();
		timeIntervall.add(0, startDate);
		Date endDate = new Date();
		endDate.setTime(startDate.getTime()+ intervall);
		timeIntervall.add(1,endDate);
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	
	/**Override in subclasses to execute reading*/
	public void ExecuteReading(){}

}
