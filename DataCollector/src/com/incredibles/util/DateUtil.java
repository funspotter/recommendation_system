package com.incredibles.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {

	public static int getMonthFromString(String monthString) {
		int month = 0;
		
		if (monthString.equalsIgnoreCase("január")) {
			month = 0;
		} else if (monthString.equalsIgnoreCase("február")) {
			month = 1;
		} else if (monthString.equalsIgnoreCase("március")) {
			month = 2;
		} else if (monthString.equalsIgnoreCase("április")) {
			month = 3;
		} else if (monthString.equalsIgnoreCase("május")) {
			month = 4;
		} else if (monthString.equalsIgnoreCase("június")) {
			month = 5;
		} else if (monthString.equalsIgnoreCase("július")) {
			month = 6;
		} else if (monthString.equalsIgnoreCase("augusztus")) {
			month = 7;
		} else if (monthString.equalsIgnoreCase("szeptember")) {
			month = 8;
		} else if (monthString.equalsIgnoreCase("október")) {
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
