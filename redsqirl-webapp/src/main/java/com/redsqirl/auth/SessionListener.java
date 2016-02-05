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

import java.rmi.registry.Registry;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

public class SessionListener implements HttpSessionListener{

	private static Logger logger = Logger.getLogger(SessionListener.class);

	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent arg0) {
		logger.info("before session destroy");
		HttpSession session = arg0.getSession();
		ServletContext sc = session.getServletContext();
		Map<String, HttpSession> sessionLoginMap = (Map<String, HttpSession>) sc.getAttribute("sessionLoginMap");
		String userName = (String) session.getAttribute("username");

		Map<String, UsageRecordWriter> sessionUsageRecordWriter = (Map<String, UsageRecordWriter>) sc.getAttribute("usageRecordLog");
		usageRecordLog(userName, sessionUsageRecordWriter).addSuccess("SESSIONTIMEOUT");
		
		try{
			ServerProcess th = (ServerProcess) session.getAttribute("serverThread");
			if (th != null){
				logger.info("kill serverThread");
				th.kill(session,userName);
			}
		}catch(Exception e){
			logger.warn("Fail to kill server process thread");
		}

		//Disconnect ssh connection
		try{
			FacesContext context = FacesContext.getCurrentInstance();
			UserInfoBean uib = (UserInfoBean) context.getApplication()
					.evaluateExpressionGet(context, "#{userInfoBean}",
							UserInfoBean.class);
			uib.sshDisconnect();
		}catch(Exception e){
			logger.warn("Fail to disconnect from the ssh session");
		}


		try {
			Registry registry = (Registry) sc.getAttribute("registry");
			for (String name : registry.list()){
				if (name.startsWith(userName+"@")){
					registry.unbind(name);
				}
			}
		}catch (Exception e) {
			logger.warn("Fail to remove object from registry");
		}

		if(sessionLoginMap != null){
			sessionLoginMap.remove(userName);
		}else{
			logger.warn("No "+userName+" to remove from session login map.");
		}

	}

	public UsageRecordWriter usageRecordLog(String userName, Map<String, UsageRecordWriter> sessionUsageRecordWriter) {

		if(sessionUsageRecordWriter != null){
			UsageRecordWriter usageRecordLog = sessionUsageRecordWriter.get(userName);
			if(usageRecordLog != null){
				return usageRecordLog;
			}else{
				return new UsageRecordWriter();
			}
		}else{
			return new UsageRecordWriter();
		}

	}

}