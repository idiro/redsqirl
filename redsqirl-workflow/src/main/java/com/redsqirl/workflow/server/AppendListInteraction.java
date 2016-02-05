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

package com.redsqirl.workflow.server;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.enumeration.DisplayType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEInteractionChecker;
import com.redsqirl.workflow.utils.LanguageManagerWF;
/**
 * Generic Class that implements an interaction using an append list
 * @author keith
 *
 */
public class AppendListInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6059678937933664413L;
	
	/**
	 * Constructor
	 * @param id of interaction
	 * @param name of interaction
	 * @param legend the descritpion
	 * @param column which column to place into
	 * @param placeInColumn where in the column to place
	 * @throws RemoteException
	 */
	public AppendListInteraction(String id,String name, String legend,
			int column, int placeInColumn)
					throws RemoteException {
		super(id,name, legend, DisplayType.appendList, 
				column, placeInColumn);
		init();
		setSorted(false);
	}
	/**
	 * Constructor
	 * @param id of interaction
	 * @param name of interaction
	 * @param legend the descritpion
	 * @param texttip The text tip
	 * @param column which column to place into
	 * @param placeInColumn where in the column to place
	 * @throws RemoteException
	 */
	public AppendListInteraction(String id,String name, String legend,
			String texttip, int column, int placeInColumn)
					throws RemoteException {
		super(id,name, legend, texttip, DisplayType.appendList, 
				column, placeInColumn);
		init();
		setSorted(false);
	}
	
	/**
	 * Constructor
	 * @param id of interaction
	 * @param name of interaction
	 * @param legend the descritpion
	 * @param column which column to place into
	 * @param placeInColumn where in the column to place
	 * @param sorted Keep the output list in order
	 * @throws RemoteException
	 */
	public AppendListInteraction(String id,String name, String legend,
			int column, int placeInColumn, boolean sorted)
					throws RemoteException {
		super(id,name, legend, DisplayType.appendList, 
				column, placeInColumn);
		init();
		setSorted(sorted);
	}
	/**
	 * Constructor
	 * @param id of interaction
	 * @param name of interaction
	 * @param legend the descrition
	 * @param texttip the text tip
	 * @param column which column to place into
	 * @param placeInColumn where in the column to place
	 * @param sorted Keep the output list in order
	 * @throws RemoteException
	 */
	public AppendListInteraction(String id,String name, String legend,
			String texttip, int column, int placeInColumn, boolean sorted)
					throws RemoteException {
		super(id,name, legend, texttip, DisplayType.appendList, 
				column, placeInColumn);
		init();
		setSorted(sorted);
	}
	/**
	 * Initialize the tree
	 * @throws RemoteException
	 */
	protected void init() throws RemoteException{
		Tree<String> list= null;
		if(tree.isEmpty()){
			list = tree.add("applist");
			list.add("values");
			list.add("output");
		}else{
			reInitAfterError();
		}
	}
	
	protected void reInitAfterError() throws RemoteException{
		if(tree.getFirstChild("applist") == null){
			tree.add("applist");
		}
		if(tree.getFirstChild("applist").getFirstChild("values") == null){
			tree.getFirstChild("applist").add("values");
		}
		if(tree.getFirstChild("applist").getFirstChild("output") == null){
			tree.getFirstChild("applist").add("output");
		}
	}
	/**
	 * Set if the Display uses checkbox or not
	 * @param checkBox
	 * @throws RemoteException
	 */
	public void setDisplayCheckBox(boolean checkBox) throws RemoteException{
		if(checkBox){
			if(tree.getFirstChild("applist").getFirstChild("display") != null){
				tree.getFirstChild("applist").getFirstChild("display").removeAllChildren();
				tree.getFirstChild("applist").getFirstChild("display").add("");
			}else{
				tree.getFirstChild("applist").add("display").add("checkbox");
			}
		}else{
			if(tree.getFirstChild("applist").getFirstChild("display") != null){
				tree.getFirstChild("applist").remove("display");
			}
		}
	}
	
	/**
	 * Set if the list is sorted or not
	 * @param sorted
	 * @throws RemoteException
	 */
	public void setSorted(boolean sorted) throws RemoteException{
		if(sorted){
			if(tree.getFirstChild("applist").getFirstChild("display") != null){
				tree.getFirstChild("applist").getFirstChild("display").removeAllChildren();
				tree.getFirstChild("applist").getFirstChild("display").add("");
			}else{
				tree.getFirstChild("applist").add("display").add("sorted");
			}
		}else{
			if(tree.getFirstChild("applist").getFirstChild("display") != null){
				tree.getFirstChild("applist").remove("display");
			}
		}
	}
	
	public void setNonEmptyChecker() throws RemoteException{
		setChecker(new DFEInteractionChecker() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 4743871962800977873L;

			@Override
			public String check(DFEInteraction interaction) throws RemoteException {
				try{
					return tree.getFirstChild("applist").getFirstChild("output").getChildren("value").size() > 0 ?
							null: LanguageManagerWF.getText("AppendListInteraction.empty");
				}catch(Exception e){
					return LanguageManagerWF.getText("AppendListInteraction.empty");
				}
			}
		});
	}
	
	/**
	 * Get the list of possible values that can be used
	 * @return The list of possible values that can be used
	 */
	public List<String> getPossibleValues(){
		return getPossibleValuesFromList();
	}
	/**
	 * Set the list of possible values for the interaction
	 * @param values 
	 * @throws RemoteException
	 */
	public void setPossibleValues(List<String> values) throws RemoteException{
		Tree<String> vals = null;
		try{
			vals = tree.getFirstChild("applist").getFirstChild("values");
			vals.removeAllChildren();
		}catch(Exception e){
			logger.warn(getId()+": Tree structure incorrect",e);
			reInitAfterError();
			vals = tree.getFirstChild("applist").getFirstChild("values");
		}
		Iterator<String> it = values.iterator();
		while(it.hasNext()){
			vals.add("value").add(it.next());
		}
	}
	
	/**
	 * Get the values selected for the interaction
	 * @return List of values selected
	 * @throws RemoteException
	 */
	public List<String> getValues() throws RemoteException{
		List<String> values = null;
		values = new LinkedList<String>();
		List<Tree<String>> lRow = null;
		Iterator<Tree<String>> rows = null;
		try{
			lRow = getTree()
					.getFirstChild("applist").getFirstChild("output").getChildren("value");
			rows = lRow.iterator();
			while(rows.hasNext()){
				values.add(rows.next().getFirstChild().getHead());
			}
		}catch(Exception e){
			logger.warn(getId()+": Tree structure incorrect",e);
			reInitAfterError();
		}
		return values;
	}
	/**
	 * Set the values to be selected
	 * @param values
	 * @return Error message
	 * @throws RemoteException
	 */
	public String setValues(List<String> values)  throws RemoteException{
		String error = null;
		Tree<String> output = null;
		try{
			output = tree.getFirstChild("applist").getFirstChild("output");
			output.removeAllChildren();
		}catch(Exception e){
			logger.warn(getId()+": Tree structure incorrect",e);
			reInitAfterError();
			output = tree.getFirstChild("applist").getFirstChild("output");
		}
		if(getPossibleValues().containsAll(values)){
			Iterator<String> it = values.iterator();
			while(it.hasNext()){
				output.add("value").add(it.next());
			}
		}else{
			error = LanguageManagerWF.getText("AppendListInteraction.setValues", new Object[]{values.toString()});
		}
		return error;
	}
	
	/**
	 * Replace the values in Possible values and values
	 */
	@Override
	public void replaceOutputInTree(String oldName, String newName,boolean regex)
			throws RemoteException {
		List<Tree<String>> vals = null;
		try{
			vals = tree.getFirstChild("applist").getFirstChild("values").getSubTreeList();
		}catch(Exception e){
			logger.warn(getId()+": Tree structure incorrect",e);
			reInitAfterError();
			vals = tree.getFirstChild("applist").getFirstChild("values").getSubTreeList();
		}
		
		if(vals != null && !vals.isEmpty()){
			setPossibleValues(replaceInChoiceArray(oldName, newName, getPossibleValues(), regex));
		}
		vals = tree.getFirstChild("applist").getFirstChild("output").getSubTreeList();
		if(vals != null && !vals.isEmpty()){
			Iterator<Tree<String>> itVals = vals.iterator();
			while(itVals.hasNext()){
				Tree<String> cur = itVals.next();
				try{
					String valCur = cur.getFirstChild().getHead(); 
					if(regex){
						cur.getFirstChild().setHead(valCur.replaceAll(oldName, newName));
					}else{
						cur.getFirstChild().setHead(valCur.replaceAll(Pattern.quote(oldName), newName));
					}
				}catch(Exception e){
					logger.error(e.getMessage(),e);
				}
			}
		}
	}

}
