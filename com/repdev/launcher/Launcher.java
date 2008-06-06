package com.repdev.launcher;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is a <s>simple</s> launcher for a JAR file that will be wrapped with Launch4J to create
 * a nice friendly repdev.exe that will run for anyone.
 * 
 * Supports logging all output to a file, subversion updating (or not with the arg --no-update)
 * 
 * @author Ryan Schultz
 *
 */
public class Launcher {
	private static final String date = SimpleDateFormat.getInstance().format(new Date());

	public static void main(String[] args) throws Exception {
		boolean log = false;
		boolean noupdate = false;
		for(String arg: args) {
			if( arg.equals("--log") )
				log = true;
			if( arg.equals("--no-update") )
				noupdate = true;
			if( arg.equals("--help") || arg.equals("-h") ) {
				usage();
				System.exit(1);
			}
		}

		if( log ) {
			PrintStream stderr = new PrintStream("stderr.txt");	
			System.setErr(stderr);

			PrintStream stdout = new PrintStream("stdout.txt");
			System.setOut(stdout);

			System.out.println(">> RepDev Started via launcher with logging");
			System.out.println(">> Started: " + date + "\n");
			System.err.println(">> RepDev Started via launcher with logging");
			System.err.println(">> Started: " + date + "\n");
		}

		// Try to update repdev via svn...
		if( !noupdate ) {
			try {
				exec(System.getProperty("user.dir") + "\\svnbin\\svn.exe cleanup");
				exec(System.getProperty("user.dir") + "\\svnbin\\svn.exe update");	
				System.out.println();
				System.err.println();
			} catch (Exception e) {
				System.err.println("Could not update via subversion.  This may not be a big issue, but to update you will need to download new zip packages");
				System.err.println("Error: " + e.getMessage());
			}
		}

		// Finally, we launch repdev...
		com.repdev.RepDevMain.main(args);		
	}

	/**
	 * Execute a command and log it to stderr and stdout...
	 * @param cmd
	 * @return Exit value of the process.
	 */
	private static int exec(String cmd) throws Exception {
		Process p = Runtime.getRuntime().exec(cmd);
		BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));

		// We want this to finish before dumping output...
		p.waitFor();	

		String line = "";
		while( (line = stdout.readLine()) != null )
			System.out.println(line);
		while( (line = stderr.readLine()) != null )
			System.err.println(line);

		stdout.close();
		stderr.close();

		return p.exitValue();
	}
	
	private static void usage() {
		String[] usage = {
				"RepDev Launcher - Usage:",
				"",
				"\tCommand\tAction",
				"\t--help or -h\tThis message",
				"\t--log\tLog all output to stdout.txt and stderr.txt",
				"\t--no-update\tSkip SVN update (faster startup)",
				"",
		};
		
		for( String line: usage )
			System.out.println(line);
	}

}
