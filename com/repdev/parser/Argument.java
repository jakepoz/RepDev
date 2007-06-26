package com.repdev.parser;

import java.util.ArrayList;

public class Argument {
	private String shortName, description;
	private ArrayList<VariableType> types = new ArrayList<VariableType>();
	
	public Argument(String shortName, String description, ArrayList<VariableType> types) {
		super();
		this.shortName = shortName;
		this.description = description;
		this.types = types;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public ArrayList<VariableType> getTypes() {
		return types;
	}

	public void setTypes(ArrayList<VariableType> types) {
		this.types = types;
	}
}