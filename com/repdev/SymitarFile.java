package com.repdev;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Date;

/**
 * File on symitar server
 * @author Jake Poznanski
 *
 */
public class SymitarFile implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name, dir;
	private FileType type;
	private Date modified = new Date(0), installed = new Date(0);
	private long size = -1;
	private boolean local = false;
	private int sym;

	
	/**
	 * New Local file
	 * @param name
	 */
	public SymitarFile(String dir, String name){
		this.dir = dir;
		this.name = name;
		this.local = true;
		this.type = FileType.REPGEN;
	}
	
	public SymitarFile(int sym, String name, FileType type) {
		this.sym = sym;
		this.name = name;
		this.type = type;
	}

	public SymitarFile(int sym, String name, FileType type, Date modified, long size) {
		this.sym = sym;
		this.name = name;
		this.type = type;
		this.modified = modified;
		this.size = size;
	}
	
	public String getData(){
		if( !local )
			return RepDevMain.SYMITAR_SESSIONS.get(sym).getFile(this);
		else{
			StringBuilder sb= new  StringBuilder();
			try {
				BufferedReader in = new BufferedReader(new FileReader(name));
				String line = "";
				
				while( (line=in.readLine()) != null)
					sb.append(line + "\n");
				
				return sb.toString();
			} catch (FileNotFoundException e) {
				return null;
			} catch (Exception e) {
				return null;
			}
		}
	}
	
	public SessionError saveFile(String data){
		//Note, this is an odd workaround for a symitar issue
		//Repgens with no newline at the end fail to pass error checks, so
		//I'm making sure one exists now
		if( type == FileType.REPGEN && data != null && data.length() > 0 && data.charAt(data.length()-1) != '\n')
			data += "\n";
		
		if( !local )
			return RepDevMain.SYMITAR_SESSIONS.get(sym).saveFile(this, data);
		else{
			try {
				PrintWriter out = new PrintWriter(new FileWriter(getPath()));
				out.write(data);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		return SessionError.NONE;
	}
	
	public boolean isLocal(){
		return local;
	}

	public int getSym() {
		return sym;
	}

	public void setSym(int sym) {
		this.sym = sym;
	}
	
	public String getName() {
		return name;
	}

	public FileType getType() {
		return type;
	}

	public String toString() {
		return name;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public boolean equals(Object o) {
		if (!(o instanceof SymitarFile))
			return false;

		SymitarFile file = (SymitarFile) o;
		return name.equals(file.name) && type.equals(file.type);
	}

	public void setInstalled(Date installed) {
		this.installed = installed;
	}

	public Date getInstalled() {
		return installed;
	}
	
	public String getDir() {
		return dir;
	}
	
	public String getPath(){
		return dir + "\\" + name;
	}
}
