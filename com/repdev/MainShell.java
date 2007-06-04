package com.repdev;

import java.awt.FlowLayout;
import java.util.ArrayList;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import com.repdev.parser.*;
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

		FormData frmLeft = new FormData();
		frmLeft.top = new FormAttachment(0);
		frmLeft.left = new FormAttachment(0);
		frmLeft.right = new FormAttachment(sashVert);
		frmLeft.bottom = new FormAttachment(100);
		left.setLayoutData(frmLeft);

		final FormData frmSashVert = new FormData();
		frmSashVert.top = new FormAttachment(0);
		frmSashVert.left = new FormAttachment(leftPercent);
		frmSashVert.bottom = new FormAttachment(100);
		sashVert.setLayoutData(frmSashVert);

		FormData frmRight = new FormData();
		frmRight.top = new FormAttachment(0);
		frmRight.left = new FormAttachment(sashVert);
		frmRight.right = new FormAttachment(100);
		frmRight.bottom = new FormAttachment(100);
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

	private Object openFile(SymitarFile file, int sym) {
		boolean found = false;
		
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
			item.setImage(getFileImage(file, sym));
			item.setData("file", file);
			item.setData("sym", sym);

			EditorComposite editor = new EditorComposite(mainfolder, file, sym);
			item.setControl(editor);

			mainfolder.setSelection(item);
			
			//Attach find/replace shell here as well (in addition to folder listener)
			findReplaceShell.attach(((EditorComposite)mainfolder.getSelection().getControl()).getStyledText());
			
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

			openFile(file, sym);
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
		} else {
			MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			dialog.setMessage("Could not connect to symitar server, please set a correct server in the options menu");
			dialog.setText("Error connecting");
			dialog.open();
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
		TreeItem[] selection = tree.getSelection();
		if (selection.length != 1)
			return;

		TreeItem cur = selection[0];
		while (cur.getParentItem() != null)
			cur = cur.getParentItem();

		int sym = (Integer) cur.getData();
		String str = NewProjShell.askForName(display, shell, sym);
		if (str != null) {
			Project proj = ProjectManager.createProject(str, sym);

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

		FileDialog dialog = new FileDialog(shell, FileDialog.Mode.OPEN, proj.getSym());
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
		}
	}

	private void newFileInProject() {
		FileDialog dialog = new FileDialog(shell, FileDialog.Mode.SAVE, getCurrentTreeSym());
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
			}

			RepDevMain.SYMITAR_SESSIONS.get(getCurrentTreeSym()).saveFile(file, "");
			openFile(file, getCurrentTreeSym());
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

		tree.notifyListeners(SWT.Selection, null);
	}

	private void createExplorer(Composite self) {
		self.setLayout(new FillLayout());
		Group group = new Group(self, SWT.NONE);
		group.setText("Project Explorer");
		group.setLayout(new FormLayout());

		ToolBar toolbar = new ToolBar(group, SWT.HORIZONTAL | SWT.WRAP);

		ToolItem addSym = new ToolItem(toolbar, SWT.PUSH);
		addSym.setImage(RepDevMain.smallSymAddImage);
		addSym.setToolTipText("Add a new Sym to this list.");

		final ToolItem remSym = new ToolItem(toolbar, SWT.PUSH);
		remSym.setImage(RepDevMain.smallSymRemoveImage);
		remSym.setToolTipText("Remove the selected Sym from this list.");
		remSym.setEnabled(false);

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

		tree = new Tree(group, SWT.NONE | SWT.BORDER );
		for (int sym : Config.getSyms()) {
			TreeItem item = new TreeItem(tree, SWT.NONE);
			item.setText("Sym " + sym);
			item.setImage(RepDevMain.smallSymImage);
			item.setData(sym);
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
				FileDialog dialog = new FileDialog(shell, FileDialog.Mode.SAVE, getCurrentTreeSym());
				ArrayList<SymitarFile> files = dialog.open();

				if (files.size() > 0) {
					SymitarFile file = files.get(0);

					RepDevMain.SYMITAR_SESSIONS.get(getCurrentTreeSym()).saveFile(file, "");
					openFile(file, getCurrentTreeSym());
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
		
		MenuItem runFMReport = new MenuItem(runMenu,SWT.NONE);
		runFMReport.setText("Run Report with FM");
		runFMReport.setImage(RepDevMain.smallRunFMImage);
		
		MenuItem openLastReport = new MenuItem(runMenu,SWT.NONE);
		openLastReport.setText("Open Last Report Run");
		openLastReport.setImage(RepDevMain.smallFileOpenImage);
		
		MenuItem findReport = new MenuItem(runMenu,SWT.NONE);
		findReport.setText("Find runs in Print History");
		findReport.setImage(RepDevMain.smallFindImage);
		

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

				FileDialog dialog = new FileDialog(shell, FileDialog.Mode.OPEN, getCurrentTreeSym());

				for (SymitarFile file : dialog.open())
					openFile(file, getCurrentTreeSym());
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
				if (tree.getSelection()[0].getData() instanceof Integer) {
					importFilem.setEnabled(false);
					newProjectFile.setEnabled(false);
				} else {
					importFilem.setEnabled(true);
					newProjectFile.setEnabled(true);
				}
				
				if( tree.getSelectionCount() == 1 && tree.getSelection()[0].getData() instanceof SymitarFile && ((SymitarFile)tree.getSelection()[0].getData()).getType() == FileType.REPGEN){
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

				if (root.getData() instanceof Integer) {
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

					for (Project proj : ProjectManager.getProjects((Integer) root.getData())) {
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

	protected void runReport() {
		Object data = tree.getSelection()[0].getData();
		
		if( !(data instanceof SymitarFile) )
			return;
		
		RunReportShell dialog = new RunReportShell(shell,(SymitarFile)data,getCurrentTreeSym());
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

	private Image getFileImage(SymitarFile file) {
		return getFileImage(file, -1);
	}

	private Image getFileImage(SymitarFile file, int sym) {
		Image img;

		switch (file.getType()) {
		case REPGEN:
			img = RepDevMain.smallRepGenImage;
			break;
		default:
			img = RepDevMain.smallFileImage;
		}

		return img;

	}

	private void createEditorPane(Composite self) {
		self.setLayout(new FillLayout());
		mainfolder = new CTabFolder(self, SWT.TOP | SWT.BORDER);
		mainfolder.setLayout(new FillLayout());
		mainfolder.setSimple(false);
		
		//Make the find/replace box know which thing we are looking through, if the window is open as we switch tabs
		mainfolder.addSelectionListener( new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				if( mainfolder.getSelection() != null && mainfolder.getSelection().getControl() instanceof EditorComposite )
					findReplaceShell.attach(((EditorComposite)mainfolder.getSelection().getControl()).getStyledText());
			}
		});
		
		mainfolder.addCTabFolder2Listener(new CTabFolder2Listener(){

			public void close(CTabFolderEvent event) {
				event.doit = confirmClose( mainfolder.getSelection() );	
				
				if( event.doit && mainfolder.getSelection().getControl() instanceof EditorComposite )
					for( TableItem item : tblErrors.getItems() )
						if( item.getData("file").equals(mainfolder.getSelection().getData("file"))  &&  item.getData("sym").equals(mainfolder.getSelection().getData("sym"))  )
								item.dispose();					
			
			}

			public void maximize(CTabFolderEvent event) {
				// TODO Auto-generated method stub
				
			}

			public void minimize(CTabFolderEvent event) {
			}

			public void restore(CTabFolderEvent event) {
			}

			public void showList(CTabFolderEvent event) {
				// TODO Auto-generated method stub
				
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
				((EditorComposite) item.getControl()).saveFile();
			}
		}

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
				SymitarFile file = (SymitarFile)item.getData("file");
				int sym = (Integer)item.getData("sym");
				Error error = (Error)item.getData("error");
				
				Object o = openFile(file, sym);
				
				EditorComposite editor = null;
				
				if( o instanceof EditorComposite)
					editor = (EditorComposite)o;
				
				if( error.getLine() >= 0 && editor != null ){
				 	editor.getStyledText().setTopIndex(Math.max(0, error.getLine() - 10));
				 	editor.getStyledText().setCaretOffset(editor.getStyledText().getOffsetAtLine(error.getLine()) + Math.max(0,error.getCol()));
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

		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileItem.setMenu(fileMenu);
		Menu toolsMenu = new Menu(shell, SWT.DROP_DOWN);
		toolsItem.setMenu(toolsMenu);
		Menu helpMenu = new Menu(shell, SWT.DROP_DOWN);
		helpItem.setMenu(helpMenu);
		Menu editMenu = new Menu(shell, SWT.DROP_DOWN);
		editItem.setMenu(editMenu);

		MenuItem fileExit = new MenuItem(fileMenu, SWT.PUSH);
		fileExit.setText("E&xit");
		fileExit.setImage(RepDevMain.smallExitImage);
		fileExit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				close();
			}
		});
		
		final MenuItem editUndo = new MenuItem(editMenu,SWT.PUSH);
		editUndo.setImage(RepDevMain.smallUndoImage);
		editUndo.setText("Undo Typing\tCTRL+Z");
		editUndo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				if( mainfolder.getSelectionIndex() == -1)
					return;
				
				((EditorComposite)mainfolder.getItem(mainfolder.getSelectionIndex()).getControl()).undo();
			}
		});

		final MenuItem editRedo = new MenuItem(editMenu,SWT.PUSH);
		editRedo.setImage(RepDevMain.smallRedoImage);
		editRedo.setText("Redo Typing\tCTRL+Y");
		editRedo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				if( mainfolder.getSelectionIndex() == -1)
					return;
				
				((EditorComposite)mainfolder.getItem(mainfolder.getSelectionIndex()).getControl()).redo();
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
				
				((EditorComposite)mainfolder.getItem(mainfolder.getSelectionIndex()).getControl()).getStyledText().cut();
			}
		});
		
		final MenuItem editCopy = new MenuItem(editMenu,SWT.PUSH);
		editCopy.setImage(RepDevMain.smallCopyImage);
		editCopy.setText("Copy\tCTRL+C");
		editCopy.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				if( mainfolder.getSelectionIndex() == -1)
					return;
				
				((EditorComposite)mainfolder.getItem(mainfolder.getSelectionIndex()).getControl()).getStyledText().copy();
			}
		});
		
		final MenuItem editPaste = new MenuItem(editMenu,SWT.PUSH);
		editPaste.setImage(RepDevMain.smallPasteImage);
		editPaste.setText("Paste\tCTRL+V");
		editPaste.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				if( mainfolder.getSelectionIndex() == -1)
					return;
				
				((EditorComposite)mainfolder.getItem(mainfolder.getSelectionIndex()).getControl()).getStyledText().paste();
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
				
				((EditorComposite)mainfolder.getItem(mainfolder.getSelectionIndex()).getControl()).getStyledText().selectAll();
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
				}
				else{
					editCut.setEnabled(true);
					editCopy.setEnabled(true);
					editPaste.setEnabled(true);
					editSelectAll.setEnabled(true);
					editFindNext.setEnabled(true);
					
					if( ((EditorComposite)mainfolder.getItem(mainfolder.getSelectionIndex()).getControl()).canRedo() )
						editRedo.setEnabled(true);
					else
						editRedo.setEnabled(false);
					
					if( ((EditorComposite)mainfolder.getItem(mainfolder.getSelectionIndex()).getControl()).canUndo() )
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
		helpAbout.setText("&About " + RepDevMain.NAMESTR);
		helpAbout.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				showAboutBox();
			}
		});

		shell.setMenuBar(bar);
	}
	
	public void showFindWindow(){
		if( mainfolder.getSelection() == null || !(mainfolder.getSelection().getControl() instanceof EditorComposite))
			findReplaceShell.attach(null);
		else
			findReplaceShell.attach(((EditorComposite)mainfolder.getSelection().getControl()).getStyledText(), ((EditorComposite)mainfolder.getSelection().getControl()).getParser());
		
		
		findReplaceShell.open();	
	}
	
	public void findNext(){
		findReplaceShell.find();
	}
	
	private void showOptions() {
		OptionsShell.showOptions(shell.getDisplay(), shell);
	}

	private void showAboutBox() {
		MessageBox dialog = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
		dialog.setMessage(RepDevMain.NAMESTR);
		dialog.setText("About");

		dialog.open();
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
}
