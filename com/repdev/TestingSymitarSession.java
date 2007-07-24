package com.repdev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;

import com.repdev.SymitarSession.FMFile;

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

				data.add(new SymitarFile(sym,name, type, new Date(current.lastModified()), current.length()));
			}

		return data;
	}

	@Override
	public boolean isConnected() {
		return isConnected;
	}

	@Override
	public RunFMResult runBatchFM(String searchTitle, FMFile file, int queue) {
		return null;
	}

	@Override
	public RunRepgenResult runRepGen(String name, int queue, ProgressBar progress, Text text, PromptListener prompter) {
		
		return new RunRepgenResult(-1,0);
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
	public SessionError removeFile(SymitarFile file) {
		File localFile = new File("testdata" + System.getProperty("file.separator") + getSym() + "." + file.getType().toString() + "." + file.getName() + ".txt");

		if (!localFile.exists())
			return SessionError.IO_ERROR;

		localFile.delete();

		return SessionError.NONE;
	}

	@Override
	public SessionError printFileLPT(SymitarFile file, int queue, boolean formsOverride, int formLength, int startPage, int endPage, int copies, boolean landscape, boolean duplex, int queuePriority) {
		return null;
	}

	@Override
	public SessionError printFileTPT(SymitarFile file, int queue) {
		return null;
	}

	@Override
	public ErrorCheckResult errorCheckRepGen(String filename) {
		return new ErrorCheckResult(filename,"",-1,ErrorCheckResult.Type.ERROR);
	}

	@Override
	public ErrorCheckResult installRepgen(String f) {
		return new ErrorCheckResult(f,"",-1,ErrorCheckResult.Type.INSTALLED_SUCCESSFULLY);
	}

	@Override
	public boolean isSeqRunning(int seq) {

		return false;
	}

	@Override
	public void terminateRepgen(int seq) {
		
	}

	@Override
	public ArrayList<PrintItem> getPrintItems(String query, int limit) {

		return null;
	}

	@Override
	public ArrayList<PrintItem> getPrintItems(Sequence seq) {

		return null;
	}

	@Override
	public boolean fileExists(SymitarFile file) {
		
		return false;
	}

	@Override
	public SessionError renameFile(SymitarFile file, String newName) {
		// TODO Auto-generated method stub
		return null;
	}

}
