package com.redsqirl.workflow.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DataFlowCoordinator extends Remote{

	List<DataFlowElement> getElements() throws RemoteException;
	
	String addElement(DataFlowElement dfe) throws RemoteException;
	
	String removeElement(DataFlowElement dfe) throws RemoteException;
	
	String getName() throws RemoteException;
	
	void setName(String name)  throws RemoteException;
	
	CoordinatorTimeConstraint getTimeCondition() throws RemoteException;
	
	Date getStartTime() throws RemoteException;
	
	void setStartTime(Date startTime) throws RemoteException;
	
	Date getEndTime() throws RemoteException;
	
	void setEndTime(Date endTime) throws RemoteException;
	
	Map<String,String> getVariables() throws RemoteException;
	
	String addVariable(String name, String value, boolean force) throws RemoteException;
}
