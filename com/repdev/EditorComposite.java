package com.repdev;

import java.util.Stack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.repdev.parser.RepgenParser;
import com.repdev.parser.Token;
import com.repdev.parser.TreeItem;

/**
 * Main editor for repgen, help, and letter files
 * Provides syntax highlighting and other advanced features for the repgen files, basically all the text editor stuff is in this class
 * 
 * @author Jake Poznanski
 *
 */
public class EditorComposite extends Composite implements TabTextEditorView{
	private SymitarFile file;
	private int sym;
	private static Color lineBackgroundColor = new Color(Display.getCurrent(), 232, 242, 254);
	private static Color blockHighlightColor = new Color(Display.getCurrent(), 200,200,200);
	/*private ToolItem save, install, print, run;*/
	private StyledText txt;
	private CTabItem tabItem;

	private static final int UNDO_LIMIT = 500;
	private Stack<TextChange> undos = new Stack<TextChange>();
	private Stack<TextChange> redos = new Stack<TextChange>();

	// 1 = Regular
	// 2 = Undoing, so save as redos
	// 0 = Ignore all
	private int undoMode = 0;

	private int lastLine = 0;
	@SuppressWarnings("unused")
	private SyntaxHighlighter highlighter;
	private RepgenParser parser;
	private boolean modified = false;
	
	static SuggestShell suggest = new SuggestShell();
	
	private static Font DEFAULT_FONT;
	
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
			this.commit = true;
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

