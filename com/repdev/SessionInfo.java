package com.repdev;

import java.io.Serializable;

public class SessionInfo implements Serializable {
	/**
	 * 
	 */
	protected String description, server, aixUsername, aixPassword, userID;
	private static final long serialVersionUID = 3L;
	public SessionInfo() {
	}

	public SessionInfo(String description, String server, String aixUsername, String aixPassword, String userID) {
		this.description = description;
		this.server = server;
		this.aixUsername = aixUsername;
		this.aixPassword = aixPassword;
		this.userID = userID;
	}

	public SessionInfo(String description, SymitarSession session) {
		this.description = description;
		this.server = session.server;
		this.aixUsername = session.aixUsername;
		this.aixPassword = session.aixPassword;
		this.userID = session.userID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getAixUserName() {
		return aixUsername;
	}

	public void setAixUserName(String aixUsername) {
		this.aixUsername = aixUsername;
	}

	public String getAixPassword() {
		return aixPassword;
	}

	public void setAixPassword(String aixPassword) {
		this.aixPassword = aixPassword;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

}
