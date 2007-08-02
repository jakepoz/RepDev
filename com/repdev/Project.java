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
