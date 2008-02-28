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
 *
 *   Created by Bruce Chang
*/

package com.repdev.parser;

import java.util.ArrayList;

/**
 * This is a background parser that will generate a list of all the sections and procedures,
 * along with the pointers to the section title, first insertion point and the last insertion
 * point of these sections.  This has a built in locking system so the methods cannot return
 * values until the current parsing is complete. 
 */
public class BackgroundSectionParser{
	private ArrayList<Token> token;
	private ArrayList<SectionInfo> sectionInfo = new ArrayList<SectionInfo>();
	private final String[] sectionHead = {"define","print title","setup","select","sort","total","procedure"};
	private String curSection = "";
	private int curPos, curFirst, curLast;
	private boolean curExist, parsing = false;;
	private String txt;
	
	BackgroundParser backgroundParser = null;
	
	public BackgroundSectionParser(ArrayList <Token> token, String txt){
		this.token = token;
		this.txt = txt;
		refreshList(token,  txt);
	}
	
	/**
	 * Returns true if the section specified was found, else false.
	 * @return <b>boolean</b> true/false
	 */
	public boolean exist(String section){
		getSectionInfo(section);
		return curExist;
	}
	
	/**
	 * Returns the starting position of the section title specified.
	 * @return <B>int</B> Position pointer
	 */
	public int getPos(String section){
		getSectionInfo(section);
		return curPos;
	}
	
	/**
	 * Returns the first insert position of the specified section.  This basically
	 * returns the position after the first line feed following the specified
	 * section title.
	 * @return <B>int</B> Position pointer
	 */
	public int getFirstInsertPos(String section){
		getSectionInfo(section);
		return curFirst;
	}
	
	/**
	 * Returns the position of the END statement of the specified section.
	 * @return <B>int</B> Position pointer
	 */
	public int getLastInsertPos(String section){
		getSectionInfo(section);
		return curLast;
	}
	
	/**
	 * Given the offset, the title of the section will be returned.  NOTE: The offset passed in
	 * must be between the section title and the associated END statement.  If the pffset is
	 * between the END statement and the next section title, nothing will be returned.
	 * @return <B>String</B> Section Title
	 */
	public synchronized String whereAmI(int txtOffset){
		String tmpTitle = "";
		for(SectionInfo sec : sectionInfo){
			if(txtOffset >= sec.getPos() && txtOffset <= sec.getLastInsertPos()+3){
				tmpTitle = sec.getTitle();
				break;
			}
		}
		
		return tmpTitle;
	}
	
	/**
	 * Returns the array list of all the sectionInfo found.
	 * @return <B>ArrayList</B> SectionInfo
	 */
	public synchronized ArrayList<SectionInfo> getList(){
		return this.sectionInfo;
	}
	
	/**
	 * This is the exposed method for calling the background parser.
	 */
	public void refreshList(ArrayList <Token> token, String txt){
		this.token = token;
		this.txt = txt;
		
		// Do not parse again if it is currently parsing
		if(!parsing){
			parsing = true;
			if(backgroundParser == null){
				backgroundParser = new BackgroundParser();
				backgroundParser.start();
			}
		}
	}
	
	/**
	 * This is an internal method that will obtain all of the info for the specified section.
	 * Before obtaining the info for the section again, it will check to see if the info is
	 * current.
	 */
	private synchronized void getSectionInfo(String section){
	
		if(!curSection.equals(section.toLowerCase())){
			curSection = section.toLowerCase();
			curPos = -1;
			curFirst = -1;
			curLast = -1;
			curExist = false;

			for(SectionInfo sec : sectionInfo){
				if(sec.getTitle().toLowerCase().equals(curSection)){
					curPos = sec.getPos();
					curFirst = sec.getFirstInsertPos();
					curLast = sec.getLastInsertPos();
					curExist = true;
					break;
				}
			}
		}
	}
	
	
	/**
	 * This is the main engine that parse out all of the sections along with the pointers.
	 * @throws InterruptedException 
	 */
	private synchronized void parseSections() {
		int curDepth = 0;
		SectionInfo si = new SectionInfo();
		
		sectionInfo.clear();
		for(Token tok : token){
			if(tok.isRealHead()){
				curDepth++;
			}
			else if(tok.isRealEnd()){
				curDepth--;
			}
			
			if(isSectionHead(tok.getStr()) && !tok.inDate() && !tok.inString() && tok.getCDepth() == 0){
				if(false && curDepth != 1){
					System.out.println("Mismatched Heads and Tails found while parsing "+si.getTitle()+" Section");
					System.out.println(tok.getStr()+":"+curDepth);
				}
				curDepth=1;
				if(tok.getStr().equals("procedure")){
					si.setTitle(txt.substring(tok.getAfter().getStart(),tok.getAfter().getEnd()));
				}
				else{
					si.setTitle(txt.substring(tok.getStart(),tok.getEnd()));
				}
				si.setPos(tok.getStart());
				si.setFirstInsertPos(txt.indexOf("\n", si.getPos())+1);
			}
			else if(curDepth == 0){
				if(tok.getStr().equals("end") && tok.isRealEnd()){
					si.setLastInsertPos(tok.getStart());
					sectionInfo.add(new SectionInfo(si));
					si.clear();
				}
			}
		}
		
		curSection = "";
		parsing = false;
	}
	
	/**
	 * This is an internal method that returns true if the specified is a section head.
	 * @return <B>boolean</B> true/false
	 */
	private boolean isSectionHead(String sStr){
		boolean bIsSection = false;

		for(String sh:sectionHead){
			if(sStr.equals(sh)){
				bIsSection = true;
				break;
			}
		}
		
		return bIsSection;
	}
	
	
	/**
	 * Sub Class for starting the background parsing
	 */
	public class BackgroundParser extends Thread{
		public void run(){
			setName("Background Section Parser");
			parseSections();
			backgroundParser = null;
		}
	}

}