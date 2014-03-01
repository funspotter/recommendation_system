package com.incredibles.ticketportal;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.incredibles.DownloadException;
import com.incredibles.Downloader;
import com.incredibles.data.Event;
import com.incredibles.data.Show;
import com.incredibles.data.SimpleEvent;

/**
 * {@link Downloader} implementation that downloads music events from http://www.ticketportal.hu 
 */
public class TicketPortalDownloader implements Downloader {

	/** Limit for the maximum number of events */
	private static int EVENT_LIMIT = Integer.MAX_VALUE;
	
	/** The url of the page */
	private static final String PAGE_URL = "http://www.ticketportal.hu/podujatia_typ_lista.asp?typ_l=2";

	/** XPath for finding the links, index of the tr needs to be appended */
	private static final String XPATH_LINKS_BASE = "/html/body/table[2]/tbody/tr/td[2]/table/tbody/tr/td/table/tbody/tr";
	
	/**
	 * XPath for finding the links, index of the tr needs to be appended.
	 * This needs to be used if there is an advertisement on the site
	 */
	private static final String XPATH_LINKS_BASE_ADVERTISEMENT = "/html/body/table[3]/tbody/tr/td[2]/table/tbody/tr/td/table/tbody/tr";
	
	/** XPath to find the description of the event */
	private static final String XPATH_DESCRIPTION = 
			"/html/body/table[2]/tbody/tr/td[2]/table[2]/tbody/tr/td/table/tbody/tr/td/table[2]/tbody/tr/td";
	
	/**
	 * XPath to find the description of the event.
	 * This needs to be used if there is an advertisement on the site
	 */
	private static final String XPATH_DESCRIPTION_ADVERTISEMENT = 
			"/html/body/table[3]/tbody/tr/td[2]/table[2]/tbody/tr/td/table/tbody/tr/td/table[2]/tbody/tr/td";
	
	/**
	 * XPath to find elements with yellow background (name of the city, name of the event, place of the event)
	 */
	private static final String XPATH_YELLOW_CATEGORY = 
			"//td[@class='td_yellow_category']";
	
	/** XPath to find the date of the event */
	private static final String XPATH_DATE = 
			"html/body/table[2]/tbody/tr/td[2]/table[2]/tbody/tr/td/table[2]/tbody/tr[1]/td/table/tbody/tr[3]/td/table/tbody/tr[3]/td";
	
	/**
	 * XPath to find the date of the event.
	 * This needs to be used if there is an advertisement on the site.
	 */
	private static final String XPATH_DATE_ADVERTISEMENT = 
			"html/body/table[3]/tbody/tr/td[2]/table[2]/tbody/tr/td/table[2]/tbody/tr[1]/td/table/tbody/tr[3]/td/table/tbody/tr[3]/td";
	
	/** XPath to find the date of the event */
	private static final String XPATH_DATE_ALT = 
			"html/body/table[2]/tbody/tr/td[2]/table[2]/tbody/tr/td/table[2]/tbody/tr[1]/td/table/tbody/tr[3]/td/table/tbody/tr[2]/td";
	
	/**
	 * XPath to find the date of the event.
	 * This needs to be used if there is an advertisement on the site.
	 */
	private static final String XPATH_DATE_ADVERTISEMENT_ALT = 
			"html/body/table[3]/tbody/tr/td[2]/table[2]/tbody/tr/td/table[2]/tbody/tr[1]/td/table/tbody/tr[3]/td/table/tbody/tr[2]/td";
	
