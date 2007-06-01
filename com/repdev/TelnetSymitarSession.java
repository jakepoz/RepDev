package com.repdev;

import java.io.*;
import java.util.ArrayList;

public class TelnetSymitarSession extends SymitarSession {
	TelnetWrapper telnet = new TelnetWrapper();

	private enum State {
		MAIN_MENU, MANAGEMENT_MENU, PC_TRANSFER_MENU,
	}

	private State state = null;
	boolean connected = false;

	@Override
	public int acceptRepGenQuery(String value) {
		// TODO Auto-generated method stub
		return 0;
	}

	private void log(String data) {
		System.out.println(data);
	}

	@Override
	public SessionError connect(String server, String aixUsername, String aixPassword, int sym, String userID) {
		String temp;

		this.server = server;
		this.sym = sym;
		this.aixUsername = aixUsername;
		this.aixPassword = aixPassword;
		this.userID = userID;

		connected = true;

		try {

			telnet.connect(server, 23);
			telnet.login(aixUsername, aixPassword);
			temp = telnet.waitfor("$", "invalid login");

			log(temp);

			if (temp.indexOf("invalid login") != -1) {
				return SessionError.AIX_LOGIN_WRONG;
			}

			log(telnet.sendLine("sym " + sym));

			// Wait for the user ID prompt
			temp = telnet.waitfor("UserId :", ")");
			if (temp.indexOf(")") != -1) {
				return SessionError.CONSOLE_BLOCKED;
			}

			log(telnet.sendLine(userID));

			// Get through Console Dedication, and application prompt and to the
			// main menu
			temp = telnet.waitfor(":");
			log(temp);

			if (temp.toLowerCase().indexOf("userid") != -1) {
				return SessionError.USERID_INVALID;
			}

			log(telnet.sendLine("Y"));

			temp = telnet.waitfor(":");

			log(temp);

			if (temp.indexOf("Application") != -1) {
				telnet.sendLine("");
				telnet.waitfor(":");
			}

			state = State.MAIN_MENU;

		} catch (Exception e) {
			return SessionError.IO_ERROR;
		}

		return SessionError.NONE;
	}

	@Override
	public SessionError disconnect() {
		try {
			telnet.disconnect();
		} catch (Exception e) {
		}

		connected = false;

		return null;
	}

	@Override
	public int getBatchOutputSequenceNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	private void getToState(State newState) throws IOException {
		if (state == State.MAIN_MENU) {
			if (newState == State.PC_TRANSFER_MENU) {
				// Management
				log(telnet.sendLine("8"));
				log(telnet.waitfor(":"));

				// PC Transfer
				log(telnet.sendLine("2"));
				log(telnet.waitfor(":"));
				state = State.PC_TRANSFER_MENU;
			}

			if (newState == State.MANAGEMENT_MENU) {
				log(telnet.sendLine("8"));
				log(telnet.waitfor(":"));
				state = State.MANAGEMENT_MENU;
			}
		}

		if (state == State.PC_TRANSFER_MENU) {
			if (newState == State.MAIN_MENU) {
				log(telnet.send(Character.toString((char) 27)));
				log(telnet.waitfor(":"));

				log(telnet.send(Character.toString((char) 27)));
				log(telnet.waitfor(":"));

				state = State.MAIN_MENU;
			}

			if (newState == State.MANAGEMENT_MENU) {
				getToState(State.MAIN_MENU);
				getToState(State.MANAGEMENT_MENU);
			}
		}

		if (state == State.MANAGEMENT_MENU) {
			if (newState == State.PC_TRANSFER_MENU) {
				// PC Transfer
				log(telnet.sendLine("2"));
				log(telnet.waitfor(":"));
				state = State.PC_TRANSFER_MENU;
			}

			if (newState == State.MAIN_MENU) {
				log(telnet.send(Character.toString((char) 27)));
				log(telnet.waitfor(":"));
				state = State.MAIN_MENU;
			}
		}
	}

