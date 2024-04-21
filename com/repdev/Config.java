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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
/**
 * Configuration class is serialized at the start/end of the program to store things like syms open, server, and other global config things
 * @author Jake Poznanski
 *
 */
public class Config implements Serializable {
	private static Config me = new Config();
	private static final long serialVersionUID = 1L;
	
	private ArrayList<Integer> syms = new ArrayList<Integer>();
	private HashMap<Integer, SessionInfo> sessionInfo = new HashMap<Integer, SessionInfo>();
	private String server = "127.0.0.1";
	private int port = 23;
	private int tabSize = 0; // 0 = Regular tab
	private String lastUsername = "", lastPassword = "", lastUserID;
	private String passwordValidator = "";
	private boolean runOptionsAskForPrompts = true;
	private int runOptionsQueue = -1;
	private int rotkeyp=0, rotkeyu=0;
	
	private String style = "default";
	
	private int liveSym = 1999;
	private String liveSymColor = "FFD7E4";
	private boolean useSourceControl = false;
	private String sourceControlDir = "";
	/**
	 * REVISION is a constant.  This constant should be incrmented everytime there is a new
	 * feature added.  RepDevMain will compare REVISION with getRevision() and if they are
	 * different, then a popup will notify the user and will launch the OptionsShell so that
	 * the users can config the new options.
	 */
	public final static int REVISION = 6; // Modify this everytime we add new options to prompt the user.
	private int revision=-1;
	private boolean windowMaximized;
	private Point windowSize;
	private boolean listUnusedVars, wrapSearch, caseSensitive, neverTerminateKeepAlive;
	private int terminateHour;
	private int terminateMinute;
	private int sashHSize, sashVSize;
	private boolean backupProjectFiles = false;
	private String noErrorCheckSuffix = ".PRO,.SET,.DEF,.INC";
	private String noErrorCheckPrefix = "INC.";
	private boolean fileNameInWinTitle = true;
	private boolean hostInTitle = true;
	private boolean viewLineNumbers = true;
	
	@SuppressWarnings("unused")
	private int maxQueues = 3; //The largest value this slider goes up to, We should probably scrap this since the max value is 9999 and the error checking code is good enough now that it can detect what needs to be entered. In real life, this can also be non continous large ranges, which complicates things.
							//UPDATE: Ok, this has been removed, however, you can't remove items from Serialized classes.
    private ArrayList<SymitarFile> recentFiles = new ArrayList<SymitarFile>();
    private ArrayList<String> mountedDirs = new ArrayList<String>();
    
	private Config() {
	}

	public static void setSyms(ArrayList<Integer> syms) {
		me.syms = syms;
	}

	public static ArrayList<Integer> getSyms() {
		return me.syms;
	}

	public static void setSessionInfo(HashMap<Integer, SessionInfo> newSessionInfo) {
		me.sessionInfo = newSessionInfo;
	}
	
	public static HashMap<Integer, SessionInfo> getSessionInfo(){
		return me.sessionInfo;
	}
	
	public static Config getConfig() {
		return me;
	}

	public static String getPasswordValidator() {
		return me.passwordValidator == null ? "" : me.passwordValidator;
	}
	
	public static void setPasswordValidator(String passwordValidator) {
		me.passwordValidator = passwordValidator;
	}
	
	public static boolean useSSO() {
		if (getPasswordValidator().equals("")) {
			return false;
		} else {
			return true;
		}
	}
 
	public static String getLastPassword() {
		String Pass="";
		String last = me.lastPassword;
		for(int i = 0; i < last.length(); i++){
			char tmp = last.charAt(i);
			tmp-=me.rotkeyp;
			Pass+=tmp;
		}
		return Pass;
	}
	
	public static void setLastPassword(String lastPassword) {
		me.rotkeyp=(int)Math.random()*150+20;
		String Pass="";
		for(int i = 0; i < lastPassword.length(); i++){
			char tmp = lastPassword.charAt(i);
			tmp+=me.rotkeyp;
			Pass+=tmp;
		}
		me.lastPassword = Pass;
		//System.out.println(Pass);
	}

	public static String getLastUsername(){
		return me.lastUsername;
	}

	public static void setLastUsername(String lastUsername) {
		me.lastUsername = lastUsername;
	}

	public static void setConfig(Config config) {
		me = config;
		Collections.sort(me.syms);
		/**
		 * Note, this is where we should set any init stuff to un-null any objects
		 */
		if( me.recentFiles == null) 
			me.recentFiles = new ArrayList<SymitarFile>();
		
		if( me.mountedDirs == null) 
			me.mountedDirs = new ArrayList<String>();
		
		if( me.port == 0 )
			me.port = 23; // default to 23 if 0 or unset.
	}

