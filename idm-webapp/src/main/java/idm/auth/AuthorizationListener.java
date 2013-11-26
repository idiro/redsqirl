package idm.auth;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/** AuthorizationListener
 * 
 * Class to control the User and application. checks if the User logged in
 * 
 * @author Igor.Souza
 */
public class AuthorizationListener implements PhaseListener {

	private static Logger logger = Logger.getLogger(AuthorizationListener.class);

	/** afterPhase
	 * 
	 * Methods to generating screen
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void afterPhase(PhaseEvent event) {

		FacesContext facesContext = event.getFacesContext();
		String currentPage = facesContext.getViewRoot().getViewId();
		HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();

		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		boolean isLoginPage = (currentPage.lastIndexOf("initial.xhtml") > -1) || (currentPage.lastIndexOf("restart.xhtml") > -1) || (currentPage.lastIndexOf("restart2.xhtml") > -1);

		//logger.info("currentPage " + currentPage);
		
		if(session==null){

			if (!isLoginPage){
				request.setAttribute("msnLoginError", "msnLoginError");
			}

			NavigationHandler nh = facesContext.getApplication().getNavigationHandler();
			nh.handleNavigation(facesContext, null, "loginPage");

		}else{
			Object currentUser = session.getAttribute("username");

			if (!isLoginPage && (currentUser == null || currentUser.equals(""))) {

				request.setAttribute("msnLoginError", "msnLoginError");
				NavigationHandler nh = facesContext.getApplication().getNavigationHandler();
				nh.handleNavigation(facesContext, null, "loginPage");
			}
		}
	}

	/** beforePhase
	 * 
	 */
	public void beforePhase(PhaseEvent event) {

	}

	public PhaseId getPhaseId() {
		return PhaseId.RESTORE_VIEW;
	}

}