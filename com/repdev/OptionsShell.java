package com.repdev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.IllegalArgumentException;
import java.util.regex.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
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
	private Composite serverOptions, editorOptions, documentationOptions, developerOptions;
	
	// Controls
	private Spinner tabSpinner;
	private Combo styleCombo, hour, minute;
	private Label varsLabel, serverLabel, portLabel, useSSOLabel, errChkPrefixLabel, errChkSuffixLabel, nameInTitleLabel, hostInTitleLabel, viewLineNumbersLabel, liveSYMLabel, liveSYMColorLabel, useSourceControlLabel, sourceControlDirLabel;
	private Text  serverText, portText, errCheckPrefix, errCheckSuffix, liveSYMText, liveSYMColorText, sourceControlDir;
	private Button varsButton, neverTerm, useSSO, devForgetBox, backupEnable, fileNameInTitle, hostInTitle, viewLineNumbers, useSourceControl;
	private String ssoPass = "";

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
		shell.setImage(RepDevMain.smallOptionsImage);
		shell.setMinimumSize(400, 300);	
		tabs = new TabFolder(shell, SWT.NONE);
		
		// Add tabs/controls
		createServerOptions();
		createEditorOptions();
		createDocumentationOptions();
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
				Config.setNoErrorCheckPrefix(errCheckPrefix.getText());
				Config.setNoErrorCheckSuffix(errCheckSuffix.getText());
				Config.setFileNameInTitle(fileNameInTitle.getSelection());
				Config.setHostNameInTitle(hostInTitle.getSelection());
				Config.setViewLineNumbers(viewLineNumbers.getSelection());
				Config.setLiveSym(Integer.parseInt(liveSYMText.getText()));
				Config.setLiveSymColor(liveSYMColorText.getText());
				Config.setUseSourceControl(useSourceControl.getSelection());
				Config.setSourceControlDir(sourceControlDir.getText());
				if (!ssoPass.contentEquals("")) {
					Config.setPasswordValidator(ssoPass);
				}

				/*if (testRadio.getSelection())
					Config.setServer("test");
				else {*/
					Config.setServer(serverText.getText());
					Config.setPort(Integer.parseInt(portText.getText()));
				//}

				if( RepDevMain.DEVELOPER )
					RepDevMain.FORGET_PASS_ON_EXIT = devForgetBox.getSelection();
				
				if(styleCombo.getSelectionIndex() > -1) {
				    Config.setStyle(styleCombo.getItem(styleCombo.getSelectionIndex()));
				    SyntaxHighlighter.loadStyle(Config.getStyle());
				}
				
				// Project file backup

				if( RepDevMain.DEVELOPER )
					Config.setBackupProjectFile( backupEnable.getSelection() );				

				if(validateData("[a-fA-F0-9]{6}", liveSYMColorText.getText(), "Incorrect value entered for the Live SYM background color. Please specify 6 hexadecimal only.  Example FFD7E4")) {
					shell.close();
				} else {
					Config.setLiveSymColor("FFD7E4");
				}
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
		portLabel.setText("Port (22 - SSH , 23 - Telnet)");
		
		portText = new Text(serverGroup, SWT.SINGLE | SWT.BORDER);
		portText.setText(""+Config.getPort());
		
		useSSOLabel = new Label(serverGroup, SWT.NONE);
		useSSOLabel.setText("Use Single Sign-On");
		
		useSSO = new Button(serverGroup, SWT.CHECK);
		useSSO.setSelection(Config.useSSO());
		useSSO.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(useSSO.getSelection()){
					// SET MASTER PASSWROD
					String password = RepDev_SSO.getRepDevPassword(shell);
					if (password.contentEquals("")) {
						useSSO.setSelection(false);
						ssoPass = "";
					} else {
						ssoPass = password;
						RepDevMain.MASTER_PASSWORD_HASH = RepDev_SSO.md5Hash(ssoPass);
						Config.setPasswordValidator(ssoPass);
						//MessageBox dialog = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
						//dialog.setText("Settings");
						//dialog.setMessage("Please click \"Save Settings\".  Exit and relaunch RepDev to log in.");
						//dialog.open();
					}
				}
				else{
					// REMOVE MASTER PASSWORD AND CLEAR SYM PASSWORDS
					MessageBox dialog = new MessageBox(shell, SWT.OK | SWT.CANCEL | SWT.ICON_WARNING);
					dialog.setText("Single Sign-On");
					dialog.setMessage("All of the cached passwords will be deleted IMMEDIATELY ! ! !");
					int selection = dialog.open();
					
					if (selection == SWT.OK) {
						System.out.println("ALL Passwords deleted");
						
						for (int sym : RepDevMain.SESSION_INFO.keySet()) {
							RepDevMain.SESSION_INFO.get(sym).clearCredential();
						}
						
						ssoPass = "";
						Config.setPasswordValidator("");
						RepDevMain.MASTER_PASSWORD_HASH = null;
					} else {
						System.out.println("ALL Passwords delete cancelled");
						useSSO.setSelection(true);
					}
				}
			}
		});
		
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
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(portLabel);
		data.width = 140;
		useSSOLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(useSSOLabel);
		data.top = new FormAttachment(portLabel);
		data.height = 25;
		useSSO.setLayoutData(data);
		
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
		
		liveSYMLabel = new Label(editorGroup, SWT.NONE);
		liveSYMLabel.setText("Live SYM Number");

		liveSYMText = new Text(editorGroup, SWT.SINGLE | SWT.BORDER);
		try{
			liveSYMText.setText(Integer.toString(Config.getLiveSym()));
		} catch (IllegalArgumentException e) {
			liveSYMText.setText("1999");
		}
		liveSYMColorLabel = new Label(editorGroup, SWT.NONE);
		liveSYMColorLabel.setText("Live SYM background Color");

		liveSYMColorText = new Text(editorGroup, SWT.SINGLE | SWT.BORDER);
		try{
			liveSYMColorText.setText(Config.getLiveSymColor());
		} catch (IllegalArgumentException e){
			liveSYMColorText.setText("FFD7E4");
			liveSYMText.setText("1999");
		}
		
		useSourceControlLabel = new Label(editorGroup, SWT.NONE);
		useSourceControlLabel.setText("Use Source Control");
		
		useSourceControl = new Button(editorGroup, SWT.CHECK);
		useSourceControl.setSelection(Config.getUseSourceControl());

		
		sourceControlDirLabel = new Label(editorGroup, SWT.NONE);
		sourceControlDirLabel.setText("Repository Dir");

		sourceControlDir = new Text(editorGroup, SWT.SINGLE | SWT.BORDER);
		try{
			sourceControlDir.setText(Config.getSourceControlDir());
		} catch (IllegalArgumentException e){
			sourceControlDir.setText("");
		}

		
		
		varsLabel = new Label(editorGroup, SWT.NONE);
		varsLabel.setText("List unused variables");
		
		varsButton = new Button(editorGroup, SWT.CHECK);
		varsButton.setSelection(Config.getListUnusedVars());

		nameInTitleLabel = new Label(editorGroup, SWT.NONE);
		nameInTitleLabel.setText("Display file name in main title");

		fileNameInTitle = new Button(editorGroup, SWT.CHECK);
		fileNameInTitle.setSelection(Config.getFileNameInTitle());

		hostInTitleLabel = new Label(editorGroup, SWT.NONE);
		hostInTitleLabel.setText("Display host name in main title");
		hostInTitle = new Button(editorGroup, SWT.CHECK);
		hostInTitle.setSelection((Config.getHostNameInTitle()));

		viewLineNumbersLabel = new Label(editorGroup, SWT.NONE);
		viewLineNumbersLabel.setText("Display line numbers");
		viewLineNumbers = new Button(editorGroup, SWT.CHECK);
		viewLineNumbers.setSelection((Config.getViewLineNumbers()));

		Group noErrorCheckGroup = new Group(editorOptions,SWT.NONE);
		noErrorCheckGroup.setText("No Error Check for these Files");
		layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;
		noErrorCheckGroup.setLayout(layout);

		errChkPrefixLabel = new Label(noErrorCheckGroup,SWT.NONE);
		errChkSuffixLabel = new Label(noErrorCheckGroup,SWT.NONE);
		errCheckPrefix = new Text(noErrorCheckGroup, SWT.SINGLE | SWT.BORDER);
		errCheckSuffix = new Text(noErrorCheckGroup, SWT.SINGLE | SWT.BORDER);
		errChkPrefixLabel.setText("Prefix");
		errChkSuffixLabel.setText("Suffix");
		// This will prevent a crash because of new Options.  Probably can
		// remove the try and catch section in subsequent release.
		try{
			errCheckPrefix.setText(Config.getNoErrorCheckPrefix());
			errCheckSuffix.setText(Config.getNoErrorCheckSuffix());
		} catch (IllegalArgumentException e){
			Config.setNoErrorCheckPrefix("INC.");
			Config.setNoErrorCheckSuffix(".DEF,.SET,.PRO,.INC");
			errCheckPrefix.setText("INC.");
			errCheckSuffix.setText(".DEF,.SET,.PRO,.INC");
			Config.setFileNameInTitle(true);
			Config.setHostNameInTitle(true);
			fileNameInTitle.setSelection(true);
			hostInTitle.setSelection(true);
			viewLineNumbers.setSelection(true);
			RepDevMain.saveSettings();
		}
		FormData data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(0);
		//data.bottom = new FormAttachment(0);
		editorGroup.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		data.width = 163;
		tabLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(tabLabel);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(0);
		tabSpinner.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(tabSpinner);
		data.width = 163;
		styleLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(styleLabel);
		data.top = new FormAttachment(tabSpinner);
		data.right = new FormAttachment(100);
		styleCombo.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(styleCombo);
		data.width = 163;
		liveSYMLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(liveSYMLabel);
		data.top = new FormAttachment(styleCombo);
		data.right = new FormAttachment(100);
		liveSYMText.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(liveSYMText);
		data.width = 163;
		liveSYMColorLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(liveSYMColorLabel);
		data.top = new FormAttachment(liveSYMText);
		data.right = new FormAttachment(100);
		liveSYMColorText.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(liveSYMColorText);
		data.width = 163;
		useSourceControlLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(useSourceControlLabel);
		data.top = new FormAttachment(liveSYMColorText);
		data.right = new FormAttachment(100);
		useSourceControl.setLayoutData(data);


		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(useSourceControl);
		data.width = 163;
		sourceControlDirLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(sourceControlDirLabel);
		data.top = new FormAttachment(useSourceControl);
		data.right = new FormAttachment(100);
		sourceControlDir.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(sourceControlDir);
		data.width = 163;
		varsLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(varsLabel);
		data.top = new FormAttachment(sourceControlDir);
		data.right = new FormAttachment(100);
		varsButton.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(varsButton);
		data.width = 163;
		nameInTitleLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(nameInTitleLabel);
		data.top = new FormAttachment(varsButton);
		data.right = new FormAttachment(100);
		fileNameInTitle.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(fileNameInTitle);
		data.width = 163;
		hostInTitleLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(hostInTitleLabel);
		data.top = new FormAttachment(fileNameInTitle);
		data.right = new FormAttachment(100);
		hostInTitle.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(hostInTitle);
		data.width = 163;
		viewLineNumbersLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(viewLineNumbersLabel);
		data.top = new FormAttachment(hostInTitle);
		data.right = new FormAttachment(100);
		viewLineNumbers.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(editorGroup);
		//data.bottom = new FormAttachment(0);
		noErrorCheckGroup.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		//data.right = new FormAttachment(100);
		data.width = 40;
		errChkPrefixLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(errChkPrefixLabel);
		data.top = new FormAttachment(0);
		data.right = new FormAttachment(100);
		errCheckPrefix.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(errChkPrefixLabel,4);
		//data.right = new FormAttachment(100);
		data.width = 40;
		errChkSuffixLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(errChkSuffixLabel);
		data.top = new FormAttachment(errChkPrefixLabel,4);
		data.right = new FormAttachment(100);
		errCheckSuffix.setLayoutData(data);
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
	
	private void createDocumentationOptions() {
		documentationOptions = new Composite(tabs, SWT.NONE);
		TabItem docOptionsTab = new TabItem(tabs, SWT.NONE);
		docOptionsTab.setText("Documentation");
		docOptionsTab.setControl(documentationOptions);
		
		GridLayout layout = new GridLayout(4, false);
		layout.marginBottom = layout.marginTop = layout.marginLeft = layout.marginRight = 5;
		documentationOptions.setLayout(layout);
		
		Group docGroup = new Group(documentationOptions, SWT.NONE);
		docGroup.setText("Add Item");
		
		layout = new GridLayout(3,false);
		docGroup.setLayout(layout);
		
		Label nameLabel = new Label( docGroup, SWT.NONE );
		nameLabel.setText("Name");		
		final Text name = new Text( docGroup, SWT.SINGLE | SWT.BORDER );
		
		Label locLabel = new Label( docGroup, SWT.NONE );
		locLabel.setText("Location");
		final Text location = new Text( docGroup, SWT.SINGLE | SWT.BORDER);
		
		Button browse = new Button( docGroup, SWT.PUSH );
		browse.setText("Browse");
		browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				org.eclipse.swt.widgets.FileDialog dialog = new org.eclipse.swt.widgets.FileDialog(shell,SWT.OPEN);
				String fn = dialog.open();
				if( fn != null ) location.setText(fn);
			}			
		});
				
		final List items = new List( documentationOptions, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL );
		
		// populate items:
		File docsFile = new File("helpmenu.conf");
		try {
			if( !docsFile.exists() )
				docsFile.createNewFile();
			BufferedReader docsReader = new BufferedReader(new FileReader(docsFile));
			String line;
			while( (line = docsReader.readLine()) != null ) {
				if( line.equals("----") ) {
					items.add("----");
					continue;
				}				
				String data[] = line.split("=");
				if( data.length != 2 ) continue; // ignore bad lines.
				items.add(data[0].trim());
				items.setData(data[0].trim(),data[1].trim());
			}			
		} catch( Exception e ) {
			e.printStackTrace();
		}
		
		items.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if( items.getSelectionIndex() == -1 )
					return;
				
				name.setText(items.getSelection()[0]);
				if( items.getSelection()[0].equals("----")) {
					location.setText("");
					return;
				}
				location.setText((String)items.getData(items.getSelection()[0]));
			}			
		});
		
		Composite upDownGroup = new Composite(documentationOptions, SWT.NONE);
		upDownGroup.setLayout(new GridLayout());
		Button moveUp = new Button(upDownGroup, SWT.ARROW | SWT.UP);
		Button moveDn = new Button(upDownGroup, SWT.ARROW | SWT.DOWN);
		
		moveUp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = items.getSelectionIndex();
				if( index < 1 ) return;
				
				String name = items.getItem(index);
				//String loc  = (String)items.getData(name);	// not actually used				
				items.remove(index);
				items.add(name, index-1);
				items.setSelection(index-1);
			}	
		});
		
		moveDn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = items.getSelectionIndex();
				if( index >= items.getItemCount()-1 ) return;
				if( index == -1 ) return;
				
				String name = items.getItem(index);
				//String loc  = (String)items.getData(name);	// not actually used				
				items.remove(index);
				items.add(name, index+1);
				items.setSelection(index+1);
			}	
		});
				
		Composite addRemGroup = new Composite(documentationOptions, SWT.NONE);
		addRemGroup.setLayout(new GridLayout(2, false));
		Button addItem = new Button(addRemGroup, SWT.PUSH);
		addItem.setText("Add");
		addItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if( name.getText().trim().equals("") || location.getText().trim().equals("" ))
					return;
				
				int index = items.getSelectionIndex();
				if( index == -1 ) index = items.getItemCount();
				items.add(name.getText(),index);
				items.setData(name.getText(), location.getText());
				
				name.setText("");
				location.setText("");
			}
		});
		
		Button remItem = new Button(addRemGroup, SWT.PUSH);
		remItem.setText("Remove");
		remItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(items.getSelectionIndex() == -1) return;
				
				items.setData(items.getSelection()[0], null);
				items.remove(items.getSelectionIndex());
				
				name.setText("");
				location.setText("");				
			}
		});
		
		addRemGroup.pack();
		
		Button save = new Button(documentationOptions, SWT.PUSH );
		save.setText("Save");
		save.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					FileWriter newMenu = new FileWriter("helpmenu.conf");
					PrintWriter file = new PrintWriter(newMenu);				
					for( String item: items.getItems() ) {
						if( item.equals("----") ) {
							file.println("----");
						} else {
							String data = (String)items.getData(item);
							if( data != null )
								file.println(item + "     =  " + data);
						}
					}			
					file.flush();
					file.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				} finally {
					RepDevMain.mainShell.createMenuDefault();
				}
			}	
		});
		
		// Do the layout/grid data:
		docGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		name.setLayoutData(new GridData(SWT.FILL,SWT.CENTER, true, false, 2, 1));
		location.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		browse.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER, false, false));
		
		items.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 2));
		upDownGroup.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 2));
		addRemGroup.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true, 3, 1));
		save.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		
		documentationOptions.pack();
		
	}
	
	private boolean validateData(String regExp, String compare, String msg) {
		boolean isMatch = Pattern.matches(regExp, compare);
		
		if(!isMatch){
			if(msg.length()!=0) {
				MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				dialog.setMessage(msg);
				dialog.setText("Input Error");
				dialog.open();
			}
			
			return false;
		} else {
			return true;
		}
	}
	

}