	@Override
	public String getFile(SymitarFile file) {
		String data = "", temp = "";
		StringBuilder stringBuilder = new StringBuilder();

		if (!connected || file == null || file.getName() == null || file.getName().trim().equals(""))
			return null;

		System.out.println("Requesting File: " + file.getName() + " from Sym " + sym);

		try {
			getToState(State.PC_TRANSFER_MENU);

			// Select which type of upload
			if (file.getType() == FileType.REPGEN) {
				log(telnet.sendLine("3"));
				log(telnet.waitfor(":"));
			} else if (file.getType() == FileType.LETTER) {
				log(telnet.sendLine("1"));
				log(telnet.waitfor(":"));
			} else if (file.getType() == FileType.HELP) {
				log(telnet.sendLine("5"));
				log(telnet.waitfor(":"));
			}

			log(telnet.sendLine(file.getName()));
			temp = telnet.waitfor(":");

			log(temp);

			if (temp.indexOf("No such file") != -1) {
				log(telnet.send(Character.toString((char) 27)));
				log(telnet.waitfor(":"));

				return null;
			}

			log(telnet.sendLine("2"));
			log(telnet.waitfor(":"));

			log(telnet.sendLine(""));
			log(telnet.waitfor(":"));

			log(telnet.sendLine(""));
			log(telnet.waitfor(":"));

			// log(telnet.sendLine(FTPManager.UPLOADUSER));
			log(telnet.waitfor(":"));

			// log(telnet.sendLine(FTPManager.UPLOADPASS));
			log(telnet.waitfor(":"));

			log(telnet.sendLine("1"));
			log(telnet.waitfor(":"));

			log(telnet.sendLine(sym + ".REPGEN." + file.getName()));
			log(telnet.waitfor(":"));

			log(telnet.sendLine(""));
			log(telnet.waitfor("221 Goodbye."));
			log(telnet.waitfor(":"));

			log(telnet.sendLine(""));
			log(telnet.waitfor(":"));

			// Read in the file now
			// BufferedReader in = new BufferedReader(new
			// FileReader(FTPManager.UPLOADDIR + "/" + sym + ".REPGEN." +
			// file.getName()));

			// while((temp=in.readLine())!= null)
			// stringBuilder.append(temp + "\n");

			return stringBuilder.toString();

		} catch (IOException e) {
			return null;
		}

	}

