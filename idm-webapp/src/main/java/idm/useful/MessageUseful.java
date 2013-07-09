package idm.useful;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;


/** MessageUseful
 * 
 * Class to add methods for message on sreens
 * 
 * 
 * @author Igor.Souza
 */
public class MessageUseful {

	/**
	 * Add global message of information(FacesMessage.SEVERITY_INFO) on request
	 * 
	 * @param msg message
	 * @see MessageUseful#addInfoMessage(String, String)
	 */
	public static void addInfoMessage(String msg) {
		addInfoMessage(null, msg);
	}

	/**
	 * Add message of information(FacesMessage.SEVERITY_INFO) on request
	 * 
	 * @param id or <code>null</code> for global message
	 * @param msg message
	 */
	public static void addInfoMessage(String id, String msg) {
		FacesContext.getCurrentInstance().addMessage(id, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg));
	}

	/**
	 * Add global message of error(FacesMessage.SEVERITY_ERROR) on request
	 * 
	 * @param msg message
	 * @see MessageUseful#addErrorMessage(String, String)
	 */
	public static void addErrorMessage(String msg) {
		addErrorMessage(null, msg);
	}

	/**
	 * Add message of error(FacesMessage.SEVERITY_WARN) on request
	 * 
	 * @param id or <code>null</code> for global message
	 * @param msg message
	 */
	public static void addErrorMessage(String id, String msg) {
		FacesContext.getCurrentInstance().addMessage(id, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
	}
	
	/**
	 * Add global message of alert(FacesMessage.SEVERITY_WARN) on request
	 * 
	 * @param msg message
	 * @see MessageUseful#addErrorMessage(String, String)
	 */
	public static void addWarnMessage(String msg) {
		addWarnMessage(null, msg);
	}

	/**
	 * Add message of alert(FacesMessage.SEVERITY_ERROR) on request
	 * 
	 * @param id or <code>null</code> for global message
	 * @param msg message
	 */
	public static void addWarnMessage(String id, String msg) {
		FacesContext.getCurrentInstance().addMessage(id, new FacesMessage(FacesMessage.SEVERITY_WARN, msg, msg));
	}

}