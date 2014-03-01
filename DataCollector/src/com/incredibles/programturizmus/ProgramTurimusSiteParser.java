package com.incredibles.programturizmus;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.incredibles.Log;
import com.incredibles.data.Event;
import com.incredibles.data.PtEvent;
import com.incredibles.data.PtEvent.PtEventType;
import com.incredibles.data.Show;
import com.incredibles.util.DateUtil;

/**
 * {@link SiteParser} implementation for http://www.programturizmus.hu
 */
public class ProgramTurimusSiteParser extends SiteParser {

	// XPath constants

	private static final String XPATH_TITLE = "/html/body/div/div/div/div[2]/div[2]/div[3]/div[2]/h1/a";
	// This will contain a lot of tags and inside them other tags in wich we
	// have the text
	private static final String XPATH_DESCRIPTION = "/html/body/div/div/div/div[2]/div[2]/div[3]/div[3]/div[@id=\"tdestination-show-offer\"]";
	private static final String XPATH_DATES = "/html/body/div/div/div/div[2]/div[2]/div[3]/div[3]/table[1]/tbody/tr/td[@class=\"cell-left\"]";
	// We have to iterate over the rows and look for the "Cím" string
	private static final String XPATH_ADDRESS_TABLE_ROWS_COL1 = "/html/body/div/div/div/div[2]/div[2]/div[3]/div[3]/table[2]/tbody/tr/td[1]";
	private static final String XPATH_ADDRESS_TABLE_ROWS_COL2 = "/html/body/div/div/div/div[2]/div[2]/div[3]/div[3]/table[2]/tbody/tr/td[2]";

	// regular expressions to identify date format

	/** 
	 * Regular expression for dates like this: 
	 * <br>"2013. június 14. /péntek/ - 2013. június 16. /vasárnap/" 
	 * */
	private static final String REGEXP_DATE_TYPE_1 = "[0-9]{4}\\. [^ ]+ [0-9]{1,2}\\. /[^/ ]+/ - [0-9]{4}\\. [^ ]+ [0-9]{1,2}\\. /[^/ ]+/[ ]*";
	
	/** 
	 * Regular expression for dates like this:
	 * <br>"2013. július 5. /péntek/ - 2013. július 7. /vasárnap/ 0:00 - 22:00 " 
	 * */
	private static final String REGEXP_DATE_TYPE_2 = "[0-9]{4}\\. [^ ]+ [0-9]{1,2}\\. /[^/ ]+/ - [0-9]{4}\\. [^ ]+ [0-9]{1,2}\\. /[^/ ]+/ "
			+ "[0-9]{1,2}:[0-9]{1,2} - [0-9]{1,2}:[0-9]{1,2}[ ]*";
	
	/** 
	 * Regular expression for dates like this:
	 * <br>"2013. június 23. /vasárnap/ 20:00 	"
	 * */
	private static final String REGEXP_DATE_TYPE_3 = "[0-9]{4}\\. [^ ]+ [0-9]{1,2}\\. /[^/ ]+/ [0-9]{1,2}:[0-9]{1,2}[ ]*";
	
	/**
	 * Regular expression for dates like this:
	 * <br>"2013. augusztus 9. /péntek/"
	 */
	private static final String REGEXP_DATE_TYPE_4 = "[0-9]{4}\\. [^ ]+ [0-9]{1,2}\\. /[^/ ]+/[ ]*";

	public ProgramTurimusSiteParser() {
		super("http://www.programturizmus.hu");
	}

	@Override
	public Event parseSite(WebDriver driver, int recursionLevel, PtEventType ptEventType) {

		PtEvent ret = null;

		try {
			String title = getTitle(driver);
			String description = getDescription(driver);
			String address = getAddress(driver);
			List<Show> showList = getShows(driver, address);
			ret = new PtEvent(ptEventType);
			ret.setName(title);
			ret.setDescription(description);
			ret.getShowList().addAll(showList);
		} catch (NoSuchElementException e) {
			e.printStackTrace();
			System.err.println("> Calm down... It was caught...");
		}

		return ret;
	}

	private String getTitle(WebDriver driver) throws NoSuchElementException {
		WebElement titleElement = driver.findElement(By.xpath(XPATH_TITLE));
		return titleElement.getText();
	}

	private String getDescription(WebDriver driver) throws NoSuchElementException {
		WebElement descriptionElement = driver.findElement(By.xpath(XPATH_DESCRIPTION));
		return descriptionElement.getText();
	}

	private String getAddress(WebDriver driver) {
		String ret = "";

		List<WebElement> addressTableRowsCol1 = driver.findElements(By.xpath(XPATH_ADDRESS_TABLE_ROWS_COL1));
		List<WebElement> addressTableRowsCol2 = driver.findElements(By.xpath(XPATH_ADDRESS_TABLE_ROWS_COL2));
		for (int i = 0; i < addressTableRowsCol1.size(); i++) {
			if (addressTableRowsCol1.get(i).getText().trim().startsWith("Cím")) {
				ret = addressTableRowsCol2.get(i).getText().trim();
				break;
			}
		}
		return ret;
	}

