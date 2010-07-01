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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class GotoLineShell {
	private Shell shell;
	private Display display;
	private StyledText txt;
	private static GotoLineShell me = new GotoLineShell();
	
	private void create(Shell parent, StyledText txt) {
		display = parent.getDisplay();
		
		this.txt = txt;
		
		shell = new Shell( parent, SWT.APPLICATION_MODAL | SWT.CLOSE | SWT.TITLE );
		shell.setText("Goto Line");
		
		FormLayout layout = new FormLayout();
		layout.marginTop = 10;
		layout.marginBottom = 10;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.spacing = 5;
		
		shell.setLayout(layout);
				
		final Label gotoLabel = new Label( shell, SWT.None );
		gotoLabel.setText("Enter Line Number (1..." + txt.getLineCount() + ")");
		
		final Text gotoText = new Text(shell, SWT.BORDER | SWT.SINGLE );
		
		final Button go = new Button(shell, SWT.PUSH );
		go.setText("Goto Line");
		
		FormData data = new FormData();
		data.top = new FormAttachment(0);
		data.left = new FormAttachment(0);
		gotoLabel.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(gotoLabel);
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		gotoText.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(gotoText);
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.bottom = new FormAttachment(100);
		go.setLayoutData(data);
		
		go.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					setLine(Integer.parseInt(gotoText.getText()));
					shell.dispose();
				} catch (Exception ex) {
					// System.err.println(ex.getMessage());
				}
			}			
		});
		
		shell.setDefaultButton(go);
		shell.pack();
		shell.open();
				
	}
	
	public static void show(Shell parent, StyledText txt) {
		me.create(parent,txt);
		
		while (!me.shell.isDisposed()) {
			if (!me.display.readAndDispatch())
				me.display.sleep();
		}
	}
	
	private void setLine(int ln) {
		EditorComposite ec = (EditorComposite)txt.getParent();
		int offset = txt.getOffsetAtLine(ln-1);
		txt.setSelection(offset,offset);
		txt.showSelection();
		ec.handleCaretChange();
		ec.lineHighlight();
		// Drop Navigation Position
		RepDevMain.mainShell.addToNavHistory(ec.getFile(), txt.getLineAtOffset(txt.getCaretOffset()));
		
	}
	
}
