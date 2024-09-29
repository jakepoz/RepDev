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
//TODO:FIX PARSER BUG WITH "#" HIGHLIGHTING
package com.repdev.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.repdev.EditorComposite;
import com.repdev.ErrorCheckResult;
import com.repdev.FileType;
import com.repdev.RepDevMain;
import com.repdev.SymitarFile;
import com.repdev.parser.Token.TokenType;

public class RepgenParser {
	private StyledText txt;
	private SymitarFile file;
	private int sym;
	private boolean reparse = true;

	private static DatabaseLayout db = DatabaseLayout.getInstance();
	private static SpecialVariables specialvars = SpecialVariables.getInstance();
	private static FunctionLayout functions = FunctionLayout.getInstance();
	private static KeywordLayout keywords = KeywordLayout.getInstance();

	private ArrayList<Token> ltokens = new ArrayList<Token>();
	private ArrayList<Variable> lvars = new ArrayList<Variable>();

	private ArrayList<Token> lasttokens = new ArrayList<Token>(); //Tokens added in last parse method call
	private ArrayList<Token> removedtokens = new ArrayList<Token>(); //Tokens removed
	private ArrayList<Include> includes = new ArrayList<Include>();

	private ArrayList<Error> errorList = new ArrayList<Error>();
	private ArrayList<Task> taskList = new ArrayList<Task>();

	private HashMap<String,ArrayList<Token>> includeTokenChache = new HashMap<String, ArrayList<Token>>();

	BackgroundSymitarErrorChecker errorCheckerWorker = null;
	BackgroundIncludeParser includeParserWorker = null;

	boolean initialIncludeParseNeeded = true; //This will make sure that we parse the includes at least once when the file is first opened
	boolean refreshIncludes = false; //The parser will keep track of changes as the file is edited, and if an include reparse is needed, this flag will be set. 
	//Since include parsing is resource intensive, it's up to the rest of the code to decide when to parse these if needed. (Usually on file save)
	private boolean noParse = false;
	public static final String[] taskTokens = { "todo", "fixme", "bug", "bugbug", "wtf", "bm", "bookmark", "test", "note" };


	public RepgenParser(StyledText txt, SymitarFile file) {
		this.txt = txt;
		this.file = file;
		this.sym = file.getSym();
	}


	public RepgenParser(StyledText txt, SymitarFile file, boolean parseFlag) {
		this.txt = txt;
		this.file = file;
		this.sym = file.getSym();
		noParse=!parseFlag;
	}


	/**
	 * Worker class for loading any included files and parsing out their contents
	 * Currently runs once at opening of the report
	 * @author poznanja
	 *
	 */
	public class BackgroundIncludeParser extends Thread{
		String text;

		public BackgroundIncludeParser(String text){
			super("Background Include Parser");
			this.text = text;
		}

		private void parseCurrentFileAsInclude(String fileName, boolean inDefs){
			boolean exists = false;
			ArrayList<Token> tokens = new ArrayList<Token>();
			String data = "";

			includes.add(new Include(fileName, inDefs ? Division.DEFINE : Division.NONE));

			if( file.isLocal() )
				data = new SymitarFile(file.getDir(),fileName).getData();
			else
				data = new SymitarFile(sym,fileName,FileType.REPGEN).getData();

			if( data == null )
				return;

			parse(fileName, data, 0, data.length(), 0, null, tokens, new ArrayList<Token>(), new ArrayList<Token>(), new ArrayList<Variable>(),null);
			
			includeTokenChache.put(fileName,tokens);

			for( Token tok : tokens ){
				tok.setInDefs(inDefs);

				if( tok.getStr().equals("#include") && tok.getAfter() != null && tok.getCDepth() == 0){
					String newFileName = getFullString(tok.getAfter(),data);

					exists = false;

					for( Include cur : includes ){
						if(cur.getFileName().equals(newFileName))
							exists=true;
					}

					if( !exists && !newFileName.equals(file.getName()) ){
						parseCurrentFileAsInclude(newFileName, inDefs);
					}
				}
			}

			if( inDefs )
				rebuildVars(fileName,data,tokens);
			//txt.notifyListeners(getSym(), null);
		}

		public void run(){
			boolean exists = false;
			ArrayList<Token> tempTokens = new  ArrayList<Token>();


			synchronized(includeTokenChache){//Sync it on the token cache, so other threads can access it safely

				cleanupTokenCache();
				
				for( Token tok : ltokens){
					tempTokens.add(new Token(tok));
					//tempTokens.add(tok);
				}

				int i = 0;

				for( Token tok : tempTokens){
					tok.setNearTokens(tempTokens, i);
					i++;
				}


				//Only run next level of parsing on the include files not including the current file
				//Variables in the current file are handled seperately

				for( Token tok : tempTokens ){
					if( tok.getStr().equals("#include") && tok.getAfter() != null && tok.getCDepth() == 0){
						String fileName = getFullString(tok.getAfter(),text);

						exists = false;

						for( Include cur : includes ){
							if(cur.getFileName().equals(fileName))
								exists=true;
						}

						if( !exists && !fileName.equals(file.getName()) ){
							parseCurrentFileAsInclude(fileName, tok.inDefs());
						}
					}
				}
			}

			includeParserWorker = null;
		}
	}

