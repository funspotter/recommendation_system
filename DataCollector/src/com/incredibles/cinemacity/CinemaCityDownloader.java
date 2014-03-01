package com.incredibles.cinemacity;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Predicate;
import com.incredibles.Config;
import com.incredibles.DownloadException;
import com.incredibles.Downloader;
import com.incredibles.Log;
import com.incredibles.data.Event;
import com.incredibles.data.FilmEvent;
import com.incredibles.data.Show;

/**
 * Class for collecting events from http://www.cinemacity.hu/movies
 * 
 * @author Andris
 * 
 */
public class CinemaCityDownloader implements Downloader {
	
	private final int page;
	
	/**
	 * Wich page of the movies should be downloaded<br> 0 means all, 1 means the first page and so on
	 * 
	 * @param page
	 */
	public CinemaCityDownloader(int page) {
		this.page = page;
	}
	
	private static final boolean SLEEP_ENABLED = true;

	@Override
	public List<Event> downloadEvents(WebDriver driver) throws DownloadException {
		
		List<Event> ret = new LinkedList<Event>();
		
		// load the page
		driver.get("http://www.cinemacity.hu/movies");

		WebDriverWait webDriverWait = new WebDriverWait(driver, Config.EXPLICIT_WAIT_TIMEOUT);

		final Predicate<WebDriver> filmDetailsVisiblePredicate = new Predicate<WebDriver>() {

			public boolean apply(WebDriver driver) {
				System.out.println(String.format("filmDetailsVisiblePredicate.apply() called"));
				return filmDetailsVisible(driver);
			}
		};
		final Predicate<WebDriver> filmDetailsInvisiblePredicate = new Predicate<WebDriver>() {

			public boolean apply(WebDriver driver) {
				System.out.println(String.format("filmDetailsInvisiblePredicate.apply() called"));
				return !filmDetailsVisible(driver);
			}
		};

		// iterate over the films
		By filmListLocator = By.xpath("//ul[@id='categoryfeatures_portfolio']/li");
		List<WebElement> filmElementList = driver.findElements(filmListLocator);
		System.out.println(String.format(">> number of films %d", filmElementList.size()));

		final int initialIndex = (page == 0 ? 0 : (page - 1) * 12); 
		final int maxIndex = (page == 0 ? 36 : page * 12);
		
		for (int i = initialIndex; i < filmElementList.size() && i < maxIndex; i++) {

			// choose the next film element
			WebElement filmElement = filmElementList.get(i);

			// break if we reached the end of the film list
			// class='moreFeat paginateCtrl cat_0'
			By endLocator = By.xpath("./a[@class='featureInfo']");
			int size = filmElement.findElements(endLocator).size();
			if (size == 0) {
				System.out.println("there are no more films");
				break;
			} else {
				System.out.println("there are more films");
			}

			if (i != 0) {
				// wait for the details window to disappear
				webDriverWait.until(filmDetailsInvisiblePredicate);
			}

			// check if we need to move to the next page
			if (i % 12 == 0 && i != 0) {
				moveToNextPage(driver, webDriverWait);
			}

			// wait for the next film element to be clickable
			// XPath indexing starts with 1
			String xpath = String.format("//ul[@id='categoryfeatures_portfolio']/li[%d]", i + 1);
			By locator = By.xpath(xpath);
			webDriverWait.until(ExpectedConditions.elementToBeClickable(locator));

			// click on the film
			System.out.println(String.format("Clicking on film %d - %s", i, driver.getTitle()));
			filmElement.click();
//			Actions actions = new Actions(driver);
//			actions.click(filmElement);
//			actions.build().perform();
			try {

				// wait for the film details window to appear
				webDriverWait.until(filmDetailsVisiblePredicate);

				if (SLEEP_ENABLED) {
					// without this it does not work, I dont know why
					Thread.sleep(1000);
				}

				Event filmEvent = collectFilmData(driver, webDriverWait);
				if (nullOrEmpty(filmEvent.getName())) {
					// something went wrong, try again
					driver.findElement(By.id("fancy_close")).click();
					System.out.println(String.format("%d. Film closed, because the name was null or empty, repeating...", i));
					i--;
					continue;
				}
				List<Show> showList = collectShows(driver, webDriverWait);
				filmEvent.getShowList().addAll(showList);
				ret.add(filmEvent);

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
				System.err.println("I caught that...");
			}
			driver.findElement(By.id("fancy_close")).click();
			System.out.println("Film closed");
		}
		
		return ret;
	}
	
