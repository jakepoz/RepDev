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

package com.repdev.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Singleton class to load symitar database layout from local cache.
 * Provides many fast routines and cacheing (since it doesn't change during run time) to speed up repgen parsing
 * @author Jake Poznanski
 *
 */
public class DatabaseLayout {

	private ArrayList<Record> tree = new ArrayList<Record>(); // Main DB from file
	private ArrayList<Record> flat = new ArrayList<Record>(); // Cached afterloading from file

	// Cached after loading to compare easily to tokens in the syntax
	private HashSet<String> lowerCaseRecordNames = new HashSet<String>();
	private HashSet<String> lowerCaseFieldNames = new HashSet<String>();

	private DatabaseLayout() {
		Pattern recPattern, fieldPattern;
		Matcher recMatcher, fieldMatcher;
		Record currentRecord = null, rootRecord = null;
		int depth = 0, lastDepth = 0;

		recPattern = Pattern.compile("(.*)\\*\\*\\*\\|(.*)\\|(.*)");
		fieldPattern = Pattern.compile("([\\s]*)([a-zA-Z0-9:]*)\\|(.*)\\|(.*)\\|(.*)\\|(.*)");

		try {
			BufferedReader br = new BufferedReader(new FileReader("db.txt"));
			String line;

			while ((line = br.readLine()) != null) {

				if (line.contains("***")) {
					recMatcher = recPattern.matcher(line);
					recMatcher.matches();

					depth = recMatcher.group(1).length();

					if (depth > lastDepth)
						rootRecord = currentRecord;
					else if (depth < lastDepth) {
						for (int i = depth; i < lastDepth; i++) {
							rootRecord = rootRecord.getRoot();
						}
					}
					
					currentRecord = new Record(recMatcher.group(2), recMatcher.group(3), rootRecord);

					if (depth == 0)
						tree.add(currentRecord);
					else
						rootRecord.getSubRecords().add(currentRecord);

					lastDepth = depth;
				} else {
					fieldMatcher = fieldPattern.matcher(line);
					fieldMatcher.matches();

					VariableType curType = null;
					int len, type;

					type = Integer.parseInt(fieldMatcher.group(5));

					for (VariableType cur : VariableType.values())
						if (cur.code == type) {
							curType = cur;
							break;
						}

					if (!fieldMatcher.group(6).equals("null"))
						len = Integer.parseInt(fieldMatcher.group(6));
					else
						len = -1;

					currentRecord.getFields().add(new Field(fieldMatcher.group(2), fieldMatcher.group(3), Integer.parseInt(fieldMatcher.group(4)), curType, len));
				}
			}

			// Build caches, this speeds things up a whole ton
			flat = getFlatRecordsWorker(tree);

			for (Record cur : flat) {
				lowerCaseRecordNames.add(cur.getName().toLowerCase());

				for (Field fCur : cur.getFields())
					lowerCaseFieldNames.add(fCur.getName().toLowerCase());
			}

			System.out.println("Loaded DB Layout");

			br.close();
		} catch (IOException e) {
		}
	}

	private ArrayList<Record> getFlatRecordsWorker(ArrayList<Record> list) {
		ArrayList<Record> toRet = new ArrayList<Record>();

		for (Record rec : list) {
			toRet.add(rec);
			toRet.addAll(getFlatRecordsWorker(rec.getSubRecords()));
		}

		return toRet;
	}

	public ArrayList<Record> getFlatRecords() {
		return flat;
	}

	/**
	 * Case insensitive search for any record name
	 * 
	 * @param name
	 * @return
	 */
	public boolean containsRecordName(String name) {
		name = name.toLowerCase();

		return lowerCaseRecordNames.contains(name);
	}

	/**
	 * Case insensitve search for any record name
	 * 
	 * @param name
	 * @return
	 */
	public boolean containsFieldName(String name) {
		name = name.toLowerCase();

		return lowerCaseFieldNames.contains(name);
	}

	private static class SingletonHolder {
		private static DatabaseLayout instance = new DatabaseLayout();
	}

	public static DatabaseLayout getInstance() {
		return SingletonHolder.instance;
	}

	public ArrayList<Record> getTreeRecords() {
		return tree;
	}
}
