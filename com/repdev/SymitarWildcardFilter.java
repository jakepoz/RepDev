package com.repdev;
import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Silly class used by Util for matching file names for mounted local directories
 * @author poznanja
 *
 */
public class SymitarWildcardFilter implements FilenameFilter {
		String matcher;

		public SymitarWildcardFilter(String matcher) {
			this.matcher = matcher.toLowerCase();
		}

		public boolean accept(File dir, String name) {
			// Strip out other things to get just the name we want
			try {
				Pattern p = Pattern.compile(matcher.replaceAll("\\+", ".*"));
				Matcher m = p.matcher(name.toLowerCase());

				return m.matches();

			} catch (Exception e) {
			}

			return true;
		}
}