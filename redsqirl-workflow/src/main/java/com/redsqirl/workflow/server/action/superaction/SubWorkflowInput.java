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

package com.redsqirl.workflow.server.action.superaction;

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
import com.redsqirl.workflow.server.AppendListInteraction;
import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.action.AbstractSource;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.enumeration.PathType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Define one input of a sub-workflow 
 * @author etienne
 *
 */
public class SubWorkflowInput extends AbstractSource{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2646594330930112981L;
	
	private static Logger logger = Logger.getLogger(SubWorkflowInput.class);
	
	private static Map<String, DFELinkProperty> input = new LinkedHashMap<String, DFELinkProperty>();
	
	public static final String key_headerInt = "header",
						  key_fieldDefInt = "field_def",
						  key_materialized= "materialized";
	
	private InputInteraction headerInt;
	private AppendListInteraction materializedInt;
	private FieldDefinitionTableInteraction defFieldInt;
	
	public static final String out_name = "";

	public SubWorkflowInput() throws RemoteException {
		super(null);
		addTypePage();
		addSubTypePage();
		
		headerInt = new InputInteraction(key_headerInt, 
				LanguageManagerWF.getText("superactioninput.header.title"), 
				LanguageManagerWF.getText("superactioninput.header.legend"), 
				0, 
				1);
		
		materializedInt= new AppendListInteraction(key_materialized,
				LanguageManagerWF.getText("superactioninput.materialized.title"),
				LanguageManagerWF.getText("superactioninput.materialized.legend"), 0, 2);
		List<String> matIntVal = new LinkedList<String>();
		matIntVal.add(LanguageManagerWF.getText("superactioninput.allow_materialized"));
		materializedInt.setPossibleValues(matIntVal);
		materializedInt.setDisplayCheckBox(true);

		page2.addInteraction(headerInt);
		page2.addInteraction(materializedInt);
		
		defFieldInt = new FieldDefinitionTableInteraction(
				key_fieldDefInt, 
				LanguageManagerWF.getText("superactioninput.field_def.title"), 
				LanguageManagerWF.getText("superactioninput.field_def.legend"), 
				0, 
				0);

		page3 = addPage(LanguageManagerWF.getText("superactioninput.page3.title"),
				LanguageManagerWF.getText("superactioninput.page3.legend"), 1);
		
		page3.addInteraction(defFieldInt);
		
		
	}
	
	public DFELinkProperty getSubWorkflowInput() throws RemoteException{
		if(output.get(out_name) == null){
			return null;
		}
		if(materializedInt.getValues().isEmpty()){
			return new DataProperty(output.get(out_name).getClass(), 1, 1,output.get(out_name).getFields());
		}else{
			return new DataProperty(output.get(out_name).getClass(), 1, 1,output.get(out_name).getFields(),PathType.MATERIALIZED);
		}
	}

	@Override
	public String getName() throws RemoteException {
		return "superactioninput";
	}

	@Override
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}
	
	/**
	 * Get the path to the Image
	 * 
	 * @return path
	 * @throws RemoteException
	 */
	@Override
	public String getImage() throws RemoteException {
		String absolutePath = "";
		String imageFile = "/image/" + getName().toLowerCase() + ".gif";
		String path = WorkflowPrefManager
						.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat);
		List<String> files = listFilesRecursively(path);
		for (String file : files) {
			if (file.contains(imageFile)) {
				absolutePath = file;
				break;
			}
		}

		return absolutePath;
	}
	
	/**
	 * Get path to help
	 * 
	 * @return path
	 * @throws RemoteException
	 */
	@Override
	public String getHelp() throws RemoteException {
		String absolutePath = "";
		String helpFile = "/help/" + getName().toLowerCase() + ".html";
		String path = WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat);
		List<String> files = listFilesRecursively(path);
		for (String file : files) {
			if (file.contains(helpFile)) {
				absolutePath = file;
				break;
			}
		}
		
		return absolutePath;
	}
	

	@Override
	public String updateOut() throws RemoteException {
		String error = null;
		FieldList fl = new OrderedFieldList();
		Iterator<Map<String,String>> it = defFieldInt.getValues().iterator();
		while(it.hasNext()){
			Map<String,String> cur = it.next();
			fl.addField(cur.get(FieldDefinitionTableInteraction.table_field_title), 
					FieldType.valueOf(cur.get(FieldDefinitionTableInteraction.table_type_title)));
		}
		if(output.get(out_name) == null){
			output.put(out_name, DataOutput.getOutput(getDataSubtype().getValue()));
		}
		output.get(out_name).setFields(fl);
		return error;
	}

	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		return false;
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		
		logger.info("update");
		
		String interId = interaction.getId();
		if (interId.equals(key_headerInt)) {
			if(output.get(out_name) != null){
				FieldList fl = output.get(out_name).getFields();
				if(fl != null && checkIntegrationUserVariables() == null){
					
					logger.info("checkIntegrationUserVariables pass");
					
					String newVal = "";
					Iterator<String> it = fl.getFieldNames().iterator();
					while(it.hasNext()){
						String cur = it.next();
						newVal += cur+" "+fl.getFieldType(cur).toString()+", ";
					}
					headerInt.setValue(newVal.substring(0,newVal.length()-2));
					
					logger.info("checkIntegrationUserVariables finesh");
					
				}
			}else{
				logger.info("the field list was empty");
			}
		} else if (interId.equals(key_fieldDefInt)) {
			defFieldInt.update(headerInt.getValue());
		}else{
			super.update(interaction);
		}
	}


}
