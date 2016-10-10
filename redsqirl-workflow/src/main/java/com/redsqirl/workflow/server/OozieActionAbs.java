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
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.interfaces.DataFlowCoordinatorVariables;
import com.redsqirl.workflow.server.interfaces.OozieAction;


public abstract class OozieActionAbs  extends UnicastRemoteObject implements OozieAction{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4050182914018968247L;
	private static Logger logger = Logger.getLogger(OozieActionAbs.class);
	protected Set<String> variables = new HashSet<String>();
	
	protected DataFlowCoordinatorVariables extraParameters = null;
	/**
	 * Default Conception
	 * @throws RemoteException
	 */
	protected OozieActionAbs() throws RemoteException {
		super();
		if(supportsExtraJobParameters()){
			extraParameters = new WfCoordVariables();
		}
	}
	
	@Override
	public Element createCredentials(
			Document oozieXmlDoc
			)throws RemoteException{
		return null;
	}
	
	/**
	 * Set Default parameter with job_Xml null
	 * @param oozieXmlDoc
	 * @param subAction
	 */
	public void defaultParam(Document oozieXmlDoc, Element subAction){
		defaultParam(oozieXmlDoc, subAction,null);
	}
	/**
	 * Set the defaultParam for a document with job_xml
	 * @param oozieXmlDoc
	 * @param subAction
	 * @param job_xml
	 */
	public void defaultParam(Document oozieXmlDoc, Element subAction,String job_xml){
		Element jobtracker = oozieXmlDoc.createElement("job-tracker");
		jobtracker.appendChild(oozieXmlDoc.createTextNode(
				getVar(OozieManager.prop_jobtracker)
				));
		subAction.appendChild(jobtracker);

		Element namenode = oozieXmlDoc.createElement("name-node");
		namenode.appendChild(oozieXmlDoc.createTextNode(
				getVar(OozieManager.prop_namenode)));
		subAction.appendChild(namenode);
		
		if(job_xml != null){
			Element jobXml = oozieXmlDoc.createElement("job-xml");
			jobXml.appendChild(oozieXmlDoc.createTextNode(
					job_xml));
			subAction.appendChild(jobXml);
		}
		Element configuration = oozieXmlDoc.createElement("configuration");
		{
			Element confName = oozieXmlDoc.createElement("name");
			confName.appendChild(oozieXmlDoc.createTextNode("mapred.job.queue.name"));
			Element confValue = oozieXmlDoc.createElement("value");
			confValue.appendChild(oozieXmlDoc.createTextNode(
					getVar(OozieManager.prop_action_queue)));

			Element property = oozieXmlDoc.createElement("property");
			property.appendChild(confName);
			property.appendChild(confValue);
			configuration.appendChild(property);
		}
		{
			Element confName = oozieXmlDoc.createElement("name");
			confName.appendChild(oozieXmlDoc.createTextNode("oozie.launcher.mapred.job.queue.name"));
			Element confValue = oozieXmlDoc.createElement("value");
			confValue.appendChild(oozieXmlDoc.createTextNode(
					getVar(OozieManager.prop_launcher_queue)));

			Element property = oozieXmlDoc.createElement("property");
			property.appendChild(confName);
			property.appendChild(confValue);
			configuration.appendChild(property);
		}
		
		if(extraParameters != null){
			try{
				Iterator<Entry<String, String>> itExtraParams = extraParameters.getKeyValues().entrySet().iterator();
				while(itExtraParams.hasNext()){
					Entry<String,String> cur = itExtraParams.next();
					{
						Element confName = oozieXmlDoc.createElement("name");
						confName.appendChild(oozieXmlDoc.createTextNode(cur.getKey()));
						Element confValue = oozieXmlDoc.createElement("value");
						confValue.appendChild(oozieXmlDoc.createTextNode(cur.getValue()));

						Element property = oozieXmlDoc.createElement("property");
						property.appendChild(confName);
						property.appendChild(confValue);
						configuration.appendChild(property);
					}
				}
			}catch(Exception e){
				logger.warn(e,e);
			}
		}

		subAction.appendChild(configuration);
	}
	
	public boolean supportsExtraJobParameters()  throws RemoteException{
		return true;
	}
	
	public DataFlowCoordinatorVariables getExtraJobParameters() throws RemoteException{
		return extraParameters;
	}
	
	/**
	 * Get the varName surrounded byu "${}"
	 * @param varName
	 * @return altered varName
	 */
	public String getVar(String varName){
		return "${"+varName+"}";
	}


	public boolean addVariable(String arg0) {
		return variables.add(arg0);
	}

	public boolean addAllVariables(Collection<? extends String> arg0) {
		return variables.addAll(arg0);
	}

	public void clearVariables() {
		variables.clear();
	}

	public boolean containsVariable(Object arg0) {
		return variables.contains(arg0);
	}

	public boolean removeVariable(Object arg0) {
		return variables.remove(arg0);
	}

	public final Set<String> getVariables() {
		return variables;
	}
}
