package com.repdev.parser;

import java.util.ArrayList;
import java.util.Stack;

/**
 * So, the RepgenParser is a live, realtime flat parser for tokens in repgens
 * However, this TreeParser creates a full source code tree, but is thread-safe
 * and can be run not in realtime in the background.
 * 
 * It will allow for things like matching do's and ends' etc.
 * 
 * Note: I am just a high school student with no formal CS experience at the time that I'm writing this,
 * I have no idea if this is the proper way to do it or not.
 * 
 * Please correct me if I'm wrong.
 * 
 * TODO: Synchronize and lock for thread safety
 * 
 * @author poznanja
 *
 */
public class TreeParser {
	private ArrayList<TreeItem> items = new ArrayList<TreeItem>();
	private ArrayList<Token> flatTokens = new ArrayList<Token>();
	
	//These strings are the ones that create new head items
	private static final String[] heads = { "target", "setup", "select", "define", "print title", "do", "total", "headers", "(", "\"", "'", "[", "procedure" };
	private static final String[] ends = {"end", ")", "\"", "'", "]"};
	
	public TreeParser(ArrayList<Token> tokenList){
		flatTokens = tokenList;
	}

	public ArrayList<TreeItem> getItems() {
		return items;
	}

	private boolean isHead(Token cur){
		for( String str : heads)
			if( str.toLowerCase().equals(cur.getStr().toLowerCase()))
				return true;
		
		return false;		
	}
	
	private boolean isEnd(Token cur){
		for( String str : ends)
			if( str.toLowerCase().equals(cur.getStr().toLowerCase()))
				return true;
		
		return false;		
	}
	
	public String toString(){
		String toRet = "";
		
		for( TreeItem cur : items)
		 toRet += toStringHandler(cur,"");
		
		return toRet;
	}
	
	public TreeItem getTreeItem(int loc){
		return getTreeItemHandler(items, loc);
	}
	
	private TreeItem getTreeItemHandler(ArrayList<TreeItem> search, int loc){
		for( TreeItem cur : search){
			if( cur.getHead() != null && loc >= cur.getHead().getStart() && loc <= cur.getHead().getEnd())
				return cur;
			
			if( cur.getEnd() != null && loc >= cur.getEnd().getStart() && loc <= cur.getEnd().getEnd())
				return cur;

			TreeItem found = getTreeItemHandler(cur.getContents(),loc);
			
			if( found != null)
				return found;
		}
		
		return null;
	}
	
	private String toStringHandler(TreeItem item, String indent){		
		String cur = "";
		
		if( item.getContents().size() > 0)
			cur += indent + "Head: "+item.getHead()+"\n";
		else
			cur += indent + "Token: "+item.getHead()+"";
		
		for( TreeItem newItem : item.getContents())
			cur += toStringHandler(newItem,indent+"  ") + "\n";
		
		if( item.getEnd() != null)
			cur += indent + "End: "+item.getEnd() + "\n";
		
		
		return cur;
		
	}
	
	/**
	 * Actually regenerates the tree
	 *
	 */
	public void treeParse(){
		synchronized( flatTokens ){
			Stack<TreeItem> openItems = new Stack<TreeItem>();
			items.clear();
					
			for( Token cur : flatTokens){
				if( isHead(cur) && 
					(cur.getCDepth() == 0 || cur.getStr().equals("[")) && 
					( (!cur.inDate() || cur.getStr().equals("'")) && openItems.size() == 0 || !openItems.peek().getHead().getStr().equals("\'") ) && 
					(  (!cur.inString() || cur.getStr().equals("\"")) && openItems.size() == 0 || !openItems.peek().getHead().getStr().equals("\""))
				){
					TreeItem head = new TreeItem( cur );
					openItems.push(head);
					
					if( openItems.size() == 1)
						items.add(head);
					else
						openItems.get(openItems.size()-2).getContents().add(head);
				}
				else if( isEnd( cur ) && (cur.getCDepth() == 0 || cur.getStr().equals("]")) && (!cur.inDate() || cur.getStr().equals("'")) && (!cur.inString() || cur.getStr().equals("\"")) && openItems.size() > 0){
					TreeItem toEnd = openItems.pop();
					toEnd.setEnd(cur);
				}
				else{
					if( openItems.size() == 0){
						TreeItem head = new TreeItem(cur);
						items.add(head);
					}
					else{
						openItems.peek().getContents().add(new TreeItem(cur));
					}
				}
			}
			
			
			//System.out.println(this);
		}
	}
}
