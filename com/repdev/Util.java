package com.repdev;

import java.util.Date;


/**
 * Small symitar related utility methods, all static
 * @author poznanja
 *
 */

//TODO: Write these methods!
public class Util {
	
	/**
	 * Parse Symitar Dates
	 * Date, ex= 05222006
	 * Time, ex = 42 = 12:42 AM
	 *      ex 2312 = 11:12PM
	 * @param date
	 * @param time
	 * @return
	 */
	public static Date parseDate( String date, String time){
		return null;
	}
	
	public static Date parseDate( String date){
		return parseDate( date, "0" );
	}
	
	/**
	 * Return nice byte size, ex. 1.24KB for symitar files
	 * @param size
	 * @return
	 */
	public static String getByteStr(int size){
		return "" + size;
	}
	
}
