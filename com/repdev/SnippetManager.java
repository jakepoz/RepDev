package com.repdev;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//Manages snippets, creating a cache, etc
public class SnippetManager {
	public ArrayList<Snippet> snippets = new ArrayList<Snippet>();
	public static SnippetManager singleton = null;
	
	public static SnippetManager getInstance(){
		if( singleton == null)
			singleton = new SnippetManager("snippets");
		
		return singleton;
	}
	
	//Loads all mah snippehts
	public SnippetManager(String snippetFolder){
		File folder = new File(snippetFolder);
		
		if( !folder.exists() || !folder.isDirectory())
			return;
		
		for( File file : folder.listFiles()){
			if( !file.isFile() )
				continue;
			
			String ext =  file.getName().substring(file.getName().lastIndexOf(".")).toLowerCase();
			
			if( ext.equals(".xml") || ext.equals(".snippet"))
			{
				Snippet cur = Snippet.createFromXML(file);
				if( cur == null){
					System.out.println("\n\nWARNING!!!\nSnippet: " + file.getName() + " was not loaded because it contained errors");
				}
				else
					snippets.add(cur);
			}
		}

		Collections.sort(snippets, new Comparator<Snippet>(){
			public int compare(Snippet s1, Snippet s2) {
				return s1.getTitle().toUpperCase().compareTo(s2.getTitle().toUpperCase());
			}
		});
	}
}
