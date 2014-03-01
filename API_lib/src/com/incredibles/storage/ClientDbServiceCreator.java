package com.incredibles.storage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * This class is responsible for creating the {@link ClientDbService}
 * implementation that is used by the servlet that serves the clients. This way
 * the servlet does not have to know the implementation class.
 */
public abstract class ClientDbServiceCreator {

	/**
	 * Create a {@link ClientDbService} implementation
	 * 
	 * @return A {@link ClientDbService} implementation
	 * 
	 * @throws IOException
	 *             If an error occured while opening the database
	 */
	public static ClientDbService createSimple() throws IOException {
		return null;
	}
	
	/**
	 * Create a {@link ClientDbService} implementation
	 * 
	 * @return A {@link ClientDbService} implementation
	 * @throws SQLException 
	 * 
	 * @throws IOException
	 *             If an error occured while opening the database
	 */
	public static ClientDbService createCloud() throws SQLException{
		return new CloudDbManager();
	}
	
}
