package idm.dynamictable;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SelectableTable implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4898672980347548506L;
	
	private List<String> titles;
	private List<SelectableRow> rows;
	
	/**
	 * @param titles
	 * @param rows
	 */
	public SelectableTable(LinkedList<String> titles) {
		super();
		this.titles = titles;
		this.rows = new LinkedList<SelectableRow>();
	}
	
	
	/**
	 * @param titles
	 * @param rows
	 */
	public SelectableTable(LinkedList<String> titles, LinkedList<SelectableRow> rows) {
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
	public List<SelectableRow> getRows() {
		return rows;
	}
	
	/**
	 * @param rows the rows to set
	 */
	public void setRows(List<SelectableRow> rows) {
		this.rows = rows;
	}


	/**
	 * @param e
	 * @return
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean add(SelectableRow e) {
		return rows.add(e);
	}


	/**
	 * @param c
	 * @return
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends SelectableRow> c) {
		return rows.addAll(c);
	}

}
