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


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;


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
	
	static private Logger logger = Logger.getLogger(AppendListInteraction.class);
	
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

	private boolean valid;
	
	public AppendListInteraction(DFEInteraction dfeInter) throws RemoteException {
		super(dfeInter);
	}

	@Override
	public void readInteraction() throws RemoteException {
		logger.debug("appendList");
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
					logger.debug("read appendList selected: " + tree.getFirstChild()
							.getHead());
				}
			}
		}




		//set options
		List<String> posValues = new LinkedList<String>();
		if (inter.getTree().getFirstChild("applist")
				.getFirstChild("values") != null) {
			List<Tree<String>> list = inter.getTree()
					.getFirstChild("applist")
					.getFirstChild("values").getChildren("value");
			if (list != null) {
				logger.debug("list not null: " + list.size());
				for (Tree<String> tree : list) {
					logger.debug("list value "
							+ tree.getFirstChild().getHead());

					String value = tree
							.getFirstChild().getHead();
					
					posValues.add(value);
					if (comboBox.equals("sortedList") && !selectedAppendListOptions.contains(value)){
						sortedAppendListOptions.add(value);
					}
					else if (comboBox.equals("checkbox") || comboBox.equals("list")){
						appendListOptions.add(new SelectItem(value, value));
					}
				}
			}
		}
		
		if(!posValues.containsAll(selectedAppendListOptions)){
			valid = false;
		}
		
	}

	@Override
	public void writeInteraction() throws RemoteException {
			inter.getTree().getFirstChild("applist")
			.getFirstChild("output").removeAllChildren();
				for (String s : selectedAppendListOptions) {
					logger.debug("appendList seleted: " + s);
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
