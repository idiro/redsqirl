package idm.dynamictable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class SelectableTable implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4898672980347548506L;

	static protected Logger logger = Logger.getLogger(SelectableTable.class);
	
	private List<String> titles;
	private List<SelectableRow> rows;

	public SelectableTable() {
		this.titles = new LinkedList<String>();
		this.rows = new LinkedList<SelectableRow>();
	}
	
	/**
	 * @param titles
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

	public String getValueRow(int rowNb, int columnNb){
		return rows.get(rowNb).getRow()[columnNb];
	}

	public String getValueRow(int rowNb, String column){
		return rows.get(rowNb).getRow()[titles.indexOf(column)];
	}

	public void setValueRow(int rowNb, int columnNb, String value){
		rows.get(rowNb).getRow()[columnNb] = value;
	}

	public void setValueRow(int rowNb, String column, String value){
		rows.get(rowNb).getRow()[titles.indexOf(column)] = value;
	}

	public boolean add(Map<String,String> row){
		String[] toAdd = new String[titles.size()];
		Iterator<String> it = titles.iterator();
		int i = 0;
		while(it.hasNext()){
			toAdd[i] = row.get(it.next());
		}
		return add(new SelectableRow(toAdd));
	}

	public Map<String,String> getRow(int index){
		Map<String,String> ans = null;
		String[] row = rows.get(index).getRow();
		if(row != null){
			ans = new LinkedHashMap<String,String>();
			for(int i = 0; i < titles.size();++i){
				ans.put(titles.get(i), row[i]);
			}
		}
		return ans;
	}

	public boolean add(String[] e) {
		return rows.add(new SelectableRow(e));
	}

	/**
	 * Return the index of the first selected row
	 * @return
	 */
	public int getSelected(){
		for (int i = 0; i < getRows().size(); i++) {
			SelectableRow selectableRow = getRows().get(i); 
			if(selectableRow.isSelected()){
				return i;
			}
		}
		return -1;
	}

	/**
	 * Return the index of the all the selected rows, sorted by index
	 * @return
	 */
	public List<Integer> getAllSelected(){
		List<Integer> list = new LinkedList<Integer>();
		for (int i = 0; i < getRows().size(); i++) {
			SelectableRow selectableRow = getRows().get(i); 
			if(selectableRow.isSelected()){
				list.add(i);
			}
		}
		logger.info("selected: "+list.toString());
		return list;
	}
	
	public void removeAllSelected(){
		List<Integer> l = getAllSelected();
		for(int i = l.size() -1; i >= 0;--i){
			logger.info(l.get(i));
			rows.remove((int) l.get(i));
		}
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


	/**
	 * @param o
	 * @return
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return titles.indexOf(o);
	}


	/**
	 * @param index
	 * @return
	 * @see java.util.List#remove(int)
	 */
	public SelectableRow remove(int index) {
		return rows.remove(index);
	}

}
