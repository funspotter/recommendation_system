package com.incredibles.programturizmus;


import org.openqa.selenium.WebDriver;

import com.incredibles.data.Event;
import com.incredibles.data.PtEvent.PtEventType;

/**
 * Interface to collect an event from a certain domain 
 */
public abstract class SiteParser {
	
	public static SiteParser[] getSiteParsers() {
		return new SiteParser[] {
				new ProgramTurimusSiteParser(),
				new SzabadterSiteParser()
		};
	}
	
	protected final String domain;
	
	public SiteParser(String domain) {
		this.domain = domain;
	}
	
	/**
	 * Get the name of the domain for which the SiteParser can be used
	 * 
	 * @return The name of the domain
	 */
	public String getDomain() {
		return this.domain;
	}
	
	/**
	 * Parse a single site to collect a single event
	 * 
	 * @param driver
	 *            The {@link WebDriver} implementation to use
	 * @param recursionLevel
	 *            The level of recursion - used to format the log output
	 * @param ptEventType
	 *            type of the event that will be created
	 * 
	 * @return The collected event
	 */
	public abstract Event parseSite(WebDriver driver, int recursionLevel, PtEventType ptEventType);
}