	private void moveToNextPage(WebDriver driver, WebDriverWait webDriverWait) throws DownloadException {
		if (page > 0) { // we are downloading only a certain page
			int currentPage = 0;
			final By pageNumberLocator = By.xpath("//span[@class='qp_counter']/span[1]");
			WebElement pageNumberElement = driver.findElement(pageNumberLocator);
			if (pageNumberElement != null) {
				currentPage = Integer.parseInt(pageNumberElement.getText().trim());
				while (currentPage != page) {
					
					if (pageNumberElement != null) {
						// go to the next page
						By nextPageLocator = By.xpath("//a[@class='qp_prev']");
						webDriverWait.until(ExpectedConditions.elementToBeClickable(nextPageLocator));
						driver.findElement(nextPageLocator).click();
						pageNumberElement = driver.findElement(pageNumberLocator);
						currentPage = Integer.parseInt(pageNumberElement.getText().trim());
					} else {
						// something went wrong
						throw new DownloadException("Hol az oldalszam?");
					}
				}
			} else {
				// something went wrong
				throw new DownloadException("Hol az oldalszam?");
			}
			
			
		} else { // we are downloading all pages in order
			By nextPageLocator = By.xpath("//a[@class='qp_prev']");
			webDriverWait.until(ExpectedConditions.elementToBeClickable(nextPageLocator));
			driver.findElement(nextPageLocator).click();
		}
	}

	/**
	 * Collect data from the website to create a {@link FilmEvent} object
	 * 
	 * @param driver
	 * @param webDriverWait
	 * @return
	 */
	private Event collectFilmData(WebDriver driver, WebDriverWait webDriverWait) {
		
		String title = 				driver.findElement(By.xpath("//div[@class='feature_info_title']")).getText();
		String originalTitle = 		driver.findElement(By.xpath("//div[@class='feature_info']/div[1]/div[2]")).getText();
		String genre = 				driver.findElement(By.xpath("//div[@class='feature_info']/div[2]/div[2]")).getText();
		String lengthInMinutes = 	driver.findElement(By.xpath("//div[@class='feature_info']/div[3]/div[2]")).getText();
		String releasedOn = 		driver.findElement(By.xpath("//div[@class='feature_info']/div[5]/div[2]")).getText();
		String rating = 			driver.findElement(By.xpath("//div[@class='feature_info']/div[6]/div[2]")).getText();
		String director = 			driver.findElement(By.xpath("//div[@class='feature_info']/div[7]/div[2]")).getText();
		String actors = 			driver.findElement(By.xpath("//div[@class='feature_info']/div[8]/div[2]")).getText();
		String countryYear = 		driver.findElement(By.xpath("//div[@class='feature_info']/div[9]/div[2]")).getText();
		String description = 		
				driver.findElement(By.xpath("//div[@class='feature_info_right_container']/div[2]/div[1]/div[1]/p")).getText();
		
		String[] releasedOnParts = releasedOn.split("/");
		
		int year = 0;
		int month = 0;
		int day = 0;
		int ratingI = 0;
		int length = 0;
		if (releasedOnParts.length > 1) {
			try {
				year = Integer.parseInt(releasedOnParts[0]);
			} catch (NumberFormatException e) {
				System.err.println("released on format exception: " + e.getMessage());
			}
			try {
				month = Integer.parseInt(releasedOnParts[1]);
			} catch (NumberFormatException e) {
				System.err.println("released on format exception: " + e.getMessage());
			}
			try {
				day = Integer.parseInt(releasedOnParts[2]);
			} catch (NumberFormatException e) {
				System.err.println("released on format exception: " + e.getMessage());
			}
			try {
				ratingI = Integer.parseInt(rating);
			} catch (NumberFormatException e) {
				System.err.println("released on format exception: " + e.getMessage());
			}
			try {
				length = Integer.parseInt(lengthInMinutes);
			} catch (NumberFormatException e) {
				System.err.println("released on format exception: " + e.getMessage());
			}
		} else {
			System.out.println(" ************************************** " + releasedOn);
		} 
		
		FilmEvent ret = new FilmEvent(title);
		ret.setOriginalTitle(originalTitle);
		ret.setGenre(genre);
		ret.setLengthInMin(length);
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1); // january is 0
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		ret.setReleasedOn(calendar.getTime());
		
		ret.setRating(ratingI);
		ret.setDirector(director);
		ret.setActors(actors);
		ret.setCountryYear(countryYear);
		ret.setDescription(description);
		
