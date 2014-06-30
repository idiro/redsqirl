package com.redsqirl.workflow.server;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.server.interfaces.OozieXmlCreator;

/**
 * Creates an oozie xml work flow generator.
 * @author etienne
 *
 */
public abstract class OozieXmlCreatorAbs
extends UnicastRemoteObject 
implements OozieXmlCreator{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 4137335038159872232L;

	/**
	 * Default Constructor
	 * @throws RemoteException
	 */
	protected OozieXmlCreatorAbs() throws RemoteException {
		super();
	}

	/**
	 * Create a node that signifies that a node is ok
	 * @param doc
	 * @param parent
	 * @param toNode
	 */
	protected void createOKNode(Document doc, Element parent, String toNode){
		Element ok = doc.createElement("ok");
		Attr attrTo = doc.createAttribute("to");
		attrTo.setValue(toNode);
		ok.setAttributeNode(attrTo);
		parent.appendChild(ok);
	}
	/**
	 * Create an error node in the DAG
	 * @param doc
	 * @param parent
	 * @param errorNodeName
	 */
	protected void createErrorNode(Document doc, Element parent,String errorNodeName){
		Element errorEl = doc.createElement("error");
		Attr attrErrorTo = doc.createAttribute("to");
		attrErrorTo.setValue(errorNodeName);
		errorEl.setAttributeNode(attrErrorTo);
		parent.appendChild(errorEl);
	}
	
	/**
	 * Create a join node from elements in the DAG
	 * @param doc document to write fork
	 * @param parent node in xml 
	 * @param nameNode node to join from
	 * @param to name of node to join at
	 */
	protected void createJoinNode(Document doc, Element parent, String nameNode,String to){
		Element join = doc.createElement("join");
		Attr attrJoinName = doc.createAttribute("name");
		attrJoinName.setValue(nameNode);
		join.setAttributeNode(attrJoinName);

		Attr attrTo = doc.createAttribute("to");
		attrTo.setValue(to);
		join.setAttributeNode(attrTo);

		parent.appendChild(join);
	}
	/**
	 * Create a fork node too elements in the DAG
	 * @param doc document to write fork
	 * @param parent node in xml 
	 * @param nameNode node to fork at
	 * @param to set of nodes to fork to
	 */
	protected void createForkNode(Document doc, Element parent, String nameNode,Set<String> to){
		Element fork = doc.createElement("fork");
		Attr attrForkName = doc.createAttribute("name");
		attrForkName.setValue(nameNode);
		fork.setAttributeNode(attrForkName);

		Iterator<String> it = to.iterator();
		while(it.hasNext()){
			Element path = doc.createElement("path");
			Attr attrStart = doc.createAttribute("start");
			attrStart.setValue(it.next());
			path.setAttributeNode(attrStart);
			fork.appendChild(path);
		}
		parent.appendChild(fork);
	}
	/**
	 * Get a list of elements a that are roots 
	 * @param list
	 * @return List of elements that are roots in the DAG
	 * @throws RemoteException
	 */
	protected List<DataFlowElement> getRootElements(
			List<DataFlowElement> list) throws RemoteException{
		List<DataFlowElement> lDf = new LinkedList<DataFlowElement>();
		Iterator<DataFlowElement> itDFE = list.iterator();
		while(itDFE.hasNext()){
			DataFlowElement dfe = itDFE.next();
			Iterator<DataFlowElement> inIt = dfe.getAllInputComponent().iterator();
			boolean noDep = true;
			if(dfe.getOozieAction() == null){
				noDep = false;
			}
			while(inIt.hasNext() && noDep){
				DataFlowElement curIn = inIt.next();
				if(curIn.getOozieAction() != null){
					noDep = !list.contains(curIn);
				}
			}
			if(noDep){
				lDf.add(dfe);
			}
		}
		return lDf;
	}
	/**
	 * Get elements that are part of a leaf in the DAG
	 * @param list
	 * @return List of elements that are part of a Leaf
	 * @throws RemoteException
	 */
	protected List<DataFlowElement> getLeafElements(
			List<DataFlowElement> list) throws RemoteException{
		List<DataFlowElement> lDf = new LinkedList<DataFlowElement>();
		Iterator<DataFlowElement> itDFE = list.iterator();
		while(itDFE.hasNext()){
			DataFlowElement dfe = itDFE.next();
			if(dfe.getAllOutputComponent().isEmpty()){
				lDf.add(dfe);
			}
		}
		return lDf;
	}
	/**
	 * Get List of action names
	 * @param list of {@link com.redsqirl.workflow.server.interfaces.DataFlowElement} that
	 * @return list of actions names
	 * @throws RemoteException 
	 */
	@Override
	public List<String> getNameActions(List<DataFlowElement> list)
			throws RemoteException{
		List<String> lName = new LinkedList<String>();
		Iterator<DataFlowElement> itDFE = list.iterator();
		while(itDFE.hasNext()){
			lName.add(getNameAction(itDFE.next()));
		}
		return lName;
	}
	/**
	 * Get action name
	 * @param e element to get action name of
	 * @return action name
	 * @throws RemoteException
	 */
	@Override
	public String getNameAction(DataFlowElement e) throws RemoteException{
		return "act_"+e.getComponentId();
	}

}
