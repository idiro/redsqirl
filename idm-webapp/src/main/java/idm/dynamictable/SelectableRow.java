package idm.dynamictable;

import java.io.Serializable;

public class SelectableRow implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8193933708078904094L;
	String[] row;
	boolean selected;
	
	/**
	 * @param selected
	 * @param row
	 */
	public SelectableRow(String[] row,boolean selected) {
		super();
		this.row = row;
		this.selected = selected;
	}
	
	/**
	 * @param selected
	 * @param row
	 */
	public SelectableRow(String[] row) {
		super();
		this.row = row;
		this.selected = false;
	}

	/**
	 * @return the selected
	 */
	public final boolean isSelected() {
		return selected;
	}

	/**
	 * @param selected the selected to set
	 */
	public final void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * @return the row
	 */
	public final String[] getRow() {
		return row;
	}

	/**
	 * @param row the row to set
	 */
	public final void setRow(String[] row) {
		this.row = row;
	}
}
