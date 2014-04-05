package com.incredibles.data;

public class ToJsonEvent extends FunspotterEvent  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected long go;
	protected long end;
	protected String location;
	protected String category;
	protected float x;
	protected float y;
	
//	public ToJsonEvent(String name, Type type, long go, long end, String location, String category) {
//		super(name, type);
//		this.go = go;
//		this.end = end;
//		this.location = location;
//		this.category = category;
////		this.showList = null;
//	}

//	public ToJsonEvent(String name, Type type) {
//		super(name, type);
//	}
//
//	public ToJsonEvent() {
//		super(Type.MOVIE);
//	}

	

	public long getGo() {
		return go;
	}

	public void setGo(long go) {
		this.go = go;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCategory() {
		return category;
	}

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

}
