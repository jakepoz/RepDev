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
