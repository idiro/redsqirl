package com.redsqirl.analyticsStore;

import java.io.Serializable;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import com.redsqirl.BaseBean;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


public class AnalyticsStoreNewUserBean extends BaseBean implements Serializable{

	private String firstName;
	private String lastName;
	private String email;
	private String company;
	private String password;
	private String repeatPassword;

	public String createUser() throws Exception{

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

				try{
					JSONObject pckObj = new JSONObject(ansServer);
					String errorBackEnd = pckObj.getString("error");

				} catch (JSONException e){
					e.printStackTrace();
				}

				ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
				String originalUrl = (String) externalContext.getRequestParameterMap().get("originalURL");
				String queryString = (String) externalContext.getRequestParameterMap().get("originalQuery");
				String url = originalUrl != null && !originalUrl.isEmpty() ? originalUrl : "secured/search.xhtml";
				if (queryString != null && !queryString.isEmpty()){
					url += "?" + queryString;
				}

				ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
				ec.redirect(url);

			}

		}catch(Exception e){
			e.printStackTrace();
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