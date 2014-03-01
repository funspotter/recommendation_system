package com.incredibles.kritikustomeg;

public class KritikustomegFilm {
	
	protected String title;
	protected String originalTitle;
	protected String description;
	
	
	
	public KritikustomegFilm(String title, String originalTitle, String description) {
		super();
		this.title = title;
		this.originalTitle = originalTitle;
		this.description = description;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getOriginalTitle() {
		return originalTitle;
	}
	public void setOriginalTitle(String originalTitle) {
		this.originalTitle = originalTitle;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

}
