package com.repdev.parser;


public class Include{
	private String fileName;
	public Division division;
	
	public Include(String fileName, Division division) {
		super();
		this.fileName = fileName;
		this.division = division;
	}
	
	public Division getDivision() {
		return division;
	}
	
	public void setDivision(Division division) {
		this.division = division;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}	
}
