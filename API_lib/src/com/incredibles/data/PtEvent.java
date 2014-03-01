package com.incredibles.data;

import java.sql.SQLException;

/**
 * Event downloaded from <a
 * href="http://www.programturizmus.hu">programturizmus.hu</a>
 */
public class PtEvent extends Event {

	/**
	 * The type of events that should be collected
	 */
	public enum PtEventType {
		FESTIVAL, HETVEGE, KOSTOLAS, KULTURA, ORSZAGJARAS, TABOROZAS, TURA, UTAZAS, UNNEP, ALBUM, ART, MUSICAL, BOOK, MUSIC, TV, MOVIE, WELLNESS, ARTIST, SPORT, PLAYLIST, TRAVEL, LEISURE, VIDEO, MUSICIAN, BAND, MUSEUM, GALLERY, CONCERT, LIBRARY, THEATER, CINEMA,EXHIBITION,GASTRO,KID,OTHER;
	}

	private static final long serialVersionUID = 1064050816908612622L;

	protected PtEventType ptEventType;

	public PtEvent(String name, PtEventType ptEventType) {
		super(name, Type.OTHER);
		this.ptEventType = ptEventType;
	}

	public PtEvent(PtEventType type) {
		this(null, type);
	}

	public PtEvent() {
		this(null, null);
	}

	@Override
	public void accept(EventVisitor visitor) throws SQLException {
		visitor.visitPt(this);
	}

	public PtEventType getPtEventType() {
		return ptEventType;
	}

	public void setPtEventType(PtEventType ptEventType) {
		this.ptEventType = ptEventType;
	}

	@Override
	public String toString() {

		return String.format("==============\n" + ">> %s <<\n"
				+ "==============\n" + "Description: \n\t%s",
				this.name != null ? this.name : "null",
				this.description != null ? this.description : "null");
	}

}
