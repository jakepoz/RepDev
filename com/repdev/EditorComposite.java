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
import java.util.HashMap;
import java.util.Stack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEffect;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;

import com.repdev.parser.Formatter;
import com.repdev.parser.Include;
import com.repdev.parser.RepgenParser;
import com.repdev.parser.Token;
import com.repdev.parser.BackgroundSectionParser;
import com.repdev.parser.SectionInfo;
import com.repdev.parser.Variable;
import com.repdev.parser.Token.TokenType;
import org.eclipse.swt.graphics.GlyphMetrics;

/**
 * Main editor for repgen, help, and letter files
 * Provides syntax highlighting and other advanced features for the repgen files, basically all the text editor stuff is in this class
 * 
 * @author Jake Poznanski
 *
 */
public class EditorComposite extends Composite implements TabTextEditorView {
	private SymitarFile file;
	private int sym;
	private Color lineBackgroundColor, blockMatchColor;
	private StyledText txt;
	private CTabItem tabItem;

	// For Section Infos
	private BackgroundSectionParser sec;
	private int prevTxtLine = -1; // Used by the handleCaretChange method to determine if the cursor moved to another line.

	private static final int UNDO_LIMIT = 1000;
	private Stack<TextChange> undos = new Stack<TextChange>();
	private Stack<TextChange> redos = new Stack<TextChange>();

	// 1 = Regular
	// 2 = Undoing, so save as redos
	// 0 = Ignore all
	private int undoMode = 0;

	private int lastLine = 0;
	private SyntaxHighlighter highlighter;
	private RepgenParser parser;
	private boolean modified = false;
	private boolean doParse = true;

	//Snippet Mode Variables
	private boolean snippetMode = false;
	private Snippet currentSnippet = null;
	private SnippetVariable currentEditVar = null;
	private int currentEditVarPos = -1;
	private int snippetStartPos = -1;

	private final static Color SNIPPET_VAR = new Color(Display.getCurrent(),new RGB(170,185,220)); //Generic Snippet Var
	private final static Color SNIPPET_VAR_CURRENT = new Color(Display.getCurrent(),new RGB(180,215,255));  //All other instances of current one you are editing
	private final static Color SNIPPET_VAR_EDITING = new Color(Display.getCurrent(),new RGB(180,215,255)); //Current one you are editing
	
	private Token startBlockToken;
	private Token endBlockToken;
	
	static SuggestShell suggest = new SuggestShell();

	private static Font DEFAULT_FONT;
	private boolean showLineNumbers = false;

	static {
		Font cur = null;

		try {
			cur = new Font(Display.getCurrent(), "Courier New", 11, SWT.NORMAL);
		} catch (Exception e) {
		}

		DEFAULT_FONT = cur;
	}

	class TextChange {
		private int start, length, topIndex;
		private String replacedText;
		private boolean commit;

		public TextChange(boolean commit) {
			this.commit = commit;
		}
		public TextChange(int start, int length, String replacedText, int topIndex) {
			this.start = start;
			this.length = length;
			this.replacedText = replacedText;
			this.topIndex = topIndex;
			this.commit = false;
		}

		public int getTopIndex(){
			return topIndex;
		}

		public boolean isCommit() {
			return commit;
		}

		public int getStart() {
			return start;
		}

		public int getLength() {
			return length;
		}

		public String getReplacedText() {
			return replacedText;
		}
	}

	public EditorComposite(Composite parent, CTabItem tabItem, SymitarFile file) {
		super(parent, SWT.NONE);
		this.file = file;
		this.tabItem = tabItem;
		this.sym = file.getSym();
		this.showLineNumbers = Config.getViewLineNumbers();

		buildGUI();
	}

	public boolean canUndo(){
		return undos.size() > 0 && !snippetMode;
	}

	public boolean canRedo(){
		return redos.size() > 0 && !snippetMode;	
	}

	public void undo() {
		if( !canUndo() )
			return;

		try {
			TextChange change;

			if (!undos.empty()) {
				if (undos.peek().isCommit() == true)
					undos.pop();

				undoMode = 2;

				//Ok, I am only allowing the last undo in the redo stack
				//redos.clear();

				txt.setRedraw(false);

				if( parser != null)
					parser.setReparse(false);



				while (!(undos.size() == 0 || (change = undos.pop()).isCommit())) {
					txt.replaceTextRange(change.getStart(), change.getLength(), change.getReplacedText());
					txt.setCaretOffset(change.getStart());
					txt.setTopIndex(change.getTopIndex());

				}

				redos.push(new TextChange(true));
			}
		} catch (Exception e) {
			MessageBox dialog = new MessageBox(this.getShell(), SWT.ICON_ERROR | SWT.OK);
			dialog.setMessage("The Undo Manager has failed during an Undo!");
			dialog.setText("ERROR!");
			dialog.open();

			e.printStackTrace();
		}
		finally{
			undoMode = 1;
			txt.setRedraw(true);
			if( parser != null){
				parser.setReparse(true);
				parser.reparseAll();
			}

			lineHighlight();
		}
	}

	public void redo() {
		if( !canRedo() )
			return;

		try {
			TextChange change;

			if (!redos.empty()) {
				if (redos.peek().isCommit() == true)
					redos.pop();

				undoMode = 1;
				txt.setRedraw(false);

				if( parser != null)
					parser.setReparse(false);

				while (!(redos.size() == 0 || (change = redos.pop()).isCommit())) {
					txt.replaceTextRange(change.getStart(), change.getLength(), change.getReplacedText());
					txt.setCaretOffset(change.getStart());
					txt.setTopIndex(change.getTopIndex());
				}
				undos.push(new TextChange(true));
			}
		} catch (Exception e) {
			MessageBox dialog = new MessageBox(this.getShell(), SWT.ICON_ERROR | SWT.OK);
			dialog.setMessage("The Undo Manager has failed during a Redo!");
			dialog.setText("ERROR!");
			dialog.open();

			e.printStackTrace();
		}
		finally{
			undoMode = 1;
			txt.setRedraw(true);
			if( parser != null){
				parser.setReparse(true);
				parser.reparseAll();
			}

			lineHighlight();
		}
	}

	public void commitUndo() {
		if (undos.size() == 0 || !undos.peek().isCommit())
			undos.add(new TextChange(true));
	}

	public void setLineColor(SyntaxHighlighter hiColor){
		lineBackgroundColor=hiColor.getLineColor();
		blockMatchColor=hiColor.getBlockMatchColor();
	}

	public void lineHighlight() {
		try {
			int start, end, currentLine;

			txt.setLineBackground(0, txt.getLineCount(), txt.getBackground());

			currentLine = txt.getLineAtOffset(txt.getCaretOffset());

			if (txt.getSelectionText().indexOf("\n") == -1)
				txt.setLineBackground(currentLine, 1, lineBackgroundColor);

			if( lastLine <= txt.getLineCount() -1 )
				start = txt.getOffsetAtLine(lastLine);
			else
				start = txt.getOffsetAtLine(txt.getLineCount()-1);

			end = txt.getOffsetAtLine(Math.min(txt.getLineCount() - 1, lastLine + 1));

			if (lastLine + 1 == txt.getLineCount())
				txt.redraw();
			else
				txt.redrawRange(start, end - start, true);

			start = txt.getOffsetAtLine(currentLine);
			end = txt.getOffsetAtLine(Math.min(txt.getLineCount() - 1, currentLine + 1));

			if (currentLine + 1 == txt.getLineCount())
				txt.redraw();
			else
				txt.redrawRange(start, end - start, true);

		} catch (Exception e) {
			System.err.println("Line Highlighter failed!!");
			e.printStackTrace();
		} finally {
			lastLine = txt.getLineAtOffset(txt.getCaretOffset());
		}
	}

