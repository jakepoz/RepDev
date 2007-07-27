package com.repdev.parser;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This is the linked list style tree item for the TreeParser
 * @author poznanja
 *
 */
public class TreeItem {
	Token head, end;
	ArrayList<TreeItem> contents;
	
	public TreeItem(Token head){
		this.head = head;
		end = null;
		contents = new ArrayList<TreeItem>();
	}
	
	public TreeItem(Token head, Token end, ArrayList<TreeItem> contents) {
		super();
		this.head = head;
		this.end = end;
		this.contents = contents;
	}

	public String toString(){
		return "Head: "+head+"\nContents: "+Arrays.asList(contents).toString()+"\nEnd: "+end;
	}
	
	public ArrayList<TreeItem> getContents() {
		return contents;
	}

	public void setContents(ArrayList<TreeItem> contents) {
		this.contents = contents;
	}

	public Token getEnd() {
		return end;
	}

	public void setEnd(Token end) {
		this.end = end;
	}

	public Token getHead() {
		return head;
	}

	public void setHead(Token head) {
		this.head = head;
	}
}
