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

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.log4j.Logger;

import com.idiro.hadoop.NameNodeVar;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.datatype.MapRedTextFileType;
import com.redsqirl.workflow.server.datatype.MapRedTextFileWithHeaderType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.oozie.ShellAction;
import com.redsqirl.workflow.utils.LanguageManagerWF;
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

		page1 = addPage(LanguageManagerWF.getText("convert_plain_text_page1.title"),
				LanguageManagerWF.getText("convert_plain_text_page1.legend"), 1);
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
	 * @param nbInMin
	 * @param nbInMax
	 * @throws RemoteException
	 */
	protected void init() throws RemoteException{
		if(input == null){
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(MapRedTextFileWithHeaderType.class, 1, 1));
			input = in;
		}
	}
	
	/**
	 * Update the output of the action
	 */
	public String updateOut() throws RemoteException {
		String error = checkIntegrationUserVariables();
		logger.info("Error in updae out : "+error);
		if(error == null){
//			FieldList new_field = getNewFields();
			DFEOutput out = output.get(key_output);
//			logger.info("new fields "+new_field.getFieldNames());
			
			if(output.get(key_output) == null){
				output.put(key_output, new MapRedTextFileType());
			}
			
//			output.get(key_output).setFields(new_field);
//			output.get(key_output).addProperty(MapRedTextType.key_delimiter, delimiterOutputInt.getValue());
			
		}
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
		
		List<String> pathList = new ArrayList<String>();
		
		String filesConcatenate = "";
		
		String path = this.getDFEInput().get(ConvertFileText.key_input).get(0).getPath();
		String pathOutput = this.getDFEOutput().get(ConvertFileText.key_output).getPath();
		
		FileSystem fs;
		try {
			fs = NameNodeVar.getFS();
			FileStatus[] stat = fs.listStatus(new Path(path),
					new PathFilter() {

				@Override
				public boolean accept(Path arg0) {
					return !arg0.getName().startsWith("_") && !arg0.getName().startsWith(".");
				}
			});
			for (int i = 0; i < stat.length; ++i) {
				if (!stat[i].isDir()) {
					String file = stat[i].getPath().toString().replace(fs.getUri().toString(), "");
					System.out.println(file);
					pathList.add(file);
					filesConcatenate += file + " ";
				}
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}

		String hadoopBin = WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_hadoop_home);
		if(hadoopBin == null){
			hadoopBin = "";
		}else if(! hadoopBin.isEmpty()){
			hadoopBin +="/bin/";
		}
		hadoopBin += "hadoop";
		
		String toWrite = ((ShellAction) getOozieAction()).getShellContent(
				hadoopBin+" fs -cat " + filesConcatenate + 
				" | "+hadoopBin+" fs -put - " + pathOutput);

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