	public static void setServer(String server) {
		me.server = server;
	}

	public static String getServer() {
		return me.server;
	}

	public static void setTabSize(int tabSize) {
		me.tabSize = tabSize;
	}

	public static int getTabSize() {
		return me.tabSize;
	}

	public static boolean isRunOptionsAskForPrompts() {
		return me.runOptionsAskForPrompts;
	}

	public static void setRunOptionsAskForPrompts(boolean runOptionsAskForPrompts) {
		me.runOptionsAskForPrompts = runOptionsAskForPrompts;
	}

	public static int getRunOptionsQueue() {
		return me.runOptionsQueue;
	}

	public static void setRunOptionsQueue(int runOptionsQueue) {
		me.runOptionsQueue = runOptionsQueue;
	}


	public static String getLastUserID() {
		String User="";
		String last = me.lastUserID;
		for(int i = 0; i < last.length(); i++){
			char tmp = last.charAt(i);
			for(int j = 0; j < me.lastUsername.length(); j++){
				tmp-=me.lastUsername.charAt(j);
			}
			tmp-=me.rotkeyu;
			User+=tmp;
		}
		return User;
	}

	public static void setLastUserID(String lastUserID) {
		me.rotkeyu=(int)Math.random()*150+15;
		String User="";
		for(int i = 0; i < lastUserID.length(); i++){
			char tmp = lastUserID.charAt(i);
			for(int j = 0; j < me.lastUsername.length(); j++){
				tmp+=me.lastUsername.charAt(j);
			}
			tmp+=me.rotkeyu;
			User+=tmp;
		}
		me.lastUserID = User;
		//System.out.println(User);
	}
	
	public static ArrayList<SymitarFile> getRecentFiles() {
		return me.recentFiles;
	}

	public static void setRecentFiles(ArrayList<SymitarFile> recentFiles) {
		me.recentFiles = recentFiles;
	}

	public static ArrayList<String> getMountedDirs() {
		return me.mountedDirs;
	}

	public static void setMountedDirs(ArrayList<String> mountedFolders) {
		me.mountedDirs = mountedFolders;
	}
	
	public static int getPort() {
		return me.port;
	}
	
	public static void setPort(int p) {
		me.port = p;
	}
	
	public static String getStyle() {
	    return me.style;
	}
	
	public static void setStyle(String s) {
	    me.style = s;
	}
	
	public static void setLiveSym(int s) {
		me.liveSym = s;
	}

	public static int getLiveSym() {
		return me.liveSym;
	}

	public static void setLiveSymColor(String c) {
		me.liveSymColor = c;
	}

	public static String getLiveSymColor() {
		return me.liveSymColor;
	}
	
	public static boolean getUseSourceControl() {
		return me.useSourceControl;
	}
	
	public static void setUseSourceControl(boolean b) {
		me.useSourceControl = b;
	}
	
	public static String getSourceControlDir() {
		return me.sourceControlDir;
	}
	
	public static void setSourceControlDir(String dir) {
		me.sourceControlDir = dir;
	}

	/**
	 * Returns true if the main shell window was maximized, and false
	 * if it was not maximized, when RepDev terminated
	 * @return b
	 */
	public static boolean getWindowMaximized(){
		return me.windowMaximized;
	}
	
	/**
	 * Used to rember the state of the main shell window.  This should be set true if
	 * the window is maximized and set false if it is not.
	 * @param boolean
	 */
	public static void setWindowMaximized(boolean b){
		me.windowMaximized = b;
	}
	
	/**
	 * Returns the size of the main shell window
	 * @return Point
	 */
	public static Point getWindowSize(){
		return me.windowSize;
	}
	
	/**
	 * Used to remember the size of the main shell window.
	 * @param Point
	 */
	public static void setWindowSize(Point p){
		me.windowSize = p;
	}
	
	/**
	 * Returns true if the user would like to list the unused variables
	 * @return boolean
	 */
	public static boolean getListUnusedVars(){
		return me.listUnusedVars;
	}
	
	/**
	 * Set true if the user would like to list the usused variables.
	 * @param b
	 */
	public static void setListUnusedVars(boolean b){
		me.listUnusedVars = b;
	}
	
	/**
	 * Returns the Keep Alive Terminate Hour
	 * @return hour
	 */
	public static int getTerminateHour(){
		return me.terminateHour;
	}
	
	/**
	 * Set the Keep Alive Termination Hour.
	 * @param hour.
	 */
	public static void setTerminateHour(int hour){
		me.terminateHour = hour;
	}
	
