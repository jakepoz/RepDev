package com.repdev;

import java.util.ArrayList;

public class Project {

	private ArrayList<SymitarFile> files = new ArrayList<SymitarFile>();
	private String name;
	private int sym;

	public Project(String name, int sym) {
		this.name = name;
		this.sym = sym;
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
			RepDevMain.SYMITAR_SESSIONS.get(sym).removeFile(file);
	}

	public String toString() {
		return name;
	}

}
