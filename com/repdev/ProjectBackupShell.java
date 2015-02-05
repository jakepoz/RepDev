package com.repdev;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ProjectBackupShell {
	private Shell shell;
	private static ProjectBackupShell me;
	
	private ProjectBackupShell() {
	}
	
	public static void open() {
		me = new ProjectBackupShell();
		me.create();
		
		while( !me.shell.isDisposed() )
			if( !me.shell.getDisplay().readAndDispatch() )
				me.shell.getDisplay().sleep();
	}
	
	private void create() {
		shell = new Shell(SWT.APPLICATION_MODAL | SWT.CLOSE );
		shell.setText("Project File Restore/Replace");
		shell.setMinimumSize(450, 250);
		
		GridLayout layout = new GridLayout(1,false);
		shell.setLayout(layout);
		layout.horizontalSpacing = 5;
		layout.verticalSpacing = 5;
		layout.makeColumnsEqualWidth = true;
		
		// Groups
		Group restoreGroup = new Group(shell, SWT.NONE);
		restoreGroup.setText("Restore/Replace");
		restoreGroup.setLayout(new GridLayout(2,false));
		
		Group statusGroup = new Group(shell, SWT.NONE);
		statusGroup.setText("Status");
		statusGroup.setLayout(new GridLayout(1,false));
		
		// Status box
		final ProgressBar progress = new ProgressBar(statusGroup, SWT.NONE);
		progress.setMaximum(100);
		progress.setMinimum(0);
		progress.setSelection(0);
		final Text status = new Text( statusGroup, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL );		
		
		// Restore box
		final Text file = new Text(restoreGroup, SWT.SINGLE | SWT.BORDER );
		Button browse = new Button(restoreGroup, SWT.PUSH);
		browse.setText("Browse...");
			
		final Combo symCombo = new Combo(restoreGroup, SWT.DROP_DOWN);		
		for( int sym: Config.getSyms() ) {
			symCombo.add("Sym " + sym);			
		}
		
		Button doit = new Button(restoreGroup, SWT.PUSH );
		doit.setText("Replace");
		
		browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				org.eclipse.swt.widgets.FileDialog f = new org.eclipse.swt.widgets.FileDialog(shell, SWT.OPEN);
				String fn = f.open();
				if( fn != null )
					file.setText(fn);
			}
		});
		
		doit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Oh my god, error checking like this sucks.  I am tempted to make an
				// "easy error dialog" static method... Imagine...
				//   ErrorBox.open("Error: no file specified", "You must specify a file to continue");
				// that would be so much nicer than this crappy 7 line method of doing it.
				if( file.getText() == null || !(new File(file.getText())).exists() ) {
					MessageBox error = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
					error.setText("File Error");
					error.setMessage("You must specify a file that exists.");
					error.open();
					return;
				}
				
				if( symCombo.getSelectionIndex() == -1 ) {
					MessageBox error = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
					error.setText("Select a sym");
					error.setMessage("You must select a sym");
					error.open();
					return;
				}
				
				int sym = Integer.parseInt( symCombo.getItem(symCombo.getSelectionIndex()).substring(4) );
				if( RepDevMain.SYMITAR_SESSIONS.get(sym).isConnected() ) {
					MessageBox error = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
					error.setText("Not logged out");
					error.setMessage("You must log out of the sym that you want to restore your project file in");
					error.open();
					return;
				}
								
				progress.setSelection(10);				
				status.setText(status.getText() + "Logging in to sym " + sym + "\r\n" );				
				
				int err = SymLoginShell.symLogin(shell.getDisplay(), shell, sym);
				if( err != -1 ) {
					progress.setSelection(25);
					
					SymitarSession session = RepDevMain.SYMITAR_SESSIONS.get(sym);
					SymitarFile pf = new SymitarFile(sym,"repdev." + session.getUserNum(true) + "projects", FileType.REPGEN);

					status.setText(status.getText() + "Replacing Project file for " + session.getUserNum(true) +
							" on sym " + sym + "...\r\n");					
					
					try {
						progress.setSelection(40);
						File f = new File(file.getText());
						FileReader project = new FileReader(f);
						char[] data = new char[(int)f.length()];
						project.read(data);
						progress.setSelection(50);
						SessionError se = session.saveFile(pf, new String(data));
						progress.setSelection(80);
						status.setText(status.getText() 
								+ "Finished, errors: " + se.toString() + "\r\n" );
						
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					session.disconnect();
					progress.setSelection(100);
				}				
			}			
		});
		
		// restoreGroup's layout data
		GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
		restoreGroup.setLayoutData(data);
		
		file.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,false));
		symCombo.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,false));
		browse.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,false));
		doit.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,false));
		
		// inside of statusGroup...
		data = new GridData(SWT.FILL, SWT.TOP, true, false );
		progress.setLayoutData(data);
		
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		status.setLayoutData(data);		
				
		// For the status group's layout stuff...
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		statusGroup.setLayoutData(data);
				
		restoreGroup.pack();
		statusGroup.pack();		
		shell.pack();
		
		shell.open();		
	}
	
	
}

