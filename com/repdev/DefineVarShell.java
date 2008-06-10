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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.MessageBox;

import com.repdev.parser.RepgenParser;
 
/**
 *  DefineVarShell will display a GUI for the user to select the variable type.
 *  It also has the option to allow the user to define the character length and
 *  the array size.  If the optional fields are left blank, the character string
 *  will be 132 characters in length and non array vars.
 */
public class DefineVarShell {
	private Shell shell;
	private EditorComposite ec;
	private String varName = "";
	private String sTmpStr = "";

	public DefineVarShell(EditorComposite ec, String varName) {
		this.ec = ec;
		this.varName = varName.trim();
	}

	public static void create(EditorComposite ec, String varName) {
		DefineVarShell self = new DefineVarShell(ec, varName);
		self.open();
	}

	/**
	 *  Create the GUI and wait for user input.  If CHARACTER is selected for
	 *  the variable type, an optional field will be enabled for character legth.
	 *  An optional field for the array size is also available.
	 */
	public void open() {
		shell = new Shell(SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM );
		shell.setImage(RepDevMain.smallDefineVarImage);
		
		// Open the GUI only if a single word is passed in.
		if(ec.isAlphaNumeric(varName)){
			FormLayout layout = new FormLayout();
			layout.marginTop = 10;
			layout.marginBottom = 10;
			layout.marginLeft = 10;
			layout.marginRight = 10;
			layout.spacing = 8;
			
			shell.setLayout(layout);

			shell.setText("Define Variable");

			Group varGroup = new Group(shell, SWT.NONE);
			varGroup.setText("  Variable Options for - "+varName+"  ");
			
			Label varTypeLabel = new Label(varGroup, SWT.NONE);
			varTypeLabel.setText("Variable Type");

			final Combo varType = new Combo(varGroup, SWT.READ_ONLY);
			varType.add("CHARACTER",0);
			varType.add("DATE",1);
			varType.add("FLOAT",2);
			varType.add("MONEY",3);
			varType.add("NUMBER",4);
			varType.add("RATE",5);
			varType.select(0);

			Label charLengthLabel = new Label(varGroup, SWT.NONE);
			charLengthLabel.setText("Char Length");

			final Text charLength = new Text(varGroup, SWT.BORDER);

			Label charLengthOptLabel = new Label(varGroup, SWT.NONE);
			charLengthOptLabel.setText("(Optional)");

			Label arraySizeLabel = new Label(varGroup, SWT.NONE);
			arraySizeLabel.setText("Array Size");

			final Text arraySize = new Text(varGroup, SWT.BORDER);

			Label arraySizeOptLabel = new Label(varGroup, SWT.NONE);
			arraySizeOptLabel.setText("(Optional)");

			Label commentTextLabel = new Label(varGroup, SWT.NONE);
			commentTextLabel.setText("Comment");

			final Text commentText = new Text(varGroup, SWT.BORDER);
			commentText.setTextLimit(90);
			
			Label commentTextOptLabel = new Label(varGroup, SWT.NONE);
			commentTextOptLabel.setText("(Optional)");
			
			Group preview = new Group(shell,SWT.NONE);
			preview.setText("Preview");
			
			final Text previewText = new Text(preview, SWT.READ_ONLY|SWT.MULTI);
			
			Button ok = new Button(shell, SWT.PUSH);
			ok.setText("Ok");

			Button cancel = new Button(shell, SWT.PUSH);
			cancel.setText("Cancel");
			
			// Key Events
			charLength.addKeyListener(new KeyListener() {
				public void keyReleased(KeyEvent e){
					formulateString(varName, varType.getText(), charLength.getText(),
						arraySize.getText(), commentText.getText());
					previewText.setText(sTmpStr+" ");
					shell.pack(true);
				}
				public void keyPressed(KeyEvent e){}
			});
			arraySize.addKeyListener(new KeyListener() {
				public void keyReleased(KeyEvent e){
					formulateString(varName, varType.getText(), charLength.getText(),
						arraySize.getText(), commentText.getText());
					previewText.setText(sTmpStr+" ");
					shell.pack(true);
				}
				public void keyPressed(KeyEvent e){}
			});
			commentText.addKeyListener(new KeyListener() {
				public void keyReleased(KeyEvent e){
					formulateString(varName, varType.getText(), charLength.getText(),
						arraySize.getText(), commentText.getText());
					previewText.setText(sTmpStr+" ");
					shell.pack(true);
				}
				public void keyPressed(KeyEvent e){}
			});
			
			// --- Combo events ---
			varType.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent e){
					// Check to see if Character Type is selected.  If so, enable the
					// character length field.  Otherwise disable it.
					if(varType.getSelectionIndex()==0){
						charLength.setEnabled(true);
					}
					else{
						charLength.setText("");
						charLength.setEnabled(false);
					}
					
					formulateString(varName, varType.getText(), charLength.getText(),
						arraySize.getText(), commentText.getText());
					previewText.setText(sTmpStr+" ");
					shell.pack(true);
				}
			});

			// --- Button events ---
			ok.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// Validate numeric characters for CharLength.
					if(charLength.getText().length()!=0 && !ec.isNum(charLength.getText())){
						messageBox("Invalid Character Length.  Please enter numbers only.");
					}
					// Validate numeric characters for arraySize.
					else if(arraySize.getText().length()!=0 && !ec.isNum(arraySize.getText())){
						messageBox("Invalid Array Size.  Please enter numbers only.");
					}
					else{
						ec.defineVariable(sTmpStr);
						shell.dispose();
					}
				}
			});

			cancel.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					shell.dispose();
				}
			});

			// Layout infos

			varGroup.setLayout(layout);
			FormData data = new FormData();
			data.top = new FormAttachment(0);
			data.left = new FormAttachment(0);
			data.right = new FormAttachment(100);
			varGroup.setLayoutData(data);
			
			data = new FormData();
			data.top = new FormAttachment(0);
			data.left = new FormAttachment(0);
			data.width=85;
			varTypeLabel.setLayoutData(data);

			data = new FormData();
			data.top = new FormAttachment(0);
			data.left = new FormAttachment(varTypeLabel);
			data.right = new FormAttachment(100);
			varType.setLayoutData(data);

			data = new FormData();
			data.top = new FormAttachment(varTypeLabel);
			data.left = new FormAttachment(0);
			data.width=85;
			charLengthLabel.setLayoutData(data);

			data = new FormData();
			data.top = new FormAttachment(varTypeLabel);
			data.left = new FormAttachment(charLengthLabel);
			data.width = 30;
			//data.right = new FormAttachment(100);
			charLength.setLayoutData(data);

			data = new FormData();
			data.top = new FormAttachment(varTypeLabel,3);
			data.left = new FormAttachment(charLength,-3);
			charLengthOptLabel.setLayoutData(data);

			data = new FormData();
			data.top = new FormAttachment(charLengthLabel);
			data.left = new FormAttachment(0);
			data.width=85;
			arraySizeLabel.setLayoutData(data);

			data = new FormData();
			data.top = new FormAttachment(charLengthLabel);
			data.left = new FormAttachment(arraySizeLabel);
			data.width = 30;
			//data.right = new FormAttachment(100);
			arraySize.setLayoutData(data);

			data = new FormData();
			data.top = new FormAttachment(charLengthLabel,3);
			data.left = new FormAttachment(arraySize,-3);
			arraySizeOptLabel.setLayoutData(data);

			data = new FormData();
			data.top = new FormAttachment(arraySizeLabel);
			data.left = new FormAttachment(0);
			data.width=85;
			commentTextLabel.setLayoutData(data);

			
			data = new FormData();
			data.top = new FormAttachment(arraySizeLabel);
			data.left = new FormAttachment(commentTextLabel);
			data.right = new FormAttachment(100);
			commentText.setLayoutData(data);
			

			data = new FormData();
			data.top = new FormAttachment(commentText,-7);
			data.left = new FormAttachment(commentTextLabel);
			data.right = new FormAttachment(100);
			commentTextOptLabel.setLayoutData(data);
			
			preview.setLayout(layout);
			data = new FormData();
			data.top = new FormAttachment(varGroup);
			data.left = new FormAttachment(0);
			data.right = new FormAttachment(100);
			preview.setLayoutData(data);
			
			data = new FormData();
			data.top = new FormAttachment(varGroup);
			data.left = new FormAttachment(0);
			data.right = new FormAttachment(95);
			previewText.setLayoutData(data);

			

			data = new FormData();
			data.top = new FormAttachment(preview);
			data.bottom = new FormAttachment(100);
			data.right = new FormAttachment(100);
			cancel.setLayoutData(data);

			data = new FormData();
			data.top = new FormAttachment(preview);
			data.right = new FormAttachment(cancel);
			data.bottom = new FormAttachment(100);
			ok.setLayoutData(data);

			varGroup.pack();
			preview.pack();
			shell.setDefaultButton(ok);

			formulateString(varName, varType.getText(), charLength.getText(),
				arraySize.getText(), commentText.getText());
				previewText.setText(sTmpStr+" ");
			shell.pack();
			shell.open();
			while (!shell.isDisposed()) {
				if (!shell.getDisplay().readAndDispatch())
					shell.getDisplay().sleep();
			}
		}
		else{
			messageBox("Multiple words were selected.  Variable not defined.");
		}
	}
	
	private void messageBox(String msg){
		MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		dialog.setMessage(msg);
		dialog.setText("Define Variable - Error");
		dialog.open();
	}
	
	private void formulateString(String varName, String varType, String charLength, String arraySize, String commentText){
		sTmpStr=" "+varName+"="+varType;
		if(charLength.length()!=0){
		sTmpStr=sTmpStr+"("+charLength+")";
		}
		if(arraySize.length()!=0){
			sTmpStr=sTmpStr+" ARRAY("+arraySize+")";
		}
		if(commentText.length()!=0){
			sTmpStr=sTmpStr+"  ["+commentText+"]";
		}

		//Added by Jake, make it no more than 80 chars at a time
		if( sTmpStr.length() > 90){
			if( commentText.length()!=0)
				sTmpStr = sTmpStr.substring(0,90) + "..]";
			else
				sTmpStr = sTmpStr.substring(0,90) + "...";
		}
				
	}


}
