package com.repdev;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SurroundWithShell {
	private Shell shell;
	
	public static void create() {
		SurroundWithShell me = new SurroundWithShell();
		me.createShell();
	}
	
	private void createShell() {
		shell = new Shell(SWT.APPLICATION_MODAL | SWT.CLOSE);
		FormLayout layout = new FormLayout();
		layout.marginTop = 10;
		layout.marginBottom = 10;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.spacing = 10;
		shell.setLayout(layout);
		
		shell.setText("Surround Selection With");
		
		Label text = new Label(shell, SWT.NONE);
		text.setText("Surround each line of the selected text with custom values\n"
				+"(%n for newline)");
		
		Group surGroup = new Group(shell, SWT.NONE);
		surGroup.setText("Surround Text Options");
		
		
		Label beforeLabel = new Label(surGroup, SWT.NONE);
		beforeLabel.setText("Before");
				
		final Text beforeText = new Text(surGroup, SWT.BORDER);
		
		Label afterLabel = new Label(surGroup, SWT.NONE);
		afterLabel.setText("After");

		final Text afterText = new Text(surGroup, SWT.BORDER);
		
		Button ok = new Button(shell, SWT.PUSH);
		ok.setText("Ok");
		
		Button cancel = new Button(shell, SWT.PUSH);
		cancel.setText("Cancel");
		
		// Button events
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String before = beforeText.getText();
				String after = afterText.getText();
				
			    before = before.replaceAll("%n", "\n");
			    after = after.replaceAll("%n", "\n");
			    
				System.out.println("Before: " + before);
				System.out.println("After:  " + after);
				
				// TODO: Make this work...
				// RepDevMain.mainShell
				
			}			
		});
		
		cancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});
		
		// Layout infos
		
		surGroup.setLayout(layout);
		
		FormData data = new FormData();		
		data.top = new FormAttachment(0);
		data.left = new FormAttachment(0);
		text.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(text);
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		surGroup.setLayoutData(data);
		
		data = new FormData();		
		data.top = new FormAttachment(0);
		data.left = new FormAttachment(0);
		beforeLabel.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(0);
		data.left = new FormAttachment(beforeLabel);
		data.right = new FormAttachment(100);
		beforeText.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(beforeLabel);
		data.left = new FormAttachment(0);
		afterLabel.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(beforeLabel);
		data.left = new FormAttachment(afterLabel);
		data.right = new FormAttachment(100);
		afterText.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(surGroup);
		data.bottom = new FormAttachment(100);
		data.right = new FormAttachment(100);
		cancel.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(surGroup);
		data.right = new FormAttachment(cancel);
		data.bottom = new FormAttachment(100);
		ok.setLayoutData(data);
		
		surGroup.pack();
		
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch())
				shell.getDisplay().sleep();
		}
		
	}
	
	
}
