package com.redsqirl.analyticsStore;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.json.JSONException;
import org.json.JSONObject;

import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


/**
 * Simple login bean.
 *
 */
public class AnalyticsStoreLoginBean implements Serializable {

	private static final long serialVersionUID = 7765876811740798583L;
	
//	@NotEmpty (message="{loginBean_null_email}")
	private String email;

//	@NotEmpty (message="{loginBean_null_password}")
	private String password;

	private boolean loggedIn;
	
	private int idUser;
	
	private String role;
	
	/**
	 * Login operation.
	 * @return
	 * @throws IOException 
	 */
	public String doLogin() throws IOException {
		
		try{
			String uri = getRepoServer()+"rest/login";
			
			JSONObject object = new JSONObject();
			object.put("email", email);
			object.put("password", password);
			
			Client client = Client.create();
			WebResource webResource = client.resource(uri);
	
			ClientResponse response = webResource.type("application/json")
			   .post(ClientResponse.class, object.toString());
			String ansServer = response.getEntity(String.class);
			
			try{
				JSONObject pckObj = new JSONObject(ansServer);
				loggedIn = pckObj.getBoolean("logged");
				role = pckObj.getString("role");
				if (loggedIn){
					idUser = pckObj.getInt("id");
				}
			} catch (JSONException e){
				e.printStackTrace();
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		
		if (loggedIn) {
			
			// Redirect the user back to where they have been before logging in
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			String originalUrl = (String) externalContext.getRequestParameterMap().get("originalURL");
			String queryString = (String) externalContext.getRequestParameterMap().get("originalQuery");
			String url = originalUrl != null && !originalUrl.isEmpty() ? originalUrl : "secured/search.xhtml";
			if (queryString != null && !queryString.isEmpty()){
				url += "?" + queryString;
			}
			
			ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
			ec.redirect(url);
			
			//FacesContext.getCurrentInstance().getExternalContext().redirect("http://localhost:9090"+url);
			
			//return "/home.xhtml?faces-redirect=true";
		}

		// Set login ERROR
//		FacesMessage msg = new FacesMessage("ERROR MSG", MessageProvider.getValue("login_error_wrong_user_password"));
		FacesMessage msg = new FacesMessage("ERROR MSG", "login_error_wrong_user_password");
		msg.setSeverity(FacesMessage.SEVERITY_ERROR);
		FacesContext.getCurrentInstance().addMessage("login-form:password-input", msg);
//		FacesContext.getCurrentInstance().validationFailed();

		// To to login page
		return null;
	}
	
	public String getRepoServer(){
		String pckServer = WorkflowPrefManager.getPckManagerUri();
		if(!pckServer.endsWith("/")){
			pckServer+="/";
		}
		return pckServer;
	}

	/**
	 * Logout operation.
	 * @return
	 */
	public String doLogout() {
		// Set the paremeter indicating that user is logged in to false
		loggedIn = false;
//		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

		// Set logout message
//		FacesMessage msg = new FacesMessage(MessageProvider.getValue("login_logout_success"), "INFO MSG");
		FacesMessage msg = new FacesMessage("login_logout_success", "INFO MSG");
		msg.setSeverity(FacesMessage.SEVERITY_INFO);
		FacesContext.getCurrentInstance().addMessage(null, msg);

		//        return navigationBean.toLogin();
//		return "/initial.xhtml?faces-redirect=true";
		return "/home.xhtml?faces-redirect=true";
	}
	
	public void logOut() {
		try {
			// Disconnect from the provider
			// Invalidate session
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
//			HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
			//this.invalidateSession(request);
//			FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
			// Redirect to home page
			FacesContext.getCurrentInstance().getExternalContext().redirect(externalContext.getRequestContextPath() + "home.xhtml");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	// ------------------------------
	// Getters & Setters

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}
	
	public int getIdUser() {
		return idUser;
	}

	public void setIdUser(int idUser) {
		this.idUser = idUser;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
}