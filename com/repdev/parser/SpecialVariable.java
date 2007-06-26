package com.repdev.parser;

public class SpecialVariable{
		String name, description, type;
		int len;

		public SpecialVariable(String name, String description, String type, int len) {
			super();
			this.name = name;
			this.description = description;
			this.type = type;
			this.len = len;
		}

		public SpecialVariable(String name, String description, String type) {
			super();
			this.name = name;
			this.description = description;
			this.type = type;
		}

		public int getLen() {
			return len;
		}

		public void setLen(int len) {
			this.len = len;
		}
		
		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
		
		
	}