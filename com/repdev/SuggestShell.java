package com.repdev;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import com.repdev.SyntaxHighlighter.*;
import com.repdev.parser.*;
import com.repdev.DatabaseLayout.*;


/**
 * Suggestion box for help as you type in Repgens
 * Only one instance is ever made, and gets attached to currently open tab
 * 
 * @author Jake Poznanski
 *
 */
public class SuggestShell {
	private Shell shell,tooltip;
	private StyledText toolText;
	private Table table;
	private StyledText txt;
	private RepgenParser parser;
	private boolean open = false;
	private Token current;

	public void open() {
		open = true;
			
		if( update() ){
			shell.setVisible(true);
			tooltip.setVisible(true);
			shell.moveAbove(txt);
			txt.setFocus();
		}
	}

	private boolean update() {
		String tokenStr = "";
		Point loc = shell.getDisplay().map(txt, null, txt.getLocationAtOffset(txt.getCaretOffset()));
		loc.x += 5;
		loc.y += 20;
		shell.setLocation(loc);
		tooltip.setLocation(shell.getLocation().x+shell.getSize().x, shell.getLocation().y);

		if (parser == null || parser.getLtokens() == null)
			return false;

		current = null;

		for (Token t : parser.getLtokens()) {
			if (t.getEnd() == txt.getCaretOffset()) {
				current = t;
				break;
			}
		}

		table.removeAll();

		if (current == null)
			tokenStr = "";
		else
			tokenStr = current.getStr();

		table.setRedraw(false);

		// add DB subfields, if we are on that
		if (current != null && (tokenStr.equals(":") || (current.getBefore() != null && current.getBefore().getStr().equals(":")))) {
			Token record = current.getBefore();

			if ((current.getBefore() != null && current.getBefore().getStr().equals(":")))
				record = record.getBefore();

			if (record == null)
				return false;

			DatabaseLayout.Record dRecord = null;

			if (DatabaseLayout.getInstance().containsRecordName(record.getStr())) {
				for (DatabaseLayout.Record cur : DatabaseLayout.getInstance().getFlatRecords()) {
					if (cur.getName().toLowerCase().equals(record.getStr())) {
						dRecord = cur;
						break;
					}
				}

				ArrayList<Field> sortedFields = dRecord.getFields();

				Collections.sort(sortedFields);

				for (DatabaseLayout.Field field : dRecord.getFields()) {
					if (tokenStr.equals(":") || field.getName().toLowerCase().startsWith(tokenStr)) {
						TableItem item = new TableItem(table, SWT.NONE);
						item.setText(field.getName().toUpperCase() + "   " + field.getDataType());
						item.setImage(RepDevMain.smallDBFieldImage);
						item.setData("value", field.getName().toUpperCase());
						String tooltip = field.getName().toUpperCase() + "\nType: " + field.getDataType() + (field.getLen() != -1 ? "(" + field.getLen() + ")" : "" ) + "\nField Number: " + field.getFieldNumber() + "\n\n" + field.getDescription();
						
						item.setData("tooltip", tooltip) ;
						StyleRange[] styles = {
								new StyleRange(0,field.getName().length(),null,null,SWT.BOLD),
								new StyleRange(field.getName().length()+1,tooltip.indexOf("\n\n") - field.getName().length(),null,null,SWT.ITALIC),
						};
						item.setData("tooltipstyles", styles);
					}
				}
			}
		} else {
			// Otherwise, let's populate with variable names, db records, etc.
			if (tokenStr.equals("=") || tokenStr.equals(":"))
				tokenStr = "";
			
			ArrayList<Variable> vars = new ArrayList<Variable>(parser.getLvars());
			
			//Sort of a bit application specific, but only show the many "@" variables if you've already typed an @, so not to over crowd the list
			//Other special vars will always show up
			
			for( Object cur :RepgenParser.getSpecialvars().toArray())
				if( cur instanceof String && ((((String)cur).startsWith("@") && tokenStr.startsWith("@")) || !((String)cur).startsWith("@")))
					vars.add(new Variable((String)cur,"Special Variable",-1,""));
				
			Collections.sort(vars);
			
			

			for (Variable var : vars) {
				if (var.getName().toLowerCase().startsWith(tokenStr)) {
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(var.getName().toUpperCase() + "   " + (var.isConstant() ? var.getType() : var.getType().toUpperCase()));
					item.setImage(RepDevMain.smallVariableImage);
					item.setData("value", var.getName().toUpperCase());
					
					String tooltip = var.getName().toUpperCase();
					
					if( var.isConstant())
						tooltip += "\nConstant Value: " + var.getType();
					else
						tooltip += "\nType: " + var.getType().toUpperCase();
					
					tooltip += "\nFile: " + var.getFilename();
					
					item.setData("tooltip", tooltip);
					
					StyleRange[] styles = {
							new StyleRange(0,var.getName().length(),null,null,SWT.BOLD),
							
					};
					item.setData("tooltipstyles", styles);
				}
			}

			ArrayList<Record> records = DatabaseLayout.getInstance().getFlatRecords();
			for (Record record : records) {
				if (record.getName().toLowerCase().startsWith(tokenStr)) {
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(record.getName().toUpperCase());
					item.setImage(RepDevMain.smallDBRecordImage);
					item.setData("value", record.getName().toUpperCase());
					
					String tooltip = record.getName().toUpperCase() + "\nParent: " + ( record.getRoot() == null ? "None" : record.getRoot().getName() ) + "\n\n" + record.getDescription();
					
					item.setData("tooltip", tooltip) ;
					
					StyleRange[] styles = {
							new StyleRange(0,record.getName().length(),null,null,SWT.BOLD),
							new StyleRange(record.getName().length()+1,tooltip.indexOf("\n\n") - record.getName().length(),null,null,SWT.ITALIC),
					};
					item.setData("tooltipstyles", styles);
				}
			}

		}


		table.setRedraw(true);
		table.setSelection(0);
				
		refreshTooltip();
		
		if( table.getItemCount() == 0){
			close();
			return false;
		}
		
		return true;
	}

