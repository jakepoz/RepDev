package com.repdev;

import java.util.ArrayList;

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

	public abstract void runRepGen(String name);

	public abstract void runRepGen(String name, int queue);

	/**
	 * Returns an arraylist of QueueInfo objects
	 * 
	 * @return
	 */
	public ArrayList<QueueInfo> getQueueInfo() {
		return queueInfoList;
	}

	/**
	 * Blocks until a change is available from the session, telnet or otherwise
	 * For example, if we had asked for a repgen to be run, entered its prompts,
	 * then we would call this method and it would block.
	 * 
	 * Then, when new data was available, ex. the repgen finished, or the queue
	 * listing got updated. It would unblock. The caller would then have to
	 * determine what changed, (ex. update the UI) and if required, call this
	 * method again
	 * 
	 * Current Changes Possible: 1. Repgen Completion 2. Repgen Prompt 3. Queue
	 * Info Chane
	 * 
	 * @return
	 */
	public abstract void waitOnChange();

	/**
	 * Returns a query that the user must respond to in this repgen, null if
	 * there are no more queries
	 * 
	 * @return
	 */
	public abstract String getRepGenQuery();

	/**
	 * Sends an answer to the next waiting query
	 * 
	 * @param value
	 * @return
	 */
	public abstract int acceptRepGenQuery(String value);

	/**
	 * 
	 * @return Queue number of currently running repgen
	 */
	public abstract int getRepgenQueue();

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