	private List<Show> getShows(WebDriver driver, String address) {
		/*
		 * ------------------- Date examples: ----------------------------------
		 * (1) "2013. június 14. /péntek/ - 2013. június 16. /vasárnap/" 
		 * (2) "2013. július 5. /péntek/ - 2013. július 7. /vasárnap/ 0:00 - 22:00 "
		 * (3) "2013. június 23. /vasárnap/ 20:00 	"
		 * (4) "2013. augusztus 9. /péntek/"
		 */

		List<Show> ret = new LinkedList<Show>();

		List<WebElement> dateElements = driver.findElements(By.xpath(XPATH_DATES));
		for (WebElement dateElement : dateElements) {
			String dateStr = null;
			try {
				dateStr = dateElement.getText().trim();
				if (!dateStr.equals("")) {
					Show show = null;
					if (dateStr.matches(REGEXP_DATE_TYPE_1)) {

						show = createShowFromType1(dateStr);

					} else if (dateStr.matches(REGEXP_DATE_TYPE_2)) {

						show = createShowFromType2(dateStr);

					} else if (dateStr.matches(REGEXP_DATE_TYPE_3)) {

						show = createShowFromType3(dateStr);

					} else if (dateStr.matches(REGEXP_DATE_TYPE_4)) {
						
						show = createShowFromType4(dateStr);
						
					}

					if (show != null) {
						show.setLocation(address);
						ret.add(show);
					} else {
						Log.myPrintln(String.format("Unknown dateformat: \"%s\"", dateStr));
					}
				}
			} catch (Exception e) {
				Log.myPrintln(String.format("\nException when parsing the date: %s", dateStr));
				e.printStackTrace();
			}
		}

		return ret;
	}

	/**
	 * Create a {@link Show} instance from a string like this:
	 * "2013. június 14. /péntek/ - 2013. június 16. /vasárnap/"
	 * 
	 * @param dateStr
	 *            The string that contains the date of the show to be created
	 * @return The created show instance
	 */
	private Show createShowFromType1(String dateStr) {
		String[] dateParts = dateStr.split(" - ");
		Date startDate = createDateFromString(dateParts[0], true);
		Date endDate = createDateFromString(dateParts[1], false); 
		return new Show(startDate, endDate);
	}

	/**
	 * Create a {@link Show} instance from a string like this:
	 * "2013. július 5. /péntek/ - 2013. július 7. /vasárnap/ 0:00 - 22:00"
	 * 
	 * @param dateStr
	 *            The string that contains the date of the show to be created
	 * @return The created show instance
	 */
	private Show createShowFromType2(String dateStr) {
		int end = dateStr.lastIndexOf('/') + 1;
		return createShowFromType1(dateStr.substring(0, end));
	}

	/**
	 * Create a {@link Show} instance from a string like this:
	 * "2013. június 23. /vasárnap/ 20:00"
	 * 
	 * @param dateStr
	 *            The string that contains the date of the show to be created
	 * @return The created show instance
	 */
	private Show createShowFromType3(String dateStr) {
		int end = dateStr.lastIndexOf(' ');
		
		Date startDate = createDateFromString(dateStr.substring(0, end), dateStr.substring(end + 1));
		
		return new Show(startDate);
	}
	
	/**
	 * Create a {@link Show} instance from a string like this:
	 * "2013. augusztus 9. /péntek/"
	 * 
	 * @param dateStr
	 *            The string that contains the date of the show to be created
	 * @return The created show instance
	 */
	private Show createShowFromType4(String dateStr) {
		Date startDate = createDateFromString(dateStr, true);
		return new Show(startDate);
	}

	/**
	 * Create a {@link Date} instance based on the dateStr, which is like the
	 * following: "2013. június 14. /péntek/"
	 * 
	 * @param dateStr
	 *            The string that contains the date
	 * @param isStart
	 *            If it is a start date, or an end date. If start, time will be
	 *            considered 00:01, otherwise 23:59
	 * @return The created date instance
	 */
	private Date createDateFromString(String dateStr, boolean isStart) {
		return createDateFromString(dateStr, isStart ? "00:01" : "23:59");
	}
	
	/**
	 * Create a {@link Date} instance based on the dateStr and timeStr 
	 * <br>dateStr example: "2013. június 14. /péntek/"  
	 * <br>timeStr example: "20:00"
	 * 
	 * @param dateStr
	 *            The string that contains the date
	 * @param timeStr
	 *            The string that contains the time - may be null, then it will
	 *            be handled as it were 00:00
	 * @return
	 */
	private Date createDateFromString(String dateStr, String timeStr) {
		String[] dateParts = dateStr.split(" ");
		int year = Integer.parseInt(dateParts[0].substring(0, 4));
		int month = DateUtil.getMonthFromString(dateParts[1]);
		int day = Integer.parseInt(dateParts[2].substring(0, dateParts[2].length() - 1));
		
		int hour = 0;
		int minute = 0;
		if (timeStr != null) {
			String[] timeParts = timeStr.split(":");
			hour = Integer.parseInt(timeParts[0]);
			minute = Integer.parseInt(timeParts[1]);
		}
		
		return DateUtil.createDate(year, month, day, hour, minute);
	}
}
