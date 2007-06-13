package com.repdev.parser;

import com.repdev.ErrorCheckResult;

public class Error{
	private String fileName = "", description = "";
	private int line = 0, col = 0;
	private Type type;
	
	public enum Type{
		SYMITAR_ERROR,
		WARNING,
		ERROR,
	}
	
	public Error(ErrorCheckResult result){
		this.fileName = result.getFile();
		line = result.getLineNumber();
		col = result.getColumn();
		description = result.getErrorMessage();
		this.type = Type.SYMITAR_ERROR;
	}
	
	public Error( String fileName, String description, int line, int col, Type type) {
		this.fileName = fileName;
		this.type = type;
		this.description = description;
		this.line = line;
		this.col = col;
	}
	
	public Type getType(){
		return type;
	}
	
	
	public void setType(Type type) {
		this.type = type;
	}

	public int getCol() {
		return col;
	}
	public void setCol(int col) {
		this.col = col;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getFile() {
		return fileName;
	}
	public void setFile(String file) {
		this.fileName = file;
	}
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}	
}