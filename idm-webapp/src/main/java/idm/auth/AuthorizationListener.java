package idm.auth;

import idm.CanvasBean;

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
		boolean iscanvasPage = (currentPage.lastIndexOf("canvas.xhtml") > -1);
		
		//logger.info("currentPage " + currentPage);
		
		
		//call init if the startInit is set to s then set it to n. This is needed as itit needs to be called at least once before 
		// the canvas has loaded otherwise Dataflowinterface is wrong. This is the event that tomcat has crashed and the interface is still running the next time.
		if(iscanvasPage){
			if(session.getAttribute("startInit") != null && session.getAttribute("startInit").equals("s")){
				FacesContext context = FacesContext.getCurrentInstance();
				String url = context.getCurrentInstance().getViewRoot().getViewId();
				logger.info("url : " + url);
				CanvasBean cb = (CanvasBean) context.getApplication()
						.evaluateExpressionGet(context, "#{canvasBean}",
								CanvasBean.class);
				cb.init();
				session.setAttribute("startInit","n");
			}
		}
		
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