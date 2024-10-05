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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;

/**
 * This is the main connection object to the Symitar host, it provides all the routines you would need to connect
 * 
 * Each action leaves no side effects for any other one in general, but the run repgen mode is a bit special, as it as an interface 
 * that it uses to get prompts which could technically use a backwards form of recursion to mess everything up.
 * 
 * Provides classes and methods also to help in reading commands from the server, parsing them, creating new ones, etc.
 * 
 * @author Jake Poznanski
 *
 */
public class DirectSymitarSession extends SymitarSession {
	Socket socket;
	BufferedReader in;
	PrintWriter out;
	BufferedReader err;
	boolean connected = false;
	boolean loggedInAIX = false;
	boolean useSSH = false;
	Process p;
	int actualSym = -1;
	int consoleNum = -1;
	String hostIPA = "";
	String symRev = "" ;
	GregorianCalendar symDate = new GregorianCalendar();
	/**
	 * lastActivity can be updated every time you deem is an activity.  This will be used in the keepAlive
	 * Thread within DirectSymitarSession to terminate Keep Alive if there has not been any activity
	 * within a specific number of minutes.
	 */
	private static Calendar lastActivity = new GregorianCalendar();
	
	boolean bFullTrace = false;
	boolean bSensitiveData = false;
	FileOutputStream trace;
	Thread keepAlive;
	boolean keepAliveEnabled = false;
	boolean keepAliveActive = false;
	boolean keepAliveNevrTerm = false;
	int keepAliveHour = 19;
	int keepAliveMin = 00;
	private final int NO_ACTIVITY_DELAY = 20;
	private final int KEEP_ALIVE_WARNING = 30;

	private String log(String str) {
		System.out.println(str);
		return str;
	}
	
	private String log(Object o){
		return log( o.toString() );
	}
	
	/**
	 * Updates the lastActivity variable in RepDevMain so that the Keep Alive Thread will not terminate
	 * if there has been activity, from any of the SYMs, within the minutes specified by NO_ACTIVITY_DELAY.
	 */
	public static void setLastActivity(){
		lastActivity = new GregorianCalendar();
	}

	/**
	 * Returns the lastActivity timestamp
	 * @return cal
	 */
	public static Calendar getLastActivity(){
		return lastActivity;
	}
	
	boolean keepAliveEnabled(){
		return keepAliveEnabled;
	}
	
	boolean keepAliveActive(){
		return keepAliveActive;
	}
	
	void enableKeepAlive(boolean neverTerminate, int termHr, int termMin){
		keepAliveEnabled = true;
		keepAliveNevrTerm = neverTerminate;
		keepAliveHour = termHr;
		keepAliveMin = termMin;
	}
	
	int getTerminateHour(){
		return keepAliveHour;
	}
	
	int getTerminateMinute(){
		return keepAliveMin;
	}
	
	boolean getNeverTerminate(){
		return keepAliveNevrTerm;
	}
	
