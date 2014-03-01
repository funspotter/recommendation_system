package com.incredibles.storage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * This class is responsible for creating the {@link RecommenderDbService}
 * implementation that is used by the recommender. This way the recommender does
 * not have to know the implementation class
 */
public abstract class RecommenderDbServiceCreator {
	
	/**
	 * Create a {@link RecommenderDbService} implementation
	 * 
	 * @return A {@link RecommenderDbService} implementation
	 * 
	 * @throws IOException
	 *             If an error occured while opening the database
	 */
	public static RecommenderDbService create() throws IOException {
		return null;
	}

	/**
	 * Create a {@link RecommenderDbService} implementation
	 * 
	 * @param logFileUrl
	 *            The url of the logfile relative to the source folder
	 * 
	 * @return A {@link RecommenderDbService} implementation
	 * 
	 * @throws IOException
	 *             If an error occured while opening the database
	 */
	public static RecommenderDbService create(String logFileUrl) throws IOException {
		return null;
	}
	
	

	
	/**
	 * Create a {@link RecommenderDbService} implementation
	 * 
	 * @return A {@link RecommenderDbService} implementation
	 * 
	 * @throws SQLException 
	 * 
	 * 
	 * @throws IOException
	 *             If an error occured while opening the database
	 */
	public static RecommenderDbService createCloud() throws SQLException{
		return new CloudDbManager();
	}
	
}
