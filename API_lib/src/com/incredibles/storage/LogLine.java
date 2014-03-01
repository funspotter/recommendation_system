package com.incredibles.storage;

import java.util.Date;

/**
 * Struct that represents a single line in the log 
 */
public class LogLine {

	/** The id of the user*/
	public int userId;
	/** The id of the event (for example a film) */
	public int eventId;
	/** When the user clicked/viewed/loged on the site ...*/
	public Date timeStamp;
	/** Type of the log line*/
	public int type;
	
	

	@Override
	public String toString() {
		return String.format("%d | %d | %s | %d", userId, eventId, timeStamp.toString(), type);
	}
}
