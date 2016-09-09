/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

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

import com.redsqirl.BaseBean;

public class SelectableTable extends BaseBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4898672980347548506L;

	static protected Logger logger = Logger.getLogger(SelectableTable.class);

	private List<String> columnIds;
	private List<String> titles;
	private List<String> filters;
	private List<SelectableRow> rows;
	private String rowNumber;
	private String name;

	public SelectableTable() {
		this.columnIds = new LinkedList<String>();
		this.rows = new LinkedList<SelectableRow>();
		updateTitles();
	}

	/**
	 * @param columnIds
	 */
	public SelectableTable(LinkedList<String> columnIds) {
		super();
		this.columnIds = columnIds;
		this.rows = new LinkedList<SelectableRow>();
		updateTitles();
	}

	protected void updateTitles(){
		if (columnIds != null) {
			titles = new LinkedList<String>();
			filters = new LinkedList<String>();
			Iterator<String> columnIdsIt = columnIds.iterator();
			while (columnIdsIt.hasNext()) {
				filters.add("");
				titles.add(WordUtils.capitalizeFully(columnIdsIt.next()
						.replace("_", " ")));
			}
			
		}
	}

	/**
	 * @param columnIds
	 * @param rows
	 */
	public SelectableTable(LinkedList<String> columnIds,
			LinkedList<SelectableRow> rows) {
		super();
		this.columnIds = columnIds;
		this.rows = rows;
	}

	public SelectableTable(String[] columnIds) {
		super();
		this.columnIds = new LinkedList<String>();
		for(String colId:columnIds){
			this.columnIds.add(colId);
		}
		this.rows = new LinkedList<SelectableRow>();
		updateTitles();
	}

	public String getValueRow(int rowNb, int columnNb) {
		return rows.size() > rowNb?rows.get(rowNb).getRow()[columnNb]:null;
	}

	public String getValueRow(int rowNb, String column) {
		return rows.size() > rowNb?rows.get(rowNb).getRow()[columnIds.indexOf(column)]:null;
	}

	public void setValueRow(int rowNb, int columnNb, String value) {
		if(rows.size() > rowNb && columnIds.size() > columnNb){
			rows.get(rowNb).getRow()[columnNb] = value;
		}
	}

	public void setValueRow(int rowNb, String column, String value) {
		if(rows.size() > rowNb && columnIds.indexOf(column) >= 0){
			rows.get(rowNb).getRow()[columnIds.indexOf(column)] = value;
		}
	}

	public boolean filterOnParam0(Object value){
		return filterOn(value, 0);
	}
	public boolean filterOnParam1(Object value){
		return filterOn(value, 1);
	}
	public boolean filterOnParam2(Object value){
		return filterOn(value, 2);
	}
	public boolean filterOnParam3(Object value){
		return filterOn(value, 3);
	}
	public boolean filterOnParam4(Object value){
		return filterOn(value, 4);
	}
	public boolean filterOnParam5(Object value){
		return filterOn(value, 5);
	}
	public boolean filterOnParam6(Object value){
		return filterOn(value, 6);
	}
	public boolean filterOnParam7(Object value){
		return filterOn(value, 7);
	}
	public boolean filterOnParam8(Object value){
		return filterOn(value, 8);
	}
	public boolean filterOnParam9(Object value){
		return filterOn(value, 9);
	}
	
	public boolean filterOn(Object value, int index){
		return value.toString().contains(filters.get(index));
	}

	public boolean add(Map<String, String> row) {
		String[] toAdd = new String[columnIds.size()];
		Iterator<String> it = columnIds.iterator();
		int i = 0;
		while (it.hasNext()) {
			toAdd[i++] = row.get(it.next());
		}
		return add(new SelectableRow(toAdd));
	}

	public Map<String, String> getRow(int index) {
		Map<String, String> ans = new LinkedHashMap<String, String>();
		if(rows.size() > index){
			String[] row = rows.get(index).getRow();
			if (row != null) {
				for (int i = 0; i < columnIds.size(); ++i) {
					ans.put(columnIds.get(i), row[i]);
				}
			}
		}
		return ans;
	}

	public boolean add(String[] e) {
		return rows.add(new SelectableRow(e));
	}

	public boolean add(String[] e, boolean selected) {
		return rows.add(new SelectableRow(e, selected));
	}

	/**
	 * Return the index of the first selected row
	 * 
	 * @return
	 */
	public int getSelected() {
		for (int i = 0; i < getRows().size(); i++) {
			SelectableRow selectableRow = getRows().get(i);
			if (selectableRow.isSelected()) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Return the index of the all the selected rows, sorted by index
	 * 
	 * @return
	 */
	public List<Integer> getAllSelected() {
		List<Integer> list = new LinkedList<Integer>();
		for (int i = 0; i < getRows().size(); i++) {
			SelectableRow selectableRow = getRows().get(i);
			if (selectableRow.isSelected()) {
				list.add(i);
			}
		}
		logger.info("selected: " + list.toString());
		return list;
	}
	
	public void unselectAll(){
		for (int i = 0; i < getRows().size(); i++) {
			getRows().get(i).setSelected(false);
		}
	}

	public void removeAllSelected() {
		List<Integer> l = getAllSelected();
		for (int i = l.size() - 1; i >= 0; --i) {
			logger.info(l.get(i));
			rows.remove((int) l.get(i));
		}
	}

	public void goUp() {
		List<SelectableRow> list = getRows();
		List<Integer> listSelected = getAllSelected();
		for (int i = 0; i < listSelected.size(); i++) {
			int index = listSelected.get(i);
			if(index > 0 && index != i){
				list.add(index-1, list.get(index));
				list.remove(index+1);
			}
		}
	}

	public void goDown() {
		List<SelectableRow> list = getRows();
		List<Integer> listSelected = getAllSelected();
		for (int i = listSelected.size()-1; i >=0 ; i--) {
			int index = listSelected.get(i);
			logger.info("go down: "+list.size()+" "+index+" "+listSelected.size()+" "+i);
			if( list.size() - index != listSelected.size() - i){
				if(index < getRows().size()-2){
					list.add(index+2, list.get(index));
					list.remove(index);
				}else{
					list.add(list.get(index));
					list.remove(index);
				}
			}
		}
	}

	public void goFirst() {
		List<SelectableRow> list = getRows();
		List<Integer> listSelected = getAllSelected();
		for (int i = 0; i < listSelected.size(); i++) {
			int index = listSelected.get(i);
			list.add(i, list.get(index));
			list.remove(index+1);
		}
	}

	public void goLast() {
		List<SelectableRow> list = getRows();
		List<Integer> listSelected = getAllSelected();
		for (int i = 0; i < listSelected.size(); i++) {
			int index = listSelected.get(i);
			list.add(list.get(index-i));
			list.remove(index-i);
		}
	}

	public void goNumberRow() {
		String regex = "([0-9]*)";
		List<SelectableRow> list = getRows();
		List<Integer> listSelected = getAllSelected();
		if(getRowNumber() != null && checkString(regex, getRowNumber())){
			int newLine = Integer.parseInt(getRowNumber());
			if(newLine == 0){
				goFirst();
			}else if(newLine > list.size()){
				goLast();
			}else{
				int lineTmp = newLine;
				for (int i = 0; i < listSelected.size(); i++) {
					int index = listSelected.get(i);
					logger.info("go number: "+list.size()+" "+index+" "+listSelected.size()+" "+i+" "+lineTmp);
					if(lineTmp > index){
						list.add(lineTmp , list.get(index-i));
						list.remove(index-i);
					}else{
						list.add(lineTmp-1, list.get(index));
						list.remove(index+1);
						++lineTmp;
					}
				}
			}
		}
		setRowNumber(null);
	}
	
	
	
	public void sortRows(){
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String collun = params.get("column");
		sortRows(collun);
	}
	
	public void sortRows(String collun){
		final int index = getTitles().indexOf(collun);
		if(index != -1){
			List<SelectableRow> list = getRows();
			Collections.sort(list, new Comparator<SelectableRow>() {
				@Override
				public int compare(SelectableRow s1, SelectableRow s2) {
					return s1.getRow()[index].compareTo(s2.getRow()[index]); 
				}
			});
		}
	}



	/**
	 * @return the columnIds
	 */
	public List<String> getColumnIds() {
		return columnIds;
	}

	/**
	 * @param columnIds
	 *            the columnIds to set
	 */
	public void setColumnIds(List<String> columnIds) {
		this.columnIds = columnIds;
		updateTitles();
	}

	/**
	 * @return the rows
	 */
	public List<SelectableRow> getRows() {
		return rows;
	}

	/**
	 * @param rows
	 *            the rows to set
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
	public SelectableRow remove(int index) {
		return rows.remove(index);
	}

	/**
	 * @return the titles
	 */
	public List<String> getTitles() {
		return titles;
	}

	public String getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(String rowNumber) {
		this.rowNumber = rowNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTitles(List<String> titles) {
		this.titles = titles;
	}

	public final List<String> getFilters() {
		return filters;
	}

	public final void setFilters(List<String> filters) {
		this.filters = filters;
	}

}