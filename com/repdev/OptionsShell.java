package com.repdev;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * OptionsShell version 2
 * @author Ryan Schultz
 * 
 *  I created this because the current (previous?) dialog has gotten crowded
 *  and has outgrown its original simple functionality.  This new one features
 *  a tabbed view to allow for more options.
 */

public class OptionsShell {
	private Shell shell;
	private static OptionsShell me = new OptionsShell();
	
	// Tabs and their contents
	private TabFolder tabs;
	private Composite serverOptions, editorOptions, developerOptions;
	
	// Controls
	private Spinner tabSpinner;
	private Combo styleCombo, hour, minute;
	private Label varsLabel, serverLabel, portLabel;
	private Text  serverText, portText;
	private Button varsButton, neverTerm, devForgetBox, backupEnable;
	
	public static void show(Shell parent) {
		me.create(parent);		
		me.shell.open();
		
		Display display = me.shell.getDisplay();		
		while (!me.shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
	
	private void create(Shell parent) {
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.APPLICATION_MODAL );
		shell.setText("Settings");
		shell.setMinimumSize(400, 300);	
		tabs = new TabFolder(shell, SWT.NONE);
		
		// Add tabs/controls
		createServerOptions();
		createEditorOptions();
		if( RepDevMain.DEVELOPER )
			createDevOptions();	
		
		Button cancel = new Button(shell, SWT.PUSH);
		cancel.setText("Cancel");
		cancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});

		Button ok = new Button(shell, SWT.PUSH);
		ok.setText("Save Settings");
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Config.setTabSize(tabSpinner.getSelection());
				Config.setListUnusedVars(varsButton.getSelection());
				Config.setTerminateHour(hour.getSelectionIndex()+1);
				Config.setTerminateMinute(minute.getSelectionIndex()*10);
				Config.setNeverTerminate(neverTerm.getSelection());

				/*if (testRadio.getSelection())
					Config.setServer("test");
				else {*/
					Config.setServer(serverText.getText());
					Config.setPort(Integer.parseInt(portText.getText()));
				//}

				RepDevMain.FORGET_PASS_ON_EXIT = devForgetBox.getSelection();
				
				if(styleCombo.getSelectionIndex() > -1) {
				    Config.setStyle(styleCombo.getItem(styleCombo.getSelectionIndex()));
				    SyntaxHighlighter.loadStyle(Config.getStyle());
				}
				
				// Project file backup
				Config.setBackupProjectFile( backupEnable.getSelection() );				
				
