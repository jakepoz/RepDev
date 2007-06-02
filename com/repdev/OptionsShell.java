package com.repdev;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;

/**
 * Global options window
 * @author Jake Poznanski
 *
 */
public class OptionsShell {
	Shell shell;
	private static OptionsShell me = new OptionsShell();
	private Button telnetRadio, testRadio;
	private Text serverText, tabText;
	private Label serverLabel;

	private void create(Shell parent) {
		FormLayout layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;

		FormData data = new FormData();

		shell = new Shell(parent, SWT.APPLICATION_MODAL | SWT.CLOSE | SWT.TITLE | SWT.RESIZE);
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

		telnetRadio = new Button(serverGroup, SWT.RADIO);
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

		serverLabel = new Label(serverGroup, SWT.NONE);
		serverLabel.setText("Symitar Server IP Address:");

		serverText = new Text(serverGroup, SWT.SINGLE | SWT.BORDER);
		serverText.setText(Config.getServer());

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
				try {
					Config.setTabSize(Integer.valueOf(tabText.getText()));
				} catch (Exception ex) {
					MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					dialog.setText("Input Error");
					dialog.setMessage("Tab Size is incorrect!");
					dialog.open();

					return;
				}

				if (testRadio.getSelection())
					Config.setServer("test");
				else
					Config.setServer(serverText.getText());

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

		tabText = new Text(editorGroup, SWT.BORDER);
		tabText.setText(String.valueOf(Config.getTabSize()));

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
		data.bottom = new FormAttachment(cancel);
		editorGroup.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		telnetRadio.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(telnetRadio);
		data.top = new FormAttachment(0);
		testRadio.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(telnetRadio);
		serverLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(serverLabel);
		data.top = new FormAttachment(telnetRadio);
		data.right = new FormAttachment(100);
		serverText.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		tabLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(tabLabel);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(0);
		tabText.setLayoutData(data);

		data = new FormData();
		data.right = new FormAttachment(100);
		data.bottom = new FormAttachment(100);
		cancel.setLayoutData(data);

		data = new FormData();
		data.bottom = new FormAttachment(100);
		data.right = new FormAttachment(cancel);
		ok.setLayoutData(data);

		redraw();

		shell.setDefaultButton(ok);
		shell.pack();
		shell.open();
	}

	private void redraw() {
		if (telnetRadio.getSelection()) {
			serverText.setEnabled(true);
			serverLabel.setEnabled(true);
		} else if (testRadio.getSelection()) {
			serverText.setEnabled(false);
			serverLabel.setEnabled(false);
			serverText.setText("");
		}

	}

	public static void showOptions(Display display, Shell parent) {
		me.create(parent);

		while (!me.shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

	}
}
