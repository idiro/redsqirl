package com.redsqirl.dynamictable;

import java.util.LinkedList;
import java.util.List;

public class SelectableRowFooter extends SelectableRow{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3349396297938403075L;

	private List<String> target;
	private List<String> actions;
	
	public SelectableRowFooter(String[] row, List<String> actions, List<String> target) {
		super(row);
		this.actions = actions;
		this.target = target;
	}
	
	public SelectableRowFooter(String[] row, List<String> actions) {
		super(row);
		this.target = new LinkedList<String>();
		this.actions = actions;
	}
	
	public List<String> getSource(){
		List<String> source = new LinkedList<String>();
		source.addAll(actions);
		source.removeAll(target);
		return source;
	}

	public List<String> getTarget() {
		return target;
	}
	
	public void setTarget(List<String> target) {
		this.target = target;
	}

	public List<String> getActions() {
		return actions;
	}

	public void setActions(List<String> actions) {
		this.actions = actions;
	}
	
}