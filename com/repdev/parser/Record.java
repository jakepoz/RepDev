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


public class Record {
	private ArrayList<Record> subRecords = new ArrayList<Record>();
	private ArrayList<Field> fields = new ArrayList<Field>();
	private String description = "";
	private String name = "";
	private Record root;

	public Record(String name, String desc, Record root) {
		this.description = desc;
		this.name = name;
		this.root = root;
	}

	public void setSubRecords(ArrayList<Record> subRecords) {
		this.subRecords = subRecords;
	}

	public ArrayList<Record> getSubRecords() {
		return subRecords;
	}

	public void setFields(ArrayList<Field> fields) {
		this.fields = fields;
	}

	public ArrayList<Field> getFields() {
		return fields;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return "Record: " + name;
	}

	public void setRoot(Record root) {
		this.root = root;
	}

	public Record getRoot() {
		return root;
	}

}
