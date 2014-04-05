package com.incredibles.data;

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
public class FunspotterEvent {
	
	/** The name of the event */
	protected String title;

	/** The description of the event */
	protected String description;
	
	/**Events discriminator*/
	protected String discriminator;
	
	/** The id of the event in the database */
	protected int funspotterId;
	
	/**Basic picture link*/
	protected String thumbnailUrl;
	
	/**Other pictures json*/
	protected String imageUrlsJson;
	
	/**What type of the event is.*/
	protected String origin;
	
	protected int isOk;
	
	protected int isIn;

	public int getIsOk() {
		return isOk;
	}

	public void setIsOk(int isOk) {
		this.isOk = isOk;
	}

	public int getIsIn() {
		return isIn;
	}

	public void setIsIn(int isIn) {
		this.isIn = isIn;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDiscriminator() {
		return discriminator;
	}

	public void setDiscriminator(String discriminator) {
		this.discriminator = discriminator;
	}

	public int getFunspotterId() {
		return funspotterId;
	}

	public void setFunspotterId(int funspotterId) {
		this.funspotterId = funspotterId;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getImageUrlsJson() {
		return imageUrlsJson;
	}

	public void setImageUrlsJson(String imageUrlsJson) {
		this.imageUrlsJson = imageUrlsJson;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public FunspotterEvent(String title, String description, String discriminator,
			int funspotterId, String thumbnailUrl, String imageUrlsJson,
			String origin, int isOk, int isIn) {
		super();
		this.title = title;
		this.description = description;
		this.discriminator = discriminator;
		this.funspotterId = funspotterId;
		this.thumbnailUrl = thumbnailUrl;
		this.imageUrlsJson = imageUrlsJson;
		this.origin = origin;
		this.isOk = isOk;
		this.isIn = isIn;
	}

	public FunspotterEvent() {
		super();
	}
}
