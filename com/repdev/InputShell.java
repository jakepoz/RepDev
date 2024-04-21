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
 *   
 *   By Bruce Chang
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.sun.org.apache.xpath.internal.operations.Bool;

public class InputShell {
	private static InputShell me = new InputShell();
	private Shell shell;
	private String result;
	private String defaultValue = "";
	private String title, prompt, defValue;
	private boolean isPassword;

	private InputShell() {
	}

	private void create(Shell parent) {
		FormLayout layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;

		shell = new Shell(parent, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		shell.setText(title);
		shell.setLayout(layout);

		Label lblSYMDesc = new Label(shell, SWT.NONE);
		lblSYMDesc.setText(prompt);

		final Text txt;
		if (isPassword) {
			txt = new Text(shell, SWT.BORDER | SWT.PASSWORD);
		} else {
			txt = new Text(shell, SWT.BORDER);
		}
		txt.setText(defaultValue);

		Button btnOk = new Button(shell, SWT.PUSH);
		btnOk.setText("   OK   ");
		btnOk.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = txt.getText();
				shell.dispose();
			}
		});
		shell.setDefaultButton(btnOk);

		Button btnCancel = new Button(shell, SWT.PUSH);
		btnCancel.setText("&Cancel");
		btnCancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});

		FormData frmLbl = new FormData();
		frmLbl.top = new FormAttachment(10);
		frmLbl.left = new FormAttachment(5);
		lblSYMDesc.setLayoutData(frmLbl);

		FormData frmTxt = new FormData();
		frmTxt.top = new FormAttachment(lblSYMDesc);
		frmTxt.left = new FormAttachment(0);
		frmTxt.right = new FormAttachment(100);
		frmTxt.width = 200;
		txt.setLayoutData(frmTxt);

		FormData frmOK = new FormData();
		frmOK.top = new FormAttachment(txt);
		frmOK.right = new FormAttachment(50);
		frmOK.bottom = new FormAttachment(100);
		btnOk.setLayoutData(frmOK);

		FormData frmCancel = new FormData();
		frmCancel.top = new FormAttachment(txt);
		frmCancel.left = new FormAttachment(50);
		frmCancel.bottom = new FormAttachment(100);
		btnCancel.setLayoutData(frmCancel);

		result = null;
		shell.pack();
		shell.open();
	}

	// returns -1 on cancel
	public static String getInput(Shell parent, String title, String prompt, String defaultValue, boolean isPassword) {
		me.title = title;
		me.prompt = prompt;
		me.defaultValue = defaultValue;
		me.isPassword = isPassword;
		
		me.create(parent);

		Display display = me.shell.getDisplay();
		
		while (!me.shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return me.result;
	}
}
