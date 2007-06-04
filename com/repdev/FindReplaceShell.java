package com.repdev;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.repdev.FileDialog.Mode;
import com.repdev.parser.RepgenParser;

public class FindReplaceShell {
	private Shell shell, parent;
	private StyledText txt;
	private RepgenParser parser; //Only used to disable it for replace All operations, can always be null
	private Label infoLabel;
	private Text findText, replaceText;
	private Button forwardButton,backwardButton,caseButton, wrapButton, findButton; 
	
	public FindReplaceShell(Shell parent){
		this.parent = parent;
		createGUI();
	}
	
	public void open(){
		shell.open();
		shell.setDefaultButton(findButton);
		findText.setFocus();
	
		if( txt != null && !txt.getSelectionText().equals(""))
			findText.setText(txt.getSelectionText());
		
		findText.selectAll();
	}
	
	public void attach(StyledText txt, RepgenParser parser){
		this.txt = txt;
		this.parser = parser;
	}
	
	public void attach(StyledText txt){
		attach(txt,null);
	}
	
	public void close(){
		shell.setVisible(false);
	}
	
	private void createGUI(){
		final int labelWidth = 50, buttonWidth = 120;
		
		FormLayout layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;

		FormData data;

		shell = new Shell(parent, SWT.DIALOG_TRIM );
		shell.setText("Find/Replace");
		shell.setLayout(layout);
		shell.addShellListener(new ShellAdapter(){
			public void shellClosed(ShellEvent e) {
				e.doit = false;
				close();
			}	
		});
		
		Label findLabel = new Label(shell,SWT.NONE);
		findLabel.setText("Find:");
		
		findText = new Text(shell,SWT.BORDER);
		
		Label replaceLabel = new Label(shell,SWT.NONE);
		replaceLabel.setText("Replace:");
		
		replaceText = new Text(shell,SWT.BORDER);
		
		Group directionGroup = new Group(shell,SWT.NONE);
		directionGroup.setText("Direction");
		
		FillLayout fillLayout = new FillLayout();
 		fillLayout.type = SWT.VERTICAL;
 		directionGroup.setLayout(fillLayout);
		
		forwardButton = new Button(directionGroup,SWT.RADIO);
		forwardButton.setText("Forward");
		forwardButton.setSelection(true);
		
		backwardButton = new Button(directionGroup,SWT.RADIO);
		backwardButton.setText("Backward");
		
		Group optionsGroup = new Group(shell,SWT.NONE);
		optionsGroup.setText("Options");
		
		fillLayout = new FillLayout();
 		fillLayout.type = SWT.VERTICAL;
 		optionsGroup.setLayout(fillLayout);
 		
 		caseButton = new Button(optionsGroup,SWT.CHECK);
 		caseButton.setText("Case sensitive");
 		
 		wrapButton = new Button(optionsGroup,SWT.CHECK);
 		wrapButton.setText("Wrap search"); 				
		
		findButton = new Button(shell,SWT.NONE);
		findButton.setText("Find");
		findButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				find();
			}
		});
		
		Button replaceFindButton = new Button(shell,SWT.NONE);
		replaceFindButton.setText("Replace/Find");
		replaceFindButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				replace();
				find();
			}
		});
		
		Button replaceButton = new Button(shell,SWT.NONE);
		replaceButton.setText("Replace");
		replaceButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				replace();
			}
		});
		
		Button replaceAllButton = new Button(shell,SWT.NONE);
		replaceAllButton.setText("Replace All");
		replaceAllButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				replaceAll();
			}
		});
		
		infoLabel = new Label(shell,SWT.NONE);
		
		
		Button closeButton = new Button(shell,SWT.NONE);
		closeButton.setText("Close");
		closeButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				close();
			}
		});
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		data.width = labelWidth;
		findLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(findLabel);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(0);
		findText.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(findText);
		data.width = labelWidth;
		replaceLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(replaceLabel);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(findText);
		replaceText.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(replaceText);
		directionGroup.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(directionGroup);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(replaceText);
		optionsGroup.setLayoutData(data);
		
		data = new FormData();
		data.width = buttonWidth;
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(directionGroup);
		findButton.setLayoutData(data);
		
		data = new FormData();
		data.width = buttonWidth;
		data.left = new FormAttachment(findButton);
		data.top = new FormAttachment(directionGroup);
		replaceFindButton.setLayoutData(data);
		
		data = new FormData();
		data.width = buttonWidth;
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(findButton);
		replaceButton.setLayoutData(data);
		
		data = new FormData();
		data.width = buttonWidth;
		data.left = new FormAttachment(replaceButton);
		data.top = new FormAttachment(findButton);
		replaceAllButton.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(closeButton);
		data.top = new FormAttachment(replaceAllButton);
		infoLabel.setLayoutData(data);
		
		data = new FormData();
		data.top = new FormAttachment(replaceAllButton);
		data.right = new FormAttachment(100);
		closeButton.setLayoutData(data);
		
		shell.setDefaultButton(findButton);
		
		shell.pack();
	}

	protected void replaceAll() {
		init();
		
		txt.setRedraw(false);
		
		if( parser != null)
			parser.setReparse(false);
		
		while(true){
			if( !find() )
				break;
			
			if( !replace() )
				break;
		}
		
		if( parser != null){
			parser.setReparse(true);
			parser.reparseAll();
		}
		
		txt.setRedraw(true);
	}

	protected boolean replace() {
		init();
		
		String text = txt.getText();
		String find = findText.getText(), replace = replaceText.getText(), selection = txt.getSelectionText();

		if( !caseButton.getSelection() ){
			text = text.toLowerCase();
			find = find.toLowerCase();
			selection = selection.toLowerCase();			
		}
		
		if( !selection.equals(find) )
			return false;
			
		txt.replaceTextRange(txt.getSelection().x, txt.getSelection().y - txt.getSelection().x, replace);
		txt.setSelection(txt.getCaretOffset() - replace.length(), txt.getCaretOffset());
		
		return true;
	}
	
	private void init(){
		if( txt == null ){
			infoLabel.setText("No document opened");
			return;
		}
		
		infoLabel.setText("");
	}

	protected boolean find() {
		init();
				
		String text = txt.getText();
		String find = findText.getText(), replace = replaceText.getText();
		int nextPos, lastPos;
		
		if( !caseButton.getSelection() ){
			text = text.toLowerCase();
			find = find.toLowerCase();
			replace = replace.toLowerCase();
		}
		
		if( forwardButton.getSelection() )
		{
			nextPos = text.indexOf(find, txt.getCaretOffset());
			
			if( nextPos == -1 && wrapButton.getSelection() ){
				nextPos = text.indexOf(find);
				
				if( nextPos >= txt.getCaretOffset() )
					nextPos = -1;
			}
		}
		else{
			//Might be slow, will check someday with a profiler
			nextPos = -1;
			lastPos = -1;
			
			while(true){
				nextPos = text.indexOf(find, nextPos + 1);
				
				if( nextPos + find.length() >= txt.getCaretOffset() || nextPos == -1)
				{
					nextPos = lastPos;
					break;
				}
				
				lastPos = nextPos;
			}
			
			if( nextPos == -1 && wrapButton.getSelection() ){
				nextPos = txt.getCharCount() - 1;
				lastPos = -1;
				
				while(true){
					nextPos = text.indexOf(find, txt.getCaretOffset());
					
					if( nextPos + find.length() < txt.getCaretOffset() || nextPos == lastPos)
					{
						nextPos = lastPos;
						break;
					}
					
					lastPos = nextPos;
				}
			}
			
		}
		
		if( nextPos == -1)
		{
			infoLabel.setText("String not found");
			return false;
		}
		else
			txt.setSelection(nextPos, nextPos + find.length());
		
		return true;
	}
}
