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

package com.redsqirl.auth;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
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

import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.interfaces.DataFlow;

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
		Registry reg = null ;
		try {
			reg = LocateRegistry.createRegistry(2001);
			logger.error("Created registry");
		} catch (RemoteException e) {
			logger.info("Got registry");
		}
		
		executeUsageRecordLogJob();

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
	
	public void executeUsageRecordLogJob(){

		try {

			JobDetail job = JobBuilder.newJob(UsageRecordLogJob.class).withIdentity("usageRecordLogJob").build();

			Calendar startTime = Calendar.getInstance();
			startTime.set(java.util.Calendar.HOUR_OF_DAY, 1);
			startTime.set(java.util.Calendar.MINUTE, 00);
			startTime.set(java.util.Calendar.SECOND, 0);
			startTime.set(java.util.Calendar.MILLISECOND, 0);
			
			Trigger trigger = TriggerBuilder.newTrigger().startAt(startTime.getTime()).withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(24).repeatForever()).build();

			SchedulerFactory schFactory = new StdSchedulerFactory();
			Scheduler sch = schFactory.getScheduler();
			sch.start();
			sch.scheduleJob(job, trigger);

		} catch (SchedulerException e) {
			e.printStackTrace();
		}

	}

}