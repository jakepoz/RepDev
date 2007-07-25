package com.repdev;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.repdev.SymitarSession.FMFile;

public class FMShell {
	private Shell shell;
	private boolean result = false;
	Button defaultQueueButton, selectQueueButton;
	Spinner queueSpinner;
	Label queueLabel;
	
	private void create(Shell parent, final String title) {
		FormLayout layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;

		shell = new Shell(parent, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		shell.setText("Run FM Options");
		shell.setLayout(layout);
		
		Group fmFields = new Group(shell,SWT.NONE);
		fmFields.setText("File Maintenance Fields");
		layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;
		fmFields.setLayout(layout);
		
		Group queueGroup = new Group(shell,SWT.NONE);
		queueGroup.setText("Queue Control");
		layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;
		queueGroup.setLayout(layout);
		
		Label titleLabel = new Label(fmFields,SWT.NONE);
		titleLabel.setText("Report Title");

		Label fileLabel = new Label(fmFields,SWT.NONE);
		fileLabel.setText("File");
		
		Label searchDaysLabel = new Label(fmFields,SWT.NONE);
		searchDaysLabel.setText("Search Days");
		
		defaultQueueButton = new Button(queueGroup,SWT.RADIO);
		defaultQueueButton.setText("Use first empty queue");
		defaultQueueButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				doEnable();
			}
		});
		
		selectQueueButton = new Button(queueGroup,SWT.RADIO);
		selectQueueButton.setText("Pick a queue");
		selectQueueButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				doEnable();
			}
		});
		
		queueLabel = new Label(queueGroup,SWT.NONE);
		queueLabel.setText("Queue:");
		
		queueSpinner = new Spinner(queueGroup,SWT.BORDER);
		queueSpinner.setMaximum(Config.getMaxQueues());
		queueSpinner.setMinimum(0);
		
		
		if( Config.getRunOptionsQueue() != -1 ){
			queueSpinner.setSelection(Config.getRunOptionsQueue());
			selectQueueButton.setSelection(true);
		}
		else{
			queueSpinner.setSelection(0);
			defaultQueueButton.setSelection(true);
		}
		
		
		//Select All when tabbing through
		FocusListener selectAllFocuser = new FocusListener(){

			public void focusGained(FocusEvent e) {
				if( e.widget instanceof Text)
					((Text)e.widget).selectAll();
			}

			public void focusLost(FocusEvent e) {
			}
			
		};
		
		MouseListener mouseFocuser = new MouseAdapter(){

			public void mouseDown(MouseEvent e) {
				if( e.widget instanceof Text)
					((Text)e.widget).selectAll();
			}
		
		};
		
		final Text titleText = new Text(fmFields,SWT.BORDER | SWT.READ_ONLY);
		titleText.setText(title);
		
		final Combo fileCombo = new Combo(fmFields,SWT.READ_ONLY);
		
		for( FMFile cur : SymitarSession.FMFile.values()){
			fileCombo.add(cur.getDisplayName());
			fileCombo.setData(cur);
		}
		
		fileCombo.select(0);
		
		final Text searchDaysText = new Text(fmFields,SWT.BORDER);
		searchDaysText.setText("1");
		searchDaysText.addFocusListener(selectAllFocuser);
		searchDaysText.addMouseListener(mouseFocuser);
		
		
		Button okButton = new Button(shell,SWT.PUSH);
		okButton.setText("Run FM");
		
		Button cancelButton = new Button(shell,SWT.PUSH);
		cancelButton.setText("Close Window");
		
		shell.setDefaultButton(okButton);
		fileCombo.setFocus();
		
		FormData data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(0);
		fmFields.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(fmFields);
		queueGroup.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		data.width=120;
		titleLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(titleLabel);
		data.top = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.width=80;
		titleText.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(titleText);
		data.width=120;
		fileLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(fileLabel);
		data.top = new FormAttachment(titleText);
		data.right = new FormAttachment(100);
		data.width=80;
		fileCombo.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(fileCombo);
		data.width=120;
		searchDaysLabel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(searchDaysLabel);
		data.top = new FormAttachment(fileCombo);
		data.right = new FormAttachment(100);
		data.width=80;
		searchDaysText.setLayoutData(data);
		
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
		
		
		data = new FormData();
		data.top = new FormAttachment(queueGroup);
		data.right = new FormAttachment(100);
		cancelButton.setLayoutData(data);
		cancelButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				result = false;
				shell.dispose();
			}			
		});
		
		data = new FormData();
		data.right = new FormAttachment(cancelButton);
		data.top= new FormAttachment(queueGroup);
		okButton.setLayoutData(data);
		okButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				result=false;
				
				shell.dispose();
			}			
		});
		
		doEnable();
		
		shell.pack();
		shell.open();
	}

	private void doEnable(){
		boolean set = false;
		
		if( selectQueueButton.getSelection() )
			set = true;
		
		queueSpinner.setEnabled(set);
		queueLabel.setEnabled(set);
	}
	
	
	/**
	 * Convencience method to just print a symitar file and ask for all needed infoz
	 * @param display
	 * @param parent
	 * @param file
	 * @return
	 */
	public static boolean runFM(Display display, Shell parent, String title) {
		FMShell dialog = new FMShell(); 
		dialog.create(parent, title);

		while (!dialog.shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return dialog.result;
	}
}
