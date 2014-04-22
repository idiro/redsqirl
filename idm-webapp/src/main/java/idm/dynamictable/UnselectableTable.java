package idm.dynamictable;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UnselectableTable implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7850738860592864276L;
	
	private List<String> titles;
	private List<Map<String,String>> rows;
	

	/**
	 * @param titles
	 * @param rows
	 */
	public UnselectableTable(LinkedList<String> titles) {
		super();
		this.titles = titles;
		this.rows = new LinkedList<Map<String,String>>();
	}
	
	/**
	 * @param titles
	 * @param rows
	 */
	public UnselectableTable(LinkedList<String> titles, LinkedList<Map<String, String>> rows) {
		super();
		this.titles = titles;
		this.rows = rows;
	}
	
	/**
	 * @return the titles
	 */
	public List<String> getTitles() {
		return titles;
	}
	/**
	 * @param titles the titles to set
	 */
	public void setTitles(List<String> titles) {
		this.titles = titles;
	}
	
	/**
	 * @return the rows
	 */
	public List<Map<String,String>> getRows() {
		return rows;
	}
	/**
	 * @param rows the rows to set
	 */
	public void setRows(List<Map<String,String>> rows) {
		this.rows = rows;
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean add(Map<String, String> e) {
		return rows.add(e);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends Map<String, String>> c) {
		return rows.addAll(c);
	}
	
	
}
