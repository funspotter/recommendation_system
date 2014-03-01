package com.incredibles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.incredibles.cinemacity.CinemaCityJSONDownloader;
import com.incredibles.data.Event;
import com.incredibles.data.Show;
import com.incredibles.geocoder.Geocoder;
import com.incredibles.geocoder.Geocoder.MyLatLng;
import com.incredibles.imageUploader.ImageUploaderForMovie;
import com.incredibles.storage.ClientDbService;
import com.incredibles.storage.ClientDbServiceCreator;
import com.incredibles.storage.EventUploader;
import com.incredibles.storage.EventUploaderCreator;

public class DataCollectorDownloader {

	
	@SuppressWarnings("unused")
	private static final int KRITIKUSTOMEG_PAGE = 0;

	public void startDownloading() {
			
		System.out.println("Started at " + new Date().toString());
		
		List<Event> downloadedEvents = null;
		downloadedEvents = downloadEvents();
		try {
			enumerateEvents(downloadedEvents, true, false);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		uploadEvents(downloadedEvents);
		
		System.out.println("finished at " + new Date().toString());
	
		ClientDbService dbManager = null;
		try {
			dbManager = ClientDbServiceCreator.createCloud();
			List<String> allMovie = dbManager.getAllMovieTitle();
			for(String s : allMovie){
				ImageUploaderForMovie img = new ImageUploaderForMovie();
				HashMap<String,List<String>> pg = img.getImages(s);
				for(String key : pg.keySet()){
					dbManager.uploadMoviePoster(s, key, pg.get(key));
				}
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("upload movie images finished" + new Date().toString());
	}
	
	@SuppressWarnings("unused")
	private static void geocodeEvents(List<Event> eventList) {
		Set<String> addressSet = new HashSet<String>();
		Geocoder geocoder = new Geocoder();
		for (Event currentEvent : eventList) {
			for (Show currentShow : currentEvent.getShowList()) {
				String address = currentShow.getLocation();
				addressSet.add(address);
				MyLatLng latLng = geocoder.geocode(address);
				System.out.println("\n\"" + address + "\"\n\t" + latLng);
			}
		}
		System.out.println("\nDisctinct address values: ");
		for (String address : addressSet) {
			System.out.println(">> " + address);
		}
	}
	
	private static void enumerateEvents(List<Event> eventList, boolean displayShows, boolean step) throws IOException {
		int eventCounter = 0;
		int showCounter = 0;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(System.in));
			if (eventList != null) {
				for (Event e : eventList) {
					eventCounter++;
					String output = String.format("\n%s\n", e.toString());
					System.out.println(output);
					if (displayShows) {
						for (Show show : e.getShowList()) {
							showCounter++;
							System.out.println("\t>> " + show.toString());
						}
					}
					if (step) {
						reader.readLine();
					}
				}
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		System.out.println(String.format("%d events, %d shows enumerated", eventCounter, showCounter));
	}
	
	private static List<Event> downloadEvents() {
		WebDriver driver = null;
		
		List<Event> downloadedEvents = new LinkedList<Event>();
		try {
			DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
			desiredCapabilities.setBrowserName("chrome");
			//desiredCapabilities.setCapability("chrome.switches", Arrays.asList("--verbose"));
			//driver = new ChromeDriver(desiredCapabilities);
			
			//driver = new FirefoxDriver();
			driver = new HtmlUnitDriver();
			
			System.out.println("Browser started");

			// set the timeout
			driver.manage().timeouts().implicitlyWait(Config.IMPLICIT_WAIT_TIMEOUT, TimeUnit.SECONDS);

			List<Downloader> downloaderList = new ArrayList<Downloader>();
			//downloaderList.add(new TicketPortalDownloader());
			//downloaderList.add(new A38Downloader());
			//downloaderList.add(new JegyHuDownloader(JEGY_HU_MAX_PAGE, JEGY_HU_MAX_EVENTS));
			//downloaderList.add(new MorrisonsOperaDownloader());
			//downloaderList.add(new Morrisons2Downloader());
////			downloaderList.add(new ExperienceLADownloader(50,250));
			downloaderList.add(new CinemaCityJSONDownloader());
			
//nem jo			//downloaderList.add(new CinemaCityDownloader(CINEMACITY_PAGE));
			for (Downloader downloader : downloaderList) {
				System.out.println("Downloading with " + downloader.getClass().getName());
				downloadedEvents.addAll(downloader.downloadEvents(driver));
			}
			
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
	
	private static void uploadEvents(List<Event> eventList) {
		EventUploader uploader = null;
		try {
			uploader = EventUploaderCreator.create();
			uploader.uploadEvents(eventList);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (uploader != null) {
				try {
					uploader.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
