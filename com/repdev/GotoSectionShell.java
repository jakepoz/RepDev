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
 *   Created by Bruce Chang.
 */

package com.repdev;

//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Combo;

import com.repdev.parser.SectionInfo;


/**
 *  GotoSectionShell will display a GUI for the user to select the variable type.
 *  It also has the option to allow the user to define the character length and
 *  the array size.  If the optional fields are left blank, the character string
 *  will be 132 characters in length and non array vars.
 */
public class GotoSectionShell {
	private Shell shell;
	private EditorComposite ec;
	private int caretOffset;
	private ArrayList<SectionInfo> secInfo = null;
	
	public GotoSectionShell(EditorComposite ec, int caretOffset) {
		this.ec = ec;
		this.caretOffset = caretOffset;
		this.secInfo = ec.getSectionInfoList();
	}

	public static void create(EditorComposite ec, int caretOffset) {
		GotoSectionShell self = new GotoSectionShell(ec, caretOffset);
		self.open();
	}

	/**
	 *  Create the GUI and wait for user input.  If CHARACTER is selected for
	 *  the variable type, an optional field will be enabled for character legth.
	 *  An optional field for the array size is also available.
	 */
	public void open() {
		shell = new Shell(SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM );
		//shell.setImage(RepDevMain.xxxxx);
		
		FormLayout layout = new FormLayout();
		layout.marginTop = 10;
		layout.marginBottom = 10;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.spacing = 8;

		shell.setLayout(layout);

		shell.setText("Goto Section/Procedure");

		Group sectionGroup = new Group(shell, SWT.NONE);
		sectionGroup.setText(" Select Section or Procedure ");

		Label sectionLabel = new Label(sectionGroup, SWT.NONE);
		sectionLabel.setText("Goto");

		Collections.sort(secInfo);
		final Combo sectionList = new Combo(sectionGroup, SWT.READ_ONLY);
		int index=0;
		for(SectionInfo si : secInfo){
			sectionList.add(si.getTitle());
			if(caretOffset >= si.getPos() && caretOffset <= si.getLastInsertPos()+3){
				sectionList.select(index);
			}
			index++;
		}
		
		Button ok = new Button(shell, SWT.PUSH);
		ok.setText("Ok");

		Button cancel = new Button(shell, SWT.PUSH);
		cancel.setText("Cancel");

		// --- Key Events ---
		sectionList.addKeyListener(new KeyListener() {
				public void keyReleased(KeyEvent e){
					if(e.keyCode == 27){
						shell.dispose();
					}
				}
				public void keyPressed(KeyEvent e){}
			});
		
		cancel.addKeyListener(new KeyListener() {
				public void keyReleased(KeyEvent e){
					if(e.keyCode == 27){
						shell.dispose();
					}
				}
				public void keyPressed(KeyEvent e){}
			});
		
		ok.addKeyListener(new KeyListener() {
				public void keyReleased(KeyEvent e){
					if(e.keyCode == 27){
						shell.dispose();
					}
				}
				public void keyPressed(KeyEvent e){}
			});
		
		// --- Button events ---
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ec.gotoSection(sectionList.getText());
				shell.dispose();
			}
		});

		cancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});

		// Layout infos

		sectionGroup.setLayout(layout);
		FormData data = new FormData();
		data.top = new FormAttachment(0);
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		sectionGroup.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(0);
		data.left = new FormAttachment(0);
		data.width=15;
		sectionLabel.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(0);
		data.left = new FormAttachment(sectionLabel);
		data.right = new FormAttachment(100);
		sectionList.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(sectionGroup);
		data.bottom = new FormAttachment(100);
		data.right = new FormAttachment(100);
		cancel.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(sectionGroup);
		data.right = new FormAttachment(cancel);
		data.bottom = new FormAttachment(100);
		ok.setLayoutData(data);

		sectionGroup.pack();
		shell.setDefaultButton(ok);

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch())
				shell.getDisplay().sleep();
		}
		
	}
			
}
