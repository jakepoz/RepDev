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

package com.repdev.parser;


/**
 * Todo items handling...
 * @author ryan
 *
 */

public class Task{
    private String fileName = "", description = "";
    private int line = 0, col = 0;
    private Type type;

    public enum Type{
	TODO,
	FIXME,
	BUG,
	WTF,
	BM,
	TEST,
	NOTE
    }
    
    public Task( String fileName, String description, int line, int col, Type type) {
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