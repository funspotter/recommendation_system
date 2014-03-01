package com.incredibles.data;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class that represents an event 
 */
public abstract class Event implements Serializable {

	private static final long serialVersionUID = 5121302671146255459L;

	/** Enum that represents the type of an event */
	public enum Type {
		MOVIE, OTHER, JEGYHU, SIMPLE, EXPERIENCELA, TiXML
	}
	
	/** The name of the event */
	protected String name;
	/** The type of the event */
	protected Type type;
	/** The description of the event */
	protected String description;
	/** The id of the event in the database */
	protected int id;
	/** List of the occasions, when and where the event will be held */
	protected List<Show> showList;
	protected String thumbnail;
	protected String image;
	protected boolean manyShows;
	protected String langCode;
	protected String video;
	protected float rank;
	
	public Event() {
		this.langCode = "HU"; // the default langCode is HU
	}
	
	public String getVideo() {
		return video;
	}

	public void setVideo(String video) {
		this.video = video;
	}

	public String getLangCode() {
		return langCode;
	}

	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}

	/**
	 * Constructor
	 * 
	 * @param type The type of the event
	 */
	public Event(Type type) {
		super();
		this.type = type;
		this.showList = new ArrayList<Show>();
		this.langCode = "HU";
	}

	/**
	 * Constructor
	 * 
	 * @param name The name of the event
	 * @param type The type of the event
	 */
	public Event(String name, Type type) {
		this(type);
		this.name = name;
	}
	
	/**
	 * Get the name of the event
	 * 
	 * @return The name of the event
	 */
	public String getName() {
		return name;
	}
	
	/** 
	 * Set the name of the event
	 * 
	 * @param name The new value to be set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the {@link Type} of the event
	 * 
	 * @return The {@link Type} of the event
	 */
	public Type getType() {
		return type;
	}

	
	/**
	 * Get the list of the occasions when and where the event will be held
	 * 
	 * @return The list of the occasions when and where the event will be held
	 */
	public List<Show> getShowList() {
		return showList;
	}
	
	public int getId() {
		return id;
	}

	public float getRank() {
		return rank;
	}

	public void setRank(float rank) {
		this.rank = rank;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Get the description of the event
	 * 
	 * @return The description of the film
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description of the event
	 * 
	 * @param description The new value to be set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}
	
	public void setShowList(List<Show> showList) {
		this.showList = showList;
	}
	
	public boolean hasManyShows() {
		return manyShows;
	}

	public void setManyShows(boolean manyShows) {
		this.manyShows = manyShows;
	}

	public boolean hasManyShowsOnOneDay() {
		boolean ret = false;
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		
		Set<String> dateSet = new HashSet<String>();
		for (Show show : showList) {
			int tmpSize = dateSet.size();
			String dateStr = dateFormat.format(show.getStart());
			dateSet.add(dateStr);
			if (tmpSize == dateSet.size()) {
				ret = true;
				break;
			}
		}
		return ret;
	}
	
	public void accept(EventVisitor visitor) throws SQLException {
		visitor.visitSimple(this);
	}

}
