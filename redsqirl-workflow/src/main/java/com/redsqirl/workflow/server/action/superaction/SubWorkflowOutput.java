package com.redsqirl.workflow.server.action.superaction;

import java.io.File;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;

/**
 * Define the output of a sub-workflow.
 * @author etienne
 *
 */
public class SubWorkflowOutput extends DataflowAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -800255607454462658L;
	
	private static Map<String,DFELinkProperty> input = new LinkedHashMap<String,DFELinkProperty>();

	public static final String input_name = "";
	
	public SubWorkflowOutput() throws RemoteException {
		super(null);
		init();
	}
	
	private static void init() throws RemoteException{
		if(input.isEmpty()){
			input.put("",new DataProperty(new LinkedList<Class<? extends DFEOutput>>(), 1, 1));
		}
	}

	@Override
	public String getName() throws RemoteException {
		return "superactionoutput";
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
		String path = WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_tomcat_path);
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
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

	@Override
	public String updateOut() throws RemoteException {
		return checkIntegrationUserVariables();
	}

	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		return false;
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
	}

	@Override
	public Boolean getPrivilege() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
