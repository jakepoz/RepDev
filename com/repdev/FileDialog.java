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

import java.text.DateFormat;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * Basic dialog box for both opening and saving files from symitar
 * Watch out, as OPEN and SAVE modes are slightly different
 * 
 * TODO: Shows installed date, rename files, delete installed
 * 
 * @author Jake Poznanski
 *
 */
public class FileDialog {
	Shell shell, parent;
	Display display;
	Mode mode;
	int sym;
	ArrayList<SymitarFile> files = new ArrayList<SymitarFile>();
	Table table;
	Combo typeCombo;
	Text nameText;
	String dir;
	enum TABLE_COLUMN{NAME, SIZE, DATE};
	
	boolean listLoaded = false;

	public enum Mode {
		SAVE, OPEN,
	}

	public FileDialog(Shell parent, Mode mode, int sym) {
		this.parent = parent;
		this.mode = mode;
		this.sym = sym;
		this.display = parent.getDisplay();
	}
	
	public FileDialog(Shell parent, Mode mode, String dir) {
		this.parent = parent;
		this.mode = mode;
		this.dir = dir;
		this.display = parent.getDisplay();
	}

	private void create() {
		FormLayout layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;

		FormData data;

		shell = new Shell(parent, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE);
		shell.setText((mode == Mode.OPEN ? "Open" : "Save") + " Symitar File" + (mode == Mode.OPEN ? "(s)" : ""));
		shell.setLayout(layout);
		shell.setMinimumSize(600, 350);
		
		if( mode == Mode.SAVE )
			shell.setImage(RepDevMain.smallActionSaveImage);
		else
			shell.setImage(RepDevMain.smallFileOpenImage);

		Label nameLabel = new Label(shell, SWT.NONE);
		nameLabel.setText("Filename:");

		Label typeLabel = new Label(shell, SWT.NONE);
		typeLabel.setText("Type:");

		typeCombo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);

		typeCombo.add("REPGEN");
		
		if( dir == null){
			typeCombo.add("LETTER");
			typeCombo.add("HELP");
		}
		
		typeCombo.select(0);

