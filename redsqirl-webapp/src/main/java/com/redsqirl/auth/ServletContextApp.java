package com.redsqirl.auth;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/** ServletContextApp
 * 
 * Class to start application context.
 * adds a list of the HttpSession session for login control
 * 
 * @author Igor.Souza
 */
public class ServletContextApp implements ServletContextListener{

	private static Logger logger = Logger.getLogger(ServletContextApp.class);	
	
	Map<String, HttpSession> sessionLoginMap = new HashMap<String, HttpSession>();
	String userName;

	/** contextInitialized
	 * 
	 * method to create application context
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void contextInitialized(ServletContextEvent contextEvent) {
		
		ServletContext context = contextEvent.getServletContext();
		context.setAttribute("sessionLoginMap", sessionLoginMap);

		logger.info("Context Created");

	}

	/** contextDestroyed
	 * 
	 * method to destroy application context
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void contextDestroyed(ServletContextEvent contextEvent) {

		logger.info("Context Destroyed");

	}

}