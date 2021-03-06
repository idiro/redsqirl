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

package com.redsqirl.workflow.server.oozie;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.OozieUniqueActionAbs;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.OozieSubWorkflowAction;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;
import com.redsqirl.workflow.server.interfaces.SuperElement;

/**
 * Oozie Action for running a SubWorkflow.
 * @author etienne
 *
 */
public class SubWorkflowAction extends OozieUniqueActionAbs implements OozieSubWorkflowAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7433588815768871628L;
	
	private static Logger logger = Logger.getLogger(SubWorkflowAction.class);
	
	private SubDataFlow subWf;
	
	private String wfId;
	
	private SuperElement superElement;
	
	private Map<String,String> superActionVariables;
	
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
		
		if(superActionVariables != null && !superActionVariables.isEmpty()){
			Element configuration = oozieXmlDoc.createElement("configuration");
			Iterator<Entry<String,String>> itVariables = superActionVariables.entrySet().iterator();
			while(itVariables.hasNext()){
				Entry<String,String> var = itVariables.next();
				Element confName = oozieXmlDoc.createElement("name");
				confName.appendChild(oozieXmlDoc.createTextNode(var.getKey()));
				Element confValue = oozieXmlDoc.createElement("value");
				confValue.appendChild(oozieXmlDoc.createTextNode(var.getValue()));

				Element property = oozieXmlDoc.createElement("property");
				property.appendChild(confName);
				property.appendChild(confValue);

				configuration.appendChild(property);
			}
			subWfElement.appendChild(configuration);
		}
		
		action.appendChild(subWfElement);
	}
	
	public boolean supportsExtraJobParameters()  throws RemoteException{
		return false;
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

	public final Map<String, String> getSuperActionVariables() {
		return superActionVariables;
	}

	public final void setSuperActionVariables(Map<String, String> superActionVariables) {
		this.superActionVariables = superActionVariables;
	}

}
