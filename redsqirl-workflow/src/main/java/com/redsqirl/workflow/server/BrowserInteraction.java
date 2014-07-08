package com.redsqirl.workflow.server;


import java.rmi.RemoteException;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.enumeration.DisplayType;
/**
 * Implent a browser interaction 
 * @author keith
 *
 */
public class BrowserInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4244649045214883613L;
	/**
	 * Constructor for the browser
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public BrowserInteraction(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, DisplayType.browser, column, placeInColumn);
	}
	/**
	 * Constructor for the browser
	 * @param id
	 * @param name
	 * @param legend
	 * @param texttip
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public BrowserInteraction(String id, String name, String legend,
			String texttip, int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, texttip, DisplayType.browser, column, placeInColumn);
	}
	
	/**
	 * Update the interaction
	 * 
	 * @param in
	 * @throws RemoteException
	 */
	public void update(String newType, String newSubtype) throws RemoteException {
		logger.info("type : " + newType);
		logger.info("subtype : " + newSubtype);
		
		Tree<String> treeDataset = getTree();

		if (treeDataset.getSubTreeList().isEmpty()) {
			treeDataset.add("browse").add("output");
			treeDataset.getFirstChild("browse").add("subtype").add(newSubtype);
			treeDataset.getFirstChild("browse").add("type").add(newType);
		} else {
			Tree<String> oldType = treeDataset.getFirstChild("browse")
					.getFirstChild("type").getFirstChild();

			if (oldType != null && !oldType.getHead().equals(newType)) {
				treeDataset.getFirstChild("browse").remove("type");
				treeDataset.getFirstChild("browse").remove("output");
				treeDataset.getFirstChild("browse").add("output");
				treeDataset.getFirstChild("browse").add("type").add(newType);
				treeDataset.getFirstChild("browse").add("subtype")
						.add(newSubtype);
			}
		}
	}

}
