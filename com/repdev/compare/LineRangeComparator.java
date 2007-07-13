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
		
		return txt.getText(txt.getOffsetAtLine(thisIndex),txt.getOffsetAtLine(Math.min(thisIndex+1,txt.getLineCount()-1))).equals(otherTxt.getText(otherTxt.getOffsetAtLine(thisIndex),otherTxt.getOffsetAtLine(Math.min(thisIndex+1,otherTxt.getLineCount()-1))));
	}

	public boolean skipRangeComparison(int length, int maxLength, IRangeComparator other) {
		return false;
	}

}
