/**
 *  RepDev - RepGen IDE for Symitar
 *  Copyright (C) 2007  Jake Poznanski, Ryan Schultz, Sean Delaney
 *  http://repdev.org/ <support@repdev.org>
 *
 *  This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.repdev;

/**
 * Result returned by SymitarSession after the server has error checked a spec
 * file.
 * 
 * @author Administrator
 * 
 */
public class ErrorCheckResult {
	private String file;
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

	public ErrorCheckResult(String file, String errorMessage, int lineNumber, int column, Type type) {
		this.file = file;
		this.errorMessage = errorMessage;
		this.lineNumber = lineNumber;
		this.column = column;
		this.type = type;
	}
	
	public ErrorCheckResult(String file, String errorMessage, int installSize, Type type) {
		this.file = file;
		this.errorMessage = errorMessage;
		this.lineNumber = -1;
		this.column = -1;
		this.installSize = installSize;
		this.type = type;
	}


	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
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
