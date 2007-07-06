package com.repdev;

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

/**
 * Allows for repeat operations, such as 
 * Overwrite existing file?
 *  Yes, No, Cancel (Apply action to all) 
 * @author poznanja
 *
 */
public class RepeatOperationShell {
	private String prompt = "";
	private Shell shell, parent;
	private int result = CANCEL | ASK_TO_ALL;
	
	public static final int YES = 1, NO=2, CANCEL=4, APPLY_TO_ALL=8, ASK_TO_ALL=16;
	
	public RepeatOperationShell(Shell parent, String prompt){
		this.parent = parent;
		this.prompt = prompt;
	}
	
	public int open(){
		buildGUI();
		
		while (!shell.isDisposed()) {
			if (!Display.getCurrent().readAndDispatch())
				Display.getCurrent().sleep();
		}
		
		return result;
	}

	private void buildGUI() {
		shell = new Shell(parent,SWT.DIALOG_TRIM);
		
		FormLayout layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;
		
		shell.setLayout(layout);
		shell.setText("Confirm Operation");
		
		Label label1 = new Label(shell,SWT.NONE);
		label1.setImage(shell.getDisplay().getSystemImage(SWT.ICON_QUESTION));
		
		Label label2 = new Label(shell,SWT.NONE);
		label2.setText(prompt);
		
		Button yes = new Button(shell,SWT.PUSH);
		yes.setText("Yes");

		
		Button no = new Button(shell,SWT.PUSH);
		no.setText("No");
		
		Button cancel = new Button(shell,SWT.PUSH);
		cancel.setText("Cancel");
		
		final Button apply = new Button(shell,SWT.CHECK);
		apply.setText("Apply this to all items");
		
		yes.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				result = YES | ( apply.getSelection() ? APPLY_TO_ALL : ASK_TO_ALL );
				shell.close();
			}
			
		});
		
		no.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				result = NO | ( apply.getSelection() ? APPLY_TO_ALL : ASK_TO_ALL );
				shell.close();
			}
			
		});
		
		cancel.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				result = CANCEL | ASK_TO_ALL;
				shell.close();
			}
			
		});
		
		shell.setDefaultButton(yes);
		
		FormData data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		label1.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(label1);
		data.top = new FormAttachment(0);
		data.bottom = new FormAttachment(label1,0,SWT.BOTTOM);
		label2.setLayoutData(data);
		
		data= new FormData();
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(label1);
		cancel.setLayoutData(data);
		
		data= new FormData();
		data.right = new FormAttachment(cancel);
		data.top = new FormAttachment(label1);
		no.setLayoutData(data);
		
		data= new FormData();
		data.right = new FormAttachment(no);
		data.top = new FormAttachment(label1);
		yes.setLayoutData(data);
		
		data= new FormData();
		data.right = new FormAttachment(yes);
		data.top = new FormAttachment(label1);
		apply.setLayoutData(data);
		
		shell.pack();
		shell.open();
		
	}
}
