package com.incredibles.data;

public class Like {
	
	protected long FacebooksId;
	protected long id;
	protected String category;
	protected String name;
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getFacebooksId(){
		return FacebooksId;
	}
	
	public void setFacebooksId(long fbId){
		this.FacebooksId = fbId;
	}
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	

}
