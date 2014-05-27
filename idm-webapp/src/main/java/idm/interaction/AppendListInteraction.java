package idm.interaction;

import idiro.utils.Tree;
import idiro.workflow.server.interfaces.DFEInteraction;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.faces.model.SelectItem;


/**
 * Make AppendList object available to client.
 * @author etienne
 *
 */
public class AppendListInteraction extends CanvasModalInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5239443046171155803L;

	/**
	 * "Y" display as comboBox "N" display as check box list
	 */
	private String comboBox;
	
	/**
	 * List of options
	 */
	private List<String> sortedAppendListOptions;
	
	/**
	 * List of options
	 */
	private List<SelectItem> appendListOptions;
	
	/**
	 * List of the selected options
	 */
	private List<String> selectedAppendListOptions;
	
	public AppendListInteraction(DFEInteraction dfeInter) throws RemoteException {
		super(dfeInter);
	}

	@Override
	public void readInteraction() throws RemoteException {
		logger.info("appendList");
		selectedAppendListOptions = new LinkedList<String>();


		// set display type
		if (inter.getTree().getFirstChild("applist")
				.getFirstChild("display") != null
				&& inter.getTree()
				.getFirstChild("applist")
				.getFirstChild("display").getFirstChild() != null) {
			String displayType = inter.getTree()
					.getFirstChild("applist")
					.getFirstChild("display").getFirstChild()
					.getHead();
			if (displayType.equalsIgnoreCase("combobox")) {
				comboBox = "list";
			} else if (displayType.equalsIgnoreCase("sorted")){
				comboBox = "sortedList";
			}
			else{
				comboBox = "checkbox";
			}


		} else {
			comboBox = "list";
		}


		if (comboBox.equals("list") || comboBox.equals("checkbox")){
			appendListOptions = new LinkedList<SelectItem>();
		}
		else{
			sortedAppendListOptions = new LinkedList<String>();
		}

		//primeiro monta lista de selecionados, depois monta a lista de valores
		//se for sorted, so adiciona na lista de valores os que nao forem selecionados

		//set selected value
		if (inter.getTree().getFirstChild("applist")
				.getFirstChild("output") != null &&
				inter.getTree().getFirstChild("applist")
				.getFirstChild("output").getChildren("value") != null) {
			List<Tree<String>> listOut = inter
					.getTree().getFirstChild("applist")
					.getFirstChild("output")
					.getChildren("value");
			if (listOut != null) {
				for (Tree<String> tree : listOut) {
					selectedAppendListOptions.add(tree.getFirstChild()
							.getHead());
					logger.info("read appendList seleted: " + tree.getFirstChild()
							.getHead());
				}
			}
		}




		//set options
		if (inter.getTree().getFirstChild("applist")
				.getFirstChild("values") != null) {
			List<Tree<String>> list = inter.getTree()
					.getFirstChild("applist")
					.getFirstChild("values").getChildren("value");
			if (list != null) {
				logger.info("list not null: " + list.size());
				for (Tree<String> tree : list) {
					logger.info("list value "
							+ tree.getFirstChild().getHead());

					String value = tree
							.getFirstChild().getHead();
					
					if (comboBox.equals("sortedList") && !selectedAppendListOptions.contains(value)){
						sortedAppendListOptions.add(value);
					}
					else if (comboBox.equals("checkbox") || comboBox.equals("list")){
						appendListOptions.add(new SelectItem(value, value));
					}
				}
			}
		}
		
	}

	@Override
	public void writeInteraction() throws RemoteException {
		System.out.println("************************************* writeInteraction");
			inter.getTree().getFirstChild("applist")
			.getFirstChild("output").removeAllChildren();
				for (String s : selectedAppendListOptions) {
					logger.info("appendList seleted: " + s);
					inter.getTree().getFirstChild("applist")
					.getFirstChild("output").add("value").add(s);
				}
	
	}

	@Override
	public void setUnchanged() {
		try {
			List<String> oldValues = new LinkedList<String>();
			Iterator<Tree<String>> it = inter.getTree()
					.getFirstChild("applist").getFirstChild("output")
					.getChildren("value").iterator();
			while (it.hasNext()) {
				oldValues.add(it.next().getFirstChild().getHead());
			}
			unchanged = selectedAppendListOptions.equals(oldValues);
		} catch (Exception e) {
			unchanged = true;
		}
	}

	/**
	 * @return the comboBox
	 */
	public final String getComboBox() {
		return comboBox;
	}

	/**
	 * @param comboBox the comboBox to set
	 */
	public final void setComboBox(String comboBox) {
		this.comboBox = comboBox;
	}

	/**
	 * @return the appendListOptions
	 */
	public final List<SelectItem> getAppendListOptions() {
		return appendListOptions;
	}

	/**
	 * @param appendListOptions the appendListOptions to set
	 */
	public final void setAppendListOptions(List<SelectItem> appendListOptions) {
		this.appendListOptions = appendListOptions;
	}
	
	/**
	 * @return the appendListOptions
	 */
	public final List<String> getSortedAppendListOptions() {
		return sortedAppendListOptions;
	}

	/**
	 * @param appendListOptions the appendListOptions to set
	 */
	public final void setSortedAppendListOptions(List<String> appendListOptions) {
		this.sortedAppendListOptions = appendListOptions;
	}

	/**
	 * @return the selectedAppendListOptions
	 */
	public final List<String> getSelectedAppendListOptions() {
		return selectedAppendListOptions;
	}

	/**
	 * @param selectedAppendListOptions the selectedAppendListOptions to set
	 */
	public final void setSelectedAppendListOptions(
			List<String> selectedAppendListOptions) {
		this.selectedAppendListOptions = selectedAppendListOptions;
	}
}
