package com.repdev;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;

import com.repdev.parser.RepgenParser;

public class CompareComposite extends Composite implements TabView{
	private SymitarFile left, right;
	private StyledText leftTxt, rightTxt;
	private SyntaxHighlighter leftHighlighter, rightHighlighter;
	private RepgenParser leftParser, rightParser;
	private Composite parent;
	private Canvas birdsEye;
	private Slider slider;
	
	public CompareComposite(Composite parent, CTabItem tabItem, SymitarFile leftFile, SymitarFile rightFile){
		super(parent,SWT.NONE);
		this.left = leftFile;
		this.right = rightFile;
		this.parent = parent;
		
		buildGUI();
	}

	private void buildGUI() {
		GridLayout layout = new GridLayout();
		layout.numColumns = 6;
		setLayout(layout);
		
		Label imageLeft = new Label(this,SWT.NONE);
		imageLeft.setImage(RepDevMain.mainShell.getFileImage(left));
		
		Label nameLeft = new Label(this,SWT.NONE);	
		nameLeft.setText(left.getName());
		nameLeft.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		
		birdsEye = new Canvas(this,SWT.NONE);
		GridData data = new GridData(SWT.NONE, SWT.NONE, false, true,1,2);
		data.widthHint = 20;
		
		birdsEye.setLayoutData(data);
		
		Label imageRight = new Label(this,SWT.NONE);
		imageRight.setImage(RepDevMain.mainShell.getFileImage(right));
		
		Label nameRight = new Label(this,SWT.NONE);
		nameRight.setText(right.getName());
		nameLeft.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
			
		slider = new Slider(this,SWT.VERTICAL);
		slider.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false, true, 1,2));
		
		leftTxt = new StyledText(this,SWT.READ_ONLY | SWT.H_SCROLL | SWT.BORDER);
		leftTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,2,1));
		
		rightTxt = new StyledText(this,SWT.READ_ONLY | SWT.H_SCROLL | SWT.BORDER);
		rightTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2,1));
		
		//If we layout now, before we add any text, then the two texts will be the same width
		pack();
		
		if( left.getType() == FileType.REPGEN)
		{
			leftParser = new RepgenParser(leftTxt, left);
			leftHighlighter = new SyntaxHighlighter(leftParser);
		}
		
		if( right.getType() == FileType.REPGEN)
		{
			rightParser = new RepgenParser(rightTxt, right);
			rightHighlighter = new SyntaxHighlighter(rightParser);
		}

		String sData = left.getData();
		
		if( sData == null )
			return;
		
		leftTxt.setText(sData);
		
		sData = right.getData();
		
		if( sData == null)
			return;
			
		rightTxt.setText(sData);
		
	}
}
