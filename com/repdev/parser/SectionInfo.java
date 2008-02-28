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

/**
 * This class holds infomation for the associated section.
 *
 * @param title The title of the section, ex. PROCEDURE, DEFINE, SETUP...
 * @param pos The position of the Section.
 * @param firstInsertPos The position after the first carriage return following the section
 * @param lastInsertPos The position of the END statement
 */
public class SectionInfo implements Comparable{
	private String title;
	private int pos, firstInsertPos, lastInsertPos;

	/**
	 * This constructor allows you to pass in the SectionInfo object
	 * @param sectionInfo SectionInfo object
	 */
	public SectionInfo(SectionInfo sectionInfo){
		this.title = sectionInfo.title;
		this.pos = sectionInfo.pos;
		this.firstInsertPos = sectionInfo.firstInsertPos;
		this.lastInsertPos = sectionInfo.lastInsertPos;
	}

	/**
	 * Constructor without any parameters
	 */
	public SectionInfo(){
		this.title = "";
		this.pos = -1;
		this.firstInsertPos = -1;
		this.lastInsertPos = -1;
	}

	/**
	 * this constructor allows you to pass in all of the information at once
	 * @param title The title of the section, ex. PROCEDURE, DEFINE, SETUP...
	 * @param pos The position of the Section.
	 * @param firstInsertPos The position after the first carriage return following the section
	 * @param lastInsertPos The position of the END statement for the section
	 */
	public SectionInfo(String title, int pos, int firstInsertPos, int lastInsertPos){
		this.title = title;
		this.pos = pos;
		this.firstInsertPos = firstInsertPos;
		this.lastInsertPos = lastInsertPos;
	}

	/**
	 * Returns the title of the section
	 */
	public String getTitle(){
		return title;
	}

	/**
	 * Returns the position of the section
	 * @return Position of the section
	 */
	public int getPos(){
		return pos;
	}

	/**
	 * Returns the first insert position of the section, which is the position after
	 * the first carriage return following the section title
	 * @return First insertion position
	 */
	public int getFirstInsertPos(){
		return firstInsertPos;
	}

	/**
	 * Returns the position of the END statement for the section
	 * @return Last insertionposition
	 */
	public int getLastInsertPos(){
		return lastInsertPos;
	}

	/**
	 * Returns this object
	 * @return this object
	 */
	public SectionInfo getSectionInfo(){
		return this;
	}

	/**
	 * Sets the title
	 *
	 * @param title Title of the section
	 */
	public void setTitle(String title){
		this.title = title;
	}

	/**
	 * Sets the position of the section
	 *
	 * @param pos Position of the section
	 */
	public void setPos(int pos){
		this.pos = pos;
	}

	/**
	 * Sets the first insertion point
	 *
	 * @param firstInsertPos The first insertion position
	 */
	public void setFirstInsertPos(int firstInsertPos){
		this.firstInsertPos = firstInsertPos;
	}

	/**
	 * Sets the last insertion point
	 *
	 * @param lastInsertPos Last insertion position
	 */
	public void setLastInsertPos(int lastInsertPos){
		this.lastInsertPos = lastInsertPos;
	}

	/**
	 * Sets all the variables for this object
	 *
	 * @param sectionInfo SectionInfo
	 */
	public void setSectionInfo(SectionInfo sectionInfo){
		this.title = sectionInfo.title;
		this.pos = sectionInfo.pos;
		this.firstInsertPos = sectionInfo.firstInsertPos;
		this.lastInsertPos = sectionInfo.lastInsertPos;
	}

	/**
	 * Output a colon delimited list of this object
	 */
	public String toString(){
		return title + ":" + pos + ":" + firstInsertPos + ":" + lastInsertPos;
	}

	/**
	 * Clears the content of this object
	 */
	public void clear(){
		this.title = "";
		this.pos = -1;
		this.firstInsertPos = -1;
		this.lastInsertPos = -1;
	}
	
	public int compareTo(Object arg0) {
		if( !(arg0 instanceof SectionInfo))
			return 0;
			
		SectionInfo arg = (SectionInfo) arg0;
			
		return getTitle().compareToIgnoreCase(arg.getTitle());
	}
}
