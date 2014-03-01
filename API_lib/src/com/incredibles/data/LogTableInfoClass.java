package com.incredibles.data;

import java.util.ArrayList;
import java.util.List;

public class LogTableInfoClass {

	
	Integer LogType;
	Integer userID;
	Integer eventID;
	List<Long> timeStamps;
	
	public LogTableInfoClass() {
		this(10,0,0,(long) 0);
	}
	
	public LogTableInfoClass(int type, int userid, int eventid, Long timestamp){
		LogType = type;
		userID = userid;
		eventID = eventid;
		timeStamps = new ArrayList<Long>();
		timeStamps.add(timestamp);
	}
	
	public void addTimestamp(Long timestamp){
		this.timeStamps.add(timestamp);
	}
	
	public void setUserID(int userid){
		this.userID = userid;
	}

	
	public void setEventId(Integer eventid) {
		this.eventID = eventid;
	}

	public void setLogType(int logtype){
		this.LogType = logtype;
	}
	
	public Integer getUserID(){
		return this.userID;
	}
	
	public Integer getEventID(){
		return this.eventID;
	}
	
	public Integer getLogType(){
		return this.LogType;
	}
	
	public List<Long> getTimestamp(){
		return this.timeStamps;
	}
	
	public Long getOldestTimestamp(){
		Long smallest = null;
		if(this.timeStamps.isEmpty()){
			return null;
		}else{
			smallest = this.timeStamps.get(0);
			for(int i=0;i<this.timeStamps.size(); i++){
				if(this.timeStamps.get(i) <= smallest){
					smallest = this.timeStamps.get(i);
				}
			}
			return smallest;
		}
	}
	
	public Long getNewestTimestamp(){
		Long biggest = (long) 0;
		if(this.timeStamps.isEmpty()){
			return null;
		}else{
			for(int i=0; i<this.timeStamps.size(); i++){
				if(biggest <= this.timeStamps.get(i)){
					biggest = this.timeStamps.get(i);
				}
			}
			return biggest;
		}	
	}

	
}
