package com.repdev;

/**
 * Also provides features for a text EDITOR in the tab view
 * @author Jake Poznanski
 *
 */
public interface TabTextEditorView extends TabTextView {
	public boolean canUndo();
	public boolean canRedo();
	
	public void undo();
	public void redo();
}
