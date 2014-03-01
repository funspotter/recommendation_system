package com.incredibles.a38;

import java.util.Calendar;
import java.util.Date;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.incredibles.Downloader;
import com.incredibles.LinkCollectorDownloader;
import com.incredibles.data.Event;
import com.incredibles.data.Show;
import com.incredibles.data.SimpleEvent;

/**
 * {@link Downloader} implementation for the page http://www.a38.hu/hu/programok  
 */
public class A38Downloader extends LinkCollectorDownloader {

	private static final int EVENT_LIMIT = Integer.MAX_VALUE;
//	private static final int EVENT_LIMIT = 5;
	
	private static final String PAGE_URL = "http://www.a38.hu/hu/programok";
	
	// XPath constants used to find elements on the html page
	
	private static final String XPATH_LINKS = 
			"html/body/div[2]/div[1]/div[2]/div[@class='cont_kozep_02']/div[@class='program_lista_sor']/div[@class='program_lista_content']/div[@class='program_lista_content_head']/a";
	
	private static final String XPATH_EVENT_PREFIX = 
			"html/body/div[2]/div[1]/div[@class='bal_and_kozep_wrapper']/div[@class='cont_kozep_01']/div[@class='vezercikk_wrapper']/div[@class='vezercikk_content']/div[@class='vezercikk_text']/";
	
	private static final String XPATH_EVENT_PROGRAMSOROZAT = 
			XPATH_EVENT_PREFIX + "div[@class='vezercikk_programsorozat']/a";
	
	private static final String XPATH_EVENT_FO_FELLEPO = 
			XPATH_EVENT_PREFIX + "div[@class='vezercikk_fofellepo']";
	
	private static final String XPATH_EVENT_TOVABBI_FELLEPOK = 
			XPATH_EVENT_PREFIX + "div[@class='vezercikk_tovabbi_fellepok']";
	
	private static final String XPATH_EVENT_DATUM = 
			XPATH_EVENT_PREFIX + "div[@class='vezercikk_datum uppercase']";
	
	private static final String XPATH_EVENT_HELYSZIN = 
			XPATH_EVENT_PREFIX + "div[@class='vezercikk_helyszin']";
	
	private static final String XPATH_EVENT_JEGYARAK = 
			XPATH_EVENT_PREFIX + "div[@class='vezercikk_jegyarak']";
	
	private static final String XPATH_EVENT_STILUS = 
			XPATH_EVENT_PREFIX + "div[@class='vezercikk_stilus uppercase']";
	
	private static final String XPATH_EVENT_LEIRAS =
			".//div[@class='program_egy_program_lead margin_left_divider']/p";
	
	private static final String XPATH_EVENT_KEPLINK = 
			".//div[@class='vezercikk_pic']/img";
	
	// regular expressions for date parsing
	
	/** Matches: 14.04.03.CS�T�RT�K... (dates with year), regardless of hour and minute */
	private static final String REGEXP_YEAR = "^([0-9]{2}.){3}[^\\.]*.*";
	
	/** Matches: 14.04.03.CS�T�RT�K.20 ... */
	private static final String REGEXP_YEAR_WITH_HOUR = "^([0-9]{2}.){3}[^\\.]*\\.[0-9]{2}.*";
	
	/** Matches: 14.04.03.CS�T�RT�K.20.30H */
	private static final String REGEXP_YEAR_WITH_HOUR_AND_MINUTE = "^([0-9]{2}.){3}[^\\.]*\\.[0-9]{2}.[0-9]{2}H$";
	
	/** Matches: 04.03.CS�T�RT�K... (dates without year), regardless of hour and minute */
	private static final String REGEXP_NO_YEAR = "^([0-9]{2}.){2}[^\\.]*.*";
	
	/** Matches: 04.03.CS�T�RT�K.20 ... */
	private static final String REGEXP_NO_YEAR_WITH_HOUR = "^([0-9]{2}.){2}[^\\.]*\\.[0-9]{2}.*";
	
