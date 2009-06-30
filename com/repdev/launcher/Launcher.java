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
		for(String arg: args) {
			if( arg.equals("--log") )
				log = true;
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

		// Finally, we launch repdev...
		com.repdev.RepDevMain.main(args);		
	}

	private static void usage() {
		String[] usage = {
				"RepDev Launcher - Usage:",
				"",
				"\tCommand\tAction",
				"\t--help or -h\tThis message",
				"\t--log\tLog all output to stdout.txt and stderr.txt",
				"",
		};
		
		for( String line: usage )
			System.out.println(line);
	}

}
