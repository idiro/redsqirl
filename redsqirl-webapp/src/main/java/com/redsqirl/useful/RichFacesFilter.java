package com.redsqirl.useful;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class RichFacesFilter implements  javax.servlet.Filter {

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		chain.doFilter(new HttpServletRequestWrapper((HttpServletRequest) request) {
			public String getRequestURI() {
				try {
					return URLDecoder.decode(super.getRequestURI(), "ISO-8859-1");
				} catch (UnsupportedEncodingException e) {
					throw new IllegalStateException("Cannot decode request URI.", e);
				}
			}
		}, response);
	}

	public void init(FilterConfig arg0) throws ServletException {

	}

	public void destroy() {

	}

}