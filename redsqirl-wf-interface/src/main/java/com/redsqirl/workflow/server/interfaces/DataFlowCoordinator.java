package com.redsqirl.workflow.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DataFlowCoordinator extends Remote{

	List<DataFlowElement> getElements() throws RemoteException;
	
	List<String> getComponentIds() throws RemoteException;
	
	DataFlowElement getElement(String componentId) throws RemoteException;
	
	String addElement(DataFlowElement dfe) throws RemoteException;
	
	String removeElement(DataFlowElement dfe) throws RemoteException;
	
	String getName() throws RemoteException;
	
	void setName(String name)  throws RemoteException;
	
	CoordinatorTimeConstraint getTimeCondition() throws RemoteException;
	
	Map<String,String> getVariables() throws RemoteException;
	
	String addVariable(String name, String value, boolean force) throws RemoteException;

	void merge(DataFlowCoordinator coord) throws RemoteException;

	DataFlowCoordinator split(List<DataFlowElement> dfe) throws RemoteException;
}
