package com.repdev;

import java.util.ArrayList;
import java.util.*;

/**
 * Global help utility class for managing projects
 * @author Jake Poznanski
 *
 */
public class ProjectManager {
	private static HashMap<Integer, ArrayList<Project>> symProjects = new HashMap<Integer, ArrayList<Project>>();

	public static void saveAllProjects() {
		for (Integer sym : symProjects.keySet())
			saveProjects(sym);
	}

	public static void removeProject(Project project, boolean delete) {
		int sym = project.getSym();
		ArrayList<Project> projects = getProjects(sym);
		int i = 0;

		for (Project cur : projects) {
			if (cur.getName().equals(project.getName()))
				break;

			i++;
		}

		projects.remove(i);

		if (delete)
			for (SymitarFile file : project.getFiles())
				RepDevMain.SYMITAR_SESSIONS.get(sym).removeFile(file);

	}

	public static void saveProjects(int sym) {
		StringBuilder sb = new StringBuilder();
		ArrayList<Project> myProjects = symProjects.get(sym);

		if (myProjects == null)
			return;

		for (Project proj : myProjects) {
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

		RepDevMain.SYMITAR_SESSIONS.get(sym).saveFile(getProjectFile(sym), sb.toString());
	}

	public static Project createProject(String name, int sym) {
		Project project = new Project(name, sym);
		getProjects(sym).add(project);

		return project;
	}

	/**
	 * Creates a file name from an open symitar session based on user id
	 * 
	 * @param session
	 * @return
	 */
	private static SymitarFile getProjectFile(int sym) {
		SymitarSession session = RepDevMain.SYMITAR_SESSIONS.get(sym);
		String prefix = "tester";

		if (session.getUserID() == null || session.getUserID().length() >= 3)
			prefix = session.userID.substring(0, 3);

		return new SymitarFile("repdev." + prefix + "projects", FileType.REPGEN);
	}

	private static void loadProjects(int sym) {
		ArrayList<Project> myProjs = new ArrayList<Project>();
		String dataTemp = RepDevMain.SYMITAR_SESSIONS.get(sym).getFile(getProjectFile(sym));
		Project curProject = null;

		if (dataTemp == null) {
			symProjects.put(sym, myProjs);
			return;
		}

		String[] data = dataTemp.trim().split("\n");

		for (String line : data) {
			String[] parts = line.split("\t");

			if (parts[0].equals("PROJECT")) {
				if (curProject != null)
					myProjs.add(curProject);

				curProject = new Project(parts[1], sym);
			} else if (parts[0].equals("FILE")) {
				curProject.addFile(new SymitarFile(parts[2].trim(), FileType.valueOf(parts[1])));
			}
		}

		if (curProject != null)
			myProjs.add(curProject);

		symProjects.put(sym, myProjs);
	}

	public static ArrayList<Project> getProjects(int sym) {
		if (symProjects.get(sym) == null) {
			loadProjects(sym);
		}

		return symProjects.get(sym);
	}

}
