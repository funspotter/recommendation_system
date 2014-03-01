package com.incredibles.experiencela;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

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
import com.incredibles.jegyhu.JegyHuEvent;
import com.incredibles.util.DateUtil;

/**
 * Class for collecting events from http://www.experiencela.com/calendar
 * 
 * @author Csabi
 * 
 */
public class ExperienceLADownloader implements Downloader {

	private final int maxPage;
	private final int maxEvents;
	private final LinkedHashSet<String> eventLinks;

	public ExperienceLADownloader(int maxPage, int maxEvents) {
		this.maxPage = maxPage;
		this.maxEvents = maxEvents;
		this.eventLinks = new LinkedHashSet<String>();
	}

	private static final boolean SLEEP_ENABLED = true;

	public List<Event> downloadEvents(WebDriver driver) throws DownloadException {
		List<Event> ret = new LinkedList<Event>();
		for (int currentPage = 1; currentPage <= maxPage; currentPage++) {
			//http://www.experiencela.com/calendar?page=1
			driver.get("http://www.experiencela.com/calendar?page=" + Integer.toString(currentPage));

			List<WebElement> eventElementList = driver.findElements(By.xpath(".//*[@id='divfGrid']/ol/li"));

			System.out.println("elementnum on page "+Integer.toString(currentPage)+": "+eventElementList.size());
			
			for (int i = 0; i < eventElementList.size(); i++) {
				WebElement filmElement = eventElementList.get(i);
				WebElement linkElement = filmElement.findElement(By.xpath("div/h3/a[1]"));
				String link = linkElement.getAttribute("href");
				if (!link.startsWith("http://www.experiencela.com")) {
					link = "http://www.experiencela.com" + link;
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
		ExperienceLAEvent ELAevent = new ExperienceLAEvent();
		
		By eventNameLocator = By.xpath(".//*[@id='ctl00_ctl00_C_LC_view']/tbody/tr/td/div/h1");
		By longDescriptionLocator = By.xpath(".//*[@id='ctl00_ctl00_C_LC_view']/tbody/tr/td/div/div[3]/div/p");
		By locationLocator = By.xpath(".//*[@id='ctl00_ctl00_C_LC_view']/tbody/tr/td/div/div[2]/table/tbody/tr[2]/td/div/label/span");
		By locationLocator2 = By.xpath(".//*[@id='ctl00_ctl00_C_LC_view']/tbody/tr/td/div/div[2]/table/tbody/tr[2]/td/div/div/label");
		By imageLocator = By.xpath("//*[@id='ctl00_ctl00_C_LC_view']/tbody/tr/td/div/div[1]/img");
		
		String eventName = driver.findElement(eventNameLocator).getText();
		String longDescription = driver.findElement(longDescriptionLocator).getText();
		String location = driver.findElement(locationLocator).getText();
		String location2 = driver.findElement(locationLocator2).getText();
		String imgUrl = driver.findElement(imageLocator).getAttribute("src");
		
		imgUrl=imgUrl.replace("width=200&height=200", "width=450&height=450");
		
		System.out.println("location: "+location);
		System.out.println("location2: "+location2);
		
		String allLocation = location + " " + location2;
		
		ELAevent.setName(eventName);
		ELAevent.setDescription(longDescription);
		ELAevent.setThumbnail(imgUrl);
		
		List<WebElement> dateElementList = null;
		try{
			WebElement moreDatesButton = driver.findElement(By.xpath(".//*[@id='moredates']"));
			moreDatesButton.click();
			dateElementList = driver.findElements(By.xpath(".//*[@id='AllDates']/div/ul/li"));
		}catch(NoSuchElementException e){
			dateElementList = driver.findElements(By.xpath(".//*[@id='dates']"));
			//.//*[@id='dates']
		}
		
		
		
		
		////*[@id="ulDates"]
		
		for (WebElement dateElement : dateElementList) {
			
			
			try{
				String dateMY = dateElement.findElement(By.xpath("label[1]")).getText();//August 9, 2013
				String dateH = dateElement.findElement(By.xpath("label[2]")).getText();//9:00 AM - 12:00 PM
				
				Date showDate = null;
				try {
					showDate = parseDate(dateMY,dateH);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(showDate!=null){
					System.out.println("showdate nem null!!! "+showDate+" "+allLocation);
					ELAevent.getShowList().add(new Show(showDate, allLocation));
				}else{
					System.out.println("showdate null!!!");
				}
				
			}catch(NoSuchElementException e){
				
			}
			
			ELAevent.setLangCode("EN");

		}
		System.out.println(ELAevent.toString());
			
		return ELAevent;
	}
	
	private Date parseDate(String m,String h) throws ParseException{
		String[] newArr = m.split(", ");
		Date ret = new Date();
		if(newArr.length==2){
			String[] arr=newArr[0].split(" ");
			Date date = new SimpleDateFormat("MMM", Locale.ENGLISH).parse(arr[0]);
		    Calendar cal = Calendar.getInstance();
		    cal.setTime(date);
		    int month = cal.get(Calendar.MONTH);
		    int day = Integer.parseInt(arr[1]);
		    int year = Integer.parseInt(newArr[1]);
		    
		    String hourArr[] = h.split(" - ");
		    String startHourArr[] = hourArr[0].split(" ");
		    String ssarr[] = startHourArr[0].split(":");
		    int startHour = Integer.parseInt(ssarr[0]);
		    int startMin = Integer.parseInt(ssarr[1]);
		    String ampm = startHourArr[1];
		    if(ampm.equals("PM")){
		    	startHour+=12;
		    	if(startHour==24){
		    		startHour=0;
		    	}
		    }
		    
		    //System.out.println(year+" "+month+" "+day+" "+startHour+" "+startMin);
		    
			//ret = new Date(year,month,day,startHour,startMin);
		    
		    Calendar c = GregorianCalendar.getInstance();
		    c.set(year, month, day, startHour, startMin,0);
		    ret = c.getTime();
		    //System.out.println(ret);
		    
		    
		}
		
	    
		return ret;
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