	/**
	 * Warning: Doesn't save symitar file
	 * 
	 * Can be called to validate
	 * 
	 * @author poznanja
	 * 
	 */
	public class BackgroundSymitarErrorChecker extends Thread {
		RepgenParser me;

		public BackgroundSymitarErrorChecker(RepgenParser instance) {
			super("Background Error Checker");
			me = instance;
		}

		public void run() {
			final Table tblErrors = RepDevMain.mainShell.getErrorTable();
			final Table tblTasks  = RepDevMain.mainShell.getTaskTable();
			ArrayList<Variable> varCache = new ArrayList<Variable>();

			if (tblErrors.isDisposed())
				return;

			try {
				Display display = tblErrors.getDisplay();

				// Remove old errors
				errorList.clear();
				taskList.clear();

				// Error check with symitar
				// Only check errors if File name does not end with .PRO, .SET, .DEF, .INC
				String[] extensionsToExclude = com.repdev.Config.getNoErrorCheckSuffix().split(",");
				boolean checkFile = true;
				if(extensionsToExclude[0].length() != 0){
					for(String extension : extensionsToExclude){
						if(file.getName().endsWith(extension))
							checkFile = false;
					}
				}
				//System.out.println(com.repdev.Config.getNoErrorCheckPrefix());
				extensionsToExclude = com.repdev.Config.getNoErrorCheckPrefix().split(",");
				if(extensionsToExclude[0].length() != 0){
					for(String extension : extensionsToExclude){
						if(file.getName().startsWith(extension))
						checkFile = false;
					}
				}
				if(checkFile){
					ErrorCheckResult result = RepDevMain.SYMITAR_SESSIONS.get(sym).errorCheckRepGen(file.getName());
					errorList.add(new Error(result));
				}


				// Variable checking
				synchronized(lvars){				
					//Duplicate variables
					for (final Variable var : lvars) {
						varCache.add(new Variable(var));

						if (var.getFilename().equals(file.getName())) {
							int count = 0;

							for (Variable var2 : lvars)
								if (var2.equals(var))
									count++;

							if (count > 1 && !tblErrors.isDisposed())
								display.syncExec(new Runnable() {
									public void run() {
										if (!txt.isDisposed())
											errorList.add(new Error(file.getName(), "Duplicate variable name: " + var.getName().toUpperCase(), txt.getLineAtOffset(var.getPos()) + 1, var.getPos() - txt.getOffsetAtLine(txt.getLineAtOffset(var.getPos())) + 1,Error.Type.WARNING));
									}
								});
						}
					}
				}


				synchronized(includeTokenChache){
					//unused var checking
					for (final Variable var : lvars) {	
						if( !var.getFilename().equals(file.getName()) )
							continue;

						boolean unused = true;

						for( int i = 0; i < ltokens.size(); i++){
							Token tok = ltokens.get(i);

							if( tok.inDefs() || tok.inDate() || tok.inString() || tok.getCDepth() > 0)
								continue;

							if( RepgenParser.getKeywords().contains(tok.getStr()) || RepgenParser.getSpecialvars().contains(tok.getStr()))
								continue;

							if( var.getName().equals(tok.getStr()) ){
								unused = false;
								break;
							}
						}

						for( ArrayList<Token> tokens : includeTokenChache.values()){
							for( Token tok : tokens){
								if( tok.inDefs() || tok.inDate() || tok.inString() || tok.getCDepth() > 0)
									continue;

								if( RepgenParser.getKeywords().contains(tok.getStr()) || RepgenParser.getSpecialvars().contains(tok.getStr()))
									continue;

								if( var.getName().equals(tok.getStr()) )
									unused = false;
							}
						}

						if( unused && !tblErrors.isDisposed()){
							display.syncExec(new Runnable() {
								public void run() {
									if (!txt.isDisposed() && com.repdev.Config.getListUnusedVars()){
										errorList.add(new Error(var.getFilename(), "Variable Unused: " + var.getName().toUpperCase(), txt.getLineAtOffset(var.getPos()) + 1, var.getPos() - txt.getOffsetAtLine(txt.getLineAtOffset(var.getPos())) + 1,Error.Type.WARNING));
									}
								}
							});
						}
					}
					// Redraw Main Screen after background variables are parsed
					RepDevMain.mainShell.getShell().getDisplay().asyncExec( //can use asyncExec() or syncExec()
							  new Runnable() {
							    public void run(){
							    	if(!txt.isDisposed())
							    		txt.redraw();
							    }
							  });

				}


				// Add to list
				display.asyncExec(new Runnable() {
					public void run() {
						if (!tblErrors.isDisposed()) {
							for (TableItem item : tblErrors.getItems()) {
								if (((SymitarFile) item.getData("file")).equals(file) && ((Integer) item.getData("sym")) == sym)
									item.dispose();

							}

							for (Error error : errorList) {
								if (!error.getDescription().trim().equals("")) {
									TableItem row = new TableItem(tblErrors, SWT.NONE);
									row.setText(0, error.getDescription());
									row.setText(1, error.getFile());

									if (error.getLine() >= 0 && error.getCol() >= 0)
										row.setText(2, String.valueOf(error.getLine()) + " : " + error.getCol());
									else
										row.setText(2, "---");

									row.setData("file", file);
									row.setData("sym", sym);
									row.setData("error", error);

									if( error.getType() == Error.Type.SYMITAR_ERROR )
										row.setImage(RepDevMain.smallErrorsImage);
									else
										row.setImage(RepDevMain.smallWarningImage);				    
								}
							}

							for( CTabItem tab: ((CTabFolder)tblErrors.getParent()).getItems() ) {
								if( tab.getText().indexOf("Errors") != -1 ) {
									tab.setText("&Errors (" + tblErrors.getItemCount() + ")");
								}
							}

						}
					}
				});

				display.syncExec(new Runnable() {
					public void run() {
						try {
							for (final Token tok : ltokens) {
								boolean isTask = false;
								for( String task: taskTokens )
									if( tok.getStr().equals(task)) isTask = true;
	
								if ( tok.getCDepth() > 0 && isTask && ( tok.getAfter()!=null ) && tok.getAfter().getStr().equals(":")) {
									int line = txt.getLineAtOffset(tok.getStart());
									int col = tok.getStart() - txt.getOffsetAtLine(line);
									String desc = txt.getText(tok.getStart(), txt.getOffsetAtLine(line+1)-1);
	
	
									desc = desc.trim();
									desc = desc.replaceAll("\\]$", "");
	
									Task.Type type;
									type = Task.Type.TODO;
									if( tok.getStr().equals("fixme") ) {
										type = Task.Type.FIXME;
									} else if( tok.getStr().equals("bug") || tok.getStr().equals("bugbug") ) {
										type = Task.Type.BUG;
									} else if( tok.getStr().equals("wtf") ) {
										type = Task.Type.WTF;
									} else if( tok.getStr().equals("bm") || tok.getStr().equals("bookmark") ) {
										type = Task.Type.BM;
									} else if( tok.getStr().equals("test") ) {
										type = Task.Type.TEST;
									} else if( tok.getStr().equals("note") ) {
										type = Task.Type.NOTE;
 									}

	
	
									/* Don't die if the item does not have a line following it...
									 * Taken from my #include "" double click code.
									 */
									int startOffset = tok.getStart();
									int pos1 = txt.getText().toString().indexOf("\n",startOffset + tok.getStr().length()) + 1;
									int pos2 = txt.getText().toString().indexOf("]",startOffset + tok.getStr().length()) - 1;
									int endOffset = (pos1<pos2 ? pos1 : pos2);
	
	
									if( endOffset - 1 <= startOffset)
										desc = "";
									else
										desc = txt.getText(startOffset, endOffset);
									desc = desc.trim();
	
									Task task = new Task(file.getName(), desc, line, col, type);
									taskList.add( task );
								}
							}
						} catch (Exception e) {
							// TODO: handle exception (Places TC here to fix a crash when a repgen being parsed is closed)
						}
					}
				});

				// Update the tasks table
				display.asyncExec(new Runnable() {
					public void run() {
						if (!tblTasks.isDisposed()) {
							for (TableItem item : tblTasks.getItems()) {
								if (((SymitarFile) item.getData("file")).equals(file) && ((Integer) item.getData("sym")) == sym)
									item.dispose();

							}


							for( Task task : taskList ) {
								if( !task.getDescription().trim().equals("")) {
									TableItem row = new TableItem(tblTasks, SWT.NONE );
									row.setText(0, task.getDescription() );
									row.setText(1, task.getFile() );
									row.setText(2, task.getLine() + " : " + task.getCol() );

									row.setData("file", file);
									row.setData("sym", sym);
									row.setData("task", task);

									if( task.getType() == Task.Type.TODO ) {
										row.setImage(RepDevMain.smallTaskTodo);
									} else if( task.getType() == Task.Type.FIXME ) {
										row.setImage(RepDevMain.smallTaskFixme);
									} else if( task.getType() == Task.Type.BUG ) {
										row.setImage(RepDevMain.smallTaskBug);
									} else if( task.getType() == Task.Type.WTF ) {
										row.setImage(RepDevMain.smallTaskWtf);
									} else if( task.getType() == Task.Type.BM ) {
										row.setImage(RepDevMain.smallTaskBookmark);
									} else if( task.getType() == Task.Type.TEST ) {
										row.setImage(RepDevMain.smallTaskTest);
									} else if( task.getType() == Task.Type.NOTE ) {
										row.setImage(RepDevMain.smallTaskNote);
 									}

								}
							}

							for( CTabItem tab: ((CTabFolder)tblTasks.getParent()).getItems() ) {
								if( tab.getText().indexOf("Tasks") != -1 ) {
									tab.setText("&Tasks (" + tblTasks.getItemCount() + ")");
								}
							}
							
						}
					}
				});
			} catch (Exception e) {
				//Just ignore if anything happens to our UI while we are error checking.
				System.out.println("Background Error Checker has failed, but recovered:\n");
				e.printStackTrace();
			}

			errorCheckerWorker = null;
		}
	}


