/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl.workflow.server;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.interfaces.DFEOptimiser;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DataFlowCoordinatorVariables;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.server.interfaces.OozieAction;

public abstract class ActionOptimiser extends UnicastRemoteObject implements
DFEOptimiser {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9142600544963533223L;
	private static final Logger logger = Logger.getLogger(ActionOptimiser.class);
	protected List<DataFlowElement> elementList = new LinkedList<DataFlowElement>();
	protected OozieAction oozieAction;
	private Set<String> lastRunOozieElementNames = new LinkedHashSet<String>();;
	
	protected ActionOptimiser(OozieAction oozieAction) throws RemoteException {
		super();
		this.oozieAction = oozieAction;
	}

	@Override
	public void resetElementList() throws RemoteException{
		elementList.clear();
	}

	@Override
	public boolean addElement(DataFlowElement dfe) throws RemoteException{
		boolean ans = isSupported(dfe);
		if(ans){
			elementList.add(dfe);
			if(dfe.getOozieAction() != null){
				oozieAction.addAllVariables(dfe.getOozieAction().getVariables());
				if(oozieAction.supportsExtraJobParameters() && dfe.getOozieAction().supportsExtraJobParameters()){
					DataFlowCoordinatorVariables extraConfs = oozieAction.getExtraJobParameters();
					extraConfs.addVariables(dfe.getOozieAction().getExtraJobParameters().getKeyValues());
				}
			}
		}
		return ans;
	}
	
	public boolean addAllElement(List<DataFlowElement> list) throws RemoteException{
		boolean ans = true;
		for(DataFlowElement dfe : list){
			ans &= isSupported(dfe);
		}
		if(ans){
			for(DataFlowElement dfe : list){
				if(dfe.getOozieAction() != null){
					oozieAction.addAllVariables(dfe.getOozieAction().getVariables());
				}
			}
			elementList.addAll(list);
		}
		return ans;
	}
	
	public abstract boolean isSupported(DataFlowElement dfe) throws RemoteException;

	@Override
	public Map<String,Element> writeProcess(Document oozieXmlDoc,
			File localDirectoryToWrite, String pathFromOozieDir) throws RemoteException {
		String actionName = getComponentId();
		String[] extensions = oozieAction.getFileExtensions();
		String[] fileNames = new String[extensions.length];

		logger.debug("writeProcess extensionslength " + extensions.length);

		File[] files = new File[extensions.length];
		for (int i = 0; i < extensions.length; ++i) {
			fileNames[i] = pathFromOozieDir + "/" + actionName + extensions[i];
			files[i] = new File(localDirectoryToWrite, actionName	+ extensions[i]);
			
			logger.debug("writeProcess fileNames  " + fileNames[i].toString());
			logger.debug("writeProcess files  " + files[i].toString());
		}

		logger.debug("writeProcess 1");

		Map<String,Element>  ans = oozieAction.createOozieElements(oozieXmlDoc, actionName, fileNames);
		
		Set<String> lastRun = new LinkedHashSet<String>();
		lastRun.addAll(ans.keySet());
		
		setLastRunOozieElementNames(lastRun);
		logger.debug("writeProcess 2");

		writeOozieActionFiles(files,elementList);

		logger.debug("writeProcess 3");
		for(DataFlowElement el:elementList){
			el.setLastTimeRun(System.currentTimeMillis());
		}
		
		return ans;
	}


	/**
	 * Write into local files what needs to be parse within the oozie action
	 * 
	 * @param files
	 * @return <code>true</code> if the actions where written else
	 *         <code>false</code>
	 * @throws RemoteException
	 */

	public abstract boolean writeOozieActionFiles(File[] files,List<DataFlowElement> elementList)
			throws RemoteException;
	

	public DataFlowElement getFirst(){
		return elementList == null || elementList.size() == 0? null: elementList.get(0);
	}
	
	public DataFlowElement getLast(){
		return elementList == null || elementList.size() == 0? null: elementList.get(elementList.size()-1);
	}

	@Override
	public List<DataFlowElement> getElements() {
		return elementList;
	}

	@Override
	public String getComponentId() throws RemoteException {
		if(elementList == null || elementList.size() == 0){
			return null;
		}
		String first = elementList.get(0).getComponentId();
		if(elementList.size()==1){
			return first;
		}
		String last = elementList.get(elementList.size()-1).getComponentId();
		return first+"_to_"+last;
	}

	@Override
	public OozieAction getOozieAction() throws RemoteException {
		return oozieAction;
	}

	@Override
	public void resetCache() throws RemoteException {
		Iterator<DataFlowElement> it = elementList.iterator();
		while(it.hasNext()){
			DataFlowElement cur = it.next();
			cur.resetCache();
			cur.setLastRunOozieElementNames(lastRunOozieElementNames);
		}
	}

	@Override
	public Map<String, List<DataFlowElement>> getInputComponent() throws RemoteException {
		return getFirst().getInputComponent();
	}

	@Override
	public Map<String, List<DataFlowElement>> getOutputComponent() throws RemoteException {
		return getLast().getOutputComponent();
	}

	@Override
	public List<DataFlowElement> getAllInputComponent() throws RemoteException {
		return getFirst().getAllInputComponent();
	}

	@Override
	public List<DataFlowElement> getAllOutputComponent() throws RemoteException {
		return getLast().getAllOutputComponent();
	}

	@Override
	public Map<String, DFEOutput> getDFEOutput() throws RemoteException {
		return getLast().getDFEOutput();
	}

	@Override
	public Map<String, List<DFEOutput>> getDFEInput() throws RemoteException {
		return getFirst().getDFEInput();
	}

	@Override
	public Set<String> getLastRunOozieElementNames() throws RemoteException{
		return lastRunOozieElementNames;
	}
	
	@Override
	public void setLastRunOozieElementNames(Set<String> lastRunOozieElementNames) throws RemoteException{
		this.lastRunOozieElementNames = lastRunOozieElementNames;
		if(elementList != null){
			Iterator<DataFlowElement> it = elementList.iterator();
			while(it.hasNext()){
				DataFlowElement cur = it.next();
				cur.setLastRunOozieElementNames(lastRunOozieElementNames);
			}
		}
	}
}
