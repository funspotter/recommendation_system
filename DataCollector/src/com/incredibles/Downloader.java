package com.incredibles;

import java.util.List;

import org.openqa.selenium.WebDriver;

import com.incredibles.data.Event;

/**
 * Common interface for collecting events from a site
 * 
 * @author Andris
 *
 */
public interface Downloader {

	/**
	 * Download the list of the events
	 *
	 * @param driver {@link WebDriver} instance to be used by selenium
	 * 
	 * @return The list of the downloaded events
	 * @throws DownloadException
	 */
	public List<Event> downloadEvents(WebDriver driver) throws DownloadException;

}
