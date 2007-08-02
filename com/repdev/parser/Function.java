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