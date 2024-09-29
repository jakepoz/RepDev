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
import com.repdev.parser.Token.SpecialBackgroundReason;


/**
 * Adds the right listeners to a StyledText object to colorize repgens. Uses RepgenParser for the tokenization 
 * @author Jake Poznanski
 *
 */
public class SyntaxHighlighter implements ExtendedModifyListener, LineStyleListener, LineBackgroundListener {
	//TODO: Style set here
	private static String styleName = "default";

	private static String FONT_NAME = ""; // "Courier New"; //Fix Font Behavior
	private static int FONT_SIZE = 0;// = 11;

	private static RGB BACKGROUND = new RGB(255, 255, 255), FOREGROUND = new RGB(0, 0, 0);
	private static EStyle MAIN = new EStyle(null,null), 
	NORMAL = new EStyle(null, null), 
	COMMENTS = new EStyle(new RGB(127, 127, 127), null), 
	VARIABLES = new EStyle(new RGB(0, 0, 0), null, SWT.BOLD), 
	FUNCTIONS = new EStyle(new RGB(0, 0, 255), null, SWT.BOLD),
	KEYWORDS = new EStyle(new RGB(0, 0, 255), null), 
	TYPE_CHAR = new EStyle(new RGB(255, 0, 0), null), 
	TYPE_DATE = new EStyle(new RGB(255, 0, 0), null, SWT.BOLD), 
	STRUCT1 = new EStyle(new RGB(255, 0, 255), null), 
	STRUCT2 = new EStyle(new RGB(255, 128, 255), null), 
	STRUCT1_INVALID = new EStyle(new RGB(255, 0, 255), new RGB(128, 0, 0), SWT.NONE), 
	STRUCT2_INVALID = new EStyle(new RGB(255, 128, 255), new RGB(128, 0, 0), SWT.NONE),
	TASK = new EStyle(new RGB(64,64,64), null, SWT.BOLD);

	private static Color FORECOLOR = new Color(Display.getCurrent(), FOREGROUND), BACKCOLOR = new Color(Display.getCurrent(), BACKGROUND), BULLETS = new Color(Display.getCurrent(),new RGB(105,105,105));
	private static Font FONT;

	private RepgenParser parser;
	private StyledText txt;
	private SymitarFile file;
	private int sym;

