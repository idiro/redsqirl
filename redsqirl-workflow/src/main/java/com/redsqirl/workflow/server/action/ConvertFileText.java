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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.datatype.MapRedTextFileType;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.oozie.ShellAction;
/**
 * Action to convert an hdfs dir to a flat file
 * @author marcos
 *
 */
public class ConvertFileText extends DataflowAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 600343170359664918L;
	
	private static Logger logger = Logger.getLogger(ConvertFileText.class);

	public static final String key_output = "",
			/**Input Key*/
			key_input = "in";
	
	/**Map of inputs*/
	protected Map<String, DFELinkProperty> input;
	
	
	/**
	 * Page for action
	 */
	private Page page1;
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public ConvertFileText() throws RemoteException {
		super(new ShellAction());
		init();

		/* page1 = addPage(LanguageManagerWF.getText("convert_plain_text_page1.title"),
				LanguageManagerWF.getText("convert_plain_text_page1.legend"), 1);
				*/
		logger.info("created page");

		logger.info("added interactions");
		logger.info("constructor ok");
	}
	/**
	 * Get the name of the action
	 * @return name
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException {
		return "convert_file_text";
	}
	
	/**
	 * Initialise the element
	 * @throws RemoteException
	 */
	protected void init() throws RemoteException{
		if(input == null){
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(MapRedTextType.class, 1, 1));
			input = in;
		}
	}
	
	/**
	 * Update the output of the action
	 */
	public String updateOut() throws RemoteException {
		String error = null;
		
		DFEOutput in = this.getDFEInput().get(ConvertFileText.key_input).get(0);
		FieldList new_field = in.getFields().cloneRemote();
		DFEOutput out = output.get(key_output);

		if(out == null){
			out = new MapRedTextFileType();
			output.put(key_output, out);

		}

		out.setFields(new_field);
		out.addProperty(MapRedTextType.key_delimiter,  
				in.getProperty(MapRedTextType.key_delimiter));

		return error;
	}
	
	/**
	 * Update the interaction 
	 * @param interaction
	 * @throws RemoteException
	 */
	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
	}
	
	@Override
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}
	
	// Override default static methods
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
		String ans = "";
		if (absolutePath.contains(path)) {
			ans = absolutePath.substring(path.length());
		}
		logger.debug("Source help absPath : " + absolutePath);
		logger.debug("Source help Path : " + path);
		logger.debug("Source help ans : " + ans);
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
		String ans = "";
		if (absolutePath.contains(path)) {
			ans = absolutePath.substring(path.length());
		}
		logger.debug("Source image abs Path : " + absolutePath);
		logger.debug("Source image Path : " + path);
		logger.debug("Source image ans : " + ans);
			
		return absolutePath;
	}
	
	/**
	 * Write the Oozie Action Files
	 * @param files
	 * @return <code>true</code> if write the oozie files was ok else <code>false</code>
	 * @throws RemoteException
	 */
	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		logger.info("Write queries in file: " + files[0].getAbsolutePath());
		
		String path = this.getDFEInput().get(ConvertFileText.key_input).get(0).getPath();
		String pathOutput = this.getDFEOutput().get(ConvertFileText.key_output).getPath();
		

		String hadoopBin = WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_hadoop_home);
		if(hadoopBin == null){
			hadoopBin = "";
		}else if(! hadoopBin.isEmpty()){
			hadoopBin +="/bin/";
		}
		hadoopBin += "hadoop";
		
		String toWrite = ((ShellAction) getOozieAction()).getShellContent(
				"export JAVA_HOME=$JAVA_HOME;"+
				hadoopBin+" fs -cat " + path + 
				"/[^_]* | "+hadoopBin+" fs -put - " + pathOutput);

		boolean ok = toWrite != null;
		if(ok){
			try {
				logger.debug("Content of "+files[0]+": "+toWrite);
				FileWriter fw = new FileWriter(files[0]);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(toWrite);	
				bw.close();
			} catch (IOException e) {
				ok = false;
				logger.error("Fail to write into the file "+files[0].getAbsolutePath(),e);
			}
		}
		return ok;
	}
}
