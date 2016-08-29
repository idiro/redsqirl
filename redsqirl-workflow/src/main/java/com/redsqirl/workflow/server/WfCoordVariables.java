package com.redsqirl.workflow.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.DataFlowCoordinatorVariable;
import com.redsqirl.workflow.server.interfaces.DataFlowCoordinatorVariables;

public class WfCoordVariables extends UnicastRemoteObject implements DataFlowCoordinatorVariables {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4444112770960681337L;
	private static Logger logger = Logger.getLogger(WfCoordVariables.class);
	
	Map<String,DataFlowCoordinatorVariable> variableList = new LinkedHashMap<String,DataFlowCoordinatorVariable>();

	protected WfCoordVariables() throws RemoteException {
		super();
	}

	@Override
	public Collection<DataFlowCoordinatorVariable> getVariables() throws RemoteException {
		return variableList.values();
	}

	@Override
	public Map<String, String> getKeyValues() throws RemoteException {
		Map<String,String> ans = new HashMap<String,String>(variableList.size());
		Iterator<DataFlowCoordinatorVariable> it = getVariables().iterator();
		while(it.hasNext()){
			DataFlowCoordinatorVariable cur = it.next();
			ans.put(cur.getKey(), cur.getValue());
		}
		return ans;
	}

	@Override
	public String addVariable(String name, String value, boolean force) throws RemoteException {
		return addVariable(name,value,"",force);
	}

	@Override
	public String addVariable(String name, String value, String description, boolean force) throws RemoteException {
		String error = null;
		if(error == null){
			if(logger.isDebugEnabled()){
				logger.debug("Add variable "+name+"="+value);
			}
			variableList.put(name,new WfCoordVariable(name,value,description));
		}
		
		return error;
	}

	@Override
	public void addVariables(Map<String, String> variables) throws RemoteException {
		Iterator<Entry<String,String>> it = variables.entrySet().iterator();
		while(it.hasNext()){
			Entry<String,String> cur = it.next();
			variableList.put(cur.getKey(), new WfCoordVariable(cur.getKey(), cur.getValue()));
		}
	}
	
	public String saveInXml(Document doc, Element rootElement) throws RemoteException{
		String error = null;
		Iterator<DataFlowCoordinatorVariable> itVar = getVariables().iterator();
		while(itVar.hasNext()){
			DataFlowCoordinatorVariable curVar = itVar.next();
			Element elProp = doc.createElement("property");
			
			Element elName = doc.createElement("name");
			elName.appendChild(doc.createTextNode(curVar.getKey()));
			elProp.appendChild(elName);

			Element elValue = doc.createElement("value");
			elValue.appendChild(doc.createTextNode(curVar.getValue()));
			elProp.appendChild(elValue);
			
			Element elDescription = doc.createElement("description");
			elDescription.appendChild(doc.createTextNode(curVar.getDescription()));
			elProp.appendChild(elDescription);
			
			rootElement.appendChild(elProp);
		}
		
		return error;
	}
	
	public void readInXml(Document doc, Element parent) throws Exception{
		NodeList props = parent.getElementsByTagName("property");
		for (int temp = 0; temp < props.getLength(); ++temp) {
			Node compCur = props.item(temp);
			String key = ((Element) compCur).getElementsByTagName("name").item(0)
					.getChildNodes().item(0).getNodeValue();
			String value = ((Element) compCur).getElementsByTagName("value").item(0)
					.getChildNodes().item(0).getNodeValue();
			String description = "";
			try{
				description = ((Element) compCur).getElementsByTagName("description").item(0)
					.getChildNodes().item(0).getNodeValue();
			}catch(Exception e){}
			addVariable(key, value, description,true);
		}
	}

	@Override
	public boolean addAll(DataFlowCoordinatorVariables obj) throws RemoteException {
		boolean ans = false;
		Iterator<DataFlowCoordinatorVariable> itVar = obj.getVariables().iterator();
		while(itVar.hasNext()){
			DataFlowCoordinatorVariable cur = itVar.next();
			ans = true;
			variableList.put(cur.getKey(), cur);
		}
		return ans;
	}

	@Override
	public Set<String> getKeys() throws RemoteException {
		return variableList.keySet();
	}

	@Override
	public String addVariable(DataFlowCoordinatorVariable var) throws RemoteException {
		variableList.put(var.getKey(), var);
		return null;
	}

	@Override
	public DataFlowCoordinatorVariable getVariable(String name) throws RemoteException {
		return variableList.get(name);
	}

	@Override
	public void removeVariable(String key) throws RemoteException {
		variableList.remove(key);
	}

	@Override
	public void removeAllVariables() throws RemoteException {
		variableList.clear();
	}

}