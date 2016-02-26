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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.OozieActionAbs;
import com.redsqirl.workflow.server.WorkflowPrefManager;

/**
 * Write a hive action into an oozie xml.
 *
 * @author etienne
 *
 */
public class HiveAction extends OozieActionAbs{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5119314850496590566L;


	/** Default Hive XML */
	public static final String	hive_default_xml = "core.jdbc.hive.hive_default_xml",
	/** Hive XML */
	hive_xml = "core.jdbc.hive.hive_xml";
	
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public HiveAction() throws RemoteException {
		super();
	}
	/**
	 * Create Oozie element for Hive actions
	 * @param oozieXmlDoc
	 * @param action
	 * @param fileNames
	 * @throws RemoteException
	 */
	@Override
	public void createOozieElement(Document oozieXmlDoc, Element action,
			String[] fileNames) throws RemoteException {

		Element hive = oozieXmlDoc.createElement("hive");
		Attr attrXmlns = oozieXmlDoc.createAttribute("xmlns");
		attrXmlns.setValue("uri:oozie:hive-action:0.2");
		hive.setAttributeNode(attrXmlns);
		
		if(WorkflowPrefManager.getProperty(
				hive_xml) != null){
			defaultParam(
					oozieXmlDoc, 
					hive,
					WorkflowPrefManager.getProperty(
							hive_xml));
		}else{
			defaultParam(
					oozieXmlDoc, 
					hive);
		}
		
		if(WorkflowPrefManager.getProperty(
				hive_default_xml) != null){
			Element confName = oozieXmlDoc.createElement("name");
			confName.appendChild(oozieXmlDoc.createTextNode("oozie.hive.defaults"));
			Element confValue = oozieXmlDoc.createElement("value");
			confValue.appendChild(oozieXmlDoc.createTextNode(
					WorkflowPrefManager.getProperty(
							hive_default_xml)));

			Element property = oozieXmlDoc.createElement("property");
			property.appendChild(confName);
			property.appendChild(confValue);

			Element configuration = (Element) hive.getElementsByTagName("configuration").item(0);
			configuration.appendChild(property);
		}
		
		Element script = oozieXmlDoc.createElement("script");
		script.appendChild(oozieXmlDoc.createTextNode(fileNames[0]));
		hive.appendChild(script);
		
		action.appendChild(hive);

	}
	/**
	 * Get the file name extensions for Hive actions 
	 * @return extension
	 */
	@Override
	public String[] getFileExtensions() {
		return new String[]{".sql"};
	}

}
