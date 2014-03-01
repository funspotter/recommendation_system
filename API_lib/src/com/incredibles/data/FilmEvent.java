package com.incredibles.data;

import java.sql.SQLException;
import java.util.Date;

/**
 * Event subclass that represents a film performance
 */
public class FilmEvent extends Event {

	private static final long serialVersionUID = -5587021875611469291L;
	
	public static final String DISCRIIMINATOR_VALUE = "Cinema";
	
	/** The original title of the film */
	protected String originalTitle;
	/** The genre of the film */
	protected String genre;
	/** The length of the film in minutes */
	protected int lengthInMin;
	/** The release date of the film */
	protected Date releasedOn;
	/** The rating of the film */
	protected int rating;
	/** The director of the film */
	protected String director;
	/** A string with the names of the actors in a comma seperated list */
	protected String actors;
	/** Country / Year */
	protected String countryYear;
	
	/**
	 * Constructor
	 */
	public FilmEvent() {
		this(null);
	}
	
	/**
	 * Constructor
	 * 
	 * @param name The name of the film
	 */
	public FilmEvent(String name) {
		super(name, Type.MOVIE);
	}
	
	/**
	 * Get the original title of the film
	 * 
	 * @return The original title of the film
	 */
	public String getOriginalTitle() {
		return originalTitle;
	}
	
	/**
	 * Set the original title of the film
	 * 
	 * @param originalTitle The new value to be set
	 */
	public void setOriginalTitle(String originalTitle) {
		this.originalTitle = originalTitle;
	}

	/**
	 * Get the genre of the film
	 * 
	 * @return The genre of the film
	 */
	public String getGenre() {
		return genre;
	}

	/**
	 * Set the genre of the film
	 * 
	 * @param genre The value to be set
	 */
	public void setGenre(String genre) {
		this.genre = genre;
	}

	/**
	 * Get the length of the film in minutes
	 * 
	 * @return The length of the film in minutes
	 */
	public int getLengthInMin() {
		return lengthInMin;
	}

	/**
	 * Set the length of the film in minutes
	 * 
	 * @param lengthInMin The new value to be set
	 */
	public void setLengthInMin(int lengthInMin) {
		this.lengthInMin = lengthInMin;
	}

	/**
	 * Get the realese date of the film
	 * 
	 * @return The release date of the film
	 */
	public Date getReleasedOn() {
		return releasedOn;
	}

	/**
	 * Set the release date of the film
	 * 
	 * @param releasedOn The new value to be set
	 */
	public void setReleasedOn(Date releasedOn) {
		this.releasedOn = releasedOn;
	}

	/**
	 * Get the rating of the film
	 * 
	 * @return The rating of the film
	 */
	public int getRating() {
		return rating;
	}

	/**
	 * Set the rating of the film
	 * 
	 * @param rating The new value to be set
	 */
	public void setRating(int rating) {
		this.rating = rating;
	}

	/**
	 * Get the director of the film
	 * 
	 * @return The director of the film
	 */
	public String getDirector() {
		return director;
	}

	/**
	 * Set the director of the film
	 * 
	 * @param director The new value to be set
	 */
	public void setDirector(String director) {
		this.director = director;
	}

	/**
	 * Get the string with the names of the actors in a comma seperated list
	 * 
	 * @return The string with the names of the actors in a comma seperated list
	 */
	public String getActors() {
		return actors;
	}

	/**
	 * Set the string with the names of the actors in a comma seperated list
	 * 
	 * @param actors The new value to be set
	 */
	public void setActors(String actors) {
		this.actors = actors;
	}

	/**
	 * Get the Country / Year
	 * 
	 * @return The Country / Year
	 */
	public String getCountryYear() {
		return countryYear;
	}
	
	/**
	 * Set the Country / Year
	 * 
	 * @param countryYear The new value to be set
	 */
	public void setCountryYear(String countryYear) {
		this.countryYear = countryYear;
	}
	
	@Override
	public String toString() {
//		return String.format("%s \n\t%s \n\t%d \n\t%s \n\t%s \n\t%s \n\t%s", 
		return String.format(
				"=================\n" + 
				">> %s <<\n" +
				"=================\n" + 
				"Original title: %s\n" +
				"Genre: %s\n" +
				"Length in minutes: %d\n" +
				"Released on: %s\n" +
				"Rating: %d\n" +
				"Director: %s\n" +
				"Actors: %s\n" +
				"-- %s",
				name, originalTitle, genre, lengthInMin, releasedOn, rating, director, actors, description);
	}
	
	@Override
	public void accept(EventVisitor visitor) throws SQLException {
		visitor.visitFilm(this);
	}

}