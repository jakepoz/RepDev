package com.repdev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpecialVariables {
	private static SpecialVariables specialVars = new SpecialVariables();
	
	private ArrayList<SpecialVariable> vars = new ArrayList<SpecialVariable>();
	private HashSet<String> nameCache = new HashSet<String>();
	
	public class SpecialVariable{
		String name, description, type;
		int len;

		public SpecialVariable(String name, String description, String type, int len) {
			super();
			this.name = name;
			this.description = description;
			this.type = type;
			this.len = len;
		}

		public SpecialVariable(String name, String description, String type) {
			super();
			this.name = name;
			this.description = description;
			this.type = type;
		}

		public int getLen() {
			return len;
		}

		public void setLen(int len) {
			this.len = len;
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

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
		
		
	}
	
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
