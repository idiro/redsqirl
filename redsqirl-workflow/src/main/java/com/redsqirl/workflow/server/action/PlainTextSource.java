package com.redsqirl.workflow.server.action;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.datatype.MapRedPlainTextHeaderType;
import com.redsqirl.workflow.server.datatype.MapRedPlainTextType;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.oozie.PlainTextAction;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Action that read a Text Map Reduce file.
 * 
 * @author marcos
 * 
 */
public class PlainTextSource extends AbstractSource {

	private static final long serialVersionUID = 7519928238030041208L;
	
	private static Logger logger = Logger.getLogger(PlainTextSource.class);
	
	/**
	 * Audit output name
	 */
	public static final String audit_out_name = "audit",
							   key_audit="audit",
							   no_header_out_name = "out_no_header";
	
	/**
	 * Parallel Interaction
	 */
	public InputInteraction parallelInt;
	
	/**
	 * Constructor containing the pages, page checks and interaction
	 * Initialization
	 * 
	 * @throws RemoteException
	 */
	public PlainTextSource() throws RemoteException {
		super(new PlainTextAction());
		
		initializeDataTypeInteraction();
		initializeDataSubtypeInteraction();
		
		addSubTypePage();
		addSourcePage();
		
		logger.info("PigPlainTextSource - addSourcePage ");
		
		browser.setTextTip(LanguageManagerWF.getText("pig.test_source_browser_interaction.header_help"));
		
		MapRedPlainTextType type = new MapRedPlainTextType();
		dataType.setValue(type.getBrowser());
		
		checkSubType();
	}
	
	/**
	 * Update the DataSubType Interaction
	 * 
	 * @param treeDatasubtype
	 * @throws RemoteException
	 */
	public void updateDataSubType(Tree<String> treeDatasubtype)
			throws RemoteException {
		logger.info("updating data subtype");

		List<String> posValuesSubType = new LinkedList<String>();
		posValuesSubType.add(new MapRedPlainTextType().getTypeName());
		posValuesSubType.add(new MapRedPlainTextHeaderType().getTypeName());
		dataSubtype.setPossibleValues(posValuesSubType);
			
		dataSubtype.setValue(new MapRedPlainTextType().getTypeName());
	}

	/**
	 * Get the name of the Action
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException {
		return "plain_text_source";
	}
	
	
	/**
	 * Update the output
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String updateOut() throws RemoteException {
		
		String error = super.updateOut();
		
		DFEOutput out = getDFEOutput().get(out_name);
		
		if (out != null && dataSubtype.getValue().equals(new MapRedPlainTextHeaderType().getTypeName())){
			output.put(no_header_out_name, new MapRedPlainTextType());
			output.get(no_header_out_name).setFields(out.getFields());
			
			String user = System.getProperty("user.name");
			output.get(no_header_out_name).generatePath(user, this.componentId, no_header_out_name);
			
		}
		else if (output.get(no_header_out_name) != null){
			output.remove(no_header_out_name);
		}
		
		if (error == null){
			
			if (output.get(audit_out_name) != null){
				output.remove(audit_out_name);
			}
		}
		return error;
		
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
		String path = WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_tomcat_path);
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
				.getSysProperty(WorkflowPrefManager.sys_tomcat_path);
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
		String toWrite = "";
			
		toWrite = "#!/bin/bash" + System.getProperty("line.separator");
		
		String path = getDFEOutput().get(out_name).getPath();
		String noHeaderPath = getDFEOutput().get(no_header_out_name).getPath();
		toWrite += "/home/hadoop/bin/hadoop fs -cat " + path + " | sed 1d | /home/hadoop/bin/hadoop fs -put - " + noHeaderPath;

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
