package com.incredibles.data;

import java.util.Date;

public class ToJsonShow extends Show {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2311029317045308530L;
	
	protected int eventId;
	protected String day;
	protected long go;

	public ToJsonShow() {
		super();
	}

	public ToJsonShow(Date start, Date end, String place, String category) {
		super(start, end, place, category);
	}

	public ToJsonShow(Date start, Date end, String place) {
		super(start, end, place);
	}

	public ToJsonShow(Date start, Date end) {
		super(start, end);
	}

	public ToJsonShow(Date start, String place, String category) {
		super(start, place, category);
	}

	public ToJsonShow(Date start, String place) {
		super(start, place);
	}

	public ToJsonShow(Date start) {
		super(start);
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}
	
	public String getDay() {
		return day;
	}
	
	public void setDay(String day) {
		this.day = day;
	}
	
	public long getGo() {
		return go;
	}
	
	public void setGo(long go) {
		this.go = go;
	}
}
