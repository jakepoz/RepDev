package com.repdev;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NewProjShell {
	private static NewProjShell me = new NewProjShell();
	private Shell shell;
	private String result;

	private NewProjShell() {
	}

	private void create(Shell parent, int sym) {
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
		lbl.setText("What is the name of the project that you want to create in Sym " + sym + "?");

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
	public static String askForName(Display display, Shell parent, int sym) {
		me.create(parent, sym);

		while (!me.shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return me.result;
	}
}
