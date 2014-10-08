package com.redsqirl.workflow.server.interfaces;

import java.rmi.RemoteException;

/**
 * Class used to set a sub-workflow action element.
 * The sub-workflow will be written in anothe xml file
 * along the main xml file (workflow.xml).
 * 
 * @author etienne
 *
 */
public interface OozieSubWorkflowAction extends OozieAction{

	/**
	 * @return the subWf
	 */
	public DataFlow getSubWf() throws RemoteException;

	/**
	 * @param subWf the subWf to set
	 */
	public void setSubWf(SubDataFlow subWf) throws RemoteException;

	/**
	 * @return the wfId
	 */
	public String getWfId() throws RemoteException;

	/**
	 * @param wfId the wfId to set
	 */
	public void setWfId(String wfId) throws RemoteException;
	

	/**
	 * @return the superElement
	 */
	public SuperElement getSuperElement() throws RemoteException;

	/**
	 * @param superElement the superElement to set
	 */
	public void setSuperElement(SuperElement superElement) throws RemoteException;
}
