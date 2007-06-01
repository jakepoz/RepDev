package com.repdev;

import java.util.Vector;

/**
 * A script handler, that tries to match strings and returns true when it found
 * the string it searched for.
 * <P>
 * <B>Maintainer:</B> Matthias L. Jugel
 * 
 * @version $Id: ScriptHandler.java 499 2005-09-29 08:24:54Z leo $
 * @author Matthias L. Jugel, Marcus Mei�ner
 */
public class ScriptHandler {

	/** debugging level */
	private final static int debug = 0;

	private int matchPos; // current position in the match
	private byte[] match; // the current bytes to look for
	private boolean done = true; // nothing to look for!

	/**
	 * Setup the parser using the passed string.
	 * 
	 * @param match
	 *            the string to look for
	 */
	public void setup(String match) {
		if (match == null)
			return;
		this.match = match.getBytes();
		matchPos = 0;
		done = false;
	}

	/**
	 * Try to match the byte array s against the match string.
	 * 
	 * @param s
	 *            the array of bytes to match against
	 * @param length
	 *            the amount of bytes in the array
	 * @return true if the string was found, else false
	 */
	public boolean match(byte[] s, int length) {
		if (done)
			return true;
		for (int i = 0; !done && i < length; i++) {
			if (s[i] == match[matchPos]) {
				// the whole thing matched so, return the match answer
				// and reset to use the next match
				if (++matchPos >= match.length) {
					done = true;
					return true;
				}
			} else
				matchPos = 0; // get back to the beginning
		}
		return false;
	}
}
