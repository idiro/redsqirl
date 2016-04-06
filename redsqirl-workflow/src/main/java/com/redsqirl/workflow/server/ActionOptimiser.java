package com.redsqirl.workflow.server;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.interfaces.DFEOptimiser;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
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
	
	protected ActionOptimiser(OozieAction oozieAction) throws RemoteException {
		super();
		this.oozieAction = oozieAction;
	}

	@Override
	public void resetElementList() {
		elementList.clear();
	}

	@Override
	public boolean addElement(DataFlowElement dfe) throws RemoteException{
		boolean ans = isSupported(dfe);
		if(ans){
			elementList.add(dfe);
		}
		return ans;
	}
	
	public boolean addAllElement(List<DataFlowElement> list) throws RemoteException{
		boolean ans = true;
		for(DataFlowElement dfe : list){
			ans &= isSupported(dfe);
		}
		if(ans){
			elementList.addAll(list);
		}
		return ans;
	}
	
	public abstract boolean isSupported(DataFlowElement dfe) throws RemoteException;

	@Override
	public void writeProcess(Document oozieXmlDoc, Element action, File localDirectoryToWrite, String pathFromOozieDir,
			String fileNameWithoutExtension) throws RemoteException {
		String[] extensions = oozieAction.getFileExtensions();
		String[] fileNames = new String[extensions.length];

		logger.debug("writeProcess extensionslength " + extensions.length);

		File[] files = new File[extensions.length];
		for (int i = 0; i < extensions.length; ++i) {
			fileNames[i] = pathFromOozieDir + "/" + fileNameWithoutExtension + extensions[i];
			files[i] = new File(localDirectoryToWrite, fileNameWithoutExtension	+ extensions[i]);
			
			logger.debug("writeProcess fileNames  " + fileNames[i].toString());
			logger.debug("writeProcess files  " + files[i].toString());
		}

		logger.debug("writeProcess 1");

		oozieAction.createOozieElement(oozieXmlDoc, action, fileNames);

		logger.debug("writeProcess 2");

		writeOozieActionFiles(files,elementList);

		logger.debug("writeProcess 3");
		for(DataFlowElement el:elementList){
			el.setLastTimeRun(System.currentTimeMillis());
		}
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
		String oozieActionId = getOozieActionId();
		while(it.hasNext()){
			DataFlowElement cur = it.next();
			cur.resetCache();
			cur.setOozieActionId(oozieActionId);
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
	public String getOozieActionId() throws RemoteException {
		return getComponentId();
	}

	@Override
	public void setOozieActionId(String oozieActionId) throws RemoteException {
	}

}