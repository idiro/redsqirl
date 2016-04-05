package com.redsqirl.workflow.server.interfaces;

import java.rmi.RemoteException;
import java.util.List;

public interface DFEOptimiser extends RunnableElement {

	/**
	 * Reset the list of element to optimise
	 */
	public void resetElementList() throws RemoteException;
	
	/**
	 * True if the element can be added and is added false otherwise
	 * @param dfe
	 * @return
	 */
	public boolean addElement(DataFlowElement dfe) throws RemoteException;
	
	/**
	 * True if all the element can be added and are added false otherwise
	 * @param dfe
	 * @return
	 */
	public boolean addAllElement(List<DataFlowElement> dfe) throws RemoteException;
	
	public List<DataFlowElement> getElements() throws RemoteException;
	
	public DataFlowElement getFirst() throws RemoteException;
	
	public DataFlowElement getLast() throws RemoteException;
}
