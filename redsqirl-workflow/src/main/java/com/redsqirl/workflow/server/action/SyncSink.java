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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.hcat.HCatStore;
import com.redsqirl.workflow.server.connect.hcat.HCatalogType;
import com.redsqirl.workflow.server.datatype.MapRedCompressedType;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.enumeration.PathType;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.enumeration.TimeTemplate;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEInteractionChecker;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.oozie.DistcpAction;
import com.redsqirl.workflow.server.oozie.HiveAction;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class SyncSink extends DataflowAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6765601797425986642L;
	private static Logger logger = Logger.getLogger(SyncSink.class);
	private static Map<String, DFELinkProperty> input;

	public static final String key_input = "in",key_output="";
	
	protected Page page1;
	protected InputInteraction templatePath;
	
	public SyncSink() throws RemoteException {
		super(new DistcpAction());
		init();
		
		page1 = addPage(LanguageManagerWF.getText("sync_sink.page1.title"),
				LanguageManagerWF.getText("sync_sink.page1.legend"), 1);
		
		//Data Set Type
		templatePath = new InputInteraction(
				"template_path", 
				LanguageManagerWF.getText("sync_sink.template_path.title"),
				LanguageManagerWF.getText("sync_sink.template_path.legend")
				, 0, 0);
		templatePath.setVariableDisable(true);
		templatePath.setChecker(new DFEInteractionChecker() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = -7018145668059968489L;

			@Override
			public String check(DFEInteraction interaction) throws RemoteException {
				String error = checkTemplatePathInt();
				if(error == null){
					error = checkFields(getDFEInput().get(key_input).get(0),templatePath.getValue());
				}
				return error;
			}
		});
		page1.addInteraction(templatePath);
	}
	
	protected String checkFields(DFEOutput in, String templatePathStr) throws RemoteException{
		String error = updateOut();
		
		if(error == null && new HCatalogType().getTypeName().equals(in.getTypeName())){
			String[] pathOut = HCatStore.getDatabaseTableAndPartition(templatePathStr);
			
			if(in.getFields().getFieldNames().removeAll(HCatStore.getPartitionNames(pathOut[2]))){
				error = "Input dataset shouldn't includes partitions.";
			}else{
				error = getDFEOutput().get(key_output).isPathValid();
			}
		}
		
		return error;
	}
	
	protected static String checkTemplatePath(String templatePathStr){
		Pattern p = Pattern.compile("\\$\\{(.*?)\\}");
		Matcher m = p.matcher(templatePathStr);
		String error = null; 
		int found = 0;
		while(m.find() && error == null){
			++found;
			boolean match = true;
			try{
				TimeTemplate cur = TimeTemplate.valueOf(m.group(1)); 
				match = cur != null;
			}catch(Exception e){
				match = false;
			}
			if(!match){
				error = m.group(1)+" is not a template variable accepted, it should be YEAR, MONTH, DAY, HOUR or MINUTE";
			}
		}
		
		if(error == null && found == 0){
			error = "No template specified.";
		}
		
		return error;
	}
	
	public String checkTemplatePathInt() throws RemoteException{
		String templatePathStr = templatePath.getValue();
		String error = checkTemplatePath(templatePathStr);
		if(error == null){
			String templateParentStr = templatePathStr.substring(0,templatePathStr.lastIndexOf("/", templatePathStr.indexOf("$")));
			DFEOutput in = getDFEInput().get(key_input).get(0);
			if(!in.getBrowser().exists(templateParentStr)){
				error = "The path "+templateParentStr+" doesn't exist";
			}
			updateOut();
			DFEOutput out = output.get(key_output);
			if(out != null &&  new HCatalogType().getTypeName().equals(out.getTypeName())){
				String createQuery = ((HCatalogType) out).createTableStatement();
				if(createQuery.indexOf("LOCATION") > -1){
					createQuery = createQuery.substring(0, createQuery.indexOf("LOCATION"));
				}
				page1.setTextTip(createQuery);
			}
		}
		return error;
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
			in.put(key_input, new DataProperty(l, 1, 1));
			input = in;
		}
	}

	@Override
	public String getName() throws RemoteException {
		return "synchronous_sink";
	}

	@Override
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

	public FieldList getNewFields() throws RemoteException{
		FieldList ans = null;
		DFEOutput in = getDFEInput().get(key_input).get(0);
		FieldList inF = in.getFields();
		if(!new HCatalogType().getTypeName().equals(in.getTypeName())){
			ans = inF;
		}else{
			ans = new OrderedFieldList();
			Iterator<String> it = inF.getFieldNames().iterator();
			while(it.hasNext()){
				String cur = it.next();
				ans.addField(cur, inF.getFieldType(cur));
			}
			String[] pathOut = HCatStore.getDatabaseTableAndPartition(templatePath.getValue());
			try{
			it = HCatStore.getPartitionNames(pathOut[2]).iterator();
			while(it.hasNext()){
				ans.addField(it.next(), FieldType.STRING);
			}
			}catch(Exception e){
				logger.error(e,e);
			}
		}
		return ans;
	}
	
	@Override
	public String updateOut() throws RemoteException {
		String error = null;
		FieldList new_field = getNewFields();
		
		logger.info("Fields "+new_field.getFieldNames());
		DFEOutput in = getDFEInput().get(key_input).get(0);
		DFEOutput out = output.get(key_output);
		if(out == null || !in.getTypeName().equals(out.getTypeName())){
			out = DataOutput.getOutput(in.getTypeName());
			output.put(key_output,out);
		}

		out.setFields(new_field);
		out.setPath(templatePath.getValue());
		out.setPathType(PathType.TEMPLATE);
		out.setSavingState(SavingState.RECORDED);
			
		if(new HCatalogType().getTypeName().equals(in.getTypeName())){
			oozieAction = new HiveAction();
			HiveAction hiveAction = (HiveAction) getOozieAction();
			hiveAction.addVariable("DATABASE_"+getComponentId());
			hiveAction.addVariable("TABLE_"+getComponentId());
			Iterator<String> parts = HCatStore.getPartitionNames(HCatStore.getDatabaseTableAndPartition(out.getPath())[2]).iterator();
			while(parts.hasNext()){
				String partCur = parts.next();
				hiveAction.addVariable("PARTITION_"+getComponentId()+"_"+partCur);
			}
		}else{
			oozieAction = new DistcpAction();
		}
		return error;
	}

	/**
	 * Write files needed to run the Oozie action
	 * 
	 * @param files
	 * @return <code>true</code> if actions were written else <code>false</code>
	 * @throws RemoteException
	 */
	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		DFEOutput in = getDFEInput().get(key_input).get(0);
		if(!new HCatalogType().getTypeName().equals(in.getTypeName())){
			DistcpAction distcp = (DistcpAction) getOozieAction();
			distcp.setInput("${"+OozieManager.prop_namenode+"}"+in.getPath());
			distcp.setOutput("${"+getComponentId()+"}");
			return true;
		}
		
		File sqlFile = files[0];
		boolean ok = true;
		if(ok){
			logger.info("Write queries in file: " + sqlFile.getAbsolutePath());
			if(logger.isDebugEnabled()){
				List<String> path = new LinkedList<String>();
				Iterator<List<DFEOutput>> ins = getDFEInput().values().iterator();
				while(ins.hasNext()){
					try{
						Iterator<DFEOutput> inIt = ins.next().iterator();
						while(inIt.hasNext()){
							path.add(inIt.next().getPath());
						}
					}catch(Exception e){}
				}
				logger.info("Jdbc Inputs: "+path.toString());
				path = new LinkedList<String>();
				Iterator<DFEOutput> outIt = getDFEOutput().values().iterator();
				while(outIt.hasNext()){
					try{
						path.add(outIt.next().getPath());
					}catch(Exception e){}
				}
				logger.info("Jdbc Outputs: "+path.toString());
			}
			writeFile(sqlFile,getQuery(in)+";\n");
		}
		
		return true;
	}
	
	protected String getQuery(DFEOutput in) throws RemoteException {
		String[] inPath = HCatStore.getDatabaseTableAndPartition(in.getPath());
		FieldList ifl = in.getFields();
		String query = "";
		
		query += "INSERT OVERWRITE TABLE ${DATABASE_"+getComponentId()+"}.${TABLE_"+getComponentId()+"} PARTITION (";
		Iterator<String> parts = HCatStore.getPartitionNames(HCatStore.getDatabaseTableAndPartition(getDFEOutput().get(key_output).getPath())[2]).iterator();
		while(parts.hasNext()){
			String partCur = parts.next();
			query += partCur+"='${PARTITION_"+getComponentId()+"_"+partCur+"}'";
			if(parts.hasNext()){
				query += ",";
			}
		}
		query += ") SELECT ";
		Iterator<String> it = ifl.getFieldNames().iterator();
		while(it.hasNext()){
			String cur = it.next();
			query += cur;
			if(it.hasNext()){
				query+=",";
			}
		}
		query += " FROM "+inPath[0]+"."+inPath[1];
		if(inPath.length > 2){
			query += " WHERE "+inPath[2].replaceAll("=", "='").replaceAll(HCatStore.partitionDelimiter, "' AND ")+"' "; 
		}
		
		
		return query;
	}

	boolean writeFile(File f, String content){
		boolean ok = content != null;
		if (ok) {

			logger.info("Content of " + f.getName() + ": " + content);
			try {
				FileWriter fw = new FileWriter(f);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(content);
				bw.close();

			} catch (IOException e) {
				ok = false;
				logger.error("Fail to write into the file "
						+ f.getAbsolutePath());
			}
		}
		return ok;
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
