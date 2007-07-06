package com.repdev;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledTextPrintOptions;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
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
 */
public class MainShell {
	private static final int MIN_COL_WIDTH = 75, MIN_COMP_SIZE = 125;
	private CTabFolder mainfolder;
	private Display display;
	private Shell shell;
	private Tree tree;
	private Table tblErrors, tblTasks; 
	private FindReplaceShell findReplaceShell;
	private final int MAX_RECENTS = 5;
	
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
		int leftPercent = 20, bottomPercent = 15;

		shell = new Shell(display);
		shell.setText(RepDevMain.NAMESTR);
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
					
		final Composite left = new Composite(shell, SWT.NONE);
		createExplorer(left);
		final Sash sashVert = new Sash(shell, SWT.VERTICAL | SWT.SMOOTH);
		final Composite right = new Composite(shell, SWT.NONE);
		right.setLayout(new FormLayout());

		Composite main = new Composite(right, SWT.NONE);
		createEditorPane(main);
		final Sash sashHoriz = new Sash(right, SWT.HORIZONTAL | SWT.SMOOTH);
		final Composite bottom = new Composite(right, SWT.NONE);
		createBottom(bottom);

		createStatusBar();
		
		FormData frmLeft = new FormData();
		frmLeft.top = new FormAttachment(0);
		frmLeft.left = new FormAttachment(0);
		frmLeft.right = new FormAttachment(sashVert);
		frmLeft.bottom = new FormAttachment(statusBar);
		left.setLayoutData(frmLeft);

		final FormData frmSashVert = new FormData();
		frmSashVert.top = new FormAttachment(0);
		frmSashVert.left = new FormAttachment(leftPercent);
		frmSashVert.bottom = new FormAttachment(statusBar);
		sashVert.setLayoutData(frmSashVert);

		FormData frmRight = new FormData();
		frmRight.top = new FormAttachment(0);
		frmRight.left = new FormAttachment(sashVert);
		frmRight.right = new FormAttachment(100);
		frmRight.bottom = new FormAttachment(statusBar);
		right.setLayoutData(frmRight);

		FormData frmMain = new FormData();
		frmMain.top = new FormAttachment(0);
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
	
	public Object openFile(int seq, int sym){
		boolean found = false;
		Composite editor;
		
		for (CTabItem c : mainfolder.getItems()) {
			if (c.getData("seq") != null && (Integer)c.getData("seq") == seq && c.getData("sym") != null && ((Integer) c.getData("sym")) == sym) {
				mainfolder.setSelection(c);
				found = true;
				return c.getControl();
			}
		}

		if (!found) {
			CTabItem item = new CTabItem(mainfolder, SWT.CLOSE);

			item.setText("Sequence View: " + seq);
			
			item.setData("seq", seq);
			item.setData("sym", sym);

			item.setImage(drawSymOverImage(RepDevMain.smallReportsImage,sym));
			
			editor = new ReportComposite(mainfolder, seq, sym);
		
			item.setControl(editor);

			mainfolder.setSelection(item);
			
			//Attach find/replace shell here as well (in addition to folder listener)
			findReplaceShell.attach(((ReportComposite)editor).getStyledText(),false);
			
			return editor;
		}
		
		return null;
	}

