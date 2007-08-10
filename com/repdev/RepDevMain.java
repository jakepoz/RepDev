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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Main run class, runs as first startup
 * Provides many application global functions/variables, as well as intializes all the stuff we need
 * 
 * TODO: Documentation for RepDev
 * 
 * @see Awsomeness
 * @author Jake Poznanski, Ryan Schultz, Sean Delaney
 */
public class RepDevMain {
	public static final HashMap<Integer, SymitarSession> SYMITAR_SESSIONS = new HashMap<Integer, SymitarSession>();

	public static final boolean DEVELOPER = false; //Set this flag to enable saving passwords, this makes it easy for developers to log in and check stuff quickly after making changes
	public static final String VERSION = "1.0" + (DEVELOPER ? "-dev" : "");
	public static final String NAMESTR = "RepDev  v" + VERSION;
	public static boolean FORGET_PASS_ON_EXIT = false; // set in options only please.
	public static MainShell mainShell;
	private static Display display;
	public static Image largeActionSaveImage, largeAddImage, largeFileAddImage, largeFileRemoveImage, largePrintLocalImage, largePrintLPTImage, largePrintTPTImage, largeProjectAddImage, largeProjectRemoveImage, largeRemoveImage, largeRunImage,
			largeSymAddImage, largeSymRemoveImage, smallAddImage, smallErrorsImage, smallFileImage, smallProjectImage, smallRemoveImage, smallRepGenImage, smallSymImage, smallTasksImage, smallActionSaveImage, smallFileAddImage, smallFileRemoveImage,
			smallProjectAddImage, smallProjectRemoveImage, smallRunImage, smallSymAddImage, smallSymRemoveImage, smallDBFieldImage, smallDBRecordImage, smallVariableImage, smallImportImage, smallFileNewImage, smallFileOpenImage, smallDeleteImage,
			smallOptionsImage, smallIndentLessImage, smallIndentMoreImage, smallCutImage, smallCopyImage, smallPasteImage, smallSelectAllImage, smallRedoImage, smallUndoImage, smallFindImage, smallFindReplaceImage, smallExitImage, smallRunFMImage,
			smallWarningImage, smallReportsImage, smallPrintImage, smallFolderImage, smallFolderAddImage, smallFolderRemoveImage, smallActionSaveAsImage, smallProgramIcon;
	public static final String IMAGE_DIR = "repdev-icons/";

	public static void main(String[] args) throws Exception{
		display = new Display();
		
		System.out.println("RepDev  Copyright (C) 2007  RepDev.org Team\n"
				+"This program comes with ABSOLUTELY NO WARRANTY.\n"
				+"This is free software, and you are welcome to redistribute it\n"
				+"under certain conditions.\n");
	
		try{
			loadSettings();
			createImages();
			createGUI();
			
			while (!mainShell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		}
		catch(Exception e){
			if (display.isDisposed())
				display = new Display();
			
			e.printStackTrace();

			ErrorDialog errorDialog = new ErrorDialog(e);
			errorDialog.open();
			
			display.dispose();
		}

		// Save off projects
		ProjectManager.saveAllProjects();
		saveSettings();
		
		//Close all symitar connections
		for( SymitarSession session : SYMITAR_SESSIONS.values() ){
			session.disconnect();
		}
		
		display.dispose();
		System.exit(0);
	}

	private static void createImages() {
		largeActionSaveImage = new Image(display, IMAGE_DIR + "large-action-save.png");
		largeAddImage = new Image(display, IMAGE_DIR + "large-add.png");
		largeFileAddImage = new Image(display, IMAGE_DIR + "large-file-add.png");
		largeFileRemoveImage = new Image(display, IMAGE_DIR + "large-file-remove.png");
		largePrintLocalImage = new Image(display, IMAGE_DIR + "large-print-local.png");
		largePrintLPTImage = new Image(display, IMAGE_DIR + "large-print-lpt.png");
		largePrintTPTImage = new Image(display, IMAGE_DIR + "large-print-tpt.png");
		largeProjectAddImage = new Image(display, IMAGE_DIR + "large-project-add.png");
		largeProjectRemoveImage = new Image(display, IMAGE_DIR + "large-project-remove.png");
		largeRemoveImage = new Image(display, IMAGE_DIR + "large-remove.png");
		largeRunImage = new Image(display, IMAGE_DIR + "large-run.png");
		largeSymAddImage = new Image(display, IMAGE_DIR + "large-sym-add.png");
		largeSymRemoveImage = new Image(display, IMAGE_DIR + "large-sym-remove.png");

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
		smallRunImage = new Image(display, IMAGE_DIR + "small-run.png");
		smallSymImage = new Image(display, IMAGE_DIR + "small-sym.png");
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
		
		smallProgramIcon = new Image(display, IMAGE_DIR + "icon-16x16.png");
	}

	/**
	 * Loads Config object and settings from a serialized file Also connects to
	 * all syms in the config file
	 * 
	 */
	public static void loadSettings() {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(System.getProperty("user.home") + System.getProperty("file.separator") + "repdev.conf"));
			Config configObject = (Config) in.readObject();
			Config.setConfig(configObject);

		} catch (ClassCastException e) {
			System.out.println("FILE OUT OF DATE!");
		} catch (IOException e) {
			//Any odd defaults
			Config.setRunOptionsQueue(-1);
			
			System.out.println("Creating data file for the first time.");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		SymitarSession session;

		// Start up data
		for (int sym : Config.getSyms()) {
			if (Config.getServer().equals("test"))
				session = new TestingSymitarSession();
			else
				session = new DirectSymitarSession();

			SYMITAR_SESSIONS.put(sym, session);
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

			for (int sym : SYMITAR_SESSIONS.keySet()) {
				newSyms.add(sym);
			}

			Config.setSyms(newSyms);
			
			
			//Only save passwords if DEVELOPER FLAG is on
			if( !DEVELOPER || FORGET_PASS_ON_EXIT ){
				Config.setLastPassword("");
				Config.setLastUserID("");
			}

			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(System.getProperty("user.home") + System.getProperty("file.separator") + "repdev.conf"));
			out.writeObject(Config.getConfig());
		} catch (Exception e) {
			System.err.println("Error saving Config data");
			e.printStackTrace();
		}
	}

	private static void createGUI() {
		mainShell = new MainShell(display);
		mainShell.open();
	}

}
