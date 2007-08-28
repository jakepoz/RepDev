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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * Global options window
 * @author Jake Poznanski
 *
 */
public class OptionsShell {
	private Shell shell;
	private static OptionsShell me = new OptionsShell();
	private Button /*telnetRadio, testRadio,*/ devForgetBox;
	private Text serverText,portText;
	private Spinner tabSpinner;
	private Label serverLabel,portLabel;

	private void create(Shell parent) {
		FormLayout layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;

		FormData data = new FormData();

		shell = new Shell(parent, SWT.APPLICATION_MODAL | SWT.CLOSE | SWT.TITLE);
		shell.setText("Global Options");
		shell.setLayout(layout);

		Group serverGroup = new Group(shell, SWT.NONE);
		serverGroup.setText("Symitar Connection Options");
		layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;
		serverGroup.setLayout(layout);
		
		/*telnetRadio = new Button(serverGroup, SWT.RADIO);
		telnetRadio.setText("Direct Symitar Session");
		telnetRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				redraw();
			}
		});

		testRadio = new Button(serverGroup, SWT.RADIO);
		testRadio.setText("Local Symitar Emulation");
		testRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				redraw();
			}
		});

		if (Config.getServer().equals("test")) {
			testRadio.setSelection(true);
		} else
			telnetRadio.setSelection(true);
		*/
		
		serverLabel = new Label(serverGroup, SWT.NONE);
		serverLabel.setText("Symitar Server IP Address:");

		serverText = new Text(serverGroup, SWT.SINGLE | SWT.BORDER);
		serverText.setText(Config.getServer());
		
		portLabel = new Label(serverGroup, SWT.NONE);
		portLabel.setText("Port (usually 23)");
		
		portText = new Text(serverGroup, SWT.SINGLE | SWT.BORDER);
		portText.setText(""+Config.getPort());
			
		
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

				/*if (testRadio.getSelection())
					Config.setServer("test");
				else {*/
					Config.setServer(serverText.getText());
					Config.setPort(Integer.parseInt(portText.getText()));
				//}

				RepDevMain.FORGET_PASS_ON_EXIT = devForgetBox.getSelection();
				
				shell.close();
			}
		});

		Group editorGroup = new Group(shell, SWT.NONE);
		editorGroup.setText("Editor Options");
		layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;
		editorGroup.setLayout(layout);

		Label tabLabel = new Label(editorGroup, SWT.NONE);
		tabLabel.setText("Tab Width (0 for Regular Tabs):");

		tabSpinner = new Spinner(editorGroup, SWT.BORDER);
		tabSpinner.setMaximum(99);
		tabSpinner.setMinimum(0);
		tabSpinner.setSelection(Config.getTabSize());
		
		/// Developer Options (dev's only :D)
		Group devGroup = new Group(shell, SWT.NONE);
		devGroup.setText("Developer Options");
		layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;
		devGroup.setLayout(layout);
		
		Label devNotice = new Label(devGroup, SWT.NONE);
		devNotice.setText("Developer mode enabled");
				
		devForgetBox = new Button(devGroup, SWT.CHECK);
		devForgetBox.setText("Forget Passwords on exit");
		devForgetBox.setSelection(RepDevMain.FORGET_PASS_ON_EXIT);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(0);
		data.bottom = new FormAttachment(editorGroup);
		serverGroup.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(serverGroup);
		if(!RepDevMain.DEVELOPER) data.bottom = new FormAttachment(cancel);
		editorGroup.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(editorGroup);
		data.bottom = new FormAttachment(cancel);
		devGroup.setLayoutData(data);

		if( !RepDevMain.DEVELOPER ) devGroup.setVisible(false);
		
		// Connection options
		/*data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		telnetRadio.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(telnetRadio);
		data.top = new FormAttachment(0);
		testRadio.setLayoutData(data);*/

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		data.width = 130;
		serverLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(serverLabel);
		data.top = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.width = 120;
		serverText.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(serverText);
		data.width = 130;
		portLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(portLabel);
		data.top = new FormAttachment(serverText);
		data.right = new FormAttachment(100);
		data.width = 120;
		portText.setLayoutData(data);


		// Editor options
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		tabLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(tabLabel);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(0);
		tabSpinner.setLayoutData(data);
		
		// Developer Options
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		devNotice.setLayoutData(data);
				
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(devNotice);
		devForgetBox.setLayoutData(data);
		
		// Ok/Cancel buttons
		data = new FormData();
		data.right = new FormAttachment(100);
		data.bottom = new FormAttachment(100);
		cancel.setLayoutData(data);

		data = new FormData();
		data.bottom = new FormAttachment(100);
		data.right = new FormAttachment(cancel);
		ok.setLayoutData(data);

		//redraw();

		shell.setDefaultButton(ok);
		shell.pack();
		shell.open();
	}

	/*private void redraw() {
		if (telnetRadio.getSelection()) {
			serverText.setEnabled(true);
			serverLabel.setEnabled(true);
		} else if (testRadio.getSelection()) {
			serverText.setEnabled(false);
			serverLabel.setEnabled(false);
			serverText.setText("");
		}

	}*/

	public static void showOptions(Display display, Shell parent) {
		me.create(parent);

		while (!me.shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

	}
}
