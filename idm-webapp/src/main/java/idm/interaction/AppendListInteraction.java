package idm.interaction;

import idiro.utils.Tree;
import idiro.workflow.server.interfaces.DFEInteraction;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.faces.model.SelectItem;

public class AppendListInteraction extends CanvasModalInteraction {

	private String comboBox;
	private List<SelectItem> appendListOptions;
	private List<String> selectedAppendListOptions;

	public AppendListInteraction(DFEInteraction dfeInter) {
		super(dfeInter);
	}

	@Override
	public void readInteraction() throws RemoteException {
		logger.info("appendList");
		appendListOptions = new LinkedList<SelectItem>();
		selectedAppendListOptions = new LinkedList<String>();
		
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
					appendListOptions.add(new SelectItem(tree
							.getFirstChild().getHead(), tree
							.getFirstChild().getHead()));
				}
			}
		}

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
				}
			}
		}
		
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
				comboBox = "Y";
			} else {
				comboBox = "N";
			}
		} else {
			comboBox = "Y";
		}
	}

	@Override
	public void writeInteraction() throws RemoteException {
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
