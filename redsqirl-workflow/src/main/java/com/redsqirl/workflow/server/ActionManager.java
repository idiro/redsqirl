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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.idiro.utils.LocalFileSystem;
import com.redsqirl.workflow.server.action.superaction.SuperAction;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.server.interfaces.ElementManager;
import com.redsqirl.workflow.server.interfaces.SuperElement;
import com.redsqirl.workflow.utils.LanguageManagerWF;
import com.redsqirl.workflow.utils.PackageManager;
import com.redsqirl.workflow.utils.WfSuperActionManager;

public class ActionManager extends UnicastRemoteObject implements ElementManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1577582692193380573L;

	static private Logger logger = Logger.getLogger(ActionManager.class);

	/**
	 * Avoid to call reflexive method again and again
	 */
	protected static Map<String, String> flowElement = new LinkedHashMap<String, String>();
	
	/**
	 * Menu of action, each tab title is link to a list of action name (@see
	 * {@link DataflowAction#getName()})
	 */
	protected Map<String, List<String[]>> menuWA;

	/**
	 * Key: action name, Value: absolute help path, absolute image path
	 */
	protected Map<String, String[]> help;
	
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
	public String loadMenu() {

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
			List<String> superActions = getSuperActions();

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
					error = LanguageManagerWF.getText("workflow.loadclassfail",
							new Object[] { line });
				}
			}
			br.close();
			// menuWA.put(children[i].getName(), new_list);
			// }
			// }

		} catch (Exception e) {
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

	public void loadHelp() {
		help = new LinkedHashMap<String, String[]>();
		Map<String, String> nameWithClass = null;
		try {
			nameWithClass = getAllWANameWithClassName();
			Iterator<String> it = nameWithClass.keySet().iterator();
			while (it.hasNext()) {
				String actionName = it.next();

				// logger.info("loadHelp " + actionName);

				try {
					DataFlowElement dfe = (DataFlowElement) Class.forName(
							nameWithClass.get(actionName)).newInstance();

					help.put(actionName,
							new String[] { dfe.getHelp(), dfe.getImage() });
				} catch (Exception e) {
					logger.error(LanguageManagerWF.getText(
							"workflow.loadclassfail",
							new Object[] { actionName }));
				}
			}
		} catch (Exception e) {
			logger.error(LanguageManagerWF
					.getText("workflow.loadclassexception"));
		}
		
	}
	
	
	public String loadMenu(Map<String, List<String>> newMenu) {

		String error = "";
		menuWA = new LinkedHashMap<String, List<String[]>>();

		Map<String, String> nameWithClass;
		try {
			nameWithClass = getAllWANameWithClassName();
			List<String> superActions = getSuperActions();

			for (Entry<String, List<String>> cur : newMenu.entrySet()) {
				LinkedList<String[]> new_list = new LinkedList<String[]>();
				Iterator<String> it = cur.getValue().iterator();
				while (it.hasNext()) {
					String action = it.next();
					try {
						if (action != null && !action.isEmpty()) {
							if (nameWithClass.get(action) != null
									|| superActions.contains(action)) {
								DataFlowElement dfe = createElementFromClassName(
										nameWithClass, action);

								String[] parameters = new String[3];
								parameters[0] = action;
								parameters[1] = dfe.getImage();
								parameters = setPrivilegeOfClass(dfe, action,
										parameters);
								new_list.add(parameters);
							} else {
								logger.warn("unknown workflow action '"
										+ action + "'");
							}
						}
					} catch (Exception e) {
						error = LanguageManagerWF.getText(
								"workflow.loadclassfail",
								new Object[] { action });
					}
				}
				menuWA.put(cur.getKey(), new_list);
			}

		} catch (Exception e) {
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
	public String saveMenu() {

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
		if (className.startsWith("sa_")) {
			dfe = new SuperAction(className);
		} else {
			dfe = (DataFlowElement) Class.forName(
					namesWithClassName.get(className)).newInstance();
		}
		return dfe;
	}

	public void addPackageToFooter(List<String> packageNames) throws RemoteException{
		Map<String,List<String>> footer = getFooter();
		String user = System.getProperty("user.name");
		PackageManager pm = new PackageManager();
		Iterator<String> packIt = packageNames.iterator();
		while(packIt.hasNext()){
			String packName = packIt.next();
			File pckFile = pm.getPackage(packName, user);
			if(!pckFile.exists()){
				pckFile = pm.getPackage(packName, null);
			}
			List<String> packActions = pm.getAction(new File(pckFile,PackageManager.action_file));
			if(!footer.containsKey(packName)){
				footer.put(packName,new ArrayList<String>(packActions.size()));
			}
			Iterator<String> actIt = packActions.iterator();
			while(actIt.hasNext()){
				String cur = actIt.next();
				if(!footer.get(packName).contains(cur)){
					footer.get(packName).add(cur);
				}
			}
		}
		loadMenu(footer);
		saveMenu();
		packageNotified(packageNames);
	}
	
	public void packageNotified(List<String> packageNames){
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
			logger.info(dfe.getName() + " " + name + " '" + priv + "");
			if (priv == null) {
				parameters[2] = null;
			} else {
				parameters[2] = String.valueOf(((SuperElement) dfe)
						.getPrivilege());
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
		return dataFlowActionClassName;
	}

	public Map<String, List<String[]>> getRelativeMenu(File curPath) {
		if (menuWA == null || menuWA.isEmpty()) {
			loadMenu();
			logger.info("getRelativeMenu loadMenu ");
		}
		if (curPath == null) {
			return menuWA;
		}
		logger.info("Load menu " + curPath.getPath());
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
					parameters[1] = LocalFileSystem.relativize(curPath,
							absCur[1]);
					parameters[2] = absCur[2];
					newActionList.add(parameters);
				} catch (Exception e) {
					logger.error(e.getMessage());
					logger.error("Error Getting relative paths for Image");
				}

			}
			ans.put(key, newActionList);
		}

		return ans;
	}


	public Map<String, String[]> getRelativeHelp(File curPath) {
		if (help == null || help.isEmpty()) {
			loadHelp();
		}
		if (curPath == null) {
			return help;
		}
		logger.info("Load help " + curPath.getPath());
		Map<String, String[]> ans = new LinkedHashMap<String, String[]>();
		Iterator<String> helpit = help.keySet().iterator();
		while (helpit.hasNext()) {
			String key = helpit.next();
			try {

				// logger.info("getRelativeHelp " + key);

				ans.put(key,
						new String[] {
								LocalFileSystem.relativize(curPath,
										help.get(key)[0]),
								LocalFileSystem.relativize(curPath,
										help.get(key)[1]) });
			} catch (Exception e) {
				logger.error(e.getMessage());
				logger.error("Error Getting relative paths for Help");
			}
		}
		return ans;
	}

	public Map<String, String[]> getRelativeHelpSuperAction(File curPath) {
		Map<String, String[]> helpSuperAction = null;
		if (helpSuperAction == null || helpSuperAction.isEmpty()) {
			helpSuperAction = new LinkedHashMap<String, String[]>();
			try {
				Iterator<String> it = getSuperActions().iterator();
				while (it.hasNext()) {
					String actionName = it.next();
					try {
						DataFlowElement dfe = new SuperAction(actionName,true);

						helpSuperAction.put(actionName,
								new String[] { dfe.getHelp(), dfe.getImage() });
						// logger.info("getRelativeHelpSuperAction " +
						// dfe.getHelp());

					} catch (Exception e) {
						logger.error(LanguageManagerWF.getText(
								"workflow.loadclassfail",
								new Object[] { actionName }));
					}
				}
			} catch (Exception e) {
				logger.error(LanguageManagerWF
						.getText("workflow.loadclassexception"));
			}
		}
		if (curPath == null) {
			return helpSuperAction;
		}
		logger.info("Load helpSuperAction " + curPath.getPath());
		Map<String, String[]> ans = new LinkedHashMap<String, String[]>();
		Iterator<String> helpit = helpSuperAction.keySet().iterator();
		while (helpit.hasNext()) {
			String key = helpit.next();
			try {
				ans.put(key,
						new String[] {
								LocalFileSystem.relativize(curPath,
										helpSuperAction.get(key)[0]),
								LocalFileSystem.relativize(curPath,
										helpSuperAction.get(key)[1]) });
			} catch (Exception e) {
				logger.error(e.getMessage());
				logger.error("Error Getting relative paths for Help");
			}
		}
		return ans;
	}
	


	public List<String> getSuperActions() throws RemoteException {
		return new WfSuperActionManager().getAvailableSuperActions(System
				.getProperty("user.name"));
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
				e.printStackTrace();
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
	public Map<String, String> getAllWANameWithClassName()
			throws RemoteException {

		logger.debug("get all the Workflow actions");

		if (flowElement.isEmpty()) {

			Iterator<String> actionClassName = getDataflowActionClasses().iterator();

			while (actionClassName.hasNext()) {
				String className = actionClassName.next();

				// logger.info("getAllWANameWithClassName " + className);

				try {
					DataflowAction wa = (DataflowAction) Class.forName(
							className).newInstance();
					if (!(wa instanceof SuperAction)) {
						flowElement.put(wa.getName(), className);
					}
				} catch (Exception e) {
					logger.error("Error instanciating class : " + className);
					logger.debug(e);
				}
			}

			logger.debug("WorkflowAction found : " + flowElement.toString());
		}
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
	
	public List<String> getPackageToNotify() throws RemoteException{
		List<String> ans = new PackageManager().getAllPackageNames(System.getProperty("user.name"));
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