	public void attach(StyledText txt, RepgenParser parser) {
		if (txt == null || parser== null || shell == null || this.txt != txt || this.parser != parser|| shell.isDisposed()) {
			this.txt = txt;
			this.parser = parser;
			createNewShell();
		}
	}

	public void close() {
		open = false;
		shell.setVisible(false);
		tooltip.setVisible(false);
	}

	private void createNewShell() {
		if (shell != null && !shell.isDisposed()){
			shell.dispose();
			tooltip.dispose();
		}

		shell = new Shell(txt.getDisplay(), SWT.BORDER | SWT.ON_TOP);
		shell.setVisible(false);
		shell.setLayout(new FillLayout());
		
		tooltip = new Shell(txt.getDisplay(),SWT.BORDER | SWT.ON_TOP  );
		tooltip.setVisible(false);
		tooltip.setLayout(new FillLayout());
		
		toolText = new StyledText(tooltip,SWT.READ_ONLY);
		toolText.setText("Testing Tooltip");
		toolText.setBackground(new Color(shell.getDisplay(),new RGB(255,255,225)));

		if( txt.getData("suggestlistenersloaded") == null){
			txt.setData("suggestlistenersloaded", true);
			
			txt.addKeyListener(new KeyListener() {
	
				public void keyPressed(KeyEvent e) {
					if (open && (e.keyCode == SWT.ESC || e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT))
						close();
					
					if (open && e.keyCode != SWT.ARROW_DOWN && e.keyCode != SWT.ARROW_UP && e.keyCode != SWT.PAGE_DOWN  && e.keyCode != SWT.PAGE_UP && e.keyCode != SWT.SHIFT)
						update();
					
	
					if ((e.character == ' ' && e.stateMask == SWT.CTRL) || e.character == ':') {
						open();
					}
	

				}
	
				public void keyReleased(KeyEvent e) {
					// TODO Auto-generated method stub
	
				}
	
			});
	
			txt.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					if (open)
						close();
				}
			});
	
			txt.addFocusListener(new FocusAdapter() {
				public void focusGained(FocusEvent e){
					
				}
	
				public void focusLost(FocusEvent e) {
	
					if (open && !(shell.isFocusControl() || table.isFocusControl())) {
						close();
						txt.setFocus();
					}
				}
	
			});
	
			txt.addVerifyKeyListener(new VerifyKeyListener() {
				public void verifyKey(VerifyEvent e) {
	
					if (open) {
	
						if (e.keyCode == SWT.ARROW_DOWN) {
							table.setSelection(Math.min(table.getSelectionIndex() + 1, table.getItemCount() - 1));
							refreshTooltip();
							e.doit = false;
						} else if (e.keyCode == SWT.ARROW_UP) {
							table.setSelection(Math.max(table.getSelectionIndex() - 1, 0));
							refreshTooltip();
							e.doit = false;
						} else if (e.keyCode == SWT.PAGE_DOWN ){
							table.setSelection(Math.min(table.getSelectionIndex() + 8, table.getItemCount() - 1));
							refreshTooltip();
							e.doit = false;
						} else if (e.keyCode == SWT.PAGE_UP){
							table.setSelection(Math.max(table.getSelectionIndex() - 8, 0));
							refreshTooltip();
							e.doit = false;
						} else if ((e.keyCode == '\r' || e.character == ':') && table.getSelectionIndex() != -1 && table.getItemCount() > 0) {
							String value = (String) table.getSelection()[0].getData("value");
	
							if (e.character == ':')
								value += ":";
	
							if (current != null && current.getStr().equals(":")) {
								txt.replaceTextRange(txt.getCaretOffset(), 0, value);
								txt.setCaretOffset(txt.getCaretOffset() + value.length());
							} else {
								int len = 0;
								if (current != null && !current.getStr().equals(":") && !current.getStr().equals("="))
									len = current.getStr().length();
	
								txt.replaceTextRange(txt.getCaretOffset() - len, len, value);
	
								if (len == 0)
									txt.setCaretOffset(txt.getCaretOffset() + value.length());
							}
	
							e.doit = false;
	
							if (e.keyCode == '\r')
								close();
						}
	
					}
				}
	
			});
		}

		table = new Table(shell, SWT.SINGLE);

		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				refreshTooltip();
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				if (open && e.item != null) {
					Event event = new Event();
					event.keyCode = '\r';
					txt.notifyListeners(SWT.KeyDown, event);
					
				}
			}
		});

		shell.setSize(280, 180);
		tooltip.setSize(200,120);

		// shell.open();
		txt.setFocus();
	}
	
	public void refreshTooltip(){
		if( table.getSelectionIndex() == -1){
			toolText.setText("");
			return;
		}
		
		if( table.getSelection()[0].getData("tooltip") != null ){
			toolText.setText((String)table.getSelection()[0].getData("tooltip"));
			toolText.setStyleRanges((StyleRange[])table.getSelection()[0].getData("tooltipstyles"));
		}
		else
			toolText.setText("");
	}

}
