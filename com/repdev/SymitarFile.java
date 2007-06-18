package com.repdev;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * File on symitar server
 * @author Jake Poznanski
 *
 */
public class SymitarFile {
	private String name;
	private FileType type;
	private Date modified = new Date(0), installed = new Date(0);
	private long size = -1;
	private boolean local = false;
	private int sym;

	
	/**
	 * New Local file
	 * @param name
	 */
	public SymitarFile(String name){
		this.name = name;
		this.local = true;
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
		if( !local )
			return RepDevMain.SYMITAR_SESSIONS.get(sym).saveFile(this, data);
		
		//TODO: Implement local file saving properly
		
		return null;
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
}
