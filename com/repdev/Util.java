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
	public static String getByteStr(long size){
		if( size < 1024)
			return "" + size + " byte" + (size == 1 ? "" : "s");
		else if ( size < 1024 * 1024 )
			return "" + new DecimalFormat("##0.00").format(size / 1024.) + " kB";
		else
			return "" + new DecimalFormat("##0.00").format(size / (1024. * 1024.)) + " MB";
	}
	
}
