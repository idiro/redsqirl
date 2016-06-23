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

package com.redsqirl.workflow.server.action;

import com.redsqirl.workflow.server.interaction.DemoTable;

import java.io.File;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.redsqirl.workflow.server.AppendListInteraction;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.datatype.MapRedTextFileType;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.oozie.PigAction;

public class Demo extends DataflowAction {
	
	//Demo Pages
	private Page page1, page2, page3;
	//input map
	protected Map<String, DFELinkProperty> input;
	//Demo List Interaction
	public ListInteraction demoList;
	//Demo Append ListInteraction
	public AppendListInteraction demoappendList;
	//Demo Table
	public DemoTable demoTable;
	//Key for referencing the input
	public static String key_input = "in";

	public Demo() throws RemoteException {
		//Set the Oozie Action type
		super(new PigAction());
		//Make Sure input map is useable
		if (input == null) {
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(MapRedTextFileType.class, 1, 1));
			input = in;
		}
		//Add a page to demo the list interaction
		page1 = addPage(
				"Demo List",
				"This Page will demonstrate a list interaction that contains the list of feature names from the input",
				1);
		//initialize the list interaction
		demoList = new ListInteraction(
				"demolist",
				"Demo List ",
				"Demo List that selects one column from the features from the input",
				1, 0);
		//add the list interaction to the page
		page1.addInteraction(demoList);
		//add a page for the list interaction
		page2 = addPage(
				"Demo Append List",
				"This page demos appendable list of remaining features from the input",
				1);
		//initialize the append list interaction
		demoappendList = new AppendListInteraction(
				"dempappendlist",
				"Demo AppendList",
				"Select the remaining features from the input for the next step",
				0, 1);
		//add append list interaction to the page
		page2.addInteraction(demoappendList);
		//add a page for the table interaction
		page3 = addPage("Demo Table",
				"This page demonstrates a table interaction", 1);
		
		//initialize the demo table interaction
		demoTable = new DemoTable(
				"demotable",
				"Demo Table",
				"This table Demos the values recieved from the List interaction and the AppendList Interaction by displaying the columns selected in each",
				1, 0);
		//add the table interaction to the page
		page3.addInteraction(demoTable);

	}

	@Override
	public String getName() throws RemoteException {
		//return the action name
		return "demo";
	}

	@Override
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		//return the input links
		return input;
	}

	@Override
	public String updateOut() throws RemoteException {
		//for demonstration purposes we leave the output blank
		return null;
	}

	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		//for demonstration purposes we leave the write oozie action blank
		return false;
	}
	
	//update method for the action that update all specified interactions
	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		DFEOutput in = getDFEInput().get(key_input).get(0);
		//chech that there is inputs available
		if (in != null) {
			if (interaction.getId().equals(demoList.getId())) {
				//set the values to appear in the list
				demoList.setPossibleValues(in.getFields().getFieldNames());
				//set the default value
				demoList.setValue(in.getFields().getFieldNames().get(0));
			} else if (interaction.getId().equals(demoappendList.getId())) {
				List<String> appendlist = in.getFields().getFieldNames();
				appendlist.remove(demoList.getValue());
				//set the values to appear in the append list
				demoappendList.setPossibleValues(appendlist);
				//set the default values to be empty
				demoappendList.setValues(new LinkedList<String>());
			} else if (interaction.getId().equals(demoTable.getId())) {
				//update the table interaction 
				demoTable.update(in, demoList, demoappendList);
			}
		}

	}

}
