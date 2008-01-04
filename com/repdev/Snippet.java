package com.repdev;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Represents a single code snippet, also has a ton of helper and loading functions
 * @author poznanja
 *
 */
public class Snippet {
	String title, description, author, shortcut;
	boolean expands, surroundsWith;
	String snippet, originalSnippet, selection;
	private static final char ESC = (char)27;
	
	private ArrayList<SnippetVariable> vars = new ArrayList<SnippetVariable>();
	
	public Snippet(String title, String description, String author, String shortcut, boolean expands, boolean surroundsWith, String snippet) {
		super();
		this.title = title;
		this.description = description;
		this.author = author;
		this.shortcut = shortcut;
		this.expands = expands;
		this.surroundsWith = surroundsWith;
		this.snippet = snippet;
	}
	
	public static Snippet createFromXML( File xmlFile){
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		Element head, header, types, declarations, snippet;
		String title, description, author, shortcut, snippetStr;
		boolean expands = false, surroundsWith = false;
		ArrayList<SnippetVariable> vars = new ArrayList<SnippetVariable>();
		
		try {
			db = dbf.newDocumentBuilder();
			Document d = db.parse(xmlFile);
			
			//Get to first CodeSnippet part
			head = (Element)d.getElementsByTagName("CodeSnippet").item(0);
			
			header = (Element)head.getElementsByTagName("Header").item(0);
			title = header.getElementsByTagName("Title").item(0).getTextContent();
			description = header.getElementsByTagName("Description").item(0).getTextContent();
			author = header.getElementsByTagName("Author").item(0).getTextContent();
			shortcut = header.getElementsByTagName("Shortcut").item(0).getTextContent();
			
			types = (Element)header.getElementsByTagName("SnippetTypes").item(0);
			
			for( int i = 0; i < types.getElementsByTagName("SnippetType").getLength(); i++){
				Node cur =  types.getElementsByTagName("SnippetType").item(i);
				
				if( cur.getTextContent().equals("Expansion"))
					expands = true;
				else if( cur.getTextContent().equals("SurroundsWith"))
					surroundsWith = true;
			}
			
			snippet = (Element)head.getElementsByTagName("Snippet").item(0);
			declarations = (Element)snippet.getElementsByTagName("Declarations").item(0);
			
			for( int i = 0; i < declarations.getElementsByTagName("Literal").getLength(); i++){
				Element cur = (Element)declarations.getElementsByTagName("Literal").item(i);
				SnippetVariable var = new SnippetVariable(cur.getElementsByTagName("ID").item(0).getTextContent(), cur.getElementsByTagName("ToolTip").item(0).getTextContent(), cur.getElementsByTagName("Default").item(0).getTextContent().trim());

				vars.add(var);				
			}
			
			snippetStr = ((Element)snippet.getElementsByTagName("Code").item(0)).getTextContent();

			Snippet mySnippet = new Snippet(title, description,author,shortcut, expands, surroundsWith,snippetStr);
			mySnippet.setVars(vars);
			mySnippet.cleanup();
		
			return mySnippet;
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		} catch (SAXException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	
	//Called to clean up some of the snippet text, requires the variables to be loaded already
	private void cleanup(){
		StringBuilder ret = new StringBuilder();
		String snippet = new String(this.snippet);
		
		snippet = snippet.replace("$end$", "");
		
		//To allow users to use $'s as the variable marker, but also as nonescaped text in their snippet,
		//We will replace the $'s used as markers to somethign else
		for(SnippetVariable var : vars){
			snippet = snippet.replace("$" + var.getId() + "$", ESC + var.getId() + ESC);
		}
		
		this.originalSnippet = snippet;
	}
	
	
	//Sets up a snippet before we start using it in an editor
	public void setup(){
		for(SnippetVariable var : vars)
			var.reset();
	}
	
	
	/**
	 * Returns the snippet text, with the variables replaced
	 * @return
	 */
	public String getReplacedText(String selection, String indent){
		if( selection != null){
			this.selection = selection;
			//Remove extra variables that we don't want
			snippet = originalSnippet.replace("$selected$", selection);
		}
		
		//Do indentation
		if( indent != null){
			boolean lastEsc = false;
			
			if( snippet.lastIndexOf(ESC) != -1 ){
				lastEsc = true;
				
				for( int x = snippet.lastIndexOf(ESC); x<snippet.length();x++){
					if( !Character.isWhitespace(snippet.charAt(x)) && snippet.charAt(x) != ESC){
						lastEsc = false;
						break;
					}
				}
			}
				
			
			snippet = snippet.trim(); //Stupid Trim also trims ESC chars
			snippet = snippet.replace("\n", "\n" + indent);
			
			if( lastEsc )
				snippet = snippet + ESC;
		}
		
		String ret = snippet;

		
		for( SnippetVariable var : vars)
			ret = ret.replace(ESC + var.getId() + ESC, var.getValue());
		
		return ret;
	}

	
	//Replaces all the text except the one var we pass in
	public String getReplaceVarText(int varPos){
		String ret = snippet;
		SnippetVariable searchVar = getVar(varPos);
		
		for( SnippetVariable var : vars)
			if( var != searchVar)
				ret = ret.replace(ESC+var.getId()+ESC, var.getValue());
		
		return ret;
	}
	
	//Returns array, alternating start pos, length
	public ArrayList<Integer> getLocations(int varPos){
		ArrayList<Integer> ret = new ArrayList<Integer>();
		SnippetVariable var = getVar(varPos);
		
		if( var == null )
			return null;
		
		String varSearch = ESC+var.getId()+ESC;
		String temp = getReplaceVarText(varPos);
		
		int offset = 0;
		
		while(temp.length() > 0){
			int cur = temp.indexOf(varSearch);
			
			if( cur > -1){
				ret.add(cur + offset);
				ret.add(getVar(varPos).getValue().length());
			}
			else
				break;
			
			offset += temp.indexOf(varSearch)+var.getValue().length();
			temp = temp.substring(temp.indexOf(varSearch)+varSearch.length());
		}
		
		return ret;
	}
	
	//Gets the start position of essentially the first place that variable appears
	public int getVarEditPos(int varPos){
		ArrayList<Integer> temp =  getLocations(varPos);
		
		if( temp != null)
			return temp.get(0);
		else
			return -1;
	}
	
	public SnippetVariable getVar(int varPos){
		String temp = new String(snippet);
		ArrayList<String> seenVars = new ArrayList<String>();
		String var;
		int count = 0;
		
		while(temp.length() > 0){
			if( temp.indexOf(ESC) == -1)
				break;
			
			temp = temp.substring(temp.indexOf(ESC)+1);
			
			if( temp.indexOf(ESC) == -1)
				break;
			
			var = temp.substring(0, Math.min(temp.length(),temp.indexOf(ESC)));
			if( !seenVars.contains(var)){
				seenVars.add(var);
				count++;
			}
			
			if( count - 1 == varPos ){
				for( SnippetVariable cur : vars){
					if( cur.getId().equals(var))
						return cur;
				}
				
				return null;
			}
				
			temp = temp.substring(temp.indexOf(ESC)+1);
		}
		
		return null;
	}
	
	/**
	 * Returns number of actual used unique vars
	 * @return
	 */
	public int getNumberOfUniqueVars(){
		String temp = new String(snippet);
		ArrayList<String> seenVars = new ArrayList<String>();
		String var;
		int count = 0;
		
		while(temp.length() > 0){
			if( temp.indexOf(ESC) == -1)
				break;
			
			temp = temp.substring(temp.indexOf(ESC)+1);
			
			if( temp.indexOf(ESC) == -1)
				break;
			
			var = temp.substring(0,temp.indexOf(ESC));
			
			if( !seenVars.contains(var)){
				boolean inData = false;
				
				//Don't allow variables that aren't described in the data structure
				for( SnippetVariable sVar : vars)
					if( sVar.getId().equals(var)){
						inData = true;
						break;
					}
				
				if( inData){
					seenVars.add(var);
					count++;
				}
			}
			
			temp = temp.substring(temp.indexOf(ESC)+1);
		}
		
		return seenVars.size();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getShortcut() {
		return shortcut;
	}

	public void setShortcut(String shortcut) {
		this.shortcut = shortcut;
	}

	public boolean isExpands() {
		return expands;
	}

	public void setExpands(boolean expands) {
		this.expands = expands;
	}

	public boolean isSurroundsWith() {
		return surroundsWith;
	}

	public void setSurroundsWith(boolean surroundsWith) {
		this.surroundsWith = surroundsWith;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	public void setVars(ArrayList<SnippetVariable> vars) {
		this.vars = vars;
	}

	public ArrayList<SnippetVariable> getVars() {
		return vars;
	}
	
	
}
