package com.redsqirl.interaction;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.faces.model.SelectItem;

import com.redsqirl.CanvasModalOutputTab;
import com.redsqirl.FileSystemBean;
import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;

public class BrowserInteraction extends CanvasModalInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3020683683280306022L;

	/**
	 * List of the field name
	 */
	private List<String> listFields;

	/**
	 * List of the properties
	 */
	private List<SelectItem> listProperties;

	/**
	 * Object that will manage the output and display it. 
	 * It is supposed that a browser element has only one output.
	 */
	private CanvasModalOutputTab modalOutput;

	public BrowserInteraction(DFEInteraction dfeInter,DataFlowElement dfe,CanvasModalOutputTab outputTab) throws RemoteException {
		super(dfeInter);
		this.modalOutput = outputTab;
		outputTab.resetNameOutput();
		outputTab.updateDFEOutputTable();
	}

	@Override
	public void readInteraction() throws RemoteException {
		// clean the map
		listFields = new LinkedList<String>();
		listProperties = new LinkedList<SelectItem>();
		//logger.info(printTree(inter.getTree()));
		try{
			if (inter.getTree().getFirstChild("browse")
					.getFirstChild("output").getFirstChild("path") != null) {
				setPath(inter.getTree()
						.getFirstChild("browse")
						.getFirstChild("output").getFirstChild("path")
						.getFirstChild().getHead());
				logger.info("path mount " + getPath());
				if (!getPath().startsWith("/")) {
					setPath("/" + getPath());
				}
			}
		}catch(Exception e){
			logger.info("Exception: "+e.getMessage());
			setPath(null);
		}

		//set properties
		try{
			if (inter.getTree().getFirstChild("browse")
					.getFirstChild("output").getChildren("property") != null) {
				List<Tree<String>> props = inter.getTree().getFirstChild("browse")
						.getFirstChild("output").getFirstChild("property").getSubTreeList();
				if (props != null) {
					logger.info("properties not null: " + props.size());
					for (Tree<String> tree : props) {
						listProperties.add(new SelectItem(tree
								.getFirstChild().getHead(),tree.getHead()));
					}
				}
			}
		}catch(Exception e){
			logger.info("Exception: "+e.getMessage());
		}

		//set fields
		try{
			List<Tree<String>> field = inter.getTree().getFirstChild("browse")
					.getFirstChild("output").getChildren("field");
			if (field != null && !field.isEmpty()) {
				logger.info("fields not null: " + field.size());
				for (Tree<String> tree : field) {
					String name = tree.getFirstChild("name").getFirstChild().getHead();
					String type = tree.getFirstChild("type").getFirstChild().getHead();
					listFields.add(name+" "+type);
				}
			}
		}catch(Exception e){
			logger.info("Exception: "+e.getMessage());
		}

		if(modalOutput != null){
			modalOutput.resetNameOutput();
			modalOutput.updateDFEOutputTable();
		}
	}

	@Override
	public void writeInteraction() throws RemoteException {
		inter.getTree().getFirstChild("browse")
		.getFirstChild("output").removeAllChildren();
		inter.getTree().getFirstChild("browse")
		.getFirstChild("output").add("path")
		.add(getPath());
		inter.getTree().getFirstChild("browse")
		.getFirstChild("output").add("name")
		.add("");

		Tree<String> myProperty = inter.getTree()
				.getFirstChild("browse").getFirstChild("output")
				.add("property");
		for (SelectItem item : listProperties) {
			logger.info("Add property: " + item.getLabel()
					+ ": " + item.getValue());
			myProperty.add(item.getLabel()).add(
					item.getValue().toString());
		}

		for (String nameValue : listFields) {
			Tree<String> myField = inter.getTree()
					.getFirstChild("browse")
					.getFirstChild("output").add("field");
			String value[] = nameValue.split(" ");
			myField.add("name").add(value[0]);
			myField.add("type").add(value[1]);
		}
	}

	@Override
	public void setUnchanged() {
		try {
			// Check path
			String oldPath = inter.getTree().getFirstChild("browse")
					.getFirstChild("output").getFirstChild("path")
					.getFirstChild().getHead();
			logger.debug("Comparaison path: " + oldPath + " , "
					+ getPath());
			unchanged = getPath().equals(
					oldPath);

			// Check properties
			if (unchanged) {
				for (SelectItem itemList : listProperties) {
					String key = itemList.getLabel();
					logger.trace("Comparaison property "
							+ key
							+ ": "
							+ itemList.getValue()
							+ " , "
							+ inter.getTree()
							.getFirstChild("browse")
							.getFirstChild("output")
							.getFirstChild("property")
							.getFirstChild(key).getFirstChild()
							.getHead());
					unchanged &= inter.getTree()
							.getFirstChild("browse")
							.getFirstChild("output")
							.getFirstChild("property")
							.getFirstChild(key).getFirstChild()
							.getHead().equals(itemList.getValue());
				}
			}

			// Check fields
			if (unchanged) {
				List<Tree<String>> oldFieldsList = inter.getTree()
						.getFirstChild("browse")
						.getFirstChild("output").getChildren("field");
				logger.debug("comparaison fields: "
						+ oldFieldsList.size() + " , "
						+ listFields.size());
				if (unchanged &= oldFieldsList.size() == listFields.size()) {
					Iterator<Tree<String>> oldFieldIt = oldFieldsList
							.iterator();
					for (String nameValue : listFields) {
						Tree<String> field = oldFieldIt.next();
						String value[] = nameValue.split(" ");
						logger.trace("Comparaison field: "
								+ field.getFirstChild("name")
								.getFirstChild().getHead()
								+ " , "
								+ value[0]
										+ " | type "
										+ field.getFirstChild("type")
										.getFirstChild().getHead()
										+ " , " + value[1]);

						if (field.getFirstChild("name")
								.getFirstChild().getHead()
								.equals(value[0])) {
							unchanged &= field
									.getFirstChild("type")
									.getFirstChild().getHead()
									.equals(value[1]);
						} else {
							unchanged = false;
						}
					}

				}
			}
		} catch (Exception e) {
			unchanged = false;
		}
	}

	/**
	 * @return the listFields
	 */
	public final List<String> getListFields() {
		return listFields;
	}

	/**
	 * @param listFields the listFields to set
	 */
	public final void setListFields(List<String> listFields) {
		this.listFields = listFields;
	}

	/**
	 * @return the listProperties
	 */
	public final List<SelectItem> getListProperties() {
		return listProperties;
	}

	/**
	 * @param listProperties the listProperties to set
	 */
	public final void setListProperties(List<SelectItem> listProperties) {
		this.listProperties = listProperties;
	}

	/**
	 * @return
	 * @see com.redsqirl.CanvasModalOutputTab#getFileSystem()
	 */
	public final FileSystemBean getFileSystem() {
		return modalOutput.getFileSystem();
	}

	/**
	 * @return
	 * @see com.redsqirl.CanvasModalOutputTab#getPath()
	 */
	public String getPath() {
		return modalOutput.getPath();
	}

	/**
	 * @param path
	 * @see com.redsqirl.CanvasModalOutputTab#setPath(java.lang.String)
	 */
	public void setPath(String path) {
		modalOutput.setPath(path);
	}

	/**
	 * @return
	 * @see com.redsqirl.CanvasModalOutputTab#getTitles()
	 */
	public List<String> getGridTitles() {
		return modalOutput != null ? modalOutput.getTitles():null;
	}

	/**
	 * @return
	 * @see com.redsqirl.CanvasModalOutputTab#getRows()
	 */
	public List<String[]> getGridRows() {
		return modalOutput != null ? modalOutput.getRows():null;
	}

}
