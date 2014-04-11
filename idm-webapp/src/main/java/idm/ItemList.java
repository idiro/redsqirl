package idm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.richfaces.model.Ordering;

/**
 * ItemList
 * 
 * Class to for control of items in a grid. used in screens file system.
 * 
 * @author Igor.Souza
 */
public class ItemList implements Serializable {

	private String name;
	private boolean selected;
	private String stringSelectedDestination;
	private String idSelected;
	private String property;
	private String value;
	private List<SelectItem> objs = new LinkedList<SelectItem>();
	private List<SelectItem> objsedit = new LinkedList<SelectItem>();

	private String file = "N";
	
	/*
	 
	private Map<String, String> typeTableInteraction = new HashMap<String, String>();
	private Map<String, String> nameValue = new HashMap<String, String>();
	private Map<String, Ordering> sortingOrder = new HashMap<String, Ordering>();
	private Map<String, Object> filterValue = new HashMap<String, Object>();
	private Map<String, String> nameValueEdit = new HashMap<String, String>();
	private Map<String, String> nameValueGrid = new HashMap<String, String>();
	*/
	
	private Map<String, Boolean> nameIsConst = new HashMap<String, Boolean>();
	private Map<String, Boolean> valueHasLineBreak = new HashMap<String, Boolean>();
	
	/*
	 *
	 * @author Igor.Souza
>>>>>>> refs/heads/master
	 */
	private Map<String, Boolean> nameIsBool = new HashMap<String, Boolean>();
	private static Logger logger = Logger.getLogger(ItemList.class);

	public ItemList() {
		super();
	}

	public ItemList(String name) {
		super();
		this.name = name;
	}

	public ItemList(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public List<String> getKeyAsListNameValue() {
		List<String> keys = new LinkedList<String>();
		for (SelectItem ob : objs) {
			keys.add(ob.getLabel());
		}
		return keys;
	}

	public List<SelectItem> getObjs() {
		return this.objs;
	}

	public List<SelectItem> getObjsEdit() {
		return this.objsedit;
	}

	public List<String> getKeyAsListNameValueEdit() {
		List<String> keys = new LinkedList<String>();
		for (SelectItem ob : objsedit) {
			keys.add(ob.getLabel());
		}
		return keys;
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

	public void setStringSelectedDestination(String selectedDestination) {
		this.stringSelectedDestination = selectedDestination;
	}

	public String getStringSelectedDestination() {
		return stringSelectedDestination;
	}

	public boolean isSelectedDestination() {
		return ("true").equals(stringSelectedDestination);
	}

	public void setSelectedDestination(boolean selectedDestination) {
		this.stringSelectedDestination = String.valueOf(selectedDestination);
	}

	public Map<String, String> getNameValue() {
		Map<String, String> nameValue = new LinkedHashMap<String, String>();
		for (SelectItem ob : objs) {
			nameValue.put((String) ob.getValue(), ob.getLabel());
		}
		return nameValue;
	}

	public void setNameValue(Map<String, String> nameValue) {
		List<SelectItem> newNameValue = new LinkedList<SelectItem>();
		Iterator<String> keySet = nameValue.keySet().iterator();
		while (keySet.hasNext()) {
			String key = keySet.next();
			newNameValue.add(new SelectItem(key, nameValue.get(key)));
		}
		this.objs = newNameValue;
	}

	public Map<String, String> getNameValueEdit() {
		Map<String, String> nameValue = new LinkedHashMap<String, String>();
		for (SelectItem ob : objsedit) {
			String newVal = null;
			try {
				if (ob.getValue() instanceof Boolean) {
					logger.info("converting a boolean : "+ob.getLabel());
					newVal = String.valueOf((Boolean) ob.getValue());
				} else if (ob.getValue() instanceof Integer) {
					newVal = String.valueOf((Integer) ob.getValue());
				} else if (ob.getValue() instanceof String) {
					newVal = String.valueOf((String) ob.getValue());
				} else if (ob.getValue() instanceof Double) {
					newVal = String.valueOf((Double) ob.getValue());
				}
			} catch (Exception e) {
				logger.error("Error : " + e.getMessage());
			}
			nameValue.put(newVal, ob.getLabel());
		}
		return nameValue;
	}

	public void setNameValueEdit(Map<String, String> nameValueEdit) {
		List<SelectItem> newNameValue = new LinkedList<SelectItem>();
		Iterator<String> keySet = nameValueEdit.keySet().iterator();
		while (keySet.hasNext()) {
			String key = keySet.next();
			newNameValue.add(new SelectItem(key, nameValueEdit.get(key)));
		}
		this.objsedit = newNameValue;
	}

	public Map<String, Boolean> getNameIsConst() {
		return nameIsConst;
	}

	public void setNameIsConst(Map<String, Boolean> nameIsConst) {
		this.nameIsConst = nameIsConst;
	}

	public String getIdSelected() {
		return idSelected;
	}

	public void setIdSelected(String idSelected) {
		this.idSelected = idSelected;
	}

	public Map<String, String> getTypeTableInteraction() {
		return null;// typeTableInteraction;
	}

	public void setTypeTableInteraction(Map<String, String> typeTableInteraction) {
		// this.typeTableInteraction = typeTableInteraction;
	}

	public Map<String, Boolean> getValueHasLineBreak() {
		return valueHasLineBreak;
	}

	public void setValueHasLineBreak(Map<String, Boolean> valueHasLineBreak) {
		this.valueHasLineBreak = valueHasLineBreak;
	}

	public String getProperty() {
		return property;
	}

	public String getValue() {
		return value;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Map<String, Boolean> getNameIsBool() {
		return nameIsBool;
	}

	public void setNameIsBool(Map<String, Boolean> nameIsBool) {
		this.nameIsBool = nameIsBool;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}
	
}
