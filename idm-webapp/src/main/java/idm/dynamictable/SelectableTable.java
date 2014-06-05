package idm.dynamictable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

public class SelectableTable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4898672980347548506L;

	static protected Logger logger = Logger.getLogger(SelectableTable.class);

	private List<String> columnIds;
	private List<String> titles;
	private List<SelectableRow> rows;

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
	public SelectableTable(LinkedList<String> columnIds,
			LinkedList<SelectableRow> rows) {
		super();
		this.columnIds = columnIds;
		this.rows = rows;
	}

	public String getValueRow(int rowNb, int columnNb) {
		return rows.get(rowNb).getRow()[columnNb];
	}

	public String getValueRow(int rowNb, String column) {
		return rows.get(rowNb).getRow()[columnIds.indexOf(column)];
	}

	public void setValueRow(int rowNb, int columnNb, String value) {
		rows.get(rowNb).getRow()[columnNb] = value;
	}

	public void setValueRow(int rowNb, String column, String value) {
		rows.get(rowNb).getRow()[columnIds.indexOf(column)] = value;
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
		Map<String, String> ans = null;
		String[] row = rows.get(index).getRow();
		if (row != null) {
			ans = new LinkedHashMap<String, String>();
			for (int i = 0; i < columnIds.size(); ++i) {
				ans.put(columnIds.get(i), row[i]);
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

	public void removeAllSelected() {
		List<Integer> l = getAllSelected();
		for (int i = l.size() - 1; i >= 0; --i) {
			logger.info(l.get(i));
			rows.remove((int) l.get(i));
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
	
	public void goUp() {
		List<SelectableRow> list = getRows();
		List<Integer> listSelected = getAllSelected();
		for (int i = 0; i < listSelected.size(); i++) {
			int index = listSelected.get(i);
			if(index > 0){
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
			if(index < getRows().size()-2){
				list.add(index+2, list.get(index));
				list.remove(index);
			}else{
				list.add(list.get(index-i));
				list.remove(index-i);
			}
		}
	}
	
	public void goFirst() {
		List<SelectableRow> list = getRows();
		List<Integer> listSelected = getAllSelected();
		for (int i = 0; i < listSelected.size(); i++) {
			int index = listSelected.get(i);
			list.add(0, list.get(index));
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

}