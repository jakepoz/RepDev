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

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;

/**
 * Abstract symitar session, allows for several different conneciton methods
 * @author Jake Poznanski
 *
 */
public abstract class SymitarSession {
	protected String server, aixUsername, aixPassword, userID ,userNum;
	protected int sym, port;
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
	public abstract SessionError connect(String server, int port, String aixUsername, String aixPassword, int sym, String userID);
	
	/**
	 * After a failed login, using connect, to the SYM you can retry multiple times.
	 *
	 * @param userID
	 * @return SessionError
	 */
	public abstract SessionError loginUser(String userID);
	
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

	public abstract boolean fileExists(SymitarFile file);

	public abstract SessionError removeFile(SymitarFile file);

	public abstract SessionError saveFile(SymitarFile file, String text);

	public abstract SessionError renameFile(SymitarFile file, String newName);

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

	/**
	 * Helper method for runRepGen stuff
	 * @param progress
	 * @param value
	 * @param text
	 * @param str
	 */
	protected void setProgress(ProgressBar progress, int value, Text text, String str){
		if( progress != null && !progress.isDisposed() )
			progress.setSelection(value);

		if( text != null && str != null && !text.isDisposed())
			text.setText(str.replace("\r", "\n"));
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

	public class RunFMResult{
		private String resultTitle;
		private int seq;

		public RunFMResult(){
			setRandomTitle();
		}

		public RunFMResult(String resultTitle, int seq) {
			super();
			this.resultTitle = resultTitle;
			this.seq = seq;
		}

		private void setRandomTitle(){
			resultTitle = "RepDev FM - " + String.format("%06d", (int)(Math.random() * 10000000));
		}

		public String getResultTitle() {
			return resultTitle;
		}

		public void setResultTitle(String resultTitle) {
			this.resultTitle = resultTitle;
		}

		public int getSeq() {
			return seq;
		}

		public void setSeq(int seq) {
			this.seq = seq;
		}		
	}

	public enum FMFile{
		ACCOUNT("Account"),
		INVENTORY("Inventory"),
		PAYEE("Payee"),
		GL_ACCOUNT("GL_Account"),
		RECIEVED_ITEM("Recieved_Item"),
		PARTICIPANT("Partipant"),
		PARTICIPATION("Participation"),
		DEALER("Dealer"),
		USER("User"),
		COLLATERAL("Collateral");

		private String displayName;

		private FMFile(String displayName){
			this.displayName = displayName;
		}

		public String getDisplayName()
		{
			return displayName;
		}
	}

	public abstract RunFMResult runBatchFM(String searchTitle, int searchDays, FMFile file, int queue);

	public abstract ArrayList<PrintItem> getPrintItems( String query, int limit );
	public abstract ArrayList<PrintItem> getPrintItems( Sequence seq );

	/**
	 * Goes through past several batch output files in print control
	 * If certain time is given, just returns that one, 
	 * if it's -1, then finds last couple
	 * 
	 * The time specifier is important for the following reason:
	 * If two instance of a samed named repgen are being run at the same time (usually long reports),
	 * we only want to pick the one that we started ourselves. This option is currently
	 * only used by the Run Report feature.
	 * 
	 * @param reportName
	 * @param time
	 * @param limit
	 * @return
	 */

	//TODO: Do not take a time, but a regular Date object
	//TODO: Only works if the reportName is the only one in it
	public ArrayList<Sequence> getReportSeqs( String reportName, int time,
			int search, int limit){
		ArrayList<PrintItem> items = getPrintItems("REPWRITER", search);
		ArrayList<Sequence> newItems = new ArrayList<Sequence>();

		if( items == null )
			return newItems;
		
		//More than likely, if we are looking for anything, it will be the newest one first
		Collections.reverse(items);

		for( PrintItem cur : items){
			String file = new SymitarFile(sym,"" +
					cur.getSeq(),FileType.REPORT).getData();

			if( file.indexOf("Processing begun on") < 0 )
				continue;

			file = file.substring(file.indexOf("Processing begun on") + 41);
			String timeStr = file.substring(0,8);
			int curTime;

			curTime = Integer.parseInt(timeStr.substring(timeStr.lastIndexOf(":")+1));
			curTime += 60 *
			Integer.parseInt(timeStr.substring(timeStr.indexOf(":")+1,
					timeStr.lastIndexOf(":")));
			curTime += 3600 *
			Integer.parseInt(timeStr.substring(0,timeStr.indexOf(":")));

			//Advance to the right place
			file =  file.substring(file.indexOf("(newline when done):") + 21);

			String name = file.substring(0,file.indexOf("\n"));

			if( (time == -1 || curTime - 1 == time || curTime == time ||
					curTime +1 == time ) && name.equals(reportName) ){
				newItems.add(new Sequence(sym,cur.getBatchSeq(), cur.getDate()));

				//If we have matched it, then we are done
				if( time != -1 || newItems.size() >= limit )
					break;
			}
		}

		return newItems;
	}

	/**
	 * Very similair to getReportSeqs, but instead, gets the batch sequence numbers for a given FM job,
	 * since we can index things based on a specific report title, we use that as the key for finding what we want
	 * 
	 * @param reportName
	 * @param search
	 * @param limit
	 * @return
	 */
	public ArrayList<Sequence> getFMSeqs( String reportName, int search, int limit){
		ArrayList<PrintItem> items = getPrintItems("MISCFMPOST", search);
		ArrayList<Sequence> newItems = new ArrayList<Sequence>();

		//More than likely, if we are looking for anything, it will be the newest one first
		Collections.reverse(items);

		for( PrintItem cur : items){
			String file = new SymitarFile(sym,"" + cur.getSeq(),FileType.REPORT).getData();

			//Advance to the right place
			file =  file.substring(file.indexOf("Name of Posting: ") + 17);

			String name = file.substring(0,file.indexOf("\n")).trim();

			if( name.equals(reportName) ){
				newItems.add(new Sequence(sym,cur.getBatchSeq(), cur.getDate()));

				//If we have matched it, then we are done
				if( newItems.size() >= limit )
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

	public abstract SessionError printFileLPT(SymitarFile file, int queue, boolean formsOverride, int formLength, int startPage, int endPage, int copies, boolean landscape, boolean duplex, int queuePriority);

	/**
	 * Calls the regular print command with default options
	 */
	public SessionError printFileLPT(SymitarFile file, int queue) {
		return printFileLPT(file, queue, false, 0, 0, 0, 1, false, false, 4);
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

	public void setServer(String server) {
		this.server = server;
	}

	public int getPort() {
		return port;
	}

	public String getUserID() {
		return userID;
	}
	
	public String getUserNum(){
		return getUserNum(false);
	}

	public String getUserNum(Boolean leadingZero) {
		if(userID.indexOf(".")!=-1) {
			userNum = userID.substring(0,userID.indexOf("."));
		} else {
			userNum = userID.substring(0,3);
		}
		
		if(!leadingZero){
			while (userNum.charAt(0) == '0' && userNum.length() > 1) {
				// System.out.println("userNum- " + userNum);
				userNum=userNum.substring(1,userNum.length());
			}
		}
		return userNum;
	}

	public abstract ErrorCheckResult installRepgen(String f);

}
