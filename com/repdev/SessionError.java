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
	NONE, SERVER_NOT_FOUND, AIX_LOGIN_WRONG, SYM_INVALID, USERID_INVALID, USERID_PASSWORD_CHANGE, ALREADY_CONNECTED, NOT_CONNECTED, IO_ERROR, CONSOLE_BLOCKED,
	INVALID_FILE_TYPE, INVALID_QUEUE, INPUT_ERROR, FILENAME_TOO_LONG, ARGUMENT_ERROR, NULL_POINTER, PLINK_NOT_FOUND, NOT_WINDOWSLEVEL_3;
	
	public void showError(){
		MessageBox dialog = new MessageBox(new Shell(),SWT.OK | SWT.ICON_ERROR);
		dialog.setText("Error in host connection");
		dialog.setMessage("Error connecting to server, check network connections");
		
		switch(this){
			case AIX_LOGIN_WRONG:
				dialog.setMessage("AIX Login information is incorrect!");
				break;
			case ALREADY_CONNECTED:
				dialog.setMessage("Symitar Session is already connected.");
				break;
			case ARGUMENT_ERROR:
				dialog.setMessage("Invalid File Argument!");
				break;
			case CONSOLE_BLOCKED:
				dialog.setMessage("This console has been blocked!");
				break;
			case FILENAME_TOO_LONG:
				dialog.setMessage("Filename is too long!");
				break;
			case INPUT_ERROR:
				dialog.setMessage("Input Error was detected.");
				break;
			case INVALID_FILE_TYPE:
				dialog.setMessage("Invalid File Type.");
				break;
			case INVALID_QUEUE:
				dialog.setMessage("Invalid Queue.");
				break;
			case IO_ERROR:
				dialog.setMessage("I/O Error.");
				break;
			case NONE:
				dialog.setMessage("No Session Error");
				break;
			case NOT_CONNECTED:
				dialog.setMessage("Symitar Session is not connected!");
				break;
			case NOT_WINDOWSLEVEL_3:
				dialog.setMessage("WINDOWSLEVEL not set to 3.");
				break;
			case NULL_POINTER:
				dialog.setMessage("Null Pointer.");
				break;
			case PLINK_NOT_FOUND:
				dialog.setMessage("Plink.exe was not found in the startup directory.");
				break;
			case SERVER_NOT_FOUND:
				dialog.setMessage("Server not found, please check network connections");
				break;
			case SYM_INVALID:
				dialog.setMessage("Specified SYM is invalid.");
				break;
			case USERID_INVALID:
				dialog.setMessage("Invalid User ID/Password");
				break;
			case USERID_PASSWORD_CHANGE:
				dialog.setMessage("User Password change required.");
				break;
			default:
				dialog.setMessage("Undefined Session Error!");
				break;
		}
		
		dialog.open();
	}
}; 