	/** Matches: 04.03.CS�T�RT�K.20.30H */
	private static final String REGEXP_NO_YEAR_WITH_HOUR_AND_MINUTE = "^([0-9]{2}.){2}[^\\.]*\\.[0-9]{2}.[0-9]{2}H$";
	
	public A38Downloader() {
		super(PAGE_URL, XPATH_LINKS, EVENT_LIMIT);
	}
	
	@Override
	protected Event downloadEventFromDetails(WebDriver driver, String eventLink) {
		SimpleEvent ret = new SimpleEvent();
		System.out.println(String.format("Downloading from link: %s", eventLink));
		driver.get(eventLink);
		
		WebElement programSorozatElement = null;
		try {
			programSorozatElement = driver.findElement(By.xpath(XPATH_EVENT_PROGRAMSOROZAT));
		} catch (NoSuchElementException e) {
			System.out.println("ProgramSorozatElement not found");
		}
		
		WebElement foFellepoElement = null;
		try {
			foFellepoElement = driver.findElement(By.xpath(XPATH_EVENT_FO_FELLEPO));
		} catch (NoSuchElementException e) {
			System.out.println("FoFellepoElement not found");
		}
		
		WebElement tovabbiFellepokElement = null;
		try {
			tovabbiFellepokElement = driver.findElement(By.xpath(XPATH_EVENT_TOVABBI_FELLEPOK));
		} catch (NoSuchElementException e) { 
			System.out.println("TovabbiFellepokElement not found");
		}
		
		WebElement datumElement = null;
		try {
			datumElement = driver.findElement(By.xpath(XPATH_EVENT_DATUM));
		} catch (NoSuchElementException e) {
			System.out.println("DatumElement not found");
		}
		
		WebElement helyszinElement = null;
		try {
			helyszinElement = driver.findElement(By.xpath(XPATH_EVENT_HELYSZIN));
		} catch (NoSuchElementException e) {
			System.out.println("HelyszinElement not found");
		}
		
		WebElement jegyarakElement = null;
		try {
			jegyarakElement = driver.findElement(By.xpath(XPATH_EVENT_JEGYARAK));
		} catch (NoSuchElementException e) {
			System.out.println("JegyarakElement not found");
		}

		WebElement stilusElement = null;
		try {
			stilusElement = driver.findElement(By.xpath(XPATH_EVENT_STILUS));
		} catch (NoSuchElementException e) {
			System.out.println("StilusElement not found");
		}
		
		WebElement leirasElement = null;
		try {
			leirasElement = driver.findElement(By.xpath(XPATH_EVENT_LEIRAS));
		} catch (NoSuchElementException e) {
			System.out.println("LeirasElement not found");
		}

		WebElement keplinkElement = null;
		try {
			keplinkElement = driver.findElement(By.xpath(XPATH_EVENT_KEPLINK));
		} catch (NoSuchElementException e) {
			System.out.println("KeplinkElement not found");
		}
		
		String programSorozat 	= programSorozatElement == null 	? "" : programSorozatElement.getText();
		String foFellepo 		= foFellepoElement == null 			? "" : foFellepoElement.getText();
		String tovabbiFellepok 	= tovabbiFellepokElement == null 	? "" : tovabbiFellepokElement.getText();
		String datum 			= datumElement == null 				? "" : datumElement.getText();
		String helyszin 		= helyszinElement == null 			? "" : helyszinElement.getText();
		String jegyarak 		= jegyarakElement == null 			? "" : jegyarakElement.getText();
		String stilus 			= stilusElement == null 			? "" : stilusElement.getText(); // TODO: store style
		String leiras 			= leirasElement == null 			? "" : leirasElement.getText();
		String keplink 			= keplinkElement == null 			? "" : keplinkElement.getAttribute("src");
		final String linkPrefix = "http://www.a38.hu/"; 
		if (!keplink.startsWith(linkPrefix)) {
			keplink = linkPrefix + keplink;
		}
		
//		System.out.println(
//				"Programsorozat: " + programSorozat +
//				"\nFofellepo: " + foFellepo +
//				"\nTovabbi fellepok: " + tovabbiFellepok +
//				"\nDatum: " + datum +
//				"\nHelyszin: " + helyszin +
//				"\nJegyarak: " + jegyarak +
//				"\nStilus" + stilus + "\n"
//		); 
		
		StringBuilder nameBuilder = new StringBuilder();
		if (programSorozat != null) {
			nameBuilder.append(programSorozat);
		}
		if (foFellepo != null) {
			if (nameBuilder.length() > 0) {
				nameBuilder.append(" ");
			}
			nameBuilder.append(foFellepo);
		}
		if (tovabbiFellepok != null) {
			if (nameBuilder.length() > 0) {
				nameBuilder.append(" ");
			}
			nameBuilder.append(tovabbiFellepok);
		}
		ret.setName(nameBuilder.toString());
		
		if (jegyarak != null) {
			leiras = leiras + "\n\n" + jegyarak;
		}
		ret.setDescription(leiras);
		ret.setImage(keplink);
		ret.setDiscriminator("Party");
		
		Show show = new Show();
		Date startDate = null;
		Date endDate = null;
		if (datum.contains(" - ")) {
			String[] dateStringArray = datum.split(" - ");
			startDate = parseDate(dateStringArray[0]);
			endDate = parseDate(dateStringArray[1]);
		} else {
			startDate = parseDate(datum);
		}
		show.setStart(startDate);
		show.setEnd(endDate);
//		show.setLocation(helyszin); // helyszin means the name of the room in A38
		show.setLocation("Budapest, Pet�fi h�d, budai h�df�");
		ret.getShowList().add(show);
		
		return ret;
	}
	
