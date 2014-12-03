package com.redsqirl;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.redsqirl.auth.UserInfoBean;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;
import com.redsqirl.workflow.utils.SuperActionManager;

public class SubWorkflowManagerBean extends BaseBean implements Serializable {

	private SuperActionManager saManager = new SuperActionManager();

	private Logger logger = Logger.getLogger(getClass());

	private String name = "";
	private String actualName = "";
	private String privilege = "";
	private String pathHDFS = "";
	private boolean admin = false;
	private String exists ;

	/**
	 * @param admin
	 *            the admin to set
	 */
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	private String asSystem = "";

	// For selection uninstall
	private List<SelectItem> uninstallUserSa = new ArrayList<SelectItem>(),
			uninstallSysSa = new ArrayList<SelectItem>();

	// List of sub workflows
	private String[] userSA = new String[] {};
	private String[] systemSA = new String[] {};

	public void installCurrentSubWorkflow() throws RemoteException {

		logger.info("subWorkflow name  " + name);
		logger.info("subWorkflow actual name  " + actualName);
		DataFlowInterface dfi = getworkFlowInterface();
		SubDataFlow swa = dfi.getSubWorkflow(actualName);

		boolean system = asSystem.equals("System");
		
		logger.info("privilage : '" + privilege + "'");
		Boolean privilageVal = null;
		if (privilege.equals("edit")) {

		} else if (privilege.equals("run")) {
			privilageVal = new Boolean(false);
		} else if (privilege.equals("license")) {
			privilageVal = new Boolean(true);
		}
		logger.info(privilege + " + " + privilageVal);
		
		
		if(!name.startsWith("sa_")){
			name = "sa_"+name;
		}

		swa.setName(name);
		
		String username = system ? null : getUserInfoBean().getUserName();
		String error = null;
		
		saManager.uninstall(username,swa.getName());
		
		error = saManager.install(username, swa, privilageVal);

		if (error != null && !error.isEmpty()) {
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			logger.info(" " + error);
		} else {
			MessageUseful
					.addInfoMessage("Install Success for " + swa.getName());
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnSuccess", "msnSuccess");

		}
	}
	
