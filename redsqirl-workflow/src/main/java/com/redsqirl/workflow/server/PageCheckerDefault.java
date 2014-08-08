package com.redsqirl.workflow.server;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEPage;
import com.redsqirl.workflow.server.interfaces.PageChecker;

/**
 * Default page checker.
 * @author etienne
 *
 */
public class PageCheckerDefault
extends UnicastRemoteObject
implements PageChecker{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1531242759979525830L;
	
	protected static Logger logger = Logger.getLogger(PageCheckerDefault.class);
	
	public PageCheckerDefault() throws RemoteException{
		super();
	}
	
	/**
	 * Check a page
	 * @param page to check
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String check(DFEPage page) throws RemoteException {
		logger.debug("Check page "+page.getTitle());
		String error = "";
		Iterator<DFEInteraction> it = page.getInteractions().iterator();
		while(it.hasNext()){
			DFEInteraction eInt = it.next();
			logger.debug("check interaction "+eInt.getId());
			String loc_error = eInt.check();
			if(loc_error != null){
				error += eInt.getName()+": "+loc_error +"\n";
			}
		}
		if(error.isEmpty()){
			error = null;
		}
		return error;
	}
	
	

}
