package com.repdev;

import java.util.Date;

public class SymitarFile {
	private String name;
	private FileType type;
	private Date modified = new Date(0), installed = new Date(0);
	private long size = -1;

	public SymitarFile(String name, FileType type) {
		this.name = name;
		this.type = type;
	}

	public SymitarFile(String name, FileType type, Date modified, long size) {
		this.name = name;
		this.type = type;
		this.modified = modified;
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public FileType getType() {
		return type;
	}

	public String toString() {
		return name;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public boolean equals(Object o) {
		if (!(o instanceof SymitarFile))
			return false;

		SymitarFile file = (SymitarFile) o;
		return name.equals(file.name) && type.equals(file.type);
	}

	public void setInstalled(Date installed) {
		this.installed = installed;
	}

	public Date getInstalled() {
		return installed;
	}
}
