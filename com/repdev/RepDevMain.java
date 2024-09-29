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

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;

/**
 * Main run class, runs as first startup
 * Provides many application global functions/variables, as well as intializes all the stuff we need
 *
 * TODO: Documentation for RepDev
 *
 * @see Awesomeness
 * @author Jake Poznanski, Ryan Schultz, Sean Delaney
 */
public class RepDevMain {
	public static final HashMap<Integer, SymitarSession> SYMITAR_SESSIONS = new HashMap<Integer, SymitarSession>();
	public static HashMap<Integer, SessionInfo> SESSION_INFO = new HashMap<Integer, SessionInfo>();
	public static byte [] MASTER_PASSWORD_HASH;
	public static final boolean DEVELOPER = false; //Set this flag to enable saving passwords, this makes it easy for developers to log in and check stuff quickly after making changes
	public static final int VMAJOR = 1;
	public static final int VMINOR = 7;
	public static final int VFIX   = 5;
	public static final String VSPECIAL = ""; // "special" string for release names, beta, etc

	public static final String VERSION = VMAJOR + "." + VMINOR + (VFIX>0?"."+VFIX:"") + (DEVELOPER ? "-dev" : "") + (!VSPECIAL.equals("")? " " + VSPECIAL : "");
	public static final String NAMESTR = "RepDev v" + VERSION;
	public static boolean FORGET_PASS_ON_EXIT = false; // set in options only please.

	public static MainShell mainShell;
	private static Display display;
	public static Image smallAddImage, smallErrorsImage, smallFileImage, smallProjectImage, smallRemoveImage, smallRepGenImage, smallSymImage, smallSymOnImage, smallTasksImage, smallActionSaveImage, smallFileAddImage, smallFileRemoveImage,
	smallProjectAddImage, smallProjectRemoveImage, smallRunImage, smallSymAddImage, smallSymRemoveImage, smallDBFieldImage, smallDBRecordImage, smallVariableImage, smallImportImage, smallFileNewImage, smallFileOpenImage, smallDeleteImage,
	smallOptionsImage, smallIndentLessImage, smallIndentMoreImage, smallCutImage, smallCopyImage, smallPasteImage, smallSelectAllImage, smallRedoImage, smallUndoImage, smallFindImage, smallFindReplaceImage, smallExitImage, smallRunFMImage,
	smallWarningImage, smallReportsImage, smallPrintImage, smallFolderImage, smallFolderAddImage, smallFolderRemoveImage, smallActionSaveAsImage, smallProgramIcon, smallInstallImage, smallCompareImage, smallSurroundImage, smallSurroundPrint,
	smallTaskTodo, smallTaskFixme, smallTaskBug, smallTaskWtf, smallTaskTest, smallTaskBookmark, smallTaskNote, smallHighlight, smallHighlightGrey, smallFormatCodeImage, smallInsertSnippetImage, smallFunctionImage, smallSnippetImage, smallKeywordImage, smallDefineVarImage,
	smallRepGenDemandImage;
	public static final String IMAGE_DIR = "repdev-icons/";

	public static SnippetManager snippetManager;

	private enum CONFIGREV{
		NORMAL, NEW, OUTDATED
	}
	private static CONFIGREV configRev = CONFIGREV.NORMAL;

