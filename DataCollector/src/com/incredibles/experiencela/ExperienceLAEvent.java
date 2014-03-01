package com.incredibles.experiencela;

import com.incredibles.data.Event;


public class ExperienceLAEvent extends Event {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9013089547405216385L;
	

	public ExperienceLAEvent() {
		super(Type.EXPERIENCELA);
	}
	
	public ExperienceLAEvent(String name) {
		super(name, Type.EXPERIENCELA);
	}
	
	
	@Override
	public String toString() {
		return String.format(
				"=================\n" + 
				">> %s <<\n" +
				"=================\n" + 
				"description: %s\n" +
				"showCount: %d\n" +
				"thumbnail: %s",
				name, description,getShowList().size(),thumbnail);
	}

}
