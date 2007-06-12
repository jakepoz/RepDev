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
	boolean promptReady = false, stillRunning = false;
	
	public RunReportShell(Shell parent, SymitarFile file, int sym) {
		this.parent = parent;
		display = parent.getDisplay();
		this.file = file;
		this.sym = sym;
	}
	
	private void create(){
		shell = new Shell(parent,SWT.SHELL_TRIM | SWT.APPLICATION_MODAL );
		shell.setText("Run Report - EXPERIMENTAL!!!");
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
		
		Group queueGroup = new Group(shell,SWT.NONE);
		layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;
		queueGroup.setLayout(layout);
		queueGroup.setText("Queue Control");
		
		final Button defaultQueueButton = new Button(queueGroup,SWT.RADIO);
		defaultQueueButton.setText("Use first empty queue");
		defaultQueueButton.setSelection(true);
		final Button selectQueueButton = new Button(queueGroup,SWT.RADIO);
		selectQueueButton.setText("Pick a queue");
		
		Label queueLabel = new Label(queueGroup,SWT.NONE);
		queueLabel.setText("Queue:");
		
		final Spinner queueSpinner = new Spinner(queueGroup,SWT.BORDER);
		queueSpinner.setMaximum(3);
		queueSpinner.setMinimum(0);
		
		final Button defaultsButton = new Button(promptGroup,SWT.RADIO);
		defaultsButton.setText("Answer default to all prompt");
		
		
		Button promptButton = new Button(promptGroup,SWT.RADIO);
		promptButton.setText("Prompt user at run time");
		promptButton.setSelection(true);
		
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
		yesFMButton.setText("Execute File Maintenance");
		yesFMButton.addSelectionListener( new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				titleLabel.setEnabled(true);
				typeLabel.setEnabled(true);
				titleText.setEnabled(true);
				typeCombo.setEnabled(true);
			}
		});
		
		final Button runButton = new Button(shell,SWT.NONE);
		runButton.setText("Run Report");
		
		
		final Button cancelButton = new Button(shell,SWT.NONE);
		cancelButton.setText("Cancel");
		cancelButton.setEnabled(false);
		
		Button closeButton = new Button(shell,SWT.NONE);
		closeButton.setText("Close Window");
		closeButton.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
			
		});
		
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
		
		final Label promptLabel = new Label(ioGroup,SWT.NONE);
		promptLabel.setText("Prompt:");
		promptLabel.setEnabled(false);
		
		final Text promptText = new Text(ioGroup,SWT.BORDER);
		promptText.setEnabled(false);
		
		final Button nextPromptButton = new Button(ioGroup,SWT.NONE);
		nextPromptButton.setText("Next");
		nextPromptButton.setEnabled(false);
		nextPromptButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				promptReady = true;
			}
		});
		
		final Button prevPromptButton = new Button(ioGroup,SWT.NONE);
		prevPromptButton.setEnabled(false);
		prevPromptButton.setText("Prev");
		
		
		runButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				SymitarSession.PromptListener prompter = new SymitarSession.PromptListener(){
					public String getPrompt(String name) {
						String toRet = "";
						
						if( shell.isDisposed() || promptLabel.isDisposed() || promptText.isDisposed()  )
							return null;
						
						//Return "" as default value
						if( defaultsButton.getSelection() )
							return "";
						
						promptLabel.setEnabled(true);
						promptText.setEnabled(true);
						prevPromptButton.setEnabled(true);
						nextPromptButton.setEnabled(true);
						
						promptLabel.setText(name);
						promptText.setText("");
						promptText.setFocus();
						
						shell.layout(true,true);
						promptReady = false;
										
						//Process SWT messages here until the prompt is ready or has been cancelled, this way we know when the button is clicked.
						while (!promptReady && !shell.isDisposed()) {
							if (!display.readAndDispatch())
								display.sleep();
						}
						
						if( !promptText.isDisposed() )
						{
							toRet = promptText.getText();
							promptLabel.setText("Prompt:");
							promptText.setText("");
							
							promptLabel.setEnabled(false);
							promptText.setEnabled(false);
							prevPromptButton.setEnabled(false);
							nextPromptButton.setEnabled(false);
							
							return toRet;
						}
						else
							return null;
					}
					
				};
				
				cancelButton.setEnabled(true);
				runButton.setEnabled(false);
				
				SymitarSession.RunRepgenResult result =  RepDevMain.SYMITAR_SESSIONS.get(sym).runRepGen(file.getName(), defaultQueueButton.getSelection() ? -1 : queueSpinner.getSelection(), progressBar, ioText, prompter);
				final int seq = result.getSeq();
				final int time = result.getTime();
				
				if( seq != -1){
					stillRunning = true;
					
					shell.getDisplay().timerExec(500, new Runnable(){
						public void run() {
							if( shell.isDisposed() )
								return;
							
							if( RepDevMain.SYMITAR_SESSIONS.get(sym).isSeqRunning(seq) ){
								ioText.setText("Still running... \nSequence: " + seq);
								progressBar.setSelection(75);
								shell.getDisplay().timerExec(1000, this);
							}
							else
							{
								stillRunning = false;
								ioText.setText("Repgen Finished\nSequence: " + seq);
								progressBar.setSelection(100);
								
								for( int seq : RepDevMain.SYMITAR_SESSIONS.get(sym).getReportSeqs(file.getName(), time) ){
									ioText.setText(ioText.getText()+"\n\nOutput Sequence: " + seq);
								}
							}
						}
					});
				}
				
				if( !ioText.isDisposed() ){
					cancelButton.setEnabled(false);
					runButton.setEnabled(true);
				}
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
		queueGroup.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(ioGroup);
		data.top = new FormAttachment(queueGroup);
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
		data.right = new FormAttachment(100);
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		noFMButton.setLayoutData(data);
		
		data = new FormData();
		data.right = new FormAttachment(100);
		data.left = new FormAttachment(0);
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
		data.left = new FormAttachment(typeLabel);
		data.top = new FormAttachment(titleText);
		typeCombo.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
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
		data.right = new FormAttachment(promptText);
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
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		defaultQueueButton.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(defaultQueueButton);
		selectQueueButton.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(selectQueueButton);
		queueLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(queueLabel);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(selectQueueButton);
		queueSpinner.setLayoutData(data);
		
		shell.pack();
		shell.open();
		shell.setMinimumSize(shell.getSize().x + 100, shell.getSize().y + 50);
	}
	
	
	public void open(){
		create();

		while (!shell.isDisposed()) {
			if (!shell.isDisposed() && !display.readAndDispatch())
				display.sleep();
		}
	}
	
	
}
