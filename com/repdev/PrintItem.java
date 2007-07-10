package com.repdev;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class PrintItem implements Comparable{
		String title;
		int seq, size, pages, batchSeq;
		Date date;
		
		public PrintItem(String title, int seq, int size, int pages, int batchSeq, Date date) {
			super();
			this.title = title;
			this.seq = seq;
			this.size = size;
			this.pages = pages;
			this.batchSeq = batchSeq;
			this.date = date;
		}

		public int getBatchSeq() {
			return batchSeq;
		}

		public void setBatchSeq(int batchSeq) {
			this.batchSeq = batchSeq;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public int getPages() {
			return pages;
		}

		public void setPages(int pages) {
			this.pages = pages;
		}

		public int getSeq() {
			return seq;
		}

		public void setSeq(int seq) {
			this.seq = seq;
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public int compareTo(Object arg0) {
			if( !(arg0 instanceof PrintItem))
				return 0;
			
			PrintItem arg = (PrintItem) arg0;
			
			Calendar cal0 = new GregorianCalendar();
			cal0.setTime(getDate());
			
			Calendar cal1 = new GregorianCalendar();
			cal1.setTime(arg.getDate());
			
			if( cal0.get(Calendar.DAY_OF_YEAR) == cal1.get(Calendar.DAY_OF_YEAR))
				if( getBatchSeq() < arg.getBatchSeq() )
					return -1;
				else if( getBatchSeq() > arg.getBatchSeq() )
					return 1;
				else 
					return 0;
			else
				return cal0.compareTo(cal1);	
		}		
	}