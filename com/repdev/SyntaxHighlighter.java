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

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.repdev.parser.FunctionLayout;
import com.repdev.parser.RepgenParser;
import com.repdev.parser.Token;
import com.repdev.parser.Variable;


/**
 * Adds the right listeners to a StyledText object to colorize repgens. Uses RepgenParser for the tokenization 
 * @author Jake Poznanski
 *
 */
public class SyntaxHighlighter implements ExtendedModifyListener, LineStyleListener, LineBackgroundListener {
	private static final String FONT_NAME = "Courier New";
	private static final int FONT_SIZE = 11;

	private static final RGB BACKGROUND = new RGB(255, 255, 255), FOREGROUND = new RGB(0, 0, 0);
	private static final EStyle NORMAL = new EStyle(null, null), COMMENTS = new EStyle(new RGB(127, 127, 127), null), VARIABLES = new EStyle(new RGB(0, 0, 0), null, SWT.BOLD), FUNCTIONS = new EStyle(new RGB(0, 0, 255), null, SWT.BOLD),
			KEYWORDS = new EStyle(new RGB(0, 0, 255), null), TYPE_CHAR = new EStyle(new RGB(255, 0, 0), null), TYPE_DATE = new EStyle(new RGB(255, 0, 0), null, SWT.BOLD), STRUCT1 = new EStyle(new RGB(255, 0, 255), null), STRUCT2 = new EStyle(
					new RGB(255, 128, 255), null), STRUCT1_INVALID = new EStyle(new RGB(255, 0, 255), new RGB(128, 0, 0), SWT.NONE), STRUCT2_INVALID = new EStyle(new RGB(255, 128, 255), new RGB(128, 0, 0), SWT.NONE);

	private static final Color FORECOLOR = new Color(Display.getCurrent(), FOREGROUND), BACKCOLOR = new Color(Display.getCurrent(), BACKGROUND);
	private static final Font FONT;

	private RepgenParser parser;
	private StyledText txt;
	private SymitarFile file;
	private int sym;
	
	private int[] customLines = null;
	private Color customColor;

	static {
		Font cur = null;

		try {
			cur = new Font(Display.getCurrent(), FONT_NAME, FONT_SIZE, SWT.NORMAL);
		} catch (Exception e) {
		}

		FONT = cur;
	}

	public SyntaxHighlighter(RepgenParser parser) {
		this.parser = parser;
		this.txt = parser.getTxt();
		this.file = parser.getFile();
		this.sym = parser.getSym();
		
		txt.setForeground(FORECOLOR);
		txt.setBackground(BACKCOLOR);
		txt.addExtendedModifyListener(this);
		txt.addLineStyleListener(this);
		if (FONT != null)
			txt.setFont(FONT);
	}
	
	/**
	 * This is used by compare composite to set custom background for sections with diffs
	 * @param parser
	 * @param customLineColor
	 * @param customLines
	 */
	public SyntaxHighlighter(RepgenParser parser, Color customLineColor, int[] customLines){
		this.parser = parser;
		this.txt = parser.getTxt();
		this.file = parser.getFile();
		this.sym = parser.getSym();
		
		this.customColor = customLineColor;
		this.customLines = customLines;
	
		txt.setForeground(FORECOLOR);
		txt.setBackground(BACKCOLOR);
		txt.addExtendedModifyListener(this);
		txt.addLineBackgroundListener(this);
		txt.addLineStyleListener(this);
		if (FONT != null)
			txt.setFont(FONT);
	}

	private static class EStyle {
		private Color fcolor = null, bgcolor = null;
		private int style;

		public EStyle(RGB frgb, RGB bgrgb, int style) {
			if (frgb != null)
				fcolor = new Color(Display.getCurrent(), frgb);
			if (bgrgb != null)
				bgcolor = new Color(Display.getCurrent(), bgrgb);
			this.style = style;
		}

		public EStyle(RGB frgb, RGB bgrgb) {
			this(frgb, bgrgb, SWT.NORMAL);
		}

		public StyleRange getRange(int start, int len) {
			return new StyleRange(start, len, fcolor, bgcolor, style);
		}
	}

	public void modifyText(ExtendedModifyEvent e) {
		parser.textModified(e.start, e.length, e.replacedText);
	}
	
	public StyleRange getStyle(Token tok) {
		boolean isVar = false;
		StyleRange range = null;

		if (tok.getCDepth() != 0)
			range = COMMENTS.getRange(tok.getStart(), tok.length());
		else if (tok.inString())
			range = TYPE_CHAR.getRange(tok.getStart(), tok.length());
		else if (tok.inDate())
			range = TYPE_DATE.getRange(tok.getStart(), tok.length());
		else if (tok.getAfter() != null && tok.getAfter().getStr().equals(":")) {
			if (tok.dbRecordValid())
				range = STRUCT1.getRange(tok.getStart(), tok.length());
			else
				range = STRUCT1_INVALID.getRange(tok.getStart(), tok.length());
		} else if (tok.getBefore() != null && tok.getBefore().getStr().equals(":")) {
			if (tok.dbFieldValid(RepgenParser.getDb().getTreeRecords()))
				range = STRUCT2.getRange(tok.getStart(), tok.length());
			else
				range = STRUCT2_INVALID.getRange(tok.getStart(), tok.length());
		} else if (FunctionLayout.getInstance().containsName(tok.getStr()) && tok.getAfter() != null && tok.getAfter().getStr().equals("("))
			range = FUNCTIONS.getRange(tok.getStart(), tok.length());
		else if (RepgenParser.getKeywords().contains(tok.getStr()))
			range = KEYWORDS.getRange(tok.getStart(), tok.length());
		else if (RepgenParser.getSpecialvars().contains(tok.getStr()))
			range = VARIABLES.getRange(tok.getStart(), tok.length());

		synchronized( parser.getLvars() ){
			for (Variable var : parser.getLvars()) {
				if (var.getName().equals(tok.getStr()))
					isVar = true;
			}
		}

		if (range == null && isVar)
			range = VARIABLES.getRange(tok.getStart(), tok.length());
		else if( range == null )
			range = NORMAL.getRange(tok.getStart(), tok.length());
		
		if( tok.getSpecialBackground() != null)
			range.background = tok.getSpecialBackground();
		
		return range;
	}

	public void lineGetStyle(LineStyleEvent event) {
		ArrayList<Token> ltokens = parser.getLtokens();
		
		int line = txt.getLineAtOffset(event.lineOffset);

		int ftoken;
		for (ftoken = 0; ftoken < ltokens.size(); ftoken++)
			if (ltokens.get(ftoken).getEnd() >= event.lineOffset)
				break;

		int ltoken = ltokens.size();
		if (line + 1 < txt.getLineCount()) {
			int pos = txt.getOffsetAtLine(line + 1);

			for (ltoken = ftoken; ltoken < ltokens.size(); ltoken++)
				if (ltokens.get(ltoken).getStart() > pos)
					break;
		}

		StyleRange[] result = new StyleRange[ltoken - ftoken];
		
		for (int i = ftoken; i < ltoken; i++)
			result[i - ftoken] = getStyle(ltokens.get(i));
		
		event.styles = result;
	}
	
	public void setCustomLines(int[] lines)
	{
		customLines = lines;
	}

	public void lineGetBackground(LineBackgroundEvent event) {
		boolean go = false;
		
		for( int i : customLines)
			if( i == txt.getLineAtOffset(event.lineOffset) )
			{
				go = true;
				break;
			}
		
		if( go ){
			event.lineBackground = customColor;
		}
	}

}
