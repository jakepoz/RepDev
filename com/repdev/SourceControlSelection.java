package com.repdev;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SourceControlSelection {
	public enum CHOICE {COMPARE,JUSTOPEN,OVRWRTSYM,OVRWRTREPO,NONE};
	public CHOICE selection=CHOICE.JUSTOPEN;
	private Shell shell;

	/**
	 *  Create the GUI and wait for user input.  If CHARACTER is selected for
	 *  the variable type, an optional field will be enabled for character legth.
	 *  An optional field for the array size is also available.
	 */
	public void open(String RepGenName) {
		shell = new Shell(SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM );
		//shell.setImage(RepDevMain.xxxxx);

		FormLayout layout = new FormLayout();
		layout.marginTop = 10;
		layout.marginBottom = 20;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.spacing = 8;

		shell.setLayout(layout);

		shell.setText("Source Control - " + RepGenName);

		Label lbldesc = new Label(shell, SWT.PUSH);
		lbldesc.setText("The RepGen is out of sync with the repository.\nWhat would you like to do?");

		Button compare = new Button(shell, SWT.PUSH);
		compare.setText("Compare");

		Label lblcompare = new Label(shell, SWT.PUSH);
		lblcompare.setText("Compare both RepGens");

		Button justOpen = new Button(shell, SWT.PUSH);
		justOpen.setText("Just Open");

		Label lbljustOpen = new Label(shell, SWT.PUSH);
		lbljustOpen.setText("Just Open and do not sync RepGens");

		Button overwriteSYM = new Button(shell, SWT.PUSH);
		overwriteSYM.setText("Overwrite SYM");

		Label lbloverwriteSYM = new Label(shell, SWT.PUSH);
		lbloverwriteSYM.setText("Overwrite SYM with the Repository");

		Button overwriteRepository = new Button(shell, SWT.PUSH);
		overwriteRepository.setText("Overwrite Repo");

		Label lbloverwriteRepository = new Label(shell, SWT.PUSH);
		lbloverwriteRepository.setText("Overwrite Repository with the SYM");

		// --- Button events ---
		compare.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// do something
				selection=CHOICE.COMPARE;
				shell.dispose();
			}
		});

		justOpen.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// do something
				selection=CHOICE.JUSTOPEN;
				shell.dispose();
			}
		});

		overwriteSYM.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// do something
				selection=CHOICE.OVRWRTSYM;
				shell.dispose();
			}
		});

		overwriteRepository.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// do something
				selection=CHOICE.OVRWRTREPO;
				shell.dispose();
			}
		});


		// Layout infos
		FormData data = new FormData();
		data.top = new FormAttachment(0);
		data.left = new FormAttachment(18);
		data.width = 300;
		lbldesc.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(lbldesc);
		data.left = new FormAttachment(4);
		data.width = 95;
		compare.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(lbldesc);
		data.left = new FormAttachment(compare);
		//data.width = 350;
		lblcompare.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(compare);
		data.left = new FormAttachment(4);
		data.width = 95;
		justOpen.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(compare);
		data.left = new FormAttachment(justOpen);
		//data.width = 350;
		lbljustOpen.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(justOpen);
		data.left = new FormAttachment(4);
		data.width = 95;
		overwriteSYM.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(justOpen);
		data.left = new FormAttachment(overwriteSYM);
		//data.width = 350;
		lbloverwriteSYM.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(overwriteSYM);
		data.left = new FormAttachment(4);
		data.width = 95;
		overwriteRepository.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(overwriteSYM);
		data.left = new FormAttachment(overwriteRepository);
		//data.width = 350;
		lbloverwriteRepository.setLayoutData(data);
		shell.pack();
		shell.open();
		justOpen.setFocus();
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch())
				shell.getDisplay().sleep();
		}
	}
}
