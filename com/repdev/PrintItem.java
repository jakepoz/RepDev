package com.repdev;
import java.util.Date;

public class PrintItem{
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
	}