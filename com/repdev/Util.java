package com.repdev;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
	public static Date parseDate( String dateStr, String time){	
	
		try {
			if( time == null || time.trim().equals("") ){
				return new SimpleDateFormat("MMddyyyy").parse(dateStr);
			}
			else{
				DecimalFormat formatter = new DecimalFormat("0000");
				time = formatter.format(Integer.parseInt(time));
				
				dateStr += time;
				
				return new SimpleDateFormat("MMddyyyyHHmm").parse(dateStr);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static Date parseDate( String date){
		return parseDate( date, "" );
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
