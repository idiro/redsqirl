package com.redsqirl.workflow.server.oozie;


import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.OozieActionAbs;

/**
 * Write a mrql action into an oozie xml file.
 * @author marcos
 *
 */
public class MrqlAction extends OozieActionAbs {

	/**
	 * 
	 */
	private static final long serialVersionUID = 233700291606047641L;
	
	private static Logger logger = Logger.getLogger(MrqlAction.class);
	
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public MrqlAction() throws RemoteException {
		super();
	}
	
	/**
	 * Create an element for a Mrql Action in Oozie file
	 * @param oozieXmlDoc
	 * @param action
	 * @param fileNames
	 * @throws RemoteException
	 */
	@Override
	public void createOozieElement(Document oozieXmlDoc, Element action, String[] fileNames) throws RemoteException {
		
		Element shellElement = oozieXmlDoc.createElement("shell");
		defaultParam(oozieXmlDoc, shellElement);
		
		Attr attrXmlns = oozieXmlDoc.createAttribute("xmlns");
		attrXmlns.setValue("uri:oozie:shell-action:0.1");
		shellElement.setAttributeNode(attrXmlns);
		
		Element execElement = oozieXmlDoc.createElement("exec");
//		execElement.appendChild(oozieXmlDoc.createTextNode("mrql.bsp"));
		execElement.appendChild(oozieXmlDoc.createTextNode(fileNames[1]));
		shellElement.appendChild(execElement);
		
//		Element argumentElement = oozieXmlDoc.createElement("argument");
//		argumentElement.appendChild(oozieXmlDoc.createTextNode("-nodes"));
//		shellElement.appendChild(argumentElement);
//		
//		Element argumentElement2 = oozieXmlDoc.createElement("argument");
//		argumentElement2.appendChild(oozieXmlDoc.createTextNode("2"));
//		shellElement.appendChild(argumentElement2);
		
		Element argumentElement3 = oozieXmlDoc.createElement("argument");
		argumentElement3.appendChild(oozieXmlDoc.createTextNode(fileNames[0].substring(fileNames[0].indexOf("/") + 1, fileNames[0].length()) ));
		shellElement.appendChild(argumentElement3);
		
		Element fileElement = oozieXmlDoc.createElement("file");
		fileElement.appendChild(oozieXmlDoc.createTextNode(fileNames[0]));
		shellElement.appendChild(fileElement);
		
		Element fileElement2 = oozieXmlDoc.createElement("file");
		fileElement2.appendChild(oozieXmlDoc.createTextNode(fileNames[1]));
		shellElement.appendChild(fileElement2);
		
		action.appendChild(shellElement);
		
	}
	
	/**
	 * Get the file extensions needed for a mrql action
	 * @return extensions
	 */
	@Override
	public String[] getFileExtensions() {
		return new String[]{".mrql", ".sh"};
	}

}