	//Custom line background, used by the compare composite interface
	private int[] customLines = null;
	private static Color customColor, tokenColor;
	
	
	static {
		loadStyle(Config.getStyle());
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
		if(parser.getSym()==Config.getLiveSym() && !this.file.isLocal())
			txt.setBackground(new Color(Display.getCurrent(), getRGB(Config.getLiveSymColor())));
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

	public void highlight(){
		txt.removeExtendedModifyListener(this);
		txt.removeLineBackgroundListener(this);
		txt.removeLineStyleListener(this);
	}

	public static Color getLineColor(){
		return customColor;
	}

	public static Color getBlockMatchColor(){
		return tokenColor;
	}

	public Color getBulletColor(){
		return BULLETS;
	}

	public static void loadStyle(String styleName){
		System.out.println("Loading theme " + styleName + ".xml");
		try{
			Style style = new Style( new File("styles\\" + styleName + ".xml" ));
			FONT_NAME = style.getFontValue("editor", "font"); // "Courier New";
			FONT_SIZE = style.getFontSize("editor", "fontSize"); // 11;
			BACKGROUND = style.getColor("editor", "bgColor");  // just... don't...
			FOREGROUND = style.getColor("editor", "fgColor");  // ask, it's not worth it.
			FORECOLOR = new Color(Display.getCurrent(), FOREGROUND);
			BACKCOLOR = new Color(Display.getCurrent(), BACKGROUND);
			NORMAL = new EStyle(null, null);
			customColor = new Color(Display.getCurrent(), style.getColor("editor", "line"));
			tokenColor = new Color(Display.getCurrent(), style.getColor("editor", "token"));
			COMMENTS = new EStyle(style.getColor("comments", "fgColor"), style.getColor("comments", "bgColor"), style.getStyle("comments")); 
			VARIABLES = new EStyle(style.getColor("variables", "fgColor"), style.getColor("variables", "bgColor"), style.getStyle("variables")); 
			FUNCTIONS = new EStyle(style.getColor("functions", "fgColor"), style.getColor("functions", "bgColor"), style.getStyle("functions"));
			KEYWORDS = new EStyle(style.getColor("keywords", "fgColor"), style.getColor("keywords", "bgColor"), style.getStyle("keywords"));
			TYPE_CHAR = new EStyle(style.getColor("typeChar", "fgColor"), style.getColor("typeChar", "bgColor"), style.getStyle("typeChar")); 
			TYPE_DATE = new EStyle(style.getColor("typeDate", "fgColor"), style.getColor("typeDate", "bgColor"), style.getStyle("typeDate")); 
			STRUCT1 = new EStyle(style.getColor("struct1", "fgColor"), style.getColor("struct1", "bgColor"), style.getStyle("struct1")); 
			STRUCT2 = new EStyle(style.getColor("struct2", "fgColor"), style.getColor("struct2", "bgColor"), style.getStyle("struct2")); 
			STRUCT1_INVALID = new EStyle(style.getColor("struct1Inv", "fgColor"), style.getColor("struct1Inv", "bgColor"), style.getStyle("struct1Inv")); 
			STRUCT2_INVALID = new EStyle(style.getColor("struct2Inv", "fgColor"), style.getColor("struct2Inv", "bgColor"), style.getStyle("struct2Inv"));
			TASK = new EStyle(style.getColor("task", "fgColor"), style.getColor("task", "bgColor"), style.getStyle("task"));
			try{
				BULLETS = new Color(Display.getCurrent(),style.getColor("linenumber","fgColor"));
			}catch(Exception e){
				BULLETS = new Color(Display.getCurrent(),new RGB(127, 127, 127));
			}
		}catch(Exception e){
			//System.out.println(e.getMessage());
			System.out.println("Invalid theme using default");
			FONT_NAME = "Courier New";
			FONT_SIZE = 11;
			BACKGROUND = new RGB(255, 255, 255);
			FOREGROUND = new RGB(0, 0, 0);
			NORMAL = new EStyle(null, null);
			COMMENTS = new EStyle(new RGB(127, 127, 127), null);
			VARIABLES = new EStyle(new RGB(0, 0, 0), null, SWT.BOLD); 
			FUNCTIONS = new EStyle(new RGB(0, 0, 255), null, SWT.BOLD);
			KEYWORDS = new EStyle(new RGB(0, 0, 255), null); 
			TYPE_CHAR = new EStyle(new RGB(255, 0, 0), null); 
			TYPE_DATE = new EStyle(new RGB(255, 0, 0), null, SWT.BOLD); 
			STRUCT1 = new EStyle(new RGB(255, 0, 255), null); 
			STRUCT2 = new EStyle(new RGB(255, 128, 255), null); 
			STRUCT1_INVALID = new EStyle(new RGB(255, 0, 255), new RGB(128, 0, 0), SWT.NONE); 
			STRUCT2_INVALID = new EStyle(new RGB(255, 128, 255), new RGB(128, 0, 0), SWT.NONE);
			TASK = new EStyle(new RGB(64,64,64), null, SWT.BOLD);

			FORECOLOR = new Color(Display.getCurrent(), FOREGROUND);
			BACKCOLOR = new Color(Display.getCurrent(), BACKGROUND);

			customColor = new Color(Display.getCurrent(), new RGB(232,242,254));
			tokenColor = new Color(Display.getCurrent(), new RGB(192,192,192));
			BULLETS = new Color(Display.getCurrent(),new RGB(127, 127, 127));
		}
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

		if (tok.getCDepth() != 0) {
			range = COMMENTS.getRange(tok.getStart(), tok.length());
			for( String taskType: RepgenParser.taskTokens )
				if( tok.getStr().equals(taskType) && (tok.getAfter() != null && tok.getAfter().getStr().equals(":")) ) range = TASK.getRange(tok.getStart(), tok.length());

		} else if (tok.inString())
			range = TYPE_CHAR.getRange(tok.getStart(), tok.length());
		else if (tok.inDate())
			range = TYPE_DATE.getRange(tok.getStart(), tok.length());
		// Validates the token is a Record before the colon
		else if (tok.getAfter() != null && tok.getAfter().getStr().equals(":")) {
			if (tok.dbRecordValid())
				range = STRUCT1.getRange(tok.getStart(), tok.length());
			else
				range = STRUCT1_INVALID.getRange(tok.getStart(), tok.length());
		// Validates the token is a Field or a Field without the Sub Field if the next token is :(
		} else if (tok.getBefore() != null && tok.getBefore().getStr().equals(":")) {
			if (tok.dbFieldValid(RepgenParser.getDb().getTreeRecords()) || (tok.dbFieldValidNoSubFld(RepgenParser.getDb().getTreeRecords())))
				range = STRUCT2.getRange(tok.getStart(), tok.length());
			else
				range = STRUCT2_INVALID.getRange(tok.getStart(), tok.length());
		} else if (FunctionLayout.getInstance().containsName(tok.getStr()) && tok.getAfter() != null && tok.getAfter().getStr().equals("("))
			range = FUNCTIONS.getRange(tok.getStart(), tok.length());
		else if (RepgenParser.getKeywords().contains(tok.getStr()))
			range = KEYWORDS.getRange(tok.getStart(), tok.length());
		else if (RepgenParser.getSpecialvars().contains(tok.getStr()))
			range = VARIABLES.getRange(tok.getStart(), tok.length());
		for (int i = 0; i < parser.getLvars().size(); i++){
			Variable var = parser.getLvars().get(i);

			if (var.getName().equals(tok.getStr()))
				isVar = true;
		}

		if (range == null && isVar)
			range = VARIABLES.getRange(tok.getStart(), tok.length());
		else if( range == null ){
			range = NORMAL.getRange(tok.getStart(), tok.length());	
		}

		if( tok.getSpecialBackground() != null)
			range.background = tok.getSpecialBackground();

		return range;
	}

	public void lineGetStyle(LineStyleEvent event) {
		ArrayList<Token> ltokens = parser.getLtokens();
		ArrayList<StyleRange> ranges = new ArrayList<StyleRange>();

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

		for (int i = ftoken; i < ltoken; i++){
			StyleRange range = getStyle(ltokens.get(i));			
			ranges.add(range);

			//If we are a backgroudn highlighted token, and a CODE SNIPPET one, then connect the highlighting over whitespace between tokens
			if( ltokens.get(i).getBackgroundReason() == SpecialBackgroundReason.CODE_SNIPPET )
				if( i + 1 < ltoken && ltokens.get(i+1).getBackgroundReason() == SpecialBackgroundReason.CODE_SNIPPET && ltokens.get(i+1).getSnippetVar() == ltokens.get(i).getSnippetVar())
					ranges.add(new StyleRange(ltokens.get(i).getEnd(),ltokens.get(i+1).getStart()-ltokens.get(i).getEnd(),null,ltokens.get(i).getSpecialBackground()));
		}

		StyleRange[] rangesArray = new StyleRange[ranges.size()];

		event.styles = ranges.toArray(rangesArray);
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

	private RGB getRGB(String rgbHex) {
		int[] rgb = {0,0,0};

		rgb[0] = Integer.parseInt(rgbHex.substring(0,2), 16);
		rgb[1] = Integer.parseInt(rgbHex.substring(2,4), 16);
		rgb[2] = Integer.parseInt(rgbHex.substring(4), 16);

		return new RGB(rgb[0],rgb[1],rgb[2]);
	}
}
