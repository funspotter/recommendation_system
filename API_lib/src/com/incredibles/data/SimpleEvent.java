package com.incredibles.data;

import java.sql.SQLException;

public class SimpleEvent extends Event {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5632985477798440756L;

	protected String discriminator;
	
	public SimpleEvent(String name) {
		super(name, Type.SIMPLE);
	}

	public SimpleEvent() {
		super(null);
	}
	
	public String getDiscriminator() {
		return discriminator;
	}
	
	public void setDiscriminator(String discriminator) {
		this.discriminator = discriminator;
	}
	
	public String toString() {
//		return String.format("%s \n\t%s \n\t%d \n\t%s \n\t%s \n\t%s \n\t%s", 
		return String.format(
				"=================\n" + 
				">> %s <<\n" +
				"=================\n" +
				"img: %s\n" +
				"-- %s",
				name, image, description);
	}
	
	public void accept(EventVisitor visitor) throws SQLException {
		visitor.visitSimpleOrganizer(this);
	}

}
