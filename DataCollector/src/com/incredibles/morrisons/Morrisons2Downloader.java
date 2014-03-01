package com.incredibles.morrisons;

public class Morrisons2Downloader extends AbstractMorrisonsDownloader {

	protected static final String MORI2_PAGE_URL = "http://www.morrisons.hu/morrisons2/hetibontas.php";
	protected static final String MORI2_PREFIX_NAME = "Morrison's 2 - ";
	protected static final String MORI2_LOCATION = "1055 Budapest, Szent István krt. 11.";
	
	public Morrisons2Downloader() {
		super(MORI2_PAGE_URL, MORI2_PREFIX_NAME, MORI2_LOCATION);
	}

}