		//TODO: Sortable columns
		table = new Table(shell, (mode == Mode.OPEN ? SWT.MULTI : SWT.SINGLE) | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLinesVisible(false);
		table.setHeaderVisible(true);

		table.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				if (table.getSelectionIndex() != -1) {
					if (mode == Mode.SAVE) {
						MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
						dialog.setText("Confirm Overwrite");
						dialog.setMessage("This file already exists, are you sure you want to overwrite it?");

						if (dialog.open() == SWT.CANCEL)
							return;
					}

					files.add((SymitarFile) (table.getSelection())[0].getData());
					shell.close();
				} else if (mode == Mode.SAVE) {
					
					if( dir == null)
						files.add(new SymitarFile(sym,nameText.getText().trim(), FileType.valueOf(typeCombo.getText())));
					else
						files.add(new SymitarFile(dir,nameText.getText().trim()));
					
					shell.close();
				}
			}
		});

		TableColumn nameCol = new TableColumn(table, SWT.NONE);
		nameCol.setText("Name");
		nameCol.setWidth(300);

		TableColumn sizeCol = new TableColumn(table, SWT.NONE);
		sizeCol.setText("Size");
		sizeCol.setWidth(120);

		TableColumn dateCol = new TableColumn(table, SWT.NONE);
		dateCol.setText("Date");
		dateCol.setWidth(150);

		// Add sort indicator and sort data when column selected
		Listener sortListener = new Listener() {
			public void handleEvent(Event e) {
				TableColumn column = (TableColumn)e.widget;
				if(column != table.getSortColumn()){
					table.setSortDirection(SWT.UP);
					table.setSortColumn(column);
				}
				else{
					table.setSortDirection(table.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP);
				}
				createList();				
			}
		};
		nameCol.addListener(SWT.Selection, sortListener);
		sizeCol.addListener(SWT.Selection, sortListener);
		dateCol.addListener(SWT.Selection, sortListener);
	
		nameText = new Text(shell, SWT.SINGLE | SWT.BORDER);

		nameText.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
 				createList();

				if (table.getItemCount() == 1 && !isTemplate()) {
					if (mode == Mode.SAVE) {
						MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
						dialog.setText("Confirm Overwrite");
						dialog.setMessage("This file already exists, are you sure you want to overwrite it?");

						if (dialog.open() == SWT.CANCEL)
							return;
					}

					files.add((SymitarFile) (table.getItems()[0].getData()));
					shell.close();
				} else if (mode == Mode.SAVE && nameText.getText().trim().length() > 0 && !isTemplate()) {
					if( dir == null)
						files.add(new SymitarFile(sym,nameText.getText().trim(), FileType.valueOf(typeCombo.getText())));
					else
						files.add(new SymitarFile(dir,nameText.getText().trim()));
					
					shell.close();
				}

			}
		});

		nameText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				listLoaded = false;
			}

		});

		final Button ok = new Button(shell, SWT.PUSH);

		if (mode == Mode.OPEN)
			ok.setText("Open File(s)");
		else
			ok.setText("Save File");

		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!listLoaded)
					createList();

				if (mode == Mode.OPEN && table.getSelectionIndex() != -1 ) {
					for (TableItem cur : table.getSelection())
						files.add((SymitarFile) cur.getData());

					shell.close();
				}
				
				if( mode == Mode.SAVE){
					if( table.getSelectionIndex() != -1 ){
						MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
						dialog.setText("Confirm Overwrite");
						dialog.setMessage("This file already exists, are you sure you want to overwrite it?");

						if (dialog.open() == SWT.CANCEL)
							return;
						
						files.add((SymitarFile)table.getSelection()[0].getData());
						shell.close();
					}
					else
					{
						if( dir == null)
							files.add(new SymitarFile(sym,nameText.getText().trim(), FileType.valueOf(typeCombo.getText())));
						else
							files.add(new SymitarFile(dir,nameText.getText().trim()));
						
						shell.close();
					}
				}
			}
		});

		Button cancel = new Button(shell, SWT.PUSH);
		cancel.setText("Cancel");
		cancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		nameLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(nameLabel);
		data.right = new FormAttachment(typeLabel);
		data.top = new FormAttachment(0);
		nameText.setLayoutData(data);

		data = new FormData();
		data.right = new FormAttachment(typeCombo);
		data.top = new FormAttachment(0);
		typeLabel.setLayoutData(data);

		data = new FormData();
		// data.left = new FormAttachment(typeLabel);
		data.top = new FormAttachment(0);
		data.right = new FormAttachment(100);
		typeCombo.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(typeCombo);
		data.bottom = new FormAttachment(cancel);
		data.right = new FormAttachment(100);
		table.setLayoutData(data);

		data = new FormData();
		data.right = new FormAttachment(100);
		data.bottom = new FormAttachment(100);
		cancel.setLayoutData(data);

		data = new FormData();
		data.right = new FormAttachment(cancel);
		data.bottom = new FormAttachment(100);
		ok.setLayoutData(data);

		nameText.setFocus();

		shell.pack();
		shell.open();
	}

	// TODO: Finish up with other template forms
	private boolean isTemplate() {
		return nameText.getText().contains("+");
	}

	private void createList() {
		table.removeAll();
		ArrayList<SymitarFile> fileList = new ArrayList<SymitarFile>();
		
		table.setRedraw(false);
		shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
		
		try{
			if( dir == null ){
				SymitarSession session = RepDevMain.SYMITAR_SESSIONS.get(sym);
				fileList = session.getFileList(FileType.valueOf(typeCombo.getText()), nameText.getText());
			}
			else{
				fileList = Util.getFileList(dir, nameText.getText());
			}
			
			// If the table sort column and direction has not been set, set them to default.
			if(table.getSortColumn() == null){
				table.setSortColumn(table.getColumn(0));
				table.setSortDirection(SWT.UP);
			}
			
			// Get the current sort column to pass into the sortFileList method.
			TABLE_COLUMN col;
			if(table.getSortColumn().getText().equalsIgnoreCase("size")){
				col = TABLE_COLUMN.SIZE;
			}
			else if(table.getSortColumn().getText().equalsIgnoreCase("date")){
				col = TABLE_COLUMN.DATE;
			}
			else{
				col = TABLE_COLUMN.NAME;
			}
			
			// Sort the list prior to populating the table.
			fileList = sortFileList(fileList, col, table.getSortDirection());
			// Populate the table.
			for (SymitarFile cur : fileList) {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(0, cur.getName());
				
				if( cur.getType() == FileType.REPGEN )
					item.setImage(0, RepDevMain.smallRepGenImage);
				else
					item.setImage(0, RepDevMain.smallFileImage);
				
				item.setText(1, Util.getByteStr(cur.getSize()));
				item.setText(2,DateFormat.getDateTimeInstance().format(cur.getModified()));
				item.setData(cur);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			table.setRedraw(true);
			shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		}

		if (table.getItemCount() > 0)
			table.select(0);

		listLoaded = true;
	}

	/**
	 * This Method is designed to sort an ArrayList of SymitarFile, given the sort column and direction.
	 * If the sort column is not size or date, it will be sorted by name.
	 * @param ArrayList&lt;SymitarFile&gt;
	 * @param sortColumn column to sort by
	 * @param sortDirection sort asscending or decending.
	 * @return ArrayList&lt;SymitarFile&gt;
	 */
	public ArrayList<SymitarFile> sortFileList(ArrayList<SymitarFile> fileList, TABLE_COLUMN sortColumn, int sortDirection){
		for (int i = 1; i < fileList.size(); i++) {
			SymitarFile file1 = fileList.get(i);
			int diff;
			for (int j = 0; j < i; j++){
				SymitarFile file2 = fileList.get(j);
				// Compare the File Info
				if(sortColumn == TABLE_COLUMN.SIZE){
					if(file1.getSize() > file2.getSize())
						diff = 1;
					else if(file1.getSize() < file2.getSize())
						diff = -1;
					else diff = 0;
				}
				else if(sortColumn == TABLE_COLUMN.DATE){
					diff = file1.getModified().compareTo(file2.getModified());
				}
				else{
					diff = file1.getName().compareTo(file2.getName());
				}
				
				// Shift in array
				if(sortDirection != SWT.DOWN){
					if(diff < 0){
						fileList.remove(i);
						fileList.add(j, file1);
						break;
					}
				}
				else{
					if(diff > 0){
						fileList.remove(i);
						fileList.add(j, file1);
						break;
					}
				}
			}
		}
		return fileList;
            
	}
	
	public ArrayList<SymitarFile> open() {
		create();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return files;
	}
}
