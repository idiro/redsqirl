package idm.interaction;

import idiro.utils.Tree;
import idiro.workflow.server.interfaces.DFEInteraction;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import javax.faces.model.SelectItem;

public class ListInteraction extends CanvasModalInteraction {

	private String comboBox;
	private List<SelectItem> listOptions;
	private String selectedListOptions;
	
	public ListInteraction(DFEInteraction dfeInter) {
		super(dfeInter);
	}

	@Override
	public void readInteraction() throws RemoteException {
		listOptions = new LinkedList<SelectItem>();
		Tree<String> dfetree = inter.getTree();
		Tree<String> values = dfetree.getFirstChild("list").getFirstChild("values");
		List<Tree<String>> list = values.getSubTreeList();

		if (list != null) {
			for (Tree<String> tree : list) {
				logger.info("list value "
						+ tree.getFirstChild().getHead());
				listOptions
						.add(new SelectItem(tree.getFirstChild()
								.getHead(), tree.getFirstChild()
								.getHead()));
			}
			selectedListOptions = listOptions.get(0).getLabel();
		}

		if (inter.getTree().getFirstChild("list")
				.getFirstChild("output").getFirstChild() != null) {
			selectedListOptions = inter.getTree()
					.getFirstChild("list").getFirstChild("output")
					.getFirstChild().getHead(); 
			logger.info("value default -> " + selectedListOptions);
		}

		// check display type
		if (inter.getTree().getFirstChild("list")
				.getFirstChild("display") != null
				&& inter.getTree().getFirstChild("list")
						.getFirstChild("display").getFirstChild() != null) {
			String displayType = inter.getTree()
					.getFirstChild("list").getFirstChild("display")
					.getFirstChild().getHead();
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
		inter.getTree().getFirstChild("list")
		.getFirstChild("output").removeAllChildren();
		inter.getTree().getFirstChild("list")
		.getFirstChild("output")
		.add(selectedListOptions);
	}

	@Override
	public void setUnchanged() {
		logger.info("value list -> "+ selectedListOptions);
		try {
			unchanged = selectedListOptions
					.equals(inter.getTree().getFirstChild("list")
							.getFirstChild("output").getFirstChild()
							.getHead());
		} catch (Exception e) {
			unchanged = false;
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
	 * @return the listOptions
	 */
	public final List<SelectItem> getListOptions() {
		return listOptions;
	}

	/**
	 * @param listOptions the listOptions to set
	 */
	public final void setListOptions(List<SelectItem> listOptions) {
		this.listOptions = listOptions;
	}

	/**
	 * @return the selectedListOptions
	 */
	public final String getSelectedListOptions() {
		return selectedListOptions;
	}

	/**
	 * @param selectedListOptions the selectedListOptions to set
	 */
	public final void setSelectedListOptions(String selectedListOptions) {
		this.selectedListOptions = selectedListOptions;
	}

}
