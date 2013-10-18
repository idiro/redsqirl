package idm.useful;

import idm.ItemList;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;


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
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		
		if(session.getAttribute("listError") == null){
			List<ItemList> listError = new ArrayList<ItemList>(); 
			session.setAttribute("listError", listError);
		}
		
		Format formatter = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss");
		((ArrayList<ItemList>)session.getAttribute("listError")).add(new ItemList(formatter.format(new Date()), msg));
		
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