package com.redsqirl.analyticsStore;
 
import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Filter checks if LoginBean has loginIn property set to true.
 * If it is not set then request is being redirected to the login.xhml page.
 *
 * @author itcuties
 *
 */
public class AnalyticsStoreLoginFilter implements Filter {
 
    /**
     * Checks if user is logged in. If not it redirects to the login.xhtml page.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Get the loginBean from session attribute
    	AnalyticsStoreLoginBean loginBean = (AnalyticsStoreLoginBean)((HttpServletRequest)request).getSession().getAttribute("analyticsStorelLoginBean");
         
        // For the first application request there is no loginBean in the session so user needs to log n
        // For other requests loginBean is present but we need to check if user has logged in successfully
        if (loginBean == null || !loginBean.isLoggedIn()) {
        	
        	
        	String requestURI = ((HttpServletRequest)request).getRequestURI();
        	String queryString = ((HttpServletRequest)request).getQueryString();
        	String encodedURL = URLEncoder.encode(requestURI, "UTF-8");
        	String encodedQuery = "";
        	if (queryString != null){
        		encodedQuery = URLEncoder.encode(queryString, "UTF-8");
        	}
        	
            String contextPath = ((HttpServletRequest)request).getContextPath();
            ((HttpServletResponse)response).sendRedirect(contextPath + 
            		"/login.xhtml?originalURL=" + encodedURL+"&originalQuery=" + encodedQuery);
        }
         
        chain.doFilter(request, response);
             
    }
 
    public void init(FilterConfig config) throws ServletException {
        // Nothing to do here!
    }
 
    public void destroy() {
        // Nothing to do here!
    }  
     
}
