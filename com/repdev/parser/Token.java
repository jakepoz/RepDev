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

package com.repdev.parser;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Color;

import com.repdev.SnippetVariable;
import com.repdev.SyntaxHighlighter;

public class Token {
	private String str;
	private Token after = null, before = null;
	private int pos, commentDepth, afterDepth;
	private boolean inString, afterString, inDate, afterDate, inDefs, inComment;

	private static final String[] heads = { "setup", "print title", "select", "define", "do", "total", "headers", ":(", "(", "\"", "'", "[", "procedure", "sort" };
	private static final String[] ends = {"end", ")", "\"", "'", "]"};
	
	private Color specialBackground = null;
	private SpecialBackgroundReason backgroundReason;
	private TokenType tokenType;
	private SnippetVariable currentVar; //Used for background highlighting logic
	
	public enum SpecialBackgroundReason{
		NONE,
		BLOCK_MATCHER,
		CODE_SNIPPET,
	}
	public enum TokenType{
		CONSTANT,
		DEFINED_VARIABLE,
		PROCEDURE,
		SYMITAR_KEYWORD,
	}
	public Token(String str, int pos, int commentDepth, int afterDepth, boolean inString, boolean afterString, boolean inDefs, boolean inDate, boolean afterDate) {
		this.str = str;
		this.pos = pos;
		this.commentDepth = commentDepth;
		this.afterDepth = afterDepth;
		this.inString = inString;
		this.afterString = afterString;
		this.inDate = inDate;
		this.afterDate = afterDate;
		this.inDefs = inDefs;
	}
	
	/**
	 * Provided for making deep copies of tokens for the background processors
	 * @param old
	 */
	public Token( Token old ){
		if( old == null)
		  return;
		
		this.str = new String(old.str);
		this.pos = old.pos;
		this.commentDepth = old.commentDepth;
		this.afterDepth = old.afterDepth;
		this.inString = old.inString;
		this.afterString = old.afterString;
		this.inDate = old.inDate;
		this.afterDate = old.afterDate;
		this.inDefs = old.inDefs;
	}

	public void setNearTokens(ArrayList<Token> tokens, int mypos) {
        after = null;
        before = null;

        if( tokens != null){
                if( mypos-1 >= 0)
                        before = tokens.get(mypos-1);
                else
                        before = null;

                if( mypos + 1 < tokens.size())
                        after = tokens.get(mypos+1);
                else
                        after = null;
        }
}

	
	public Color getSpecialBackground() {
		return specialBackground;
	}

	public void setSpecialBackground(Color specialBackground) {
		this.specialBackground = specialBackground;
	}
	
	public void setStr(String str) {
		this.str = str;
	}

	public Token getNextNCToken() {
		return after;
	}

	public int getStart() {
		return pos;
	}

	public int getEnd() {
		return pos + str.length();
	}
	
	public void setPos(int pos) {
		this.pos = pos;
	}

	public String getStr() {
		return str;
	}

	public int length() {
		return str.length();
	}

	public int getCDepth() {
		return commentDepth;
	}

	public int getEndCDepth() {
		return afterDepth;
	}

	public boolean inString() {
		return inString;
	}

	public boolean endInString() {
		return afterString;
	}

	public boolean inDate() {
		return inDate;
	}

	public boolean getEndInDate() {
		return afterDate;
	}

	public boolean inDefs() {
		return inDefs;
	}

	public void incStart(int amount) {
		pos += amount;
	}

	public void setInDefs(boolean b) {
		inDefs = b;
	}

	public void setCDepth(int before, int after) {
		commentDepth = before;
		afterDepth = after;
	}

	public void setInString(boolean before, boolean after) {
		inString = before;
		afterString = after;
	}

	public void setInDate(boolean before, boolean after) {
		inDate = before;
		afterDate = after;
	}

