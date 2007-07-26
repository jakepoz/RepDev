package com.repdev;

import java.io.File;
import java.util.ArrayList;

/**
 * Class describing a project on the symitar server
 * @author Jake Poznanski
 *
 */
public class Project {

	private ArrayList<SymitarFile> files = new ArrayList<SymitarFile>();
	private String name;
	private int sym;
	private String dir;
	
	public Project(String name, int sym) {
		this.name = name;
		this.sym = sym;
	}
	
	public Project(String name, String dir){
		this.name = name;
		this.dir = dir;
	}
	
	public boolean isLocal(){
		return dir != null && !dir.trim().equals("");
	}
	
	public String getDir() {
		return dir;
	}

	public int getSym() {
		return sym;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<SymitarFile> getFiles() {
		return files;
	}

	public boolean hasFile(SymitarFile file) {
		for (SymitarFile cFile : files)
			if (cFile.getName().equals(file.getName()) && cFile.getType().equals(file.getType()))
				return true;

		return false;
	}

	public void addFile(SymitarFile file) {
		if (!hasFile(file))
			files.add(file);
	}

	public void removeFile(SymitarFile file, boolean delete) {
		if (!hasFile(file))
			return;

		files.remove(file);

		if (delete)
			if( isLocal() )
				new File( file.getPath() ).delete();
			else
				RepDevMain.SYMITAR_SESSIONS.get(sym).removeFile(file);
	}

	public String toString() {
		return name;
	}

}
