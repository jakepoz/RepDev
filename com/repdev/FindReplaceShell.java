package com.repdev;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Shell;

import com.repdev.FileDialog.Mode;

public class FindReplaceShell {
	private Shell shell, parent;
	
	public FindReplaceShell(Shell parent){
		this.parent = parent;
		createGUI();
	}
	
	private void createGUI(){
		FormLayout layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;

		FormData data;

		shell = new Shell(parent, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE);
		shell.setText("Find/Replace");
		shell.setLayout(layout);
		shell.setMinimumSize(250, 350);
	}
}
