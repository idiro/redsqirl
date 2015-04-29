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
import com.redsqirl.workflow.utils.SuperActionInstaller;
import com.redsqirl.workflow.utils.SuperActionManager;

public class SubWorkflowManagerBean extends BaseBean implements Serializable {

	private static  Logger logger = Logger.getLogger(SubWorkflowManagerBean.class);

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
		SuperActionInstaller saInst = new SuperActionInstaller(getSuperActionManager());
		boolean system = asSystem.equals("System");
		
		logger.info("privilege : '" + privilege + "'");
		Boolean privilegeVal = null;
		if (privilege.equals("edit")) {

		} else if (privilege.equals("run")) {
			privilegeVal = new Boolean(false);
		} else if (privilege.equals("license")) {
			privilegeVal = new Boolean(true);
		}
		logger.info(privilege + " + " + privilegeVal);
		
		
		if(!name.startsWith("sa_")){
			name = "sa_"+name;
		}

		swa.setName(name);
		
		String username = system ? null : getUserInfoBean().getUserName();
		String error = null;
		
		saInst.uninstall(username,swa.getName());
		
		error = saInst.install(getUserInfoBean().getUserName(),system, swa, privilegeVal);

		if (error != null && !error.isEmpty()) {
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			logger.info(" " + error);
			usageRecordLog().addError("ERROR INSTALLSUBWORKFLOW", error);
		} else {
			MessageUseful.addInfoMessage("Install Success for " + swa.getName());
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnSuccess", "msnSuccess");
			usageRecordLog().addSuccess("INSTALLSUBWORKFLOW");
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
		
		if(!getSuperActionManager().getAvailableSuperActions(username).contains(swa.getName())){
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

	public void refreshSubworkflowsAllList() throws RemoteException {
		getAdminValue();
		refreshSubworkflowsSystemList();
		refreshSubworkflowsUser();
	}

	public void refreshSubworkflowsSystemList() throws RemoteException {
		List<String> listSa = getSuperActionManager().getSysSuperActions();
		
		uninstallSysSa = new ArrayList<SelectItem>();
		for (int i = 0; i < listSa.size(); ++i) {
			String s = listSa.get(i);
			uninstallSysSa.add(new SelectItem(s, s));
		}
		logger.info("system sa " + systemSA.length);
	}

	public void refreshSubworkflowsUser() throws RemoteException {
		List<String> listSa = getSuperActionManager()
				.getUserSuperActions(getUserInfoBean().getUserName());
		
		uninstallUserSa = new ArrayList<SelectItem>();
		for (int i = 0; i < listSa.size(); ++i) {
			String s = listSa.get(i);
			uninstallUserSa.add(new SelectItem(s, s));
		}
	}

	public void deleteSASystem() throws RemoteException {
		logger.info("delete user sa");
		if (getAdmin()) {
			SuperActionInstaller saInst = new SuperActionInstaller(getSuperActionManager());
			for (String s : getSystemSA()) {
				saInst.uninstall(null, s);
			}
		}
		refreshSubworkflowsSystemList();
		
		usageRecordLog().addSuccess("DELETESASYSTEM");
	}

	public void deleteSaUser() throws RemoteException {
		logger.info("delete user sa");
		String user = getUserInfoBean().getUserName();
		SuperActionInstaller saInst = new SuperActionInstaller(getSuperActionManager());
		for (String s : getUserSA()) {
			logger.info("delete user sa " + s);
			saInst.uninstall(user, s);
		}
		refreshSubworkflowsUser();

		usageRecordLog().addSuccess("DELETESAUSER");
	}

	public void exportSa() throws RemoteException {
		DataFlowInterface dfi = getworkFlowInterface();
		SubDataFlow swa = dfi.getSubWorkflow(actualName);
		logger.info("privilege : '" + privilege + "'");
		Boolean privilegeVal = null;
		if (privilege.equals("edit")) {

		} else if (privilege.equals("run")) {
			privilegeVal = new Boolean(false);
		} else if (privilege.equals("license")) {
			privilegeVal = new Boolean(true);
		}
		logger.info(privilege + " + " + privilegeVal);
		if(!name.startsWith("sa_")){
			name = "sa_"+name;
		}
		
		swa.setName(name);

		String filePath ="/user/"+getUserInfoBean().getUserName()+"/redsqirl-save/"+name+".srs";
		String error = getSuperActionManager().export(filePath, swa, privilegeVal);

		if (error != null && !error.isEmpty()) {
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			logger.info(" " + error);
			usageRecordLog().addError("ERROR EXPORTSUPERACTION", error);
		} else {
			MessageUseful
					.addInfoMessage("Export Success for " + swa.getName());
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnSuccess", "msnSuccess");
			usageRecordLog().addSuccess("EXPORTSUPERACTION");
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
			String error = getSuperActionManager().importSA(getUserInfoBean().getUserName(),
					pathHdfs);

			if (error != null && !error.isEmpty()) {
				MessageUseful.addErrorMessage(error);
				HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
				request.setAttribute("msnError", "msnError");
				usageRecordLog().addError("ERROR IMPORTSUPERACTION", error);
			} else {
				MessageUseful.addInfoMessage("Import Success");
				HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
				request.setAttribute("msnSuccess", "msnSuccess");
				usageRecordLog().addSuccess("IMPORTSUPERACTION");
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
