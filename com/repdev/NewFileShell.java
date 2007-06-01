package com.repdev;

import org.eclipse.swt.*;
import java.util.ArrayList;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NewFileShell {
	private static NewFileShell me = new NewFileShell();
	private Shell shell;
	private ArrayList<SymitarFile> result = null;

	private NewFileShell() {
	}

	public enum Mode {
		NEW, OPEN, BOTH
	};

	private void create(Shell parent, final Project project, Mode mode) {
		FormLayout layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;

		shell = new Shell(parent, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE);
		shell.setText("Save/Open Symitar File");
		shell.setLayout(layout);
		shell.setMinimumSize(300, 450);

		final Button createNew = new Button(shell, SWT.RADIO);
		createNew.setText("Create New");

		final Label nameLabelNew = new Label(shell, SWT.NONE);
		nameLabelNew.setText("Name: ");

		final Label typeLabelNew = new Label(shell, SWT.NONE);
		typeLabelNew.setText("Type: ");

		final Text nameTextNew = new Text(shell, SWT.BORDER);

		final Combo typeComboNew = new Combo(shell, SWT.READ_ONLY);

		for (FileType type : FileType.values()) {
			typeComboNew.add(type.toString());
		}

		typeComboNew.select(FileType.REPGEN.ordinal());

		final Button importExisting = new Button(shell, SWT.RADIO);
		importExisting.setText("Import Existing");

		final List fileList = new List(shell, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		fileList.setToolTipText("Select the files you want to import into your project. Hold CTRL to select more than one");

		final Label nameLabelImport = new Label(shell, SWT.NONE);
		nameLabelImport.setText("Name: ");

		final Label typeLabelImport = new Label(shell, SWT.NONE);
		typeLabelImport.setText("Type: ");

		final Text nameTextImport = new Text(shell, SWT.BORDER);
		nameTextImport.setToolTipText("Enter the pattern for files to load, standard Symitar wildcards apply ('+')");

		final Combo typeComboImport = new Combo(shell, SWT.READ_ONLY);

		// Only letter/help/repgen available here
		typeComboImport.add("REPGEN");
		typeComboImport.add("HELP");
		typeComboImport.add("LETTER");

		typeComboImport.select(0);

		final Button search = new Button(shell, SWT.PUSH);
		search.setText("Search");
		search.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				fileList.removeAll();
				fileList.select(-1);
				SymitarSession session = RepDevMain.SYMITAR_SESSIONS.get(project.getSym());

				for (SymitarFile file : session.getFileList(FileType.valueOf(typeComboImport.getText()), nameTextImport.getText())) {
					fileList.add(file.getName());
				}
			}

		});

		final Button add = new Button(shell, SWT.PUSH);
		add.setText("Create &File");
		add.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = new ArrayList<SymitarFile>();

				if (createNew.getSelection()) {
					if (nameTextNew.getText().trim().equals("")) {
						MessageBox dialog = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
						dialog.setText("Input Error");
						dialog.setMessage("You must enter a valid name for your file.");
						dialog.open();
						return;
					}

					result.add(new SymitarFile(nameTextNew.getText().trim(), FileType.valueOf(typeComboNew.getText())));
				}
				if (importExisting.getSelection()) {
					for (String line : fileList.getSelection()) {
						result.add(new SymitarFile(line, FileType.valueOf(typeComboImport.getText())));
					}
				}

				shell.dispose();
			}
		});

		fileList.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				if (fileList.getSelectionCount() == 1) {
					add.notifyListeners(SWT.Selection, null);
				}
			}
		});

		final Button cancel = new Button(shell, SWT.PUSH);
		cancel.setText("&Cancel");
		cancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = null;
				shell.dispose();
			}
		});

		// These three listeners are down here so that everything is defined UI
		// wises
		createNew.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (Control control : shell.getChildren())
					control.setEnabled(false);

				nameLabelNew.setEnabled(true);
				nameTextNew.setEnabled(true);
				typeLabelNew.setEnabled(true);
				typeComboNew.setEnabled(true);

				createNew.setEnabled(true);
				importExisting.setEnabled(true);
				add.setEnabled(true);
				cancel.setEnabled(true);

				add.setText("Create File");
				shell.layout();
				add.pack();
				shell.setDefaultButton(add);
			}
		});

		importExisting.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				for (Control control : shell.getChildren())
					control.setEnabled(false);

				fileList.setEnabled(true);
				nameLabelImport.setEnabled(true);
				nameTextImport.setEnabled(true);
				typeLabelImport.setEnabled(true);
				typeComboImport.setEnabled(true);
				search.setEnabled(true);

				createNew.setEnabled(true);
				importExisting.setEnabled(true);
				add.setEnabled(true);
				cancel.setEnabled(true);

				add.setText("Import File(s)");
				shell.layout();

				shell.setDefaultButton(search);
			}
		});

		typeComboImport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fileList.removeAll();
			}
		});

		createNew.setSelection(true);
		importExisting.setSelection(false);

		createNew.notifyListeners(SWT.Selection, new Event());

		FormData data = new FormData();

		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(0);
		createNew.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(createNew, 5, SWT.LEFT);
		data.top = new FormAttachment(createNew);
		nameLabelNew.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(nameLabelNew);
		data.top = new FormAttachment(createNew);
		data.right = new FormAttachment(100);
		nameTextNew.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(nameLabelNew, 0, SWT.LEFT);
		data.top = new FormAttachment(nameTextNew);
		typeLabelNew.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(typeLabelNew);
		data.top = new FormAttachment(nameTextNew);
		data.right = new FormAttachment(100);
		typeComboNew.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(typeComboNew);
		data.right = new FormAttachment(100);
		importExisting.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(importExisting, 5, SWT.LEFT);
		data.top = new FormAttachment(importExisting);
		typeLabelImport.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(importExisting);
		data.left = new FormAttachment(typeLabelImport);
		data.right = new FormAttachment(100);
		typeComboImport.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(typeLabelImport, 0, SWT.LEFT);
		data.top = new FormAttachment(typeComboImport);
		data.bottom = new FormAttachment(search);
		data.right = new FormAttachment(100);
		fileList.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(fileList, 0, SWT.LEFT);
		data.bottom = new FormAttachment(cancel);
		nameLabelImport.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(nameLabelImport);
		data.bottom = new FormAttachment(cancel);
		data.right = new FormAttachment(search);
		nameTextImport.setLayoutData(data);

		data = new FormData();
		data.bottom = new FormAttachment(cancel);
		data.right = new FormAttachment(100);
		search.setLayoutData(data);

		data = new FormData();
		data.right = new FormAttachment(100);
		data.bottom = new FormAttachment(100);
		cancel.setLayoutData(data);

		data = new FormData();
		data.right = new FormAttachment(cancel);
		data.bottom = new FormAttachment(100);
		add.setLayoutData(data);

		result = null;
		shell.pack();
		shell.open();
	}

	// returns -1 on cancel

	public static ArrayList<SymitarFile> getNewFile(Display display, Shell parent, Project project, Mode mode) {

		me.create(parent, project, mode);

		while (!me.shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return me.result;
	}
}
