package com.redsqirl.auth;


import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.idiro.ProjectID;
import com.redsqirl.CanvasBean;
import com.redsqirl.ConfigureTabsBean;
import com.redsqirl.HelpBean;
import com.redsqirl.ProcessManagerBean;
import com.redsqirl.SettingsBean;
import com.redsqirl.workflow.server.WorkflowPrefManager;

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
		HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

		if(facesContext != null && facesContext.getViewRoot() != null && facesContext.getViewRoot().getViewId() != null){
			String currentPage = facesContext.getViewRoot().getViewId();

			boolean isLoginPage = (currentPage.lastIndexOf("initial.xhtml") > -1) || (currentPage.lastIndexOf("restart.xhtml") > -1);
			boolean iscanvasPage = (currentPage.lastIndexOf("canvas.xhtml") > -1);

			//logger.info("currentPage " + currentPage);

			//call init if the startInit is set to s then set it to n. This is needed as itit needs to be called at least once before 
			// the canvas has loaded otherwise Dataflowinterface is wrong. This is the event that tomcat has crashed and the interface is still running the next time.
			if(iscanvasPage && session != null){
				if(session.getAttribute("startInit") != null && session.getAttribute("startInit").equals("s")){

					FacesContext context = FacesContext.getCurrentInstance();

					String url = context.getCurrentInstance().getViewRoot().getViewId();
					logger.info("url : " + url);

					CanvasBean cb = (CanvasBean) context.getApplication().evaluateExpressionGet(context, "#{canvasBean}", CanvasBean.class);
					cb.init();

					ConfigureTabsBean configureTabsBean = (ConfigureTabsBean) context.getApplication().evaluateExpressionGet(context, "#{configureTabsBean}", ConfigureTabsBean.class);
					configureTabsBean.openCanvasScreen();

					ProcessManagerBean processManagerBean = (ProcessManagerBean) context.getApplication().evaluateExpressionGet(context, "#{processManagerBean}", ProcessManagerBean.class);
					processManagerBean.retrievesProcessesGrid();

					HelpBean helpBean = (HelpBean) context.getApplication().evaluateExpressionGet(context, "#{helpBean}", HelpBean.class);
					helpBean.calcHelpItens();

					/*SettingsBean settingsBean = (SettingsBean) context.getApplication().evaluateExpressionGet(context, "#{settingsBean}", SettingsBean.class);
					settingsBean.calcSettings();
					settingsBean.defaultSettings();*/

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
			
			if(isLoginPage){
				String value = checkIfExistLicense();
				if(value != null){
					NavigationHandler nh = facesContext.getApplication().getNavigationHandler();
					nh.handleNavigation(facesContext, null, value);
				}
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
	
	public String checkIfExistLicense(){

		String softwareKey = getSoftwareKey();
		if(softwareKey == null ){
			return "adminLogin";
		}

		return null;
	}
	
	private String getSoftwareKey(){
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(WorkflowPrefManager.pathSystemPref + "/licenseKey.properties");

			// load a properties file
			prop.load(input);

			// get the property value and print it out

			String licenseKey;
			String[] value = ProjectID.get().trim().split("-");
			if(value != null && value.length > 1){
				licenseKey = value[0].replaceAll("[0-9]", "") + value[value.length-1];
			}else{
				licenseKey = ProjectID.get();
			}

			return prop.getProperty(formatTitle(licenseKey));
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	private String formatTitle(String title){
		return title.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
	}

}