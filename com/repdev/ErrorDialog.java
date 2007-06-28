package com.repdev;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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


/**
 * Crash/Error dialog box. Allows for sending error reports.
 * 
 * @author Jake Poznanski
 */

public class ErrorDialog {
	private Shell shell = null;
	private Label exceptionLabel, descriptionLabel, errorReportLabel, errorImage;
	private Text errorReportText, customText;
	private Button exitButton, clipButton;
	boolean customTextCleared = false;

	Exception exception;

	/**
	 * Initialize the error dialog with an exception 
	 * @param myException
	 */
	public ErrorDialog(Exception myException) {
		exception = myException;

		createShell();
		createLayout();
	}

	/**
	 * Open the window
	 *
	 */
	public void open() {
		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!Display.getCurrent().readAndDispatch())
				Display.getCurrent().sleep();
		}
	}

	/**
	 * Creates the controls
	 *
	 */
	private void createShell() {
		final Clipboard cb = new Clipboard(Display.getCurrent());
		
		shell = new Shell(Display.getCurrent(), SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
		shell.setText("RepDev - Error");
	
		FormLayout layout = new FormLayout();
		layout.spacing = layout.marginHeight = layout.marginWidth = 4;
		shell.setLayout(layout);

		descriptionLabel = new Label(shell, SWT.LEFT | SWT.WRAP | SWT.CENTER | SWT.BOLD);
		descriptionLabel.setText("A fatal exception has occured in the user thread, which caused the program to crash. Copy/paste the error text below and send it to jakepoz@gmail.com");

		exceptionLabel = new Label(shell, SWT.LEFT | SWT.WRAP);
		exceptionLabel.setText("Error Text: \"" + exception.getMessage() + "\" - " + exception.getClass().getName());
		
		errorReportLabel = new Label(shell, SWT.LEFT | SWT.WRAP);
		errorReportLabel.setText("Please send this report to help improved the stability of the program in the future. No information is sent that can identify yourself, your account information, or your computer.\n\nError Report Text:");

		errorImage = new Label(shell, SWT.NONE);
		errorImage.setImage(Display.getCurrent().getSystemImage(SWT.ICON_ERROR));

		customText = new Text(shell, SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
		customText.setText("Enter any notes about the error here. \n(Or a contact email address.)");
		customText.selectAll();
		customText.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (!customTextCleared) {
					customText.setText("");
					customTextCleared = true;
				}
			}
		});

		customText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				errorReportText.setText(getErrorReportText());
			}
		});

		errorReportText = new Text(shell, SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY | SWT.BORDER);
		errorReportText.setText(getErrorReportText());

		exitButton = new Button(shell, SWT.PUSH);
		exitButton.setText("Exit");
		exitButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
				shell.dispose();
			}
		});

		clipButton = new Button(shell, SWT.PUSH);
		clipButton.setText("Copy report to clipboard");
		clipButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TextTransfer textTransfer = TextTransfer.getInstance();
				cb.setContents(new Object[]{errorReportText.getText()}, new Transfer[]{textTransfer});
				
				//shell.close();
				//shell.dispose();
			}
		});

	}

	/**
	 * Generates error report text
	 * 
	 * @return text
	 */
	private String getErrorReportText() {
		String toRet = "RepDev: " + RepDevMain.VERSION + "\n\n";

		toRet = "Error Text: " + exception.getMessage() + " - " + exception.getClass().getName() + "\n";
		toRet += new Date().toString() + "\n\n";

		for (StackTraceElement element : exception.getStackTrace()) {
			toRet += element.toString() + "\n";
		}

		toRet += "\nUser Notes: \n" + customText.getText() + "\n\n";

		return toRet;
	}

	/**
	 * Sets up the layout of the controls
	 *
	 */
	private void createLayout() {
		FormData data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		errorImage.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(errorImage, 10);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(errorImage, 0, SWT.TOP);
		data.bottom = new FormAttachment(exceptionLabel);
		data.width = 320;
		descriptionLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(descriptionLabel);
		data.width = 320;
		exceptionLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(exceptionLabel);
		data.width = 200;
		errorReportLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(errorReportLabel);
		data.height = 80;
		data.width = 320;
		errorReportText.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(errorReportText);
		data.height = 55;
		data.width = 320;
		customText.setLayoutData(data);

		data = new FormData();
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(customText);
		//data.height = 26;
		data.bottom = new FormAttachment(customText,31,SWT.BOTTOM);
		clipButton.setLayoutData(data);

		data = new FormData();
		data.right = new FormAttachment(clipButton);
		data.top = new FormAttachment(customText);
		//data.height = 26;
		data.bottom = new FormAttachment(customText,31,SWT.BOTTOM);
		exitButton.setLayoutData(data);
	}
}
