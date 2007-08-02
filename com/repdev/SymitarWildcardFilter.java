/**
 *  RepDev - RepGen IDE for Symitar
 *  Copyright (C) 2007  Jake Poznanski, Ryan Schultz, Sean Delaney
 *  http://repdev.org/ <support@repdev.org>
 *
 *  This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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