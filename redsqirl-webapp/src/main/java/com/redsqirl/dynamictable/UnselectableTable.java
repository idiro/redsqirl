package com.redsqirl.dynamictable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.apache.commons.lang.WordUtils;

public class UnselectableTable implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7850738860592864276L;

	private List<String> columnIds;
	private List<String> titles;
	private List<String[]> rows;


	/**
	 * @param columnIds
	 * @param rows
	 */
	public UnselectableTable(LinkedList<String> columnIds) {
		super();
		this.columnIds = columnIds;
		this.rows = new LinkedList<String[]>();
		updateTitles();
	}

	protected void updateTitles(){
		if (columnIds != null) {
			titles = new LinkedList<String>();
			Iterator<String> columnIdsIt = columnIds.iterator();
			while (columnIdsIt.hasNext()) {
				titles.add(WordUtils.capitalizeFully(columnIdsIt.next()
						.replace("_", " ")));
			}
		}
	}
	
	/**
	 * @param columnIds
	 * @param rows
	 */
	public UnselectableTable(LinkedList<String> columnIds, LinkedList<String[]> rows) {
		super();
		this.columnIds = columnIds;
		this.rows = rows;
	}


	public String getValueRow(int rowNb, int columnNb){
		return rows.get(rowNb)[columnNb];
	}

	public String getValueRow(int rowNb, String column){
		return rows.get(rowNb)[columnIds.indexOf(column)];
	}

	public void setValueRow(int rowNb, int columnNb, String value){
		rows.get(rowNb)[columnNb] = value;
	}

	public void setValueRow(int rowNb, String column, String value){
		rows.get(rowNb)[columnIds.indexOf(column)] = value;
	}

	public boolean add(Map<String,String> row){
		String[] toAdd = new String[columnIds.size()];
		Iterator<String> it = columnIds.iterator();
		int i = 0;
		while(it.hasNext()){
			toAdd[i] = row.get(it.next());
		}
		return rows.add(toAdd);
	}

	public Map<String,String> getRow(int index){
		Map<String,String> ans = null;
		String[] row = rows.get(index);
		if(row != null){
			ans = new LinkedHashMap<String,String>();
			for(int i = 0; i < columnIds.size();++i){
				ans.put(columnIds.get(i), row[i]);
			}
		}
		return ans;
	}
	
	public void sortScrollableDataTable(){

		final String nameColumnToSort = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("nameColumnToSort");
		if(nameColumnToSort != null && rows != null && !rows.isEmpty()){
			final int indexCol = getColumnIds().indexOf(nameColumnToSort.toUpperCase());
			if(indexCol != -1){
				//final int asc = rows.get(0)[indexCol].compareTo(rows.get(rows.size()-1)[indexCol]) < 0 ? -1 : 1;
				final int asc = rows.get(0)[indexCol].compareTo(rows.get(rows.size()-1)[indexCol]);
				if(asc < 0 ){
					Collections.sort(getRows(),new Comparator<String[]>() {
						public int compare(String[] aux1, String[] aux2) {
							return aux1[indexCol].compareTo(aux2[indexCol]);
						}
					});
				}else{
					Collections.sort(getRows(),new Comparator<String[]>() {
						public int compare(String[] aux1, String[] aux2) {
							return aux2[indexCol].compareTo(aux1[indexCol]);
						}
					});
				}
			}
		}

	}

	/**
	 * @return the columnIds
	 */
	public List<String> getColumnIds() {
		return columnIds;
	}
	/**
	 * @param columnIds the columnIds to set
	 */
	public void setColumnIds(List<String> columnIds) {
		this.columnIds = columnIds;
		updateTitles();
	}

	/**
	 * @return the rows
	 */
	public List<String[]> getRows() {
		return rows;
	}
	/**
	 * @param rows the rows to set
	 */
	public void setRows(List<String[]> rows) {
		this.rows = rows;
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean add(String[] e) {
		return rows.add(e);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends String[]> c) {
		return rows.addAll(c);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int columnIdsIndexOf(Object o) {
		return columnIds.indexOf(o);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#remove(int)
	 */
	public String[] remove(int index) {
		return rows.remove(index);
	}

	/**
	 * @return the titles
	 */
	public List<String> getTitles() {
		return titles;
	}

}