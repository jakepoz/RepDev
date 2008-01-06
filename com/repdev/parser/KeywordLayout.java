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

/**
 * Keeps track of the keywords used for syntax highlighting and in program docs
 * @author Jake Poznanski
 *
 */
public class KeywordLayout {
	private static KeywordLayout keywordLayout = new KeywordLayout();
	private HashMap<String, Keyword> keywordMap = new HashMap<String, Keyword>();
	private ArrayList<Keyword> keywordList = new ArrayList<Keyword>();
	
	public KeywordLayout(){
		Pattern wordPattern = Pattern.compile("(.*)\\|(.*)\\|(.*)");
		Matcher wordMatcher;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("keywords.txt")));
			String line;
			
			while ((line = br.readLine()) != null) {
				if (line.length() != 0) {
					
					wordMatcher = wordPattern.matcher(line);
					Keyword word = null;
					
					if( wordMatcher.matches() ){
						word = new Keyword(wordMatcher.group(1), wordMatcher.group(2), wordMatcher.group(3));
					}
					else {
						//Just add a name, no docs here
						word = new Keyword(line,"","");
					}
					
					keywordMap.put(word.name,word);
				}
		
			}

			br.close();
			
			//Sort all alphabeticall right now
			keywordList = new ArrayList<Keyword>(keywordMap.values());
			Collections.sort(keywordList, new Comparator<Keyword>(){
				public int compare(Keyword o1, Keyword o2) {
					return o1.name.toUpperCase().compareTo(o2.name.toUpperCase());
				}			
			});
			
		} catch (IOException e) {
		}
	}
	
	public ArrayList<Keyword> getList(){
		return keywordList;
	}
	
	public boolean contains(String name){
		return keywordMap.containsKey(name.toUpperCase());
	}	
	
	public static KeywordLayout getInstance(){
		return keywordLayout;
	}
	
}
