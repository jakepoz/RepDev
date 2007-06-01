package com.repdev;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class RemProjShell {
	private static RemProjShell me = new RemProjShell();
	private Shell shell;
	private Result result = Result.CANCEL;

	private RemProjShell() {
	}

	public enum Result {
		OK_DELETE, OK_KEEP, CANCEL
	};

	private void create(Shell parent, int sym, Project project) {
		FormLayout layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;

		shell = new Shell(parent, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		shell.setText("Remove Project");
		shell.setLayout(layout);

		Label questionImage = new Label(shell, SWT.NONE);
		questionImage.setImage(shell.getDisplay().getSystemImage(SWT.ICON_QUESTION));

		Label lbl = new Label(shell, SWT.NONE);
		lbl.setText("Are you sure you want to delete project '" + project.getName() + "' from Sym " + sym + "?");

		final Button radioDelete = new Button(shell, SWT.RADIO);
		radioDelete.setText("Also delete the files in the project from the server");

		final Button radioKeep = new Button(shell, SWT.RADIO);
		radioKeep.setText("Do not delete the files");
		radioKeep.setSelection(true);

		Button add = new Button(shell, SWT.PUSH);
		add.setText("Delete &Project");
		add.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (radioKeep.getSelection())
					result = Result.OK_KEEP;
				else if (radioDelete.getSelection())
					result = Result.OK_DELETE;

				shell.dispose();
			}
		});

		Button cancel = new Button(shell, SWT.PUSH);
		cancel.setText("&Cancel");
		cancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = Result.CANCEL;
				shell.dispose();
			}
		});

		FormData frmImg = new FormData();
		frmImg.left = new FormAttachment(0);
		frmImg.top = new FormAttachment(0, 10);
		questionImage.setLayoutData(frmImg);

		FormData frmLbl = new FormData();
		frmLbl.top = new FormAttachment(0, 10);
		frmLbl.left = new FormAttachment(questionImage);
		frmLbl.right = new FormAttachment(100);
		lbl.setLayoutData(frmLbl);

		FormData frmRadioK = new FormData();
		frmRadioK.left = new FormAttachment(0);
		frmRadioK.right = new FormAttachment(100);
		frmRadioK.top = new FormAttachment(radioDelete);
		radioKeep.setLayoutData(frmRadioK);

		FormData frmRadioD = new FormData();
		frmRadioD.left = new FormAttachment(0);
		frmRadioD.right = new FormAttachment(100);
		frmRadioD.top = new FormAttachment(questionImage, 15);
		radioDelete.setLayoutData(frmRadioD);

		FormData frmOK = new FormData();
		frmOK.top = new FormAttachment(radioKeep);
		frmOK.right = new FormAttachment(cancel);
		frmOK.bottom = new FormAttachment(100);
		add.setLayoutData(frmOK);

		FormData frmCancel = new FormData();
		frmCancel.top = new FormAttachment(radioKeep);
		frmCancel.right = new FormAttachment(100);
		frmCancel.bottom = new FormAttachment(100);
		cancel.setLayoutData(frmCancel);

		result = null;
		shell.pack();
		shell.open();
	}

	// returns -1 on cancel
	public static Result confirm(Display display, Shell parent, Project project) {
		me.create(parent, project.getSym(), project);

		while (!me.shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return me.result;
	}
}
