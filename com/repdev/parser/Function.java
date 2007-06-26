package com.repdev.parser;

import java.util.ArrayList;

public class Function {
	private String name;
	private String description;
	private ArrayList<VariableType> returnTypes = new ArrayList<VariableType>();
	private ArrayList<Argument> arguments = new ArrayList<Argument>();
	
	public Function(String name, String description, ArrayList<VariableType> returnTypes) {
		super();
		this.name = name;
		this.description = description;
		this.returnTypes = returnTypes;
	}

	public ArrayList<Argument> getArguments() {
		return arguments;
	}

	public void setArguments(ArrayList<Argument> arguments) {
		this.arguments = arguments;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<VariableType> getReturnTypes() {
		return returnTypes;
	}

	public void setReturnTypes(ArrayList<VariableType> returnTypes) {
		this.returnTypes = returnTypes;
	}
}