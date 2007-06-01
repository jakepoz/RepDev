package com.repdev;

import java.util.ArrayList;
import java.util.Date;
import java.io.*;
import java.util.regex.*;

public class TestingSymitarSession extends SymitarSession {
	boolean isConnected;
	int repGenRun = -1;

	@Override
	public SessionError connect(String server, String aixUsername, String aixPassword, int sym, String userID) {
		this.sym = sym;
		this.aixUsername = aixUsername;
		this.aixPassword = aixPassword;
		this.userID = userID;
		isConnected = true;
		return SessionError.NONE;
	}

	@Override
	public SessionError disconnect() {
		if (isConnected) {
			isConnected = false;
			return SessionError.NONE;
		} else
			return SessionError.NOT_CONNECTED;
	}

	@Override
	public int acceptRepGenQuery(String value) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getBatchOutputSequenceNumber() {
		if (repGenRun == 1)
			return 133;
		else
			return -1;
	}

	@Override
	public String getFile(SymitarFile file) {
		StringBuilder data = new StringBuilder();
		String line;
		BufferedReader in;

		System.out.println("Requesting File: " + file.getName() + " from Sym " + sym);

		try {
			in = new BufferedReader(new FileReader("testdata" + System.getProperty("file.separator") + getSym() + "." + file.getType().toString() + "." + file.getName() + ".txt"));

			while ((line = in.readLine()) != null) {
				data.append(line);
				data.append("\r\n");
			}

			if (in != null)
				in.close();

			return data.toString();
		} catch (Exception e) {
			return null;
		}

	}

	// A silly class to make file system files work with wildcards like on
	// Symitar
	private class SymitarWildcardFilter implements FilenameFilter {
		String matcher;
		FileType type;

		public SymitarWildcardFilter(String matcher, FileType type) {
			this.matcher = matcher;
			this.type = type;
		}

		public boolean accept(File dir, String name) {
			// Strip out other things to get just the name we want
			try {
				if (!name.startsWith(String.valueOf(getSym())))
					return false;

				name = name.substring(name.indexOf(".") + 1);

				if (!name.startsWith(type.toString()))
					return false;

				name = name.substring(name.indexOf(".") + 1);

				name = name.substring(0, name.lastIndexOf("."));

				Pattern p = Pattern.compile(matcher.replaceAll("\\+", ".*"));
				Matcher m = p.matcher(name);

				return m.matches();

			} catch (Exception e) {
			}

			return true;
		}
	}

	@Override
	public ArrayList<SymitarFile> getFileList(FileType type, String search) {
		ArrayList<SymitarFile> data = new ArrayList<SymitarFile>();

		File file = new File("testdata");
		File[] fileListData;

		search = search.trim();

		if (search.equals(""))
			search = "+";

		fileListData = file.listFiles(new SymitarWildcardFilter(search, type));

		if (fileListData != null)
			for (File current : fileListData) {
				String name = current.getName();
				name = name.substring(name.indexOf(".") + 1);
				name = name.substring(name.indexOf(".") + 1);
				name = name.substring(0, name.lastIndexOf("."));

				data.add(new SymitarFile(name, type, new Date(current.lastModified()), current.length()));
			}

		return data;
	}

	@Override
	public String getRepGenQuery() {
		switch (repGenRun) {
		case 0:
			repGenRun = 1;
			return "Enter the date to run report on";
		}

		return null;
	}

	@Override
	public int getRepgenQueue() {
		return 0;
	}

	@Override
	public ArrayList<Integer> getSequenceNumbers() {
		if (repGenRun == 1) {
			ArrayList<Integer> temp = new ArrayList<Integer>();
			temp.add(134);
			temp.add(135);
		}

		return null;
	}

	@Override
	public boolean isConnected() {
		return isConnected;
	}

	@Override
	public SessionError runBatchFM(String title) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void runRepGen(String name) {
		runRepGen(name, 0);
	}

	@Override
	public void runRepGen(String name, int queue) {
		repGenRun = 0;
	}

	@Override
	public SessionError saveFile(SymitarFile file, String text) {
		PrintWriter out = null;

		System.out.println("Saving File: " + file.getName() + " from Sym " + sym);

		try {
			out = new PrintWriter(new FileWriter("testdata" + System.getProperty("file.separator") + getSym() + "." + file.getType().toString() + "." + file.getName() + ".txt"));

			out.print(text);

			if (out != null)
				out.close();

			return SessionError.NONE;

		} catch (Exception e) {
			return SessionError.IO_ERROR;
		}

	}

	@Override
	public void waitOnChange() {
		return;
	}

	@Override
	public SessionError removeFile(SymitarFile file) {
		File localFile = new File("testdata" + System.getProperty("file.separator") + getSym() + "." + file.getType().toString() + "." + file.getName() + ".txt");

		if (!localFile.exists())
			return SessionError.IO_ERROR;

		localFile.delete();

		return SessionError.NONE;
	}

	@Override
	public SessionError printFileLPT(SymitarFile file, boolean formsOverride, int formLength, int startPage, int endPage, int copies, boolean landscape, boolean duplex, int queuePriority) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SessionError printFileTPT(SymitarFile file, int queue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ErrorCheckResult errorCheckRepGen(String filename) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ErrorCheckResult installRepgen(String f) {
		return null;
	}

}
