package com.incredibles.data;

import java.sql.SQLException;

public interface EventVisitor {

	public void visitFilm(FilmEvent event) throws SQLException;
	public void visitPt(PtEvent event) throws SQLException;
//	public void visitJegyHu(JegyHuEvent event) throws SQLException;
	public void visitSimple(Event event) throws SQLException;
	public void visitInterTicket(InterTicketEvent event) throws SQLException;
	public void visitSimpleOrganizer(SimpleEvent event) throws SQLException;
}