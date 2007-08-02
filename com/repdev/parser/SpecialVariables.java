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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpecialVariables {
	private static SpecialVariables specialVars = new SpecialVariables();
	
	private ArrayList<SpecialVariable> vars = new ArrayList<SpecialVariable>();
	private HashSet<String> nameCache = new HashSet<String>();
	
	private SpecialVariables(){
		Pattern varPattern = Pattern.compile("(.*)\\|(.*)\\|(.*)\\|(.*)");
		Matcher varMatcher;
		
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("vars.txt")));
			String line;

			while ((line = br.readLine()) != null) {
				line = line.trim();

				if (line.length() != 0){
					varMatcher = varPattern.matcher(line);
					if( varMatcher.matches() ){
						SpecialVariable var = new SpecialVariable(varMatcher.group(1), varMatcher.group(2), varMatcher.group(3), varMatcher.group(4).trim().equals("") ? -1 : Integer.parseInt(varMatcher.group(4)));
						vars.add(var);
						nameCache.add(varMatcher.group(1).toLowerCase());
					}
				}
			}

			br.close();
		} catch (IOException e) {
		}
	}
	
	public ArrayList<SpecialVariable> getVars(){
		return vars;
	}
	
	public boolean contains(String name){
		name = name.toLowerCase();
		
		return nameCache.contains(name);
	}
	
	public static SpecialVariables getInstance(){
		return specialVars;
	}


}
