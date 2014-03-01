package com.incredibles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

import com.incredibles.a38.A38Downloader;
import com.incredibles.cinemacity.CinemaCityDownloader;
import com.incredibles.cinemacity.CinemaCityJSONDownloader;
import com.incredibles.data.Event;
import com.incredibles.data.Show;
import com.incredibles.experiencela.ExperienceLADownloader;
import com.incredibles.geocoder.Geocoder;
import com.incredibles.geocoder.Geocoder.MyLatLng;
import com.incredibles.imageUploader.ImageUploaderForMovie;
import com.incredibles.jegyhu.JegyHuDownloader;
import com.incredibles.morrisons.Morrisons2Downloader;
import com.incredibles.morrisons.MorrisonsOperaDownloader;
import com.incredibles.storage.ClientDbService;
import com.incredibles.storage.ClientDbServiceCreator;
import com.incredibles.storage.EventUploader;
import com.incredibles.storage.EventUploaderCreator;
import com.incredibles.ticketportal.TicketPortalDownloader;

public class Main {
	
	private static final int JEGY_HU_MAX_PAGE = 20;
	private static final int JEGY_HU_MAX_EVENTS = Integer.MAX_VALUE;
	private static final int CINEMACITY_PAGE = 0;
	
	private static final int FLAG_DOWNLOAD 		= 0b0001;
	private static final int FLAG_SERIALIZE 	= 0b0010;
	private static final int FLAG_DESERIALIZE 	= 0b0100;
	private static final int FLAG_UPLOAD 		= 0b1000;
	
	@SuppressWarnings("unused")
	private static final int KRITIKUSTOMEG_PAGE = 0;

	public static void main(String[] args) {

//		//ezzel javitjuk ki az adatbazisban levo rossz linkeket és link tomboket jora
////		ConverthtmlToPlain cv = new ConverthtmlToPlain();
////		cv.convert();
//		
//		//ez tolti le a mozikhoz a kepeket
////		ImageUploaderForMovie img = new ImageUploaderForMovie();
////		img.uploadImages("Star Wars");
//		
		System.out.println("Started at " + new Date().toString());
		int parsedArgs = parseArgs(args);
		if (parsedArgs < 0) {
			printHelp();
		} else {

			List<Event> downloadedEvents = null;
			try {
				if ((parsedArgs & FLAG_DOWNLOAD) > 0) { 
					downloadedEvents = downloadEvents(args);
				}
				// enumerateEvents(downloadedEvents, false, false);
				if ((parsedArgs & FLAG_SERIALIZE) > 0) {
					serializeEvents(downloadedEvents, Config.SERIALIZATION_FILENAME);
				}
				if ((parsedArgs & FLAG_DESERIALIZE) > 0) {
					downloadedEvents = deserializeEvents(Config.SERIALIZATION_FILENAME);
				}
				
				enumerateEvents(downloadedEvents, true, false);
				
//				for (Event event : downloadedEvents) {
//					if (event.hasManyShowsOnOneDay()) {
//						event.hasManyShowsOnOneDay();
//						System.out.println(event.getName());
//					}
//				}
//				enumerateEvents(downloadedEvents, true, false);
				if ((parsedArgs & FLAG_UPLOAD) > 0) {
					uploadEvents(downloadedEvents);
				}

				// geocodeEvents(downloadedEvents);

			} catch (IllegalArgumentException e) {
				printHelp();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("finished at " + new Date().toString());
//		
//		System.out.println("upload movie images" + new Date().toString());
		
//		ClientDbService dbManager = null;
//		try {
//			dbManager = ClientDbServiceCreator.createCloud();
//			List<String> allMovie = dbManager.getAllMovieTitle();
//			for(String s : allMovie){
//				ImageUploaderForMovie img = new ImageUploaderForMovie();
//				HashMap<String,List<String>> pg = img.getImages(s);
//				for(String key : pg.keySet()){
//					dbManager.uploadMoviePoster(s, key, pg.get(key));
//				}
//				
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
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
	
	private static List<Event> downloadEvents(String[] args) {
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
	
	private static void serializeEvents(List<Event> eventList, String fileName) {
		
		ObjectOutputStream oos = null;
		try {
			String prefix = "all";
			File outputFile = new File(String.format("%s_%s", prefix, fileName));
			oos = new ObjectOutputStream(new FileOutputStream(outputFile));
			
			oos.writeObject(eventList);
			oos.flush();
			System.out.println("Serialization successful");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@SuppressWarnings({ "unused", "unchecked" })
	private static List<Event> deserializeEvents(String fileName) {
		
		List<Event> ret = null;
		ObjectInputStream ois = null;
		
		try {
			String prefix = "all";
			File inputFile = new File(String.format("%s_%s", prefix, fileName));
			ois = new ObjectInputStream(new FileInputStream(inputFile));
			ret = (List<Event>) ois.readObject(); 
			System.out.println("Deserialization successful");
			
			for (Event event : ret) {
				if (event.getLangCode() == null) {
					event.setLangCode("HU");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return ret;
	}

	/**
	 * Get the event type specified in the command line arguments
	 * 
	 * @param args
	 *            The command line arguments
	 * @return The event type specified in the command line arguments
	 * 
	 * @throws IllegalArgumentException
	 *             If there werent any arguments, or invalid event type was
	 *             specified
	 */
	private static int parseArgs(String[] args) throws IllegalArgumentException {
		int ret = 0;
		if (args.length == 0) {
			ret = FLAG_DOWNLOAD | FLAG_SERIALIZE | FLAG_UPLOAD;
		} else {
			for (String arg : args) {
				if (arg.equals("-d")) {	// Download events
					ret = ret | FLAG_DOWNLOAD;
					System.out.println("download");
				} else if (arg.equals("-s")) { // Save to file
					ret = ret | FLAG_SERIALIZE;
					System.out.println("serialize");
				} else if (arg.equals("-l")) { // Load from file
					ret = ret | FLAG_DESERIALIZE;
					System.out.println("deserialize");
				} else if (arg.equals("-u")) {
					ret = ret | FLAG_UPLOAD;
					System.out.println("upload");
				} else if (arg.equals("-h")) {
					ret = -1;
					System.out.println("help");
				}
			}
		}
		
		return ret;
	}
	
	/**
	 * Print the help for the command line arguments
	 */
	private static void printHelp() {
		String help = "Valid parameters are:\n" +
				"-d : download\n" +
				"-s : save to file\n" +
				"-l : load from file\n" +
				"-u : upload to server\n" +
				"-h : help";
		System.out.println(help);
	}
}
