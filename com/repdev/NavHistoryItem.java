package com.repdev;

public class NavHistoryItem {
	private SymitarFile file;
	private int lineNumber;
	public NavHistoryItem(SymitarFile file, int lineNumber){
		this.file = file;
		this.lineNumber = lineNumber;
	}
	SymitarFile getFile() {
		return file;
	}
	int getLineNumber() {
		return lineNumber;
	}
}
