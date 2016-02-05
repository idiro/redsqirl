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

package com.redsqirl.useful;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletException;

public class Error {

	public String getStackTrace() {
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, Object> request = context.getExternalContext().getRequestMap();
		Throwable ex = (Throwable) request.get("javax.servlet.error.exception");
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		fillStackTrace(ex, pw);
		return sw.toString();
	}

	private static void fillStackTrace(Throwable t, PrintWriter w) {
		if (t == null) return;
		t.printStackTrace(w);
		if (t instanceof ServletException) {
			Throwable cause = ((ServletException) t).getRootCause();
			if (cause != null) {
				w.println("Root cause:");
				fillStackTrace(cause, w);
			}
		} else if (t instanceof SQLException) {
			Throwable cause = ((SQLException) t).getNextException();
			if (cause != null) {
				w.println("Next exception:");
				fillStackTrace(cause, w);
			}
		} else {
			Throwable cause = t.getCause();
			if (cause != null) {
				w.println("Cause:");
				fillStackTrace(cause, w);
			}
		}
	}

}