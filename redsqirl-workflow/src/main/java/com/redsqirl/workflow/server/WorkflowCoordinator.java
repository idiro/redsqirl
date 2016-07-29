package com.redsqirl.workflow.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.idiro.utils.RandomString;
import com.redsqirl.workflow.server.interfaces.CoordinatorTimeConstraint;
import com.redsqirl.workflow.server.interfaces.DataFlowCoordinator;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;

public class WorkflowCoordinator extends UnicastRemoteObject implements DataFlowCoordinator{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2983015342985143960L;
	private static Logger logger = Logger.getLogger(WorkflowCoordinator.class);
	
	CoordinatorTimeConstraint timeCondition = new WfCoordTimeConstraint();
	String name;
	List<DataFlowElement> elements = new LinkedList<DataFlowElement>();
	Map<String,String> variables = new LinkedHashMap<String,String>();
	
	protected WorkflowCoordinator() throws RemoteException {
		super();
	}
	
	protected WorkflowCoordinator(String name) throws RemoteException {
		super();
		this.name = name;
	}

	@Override
	public List<DataFlowElement> getElements() throws RemoteException {
		return elements;
	}
	
	@Override
	public List<String> getComponentIds() throws RemoteException{
		List<String> ans = new LinkedList<String>();
		Iterator<DataFlowElement> it = elements.iterator();
		while(it.hasNext()){
			ans.add(it.next().getComponentId());
		}
		return ans;
	}
	
	/**
	 * Get the WorkflowAction corresponding to the componentId.
	 * 
	 * @param componentId
	 *            the componentId @see {@link DataflowAction#componentId}
	 * @return a WorkflowAction object or null
	 * @throws RemoteException
	 */
	public DataFlowElement getElement(String componentId)
			throws RemoteException {
		Iterator<DataFlowElement> it = elements.iterator();
		DataFlowElement ans = null;
		while (it.hasNext() && ans == null) {
			ans = it.next();
			if (!ans.getComponentId().equals(componentId)) {
				ans = null;
			}
		}
		if (ans == null) {
			logger.debug("Component " + componentId + " not found");
		}
		return ans;
	}

	@Override
	public String addElement(DataFlowElement dfe) throws RemoteException {
		//If error or not linked
		elements.add(dfe);
		return null;
	}
	
	@Override
	public void merge(DataFlowCoordinator coord) throws RemoteException{
		elements.addAll(coord.getElements());
		variables.putAll(coord.getVariables());
	}
	
	@Override
	public DataFlowCoordinator split(List<DataFlowElement> dfe) throws RemoteException {
		DataFlowCoordinator dfC = new WorkflowCoordinator(RandomString.getRandomName(8));
		dfC.getVariables().putAll(variables);
		dfC.getElements().addAll(dfe);
		Iterator<DataFlowElement> it = elements.iterator();
		while(it.hasNext()){
			if(elements.contains(it.next())){
				it.remove();
			}
		}
		return dfC;
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
