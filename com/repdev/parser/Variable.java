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


public class Variable implements Comparable {
	private String name, filename, type;
	boolean constant;
	private int pos;
	
	public Variable(Variable old){
		if( old.name != null)
			this.name = new String(old.name);
		
		if( old.filename != null)
			this.filename = new String(old.filename);
		
		if( old.type != null)
			this.type = new String(old.type);
		
		this.constant = old.constant;
		this.pos = old.pos;	
	}

	public Variable(String name, String filename, int pos, String type) {
		this.name = name;
		this.filename = filename;
		this.pos = pos;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getFilename() {
		return filename;
	}

	public int getPos() {
		return pos;
	}

	public void incPos(int amount) {
		pos += amount;
	}

	public String toString() {
		return pos + ":" + name;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Variable))
			return false;

		Variable v = (Variable) o;

		return v.name.equals(name);
	}

	public int compareTo(Object o) {
		if (o instanceof Variable)
			return name.compareToIgnoreCase(((Variable) o).getName());
		else
			return -1;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public boolean isConstant() {
		return constant;
	}

	public void setConstant(boolean constant) {
		this.constant = constant;
	}
}