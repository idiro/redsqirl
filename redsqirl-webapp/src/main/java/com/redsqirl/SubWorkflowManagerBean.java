package com.redsqirl;

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

public class SubWorkflowManagerBean extends BaseBean implements Serializable{
	
	private SuperActionManager saManager = new SuperActionManager();
	
	private Logger logger = Logger.getLogger(getClass());

	private String name ="";
	private String actualName ="";
	
	private String asSystem = "";
	
	//For selection uninstall
	private List<SelectItem> uninstallUserSa = new ArrayList<SelectItem>(),	uninstallSysSa = new ArrayList<SelectItem>();
	
	//List of sub workflows
	private List<String> userSA = new ArrayList<String>();
	private List<String> systemSA = new ArrayList<String>();
	
	public void installCurrentSubWorkflow() throws RemoteException{
		boolean ok = false;
		
		
		logger.info("subWorkflow name  "+name);
		logger.info("subWorkflow name  "+actualName);
		DataFlowInterface dfi = getworkFlowInterface();
		SubDataFlow swa = dfi.getSubWorkflow(actualName);
		
		boolean system = asSystem.equals("System");
		swa.setName("sa_"+name);
		
		String username = system ? null : getUserInfoBean().getUserName();
		
		String error = saManager.install(username, swa, null);
		
		
		if(error!=null && !error.isEmpty()){
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			logger.info(" "+error);
		}else{
			MessageUseful.addInfoMessage("Install Success for "+swa.getName());
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnSuccess", "msnSuccess");
			
		}
		ok = error ==null || error.isEmpty();
	}
	
	
	public String getName(){
		return name;
	}
	
	public void setName(String name) {
		
		this.name = name;
	}


	
	
	public boolean getAdmin(){
		boolean admin = false;
		try{
			logger.debug("is admin");
			
			String user = getUserInfoBean().getUserName();
			String[] admins = WorkflowPrefManager.getSysAdminUser();
			if(admins != null){
				for(String cur: admins){
					admin = admin || cur.equals(user);
					logger.debug("admin user: "+cur);
				}
			}
		}catch(Exception e){
			logger.warn("Exception in isAdmin: "+e.getMessage());
		}
		return admin;
	}
	public void mountSubName(){
		String val = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("subWorkflowName");
		setAsSystem("User");
		this.actualName = val;
		setName(val);
	}


	/**
	 * @return the asSystem
	 */
	public String getAsSystem() {
		return asSystem;
	}


	/**
	 * @param asSystem the asSystem to set
	 */
	public void setAsSystem(String asSystem) {
		this.asSystem = asSystem;
	}
	
	public void refreshSubworkflowsAllList(){
		refreshSubworkflowsSystemList();
		refreshSubworkflowsUser();
	}

	
	public void refreshSubworkflowsSystemList(){
		systemSA = saManager.getAvailableSuperActions(null);
		uninstallSysSa = new ArrayList<SelectItem>(systemSA.size());
		setSystemSA(systemSA);
	}
	
	public void refreshSubworkflowsUser(){
		
		userSA =  saManager.getAvailableSuperActions(getUserInfoBean().getUserName());
		uninstallUserSa = new ArrayList<SelectItem>(userSA.size());
		setUserSA(userSA);
	}
	
	public void deleteSASystem(){
		if(getAdmin()){
			for(SelectItem saName : uninstallSysSa){
				saManager.uninstall(null, saName.getLabel());
			}
		}
	}
	
	public void deleteSaUser(){
		String user = getUserInfoBean().getUserName();
		for(SelectItem saName : uninstallUserSa){
			saManager.uninstall(user, saName.getLabel());
		}
	}
	
	private UserInfoBean getUserInfoBean(){
		FacesContext context = FacesContext.getCurrentInstance();
		UserInfoBean userInfoBean = (UserInfoBean) context.getApplication()
				.evaluateExpressionGet(context, "#{userInfoBean}",
						UserInfoBean.class);
		
		return userInfoBean;
	}
	


	/**
	 * @return the userSA
	 */
	public List<String> getUserSA() {
		return userSA;
	}


	/**
	 * @param userSA the userSA to set
	 */
	public void setUserSA(List<String> userSA) {
		this.userSA = userSA;
	}

	

	/**
	 * @param systemSA the systemSA to set
	 */
	public void setSystemSA(List<String> systemSA) {
		this.systemSA = systemSA;
	}


	/**
	 * @return the systemSA
	 */
	public List<String> getSystemSA() {
		return systemSA;
	}


	/**
	 * @return the uninstallUserSa
	 */
	public List<SelectItem> getUninstallUserSa() {
		return uninstallUserSa;
	}


	/**
	 * @param uninstallUserSa the uninstallUserSa to set
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
	 * @param uninstallSysSa the uninstallSysSa to set
	 */
	public void setUninstallSysSa(List<SelectItem> uninstallSysSa) {
		this.uninstallSysSa = uninstallSysSa;
	}

	




}