	/**
	 * Adds tokens to ltokens, but also keeps track of it in the new tokens list
	 * for determining what to process later
	 * 
	 * @param spot
	 * @param tok
	 */
	private void addToken(ArrayList<Token> tokens, int spot, Token tok){
		if(spot > 0 && tokens.get(spot-1).getStr().equals("procedure") && !tok.inString() && tok.getCDepth() == 0)
			tok.setTokenType(TokenType.PROCEDURE); //TODO: add other types
		if(spot > 0 && tok.getStr().equalsIgnoreCase("=") && tok.inDefs() && !tok.inString() && tok.getCDepth() == 0)
			tokens.get(spot-1).setTokenType(TokenType.DEFINED_VARIABLE);
		tokens.add(spot,tok);
		lasttokens.add(tok);
	}

	/**
	 * Parses given file and data into tokens, also uses current token lists and usually only replaces certain sections of the
	 * token list.
	 * 
	 * Doesn't do any error checking, or anything like that
	 * 
	 * @param filename
	 * @param str
	 * @param start
	 * @param end
	 * @param replacedText
	 * @param oldend
	 * @param tokens
	 * @param vars
	 * @param txt
	 * @return
	 * 
	 * TODO: Type asdf" then going back and putting a " in the start of the string doesn't redraw the file after the second "
	 */

