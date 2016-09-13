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


import java.io.File;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.utils.TreeNonUnique;
import com.redsqirl.workflow.server.BrowserInteraction;
import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.connect.WorkflowInterface;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DFEPage;
import com.redsqirl.workflow.server.interfaces.OozieAction;
import com.redsqirl.workflow.server.interfaces.PageChecker;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Abstract action that read a source file. For now, Hive and HDFS types are supported.
 * This class has methods to create pages to select the Type (Hive or HDFS), Subtype and
 * the path to the file.
 * 
 * @author etienne
 * 
 */
public abstract class AbstractMultipleSources extends DataflowAction {

	private static final long serialVersionUID = 7519928238030041208L;
	
	private static Logger logger = Logger.getLogger(AbstractMultipleSources.class);
	
	/**
	 * Map of inputs
	 */
	protected static Map<String, DFELinkProperty> input = new LinkedHashMap<String, DFELinkProperty>();
	
	/**
	 * datatype key
	 */
	public static final String key_datatype = "data_type";
	/**
	 * datasubtype key
	 */
	public static final String key_datasubtype = "data_subtype";
	/**
	 * dataset key
	 */
	public static final String key_dataset = "data_set";
	
	protected Map<String,String> idVsOutputName = new LinkedHashMap<String,String>();

	/**
	 * Constructor to initalize the DataFlowAction.
	 * 
	 * @throws RemoteException
	 */
	public AbstractMultipleSources(OozieAction action) throws RemoteException {
		super(action);
		
	}
	
	/**
	 * Add a page with a list interaction to select the Data Type.
	 * 
	 * @throws RemoteException
	 * 
	 */
	protected void addTypePage(Page page, String id) throws RemoteException{
		page.addInteraction(initializeDataTypeInteraction(id));
	}
	
	/**
	 * Create the Data Type interaction and populate it with the Hive and HDFS 
	 * options.
	 * 
	 * @throws RemoteException
	 * 
	 */
	protected ListInteraction initializeDataTypeInteraction(String id) throws RemoteException{
		ListInteraction dataType = new ListInteraction(
				key_datatype+id,
				LanguageManagerWF.getText("source.datatype_interaction.title"),
				LanguageManagerWF.getText("source.datatype_interaction.legend"),
				0, 0);
		dataType.setReplaceDisable(true);
		dataType.setDisplayRadioButton(true);
		List<String> posValues = new LinkedList<String>();
		posValues.addAll(WorkflowInterface.getInstance().getBrowsersName());
		dataType.setPossibleValues(posValues);
		return dataType;
	}
	
	protected String getDataTypeUniqueId(String dataTypeId) throws RemoteException{
		return dataTypeId.substring(key_datatype.length());
	}
	
	protected String getDataSubTypeUniqueId(String dataSubTypeId) throws RemoteException{
		return dataSubTypeId.substring(key_datasubtype.length());
	}
	
	protected String getBrowserUniqueId(String browserId) throws RemoteException{
		return browserId.substring(key_dataset.length());
	}
	
	/**
	 * Add a page with a list interaction to select the Data Sub Type
	 * 
	 * @throws RemoteException
	 * 
	 */
	protected void addSubTypePage(Page page, String id, String outputName) throws RemoteException{
		ListInteraction dataSubType = initializeDataSubtypeInteraction(id); 
		page.addInteraction(dataSubType);
		page.setChecker(new SubTypePageChecker(this,outputName, dataSubType));
		idVsOutputName.put(id, outputName);
	}
	
	
	/**
	 * Create the Data Sub Type interaction.
	 * 
	 * @throws RemoteException
	 * 
	 */
	protected ListInteraction initializeDataSubtypeInteraction(String id) throws RemoteException{
		ListInteraction dataSubtype = new ListInteraction(key_datasubtype+id,
				LanguageManagerWF
						.getText("source.datasubtype_interaction.title"),
				LanguageManagerWF
						.getText("source.datasubtype_interaction.legend"), 0, 0);
		dataSubtype.setReplaceDisable(true);
		dataSubtype.setDisplayRadioButton(true);
		return dataSubtype;
	}
	
