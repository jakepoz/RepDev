package com.repdev.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
				if (line.length() != 0) {
					
					argMatcher = argPattern.matcher(line);
					
					if( argMatcher.matches() ){
						if (argMatcher.matches() && cur != null) {
							ArrayList<VariableType> types = new ArrayList<VariableType>();
							String[] typeNames = argMatcher.group(3).split(",");
							
							for( String name : typeNames)
								types.add(VariableType.valueOf(name.trim().toUpperCase()));
							
							cur.getArguments().add(new Argument(argMatcher.group(1), argMatcher.group(2),types));							
						}
					}
					else {
						funcMatcher = funcPattern.matcher(line);
						
						if( funcMatcher.matches() ){
							ArrayList<VariableType> types = new ArrayList<VariableType>();
							String[] typeNames = funcMatcher.group(3).split(",");
							
							for( String name : typeNames)
								types.add(VariableType.valueOf(name.toUpperCase()));
							
							cur = new Function( funcMatcher.group(1), funcMatcher.group(2), types);
							functionMap.put(cur.getName().toLowerCase(), cur);
						}
					}
					
				}
		
			}

			br.close();
			
			//Sort all alphabeticall right now
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
