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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Project creation window
 * @author Jake Poznanski
 *
 */
public class NewProjShell {
	private static NewProjShell me = new NewProjShell();
	private Shell shell;
	private String result;

	private NewProjShell() {
	}

	private void create(Shell parent) {
		FormLayout layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;

		shell = new Shell(parent, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		shell.setText("Create new project");
		shell.setLayout(layout);

		Label questionImage = new Label(shell, SWT.NONE);
		questionImage.setImage(shell.getDisplay().getSystemImage(SWT.ICON_QUESTION));

		Label lbl = new Label(shell, SWT.NONE);
		lbl.setText("What is the name of the project that you want to create?");

		final Text txt = new Text(shell, SWT.BORDER);

		Button add = new Button(shell, SWT.PUSH);
		add.setText("Create &Project");
		add.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = txt.getText();
				shell.dispose();
			}
		});
		shell.setDefaultButton(add);

		Button cancel = new Button(shell, SWT.PUSH);
		cancel.setText("&Cancel");
		cancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});

		FormData frmImg = new FormData();
		frmImg.left = new FormAttachment(0);
		frmImg.top = new FormAttachment(0);
		questionImage.setLayoutData(frmImg);

		FormData frmLbl = new FormData();
		frmLbl.top = new FormAttachment(0);
		frmLbl.left = new FormAttachment(questionImage);
		frmLbl.right = new FormAttachment(100);
		lbl.setLayoutData(frmLbl);

		FormData frmTxt = new FormData();
		frmTxt.top = new FormAttachment(lbl);
		frmTxt.left = new FormAttachment(lbl, 5, SWT.LEFT);
		frmTxt.right = new FormAttachment(100);
		txt.setLayoutData(frmTxt);

		FormData frmOK = new FormData();
		frmOK.top = new FormAttachment(txt);
		frmOK.right = new FormAttachment(cancel);
		frmOK.bottom = new FormAttachment(100);
		add.setLayoutData(frmOK);

		FormData frmCancel = new FormData();
		frmCancel.top = new FormAttachment(txt);
		frmCancel.right = new FormAttachment(100);
		frmCancel.bottom = new FormAttachment(100);
		cancel.setLayoutData(frmCancel);

		result = null;
		shell.pack();
		shell.open();
	}

	// returns -1 on cancel
	public static String askForName(Display display, Shell parent) {
		me.create(parent);

		while (!me.shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return me.result;
	}
}
