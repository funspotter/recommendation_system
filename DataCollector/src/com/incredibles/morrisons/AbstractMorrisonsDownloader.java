package com.incredibles.morrisons;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.incredibles.LinkCollectorDownloader;
import com.incredibles.data.Event;
import com.incredibles.data.Show;
import com.incredibles.data.SimpleEvent;

abstract class AbstractMorrisonsDownloader extends LinkCollectorDownloader {

	protected String prefixName;
	protected String location;
	
	public AbstractMorrisonsDownloader(String pageUrl, String prefixName, String location) {
		super(pageUrl, "//div[@id='esemeny']/a", Integer.MAX_VALUE);
		this.prefixName = prefixName;
		this.location = location;
	}

	@Override
	protected Event downloadEventFromDetails(WebDriver driver, String eventLink) {
		driver.get(eventLink);
		List<WebElement> contRightChildren = driver.findElements(By.xpath("//div[@id='cont_right']/*"));
		
		WebElement partyHeaderElement = contRightChildren.get(0);
		List<WebElement> descriptionParagraphList = driver.findElements(By.xpath("//div[@id='cont_right']/p"));
		WebElement posterElement = driver.findElement(By.xpath("//div[@id='cont_left']/img"));
		WebElement dateElement = contRightChildren.get(1);
		
		WebElement contRightElement = driver.findElement(By.xpath("//div[@id='cont_right']"));
		
		
		String partyHeader = partyHeaderElement.getText();
		StringBuilder descriptionBuilder = new StringBuilder();
		for (WebElement descriptionParagraph : descriptionParagraphList) {
			descriptionBuilder.append(descriptionParagraph.getText());
		}
		
//		String dateStr = dateElement.getText();
		String[] contRightParts = contRightElement.getText().trim().split("\n");
		String dateStr = contRightParts[1].trim();
		Date startDate = parseDate(dateStr);
		
		SimpleEvent ret = new SimpleEvent();
		ret.setName(prefixName + partyHeader);
		ret.setDescription(descriptionBuilder.toString());
		ret.setImage(posterElement.getAttribute("src"));
		ret.setManyShows(false);
		ret.setDiscriminator("Party");
		Show show = new Show();
		show.setStart(startDate);
		show.setLocation(location);
		ret.getShowList().add(show);
		return ret;
	}
	
	private Date parseDate(String dateStr) {
		String monthStr = dateStr.substring(0, 2);
		String dayStr = dateStr.substring(3, 5);
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, Integer.parseInt(monthStr) - 1);
		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayStr));
		cal.set(Calendar.HOUR_OF_DAY, 17);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTime();
	}

}
