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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public enum SessionError {
	NONE, SERVER_NOT_FOUND, AIX_LOGIN_WRONG, SYM_INVALID, USERID_INVALID, ALREADY_CONNECTED, NOT_CONNECTED, IO_ERROR, CONSOLE_BLOCKED,
	INVALID_FILE_TYPE, INVALID_QUEUE, INPUT_ERROR, FILENAME_TOO_LONG, ARGUMENT_ERROR, NULL_POINTER;
	
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