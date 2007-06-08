package com.repdev;

import java.util.ArrayList;

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

	public abstract String runRepGen(String name, int queue, ProgressBar progress, Text text, PromptListener prompter);

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


	/**
	 * Sequence numbers for the last batch process to run, not including BATCH
	 * OUTPUT file
	 * 
	 * @return
	 */
	public abstract ArrayList<Integer> getSequenceNumbers();

	/**
	 * Batch ouput sequence number for the last batch process to run
	 * 
	 * @return
	 */
	public abstract int getBatchOutputSequenceNumber();

	public abstract SessionError runBatchFM(String title);

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