	public static void main(String[] args) throws Exception {
		display = new Display();

		System.out.println("\nRepDev " + VERSION + " Copyright (C) 2008-2014  RepDev.org Team\n"
				+"This program comes with ABSOLUTELY NO WARRANTY.\n"
				+"This is free software, and you are welcome to redistribute it \n"
				+"under certain conditions.\n");

		System.out.println("Java Runtime version " + System.getProperty("java.runtime.version"));
		System.out.println("---------------------------------------------------------");
		System.out.println("Charset.defaultCharset()                  = " + Charset.defaultCharset());
		System.out.println("System.getProperty(\"file.encoding\")       = " + System.getProperty("file.encoding"));

		try{
			loadSettings();
			createImages();
			createGUI();

			if (!Config.getPasswordValidator().contentEquals("") && RepDevMain.MASTER_PASSWORD_HASH == null) RepDev_SSO.login(mainShell.shell);
			
			while (!mainShell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		} catch(Exception e){
			if( e != null && e.getMessage() != null && e.getMessage().indexOf("[GDI+ is required]") != -1) {
				System.out.println("RepDev Requires GDI+ to be installed in order to run");
				System.out.println("GDI+ can be obtained from: http://www.microsoft.com/downloads/details.aspx?FamilyID=6a63ab9c-df12-4d41-933c-be590feaa05a&DisplayLang=en");
			} else {

				if (display.isDisposed())
					display = new Display();

				e.printStackTrace();

				ErrorDialog errorDialog = new ErrorDialog(e);
				errorDialog.open();

				display.dispose();
			}
		}

		// Save off projects
		ProjectManager.saveAllProjects();
		saveSettings();

		//Close all symitar connections
		for( SymitarSession session : SYMITAR_SESSIONS.values() ){
			if( session != null )
				session.disconnect();
		}

		display.dispose();
		System.exit(0);
	}

	private static void createImages() {
		smallActionSaveImage = new Image(display, IMAGE_DIR + "small-action-save.png");
		smallAddImage = new Image(display, IMAGE_DIR + "small-add.png");
		smallErrorsImage = new Image(display, IMAGE_DIR + "small-errors.png");
		smallFileAddImage = new Image(display, IMAGE_DIR + "small-file-add.png");
		smallFileRemoveImage = new Image(display, IMAGE_DIR + "small-file-remove.png");
		smallFileImage = new Image(display, IMAGE_DIR + "small-file.png");
		smallErrorsImage = new Image(display, IMAGE_DIR + "small-errors.png");
		smallProjectAddImage = new Image(display, IMAGE_DIR + "small-project-add.png");
		smallProjectRemoveImage = new Image(display, IMAGE_DIR + "small-project-remove.png");
		smallProjectImage = new Image(display, IMAGE_DIR + "small-project.png");
		smallRemoveImage = new Image(display, IMAGE_DIR + "small-remove.png");
		smallRepGenImage = new Image(display, IMAGE_DIR + "small-repgen.png");
		smallRepGenDemandImage = new Image(display, IMAGE_DIR + "small-repgen-demand.png");
		smallRunImage = new Image(display, IMAGE_DIR + "small-run.png");
		smallSymImage = new Image(display, IMAGE_DIR + "small-sym.png");
		smallSymOnImage = new Image(display, IMAGE_DIR + "small-sym-on.png");
		smallTasksImage = new Image(display, IMAGE_DIR + "small-tasks.png");
		smallSymAddImage = new Image(display, IMAGE_DIR + "small-sym-add.png");
		smallSymRemoveImage = new Image(display, IMAGE_DIR + "small-sym-remove.png");
		smallDBRecordImage = new Image(display, IMAGE_DIR + "small-db-record.png");
		smallDBFieldImage = new Image(display, IMAGE_DIR + "small-db-field.png");
		smallVariableImage = new Image(display, IMAGE_DIR + "small-variable.png");
		smallImportImage = new Image(display, IMAGE_DIR + "small-import.png");
		smallFileNewImage = new Image(display, IMAGE_DIR + "small-file-new.png");
		smallFileOpenImage = new Image(display, IMAGE_DIR + "small-file-open.png");
		smallDeleteImage = new Image(display, IMAGE_DIR + "small-delete.png");
		smallOptionsImage = new Image(display, IMAGE_DIR + "small-options.png");
		smallIndentLessImage = new Image(display, IMAGE_DIR + "small-indent-less.png");
		smallIndentMoreImage = new Image(display, IMAGE_DIR + "small-indent-more.png");
		smallCutImage = new Image(display, IMAGE_DIR + "small-cut.png");
		smallCopyImage = new Image(display, IMAGE_DIR + "small-copy.png");
		smallPasteImage = new Image(display, IMAGE_DIR + "small-paste.png");
		smallRedoImage = new Image(display, IMAGE_DIR + "small-redo.png");
		smallUndoImage = new Image(display, IMAGE_DIR + "small-undo.png");
		smallSelectAllImage = new Image(display, IMAGE_DIR + "small-select-all.png");
		smallFindImage = new Image(display, IMAGE_DIR + "small-find.png");
		smallFindReplaceImage = new Image(display, IMAGE_DIR + "small-find-replace.png");
		smallExitImage = new Image(display, IMAGE_DIR + "small-exit.png");
		smallRunFMImage = new Image(display, IMAGE_DIR + "small-run-fm.png");
		smallWarningImage = new Image(display, IMAGE_DIR + "small-warning.png");
		smallReportsImage = new Image(display, IMAGE_DIR + "small-reports.png");
		smallPrintImage = new Image(display, IMAGE_DIR + "small-print.png");
		smallFolderImage = new Image(display, IMAGE_DIR + "small-folder.png");
		smallFolderAddImage = new Image(display, IMAGE_DIR + "small-folder-add.png");
		smallFolderRemoveImage = new Image(display, IMAGE_DIR + "small-folder-remove.png");
		smallActionSaveAsImage = new Image(display, IMAGE_DIR + "small-action-save-as.png");

		smallProgramIcon = new Image(display, IMAGE_DIR + "monkeyIcon16.png");
		smallInstallImage = new Image(display, IMAGE_DIR + "small-install-repgen.png");
		smallCompareImage = new Image(display, IMAGE_DIR + "small-compare.png");
		smallSurroundImage = new Image(display, IMAGE_DIR + "small-surround.png");
		smallSurroundPrint = new Image(display, IMAGE_DIR + "small-surround-print.png");

		smallHighlight = new Image(display, IMAGE_DIR + "small-highlight.png");
		smallHighlightGrey = new Image(display, IMAGE_DIR + "small-highlight-grey.png");

		smallTaskTodo = new Image(display, IMAGE_DIR + "small-task-todo.png");
		smallTaskFixme = new Image(display, IMAGE_DIR + "small-task-fixme.png");
		smallTaskBug = new Image(display, IMAGE_DIR + "small-task-bug.png");
		smallTaskWtf = new Image(display, IMAGE_DIR + "small-task-wtf.png");
		smallTaskTest = new Image(display, IMAGE_DIR + "small-task-test.png");
		smallTaskBookmark = new Image(display, IMAGE_DIR + "small-task-bookmark.png");
		smallTaskNote = new Image(display, IMAGE_DIR + "small-task-note.png");

		smallFormatCodeImage = new Image(display, IMAGE_DIR + "small-format-code.png");
		smallInsertSnippetImage = new Image(display, IMAGE_DIR + "small-insert-snippet.png");

		smallFunctionImage = new Image(display, IMAGE_DIR + "small-function.png");
		smallKeywordImage = new Image(display, IMAGE_DIR + "small-keyword.png");
		smallSnippetImage = new Image(display, IMAGE_DIR + "small-snippet.png");
		smallDefineVarImage = new Image(display, IMAGE_DIR + "small-define-var.png");
	}

	/**
	 * Loads Config object and settings from a serialized file Also connects to
	 * all syms in the config file
	 *
	 * Also, starts the snippet manager
	 */
	public static void loadSettings() {
		String localFile = "repdev.conf";
		String userFile = System.getProperty("user.home") + System.getProperty("file.separator") + "repdev.conf";
		String loadFile = localFile;

		if( new File(userFile).exists() && !new File(localFile).exists() ){
			System.out.println("The config file is being copied from it's old location in your user folder, to the local Repdev install folder.\nOld Location: " +
								userFile);
			loadFile = userFile;
		}

		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(loadFile));
			Config configObject = (Config) in.readObject();
			Config.setConfig(configObject);
			in.close();
		} catch (ClassCastException e) {
			System.out.println("FILE OUT OF DATE!");
		} catch (IOException e) {
			//Any odd defaults
			Config.setRevision(-1);
			Config.setRunOptionsQueue(-1);
			Config.setLastPassword(""); // only saved if RepDevMain.DEVELOPER
			Config.setLastUserID("");
			Config.setLastUsername("");
			Config.setTerminateHour(20);
			Config.setTerminateMinute(0);
			Config.setListUnusedVars(true);

			System.out.println("Creating data file for the first time.");
			saveSettings();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		SESSION_INFO = Config.getSessionInfo();
		if(SESSION_INFO == null) {
			SESSION_INFO = new HashMap<Integer, SessionInfo>();
		}
		
		SymitarSession session;

		// Start up data
		for (int sym : Config.getSyms()) {

			if( Config.getServer().equalsIgnoreCase("testsession")) //Allows for a testing mode when no symitar server's are available
				session = new TestingSymitarSession();
			else
				session = new DirectSymitarSession();

			if (SESSION_INFO.get(sym) == null) {
				SessionInfo si = new SessionInfo("", "", "", "", "");
				SESSION_INFO.put(sym, si);
			} else {
				session.setServer(SESSION_INFO.get(sym).getServer());
			}
			SYMITAR_SESSIONS.put(sym, session);
		}
		if(Config.getTerminateHour()==0){
			Config.setTerminateHour(20);
			Config.setTerminateMinute(0);
			saveSettings();
		}

		if(Config.getRevision()==-1){
			configRev = CONFIGREV.NEW;
		}
		else if (Config.getRevision()!=Config.REVISION){
			configRev = CONFIGREV.OUTDATED;
		}
	}

