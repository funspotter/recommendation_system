package com.incredibles.storage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.incredibles.data.Event;
import com.incredibles.data.InterTicketEvent;
import com.incredibles.data.SimpleEvent;

/**
 * Using interface the collected events can be uploaded to the server 
 */
public interface EventUploader {

	/**
	 * Upload the collected events to the server
	 * 
	 * @param events The list of the collected events
	 * @throws SQLException 
	 */
	public void uploadEvents(List<Event> events) throws SQLException;
	
	/**
	 * Update events and shows that were downloaded from jegy.hu with the data
	 * necessary to sell tickets
	 * 
	 * @param event
	 * @throws SQLException
	 */
	public void updateInterTicketEvents(List<InterTicketEvent> event) throws SQLException;
	
	/**
	 * Release unmanaged resources
	 */
	public void close() throws IOException, SQLException;

	void uploadOrganizerEvents(List<SimpleEvent> events) throws SQLException;

	void uploadInterticketEvents(List<InterTicketEvent> events)
			throws SQLException;
}