	private synchronized boolean parse(String filename, String str, int start, int end, int oldend, String replacedText, ArrayList<Token> tokens, ArrayList<Token> lasttokens, ArrayList<Token> removedtokens, ArrayList<Variable> vars, StyledText txt) {
		boolean allDefs = true, redrawAll = false;
		lasttokens.clear();
		removedtokens.clear();

		int ftoken;
		for(ftoken = 0; ftoken < tokens.size(); ftoken++)
			if(tokens.get(ftoken).getEnd()>=start)
				break;

		int ltoken;
		for(ltoken = ftoken; ltoken < tokens.size(); ltoken++)
			if(tokens.get(ltoken).getStart()>oldend)
				break;

		int charStart, charEnd;

		if(ftoken<tokens.size())
			charStart = Math.min(start, tokens.get(ftoken).getStart());
		else
			charStart = start;

		if(ltoken<tokens.size())
			charEnd = Math.max(end, tokens.get(ltoken).getStart()+end-oldend);
		else
			charEnd = str.length();

		char[] chars = str.substring(charStart, charEnd).toLowerCase().toCharArray();

		boolean inString=false, inDate=false, inDefine = false, inSetup = false;
		int commentDepth=0;
		if(ftoken>0){
			inString = tokens.get(ftoken-1).endInString();
			inDate = tokens.get(ftoken-1).getEndInDate();
			commentDepth = tokens.get(ftoken-1).getEndCDepth();
			inDefine = tokens.get(ftoken-1).inDefs();
		}

		boolean oldInString=false, oldInDefine = false, oldInDate=false;
		int oldCommentDepth=0;
		if(ltoken<tokens.size()) {
			oldInString=tokens.get(ltoken).inString();
			oldInDate=tokens.get(ltoken).inDate();
			oldCommentDepth=tokens.get(ltoken).getCDepth();
			oldInDefine=tokens.get(ltoken).inDefs();
		}

		if( inDefine || oldInDefine )
			redrawAll = true;

		if(tokens.size()>0)
			for(int i=0;i<ltoken-ftoken;i++){
				removedtokens.add(tokens.remove(ftoken));
			}

		int curspot = ftoken, cstart = charStart;
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<chars.length; i++) {
			char cur = chars[i];

			if((cur>='a'&&cur<='z')||(cur>='0'&&cur<='9')||cur=='#'||cur=='@') {
				sb.append(cur);
			} else {
				String scur=sb.toString().trim();

				if(scur.length()>0){			
					if(commentDepth==0 && !inString) {
						if(scur.equals("define")) {
							inDefine = true;
						} else if(scur.equals("setup")){
							inSetup = true;
						} else if(scur.equals("end")) {
							inDefine = false;
							inSetup = false;
							allDefs = false;
						}
					}

					addToken(tokens,curspot,new Token(scur,cstart,commentDepth,commentDepth,inString,inString,inDefine,inDate,inDate));
					curspot++;
				}
				sb = new StringBuilder();
				cstart = i + charStart;

				scur = "" + cur;
				if(commentDepth==0 && cur=='"') {
					if(inString)
						addToken(tokens,curspot,new Token(scur,cstart,0,0,true,false,inDefine,inDate,inDate));
					else
						addToken(tokens,curspot,new Token(scur,cstart,0,0,true,true,inDefine,inDate,inDate));

					curspot++;
					inString = !inString;
				} else if(!inString && cur=='[') {
					commentDepth++;
					addToken(tokens,curspot,new Token(scur,cstart,commentDepth,commentDepth,false,false,inDefine,
							inDate,inDate));
					curspot++;
				} else if(!inString && cur==']') {
					commentDepth--;
					if(commentDepth<0)
						commentDepth=0;

					addToken(tokens,curspot,new Token(scur,cstart,commentDepth+1,commentDepth,false,false,inDefine,
							inDate,inDate));
					curspot++;
				} else if(commentDepth==0 && !inString && cur =='\'') {
					if(inDate)
						addToken(tokens,curspot,new Token(scur,cstart,0,0,inString,inString,inDefine,true,false));
					else
						addToken(tokens,curspot,new Token(scur,cstart,0,0,inString,inString,inDefine,true,true));

					curspot++;
					inDate = !inDate;
				} else if(cur == ':'){
					if(chars[i+1] == '(') {
						addToken(tokens,curspot,new Token(":(",cstart,commentDepth,commentDepth,inString,inString,inDefine, inDate,inDate));
						i++;
						cstart++;
					} else {
						addToken(tokens,curspot,new Token(":",cstart,commentDepth,commentDepth,inString,inString,inDefine, inDate,inDate));
					}
					curspot++;
				} else if(scur.trim().length()!=0){
					addToken(tokens,curspot,new Token(scur,cstart,commentDepth,commentDepth,inString,inString,inDefine,
							inDate,inDate));
					curspot++;
				}

				cstart++;
			}
		}

