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
	INVALID_FILE_TYPE, INVALID_QUEUE, INPUT_ERROR, IP_NOT_ALLOWED, FILENAME_TOO_LONG, ARGUMENT_ERROR, NULL_POINTER, PLINK_NOT_FOUND, NOT_WINDOWSLEVEL_3,
	INCOMPATIBLE_REVISION, UNDEFINED_ERROR, AIX_PASSWORD_TO_EXPIRE, SSH_KEY_CHANGED;
	
	public void showError(){
		MessageBox dialog = new MessageBox(new Shell(),SWT.OK | SWT.ICON_ERROR);
		dialog.setText("Error in host connection");
		dialog.setMessage("Error connecting to server, check network connections");
		dialog.setMessage(getErrorString());
		dialog.open();
	}

	public String getErrorString(){
		String msg = "";
		
		switch(this){
		case AIX_LOGIN_WRONG:
			msg = "AIX Login information is incorrect!";
			break;
		case AIX_PASSWORD_TO_EXPIRE:
			msg = "AIX password due to expire.";
			break;
		case ALREADY_CONNECTED:
			msg = "Symitar Session is already connected.";
			break;
		case ARGUMENT_ERROR:
			msg = "Invalid File Argument!";
			break;
		case CONSOLE_BLOCKED:
			msg = "This console has been blocked!";
			break;
		case FILENAME_TOO_LONG:
			msg = "Filename is too long!";
			break;
		case INPUT_ERROR:
			msg = "Input Error was detected.";
			break;
		case INVALID_FILE_TYPE:
			msg = "Invalid File Type.";
			break;
		case INVALID_QUEUE:
			msg = "Invalid Queue.";
			break;
		case IO_ERROR:
			msg = "I/O Error.";
			break;
		case IP_NOT_ALLOWED:
			msg = "Logins not allowed from host.";
			break;
		case NONE:
			msg = "No Session Error";
			break;
		case NOT_CONNECTED:
			msg = "Symitar Session is not connected!";
			break;
		case NOT_WINDOWSLEVEL_3:
			msg = "WINDOWSLEVEL not set to 3.";
			break;
		case NULL_POINTER:
			msg = "Null Pointer.";
			break;
		case PLINK_NOT_FOUND:
			msg = "Plink.exe was not found in the startup directory.";
			break;
		case SERVER_NOT_FOUND:
			msg = "Server not found, please check network connections";
			break;
		case SYM_INVALID:
			msg = "Specified SYM is invalid.";
			break;
		case USERID_INVALID:
			msg = "Invalid User ID/Password";
			break;
		case USERID_PASSWORD_CHANGE:
			msg = "User Password change required.";
			break;
		case INCOMPATIBLE_REVISION:
			msg = "Incompatible Revison";
			break;
		case SSH_KEY_CHANGED:
			msg = "The SSH Key on the host has changed.  Please ensure this is a valid change, then delete the key from the Registry and try again;\n\nHKEY_CURRENT_USER\\Software\\SimonTatham\\PuTTY\\SshHostKeys\\rsa2@22:YOURHOSTNAME";
			break;
		case UNDEFINED_ERROR:
			msg = "Undefined Error";
			break;
		default:
			msg = "Undefined Session Error!";
			break;
		}
		
		return msg;
	}
}; 