	@Override
	public SessionError connect(String server, int port, String aixUsername, String aixPassword, int sym, String userID) {
		File traceFile = new File("fulltrace." + Integer.toString(sym) + ".txt");
		if(traceFile.exists()){
			bFullTrace = true;
			traceFile.delete();
			try {
				traceFile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			try {
				trace = new FileOutputStream(traceFile);
				System.out.println("opening fulltrace file...");
				traceLog("Starting the trace for Host:" + server + " Port:" + Integer.toString(port) + " SYM:" + Integer.toString(sym)+ " AIX:" + aixUsername);
				traceLog("   ***   MAKE SURE TO DELETE THIS FILE WHEN DONE, OR IT MAY SLOW DOWN YOUR CONNECTION ! ! !   ***\n\n");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		String line = "";
		setLastActivity();
		
		if( connected )
			return SessionError.ALREADY_CONNECTED;
		
		this.sym = sym;
		this.server = server;
		this.port = port;
		this.aixUsername = aixUsername;
		this.aixPassword = aixPassword;
		//final int tmpSym = this.sym;

		if(port == 22){
	    	useSSH = true;
		System.out.println("*** Using SSH ***\nIf this is the last message you see, you may need to cache your SSH Key.");
		System.out.println("For help, see the Wiki Page at https://github.com/jakepoz/RepDev/wiki/Using-SSH\n");
	    }
		try {
			if(useSSH){
				String command = "plink -load aixterm " + server;
				p = Runtime.getRuntime().exec(command);
				
				in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				out = new PrintWriter(p.getOutputStream());
				err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				
				pause(700); // This delay is to give time for the Error Stream to capture the data.
				if(cacheSSHKey() == SessionError.SSH_KEY_CHANGED) {
					return SessionError.SSH_KEY_CHANGED;
				}
			} else {
				socket = new Socket(server, port);
				socket.setKeepAlive(true);
			
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream());
				
				// Constant commands, these are the basic telnet establishment
				// stuffs, which really don't change, so I just send them directly
				char init1[] = { 0xff, 0xfb, 0x18 };
				char init2[] = { 0xff, 0xfa, 0x18, 0x00, 0x61, 0x69, 0x78, 0x74, 0x65, 0x72, 0x6d, 0xff, 0xf0 };
				char init3[] = { 0xff, 0xfd, 0x01 };
				char init4[] = { 0xff, 0xfd, 0x03, 0xff, 0xfc, 0x1f, 0xff, 0xfc, 0x01 };
				
				out.print(init1);
				out.print(init2);
				out.print(init3);
				out.print(init4);
				traceLog(init1.toString());
				traceLog(init2.toString());
				traceLog(init3.toString());
				traceLog(init4.toString());
			}

			traceLog(aixUsername);
			
			if(useSSH){
				out.print(aixUsername + "\r\n");
			} else {
				out.print(aixUsername + "\r");
			}
			out.flush();
			String temp = readUntil("Password:", "password:", "[c");
		
			if( temp.indexOf("[c") == -1 ){
				bSensitiveData = true;
				if(useSSH){
					line = writeLog(aixPassword + "\r\n", "[c", "password:", "Password:");
				} else {
					line = writeLog(aixPassword + "\r", "[c", "invalid login name or password");
				}
				bSensitiveData = false;
	
				if (line.indexOf("invalid login") != -1 || line.indexOf("password:") != -1 || line.indexOf("Password:") != -1){
					disconnect();
					return SessionError.AIX_LOGIN_WRONG;
				} else if (line.contains("$ ")) {
					System.out.print(line);
					System.out.print("It appears we weren't able to bypass text mode.\nYou may have a slow connection.\nOr this console is not setup as a 'Windows PC' in SYMOP.");
					disconnect();
					return SessionError.NOT_WINDOWSLEVEL_3;
				} else if (line.indexOf("[c") == -1) {
					System.out.print(line);
					System.out.print("Unsure what happened here.  Check logs!");
					disconnect();
					return SessionError.IO_ERROR;
				}
			}

			write("WINDOWSLEVEL=3\n");
			
			temp = readUntil("$ ", "SymStart~Global", "Selection :", "no longer supported!","Logins not allowed from host: ","Your password will expire:","invalid login name or password");
			System.out.println(temp);
			if (temp.contains("no longer supported!")) {
				disconnect();
				System.out.println(temp);
				return SessionError.NOT_WINDOWSLEVEL_3;
			} else if (temp.contains("Logins not allowed")) {
				System.out.print("You cannot log in from this IP. Verify this PC is setup to use Symitar!");
				disconnect();
				return SessionError.IP_NOT_ALLOWED;
			} else if (temp.contains("Your password will expire:")) {
				System.out.print("Your AIX password is due to expire.  Please Change it now.");
				disconnect();
				return SessionError.AIX_PASSWORD_TO_EXPIRE;
			} else if (temp.contains("invalid login name or password")) {
				System.out.print("Invalid AIX Password was entered");
				disconnect();
				return SessionError.AIX_LOGIN_WRONG;
			} else if (temp.contains("Selection :")) { // This is for EASE Menu
				System.out.println("EASE Menu has been detected");
				int EASE_Selection = EaseSelection.getEASESelection(temp, sym);
				System.out.println("EASE Selection = "+EASE_Selection+"\n");
				if(EASE_Selection == -1)
				{
					disconnect();
					System.out.println("EASE Selection was not found for sym " + sym + "\n");
					return SessionError.SYM_INVALID;
				} else {
					System.out.println("Sending EASE Selection: " + EASE_Selection);
					write(EASE_Selection+"\r");
				}
			}else if( temp.contains("$ ") ){
				write("sym " + sym + "\r");
			}
			
			Command current;
			
			// Checks to see if the specified SYM is valid.
			try{
				while (!(current = readNextCommand()).getCommand().equals("Input") || current.getParameters().get("HelpCode").equals("10025")){
					log(current);
					if(current.getCommand().equals("Input") && current.getParameters().get("HelpCode").equals("10025")){
						write("$WinHostSync$\r");
					}
					// Get the actual SYM logged into.
					if(current.getCommand().equals("SymLogonDir")){
						actualSym = Integer.parseInt(current.getParameters().get("Dir").trim());
						hostIPA = current.getParameters().get("Host").trim();
					}
					// Get the Symitar Release Level.
					if(current.getCommand().equals("SymLogonRev")){
						symRev = current.getParameters().get("HostRev").trim();
					}
					// Checks to see if the Console is already locked out.
					if( current.getCommand().equals("SymLogonError")){
						if(current.getParameters().get("Text").contains("Too Many Invalid Password Attempts")){
							disconnect();
							return SessionError.CONSOLE_BLOCKED;
						} else if(current.getParameters().get("Text").contains("Revision Incompatibility")){
							disconnect();
							return SessionError.INCOMPATIBLE_REVISION;
						} else {
							disconnect();
							return SessionError.UNDEFINED_ERROR;
						}
					}
				}
	
				log(current.toString());
			} catch (IOException e) {
				disconnect();
				if(e.toString().indexOf("SYM not Found") != -1){
					System.out.println("SYM not Found");
					return SessionError.SYM_INVALID;
				} else{
					return SessionError.IO_ERROR;
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
			disconnect();
			return SessionError.SERVER_NOT_FOUND;
		} catch (IOException e) {
			e.printStackTrace();
			disconnect();
			if(useSSH){
				return SessionError.PLINK_NOT_FOUND;
			} else {
				return SessionError.IO_ERROR;
			}
		}
		
		loggedInAIX = true;
		return loginUser(userID);
	}
	
	@Override
	public SessionError loginUser(String userID){
		// First check to see if we are logged into AIX, using the connect method.
		if (loggedInAIX == false){
			return SessionError.NOT_CONNECTED;
		}
		
		this.userID = userID;
		// Attempt to log into the SYM and start the session.
		try{
			final int tmpSym = this.sym;
			bSensitiveData = true;
			if(useSSH){
				write(userID + " \r");
			} else {
				write(userID + "\r");
			}
			bSensitiveData = false;
			
			Command current;
			current = readNextCommand();
			// Checks for Password Expiration warning.
			if(current.getCommand().equals("MsgDlg")){
				log("USER RESPONSE: " + current.getParameters().get("Text"));
				write("\r0\r");
				current = readNextCommand();
			}
			log("USER RESPONSE: " + current.getCommand());
			// Checking for bad Teller ID/Password.
			if (current.getCommand().equals("SymLogonInvalidUser")){
				System.out.println("Bad User Password");
				write("\r");
				while (!(current = readNextCommand()).getCommand().equals("Input")){
					log(current);
				}
				return SessionError.USERID_INVALID;
			// Check for frozen console.
			}else if(current.getCommand().equals("SymLogonFrozen")){
				System.out.println("Console Frozen");
				disconnect();
				return SessionError.CONSOLE_BLOCKED;
			// Check for Password Change.
			}else if(current.getCommand().equals("SymLogonChangePassword")){
				System.out.println("Change Password Required");
				disconnect();
				return SessionError.USERID_PASSWORD_CHANGE;
			}
			
			write("\r");
			readNextCommand();
			
			write("\r");
			log(readNextCommand().toString());
			
			// Get SYM Date
			{
				// [0x07]39[0x0d]Misc~InfoType=BankingDate~MsgId=xxxxxxx
				Command cmdGetDate=new Command();
				cmdGetDate.setCommand("Misc");
				cmdGetDate.getParameters().put("InfoType","BankingDate");
				write(cmdGetDate.sendStr());
				String symdate = readNextCommand().getParameters().get("BankingDate");
				symDate.setTimeInMillis(0);
				symDate.set(Integer.parseInt(symdate.substring(4,8)), Integer.parseInt(symdate.substring(0,2))-1, Integer.parseInt(symdate.substring(2,4)));
			}
			
			// Get Console Number
			{
				// [0x07]41[0x0d]Misc~InfoType=ConsoleNumber~MsgId=xxxxxxx
				Command cmdGetCon=new Command();
				cmdGetCon.setCommand("Misc");
				cmdGetCon.getParameters().put("InfoType","ConsoleNumber");
				write(cmdGetCon.sendStr());
				consoleNum = Integer.parseInt(readNextCommand().getParameters().get("ConsoleNumber"));
			}
			
			connected = true;
			log("Connected to Symitar!");
			
			// Keep alive needs to be enabled with enableKeepAlive(); Otherwise the session will be terminated
			//  per the console setting in Symitar.
			if(keepAliveEnabled){
				//Establish keepalive timer, every 55 seconds send an empty command
				keepAlive = new Thread(new Runnable(){
					public void run() {
						Calendar cal = new GregorianCalendar();
						int terminateTime;
						int lastActivityTime = (getLastActivity().get(Calendar.HOUR_OF_DAY)*60)+getLastActivity().get(Calendar.MINUTE)+NO_ACTIVITY_DELAY;
						int termOptionTime = (getTerminateHour()*60)+getTerminateMinute();
						int curTime = (cal.get(Calendar.HOUR_OF_DAY)*60)+cal.get(Calendar.MINUTE);
						keepAliveActive = true;
						boolean neverTerminate;
						
						terminateTime = (lastActivityTime > termOptionTime ? lastActivityTime : termOptionTime);
						neverTerminate = getNeverTerminate();
						boolean firstRun = true;
						try{
							while(terminateTime > curTime || neverTerminate){
								firstRun = false;
								Thread.sleep(55000);
								wakeUp();
								
								// Get the current time and convert it to minutes.
								cal = Calendar.getInstance();
								curTime = (cal.get(Calendar.HOUR_OF_DAY)*60)+cal.get(Calendar.MINUTE);
								// Get the last activity time and convert it to minutes and add a delay.
								lastActivityTime = (getLastActivity().get(Calendar.HOUR_OF_DAY)*60)+getLastActivity().get(Calendar.MINUTE)+NO_ACTIVITY_DELAY;
								// Set terminateTime to what ever is larger
								terminateTime = (lastActivityTime > termOptionTime ? lastActivityTime : termOptionTime);
								// If terminateTime is within the Warning period, log a Warning.
								if (terminateTime-curTime < KEEP_ALIVE_WARNING && !neverTerminate){
									log(cal.getTime().toString().substring(11, 19)+" Keep Alive (SYM "+tmpSym+") will terminate in "+(terminateTime-curTime)+" minutes");
								}
								else{
									log(cal.getTime().toString().substring(11, 19)+" Keep Alive (SYM "+tmpSym+") ");
								}
							}
							
							log(cal.getTime().toString().substring(11, 19)+" Keep Alive (SYM "+tmpSym+") Terminated");
							// TODO: If you are running DirectSymitarSession outside of RepDev, you may want to
							//  comment this part out to eliminate the GUI, or not enable KeepAlive.
							if (!firstRun){
								Display.getDefault().syncExec(new Runnable(){
									public void run(){
										keepAliveActive = false;
										MessageBox msg = new MessageBox(RepDevMain.mainShell.getShell(), SWT.ICON_WARNING);
										msg.setText("Keep Alive");
										msg.setMessage("Keep Alive has terminated for SYM "+tmpSym+".  Please take proper measures to avoid the lost of work.");
										msg.open();
									}
								});
							}
						}
						catch(InterruptedException e){
							System.out.println("Terminating keepalive thread");
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
					
				});
				
				keepAlive.start();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
			disconnect();
			return SessionError.SERVER_NOT_FOUND;
		} catch (IOException e) {
			e.printStackTrace();
			disconnect();
			return SessionError.IO_ERROR;
		}

		return SessionError.NONE;
	}
	
	private static class Command {
		String command = "";
		HashMap<String, String> parameters = new HashMap<String, String>();
		String data = "";
		static Pattern commandPattern = Pattern.compile("(.*?)~.*");
		static int currentMessageId = 10000;

		public Command(String command) {
			parameters.put("MsgId", String.valueOf(currentMessageId));
			currentMessageId++;

			this.command = command;
		}

		public Command() {
			parameters.put("MsgId", String.valueOf(currentMessageId));
			currentMessageId++;
		}

		// Returns string containing any file data sent in this message
		public String getFileData() {
			
			if( data.indexOf(Character.toString((char) 253)) != -1 && data.indexOf(Character.toString((char) 254)) != -1)
				return data.substring(data.indexOf(Character.toString((char) 253)) + 1, data.indexOf(Character.toString((char) 254)));
			else
				return "";
		}

		public String sendStr() {
			String data = "";
			data += command + "~";

			for (String key : parameters.keySet())
				if (parameters.get(key).equals(""))
					data += key + "~";
				else
					data += key + "=" + parameters.get(key) + "~";

			data = data.substring(0, data.length() - 1);

			return Character.toString((char) 0x07) + data.length() + "\r" + data;
		}

		public static Command parse(String data) {
			String[] sep;

			Command command = new Command();
			command.setData(data);

			if (data.indexOf("~") != -1 && data.indexOf(253) == -1) {
				Matcher match;
				match = commandPattern.matcher(data);
				match.matches();

				command.setCommand(match.group(1));
				sep = data.substring(match.group(1).length() + 1).split("~");

				for (String cur : sep) {
					if (cur.indexOf("=") == -1)
						command.getParameters().put(cur, "");
					else
						command.getParameters().put(cur.substring(0, cur.indexOf("=")), cur.substring(cur.indexOf("=") + 1));
				}
			} else
				command.setCommand(data);

			return command;
		}

		public String toString() {
			return data;
		}

		public String getCommand() {
			return command;
		}

		public void setCommand(String command) {
			this.command = command;
		}

		public void setData(String data) {
			this.data = data;
		}

		public HashMap<String, String> getParameters() {
			return parameters;
		}
	}

	private void write(String str) {
		traceLog(str);
		
		out.write(str);
		out.flush();
	}

	private void write(Command cmd) {
		write(cmd.sendStr());
	}

	private String writeLog(String command, String... waitFor) throws IOException {
		write(command);
		return log(readUntil(waitFor));
	}

	private Command readNextCommand() throws IOException {
		String tmpData=readUntil(Character.toString((char) 0x1b) + Character.toString((char) 0xfe),"No such file or directory");
		// Not sure if this is the best way to detect an invalid SYM, but here it is anyway.
		if(tmpData.indexOf("No such file or directory") != -1){
			throw new IOException("SYM not Found");
		}
		
		String data = readUntil(Character.toString((char) 0xfc));
		Command cmd = Command.parse(data.substring(0, data.length() - 1));
		
		//Filter out Messages that come in asychronously and muck everything up
		if( cmd.getCommand().equals("MsgDlg") && cmd.getParameters().get("Text").contains("From PID") )
			return readNextCommand();
		else
			return cmd;
	}

	private String readUntil(String... strs) throws IOException {
		String buf = "";
		if(bFullTrace){
			try {
				while (true) {
					int cur = in.read();
					trace.write((byte)cur);
					trace.flush();
					
					//System.out.print((char)cur);
					buf += (char) cur;
					for (String str : strs)
						if (buf.indexOf(str) != -1)
							return buf;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return buf;
			}
		} else{
			while (true) {
				int cur = in.read();
				//System.out.print((char)cur);
				buf += (char) cur;
				for (String str : strs)
					if (buf.indexOf(str) != -1)
						return buf;
			}
		}
	}

	@Override
	public SessionError disconnect() {
		try {
			if( keepAlive != null)
				keepAlive.interrupt();
			
			if( in != null)
				in.close();
			
			if( out != null)
				out.close();
			
			if( socket != null)
				socket.close();
			if( useSSH )
				p.destroy();
			if( trace != null){
				System.out.println("Closing Full Trace");
				trace.flush();
				trace.close();
				bFullTrace = false;
			}
		} catch (Exception e) {
			return SessionError.IO_ERROR;
		}
		connected = false;
		loggedInAIX = false;
		return SessionError.NONE;
	}

	private synchronized void wakeUp(){
		write(new Command("WakeUp"));
	}
	
	@Override
	public synchronized ErrorCheckResult errorCheckRepGen(String filename) {
		Command cur;
		String error = "", errFile = "";
		int line = -1, column = -1;

		if( !connected )
			return null;
		
		try{
			write("mm3" + (char)27); //Managment menu #3- repgen, of course!!
			
			while ( !(cur = readNextCommand()).getCommand().equals("Input"))
				log(cur);
			
			write("7\r");
			
			log(readNextCommand().toString());
			log(readNextCommand().toString());
			
			write(filename+"\r");
			
			while (!(cur = readNextCommand()).getCommand().equals("SpecfileErr") && !cur.getCommand().equals("MsgDlg"))
				log(cur.toString());
			
			if( cur.getParameters().get("Type") != null){
				return new ErrorCheckResult(filename,"File does not exist on server!",-1,-1,ErrorCheckResult.Type.ERROR);
			}

			if( cur.getParameters().get("Warning") != null || cur.getParameters().get("Error") != null){
				readNextCommand();
				return new ErrorCheckResult(filename,"File does not exist on server!",-1,-1,ErrorCheckResult.Type.ERROR);
			}
			
			if( cur.getParameters().get("Action").equals("NoError") ){
				readNextCommand();
				return new ErrorCheckResult(filename,"",-1,-1,ErrorCheckResult.Type.NO_ERROR);
			}
			
			if( cur.getParameters().get("Action").equals("Init")){
				errFile = cur.getParameters().get("FileName");
				
			  	while( !(cur=readNextCommand()).getParameters().get("Action").equals("DisplayEdit")){
			  		if( cur.getParameters().get("Action").equals("FileInfo") )
			  		{
			  			line = Integer.parseInt(cur.getParameters().get("Line").replace(",", ""));
			  			column = Integer.parseInt(cur.getParameters().get("Col").replace(",", ""));
			  		}
			  		else if( cur.getParameters().get("Action").equals("ErrText") )
			  			error += cur.getParameters().get("Line") + " ";
			  		
			  		log(cur.toString());
			  	}
			  	
			  	readNextCommand();
			  	
			  	return new ErrorCheckResult(errFile,error.trim(),line,column,ErrorCheckResult.Type.ERROR);  		  	
			}
		
		}
		catch(IOException e)
		{
			return null;
		}
		
		return null;
	}

	@Override
	public synchronized boolean fileExists(SymitarFile file){
		return getFileList(file.getType(), file.getName()).size() > 0;
	}
	
	@Override
	public synchronized String getFile(SymitarFile file) {
/*		if(RepDevMain.useSourceSafe && !file.isLocal() && file instanceof SymitarFile && file.getType()==FileType.REPGEN) {
			log("Source Control");
			SourceControl sc = new SourceControl();
			
			return sc.getFile(file);
		}else {
			log("Not Source Control");
			return getSymitarFile(file);
		}
	}

	public synchronized String getSymitarFile(SymitarFile file) {*/
		StringBuilder data = new StringBuilder();
		final long maxSize = 2097152; //Don't download more than 2MB, otherwise things get ugly
		boolean wroteSizeWarning = false;
		
		if( !connected )
			return null;
		setLastActivity();
		Command current;
		Command retrieve = new Command();
		retrieve.setCommand("File");
		retrieve.getParameters().put("Action", "Retrieve");

		if (file.getType() == FileType.REPGEN)
			retrieve.getParameters().put("Type", "RepWriter");
		else if (file.getType() == FileType.HELP)
			retrieve.getParameters().put("Type", "Help");
		else if (file.getType() == FileType.LETTER)
			retrieve.getParameters().put("Type", "Letter");
		else if( file.getType() == FileType.REPORT)
			retrieve.getParameters().put("Type", "Report");

		
		retrieve.getParameters().put("Name", file.getName());

		write(retrieve);
		
		try {
			while (true) {
				current = readNextCommand();

				if (current.getParameters().get("Status") != null && current.getParameters().get("Status").contains("No such file or directory"))
					return "";
				else if( current.getParameters().get("Status") != null )
					return null;

				if (current.getParameters().get("Done") != null){
					return data.toString();
				}

				if( data.length() < maxSize){
					data.append(current.getFileData());
					
					if( file.getType() == FileType.REPORT )
						data.append( "\n");
				}
				else if( !wroteSizeWarning ){
					data.insert(0,"WARNING - This file exceeds the 2MB limit that RepDev has for loading files. This text should only be used as a preview!\n\n");
					data.append("\n\nWARNING - This file exceeds the 2MB limit that RepDev has for loading files. This text should only be used as a preview!");
					wroteSizeWarning = true;
				}
			}
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public synchronized ArrayList<SymitarFile> getFileList(FileType type, String search) {
		ArrayList<SymitarFile> toRet = new ArrayList<SymitarFile>();
		Command current;
		setLastActivity();
		if( !connected )
			return toRet;

		Command list = new Command("File");

		if (type == FileType.REPGEN)
			list.getParameters().put("Type", "RepWriter");
		else if (type == FileType.HELP)
			list.getParameters().put("Type", "Help");
		else if (type == FileType.LETTER)
			list.getParameters().put("Type", "Letter");

		list.getParameters().put("Name", search);
		list.getParameters().put("Action", "List");

		write(list);

		while (true) {
			try {
				current = readNextCommand();
			} catch (IOException e) {
				e.printStackTrace();
				return toRet;
			}

			//log(current.toString());

			if (current.getParameters().get("Status") != null)
				break;

			if( current.getParameters().get("Name") != null &&
				current.getParameters().get("Date") != null &&
				current.getParameters().get("Time") != null &&
				current.getParameters().get("Size") != null)
				toRet.add(new SymitarFile(sym, current.getParameters().get("Name"), type, Util.parseDate(current.getParameters().get("Date"), current.getParameters().get("Time")), Integer.parseInt(current.getParameters().get("Size"))));
		
						
			if(current.getParameters().get("Done") != null)
				break;
		}

		return toRet;
	}


	@Override
	public boolean isConnected() {
		return connected;
	}

	//TODO: Add more error checking
	@Override
	public SessionError printFileLPT(SymitarFile file, int queue, boolean formsOverride, int formLength, int startPage, int endPage, int copies, boolean landscape, boolean duplex, int queuePriority) {
		Command cur;
		
		if( !connected )
			return SessionError.NOT_CONNECTED;
			
		if( !(file.getType() == FileType.REPORT))
			return SessionError.INVALID_FILE_TYPE;
		
		try {
			write("mm1" + (char)27); //Managment menu #3- repgen, of course!!
			
			while( !(cur = readNextCommand()).getCommand().equals("Input"))
				log(cur);

			write("P\r");
			
			while( !(cur = readNextCommand()).getCommand().equals("Input"))
				log(cur);
			
			write( file.getName() + "\r");
			
			while( !(cur = readNextCommand()).getCommand().equals("Input"))
				log(cur);
			
			write( "\r");
			
			//Something wierd was added recently, it waits for a specific queue prompt now.
			while( (cur = readNextCommand()) != null){
				log(cur);
				
				if( cur.getCommand().equals("Input") && cur.getParameters().get("HelpCode").equals("10008") )
					break;
			}

			
			write( queue + "\r");
			
			while( !(cur = readNextCommand()).getCommand().equals("Input")){
				log(cur);
				
				if( cur.getCommand().equals("MsgDlg") && cur.getParameters().get("Type").equals("Error") ){
					 wakeUp();
					 return SessionError.INVALID_QUEUE;
				}
			}
			
			write( "\r");
			
			
			// A New Prompt was added in release 2007.00 The New prompt is Banner Page.
			// I've defaulted it to "0" for now until RepDev Team can modify the Dialog
			// box for the LPT Print Options.
			// Bruce Chang 11/21/07
			while( !(cur = readNextCommand()).getCommand().equals("Input"))
				log(cur);
			//Banner Page?~HelpCode=10026
			
			//If we were asked for a banner page, write that out, then read the next input code before writing out the next field
			if( cur.getCommand().equals("Input") && cur.getParameters().get("HelpCode").equals("10026"))
			{
				write("0\r");
			
				while( !(cur = readNextCommand()).getCommand().equals("Input"))
					log(cur);
			}
			
			
			write( (formsOverride ? "1" : "0")+ "\r");
			
			while( !(cur = readNextCommand()).getCommand().equals("Input"))
				log(cur);
			
			if( formsOverride ){
				write( formLength + "\r");

				while( !(cur = readNextCommand()).getCommand().equals("Input"))
					log(cur);
			}
			
			write( startPage + "\r");

			while( !(cur = readNextCommand()).getCommand().equals("Input"))
				log(cur);

			write( endPage + "\r");

			while( !(cur = readNextCommand()).getCommand().equals("Input"))
				log(cur);

			write( copies + "\r");

			while( !(cur = readNextCommand()).getCommand().equals("Input"))
				log(cur);

			write( (landscape ? "1" : "0") + "\r");

			while( !(cur = readNextCommand()).getCommand().equals("Input"))
				log(cur);
			
			write( (duplex ? "1" : "0") + "\r");

			while( !(cur = readNextCommand()).getCommand().equals("Input"))
				log(cur);
			
			write("4\r");

			while( !(cur = readNextCommand()).getCommand().equals("Input")){
				log(cur);
				
				if( cur.getCommand().equals("MsgDlg") && cur.getParameters().get("Type").equals("Error") ){
					 wakeUp();
					 return SessionError.INPUT_ERROR;
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return SessionError.IO_ERROR;
		}
		
		return SessionError.NONE;
	}

	@Override
	public SessionError printFileTPT(SymitarFile file, int queue) {
		
		return null;
	}

	@Override
	public SessionError removeFile(SymitarFile file) {
		Command delete = new Command();
		delete.setCommand("File");
		delete.getParameters().put("Action", "Delete");

		if (file.getType() == FileType.REPGEN)
			delete.getParameters().put("Type", "RepWriter");
		else if (file.getType() == FileType.HELP)
			delete.getParameters().put("Type", "Help");
		else if (file.getType() == FileType.LETTER)
			delete.getParameters().put("Type", "Letter");
		else if (file.getType() == FileType.REPORT)
			delete.getParameters().put("Type", "Report");

		delete.getParameters().put("Name", file.getName());

		write(delete);

		Command current = null;

		try {
			current = readNextCommand();

			if (current.getParameters().get("Status") != null && current.getParameters().get("Status").contains("No such file or directory"))
				return SessionError.ARGUMENT_ERROR;
			else if (current.getParameters().get("Status") != null)
				return SessionError.FILENAME_TOO_LONG;
			else if (current.getParameters().get("Done") != null)
				return SessionError.NONE;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return SessionError.IO_ERROR;
	}

	@Override
	public synchronized RunFMResult runBatchFM(String searchTitle, int searchDays, FMFile file, int queue) {
		RunFMResult result = new RunFMResult();
		int[] queueCounts = new int[10000];
		boolean[] queueAvailable = new boolean[10000];
		
		for( int i = 0; i < queueCounts.length; i++)
			queueCounts[i] = -1;
		
		Command cur;
		try {
			write("mm0" + (char)27);
			while( !(cur = readNextCommand()).getCommand().equals("Input") )
				log(cur);
			
			write("1\r");
			while( !(cur = readNextCommand()).getCommand().equals("Input") )
				log(cur);
			
			write("24\r");
			while( !(cur = readNextCommand()).getCommand().equals("Input") )
				log(cur);
			
			// menu option for batch fm
			write("5\r");
			while( !(cur = readNextCommand()).getCommand().equals("Input") )
				log(cur);
			
			write(file.ordinal() + "\r");
			while( !(cur = readNextCommand()).getCommand().equals("Input") )
				log(cur);
			
			write("0\r");
			while( !(cur = readNextCommand()).getCommand().equals("Input") )
				log(cur);
			
			write(searchTitle + "\r");
			while( !(cur = readNextCommand()).getCommand().equals("Input") )
				log(cur);
			
			write( searchDays + "\r");
			while( !(cur = readNextCommand()).getCommand().equals("Input") )
				log(cur);
			
			if( file == FMFile.ACCOUNT ){
				write("1\r");
				while( !(cur = readNextCommand()).getCommand().equals("Input") )
					log(cur);
			}
			
			write(result.getResultTitle() + "\r");
			while( !(cur = readNextCommand()).getCommand().equals("Input") )
				log(cur);
			
			write("1\r");
			while( !(cur = readNextCommand()).getCommand().equals("Input") )
				log(cur);
			
			write("0\r");
			while( !(cur = readNextCommand()).getCommand().equals("Input") ){
				log(cur);
				if( cur.getParameters().get("Action").equals("DisplayLine") && cur.getParameters().get("Text").contains("Batch Queues Available:")){
					String line = cur.getParameters().get("Text");				
					String[] tempQueues = line.substring(line.indexOf(":") + 1).split(",");
					
					for( String temp : tempQueues){
						temp = temp.trim();
						
						if( temp.contains("-"))
						{
							String[] tempList = temp.split("-");
							
							int start = Integer.parseInt(tempList[0].trim());
							int end = Integer.parseInt(tempList[1].trim());
							
							for( int x = start; x <= end; x++){
								queueAvailable[x]=true;
							}
						}
						else
						{
							queueAvailable[Integer.parseInt(temp)] = true;
						}
					}
				}
			}
//			Batch queue selection
			Command getQueues = new Command("Misc");
			getQueues.getParameters().put("InfoType", "BatchQueues");
			write(getQueues);
	
			while( (cur = readNextCommand()).getParameters().get("Done") == null ){
				log(cur);
				
				if( cur.getParameters().get("Action").equals("QueueEntry") && cur.getParameters().get("Stat").equals("Running"))
					if(queueCounts[Integer.parseInt(cur.getParameters().get("Queue"))] < 0)
						queueCounts[Integer.parseInt(cur.getParameters().get("Queue"))] = 1;
					else
						queueCounts[Integer.parseInt(cur.getParameters().get("Queue"))]++;
				else if( cur.getParameters().get("Action").equals("QueueEmpty"))
					queueCounts[Integer.parseInt(cur.getParameters().get("Queue"))] = 0;
			}
			int lastGood = -1;
			
			if( (queue != -1 && !queueAvailable[queue]) || queue == -1 )
			{
				for( queue = 0; queue < queueCounts.length; queue++)
				{
					if( queueAvailable[queue])
						lastGood = queue;

					if( queueAvailable[queue] && queueCounts[queue] == 0)
						break;
				}
				
				queue = lastGood;
			}
			
			write( queue + "\r");
			while( !(cur = readNextCommand()).getCommand().equals("Input") )
				log(cur);
			
			write("1\r");
			while( !(cur = readNextCommand()).getCommand().equals("Input") )
				log(cur);
			
			write(getQueues);
			
			int newestTime = 0;
			
			while( (cur = readNextCommand()).getParameters().get("Done") == null ){
				log(cur);
				
				//Get the Sequence for the latest running one at this point, and return it so we can keep track of it
				if( cur.getParameters().get("Action").equals("QueueEntry") ){
					int curTime = 0;
					String timeStr = cur.getParameters().get("Time");
					curTime = Integer.parseInt(timeStr.substring(timeStr.lastIndexOf(":")+1));
					curTime += 60 * Integer.parseInt(timeStr.substring(timeStr.indexOf(":")+1, timeStr.lastIndexOf(":")));
					curTime += 3600 * Integer.parseInt(timeStr.substring(0,timeStr.indexOf(":")));
					
					if( curTime > newestTime )
					{
						newestTime = curTime;
						result.setSeq(Integer.parseInt(cur.getParameters().get("Seq")));
						
					}
				}
			}
			
		} catch (IOException e) {
			System.err.println("ERROR: " + e.getMessage());
		}
		
		
		return result;
	}
	
	
	/**
	 * Queue, -1 for first available,
	 * any other number for a specific one
	 * 
	 * Progressbar is useful for updating GUI stuff
	 * 
	 * The listener object is the best way to ask for prompts while still blocking from other threads.
	 * 
	 * 
	 * This allows for amodal dialogs for running, and also the option to cancel
	 * Non polling would be nice, but maybe tricky to implement, I will look into it.
	 */
	public synchronized RunRepgenResult runRepGen(String name, int queue, ProgressBar progress, Text text, PromptListener prompter) {
		Command cur, old;
		boolean isError = false;
		int[] queueCounts = new int[10000];
		boolean[] queueAvailable = new boolean[10000];
		
		//We cannot use queueCounts as an availbility thing, though it would be nice
		//The two arrays are parsed seperately, queueCounts from the list of queus and wahts in them
		//queueAvailable is from a seperate request saying which ones can actually run repwriters
		
		int seq = -1, time = 0;
		setLastActivity();
		for( int i = 0; i < queueCounts.length; i++)
			queueCounts[i] = -1;
		
		setProgress(progress,0, text, "Queuing batch run, please wait...");
		
		try{
			write("mm0" + (char)27);
			
			while( !(cur = readNextCommand()).getCommand().equals("Input") )
				log(cur);
			
			setProgress(progress,5, null, null);
			
			write("1\r");
			
			while( !(cur = readNextCommand()).getCommand().equals("Input") )
				log(cur);
			
			setProgress(progress,10, null, null);
			
			write("11\r");
			
			while( !(cur = readNextCommand()).getCommand().equals("Input") )
				log(cur);
			
			setProgress(progress,15, text, "Please answer prompts");
			
			write( name + "\r");
			
			isError = false;
			while( (cur = readNextCommand()) != null ){
							
				log(cur);
				
				if( cur.getCommand().equals("Input") && cur.getParameters().get("HelpCode").equals("20301"))
					break;
				else if( cur.getCommand().equals("Input") ){
					String result = prompter.getPrompt(cur.getParameters().get("Prompt"));
					
					if( result == null ){
						write( Character.toString((char)0x1b));
						
						while( !(cur = readNextCommand()).getCommand().equals("Input") )
							log(cur);
						
						return new RunRepgenResult(-1,0);
					}
					else {
						setProgress(null, 0, text, text.getText().replace("\r", "") + "\n" + cur.getParameters().get("Prompt") + ": " + result.trim() + "\n");
						text.setSelection(text.getText().length()-1);
						write( result.trim() + "\r");
					}
				}
				else if( cur.getCommand().equals("Bell") ){
					setProgress(progress, 15, text, "That prompt input is invalid, please reenter");
				}
				else if( cur.getCommand().equals("Batch") && cur.getParameters().get("Text").contains("No such file or directory")){
					old = cur;
					
					while( !(cur = readNextCommand()).getCommand().equals("Input") )
						log(cur);
					
					setProgress(progress,100, text, "Error: No such file or directory");					
					
					return new RunRepgenResult(-1,0);
				}
				else if( cur.getCommand().equals("SpecfileErr"))
					isError = true;
				else if (isError && cur.getCommand().equals("Batch") && cur.getParameters().get("Action").equals("DisplayLine")){
					old = cur;
					
					while( !(cur = readNextCommand()).getCommand().equals("Input") )
						log(cur);
					
					setProgress(progress,100, text, "There was an error in your program,\n that is preventing it from running:\n\n" + old.getParameters().get("Text"));	
					
					return new RunRepgenResult(-1,0);
				}
				else if( cur.getCommand().equals("Batch") && cur.getParameters().get("Action").equals("DisplayLine")){
					setProgress(null, 0, text, text.getText().replace("\r", "") + "\n" + cur.getParameters().get("Text"));

				}
			}
			
			write( "\r" );
			
			while( !(cur = readNextCommand()).getCommand().equals("Input") )
				log(cur);
			
			setProgress(progress,20, null, null);	
			
			write( "0\r");
			
			while( !(cur = readNextCommand()).getCommand().equals("Input") ){
				log(cur);
				if( cur.getParameters().get("Action").equals("DisplayLine") && cur.getParameters().get("Text").contains("Batch Queues Available:")){
					String line = cur.getParameters().get("Text");				
					String[] tempQueues = line.substring(line.indexOf(":") + 1).split(",");
					
					for( String temp : tempQueues){
						temp = temp.trim();
						
						if( temp.contains("-"))
						{
							String[] tempList = temp.split("-");
							
							int start = Integer.parseInt(tempList[0].trim());
							int end = Integer.parseInt(tempList[1].trim());
							
							for( int x = start; x <= end; x++){
								queueAvailable[x]=true;
							}
						}
						else
						{
							queueAvailable[Integer.parseInt(temp)] = true;
						}
					}
				}
			}
			
			setProgress(progress,25, null, null);	
				
			//Batch queue selection
			Command getQueues = new Command("Misc");
			getQueues.getParameters().put("InfoType", "BatchQueues");
			write(getQueues);
	
			while( (cur = readNextCommand()).getParameters().get("Done") == null ){
				log(cur);
				
				if( cur.getParameters().get("Action").equals("QueueEntry") && cur.getParameters().get("Stat").equals("Running"))
					if(queueCounts[Integer.parseInt(cur.getParameters().get("Queue"))] < 0)
						queueCounts[Integer.parseInt(cur.getParameters().get("Queue"))] = 1;
					else
						queueCounts[Integer.parseInt(cur.getParameters().get("Queue"))]++;
				else if( cur.getParameters().get("Action").equals("QueueEmpty"))
					queueCounts[Integer.parseInt(cur.getParameters().get("Queue"))] = 0;
			}
			int lastGood = -1;
			
			if( (queue != -1 && !queueAvailable[queue]) || queue == -1 )
			{
				for( queue = 0; queue < queueCounts.length; queue++)
				{
					if( queueAvailable[queue])
						lastGood = queue;

					if( queueAvailable[queue] && queueCounts[queue] == 0)
						break;
				}
				
				queue = lastGood;
			}
			
			write( queue + "\r");

			while( !(cur = readNextCommand()).getCommand().equals("Input") )
				log(cur);
			
			setProgress(progress,30, null, null);	
			
			write( "1\r");
			
			while( !(cur = readNextCommand()).getCommand().equals("Input") )
				log(cur);
			
			write(getQueues);
			
			int newestTime = 0;
			
			while( (cur = readNextCommand()).getParameters().get("Done") == null ){
				log(cur);
				
				//Get the Sequence for the latest running one at this point, and return it so we can keep track of it
				if(cur.getParameters().get("Action").equals("QueueEntry") &&
				   !cur.getParameters().get("Stat").equals("Scheduled")  ){
					int curTime = 0;
					String timeStr = cur.getParameters().get("Time");
					curTime = Integer.parseInt(timeStr.substring(timeStr.lastIndexOf(":")+1));
					curTime += 60 * Integer.parseInt(timeStr.substring(timeStr.indexOf(":")+1, timeStr.lastIndexOf(":")));
					curTime += 3600 * Integer.parseInt(timeStr.substring(0,timeStr.indexOf(":")));
					
					if( curTime > newestTime )
					{
						newestTime = curTime;
						seq = Integer.parseInt(cur.getParameters().get("Seq"));
						time = curTime;
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			
			return new RunRepgenResult(-1,0);
		}
		
		setProgress(progress,50, text, "Repgen queued\nWaiting for batch job to finish");	
		
		return new RunRepgenResult(seq,time);
	}


	@Override
	public synchronized SessionError saveFile(SymitarFile file, String text) {
		int partSize = 3996;
		int curPart = 0;
		DecimalFormat f3 = new DecimalFormat("000");
		DecimalFormat f5 = new DecimalFormat("00000");
		char[] buf = new char[16];
		String pad20 = "";
		
		setLastActivity();
		if (!connected)
			return SessionError.NOT_CONNECTED;
		
		if( file == null || text == null)
			return SessionError.ARGUMENT_ERROR;

		log("Saving file: " + file);
		
		for (int i = 0; i < 6; i++)
			pad20 += Character.toString((char) 0x20);

		Command store = new Command();
		store.setCommand("File");
		store.getParameters().put("Action", "Store");

		Command unpause = new Command();
		unpause.setCommand("WakeUp");

		Command current;

		if (file.getType() == FileType.REPGEN)
			store.getParameters().put("Type", "RepWriter");
		else if (file.getType() == FileType.HELP)
			store.getParameters().put("Type", "Help");
		else if (file.getType() == FileType.LETTER)
			store.getParameters().put("Type", "Letter");

		store.getParameters().put("Name", file.getName());

		wakeUp();
		write(store);

		try {
			current = readNextCommand();
		
			if( current == null ){
				System.out.println("Returned null for the save file command, ack!! trying to restore");
				
				wakeUp();
				write(store);
				
				current = readNextCommand();
			}
			
			int breakcount = 0;
			while(current.toString().indexOf("BadCharList") == -1){
				current = readNextCommand();
				if(breakcount++ > 5){
					return SessionError.NULL_POINTER;
				}
			}
			
			System.out.println("Save file command:\n" + current.toString()+ "\n");
			
			if( current.getParameters().get("Status") != null && current.getParameters().get("Status").contains("Filename is too long") )
				return SessionError.FILENAME_TOO_LONG;

			String[] badList = current.getParameters().get("BadCharList").split(",");

			for (String cur : badList) {
				text = text.replaceAll(Character.toString((char) Integer.parseInt(cur)), "");
			}

			String toSend = "";
			do {
				do {
					toSend = text.substring(0, Math.min(text.length(), partSize));
					write("PROT" + f3.format(curPart) + "DATA" + f5.format(toSend.length()));
					write(toSend);
					in.read(buf, 0, 16);
					if(bFullTrace) trace.write(String.valueOf(buf).getBytes());
				} while (buf[7] == 'N'); // Resend if we get a NAK message

				curPart++;
				text = text.substring(toSend.length());
			} while (text.length() > 0);

			write("PROT" + f3.format(curPart) + "EOF" + pad20);
			in.read(buf, 0, 16);
			if(bFullTrace) trace.write(String.valueOf(buf).getBytes());

			current = readNextCommand();
			write(unpause);
		} catch (IOException e) {
			return SessionError.IO_ERROR;
		}
		return SessionError.NONE;
	}

	@Override
	public synchronized ErrorCheckResult installRepgen(String filename) {
		Command cur;
		String error = "", errFile = "";
		int line = -1, column = -1;
		
		try{

			write("mm3" + (char)27); //Managment menu #3- repgen, of course!!
			
			while ( !(cur = readNextCommand()).getCommand().equals("Input"))
				log(cur);

			write("8\r");
			
			log(readNextCommand().toString());
			log(readNextCommand().toString());
			
			write(filename+"\r");
			
			cur = readNextCommand();
			log(cur.toString());
			
			if( cur.getParameters().get("Warning") != null || cur.getParameters().get("Error") != null){
				readNextCommand();
				return new ErrorCheckResult(filename,"File does not exist on server!",-1,-1,ErrorCheckResult.Type.ERROR);
			}
			
			if( cur.getCommand().equals("SpecfileData") ){
				readNextCommand();
				write("1\r");
				
				readNextCommand();
				readNextCommand();
				return new ErrorCheckResult(filename,"",Integer.parseInt(cur.getParameters().get("Size").replace(",", "")),ErrorCheckResult.Type.INSTALLED_SUCCESSFULLY);
				
			}
			
			if( cur.getParameters().get("Action").equals("Init")){
				errFile = cur.getParameters().get("FileName");
				
			  	while( !(cur=readNextCommand()).getParameters().get("Action").equals("DisplayEdit")){
			  		if( cur.getParameters().get("Action").equals("FileInfo") )
			  		{
			  			line = Integer.parseInt(cur.getParameters().get("Line").replace(",", ""));
			  			column = Integer.parseInt(cur.getParameters().get("Col").replace(",", ""));
			  		}
			  		else if( cur.getParameters().get("Action").equals("ErrText") )
			  			error += cur.getParameters().get("Line") + " ";
			  		
			  		log(cur.toString());
			  	}
			  	
			  	readNextCommand();
			  	
			  	return new ErrorCheckResult(errFile,error.trim(),line,column,ErrorCheckResult.Type.ERROR);  		  	
			}
		
		}
		catch(IOException e)
		{
			return null;
		}
		
		return null;
	}

	@Override
	/**
	 * Remeber that batch queue sequence numbers are not related to print queue ones!
	 */
	public synchronized boolean isSeqRunning(int seq) {
		Command cur;
		boolean running = false;
		
		if( !connected )
			return false;
		
		//Batch queue selection
		Command getQueues = new Command("Misc");
		getQueues.getParameters().put("InfoType", "BatchQueues");
		write(getQueues);
		
		try{
			while( (cur = readNextCommand()).getParameters().get("Done") == null ){
				log(cur);
				
				if( cur.getParameters().get("Action").equals("QueueEntry") && Integer.parseInt(cur.getParameters().get("Seq"))==seq)
					running = true;
			}
		}
		catch(IOException e){
			return false;
		}
		
		return running;
	}
	
	

	@Override
	public void terminateRepgen(int seq) {
	
	}

	@Override
	public synchronized ArrayList<PrintItem> getPrintItems(String query, int limit) {
		ArrayList<PrintItem> items = new ArrayList<PrintItem>();
		Command cur;
		
		if( !connected )
			return null;
		
		limit = Math.min(40, limit);
		
		Command getItems = new Command("File");
		getItems.getParameters().put("Action", "List");
		getItems.getParameters().put("MaxCount", "50");
		getItems.getParameters().put("Query", "LAST " + limit + " \"+" + query + "+\"");// + " \"+" + query + "+\"");
		getItems.getParameters().put("Type", "Report");
		
		write(getItems);
		
		try {
			while( (cur = readNextCommand()).getParameters().get("Done") == null ){
				log(cur);
				
				if( cur.getParameters().get("Sequence") != null ){
					try {
						Date date = Util.parseDate(cur.getParameters().get("Date"), cur.getParameters().get("Time"));
						
						items.add( new PrintItem(cur.getParameters().get("Title"),Integer.parseInt(cur.getParameters().get("Sequence")),Integer.parseInt(cur.getParameters().get("Size")),Integer.parseInt(cur.getParameters().get("PageCount")),Integer.parseInt(cur.getParameters().get("BatchSeq")),date ));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
			
			if (in.ready()) { cur = readNextCommand(); log(cur); }
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Collections.sort(items);
		
		return items;
	}

	@Override
	public synchronized ArrayList<PrintItem> getPrintItems(Sequence seq) {
		ArrayList<PrintItem> items = new ArrayList<PrintItem>();
		
		Command cur;
		
		Calendar seqCal = new GregorianCalendar();
		seqCal.setTime(seq.getDate());
		
		if( !connected )
			return null;
		
		Command getItems = new Command("File");
		getItems.getParameters().put("Action", "List");
		getItems.getParameters().put("MaxCount", "300");
		getItems.getParameters().put("Query", "BATCH " + seq.getSeq());
		getItems.getParameters().put("Type", "Report");
		
		write(getItems);
		log("Requesting batch sequence: " + seq);
		
		try {
			while( (cur = readNextCommand()).getParameters().get("Done") == null ){
				log(cur);
				
				if( cur.getParameters().get("Sequence") != null ){
					try {
					   Date date = Util.parseDate(cur.getParameters().get("Date"), cur.getParameters().get("Time"));
					   Calendar curCal = new GregorianCalendar();
					   curCal.setTime(date);
					   
					   if( curCal.get(Calendar.DAY_OF_YEAR) == seqCal.get(Calendar.DAY_OF_YEAR))
						   items.add( new PrintItem(cur.getParameters().get("Title"),Integer.parseInt(cur.getParameters().get("Sequence")),Integer.parseInt(cur.getParameters().get("Size")),Integer.parseInt(cur.getParameters().get("PageCount")),Integer.parseInt(cur.getParameters().get("BatchSeq")),date ));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
					
			}
			
			if (in.ready()) { cur = readNextCommand(); log(cur); }					
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Collections.sort(items);

		
		return items;
	}

	@Override
	public synchronized SessionError renameFile(SymitarFile file, String newName) {
		Command retrieve = new Command();
		retrieve.setCommand("File");
		retrieve.getParameters().put("Action", "Rename");

		if (file.getType() == FileType.REPGEN)
			retrieve.getParameters().put("Type", "RepWriter");
		else if (file.getType() == FileType.HELP)
			retrieve.getParameters().put("Type", "Help");
		else if (file.getType() == FileType.LETTER)
			retrieve.getParameters().put("Type", "Letter");
		else if (file.getType() == FileType.REPORT)
			retrieve.getParameters().put("Type", "Report");

		retrieve.getParameters().put("Name", file.getName());
		retrieve.getParameters().put("NewName", newName);

		write(retrieve);

		Command current = null;

		try {
			current = readNextCommand();

			if (current.getParameters().get("Status") != null && current.getParameters().get("Status").contains("No such file or directory"))
				return SessionError.ARGUMENT_ERROR;
			else if (current.getParameters().get("Status") != null)
				return SessionError.FILENAME_TOO_LONG;
			else if (current.getParameters().get("Done") != null)
				return SessionError.NONE;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return SessionError.IO_ERROR;
	}
	
	/**
	 * @return Date of the SYM in GregorianCalendar format.
	 */
	public GregorianCalendar getSymDate() {
		return symDate;
	}

	/**
	 * @return Date of the SYM in a string format.
	 */
	public String getSymDateString() {
		return (symDate.get(Calendar.MONTH) + 1) + "/" + symDate.get(Calendar.DATE)+"/" + symDate.get(Calendar.YEAR);
	}

	/**
	 * Depending on the AIX Username used, you may be automatically logged into a
	 * SYM other than the one specified during login.  getAcutalSym() will obtain
	 * the actual SYM this session is connected to.
	 * 
	 * @return the actual SYM this session is logged into.
	 */
	public int getActualSym() {
		return actualSym;
	}

	/**
	 * @return Console Number of the Symitar Session.
	 */
	public int getConsoleNum() {
		return consoleNum;
	}

	/**
	 * @return Host IPA of this Symitar Session.
	 */
	public String getHostIPA() {
		return hostIPA;
	}

	/**
	 * @return the Symitar Release Level.
	 */
	public String getSymRev() {
		return symRev;
	}
	
	/**
	 * @return a String description of the Session, including the following;
	 * 
	 */
	public String getProperties(){
		String str =  "Server IPA: " + hostIPA + "\n"+
	                  "Port: " + port + "\n"+
	                  "SYM: " + actualSym + "\n"+
	                  "SYM Date: " + getSymDateString() + "\n"+
	                  "Release: " + symRev + "\n"+
	                  "Console: " + consoleNum + "\n"+
	                  "Username: " + aixUsername + "\n"+
	                  "TellerID: " + userNum + "\n";
		
		return str;
	}

	private void traceLog(String str){
		if(bFullTrace){
			try {
				trace.write(10);
				
				if(!bSensitiveData){
					trace.write("~~>".getBytes());
					trace.write(str.getBytes());
					trace.write("<~~".getBytes());
				} else {
					trace.write("~~>XXXXXXXX<~~".getBytes());
				}
				
				trace.write(10);
				
				trace.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	private SessionError cacheSSHKey() {
		int iData = 0;
		String sData = "";
		
		try {
			while(err.ready()) {
				iData = err.read();
				sData += (char)iData;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(sData.indexOf("host key is not cached")>-1) {
			System.out.println("\r\n" + sData + "\r\n");
			System.out.println("Caching the SSH Key...");
			out.print("y\r");
			out.flush();
		} else if(sData.indexOf("host key does not match")>-1) {
			System.out.println("\n   *** HOST KEY HAS CHANGED ! ! ! ***\n       RepDev TERMINATED");
			disconnect();
			return SessionError.SSH_KEY_CHANGED;
		}
		
		return SessionError.NONE;
	}

	
	private void pause(int msec) {
		try {
			Thread.sleep(msec);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