		String scur=sb.toString().trim();
		if(scur.length()>0){
			if(commentDepth==0 && !inString) {
				if(scur.equals("define")) {
					inDefine = true;
				} else if(scur.equals("end")) {
					inDefine = false;
					allDefs = false;
				}
			}

			addToken(tokens,curspot,new Token(scur,cstart,commentDepth,commentDepth,inString,inString,inDefine,
					inDate,inDate));
			curspot++;
		}

		if(end!=oldend)
			for(int i=curspot;i<tokens.size();i++)
				tokens.get(i).incStart(end-oldend);

		int fixspot = curspot;

		if(inString!=oldInString || commentDepth!=oldCommentDepth || inDefine!=oldInDefine || inDate!=oldInDate) {
			for(fixspot=curspot;fixspot<tokens.size();fixspot++) {
				Token tcur = tokens.get(fixspot);
				String cur = tcur.getStr();


				oldInString = tcur.inString();
				oldInDate = tcur.inDate();
				oldCommentDepth = tcur.getCDepth();
				oldInDefine = tcur.inDefs();

				tcur.setInString(inString,inString);
				tcur.setInDate(inDate,inDate);
				tcur.setCDepth(commentDepth,commentDepth);
				tcur.setInDefs(inDefine);			

				if(commentDepth==0 && cur.equals("\"")) {
					if(inString)
						tcur.setInString(true,false);
					else
						tcur.setInString(true,true);

					inString = !inString;
				} else if(!inString && cur.equals("[")) {
					commentDepth++;

					tcur.setCDepth(commentDepth,commentDepth);
				} else if(!inString && cur.equals("]")) {
					commentDepth--;
					if(commentDepth<0)
						commentDepth=0;

					tcur.setCDepth(commentDepth+1,commentDepth);
				} else if(!inString && commentDepth==0 && cur.equals("define")) {
					inDefine = true;
					tcur.setInDefs(true);
				} else if(!inString && commentDepth==0 && cur.equals("end")) {
					inDefine = false;
					tcur.setInDefs(false);
				} else if(!inString && commentDepth==0 && cur.equals("'")) {
					if(inDate)
						tcur.setInDate(true,false);
					else
						tcur.setInDate(true,true);

					inDate=!inDate;
				} else if(inDefine==oldInDefine && commentDepth==oldCommentDepth && inString==oldInString &&
						inDate==oldInDate) {
					break;
				}
			}
		}

