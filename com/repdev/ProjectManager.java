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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Global help utility class for managing projects
 * @author Jake Poznanski
 *
 */
public class ProjectManager {
	public static String prefix = "tester";
	private static HashMap<Object, ArrayList<Project>> projects = new HashMap<Object, ArrayList<Project>>();

	public static void saveAllProjects() {
		for (Object key: projects.keySet())
			if( key instanceof Integer)
				saveProjects((Integer)key);
			else if( key instanceof String)
				saveProjects((String)key);
	}

	public static void removeProject(Project project, boolean delete) {
		int sym = project.getSym();
		boolean found = false;
		ArrayList<Project> projects = new ArrayList<Project>();
		
		if( project.isLocal() )
			projects = getProjects(project.getDir());
		else
			projects = getProjects(sym);
		
		int i = 0;

		for (Project cur : projects) {
			if (cur.getName().equals(project.getName())){
				found = true;
				break;
			}

			i++;
		}
		if(!found)
			return;
		projects.remove(i);
		
		if( project.isLocal() )
			saveProjects(project.getDir());
		else
			saveProjects(sym);

		if (delete)
			for (SymitarFile file : project.getFiles())
					if( file.isLocal() )
						new File( file.getPath() ).delete();
					else
						RepDevMain.SYMITAR_SESSIONS.get(sym).removeFile(file);

	}

	public static void saveProjects(String dir){
		ArrayList<Project> myProjects = getProjects(dir);
		getProjectFile(dir).saveFile(processSaveProjects(myProjects));
	}
	
	private static String processSaveProjects(ArrayList<Project> list){
		StringBuilder sb = new StringBuilder();
	
		if (list == null)
			return null;

		for (Project proj : list) {
			sb.append("PROJECT");
			sb.append("\t");
			sb.append(proj.getName());
			sb.append("\r\n");

			for (SymitarFile file : proj.getFiles()) {
				sb.append("FILE");
				sb.append("\t");
				sb.append(file.getType());
				sb.append("\t");
				sb.append(file.getName());
				sb.append("\r\n");
			}

			sb.append("\r\n");
		}
		
		return sb.toString();
	}
	
	public static void saveProjects(int sym) {
		ArrayList<Project> myProjects = getProjects(sym);
		
		if( RepDevMain.SYMITAR_SESSIONS.get(sym) == null || !RepDevMain.SYMITAR_SESSIONS.get(sym).isConnected() )
			return;
		
		SymitarFile file = getProjectFile(sym);		
		String projectFile = processSaveProjects(myProjects);
		
		if( file != null )
			file.saveFile(projectFile);

		// TODO: Set to live when ready...
		if( !RepDevMain.DEVELOPER && Config.getBackupProjectFiles() ) // only dev's can backup project files for now
			return;
		
		// Make a backup of the projects file
		try {
			File dir = new File("backup\\");
			if( !dir.isDirectory() ) {
				dir.mkdir();
			}
			
			SymitarSession session = RepDevMain.SYMITAR_SESSIONS.get(sym);
			SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");
			PrintWriter out = new PrintWriter(new FileWriter("backup\\repdev" + session.getUserNum() + 
							"projects_sym" + sym + "." + date.format(new Date())));

			out.write(projectFile);
			out.flush();
			out.close();
			System.out.println( "Backup of projects for sym" + sym + " created" );
		} catch( IOException e ) {
			//e.printStackTrace();
			System.err.println("ERROR: Unable to create backup of projects file on sym " + sym);
		}
	}


	public static Project createProject(String name, int sym) {
		return createProject(name,  sym, -1);
	}
	public static Project createProject(String name, int sym, int index) {
		//removeProject(project)
		Project project = new Project(name, sym);
		boolean exists = false;
		
		for( Project p : getProjects(sym) )
			if( p.getName().equals(name))
			{
				if(index > -1){
					removeProject(project, false);
					break;
				}
				exists = true;
				project = p;
				break;
			}
		
		if( !exists ){
			if(getProjects(sym).size() == 0 || getProjects(sym).size() < index || index == -1)
				getProjects(sym).add(project);
			else
				getProjects(sym).add(index, project);
			saveProjects(sym);
		}
		
		return project;
	}
	
