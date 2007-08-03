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
import org.eclipse.swt.custom.StyledText;
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
	private EditorComposite ec;
	
	public SurroundWithShell(EditorComposite ec) {
		this.ec = ec;
	}
	
	public static void create(EditorComposite ec) {
		SurroundWithShell self = new SurroundWithShell(ec);
		self.open();
	}
		
	public void open() {
		shell = new Shell(SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM );
		FormLayout layout = new FormLayout();
		layout.marginTop = 10;
		layout.marginBottom = 10;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.spacing = 8;
		shell.setLayout(layout);
		
		shell.setText("Surround Selection With");
		
		Label text = new Label(shell, SWT.NONE);
		text.setText("Surround each line of the selected text with custom values\n"
				+"(\\n for newline)\nThe after text should usually include a \\n at the end");
		
		Group surGroup = new Group(shell, SWT.NONE);
		surGroup.setText("Surround Text Options");
		
		
		Label beforeLabel = new Label(surGroup, SWT.NONE);
		beforeLabel.setText("Before");
				
		final Text beforeText = new Text(surGroup, SWT.BORDER);
		
		Label afterLabel = new Label(surGroup, SWT.NONE);
		afterLabel.setText("After");

		final Text afterText = new Text(surGroup, SWT.BORDER);
		afterText.setText("\\n");
		
		final Button replaceQuotes = new Button(surGroup, SWT.CHECK);
		replaceQuotes.setText("Replace Quotes");
		replaceQuotes.setSelection(true);
		
		Button ok = new Button(shell, SWT.PUSH);
		ok.setText("Ok");
		
		Button cancel = new Button(shell, SWT.PUSH);
		cancel.setText("Cancel");
		
		// --- Button events ---
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String before = beforeText.getText();
				String after = afterText.getText();
				
			    before = before.replaceAll("\\\\n", "\n");
			    after = after.replaceAll("\\\\n", "\n");
			    
				System.out.println("Before: " + before);
				System.out.println("After:  " + after);
				
				ec.surroundEachLineWith(before, after, replaceQuotes.getSelection());
				
				// Close the shell now.
				shell.dispose();
				
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
		data.width=50;
		beforeLabel.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(0);
		data.left = new FormAttachment(beforeLabel);
		data.right = new FormAttachment(100);
		beforeText.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(beforeLabel);
		data.left = new FormAttachment(0);
		data.width=50;
		afterLabel.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(beforeLabel);
		data.left = new FormAttachment(afterLabel);
		data.right = new FormAttachment(100);
		afterText.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(afterLabel);
		data.left = new FormAttachment(0);
		replaceQuotes.setLayoutData(data);
		
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
		
		shell.setDefaultButton(ok);
		
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch())
				shell.getDisplay().sleep();
		}
		
	}
	
	
}
