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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


public class ReportComposite extends Composite implements TabTextView{
	private StyledText txt;
	private Table table;
	private SymitarFile file = null;
	private Sequence seq;
	private CTabItem tabItem;
	private int sym;

	/**
	 * Either send a single report to view as a SymitarFile, or a batch seq to view a bnch from the same run
	 * @param parent
	 * @param file
	 * @param sym
	 * @param seq
	 */
	public ReportComposite(Composite parent, CTabItem item, SymitarFile file) {
		super(parent, SWT.NONE);
		this.file = file;
		this.sym = file.getSym();
		this.tabItem = item;
		
		buildGUI();
	}
	
	public ReportComposite(Composite parent, CTabItem item, Sequence seq) {
		super(parent, SWT.NONE);
		this.seq = seq;
		this.sym = seq.getSym();
		this.tabItem = item;
		
		buildGUI();
	}
	
	public StyledText getStyledText(){
		return txt;
	}

	private void buildGUI() {
		setLayout(new FormLayout());
		
		txt = new StyledText(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		txt.setFont(new Font(Display.getCurrent(), "Courier New", 9, SWT.NORMAL));
		txt.setBackground(new Color(Display.getCurrent(),new RGB(255,255,225)));
		
		txt.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {				
				if (e.stateMask == SWT.CTRL) {
					switch (e.keyCode) {
					case 'a':
					case 'A':
						txt.selectAll();
						break;
					case 'f':
					case 'F':
						RepDevMain.mainShell.showFindWindow();
						break;
					case 'p':
					case 'P':
						RepDevMain.mainShell.print();
						break;
					}
				}
				else{
					if( e.keyCode == SWT.F3 )
						RepDevMain.mainShell.findNext();
				}


			}

			public void keyReleased(KeyEvent e) {

			}
		});
		
		table = new Table(this, SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				openTableItem();
			}			
		});
		
		TableColumn col = new TableColumn(table,SWT.NONE);
		col.setText("Title");
		col.setWidth(230);
		
		col = new TableColumn(table,SWT.NONE);
		col.setText("Sequence");
		col.setWidth(70);
		
		col = new TableColumn(table,SWT.NONE);
		col.setText("Pages");
		col.setWidth(50);
		
		col = new TableColumn(table,SWT.NONE);
		col.setText("Size");
		col.setWidth(70);
		
		col = new TableColumn(table,SWT.NONE);
		col.setText("Date");
		col.setWidth(150);
		
		col = new TableColumn(table,SWT.NONE);
		col.setText("Options");
		col.setWidth(200);
		
		FormData data = new FormData();
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.top = new FormAttachment(0);
		data.height = 48;
		table.setLayoutData(data);

		FormData frmTxt = new FormData();
		frmTxt.top = new FormAttachment(table);
		frmTxt.left = new FormAttachment(0);
		frmTxt.right = new FormAttachment(100);
		frmTxt.bottom = new FormAttachment(100);
		txt.setLayoutData(frmTxt);

		
		if( file != null){
			txt.setText(file.getData());
			
			TableItem row = new TableItem(table,SWT.NONE);
			row.setText(0, "");
			row.setText(1, file.getName());
		}
		else
		{
			for( final PrintItem item : RepDevMain.SYMITAR_SESSIONS.get(sym).getPrintItems(seq)){
				TableItem row = new TableItem(table,SWT.NONE);
				row.setText(0, item.getTitle());
				row.setText(1, String.valueOf(item.getSeq()));
				row.setText(2, String.valueOf(item.getPages()));
				row.setText(3, Util.getByteStr(item.getSize()));
				row.setText(4, DateFormat.getDateTimeInstance().format(item.getDate()));
				
				TableEditor editor = new TableEditor(table);
				editor.grabHorizontal=true;
				editor.grabVertical=true;
				
				Composite labelComposite = new Composite(table,SWT.NONE);
				FillLayout layout = new FillLayout();
				labelComposite.setLayout(layout);
				
				Link printLocal = new Link(labelComposite,SWT.NONE);
				printLocal.setText("Print: <a href=\"local\">Local</a> <a href=\"lpt\">Host LPT</a>  <a href=\"fm\">Run as FM</a>");
				printLocal.setBackground(table.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
				printLocal.addSelectionListener(new SelectionAdapter(){

					@Override
					public void widgetSelected(SelectionEvent e) {
						if( e.text.equals("local")){
							openTableItem(item);
							RepDevMain.mainShell.print();
						}
						else if(e.text.equals("lpt"))
							LPTPrintShell.print(getDisplay(), getShell(), new SymitarFile(sym,String.valueOf(item.getSeq()),FileType.REPORT));
						else if( e.text.equals("fm"))
							runFM(item);
					}
					
				});
				
				editor.setEditor(labelComposite, row, 5);
				
				row.setData(item);
			}
		}
		
		if( table.getItemCount() > 0 ){
			table.setSelection(0);
			openTableItem();
		}
		else
			txt.setText("Error loading file");
	}
	
	protected void runFM(PrintItem item) {
		//RunFMResult result = RepDevMain.SYMITAR_SESSIONS.get(sym).runBatchFM(item.getTitle(), SymitarSession.FMFile.ACCOUNT, -1);
		
		//System.out.println("FM Name: " + result.getResultTitle());
		//System.out.println("Queue Seq: " + result.getSeq());
		
		FMShell.runFM(getDisplay(), getShell(), sym, item.getTitle());
	}

	protected void openTableItem(PrintItem item) {
		String data = new SymitarFile(sym,String.valueOf(item.getSeq()),FileType.REPORT).getData();
	
		if( data != null)
			txt.setText( data);
	}

	private void openTableItem(){
		PrintItem item = null;
		
		if( table.getSelection()[0].getData() == null )
			return;
		else
			item = (PrintItem)table.getSelection()[0].getData();
		
		openTableItem(item);
	}
}
