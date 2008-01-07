package com.repdev.parser;

/**
 * A single keyword from the Repgen language, with extra fields to help with content assist and provide dynamic documentaiton
 * @author Jake Poznanski
 *
 */
public class Keyword {
	public String name, description, example;

	//Also sanitizes the input
	public Keyword(String name, String description, String example) {
		super();
		this.name = name.trim().toUpperCase();
		
		if( description == null )
			this.description = "";
		else
			this.description = description.trim();
		
		if( example == null)
			this.example = "";
		else
			this.example = example.trim();
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getExample() {
		return example;
	}
	
	
	
}
