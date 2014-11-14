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