	/**
	 * Saves the config object to a file
	 *
	 */

	public static void saveSettings() {
		try {
			// Write the current syms to the Config file
			ArrayList<Integer> newSyms = new ArrayList<Integer>();
			HashMap<Integer, SessionInfo> newSessionInfo = new HashMap<Integer, SessionInfo>();

			for (int sym : SYMITAR_SESSIONS.keySet()) {
				newSyms.add(sym);
				newSessionInfo.put(sym, SESSION_INFO.get(sym));
			}

			Config.setSyms(newSyms);
			Config.setSessionInfo(newSessionInfo);


			//Only save passwords if DEVELOPER FLAG is on
			if( !DEVELOPER || FORGET_PASS_ON_EXIT ){
				Config.setLastPassword("");
				Config.setLastUserID("");
			}

			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("repdev.conf"));
			out.writeObject(Config.getConfig());
			out.close();
		} catch (Exception e) {
			System.err.println("Error saving Config data");
			e.printStackTrace();
		}
	}

	private static void createGUI() {
		// Set Default Size
		if(configRev == CONFIGREV.NEW){
			Config.setSashHSize(150);
			Config.setSashVSize(150);
		}

		mainShell = new MainShell(display);
		mainShell.open();
		createGlobalHotkeys();
		if(configRev != CONFIGREV.NORMAL){
			MessageBox msg = new MessageBox(mainShell.getShell(), SWT.ICON_WARNING);
			msg.setText("RepDev Options");

			if(configRev == CONFIGREV.NEW){
				msg.setMessage("Welcome to RepDev. Please take a few moments to configure your Options.");
			}
			else{
				msg.setMessage("The RepDev Team has added new options.  Please take a few moments to configure them.");
			}
			msg.open();
			OptionsShell.show(mainShell.getShell());
			Config.setRevision(Config.REVISION);
		}
	}

	private static void createGlobalHotkeys(){
		Display.getDefault().addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if( e.stateMask == (SWT.CTRL | SWT.SHIFT) ){
//					if(e.keyCode == SWT.F11)
//						RepDevMain.mainShell.toggleFullScreen();
					switch(e.keyCode) {
					case 'f':
					case 'F':
						RepDevMain.mainShell.toggleFullScreen();
						break;
					case 's':
					case 'S':
						RepDevMain.mainShell.saveAllRepgens();
						break;

					case 'o':
					case 'O':
						RepDevMain.mainShell.showOptions();
						break;
					}

				}
				else if (e.stateMask == SWT.CTRL) {
					switch (e.keyCode) {
					case 'o':
					case 'O':
						RepDevMain.mainShell.showFileOpenMenu();
						break;
					}
				}
			}
			});
	}
}
