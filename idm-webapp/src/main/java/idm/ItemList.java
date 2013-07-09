package idm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** ItemList
 * 
 * Class to for control of items in a grid. used in screens file system.
 * 
 * @author Igor.Souza
 */
public class ItemList {
	
	private String name;
	private boolean selected;
	private boolean selectedDestination;
	
	private Map<String, String> nameValue = new HashMap();
	private Map<String, String> nameValueEdit = new HashMap();
	private Map<String, Boolean> nameIsConst = new HashMap();
	
	/*
	 *
	 * @author Igor.Souza
	 */
	public ItemList() {
		super();
	}
	
	public ItemList(String name) {
		super();
		this.name = name;
	}

	public List<String> getKeyAsListNameValue(){
		return new ArrayList<String>(nameValue.keySet());
	}
	
	public List<String> getKeyAsListNameValueEdit(){
		return new ArrayList<String>(nameValueEdit.keySet());
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelectedDestination() {
		return selectedDestination;
	}

	public void setSelectedDestination(boolean selectedDestination) {
		this.selectedDestination = selectedDestination;
	}

	public Map<String, String> getNameValue() {
		return nameValue;
	}

	public void setNameValue(Map<String, String> nameValue) {
		this.nameValue = nameValue;
	}

	public Map<String, String> getNameValueEdit() {
		return nameValueEdit;
	}

	public void setNameValueEdit(Map<String, String> nameValueEdit) {
		this.nameValueEdit = nameValueEdit;
	}

	public Map<String, Boolean> getNameIsConst() {
		return nameIsConst;
	}

	public void setNameIsConst(Map<String, Boolean> nameIsConst) {
		this.nameIsConst = nameIsConst;
	}
	
}