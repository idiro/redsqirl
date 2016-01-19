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
import org.apache.log4j.Logger;

@SuppressWarnings("rawtypes")
public class UnselectableTable implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7850738860592864276L;

	private static Logger logger = Logger.getLogger(UnselectableTable.class);
	
	private List<String> columnIds;
	private List<String> titles;
	private List<Comparable[]> rows;
	
	private Integer indexOrdered;
	private Integer order;


	/**
	 * @param columnIds
	 * @param rows
	 */
	public UnselectableTable(LinkedList<String> columnIds) {
		super();
		this.columnIds = columnIds;
		this.rows = new LinkedList<Comparable[]>();
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
	public void sort(){
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

		String sortIdx = params.get("sortIdx");
		try{
			sort(Integer.valueOf(sortIdx));
		}catch(Exception e){}
	}
	
	public void sort(final Integer index){
		logger.info("Sort "+index);
		
		if(index == null || index < 0 || index >= columnIds.size()){
			return;
		}
		
		if(index.equals(indexOrdered)){
			order*=-1;
		}else{
			indexOrdered = index;
			order=1;
		}
		final Integer ord = order;
		Collections.sort(rows, new Comparator<Comparable[]>() {
			@SuppressWarnings("unchecked")
			@Override
			public int compare(Comparable[] o1, Comparable[]o2) {
				if(o1[index] == null){
					return o2[index] == null ? 0 : -ord;
				}else if(o2[index] == null){
					return ord;
				}
				
				return ord*o1[index].compareTo(o2[index]);
			}
		});
	}
	
	/**
	 * @param columnIds
	 * @param rows
	 */
	public UnselectableTable(LinkedList<String> columnIds, LinkedList<Comparable[]> rows) {
		super();
		this.columnIds = columnIds;
		this.rows = rows;
	}


	public Comparable getValueRow(int rowNb, int columnNb){
		return rows.get(rowNb)[columnNb];
	}

	public Comparable getValueRow(int rowNb, String column){
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
		Comparable[] row = rows.get(index);
		if(row != null){
			ans = new LinkedHashMap<String,String>();
			for(int i = 0; i < columnIds.size();++i){
				ans.put(columnIds.get(i), row[i].toString() );
			}
		}
		return ans;
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
	public List<Comparable[]> getRows() {
		return rows;
	}
	/**
	 * @param rows the rows to set
	 */
	public void setRows(List<Comparable[]> rows) {
		this.rows = rows;
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.List#add(java.lang.Comparable)
	 */
	public boolean add(Comparable[] e) {
		return rows.add(e);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends Comparable[]> c) {
		return rows.addAll(c);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#indexOf(java.lang.Comparable)
	 */
	public int columnIdsIndexOf(Comparable o) {
		return columnIds.indexOf(o);
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#remove(int)
	 */
	public Comparable[] remove(int index) {
		return rows.remove(index);
	}

	/**
	 * @return the titles
	 */
	public List<String> getTitles() {
		return titles;
	}

}