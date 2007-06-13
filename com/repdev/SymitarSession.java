package com.repdev;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;

import com.repdev.SymitarSession.PromptListener;

/**
 * Abstract symitar session, allows for several different conneciton methods
 * @author Jake Poznanski
 *
 */
public abstract class SymitarSession {
	protected String server, aixUsername, aixPassword, userID;
	protected int sym;
	protected ArrayList<QueueInfo> queueInfoList;

	/**
	 * Initiates a connection to the server, if we are not already connected
	 * 
	 * @param server
	 * @param aixUsername
	 * @param aixPassword
	 * @param sym
	 * @param userID
	 * @return SessionError
	 */
	public abstract SessionError connect(String server, String aixUsername, String aixPassword, int sym, String userID);

	/**
	 * 
	 * @return SessionError
	 */
	public abstract SessionError disconnect();

	/**
	 * 
	 * @return boolean
	 */
	public abstract boolean isConnected();

	/**
	 * Gets file of given type from server
	 * 
	 * @param type
	 * @param name
	 * @return String containing contents, null if not connected or if can't
	 *         find file
	 */
	public abstract String getFile(SymitarFile file);

	public abstract SessionError removeFile(SymitarFile file);

	/**
	 * 
	 * @param type
	 * @param name
	 * @param text
	 * @return SessionError
	 */
	public abstract SessionError saveFile(SymitarFile file, String text);

	public class RunRepgenResult{
		int seq, time;

		public RunRepgenResult(int seq, int time) {
			super();
			this.seq = seq;
			this.time = time;
		}

		public int getSeq() {
			return seq;
		}

		public void setSeq(int seq) {
			this.seq = seq;
		}

		public int getTime() {
			return time;
		}

		public void setTime(int time) {
			this.time = time;
		}
		
		
	}
	
	public abstract RunRepgenResult runRepGen(String name, int queue, ProgressBar progress, Text text, PromptListener prompter);

	/**
	 * Interface needed for runRepGen stuff, should maybe be it's own file later
	 */
	public interface PromptListener{
		String getPrompt(String name);
	}
	/**
	 * Returns an arraylist of QueueInfo objects
	 * 
	 * @return
	 */
	public ArrayList<QueueInfo> getQueueInfo() {
		return queueInfoList;
	}


	public abstract boolean isSeqRunning(int seq);
	
	public abstract void terminateRepgen(int seq);

	public abstract SessionError runBatchFM(String title);

	public abstract ArrayList<PrintItem> getPrintItems( String query, int limit );
	public abstract ArrayList<PrintItem> getPrintItems( int seq );
	
	/**
	 * Goes through past several batch output files in print control
	 * If certain time is given, just returns that one, 
	 * if it's -1, then finds last couple
	 * @param reportName
	 * @param time
	 * @return
	 */
	public ArrayList<Integer> getReportSeqs( String reportName, int time ){
		ArrayList<PrintItem> items = getPrintItems("REPWRITER", 10);
		ArrayList<Integer> newItems = new ArrayList<Integer>();
		
		//More than likely, if we are looking for anything, it will be the newest one first
		Collections.reverse(items);
		
		for( PrintItem cur : items){
			String file = getFile(new SymitarFile("" + cur.getSeq(),FileType.REPORT));
			
			file = file.substring(file.indexOf("Processing begun on") + 41);
			String timeStr = file.substring(0,8);
			int curTime;
			
			curTime = Integer.parseInt(timeStr.substring(timeStr.lastIndexOf(":")+1));
			curTime += 60 * Integer.parseInt(timeStr.substring(timeStr.indexOf(":")+1, timeStr.lastIndexOf(":")));
			curTime += 3600 * Integer.parseInt(timeStr.substring(0,timeStr.indexOf(":")));
			
			//Advance to the right place
			file =  file.substring(file.indexOf("(newline when done):") + 21);
			
			String name = file.substring(0,file.indexOf("\n"));
			
			if( (time == -1 || curTime - 1 == time || curTime == time || curTime +1 == time ) && name.equals(reportName) ){
				newItems.add(cur.getBatchSeq());
				
				//If we have matched it, then we are done
				if( time != -1 )
					break;
			}
		}
		
		return newItems;
	}
	
	/**
	 * Supports default "+" as a wildcard
	 * 
	 * @param search
	 * @return
	 */
	public abstract ArrayList<SymitarFile> getFileList(FileType type, String search);

	public abstract SessionError printFileLPT(SymitarFile file, boolean formsOverride, int formLength, int startPage, int endPage, int copies, boolean landscape, boolean duplex, int queuePriority);

	/**
	 * Calls the regular print command with default options
	 */
	public SessionError printFileLPT(SymitarFile file) {
		return printFileLPT(file, false, 0, 0, 0, 1, false, false, 4);
	}

	public abstract SessionError printFileTPT(SymitarFile file, int queue);

	public abstract ErrorCheckResult errorCheckRepGen(String filename);

	public String getAixUsername() {
		return aixUsername;
	}

	public String getAixPassword() {
		return aixPassword;
	}

	public int getSym() {
		return sym;
	}

	public String getServer() {
		return server;
	}

	public String getUserID() {
		return userID;
	}

	public abstract ErrorCheckResult installRepgen(String f);
}
