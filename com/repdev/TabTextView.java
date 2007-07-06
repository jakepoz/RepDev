package com.repdev;

import org.eclipse.swt.custom.StyledText;


/**
 * Any tab control in the main window that works with a text view should at least implement this interface, so we can do operations on the styled text it uses.
 * 
 * @author Jake Poznanski
 * @see TabTextEditorView for text EDTIORS, which is this class + aditional stuff
 */
public interface TabTextView extends TabView{
	public StyledText getStyledText();

}
