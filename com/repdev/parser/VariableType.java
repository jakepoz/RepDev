package com.repdev.parser;

public enum VariableType {

	CHARACTER(0), DATE(3), NUMBER(4), MONEY(7), RATE(2), CODE(5), NULL(-1), FLOAT(10), BOOLEAN(100);

	public int code;

	VariableType(int myCode) {
		code = myCode;
	}

}