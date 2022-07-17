package com.repdev;

import java.io.File;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;


public class SourceControl {
	boolean useSourceControl = Config.getUseSourceControl();
	String sourceControlDir = Config.getSourceControlDir();
	boolean sourceControlDirExist;

	String fileData = "";
	String sourceControlFileData = "";

	public String getFile(SymitarFile file) {
		fileData = ((DirectSymitarSession) RepDevMain.SYMITAR_SESSIONS.get(file.getSym())).getFile(file);
		file.syncRepGen(false);

		if(!file.disableSourceControl()) {
			if (useSourceControl && file.getType() == FileType.REPGEN && /*!fileData.equals("") &&*/ file.getSym() != Config.getLiveSym() && !file.isCompareMode()) {
				// Using Source Control
				sourceControlDirExist = dirExist();
				if (sourceControlDirExist) {
					// repository Directory exist
					SymitarFile sourceControlFile = getSourceControlFile(file);
					//System.out.println("file name: " + sourceControlFile.getPath());
					if (fileExist(sourceControlFile)) {
						// file exist in repository
						sourceControlFileData = sourceControlFile.getData();
						if (fileData.equals(sourceControlFileData)) {
							// Files match
							file.syncRepGen(true);
							return fileData;
						} else {
							// Files do not match
							SourceControlSelection scs=new SourceControlSelection();
							scs.open(file.getName());
							if(scs.selection == SourceControlSelection.CHOICE.COMPARE) {
								RepDevMain.mainShell.compareFiles(file, sourceControlFile);
								file.compareMode(true);
								return null;
							} else if(scs.selection == SourceControlSelection.CHOICE.JUSTOPEN) {
								return fileData;
							} else if(scs.selection == SourceControlSelection.CHOICE.OVRWRTSYM) {
								MessageBox dialog = new MessageBox(RepDevMain.mainShell.shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
								dialog.setText("Source Control");
								dialog.setMessage(
										"Are you sure you want to overwrite "+file.getName()+" in SYM "+file.getSym()+" with the Repository and sync?");
								if (dialog.open() == SWT.YES) {
									// Symitar overwrite SYM
									file.saveFile(sourceControlFileData);
									file.syncRepGen(true);
									fileData = sourceControlFileData;
								}
							} else if(scs.selection == SourceControlSelection.CHOICE.OVRWRTREPO) {
								MessageBox dialog = new MessageBox(RepDevMain.mainShell.shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
								dialog.setText("Source Control");
								dialog.setMessage(
										"Are you sure you want to overwrite "+file.getName()+" in the repository with SYM "+file.getSym()+" and sync?");
								if (dialog.open() == SWT.YES) {
									// Symitar overwrite repository
									sourceControlFile.saveFile(fileData);
									file.syncRepGen(true);
								}
							}
						}
					} else {
						// file does not exist in repository
						MessageBox dialog = new MessageBox(RepDevMain.mainShell.shell,
								SWT.ICON_QUESTION | SWT.YES | SWT.NO);
						dialog.setText("Source Control");
						dialog.setMessage(
								"The RepGen ("+file.getName()+") does not exist in the repository.  Would you like to create a copy and Sync the RepGens?");
						if (dialog.open() == SWT.YES) {
							// Sync the File
							file.syncRepGen(true);
							sourceControlFile.saveFile(fileData);
						}
					}
				} else {
					// repository Directory does not exist
					MessageBox dialog = new MessageBox(RepDevMain.mainShell.shell, SWT.ICON_ERROR | SWT.OK);
					dialog.setText("Source Control");
					dialog.setMessage("Repository Directory is not accessible;  "+sourceControlDir+". RepGens will not be sync'd.");
					dialog.open();
				}
			} else {
				// Not using Source Control, return the file
				return fileData;
			}
		}
		
		return fileData;
	}
	
	public void copyFromSourceControl(SymitarFile file) {
		MessageBox dialog = new MessageBox(RepDevMain.mainShell.shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
		dialog.setText("Source Control");
		dialog.setMessage("Are you sure you want to overwrite " + file.getName() + " in sym " + file.getSym()
				+ " with the copy from the repository?");
		if (dialog.open() == SWT.YES) {
			
			SymitarFile sourceControlFile = getSourceControlFile(file);
			String scFileData = sourceControlFile.getData();
			file.saveFile(scFileData);
			
			dialog = new MessageBox(RepDevMain.mainShell.shell, SWT.ICON_INFORMATION | SWT.OK);
			dialog.setText("Source Control");
			dialog.setMessage("The RepGen ("+file.getName()+") has been copied to sym "+file.getSym()+".");
			dialog.open();
		}
	}
	
	public void compareToProduction(SymitarFile file){
		MessageBox dialog;
		
		if(!RepDevMain.SYMITAR_SESSIONS.get(Config.getLiveSym()).isConnected()) {
			dialog = new MessageBox(RepDevMain.mainShell.shell, SWT.ERROR | SWT.OK);
			dialog.setText("Source Control");
			dialog.setMessage("Please log into sym " + Config.getLiveSym() +" first.");
			dialog.open();
		}else {
			SymitarFile prodFile = new SymitarFile(Config.getLiveSym(), file.getName(),file.getType());
			String fileData = file.getData();
			String prodFileData = prodFile.getData();
			if(prodFileData.contentEquals("")) {
				dialog = new MessageBox(RepDevMain.mainShell.shell, SWT.ERROR | SWT.OK);
				dialog.setText("Source Control");
				dialog.setMessage("There was an error opening the RepGen.\nProduction RepGen may not exist or is blank.");
				dialog.open();
			}else if(fileData.contentEquals("")) {
				dialog = new MessageBox(RepDevMain.mainShell.shell, SWT.ERROR | SWT.OK);
				dialog.setText("Source Control");
				dialog.setMessage("There was an error opening the RepGen.\nRepGen may not exist or is blank.");
				dialog.open();
			}else {
				file.compareMode(true);
				prodFile.compareMode(true);
				RepDevMain.mainShell.compareFiles(file, prodFile);
			}
		}
	}

	public SymitarFile getSourceControlFile(SymitarFile symitarFile) {
		SymitarFile sourceControlFile = new SymitarFile(sourceControlDir, symitarFile.getName(), symitarFile.getType());
		return sourceControlFile;
	}
	
	public boolean fileExist(SymitarFile file) {
		File sfile = new File(file.getPath());
		if (sfile.exists())
			return true;
		else
			return false;
	}
	
	public boolean dirExist() {
		File dir = new File(sourceControlDir);
		if (dir.exists())
			return true;
		else
			return false;
	}
	
	public boolean useSourceControl() {
		return this.useSourceControl;
	}
	
	public String sourceControlDir() {
		return this.sourceControlDir;
	}
	
}
