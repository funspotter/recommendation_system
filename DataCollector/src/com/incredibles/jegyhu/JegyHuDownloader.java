package com.incredibles.jegyhu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.incredibles.Config;
import com.incredibles.DownloadException;
import com.incredibles.Downloader;
import com.incredibles.data.Event;
import com.incredibles.data.Show;
import com.incredibles.util.DateUtil;

public class JegyHuDownloader implements Downloader {

	private final int maxPage;
	private final int maxEvents;
	private final LinkedHashSet<String> eventLinks;

	public JegyHuDownloader(int maxPage, int maxEvents) {
		this.maxPage = maxPage;
		this.maxEvents = maxEvents;
		this.eventLinks = new LinkedHashSet<String>();
	}

	private static final boolean SLEEP_ENABLED = true;

	public List<Event> downloadEvents(WebDriver driver) throws DownloadException {
		List<Event> ret = new LinkedList<Event>();
		for (int currentPage = 1; currentPage <= maxPage; currentPage++) {
			driver.get("http://jegy.hu/search.php?all=1&p=" + Integer.toString(currentPage));

			List<WebElement> eventElementList = driver.findElements(By.xpath("//ul[@class='tallozo']/li"));

			for (int i = 0; i < eventElementList.size(); i++) {
				WebElement filmElement = eventElementList.get(i);
				WebElement linkElement = filmElement.findElement(By.xpath("div[3]/a"));
				String link = linkElement.getAttribute("href");
				if (!link.startsWith("http://jegy.hu")) {
					link = "http://jegy.hu" + link;
				}
				eventLinks.add(link);
			}
		}
		int counter = 0;
		BufferedReader reader = null;
		try {
			if (Config.DEBUG) {
				reader = new BufferedReader(new InputStreamReader(System.in));
			}
			for (String link : eventLinks) {

				if (++counter >= maxEvents) {
					break;
				}

				System.out.println(String.format("link (%d/%d): %s", counter, eventLinks.size(), link));
				if (!Config.DEBUG) {
					// release mode
					try {
						Event event = downloadEventFromDetails(driver, link);
						ret.add(event);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					// debug mode
					boolean passed = false;
					while (!passed) {
						try {
							Event event = downloadEventFromDetails(driver, link);
							ret.add(event);
							passed = true;
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("Press any key to continue...");
							try {
								reader.readLine();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return ret;
	}
	
	protected Event downloadEventFromDetails(WebDriver driver, String link) {
		
		driver.get(link);
		JegyHuEvent jhEvent = new JegyHuEvent();
		
		By eventNameLocator = By.xpath("/html/body/div/div[3]/div/div/div/div[2]/div/div/h1/span");
		By shortDescriptionLocator = By.xpath("/html/body/div/div[3]/div/div/div/div[2]/div/div[2]/h4/span");
		By longDescriptionLocator = By.xpath("/html/body/div/div[3]/div/div/div/div[2]/div/div[2]/div[4]/span");
		By locationLocator = By.xpath("/html/body/div/div[3]/div/div/div/div[2]/div/div[2]/div/div[2]/a[1]");
		By boxListSelector = By.xpath("html/body/div[1]/div[3]/div[1]/div[@class='leftBoxShadow']");
		
		String eventName = driver.findElement(eventNameLocator).getText();
		String shortDescription = driver.findElement(shortDescriptionLocator).getText();
		String longDescription = driver.findElement(longDescriptionLocator).getText();
		String location = driver.findElement(locationLocator).getText();
		
		
		jhEvent.setName(eventName);
		jhEvent.setShortDescription(shortDescription);
		jhEvent.setDescription(longDescription);
		
		int boxIndex = 0;
		List<WebElement> boxElementList = driver.findElements(boxListSelector);
		for (int i = 0; i < boxElementList.size(); i++) {
			WebElement boxElement = boxElementList.get(i);
			List<WebElement> ticketLinkList = boxElement.findElements(By.id("jegylink"));
			if (ticketLinkList.size() == 1) {
				boxIndex = i;
			}
		}
		
//		By dateListLocator = 
//				By.xpath(String.format("/html/body/div/div[3]/div/div[@class='leftBoxShadow'][%d]/div/div/div/div[2]/div[@class='datesRow']", boxIndex));
		List<WebElement> dateElementList = boxElementList.get(boxIndex).findElements(By.xpath("div/div/div/div[2]/div[@class='datesRow']"));
		for (WebElement dateElement : dateElementList) {
			String dateStr = dateElement.findElement(By.xpath("div[1]")).getText();
			Date showDate = parseDate(dateStr);
			jhEvent.getShowList().add(new Show(showDate, location));
		}
		
		return jhEvent;
	}
	
	protected Date parseDate(String dateStr) {
		String[] fullDateParts = dateStr.split(", ");
		String[] dateParts = fullDateParts[0].split(" ");
		String[] timeParts = fullDateParts[1].split(":");
		
		int year = Integer.parseInt(dateParts[0].substring(0, 4));
		int month = DateUtil.getMonthFromString(dateParts[1]);
		String dayStr = dateParts[2].substring(0, dateParts[2].length() - 1);
		int day = Integer.parseInt(dayStr);
		int hour = Integer.parseInt(timeParts[0]);
		int minute = Integer.parseInt(timeParts[1]);
		
		return DateUtil.createDate(year, month, day, hour, minute);
	}
	
	@SuppressWarnings("unused")
	@Deprecated
	private void oldVersion(WebDriver driver) {
		List<Event> ret = new LinkedList<Event>();
		List<WebElement> eventElementList = driver.findElements(By.xpath("//ul[@class='tallozo']/li"));
		for (int i = 0; i < eventElementList.size(); i++) {
			eventElementList = driver.findElements(By.xpath("//ul[@class='tallozo']/li"));
			System.out.println(String.format(">> number of films %d", eventElementList.size()));

			if (i >= eventElementList.size()) {
				break;
			}
			JegyHuEvent jhEvent = new JegyHuEvent();

			WebElement filmElement = eventElementList.get(i);

			jhEvent.setIdo(filmElement.findElement(By.xpath("div[1]")).getText());
			jhEvent.setHelyszin(filmElement.findElement(By.xpath("div[2]/a[1]")).getText());
			jhEvent.setVaros(filmElement.findElement(By.xpath("div[2]/a[2]")).getText());
			jhEvent.setEloadas(filmElement.findElement(By.xpath("div[3]/a")).getText());
			filmElement.findElement(By.xpath("div[3]/a")).click();

			// details
			System.out.println(jhEvent.getIdo() + " " + jhEvent.getHelyszin() + " " + jhEvent.getVaros() + " " + jhEvent.getEloadas());

			// html/body/table/tbody/tr/td[2]/div[1]/table/tbody/tr/td[2]/span[3]
			try {
				if (SLEEP_ENABLED) {
					// without this it does not work, I dont know why
					Thread.sleep(1000);
				}

//				jhEvent.setRovidLeiras(driver.findElement(By.xpath("/html/body/div/div[3]/div/div/div/div[2]/div/div[2]/h4/span")).getText());
				jhEvent.setHosszuLeiras(driver.findElement(By.xpath("/html/body/div/div[3]/div/div/div/div[2]/div/div[2]/div[4]/span")).getText());
//				System.out.println(jhEvent.getRovidLeiras());
				System.out.println(jhEvent.getHosszuLeiras());

				WebElement we = driver.findElement(By.xpath("html/body/div[1]/div[3]/div[1]/div[2]/div/div/div/div[1]/div[1]/div[1]"));
				we.click();

				List<WebElement> jegyTipusok = driver.findElements(By.xpath("/html/body/div[1]/div[3]/div[1]/div[2]/div/div/div/div[3]/div"));
				System.out.println(Integer.toString(jegyTipusok.size()) + " lett maradhat?");
				HashMap<String, String> hm = new HashMap<String, String>();
				for (int j = 1; j <= jegyTipusok.size(); j++) {
					hm.put(driver
							.findElement(By.xpath("/html/body/div[1]/div[3]/div[1]/div[2]/div/div/div/div[3]/div[" + Integer.toString(j) + "]/div[2]"))
							.getText(),
							driver.findElement(
									By.xpath("/html/body/div[1]/div[3]/div[1]/div[2]/div/div/div/div[3]/div[" + Integer.toString(j) + "]/div[1]"))
									.getText());
				}
				jhEvent.setJegyTipusok(hm);

				List<WebElement> eloadasok = driver.findElements(By.xpath("html/body/div[1]/div[3]/div[1]/div[4]/div/div/div/div[2]/div"));
				List<String> eloadasokString = new ArrayList<String>();
				for (int j = 0; j < eloadasok.size(); j++) {
					WebElement tmp = eloadasok.get(j);
					try {
						eloadasokString.add(tmp.findElement(By.xpath("div[1]")).getText());
						System.out.println(tmp.findElement(By.xpath("div[1]")).getText());
					} catch (NoSuchElementException e) {
						e.printStackTrace();
					}
				}
				jhEvent.setEloadasok(eloadasokString);

				ret.add(jhEvent);

				driver.navigate().back();

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
				System.err.println("I caught that...");
			}
		}
	}
}
