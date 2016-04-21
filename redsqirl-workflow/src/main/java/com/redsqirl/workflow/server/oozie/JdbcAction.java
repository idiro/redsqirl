package com.redsqirl.workflow.server.oozie;

import java.rmi.RemoteException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.OozieUniqueActionAbs;
import com.redsqirl.workflow.server.interfaces.OozieAction;
import com.redsqirl.workflow.server.oozie.HiveAction;

public class JdbcAction extends OozieUniqueActionAbs {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7581034254093689611L;
	protected boolean hiveAction;
	protected OozieAction action;
	
	public JdbcAction() throws RemoteException {
		super();
		action = new JdbcShellAction();
		hiveAction = false;
	}

	@Override
	public void createOozieElement(Document oozieXmlDoc, Element action, String[] fileNames) throws RemoteException {
		this.action.createOozieElement(oozieXmlDoc, action, fileNames);
	}

	@Override
	public String[] getFileExtensions() throws RemoteException {
		return action.getFileExtensions();
	}

	public boolean isHiveAction() {
		return hiveAction;
	}

	public void setHiveAction(boolean hiveAction) throws RemoteException {
		if(this.hiveAction != hiveAction){
			if(hiveAction){
				action = new HiveAction();
			}else{
				action = new JdbcAction();
			}
			this.hiveAction = hiveAction;
		}
	}

	public OozieAction getAction() {
		return action;
	}

}
