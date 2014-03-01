package com.incredibles.programturizmus;

import java.util.Date;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.incredibles.data.Event;
import com.incredibles.data.PtEvent;
import com.incredibles.data.PtEvent.PtEventType;
import com.incredibles.data.Show;
import com.incredibles.util.DateUtil;

/**
 * {@link SiteParser} implementation for http://www.szabadter.hu 
 */
public class SzabadterSiteParser extends SiteParser {

	// XPath constants
	
	private static final String XPATH_TITLE = "/html/body/center/div/center/table/tbody/tr/td/div/div/table/tbody/tr/td/div/div/div/div/h2[@class=\"itemTitle\"]";
	private static final String XPATH_DATE = "/html/body/center/div/center/table/tbody/tr/td/div/div/table/tbody/tr/td/div/div/div/div/span[@class=\"itemDateCreated\"]";
	private static final String XPATH_DESCRIPTION =
	// "/html/body/center/div/center/table/tbody/tr/td/div/div/table/tbody/tr/td/div/div/div/div[3]/div[2]/p";
			"/html/body/center/div/center/table/tbody/tr/td/div/div/table/tbody/tr/td/div/div/div/div[3]/div[2]/*[@mce_style=\"text-align: justify; \"]";
	private static final String XPATH_LOCATION = "/html/body/center/div/center/table/tbody/tr/td/div/div/table/tbody/tr/td/div/div/div/div[5]/div/a";
	
	/**
	 * Contructor
	 */
	public SzabadterSiteParser() {
		super("http://www.szabadter.hu");
	}
	
	@Override
	public Event parseSite(WebDriver driver, int recursionLevel, PtEventType ptEventType) {
		PtEvent ret = null;
		try {
			// find elements
			WebElement titleElement = driver.findElement(By.xpath(XPATH_TITLE));
			WebElement dateElement = driver.findElement(By.xpath(XPATH_DATE));
			WebElement locationElement = driver.findElement(By.xpath(XPATH_LOCATION));
			List<WebElement> descriptionElements = driver.findElements(By.xpath(XPATH_DESCRIPTION));
			
			// read in values
			String titleStr = titleElement.getText();
			String dateStr = dateElement.getText();
			String locationStr = locationElement.getText();
			StringBuilder descriptionSb = new StringBuilder();
			
			// process values
			int endIdx = titleStr.lastIndexOf(" - ");
			if (endIdx > 0) {
				titleStr = titleStr.substring(0, endIdx);
			}
			
			Date startDate = null;
			try {
				startDate = parseDate(dateStr);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				startDate = new Date(0);
			}
			
			for (WebElement textElement : descriptionElements) {
				descriptionSb.append(textElement.getText()).append("\n");
			}
//			Log.myPrintln(String.format("Title: %s\nDate: %s\nLocation: %s\nDescription: %s", titleStr, dateStr, locationStr, descriptionSb.toString()));

			ret = new PtEvent(ptEventType);
			ret.setName(titleStr);
			ret.setDescription(descriptionSb.toString());
			ret.getShowList().add(new Show(startDate, locationStr));
			
		} catch (NoSuchElementException e) {
			System.err.println("\n Caught exception");
			e.printStackTrace();
			System.err.println("\n");
		}
		return ret;
	}
	
	/**
	 * Parse date
	 * 
	 * @param dateStr
	 *            The string containing the date
	 * @return The parsed date
	 * 
	 * @throws NumberFormatException
	 *             If there is something wrong with the date format
	 */
	private Date parseDate(String dateStr) throws NumberFormatException {
		// date example: "2013. június 20. csütörtök 20:00"
		
		Date ret = null;
		String[] dateParts = dateStr.split(" ");

		if (dateParts.length == 5) {
		
			int year = Integer.parseInt(dateParts[0].substring(0, 4));
			int month = DateUtil.getMonthFromString(dateParts[1]);
			int day = Integer.parseInt(dateParts[2].substring(0, dateParts[2].length() - 1));
			
			String[] timeParts = dateParts[4].split(":");
			int hour = Integer.parseInt(timeParts[0]);
			int minute = Integer.parseInt(timeParts[1]);
			
			ret = DateUtil.createDate(year, month, day, hour, minute);
			
		} else {
			throw new NumberFormatException(String.format("dateParts.length was %d", dateParts.length));
		}
		
		return ret;
	}

}
