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

public class Field implements Comparable {
	private String name = "", description = "";
	private int fieldNumber;
	private VariableType variableType;
	private int len;

	public Field(String name, String description, int fieldNumber, VariableType variableType, int len) {
		super();
		this.name = name;
		this.description = description;
		this.fieldNumber = fieldNumber;
		this.variableType = variableType;
		this.len = len;
	}

	public int compareTo(Object o) {
		if (o instanceof Field)
			return name.compareToIgnoreCase(((Field) o).getName());
		else
			return -1;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public VariableType getVariableType() {
		return variableType;
	}

	public void setDataType(VariableType variableType) {
		this.variableType = variableType;
	}

	public int getFieldNumber() {
		return fieldNumber;
	}

	public void setFieldNumber(int fieldNumber) {
		this.fieldNumber = fieldNumber;
	}

	public String toString() {
		return "Field: " + name;
	}

	public void setLen(int len) {
		this.len = len;
	}

	public int getLen() {
		return len;
	}
}