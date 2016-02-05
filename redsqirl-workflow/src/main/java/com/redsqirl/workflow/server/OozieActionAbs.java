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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.interfaces.OozieAction;

/**
 * Create an oozie action.
 * 
 * @author etienne
 *
 */
public abstract class OozieActionAbs  extends UnicastRemoteObject implements OozieAction{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4050182914018968247L;

	/**
	 * Default Conception
	 * @throws RemoteException
	 */
	protected OozieActionAbs() throws RemoteException {
		super();
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

		subAction.appendChild(configuration);
	}
	/**
	 * Get the varName surrounded byu "${}"
	 * @param varName
	 * @return altered varName
	 */
	public String getVar(String varName){
		return "${"+varName+"}";
	}
	
}
