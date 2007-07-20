package com.repdev;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.graphics.*;

public class AboutBoxShell {

	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="24,10"
	private Label logo = null;
	private Label aboutVersion = null;
	private static AboutBoxShell me = new AboutBoxShell();
	private Text aboutTextBox = null;

	private AboutBoxShell() {}
	
	public static void show() {
		me.createSShell();
	}
	
	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
				
		GridData gridData11 = new GridData();
		gridData11.grabExcessHorizontalSpace = true;
		gridData11.grabExcessVerticalSpace = false;
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.verticalSpan = 2;
		gridData1.grabExcessVerticalSpace = true;
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.verticalAlignment = GridData.FILL;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.BEGINNING;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.makeColumnsEqualWidth = false;
		sShell = new Shell(Display.getCurrent(),SWT.DIALOG_TRIM);
		sShell.setText("About RepDev");
		sShell.setLayout(gridLayout);
		sShell.setSize(new Point(400, 400));
		
		logo = new Label(sShell, SWT.NONE);
		logo.setText("");
		logo.setLayoutData(gridData11);
		logo.setImage(new Image(null, RepDevMain.IMAGE_DIR + "repdev_logo.png" ));
		aboutVersion = new Label(sShell, SWT.NONE);
		aboutVersion.setBackground(null);
		aboutVersion.setLayoutData(gridData);
		aboutVersion.setText("RepDev " + RepDevMain.VERSION);
		
		aboutTextBox = new Text(sShell, SWT.BORDER | SWT.V_SCROLL);
		aboutTextBox.setEditable(false);
		aboutTextBox.setBackground( new Color(null, 255, 255, 255) );
		aboutTextBox.setLayoutData(gridData1);
		
		String aboutText = 
		   "RepDev (" + RepDevMain.VERSION + ")\n"
		  +"(c) 2007\n"
		  +"  http://repdev.org/\n"
		  +"  support@repdev.org"
		  +"\n"
		  +"RepDev is a community based free IDE for use writing\n"
		  +"RepGens for Symitar systems.  It is ment to be an\n"
		  + "alternative to using Episys's built in RepGen tools.\n"
		  +"\n\n"
		  +"This program is distributed in the hope that it will be useful,\n"
		  +"but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
          +"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE."
          +"\n\n"
		  +"Credits:\n"
		  +"  Jake Poznanski - Programmer/Project Head\n"
		  +"  Ryan Schultz   - Programmer\n"
		  +"  Michael Webb   - Documentation\n" 
		  +"  Bruce Chang    - Documentation\n";
		
		aboutText += "\n";		
		aboutTextBox.setText(aboutText);
		
		//sShell.pack();
		sShell.setVisible(true);
		sShell.setActive();
	}

}