	@Override
	public ArrayList<SymitarFile> getFileList(FileType type, String search) {
		ArrayList<SymitarFile> files = new ArrayList<SymitarFile>();
		String temp = "";

		try {
			getToState(State.MANAGEMENT_MENU);

			if (type == FileType.REPGEN) {
				log(telnet.sendLine("3"));
				log(telnet.waitfor(":"));
			} else if (type == FileType.LETTER) {
				log(telnet.sendLine("4"));
				log(telnet.waitfor(":"));
			} else if (type == FileType.HELP) {
				log(telnet.sendLine("5"));
				log(telnet.waitfor(":"));
			}

			log(telnet.sendLine("1"));
			log(telnet.waitfor(":"));

			log(telnet.sendLine(search));
			log(telnet.waitfor(":"));

			log(telnet.sendLine(""));
			log(telnet.waitfor(":"));

			log(telnet.sendLine("1"));
			log(telnet.waitfor(":"));

			do {
				log(telnet.sendLine(""));
				temp = telnet.waitfor(":").trim();
				log(temp);

				files.addAll(parseFileListPage(temp));
			} while (temp.indexOf("End of File List") == -1);

			log(telnet.sendLine(""));
			log(telnet.waitfor(":"));

			log(telnet.send(Character.toString((char) 27)));
			temp = telnet.waitfor(":");
			log(temp);

			// Bizzarre bug here, when requesting a lot of list files
			if (temp.indexOf("File Name") != -1) {
				// log(telnet.send(Character.toString((char)27)));
				byte[] bytes = { (byte) 27 };
				telnet.write(bytes);
				log(telnet.waitfor(":"));

				log(telnet.send(Character.toString((char) 27)));
				temp = telnet.waitfor(":");
				log(temp);

				state = State.MAIN_MENU;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return files;
		}

		return files;
	}

	/**
	 * Parses one page of file listing
	 * 
	 * @param data
	 * @return
	 */
	private ArrayList<SymitarFile> parseFileListPage(String data) {
		ArrayList<SymitarFile> files = new ArrayList<SymitarFile>();
		String[] lines = data.split("\n");

		for (String currentLine : lines) {
			if (currentLine.length() > 3 && currentLine.substring(1, 4).equals("[1m"))
				files.add(new SymitarFile(currentLine.substring(4, currentLine.length() - 1), FileType.REPGEN));
		}

		return files;
	}

	@Override
	public String getRepGenQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRepgenQueue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ArrayList<Integer> getSequenceNumbers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public SessionError removeFile(SymitarFile file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SessionError runBatchFM(String title) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void runRepGen(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void runRepGen(String name, int queue) {
		// TODO Auto-generated method stub

	}

	@Override
	public SessionError saveFile(SymitarFile file, String text) {
		String temp = "";
		PrintWriter out;

		if (!connected || file == null || file.getName() == null || file.getName().trim().equals(""))
			return null;

		System.out.println("Saving File: " + file.getName() + " from Sym " + sym);

		try {
			// out = new PrintWriter(new FileWriter(FTPManager.UPLOADDIR + "/" +
			// sym + ".REPGEN." + file.getName()));
			// out.print(text.replaceAll("\r\n", "\n"));
			// out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			getToState(State.PC_TRANSFER_MENU);

			// Select which type of upload
			if (file.getType() == FileType.REPGEN) {
				log(telnet.sendLine("9"));
				log(telnet.waitfor(":"));
			} else if (file.getType() == FileType.LETTER) {
				log(telnet.sendLine("7"));
				log(telnet.waitfor(":"));
			} else if (file.getType() == FileType.HELP) {
				log(telnet.sendLine("10"));
				log(telnet.waitfor(":"));
			}

			log(telnet.sendLine("2"));
			log(telnet.waitfor(":"));

			log(telnet.sendLine(""));
			log(telnet.waitfor(":"));

			// log(telnet.sendLine(FTPManager.UPLOADUSER));
			// log(telnet.waitfor(":"));

			// log(telnet.sendLine(FTPManager.UPLOADPASS));
			// log(telnet.waitfor(":"));

			log(telnet.sendLine("1"));
			log(telnet.waitfor(":"));

			log(telnet.sendLine(sym + ".REPGEN." + file.getName()));
			log(telnet.waitfor(":"));

			log(telnet.sendLine(file.getName()));
			log(telnet.waitfor(":"));

			log(telnet.sendLine(""));
			log(telnet.waitfor("221 Goodbye."));
			log(telnet.waitfor(":"));

			log(telnet.sendLine(""));
			log(telnet.waitfor(":"));

		} catch (IOException e) {
			return SessionError.IO_ERROR;
		}

		return SessionError.NONE;
	}

	@Override
	public void waitOnChange() {
		// TODO Auto-generated method stub

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

	public ErrorCheckResult installRepgen(String file) {
		try {
			getToState(State.MANAGEMENT_MENU);

			log(telnet.sendLine("3"));
			log(telnet.waitfor(":"));

			log(telnet.sendLine("8")); // get to the install repgen section
			log(telnet.waitfor(":"));

			log(telnet.sendLine(file));
			log(telnet.waitfor(":"));
			log(telnet.sendLine("Y"));

			log(telnet.waitfor(":"));
			log(telnet.send(Character.toString((char) 27)));
			log(telnet.waitfor(":"));
			log(telnet.send(Character.toString((char) 27)));
			log(telnet.waitfor(":"));

			getToState(State.MANAGEMENT_MENU);
		} catch (IOException e) {
			// ut oh!
		}
		return null;
	}
}
