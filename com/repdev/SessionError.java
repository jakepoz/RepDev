package com.repdev;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public enum SessionError {
	NONE, SERVER_NOT_FOUND, AIX_LOGIN_WRONG, SYM_INVALID, USERID_INVALID, ALREADY_CONNECTED, NOT_CONNECTED, IO_ERROR, CONSOLE_BLOCKED,
	INVALID_FILE_TYPE, INVALID_QUEUE, INPUT_ERROR, FILENAME_TOO_LONG, ARGUMENT_ERROR;
	
	public void showError(){
		MessageBox dialog = new MessageBox(new Shell(),SWT.OK | SWT.ICON_ERROR);
		dialog.setText("Error in host connection");
		dialog.setMessage("Error connecting to server, check network connections");
		
	
		if( this == SessionError.CONSOLE_BLOCKED)
			dialog.setMessage("This console has been blocked");
		else if( this == SessionError.SERVER_NOT_FOUND)
			dialog.setMessage("Server not found, please check network connections");
		else if( this == SessionError.USERID_INVALID)
			dialog.setMessage("Invalid User ID");
		else if( this == SessionError.AIX_LOGIN_WRONG)
			dialog.setMessage("AIX Login information is incorrect");
		else if( this == SessionError.FILENAME_TOO_LONG)
			dialog.setMessage("Filename is too long!");
		
		dialog.open();
	}
};