	@Override
	public List<Event> downloadEvents(WebDriver driver) throws DownloadException {
		
		// the downloaded events will be collected in this list
		List<Event> downloadedEventList = new LinkedList<Event>();
		List<String> eventLinkList = new LinkedList<String>();
		
		driver.get(PAGE_URL);
		
		int number = -1;
		List<WebElement> yellowElementList = driver.findElements(By.xpath(XPATH_YELLOW_CATEGORY));
		for (WebElement yellowElement : yellowElementList) {
			if (yellowElement.getText().trim().equalsIgnoreCase("BUDAPEST")) {
				number = yellowElementList.indexOf(yellowElement);
				break;
			}
		}
		if (number == -1) {
			throw new DownloadException("Budapest yellow category not found!");
		}
		number = number * 3 + 1 + 4;
		String eventLinksXpath = null;
		List<WebElement> linkElementList = null;
		eventLinksXpath = String.format("%s[%d]//a[@class='graylink']", XPATH_LINKS_BASE, number);
		linkElementList = driver.findElements(By.xpath(eventLinksXpath));
		if (linkElementList.isEmpty()) {
			eventLinksXpath = String.format("%s[%d]//a[@class='graylink']", XPATH_LINKS_BASE_ADVERTISEMENT, number);
			linkElementList = driver.findElements(By.xpath(eventLinksXpath));
		}
		
		// collect event links from the list
		for (WebElement linkElement : linkElementList) {
			String href = linkElement.getAttribute("href");
			eventLinkList.add(href);
		}
		
		// download events from each link
		int eventsSoFar = 0;
		for (String eventLink : eventLinkList) {
			try {
				if (eventsSoFar < EVENT_LIMIT) {
					eventsSoFar++;
					Event event = downloadEventFromDetails(driver, eventLink);
					downloadedEventList.add(event);
				} else {
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return downloadedEventList;
	}
	
	public Event downloadEventFromDetails(WebDriver driver, String link) throws DownloadException {
		
		SimpleEvent event = new SimpleEvent();
		System.out.println("Downloading from link: " + link);
		driver.get(link);
		List<WebElement> yellowElements = driver.findElements(By.xpath(XPATH_YELLOW_CATEGORY));
		
		if (yellowElements.size() == 2) { 

			// both the name and the location of the event is found
			
			WebElement nameElement = yellowElements.get(0);
			WebElement locationElement= yellowElements.get(1);
			WebElement dateElement = null;
			try {
				dateElement = driver.findElement(By.xpath(XPATH_DATE));
			} catch (NoSuchElementException e) {
				try {
					dateElement = driver.findElement(By.xpath(XPATH_DATE_ADVERTISEMENT));
				} catch (NoSuchElementException e2) {
					try {
						dateElement = driver.findElement(By.xpath(XPATH_DATE_ALT));
					} catch (NoSuchElementException e3) {
						dateElement = driver.findElement(By.xpath(XPATH_DATE_ADVERTISEMENT_ALT));
					}
				}
			}
			WebElement descriptionElement = null;
			try {
				descriptionElement = driver.findElement(By.xpath(XPATH_DESCRIPTION));
			} catch (NoSuchElementException e) {
				descriptionElement = driver.findElement(By.xpath(XPATH_DESCRIPTION_ADVERTISEMENT));
			}
			String descriptionHtml = descriptionElement.getText();
			
			String name = nameElement.getText();
			String location = locationElement.getText();
			String dateStr = dateElement.getText(); 
			String description = descriptionHtml;
			
			event.setName(name);
			event.setDescription(description);
			Show show = new Show();
			show.setLocation(location);
			try {
				show.setStart(parseDate(dateStr));
			} catch (ParseException e) {
				System.out.println(String.format("Date string could not be parsed: \"%s\", error: %s", dateStr, e.getMessage()));
				throw new DownloadException(e);
			}
			event.getShowList().add(show);
			
			
		} else {
			System.out.println("!!! Number of yellow elements were " + yellowElements.size());
		}
		
		return event;
	}
	
	public Date parseDate(String str) throws ParseException {
	
		// 13.09.04. 20:00 (Sze)
		String[] parts = str.split(" \\(");
		DateFormat dateFormat = new SimpleDateFormat("yy.MM.dd. hh:mm");
		Date ret = dateFormat.parse(parts[0]); 
		return ret;
	}

}
