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
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.datatype.MapRedTextFileType;
import com.redsqirl.workflow.server.datatype.MapRedTextFileWithHeaderType;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.oozie.ShellAction;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Action that read a Text Map Reduce file.
 * 
 * @author marcos
 * 
 */
public class FileTextSource extends AbstractSource {

	private static final long serialVersionUID = 7519928238030041208L;
	
	private static Logger logger = Logger.getLogger(FileTextSource.class);
	
	/**
	 * Audit output name
	 */
	public static final String audit_out_name = "audit",
							   key_audit="audit",
							   no_header_out_name = "out_no_header";
	
	/**
	 * Constructor containing the pages, page checks and interaction
	 * Initialization
	 * 
	 * @throws RemoteException
	 */
	public FileTextSource() throws RemoteException {
		super(new ShellAction());
		
		initializeDataTypeInteraction();
		initializeDataSubtypeInteraction();
		
		addSubTypePage();
		addSourcePage();
		
		logger.info("PigFileTextSource - addSourcePage ");
		
		browser.setTextTip(LanguageManagerWF.getText("pig.test_source_browser_interaction.header_help"));
		
		MapRedTextFileType type = new MapRedTextFileType();
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
		posValuesSubType.add(new MapRedTextFileType().getTypeName());
		posValuesSubType.add(new MapRedTextFileWithHeaderType().getTypeName());
		dataSubtype.setPossibleValues(posValuesSubType);
			
		if(dataSubtype.getValue() == null || !posValuesSubType.contains(dataSubtype.getValue())){
			dataSubtype.setValue(new MapRedTextFileType().getTypeName());
		}
	}

	/**
	 * Get the name of the Action
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException {
		return "file_text_source";
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
		
		if (out != null && dataSubtype.getValue().equals(new MapRedTextFileWithHeaderType().getTypeName())){
			if(output.get(no_header_out_name) == null){
				String user = System.getProperty("user.name");
				output.put(no_header_out_name, new MapRedTextFileType());
				output.get(no_header_out_name).generatePath(user, this.componentId, no_header_out_name);
			}
			output.get(no_header_out_name).setFields(out.getFields());
			output.get(no_header_out_name).addProperty(MapRedTextFileType.key_delimiter, 
					out.getProperty(MapRedTextFileWithHeaderType.key_delimiter));
			
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
		String path = getDFEOutput().get(out_name).getPath();
		String noHeaderPath = getDFEOutput().get(no_header_out_name).getPath();
		String hadoopBin = WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_hadoop_home);
		if(hadoopBin == null){
			hadoopBin = "";
		}else if(! hadoopBin.isEmpty()){
			hadoopBin +="/bin/";
		}
		hadoopBin += "hadoop";
		String toWrite = ((ShellAction) getOozieAction()).getShellContent(
				"export JAVA_HOME=$JAVA_HOME;"+
				hadoopBin+" fs -cat " + path + 
				" | /bin/sed 1d | "+hadoopBin+" fs -put - " + noHeaderPath
				);
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
