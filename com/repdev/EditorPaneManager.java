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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Sash;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * Owns the editor area: a SashForm holding one or two CTabFolders, and the knowledge of
 * which folder is currently active. All per-folder wiring lives in createFolder() so both
 * panes are built from one source and cannot drift apart.
 *
 * Secondary is null until the user first moves a tab across, and is disposed again when its
 * last tab closes.
 */
public class EditorPaneManager {
	private final SashForm sash;
	private final MainShell shell;
	private CTabFolder primary;
	private CTabFolder secondary;
	private CTabFolder active;

	public EditorPaneManager(Composite parent, MainShell shell) {
		this.shell = shell;
		this.sash = new SashForm(parent, Config.getSplitOrientation());
		ensureTabColors();
		this.primary = createFolder();
		this.active = primary;

		// SWT events do not propagate child -> parent, so a listener on the CTabFolder never
		// sees focus landing in the StyledText it hosts. A display filter plus a walk up the
		// parent chain is the only reliable way i found to know which pane the user is 
		// actually working in.
		final Listener focusFilter = new Listener() {
			public void handleEvent(Event e) {
				if (!(e.widget instanceof Control))
					return;
				CTabFolder owner = folderOf((Control) e.widget);
				if (owner != null)
					active = owner;
			}
		};
		sash.getDisplay().addFilter(SWT.FocusIn, focusFilter);
		sash.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (!sash.getDisplay().isDisposed())
					sash.getDisplay().removeFilter(SWT.FocusIn, focusFilter);
			}
		});
	}

	/** Saves the primary pane's current share while preserving a usable secondary pane. */
	private void captureSplitWeight() {
		int[] w = sash.getWeights();
		if (w.length == 2 && w[0] + w[1] > 0) {
			int weight = (int) (1000L * w[0] / (w[0] + w[1]));
			Config.setSplitWeight(Math.max(50, Math.min(950, weight)));
		}
	}

	/** The folder that last had focus. Never null. */
	public CTabFolder active() {
		if (active == null || active.isDisposed())
			active = primary;
		return active;
	}

	public boolean isSplit() {
		return secondary != null && !secondary.isDisposed();
	}

	/** @param swtDir SWT.HORIZONTAL for side-by-side panes, SWT.VERTICAL for stacked. */
	public void setOrientation(int swtDir) {
		sash.setOrientation(swtDir);
		sash.layout();
	}

	public int getOrientation() {
		return sash.getOrientation();
	}

	/** Moves keyboard focus to the opposite pane's selected editor, if there is one. */
	public void focusOtherView() {
		if (!isSplit())
			return;
		CTabFolder target = active() == secondary ? primary : secondary;
		CTabItem sel = target.getSelection();
		if (sel == null)
			return;
		boolean focused;
		if (sel.getControl() instanceof TabTextView)
			focused = ((TabTextView) sel.getControl()).getStyledText().setFocus();
		else
			focused = target.setFocus();
		if (focused)
			active = target;
	}

	/**
	 * Snapshot of every tab in both panes. A copy, so callers caan dispose items while
	 * iterating, which close-all and close-others both do.
	 */
	public List<CTabItem> allItems() {
		List<CTabItem> out = new ArrayList<CTabItem>();
		if (primary != null && !primary.isDisposed())
			for (CTabItem i : primary.getItems())
				out.add(i);
		if (secondary != null && !secondary.isDisposed())
			for (CTabItem i : secondary.getItems())
				out.add(i);
		return out;
	}

	/** Finds an already-open file in either pane, or null. */
	public CTabItem findItem(SymitarFile file, Object loc) {
		for (CTabItem c : allItems())
			if (c.getData("file") != null && c.getData("file").equals(file)
					&& c.getData("loc") != null && c.getData("loc").equals(loc))
				return c;
		return null;
	}

	/** Finds an already-open sequence view in either pane, or null. */
	public CTabItem findItem(Sequence seq, int sym) {
		for (CTabItem c : allItems())
			if (c.getData("seq") != null && c.getData("seq") == seq
					&& c.getData("sym") != null && ((Integer) c.getData("sym")) == sym)
				return c;
		return null;
	}

	/** Creates a tab in the active pane. */
	public CTabItem newItem(int style) {
		return createItem(active(), style);
	}

	/** Creates a tab whose disposal schedules a deferred empty-pane collapse. */
	private CTabItem createItem(final CTabFolder folder, int style) {
		return watchForDispose(new CTabItem(folder, style), folder);
	}

	/** Creates an indexed tab whose disposal schedules a deferred empty-pane collapse. */
	private CTabItem createItem(final CTabFolder folder, int style, int index) {
		return watchForDispose(new CTabItem(folder, style, index), folder);
	}

	private CTabItem watchForDispose(CTabItem item, final CTabFolder folder) {
		item.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				collapseIfEmpty(folder);
			}
		});
		return item;
	}

	/** Creates the second pane on demand and restores the saved divider position. */
	private CTabFolder ensureSecondary() {
		if (secondary == null || secondary.isDisposed()) {
			secondary = createFolder();
			int w = Config.getSplitWeight();
			sash.setWeights(new int[] { w, 1000 - w });
			sash.layout();
			for (Control child : sash.getChildren())
				if (child instanceof Sash && child.getData("splitWeightListener") == null) {
					child.setData("splitWeightListener", Boolean.TRUE);
					child.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							if (e.detail != SWT.DRAG)
								captureSplitWeight();
						}
					});
				}
		}
		return secondary;
	}

	/**
	 * Returns to one pane after a folder empties. If the primary empties while the secondary
	 * still has tabs, the secondary is promoted to primary. Deferred with asyncExec so this
	 * never runs while allItems() is being iterated or while SWT is dispatching a close event.
	 */
	private void collapseIfEmpty(final CTabFolder f) {
		if (f == null || f.isDisposed() || sash.isDisposed() || sash.getDisplay().isDisposed())
			return;
		sash.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (sash.isDisposed() || f.isDisposed() || f.getItemCount() != 0)
					return;
				if (f == secondary) {
					f.dispose();
					secondary = null;
					active = primary;
					sash.layout();
				} else if (f == primary && secondary != null && !secondary.isDisposed()
						&& secondary.getItemCount() > 0) {
					CTabFolder surviving = secondary;
					primary = surviving;
					secondary = null;
					active = primary;
					f.dispose();
					sash.layout();
				}
			}
		});
	}

	public boolean canMove(CTabItem src) {
		if (src == null || src.isDisposed() || src.getControl() == null)
			return false;
		return !(src.getParent() == primary && primary.getItemCount() == 1 && secondary == null);
	}

	/**
	 * Moves a tab to the opposite pane, preserving the live editor control, undo history, 
	 * fold state, parser state, caret and scroll position.
	 */
	public void moveToOtherView(CTabItem src) {
		if (!canMove(src))
			return;

		CTabFolder source = src.getParent();
		CTabFolder target = source == secondary ? primary : secondary;
		boolean createdTarget = false;
		if (target == null || target.isDisposed()) {
			target = ensureSecondary();
			createdTarget = true;
		}
		Control c = src.getControl();

		if (!c.setParent(target)) {
			if (createdTarget)
				collapseIfEmpty(target);
			MessageBox mb = new MessageBox(sash.getShell(), SWT.ICON_ERROR | SWT.OK);
			mb.setText("Move Failed");
			mb.setMessage("This tab could not be moved to the other view.");
			mb.open();
			return;
		}


		CTabItem dst = createItem(target, SWT.CLOSE);
		MainShell.copyTabState(src, dst);

		// Order here is load-bearing, and every step is doing something non-obvious.
		//
		// 1. src.setControl(null) must happen BEFORE the control is attached to dst.
		//    Its purpose is to stop src.dispose() from destroying the live editor, but
		//    CTabItem.setControl(null) also calls setVisible(false) on whatever control the
		//    item still references, and src still references this one. Done after the
		//    attach, it hides the editor just showed, and nothing puts it back:
		//    setSelection early-returns via showItem() when the item is already selected,
		//    so with a single tab in the pane there is no selection CHANGE to re-show it.
		//    That was causing blank tabs when moving the initial doc to the pane.
		// 2. target.setSelection(dst) must happen BEFORE dst.setControl(c), because
		//    setControl only sizes and shows the control when its item is the folder's
		//    current selection; otherwise it hides it. A pane created by this move has no
		//    selection yet, as CTabFolder does not auto-select its first item.
		//
		// So... detach from src, destroy src, select dst, and attach last... leaving the
		// showing operation as the final thing done to the control.
		src.setControl(null);
		src.dispose();

		target.setSelection(dst);
		dst.setControl(c);

		if (c instanceof EditorComposite)
			((EditorComposite) c).setTabItem(dst);
		else if (c instanceof ReportComposite)
			((ReportComposite) c).setTabItem(dst);

		if (source.getSelection() != null)
			source.notifyListeners(SWT.Selection, new Event());
		active = target;
		shell.setMainFolderSelection(dst);
		target.notifyListeners(SWT.Selection, new Event());
		if (c instanceof TabTextView)
			((TabTextView) c).getStyledText().setFocus();

		collapseIfEmpty(source);
		shell.setMainTitle();
		sash.layout();
	}

	/** Walks up the parent chain to the editor pane containing this control, or null. */
	private CTabFolder folderOf(Control c) {
		while (c != null && !c.isDisposed()) {
			if (c == primary)
				return primary;
			if (secondary != null && c == secondary)
				return secondary;
			c = c.getParent();
		}
		return null;
	}

	/** Which pane, if any, sits under this display-relative point. */
	private CTabFolder folderAt(Point displayPoint) {
		if (secondary != null && !secondary.isDisposed()
				&& secondary.getBounds().contains(secondary.getParent().toControl(displayPoint)))
			return secondary;
		if (!primary.isDisposed()
				&& primary.getBounds().contains(primary.getParent().toControl(displayPoint)))
			return primary;
		return null;
	}

	private void ensureTabColors() {
		// XP Theme Color Tabs With Gradient start
		  try {
				  File file = new File("styles\\" + Config.getStyle() + ".xml");
				  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				  DocumentBuilder db = dbf.newDocumentBuilder();
				  Document doc = db.parse(file);
				  doc.getDocumentElement().normalize();
				  NodeList nodeLst = doc.getElementsByTagName("tabStyle");
				  if(nodeLst.getLength() > 0){
					  NamedNodeMap attributes = nodeLst.item(0).getAttributes();
					  shell.titleForeColor = MainShell.HextoColor(attributes.getNamedItem("fgColor").getTextContent());
					  shell.titleBackColor1 = MainShell.HextoColor(attributes.getNamedItem("bgcolor1").getTextContent());
					  shell.titleBackColor2 = MainShell.HextoColor(attributes.getNamedItem("bgcolor2").getTextContent());
				  }
			  } catch (Exception e) {
				  e.printStackTrace();
			  }
			  if(shell.titleForeColor == null || shell.titleBackColor1 == null || shell.titleBackColor2 == null){
				shell.titleForeColor = shell.display.getSystemColor(SWT.COLOR_TITLE_FOREGROUND);
				shell.titleBackColor1 = shell.display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
				shell.titleBackColor2 = shell.display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
			  }
		// XP Theme Color Tabs With Gradient End
	}

	private CTabFolder createFolder() {
		final CTabFolder folder = new CTabFolder(sash, SWT.TOP | SWT.BORDER);
		final Cursor cursor = new Cursor(shell.display, SWT.CURSOR_SIZEALL);
		folder.setLayout(new FillLayout());
		folder.setSimple(false);

		final Menu tabContextMenu = new Menu(folder);
		folder.setMenu(tabContextMenu);
		folder.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (!cursor.isDisposed())
					cursor.dispose();
				if (!tabContextMenu.isDisposed())
					tabContextMenu.dispose();
			}
		});

		folder.setSelectionForeground(shell.titleForeColor);
		folder.setSelectionBackground(new Color[] { shell.titleBackColor1, shell.titleBackColor2 }, new int[] { 100 }, true);

		// Drag tab code start
		// Close tab with middle mouse code start
		// Tab history code start
		Listener listener = new Listener() {
			boolean drag = false;
			boolean exitDrag = false;
			CTabItem dragItem;
			public void handleEvent(Event e) {
				Point p = new Point(e.x, e.y);
				if (e.type == SWT.DragDetect) {
					p = folder.toControl(shell.display.getCursorLocation()); // see bug 43251
				}
				switch (e.type) {
				case SWT.MouseDown: {
					  if (e.button == 2){ // Close tab with middle click (or mouse wheel click)
							if (shell.confirmClose(folder.getSelection())) {
								shell.clearErrorAndTaskList(folder.getSelection());
								folder.getSelection().dispose();
								shell.setLineColumn();
							}
					  }
					  else{ // Record that tab selection was changed
						  shell.addToTabHistory();
						  //addToNavHistory(((EditorComposite)item.getControl()).getFile(),line);
					  }
					  break;
				}
				case SWT.DragDetect: {
					CTabItem item = folder.getItem(p);
					if (item == null)
						return;
					//e.image = shell.display.getSystemImage(SWT.ICON_WARNING);

					drag = true;
					exitDrag = false;
					dragItem = item;
					folder.setCursor(cursor);
					break;
				}
				case SWT.MouseEnter:
					if (exitDrag) {
						exitDrag = false;
						drag = e.button != 0;
					}
					break;
				case SWT.MouseExit:
					if (drag) {
						folder.setInsertMark(null, false);
						exitDrag = true;
						// Deliberately keep drag = true: the pointer may be crossing into
						// the sibling pane, which MouseUp handles above.
					}
					break;
				case SWT.MouseUp: {
					if (!drag)
						return;
					folder.setInsertMark(null, false);
					shell.drawDestTabRect(null);
					CTabFolder over = folderAt(folder.toDisplay(p));
					if (over != folder) {
						if (over != null && canMove(dragItem))
							moveToOtherView(dragItem);
						drag = false;
						exitDrag = false;
						dragItem = null;
						folder.setCursor(null);
						return;
					}
					CTabItem item = folder.getItem(p);
					if (item != null) {
						Rectangle sourceRect = dragItem.getBounds();
						Rectangle destRect = item.getBounds();
						boolean after = sourceRect.x < destRect.x;
						int index = folder.indexOf(item);
						index = after ? index + 1 : index - 0;
						index = Math.max(0, index);
						CTabItem newItem = createItem(folder, SWT.CLOSE, index);
						//newItem.setText("new tab item");
						MainShell.copyTabState(dragItem, newItem);
						Control c = dragItem.getControl();
						newItem.setControl(c);
						dragItem.setControl(null);
						dragItem.dispose();
						shell.setMainFolderSelection(newItem);
						if (folder.getSelection() != null && (folder.getSelection().getControl()) instanceof EditorComposite)
							((EditorComposite) folder.getSelection().getControl()).getStyledText().setFocus();
						shell.setMainTitle();
					}
					drag = false;
					exitDrag = false;
					dragItem = null;
					folder.setCursor(null);
					break;
				}
				case SWT.MouseMove: {
					if (!drag)
						return;
					CTabItem item = folder.getItem(p);
					if (item == null) {
						folder.setInsertMark(null, false);
						shell.drawDestTabRect(null);
						return;
					}
					Rectangle rect = item.getBounds();
					boolean after = p.x > rect.x + rect.width / 2;
					folder.setInsertMark(item, after);
					shell.drawDestTabRect(item);
//				    // Workaround for bug #32846
//				    if (item == -1) {
//				    	folder.redraw();
//				    }
					break;
				}
				}
			}
		};
		folder.addListener(SWT.DragDetect, listener);
		folder.addListener(SWT.MouseUp, listener);
		folder.addListener(SWT.MouseMove, listener);
		folder.addListener(SWT.MouseExit, listener);
		folder.addListener(SWT.MouseEnter, listener);
		folder.addListener(SWT.MouseDown, listener);

		// Drag tab code end

		folder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (folder.getSelection() != null && (folder.getSelection().getControl()) instanceof EditorComposite) {
					SymitarFile file = ((EditorComposite) folder.getSelection().getControl()).getFile();
					if (file.getType() != FileType.REPGEN || file.isLocal())
						shell.install.setEnabled(false);
					else
						shell.install.setEnabled(true);

					if (file.getType() != FileType.REPGEN || file.isLocal())
						shell.run.setEnabled(false);
					else
						shell.run.setEnabled(true);

					if ((file.getType() == FileType.REPGEN)||(file.getType() == FileType.LETTER)||(file.getType() == FileType.HELP)||(file.getType() == FileType.DATA))
						shell.hltoggle.setEnabled(true);

					shell.savetb.setEnabled(true);
					shell.print.setEnabled(true);
				} else {
					shell.print.setEnabled(true);
					shell.savetb.setEnabled(false);
					shell.run.setEnabled(false);
					shell.install.setEnabled(false);
					shell.hltoggle.setEnabled(false);
				}
			}
		});

		final MenuItem closeTab = new MenuItem(tabContextMenu, SWT.NONE);
		closeTab.setText("Close Tab");
		closeTab.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (folder.getSelectionIndex() != -1) {
					if (shell.confirmClose(folder.getSelection())) {
						shell.clearErrorAndTaskList(folder.getSelection());
						folder.getSelection().dispose();
						shell.setLineColumn();
					}
				}
			}

		});

		final MenuItem closeOthers = new MenuItem(tabContextMenu, SWT.NONE);
		closeOthers.setText("Close Others");
		closeOthers.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				CTabItem selection = folder.getSelection();
				if (selection != null && allItems().size() > 1) {

					for (CTabItem item : allItems())
						if (!item.equals(selection))
							if (shell.confirmClose(item)) {
								shell.clearErrorAndTaskList(item);
								item.dispose();
							}

				}
			}

		});

		final MenuItem closeAll = new MenuItem(tabContextMenu, SWT.NONE);
		closeAll.setText("Close All");
		closeAll.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (allItems().size() >= 1) {

					for (CTabItem item : allItems())
						if (shell.confirmClose(item)) {
							shell.clearErrorAndTaskList(item);
							item.dispose();
						}

				}
			}

		});

		final MenuItem separator = new MenuItem(tabContextMenu, SWT.SEPARATOR);

		final MenuItem save = new MenuItem(tabContextMenu, SWT.None);
		save.setText("Save");
		save.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (folder.getSelectionIndex() != -1 && (folder.getSelection().getControl() instanceof EditorComposite)) {
					((EditorComposite) folder.getSelection().getControl()).saveFile(true);
				} else {
					System.out.println("Error:  Can not save non-EditorComposite File");
				}
			}
		});

		final MenuItem saveAll = new MenuItem(tabContextMenu, SWT.NONE);
		saveAll.setText("Save All");
		saveAll.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.saveAllRepgens();
			}

		});

		final MenuItem installRepgen = new MenuItem(tabContextMenu, SWT.NONE);
		installRepgen.setText("Install");
		installRepgen.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (folder.getSelectionIndex() != -1 && (folder.getSelection().getControl() instanceof EditorComposite)) {

					((EditorComposite) folder.getSelection().getControl()).installRepgen(true);
				}
			}
		});

		final MenuItem moveToOther = new MenuItem(tabContextMenu, SWT.NONE);
		moveToOther.setText("&Move to Other View");
		moveToOther.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (folder.getSelectionIndex() != -1)
					moveToOtherView(folder.getSelection());
			}
		});

		tabContextMenu.addMenuListener(new MenuAdapter() {

			@Override
			public void menuShown(MenuEvent e) {
				boolean flag = folder.getSelectionIndex() != -1;

				closeTab.setEnabled(flag);
				closeAll.setEnabled(flag);

				save
				.setEnabled((flag && (folder.getSelection().getControl() instanceof EditorComposite) && folder.getSelection().getData("modified") != null && (Boolean) folder
						.getSelection().getData("modified")));

				saveAll.setEnabled(flag);
				installRepgen.setEnabled(flag && (folder.getSelection().getControl() instanceof EditorComposite)
						&& !((EditorComposite) folder.getSelection().getControl()).getFile().isLocal());

				closeOthers.setEnabled(flag && allItems().size() > 1);
				moveToOther.setEnabled(flag && canMove(folder.getSelection()));

			}

		});

		// Make the find/replace box know which thing we are looking through, if
		// the window is open as we switch tabs
		folder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (folder.getSelection() != null && folder.getSelection().getControl() instanceof EditorComposite)
					shell.findReplaceShell.attach(((EditorComposite) folder.getSelection().getControl()).getStyledText(), true);
				else if (folder.getSelection() != null && folder.getSelection().getControl() instanceof ReportComposite)
					shell.findReplaceShell.attach(((ReportComposite) folder.getSelection().getControl()).getStyledText(), false);

				// show active repgen's title in the window title
				shell.setMainTitle();
			}

		});

		folder.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				if (folder.getSelection().getControl() instanceof EditorComposite){
					if(((EditorComposite)folder.getSelection().getControl()).getHighlight()){
						shell.hltoggle.setImage(RepDevMain.smallHighlight);
					}else{
						shell.hltoggle.setImage(RepDevMain.smallHighlightGrey);
					}
				}
			}
		});

		folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				event.doit = shell.confirmClose((CTabItem) event.item);
				shell.setLineColumn();

				if (event.doit) {
					if( folder.getSelection() == event.item )
						shell.getShell().setText(RepDevMain.NAMESTR); // remove active repgen name from title
					shell.clearErrorAndTaskList((CTabItem) event.item);
				}

				if (allItems().size() == 1) {
					shell.install.setEnabled(false);
					shell.savetb.setEnabled(false);
					shell.hltoggle.setEnabled(false);
					shell.print.setEnabled(false);
					shell.run.setEnabled(false);
				}
			}
		});
		return folder;
	}
}