	//Allow for unindenting single lines -- Fixed
	private void groupIndent(int direction, int startLine, int endLine) {
		String tabStr = getTabStr();

		if (endLine > txt.getLineCount() - 1 )
			endLine = Math.max(txt.getLineCount() - 1, startLine + 1);

		try {
			Point oldSelection = txt.getSelection();
			int offset = 0;

			if( parser != null)
				parser.setReparse(false);

			if (direction < 0) {
				for (int i = startLine; i <= endLine; i++) {
					int startOffset = txt.getOffsetAtLine(i);
					int endOffset;
					String line;

					if( i >= txt.getLineCount() - 1 ){
						endOffset = txt.getCharCount() ;
					}
					else
						endOffset = txt.getOffsetAtLine(i + 1);

					if( endOffset - 1 <= startOffset)
						line = "\n";
					else
						line = txt.getText(startOffset, endOffset - 1);		


//					for (int x = 0; x < Math.min(tabStr.length(), line.length()); x++)
//						if (line.charAt(x) > 32)
//							return;
				}
			}
			txt.setRedraw(false);
			int totalSpaces = 0;
			for (int i = startLine; i <= endLine; i++) {
				int startOffset = txt.getOffsetAtLine(i);
				int endOffset;
				String line;

				if( i >= txt.getLineCount() - 1 ){
					endOffset = txt.getCharCount() ;
				}
				else
					endOffset = txt.getOffsetAtLine(i + 1);

				if( endOffset - 1 <= startOffset)
					line = "\n";
				else
					line = txt.getText(startOffset, endOffset - 1);			
				int spaces = tabStr.length();

				if (direction > 0)
					txt.replaceTextRange(startOffset, endOffset - startOffset, tabStr + line);
				else {
					
					for (int x = 0; x < Math.min(tabStr.length(), line.length()); x++)
						if (line.charAt(x) > 32){
							spaces = x;
							break;
						}
					totalSpaces += spaces; // This is not currently being used
					
					txt.replaceTextRange(startOffset, endOffset - startOffset, line.substring(Math.min(spaces, line.length())));
				}

				offset += spaces * direction;

			}

			if( parser != null)
				parser.setReparse(true);

			oldSelection.y += offset;

			oldSelection.x = Math.max(oldSelection.x + tabStr.length() * direction, txt.getOffsetAtLine(startLine));

			//if( txt.getText().charAt(oldSelection.x) == '\n' && txt.getText().charAt(Math.max(0,oldSelection.x-1)) == '\r')
			//	oldSelection.x++;



			// TODO: This fails if you are right inbetween a /r
			// and /n, better fix it ;)
			txt.setSelection(oldSelection);


		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			txt.setRedraw(true);
			if( parser != null){
				parser.setReparse(true);
				parser.reparseAll();
			}
		}

	}

	/**
	 * Utility tabbing function
	 * @return
	 */
	public static  String getTabStr() {
		String tabStr = "";

		if (Config.getTabSize() == 0)
			tabStr = "\t";
		else
			for (int i = 0; i < Config.getTabSize(); i++)
				tabStr += " ";

		return tabStr;
	}

	public StyledText getStyledText(){
		return txt;
	}

	/*
	 * This Function turns highlighting of the editor composite on and off by either
	 * creating or destroying the syntax highlighter
	 */
	public void highlight(boolean parse, boolean firstRun){
		if(firstRun && parse){
			//Preventing bad errors, aka recreating the highlighter if it is not null
		}else if(parse){
			highlighter = new SyntaxHighlighter(parser);
			setLineColor(highlighter);
		}else{
			highlighter.highlight();
			highlighter = null;
		}
		if(!firstRun)doParse=parse;
		txt.redraw();
	}

	public boolean getHighlight(){
		return doParse;
	}

	public Color getLineColor(){
		return highlighter.getLineColor();
	}

