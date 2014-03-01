package com.incredibles.kritikustomeg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.incredibles.Config;
import com.incredibles.DownloadException;
import com.incredibles.Downloader;
import com.incredibles.data.Event;
import com.incredibles.data.FilmEvent;
import com.incredibles.data.Show;

public class KritikustomegDownloader implements Downloader {
	
	private final int page;
	
	/**
	 * Wich page of the movies should be downloaded<br> 0 means all, 1 means the first page and so on
	 * 
	 * @param page
	 */
	public KritikustomegDownloader(int page) {
		this.page = page;
	}
	
	private static final boolean SLEEP_ENABLED = true;
	

	@Override
	public List<Event> downloadEvents(WebDriver driver) throws DownloadException {
		// TODO Auto-generated method stub
		List<Event> ret = new LinkedList<Event>();
		
		HashMap<Integer,String> m = new HashMap<Integer,String>();
		m.put(3,"akcio");
		m.put(368, "animacio");
		m.put(95, "dokumentumfilm");
		m.put(32, "drama");
		m.put(674,"fantasy");
		m.put(62, "horror");
		m.put(954, "katasztrofafilm");
		m.put(212, "krimi");
		m.put(29, "scifi");
		m.put(55, "romantikus");
		m.put(56,"thriller");
		m.put(54,"vigjatek");
		
		Iterator it = m.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        int id = Integer.parseInt(pairs.getKey().toString());
	        String type = pairs.getValue().toString();


	        for (int oldal = 1;oldal<21;oldal++){
				driver.get("http://kritikustomeg.org/ksz.php?kszid="+Integer.toString(id)+"&r=7&oldal="+Integer.toString(oldal));
				WebDriverWait webDriverWait = new WebDriverWait(driver, Config.EXPLICIT_WAIT_TIMEOUT);
				
				// iterate over the films
				List<WebElement> filmElementList = driver.findElements(By.xpath("html/body/table/tbody/tr/td[2]/form/table/tbody/tr"));

				
				
				for (int i = 0;i<filmElementList.size();i++){
					filmElementList = driver.findElements(By.xpath("html/body/table/tbody/tr/td[2]/form/table/tbody/tr"));
					System.out.println(String.format(">> number of films %d", filmElementList.size()));

					if(i>=filmElementList.size()){
						break;
					}
					
					WebElement filmElement = filmElementList.get(i);
					//html/body/table/tbody/tr/td[2]/form/table/tbody/tr[1]/td[1]/a
					WebElement filmElementAnchor = filmElement.findElement(By.xpath("td[1]/a"));
					filmElementAnchor.click();
					
					//html/body/table/tbody/tr/td[2]/div[1]/table/tbody/tr/td[2]/span[3]
					try {
						if (SLEEP_ENABLED) {
							// without this it does not work, I dont know why
							Thread.sleep(1000);
						}

						KritikustomegFilm filmEvent = collectFilmData(driver, webDriverWait);
						
						try {
							writeToFile(filmEvent,type);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						driver.navigate().back();
						

					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (TimeoutException e) {
						e.printStackTrace();
						System.err.println("I caught that...");
					}
				
				}
			}
		
	        
	       
	        it.remove(); // avoids a ConcurrentModificationException
	    }
			
		return null;
	}
	
	
	private KritikustomegFilm collectFilmData(WebDriver driver, WebDriverWait webDriverWait) {
		String title = null;
		try{
			title = driver.findElement(By.xpath("html/body/table/tbody/tr/td[2]/div[1]/table/tbody/tr/td[2]/span[3]")).getText();
		}catch(NoSuchElementException e){
			e.printStackTrace();
		}
		String originalTitle = null;
		try{
			originalTitle = driver.findElement(By.xpath("html/body/table/tbody/tr/td[2]/div[1]/table/tbody/tr/td[2]/span[1]")).getText();

		}catch(NoSuchElementException e){
			e.printStackTrace();
		}
		String description = null;
		try{
			description = driver.findElement(By.xpath("html/body/table/tbody/tr/td[2]/table/tbody/tr/td[1]/table/tbody/tr[1]/td/p/table/tbody/tr")).getText();

		}catch(NoSuchElementException e){
			e.printStackTrace();
		}
		
		KritikustomegFilm ret = new KritikustomegFilm(title,originalTitle,description);
		
		System.out.println(title+originalTitle+description);
		
		return ret;
	}
	private void writeToFile(KritikustomegFilm film,String category) throws IOException{
		File parentDir = new File("/Users/huszarcsaba/Desktop/dk/"+category);
		parentDir.mkdir(); 
    	
        String fileName = film.originalTitle + ".txt";
        File file = new File(parentDir, fileName);
        file.createNewFile(); // Creates file crawl_html/abc.txt

        BufferedWriter oute = new BufferedWriter(new FileWriter(file));
        
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));

        
        if(film.description!=null){
        	 out.write(film.description);
        }
        out.close();

        
	}

}
