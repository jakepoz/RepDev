package com.repdev.parser;

import java.util.ArrayList;

import com.repdev.EditorComposite;

/**
 * Takes an arraylist of tokens and the required options, and generates a newly formatted repgen
 * @author poznanja
 *
 */
public class Formatter {
	ArrayList<Token> tokens;
	String oldFile = "", newFile = "";
	
	public Formatter(String oldFile, ArrayList<Token> tokens){
		this.tokens = tokens;
		this.oldFile = oldFile;
	}
	
	private String getWhitespaceAfter(Token cur){
		if( cur.getAfter() == null)
			return oldFile.substring(cur.getEnd());
		
		return oldFile.substring(cur.getEnd(),cur.getAfter().getStart());
	}
	
	/**
	 * Gets correctly capped string
	 * @param cur
	 * @return
	 */
	private String getCorrectTokenString(Token cur){
		return oldFile.substring(cur.getStart(),cur.getEnd());
	}
	
	private int contains(String[] list, String str){
		int i = 0;
		
		for( String test : list){
			if( test.equals(str))
				return i;
			
			i++;
		}
		
		return -1;
	}
	
	private boolean processBeforeAndAfter(StringBuilder str, Token cur, String indent){
		String noSpaceBefore = "():.,%=+-/*<>";
		String noSpaceAfter = "(:.=+-/*$<>";
		String[] newLineAfter = { "do", "end" };
		int[] nNewLineAfter = { 1, 2 };
		
		String[] newLineBefore = { "do", "end" };
		int[] nNewLineBefore = { 1,1 };
		
		boolean addedNewline = false;
		int index;
		
		if( noSpaceAfter.contains(cur.getStr()) )
			;//For now, nothing
		else if( cur.getAfter() != null && noSpaceBefore.contains(cur.getAfter().getStr()))
			;
		else
			str.append(" ");
		
		if( (index = contains(newLineAfter,cur.getStr())) >= 0){
			int offset = 0;
			
			//If statements, if there is an else, we don't want the second new line
			if( cur.getAfter() != null && (cur.getAfter().getStr().equals("else") || cur.getAfter().getStr().equals("end")))
				offset++;
			
			for( int i = 0; i < nNewLineAfter[index] - offset; i++)
				str.append("\n" + indent);
			
			addedNewline = true;
		}
		
		if( cur.getAfter() != null && (index = contains(newLineBefore,cur.getAfter().getStr())) >= 0 ){
			for( int i = 0; i < nNewLineBefore[index]; i++)
				str.append("\n" + indent);
			
			addedNewline = true;
		}
			
		if( !addedNewline){ //Default, add a space after it and continue

			if( getWhitespaceAfter(cur).contains("\n")){
				str.append(getWhitespaceAfter(cur).replaceAll(" ","").replaceAll("\t","").replaceAll("\n", "\n" + indent));
			}
		}
		
		return true;
	}
	
	/**All the code formatting logic is in here*/
	public String getFormattedFile(){
		StringBuilder str = new StringBuilder();
		String indent = "", curStr;
	
			
		Token after = null;
		
		//Go through each token, and write the proper output to a new str buffer
		for( Token cur : tokens ){
			after = cur.getAfter();
			str.append(getCorrectTokenString(cur));
			curStr = cur.getStr();
			
			//Modify indentation
			if( cur.isRealHead() )
				indent += EditorComposite.getTabStr();
			
			if( after != null && after.isRealEnd() )
				indent = indent.substring(0,Math.max(0,indent.length()-EditorComposite.getTabStr().length()));
				
			
			if( !cur.inString() && cur.getCDepth() == 0 && !cur.inDate()){ //Plain old tokens
				processBeforeAndAfter(str, cur, indent);
			}
			else if( cur.inString() && cur.getCDepth() == 0 && !cur.inDate()){ //Inside strings
				if( after != null && after.inString())
					str.append(getWhitespaceAfter(cur)); //Maintain existing formatting if we are in a string
				else {
					processBeforeAndAfter(str, cur, indent); 
				}	
			}
			else if(cur.getCDepth() > 0) //In comments, don't format
				if( cur.isRealEnd() && curStr.equals("]"))
					processBeforeAndAfter(str, cur, indent); 
				else
					str.append(getWhitespaceAfter(cur));
		}
		
		return str.toString();
	}
}
