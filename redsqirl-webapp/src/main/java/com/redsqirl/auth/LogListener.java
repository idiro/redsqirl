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