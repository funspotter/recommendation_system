package com.incredibles.data;

import java.util.Date;


public class InterTicketShow extends Show {

	protected String NetEvent_id;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3283115822041877847L;

	public InterTicketShow() {
		super();
	}

	public InterTicketShow(Date start, Date end, String place, String category) {
		super(start, end, place, category);
	}

	public InterTicketShow(Date start, Date end, String place) {
		super(start, end, place);
	}

	public InterTicketShow(Date start, Date end) {
		super(start, end);
	}

	public InterTicketShow(Date start, String place, String category) {
		super(start, place, category);
	}

	public InterTicketShow(Date start, String place) {
		super(start, place);
	}

	public InterTicketShow(Date start) {
		super(start);
	}

	public String getNetEvent_id() {
		return NetEvent_id;
	}

	public void setNetEvent_id(String netEvent_id) {
		NetEvent_id = netEvent_id;
	}

}