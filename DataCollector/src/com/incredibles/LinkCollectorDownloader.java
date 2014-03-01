package com.incredibles;

import java.util.LinkedList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.incredibles.data.Event;

/**
 * Abstract baseclass for downloaders that collect the links of events first,
 * then iterates over them and collects the details
 */
public abstract class LinkCollectorDownloader implements Downloader {

	/** The url of the web page */
	private final String pageUrl;
	/** xPath expression that is used to find the links */
	private final String xpathLinks;
	/** The maximum number of events to download */
	private final int eventLimit;

	/**
	 * Constructor
	 * 
	 * @param pageUrl The url of the web page
	 * @param xpathLinks xPath expression that is used to find the links
	 * @param eventLimit The maximum number of events to download
	 */
	public LinkCollectorDownloader(String pageUrl, String xpathLinks, int eventLimit) {
		this.pageUrl = pageUrl;
		this.xpathLinks = xpathLinks;
		this.eventLimit = eventLimit;
	}

	@Override
	public List<Event> downloadEvents(WebDriver driver) throws DownloadException {

		// the downloaded events will be collected in this list
		List<Event> downloadedEventList = new LinkedList<Event>();
		
		driver.get(pageUrl);
		List<WebElement> linkElementList = driver.findElements(By.xpath(xpathLinks));
		List<String> eventLinkList = new LinkedList<String>();
		
		// collect event links from the list
		for (WebElement linkElement : linkElementList) {
			String href = linkElement.getAttribute("href");
			eventLinkList.add(href);
		}
		
		// download events from each link
		int eventsSoFar = 0;
		for (String eventLink : eventLinkList) {
			if (eventsSoFar < eventLimit) {
				eventsSoFar++;
				Event event = downloadEventFromDetails(driver, eventLink);
				downloadedEventList.add(event);
			} else {
				break;
			}
		}
		
		return downloadedEventList;
	}
	
	/**
	 * Download an event from a details page
	 * 
	 * @param driver {@link WebDriver} instance
	 * @param eventLink The link of the events detail page
	 * 
	 * @return {@link Event} instance that represents the currently downloaded event
	 */
	protected abstract Event downloadEventFromDetails(WebDriver driver, String eventLink);
}
