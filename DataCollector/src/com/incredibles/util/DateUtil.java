package com.incredibles.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {

	public static int getMonthFromString(String monthString) {
		int month = 0;
		
		if (monthString.equalsIgnoreCase("janu�r")) {
			month = 0;
		} else if (monthString.equalsIgnoreCase("febru�r")) {
			month = 1;
		} else if (monthString.equalsIgnoreCase("m�rcius")) {
			month = 2;
		} else if (monthString.equalsIgnoreCase("�prilis")) {
			month = 3;
		} else if (monthString.equalsIgnoreCase("m�jus")) {
			month = 4;
		} else if (monthString.equalsIgnoreCase("j�nius")) {
			month = 5;
		} else if (monthString.equalsIgnoreCase("j�lius")) {
			month = 6;
		} else if (monthString.equalsIgnoreCase("augusztus")) {
			month = 7;
		} else if (monthString.equalsIgnoreCase("szeptember")) {
			month = 8;
		} else if (monthString.equalsIgnoreCase("okt�ber")) {
			month = 9;
		} else if (monthString.equalsIgnoreCase("november")) {
			month = 10;
		} else if (monthString.equalsIgnoreCase("december")) {
			month = 11;
		}
		
		return month;
	}

	public static Date createDate(int year, int month, int day, int hour, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

}
