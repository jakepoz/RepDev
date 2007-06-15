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
 * 
 * Provides many application global functions/variables, as well as intializes all the stuff we need
 * 
 * @author Jake Poznanski
 *
 */
public class RepDevMain {
	public static final HashMap<Integer, SymitarSession> SYMITAR_SESSIONS = new HashMap<Integer, SymitarSession>();
	public static final String VERSION = "v0.1.4-20070615";
	public static final String NAMESTR = "RepDev - " + VERSION;
	public static MainShell mainShell;
	private static Display display;
	public static Image largeActionSaveImage, largeAddImage, largeFileAddImage, largeFileRemoveImage, largePrintLocalImage, largePrintLPTImage, largePrintTPTImage, largeProjectAddImage, largeProjectRemoveImage, largeRemoveImage, largeRunImage,
			largeSymAddImage, largeSymRemoveImage, smallAddImage, smallErrorsImage, smallFileImage, smallProjectImage, smallRemoveImage, smallRepGenImage, smallSymImage, smallTasksImage, smallActionSaveImage, smallFileAddImage, smallFileRemoveImage,
			smallProjectAddImage, smallProjectRemoveImage, smallRunImage, smallSymAddImage, smallSymRemoveImage, smallDBFieldImage, smallDBRecordImage, smallVariableImage, smallImportImage, smallFileNewImage, smallFileOpenImage, smallDeleteImage,
			smallOptionsImage, smallIndentLessImage, smallIndentMoreImage, smallCutImage, smallCopyImage, smallPasteImage, smallSelectAllImage, smallRedoImage, smallUndoImage, smallFindImage, smallFindReplaceImage, smallExitImage, smallRunFMImage,
			smallWarningImage, smallReportsImage;
	public static final String IMAGE_DIR = "repdev-icons/";

	public static void main(String[] args) {
		display = new Display();

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

			ErrorDialog errorDialog = new ErrorDialog(e);
			errorDialog.open();
		}

		// Save off projects
		ProjectManager.saveAllProjects();
		saveSettings();
		
		//Close all symitar connections
		for( SymitarSession session : SYMITAR_SESSIONS.values() ){
			session.disconnect();
		}
		
		display.dispose();
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
			System.out.println("Could not load data file.");
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

	public static void saveSettings() {
		try {
			// Write the current syms to the Config file
			ArrayList<Integer> newSyms = new ArrayList<Integer>();

			for (int sym : SYMITAR_SESSIONS.keySet()) {
				newSyms.add(sym);
			}

			Config.setSyms(newSyms);

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
