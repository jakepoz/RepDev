package com.repdev.launcher;

import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is a simple launcher for a JAR file that will be wrapped with Launch4J to create
 * a nice friendly repdev.exe that will run for anyone.  
 * 
 * @author schultrd
 *
 */
public class Launcher {
	private static final String date = SimpleDateFormat.getInstance().format(new Date());
	
	public static void main(String[] args) throws Exception {
		boolean log = false;
		for(String arg: args) {
			if( arg.equals("--log") )
				log = true;
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
		try {
			Runtime.getRuntime().exec("svnbin\\svn cleanup");
			Runtime.getRuntime().exec("svnbin\\svn update");
		} catch (IOException e) {
			System.err.println("Could not update via subversion.  This may not be a big issue, but to update you will need to download new zip packages");
		}

		// Finally, we launch repdev...
		com.repdev.RepDevMain.main(args);
	}
}
