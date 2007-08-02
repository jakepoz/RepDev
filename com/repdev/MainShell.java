package com.repdev;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabFolderListener;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextPrintOptions;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.repdev.parser.Error;

/**
 * Main graphical user interface. Provides some utility methods as well. 
 * Really should move things out to other places as it gets more complex, either way it will be  large file.
 * @author Jake Poznanski
 * 
 *
 */
public class MainShell {
	private static final int MIN_COL_WIDTH = 75, MIN_COMP_SIZE = 65;
	private CTabFolder mainfolder;
	private Display display;
	private Shell shell;
	private Tree tree;
	private Table tblErrors, tblTasks; 
	private FindReplaceShell findReplaceShell;
	private SurroundWithShell surroundWithShell;
	private final int MAX_RECENTS = 5;
	
	// CoolBar for our universal tool bar at the top.
	private CoolBar coolBar;
	private ToolBar editorBar;
	private ToolItem savetb, install, print, run;
	private ArrayList<CoolItem> coolItems; // <-- may not be needed, keep for future stuff though
	
	//Used for DND
	private TreeItem[] dragSourceItems;
	
	// Status bar stuff...	
	private Composite statusBar;
	private Label lineColumn;
	
	public MainShell(Display display) {
		this.display = display;
		createShell();
	}

	public void open() {
		shell.open();
	}

	public boolean isDisposed() {
		return shell.isDisposed();
	}

