package com.repdev;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class FailedLogonShell {
	private Shell failShell;
	private static FailedLogonShell me = new FailedLogonShell();
	private Text pass;
	private Label FailText;
	private String newPass;
	
	private void create() {
		failShell = new Shell(SWT.APPLICATION_MODAL | SWT.TITLE | SWT.CLOSE);
		failShell.setText("Invalid Password");
		
		FormLayout layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;
		failShell.setLayout(layout);
		
		FailText = new Label(failShell, SWT.NONE);
		FailText.setText("Please retype your userID:");
		
		pass = new Text(failShell, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);	
		
		Button ok = new Button(failShell, SWT.PUSH);
		ok.setText("Submit");
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {	
				newPass = pass.getText();
				failShell.close();
			}
		});
		
		FormData data = new FormData();
		
		data = new FormData();
		data.left = new FormAttachment(0);
		//data.right = new FormAttachment(100);
		data.top = new FormAttachment(0);
		//data.bottom = new FormAttachment();
		FailText.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(FailText);
		//data.bottom = new FormAttachment(ok);
		pass.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		//data.right = new FormAttachment(100);
		data.top = new FormAttachment(pass);
		data.bottom = new FormAttachment(100);
		ok.setLayoutData(data);
		
		failShell.setDefaultButton(ok);
		
		failShell.pack();
		failShell.open();
		
		while (!failShell.isDisposed()) {
			if (!failShell.getDisplay().readAndDispatch())
				failShell.getDisplay().sleep();
		}
	}
	
	public static String checkPass() {
		me.newPass="fail";
		me.create();
		return me.newPass;
	}
}
