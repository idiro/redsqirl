package com.redsqirl.workflow.server.oozie;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.OozieActionAbs;
import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.OozieSubWorkflowAction;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;
import com.redsqirl.workflow.server.interfaces.SuperElement;

/**
 * Oozie Action for running a SubWorkflow.
 * @author etienne
 *
 */
public class SubWorkflowAction extends OozieActionAbs implements OozieSubWorkflowAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7433588815768871628L;
	
	private static Logger logger = Logger.getLogger(SubWorkflowAction.class);
	
	private SubDataFlow subWf;
	
	private String wfId;
	
	private SuperElement superElement;
	
	public SubWorkflowAction() throws RemoteException {
		super();
	}
	
	public SubWorkflowAction(SuperElement superElement) throws RemoteException {
		super();
		this.superElement = superElement;
	}

	@Override
	public void createOozieElement(Document oozieXmlDoc, Element action,
			String[] fileNames) throws RemoteException {
		superElement.updateOozieSubWorkflowAction();
		
		logger.debug("Create sub-workflow action: "+wfId);
		Element subWfElement = oozieXmlDoc.createElement("sub-workflow");
		
		Element pathElement = oozieXmlDoc.createElement("app-path");
		pathElement.appendChild(oozieXmlDoc.createTextNode("${"+OozieManager.prop_workflowpath+"}/"+wfId));
		subWfElement.appendChild(pathElement);
		
		Element propElement = oozieXmlDoc.createElement("propagate-configuration");
		subWfElement.appendChild(propElement);
		
		action.appendChild(subWfElement);
	}

	@Override
	public String[] getFileExtensions() throws RemoteException {
		return new String[0];
	}

	/**
	 * @return the subWf
	 */
	public DataFlow getSubWf() {
		return subWf;
	}

	/**
	 * @param subWf the subWf to set
	 */
	public void setSubWf(SubDataFlow subWf) {
		this.subWf = subWf;
	}

	/**
	 * @return the wfId
	 */
	public String getWfId() {
		return wfId;
	}

	/**
	 * @param wfId the wfId to set
	 */
	public void setWfId(String wfId) {
		this.wfId = wfId;
	}

	/**
	 * @return the superElement
	 */
	public SuperElement getSuperElement() {
		return superElement;
	}

	/**
	 * @param superElement the superElement to set
	 */
	public void setSuperElement(SuperElement superElement) {
		this.superElement = superElement;
	}

}
