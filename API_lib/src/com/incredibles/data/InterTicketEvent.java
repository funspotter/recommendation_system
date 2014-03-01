package com.incredibles.data;

import java.sql.SQLException;


public class InterTicketEvent extends Event {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2370796403735107191L;
	
	/** Id of the venue of the event in ticket express database, this is necessary to buy a ticket */
	protected int venueId;
	/** Id of the audit of the event in ticket express database, this is necessary to buy a ticket */
	protected int auditId;
	/**
	 * In Ticket Express database, this belongs to the event, not to the show,
	 * so it it temporarily stored here
	 */
	transient protected String location;

	/**
	 * Constructor
	 * 
	 * @param name The name of the event
	 */
	public InterTicketEvent(String name) {
		super(name, Type.TiXML);
	}

	/**
	 * Constructor
	 */
	public InterTicketEvent() {
		this(null);
	}

	public int getVenueId() {
		return venueId;
	}

	public void setVenueId(int venueId) {
		this.venueId = venueId;
	}

	public int getAuditId() {
		return auditId;
	}

	public void setAuditId(int auditId) {
		this.auditId = auditId;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	@Override
	public void accept(EventVisitor visitor) throws SQLException {
		visitor.visitInterTicket(this);
	}
	
}