	public void checkExistenceCurrentSubWorkflow() throws RemoteException{
		exists = "false";
		DataFlowInterface dfi = getworkFlowInterface();
		SubDataFlow swa = dfi.getSubWorkflow(actualName);
		boolean system = asSystem.equals("System");
		String username = system ? null : getUserInfoBean().getUserName();
		logger.info(system);
		logger.info(username);
		logger.info(name);
		logger.info(actualName);
		logger.info(swa==null);
		
		if(!name.startsWith("sa_")){
			name = "sa_"+name;
		}

		swa.setName(name);
		
		if(!saManager.getAvailableSuperActions(username).contains(swa.getName())){
			exists = "true";
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {

		this.name = name;
	}

	public boolean getAdmin() {
		return admin;
	}

	public void getAdminValue() {
		admin = false;
		try {
			logger.info("is admin");

			String user = getUserInfoBean().getUserName();
			String[] admins = WorkflowPrefManager.getSysAdminUser();
			if (admins != null) {
				for (String cur : admins) {
					admin = admin || cur.equals(user);
					logger.debug("admin user: " + cur);
				}
			}
		} catch (Exception e) {
			logger.warn("Exception in isAdmin: " + e.getMessage());
		}
		logger.info("is admin " + admin);
	}

	public void mountSubName() {
		String val = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("subWorkflowName");
		setAsSystem("User");
		this.actualName = val;
		setName(val);
		setPrivilege("edit");
	}

	/**
	 * @return the asSystem
	 */
	public String getAsSystem() {
		return asSystem;
	}

	/**
	 * @param asSystem
	 *            the asSystem to set
	 */
	public void setAsSystem(String asSystem) {
		this.asSystem = asSystem;
	}

	public void refreshSubworkflowsAllList() {
		getAdminValue();
		refreshSubworkflowsSystemList();
		refreshSubworkflowsUser();
	}

	public void refreshSubworkflowsSystemList() {
		List<String> listSa = saManager.getAvailableSuperActions(null);
		
		uninstallSysSa = new ArrayList<SelectItem>();
		for (int i = 0; i < listSa.size(); ++i) {
			String s = listSa.get(i);
			uninstallSysSa.add(new SelectItem(s, s));
		}
		logger.info("system sa " + systemSA.length);
	}

	public void refreshSubworkflowsUser() {
		List<String> listSa = saManager
				.getAvailableSuperActions(getUserInfoBean().getUserName());
		
		uninstallUserSa = new ArrayList<SelectItem>();
		for (int i = 0; i < listSa.size(); ++i) {
			String s = listSa.get(i);
			uninstallUserSa.add(new SelectItem(s, s));
		}
	}

	public void deleteSASystem() {
		logger.info("delete user sa");
		if (getAdmin()) {
			for (String s : getSystemSA()) {
				saManager.uninstall(null, s);
			}
		}
		refreshSubworkflowsSystemList();
	}

	public void deleteSaUser() {
		logger.info("delete user sa");
		String user = getUserInfoBean().getUserName();
		for (String s : getUserSA()) {
			logger.info("delete user sa " + s);
			saManager.uninstall(user, s);
		}
		refreshSubworkflowsUser();

	}

	public void exportSa() throws RemoteException {
		DataFlowInterface dfi = getworkFlowInterface();
		SubDataFlow swa = dfi.getSubWorkflow(actualName);
		logger.info("privilage : '" + privilege + "'");
		Boolean privilageVal = null;
		if (privilege.equals("edit")) {

		} else if (privilege.equals("run")) {
			privilageVal = new Boolean(false);
		} else if (privilege.equals("license")) {
			privilageVal = new Boolean(true);
		}
		logger.info(privilege + " + " + privilageVal);
		if(!name.startsWith("sa_")){
			name = "sa_"+name;
		}
		
		swa.setName(name);

		String filePath ="/user/"+getUserInfoBean().getUserName()+"/redsqirl-save/"+name+".srs";
		String error = saManager.export(filePath, swa, privilageVal);

		if (error != null && !error.isEmpty()) {
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			logger.info(" " + error);
		} else {
			MessageUseful
					.addInfoMessage("Export Success for " + swa.getName());
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnSuccess", "msnSuccess");

		}

	}

	public void importSa() throws IOException {

		String pathHdfs = getPathHDFS();
		logger.info("path '" + pathHdfs + "'");
		if (pathHdfs == null || pathHdfs.isEmpty()) {
			MessageUseful.addErrorMessage("Path to get SubWorkflow is Empty");
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		} else {
			String error = saManager.importSA(getUserInfoBean().getUserName(),
					pathHdfs);

			if (error != null && !error.isEmpty()) {
				MessageUseful.addErrorMessage(error);
				HttpServletRequest request = (HttpServletRequest) FacesContext
						.getCurrentInstance().getExternalContext().getRequest();
				request.setAttribute("msnError", "msnError");
			} else {
				MessageUseful.addInfoMessage("Import Success");
				HttpServletRequest request = (HttpServletRequest) FacesContext
						.getCurrentInstance().getExternalContext().getRequest();
				request.setAttribute("msnSuccess", "msnSuccess");
			}
		}

	}

	private UserInfoBean getUserInfoBean() {
		FacesContext context = FacesContext.getCurrentInstance();
		UserInfoBean userInfoBean = (UserInfoBean) context.getApplication()
				.evaluateExpressionGet(context, "#{userInfoBean}",
						UserInfoBean.class);

		return userInfoBean;
	}

	/**
	 * @return the userSA
	 */
	public String[] getUserSA() {
		return userSA;
	}

	/**
	 * @param userSA2
	 *            the userSA to set
	 */
	public void setUserSA(String[] userSA2) {
		this.userSA = userSA2;
	}

	/**
	 * @param systemSA
	 *            the systemSA to set
	 */
	public void setSystemSA(String[] systemSA) {
		this.systemSA = systemSA;
	}

	/**
	 * @return the systemSA
	 */
	public String[] getSystemSA() {
		return systemSA;
	}

	/**
	 * @return the uninstallUserSa
	 */
	public List<SelectItem> getUninstallUserSa() {
		return uninstallUserSa;
	}

	/**
	 * @param uninstallUserSa
	 *            the uninstallUserSa to set
	 */
	public void setUninstallUserSa(List<SelectItem> uninstallUserSa) {
		this.uninstallUserSa = uninstallUserSa;
	}

	/**
	 * @return the uninstallSysSa
	 */
	public List<SelectItem> getUninstallSysSa() {
		return uninstallSysSa;
	}

	/**
	 * @param uninstallSysSa
	 *            the uninstallSysSa to set
	 */
	public void setUninstallSysSa(List<SelectItem> uninstallSysSa) {
		this.uninstallSysSa = uninstallSysSa;
	}

	public String getPathHDFS() {
		return pathHDFS;
	}

	public void setPathHDFS(String pathHDFS) {
		this.pathHDFS = pathHDFS;
	}

	public String getPrivilege() {
		return privilege;
	}

	public void setPrivilege(String privilege) {
		this.privilege = privilege;
	}

	public String getExists() {
		return exists;
	}

	public void setExists(String exists) {
		this.exists = exists;
	}

}