		for(int i=curspot-1;i>=0;i--)
			if(!tokens.get(i).inString() && tokens.get(i).getCDepth()!=0) {
				tokens.get(i).setNearTokens(tokens,i);
				break;
			}

		for(int i=Math.max(0,ftoken-1);i<fixspot;i++){
			tokens.get(i).setNearTokens(tokens,i); //Set near tokens on the ones we edited
		}

		if(tokens.size()>1) {
			//Go through and merge multi tokens into single ones, ex. db fields and records

			//First go through and add buffer distances to the tokens we have already
			if(lasttokens.size() > 0 ){
				Token first = lasttokens.get(0);
				Token last = lasttokens.get(lasttokens.size()-1);

				if( first.getBefore() != null){
					lasttokens.add(0,first.getBefore());
					if( first.getBefore().getBefore() != null)
						lasttokens.add(0,first.getBefore().getBefore());
				}

				if( last.getAfter() != null){
					lasttokens.add(first.getAfter());
					if( last.getAfter().getAfter() != null)
						lasttokens.add(first.getAfter().getAfter());
				}

				int i = 0;

				while(i < lasttokens.size() - 1){
					Token cur = lasttokens.get(i);

					if( cur.getAfter() == null)
						break;

					//Merge Print title so we can make that a division in the parser later versus regular print commands

					if( (cur.getStr().equals("print") && cur.getAfter().getStr().equals("title")) ||
							(db.containsRecordName(cur.getStr() + " " + cur.getAfter().getStr()) && str.substring(cur.getEnd(), cur.getAfter().getStart()).equals(" ")))
					{
						cur.setStr(cur.getStr() + " " + cur.getAfter().getStr() );
						tokens.remove(cur.getAfter());
						cur.setNearTokens(tokens, tokens.indexOf(cur));
						if( cur.getAfter() != null )
							cur.getAfter().setNearTokens(tokens, tokens.indexOf(cur.getAfter()));

						continue;
					}
					if( cur.getAfter().getAfter() != null)	
					{
						if ( db.containsFieldName(cur.getStr() + ":" + cur.getAfter().getAfter().getStr()) && str.substring(cur.getEnd(), cur.getAfter().getAfter().getStart()).equals(":") ) {
							cur.setStr(cur.getStr() + ":" + cur.getAfter().getAfter().getStr() );
							tokens.remove(cur.getAfter());
							tokens.remove(cur.getAfter().getAfter());
							cur.setNearTokens(tokens, tokens.indexOf(cur));
							if( cur.getAfter() != null )
								cur.getAfter().setNearTokens(tokens, tokens.indexOf(cur.getAfter()));

							continue;
						} else if ( cur.getAfter().getAfter().getAfter() != null && db.containsFieldName(cur.getStr() + ":1") && cur.getAfter().getStr().equals(":(") && cur.getAfter().getAfter().getAfter().getStr().equals(")") ) {
							cur.setStr(cur.getStr() + ":(" + cur.getAfter().getAfter().getStr()+")" );
							tokens.remove(cur.getAfter());
							tokens.remove(cur.getAfter().getAfter());
							tokens.remove(cur.getAfter().getAfter().getAfter());
							cur.setNearTokens(tokens, tokens.indexOf(cur));
							if( cur.getAfter() != null )
								cur.getAfter().setNearTokens(tokens, tokens.indexOf(cur.getAfter()));

							continue;
						}
					}

					i++;
				}
			}

			if(ftoken<tokens.size())
				charStart = tokens.get(ftoken).getStart();
			else
				charStart = tokens.get(tokens.size()-1).getEnd();

			if(fixspot<tokens.size())
				charEnd = tokens.get(fixspot).getStart();
			else
				charEnd = str.length();

			if( txt != null)
				if( redrawAll )
					txt.redrawRange(0, txt.getCharCount(), false);
				else
					if( replacedText != null &&  replacedText.contains("\n") )
						txt.redrawRange(charStart, txt.getCharCount()-charStart, false);
					else
						txt.redrawRange(charStart,charEnd-charStart,false); 
		}

