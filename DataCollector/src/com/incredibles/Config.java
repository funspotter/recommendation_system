package com.incredibles;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

//import com.incredibles.programturizmus.ProgramTurizmusDownloader;

/**
 * Class to hold the configuration constants
 * 
 * @author Andris
 *
 */
public class Config {

	public static final boolean DEBUG = false;
	
	/** whether currently debugging {@link ProgramTurizmusDownloader} or not */
//	public static final boolean DEBUG_PT = true;
	public static final boolean DEBUG_PT = false;
	
	/** implicit timeout for the {@link WebDriver} actions, when no other value is specified */ 
//	public static final int IMPLICIT_WAIT_TIMEOUT = 10;
	public static final int IMPLICIT_WAIT_TIMEOUT = 5;
	/** explicit timeout for the {@link WebDriverWait} calls */
	public static final int EXPLICIT_WAIT_TIMEOUT = 60;
	/** whether the log is enabled or not */
	public static final boolean LOG_ENABLED = true;
	/** name of the file to which the events should be written out and read from */
	public static final String SERIALIZATION_FILENAME = "downloaded_events"; 
}
