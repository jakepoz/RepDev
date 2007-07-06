package com.repdev;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	public static ArrayList<SymitarFile> getFileList(String dir, String search) {
		ArrayList<SymitarFile> data = new ArrayList<SymitarFile>();

		File file = new File(dir);
		File[] fileListData;

		search = search.trim();

		if (search.equals(""))
			search = "+";

		fileListData = file.listFiles(new SymitarWildcardFilter(search));

		if (fileListData != null)
			for (File current : fileListData) {
				if( !current.isDirectory())
					data.add(new SymitarFile(dir,current.getName(),new Date(current.lastModified()), current.length()));
			}

		return data;
	}
	
	/**
	 * Used to find out if a file by the given name exists yet.
	 * Pass it a symitar file object you made, and it looks in the right places
	 * 
	 * Returns false if sym is not open
	 * @param file
	 * @return
	 */
	public static boolean fileExists(SymitarFile file){
		if( file.isLocal() ){
			for( SymitarFile cur : getFileList(file.getDir(), file.getName() )){
				if( cur.getName().toLowerCase().equals(file.getName().toLowerCase()) )
					return true;
			}
		}
		else{
			if( RepDevMain.SYMITAR_SESSIONS.get(file.getSym()) == null || !RepDevMain.SYMITAR_SESSIONS.get(file.getSym()).isConnected() )
				return false;
			
			return RepDevMain.SYMITAR_SESSIONS.get(file.getSym()).getFile(file)!=null;
		}
		
		return false;		
	}
}