		return ret;
	}

	/**
	 * Collect the shows that belong to a certain film
	 * 
	 * @param driver
	 * @param webDriverWait
	 * 
	 * @throws InterruptedException
	 */
	private List<Show> collectShows(WebDriver driver, WebDriverWait webDriverWait) throws InterruptedException {

		List<Show> ret = new LinkedList<Show>();
		
		By cinemaListLocator = By.xpath("//div[@class='feature_info_bottom_container']/div[2]/div[1]/a[1]");
		By categoryListLocator = By.xpath("//div[@class='feature_info_bottom_container']/div[2]/div[2]/a[1]");
		By dateListLocator = By.xpath("//div[@class='feature_info_bottom_container']/div[2]/div[3]/a[1]");
		By timeListLocator = By.xpath("//div[@class='feature_info_bottom_container']/div[2]/div[4]/a[1]");

		By allCinemaLocator = By.xpath("//div[@class='feature_info_bottom_container']/div[2]/div[1]/ul[1]/div[1]/div[1]/li");
		By allCategoryLocator = By.xpath("//div[@class='feature_info_bottom_container']/div[2]/div[2]/ul[1]/div[1]/div[1]/li");
		By allDateLocator = By.xpath("//div[@class='feature_info_bottom_container']/div[2]/div[3]/ul[1]/div[1]/div[1]/li");
		String allTimeLocatorXPath = "//div[@class='feature_info_bottom_container']/div[2]/div[4]/ul[1]/div[1]/div[1]/li";
		By allTimeLocator = By.xpath(allTimeLocatorXPath);

		// open cinema list
		webDriverWait.until(ExpectedConditions.elementToBeClickable(cinemaListLocator));
		driver.findElement(cinemaListLocator).click();

		List<WebElement> cinemaElementList = driver.findElements(allCinemaLocator);
		Log.myPrintln(String.format(">> number of cinemas %d", cinemaElementList.size()));

		// we dont need the first one with index 0
		// TODO: iterate over the full list
		for (int i = 1; i < cinemaElementList.size(); i++) {
//		for (int i = 1; i < 2; i++) {
			WebElement cinemaElement = cinemaElementList.get(i);
			String cinemaText = cinemaElement.getText();

			try {
				webDriverWait.until(ExpectedConditions.visibilityOf(cinemaElement));
				Log.myPrintln(cinemaElement.getText());
				cinemaElement.click();

				// open category list
				webDriverWait.until(ExpectedConditions.elementToBeClickable(categoryListLocator));
				driver.findElement(categoryListLocator).click();

				List<WebElement> categoryElementList = driver.findElements(allCategoryLocator);
				Log.myPrintln(String.format(">> number of categories %d", categoryElementList.size()));
				// TODO: iterate over the full list
//				for (int j = 1; j < 2; j++) {
				for (int j = 1; j < categoryElementList.size(); j++) {
					WebElement categoryElement = categoryElementList.get(j);
					String categoryText = categoryElement.getText();
					Log.myPrintln("\t" + categoryElement.getText());
					categoryElement.click();

					// open date list
					webDriverWait.until(ExpectedConditions.elementToBeClickable(dateListLocator));
					driver.findElement(dateListLocator).click();

					List<WebElement> dateElementList = driver.findElements(allDateLocator);
					Log.myPrintln(String.format(">> number of dates %d", dateElementList.size()));
					
					// TODO: iterate over the full list
//					for (int k = 1; k < 2; k++) {
					for (int k = 1; k < dateElementList.size(); k++) {
						WebElement dateElement = dateElementList.get(k);
						String dateText = dateElement.getText();
						Log.myPrintln("\t\t" + dateElement.getText());
						
						try{
							dateElement.click();
						}catch(ElementNotVisibleException e){
							
						}
						
						// open time list
						webDriverWait.until(ExpectedConditions.elementToBeClickable(timeListLocator));
						driver.findElement(timeListLocator).click();

						List<WebElement> timeElementList = driver.findElements(allTimeLocator);
						Log.myPrintln(String.format(">> number of times %d", timeElementList.size()));
//						for (int l = 1; l < 2; l++) {
						for (int l = 1; l < timeElementList.size(); l++) {
							WebElement timeElement = timeElementList.get(l);
							Log.myPrintln("\t\t\t" + timeElement.getText());
							
							String timeText = timeElement.getText();
							
							Date date = null;
							
							if(dateText!=null&&timeText!=null&&timeText.length()>4&&dateText.length()>4){
								date = createDateObject(dateText, timeText);
							}
							
						 
							
							try {
//								webDriverWait.until(
//										ExpectedConditions.elementToBeClickable(
//												By.xpath(String.format("%s[%d]", allTimeLocatorXPath, l + 1))
//										)
//								);
//								timeElement.click();
								if(date!=null){
									ret.add(new Show(date, cinemaText, categoryText));
								}
								
//								webDriverWait.until(ExpectedConditions.elementToBeClickable(timeListLocator));
//								driver.findElement(timeListLocator).click();

							} catch (ElementNotVisibleException e) {
								// item is disabled, continue with the next one
								System.err.println(String.format("Element was not visible: \"%s\"", timeText));
								continue;
							} catch (TimeoutException e) {
								// item is disabled, continue with the next one
								System.err.println(String.format("Element was not clickable: \"%s\"", timeText));
								continue;
							}

						}

						webDriverWait.until(ExpectedConditions.elementToBeClickable(dateListLocator));
						driver.findElement(dateListLocator).click();
					}

					webDriverWait.until(ExpectedConditions.elementToBeClickable(categoryListLocator));
					driver.findElement(categoryListLocator).click();
				}

			} catch (TimeoutException e) {
				// if an item cannot be clicked, continue with the next one
				e.printStackTrace();
				continue;
			}

			// open the cinema list
			webDriverWait.until(ExpectedConditions.elementToBeClickable(cinemaListLocator));
			driver.findElement(cinemaListLocator).click();
		}
		
		return ret;
	}
	
	/**
	 * Create a date object from the read texts
	 * 
	 * @param dateText String containing "year/month/dayOfMonth dayOfWeek"
	 * @param timeText String containing "hourOfDay:minutes"
	 * @return
	 */
	private Date createDateObject(String dateText, String timeText) {
		
		System.out.println("dateText: "+dateText+" timeText: "+timeText);
		
		dateText = convertToUTF8(dateText);
		timeText = convertToUTF8(timeText);
		
		if (timeText.endsWith(" IMAX")) {
			timeText = timeText.substring(0, timeText.indexOf(" IMAX"));
		}
		if (timeText.endsWith(" VIP")) {
			timeText = timeText.substring(0, timeText.indexOf(" VIP"));
		}
		if (timeText.endsWith(" 4DX")) {
			timeText = timeText.substring(0, timeText.indexOf(" 4DX"));
		}
		
		timeText = timeText.replace(" ", "");
		
		String dateStr[] = dateText.split(" ")[0].split("/");
		String timeStr[] = timeText.split(":");
		Calendar calendar = Calendar.getInstance();
		
		try {
			int year = Integer.parseInt(dateStr[0]);
			int month = Integer.parseInt(dateStr[1]);
			int dayOfMonth = Integer.parseInt(dateStr[2]);
			int hourOfDay = Integer.parseInt(timeStr[0]);
			int minute = Integer.parseInt(timeStr[1]);

			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, month - 1); // january is 0
			calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			calendar.set(Calendar.MINUTE, minute);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
		} catch (NumberFormatException e) {
			System.err.println(String.format("Format not valid : %s %s", dateText, timeText));
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			e.printStackTrace();
		}
		
		return calendar.getTime();
	}
	
	/**
	 * Check if the film details window is visible
	 * 
	 * @param driver
	 *            driver
	 * @return If the film details window is visible
	 */
	private boolean filmDetailsVisible(WebDriver driver) {
		By locator = By.xpath("//div[@class='feature_info_bottom_container']");
//		List<WebElement> elements = driver.findElements(locator);
//		return elements != null && elements.size() > 0;
		boolean ret;
		try {
			driver.findElement(locator);
			ret = true;
		} catch (NoSuchElementException e) {
			ret = false;
		}
		return ret;
	}
	
	@SuppressWarnings("unused")
	/**
	 * Jump to the last page of the film list
	 * 
	 * @param driver
	 * @param webDriverWait
	 */
	private void jumpToTheLastPage(WebDriver driver, WebDriverWait webDriverWait) {
		By nextPageLocator = By.xpath("//a[@class='qp_prev']");
		webDriverWait.until(ExpectedConditions.elementToBeClickable(nextPageLocator));
		driver.findElement(nextPageLocator).click();
		webDriverWait.until(ExpectedConditions.elementToBeClickable(nextPageLocator));
		driver.findElement(nextPageLocator).click();
	}

	private static boolean nullOrEmpty(String str) {
		return str == null || str.equals("");
	}
	
	  public static String convertToUTF8(String s) {
	        String out = null;
	        try {
	            out = new String(s.getBytes("UTF-8"), "ISO-8859-1");
	        } catch (java.io.UnsupportedEncodingException e) {
	            return null;
	        }
	        return out;
	    }
}
