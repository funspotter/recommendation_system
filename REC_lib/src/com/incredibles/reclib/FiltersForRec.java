package com.incredibles.reclib;

import java.util.HashMap;
import java.util.List;

import com.incredibles.data.Like;

public interface FiltersForRec {

	// Facebook id User id change
	public Integer getUserIdFromFbId(long fbId);
	
	// Return all like for user
	public List<Like> getAllLikeForUser(long fbId);
	
	// Get events from database
	//public List<Integer> getEventsFromDate(long date);
	
	// Get user's birthdate from database
	public Integer getUserBirtdate(long fbId);
	
	// Get current date
	//public Integer getCurrentDate();

	// Input: Facebook User ID || Output: Uploaded ranks to database
	public HashMap<Integer,Double> setEventsRank(long fbId);
	
}