	public boolean dbFieldValid(ArrayList<Record> records) {
		Token record = before.before;

		if (record == null)
			return false;

		if (!record.dbRecordValid())
			return false;

		String recordName = record.getStr();

		for (Record rec : DatabaseLayout.getInstance().getFlatRecords()) {
			if (rec.getName().toLowerCase().equals(recordName)) {
				for (Field field : rec.getFields()) {
					if (field.getName().toLowerCase().equals(str))
						return true;
				}
			}
		}

		return false;
	}

	public boolean dbFieldValidNoSubFld(ArrayList<Record> records) {
		Token record = before.before;

		if (record == null)
			return false;

		if (!record.dbRecordValid())
			return false;

		String recordName = record.getStr();
		String [] tmpField = str.split(":");

		for (Record rec : DatabaseLayout.getInstance().getFlatRecords()) {
			if (rec.getName().toLowerCase().equals(recordName)) {
				for (Field field : rec.getFields()) {
					String [] tmp = field.getName().toLowerCase().split(":");
					if (tmp.length>0 && tmp[0].equals(tmpField[0])) {
						if ( tmpField.length>1 && tmpField[1].indexOf("(") == 0 && tmpField[1].indexOf(")") == tmpField[1].length() - 1 ) {
							String myVar= tmpField[1].substring(1, tmpField[1].length() - 1);
							// if(isVar(myVar)) return true; //TODO: need to find a way to validate the info in the parenthesis is a variable.
							return true;
							
						}
					}
				}
			}
		}

		return false;
	}

	//TODO: Cannot make a reference to the non-static method.  By making getLvars() static, fubars the variable parser.
	/*
	public boolean isVar(String str) {
		for (int i = 0; i < RepgenParser.getLvars().size(); i++){
			Variable var = RepgenParser.getLvars().get(i);

			if (var.getName().equals(str))
				return true;
		}

		return false;
	}
*/
	public boolean dbRecordValid() {
		return DatabaseLayout.getInstance().containsRecordName(str);
	}

	public String toString() {
		return pos + ":" + str + "(" + commentDepth + "," + inString + "," + inDefs + ")";
	}

	public Token getAfter() {
		return after;
	}

	public Token getBefore() {
		return before;
	}
	
	public boolean isHead() {
		for( String head: heads ) {
			if( this.getStr().equals(head) )
				return true;
		}
		
		return false;		
	}
	
	public boolean isEnd() {
		for( String end: ends ) {
			if( this.getStr().equals(end) )
				return true;
		}
		
		return false;		
	}
	
	//The "real" methods also know about tokens that can be either the start or end of a block, like quotes
	public boolean isRealHead(){
		return isHead() && 
		((getCDepth() == 0 || ( getStr().equals("[") && getAfter() != null && getAfter().getCDepth() > 0))) && 
		((!inDate() || ( getStr().equals("'") && getAfter() != null && getAfter().inDate() ))) &&
		((!inString() || ( getStr().equals("\"") && getAfter() != null && getAfter().inString() )));
	}

	public boolean isRealEnd(){
		return  isEnd() && 
		((getCDepth() == 0 || ( getStr().equals("]") && getBefore() != null && getBefore().getCDepth() > 0))) && 
		((!inDate() || ( getStr().equals("'") && getBefore() != null && getBefore().inDate() ))) &&
		((!inString() || ( getStr().equals("\"") && getBefore() != null && getBefore().inString() )));
	}

	public void setBackgroundReason(SpecialBackgroundReason backgroundReason) {
		this.backgroundReason = backgroundReason;
	}

	public SpecialBackgroundReason getBackgroundReason() {
		return backgroundReason;
	}

	public void setCurrentVar(SnippetVariable currentVar) {
		this.currentVar = currentVar;
	}

	public SnippetVariable getSnippetVar() {
		return currentVar;
	}

	public void setTokenType(TokenType tokenType) {
		this.tokenType = tokenType;
	}

	public TokenType getTokenType() {
		return tokenType;
	}

	private void setInComment(boolean inComment) {
		this.inComment = inComment;
	}

	private boolean isInComment() {
		return inComment;
	}
}