	protected ListInteraction initializeDataSubtypeInteraction(String id,String value) throws RemoteException{
		ListInteraction dataSubtype = initializeDataSubtypeInteraction(id);
		List<String> valL = new LinkedList<String>();
		valL.add(value);
		dataSubtype.setPossibleValues(valL);
		dataSubtype.setValue(value);
		return dataSubtype;
	}
	
	
	/**
	 * Add a page with a browser interaction to select the path to the file.
	 * 
	 * @throws RemoteException
	 * 
	 */
	protected void addSourcePage(Page sourcePage, String id) throws RemoteException{

		BrowserInteraction browser = new BrowserInteraction(key_dataset+id,
				LanguageManagerWF.getText("source.browse_interaction.title"),
				LanguageManagerWF.getText("source.browse_interaction.legend"),
				0, 0);
		browser.setReplaceDisable(true);
		
		sourcePage.addInteraction(browser);

		sourcePage.setChecker(new SourcePageChecker(this, idVsOutputName.get(id), browser));
	}

	/**
	 * Get the Map of Inputs
	 * 
	 * @return Map of Inputs
	 * @throws RemoteException
	 * 
	 */
	@Override
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}


	/**
	 * Update the Interaction provided
	 * 
	 * @param interaction
	 * @throws RemoteException
	 */
	@Override
	public void update(DFEInteraction interaction) throws RemoteException {

		logger.debug("updateinteraction Source ");
		String interId = interaction.getId();
		logger.debug("interaction : " + interId);
		if (interId.startsWith(key_datasubtype)) {
			updateDataSubType(interaction);
		} else if (interId.startsWith(key_dataset)){
			String id = getBrowserUniqueId(interId);
			DFEOutput out = getDFEOutput().get(idVsOutputName.get(id));
			String dataTypeStr = out.getBrowserName();
			String datasubtypeStr = out.getTypeName();
			((BrowserInteraction) interaction).update(dataTypeStr, datasubtypeStr,idVsOutputName.get(id));
		}
	}

	/**
	 * Update the DataSubType Interaction
	 * 
	 * @param treeDatasubtype
	 * @throws RemoteException
	 */
	public void updateDataSubType(DFEInteraction interaction)
			throws RemoteException {
		ListInteraction dataType = (ListInteraction) getInteraction(key_datatype+getDataSubTypeUniqueId(interaction.getId()));
		ListInteraction dataSubtype = (ListInteraction) interaction;
		
		logger.debug("updating data subtype");
		String type = dataType.getValue();
		logger.debug("data type : " + type);
		if (type != null) {
			String setValue = null;
			List<String> posValues = new LinkedList<String>();
			List<String> dataOutputClassName = DataOutput
					.getAllClassDataOutput();
			for (String className : dataOutputClassName) {
				logger.debug(className);
				DataOutput wa = null;
				try {
					wa = (DataOutput) Class.forName(className).newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
				logger.debug("class : " + wa.getClass().getCanonicalName());
				logger.debug("wa type : " + wa.getTypeName());
				if (wa.getBrowserName() != null
						&& wa.getBrowserName().toString().equalsIgnoreCase(type)) {
					posValues.add(wa.getTypeName());
					if(wa.getTypeName()
							.equalsIgnoreCase(
									(new MapRedTextType()).getTypeName())
							&& dataSubtype.getValue() == null) {
						setValue = wa.getTypeName();
					}
				}
			}
			logger.debug("set possibilities...");
			logger.debug(" is " + posValues.toString());
			dataSubtype.setPossibleValues(posValues);
			if (setValue != null) {
				logger.debug("set value...");
				dataSubtype.setValue(setValue);
			}
		} else {
			logger.error("No type specified");
		}
	}

	/**
	 * Update the output
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String updateOut() throws RemoteException {
		return null;
	}

	/**
	 * Not Supported
	 */
	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		return false;
	}
}
