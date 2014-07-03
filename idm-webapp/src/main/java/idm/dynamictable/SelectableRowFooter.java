package idm.dynamictable;

import java.util.LinkedList;
import java.util.List;

public class SelectableRowFooter extends SelectableRow{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3349396297938403075L;

	private LinkedList<String> target;
	private LinkedList<String> actions;
	
	public SelectableRowFooter(String[] row, LinkedList<String> actions, LinkedList<String> target) {
		super(row);
		this.actions = actions;
		this.target = target;
	}
	
	public SelectableRowFooter(String[] row, LinkedList<String> actions) {
		super(row);
		this.target = new LinkedList<String>();
		this.actions = actions;
	}
	
	public List<String> getSource(){
		LinkedList<String> source = new LinkedList<String>();
		source.addAll(actions);
		source.removeAll(target);
		return source;
	}

	public List<String> getTarget() {
		return target;
	}
	
	public void setTarget(List<String> target) {
		this.target = new LinkedList<String>();
		this.target.addAll(target);
	}

	public List<String> getActions() {
		return actions;
	}
	
	public void setActions(List<String> actions) {
		this.target = new LinkedList<String>();
		this.actions.addAll(actions);
	}
	
}