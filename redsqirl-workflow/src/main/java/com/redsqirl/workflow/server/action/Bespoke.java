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
import java.util.Set;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.action.utils.ScriptTemplate;
import com.redsqirl.workflow.server.enumeration.PathType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.oozie.BespokeScriptOozieAction;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class Bespoke extends DataflowAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2473298084508781315L;
	private static Logger logger = Logger.getLogger(Script.class);
	private static Map<String, DFELinkProperty> input = null;
	private static boolean init = false;

	public static final String key_input = "",
						  key_template = "template";
	
	private ListInteraction templateInt;
	
	protected Page page0;
	
	protected ScriptTemplate scriptTemplate = null;

	public Bespoke() throws RemoteException {
		super(null);
		oozieAction = new BespokeScriptOozieAction(this);
		init();
		
		page0 = addPage(LanguageManagerWF.getText("bespoke.page0.title"),
				LanguageManagerWF.getText("bespoke.page0.legend"), 1);
		
		templateInt = new ListInteraction(
				key_template, 
				LanguageManagerWF.getText("bespoke.template_inter.title"), 
				LanguageManagerWF.getText("bespoke.template_inter.legend"), 0, 0);
		updateTemplate();
		
		page0.addInteraction(templateInt);
	}
	


	private static void init() throws RemoteException {
		if(!init){
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(new LinkedList<Class<? extends DFEOutput>>(), 
					0, Integer.MAX_VALUE,PathType.MATERIALIZED));
			input = in;
			init = true;
		}
	}
	

	@Override
	public String getName() throws RemoteException {
		return "bespoke";
	}

	@Override
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

	@Override
	public String updateOut() throws RemoteException {
		String error = checkInputFromBespoke();
		
		if(error == null){
			List<String> outputNames = scriptTemplate.getOutputNames();
			if(outputNames == null){
				logger.info("No output declared in "+getComponentId());
			}
			if(outputNames.isEmpty()){
				updateOutput("");
			}else{
				Iterator<String> it = outputNames.iterator();
				while(it.hasNext()){
					updateOutput(it.next());
				}
			}
		}
		if(error == null){
			BespokeScriptOozieAction oozieAction = (BespokeScriptOozieAction) getOozieAction();
			oozieAction.setFileExtension(scriptTemplate.getExtension());
			try{
				oozieAction.setXmlContent(scriptTemplate.readOozie());
			}catch(Exception e){
				error = "Fail reading the oozie xml extract: "+e.getMessage();
			}
		}
		
		return error;
	}
	
	protected void updateOutput(String name) throws RemoteException{
		DFEOutput out = scriptTemplate.getNewOutput(name);
		FieldList outFields = null;
		try{
			outFields = scriptTemplate.getOutputFieldList(name);
		}catch(Exception e){
			logger.error(e,e);
		}
		
		if(getDFEOutput().get(name) == null){
			getDFEOutput().put(name,out);
		}else if(!getDFEOutput().get(name).getTypeName().equals(out.getTypeName())){
			getDFEOutput().get(name).clean();
			getDFEOutput().put(name,out);
		}else{
			out = getDFEOutput().get(name);
		}
		out.setFields(outFields);
	}
	
	protected String checkInputFromBespoke() throws RemoteException{
		String error = null;
		Integer min = getScriptTemplate().getInputNumberMin(), 
				max = getScriptTemplate().getInputNumberMax();
		List<Class<? extends DFEOutput>> inputType = scriptTemplate.getInputTypes();
		
		FieldList fl = null;
		try{
			fl = scriptTemplate.getInputFieldList();
		}catch(Exception e){
			error = e.getMessage();
		}
		if(error == null){
			if(min != null && min > getDFEInput().get(key_input).size()){
				error = "There are not enough inputs, the minimum is "+min;
			}
			if(max != null){
				error = "There are too many inputs, the maximum is "+min;
			}
			if(inputType != null && !inputType.isEmpty()){
				Iterator<DFEOutput> itIn = getDFEInput().get(key_input).iterator();
				while(itIn.hasNext() && error == null){
					boolean ok = false;
					DFEOutput inCur = itIn.next();
					if(fl == null){
						ok = new DataProperty(inputType, min != null?min:0, max!=null?max:Integer.MAX_VALUE).check(inCur);
					}else{
						ok = new DataProperty(inputType, min != null?min:0, max!=null?max:Integer.MAX_VALUE,fl).check(inCur);
					}
					if(!ok){
						if(fl != null){
							error = "The input "+inCur.getTypeName()+" is not accepted by this script. The fields required are "+fl.toString();
						}else{
							error = "The input "+inCur.getTypeName()+" is not accepted by this script.";
						}
					}
				}
			}
		}
		
		return error;
	}

	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		String scriptContent = scriptTemplate.readScript();
		if(files.length > 0 && scriptContent != null){
			return writeFile(scriptContent, files[0]);
		}
		return true;
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		logger.info("update");
		String interId = interaction.getId();
		if(interId.equals(key_template)){
			updateTemplate();
		}
	}

	private void updateTemplate() throws RemoteException {
		Set<String> posVals = ScriptTemplate.getBespokeNames();
		templateInt.setPossibleValues(posVals);
	}

	public ScriptTemplate getScriptTemplate() throws RemoteException{
		String templateStr = templateInt.getValue();
		if(templateStr == null || templateStr.isEmpty()){
			return null;
		}else if(scriptTemplate == null || !templateStr.equals(scriptTemplate.getName())){
			scriptTemplate = new ScriptTemplate(templateStr); 
		}
		return scriptTemplate;
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
}
