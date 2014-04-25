package idm.useful;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import com.sun.faces.util.MessageFactory;


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
	@SuppressWarnings("unchecked")
	public static void addErrorMessage(String msg) {
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		
		if(session.getAttribute("listError") == null){
			List<SelectItem> listError = new LinkedList<SelectItem>(); 
			session.setAttribute("listError", listError);
		}
		
		if( ((List<SelectItem>)session.getAttribute("listError")).size() <= 500){
			Format formatter = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss");
			((List<SelectItem>)session.getAttribute("listError")).add(new SelectItem(formatter.format(new Date()), msg));
		}
		
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
	 * Add message of error(FacesMessage.SEVERITY_WARN) on request with parameters
	 * 
	 * @param id or <code>null</code> for global message
	 * @param msg idmessage
	 * @param arg array of parameters
	 */
	public static void addErrorMessageParameter(String id, String msg, Object[] arg) {
		FacesContext.getCurrentInstance().addMessage(id, MessageFactory.getMessage(FacesContext.getCurrentInstance(), msg, arg));
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