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