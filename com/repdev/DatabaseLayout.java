package com.repdev;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;

public class DatabaseLayout {

	private ArrayList<Record> tree = new ArrayList<Record>(); // Main DB from
																// file
	private ArrayList<Record> flat = new ArrayList<Record>(); // Cached after
																// loading from
																// file

	// Cached after loading to compare easily to tokens in the syntax
	private HashSet<String> lowerCaseRecordNames = new HashSet<String>();
	private HashSet<String> lowerCaseFieldNames = new HashSet<String>();

	public enum DataType {
		CHARACTER(0), DATE(3), NUMBER(4), MONEY(7), RATE(2), CODE(5);

		public int code;

		DataType(int myCode) {
			code = myCode;
		}

	}

	public class Record {
		private ArrayList<Record> subRecords = new ArrayList<Record>();
		private ArrayList<Field> fields = new ArrayList<Field>();
		private String description = "";
		private String name = "";
		private Record root;

		public Record(String name, String desc, Record root) {
			this.description = desc;
			this.name = name;
			this.root = root;
		}

		public void setSubRecords(ArrayList<Record> subRecords) {
			this.subRecords = subRecords;
		}

		public ArrayList<Record> getSubRecords() {
			return subRecords;
		}

		public void setFields(ArrayList<Field> fields) {
			this.fields = fields;
		}

		public ArrayList<Field> getFields() {
			return fields;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String toString() {
			return "Record: " + name;
		}

		public void setRoot(Record root) {
			this.root = root;
		}

		public Record getRoot() {
			return root;
		}

	}

	public class Field implements Comparable {
		private String name = "", description = "";
		private int fieldNumber;
		private DataType dataType;
		private int len;

		public Field(String name, String description, int fieldNumber, DataType dataType, int len) {
			super();
			this.name = name;
			this.description = description;
			this.fieldNumber = fieldNumber;
			this.dataType = dataType;
			this.len = len;
		}

		public int compareTo(Object o) {
			if (o instanceof Field)
				return name.compareToIgnoreCase(((Field) o).getName());
			else
				return -1;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		public DataType getDataType() {
			return dataType;
		}

		public void setDataType(DataType dataType) {
			this.dataType = dataType;
		}

		public int getFieldNumber() {
			return fieldNumber;
		}

		public void setFieldNumber(int fieldNumber) {
			this.fieldNumber = fieldNumber;
		}

		public String toString() {
			return "Field: " + name;
		}

		public void setLen(int len) {
			this.len = len;
		}

		public int getLen() {
			return len;
		}
	}

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

					// System.out.println("On Record: " + recMatcher.group(2) +
					// " Root: " + rootRecord + " depth: " + depth + " last
					// Depth: " + lastDepth);

					currentRecord = new Record(recMatcher.group(2), recMatcher.group(3), rootRecord);

					if (depth == 0)
						tree.add(currentRecord);
					else
						rootRecord.getSubRecords().add(currentRecord);

					lastDepth = depth;
				} else {
					fieldMatcher = fieldPattern.matcher(line);
					fieldMatcher.matches();

					DataType curType = null;
					int len, type;

					type = Integer.parseInt(fieldMatcher.group(5));

					for (DataType cur : DataType.values())
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
