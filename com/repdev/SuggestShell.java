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

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.repdev.parser.Argument;
import com.repdev.parser.DatabaseLayout;
import com.repdev.parser.Field;
import com.repdev.parser.Function;
import com.repdev.parser.FunctionLayout;
import com.repdev.parser.Record;
import com.repdev.parser.RepgenParser;
import com.repdev.parser.SpecialVariable;
import com.repdev.parser.Token;
import com.repdev.parser.Variable;

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
	private boolean open = false, snippetMode = false;
	private Token current;
	private EditorComposite editor;
	
	public void open(){
		open(false);
	}

	public void open(boolean inSnippet) {
		open = true;
		snippetMode = inSnippet;
			
		if( update() ){
			txt.setFocus();
			shell.setVisible(true);
			tooltip.setVisible(true);
			shell.moveAbove(txt);
			
		}
	}

	private boolean update() {
		String tokenStr = "";
		Token before = null;
			
		Point loc = shell.getDisplay().map(txt, null, txt.getLocationAtOffset(txt.getCaretOffset()));
		loc.x += 5;
		loc.y += 20;
		
		if( loc.x + shell.getBounds().width > shell.getMonitor().getBounds().width )
			loc.x = shell.getMonitor().getBounds().width - shell.getBounds().width;
		
		shell.setLocation(loc);
		
		loc = new Point(shell.getBounds().x+shell.getBounds().width,shell.getBounds().y);
		
		if( loc.x + tooltip.getBounds().width > shell.getMonitor().getBounds().width )
			loc.x = Math.max(0, shell.getBounds().x - tooltip.getBounds().width);
		
		tooltip.setLocation(loc);

		if (parser == null || parser.getLtokens() == null)
			return false;

		current = null;

		for (Token t : parser.getLtokens()) {
			if(t.getEnd() <= txt.getCaretOffset())
				before = t;
			
			if (t.getEnd() == txt.getCaretOffset()) {
				current = t;
				break;
			}
		}

		//Do not open window if we are in comments/strings etc.
		if( before != null && ( 
				( before.inDate() && !before.getStr().equals("'")) || 
				( before.inString() && !before.getStr().equals("\"") ) ||
				( before.getCDepth() != 0 && !before.getStr().equals("]") ) ) ) 
			return false;
		
		table.removeAll();
		

		if (current == null)
			tokenStr = "";
		else
			tokenStr = current.getStr();

		table.setRedraw(false);

		if(snippetMode){
			if( SnippetManager.getInstance().snippets != null && SnippetManager.getInstance().snippets.size() > 0){
				//Add snippets
				for( Snippet cur : SnippetManager.getInstance().snippets){

					TableItem item = new TableItem(table, SWT.NONE);
					ArrayList<StyleRange> ranges = new ArrayList<StyleRange>();

					item.setText(cur.getTitle());
					item.setImage(RepDevMain.smallVariableImage);
					item.setData("snippet", cur);
					item.setData("value","");

					String tooltip = cur.getTitle()+"\n";
					ranges.add(new StyleRange(0,tooltip.length(),null,null,SWT.BOLD));

					tooltip += cur.getDescription() + "\n\n";
					ranges.add(new StyleRange(tooltip.length() - cur.getDescription().length(), cur.getDescription().length(),null,null));


					ranges.add(new StyleRange(tooltip.length(),11,null,null,SWT.BOLD));
					tooltip += "Variables:\n";

					for(SnippetVariable var : cur.getVars()){
						ranges.add(new StyleRange(tooltip.length(), 1 + var.getId().length(),null,null,SWT.BOLD));
						tooltip += "\t" + var.getId();

						ranges.add(new StyleRange(tooltip.length(), 3 + var.getTooltip().length(),null,null));
						tooltip += ": " + var.getTooltip() + "\n";

					}

					item.setData("tooltip", tooltip);


					item.setData("tooltipstyles", ranges.toArray(new StyleRange[0]));
				}

			}
			else
				snippetMode = false;
		}
		else{
			// add DB subfields, if we are on that
			if (current != null && (tokenStr.equals(":") || (current.getBefore() != null && current.getBefore().getStr().equals(":")))) {
				Token record = current.getBefore();

				if ((current.getBefore() != null && current.getBefore().getStr().equals(":")))
					record = record.getBefore();

				if (record == null)
					return false;

				Record dRecord = null;

				if (DatabaseLayout.getInstance().containsRecordName(record.getStr())) {
					for (Record cur : DatabaseLayout.getInstance().getFlatRecords()) {
						if (cur.getName().toLowerCase().equals(record.getStr())) {
							dRecord = cur;
							break;
						}
					}

					ArrayList<Field> sortedFields = dRecord.getFields();

					Collections.sort(sortedFields);

					for (Field field : dRecord.getFields()) {
						if (tokenStr.equals(":") || field.getName().toLowerCase().startsWith(tokenStr)) {
							TableItem item = new TableItem(table, SWT.NONE);
							item.setText(field.getName().toUpperCase() + "   " + field.getVariableType());
							item.setImage(RepDevMain.smallDBFieldImage);
							item.setData("value", field.getName().toUpperCase());
							String tooltip = field.getName().toUpperCase() + "\nType: " + field.getVariableType() + (field.getLen() != -1 ? "(" + field.getLen() + ")" : "" ) + "\nField Number: " + field.getFieldNumber() + "\n\n" + field.getDescription();

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

				//Special system vars next
				//Sort of a bit application specific, but only show the many "@" variables if you've already typed an @, so not to over crowd the list
				//Other special vars will always show up

				for( SpecialVariable var :RepgenParser.getSpecialvars().getVars())
					if( (!tokenStr.equals("") && var.getName().toLowerCase().startsWith(tokenStr)) || 
							(tokenStr.equals("") && !var.getName().startsWith("@") ) )
					{
						TableItem item = new TableItem(table, SWT.NONE);
						item.setText(var.getName().toUpperCase() + "   " + var.getType());
						item.setImage(RepDevMain.smallVariableImage);
						item.setData("value", var.getName().toUpperCase());

						String tooltip = var.getName().toUpperCase();

						tooltip += "\nType: " + var.getType().toUpperCase();
						tooltip += "\nSystem Variable\n\n";
						tooltip += var.getDescription();

						item.setData("tooltip", tooltip);

						StyleRange[] styles = {
								new StyleRange(0,var.getName().length(),null,null,SWT.BOLD),
								new StyleRange(tooltip.indexOf("System Variable"),15, null,null,SWT.ITALIC ),

						};
						item.setData("tooltipstyles", styles);
					}

				//Add functions
				//TODO: SHow tip once you start typing arguments
				String funcName = tokenStr;
				if( tokenStr.equals("(") && current.getBefore() != null )
					funcName = current.getBefore().getStr();

				for( Function func : FunctionLayout.getInstance().getList()){
					if( func.getName().toLowerCase().startsWith(funcName)){
						TableItem item = new TableItem(table,SWT.NONE);
						String nameText = func.getName().toUpperCase() + "(";
						ArrayList<StyleRange> ranges = new ArrayList<StyleRange>();

						for( Argument arg : func.getArguments())
							nameText += arg.getShortName() + ", ";

						nameText = nameText.substring(0,nameText.length()-2) + ")";

						item.setText(nameText);
						item.setImage(RepDevMain.smallFileImage);
						item.setData("value", func.getName().toUpperCase() + "(");

						String tooltip = func.getName().toUpperCase() + "\n";
						ranges.add(new StyleRange(0,tooltip.length(),null,null,SWT.BOLD));

						tooltip += func.getDescription() + "\n\n";
						ranges.add(new StyleRange(ranges.get(ranges.size()-1).start + ranges.get(ranges.size()-1).length, (func.getDescription()).length(),null,null,SWT.ITALIC));

						tooltip += "Arguments:\n";
						ranges.add(new StyleRange(ranges.get(ranges.size()-1).start + ranges.get(ranges.size()-1).length, 12,null,null,SWT.BOLD));


						for( Argument arg : func.getArguments()){
							ranges.add(new StyleRange(tooltip.length(), arg.getShortName().length() + 1,null,null,SWT.BOLD));
							tooltip += "\t" + arg.getShortName() + " - " + arg.getDescription() + " " + arg.getTypes() + "\n";						
						}

						tooltip += "\nReturns: ";
						ranges.add(new StyleRange(tooltip.length()-11, 11,null,null,SWT.BOLD));

						tooltip += func.getReturnTypes();

						item.setData("tooltip",tooltip);

						item.setData("tooltipstyles", ranges.toArray(new StyleRange[0]));

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

	public void attach(EditorComposite editor, RepgenParser parser) {
		StyledText txt = editor.getStyledText();

		this.editor = editor;
		
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
		
		tooltip = new Shell(txt.getDisplay(),SWT.BORDER | SWT.ON_TOP);
		tooltip.setVisible(false);
		tooltip.setLayout(new FillLayout());
		
		toolText = new StyledText(tooltip,SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
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
					
	
					if ((e.character == ' ' && e.stateMask == SWT.CTRL) || e.character == ':' || e.character == '@') {
						if( open && (e.character == ' ' && e.stateMask == SWT.CTRL)){
							snippetMode = !snippetMode;
							update();
						}
						else
							open();
					}
	

				}
	
				public void keyReleased(KeyEvent e) {
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
					if (open) {
						//This is a weird workaround, we must filter the next focus in message to see where the focus went and if we need to close the window. Since the methods for getting current focus no longer update until this listener finishes 
						
						Display.getCurrent().addFilter(SWT.FocusIn, new Listener(){

							public void handleEvent(Event event) {
								System.out.println(event);
								if( event.widget != table && event.widget != shell && event.widget != tooltip && event.widget != toolText){
									close();			
								}
								
								Display.getCurrent().removeFilter(SWT.FocusIn, this);
							}
							
						});
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
							
							
							//Actual replacement code
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
							
							if( snippetMode ){							
								e.doit = false;
								editor.activateSnippet((Snippet) table.getSelection()[0].getData("snippet"));
								close();					
								return;
							}
	
							e.doit = false;
	
							if (e.keyCode == '\r')
								close();
						}
	
					}
				}
	
			});
		}
		
		if( !txt.getShell().isListening(SWT.Resize) || !txt.getShell().isListening(SWT.Move) || !txt.getShell().isListening(SWT.Iconify)){
			txt.getShell().addControlListener(new ControlListener(){
	
				public void controlMoved(ControlEvent e) {
					close();
				}
	
				public void controlResized(ControlEvent e) {
					close();
				}
				
			});
			
			txt.getShell().addShellListener(new ShellAdapter(){

				public void shellIconified(ShellEvent e) {
					close();
				}

				@Override
				public void shellDeactivated(ShellEvent e) {
					//TODO: Add some code here to close the suggest shell if the main program loses focus, sort of hard to do
				}

				@Override
				public void shellDeiconified(ShellEvent e) {
					//close();
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
		
		table.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e) {
				if( open && (e.character == ' ' && e.stateMask == SWT.CTRL)){
					snippetMode = !snippetMode;
					update();
					e.doit = false;
				}
			}

			public void keyReleased(KeyEvent e) {
			}
			
		});

		shell.setSize(280, 180);
		tooltip.setSize(250,180);

		// shell.open();
		txt.setFocus();
	}
	
	public void refreshTooltip(){
		if( table.getSelectionIndex() == -1){
			toolText.setText("");
			return;
		}
		
		if( table.getSelection()[0].getData("tooltip") != null ){
			if( table.getSelection()[0].getData("tooltip") != null )
				toolText.setText((String)table.getSelection()[0].getData("tooltip"));
			
			if( table.getSelection()[0].getData("tooltipstyles") != null)
				toolText.setStyleRanges((StyleRange[])table.getSelection()[0].getData("tooltipstyles"));
		}
		else
			toolText.setText("");
	}

}
