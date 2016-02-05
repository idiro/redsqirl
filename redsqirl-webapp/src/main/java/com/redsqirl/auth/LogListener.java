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

import java.util.Map;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class LogListener implements PhaseListener {

	@Override
	public void afterPhase(PhaseEvent event) {

		FacesContext facesContext = event.getFacesContext();
		HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

		String currentPage = facesContext.getViewRoot().getViewId();
		
		UICommand component = findInvokedCommandComponent(facesContext);

		if (component != null) {
			if(component.getActionExpression() != null && component.getActionExpression().getExpressionString() != null){
				String methodExpression = component.getActionExpression().getExpressionString();
				System.out.println(currentPage + "  " + methodExpression);
			}
		}

	}

	@Override
	public void beforePhase(PhaseEvent event) {

	}

	@Override
	public PhaseId getPhaseId() {
		return PhaseId.RESTORE_VIEW;
	}

	private UICommand findInvokedCommandComponent(FacesContext context) {
		UIViewRoot view = context.getViewRoot();
		Map<String, String> params = context.getExternalContext().getRequestParameterMap();
		for (String clientId : params.keySet()) {
			UIComponent component = view.findComponent(clientId);
			if (component instanceof UICommand) {
				return (UICommand) component;
			}
		}

		return null;
	}

}