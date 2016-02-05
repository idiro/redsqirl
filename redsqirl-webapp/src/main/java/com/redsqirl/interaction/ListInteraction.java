/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl.interaction;


import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;

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

	static private Logger logger = Logger.getLogger(ListInteraction.class);
	
	/**
	 * "Y" display as comboBox "N" display as radio button list
	 */
	private String comboBox;
	
	/**
	 * List of options
	 */
	private List<SelectItem> listOptions;
	
	private List<String> listOptionsString;
	
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
				try{
					logger.info("list value "
							+ tree.getFirstChild().getHead());
					listOptions
					.add(new SelectItem(tree.getFirstChild()
							.getHead(), tree.getFirstChild()
							.getHead()));
				}catch(Exception e){
					logger.warn("Cannot get possible value from "+inter.getId());
				}
			}
			if(!listOptions.isEmpty()){
				selectedListOption = listOptions.get(0).getLabel();
				
				listOptionsString = new LinkedList<String>();
				listOptionsString.add(calcString(listOptions));
			}
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
	
	public String calcString(List<SelectItem> listFields){
		StringBuffer ans = new StringBuffer();
		for (SelectItem selectItem : listFields) {
			ans.append(",'"+selectItem.getLabel()+"'");
		}
		return ans.toString().substring(1);
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

	public List<String> getListOptionsString() {
		return listOptionsString;
	}

	public void setListOptionsString(List<String> listOptionsString) {
		this.listOptionsString = listOptionsString;
	}

}