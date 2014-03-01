package com.incredibles;

public class Log {

	public static void myPrintln(String output) {
		if (Config.LOG_ENABLED) {
			System.out.println(output);
		}
		
	}

}
