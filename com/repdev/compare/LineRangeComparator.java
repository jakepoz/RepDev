package com.repdev.compare;

import org.eclipse.swt.custom.StyledText;

/**
 * Jake's implementaiton of IRangeComparator for lines of a StyledText
 * @author poznanja
 *
 */
public class LineRangeComparator implements IRangeComparator {
	StyledText txt;
	
	public LineRangeComparator(StyledText txt){
		this.txt = txt;
	}
	
	public StyledText getTxt(){
		return txt;
	}
	
	public int getRangeCount() {
		return txt.getLineCount();
	}

	public boolean rangesEqual(int thisIndex, IRangeComparator other, int otherIndex) {
		if( !(other instanceof LineRangeComparator))
			return false;
		
		StyledText otherTxt = ((LineRangeComparator)other).getTxt();
		String myLine, otherLine;
		
		//Don't try what we can't request
		if( thisIndex > txt.getLineCount() - 1)
			return false;
		
		if( otherIndex > otherTxt.getLineCount() - 1)
			return false;
		
		//Get my line first
		int startOffset = txt.getOffsetAtLine(thisIndex);
		int endOffset;
		
		if( thisIndex >= txt.getLineCount() - 1 )
			endOffset = txt.getCharCount() ;
		else
			endOffset = txt.getOffsetAtLine(thisIndex + 1);

		if( endOffset - 1 <= startOffset)
			myLine = "\n";
		else
			myLine = txt.getText(startOffset, endOffset - 2);			
		
		//Get the other line
		startOffset = otherTxt.getOffsetAtLine(otherIndex);
		endOffset = 0;
		
		if( otherIndex >= otherTxt.getLineCount() - 1 )
			endOffset = otherTxt.getCharCount() ;
		else
			endOffset = otherTxt.getOffsetAtLine(otherIndex + 1);

		if( endOffset - 1 <= startOffset)
			otherLine = "\n";
		else
			otherLine = otherTxt.getText(startOffset, endOffset - 2);	
		
		return myLine.equals(otherLine);
	}

	public boolean skipRangeComparison(int length, int maxLength, IRangeComparator other) {
		return false;
	}

}