		return allDefs;
	}

	private static String getFullString(Token cur, String fileData){
		if( !cur.inString() )
			return "";

		String type = "";
		Token fToken, lToken = null;

		if( cur.getAfter() == null)
			return "";

		fToken = cur.getAfter();


		while( (cur=cur.getAfter()) != null )
		{
			if(!cur.inString() || cur.getStr().equals("\""))
				break;

			lToken =cur;
		}

		type += fileData.substring(fToken.getStart(),Math.min(lToken == null ? fileData.length() -1 : lToken.getEnd(),fileData.length()-1));		

		return type;
	}

	private static boolean isNumber(String str){
		try{
			Integer.parseInt(str);
			return true;
		}
		catch(Exception e){
			return false;
		}
	}

	private synchronized void rebuildVars(String fileName, String data, ArrayList<Token> tokens) {
		ArrayList<Variable> newvars = new ArrayList<Variable>();
		ArrayList<Variable> oldvars = new ArrayList<Variable>();

		boolean changed = false, exists = false;

		int c = 0;

		//Note: I don't think this block still needs syncing, I changed a number of things
		while( c < lvars.size() ){
			if( lvars.get(c).getFilename().equals(fileName))
				oldvars.add(lvars.remove(c));
			else
				c++;
		}


		System.out.println("Parsing vars for " + fileName);

		for (Token tcur : tokens) {
			Variable newVar;

			if (tcur.getAfter() != null && tcur.inDefs() && tcur.getCDepth() == 0 && !tcur.inString() && !tcur.inDate() && tcur.getAfter().getStr().equals("=") && tcur.getAfter().getAfter() != null) {
				Token typeToken = tcur.getAfter().getAfter();
				boolean isConstant = true;

				if( typeToken == null )
					continue;

				String type = typeToken.getStr().toUpperCase();

				//Strings
				if (typeToken.inString()){
					type = "\"" + getFullString(typeToken,data) + "\"";
				}

				//Date
				if( typeToken != null && typeToken.inDate() ){
					for(int i = 0; i<=5;i++)
					{		
						typeToken = typeToken.getAfter();

						if( typeToken == null)
							break;

						type += typeToken.getStr();
					}					
				}

				//Rate
				if( typeToken != null && isNumber(typeToken.getStr()) && typeToken.getAfter() != null && typeToken.getAfter().getStr().equals(".") && typeToken.getAfter().getAfter() != null && isNumber(typeToken.getAfter().getAfter().getStr())){
					for(int i = 0; i<=2;i++)
					{		
						typeToken = typeToken.getAfter();

						if( typeToken == null)
							break;

						type += typeToken.getStr();
					}	
				}

				//Money
				if( typeToken != null && typeToken.getStr().equals("$")){
					while( (typeToken = typeToken.getAfter()) != null )
					{
						if( !isNumber(typeToken.getStr()) && !typeToken.getStr().equals(",") && !typeToken.getStr().equals(".") )
							break;

						type += typeToken.getStr();
					}
				}

				//Character with ()'s
				if( typeToken != null && typeToken.getStr().equals("character") && typeToken.getAfter() != null && typeToken.getAfter().getStr().equals("(")){
					Token curTok = typeToken;

					while( curTok != null && !curTok.getStr().equals(")") ){
						curTok = curTok.getAfter();
						type += curTok.getStr().toUpperCase();						
					}

					typeToken = curTok;
					isConstant = false;
				}

				for( VariableType cur : VariableType.values())
					if( cur.toString().equalsIgnoreCase(type))
						isConstant = false;

				//Array support
				if( typeToken != null && typeToken.getAfter() != null && typeToken.getAfter().getStr().equals("array")){

					while( (typeToken = typeToken.getAfter()) != null )
					{					
						if( !isNumber(typeToken.getStr()) && !typeToken.getStr().equals(")") && !typeToken.getStr().equals("(") && !typeToken.getStr().equals("array") && !typeToken.getStr().equals(","))
							break;

						type += (isNumber(typeToken.getStr()) || typeToken.getStr().equals(")") ? "" : " ") + typeToken.getStr().toUpperCase();
					}
				}

				newVar = new Variable(tcur.getStr(), fileName, tcur.getStart(), type);
				newVar.setConstant(isConstant);
				newvars.add(newVar);
			}
		}

		changed = !(oldvars.size() == newvars.size());

		if( !changed ){
			for (Variable var : newvars) {
				exists = false;

				for (Variable lvar : oldvars)
					if (lvar.equals(var))
						exists = true;

				if (!exists) {
					changed = true;
					break;
				}
			}
		}

		//Still needs synchronizing, as the method level synchronized doesn't effect calls from the background parsers
		synchronized(lvars){
			lvars.addAll(newvars);
		}

		if (changed && fileName.equals(file.getName()))
			txt.redrawRange(0, txt.getText().length(), false);

	}

	public void textModified(int start, int length, String replacedText){
		if (reparse) {
			int st = start;
			int end = st + length;
			int oldend = st + replacedText.length();
			boolean rebuildVars = false;

			long time = System.currentTimeMillis();

			try {
				parse(file.getName(), txt.getText(), st, end, oldend, replacedText, ltokens, lasttokens, removedtokens, lvars, txt);

				for( Token cur : lasttokens){
					if( cur.inDefs() )
						rebuildVars = true;

					if( cur.getStr().equals("#include") && !cur.inDate() && !cur.inString() && cur.getCDepth() == 0)
					{
						refreshIncludes = true;
					}

					if( cur.inString() ){
						Token t = cur;

						while(t != null){
							t = t.getBefore();

							if( t != null && !t.inString() ){
								if( t.getStr().equals("#include"))
									refreshIncludes = true;

								break;								
							}
						}
					}
				}
				//problems here
				for( Token cur : removedtokens){
					if( cur.inDefs() )
						rebuildVars = true;

					if( cur.getStr().equals("#include") && !cur.inDate() && !cur.inString() && cur.getCDepth() == 0)
					{
						refreshIncludes = true;
					}

					if( cur.inString() ){
						Token t = cur;

						while(t != null){
							t = t.getBefore();

							if( t != null && !t.inString() ){
								if( t.getStr().equals("#include"))
									refreshIncludes = true;

								break;								
							}
						}
					}
				}


				if( rebuildVars )
					rebuildVars(file.getName(), txt.getText(), ltokens);

				if( initialIncludeParseNeeded ){
					parseIncludes();
					initialIncludeParseNeeded = false;
				}
			} catch (Exception e) {
				System.err.println("Syntax Highlighter error!");
				e.printStackTrace();
			}
			// parse(file.getName(),txt.getText(),txt.,end,end,ltokens,lvars);
			//System.out.println("Parse time: " + (System.currentTimeMillis() - time));
		}
	}

	public void parseIncludes(){
		if( includeParserWorker == null ){
			includeParserWorker = new BackgroundIncludeParser(txt.getText());
			refreshIncludes = false;
			includeParserWorker.start();
		}
	}

	public void errorCheck(){
		if( errorCheckerWorker == null && !noParse){
			errorCheckerWorker = new BackgroundSymitarErrorChecker(this);
			errorCheckerWorker.start();
		}
	}

	public void reparseAll() {
		try {
			ltokens = new ArrayList<Token>();
			parse(file.getName(), txt.getText(), 0, txt.getCharCount() - 1, 0, null, ltokens, lasttokens, removedtokens, lvars, txt);
			rebuildVars(file.getName(), txt.getText(), ltokens);
			System.out.println("Reparsed");
		} catch (Exception e) {
			System.err.println("Syntax Highlighter error!");
			e.printStackTrace();
		}
	}

	public void cleanupTokenCache(){
		//WTF: There is a weird bug I need to account for with the garbage collector.
		//When we remove references to the token arraylists, that doens't make any difference, since each token is in a linked list too, which doesn't get properly collected
		//So, I need to essentially clear the linked list
		
		for( ArrayList<Token> tokens : includeTokenChache.values())
			for( Token tok : tokens)
				tok.setNearTokens(null,0);
		
		includeTokenChache.clear();
		includes.clear();
	}

	public ArrayList<Token> getLtokens() {
		return ltokens;
	}

	public ArrayList<Variable> getLvars() {
		return lvars;
	}

	public void setReparse(boolean reparse) {
		this.reparse = reparse;
	}

	public boolean isReparse() {
		return reparse;
	}

	public SymitarFile getFile() {
		return file;
	}

	public void setFile(SymitarFile file) {
		this.file = file;
	}

	public int getSym() {
		return sym;
	}

	public void setSym(int sym) {
		this.sym = sym;
	}

	public StyledText getTxt() {
		return txt;
	}

	public void setTxt(StyledText txt) {
		this.txt = txt;
	}

	public static FunctionLayout getFunctions() {
		return functions;
	}


	public static KeywordLayout getKeywords() {
		return keywords;
	}

	public static void setKeywords(KeywordLayout keywords) {
		RepgenParser.keywords = keywords;
	}

	public static SpecialVariables getSpecialvars() {
		return specialvars;
	}

	public static void setSpecialvars(SpecialVariables specialvars) {
		RepgenParser.specialvars = specialvars;
	}

	public static DatabaseLayout getDb() {
		return db;
	}

	public static void setDb(DatabaseLayout db) {
		RepgenParser.db = db;
	}

	public void setErrorList(ArrayList<Error> errorList) {
		this.errorList = errorList;
	}

	public ArrayList<Error> getErrorList() {
		return errorList;
	}

	public void setTaskList(ArrayList<Task> taskList) {
		this.taskList = taskList;
	}

	public ArrayList<Task> getTaskList() {
		return taskList;
	}

	public boolean needRefreshIncludes(){
		return refreshIncludes;
	}

	public void setRefreshIncludes(boolean val){
		refreshIncludes = val;
	}

	//Returns the last tokens to be added in the previous call to the parser, useful for the snippet manager and stuff
	public ArrayList<Token> getLastTokens(){
		return lasttokens;
	}

	public ArrayList<Include> getIncludes(){
		return includes;
	}

	public HashMap<String,ArrayList<Token>> getIncludeTokenChache() {
		return includeTokenChache;
	}
}

