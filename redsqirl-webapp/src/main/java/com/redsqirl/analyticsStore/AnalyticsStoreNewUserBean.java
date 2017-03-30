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

package com.redsqirl.analyticsStore;

import java.io.Serializable;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.redsqirl.BaseBean;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


public class AnalyticsStoreNewUserBean extends BaseBean implements Serializable{

	private static Logger logger = Logger.getLogger(AnalyticsStoreNewUserBean.class);
	private String firstName;
	private String lastName;
	private String email;
	private String company;
	private String password;
	private String repeatPassword;
	private boolean creatingUser = false;

	public String createUser() throws Exception{
		if(!creatingUser){
			creatingUser = true;
			try{
				String uri = getRepoServer()+"rest/createnewuser";

				JSONObject object = new JSONObject();
				object.put("firstName", getFirstName());
				object.put("lastName", getLastName());
				object.put("email", getEmail());
				object.put("company", getCompany());
				object.put("password", getPassword());

				boolean error = false;

				if (password != null && !password.isEmpty()){
					if (!password.equals(repeatPassword)){
						error = true;
						MessageUseful.addErrorMessage(getMessageResources("error_different_passwords"));
						HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
						request.setAttribute("msnErrorNewUser", "msnErrorNewUser");
					}
				}

				if (email != null && !email.isEmpty() && !error){

					boolean isEmail = email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
							+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

					if (!isEmail){
						error = true;
						MessageUseful.addErrorMessage(getMessageResources("error_invalid_email"));
						HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
						request.setAttribute("msnErrorNewUser", "msnErrorNewUser");
					}

				}

				if(!error){

					Client client = Client.create();
					WebResource webResource = client.resource(uri);

					ClientResponse response = webResource.type("application/json").post(ClientResponse.class, object.toString());
					String ansServer = response.getEntity(String.class);

					JSONObject pckObj = new JSONObject(ansServer);
					String errorBackEnd = pckObj.getString("error");

					if(errorBackEnd.contains("successfully")){
						MessageUseful.addInfoMessage(errorBackEnd);
						HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
						request.setAttribute("msnSuccessNewUser", "msnSuccessNewUser");
					}else{
						MessageUseful.addErrorMessage(errorBackEnd);
						HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
						request.setAttribute("msnErrorNewUser", "msnErrorNewUser");
					}
					
				}

			}catch(Exception e){
				logger.error(e,e);
			}
			creatingUser = false;
		}

		return null;
	}

	public String getRepoServer(){
		String pckServer = WorkflowPrefManager.getPckManagerUri();
		if(!pckServer.endsWith("/")){
			pckServer+="/";
		}
		return pckServer;
	}



	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public String getRepeatPassword() {
		return repeatPassword;
	}

	public void setRepeatPassword(String repeatPassword) {
		this.repeatPassword = repeatPassword;
	}

}