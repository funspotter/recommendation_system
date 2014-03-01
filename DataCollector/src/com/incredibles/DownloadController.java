package com.incredibles;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.incredibles.data.Event;
import com.incredibles.jegyhu.JegyHuDownloader;

public class DownloadController {
	
	public List<Event> downloadJegyHuEvents(int toPage) {
		WebDriver driver = null;
		
		List<Event> downloadedEvents = null;
		try {

			driver = new FirefoxDriver();
			//driver = new HtmlUnitDriver();

			driver.manage().timeouts().implicitlyWait(Config.IMPLICIT_WAIT_TIMEOUT, TimeUnit.SECONDS);

			Downloader downloader = null;
			downloader = new JegyHuDownloader(10, Integer.MAX_VALUE);

			downloadedEvents = downloader.downloadEvents(driver);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (driver != null) {
				driver.close();
			}
		}
		
		
		if (downloadedEvents != null) {
			Log.myPrintln(String.format("Number of downloaded events: %d", downloadedEvents.size()));
		} else {
			Log.myPrintln("No events were downloaded");
		}
		return downloadedEvents;
	}


}
