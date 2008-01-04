/**
 *  RepDev - RepGen IDE for Symitar
 *  Copyright (C) 2007  Jake Poznanski, Ryan Schultz, Sean Delaney
 *  http://repdev.org/ <support@repdev.org>
 *
 *  This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.repdev;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
			
			return RepDevMain.SYMITAR_SESSIONS.get(file.getSym()).fileExists(file);
		}
		
		return false;		
	}
	
	
}
