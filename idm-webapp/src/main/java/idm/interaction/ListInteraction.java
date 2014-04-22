package idm.interaction;

import idiro.utils.Tree;
import idiro.workflow.server.interfaces.DFEInteraction;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import javax.faces.model.SelectItem;

/**
 * 
 * @author etienne
 *
 */
public class ListInteraction extends CanvasModalInteraction implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4296790821079880612L;

	/**
	 * "Y" display as comboBox "N" display as radio button list
	 */
	private String comboBox;
	
	/**
	 * List of options
	 */
	private List<SelectItem> listOptions;
	
	/**
	 * The selected option
	 */
	private String selectedListOption;
	
	public ListInteraction(DFEInteraction dfeInter) throws RemoteException {
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
			selectedListOption = listOptions.get(0).getLabel();
		}

		if (inter.getTree().getFirstChild("list")
				.getFirstChild("output").getFirstChild() != null) {
			selectedListOption = inter.getTree()
					.getFirstChild("list").getFirstChild("output")
					.getFirstChild().getHead(); 
			logger.info("value default -> " + selectedListOption);
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
		.add(selectedListOption);
	}

	@Override
	public void setUnchanged() {
		logger.info("value list -> "+ selectedListOption);
		try {
			unchanged = selectedListOption
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
	 * @return the selectedListOption
	 */
	public final String getSelectedListOption() {
		return selectedListOption;
	}

	/**
	 * @param selectedListOption the selectedListOption to set
	 */
	public final void setSelectedListOption(String selectedListOption) {
		this.selectedListOption = selectedListOption;
	}

}
