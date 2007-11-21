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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * AboutBox - Second version.  Uses the beloved formlayout rather
 * than using the gridlayout and VE.  Includes more options and whatnot.
 * @author Ryan Schultz
 *
 */
public class AboutBoxShell {
	private Shell shell;
	
	public static final String aboutText = "\n" 
		  +"RepDev (" + RepDevMain.VERSION + ")\n"
		  +"(c) 2007 http://repdev.org/\n"
		  +"support@repdev.org\n"
		  +"\nSWT Version: " + SWT.getVersion() + "\n\n"
		  +"RepDev is a community based free IDE for use writing\n"
		  +"RepGens for Symitar systems.  It is ment to be an\n"
		  +"alternative to using Episys's built in RepGen tools.\n"
		  +"\n";
	public static final String creditsText = "\n"
		  +"Credits:\n"
		  +"Jake Poznanski - Programmer/Project Head\n"
		  +"Ryan Schultz   - Programmer\n"
		  +"Sean Delaney   - Programmer\n"
		  +"Bruce Chang    - Programmer/Documentation\n"
		  +"Michael Webb   - Documentation\n\n";
	
	public static void show() {
		AboutBoxShell me = new AboutBoxShell();
		me.createShell();
		
	}
	
	private void createShell() {
		shell = new Shell(SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		shell.setText("About RepDev");
		
		FormLayout layout = new FormLayout();
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.spacing = 0;
		shell.setLayout(layout);
		
		//shell.setBackground(new Color(shell.getDisplay(), 255, 255, 255) );
		
		final Image logo = new Image(shell.getDisplay(), RepDevMain.IMAGE_DIR + "repdev_logo.png");
		final Label logoLabel = new Label(shell,SWT.NONE);
		logoLabel.setImage(logo);
		
		final ScrolledComposite sc = new ScrolledComposite(shell, SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setLayout(new FillLayout());
		sc.setBackground( new Color(sc.getDisplay(), 255, 255, 255) );
		
		final Composite inner = new Composite(sc,SWT.NONE);
		inner.setLayout(new FillLayout());
		
		final Label mainText = new Label(inner, SWT.NONE);
		mainText.setBackground( new Color(sc.getDisplay(), 255, 255, 255) );		
		mainText.setAlignment(SWT.CENTER);
		mainText.setText(aboutText);

		inner.pack();
		sc.setContent(inner);
		sc.setExpandVertical(false);
		sc.setExpandHorizontal(true);
	    
		ToolBar bar = new ToolBar(shell, SWT.FLAT | SWT.HORIZONTAL);
		
		ToolItem repdev = new ToolItem(bar,SWT.PUSH);
		repdev.setText("RepDev");
		
		ToolItem license = new ToolItem(bar, SWT.PUSH);
		license.setText("License");
		
		ToolItem credits = new ToolItem(bar, SWT.PUSH);
		credits.setText("Credits");
		
		// ----- Button Actions ----- //
		
		repdev.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				mainText.setText(aboutText);
				inner.pack();
			}
		});
		
		license.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String gplTxt = "",line;
				try {
					BufferedReader gpl = new BufferedReader(new FileReader("GPL.txt"));
					while( (line = gpl.readLine()) != null ) {
						gplTxt += line.trim() + "\n";
					}
				} catch (FileNotFoundException e1) {
					gplTxt = "File not found: GPL.txt";
				} catch (IOException e2) {
					System.err.println("IOException in AboutBoxShell2");
				}
				mainText.setText(gplTxt);
				inner.pack();
			}
		});
		
		credits.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				mainText.setText(creditsText);
				inner.pack();
			}
		});
				
		// ----- Layout Stuff ----- //
		FormData data;
		
		data = new FormData();
		data.top = new FormAttachment(0);
		data.left = new FormAttachment(0);
		logoLabel.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(logoLabel);
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.bottom = new FormAttachment(bar);
		sc.setLayoutData(data);
				
		data = new FormData();
		data.bottom = new FormAttachment(100);
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		bar.setLayoutData(data);
				
		shell.setMinimumSize(shell.computeSize(SWT.DEFAULT, 400));
		
		shell.pack();
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch())
				shell.getDisplay().sleep();
		}
		
	}		
}
