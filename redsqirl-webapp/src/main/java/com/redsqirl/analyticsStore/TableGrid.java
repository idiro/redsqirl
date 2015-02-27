package com.redsqirl.analyticsStore;

import java.util.List;

public class TableGrid {
	
	private List<String> titles;
	private List<String[]> rows;
	
	public TableGrid(List<String> titles, List<String[]> rows){
		this.titles = titles;
		this.rows = rows;
	}

	public List<String> getTitles() {
		return titles;
	}

	public void setTitles(List<String> titles) {
		this.titles = titles;
	}

	public List<String[]> getRows() {
		return rows;
	}

	public void setRows(List<String[]> rows) {
		this.rows = rows;
	}
	
}