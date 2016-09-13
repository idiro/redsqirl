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

package com.redsqirl.workflow.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

import com.idiro.utils.LocalFileSystem;
import com.redsqirl.workflow.server.action.superaction.SuperAction;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.server.interfaces.ElementManager;
import com.redsqirl.workflow.server.interfaces.SuperElement;
import com.redsqirl.workflow.utils.LanguageManagerWF;
import com.redsqirl.workflow.utils.ModelInt;
import com.redsqirl.workflow.utils.ModelManager;
import com.redsqirl.workflow.utils.PackageManager;
import com.redsqirl.workflow.utils.RedSqirlModel;
import com.redsqirl.workflow.utils.RedSqirlPackage;

public class ActionManager extends UnicastRemoteObject implements ElementManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1577582692193380573L;

	private static Logger logger = Logger.getLogger(ActionManager.class);

	/**
	 * Avoid to call reflexive method again and again
	 */
	protected static Map<String, String> flowElement = null;
	private static boolean initializeFlowElement = false;

	/**
	 * Menu of action, each tab title is link to a list of action name (@see
	 * {@link DataflowAction#getName()})
	 */
	protected Map<String, List<String[]>> menuWA;

	/**
	 * Key: action name, Value: absolute help path, absolute image path
	 */
	private Map<String, Map<String, String[]>> packageHelp;

	private Map<String, Map<String, String[]>> modelHelp;
	
	ActionManager() throws RemoteException{
		super();
	}

	/**
	 * Load the icon menu.
	 * 
	 * The icon menu is read from a directory. All the directory are tab, and
	 * each line in each file is an action. The files can be commented by '#' on
	 * the beginning of each line.
	 * 
	 * @return null if ok, or all the error found
	 * 
	 */
	protected String loadMenu() {

		String error = "";
		File menuDir = new File(WorkflowPrefManager.getPathIconMenu());
		/*
		 * File[] children = menuDir.listFiles(new FileFilter() {
		 * 
		 * @Override public boolean accept(File pathname) { return
		 * !pathname.getName().startsWith("."); } });
		 */

		menuWA = new LinkedHashMap<String, List<String[]>>();
		Map<String, String> nameWithClass;
		try {
			nameWithClass = getAllWANameWithClassName();
			Set<String> superActions = getSuperActions();

			String nameMenu = "";

			// for (int i = 0; i < children.length; ++i) {
			// if (children[i].isFile()) {
			LinkedList<String[]> new_list = new LinkedList<String[]>();
			BufferedReader br = new BufferedReader(new FileReader(menuDir));
			String line;
			while ((line = br.readLine()) != null) {
				try {
					if (!line.isEmpty() && !line.startsWith("#")) {

						if (line.startsWith("menu:")) {
							nameMenu = line.split(":")[1];
							new_list = new LinkedList<String[]>();
							menuWA.put(nameMenu, new_list);
						} else {
							if (nameWithClass.get(line) != null
									|| superActions.contains(line)) {
								DataFlowElement dfe = null;
								if(superActions.contains(line)){
									dfe = new SuperAction(line,true);
								}else{
									dfe = createElementFromClassName(
											nameWithClass, line);
								}
								String[] parameters = new String[3];
								parameters[0] = line;
								parameters[1] = dfe.getImage();
								parameters = setPrivilegeOfClass(dfe, line,
										parameters);
								// logger.info(parameters[0] +
								// " , "+parameters[2]);
								new_list.add(parameters);
							} else {
								logger.warn("unknown workflow action '" + line
										+ "'");
							}
						}

					}
				} catch (Exception e) {
					logger.error(e,e);
					error = LanguageManagerWF.getText("workflow.loadclassfail",
							new Object[] { line });
				}
			}
			br.close();
			// menuWA.put(children[i].getName(), new_list);
			// }
			// }

		} catch (Exception e) {
			logger.error(e,e);
			error += "\n"
					+ LanguageManagerWF.getText("workflow.loadclassexception");
		}

		if (error.isEmpty()) {
			error = null;
		} else {
			logger.error(error);
		}

		return error;
	}
	
	protected void loadPackageHelp() {
		packageHelp = new LinkedHashMap<String, Map<String, String[]>>();
		Map<String, String> nameWithClass = null;
		try {
			nameWithClass = getAllWANameWithClassName();
			
			String user = System.getProperty("user.name");
			PackageManager pm = new PackageManager();
			Map<String,List<String>> mapAction = pm.getActionsPerPackage(user);
			
			mapAction.put("core", pm.getCoreActions());
			
			Iterator<String> iterator = mapAction.keySet().iterator();
			while (iterator.hasNext()) {
				String packageName = iterator.next();
				LinkedHashMap<String, String[]> aux = new LinkedHashMap<String, String[]>();
				for (String actionName : mapAction.get(packageName)) {
					try {
						logger.debug("Class "+nameWithClass.get(actionName));
						DataFlowElement dfe = (DataFlowElement) Class.forName(nameWithClass.get(actionName)).newInstance();
						aux.put(actionName, new String[] { dfe.getHelp(), dfe.getImage() });
					} catch (Exception e) {
						logger.error(LanguageManagerWF.getText("workflow.loadclassfail", new Object[] { actionName }),e);
					}
				}
				packageHelp.put(packageName, aux);
			}
			
		} catch (Exception e) {
			logger.error(e,e);
			logger.error(LanguageManagerWF
					.getText("workflow.loadclassexception"));
		}

	}

	protected void loadModelHelp() {
		modelHelp = new LinkedHashMap<String, Map<String, String[]>>();
		try {

			String user = System.getProperty("user.name");
			ModelManager model = new ModelManager();
			List<ModelInt> l = model. getAvailableModels(user);
			for (ModelInt modelInt : l) {

				Map<String, String[]> out = new LinkedHashMap<String, String[]>();
				for (String subworkflow : modelInt.getPublicSubWorkflowNames()) {
					DataFlowElement dfe = new SuperAction(modelInt.getFullName(subworkflow),true);
					out.put(subworkflow, new String[] { dfe.getHelp(), dfe.getImage() });
				}

				modelHelp.put(modelInt.getName(), out);

			}

		} catch (Exception e) {
			logger.error(LanguageManagerWF
					.getText("workflow.loadclassexception"),e);
		}
	}

	public String saveMenu(Map<String, List<String>> newMenu) {
		String error = loadMenu(newMenu);
		if(error == null){
			error = saveMenu();
		}
		return error;
	}
	
	protected String loadMenu(Map<String, List<String>> newMenu) {

		String error = "";
		menuWA = new LinkedHashMap<String, List<String[]>>();

		Map<String, String> nameWithClass;
		try {
			nameWithClass = getAllWANameWithClassName();
			Set<String> superActions = getSuperActions();

			for (String key : nameWithClass.keySet()) {
				logger.debug("nameWithClass " + nameWithClass.get(key) + " key " + key);
			}

			for (Entry<String, List<String>> cur : newMenu.entrySet()) {
				LinkedList<String[]> new_list = new LinkedList<String[]>();
				Iterator<String> it = cur.getValue().iterator();
				while (it.hasNext()) {
					String action = it.next();
					try {
						if (action != null && !action.isEmpty()) {
							logger.debug("action '"	+ action + "'");
							if (nameWithClass.get(action) != null || superActions.contains(action)) {
								DataFlowElement dfe = createElementFromClassName(nameWithClass, action);
								String[] parameters = new String[3];
								parameters[0] = action;
								parameters[1] = dfe.getImage();
								parameters = setPrivilegeOfClass(dfe, action, parameters);
								new_list.add(parameters);
							} else {
								logger.debug("unknown workflow action '"	+ action + "'");
							}
						}
					} catch (Exception e) {
						logger.error(e,e);
						error = LanguageManagerWF.getText(
								"workflow.loadclassfail",
								new Object[] { action });
					}
				}
				menuWA.put(cur.getKey(), new_list);
			}

		} catch (Exception e) {
			logger.error(e,e);
			error += "\n"
					+ LanguageManagerWF.getText("workflow.loadclassexception");
		}

		if (error.isEmpty()) {
			error = null;
		} else {
			logger.error(error);
		}

		return error;
	}


	/**
	 * Save the icon menu.
	 * 
	 * @return null if ok, or all the error found
	 * 
	 */
	protected String saveMenu() {

		String error = "";

		File menuDir = new File(WorkflowPrefManager.getPathIconMenu());

		try {
			// FileUtils.cleanDirectory(menuDir);
			// File file = new File(menuDir.getAbsolutePath() + "/icon_menu.txt"
			// );
			PrintWriter s = new PrintWriter(menuDir);

			for (Entry<String, List<String[]>> e : getMenuWA().entrySet()) {
				s.println("menu:" + e.getKey());
				for (String[] string : e.getValue()) {
					s.println(string[0]);
				}
			}
			s.close();

		} catch (Exception e) {
			logger.error(e,e);
			error += "\n" + LanguageManagerWF.getText("workflow.saveMenuFail");
		}

		if (error.isEmpty()) {
			error = null;
		} else {
			logger.error(error);
		}

		return error;
	}

	public DataFlowElement createElementFromClassName(
			Map<String, String> namesWithClassName, String className)
					throws RemoteException, InstantiationException,
					IllegalAccessException, ClassNotFoundException {
		DataFlowElement dfe = null;
		if (className.contains(">")) {
			dfe = new SuperAction(className);
		} else {
			dfe = (DataFlowElement) Class.forName(
					namesWithClassName.get(className)).newInstance();
		}
		return dfe;
	}

	public void addPackageToFooter(Collection<String> packageNames) throws RemoteException{
		Map<String,List<String>> footer = getFooter();
		String user = System.getProperty("user.name");
		PackageManager pm = new PackageManager();
		Iterator<String> packIt = packageNames.iterator();
		while(packIt.hasNext()){
			String packName = packIt.next();
			RedSqirlPackage pck = pm.getAvailablePackage(user, packName);
			String packFooterName = WordUtils.capitalizeFully(packName.replace("redsqirl-", "")
					.replace("_", " ").replace("-", " ")).replace(" ", "_");
			List<String> packActions = pck.getAction();
			if(!footer.containsKey(packFooterName)){
				footer.put(packFooterName,new ArrayList<String>(packActions.size()));
			}
			Iterator<String> actIt = packActions.iterator();
			while(actIt.hasNext()){
				String cur = actIt.next();
				if(!footer.get(packFooterName).contains(cur)){
					footer.get(packFooterName).add(cur);
				}
			}
		}
		loadMenu(footer);
		saveMenu();
		packageNotified(packageNames);
	}

	public void packageNotified(Collection<String> packageNames){
		File footerPackageNotificationFile = new File(WorkflowPrefManager.getPathFooterPackageNotification());
		BufferedWriter bw = null;
		try{
			if(footerPackageNotificationFile.exists()){
				bw = new BufferedWriter(new FileWriter(footerPackageNotificationFile,true));
			}else{
				bw = new BufferedWriter(new FileWriter(footerPackageNotificationFile));
			}
			Iterator<String> it = packageNames.iterator();
			while(it.hasNext()){
				String cur = it.next();
				bw.write(cur);
				bw.newLine();
			}
			bw.close();
		}catch(Exception e){
			logger.error("Error while reading class file",e);
			footerPackageNotificationFile.delete();
		}
	}

	public String[] setPrivilegeOfClass(DataFlowElement dfe, String name,
			String[] parameters) throws RemoteException {
		if (dfe instanceof SuperElement) {
			Boolean priv = ((SuperElement) dfe).getPrivilege();
			logger.debug(dfe.getName() + " " + name + " '" + priv + "");
			if (priv == null) {
				parameters[2] = null;
			} else if(new ModelManager().getAvailableModel(
					System.getProperty("user.name"), 
					RedSqirlModel.getModelAndSW(dfe.getName())[0]).isEditable()){
				parameters[2] = String.valueOf(priv);
			}else{
				parameters[2] = null;
			}
		} else {
			parameters[2] = null;

		}

		return parameters;
	}


	/**
	 * List (cannonical class names) all the classes extending DataflowAction.
	 * 
	 * If possible, the classes will be read from a file. If not a file will be written for next time.
	 * 
	 * @see com.idiro.BlockManager#getNonAbstractClassesFromSuperClass(String)
	 * @return The classes that extends DataflowAction
	 */
	private List<String> getDataflowActionClasses(){
		File dataFlowActionClassFile = new File(WorkflowPrefManager.getPathDataFlowActionClasses());
		List<String> dataFlowActionClassName = new LinkedList<String>();
		if(dataFlowActionClassFile.exists()){

			//logger.info("getDataflowActionClasses exist");

			try{
				BufferedReader br = new BufferedReader(new FileReader(dataFlowActionClassFile));
				String line = null;
				while((line = br.readLine()) != null){
					dataFlowActionClassName.add(line);
				}
				br.close();
			}catch(Exception e){
				logger.error("Error while reading class file",e);
				dataFlowActionClassFile.delete();
			}
		}

		if(!dataFlowActionClassFile.exists()){

			//logger.info("getDataflowActionClasses not exist");

			dataFlowActionClassName = WorkflowPrefManager
					.getInstance()
					.getNonAbstractClassesFromSuperClass(
							DataflowAction.class.getCanonicalName());
			try{
				BufferedWriter bw = new BufferedWriter(new FileWriter(dataFlowActionClassFile));
				Iterator<String> dataoutputClassNameIt = dataFlowActionClassName.iterator();
				while(dataoutputClassNameIt.hasNext()){
					bw.write(dataoutputClassNameIt.next());
					bw.newLine();
				}
				bw.close();
				//Everyone can remove this file
				dataFlowActionClassFile.setReadable(true, false);
				dataFlowActionClassFile.setWritable(true, false);
			}catch(Exception e){
				logger.error("Error while writing class file",e);
				dataFlowActionClassFile.delete();
			}

		}
		logger.debug("Return data flow classes: "+dataFlowActionClassName);
		return dataFlowActionClassName;
	}

	public Map<String, List<String[]>> getRelativeMenu(File curPath) {
		loadMenu();
		if (curPath == null) {
			return menuWA;
		}
		
		logger.debug("Load menu " + curPath.getPath());
		Map<String, List<String[]>> ans = new LinkedHashMap<String, List<String[]>>();
		Iterator<String> menuWAit = menuWA.keySet().iterator();
		while (menuWAit.hasNext()) {
			String key = menuWAit.next();
			Iterator<String[]> actionListit = menuWA.get(key).iterator();
			List<String[]> newActionList = new ArrayList<String[]>();
			while (actionListit.hasNext()) {
				String[] parameters = new String[3];
				String[] absCur = actionListit.next();
				parameters[0] = absCur[0];
				try {
					logger.debug("loadMenu " + curPath + " " + absCur[1]);
					parameters[1] = LocalFileSystem.relativize(curPath,	absCur[1]);
					parameters[2] = absCur[2];
					newActionList.add(parameters);
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
					logger.error("Error Getting relative paths for Image");
				}

			}
			ans.put(key, newActionList);
		}

		return ans;
	}

	public Map<String, Map<String, String[]>> getRelativePackageHelp(File curPath) {
		//packageHelp is static
		if (packageHelp == null || packageHelp.isEmpty()) {
			loadPackageHelp();
		}
		return getRelativeHelp(curPath, packageHelp);
	}
	
	public Map<String, Map<String, String[]>> getRelativeModelHelp(File curPath) {
		loadModelHelp();
		return getRelativeHelp(curPath, modelHelp);
	}

	public Map<String, Map<String, String[]>> getRelativeHelp(File curPath, Map<String, Map<String, String[]>> absoluteHelp) {
		if (curPath == null) {
			return absoluteHelp;
		}
		
		logger.debug("Load help " + curPath.getPath());
		Map<String, Map<String, String[]>> ans = new LinkedHashMap<String, Map<String, String[]>>();
		Iterator<String> helpit = absoluteHelp.keySet().iterator();
		while (helpit.hasNext()) {
			String key = helpit.next();
			try {

				// logger.info("getRelativeHelp " + key);

				Map<String, String[]> out = new LinkedHashMap<String, String[]>();
				
				Map<String, String[]> aux = absoluteHelp.get(key);
				Iterator<String> it = aux.keySet().iterator();
				while (it.hasNext()) {
					String action = it.next();
					
					out.put(action, new String[] {
							action,
							WordUtils.capitalizeFully(action.replace("_", " ")),
							LocalFileSystem.relativize(curPath,aux.get(action)[0]),
							LocalFileSystem.relativize(curPath,aux.get(action)[1])
							});
				}
				
				ans.put(key, out);
				
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
				logger.error("Error Getting relative paths for Help");
			}
		}
		return ans;
	}

	public Set<String> getSuperActions(String wfName) throws RemoteException {
		Set<String> ans = new ModelManager().getAvailableSuperActions(System
				.getProperty("user.name"));
		if(wfName != null && wfName.contains(">")){
			String[] modelSW = RedSqirlModel.getModelAndSW(wfName);
			ans.addAll(new ModelManager().getAvailableModel(System.getProperty("user.name"),modelSW[0]).getSubWorkflowFullNames());
		}
		return ans;
	}

	private Set<String> getSuperActions() throws RemoteException {
		Set<String> ans = new ModelManager().getAvailableSuperActions(System
				.getProperty("user.name"));
		return ans;
	}


	/**
	 * Get all the WorkflowAction available in the jars file
	 * 
	 * To find the jars, the method use
	 * @see com.idiro.BlockManager#getNonAbstractClassesFromSuperClass(String)
	 * 
	 * @return an array containing the name, image and help of the action
	 * @throws Exception
	 *             if one action cannot be load
	 */
	public List<String[]> getAllWA() throws RemoteException {
		logger.debug("get all the Workflow actions");
		List<String[]> result = new LinkedList<String[]>();

		Iterator<String> actionClassName = getAllWANameWithClassName().values().iterator();

		while (actionClassName.hasNext()) {
			String className = actionClassName.next();
			DataflowAction wa;
			try {
				wa = (DataflowAction) Class.forName(className).newInstance();
				result.add(new String[] { wa.getName(), wa.getImage(),
						wa.getHelp() });
			} catch (Exception e) {
				logger.error(e,e);
			}
		}
		// result.add(new String[]{"Red Sqirl Help", "", "test.html"});
		return result;
	}


	/**
	 * Get all the WorkflowAction available in the jars file.
	 * 
	 * To find the jars, the method use
	 * 
	 * @see com.idiro.BlockManager#getNonAbstractClassesFromSuperClass(String)
	 * 
	 * @return the dictionary: key name @see {@link DataflowAction#getName()} ;
	 *         value the canonical class name.
	 * @throws Exception
	 *             if one action cannot be load
	 */
	public Map<String, String> getAllWANameWithClassName() throws RemoteException {

		logger.debug("get all the Workflow actions");
		
		if (flowElement == null && !initializeFlowElement) {
			initializeFlowElement = true;
			List<String> l = getDataflowActionClasses();
			logger.debug("getDataflowActionClasses size " + l.size());
			Map<String, String> flowElementTmp = new HashMap<String,String>(l.size()); 
			
			for (int i = 0; i < l.size(); ++i) {
				String className = l.get(i);

				logger.debug("getAllWANameWithClassName " + i + " " + className);

				try {
					DataflowAction wa = (DataflowAction) Class.forName(className).newInstance();
					if (!(wa instanceof SuperAction)) {
						flowElementTmp.put(wa.getName(), className);
					}else{
						logger.debug("superAction " + className);
					}
				} catch (Exception e) {
					logger.error("Error instanciating class : " + className);
					logger.error(e,e);
				}
			}
			flowElement = flowElementTmp;

			logger.debug("WorkflowAction found : " + flowElement.toString());
		}else if(flowElement == null){
			do{
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					logger.debug(e,e);
				}
			}while(flowElement == null);
		}
		
		logger.debug("flowElement size " +flowElement.size());
		
		return flowElement;
	}




	protected List<String> getFooterPackages(){
		File footerPackageNotificationFile = new File(WorkflowPrefManager.getPathFooterPackageNotification());
		List<String> packageName = new LinkedList<String>();
		if(footerPackageNotificationFile.exists()){
			try{
				BufferedReader br = new BufferedReader(new FileReader(footerPackageNotificationFile));
				String line = null;
				while((line = br.readLine()) != null){
					packageName.add(line);
				}
				br.close();
			}catch(Exception e){
				logger.error("Error while reading class file",e);
				footerPackageNotificationFile.delete();
			}
		}
		return packageName;
	}

	public Collection<String> getPackageToNotify() throws RemoteException{
		Set<String> ans = new PackageManager().getAvailablePackageNames(System.getProperty("user.name"));
		ans.removeAll(getFooterPackages());
		return ans;
	}


	protected Map<String,List<String>> getFooter(){
		if(menuWA == null){
			loadMenu();
		}
		Map<String,List<String>> ans = new LinkedHashMap<String,List<String>>();
		Iterator<String> menuTitleIt = menuWA.keySet().iterator();
		while(menuTitleIt.hasNext()){
			String cur = menuTitleIt.next();
			ans.put(cur, new ArrayList<String>(menuWA.get(cur).size()));
			Iterator<String[]> actionIt = menuWA.get(cur).iterator();
			while(actionIt.hasNext()){
				ans.get(cur).add(actionIt.next()[0]);
			}
		}
		return ans;
	}

	public Map<String, List<String[]>> getMenuWA() {
		return menuWA;
	}

	public void setMenuWA(Map<String, List<String[]>> menuWA) {
		this.menuWA = menuWA;
	}

}
