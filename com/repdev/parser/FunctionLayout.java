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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Loads and provides fast helper methods for function content assist type stuff
 * 
 * @author Jake Poznanski
 * 
 */
public class FunctionLayout {
	private static FunctionLayout functionLayout = new FunctionLayout();
	private HashMap<String, Function> functionMap = new HashMap<String, Function>();
	private ArrayList<Function> functionList = new ArrayList<Function>();

	private FunctionLayout() {
		Pattern funcPattern = Pattern.compile("(.*)\\|(.*)\\|(.*)");
		Matcher funcMatcher;

		Pattern argPattern = Pattern.compile("\\t(.*)\\|(.*)\\|(.*)");
		Matcher argMatcher;

		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("functions.txt")));
			String line;
			Function cur = null;

			while ((line = br.readLine()) != null) {
				if (line.trim().length() != 0) {

					argMatcher = argPattern.matcher(line);
					funcMatcher = funcPattern.matcher(line);

					if( argMatcher.matches() && cur != null){		
						ArrayList<VariableType> types = new ArrayList<VariableType>();
						String[] typeNames = argMatcher.group(3).split(",");

						for( String name : typeNames)
							types.add(VariableType.valueOf(name.trim().toUpperCase()));

						cur.getArguments().add(new Argument(argMatcher.group(1), argMatcher.group(2),types));							
					}
					else if( funcMatcher.matches() ){
						ArrayList<VariableType> types = new ArrayList<VariableType>();
						String[] typeNames = funcMatcher.group(3).split(",");

						for( String name : typeNames)
							types.add(VariableType.valueOf(name.toUpperCase()));

						cur = new Function( funcMatcher.group(1), funcMatcher.group(2), types);
						functionMap.put(cur.getName().toLowerCase(), cur);
					}
					else{
						//Just add whatever is on the line as the fucniton name, leaving the rest blank
						
						cur = new Function( line.trim(),"",new ArrayList<VariableType>());
						
						if( functionMap.get(cur.getName().toLowerCase()) != null)
							System.out.println("Duplicate Function Entry: " + cur.getName());
						else
							functionMap.put(cur.getName().toLowerCase(), cur);
					}
				}
			}

			br.close();

			//Sort all alphabetical right now
			functionList = new ArrayList<Function>(functionMap.values());
			Collections.sort(functionList, new Comparator<Function>(){
				public int compare(Function o1, Function o2) {
					return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
				}			
			});

		} catch (IOException e) {
		}
	}

	public ArrayList<Function> getList(){
		return functionList;
	}

	public boolean containsName(String name){
		return functionMap.containsKey(name.toLowerCase());
	}

	public static FunctionLayout getInstance(){
		return functionLayout;
	}
}
