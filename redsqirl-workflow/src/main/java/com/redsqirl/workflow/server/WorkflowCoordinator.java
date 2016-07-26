package com.redsqirl.workflow.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.redsqirl.workflow.server.interfaces.CoordinatorTimeConstraint;
import com.redsqirl.workflow.server.interfaces.DataFlowCoordinator;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;

public class WorkflowCoordinator extends UnicastRemoteObject implements DataFlowCoordinator{

	CoordinatorTimeConstraint timeCondition = new WfCoordTimeConstraint();
	String name;
	List<DataFlowElement> elements = new LinkedList<DataFlowElement>();
	Date startTime;
	Date endTime;
	Map<String,String> variables = new LinkedHashMap<String,String>();
	
	protected WorkflowCoordinator() throws RemoteException {
		super();
	}

	@Override
	public List<DataFlowElement> getElements() throws RemoteException {
		return elements;
	}

	@Override
	public String addElement(DataFlowElement dfe) throws RemoteException {
		//If error or not linked
		elements.add(dfe);
		return null;
	}

	@Override
	public String removeElement(DataFlowElement dfe) throws RemoteException {
		elements.remove(dfe);
		return null;
	}

	@Override
	public String getName() throws RemoteException {
		return name;
	}

	@Override
	public void setName(String name) throws RemoteException {
		this.name = name;
	}

	@Override
	public CoordinatorTimeConstraint getTimeCondition() throws RemoteException {
		return timeCondition;
	}

	@Override
	public Date getStartTime() throws RemoteException {
		return startTime;
	}

	@Override
	public void setStartTime(Date startTime) throws RemoteException {
		this.startTime = startTime;
	}

	@Override
	public Date getEndTime() throws RemoteException {
		return endTime;
	}

	@Override
	public void setEndTime(Date endTime) throws RemoteException {
		this.endTime = endTime;
	}

	@Override
	public Map<String, String> getVariables() throws RemoteException {
		return variables;
	}

	@Override
	public String addVariable(String name, String value, boolean force)
			throws RemoteException {
		String error = null;
		if(error == null){
			variables.put(name,value);
		}
		return error;
	}

}
