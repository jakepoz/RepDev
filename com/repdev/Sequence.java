package com.repdev;

import java.text.DateFormat;
import java.util.Date;

/**
 * Storage class for a Sequence
 * @author poznanja
 *
 */
public class Sequence {
	int seq, sym;
	Date date;

	public Sequence(int sym, int seq, Date date) {
		super();
		this.sym = sym;
		this.seq = seq;
		this.date = date;
	}
	@Override
	public String toString() {
		return DateFormat.getDateTimeInstance().format(date) + " - Sequence: "+seq;
	}

	public int getSym() {
		return sym;
	}

	public void setSym(int sym) {
		this.sym = sym;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

}
