package com.incredibles.cinemacity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
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

public class CinemaCityJSONDownloader implements Downloader {

	

	
	
	@Override
	public List<Event> downloadEvents(WebDriver driver) throws DownloadException {
		
		List<Event> ret = new LinkedList<Event>();
		
		HashMap <String, String> allMovies = new HashMap<String, String>();
		
		try {
			JSONObject json = readJsonFromUrl("http://www.cinemacity.hu/presentationsJSON");
			JSONArray sites = json.getJSONArray("sites");
			for(int i = 0;i<sites.length();i++){
				JSONArray m = sites.getJSONObject(i).getJSONArray("fe");
				for(int j = 0;j<m.length();j++){
					allMovies.put(m.getJSONObject(j).getString("dc"), m.getJSONObject(j).getString("fn"));
				}
			}
			
			
			for (String key : allMovies.keySet()) {
				driver.get("http://www.cinemacity.hu/featureInfo?featureCode="+key);
				Event filmEvent = collectFilmData(driver);
				
				List<Show> showList = collectShows(key,sites);
				filmEvent.getShowList().addAll(showList);
				ret.add(filmEvent);
				
			}
			
			
		} catch (IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return ret;
	}
	
	
	 private static String readAll(Reader rd) throws IOException {
		    StringBuilder sb = new StringBuilder();
		    int cp;
		    while ((cp = rd.read()) != -1) {
		      sb.append((char) cp);
		    }
		    return sb.toString();
		  }

		  public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		    InputStream is = new URL(url).openStream();
		    try {
		      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
		      String jsonText = readAll(rd);
		      JSONObject json = new JSONObject(jsonText);
		      return json;
		    } finally {
		      is.close();
		    }
		  }


	/**
	 * Collect data from the website to create a {@link FilmEvent} object
	 * 
	 * @param driver
	 * @param webDriverWait
	 * @return
	 */
	private Event collectFilmData(WebDriver driver) {
		
		String title = 				driver.findElement(By.xpath("html/body/div/div[1]")).getText();
		String originalTitle = 		driver.findElement(By.xpath("html/body/div/div[3]/div[1]/div[1]/div[2]")).getText();
		String genre = 				driver.findElement(By.xpath("html/body/div/div[3]/div[1]/div[2]/div[2]")).getText();
		String lengthInMinutes = 	driver.findElement(By.xpath("html/body/div/div[3]/div[1]/div[3]/div[2]")).getText();
		String releasedOn = 		driver.findElement(By.xpath("html/body/div/div[3]/div[1]/div[5]/div[2]")).getText();
		String rating = 			driver.findElement(By.xpath("html/body/div/div[3]/div[1]/div[6]/div[2]")).getText();
		String director = 			driver.findElement(By.xpath("html/body/div/div[3]/div[1]/div[7]/div[2]")).getText();
		String actors = 			driver.findElement(By.xpath("html/body/div/div[3]/div[1]/div[8]/div[2]/p")).getText();
		String countryYear = 		driver.findElement(By.xpath("html/body/div/div[3]/div[1]/div[9]/div[2]")).getText();
		String description = 		
				driver.findElement(By.xpath("html/body/div/div[3]/div[2]/p")).getText();
		
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

	
	private List<Show> collectShows(String movieID, JSONArray sites){
		List<Show> ret = new LinkedList<Show>();
		
		try{
			for(int i = 0;i<sites.length();i++){
				JSONObject aSite = sites.getJSONObject(i);
				String cinemaText = aSite.getString("sn");
				JSONArray fe = aSite.getJSONArray("fe");
				
				for(int j=0;j<fe.length();j++){
					JSONObject aMovieInCinema = fe.getJSONObject(j);
					if(movieID.equals(aMovieInCinema.getString("dc"))){
						JSONArray shows = aMovieInCinema.getJSONArray("pr");
						for(int k = 0;k<shows.length();k++){
							JSONObject aShow = shows.getJSONObject(k);
							Date date = createDateObject(aShow.getString("dt"), aShow.getString("tm"));
							String categoryText;
							if(aShow.has("td")){
								categoryText = "3D ";
							}else{
								categoryText = "2D ";
							}
							if(aShow.has("db")){
								categoryText += "Szinkronizált";
							}else{
								categoryText += "Feliratos";
							}
							ret.add(new Show(date, cinemaText, categoryText));
						}
					}
				}
			}
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
