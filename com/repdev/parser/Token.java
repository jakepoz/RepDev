package com.repdev.parser;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Color;

public class Token {
	private String str;
	private Token after = null, before = null;
	private int pos, commentDepth, afterDepth;
	private boolean inString, afterString, inDate, afterDate, inDefs;
	private Color specialBackground = null;


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

		// if(inString || commentDepth!=0)
		// return;

		for (int i = mypos - 1; i >= 0; i--) {
			// if(!tokens.get(i).getInString() &&
			// tokens.get(i).getCDepth()==0) {
			before = tokens.get(i);
			break;
			// }
		}

		for (int i = mypos + 1; i < tokens.size(); i++) {
			// if(!tokens.get(i).getInString() &&
			// tokens.get(i).getCDepth()==0) {
			after = tokens.get(i);
			break;
		}
		// }
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
}