package com.repdev;

import java.awt.TextArea;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;

public class RunReportShell {
	private Shell shell, parent;
	private Display display;
	private SymitarFile file;
	int sym;
	
	public RunReportShell(Shell parent, SymitarFile file, int sym) {
		this.parent = parent;
		display = parent.getDisplay();
		this.file = file;
		this.sym = sym;
	}
	
	private void create(){
		shell = new Shell(parent,SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
		shell.setText("Run Report");
		shell.setImage(RepDevMain.smallRunImage);
		
		FormLayout layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;
		shell.setLayout(layout);
		
		FormData data;

		Label fileLabel = new Label(shell,SWT.NONE);
		fileLabel.setText("File name:");
		
		Label symLabel = new Label(shell,SWT.NONE);
		symLabel.setText("Sym: ");
		
		Text fileText = new Text(shell,SWT.READ_ONLY | SWT.BORDER);
		fileText.setText(file.getName());
		
		Text symText = new Text(shell,SWT.READ_ONLY | SWT.BORDER);
		symText.setText(String.valueOf(sym));
		
		Group promptGroup = new Group(shell,SWT.NONE);
		promptGroup.setText("User Prompt Options");
		layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;
		promptGroup.setLayout(layout);
		
		Group fmGroup = new Group(shell,SWT.NONE);
		fmGroup.setText("File Maintenance Options");
		layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;
		fmGroup.setLayout(layout);
		
		Button defaultsButton = new Button(promptGroup,SWT.RADIO);
		defaultsButton.setText("Answer default to all prompt");
		defaultsButton.setSelection(true);
		
		Button promptButton = new Button(promptGroup,SWT.RADIO);
		promptButton.setText("Prompt user at run time");
		
		final Label titleLabel = new Label(fmGroup,SWT.NONE);
		titleLabel.setText("Report Title: ");
		
		final Label typeLabel = new Label(fmGroup,SWT.NONE);
		typeLabel.setText("Root File Type: ");
		
		final Text titleText = new Text(fmGroup, SWT.BORDER);
		final Combo typeCombo = new Combo(fmGroup,SWT.READ_ONLY | SWT.DROP_DOWN);
		
		Button noFMButton = new Button(fmGroup,SWT.RADIO);
		noFMButton.setText("Do not run File Maintenance");
		noFMButton.addSelectionListener( new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				titleLabel.setEnabled(false);
				typeLabel.setEnabled(false);
				titleText.setEnabled(false);
				typeCombo.setEnabled(false);
			}
		});
		noFMButton.setSelection(true);
		
		Button yesFMButton = new Button(fmGroup,SWT.RADIO);
		yesFMButton.setText("Execute File Maintenance after running report");
		yesFMButton.addSelectionListener( new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				titleLabel.setEnabled(true);
				typeLabel.setEnabled(true);
				titleText.setEnabled(true);
				typeCombo.setEnabled(true);
			}
		});
		
		Button runButton = new Button(shell,SWT.NONE);
		runButton.setText("Run Report");
		
		
		Button cancelButton = new Button(shell,SWT.NONE);
		cancelButton.setText("Cancel");
		
		Button closeButton = new Button(shell,SWT.NONE);
		closeButton.setText("Close Window");
		
		Group ioGroup = new Group(shell,SWT.NONE);
		ioGroup.setText("Report Run I/O");
		layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;
		ioGroup.setLayout(layout);
		
		final ProgressBar progressBar = new ProgressBar(ioGroup,SWT.NONE);
		progressBar.setMaximum(100);
		progressBar.setMinimum(0);
		
		
		Label messageLabel = new Label(ioGroup,SWT.NONE);
		messageLabel.setText("Messages:");
		
		final Text ioText = new Text(ioGroup,SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		
		Label promptLabel = new Label(ioGroup,SWT.NONE);
		promptLabel.setText("Prompt:");
		
		Text promptText = new Text(ioGroup,SWT.BORDER);
		
		Button nextPromptButton = new Button(ioGroup,SWT.NONE);
		nextPromptButton.setText("Next");
		
		Button prevPromptButton = new Button(ioGroup,SWT.NONE);
		prevPromptButton.setText("Prev");
		
		
		runButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				
				ioText.setText(RepDevMain.SYMITAR_SESSIONS.get(sym).runRepGen(file.getName(), -1, progressBar, ioText, null));
			}	
		});
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		fileLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(fileLabel);
		data.right = new FormAttachment(symLabel);
		data.top = new FormAttachment(0);
		fileText.setLayoutData(data);
		
		data = new FormData();
		data.right = new FormAttachment(symText);
		data.top = new FormAttachment(0);
		symLabel.setLayoutData(data);
		
		data = new FormData();
		//data.left = new FormAttachment(symLabel);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(0);
		symText.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(fileText,0,SWT.BOTTOM);
		data.right = new FormAttachment(ioGroup);
		//data.bottom = new FormAttachment(fmGroup);
		promptGroup.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(promptGroup);
		data.right = new FormAttachment(ioGroup);
		fmGroup.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(promptGroup);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(symText);
		data.bottom = new FormAttachment(cancelButton);
		ioGroup.setLayoutData(data);
		
		data = new FormData();
		data.right = new FormAttachment(100);
		data.bottom = new FormAttachment(100);
		closeButton.setLayoutData(data);
		
		data = new FormData();
		data.bottom = new FormAttachment(100);
		data.right = new FormAttachment(closeButton);
		cancelButton.setLayoutData(data);
		
		data = new FormData();
		data.bottom = new FormAttachment(100);
		data.right = new FormAttachment(cancelButton);
		runButton.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		defaultsButton.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(defaultsButton);
		promptButton.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		noFMButton.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(noFMButton);
		yesFMButton.setLayoutData(data);
		
		data = new FormData();
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(yesFMButton);
		titleText.setLayoutData(data);
		
		data = new FormData();
		data.right = new FormAttachment(titleText);
		data.top = new FormAttachment(yesFMButton);
		titleLabel.setLayoutData(data);
		
		data = new FormData();
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(titleText);
		typeCombo.setLayoutData(data);
		
		data = new FormData();
		data.right = new FormAttachment(typeCombo);
		data.top = new FormAttachment(titleText);
		typeLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		data.right = new FormAttachment(100);
		progressBar.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(progressBar);
		messageLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(messageLabel);
		data.bottom = new FormAttachment(prevPromptButton);
		ioText.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.bottom = new FormAttachment(100);
		promptLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(promptLabel);
		data.right = new FormAttachment(nextPromptButton);
		data.bottom = new FormAttachment(100);
		promptText.setLayoutData(data);
		
		data = new FormData();
		data.right = new FormAttachment(100);
		data.bottom = new FormAttachment(100);
		prevPromptButton.setLayoutData(data);
		
		data = new FormData();
		data.right = new FormAttachment(prevPromptButton);
		data.bottom = new FormAttachment(100);
		nextPromptButton.setLayoutData(data);
		
		shell.pack();
		shell.open();
		shell.setMinimumSize(shell.getSize().x + 100, shell.getSize().y + 50);
	}
	
	
	public void open(){
		create();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
	
	
}
