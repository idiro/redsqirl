package com.redsqirl.workflow.server.action;

import java.io.File;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.hcat.HCatalogType;
import com.redsqirl.workflow.server.datatype.MapRedCompressedType;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.enumeration.PathType;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.enumeration.TimeTemplate;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEInteractionChecker;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.oozie.DistcpAction;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class SyncSink extends DataflowAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6765601797425986642L;
	private static Logger logger = Logger.getLogger(SyncSink.class);
	private static Map<String, DFELinkProperty> input;

	public static final String key_input = "in",key_output="";
	
	protected InputInteraction templatePath;
	
	public SyncSink() throws RemoteException {
		super(new DistcpAction());
		init();
		
		Page page1 = addPage(LanguageManagerWF.getText("sync_sink.page1.title"),
				LanguageManagerWF.getText("sync_sink.page1.legend"), 1);
		
		//Data Set Type
		templatePath = new InputInteraction(
				"template_path", 
				LanguageManagerWF.getText("sync_sink.template_path.title"),
				LanguageManagerWF.getText("sync_sink.template_path.legend")
				, 0, 0);
		templatePath.setChecker(new DFEInteractionChecker() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = -7018145668059968489L;

			@Override
			public String check(DFEInteraction interaction) throws RemoteException {
				return checkTemplatePathInt();
			}
		});
		page1.addInteraction(templatePath);
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
			//l.add(HCatalogType.class);
			in.put(key_input, new DataProperty(l, 1, 1));
			input = in;
		}
	}

	@Override
	public String getName() throws RemoteException {
		return "synchronuous_sink";
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
		FieldList new_field = getNewFields();
		
		logger.info("Fields "+new_field.getFieldNames());
		DFEOutput in = getDFEInput().get(key_input).get(0);
		if(new HCatalogType().getTypeName().equals(in.getTypeName())){
			output.put(key_output, new HCatalogType());
		}else{
			output.put(key_output, new MapRedTextType());
		}

		DFEOutput out = output.get(key_output);
		out.setFields(new_field);
		out.setPath(templatePath.getValue());
		out.setPathType(PathType.TEMPLATE);
		out.setSavingState(SavingState.RECORDED);
		DistcpAction distcp = ((DistcpAction) getOozieAction());
		distcp.setInput("${"+OozieManager.prop_namenode+"}"+in.getPath());
		distcp.setOutput("${"+getComponentId()+"}");
			
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
		return true;
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