	private void createShell() {
		int leftPercent = 20, bottomPercent = 20;

		shell = new Shell(display);
		shell.setText(RepDevMain.NAMESTR);
		shell.setImage(RepDevMain.smallProgramIcon);
		
		createMenuDefault();

		shell.setLayout(new FormLayout());
		
		shell.addShellListener(new ShellAdapter(){
			
			public void shellClosed(ShellEvent e){
				boolean close = true;
				
				for(CTabItem tab : mainfolder.getItems())
					if( !confirmClose(tab) )
						close = false;
				
				e.doit = close;
			}		
		});
		
		// Create the CoolBar
		coolBar = new CoolBar(shell, SWT.NONE);
		FormData cBarData = new FormData();
		cBarData.top = new FormAttachment(0);
		cBarData.left = new FormAttachment(0);
		cBarData.right = new FormAttachment(100);
		coolBar.setLayoutData(cBarData);
		
		coolItems = new ArrayList<CoolItem>();
		coolBar.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				shell.layout();
			}
		});
				
		final Composite left = new Composite(shell, SWT.NONE);
		createExplorer(left);
		final Sash sashVert = new Sash(shell, SWT.VERTICAL | SWT.SMOOTH);
		final Composite right = new Composite(shell, SWT.NONE);
		right.setLayout(new FormLayout());

		createEditorBar();
		
		Composite main = new Composite(right, SWT.NONE);
		createEditorPane(main);
		final Sash sashHoriz = new Sash(right, SWT.HORIZONTAL | SWT.SMOOTH);
		final Composite bottom = new Composite(right, SWT.NONE);
		createBottom(bottom);

		createStatusBar();
		
		FormData frmLeft = new FormData();
		frmLeft.top = new FormAttachment(coolBar);
		frmLeft.left = new FormAttachment(0);
		frmLeft.right = new FormAttachment(sashVert);
		frmLeft.bottom = new FormAttachment(statusBar);
		left.setLayoutData(frmLeft);

		final FormData frmSashVert = new FormData();
		frmSashVert.top = new FormAttachment(coolBar);
		frmSashVert.left = new FormAttachment(leftPercent);
		frmSashVert.bottom = new FormAttachment(statusBar);
		sashVert.setLayoutData(frmSashVert);

		FormData frmRight = new FormData();
		frmRight.top = new FormAttachment(coolBar);
		frmRight.left = new FormAttachment(sashVert);
		frmRight.right = new FormAttachment(100);
		frmRight.bottom = new FormAttachment(statusBar);
		right.setLayoutData(frmRight);

		FormData frmMain = new FormData();
		frmMain.top = new FormAttachment(coolBar);
		frmMain.left = new FormAttachment(0);
		frmMain.right = new FormAttachment(100);
		frmMain.bottom = new FormAttachment(sashHoriz);
		main.setLayoutData(frmMain);

		final FormData frmSashHoriz = new FormData();
		frmSashHoriz.top = new FormAttachment(100 - bottomPercent);
		frmSashHoriz.left = new FormAttachment(0);
		frmSashHoriz.right = new FormAttachment(100);
		sashHoriz.setLayoutData(frmSashHoriz);
				
		FormData frmBottom = new FormData();
		frmBottom.top = new FormAttachment(sashHoriz);
		frmBottom.left = new FormAttachment(0);
		frmBottom.right = new FormAttachment(100);
		frmBottom.bottom = new FormAttachment(100);
		bottom.setLayoutData(frmBottom);
				
		left.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				if (left.getSize().x < MIN_COMP_SIZE) {
					frmSashVert.left = new FormAttachment(0, MIN_COMP_SIZE);
					shell.layout();
				}
			}
		});

		bottom.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				if (bottom.getSize().y < MIN_COMP_SIZE) {
					frmSashHoriz.top = new FormAttachment(right.getSize().y - MIN_COMP_SIZE, right.getSize().y, 0);
					right.layout();
				}
			}
		});

		sashVert.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {

				if (e.x < MIN_COMP_SIZE)
					e.x = MIN_COMP_SIZE;

				if (shell.getClientArea().width - e.x < MIN_COMP_SIZE)
					e.x = shell.getClientArea().width - MIN_COMP_SIZE;

				if (e.x != sashVert.getBounds().x) {
					frmSashVert.left = new FormAttachment(0, e.x);
					shell.layout();
				}
			}
		});

		sashHoriz.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if (e.y < MIN_COMP_SIZE)
					e.y = MIN_COMP_SIZE;

				if (right.getSize().y - e.y < MIN_COMP_SIZE)
					e.y = right.getSize().y - MIN_COMP_SIZE;

				if (e.y != sashHoriz.getBounds().y) {
					frmSashHoriz.top = new FormAttachment(e.y, right.getSize().y, 0);
					right.layout();
				}
			}
		});
		
		findReplaceShell = new FindReplaceShell(shell);
		shell.setMinimumSize(3 * MIN_COMP_SIZE, 3 * MIN_COMP_SIZE);
	}
	
	public Object openFile(Sequence seq, int sym){
		boolean found = false;
		Composite editor;
		
		for (CTabItem c : mainfolder.getItems()) {
			if (c.getData("seq") != null && (Sequence)c.getData("seq") == seq && c.getData("sym") != null && ((Integer) c.getData("sym")) == sym) {
				mainfolder.setSelection(c);
				found = true;
				return c.getControl();
			}
		}

		if (!found) {
			CTabItem item = new CTabItem(mainfolder, SWT.CLOSE);

			item.setText("Sequence View: " + seq.getSeq());
			
			item.setData("seq", seq);
			item.setData("sym", sym);

			item.setImage(drawSymOverImage(RepDevMain.smallReportsImage,sym));
			
			editor = new ReportComposite(mainfolder, item, seq);
			editor.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
					
				}
				public void focusLost(FocusEvent e) {

				}
			});
			
			run.setEnabled(false);
			savetb.setEnabled(false);
			install.setEnabled(false);
		
			//If anything goes wrong initializing the error, it will dispose of the current item
			//So, then we shouldn't do anything
			if( item.isDisposed())
				return null;
			
			mainfolder.setSelection(item);
			item.setControl(editor);			
			
			//Attach find/replace shell here as well (in addition to folder listener)
			findReplaceShell.attach(((ReportComposite)editor).getStyledText(),false);
			
			return editor;
		}
		
		return null;
	}

	public Object openFile(final SymitarFile file) {
		boolean found = false;
		Composite editor;
		Object loc;
		
		if( file.isLocal())
			loc = file.getDir();
		else
			loc = file.getSym();
		
		for (CTabItem c : mainfolder.getItems()) {
			if (c.getData("file") != null && c.getData("file").equals(file) && c.getData("loc") != null && c.getData("loc").equals(loc)) {
				mainfolder.setSelection(c);
				found = true;
				return c.getControl();
			}
		}

		if (!found) {
			CTabItem item = new CTabItem(mainfolder, SWT.CLOSE);
			item.setText(file.getName());
			item.setImage(getFileImage(file));
			item.setData("file", file);
			item.setData("loc", loc);

			if( file.getType() == FileType.REPORT)
				editor = new ReportComposite(mainfolder, item, file);
			else {
				editor = new EditorComposite(mainfolder, item, file /*,save, install, print, run*/);
			}
								
			//If anything goes wrong creating the Editor, we want to fail here
			//It will dispose of the item to indicate this fault.
			if( item.isDisposed()){
				MessageBox dialog = new MessageBox(shell,SWT.ICON_ERROR | SWT.OK);
				dialog.setMessage("There has been an error loading this file, the filename is probably too long");
				dialog.setText("Error");
				dialog.open();
				
				return null;
			}
			
			mainfolder.setSelection(item);
			mainfolder.notifyListeners(SWT.Selection, new Event());
			item.setControl(editor);

			if (file.getType() != FileType.REPGEN || file.isLocal())
				install.setEnabled(false);
			else
				install.setEnabled(true);

			if (file.getType() != FileType.REPGEN || file.isLocal())
				run.setEnabled(false);
			else
				run.setEnabled(true);

			savetb.setEnabled(true);
			
			//Attach find/replace shell here as well (in addition to folder listener)
			findReplaceShell.attach(((EditorComposite)mainfolder.getSelection().getControl()).getStyledText(),true);
			
			if( !Config.getRecentFiles().contains(file) ) 
				Config.getRecentFiles().add(0, file);
			
			if( Config.getRecentFiles().size() > MAX_RECENTS)
				Config.getRecentFiles().remove(Config.getRecentFiles().size()-1);
			
			return editor;
		}
		
		return null;
	}

	private void doubleClickTreeItem() {
		TreeItem[] selection = tree.getSelection();
		if (selection.length != 1)
			return;

		TreeItem cur = selection[0];

		if ((cur.getData() instanceof SymitarFile)) {
			SymitarFile file = (SymitarFile) cur.getData();
			int sym = ((Project) cur.getParentItem().getData()).getSym();
			openFile(file);
		} else {
			doTree(cur);
		}
	}

	/**
	 * Expands or Collapses the item passed to it on the tree
	 * 
	 * @param item
	 */
	private void doTree(TreeItem item) {
		item.setExpanded(!item.getExpanded());

		if (item.getExpanded()) {
			Event e = new Event();
			e.item = item;
			tree.notifyListeners(SWT.Expand, e);
		}
	}

	private void addSym() {
		int sym = SymLoginShell.symLogin(display, shell, -1);

		if (sym != -1) {
			boolean exists = false;

			for (TreeItem current : tree.getItems()) {
				if (current.getData() instanceof Integer && ((Integer) current.getData()) == sym)
					exists = true;
			}

			if (!exists) {
				TreeItem item = new TreeItem(tree, SWT.NONE);
				item.setText("Sym " + sym);
				item.setImage(RepDevMain.smallSymImage);
				item.setData(sym);
				new TreeItem(item, SWT.NONE).setText("Loading...");
			}
		} 
	}
	
	private void addFolder(){
		DirectoryDialog dialog = new DirectoryDialog(shell,SWT.NONE);
		dialog.setMessage("Select a folder to mount in RepDev");
		String dir;
		
		if( (dir = dialog.open()) != null){
			boolean exists = false;

			for (TreeItem current : tree.getItems()) {
				if (current.getData() instanceof String && ((String) current.getData()).equals(dir))
					exists = true;
			}

			if (!exists) {
				TreeItem item = new TreeItem(tree, SWT.NONE);
				item.setText(dir.substring(dir.lastIndexOf("\\")));
				item.setImage(RepDevMain.smallFolderImage);
				item.setData(dir);
				new TreeItem(item, SWT.NONE).setText("Loading...");
				Config.getMountedDirs().add(dir);
			}
		}
	}

	private void removeSym(TreeItem currentItem) {
		int sym;

		while (!(currentItem.getData() instanceof Integer)) {
			currentItem = currentItem.getParentItem();

			if (currentItem == null)
				return;
		}

		sym = (Integer) currentItem.getData();

		MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
		dialog.setText("Confirm Sym Close");
		dialog.setMessage("Are you sure you want to close this Sym?");

		if (dialog.open() == SWT.OK) {
			ProjectManager.saveProjects(sym);
			RepDevMain.SYMITAR_SESSIONS.get(sym).disconnect();
			RepDevMain.SYMITAR_SESSIONS.remove(sym);

			currentItem.dispose();
		}

		tree.notifyListeners(SWT.Selection, null);
	}

	private void addProject() {
		int sym = -1;
		String dir = null;
		
		TreeItem[] selection = tree.getSelection();
		if (selection.length != 1)
			return;

		TreeItem cur = selection[0];
		while (cur.getParentItem() != null)
			cur = cur.getParentItem();

		if( cur.getData() instanceof Integer)
		  sym = (Integer) cur.getData();
		else if( cur.getData() instanceof String)
		  dir = (String) cur.getData();
		
		String str = NewProjShell.askForName(display, shell);
		
		if (str != null) {		
			Project proj = null;
			
			//TODO: Add error message for already existing projects
			if( cur.getData() instanceof Integer){
				if( ProjectManager.containsProject(sym, str))
					return;
				
				proj = ProjectManager.createProject(str, sym);
			}
			else if( cur.getData() instanceof String){
				if( ProjectManager.containsProject(dir, str))
					return;
				
				proj = ProjectManager.createProject(str, dir);
			}
				

			if (proj != null) {
				TreeItem item = new TreeItem(cur, SWT.NONE);
				item.setText(proj.getName());
				item.setData(proj);
				item.setImage(RepDevMain.smallProjectImage);
				new TreeItem(item, SWT.NONE).setText("Loading...");
			}
		}
	}

	private void removeProject(TreeItem cur) {
		while (cur != null && !(cur.getData() instanceof Project))
			cur = cur.getParentItem();

		if (cur == null)
			return;

		Project proj = (Project) cur.getData();
		RemProjShell.Result result = RemProjShell.confirm(display, shell, proj);

		if (result == RemProjShell.Result.OK_KEEP) {
			cur.dispose();
			ProjectManager.removeProject(proj, false);
		} else if (result == RemProjShell.Result.OK_DELETE) {
			cur.dispose();
			ProjectManager.removeProject(proj, true);
		}

		tree.notifyListeners(SWT.Selection, null);
	}

	private void importFiles() {
		TreeItem[] selection = tree.getSelection();
		if (selection.length != 1)
			return;

		TreeItem cur = selection[0];
		while (cur != null && !(cur.getData() instanceof Project))
			cur = cur.getParentItem();

		if (cur == null)
			return;

		Project proj = (Project) cur.getData();

		FileDialog dialog;
		
		if( isCurrentItemLocal())
			dialog = new FileDialog(shell, FileDialog.Mode.OPEN, getCurrentTreeDir());
		else
			dialog = new FileDialog(shell, FileDialog.Mode.OPEN, getCurrentTreeSym());
		
		ArrayList<SymitarFile> files = dialog.open();

		if (files != null) {
			for (SymitarFile file : files) {
				if (!proj.hasFile(file)) {
					proj.addFile(file);
					TreeItem item = new TreeItem(cur, SWT.NONE);
					item.setText(file.getName());
					item.setData(file);
					item.setImage(getFileImage(file));
				}
			}
			
			if( proj.isLocal() )
				ProjectManager.saveProjects(proj.getDir());
			else
				ProjectManager.saveProjects(proj.getSym());
		}
	}

	private void newFileInProject() {
		FileDialog dialog;
		
		if( isCurrentItemLocal())
			dialog = new FileDialog(shell, FileDialog.Mode.SAVE, getCurrentTreeDir());
		else
			dialog = new FileDialog(shell, FileDialog.Mode.SAVE, getCurrentTreeSym());
		
		ArrayList<SymitarFile> files = dialog.open();

		if (files.size() > 0) {
			SymitarFile file = files.get(0);

			TreeItem[] selection = tree.getSelection();
			if (selection.length != 1)
				return;

			TreeItem cur = selection[0];
			while (cur != null && !(cur.getData() instanceof Project))
				cur = cur.getParentItem();

			if (cur == null)
				return;

			Project proj = (Project) cur.getData();

			SessionError error = file.saveFile("");
			
			if( error == SessionError.NONE){
				if (!proj.hasFile(file)) {
					proj.addFile(file);
					TreeItem item = new TreeItem(cur, SWT.NONE);
					item.setText(file.getName());
					item.setData(file);
					item.setImage(getFileImage(file));
					
					if( proj.isLocal() )
						ProjectManager.saveProjects(proj.getDir());
					else
						ProjectManager.saveProjects(proj.getSym());
				}
				
				openFile(file);
				tree.notifyListeners(SWT.Selection, null);
			}
			
		}
	}

	private int removeFile(TreeItem cur, int lastResult) {
		if (!(cur.getData() instanceof SymitarFile))
			return 0;

		SymitarFile file = (SymitarFile) cur.getData();
		Project proj = (Project) cur.getParentItem().getData();
		int result = lastResult;
		
		if( (lastResult & RemFileShell.REPEAT) == 0 )
			result = RemFileShell.confirm(display, shell, proj, file);

		if ( (result & RemFileShell.OK) > 0 && (result & RemFileShell.DELETE) == 0 ) {
			proj.removeFile(file, false);
			cur.dispose();
		} else if ((result & RemFileShell.OK) > 0 && (result & RemFileShell.DELETE) > 0) {
			proj.removeFile(file, true);
			cur.dispose();
		}
		
		if( !proj.isLocal() )
			ProjectManager.saveProjects(proj.getSym());
		else
			ProjectManager.saveProjects(proj.getDir());

		tree.notifyListeners(SWT.Selection, null);
		for (CTabItem c : mainfolder.getItems()) {
			if (c.getData("file")!=null && c.getData("file").equals(file)){
				c.dispose();
			}
		}
		return result;
	}

	private void createExplorer(Composite self) {
		self.setLayout(new FormLayout());
		ToolBar toolbar = new ToolBar(coolBar, SWT.FLAT | SWT.HORIZONTAL);
		
		final ToolItem addSym = new ToolItem(toolbar, SWT.PUSH );
		addSym.setImage(RepDevMain.smallSymAddImage);
		addSym.setToolTipText("Add a new Sym to this list.");

		
		final ToolItem addFolder = new ToolItem(toolbar, SWT.PUSH);
		addFolder.setImage(RepDevMain.smallFolderAddImage);
		addFolder.setToolTipText("Mounts a local folder to store files and projects.");
		
		final ToolItem addProj = new ToolItem(toolbar, SWT.PUSH);
		addProj.setImage(RepDevMain.smallProjectAddImage);
		addProj.setToolTipText("Create a new project in the selected Sym.");
		addProj.setEnabled(false);

		final ToolItem newFile = new ToolItem(toolbar, SWT.PUSH);
		newFile.setImage(RepDevMain.smallFileAddImage);
		newFile.setToolTipText("Create a new file in your current project.");
		newFile.setEnabled(false);
		
		final ToolItem remItem = new ToolItem(toolbar, SWT.PUSH);
		remItem.setImage(RepDevMain.smallDeleteImage);
		remItem.setToolTipText("Remove the selected explorer items.");
		remItem.setEnabled(false);

		final ToolItem importFile = new ToolItem(toolbar, SWT.PUSH);
		importFile.setImage(RepDevMain.smallImportImage);
		importFile.setToolTipText("Import Existing Files to your current project.");
		importFile.setEnabled(false);
		
		final ToolItem openFileToolbar = new ToolItem(toolbar, SWT.PUSH);
		openFileToolbar.setImage(RepDevMain.smallFileOpenImage);
		openFileToolbar.setToolTipText("Open a file on the symitar server that's not in a project");
		openFileToolbar.setEnabled(false);


		toolbar.setData("explorer");
		addBar(toolbar);
		toolbar.pack();

		tree = new Tree(self, SWT.NONE | SWT.BORDER | SWT.MULTI );
		
		//Configure drag + drop
		Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		
		final DragSource source = new DragSource (tree, operations);
		source.setTransfer(types);
		
		
		source.addDragListener (new DragSourceListener () {
			public void dragStart(DragSourceEvent event) {		
				if( tree.getSelectionCount() == 0){
					event.doit=false;
					return;
				}
				
				//Only allow if all thiings are the same type
				Object first = tree.getSelection()[0].getData();
				boolean allSameType = true;
				
				for( TreeItem item : tree.getSelection()){
					if( !(item.getData().getClass().isInstance(first))){
						allSameType = false;
						break;
					}
				}
				
				if( allSameType && !(first instanceof Integer || first instanceof String) ){
					dragSourceItems = tree.getSelection();
					event.doit=true;
				}
				else
					event.doit=false;
			};
			public void dragSetData (DragSourceEvent event) {
				event.data = Arrays.asList(dragSourceItems).toString();
			}
			public void dragFinished(DragSourceEvent event) {
			}

		});
		
		DropTarget target = new DropTarget(tree, operations);
		target.setTransfer(types);
		target.addDropListener (new DropTargetAdapter() {
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
				if (event.item != null) {
					TreeItem item = (TreeItem)event.item;
					Point pt = display.map(null, tree, event.x, event.y);
					Rectangle bounds = item.getBounds();
					if (pt.y < bounds.y + bounds.height/3) {
						event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
					} else if (pt.y > bounds.y + 2*bounds.height/3) {
						event.feedback |= DND.FEEDBACK_INSERT_AFTER;
					} else {
						event.feedback |= DND.FEEDBACK_SELECT;
					}
					
				}
				else
					event.feedback = DND.FEEDBACK_NONE;
			}
			
			//TODO: Add Move stuff later
			public void drop(DropTargetEvent event) {
				if (event.data == null) {
					event.detail = DND.DROP_NONE;
					return;
				}
				
				String text = (String)event.data;
				System.out.println("Event.data: " + text);
				
				if (event.item == null) {
					System.out.println("Adding data to blank section");
				//	TreeItem item = new TreeItem(tree, SWT.NONE);
					//item.setText(text);
				}
				else{
					TreeItem root = (TreeItem)event.item;
					
					if( root.getData() instanceof SymitarFile ){
						root = root.getParentItem();
					}
					
					int overwrite;

					if( (getTreeDir(root) == null || getTreeDir(dragSourceItems[0]) == null || getTreeDir(root).equals(getTreeDir(dragSourceItems[0]))) && getTreeSym(root) == getTreeSym(dragSourceItems[0]) )
						overwrite = RepeatOperationShell.APPLY_TO_ALL | RepeatOperationShell.NO;
					else
						overwrite = RepeatOperationShell.ASK_TO_ALL | RepeatOperationShell.YES;
					
					shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
					
					if( dragSourceItems[0].getData() instanceof SymitarFile){
						for( TreeItem item : dragSourceItems){
							boolean exists = false;
							
							SymitarFile source = (SymitarFile)item.getData();
							SymitarFile destination;
							
							if( isItemLocal(root) ){
								destination = new SymitarFile(getTreeDir(root),source.getName());
							}
							else
								destination = new SymitarFile(getTreeSym(root),source.getName(),source.getType());
							
							if( (overwrite & RepeatOperationShell.ASK_TO_ALL) != 0)
								exists = Util.fileExists(destination);
							
							System.out.println("exists: " + exists);
							
							if( exists && (overwrite & RepeatOperationShell.ASK_TO_ALL) != 0 )
							{
								RepeatOperationShell dialog = new RepeatOperationShell(shell,"File " + destination.getName() + " already exits at the destination. Overwrite?");
								overwrite = dialog.open();
								
								if( (overwrite & RepeatOperationShell.CANCEL) != 0 ){
									shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
									return;
								}
							}
							
							SessionError error = SessionError.NONE;
							
							if((overwrite & RepeatOperationShell.YES) != 0)
								error = destination.saveFile(source.getData());
							
							if( error == SessionError.NONE && root.getData() instanceof Project)
								((Project)root.getData()).addFile(destination);
						}
					}
					else if( dragSourceItems[0].getData() instanceof Project ){
						for( TreeItem item : dragSourceItems){
							Project source = (Project)item.getData();
							Project destination;
							
							if( isItemLocal(root))
								destination = ProjectManager.createProject(source.getName(),getTreeDir(root));
							else
								destination = ProjectManager.createProject(source.getName(), getTreeSym(root));
							
							for( SymitarFile file : source.getFiles()){
								boolean exists = false;
								
								SymitarFile newFile;
								
								if( isItemLocal(root))
									newFile = new SymitarFile(getTreeDir(root),file.getName());
								else
									newFile = new SymitarFile(getTreeSym(root),file.getName(),file.getType());
								
								if( (overwrite & RepeatOperationShell.ASK_TO_ALL) != 0)
									exists = Util.fileExists(newFile);
								
								if( exists && (overwrite & RepeatOperationShell.ASK_TO_ALL) != 0 )
								{
									RepeatOperationShell dialog = new RepeatOperationShell(shell,"File " + newFile.getName() + " already exits at the destination. Overwrite?");
									overwrite = dialog.open();
									
									if( (overwrite & RepeatOperationShell.CANCEL) != 0 ){
										shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
										return;
									}
								}
								
								SessionError error = SessionError.NONE;
								
								if((overwrite & RepeatOperationShell.YES) != 0)
									error = newFile.saveFile(file.getData());
									
								if( error == SessionError.NONE)
									destination.addFile(newFile);
							}
						}
					}
					
					//Dont redraw if we added to sym/dir
					if( dragSourceItems[0].getData() instanceof Project ||
						(dragSourceItems[0].getData() instanceof SymitarFile && !(root.getData() instanceof String || root.getData() instanceof Integer))){
						
						if( root.getData() instanceof SymitarFile)
							root = root.getParentItem().getParentItem();	
						else if( root.getData() instanceof Project )
							root = root.getParentItem();

						//Redraws the tree, since it do be VIRTUAL!!!!	
						for( TreeItem victim : root.getItems())
							victim.dispose();
				
						root.clearAll(true);
						root.setExpanded(false);
						
						tree.showItem(root);
						Event e= new Event();
						e.item = root;
						tree.notifyListeners(SWT.Expand, e);
						
						//if( !(root.getData() instanceof String || root.getData() instanceof Integer))
						//	root = root.getParentItem();
						
						//root.dispose();
						
					}
					
					shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
				}
			}
		});
		
		for (int sym : Config.getSyms()) {
			TreeItem item = new TreeItem(tree, SWT.NONE);
			item.setText("Sym " + sym);
			item.setImage(RepDevMain.smallSymImage);
			item.setData(sym);
			new TreeItem(item, SWT.NONE).setText("Loading...");
		}
		
		for (String dir : Config.getMountedDirs()) {
			TreeItem item = new TreeItem(tree, SWT.NONE);
			item.setText(dir.substring(dir.lastIndexOf("\\")));
			item.setImage(RepDevMain.smallFolderImage);
			item.setData(dir);
			new TreeItem(item, SWT.NONE).setText("Loading...");
		}

		Menu treeMenu = new Menu(tree);

		final Menu newMenu = new Menu(treeMenu);
		MenuItem newItem = new MenuItem(treeMenu, SWT.CASCADE);
		newItem.setMenu(newMenu);
		newItem.setText("New...");

		final MenuItem newFreeFile = new MenuItem(newMenu, SWT.NONE);
		newFreeFile.setImage(RepDevMain.smallFileNewImage);
		newFreeFile.setText("New File");
		newFreeFile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog;
				
				if( isCurrentItemLocal())
					dialog = new FileDialog(shell, FileDialog.Mode.SAVE, getCurrentTreeDir());
				else
					dialog = new FileDialog(shell, FileDialog.Mode.SAVE, getCurrentTreeSym());
				
				
				ArrayList<SymitarFile> files = dialog.open();

				if (files.size() > 0) {
					SymitarFile file = files.get(0);

					file.saveFile("");
					openFile(file);
				}
			}
		});

		final MenuItem newProjectFile = new MenuItem(newMenu, SWT.NONE);
		newProjectFile.setImage(RepDevMain.smallFileAddImage);
		newProjectFile.setText("New File in Project");
		newProjectFile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newFileInProject();
			}
		});

		new MenuItem(newMenu, SWT.SEPARATOR);

		final MenuItem newProject = new MenuItem(newMenu, SWT.NONE);
		newProject.setText("Project");
		newProject.setImage(RepDevMain.smallProjectAddImage);
		newProject.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addProject();
			}
		});
		
		final MenuItem openAllItem = new MenuItem(treeMenu, SWT.NONE);
		openAllItem.setText("Open All in Project");
		openAllItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] selection = tree.getSelection();
				if (selection.length != 1)
					return;

				TreeItem cur = selection[0];
				while (cur != null && !(cur.getData() instanceof Project))
					cur = cur.getParentItem();

				if (cur == null)
					return;

				Project proj = (Project) cur.getData();
				
				
				shell.setCursor(display.getSystemCursor(SWT.CURSOR_WAIT));
				ArrayList<SymitarFile> files = proj.getFiles();
				for( SymitarFile file: files ) {
					openFile(file);
				}
				shell.setCursor(display.getSystemCursor(SWT.CURSOR_ARROW));
			}			
		});
		
		
		new MenuItem(treeMenu, SWT.SEPARATOR);
		
		final Menu runMenu = new Menu(treeMenu);
		final MenuItem runMenuItem = new MenuItem(treeMenu,SWT.CASCADE);
		runMenuItem.setMenu(runMenu);
		runMenuItem.setText("Run");
		runMenuItem.setImage(RepDevMain.smallRunImage);
		
		MenuItem runReport = new MenuItem(runMenu,SWT.NONE);
		runReport.setText("Run Report");
		runReport.setImage(RepDevMain.smallRunImage);
		runReport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				runReport();
			}
		});
		
		
		MenuItem openLastReport = new MenuItem(runMenu,SWT.NONE);
		openLastReport.setText("Open Last Report Run");
		openLastReport.setImage(RepDevMain.smallFileOpenImage);
		openLastReport.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				Object data = tree.getSelection()[0].getData();
				
				if( !(data instanceof SymitarFile) )
					return;
				
				shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
				
				SymitarFile file = (SymitarFile)data;
				
				ArrayList<Sequence> seqs = RepDevMain.SYMITAR_SESSIONS.get(file.getSym()).getReportSeqs(file.getName(), -1, 40,1);
				
				shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
				
				if( seqs != null && seqs.size() > 0){
					openFile(seqs.get(0), file.getSym());
				}
				else
				{
					MessageBox dialog = new MessageBox(shell,SWT.OK | SWT.ICON_ERROR);
					dialog.setMessage("This report was not found within the last 40 REPWRITER jobs");
					dialog.setText("Report Not Found");
					dialog.open();
				}			
				
			}
			
		});
		
		MenuItem findReport = new MenuItem(runMenu,SWT.NONE);
		findReport.setText("Find runs in Print History");
		findReport.setImage(RepDevMain.smallFindImage);
		findReport.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				Object obj = tree.getSelection()[0].getData();
				
				if( !(obj instanceof SymitarFile) )
					return;
				
				shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
				
				final SymitarFile file = (SymitarFile)obj;
				
				ArrayList<Sequence> seqs = RepDevMain.SYMITAR_SESSIONS.get(file.getSym()).getReportSeqs(file.getName(), -1, 40,10);
				
				shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
				
				if( seqs != null && seqs.size() > 0){
					//Create mini shell to pick report
					final Shell dialog = new Shell(shell,SWT.DIALOG_TRIM);
					FormLayout layout = new FormLayout();
					layout.marginTop = 5;
					layout.marginBottom = 5;
					layout.marginLeft = 5;
					layout.marginRight = 5;
					layout.spacing = 5;
					dialog.setLayout(layout);
					
					FormData data;
					dialog.setText("Please select which report run to view");
					
					Label label = new Label(dialog,SWT.NONE);
					label.setText("Report Run: ");

					
					final Combo combo = new Combo(dialog,SWT.READ_ONLY);
					data = new  FormData();

					int i = 0;
					for(Sequence seq : seqs){
						combo.add(seq.toString());
						combo.setData(String.valueOf(i), seq);
						i++;
					}
					
					combo.select(0);
					
					Button cancelButton = new Button(dialog,SWT.PUSH);
					cancelButton.setText("Cancel");
					cancelButton.addSelectionListener(new SelectionAdapter(){

						@Override
						public void widgetSelected(SelectionEvent e) {
							dialog.close();
						}
						
					});
	
					Button okButton = new Button(dialog,SWT.PUSH);
					okButton.setText("View Report");
					okButton.addSelectionListener(new SelectionAdapter(){

						@Override
						public void widgetSelected(SelectionEvent e) {
							openFile((Sequence)combo.getData(String.valueOf(combo.getSelectionIndex())), file.getSym());
							dialog.close();
						}
						
					});
					
					data = new FormData();
					data.left = new FormAttachment(0);
					data.top = new FormAttachment(0);
					label.setLayoutData(data);
					
					data = new FormData();
					data.left = new FormAttachment(label);
					data.top = new FormAttachment(0);
					data.right = new FormAttachment(100);
					combo.setLayoutData(data);
					
					data = new FormData();
					data.right = new FormAttachment(100);
					data.top = new FormAttachment(combo);
					cancelButton.setLayoutData(data);
					
					data = new FormData();
					data.right = new FormAttachment(cancelButton);
					data.top = new FormAttachment(combo);
					okButton.setLayoutData(data);
					

					dialog.layout(true,true);
					dialog.pack();
					dialog.open();
				}
				else
				{
					MessageBox dialog = new MessageBox(shell,SWT.OK | SWT.ICON_ERROR);
					dialog.setMessage("This report was not found within the last 40 REPWRITER jobs");
					dialog.setText("Report Not Found");
					dialog.open();
				}			
				
			}
			
		});
		

		new MenuItem(treeMenu, SWT.SEPARATOR);

		final MenuItem deleteFile = new MenuItem(treeMenu, SWT.NONE);
		deleteFile.setText("Remove");		
		deleteFile.setImage(RepDevMain.smallDeleteImage);
		deleteFile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeItem(tree.getSelection());
			}

		});
		
		final MenuItem renameFile = new MenuItem(treeMenu, SWT.NONE);
		renameFile.setText("Rename Item");
		renameFile.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				if( tree.getSelectionCount() == 1)
					renameItem(tree.getSelection()[0]);
			}

		}); 

		
		final MenuItem compareFile = new MenuItem(treeMenu, SWT.NONE);
		compareFile.setText("Compare Two Files");
		compareFile.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				compare();
			}

		}); 

		
		new MenuItem(treeMenu, SWT.SEPARATOR);

		final MenuItem openFile = new MenuItem(treeMenu, SWT.NONE);
		openFile.setText("Open Existing...");
		openFile.setImage(RepDevMain.smallFileOpenImage);
		openFile.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog;
				
				if( isCurrentItemLocal() )
					dialog = new FileDialog(shell, FileDialog.Mode.OPEN, getCurrentTreeDir());
				else
					dialog = new FileDialog(shell, FileDialog.Mode.OPEN, getCurrentTreeSym());

				for (SymitarFile file : dialog.open())
					openFile(file);
			}

		});

		final MenuItem importFilem = new MenuItem(treeMenu, SWT.NONE);
		importFilem.setText("Import Files to Project");
		importFilem.setImage(RepDevMain.smallImportImage);
		importFilem.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				importFiles();
			}

		});
		

		treeMenu.addMenuListener(new MenuListener() {

			public void menuHidden(MenuEvent e) {
			}

			public void menuShown(MenuEvent e) {	
				//Disable everything but remove first, then add menu options later
				newFile.setEnabled(false);
				newProjectFile.setEnabled(false);
				importFilem.setEnabled(false);
				openAllItem.setEnabled(false);
				runMenuItem.setEnabled(false);
				newFreeFile.setEnabled(false);
				newProject.setEnabled(false);
				openFile.setEnabled(false);
				
				deleteFile.setEnabled(false);
				
				if( tree.getSelectionCount() == 0)
					return;
				
				//Only allow deletes if all selections are same type
				Object first = tree.getSelection()[0].getData();
				boolean allSameType = true;
				
				for( TreeItem item : tree.getSelection()){
					if( !(item.getData().getClass().isInstance(first))){
						allSameType = false;
						break;
					}
				}
				
				deleteFile.setEnabled(allSameType);
				
				
				if( tree.getSelectionCount() == 1){
					newFreeFile.setEnabled(true);
					newProject.setEnabled(true);
					openFile.setEnabled(true);
					
					renameFile.setEnabled( !( tree.getSelection()[0].getData() instanceof String ||tree.getSelection()[0].getData() instanceof Integer ));
				}
	
				if( tree.getSelectionCount() == 2 && tree.getSelection()[0].getData() instanceof SymitarFile && tree.getSelection()[0].getData() instanceof SymitarFile )
					compareFile.setEnabled(true);
				else
					compareFile.setEnabled(false);
				
				if ( !(tree.getSelection()[0].getData() instanceof Integer || tree.getSelection()[0].getData() instanceof String) && tree.getSelectionCount() == 1) {
					importFilem.setEnabled(true);
					newProjectFile.setEnabled(true);
				}
				
				if( tree.getSelection()[0].getData() instanceof Project && tree.getSelectionCount() == 1 ) {
					openAllItem.setEnabled(true);
				} 
					
				if( tree.getSelectionCount() == 1 && tree.getSelection()[0].getData() instanceof SymitarFile && ((SymitarFile)tree.getSelection()[0].getData()).getType() == FileType.REPGEN && !((SymitarFile)tree.getSelection()[0].getData()).isLocal())
					runMenuItem.setEnabled(true);
				
			}

		});

		tree.setMenu(treeMenu);
		
		tree.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				if( e.keyCode == SWT.DEL ){
					deleteFile.notifyListeners(SWT.Selection, null);
				}
				else if( e.keyCode == SWT.F2){
					renameFile.notifyListeners(SWT.Selection, null);
				}else if( e.keyCode == '\r' || e.keyCode == 16777296){
					TreeItem[] selection = tree.getSelection();
					for(int i = 0; i < selection.length; i++){
						TreeItem cur = selection[i];
						if( cur.getData() instanceof SymitarFile ){
							SymitarFile file = (SymitarFile) cur.getData();
							openFile(file);
						}
					}
				}
				//System.out.println("-"+e.keyCode+"-");
				//Maybe make it open selected files when you hit enter -- fixed
			}
		});

		tree.addListener(SWT.Expand, new Listener() {
			public void handleEvent(Event e) {
				final TreeItem root = (TreeItem) e.item;

				for (TreeItem child : root.getItems())
					child.dispose();

				if (root.getData() instanceof Integer || root.getData() instanceof String) {
					if( root.getData() instanceof Integer){
						SymitarSession session = RepDevMain.SYMITAR_SESSIONS.get(root.getData());
	
						if (session == null || !session.isConnected()) {
							if (SymLoginShell.symLogin(display, shell, (Integer) root.getData()) == -1) {
								display.asyncExec(new Runnable() {
	
									public void run() {
										TreeItem newItem = new TreeItem(root, SWT.NONE);
										newItem.setText("Loading...");
										newItem.setExpanded(false);
										root.setExpanded(false);
									}
	
								});
	
								return;
							}
						}
					}
					ArrayList<Project> projects = new ArrayList<Project>();
					
					if( root.getData() instanceof Integer)
						projects = ProjectManager.getProjects((Integer)root.getData());
					else if( root.getData() instanceof String)
						projects = ProjectManager.getProjects((String)root.getData());
					
					for (Project proj : projects) {
						TreeItem item = new TreeItem(root, SWT.NONE);
						item.setText(proj.getName());
						item.setData(proj);
						item.setImage(RepDevMain.smallProjectImage);
						new TreeItem(item, SWT.NONE).setText("Loading...");
					}
				} else if (root.getData() instanceof Project) {
					for (SymitarFile file : ((Project) root.getData()).getFiles()) {
						TreeItem item = new TreeItem(root, SWT.NONE);
						item.setText(file.getName());
						item.setImage(getFileImage(file));
						item.setData(file);
					}
				}
			}
		});

		tree.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				TreeItem[] selection = tree.getSelection();

				remItem.setEnabled(selection.length != 0);
				
				if (selection.length != 1) {
					addProj.setEnabled(false);
					importFile.setEnabled(false);
					newFile.setEnabled(false);
					openFileToolbar.setEnabled(false);
					return;
				}
				else{
					openFileToolbar.setEnabled(true);
				}

				Object data = ((TreeItem) selection[0]).getData();
				if (data instanceof Integer) {
					addProj.setEnabled(true);
					importFile.setEnabled(false);
					newFile.setEnabled(false);
				} else if( data instanceof String){
					addProj.setEnabled(true);
					importFile.setEnabled(false);
					newFile.setEnabled(false);
				} else if (data instanceof Project) {
					addProj.setEnabled(true);
					importFile.setEnabled(true);
					newFile.setEnabled(true);
				} else if (data instanceof SymitarFile) {
					addProj.setEnabled(true);
					importFile.setEnabled(true);
					newFile.setEnabled(true);
				}
			}
		});

		tree.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				doubleClickTreeItem();
			}
		});

		addSym.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addSym();
			}
		});
		
		addFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addFolder();
			}
		});
		

		addProj.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addProject();
			}
		});
		importFile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				importFiles();
			}
		});
		remItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeItem(tree.getSelection());
			}
		});
		newFile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newFileInProject();
			}
		});
		openFileToolbar.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog;
				
				if( isCurrentItemLocal() )
					dialog = new FileDialog(shell, FileDialog.Mode.OPEN, getCurrentTreeDir());
				else
					dialog = new FileDialog(shell, FileDialog.Mode.OPEN, getCurrentTreeSym());

				for (SymitarFile file : dialog.open())
					openFile(file);
			}
		});

		FormData frmToolbar = new FormData();
		frmToolbar.top = new FormAttachment(0);
		frmToolbar.left = new FormAttachment(0);
		frmToolbar.right = new FormAttachment(100);
		toolbar.setLayoutData(frmToolbar);

		FormData frmTree = new FormData();
		frmTree.top = new FormAttachment(toolbar);
		frmTree.left = new FormAttachment(0);
		frmTree.right = new FormAttachment(100);
		frmTree.bottom = new FormAttachment(100);
		tree.setLayoutData(frmTree);
	}
	
	private void handleRenameItem(TreeItem item, String newName){
		for (CTabItem c : mainfolder.getItems()) {
			System.out.println(c.getData("file")+","+item.getData());
			if (c.getData("file").equals(item.getData())){
				c.setText(newName);
			}
			System.out.println(c.getData("file")+","+item.getData());
		}
		if( item.getData() instanceof SymitarFile)
			if( ((SymitarFile)item.getData()).saveName(newName) )
				item.setText(newName);
		if( item.getData() instanceof Project){
			((Project)item.getData()).setName(newName);
			item.setText(newName);
		}
		//item.setText(file.getName());
		//item.setText(newName);
		/*if( mainfolder.getSelectionIndex() != -1){
			if( confirmClose(mainfolder.getSelection()) ){
				clearErrorList(mainfolder.getSelection());
				mainfolder.getSelection().dispose();
				setLineColumn();
			}
		}
		//TreeItem[] selection = tree.getSelection();
		//TreeItem cur = selection[0];
		SymitarFile file = (SymitarFile) item.getData();
		openFile(file);*/
	}

	protected void renameItem(final TreeItem item) {
		boolean showBorder = true;
		final TreeEditor editor = new TreeEditor(tree);
		final Composite composite = new Composite(tree, SWT.NONE);
		final Text text = new Text(composite, SWT.NONE);
		final int inset = showBorder ? 1 : 0;

		//NONO for syms and dirs
		if( item.getData() instanceof String || item.getData() instanceof Integer)
			return;
		
		if (showBorder)
			composite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));

		composite.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				Rectangle rect = composite.getClientArea();
				text.setBounds(rect.x + inset, rect.y + inset, rect.width - inset * 2, rect.height - inset * 2);
			}
		});
		
		Listener textListener = new Listener() {
			public void handleEvent(final Event e) {
				switch (e.type) {
				case SWT.FocusOut:
					handleRenameItem(item, text.getText());
					composite.dispose();
					break;
				case SWT.Verify:
					String newText = text.getText();
					String leftText = newText.substring(0, e.start);
					String rightText = newText.substring(e.end, newText.length());
					
					GC gc = new GC(text);
					Point size = gc.textExtent(leftText + e.text + rightText);
					gc.dispose();
					
					size = text.computeSize(size.x, SWT.DEFAULT);
					editor.horizontalAlignment = SWT.LEFT;
					
					Rectangle itemRect = item.getBounds(),
					rect = tree.getClientArea();
					editor.minimumWidth = Math.max(size.x, itemRect.width) + inset * 2;
					int left = itemRect.x,
					right = rect.x + rect.width;
					
					editor.minimumWidth = Math.min(editor.minimumWidth, right - left);
					editor.minimumHeight = size.y + inset * 2;
					editor.layout();
					break;
				case SWT.Traverse:
					switch (e.detail) {
					case SWT.TRAVERSE_RETURN:
						handleRenameItem(item, text.getText());
						// FALL THROUGH
					case SWT.TRAVERSE_ESCAPE:
						composite.dispose();
						e.doit = false;
					}
					break;
				}
			}
		};
		
		text.addListener(SWT.FocusOut, textListener);
		text.addListener(SWT.Traverse, textListener);
		text.addListener(SWT.Verify, textListener);
		
		editor.setEditor(composite, item);
		
		text.setText(item.getText());
		text.selectAll();
		text.setFocus();
	}

	protected void removeItem(TreeItem[] selection) {
		int lastResult = RemFileShell.CANCEL;
		
		if (selection.length == 0)
			return;

		for( TreeItem cur : selection){
			Object data = cur.getData();
		
			if (data instanceof Integer)
				removeSym(cur);
			else if( data instanceof String)
				removeDir(cur);
			else if (data instanceof Project)
				removeProject(cur);
			else
				lastResult = removeFile(cur,lastResult);
		}
	}

	protected void compare() {
		if( tree.getSelectionCount() != 2)
			return;
		
		if( !(tree.getSelection()[0].getData() instanceof SymitarFile) )
			return;
		
		if( !(tree.getSelection()[1].getData() instanceof SymitarFile) )
			return;
				
		CTabItem item = new CTabItem(mainfolder, SWT.CLOSE);

		item.setText("Compare Text BETA");
		//item.setImage(getFileImage(file));
		//item.setData("file", file);
		//item.setData("loc", loc);

		item.setControl(new CompareComposite(mainfolder,item,(SymitarFile)tree.getSelection()[0].getData(),(SymitarFile)tree.getSelection()[1].getData()));
		
		mainfolder.setSelection(item);
	}

	private void removeDir(TreeItem currentItem) {
		String dir;

		while (!(currentItem.getData() instanceof String)) {
			currentItem = currentItem.getParentItem();

			if (currentItem == null)
				return;
		}

		dir = (String) currentItem.getData();

		MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
		dialog.setText("Confirm Directory Close");
		dialog.setMessage("Are you sure you want to close this directory?");

		if (dialog.open() == SWT.OK) {
			ProjectManager.saveProjects(dir);
			Config.getMountedDirs().remove(dir);

			currentItem.dispose();
		}

		tree.notifyListeners(SWT.Selection, null);
	}

	public void createStatusBar() {
		statusBar = new Composite( shell, SWT.BORDER | SWT.SHADOW_IN );
		
		// Layout Stuff
		FormLayout slayout = new FormLayout();
		

		statusBar.setLayout(slayout);
		FormData statusBarData = new FormData();
		statusBarData.left = new FormAttachment(0);
		statusBarData.right = new FormAttachment(100);
		statusBarData.bottom = new FormAttachment(100);
		statusBarData.height = 16;
	    statusBar.setLayoutData(statusBarData);
	    	    
	    final Label verLabel = new Label(statusBar, SWT.NONE );
	    verLabel.setText( "RepDev " + RepDevMain.VERSION + " ");
	    verLabel.setSize(100,16);
	    FormData data = new FormData();
	    data.left = new FormAttachment(0);
	    verLabel.setLayoutData(data);
	    
	    Label sep1 = new Label(statusBar,SWT.SEPARATOR);
	    data = new FormData();
	    data.left = new FormAttachment(verLabel);
	    sep1.setLayoutData(data);
	    
	    lineColumn = new Label(statusBar, SWT.NONE);
	    data = new FormData();
	    data.left = new FormAttachment(sep1);
	    lineColumn.setLayoutData(data);
	    
	    setLineColumn();
	}
	
	public void setLineColumn() {
		int line, col;
		
		if( mainfolder.getSelection() != null && mainfolder.getSelection().getControl() instanceof TabTextView )
		{
			StyledText txt = ((TabTextView)mainfolder.getSelection().getControl()).getStyledText(); 
			line = txt.getLineAtOffset(txt.getCaretOffset()) ;
			col = txt.getCaretOffset() - txt.getOffsetAtLine(line) + 1;
			
			lineColumn.setText("Location: " + (line + 1) + " : " + col);
			statusBar.pack();
		}
		else
			lineColumn.setText("");
	}
	
	protected void runReport() {
		Object data = tree.getSelection()[0].getData();
		
		if( !(data instanceof SymitarFile) )
			return;
		
		runReport((SymitarFile)data);		
	}
	
	public void runReport(SymitarFile file){
		RunReportShell dialog = new RunReportShell(shell,file);
		dialog.open();
	}

	private int getTreeSym(TreeItem item){
		int sym = -1;
		Object data = item.getData();

		if (data instanceof Integer)
			sym = (Integer) data;
		else if (data instanceof Project)
			sym = ((Project) data).getSym();
		else if( item.getParentItem() != null)
			sym = ((Project) item.getParentItem().getData()).getSym();

		return sym;
	}
	
	/**
	 * 
	 * @return Sym of currently selected tree item
	 */
	private int getCurrentTreeSym() {
		if( tree.getSelectionCount()== 0)
			return -1;
		
		return getTreeSym(tree.getSelection()[0]);
	}
	
	private String getTreeDir(TreeItem item){
		String dir = "";
		
		Object data = item.getData();

		if( data instanceof Integer)
			return null;
		if (data instanceof String)
			dir = (String) data;
		else if (data instanceof Project)
			dir = ((Project) data).getDir();
		else
			dir = ((Project) item.getParentItem().getData()).getDir();

		return dir;
	}
	
	private String getCurrentTreeDir(){
		if( tree.getSelectionCount()== 0)
			return "";
		
		return getTreeDir(tree.getSelection()[0]);
	}
	
	private boolean isItemLocal(TreeItem item){
		Object data = item.getData();

		if( data instanceof String)
			return true;
		else if (data instanceof Integer)
			return false;
		else if (data instanceof Project)
			return ((Project) data).getDir() != null;
		else
			return ((SymitarFile) data).isLocal();
	}
	
	private boolean isCurrentItemLocal(){
		if( tree.getSelectionCount()== 0)
			return false;
		
		return isItemLocal(tree.getSelection()[0]);		
	}
	
	private Image drawSymOverImage(Image img, int sym){
		Image image = new Image(display, 16, 16);

		GC gc = new GC(image);
		gc.drawImage(img, 0,0);			
		gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
		gc.setAlpha(254);
		
		if( sym < 100){
			gc.setFont(new Font(Display.getCurrent(),"Courier New",8,SWT.BOLD));
			gc.drawString(String.valueOf(sym), 16-7*String.valueOf(sym).length(),5,true);
		}
		else{
			gc.setFont(new Font(Display.getCurrent(),"Courier New",7,SWT.BOLD));
			gc.drawString(String.valueOf(sym), 0,5,true);
		}
		gc.dispose();
		
		ImageData imageData = image.getImageData();
		PaletteData palette = new PaletteData(new RGB[] { new RGB(0, 0, 0), new RGB(0xFF, 0xFF, 0xFF), });
		ImageData maskData = new ImageData(16, 16, 1, palette);
		Image mask = new Image(display, maskData);
		gc = new GC(mask);
		gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		gc.fillRectangle(0,0,16,16);
		gc.dispose();
		maskData = mask.getImageData();

		return new Image(display, imageData, maskData);
	}

	public Image getFileImage(SymitarFile file) {
		Image img;

		if( file.isLocal() )
			return RepDevMain.smallRepGenImage;
		
		switch (file.getType()) {
		case REPGEN:
			img = drawSymOverImage(RepDevMain.smallRepGenImage, file.getSym());
			break;
		default:
			img = drawSymOverImage(RepDevMain.smallFileImage, file.getSym());
		}

		return img;

	}

	private void createEditorPane(Composite self) {
		self.setLayout(new FillLayout());
		mainfolder = new CTabFolder(self, SWT.FLAT | SWT.TOP | SWT.BORDER);
		mainfolder.setLayout(new FillLayout());
		mainfolder.setSimple(false);
		
		Menu tabContextMenu = new Menu(mainfolder);
		mainfolder.setMenu(tabContextMenu);
				
		mainfolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
					if (mainfolder.getSelection() != null
						&& (mainfolder.getSelection().getControl()) instanceof EditorComposite) {
					SymitarFile file = ((EditorComposite) mainfolder
							.getSelection().getControl()).getFile();
					if (file.getType() != FileType.REPGEN || file.isLocal())
						install.setEnabled(false);
					else
						install.setEnabled(true);

					if (file.getType() != FileType.REPGEN || file.isLocal())
						run.setEnabled(false);
					else
						run.setEnabled(true);

					savetb.setEnabled(true);
					print.setEnabled(true);
				} else {
					print.setEnabled(true);
					savetb.setEnabled(false);
					run.setEnabled(false);
					install.setEnabled(false);
				}
			}
		});
				
		final MenuItem closeTab = new MenuItem(tabContextMenu,SWT.NONE);
		closeTab.setText("Close Tab");
		closeTab.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {		
				if( mainfolder.getSelectionIndex() != -1){
					if( confirmClose(mainfolder.getSelection()) ){
						clearErrorList(mainfolder.getSelection());
						mainfolder.getSelection().dispose();
						setLineColumn();
					}
				}
			}
			
		});
		
		final MenuItem closeOthers = new MenuItem(tabContextMenu,SWT.NONE);
		closeOthers.setText("Close Others");
		closeOthers.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				if( mainfolder.getItems().length > 1){
	
					for( CTabItem item : mainfolder.getItems())
						if( !item.equals(mainfolder.getSelection()))
							if( confirmClose(item) ){
								clearErrorList(item);
								item.dispose();
							}
				
				}
			}
			
		});
		
		final MenuItem closeAll = new MenuItem(tabContextMenu,SWT.NONE);
		closeAll.setText("Close All");
		closeAll.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				if( mainfolder.getItems().length >= 1){
	
					for( CTabItem item : mainfolder.getItems())
						if( confirmClose(item) ){
							clearErrorList(item);
							item.dispose();
						}
				
				}
			}
			
		});

		final MenuItem separator = new MenuItem(tabContextMenu, SWT.SEPARATOR );
		
		
		final MenuItem save = new MenuItem(tabContextMenu, SWT.None );
		save.setText("Save");
		save.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if( mainfolder.getSelectionIndex() != -1 
						&& (mainfolder.getSelection().getControl() instanceof EditorComposite) ) {
					((EditorComposite)mainfolder.getSelection().getControl()).saveFile(true);
				} else {
					System.out.println("Error:  Can not save non-EditorComposite File");
				}
			}
		});
		
		
		final MenuItem saveAll = new MenuItem(tabContextMenu,SWT.NONE);
		saveAll.setText("Save All");
		saveAll.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				saveAllRepgens();
			}
			
		});
		
		final MenuItem installRepgen = new MenuItem(tabContextMenu, SWT.NONE);
		installRepgen.setText("Install");
		installRepgen.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if( mainfolder.getSelectionIndex() != -1 
						&& (mainfolder.getSelection().getControl() instanceof EditorComposite) ) {
					
					((EditorComposite)mainfolder.getSelection().getControl()).installRepgen(true);					
				}
			}
		});
		
		tabContextMenu.addMenuListener(new MenuAdapter(){

			@Override
			public void menuShown(MenuEvent e) {
				boolean flag = mainfolder.getSelectionIndex() != -1;
				
				closeTab.setEnabled(flag);
				closeAll.setEnabled(flag);
				
				save.setEnabled( (flag 
						&& (mainfolder.getSelection().getControl() instanceof EditorComposite) 
						&& mainfolder.getSelection().getData("modified") != null 
						&& (Boolean) mainfolder.getSelection().getData("modified") ) );
				
				saveAll.setEnabled(flag);
				installRepgen.setEnabled( flag 
						&& (mainfolder.getSelection().getControl() instanceof EditorComposite) 
						&& !((EditorComposite)mainfolder.getSelection().getControl()).getFile().isLocal() );
				
				closeOthers.setEnabled(mainfolder.getItems().length>1);
				
				
			}
						
		});
		
		//Make the find/replace box know which thing we are looking through, if the window is open as we switch tabs
		mainfolder.addSelectionListener( new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				if( mainfolder.getSelection() != null && mainfolder.getSelection().getControl() instanceof EditorComposite )
					findReplaceShell.attach(((EditorComposite)mainfolder.getSelection().getControl()).getStyledText(), true);
				else if( mainfolder.getSelection() != null && mainfolder.getSelection().getControl() instanceof ReportComposite )
					findReplaceShell.attach(((ReportComposite)mainfolder.getSelection().getControl()).getStyledText(), false);
			}
		});
		
		mainfolder.addCTabFolder2Listener(new CTabFolder2Adapter(){
			public void close(CTabFolderEvent event) {
				event.doit = confirmClose( (CTabItem)event.item );
				setLineColumn();
				
				if( event.doit ){
					clearErrorList((CTabItem)event.item);
				}
				
				if(mainfolder.getItemCount()==1) {
					install.setEnabled(false);
					savetb.setEnabled(false);
					print.setEnabled(false);
					run.setEnabled(false);
				}				
			}		
		});
	}

	protected void clearErrorList(CTabItem item) {
		SymitarFile file = (SymitarFile)(item).getData("file");
		
		if( file != null){
			//Remove from error list
			for (TableItem eItem : tblErrors.getItems()) {
				if (((SymitarFile) eItem.getData("file")).equals(file))
					eItem.dispose();
			}
		}
	}

	private boolean confirmClose(CTabItem item) {
		if (item != null && item.getData("modified") != null && ((Boolean) item.getData("modified"))) {
			MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
			dialog.setText("Confirm File Close");
			
			if( item.getData("loc") instanceof Integer )
				dialog.setMessage("The file '" + item.getData("file") + "' on Sym " + item.getData("loc") + " has been modified, do you want to save it before closing it?");
			else
				dialog.setMessage("The file '" + item.getData("file") + "' in directory " + item.getData("loc") + " has been modified, do you want to save it before closing it?");

			int result = dialog.open();

			if (result == SWT.CANCEL)
				return false;
			else if (result == SWT.YES) {
				((EditorComposite) item.getControl()).saveFile(false);
			}
		}

		if( mainfolder.getSelection().getControl() instanceof EditorComposite )
			for( TableItem tItem : tblErrors.getItems() )
				if( tItem.getData("file").equals(mainfolder.getSelection().getData("file"))  &&  tItem.getData("sym").equals(mainfolder.getSelection().getData("sym"))  )
						tItem.dispose();		
		
		return true;
	}

	private void createBottom(Composite self) {
		self.setLayout(new FillLayout());
		CTabFolder folder = new CTabFolder(self, SWT.TOP | SWT.BORDER);
		folder.setLayout(new FillLayout());
		folder.setSimple(false);

		CTabItem errors = new CTabItem(folder, SWT.NONE);
		errors.setText("&Errors");
		errors.setImage(RepDevMain.smallErrorsImage);
		tblErrors = new Table(folder, SWT.MULTI | SWT.FULL_SELECTION);
		createTable(tblErrors);
		tblErrors.addSelectionListener(new SelectionAdapter(){
			public void widgetDefaultSelected(SelectionEvent e){
				TableItem item = (TableItem)e.item;
				int sym = (Integer)item.getData("sym");
				Error error = (Error)item.getData("error");
				SymitarFile file = new SymitarFile(sym,error.getFile(),FileType.REPGEN);
				
				Object o = openFile(file);
	
				EditorComposite editor = null;
				
				if( o instanceof EditorComposite)
					editor = (EditorComposite)o;
				
				if( error.getLine() >= 0 && editor != null ){	
				 	editor.getStyledText().setTopIndex(Math.max(0, error.getLine() - 10));
				 	
				 	try{
				 		editor.getStyledText().setCaretOffset(Math.min(editor.getStyledText().getOffsetAtLine(Math.max(0,error.getLine() - 1))+ Math.max(0,error.getCol() - 1 ),editor.getStyledText().getCharCount()-1 ));
				 	}
				 	catch(IllegalArgumentException ex){
				 		//Just ignore it
				 	}
				 	
				 	editor.getStyledText().setFocus();
				}				
			}
		});
		
		errors.setControl(tblErrors);

		CTabItem tasks = new CTabItem(folder, SWT.NONE);
		tasks.setText("&Tasks");
		tasks.setImage(RepDevMain.smallTasksImage);
		tblTasks = new Table(folder, SWT.MULTI | SWT.FULL_SELECTION);
		createTable(tblTasks);
		tasks.setControl(tblTasks);

		folder.setSelection(errors);
	}

	private void createTable(Table tbl) {
		tbl.setHeaderVisible(true);
		tbl.setLinesVisible(true);

		String[] names = { "Description", "RepGen", "Location" };
		int[] widths = {400,100,50};
		int i = 0;
		
		for (String name : names) {
			TableColumn col = new TableColumn(tbl, SWT.NONE);
			col.setText(name);
			col.setMoveable(false);
			col.pack();

			col.setWidth(widths[i]);
			
			if (col.getWidth() < MIN_COL_WIDTH)
				col.setWidth(MIN_COL_WIDTH);

			col.addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent e) {
					TableColumn col = (TableColumn) e.widget;

					if (col.getWidth() < MIN_COL_WIDTH)
						col.setWidth(MIN_COL_WIDTH);
				}
			});
			
			i++;
		}
	}

	private void createMenuDefault() {
		Menu bar = new Menu(shell, SWT.BAR);

		MenuItem fileItem = new MenuItem(bar, SWT.CASCADE);
		fileItem.setText("&File");
		MenuItem editItem = new MenuItem(bar, SWT.CASCADE);
		editItem.setText("&Edit");
		MenuItem toolsItem = new MenuItem(bar, SWT.CASCADE);
		toolsItem.setText("&Tools");
		MenuItem helpItem = new MenuItem(bar, SWT.CASCADE);
		helpItem.setText("&Help");

		final Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileItem.setMenu(fileMenu);
		Menu toolsMenu = new Menu(shell, SWT.DROP_DOWN);
		toolsItem.setMenu(toolsMenu);
		Menu helpMenu = new Menu(shell, SWT.DROP_DOWN);
		helpItem.setMenu(helpMenu);
		Menu editMenu = new Menu(shell, SWT.DROP_DOWN);
		editItem.setMenu(editMenu);
		
		final MenuItem fileSave = new MenuItem(fileMenu, SWT.PUSH);
		fileSave.setText("&Save\tCTRL+S");
		fileSave.setImage(RepDevMain.smallActionSaveImage);
		fileSave.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				if( mainfolder.getSelection() != null && mainfolder.getSelection().getControl() instanceof EditorComposite )
					((EditorComposite)mainfolder.getSelection().getControl()).saveFile(true);
			}
		});
		
		final MenuItem fileSaveAs = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveAs.setText("S&ave As");
		fileSaveAs.setImage(RepDevMain.smallActionSaveAsImage);
		fileSaveAs.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog dialog;
				
				if( mainfolder.getSelection() != null && mainfolder.getSelection().getControl() instanceof EditorComposite ){
					SymitarFile file = ((EditorComposite)mainfolder.getSelection().getControl()).getFile();
					
					if( file.isLocal())
						dialog = new FileDialog(shell,FileDialog.Mode.SAVE, file.getDir());
					else
						dialog = new FileDialog(shell,FileDialog.Mode.SAVE, file.getSym());
					
					ArrayList<SymitarFile> result = dialog.open();
					
					if( result.size() == 1){
						if( result.get(0).equals(file) )
							return;
						
						result.get(0).saveFile(file.getData());
						
						//Remove any already open tabs of the new file, so if we are overrwriting, it gets updated and this is clear to the user
						for( CTabItem item : mainfolder.getItems())
							if( item.getData("file").equals(result.get(0)))
								item.dispose();
						
						openFile(result.get(0));
					}				
				}
			}
		});
		
		new MenuItem(fileMenu,SWT.SEPARATOR);
	
		final MenuItem filePrint = new MenuItem(fileMenu, SWT.PUSH);
		filePrint.setText("&Print\tCTRL+P");
		filePrint.setImage(RepDevMain.smallPrintImage);
		filePrint.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				print();
			}
		});
		
		final MenuItem recentSeperator = new MenuItem(fileMenu,SWT.SEPARATOR);
			
		
		fileMenu.addMenuListener(new MenuListener(){

			public void menuHidden(MenuEvent e) {
			}

			public void menuShown(MenuEvent e) {
				filePrint.setEnabled(mainfolder.getSelection() != null);
				fileSave.setEnabled(mainfolder.getSelection() != null && mainfolder.getSelection().getControl() instanceof EditorComposite);
				fileSaveAs.setEnabled(mainfolder.getSelection() != null && mainfolder.getSelection().getControl() instanceof EditorComposite);
				
				int i;
				
				for( i = fileMenu.indexOf(recentSeperator) + 1; i < fileMenu.getItemCount(); ){
					if( !fileMenu.getItem(i).isDisposed() )
						fileMenu.getItem(i).dispose();
				}
				
				i = 1;
				
				for( final SymitarFile file : Config.getRecentFiles()){
					MenuItem item = new MenuItem(fileMenu,SWT.PUSH);
					
					item.setData(file);
					item.setText(i + " " + file.getName());
					item.setImage(getFileImage(file));
					
					item.addSelectionListener(new SelectionAdapter(){

						@Override
						public void widgetSelected(SelectionEvent e) {
							if( !RepDevMain.SYMITAR_SESSIONS.get(file.getSym()).isConnected())
								if (SymLoginShell.symLogin(display, shell, file.getSym()) == -1) 
									return;
								
							openFile(file);							
						}
						
					});
					
					i++;
				}
				
//				Indicator used for creating recents list
				MenuItem staticSeperator = new MenuItem(fileMenu,SWT.SEPARATOR);
				
				MenuItem fileExit = new MenuItem(fileMenu, SWT.PUSH);
				fileExit.setText("E&xit");
				fileExit.setImage(RepDevMain.smallExitImage);
				fileExit.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent arg0) {
						close();
					}
				});
			}
			
		});
		
		final MenuItem editUndo = new MenuItem(editMenu,SWT.PUSH);
		editUndo.setImage(RepDevMain.smallUndoImage);
		editUndo.setText("Undo Typing\tCTRL+Z");
		editUndo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				if( mainfolder.getSelectionIndex() == -1)
					return;
				
				if( mainfolder.getItem(mainfolder.getSelectionIndex()).getControl() instanceof TabTextEditorView )
					((TabTextEditorView)mainfolder.getItem(mainfolder.getSelectionIndex()).getControl()).undo();
			}
		});

		final MenuItem editRedo = new MenuItem(editMenu,SWT.PUSH);
		editRedo.setImage(RepDevMain.smallRedoImage);
		editRedo.setText("Redo Typing\tCTRL+Y");
		editRedo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				if( mainfolder.getSelectionIndex() == -1)
					return;
				
				if( mainfolder.getItem(mainfolder.getSelectionIndex()).getControl() instanceof TabTextEditorView )
					((TabTextEditorView)mainfolder.getItem(mainfolder.getSelectionIndex()).getControl()).redo();
			}
		});
		
		new MenuItem(editMenu,SWT.SEPARATOR);
		
		final MenuItem editCut = new MenuItem(editMenu,SWT.PUSH);
		editCut.setImage(RepDevMain.smallCutImage);
		editCut.setText("Cut\tCTRL+X");
		editCut.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				if( mainfolder.getSelectionIndex() == -1)
					return;
				
				if( mainfolder.getItem(mainfolder.getSelectionIndex()).getControl() instanceof TabTextView )
					((TabTextView)mainfolder.getItem(mainfolder.getSelectionIndex()).getControl()).getStyledText().cut();
			}
		});
		
		final MenuItem editCopy = new MenuItem(editMenu,SWT.PUSH);
		editCopy.setImage(RepDevMain.smallCopyImage);
		editCopy.setText("Copy\tCTRL+C");
		editCopy.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				if( mainfolder.getSelectionIndex() == -1)
					return;
				
				if( mainfolder.getItem(mainfolder.getSelectionIndex()).getControl() instanceof TabTextView )
					((TabTextView)mainfolder.getItem(mainfolder.getSelectionIndex()).getControl()).getStyledText().copy();
			}
		});
		
		final MenuItem editPaste = new MenuItem(editMenu,SWT.PUSH);
		editPaste.setImage(RepDevMain.smallPasteImage);
		editPaste.setText("Paste\tCTRL+V");
		editPaste.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				if( mainfolder.getSelectionIndex() == -1)
					return;
				
				if( mainfolder.getItem(mainfolder.getSelectionIndex()).getControl() instanceof TabTextView )
					((TabTextView)mainfolder.getItem(mainfolder.getSelectionIndex()).getControl()).getStyledText().paste();
			}
		});
		
		new MenuItem(editMenu,SWT.SEPARATOR);
		
		final MenuItem editSelectAll = new MenuItem(editMenu,SWT.PUSH);
		editSelectAll.setImage(RepDevMain.smallSelectAllImage);
		editSelectAll.setText("Select All\tCTRL+A");
		editSelectAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				if( mainfolder.getSelectionIndex() == -1)
					return;
				
				if( mainfolder.getItem(mainfolder.getSelectionIndex()).getControl() instanceof TabTextView )
					((TabTextView)mainfolder.getItem(mainfolder.getSelectionIndex()).getControl()).getStyledText().selectAll();
			}
		});
		
		new MenuItem(editMenu,SWT.SEPARATOR);
		
		final MenuItem editFind = new MenuItem(editMenu,SWT.PUSH);
		editFind.setImage(RepDevMain.smallFindReplaceImage);
		editFind.setText("Find/Replace\tCTRL+F");
		editFind.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showFindWindow();
			}
		});
		
		final MenuItem editFindNext = new MenuItem(editMenu,SWT.PUSH);
		editFindNext.setImage(RepDevMain.smallFindImage);
		editFindNext.setText("Find Next\tF3");
		editFindNext.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				findNext();
			}
		});
		
		final MenuItem editGotoLine = new MenuItem(editMenu,SWT.PUSH);
		editGotoLine.setText("Goto Line\tCTRL+L");
		editGotoLine.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				StyledText txt = ((TabTextView)mainfolder.getItem(mainfolder.getSelectionIndex()).getControl()).getStyledText();
				GotoLineShell.show(txt.getParent().getShell(),txt);
			}
		});
		
		editMenu.addMenuListener(new MenuListener(){

			public void menuHidden(MenuEvent e) {		
			}

			public void menuShown(MenuEvent e) {
				if(mainfolder.getSelectionIndex() == -1){
					editRedo.setEnabled(false);
					editUndo.setEnabled(false);
					
					editCut.setEnabled(false);
					editCopy.setEnabled(false);
					editPaste.setEnabled(false);
					
					editSelectAll.setEnabled(false);
					editFindNext.setEnabled(false);
					editFind.setEnabled(false);
					
					editGotoLine.setEnabled(false);
				}
				else{
					editCut.setEnabled(true);
					editCopy.setEnabled(true);
					editPaste.setEnabled(true);
					editSelectAll.setEnabled(true);
					editFindNext.setEnabled(true);
					editFind.setEnabled(true);
					
					if( mainfolder.getItem(mainfolder.getSelectionIndex()).getControl() instanceof TabTextEditorView && ((TabTextEditorView)mainfolder.getItem(mainfolder.getSelectionIndex()).getControl()).canRedo() )
						editRedo.setEnabled(true);
					else
						editRedo.setEnabled(false);
					
					if( mainfolder.getItem(mainfolder.getSelectionIndex()).getControl() instanceof TabTextEditorView && ((TabTextEditorView)mainfolder.getItem(mainfolder.getSelectionIndex()).getControl()).canUndo() )
						editUndo.setEnabled(true);
					else
						editUndo.setEnabled(false);
					
					if( mainfolder.getItem(mainfolder.getSelectionIndex()).getControl() instanceof TabTextEditorView)
						editGotoLine.setEnabled(true);
					else
						editGotoLine.setEnabled(false);
				}
			}
			
		});
		
		MenuItem toolsOptions = new MenuItem(toolsMenu, SWT.PUSH);
		toolsOptions.setText("&Options");
		toolsOptions.setImage(RepDevMain.smallOptionsImage);
		toolsOptions.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showOptions();
			}
		});

		MenuItem helpAbout = new MenuItem(helpMenu, SWT.PUSH);
		helpAbout.setText("&About");
		helpAbout.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showAboutBox();
			}
		});

		shell.setMenuBar(bar);
	}
	
	protected void print() {
		if( mainfolder.getSelection() != null && mainfolder.getSelection().getControl() instanceof TabTextView){
			PrintDialog dialog = new PrintDialog(shell);
			PrinterData data = dialog.open();
			
			if( data != null ){
				 StyledTextPrintOptions options = new StyledTextPrintOptions();
				 options.footer = "\t\t<page>"; 
				 options.jobName = "RepDev - " + mainfolder.getSelection().getText();
				 options.printLineBackground = false;
				 options.printTextFontStyle = true;
				 options.printTextForeground = true;
				 options.printTextBackground = true;
				 
							 		 
				 Runnable runnable = ((TabTextView)mainfolder.getSelection().getControl()).getStyledText().print(new Printer(data), options); 
				 runnable.run();
			}
		}
	}

	public void showFindWindow(){
		if( mainfolder.getSelection() == null )
			findReplaceShell.attach(null, false);
		else if( mainfolder.getSelection().getControl() instanceof EditorComposite )
			findReplaceShell.attach(((EditorComposite)mainfolder.getSelection().getControl()).getStyledText(), ((EditorComposite)mainfolder.getSelection().getControl()).getParser(), true);
		else if(  mainfolder.getSelection().getControl() instanceof ReportComposite )
			findReplaceShell.attach(((ReportComposite)mainfolder.getSelection().getControl()).getStyledText(), null, false);
		
		
		findReplaceShell.open();	
	}
	
	public void findNext(){
		findReplaceShell.find();
	}
	
	public void showOptions() {
		OptionsShell.showOptions(shell.getDisplay(), shell);
	}

	private void showAboutBox() {
		AboutBoxShell.show();
	}
	
	public Table getErrorTable(){
		return tblErrors;
	}
	
	public Table getTaskTable(){
		return tblTasks;
	}

	private void close() {
		shell.dispose();
	}
	
	public void saveAllRepgens() {
		if( mainfolder.getItems().length >= 1){
			for( CTabItem item : mainfolder.getItems()) {
				if( (item.getControl() instanceof EditorComposite) 
						&& item.getData("modified") != null && (Boolean) item.getData("modified") ) {
					System.out.println("Saving file: " + item.getData("file") );
					((EditorComposite)item.getControl()).saveFile(true);						
				} 
			}
		
		}
	}

	public void closeCurrentTab() {
		System.out.println(mainfolder.getSelection());
		mainfolder.getSelection().dispose();
	}
	
	/**
	 * Add a toolbar to the coolBar (sorry, but no pun intended.)
	 */
	public void addBar(ToolBar b) {
		CoolItem item = new CoolItem(coolBar, SWT.NONE);
		item.setControl(b);
		Point size = b.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		item.setMinimumSize(size);
		
		coolItems.add(item);		
	}
	
	public CoolBar getCoolBar() {
		return this.coolBar;
	}
	
	private void createEditorBar() {
		editorBar = new ToolBar(coolBar, SWT.FLAT);
		editorBar.setData("editorComposite");
		addBar(editorBar);
		
		savetb = new ToolItem(editorBar, SWT.NONE);
		savetb.setImage(RepDevMain.smallActionSaveImage);
		savetb.setToolTipText("Saves the current file.");
		savetb.setEnabled(false);

		install = new ToolItem(editorBar, SWT.NONE);
		install.setImage(RepDevMain.smallSymAddImage);
		install.setToolTipText("Installs current file for onDemand use.");
		install.setEnabled(false);
		
		run = new ToolItem(editorBar,SWT.NONE);
		run.setImage(RepDevMain.smallRunImage);
		run.setToolTipText("Opens the run report dialog.");
		run.setEnabled(false);
		
		print = new ToolItem(editorBar,SWT.NONE);
		print.setImage(RepDevMain.smallPrintImage);
		print.setToolTipText("Prints the current file to a local printer.");
		print.setEnabled(false);
				
		// EditorBar button actions
		savetb.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if( mainfolder.getSelection().getControl() instanceof EditorComposite )
					((EditorComposite)mainfolder.getSelection().getControl()).saveFile(true);
			}
		});

		install.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if( mainfolder.getSelection().getControl() instanceof EditorComposite )
					((EditorComposite)mainfolder.getSelection().getControl()).installRepgen(true);					
			}
		});
		
		print.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RepDevMain.mainShell.print();			
			}
		});
		
		run.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if( mainfolder.getSelection().getControl() instanceof EditorComposite )
					RepDevMain.mainShell.runReport(((EditorComposite)mainfolder.getSelection().getControl()).getFile());			
			}
		});
	}
	
	private void setEditorBarStatus() {
		if ( mainfolder.getSelection().getControl() instanceof EditorComposite) {
			SymitarFile file = ((EditorComposite)mainfolder.getSelection().getControl()).getFile();
			if (file.getType() != FileType.REPGEN || file.isLocal())
				install.setEnabled(false);
			else
				install.setEnabled(true);

			if (file.getType() != FileType.REPGEN || file.isLocal())
				run.setEnabled(false);
			else
				run.setEnabled(true);

			savetb.setEnabled(true);
		} else {
			savetb.setEnabled(false);
			run.setEnabled(false);
			install.setEnabled(false);
			print.setEnabled(true);
		}
	}
}
