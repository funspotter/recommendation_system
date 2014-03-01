package com.incredibles.programturizmus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.incredibles.Config;
import com.incredibles.DownloadException;
import com.incredibles.Downloader;
import com.incredibles.Log;
import com.incredibles.data.Event;
import com.incredibles.data.PtEvent.PtEventType;

public class ProgramTurizmusDownloader implements Downloader {

	@Override
	public List<Event> downloadEvents(WebDriver driver)
			throws DownloadException {
		// TODO Auto-generated method stub
		return null;
	}

//	private static final String XPATH_TITLES =
//	// "//div[@class=\"connect-title\"]/h3/a";
//	"/html/body/div/div/div/div[4]/div[2]/div[6]/div[2]/div[@class=\"connect-item\"]/div[2]/h3/a";
//
//	private static final String XPATH_AGGREGATE_LINKS =
//	// "//table[@class=\"table-format\"]//a";
//	"/html/body/div/div/div/div[2]/div[2]/div[3]/div[3]/table/tbody/tr/td/ul/li/a";
//
//	private static final String[] START_URLS = new String[] {
//			"http://www.programturizmus.hu/tdcategory-fesztival-esemeny-rendezveny-magyarorszag.html?s=G2:R13", // Fesztival
//			"http://www.programturizmus.hu/tdcategory-hetvege-utazas-szabadido-magyarorszag.html?s=G2:R13", 	// Hetvege, szabadido
//			"http://www.programturizmus.hu/tdcategory-gasztronomia-esemeny-info.html?s=G2:R13", 				// Kostolas, fozes
//			"http://www.programturizmus.hu/tdcategory-kultura-kozosseg-programturizmus.html?s=G2:R13", 			// Kultura, muvelodes
//			"http://www.programturizmus.hu/tdcategory-orszagjaras-utazas.html?s=G2:R13", 						// Orszagjaras, utazas
//			"http://www.programturizmus.hu/tdcategory-iskolai-szunido-vakacio.html?s=G2:R13", 					// Taborozas, szunido
//			"http://www.programturizmus.hu/tdcategory-turazas-kirandulas-magyarorszag.html?s=G2:R13", 			// Tura, kirandulas
//			"http://www.programturizmus.hu/tdcategory-vilagjaras-egyeni-" +
//				"csoportos-szervezett-utak-utazasok-busz-repulo-hajo-auto.html?s=G2:R13", 						// Utazas, vilagjaras
//			"http://www.programturizmus.hu/tdcategory-unnep-jeles-nap-esemeny-naptar.html?s=G2:R13" 			// Unnep, jeles nap
//	};
//
//	private static final String[] RECURSION_PREFIX = new String[] { "", "  ", "    ", "      ", "        ", "          " };
//
//	private final Set<String> visitedUrls;
//	private final Set<String> handledDomains;
//	private final Set<String> unhandledUrls;
//	private final Map<String, SiteParser> siteParserMap;
//	private final PtEventType[] typesToCollect;
//
//	/**
//	 * Constructor - only the specified types of events will be collected
//	 * 
//	 * @param typesToCollect
//	 *            Array holding the types of events that should be collected
//	 */
//	public ProgramTurizmusDownloader(PtEventType[] typesToCollect) {
//		super();
//		this.typesToCollect = typesToCollect;
//		this.visitedUrls = new HashSet<String>();
//		this.handledDomains = new HashSet<String>();
//		this.unhandledUrls = new HashSet<String>();
//		this.siteParserMap = new HashMap<String, SiteParser>();
//		
//		for (SiteParser siteParser : SiteParser.getSiteParsers()) {
//			siteParserMap.put(siteParser.getDomain(), siteParser);
//		}
//	}
//
//	/**
//	 * Constructor - all types of events will be collected
//	 */
//	public ProgramTurizmusDownloader() {
//		this(null);
//	}
//
//	@Override
//	public List<Event> downloadEvents(WebDriver driver) {
//		LinkedList<Event> ret = new LinkedList<>();
//
//		if (!Config.DEBUG_PT) { // not debugging
//			
//			if (typesToCollect == null) { // we collect all types
//				for (PtEventType type : PtEventType.values()) {
//					List<Event> eventList = collectFromCategory(driver, START_URLS[type.ordinal()], type);
//					if (eventList != null) {
//						ret.addAll(eventList);
//					}
//				}
//			} else { // collect the specified types
//				for (PtEventType type : typesToCollect) {
//					List<Event> eventList = collectFromCategory(driver, START_URLS[type.ordinal()], type);
//					if (eventList != null) {
//						ret.addAll(eventList);
//					}
//				}
//			}
//		} else { // just debugging
//			
////			String url = "http://www.szabadter.hu/varosmajori-szabadteri-szinpad/item/1363-heltai-a-t%C3%BCnd%C3%A9rlaki-l%C3%A1nyok-v%C3%ADgj%C3%A1t%C3%A9k-2013-j%C3%BAnius-20.html";
////			String url = "http://www.programturizmus.hu/tdestination-dumtsa-korzo-szentendren.html";
//			String url = "http://www.programturizmus.hu/tdestination-sziget-fesztival-budapest.html";
//			driver.get(url);
//			Event event = collectFromSimpleSite(driver, url, 0, PtEventType.FESTIVAL);
//			if (event != null) {
//				ret.add(event);
//			}
//		}
//		
//		
//		if (handledDomains.size() > 0) {
//			Log.myPrintln("\nThe handled domains are: \n");
//			for (String handledDomain : handledDomains) {
//				Log.myPrintln(String.format("\t%s\n", handledDomain));
//			}
//		} else {
//			Log.myPrintln("\nNo domains were handled.\n");
//		}
//		if (unhandledUrls.size() > 0) {
//			Log.myPrintln("\nThe unhandled urls are: \n");
//			for (String unhandledUrl : unhandledUrls) {
//				Log.myPrintln(String.format("\t%s\n", unhandledUrl));
//			}
//		} else {
//			Log.myPrintln("\nThere were no unhandled domains.\n");
//		}
//		return ret;
//	}
//
//	private List<Event> collectFromCategory(WebDriver driver, String url, PtEventType ptEventType) {
//
//		List<Event> ret = new LinkedList<>();
//		driver.get(url);
//		Log.myPrintln(driver.getTitle());
//		List<WebElement> festivalList = driver.findElements(By.xpath(XPATH_TITLES));
//		Log.myPrintln(String.format("number of events: %d", festivalList.size()));
//
//		List<String> eventUrls = new LinkedList<String>();
//		for (WebElement titleElement : festivalList) {
//			Log.myPrintln(titleElement.getText());
//			eventUrls.add(titleElement.getAttribute("href"));
//		}
//		Log.myPrintln("\nCollecting...\n");
//		for (String eventUrl : eventUrls) {
//			List<Event> collectFromSite = collectFromSite(driver, eventUrl, 0, ptEventType);
//			if (collectFromSite != null) {
//				ret.addAll(collectFromSite);
//			}
//		}
//		Log.myPrintln(String.format("%d urls visited", visitedUrls.size()));
//		return ret;
//	}
//
//	private List<Event> collectFromSite(WebDriver driver, String url, int recursionLevel, PtEventType ptEventType) {
//		List<Event> ret = null;
//		if (!visitedUrls.contains(url)) {
//			visitedUrls.add(url);
//			ret = new LinkedList<>();
//			driver.get(url);
//
//			List<WebElement> aggregateLinks = driver.findElements(By.xpath(XPATH_AGGREGATE_LINKS));
//			if (aggregateLinks.size() > 1) { // Aggregate site
//				Log.myPrintln(RECURSION_PREFIX[recursionLevel] + "Aggregate site: " + driver.getTitle() + ", " + url);
//				List<Event> collectFromAggregateSite = collectFromAggregateSite(driver, url, recursionLevel + 1, aggregateLinks, ptEventType);
//				if (collectFromAggregateSite != null) {
//					ret.addAll(collectFromAggregateSite);
//				}
//			} else { // Simple site
//				Log.myPrintln(RECURSION_PREFIX[recursionLevel] + "Simple site: " + driver.getTitle() + ", " + url);
//				Event collectedEvent = collectFromSimpleSite(driver, url, recursionLevel, ptEventType);
//				if (collectedEvent != null) {
//					ret.add(collectedEvent);
//				}
//			}
//		}
//		return ret;
//	}
//
//	private List<Event> collectFromAggregateSite(WebDriver driver, String url, int recursionLevel, List<WebElement> aggregateLinks, PtEventType ptEventType) {
//		List<Event> ret = new LinkedList<Event>();
//		List<String> urlsToVisit = new LinkedList<String>();
//		for (WebElement linkElement : aggregateLinks) {
//			String linkUrl = linkElement.getAttribute("href");
//			
//			/*
//			 * We only visit those urls that have a registered SiteParser
//			 */
//			String foundDomain = null;
//			for (String domain : siteParserMap.keySet()) {
//				if (linkUrl.startsWith(domain)) {
//					foundDomain = domain;
//					urlsToVisit.add(linkUrl);
//					break;
//				}
//			}
//			if (foundDomain != null) {
//				handledDomains.add(foundDomain);
//			} else {
//				unhandledUrls.add(linkUrl);
//			}
//			
//		}
//		for (String linkUrl : urlsToVisit) {
//			List<Event> collectedEvents = collectFromSite(driver, linkUrl, recursionLevel, ptEventType);
//			if (collectedEvents != null) {
//				ret.addAll(collectedEvents);
//			}
//		}
//		return ret;
//	}
//
//	private Event collectFromSimpleSite(WebDriver driver, String url, int recursionLevel, PtEventType ptEventType) {
//		Event ret = null;
//		String foundDomain = null;
//		for (String domain : siteParserMap.keySet()) {
//			if (url.startsWith(domain)) {
//				ret = siteParserMap.get(domain).parseSite(driver, recursionLevel, ptEventType);
//				foundDomain = domain;
//				break;
//			}
//		}
//		if (foundDomain != null) {
//			handledDomains.add(foundDomain);
//		} else {
//			unhandledUrls.add(url);
//		}
//		return ret;
//	}

}
