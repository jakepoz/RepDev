package com.repdev;

import org.eclipse.swt.custom.StyledText;

/**
 * Any tab control should implement this interface, so we can do operations on the styled text it uses.
 * @author Jake Poznanski
 * @see TabTextEditorView
 */
public interface TabTextView {
	public StyledText getStyledText();

}
