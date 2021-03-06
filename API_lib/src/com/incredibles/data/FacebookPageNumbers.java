package com.incredibles.data;

/**Contains information about an event place page like and checkin number with other system id-s.*/
public class FacebookPageNumbers {

	/**Facebook place page like number default is long.*/
	protected long likeNumber;
	
	/**Facebook place page checkin number default is int.*/
	protected int chekinNumber;
	
	/**The place page id.*/
	protected long pageId;
	
	/**Funspotter Event Id*/
	protected int funspotterEventId;

	/**Facebook event Id*/
	protected long facebookEventId;
	
	
	public long getCheckinNumberLong(){
		return (long)this.chekinNumber;
	}
	
	public long getSumLikeAndCheckin(){
		long checkin = getCheckinNumberLong();
		long sum = this.likeNumber+checkin;
		return sum;
	}

	public long getLikeNumber() {
		return likeNumber;
	}

	public void setLikeNumber(long likeNumber) {
		this.likeNumber = likeNumber;
	}

	public int getChekinNumber() {
		return chekinNumber;
	}

	public void setChekinNumber(int chekinNumber) {
		this.chekinNumber = chekinNumber;
	}

	public long getPageId() {
		return pageId;
	}

	public void setPageId(long pageId) {
		this.pageId = pageId;
	}

	public int getFunspotterEventId() {
		return funspotterEventId;
	}

	public void setFunspotterEventId(int funspotterEventId) {
		this.funspotterEventId = funspotterEventId;
	}

	public long getFacebookEventId() {
		return facebookEventId;
	}

	public void setFacebookEventId(long facebookEventId) {
		this.facebookEventId = facebookEventId;
	}
}