	public Object openFile(SymitarFile file) {
		boolean found = false;
		Composite editor;
		int sym = file.getSym();
		
		for (CTabItem c : mainfolder.getItems()) {
			if (c.getData("file") != null && c.getData("file").equals(file) && c.getData("sym") != null && ((Integer) c.getData("sym")) == sym) {
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
			item.setData("sym", sym);

			if( file.getType() == FileType.REPORT)
				editor = new ReportComposite(mainfolder, file);
			else
				editor = new EditorComposite(mainfolder, file);
			
			item.setControl(editor);

			mainfolder.setSelection(item);
			
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

	private void removeSym() {
		TreeItem[] selection = tree.getSelection();
		TreeItem currentItem;
		int sym;

		if (selection.length != 1)
			return;
		currentItem = selection[0];

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
			
			if( cur.getData() instanceof Integer)
				proj = ProjectManager.createProject(str, sym);
			else if( cur.getData() instanceof String)
				proj = ProjectManager.createProject(str, dir);
				

			if (proj != null) {
				TreeItem item = new TreeItem(cur, SWT.NONE);
				item.setText(proj.getName());
				item.setData(proj);
				item.setImage(RepDevMain.smallProjectImage);
				new TreeItem(item, SWT.NONE).setText("Loading...");
			}
		}
	}

	private void removeProject() {
		TreeItem[] selection = tree.getSelection();
		if (selection.length != 1)
			return;

		TreeItem cur = selection[0];
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

			if (!proj.hasFile(file)) {
				proj.addFile(file);
				TreeItem item = new TreeItem(cur, SWT.NONE);
				item.setText(file.getName());
				item.setData(file);
				item.setImage(getFileImage(file));
				
				ProjectManager.saveProjects(proj.getSym());
			}

			file.saveFile("");
			openFile(file);
			tree.notifyListeners(SWT.Selection, null);
		}
	}

	private void removeFile() {
		TreeItem[] selection = tree.getSelection();
		if (selection.length != 1)
			return;

		if (!(selection[0].getData() instanceof SymitarFile))
			return;

		SymitarFile file = (SymitarFile) selection[0].getData();
		Project proj = (Project) selection[0].getParentItem().getData();

		RemFileShell.Result result = RemFileShell.confirm(display, shell, proj, file);

		if (result == RemFileShell.Result.OK_KEEP) {
			proj.removeFile(file, false);
			selection[0].dispose();
		} else if (result == RemFileShell.Result.OK_DELETE) {
			proj.removeFile(file, true);
			selection[0].dispose();
		}
		
		ProjectManager.saveProjects(proj.getSym());

		tree.notifyListeners(SWT.Selection, null);
	}

	private void createExplorer(Composite self) {
		self.setLayout(new FillLayout());
		Group group = new Group(self, SWT.NONE);
		group.setText("Project Explorer");
		group.setLayout(new FormLayout());

		ToolBar toolbar = new ToolBar(group, SWT.HORIZONTAL | SWT.WRAP);

		final ToolItem addSym = new ToolItem(toolbar, SWT.PUSH);
		addSym.setImage(RepDevMain.smallSymAddImage);
		addSym.setToolTipText("Add a new Sym to this list.");

		final ToolItem remSym = new ToolItem(toolbar, SWT.PUSH);
		remSym.setImage(RepDevMain.smallSymRemoveImage);
		remSym.setToolTipText("Remove the selected Sym from this list.");
		remSym.setEnabled(false);
		
		final ToolItem addFolder = new ToolItem(toolbar, SWT.PUSH);
		addFolder.setImage(RepDevMain.smallFolderAddImage);
		addFolder.setToolTipText("Mounts a local folder to store files and projects.");
		
		final ToolItem addProj = new ToolItem(toolbar, SWT.PUSH);
		addProj.setImage(RepDevMain.smallProjectAddImage);
		addProj.setToolTipText("Create a new project in the selected Sym.");
		addProj.setEnabled(false);

		final ToolItem remProj = new ToolItem(toolbar, SWT.PUSH);
		remProj.setImage(RepDevMain.smallProjectRemoveImage);
		remProj.setToolTipText("Delete the selected project.");
		remProj.setEnabled(false);

		final ToolItem newFile = new ToolItem(toolbar, SWT.PUSH);
		newFile.setImage(RepDevMain.smallFileAddImage);
		newFile.setToolTipText("Create a new file in your current project.");
		newFile.setEnabled(false);

		final ToolItem importFile = new ToolItem(toolbar, SWT.PUSH);
		importFile.setImage(RepDevMain.smallImportImage);
		importFile.setToolTipText("Import Existing Files to your current project.");
		importFile.setEnabled(false);

		final ToolItem remFile = new ToolItem(toolbar, SWT.PUSH);
		remFile.setImage(RepDevMain.smallFileRemoveImage);
		remFile.setToolTipText("Disassociate the selected file from its project, and optionally delete the file from the server.");
		remFile.setEnabled(false);

		toolbar.pack();

		tree = new Tree(group, SWT.NONE | SWT.BORDER | SWT.MULTI );
		
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

		Menu newMenu = new Menu(treeMenu);
		MenuItem newItem = new MenuItem(treeMenu, SWT.CASCADE);
		newItem.setMenu(newMenu);
		newItem.setText("New...");

		MenuItem newFreeFile = new MenuItem(newMenu, SWT.NONE);
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

		MenuItem newProject = new MenuItem(newMenu, SWT.NONE);
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
				
				ArrayList<Integer> seqs = RepDevMain.SYMITAR_SESSIONS.get(file.getSym()).getReportSeqs(file.getName(), -1, 40,1);
				
				shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
				
				//FIXME: This whole thing only works if the last run is the current date
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
				
				ArrayList<Integer> seqs = RepDevMain.SYMITAR_SESSIONS.get(file.getSym()).getReportSeqs(file.getName(), -1, 40,10);
				
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
					for(int seq : seqs){
						combo.add("Sequence: " + seq);
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
							openFile((Integer)combo.getData(String.valueOf(combo.getSelectionIndex())), file.getSym());
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

		MenuItem deleteFile = new MenuItem(treeMenu, SWT.NONE);
		deleteFile.setText("Remove");
		deleteFile.setImage(RepDevMain.smallDeleteImage);
		deleteFile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] selection = tree.getSelection();
				if (selection.length != 1)
					return;

				TreeItem cur = selection[0];
				Object data = cur.getData();

				if (data instanceof Integer)
					removeSym();
				if( data instanceof String)
					removeDir();
				else if (data instanceof Project)
					removeProject();
				else
					removeFile();

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
				if ((tree.getSelection()[0].getData() instanceof Integer || tree.getSelection()[0].getData() instanceof String) && tree.getSelectionCount() == 1) {
					importFilem.setEnabled(false);
					newProjectFile.setEnabled(false);
				} else {
					importFilem.setEnabled(true);
					newProjectFile.setEnabled(true);
				}
				
				if( tree.getSelection()[0].getData() instanceof Project && tree.getSelectionCount() == 1 ) {
					openAllItem.setEnabled(true);
				} else
					openAllItem.setEnabled(false);
					
				if( tree.getSelectionCount() == 1 && tree.getSelection()[0].getData() instanceof SymitarFile && ((SymitarFile)tree.getSelection()[0].getData()).getType() == FileType.REPGEN && !((SymitarFile)tree.getSelection()[0].getData()).isLocal()){
					runMenuItem.setEnabled(true);
				}
				else
					runMenuItem.setEnabled(false);
			}

		});

		tree.setMenu(treeMenu);

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

				if (selection.length != 1) {
					remSym.setEnabled(false);
					addProj.setEnabled(false);
					remProj.setEnabled(false);
					importFile.setEnabled(false);
					newFile.setEnabled(false);
					remFile.setEnabled(false);
					return;
				}

				Object data = ((TreeItem) selection[0]).getData();
				if (data instanceof Integer) {
					remSym.setEnabled(true);
					addProj.setEnabled(true);
					remProj.setEnabled(false);
					importFile.setEnabled(false);
					newFile.setEnabled(false);
					remFile.setEnabled(false);
				} else if( data instanceof String){
					remSym.setEnabled(true);
					addProj.setEnabled(true);
					remProj.setEnabled(false);
					importFile.setEnabled(false);
					newFile.setEnabled(false);
					remFile.setEnabled(false);
				} else if (data instanceof Project) {
					remSym.setEnabled(true);
					addProj.setEnabled(true);
					remProj.setEnabled(true);
					importFile.setEnabled(true);
					newFile.setEnabled(true);
					remFile.setEnabled(false);
				} else if (data instanceof SymitarFile) {
					remSym.setEnabled(true);
					addProj.setEnabled(true);
					remProj.setEnabled(true);
					importFile.setEnabled(true);
					newFile.setEnabled(true);
					remFile.setEnabled(true);
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
		
		remSym.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeSym();
			}
		});
		addProj.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addProject();
			}
		});
		remProj.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeProject();
			}
		});
		importFile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				importFiles();
			}
		});
		remFile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeFile();
			}
		});
		newFile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newFileInProject();
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

	private void removeDir() {
		TreeItem[] selection = tree.getSelection();
		TreeItem currentItem;
		String dir;

		if (selection.length != 1)
			return;
		
		currentItem = selection[0];

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
		RowLayout slayout = new RowLayout();
		slayout.spacing = 5;
		
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
	    
	    new Label(statusBar,SWT.SEPARATOR);
	    
	    lineColumn = new Label(statusBar, SWT.NONE);
	    lineColumn.setSize(150,16);
	    lineColumn.setText("Location: 0 : 0                        ");
	    	    	    	    
	    //statusBar.pack();
	}
	
	public void setLineColumn(int line, int col) {
		lineColumn.setText("Location: " + line + " : " + col);
	}
	
	protected void runReport() {
		Object data = tree.getSelection()[0].getData();
		
		if( !(data instanceof SymitarFile) )
			return;
		
		RunReportShell dialog = new RunReportShell(shell,(SymitarFile)data);
		dialog.open();
	}

	/**
	 * 
	 * @return Sym of currently selected tree item
	 */
	private int getCurrentTreeSym() {
		int sym = -1;
		Object data = tree.getSelection()[0].getData();

		if (data instanceof Integer)
			sym = (Integer) data;
		else if (data instanceof Project)
			sym = ((Project) data).getSym();
		else
			sym = ((Project) tree.getSelection()[0].getParentItem().getData()).getSym();

		return sym;
	}
	
	private String getCurrentTreeDir(){
		String dir = "";
		
		Object data = tree.getSelection()[0].getData();

		if( data instanceof Integer)
			return null;
		if (data instanceof String)
			dir = (String) data;
		else if (data instanceof Project)
			dir = ((Project) data).getDir();
		else
			dir = ((Project) tree.getSelection()[0].getParentItem().getData()).getDir();

		return dir;
	}
	
	private boolean isCurrentItemLocal(){
		
		Object data = tree.getSelection()[0].getData();

		if( data instanceof String)
			return true;
		else if (data instanceof Integer)
			return false;
		else if (data instanceof Project)
			return ((Project) data).getDir() != null;
		else if( ((Project) tree.getSelection()[0].getParentItem().getData()).getDir() != null)
			return false;

		return true;
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

	private Image getFileImage(SymitarFile file) {
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
		mainfolder = new CTabFolder(self, SWT.TOP | SWT.BORDER);
		mainfolder.setLayout(new FillLayout());
		mainfolder.setSimple(false);
		
		Menu tabContextMenu = new Menu(mainfolder);
		mainfolder.setMenu(tabContextMenu);
				
		final MenuItem closeTab = new MenuItem(tabContextMenu,SWT.NONE);
		closeTab.setText("Close Tab");
		closeTab.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {		
				if( mainfolder.getSelectionIndex() != -1){
					if( confirmClose(mainfolder.getSelection()) ){
						mainfolder.getSelection().dispose();
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
							if( confirmClose(item) )
								item.dispose();
				
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
						if( confirmClose(item) )
							item.dispose();
				
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
				installRepgen.setEnabled( (flag && (mainfolder.getSelection().getControl() instanceof EditorComposite)) );
				
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
				event.doit = confirmClose( mainfolder.getSelection() );						
			}		
		});
	}

	private boolean confirmClose(CTabItem item) {
		if (item != null && item.getData("modified") != null && ((Boolean) item.getData("modified"))) {
			MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
			dialog.setText("Confirm File Close");
			dialog.setMessage("The file '" + item.getData("file") + "' on Sym " + item.getData("sym") + " has been modified, do you want to save it before closing it?");

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
				 	editor.getStyledText().setCaretOffset(Math.min(editor.getStyledText().getOffsetAtLine(Math.max(0,error.getLine() - 1))+ Math.max(0,error.getCol() - 1 ),editor.getStyledText().getCharCount()-1 ));
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
				// TODO Auto-generated method stub
				
			}

			public void menuShown(MenuEvent e) {
				filePrint.setEnabled(mainfolder.getSelection() != null);
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
		
		
		editMenu.addMenuListener(new MenuListener(){

			public void menuHidden(MenuEvent e) {
				// TODO Auto-generated method stub
				
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
	
}
