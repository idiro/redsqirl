package com.redsqirl.useful;

import java.util.Comparator;

import javax.faces.model.SelectItem;

public class SelectItemComparator implements Comparator<SelectItem> {

	public int compare(SelectItem item1, SelectItem item2){
		return item1.getLabel().compareTo(item2.getLabel());
	}

}