package com.repdev;

/**
 * Result returned by SymitarSession after the server has error checked a spec
 * file.
 * 
 * @author Administrator
 * 
 */
public class ErrorCheckResult {
	private String errorMessage;
	private int lineNumber;
	private int column;
	private Type type;
	private int installSize;
	
	public enum Type{
		ERROR,
		WARNING,
		NO_ERROR,
		INSTALLED_SUCCESSFULLY,
	}

	public ErrorCheckResult() {

	}

	public ErrorCheckResult(String errorMessage, int lineNumber, int column, Type type) {
		this.errorMessage = errorMessage;
		this.lineNumber = lineNumber;
		this.column = column;
		this.type = type;
	}
	
	public ErrorCheckResult(String errorMessage, int installSize, Type type) {
		this.errorMessage = errorMessage;
		this.lineNumber = -1;
		this.column = -1;
		this.installSize = installSize;
		this.type = type;
	}


	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public int getLineNumber() {
		return lineNumber;
	}
	
	public Type getType(){
		return type;
	}
	
	public void setType(Type newType){
		type=newType;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public int getColumn() {
		return column;
	}

	public void setInstallSize(int installSize) {
		this.installSize = installSize;
	}

	public int getInstallSize() {
		return installSize;
	}
}
