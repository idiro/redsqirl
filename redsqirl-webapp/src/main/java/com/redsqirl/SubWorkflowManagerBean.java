package com.redsqirl;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.faces.context.FacesContext;
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
	
	public void installCurrentSubWorkflow() throws RemoteException{
		boolean ok = false;
		
		FacesContext context = FacesContext.getCurrentInstance();
		UserInfoBean userInfoBean = (UserInfoBean) context.getApplication()
				.evaluateExpressionGet(context, "#{userInfoBean}",
						UserInfoBean.class);
		logger.info("subWorkflow name  "+name);
		logger.info("subWorkflow name  "+actualName);
		DataFlowInterface dfi = getworkFlowInterface();
		SubDataFlow swa = dfi.getSubWorkflow(actualName);
		
		boolean system = asSystem.equals("System");
		swa.setName("sa_"+name);
		
		String username = system ? null : userInfoBean.getUserName();
		
		String error = saManager.install(username, swa, null);
		
		
		if(error!=null && !error.isEmpty()){
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			logger.info(" "+error);
		}else{
//			MessageUseful.addInfoMessage("Install Success for "+swa.getName());
			
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
			FacesContext context = FacesContext.getCurrentInstance();
			UserInfoBean userInfoBean = (UserInfoBean) context.getApplication()
					.evaluateExpressionGet(context, "#{userInfoBean}",
							UserInfoBean.class);
			String user = userInfoBean.getUserName();
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



}
