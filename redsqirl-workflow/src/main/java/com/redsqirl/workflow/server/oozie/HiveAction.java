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

import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.idiro.hadoop.NameNodeVar;
import com.redsqirl.workflow.server.OozieUniqueActionAbs;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.jdbc.HivePropertiesDetails;

/**
 * Write a hive action into an oozie xml.
 *
 * @author etienne
 *
 */
public class HiveAction extends OozieUniqueActionAbs{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5119314850496590566L;

	private static Logger logger = Logger.getLogger(HiveAction.class);

	/** Default Hive XML */
	public static final String	
	//hive_default_xml = "core.jdbc.hive.hive_default_xml",
	/** Hive XML */
	hive_xml = WorkflowPrefManager.core_settings_hcatalog+".hive_xml";
	
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public HiveAction() throws RemoteException {
		super();
		if(WorkflowPrefManager.isSecEnable()){
			setCredential("hive2-cred");
		}
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

		Element hive = oozieXmlDoc.createElement("hive2");
		Attr attrXmlns = oozieXmlDoc.createAttribute("xmlns");
		attrXmlns.setValue("uri:oozie:hive2-action:0.1");
		hive.setAttributeNode(attrXmlns);
		
		String hive_hdfs_xml = WorkflowPrefManager.getProperty(
				hive_xml);
		boolean hiveXmlExists;
		try{
			org.apache.hadoop.fs.FileSystem fs = NameNodeVar.getFS();
			hiveXmlExists = fs.exists(new Path(hive_hdfs_xml));
		}catch(Exception e){
			hiveXmlExists = false;
		}
		
		if(hiveXmlExists){
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

		HivePropertiesDetails hpd = new HivePropertiesDetails("hive");
		Element urlEl = oozieXmlDoc.createElement("jdbc-url");
		String urlStr = hpd.getDburl();
		if(WorkflowPrefManager.isSecEnable() && urlStr.contains(";")){
			String[] options = urlStr.substring(urlStr.indexOf(';')+1).split(";");
			String newOptions = "";
			for(int i = 0; i < options.length;++i){
				if(!options[i].startsWith("kerberos")){
					newOptions += ";"+options[i];
				}
			}
			urlStr = urlStr.substring(0, urlStr.indexOf(';'))+newOptions;
		}
		urlEl.appendChild(oozieXmlDoc.createTextNode(urlStr));
		hive.appendChild(urlEl);
		
		if(hpd.getPassword() != null && !hpd.getPassword().isEmpty()){
			Element passEl = oozieXmlDoc.createElement("password");
			passEl.appendChild(oozieXmlDoc.createTextNode(hpd.getPassword()));
			hive.appendChild(passEl);
		}
		
		Element script = oozieXmlDoc.createElement("script");
		script.appendChild(oozieXmlDoc.createTextNode(fileNames[0]));
		hive.appendChild(script);
		
		Iterator<String> it = getVariables().iterator();
		while(it.hasNext()){
			String var = it.next();
			Element paramEl = oozieXmlDoc.createElement("param");
			paramEl.appendChild(oozieXmlDoc.createTextNode(var+"=${"+var+"}"));
			hive.appendChild(paramEl);
		}
		
		action.appendChild(hive);

	}
	
	@Override
	public Element createCredentials(
			Document oozieXmlDoc
			)throws RemoteException{
		logger.debug("Get into hive create credentials function");
		Element credential = null;
		
		if(WorkflowPrefManager.isSecEnable()){
			logger.debug("Calculate hive credentials");

			String url = new HivePropertiesDetails("hive").getDburl();
			String credUrl = url;
			try{
				while(credUrl.contains(";")){
					credUrl = credUrl.substring(0, credUrl.indexOf(";"));
				}
			}catch(Exception e){}
			
			credential = oozieXmlDoc.createElement("credential");
			credential.setAttribute("name", "hive2-cred");
			credential.setAttribute("type", "hive2");
			
			{
				//Principal
				Element property = oozieXmlDoc.createElement("property");
				Element name = oozieXmlDoc.createElement("name");
				name.appendChild(oozieXmlDoc.createTextNode("hive2.server.principal"));
				property.appendChild(name);
				Element value = oozieXmlDoc.createElement("value");
				String principal = url;
				if(principal.contains("principal=")){
					principal = url.substring(url.indexOf("principal=")+10);
					if(principal.contains(";")){
						principal = principal.substring(0, principal.indexOf(";"));
					}
				}
				value.appendChild(oozieXmlDoc.createTextNode(principal));
				property.appendChild(value);
				credential.appendChild(property);
			}

			{
				//URL
				Element property = oozieXmlDoc.createElement("property");
				Element name = oozieXmlDoc.createElement("name");
				name.appendChild(oozieXmlDoc.createTextNode("hive2.jdbc.url"));
				property.appendChild(name);
				Element value = oozieXmlDoc.createElement("value");
				value.appendChild(oozieXmlDoc.createTextNode(credUrl));
				property.appendChild(value);
				credential.appendChild(property);
			}
		}
		return credential;
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
