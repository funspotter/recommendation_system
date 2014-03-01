package com.incredibles.morrisons;

public class MorrisonsOperaDownloader extends AbstractMorrisonsDownloader {

	protected static final String OPERA_PAGE_URL = "http://www.morrisons.hu/opera/hetibontas.php";
	protected static final String OPERA_PREFIX_NAME = "Morrison's Opera - ";
	protected static final String OPERA_LOCATION = "1065 Budapest, Révay utca 25.";
	
	public MorrisonsOperaDownloader() {
		super(OPERA_PAGE_URL, OPERA_PREFIX_NAME, OPERA_LOCATION);
	}
}