		buildGUI();
	}
	
	
	public boolean canUndo(){
		return undos.size() > 0;
	}
	
	public boolean canRedo(){
		return redos.size() > 0;	
	}

	public void undo() {
				
		try {
			TextChange change;

			if (!undos.empty()) {
				if (undos.peek().isCommit() == true)
					undos.pop();

				undoMode = 2;
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
			dialog.setMessage("The Undo Manager has failed! Email Jake!");
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

			}
		} catch (Exception e) {
			MessageBox dialog = new MessageBox(this.getShell(), SWT.ICON_ERROR | SWT.OK);
			dialog.setMessage("The Undo Manager has failed! Email Jake!");
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

	private void lineHighlight() {
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
	
	//TODO: Allow for unindenting single lines
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

					for (int x = 0; x < Math.min(tabStr.length(), line.length()); x++)
						if (line.charAt(x) > 32)
							return;
				}
			}
			txt.setRedraw(false);

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
				

				if (direction > 0)
					txt.replaceTextRange(startOffset, endOffset - startOffset, tabStr + line);
				else {
					txt.replaceTextRange(startOffset, endOffset - startOffset, line.substring(Math.min(tabStr.length(), line.length())));
				}

				offset += tabStr.length() * direction;

			}

			if( parser != null)
				parser.setReparse(true);

			oldSelection.y += offset;
			oldSelection.x += tabStr.length() * direction;

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

	private String getTabStr() {
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

	private void buildGUI() {
		setLayout(new FormLayout());
		
		
		txt = new StyledText(this, SWT.H_SCROLL | SWT.V_SCROLL);

		if (file.getType() == FileType.REPGEN){
			parser = new RepgenParser(txt, file);
			highlighter = new SyntaxHighlighter(parser);
		}
		else{
			if( DEFAULT_FONT != null)
				txt.setFont(DEFAULT_FONT);
		}
		
		txt.addFocusListener(new FocusListener(){

			public void focusGained(FocusEvent e) {
				suggest.attach(txt, parser);
				
			}

			public void focusLost(FocusEvent e){
			}
			
		});

		suggest.attach(txt, parser);

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
			}

		});

		
		//Place any auto complete things in here
		txt.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				if (e.text.equals("\t")) {

					int direction = (e.stateMask == SWT.SHIFT) ? -1 : 1;

					if (txt.getSelectionCount() > 1) {
						e.doit = false;

						int startLine = txt.getLineAtOffset(e.start);
						int endLine = txt.getLineAtOffset(e.end);

						if (startLine == endLine) {
							e.doit = true;
							e.text = getTabStr();
							return;
						}

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

				if (undoMode != 0) {
					stack.push(new TextChange(event.start, event.length, event.replacedText, txt.getTopIndex()));

					if (stack.size() > UNDO_LIMIT)
						stack.remove(0);
				}
			
			}

		});

		
		txt.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				lineHighlight();
				
				int line = txt.getLineAtOffset(txt.getCaretOffset());						
				handleCaretChange();
				
				if (e.stateMask == SWT.CTRL) {
					switch (e.keyCode) {
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
						surroundEachLineWith("PRINT \"", "\"", true);
						break;
					case 'r':
					case 'R':
						RepDevMain.mainShell.runReport(file);
						break;
						
					}
				} else if( e.stateMask == (SWT.CTRL | SWT.SHIFT) ) {
					switch(e.keyCode) {
					case 's':
					case 'S':
						RepDevMain.mainShell.saveAllRepgens();
						break;
						
					case 'o':
					case 'O':
						RepDevMain.mainShell.showOptions();
						break;
					}
				}
				else{
					if( e.keyCode == SWT.F3 )
						RepDevMain.mainShell.findNext();
					if( e.keyCode == SWT.F8 )
						installRepgen(true);
				}
				

				if (e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT || e.keyCode == SWT.ARROW_UP)
					commitUndo();

			}

			public void keyReleased(KeyEvent e) {

			}
		});
		
		txt.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				lineHighlight();

				commitUndo();
			}

			public void mouseUp(MouseEvent e) {
				lineHighlight();
				
				int line = txt.getLineAtOffset(txt.getCaretOffset());							
				handleCaretChange();
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
						RepDevMain.mainShell.openFile(new SymitarFile(file.getDir(), fileStr));
					else	
						RepDevMain.mainShell.openFile(new SymitarFile(sym, fileStr, FileType.REPGEN));
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
				} else {
					indentMore.setEnabled(true);
					indentLess.setEnabled(true);
				}
			}

		});

		txt.setMenu(contextMenu);

		String str = file.getData();
		
		if (str == null){
			tabItem.dispose();
			return;
		}
		
		txt.setText(str);

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
	}
	
	/**
	 * Location update and highlight blocks
	 *
	 */
	private void handleCaretChange(){
		//Set location in status bar
		RepDevMain.mainShell.setLineColumn();
		boolean needRedraw = false;
		
		for( Token tok : parser.getLtokens() )
		{
			if( tok.getSpecialBackground() != null)
				needRedraw = true;
			
			tok.setSpecialBackground(null);
		}
		
		//Highlight block
		if( parser != null){
			TreeItem treeItem = parser.getTreeParser().getTreeItem(txt.getCaretOffset());
					
			if( treeItem != null && treeItem.getHead() != null && treeItem.getEnd() != null){
				treeItem.getHead().setSpecialBackground(blockHighlightColor);
				treeItem.getEnd().setSpecialBackground(blockHighlightColor);
				needRedraw = true;
			}
						
		}
		
		if( needRedraw )
			txt.redrawRange(0,txt.getCharCount(),true);		
	}
	
	public void saveFile( boolean errorCheck ){
		file.saveFile(txt.getText());
		commitUndo();
		modified = false;
		updateModified();
		
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
	
	//TODO: Finish this method, make it allow end be more than 1 line
	public void surroundEachLineWith(String start, String end, boolean escapeQuotes){
		if( txt.getSelectionCount() == 0)
			return;
		
		int startLine = txt.getLineAtOffset(txt.getSelection().x);
		int endLine = txt.getLineAtOffset(txt.getSelection().y);
		

		for( int i = startLine; i < endLine; i++ ){
			String line = txt.getText(txt.getOffsetAtLine(i), txt.getOffsetAtLine(Math.min(txt.getLineCount()-1,i+1)));
			
			txt.replaceTextRange(txt.getOffsetAtLine(i),
					             txt.getOffsetAtLine(Math.min(txt.getLineCount()-1,i+1)) - txt.getOffsetAtLine(i),
					             start + line.substring(0,line.length()-2).replaceAll("\"", "\"+CTRLCHR(34)+\"") + end + "\r\n" );
		}
		
		commitUndo();
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
	
	public void setToolItems( ToolItem save, ToolItem install, ToolItem print, ToolItem run ) {
		
	}
}