	private static Date parseDate(String dateStr) {
		
		String[] dateParts = dateStr.split("\\.");
		String yearStr = null;
		String monthStr = null;
		String dayStr = null;
		String hourStr = null;
		String minuteStr = null;
		
		// determine the pattern of the dateStr
		if (dateStr.matches(REGEXP_YEAR)) {
			
			yearStr = dateParts[0];
			monthStr = dateParts[1];
			dayStr = dateParts[2];
			// dateParts[3] is the name of the day
			if (dateStr.matches(REGEXP_YEAR_WITH_HOUR)) {
				hourStr = dateParts[4];
				if (dateStr.matches(REGEXP_YEAR_WITH_HOUR_AND_MINUTE)) {
					minuteStr = dateParts[5];
					minuteStr = minuteStr.substring(0, minuteStr.length() - 1); // ignore the last character, wich is 'H'
				} else {
					hourStr = hourStr.substring(0, hourStr.length() - 1); // ignore the last character, wich is 'H'
				}
			} 
			
		} else if (dateStr.matches(REGEXP_NO_YEAR)) {
			
			monthStr = dateParts[0];
			dayStr = dateParts[1];
			// dateParts[2] is the name of the day
			if (dateStr.matches(REGEXP_NO_YEAR_WITH_HOUR)) {
				hourStr = dateParts[3];
				if (dateStr.matches(REGEXP_NO_YEAR_WITH_HOUR_AND_MINUTE)) {
					minuteStr = dateParts[4];
					minuteStr = minuteStr.substring(0, minuteStr.length() - 1); // ignore the last character, wich is 'H'
				} else {
					hourStr = hourStr.substring(0, hourStr.length() - 1); // ignore the last character, wich is 'H'
				}
			}
			
		} else  {
			throw new IllegalArgumentException("Unknown date format");
		}

		Calendar cal = Calendar.getInstance();
		
		int year = 0;
		int month = 0;
		int day = 0;
		int hour = 0;
		int minute = 0;

		// parse strings
		if (yearStr != null) {
			year = Integer.parseInt(yearStr);
			year += 2000;
		} else {
			year = cal.get(Calendar.YEAR);
		}
		month = Integer.parseInt(monthStr);
		day = Integer.parseInt(dayStr);
		if (hourStr != null) {
			hour = Integer.parseInt(hourStr);
		}
		if (minuteStr != null) {
			minute = Integer.parseInt(minuteStr);
		}
		
		// set up calendar
		cal.set(year, month - 1, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTime();
	}
}
