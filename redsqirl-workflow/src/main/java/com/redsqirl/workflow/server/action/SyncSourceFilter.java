package com.redsqirl.workflow.server.action;

import java.io.File;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

public class SyncSourceFilter extends DataflowAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6765601797425986642L;
	private static Logger logger = Logger.getLogger(SyncSourceFilter.class);
	private static Map<String, DFELinkProperty> input;

	public static final String key_input = "in",key_output="";
	
	protected InputInteraction nbUsedPath;
	
	public SyncSourceFilter(
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
		
		page1.addInteraction(nbUsedPath);
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
		return "synchronuous_source_filter";
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