	/**
	 * Returns the Keep Alive Termination Minute.
	 * @return minute
	 */
	public static int getTerminateMinute(){
		return me.terminateMinute;
	}
	
	/**
	 * Set the Keep Alive Termination Minute.
	 * @param minute.
	 */
	public static void setTerminateMinute(int minute){
		me.terminateMinute = minute;
	}
	
	/**
	 * Returns the size of the left pane of the main shell window.
	 * @return size
	 */
	public static int getSashHSize(){
		return me.sashHSize;
	}
	
	/**
	 * Set the size of the left pane of the main shell window.
	 * @param size
	 */
	public static void setSashHSize(int size){
		me.sashHSize = size;
	}
	
	/**
	 * Returns the size of the bottom pane of the main shell window.
	 * @return size
	 */
	public static int getSashVSize(){
		return me.sashVSize;
	}
	
	/**
	 * Set the size of the bottom pane of the main shell window.
	 * @param size
	 */
	public static void setSashVSize(int size){
		me.sashVSize = size;
	}
	
	/**
	 * Return true if Wrap Search is checked in the FindReplaceShell dialogue box.
	 * @return boolean
	 */
	public static boolean getWrapSearch(){
		return me.wrapSearch;
	}
	
	/**
	 * Set this to true if Wrap Search is checked in the FindReplaceShell dialogue box.
	 * @param boolean
	 */
	public static void setWrapSearch(boolean b){
		me.wrapSearch = b;
	}

	/**
	 * Return true if Case Sensiive is checked in the FindReplaceShell dialogue box.
	 * @return boolean
	 */
	public static boolean getCaseSensitive(){
		return me.caseSensitive;
	}
	
	/**
	 * Set this to true if Case Sensitive is checked in the FindReplaceShell dialogue box.
	 * @param boolean
	 */
	public static void setCaseSensitive(boolean b){
		me.caseSensitive = b;
	}
	
	/**
	 * Returns the current version in the repdev.conf file
	 * @return rev - the revision in the repdev.conf file
	 */
	public static int getRevision(){
		return me.revision;
	}
	
	/**
	 * Sets the revision to save in the repdev.conf file.
	 * @param rev the revision for the repdev.conf file
	 */
	public static void setRevision(int rev){
		me.revision = rev;
	}
	
	/**
	 * Returns true if the user does not want to terminate Keep Alive at the selected time.
	 * after the set time.
	 * @return boolean
	 */
	public static boolean getNeverTerminate(){
		return me.neverTerminateKeepAlive;
	}
	
	/**
	 * Set true if the user does not want to terminat Keep Alive at the selected time.
	 * Set false if the user does want to terminate Keep Alive at the selected time.
	 */
	public static void setNeverTerminate(boolean b){
		me.neverTerminateKeepAlive = b;
	}

	public static boolean getBackupProjectFiles() {
		return me.backupProjectFiles;
	}
	
	public static void setBackupProjectFile(boolean b){
		me.backupProjectFiles = b;
	}

	/**
	 * Any RepGens with these prefixes will not be checked for errors.
	 * @param - a comma delimited list of prefixes.
	 */
	public static void setNoErrorCheckPrefix(String filePrefix){
		me.noErrorCheckPrefix = filePrefix;
	}

	/**
	 * Returns the string of prefixes to exclude from error check
	 * @return comma delimited list of prefixes
	 */
	public static String getNoErrorCheckPrefix(){
		return me.noErrorCheckPrefix;
	}

	/**
	 * Any RepGens with these suffixes will not be checked for errors.
	 * @param - a comma delimited list of suffixes.
	 */
	public static void setNoErrorCheckSuffix(String fileSuffix){
		me.noErrorCheckSuffix = fileSuffix;
	}

	/**
	 * Returns the string of suffixes to exclude from error check
	 * @return comma delimited list of suffixes
	 */
	public static String getNoErrorCheckSuffix(){
		return me.noErrorCheckSuffix;
	}

	public static void setFileNameInTitle(boolean nameInTitle){
		me.fileNameInWinTitle = nameInTitle;
	}

	public static boolean getFileNameInTitle(){
		return me.fileNameInWinTitle;
	}

	public static void setHostNameInTitle(boolean nameInTitle){
		me.hostInTitle = nameInTitle;
	}

	public static boolean getHostNameInTitle(){
		return me.hostInTitle;
	}

	public static void setViewLineNumbers(boolean lineNumbers){
		me.viewLineNumbers = lineNumbers;
	}

	public static boolean getViewLineNumbers(){
		return me.viewLineNumbers;
	}
}
