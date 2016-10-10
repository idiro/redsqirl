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
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.hcat.HCatalogType;
import com.redsqirl.workflow.server.datatype.MapRedCompressedType;
import com.redsqirl.workflow.server.enumeration.PathType;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class SyncSinkFilter extends DataflowAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6765601797425986642L;
	private static Logger logger = Logger.getLogger(SyncSinkFilter.class);
	private static Map<String, DFELinkProperty> input;

	public static final String key_input = "in",key_output="";

	protected InputInteraction nbUsedPath;
	protected InputInteraction offsetPath;
	
	public SyncSinkFilter(
			) throws RemoteException {
		super(null);
		init();
		
		Page page1 = addPage(LanguageManagerWF.getText("sync_source_filter.page1.title"),
				LanguageManagerWF.getText("sync_source_filter.page1.legend"), 1);
		
		//Number of dataset to use
		nbUsedPath = new InputInteraction(
				"nb_template_path_used", 
				LanguageManagerWF.getText("sync_source_filter.nb_template_path_used.title"),
				LanguageManagerWF.getText("sync_source_filter.nb_template_path_used.legend")
				, 0, 0);
		nbUsedPath.setRegex("^[1-9][0-9]*$");
		nbUsedPath.setValue("1");
		

		offsetPath = new InputInteraction(
				"nb_offset", 
				LanguageManagerWF.getText("sync_source_filter.nb_offset.title"),
				LanguageManagerWF.getText("sync_source_filter.nb_offset.legend")
				, 0, 1);
		offsetPath.setRegex("^(-?)[0-9]+$");
		offsetPath.setValue("0");

		page1.addInteraction(nbUsedPath);
		page1.addInteraction(offsetPath);
	}
	
	public int getNbPath(){
		int nbPath = 1;
		try{
			nbPath = Integer.valueOf(nbUsedPath.getValue());
		}catch(Exception e){
			logger.warn(e,e);
		}
		return nbPath;
	}
	
	public int getOffsetPath(){
		int offsetPathInt = 0;
		try{
			offsetPathInt = Integer.valueOf(offsetPath.getValue());
		}catch(Exception e){
			logger.warn(e,e);
		}
		return offsetPathInt;
	}
	
	/**
	 * Initialise the element
	 * @param nbInMin
	 * @param nbInMax
	 * @throws RemoteException
	 */
	protected static void init() throws RemoteException{
		if(input == null){
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			List<Class< ? extends DFEOutput>> l = new LinkedList<Class< ? extends DFEOutput>>();
			l.add(MapRedCompressedType.class);
			l.add(HCatalogType.class);
			in.put(key_input, new DataProperty(l, 1, 1,PathType.TEMPLATE));
			input = in;
		}
	}

	@Override
	public String getName() throws RemoteException {
		return "synchronous_sink_filter";
	}

	@Override
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

	public FieldList getNewFields() throws RemoteException{
		return getDFEInput().get(key_input).get(0).getFields();
	}
	
	@Override
	public String updateOut() throws RemoteException {
		String error = null;
		DFEOutput in = getDFEInput().get(key_input).get(0);
		DFEOutput out = output.get(key_output);
		FieldList new_field = getNewFields();
		logger.info("Fields "+new_field.getFieldNames());

		if( out == null || !out.getTypeName().equals(in.getTypeName())){
			output.put(key_output, DataOutput.getOutput(in.getTypeName()));
			out = output.get(key_output);
			out.setPathType(PathType.MATERIALIZED);
			out.setSavingState(SavingState.RECORDED);
		}
		
		out.setPath(in.getPath());
		out.setFields(new_field);
		out.setNumberMaterializedPath(getNbPath());
		out.setOffsetPath(getOffsetPath());
		out.removeAllProperties();
		Iterator<Entry<String,String>> itProp = in.getProperties().entrySet().iterator();
		while(itProp.hasNext()){
			Entry<String,String> cur = itProp.next();
			out.addProperty(cur.getKey(), cur.getValue());
		}
		return error;
	}

	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		return false;
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
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
		if(logger.isDebugEnabled()){
			String ans = "";
			if (absolutePath.contains(path)) {
				ans = absolutePath.substring(path.length());
			}
			logger.debug("Source help absPath : " + absolutePath);
			logger.debug("Source help Path : " + path);
			logger.debug("Source help ans : " + ans);
		}
		// absolutePath
		return absolutePath;
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
		if(logger.isDebugEnabled()){
			String ans = "";
			if (absolutePath.contains(path)) {
				ans = absolutePath.substring(path.length());
			}
			logger.debug("Source image abs Path : " + absolutePath);
			logger.debug("Source image Path : " + path);
			logger.debug("Source image ans : " + ans);
		}
		return absolutePath;
	}
}