				shell.close();
			}
		});
		
		// Layout the shell
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 5;
		layout.verticalSpacing   = 5;
		layout.numColumns = 2;
		shell.setLayout(layout);
		
		GridData data;
		data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		tabs.setLayoutData(data);
		
		data = new GridData( SWT.END, SWT.FILL, false, false, 1, 1 );
		ok.setLayoutData(data);
		
		data = new GridData( SWT.END, SWT.FILL, true, false, 1, 1 );
		cancel.setLayoutData(data);
		
		shell.setDefaultButton(ok);
		shell.pack();
	}
	
	private void createServerOptions() {
		serverOptions = new Composite(tabs, SWT.NONE);
		
		TabItem serverOptionsTab = new TabItem(tabs, SWT.NONE);
		serverOptionsTab.setText("Server Options");
		serverOptionsTab.setControl(serverOptions);
		
		Group serverGroup = new Group(serverOptions, SWT.NONE);
		serverGroup.setText("Symitar Connection Options");
		FormLayout layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;
		serverGroup.setLayout(layout);
		
		layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;
		serverOptions.setLayout(layout);
		
		serverLabel = new Label(serverGroup, SWT.NONE);
		serverLabel.setText("Symitar Server IP Address:");

		serverText = new Text(serverGroup, SWT.SINGLE | SWT.BORDER);
		serverText.setText(Config.getServer());
		
		portLabel = new Label(serverGroup, SWT.NONE);
		portLabel.setText("Port (usually 23)");
		
		portText = new Text(serverGroup, SWT.SINGLE | SWT.BORDER);
		portText.setText(""+Config.getPort());
		
		Group keepAliveGroup = new Group(serverOptions,SWT.NONE);
		keepAliveGroup.setText("Keep Alive Options (Log out Sym Required)");
		layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		keepAliveGroup.setLayout(layout);
		
		Label neverTermLabel = new Label(keepAliveGroup, SWT.NONE);
		neverTermLabel.setText("Never Terminate");
		
		neverTerm = new Button(keepAliveGroup, SWT.CHECK);
		neverTerm.setSelection(Config.getNeverTerminate());
		neverTerm.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(neverTerm.getSelection()){
					hour.setEnabled(false);
					minute.setEnabled(false);
				}
				else{
					hour.setEnabled(true);
					minute.setEnabled(true);					
				}
			}
		});
		
		Label keepAliveLabel = new Label(keepAliveGroup,  SWT.NONE);
		keepAliveLabel.setText("Terminate Time (HH:MM)");
		hour = new Combo(keepAliveGroup, SWT.READ_ONLY);
		for(int i=0 ; i<24 ; i++){
			hour.add(((i+1) < 10 ? "0" : "")+Integer.toString(i+1),i);
		}
		hour.select(Config.getTerminateHour()-1);
		Label colon = new Label(keepAliveGroup, SWT.NONE);
		colon.setText(" : ");
		minute = new Combo(keepAliveGroup,  SWT.READ_ONLY);
		for(int i=0 ; i<6 ; i++){
			minute.add(((i * 10) < 10 ? "0" : "")+Integer.toString(i * 10),i);
		}
		minute.select(Config.getTerminateMinute()/10);
		
		if(neverTerm.getSelection()){
			hour.setEnabled(false);
			minute.setEnabled(false);
		}
		else{
			hour.setEnabled(true);
			minute.setEnabled(true);					
		}
		
		FormData data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(0);
		data.bottom = new FormAttachment(keepAliveGroup);
		serverGroup.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(serverGroup);
		keepAliveGroup.setLayoutData(data);
		
		// align the controls for the server group:
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		data.width = 140;
		serverLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(serverLabel);
		data.top = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.width = 140;
		serverText.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(serverText);
		data.width = 140;
		portLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(portLabel);
		data.top = new FormAttachment(serverText);
		data.right = new FormAttachment(100);
		data.width = 140;
		portText.setLayoutData(data);
		
		// align controls for the keepalive group:
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		data.width = 160;
		neverTermLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(neverTermLabel);
		data.top = new FormAttachment(0);
		neverTerm.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(neverTerm,4);
		data.width = 160;
		keepAliveLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(keepAliveLabel);
		data.top = new FormAttachment(neverTerm,4);
		data.width = 10;
		hour.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(hour);
		data.top = new FormAttachment(neverTerm,4);
		colon.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(colon);
		data.top = new FormAttachment(neverTerm,4);
		data.width = 10;
		minute.setLayoutData(data);
				
	}
	
	private void createEditorOptions() {
		editorOptions = new Composite(tabs,SWT.NONE);
		
		TabItem editorOptionsTab = new TabItem(tabs, SWT.NONE);
		editorOptionsTab.setText("Editor Options");
		editorOptionsTab.setControl(editorOptions);
		
		Group editorGroup = new Group(editorOptions, SWT.NONE);
		editorGroup.setText("Editor Options");
		FormLayout layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;
		editorGroup.setLayout(layout);
		editorOptions.setLayout(layout);

		Label tabLabel = new Label(editorGroup, SWT.NONE);
		tabLabel.setText("Tab Width (0 for Regular Tabs):");

		tabSpinner = new Spinner(editorGroup, SWT.BORDER);
		tabSpinner.setMaximum(99);
		tabSpinner.setMinimum(0);
		tabSpinner.setSelection(Config.getTabSize());
		
		Label styleLabel = new Label(editorGroup, SWT.NONE);
		styleLabel.setText("Style (requires restart)");
		
		styleCombo = new Combo(editorGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		File dir = new File("styles\\");
		if( dir.isDirectory() ) {
		    for( String file: dir.list() ) {
			if( file.endsWith(".xml") ) styleCombo.add(file.substring(0, file.length()-4));
		    }
		}
		
		if( Config.getStyle() != null ) 
		    styleCombo.setText(Config.getStyle());
		
		varsLabel = new Label(editorGroup, SWT.NONE);
		varsLabel.setText("List unused variables");
		
		varsButton = new Button(editorGroup, SWT.CHECK);
		varsButton.setSelection(Config.getListUnusedVars());
		
		FormData data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(0);
		//data.bottom = new FormAttachment(0);
		editorGroup.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		data.width = 160;
		tabLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(tabLabel);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(0);
		tabSpinner.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(tabSpinner);
		data.width = 160;
		styleLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(styleLabel);
		data.top = new FormAttachment(tabSpinner);
		data.right = new FormAttachment(100);
		styleCombo.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(styleCombo);
		data.width = 160;
		varsLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(varsLabel);
		data.top = new FormAttachment(styleCombo);
		data.right = new FormAttachment(100);
		varsButton.setLayoutData(data);		
	}
	
	private void createDevOptions() {
		developerOptions = new Composite(tabs, SWT.NONE);
		TabItem devOptionsTab = new TabItem(tabs, SWT.NONE);
		devOptionsTab.setText("Developer");
		devOptionsTab.setControl(developerOptions);
		
		Group devGroup = new Group(developerOptions, SWT.NONE);
		devGroup.setText("Developer Options");
		FormLayout layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;
		devGroup.setLayout(layout);
		developerOptions.setLayout(layout);
		
		Label devNotice = new Label(devGroup, SWT.NONE);
		devNotice.setText("Developer mode enabled");
				
		devForgetBox = new Button(devGroup, SWT.CHECK);
		devForgetBox.setText("Forget Passwords on exit");
		devForgetBox.setSelection(RepDevMain.FORGET_PASS_ON_EXIT);
		
		Group devBackup = new Group(developerOptions, SWT.NONE);
		devBackup.setText("Backup Options");
		devBackup.setLayout(new GridLayout(2, false));
		
		backupEnable = new Button(devBackup, SWT.CHECK);
		backupEnable.setText("Enable project file backup");
		backupEnable.setSelection(Config.getBackupProjectFiles());
				
		FormData data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(0);
		devGroup.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		devNotice.setLayoutData(data);
				
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(devNotice);
		devForgetBox.setLayoutData(data);
		
		// Backup options under normal dev options
		data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(devGroup);
		devBackup.setLayoutData(data);
	}
}
