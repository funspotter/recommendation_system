package com.incredibles.data;

import java.io.Serializable;
import java.util.Date;

/**
 * Class that represents an occasion, when and where an event will be held 
 * 
 * @author Andris
 *
 */
public class Show implements Serializable {
	
	private static final long serialVersionUID = -2552785697359391541L;
	
	/** The start date of the show */
	protected Date start;
	/** The end date of the show - can be null if not specified */
	protected Date end;
	/** The place of the show */
	protected String location;
	/** The category of the show - can be null if not specified */
	protected String category;
	
	protected float x;

	protected float y;
	
	/**
	 * Constructor
	 * 
	 * @param start The start date of the show
	 * @param place The end date of the show
	 */
	public Show(Date start, String place) {
		super();
		this.start = start;
		this.location = place;
	}
	
	/**
	 * Constructor
	 * 
	 * @param start The start date of the show
	 * @param place The end date of the show
	 * @param category The category of the show
	 */
	public Show(Date start, String place, String category) {
		this(start, place);
		this.category = category;
	}

	/**
	 * Constructor
	 * 
	 * @param start The start date of the show
	 * @param end The end date of the show
	 * @param place The end date of the show
	 */
	public Show(Date start, Date end, String place) {
		this(start, place);
		this.end = end;
	}
	
	/**
	 * Constructor
	 * 
	 * @param start The start date of the show
	 * @param end The end date of the show
	 * @param place The end date of the show
	 * @param category The category of the show
	 */
	public Show(Date start, Date end, String place, String category) {
		this(start, end, place);
		this.category = category;
	}
	
	/**
	 * Constructor
	 * 
	 * @param start The start date of the show
	 * @param end The end date of the show
	 */
	public Show(Date start, Date end) {
		super();
		this.start = start;
		this.end = end;
	}
	
	/**
	 * Constructor
	 * 
	 * @param start The start date of the show
	 */
	public Show(Date start) {
		super();
		this.start = start;
	}
	
	public Show() {
		super();
	}

	/**
	 * Get the start date of the occasion
	 * 
	 * @return The start date of the occasion
	 */
	public Date getStart() {
		return start;
	}
	
	/**
	 * Set the start date of the occasion
	 * 
	 * @param start
	 *            The new value to be set
	 */
	public void setStart(Date start) {
		this.start = start;
	}

	/**
	 * Get the end date of the occasion - can be null if not specified
	 * 
	 * @return The end date of the occasion
	 */
	public Date getEnd() {
		return end;
	}
	
	/**
	 * Set the end date of the occasion
	 * 
	 * @param end
	 *            The new value to be set
	 */
	public void setEnd(Date end) {
		this.end = end;
	}

	/**
	 * Get the location of the show
	 * 
	 * @return The location of the show
	 */
	public String getLocation() {
		return location;
	}
	
	/**
	 * Set the location of the show
	 * 
	 * @param location
	 * The new value to be set
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	
	/**
	 * Get the category of the show
	 * 
	 * @return The category of the show
	 */
	public String getCategory() {
		return category;
	}
	
	/**
	 * Set the category of the show
	 * 
	 * @param category
	 *            The new value to be set
	 */
	public void setCategory(String category) {
		this.category = category;
	}
	
	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}
	

	@Override
	public String toString() {
		
		return String.format("%s; %s; start: %s; end: %s", location, category, "" + start, "" + end);
	}
	
}
