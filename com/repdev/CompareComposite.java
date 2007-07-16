package com.repdev;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import com.repdev.compare.BasicCompareComposite;
import com.repdev.compare.RangeDifference;
import com.repdev.parser.RepgenParser;

/**
 * Extends the basic compare composite to add repdev specific features
 * @author poznanja
 *
 */
public class CompareComposite extends BasicCompareComposite implements TabView {
	private SymitarFile leftFile, rightFile;
	private SyntaxHighlighter leftHighlighter, rightHighlighter;
	private RepgenParser leftParser, rightParser;
	private int[] lIntLines, rIntLines; //Lines needing background highlighting
	
	public CompareComposite(Composite parent, CTabItem tabItem, SymitarFile leftFile, SymitarFile rightFile){
		super(parent,leftFile.getName(), rightFile.getName(),RepDevMain.mainShell.getFileImage(leftFile),RepDevMain.mainShell.getFileImage(rightFile));
		
		this.leftFile = leftFile;
		this.rightFile = rightFile;
		
		//Setup custom stuff on the styled texts for our application
		String sData = leftFile.getData();
		
		if( sData == null )
			return;
		
		leftTxt.setText(sData);
		
		if( leftFile.getType() == FileType.REPGEN)
		{
			leftParser = new RepgenParser(leftTxt, leftFile);
			leftHighlighter = new SyntaxHighlighter(leftParser, new RGB(247,247,247),lIntLines);
			leftParser.reparseAll();
		}
		
		sData = rightFile.getData();
		
		if( sData == null)
			return;
			
		rightTxt.setText(sData);
		
		if( rightFile.getType() == FileType.REPGEN)
		{
			rightParser = new RepgenParser(rightTxt, rightFile);
			rightHighlighter = new SyntaxHighlighter(rightParser, new RGB(247,247,247),rIntLines);
			rightParser.reparseAll();
		}
		
		refreshDiffs();
	}
	
	@Override
	/**
	 * Allows for custom highlighting
	 */
	public void redraw() {
		super.redraw();
		leftTxt.redrawRange(0, leftTxt.getCharCount(), true);
		rightTxt.redrawRange(0, rightTxt.getCharCount(), true);
	}

	@Override
	protected void refreshDiffs() {
		super.refreshDiffs();
		
		//Create custom diffs line
		ArrayList<Integer> lLines = new ArrayList<Integer>(), rLines = new ArrayList<Integer>();
		
		System.out.println(Arrays.asList(diffs));
		
		for( RangeDifference diff : diffs){
			for( int i = diff.leftStart(); i < diff.leftEnd(); i++)
				lLines.add(i);
			
			for( int i = diff.rightStart(); i < diff.rightEnd(); i++)
				rLines.add(i);
		}
		
		lIntLines = new int[lLines.size()];
		rIntLines = new int[rLines.size()];
		
		int count = 0;
		
		for( int i : lLines )
		{
			lIntLines[count] = i;
			count++;
		}
		
		count = 0;
		
		for( int i : rLines )
		{
			rIntLines[count] = i;
			count++;
		}
		
		if( leftHighlighter != null)
			leftHighlighter.setCustomLines(lIntLines);
		if( rightHighlighter != null)
			rightHighlighter.setCustomLines(rIntLines);
	}

}
