package com.repdev;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Global help utility class for managing projects
 * @author Jake Poznanski
 *
 */
public class ProjectManager {
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
		ArrayList<Project> projects = new ArrayList<Project>();
		
		if( project.isLocal() )
			projects = getProjects(project.getDir());
		else
			projects = getProjects(sym);
		
		int i = 0;

		for (Project cur : projects) {
			if (cur.getName().equals(project.getName()))
				break;

			i++;
		}

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
		SymitarFile file = getProjectFile(sym);
		
		if( file != null)
			file.saveFile(processSaveProjects(myProjects));
	}

	public static Project createProject(String name, int sym) {
		Project project = new Project(name, sym);
		boolean exists = false;
		
		for( Project p : getProjects(sym))
			if( p.getName().equals(name))
			{
				exists = true;
				project = p;
				break;
			}
		
		if( !exists ){
			getProjects(sym).add(project);
			saveProjects(sym);
		}
		
		return project;
	}
	
	public static Project createProject(String name, String dir) {
		Project project = new Project(name, dir);
		boolean exists = false;
		
		for( Project p : getProjects(dir))
			if( p.getName().equals(name))
			{
				exists = true;
				project = p;
				break;
			}
		
		if( !exists ){
			getProjects(dir).add(project);
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
		SymitarSession session = RepDevMain.SYMITAR_SESSIONS.get(sym);
		String prefix = "tester";

		if( session == null)
			return null;
		
		if (session.getUserID() != null && session.getUserID().length() >= 3)
			prefix = session.userID.substring(0, 3);
		else if( session.getUserID() != null)
			prefix = session.getUserID();


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
					curProject.addFile(new SymitarFile((String)key, parts[2].trim()));
			}
		}

		if (curProject != null)
			myProjs.add(curProject);

		projects.put(key, myProjs);
	}
	
	

	private static void loadProjects(int sym) {
		ArrayList<Project> myProjs = new ArrayList<Project>();
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