	public static Project createProject(String name, String dir) {
		return createProject(name, dir,-1);
	}
	public static Project createProject(String name, String dir, int index) {
		Project project = new Project(name, dir);
		boolean exists = false;
		
		for( Project p : getProjects(dir) )
			if( p.getName().equals(name))
			{
				if(index > -1){
					removeProject(project, false);
				}
				else
					exists = true;
				project = p;
				break;
			}
		
		if( !exists ){
			if(getProjects(dir).size() == 0 || getProjects(dir).size() < index || index == -1)
				getProjects(dir).add(project);
			else
				getProjects(dir).add(index, project);
			saveProjects(dir);
		}
		
		return project;
	}

	/**
	 * Creates a file name from an open symitar session based on user id
	 * TODO: See Clay Yearsley's email, use first 4 characters, or until a period is entered? Many first X numeric ones, padded with zeros.
	 * @param session
	 * @return
	 */
	private static SymitarFile getProjectFile(String dir) {
		return new SymitarFile(dir,"repdev.projects");
	}
	
	private static SymitarFile getProjectFile(int sym) {
		if( RepDevMain.SYMITAR_SESSIONS.get(sym) == null || !RepDevMain.SYMITAR_SESSIONS.get(sym).isConnected() )
			return null;
		
		SymitarSession session = RepDevMain.SYMITAR_SESSIONS.get(sym);

		if( session == null || !session.isConnected() )
			return null;

		if(session.getUserID() != null)
			prefix = session.getUserNum(true);

		return new SymitarFile(sym,"repdev." + prefix + "projects", FileType.REPGEN);

	}
	
	private static void loadProjects(String dir){
		ArrayList<Project> myProjs = new ArrayList<Project>();
		String dataTemp = getProjectFile(dir).getData();

		if (dataTemp == null) {
			projects.put(dir, myProjs);
			return;
		}

		processLoadProject(dataTemp, dir);
	}
	
	private static void processLoadProject(String dataStr, Object key){
		Project curProject = null;
		ArrayList<Project> myProjs = new ArrayList<Project>();
		
		String[] data = dataStr.trim().split("\n");

		for (String line : data) {
			String[] parts = line.split("\t");
			parts[parts.length-1] = parts[parts.length-1].trim(); //Trim off any whitespace from the last part split, depending on the loading method and file storage mechanism, this can cause issues.
			
			if (parts[0].equals("PROJECT")) {
				if (curProject != null)
					myProjs.add(curProject);

				if( key instanceof Integer)
					curProject = new Project(parts[1], (Integer)key);
				else if( key instanceof String)
					curProject = new Project(parts[1], (String)key);
			} else if (parts[0].equals("FILE")) {
				
				if( key instanceof Integer)
					curProject.addFile(new SymitarFile((Integer)key,parts[2].trim(), FileType.valueOf(parts[1])));
				else if ( key instanceof String )
					curProject.addFile(new SymitarFile((String)key,parts[2].trim(), FileType.valueOf(parts[1])));
			}
		}

		if (curProject != null)
			myProjs.add(curProject);

		projects.put(key, myProjs);
	}
	
	

	private static void loadProjects(int sym) {
		ArrayList<Project> myProjs = new ArrayList<Project>();

		if( RepDevMain.SYMITAR_SESSIONS.get(sym) == null || !RepDevMain.SYMITAR_SESSIONS.get(sym).isConnected() )
			return;
		
		String dataTemp = getProjectFile(sym).getData();

		
		if (dataTemp == null) {
			projects.put(sym, myProjs);
			return;
		}

		processLoadProject(dataTemp, sym);
	}
	


	public static ArrayList<Project> getProjects(String dir){
		if( projects.get(dir) == null)
			loadProjects(dir);
			
		return projects.get(dir);
	}
	
	public static ArrayList<Project> getProjects(int sym) {
		if (projects.get(sym) == null) {
			loadProjects(sym);
		}

		return projects.get(sym);
	}
	
	public static boolean containsProject(String dir, String name){
		boolean exists = false;
		
		for( Project cur : getProjects(dir))
			if( cur.getName().equals(name)){
				exists = true;
				break;
			}
		
		return exists;
	}
	
	public static boolean containsProject(int sym, String name){
		boolean exists = false;
		
		for( Project cur : getProjects(sym))
			if( cur.getName().equals(name)){
				exists = true;
				break;
			}
		
		return exists;
	}

}