	//Calculate and expand the width of numbered "bullet" margin.  Allows the whole number to be displayed as more lines are added to the file.
	final public int calcWidth(){
		if (this.showLineNumbers) {
			int lastLine = txt.getLineCount()+1;
			return (Integer.toString(lastLine).length() * 12) +6;
		}
		return 12; //return a width of 12px for "right click" implementation... eventually.
	}
	private void buildGUI() {
		setLayout(new FormLayout());

		txt = new StyledText(this, SWT.H_SCROLL | SWT.V_SCROLL);

		if (file.getType() == FileType.REPGEN){
			doParse=true;
			parser = new RepgenParser(txt, file, true);
//			if(parser.getIncludes().size() == 0){
//				for(EditorComposite editorComposite :RepDevMain.mainShell.getEditorCompositeList()){
//					for(Include inc : editorComposite.parser.getIncludes()){
//						if(inc.getFileName().equalsIgnoreCase(file.getName())){
//							parser = editorComposite.parser;
//						}
//					}
//				}
//			}
				
		}else{
			doParse=false;
			parser = new RepgenParser(txt, file, false);
			if( DEFAULT_FONT != null)
				txt.setFont(DEFAULT_FONT);
		}

		highlighter = new SyntaxHighlighter(parser);
		setLineColor(highlighter);

		final EditorComposite tempEditor = this;

		// Load the Section Info
		sec = new BackgroundSectionParser(parser.getLtokens(),txt.getText());

		txt.addDisposeListener(new DisposeListener(){

			public void widgetDisposed(DisposeEvent e) {
				if( parser != null)
					parser.cleanupTokenCache();
			}

		});

		txt.addFocusListener(new FocusListener(){

			public void focusGained(FocusEvent e) {
				suggest.attach(tempEditor, parser);
			}

			public void focusLost(FocusEvent e){
			}

		});

		suggest.attach(this, parser);

		txt.addTraverseListener(new TraverseListener() {

			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
					Event ev = new Event();
					ev.stateMask = SWT.SHIFT;
					ev.text = "\t";
					ev.start = txt.getSelection().x;
					ev.end = txt.getSelection().y;

					txt.notifyListeners(SWT.Verify, ev);

					e.detail = SWT.TRANSPARENCY_NONE;
				}
				//RepDevMain.mainShell.addToNavHistory(file, txt.getLineAtOffset(txt.getCaretOffset()));
				RepDevMain.mainShell.addToTabHistory();
			}

		});


		//Place any auto complete things in here
		txt.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				if (e.text.equals("\t")) {

					if(snippetMode){
						if( currentEditVarPos != -1){
							currentEditVarPos = (currentEditVarPos + 1) % currentSnippet.getNumberOfUniqueVars();
							currentEditVar = currentSnippet.getVar(currentEditVarPos);
							txt.setCaretOffset(snippetStartPos + currentSnippet.getVarEditPos(currentEditVarPos));
							updateSnippet();
							e.doit = false;
						}
						return;
					}

					int direction = (e.stateMask == SWT.SHIFT) ? -1 : 1;

					if (txt.getSelectionCount() > 1) {
						e.doit = false;

						int startLine = txt.getLineAtOffset(e.start);
						int endLine = txt.getLineAtOffset(e.end);

						groupIndent(direction, startLine, endLine);

					} else {
						e.doit = true;
						e.text = getTabStr();

						return;
					}
				}

				if (e.text.equals("\r\n")) {
					String indent = "";
					int posStart = txt.getOffsetAtLine(txt.getLineAtOffset(e.start));
					int posEnd = e.start;
					String lastLine = txt.getTextRange(posStart, posEnd - posStart);

					for (int i = 0; i < lastLine.length(); i++)
						if (lastLine.charAt(i) != ' ' && lastLine.charAt(i) != '\t')
							break;
						else
							indent += lastLine.charAt(i);

					e.text += indent;

					lineHighlight();
					commitUndo();
				}
			}
		});

		// Set the style of numbered bullets (12 pixels wide for each digit)
		final StyleRange style = new StyleRange();
		final int bulletStyle; 
		int bulletWidth = 12;
		if (showLineNumbers){
			bulletWidth = calcWidth();
			bulletStyle = ST.BULLET_NUMBER;
		} else{
			bulletStyle = ST.BULLET_TEXT;  //another eventual implementation for "right click" feature
		}
		style.foreground = highlighter.getBulletColor();
		style.start = 1;
		style.length = txt.getLineCount();
		style.metrics = new GlyphMetrics(0, 0, bulletWidth);
		
		// Add the style (numbered bullets) to the text in file. 
		txt.addLineStyleListener(new LineStyleListener() {
			public void lineGetStyle(LineStyleEvent e) {
				e.bulletIndex = txt.getLineAtOffset(e.lineOffset);
				if (showLineNumbers){
					style.metrics.width = calcWidth();
					e.bullet = new Bullet(bulletStyle, style);
				}
			}
		});
		
		// Add paint listener to modify numbered bullets, when lines are being added
		txt.addPaintListener(new PaintListener (){
			public void paintControl (PaintEvent e){
				
	            Rectangle clientArea = txt.getClientArea();
	            // To minimize the amount of page being redrawn, trying to limit it to just the numbered bullet margin area
				int width = calcWidth();
				Rectangle bgArea = new Rectangle(-2,-2,width,(txt.getLineCount()+1) * txt.getLineHeight());
				
				txt.getLineCount();
			}
		});

		txt.addExtendedModifyListener(new ExtendedModifyListener() {

			public void modifyText(ExtendedModifyEvent event) {
				lineHighlight();

				modified = true;
				updateModified();

				Stack<TextChange> stack = null;

				if (undoMode == 1)
					stack = undos;
				else if (undoMode == 2)
					stack = redos;

				if (undoMode != 0 ) {
					stack.push(new TextChange(event.start, event.length, event.replacedText, txt.getTopIndex()));

					if (stack.size() > UNDO_LIMIT)
						stack.remove(0);
				}



				if( snippetMode && currentEditVarPos != -1 ){ 
					int offset = 0;
					int end = event.start + event.length;
					int oldEnd = event.start + event.replacedText.length();
					int varStart, varEnd, oldLength = currentEditVar.getValue().length();
					int oldUndoMode = undoMode;
					boolean wasEdited = false;

					ArrayList<Integer> pos;

					if( end > oldEnd)
						offset = end - event.start;
					else
						offset = event.start - oldEnd;

					SnippetVariable var = currentEditVar;

					varStart = snippetStartPos + currentSnippet.getVarEditPos(currentEditVarPos);
					varEnd = varStart + var.getValue().length() + offset - 1;

					if( varStart == varEnd + 1) //Single edit var changed to "" 
						var.setValue("");
					else if( varStart > varEnd){ //End snippet mode, we've edited too much
						snippetMode = false;
						clearSnippetMode();
						return;
					}
					else if(!var.isEdited()){
						if( end-1<event.start)
							var.setValue("");
						else{						
							var.setValue(txt.getText(event.start,end-1));				
						}

						var.setEdited(true);
						wasEdited = true; //Make the replace loop below replace the edit var as well, since we changed it beyond what the user did
					}
					else
						var.setValue(txt.getText(varStart,varEnd));


					//Replace all the old variable values
					pos = currentSnippet.getLocations(currentEditVarPos);
					snippetMode = false;			


					for( int x = (wasEdited ? 0 : 2); x < pos.size(); x+=2 ){ //If it was edited, then we need to replace the original edit position as well
						if( x==0)
							undoMode = oldUndoMode;
						else
							undoMode = 0;

						txt.replaceTextRange(snippetStartPos + pos.get(x), oldLength + (x==0 ? event.length : 0), var.getValue()); //If we are changing the original edit position, compensate for the length of the string that we typed in
					}

					undoMode = oldUndoMode;

					snippetMode = true;			

					//commitUndo();
					// Drop Navigation Position
					RepDevMain.mainShell.addToNavHistory(file, txt.getLineAtOffset(txt.getCaretOffset()));
					updateSnippet();
				}

				// Redraw numbered bullets area
				if (showLineNumbers){
					style.metrics.width = calcWidth();
					txt.setStyleRange(style);
					txt.redraw(1,1, 72, txt.getLineHeight()*txt.getLineCount(), true);
				}
			}

		});


		txt.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				lineHighlight();
				handleCaretChange();
				if (e.stateMask == SWT.CTRL) {
					switch (e.keyCode) {
					case SWT.HOME:
					case SWT.END:
						// Check to see if we need to add to a nav history
						RepDevMain.mainShell.addToNavHistory(file, txt.getLineAtOffset(txt.getCaretOffset()));
					break;
					// TODO: This does not work. I think that the CTabfolder is eating the key
					case SWT.PAGE_UP:
					case SWT.PAGE_DOWN:
						RepDevMain.mainShell.addToTabHistory();
						break;
					case 's':
					case 'S':
						saveFile(true);
						break;
					case 'z':
					case 'Z':
						undo();
						break;
					case 'y':
					case 'Y':
						redo();
						break;
					case 'a':
					case 'A':
						txt.selectAll();
						break;
					case 'f':
					case 'F':
						RepDevMain.mainShell.showFindWindow();
						break;	
					case 'd':
					case 'D':
						installRepgen(false); // install without confirmation
						break;	
					case 'p':
					case 'P':
						RepDevMain.mainShell.print();
						break;
					case 'l':
					case 'L':
						GotoLineShell.show(txt.getParent().getShell(),txt);
						break;
					case 'U':
					case 'u':
						surroundEachLineWith("PRINT \"", "\"\nNEWLINE\n", true);
						break;
					case SWT.F4:
					case 'w':
					case 'W':
						RepDevMain.mainShell.closeCurrentTab();
						break;
					case 'r':
					case 'R':
						RepDevMain.mainShell.runReport(file);
						break;
					case 'h':
					case 'H':
						parser.reparseAll();
						break;

					case 'g':
					case 'G':
						gotoSectionShell();
						break;
					}
				}
				else if( e.stateMask == (SWT.CTRL | SWT.SHIFT) ) {
					switch(e.keyCode) {
					case 'p':
					case 'P':
						if(startBlockToken != null && endBlockToken != null){					
							try {
								int setPos = 0;
								StyledText newTxt = tempEditor.getStyledText();
								if(newTxt.getCaretOffset() >= startBlockToken.getStart() &&
										newTxt.getCaretOffset() <= startBlockToken.getEnd())
									setPos = endBlockToken.getEnd();
								else
									setPos = startBlockToken.getStart();
								
								//newTxt.setCaretOffset(txt.getText().length());
								//newTxt.showSelection();
								newTxt.setCaretOffset(setPos);
								tempEditor.handleCaretChange();
								newTxt.showSelection();
								tempEditor.lineHighlight();
							} catch (IllegalArgumentException ex) {
								// Just ignore it
							}
							tempEditor.getStyledText().setFocus();
						}
						break;
						
					case 'r':
					case 'R':
						surroundWithShell();
						break;
					case 't':
					case 'T':
						sendToFormatter();
						break;
					case 'd':
					case 'D':
						String sTmpString=txt.getSelectionText();
						if(sTmpString.length() == 0 && getTokenAt(txt.getCaretOffset()) != null){
							int iStart, iEnd;
							iStart=getTokenAt(txt.getCaretOffset()).getStart();
							iEnd=getTokenAt(txt.getCaretOffset()).getEnd();
							txt.setSelection(iStart, iEnd);
							sTmpString= txt.getSelectionText();
						}
						if(isAlphaNumeric(sTmpString))
							defineVarShell(sTmpString);
						break;
					case 'g':
					case 'G':
						gotoDefinition();
						break;
					}
				}
				else if(e.stateMask == (SWT.ALT)){
					switch (e.keyCode) {
					case SWT.ARROW_LEFT:
						RepDevMain.mainShell.navigatToHistory(false);
						break;
					case SWT.ARROW_RIGHT:
					RepDevMain.mainShell.navigatToHistory(true);
						break;
					}
				}
				else{
					if( e.keyCode == SWT.F3 )
						RepDevMain.mainShell.findNext();
					if( e.keyCode == SWT.F5 )
						RepDevMain.mainShell.reopenCurrentTab();
					if( e.keyCode == SWT.F8 )
						installRepgen(true);
				}

				if (e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT || e.keyCode == SWT.ARROW_UP){
					commitUndo();
									}
				if (e.keyCode == SWT.PAGE_UP || e.keyCode == SWT.PAGE_DOWN || e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN)
					// Check to see if we need to add to a nav history
					RepDevMain.mainShell.addToNavHistory(file, txt.getLineAtOffset(txt.getCaretOffset()));
			}

			public void keyReleased(KeyEvent e) {

			}
		});
		final String DRAG_START_DATA = "DRAG_START_DATA";

		// Drag Copy Text - Code taken from here 
		// http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet257.java?view=co
		final DragSource source = new DragSource(txt, DND.DROP_COPY | DND.DROP_MOVE);
		source.setDragSourceEffect(new DragSourceEffect(txt) {
			public void dragStart(DragSourceEvent event) {
				event.image = Display.getCurrent().getSystemImage(SWT.ICON_WORKING); //RepDevMain.smallCopyImage;
			}
		});
		source.setTransfer(new Transfer[] {TextTransfer.getInstance()});
		source.addDragListener(new DragSourceAdapter() {
			Point selection;
			public void dragStart(DragSourceEvent event) {
				selection = txt.getSelection();
				event.doit = selection.x != selection.y;
				txt.setData(DRAG_START_DATA, selection);
			}
			public void dragSetData(DragSourceEvent e) {
				e.data = txt.getText(selection.x, selection.y-1);
			}
			public void dragFinished(DragSourceEvent event) {
				if (event.detail == DND.DROP_MOVE) {
					Point newSelection= txt.getSelection();
					int length = selection.y - selection.x;
					int delta = 0;
					if (newSelection.x < selection.x)
						delta = length; 
					txt.replaceTextRange(selection.x + delta, length, "");
				}
				selection = null;
				txt.setData(DRAG_START_DATA, null);
			}
		});
		
		DropTarget target = new DropTarget(txt, DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
		target.setTransfer(new Transfer[] {TextTransfer.getInstance()});
		target.addDropListener(new DropTargetAdapter() {
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if (txt.getData(DRAG_START_DATA) == null)
						event.detail = DND.DROP_COPY;
					else 
						event.detail = DND.DROP_MOVE;
				}
			}
			public void dragOperationChanged(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if (txt.getData(DRAG_START_DATA) == null)
						event.detail = DND.DROP_COPY;
					else 
						event.detail = DND.DROP_MOVE;
				}
			}
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_SCROLL | DND.FEEDBACK_SELECT;
			}
			public void drop(DropTargetEvent event) {
				if (event.detail != DND.DROP_NONE) {
					Point selection = (Point) txt.getData(DRAG_START_DATA);
					int insertPos = txt.getCaretOffset();
					if (event.detail == DND.DROP_MOVE && selection != null && selection.x <= insertPos  && insertPos <= selection.y 
							|| event.detail == DND.DROP_COPY && selection != null && selection.x < insertPos  && insertPos < selection.y) {
						txt.setSelection(selection);
						event.detail = DND.DROP_COPY;  // prevent source from deleting selection
					} else {
						String string = (String)event.data;
						txt.insert(string);
						if (selection != null)
							txt.setSelectionRange(insertPos, string.length());
					}
				}
			}
		});
		// End Drag Copy Text
		txt.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				lineHighlight();

				commitUndo();
			}

			public void mouseUp(MouseEvent e) {
				lineHighlight();
				handleCaretChange();
				// Drop Navigation Position
				RepDevMain.mainShell.addToNavHistory(file, txt.getLineAtOffset(txt.getCaretOffset()));
			}

			// TODO: Make double clicking include files work when last line of the file
			public void mouseDoubleClick(MouseEvent e) {
				int curLine = txt.getLineAtOffset(txt.getSelection().x);
				int startOffset = txt.getOffsetAtLine(curLine);
				int endOffset;
				String line;

				endOffset = txt.getOffsetAtLine(Math.min(txt.getLineCount() - 1, curLine + 1));

				if( endOffset - 1 <= startOffset)
					line = "";
				else
					line = txt.getText(startOffset, endOffset - 1);	

				if( line.indexOf("#INCLUDE") != -1 ) {
					String fileStr = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""));
					if( file.isLocal() )
						RepDevMain.mainShell.openFile(new SymitarFile(file.getDir(), fileStr, file.getType()));
					else {
						SymitarFile sf = new SymitarFile(sym, fileStr, FileType.REPGEN);
						sf.disableSourceControl(true);
						RepDevMain.mainShell.openFile(sf);
					}
				}
				else if(txt.getSelectionText().equalsIgnoreCase("CALL")) {
					txt.setCaretOffset(txt.getCaretOffset()+1);
					gotoDefinition();
//					Token tmpToken;
//
//					tmpToken = getTokenAt(txt.getCaretOffset());
//					if(tmpToken != null){
//						if(sec.exist(tmpToken.getStr())){
//							gotoSection(tmpToken.getStr());
//						}
//					}
				}
			}

		});

		txt.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				lineHighlight();
			}
		});

		Menu contextMenu = new Menu(txt);

		final MenuItem indentMore = new MenuItem(contextMenu, SWT.NONE);
		indentMore.setText("Increase Indentation\tTAB");
		indentMore.setImage(RepDevMain.smallIndentMoreImage);
		indentMore.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int startLine = txt.getLineAtOffset(txt.getSelection().x);
				int endLine = txt.getLineAtOffset(txt.getSelection().y);

				groupIndent(1, startLine, endLine);
			}
		});

		final MenuItem indentLess = new MenuItem(contextMenu, SWT.NONE);
		indentLess.setText("Decrease Indentation\tSHIFT+TAB");
		indentLess.setImage(RepDevMain.smallIndentLessImage);
		indentLess.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int startLine = txt.getLineAtOffset(txt.getSelection().x);
				int endLine = txt.getLineAtOffset(txt.getSelection().y);

				groupIndent(-1, startLine, endLine);
			}
		});

		new MenuItem(contextMenu,SWT.SEPARATOR);

		final MenuItem surroundWithDialog = new MenuItem(contextMenu, SWT.NONE);
		surroundWithDialog.setText("Surround Text Dialog\tCTRL+SHIFT+R");
		surroundWithDialog.setImage(RepDevMain.smallSurroundImage);
		surroundWithDialog.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				surroundWithShell();
			}
		});

		// TODO: New icon for SurroundWithPrints
		final MenuItem surroundWithPrint = new MenuItem(contextMenu, SWT.NONE);
		surroundWithPrint.setText("Surround Text with PRINTs\tCTRL+U");
		surroundWithPrint.setImage(RepDevMain.smallSurroundPrint);
		surroundWithPrint.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				surroundEachLineWith("PRINT \"", "\"\nNEWLINE\n", true);
			}
		});

		final MenuItem formatCode = new MenuItem(contextMenu, SWT.NONE);
		formatCode.setText("Format Code (BETA)\tCTRL+SHIFT+T");
		formatCode.setImage(RepDevMain.smallFormatCodeImage);
		formatCode.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sendToFormatter();
			} 
		});

		new MenuItem(contextMenu,SWT.SEPARATOR);

		final MenuItem insertSnippet = new MenuItem(contextMenu, SWT.NONE);
		insertSnippet.setText("Insert Snippet\t(CTRL+SPACE) X 2");
		insertSnippet.setImage(RepDevMain.smallInsertSnippetImage);
		insertSnippet.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				suggest.open(true);
			} 
		});

		// Bruce - Start 02/04/08
		final MenuItem defineVar = new MenuItem(contextMenu, SWT.NONE);
		defineVar.setText("Define Variable\tCTRL+SHIFT+D");
		defineVar.setImage(RepDevMain.smallDefineVarImage);
		defineVar.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String sTmpString=txt.getSelectionText();
				if(sTmpString.length() == 0 && getTokenAt(txt.getCaretOffset()) != null){
					int iStart, iEnd;
					iStart=getTokenAt(txt.getCaretOffset()).getStart();
					iEnd=getTokenAt(txt.getCaretOffset()).getEnd();
					txt.setSelection(iStart, iEnd);
					sTmpString= txt.getSelectionText();
				}
				if(isAlphaNumeric(sTmpString))
					defineVarShell(sTmpString);
			} 
		});
		// Bruce - End

		final MenuItem gotoDefinition = new MenuItem(contextMenu, SWT.NONE);
		gotoDefinition.setText("Goto Definition\tCTRL+SHIFT+G");
		gotoDefinition.setImage(RepDevMain.smallFunctionImage);
		gotoDefinition.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				gotoDefinition();
			} 
		});
		
		final MenuItem gotoSection = new MenuItem(contextMenu, SWT.NONE);
		gotoSection.setText("Goto Section\tCTRL+G");
		gotoSection.setImage(RepDevMain.smallFindImage);
		gotoSection.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				gotoSectionShell();
			} 
		});
		new MenuItem(contextMenu,SWT.SEPARATOR);

		final MenuItem editCut = new MenuItem(contextMenu,SWT.PUSH);
		editCut.setImage(RepDevMain.smallCutImage);
		editCut.setText("Cut\tCTRL+X");
		editCut.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				txt.cut();
			}
		});

		final MenuItem editCopy = new MenuItem(contextMenu,SWT.PUSH);
		editCopy.setImage(RepDevMain.smallCopyImage);
		editCopy.setText("Copy\tCTRL+C");
		editCopy.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				txt.copy();
			}
		});

		final MenuItem editPaste = new MenuItem(contextMenu,SWT.PUSH);
		editPaste.setImage(RepDevMain.smallPasteImage);
		editPaste.setText("Paste\tCTRL+V");
		editPaste.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				txt.paste();
			}
		});


		contextMenu.addMenuListener(new MenuListener() {

			public void menuHidden(MenuEvent e) {
			}

			public void menuShown(MenuEvent e) {
				int startLine = txt.getLineAtOffset(txt.getSelection().x);
				int endLine = txt.getLineAtOffset(txt.getSelection().y);

				if (startLine == endLine) {
					indentMore.setEnabled(false);
					indentLess.setEnabled(false);
					surroundWithDialog.setEnabled(false);
					surroundWithPrint.setEnabled(false);
				} else {
					indentMore.setEnabled(true);
					indentLess.setEnabled(true);
					surroundWithDialog.setEnabled(true);
					surroundWithPrint.setEnabled(true);
				}
				// Bruce - Start 02/04/08
				// Enable the Defind Variable Menu option only if a word is highlighted
				// and the DEFINE section is found.
				if(getTokenAt(txt.getCaretOffset()) != null && isAlphaNumeric(getTokenAt(txt.getCaretOffset()).getStr()) && sec.exist("define")){
					defineVar.setEnabled(true);
				}
				else{
					defineVar.setEnabled(false);
				}
				if(getTokenAt(txt.getCaretOffset()) != null && isAlphaNumeric(getTokenAt(txt.getCaretOffset()).getStr())){
					gotoDefinition.setEnabled(true);
				}
				else{
					gotoDefinition.setEnabled(false);
				}
				// Bruce - End
			}

		});

		txt.setMenu(contextMenu);

		String str = file.getData(true);
		if(file.syncRepGen())
			tabItem.setText(">"+file.getName());

		if (str == null){
			tabItem.dispose();
			return;
		}

		txt.setText(str);
		handleCaretChange();
		// Drop Navigation Position
		//RepDevMain.mainShell.addToNavHistory(file, txt.getLineAtOffset(txt.getCaretOffset()));
		suggest.close();

		/*		FormData frmBar = new FormData();
		frmBar.top = new FormAttachment(0);
		frmBar.left = new FormAttachment(0);
		frmBar.right = new FormAttachment(100);
		bar.setLayoutData(frmBar);*/

		FormData frmTxt = new FormData();
		frmTxt.top = new FormAttachment(0);
		frmTxt.left = new FormAttachment(0);
		frmTxt.right = new FormAttachment(100);
		frmTxt.bottom = new FormAttachment(100);
		txt.setLayoutData(frmTxt);

		if( parser != null && !file.isLocal())
			parser.errorCheck();

		undoMode = 1;
		modified = false;
		updateModified();
		highlight(doParse,true);
	}

	public void gotoDefinition(){
		
		CTabFolder mainfolder = RepDevMain.mainShell.getMainfolder();
			
		HashMap<String, ArrayList<Token>> incTokenCache = parser.getIncludeTokenChache();
		String selString=txt.getSelectionText();
		if(selString.length() == 0)
			selString=getTokenAt(txt.getCaretOffset()) != null ? getTokenAt(txt.getCaretOffset()).getStr() : "";
		if(!isAlphaNumeric(selString))
			return;
		
				
		if( parser.needRefreshIncludes() )
			parser.parseIncludes();
		
		//sec = new BackgroundSectionParser(parser.getLtokens(),txt.getText());
		//sec.refreshList(parser.getLtokens(),txt.getText());
		if(sec.exist(selString)){
			gotoSection(selString);
			return;
		}
		for( String key : incTokenCache.keySet()){
			for(Token token : incTokenCache.get(key)){
				if(matchTokenAndGoto(token, key, selString))
					return;
			}
		}
		for(Variable var : parser.getLvars()){
			if(matchVarAndGoto(var, selString))
				return;
		}
		// Go through open files which include this file. Search for Variables/Procedures and goto. 
		for(CTabItem tf : mainfolder.getItems()){
			if(tf.getControl() instanceof EditorComposite) {
				EditorComposite ec = ((EditorComposite) tf.getControl());
				incTokenCache = ec.parser.getIncludeTokenChache();
				for( String key : incTokenCache.keySet()){
					if(key.equalsIgnoreCase(file.getName())){
						
						if( ec.parser.needRefreshIncludes() )
							ec.parser.parseIncludes();
						
						if(ec.sec.exist(selString)){
							gotoSection(selString);
							return;
						}
						for( String key2 : incTokenCache.keySet()){
							for(Token token : incTokenCache.get(key2)){
								if(matchTokenAndGoto(token, key2, selString))
									return;
							}
						}
						for(Variable var : ec.parser.getLvars()){
							if(matchVarAndGoto(var, selString))
								return;
						}
					}
				}
			}
		}
	}
	private Boolean matchVarAndGoto(Variable var, String varToMatch){
		Object o;
		if(var.getName().equals(varToMatch)){
			if( file.isLocal() )
				o = RepDevMain.mainShell.openFile(new SymitarFile(file.getDir(), var.getFilename(), file.getType()));
			else {
				SymitarFile sf=new SymitarFile(sym, var.getFilename(), FileType.REPGEN);
				sf.disableSourceControl(true);
				o = RepDevMain.mainShell.openFile(sf);
			}
			EditorComposite editor = null;

			if (o instanceof EditorComposite)
				editor = (EditorComposite) o;
//					if (token.getStart() >= 0 && editor != null) {
//						editor.getStyledText().setTopIndex(Math.max(0, task.getLine() - 10));
				try {
					StyledText newTxt = editor.getStyledText();
					newTxt.setCaretOffset(newTxt.getText().length());
					newTxt.showSelection();
					newTxt.setCaretOffset(var.getPos());
					editor.handleCaretChange();
					// Drop Navigation Position
					RepDevMain.mainShell.addToNavHistory(file, txt.getLineAtOffset(txt.getCaretOffset()));
					newTxt.showSelection();
					editor.lineHighlight();
				} catch (IllegalArgumentException ex) {
					// Just ignore it
				}
				editor.getStyledText().setFocus();
			return true;
		}
		return false;
	}
	private Boolean matchTokenAndGoto(Token token, String key, String nameToMAtch){
		Object o;
		int i = 0;
		if(token.getTokenType() != null && token.getTokenType().equals(TokenType.DEFINED_VARIABLE)){
			i = 1;
		}
				
		if(token.getTokenType() != null &&
			(token.getTokenType().equals(TokenType.PROCEDURE) || 
				token.getTokenType().equals(TokenType.DEFINED_VARIABLE)) &&
			token.getStr().equalsIgnoreCase(nameToMAtch)){
			if( file.isLocal() )
				o = RepDevMain.mainShell.openFile(new SymitarFile(file.getDir(), key, file.getType()));
			else {
				SymitarFile sf=new SymitarFile(sym, key, FileType.REPGEN);
				sf.disableSourceControl(true);
				o = RepDevMain.mainShell.openFile(sf);
			}
			
			EditorComposite editor = null;

			if (o instanceof EditorComposite)
				editor = (EditorComposite) o;
//			if (token.getStart() >= 0 && editor != null) {
//				editor.getStyledText().setTopIndex(Math.max(0, task.getLine() - 10));
				try {
					StyledText newTxt = editor.getStyledText();
					newTxt.setCaretOffset(newTxt.getText().length());
					newTxt.showSelection();
					newTxt.setCaretOffset(token.getBefore().getStart());
					editor.handleCaretChange();
					// Drop Navigation Position
					RepDevMain.mainShell.addToNavHistory(file, txt.getLineAtOffset(txt.getCaretOffset()));
					newTxt.showSelection();
					editor.lineHighlight();
				} catch (IllegalArgumentException ex) {
					// Just ignore it
				}
				editor.getStyledText().setFocus();
			return true;
		}
		return false;
	}
	// Bruce - Start 02/04/08

	/**
	 *  defineVariable will call the getDefineSection method and obtain an insertion
	 *  point at the end of the DEFINE section.  Then it will take the sStr string and
	 *  insert it.  But if the DEFINE section is not found, an error message will be
	 *  displayed.
	 *
	 *  @param sStr preformulated string for defining a variable ex. XYZ=CHARACTER(5) ARRAY(99)
	 */
	public void defineVariable(String sStr){
		int iEndPos, iCurPos;

		if(sec.exist("define")){
			// Get the insertion point in the DEFINE section.
			iEndPos = sec.getLastInsertPos("define");
			// Save the current position of the cursor so that we can return to this
			// point after the variable has been inserted.
			iCurPos = txt.getCaretOffset();
			// Move the cursor to the insertion point and insert the variable definition.
			
			try{		
				txt.setCaretOffset(iEndPos);
				txt.insert(sStr+"\n");
				// if the original cursor is after the define section, then recalculate the
				// new cursor position.
				if(iCurPos>iEndPos){
					iCurPos=iCurPos+sStr.length()+1;
				}
				txt.setSelection(iCurPos);
				lineHighlight();
			}
			catch(Exception e ){
				//Alert no Define section Found.
				MessageBox dialog = new MessageBox(this.getShell(), SWT.ICON_ERROR | SWT.OK);
				dialog.setMessage("DEFINE Section was not found.  Variable was not added.");
				dialog.setText("Define Variable");
				dialog.open();
			}
		}
		else{
			//Alert no Define section Found.
			MessageBox dialog = new MessageBox(this.getShell(), SWT.ICON_ERROR | SWT.OK);
			dialog.setMessage("DEFINE Section was not found.  Variable was not added.");
			dialog.setText("Define Variable");
			dialog.open();
		}
	}

	/**
	 * Calls and creates the Define Variable Shell, which will allow the user to select
	 * the variable type. Define an array size as well as a comment.
	 *
	 * @param varName name of the variable to define.
	 */
	public void defineVarShell(String varName){
		DefineVarShell.create(this, varName);
	}

	/**
	 * Calls and creates the Goto Section Shell, which allows the user to select the Section/
	 * Procedure to jump to.  The default selection will be the Section/Procedure the caret is
	 * in.
	 */
	public void gotoSectionShell(){
		GotoSectionShell.create(this, txt.getCaretOffset());
	}

	/**
	 * Return the list of SectionInfo
	 * @return ArrayList<SectionInfo>
	 */
	public ArrayList<SectionInfo> getSectionInfoList(){
		return sec.getList();
	}

	/**
	 * gotoSection will jump to the specified section, if it exists.
	 * @param <B>section</B> - Name of the section/procedure
	 */
	public void gotoSection(String section){
		if(!section.equals("") && sec.exist(section)){
			txt.setCaretOffset(txt.getText().length());
			txt.showSelection();
			txt.setCaretOffset(sec.getPos(section));
			handleCaretChange();
			// Drop Navigation Position
			RepDevMain.mainShell.addToNavHistory(file, txt.getLineAtOffset(txt.getCaretOffset()));
			txt.showSelection();
			lineHighlight();
			
		}
	}

	/**
	 *  isAlphaNumeric will return true if all the characters in str are A-Z or a-z or 0-9.  Otherwise
	 *  false is returned.  A null string will also return false.
	 *
	 *  @param str string to check
	 */
	public boolean isAlphaNumeric(String str){

		int i;
		char c;
		boolean b=true;

		if(str.length()==0)
			return false;

		for(i=0;i<str.length();i++){
			c=str.toLowerCase().charAt(i);
			if(!Character.isLetter(c) && !Character.isDigit(c)){
				b=false;
				break;
			}
		}
		return b;
	}

	/**
	 *  isAlpha will return true if all the characters in str are A-Z or a-z.  Otherwise
	 *  false is returned.  A null string will also return false.
	 *
	 *  @param str string to check
	 */
	public boolean isAlpha(String str){

		int i;
		char c;
		boolean b=true;

		if(str.length()==0)
			return false;

		for(i=0;i<str.length();i++){
			c=str.toLowerCase().charAt(i);
			if(!Character.isLetter(c)){
				b=false;
				break;
			}
		}
		return b;
	}

	/**
	 *  isNum will return true if all the characters in str are 0-9.  Otherwise
	 *  false is returned.  A null string will also return false.
	 *
	 *  @param str string to check
	 */
	public boolean isNum(String str){
		int i;
		char c;
		boolean b=true;

		if(str.length()==0)
			return false;

		for(i=0;i<str.length();i++){
			c=str.toLowerCase().charAt(i);
			if(!Character.isDigit(c)){
				b=false;
				break;
			}
		}
		return b;
	}

	/**
	 * Returns the token pointed by offset.  If the pointer is at the begining or the
	 * middle of a token, then that token is returned.  And if the pointer is at the end of
	 * a token, then the next token is returned.  But if the pointer is after the last token
	 * then null is returned.  NOTE: Make sure you check for null prior to using the returned
	 * value
	 *
	 * @param offset
	 * @return Token
	 */
	public Token getTokenAt(int offset){
		int beforeTokenEnd = 0;
		Token token = null;

		for(Token tok : parser.getLtokens()){
			if(offset == 0){
				token = tok;
				break;
			}
			else if(tok != null && tok.getBefore() != null){
				if(offset >= tok.getBefore().getEnd() && offset < tok.getEnd()){
					token = tok;
					break;
				}
			}
		}
		return token;
	}

	// Bruce - End

	/**
	 * Saves the currently open report
	 * @param errorCheck Flag to check errors with symitar
	 */
	public void saveFile( boolean errorCheck ){
		file.saveFile(txt.getText());
		commitUndo();
		modified = false;
		updateModified();

		//If this was an include file to some other files we are currently ediditing, we must update those
		//We also want to do it before the error checker, so any errors with include files get put in asap
		if( parser.needRefreshIncludes() )
			parser.parseIncludes();

		//Check other open tabs, and if needed set the flag to reparse their includes
		CTabFolder folder = (CTabFolder)getParent();
		Object loc;

		if( file.isLocal())
			loc = file.getDir();
		else
			loc = file.getSym();

		for( CTabItem cur : folder.getItems()){
			if( cur.getControl() == null || !(cur.getControl() instanceof EditorComposite) )
				continue;

			RepgenParser parser = ((EditorComposite)cur.getControl()).getParser();

			if( parser != null){
				for( Include inc : parser.getIncludes()){
					if( inc.getFileName().equals(file.getName())){
						parser.setRefreshIncludes(true);
						break;
					}
				}
			}
		}


		if( parser != null && errorCheck && !file.isLocal())
			parser.errorCheck();
	}

	public void updateModified(){
		CTabFolder folder = (CTabFolder)getParent();
		Object loc;

		if( file.isLocal())
			loc = file.getDir();
		else
			loc = file.getSym();

		for( CTabItem cur : folder.getItems())
			if( cur.getData("file") != null && ((SymitarFile)cur.getData("file")).equals(file) && cur.getData("loc").equals(loc)  )
				if( modified && ( cur.getData("modified") == null || !((Boolean)cur.getData("modified")))){
					cur.setData("modified", true);
					cur.setText(cur.getText() + " *");
				}
				else if( !modified && ( cur.getData("modified") == null || ((Boolean)cur.getData("modified")))){
					cur.setData("modified", false);
					cur.setText(cur.getText().substring(0,cur.getText().length() - 2));
				}
	}

	/**
	 * @param confirm Pops up an SWT Message box if true, confirms wether the repgen should be installed or not
	 */
	public void installRepgen(boolean confirm) {
		if( file.getType() != FileType.REPGEN || file.isLocal() ) {
			return;
		}

		MessageBox dialog = new MessageBox(Display.getCurrent().getActiveShell(),SWT.YES | SWT.NO | SWT.ICON_QUESTION);
		MessageBox dialog2 = null;

		dialog.setText("Confirm Repgen Installation");
		dialog.setMessage("Are you sure you want to save this file and install this repgen?");

		if( !confirm || dialog.open() == SWT.YES ){
			getShell().setCursor(getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

			if(modified) saveFile(true);
			ErrorCheckResult result = RepDevMain.SYMITAR_SESSIONS.get(sym).installRepgen(file.getName());

			getShell().setCursor(getDisplay().getSystemCursor(SWT.CURSOR_ARROW));


			dialog2 = new MessageBox(Display.getCurrent().getActiveShell(),SWT.OK | ( result.getType() == ErrorCheckResult.Type.INSTALLED_SUCCESSFULLY ? SWT.ICON_INFORMATION : SWT.ICON_ERROR ));
			dialog2.setText("Installation Result");

			if( result.getType() != ErrorCheckResult.Type.INSTALLED_SUCCESSFULLY )
				dialog2.setMessage("Error Installing Repgen: \n" + result.getErrorMessage());
			else
				dialog2.setMessage("Repgen Installed, Size: " + result.getInstallSize());

			dialog2.open();
		}
	}

	public void surroundEachLineWith(String start, String end, boolean escapeBadChars) {
		int old = txt.getTopIndex();
		//My algorithm: Go through each line of the current text, if it's a line we are working with (Defined by the selection),
		//we append it + start and end stuff to the new Txt, otherwise, just append the regular line to the new Txt
		//When you are done, just write out the newTxt to the box and reparse/reload the highlighting, etc.
		//I decided not to alter the tabbing of the surrounded text, the user should be able to select what they want after the operation
		char[] badChars = { '"' }; //TODO: Add more to list later

		StringBuilder newTxt = new StringBuilder();
		int startLine, endLine;

		//If not selecting anything, operate on current line
		if( txt.getSelectionCount() == 0)
		{
			startLine = endLine = txt.getLineAtOffset(txt.getCaretOffset());
		}
		else{
			startLine = txt.getLineAtOffset(txt.getSelection().x);
			endLine = txt.getLineAtOffset (txt.getSelection().y);
		}

		if (endLine > txt.getLineCount() - 1 )
			endLine = Math.max(txt.getLineCount() - 1, startLine + 1);

		try {

			for (int i = 0; i < txt.getLineCount(); i++) {
				int startOffset = txt.getOffsetAtLine(i);
				int endOffset;
				String line;

				if( i >= txt.getLineCount () - 1 ){
					endOffset = txt.getCharCount() ;
				}
				else
					endOffset = txt.getOffsetAtLine(i + 1);

				if( endOffset - 1 <= startOffset)
					line = "\n";
				else
					line = txt.getText(startOffset, endOffset - 1);            

				if( i >= startLine && i <= endLine )
				{    
					newTxt.append(start);

					int j;
					for( j = line.length()-1; j >= 0; j--){
						if(line.charAt(j) != '\n' && line.charAt(j) != '\r'){
							break;
						}
					}
					if(j!=-1){
						line = line.substring(0,j+1);
					}
					if( escapeBadChars )
						for(char cur : badChars)
							line = line.replaceAll("\\"+cur, "\"+CTRLCHR("+((int)cur)+")+\"");

					newTxt.append(line);
					newTxt.append(end);
				}
				else
					newTxt.append(line);
			}



		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if( parser != null )
				parser.setReparse(false);

			txt.setText(newTxt.toString());

			if( parser != null ){
				parser.reparseAll();
				parser.setReparse(true);
			}
		}
		txt.setTopIndex(old);
	}

	public RepgenParser getParser() {
		return parser;
	}

	public void setParser(RepgenParser parser) {
		this.parser = parser;
	}

	public SymitarFile getFile() {
		return file;
	}

	public void handleCaretChange() {
		boolean found = false;
		Token cur = null;
		int tokloc = 0;
		ArrayList<Token> tokens = null;
		ArrayList<Token> redrawTokens = new ArrayList<Token>();

		RepDevMain.mainShell.setLineColumn();
		if( parser == null )
			return;


		if( snippetMode ){
			if(currentEditVarPos != -1 && txt.getCaretOffset() < snippetStartPos + currentSnippet.getVarEditPos(currentEditVarPos))
				snippetMode = false;
			else if( currentEditVarPos != -1 && txt.getCaretOffset() > snippetStartPos + currentSnippet.getVarEditPos(currentEditVarPos) + currentEditVar.getValue().length())
				snippetMode = false;

			if( !snippetMode )
				clearSnippetMode();
		}

		tokens = parser.getLtokens();

		//Find your current token
		for( Token tok: tokens ) {
			tokloc++;

			if(txt.getCaretOffset() >= tok.getStart()  && txt.getCaretOffset() <= tok.getEnd()) {
				cur = tok;
				break;
			}
		}

		//Clear all other special backgrounds, possibly move this up to previous loop in future to make faster
		for( Token tok : tokens){
			if( tok.getSpecialBackground() != null && tok.getBackgroundReason() == Token.SpecialBackgroundReason.BLOCK_MATCHER){
				tok.setSpecialBackground(null);
				tok.setBackgroundReason(Token.SpecialBackgroundReason.NONE);
				redrawTokens.add(tok);
			}
		}

		// Refresh the Section Info
		if(prevTxtLine != txt.getLineAtOffset(txt.getCaretOffset())){
			sec.refreshList(tokens, txt.getText());
			prevTxtLine = txt.getLineAtOffset(txt.getCaretOffset());
		}


		found = false;

		if( cur != null ) {

			//If it's a start token, read forward to the matching block
			if(cur.isRealHead())
			{

				Stack<Token> tStack = new Stack<Token>();
				tStack.push(cur);

				//tokloc is already set at next token since it was set before the break in the for loop above
				//All this messy code is to differentiate between starts and ends that are the same
				while( tokloc < tokens.size() ) {
					if( tokens.get(tokloc).isHead() && 
							(tokens.get(tokloc).getCDepth() == 0 ||tokens.get(tokloc).getStr().equals("["))&&
							((!tokens.get(tokloc).inDate() || tokens.get(tokloc).getStr().equals("'")) && tStack.size() == 0 || !tStack.peek().getStr().equals("\'")) && 
							((!tokens.get(tokloc).inString() || tokens.get(tokloc).getStr().equals("\"")) && tStack.size() == 0 || !tStack.peek().getStr().equals("\"")))
					{
						tStack.push(tokens.get(tokloc));
					}
					else if( tokens.get(tokloc).isEnd() && 
							( tokens.get(tokloc).getCDepth() == 0 ||  tokens.get(tokloc).getStr().equals("]")) && 
							(!tokens.get(tokloc).inDate() ||  tokens.get(tokloc).getStr().equals("'")) &&
							(!tokens.get(tokloc).inString() ||  tokens.get(tokloc).getStr().equals("\"")) && tStack.size() > 0)
					{
						tStack.pop();						
					}

					if( tStack.size() == 0 ) {
						found = true;
						break;
					}

					tokloc++;
				}

				if( found ){
					//TODO:Special background
					setLineColor(highlighter);
					cur.setSpecialBackground(blockMatchColor);
					cur.setBackgroundReason(Token.SpecialBackgroundReason.BLOCK_MATCHER);
					tokens.get(tokloc).setSpecialBackground(blockMatchColor);
					tokens.get(tokloc).setBackgroundReason(Token.SpecialBackgroundReason.BLOCK_MATCHER);
					redrawTokens.add(cur);
					redrawTokens.add(tokens.get(tokloc));
					startBlockToken = cur;
					endBlockToken = tokens.get(tokloc);
				}
			} else if( cur.isRealEnd() )
			{

				Stack<Token> tStack = new Stack<Token>();
				tStack.push(cur);

				//tokloc must be moved back, one back to current token, one more back to first one we should be reading
				tokloc = Math.max(0,tokloc-2);

				//All this messy code is to differentiate between starts and ends that are the same
				while( tokloc >=0 ) {
					if( tokens.get(tokloc).isEnd() && 
							( tokens.get(tokloc).getCDepth() == 0 ||  tokens.get(tokloc).getStr().equals("]")) && 
							((!tokens.get(tokloc).inDate() ||  tokens.get(tokloc).getStr().equals("'")) && tStack.size() == 0 || !tStack.peek().getStr().equals("\'")) &&
							((!tokens.get(tokloc).inString() ||  tokens.get(tokloc).getStr().equals("\"")) && tStack.size() == 0 || !tStack.peek().getStr().equals("\"")))
					{
						tStack.push(tokens.get(tokloc));					
					}
					else if( tokens.get(tokloc).isHead() && 
							(tokens.get(tokloc).getCDepth() == 0 ||tokens.get(tokloc).getStr().equals("["))&&
							((!tokens.get(tokloc).inDate() || tokens.get(tokloc).getStr().equals("'"))) && 
							((!tokens.get(tokloc).inString() || tokens.get(tokloc).getStr().equals("\""))) && tStack.size() > 0)
					{
						tStack.pop();	
					}


					if( tStack.size() == 0 ) {
						found = true;
						break;
					}

					tokloc--;
				}

				if( found ){
					setLineColor(highlighter);
					cur.setSpecialBackground(blockMatchColor);
					cur.setBackgroundReason(Token.SpecialBackgroundReason.BLOCK_MATCHER);
					tokens.get(tokloc).setSpecialBackground(blockMatchColor);
					tokens.get(tokloc).setBackgroundReason(Token.SpecialBackgroundReason.BLOCK_MATCHER);
					redrawTokens.add(cur);
					redrawTokens.add(tokens.get(tokloc));
					
					startBlockToken = cur;
					endBlockToken = tokens.get(tokloc);
				}
			}
			else{
				startBlockToken = null;
				endBlockToken = null;
			}
		}

		//IF we need to update, only call this once
		for( Token tok : redrawTokens )
			txt.redrawRange(tok.getStart(),tok.getStr().length(),false);
	}

	private void clearSnippetMode() {
		if( parser == null)
			return;

		for( Token tok : parser.getLtokens()){
			if(tok.getBackgroundReason() == Token.SpecialBackgroundReason.CODE_SNIPPET){
				tok.setBackgroundReason(Token.SpecialBackgroundReason.NONE);
				tok.setSpecialBackground(null);
				tok.setCurrentVar(null);
				txt.redrawRange(tok.getStart(),tok.getStr().length(),false);
			}
		}

		currentEditVar = null;
		currentEditVarPos = -1;
		currentSnippet = null;
		snippetStartPos = -1;
		snippetMode = false;
	}

	public void surroundWithShell() {
		SurroundWithShell.create(this);
	}

	public void sendToFormatter(){
		Formatter formatter = new Formatter(txt.getText(),parser.getLtokens());
		parser.setReparse(false);
		txt.setText(formatter.getFormattedFile());
		parser.setReparse(true);
		parser.reparseAll();
	}

	public boolean isModified() {
		return modified;
	}

	/**Puts us into snippet mode, if text is selected, we surround it with a snippet if supported, otherwise we insert it in, and start the editing mode
	 * 
	 * @param test
	 */
	public void activateSnippet(Snippet snippet) {
		String indent = "";
		String lastLine, sText;

		if( parser == null)
			return;


		commitUndo();

		currentSnippet = snippet;
		snippet.setup();

		snippetStartPos = txt.getCaretOffset();

		int startOffset = txt.getOffsetAtLine(txt.getLineAtOffset(txt.getCaretOffset()));
		int endOffset;

		endOffset = txt.getCaretOffset();

		if( endOffset <= startOffset)
			lastLine = "\n";
		else
			lastLine = txt.getText(startOffset, endOffset - 1);	

		for (int i = 0; i < lastLine.length(); i++)
			if (lastLine.charAt(i) != ' ' && lastLine.charAt(i) != '\t')
				break;
			else
				indent += lastLine.charAt(i);


		sText = snippet.getReplacedText(txt.getSelectionText(),indent);

		txt.insert(sText);
		//undos.push(new TextChange(snippetStartPos,sText.length(),"",0));
		commitUndo();
		snippetMode = true;
		//Set the snippet mode on so the edit listener doens't do anything until after the initial insertion of the snippet

		if( snippet.getNumberOfUniqueVars() > 0){
			currentEditVarPos = 0;
			currentEditVar = snippet.getVar(0);
			//Put cursor to first var
			txt.setCaretOffset(snippetStartPos + snippet.getVarEditPos(currentEditVarPos));
			snippetMode = true;
		}
		else{
			currentEditVarPos = -1;
			currentEditVar = null;
		}


		//Highlight the variables
		//Find the tokens
		updateSnippet();

	}

	//Highlights the tokens, etc
	private void updateSnippet(){
		ArrayList<Integer> list;

		if( !snippetMode )
			return;

		//TODO: These loops are super inefficient!
		for( int i = 0; i < currentSnippet.getNumberOfUniqueVars(); i++){
			list = currentSnippet.getLocations(i);

			if( list == null)
				continue;


			for( int x = 0; x < list.size(); x+=2){
				for( Token tok : parser.getLtokens()){


					if( tok.getStart() >= (list.get(x) + snippetStartPos) && tok.getEnd() <= (snippetStartPos + list.get(x) + list.get(x+1))){
						if( i == currentEditVarPos ){
							tok.setSpecialBackground(SNIPPET_VAR_CURRENT);

							if( x == 0 )
								tok.setSpecialBackground(SNIPPET_VAR_EDITING);
						}
						else
							tok.setSpecialBackground(SNIPPET_VAR);

						tok.setBackgroundReason(Token.SpecialBackgroundReason.CODE_SNIPPET);
						tok.setCurrentVar(currentSnippet.getVar(i));
						txt.redrawRange(tok.getStart(),tok.getStr().length(),false);
					}
				}
			}
		}

		txt.redraw();
	}

	public SyntaxHighlighter getHighlighter() {
		return highlighter;
	}


}
