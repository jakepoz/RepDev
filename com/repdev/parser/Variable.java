package com.repdev.parser;


public class Variable implements Comparable {
	private String name, filename, type;
	boolean constant;
	private int pos;

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