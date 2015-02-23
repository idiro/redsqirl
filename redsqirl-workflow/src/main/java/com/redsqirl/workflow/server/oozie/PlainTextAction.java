package com.redsqirl.workflow.server.oozie;

import java.rmi.RemoteException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.OozieActionAbs;


public class PlainTextAction extends OozieActionAbs{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7136772929442974523L;
	
	public PlainTextAction() throws RemoteException {
		super();
	}

	@Override
	public void createOozieElement(Document oozieXmlDoc, Element action,
			String[] fileNames) throws RemoteException {
		
		
		Element shellElement = oozieXmlDoc.createElement("shell");
		defaultParam(oozieXmlDoc, shellElement);
		
		Attr attrXmlns = oozieXmlDoc.createAttribute("xmlns");
		attrXmlns.setValue("uri:oozie:shell-action:0.1");
		shellElement.setAttributeNode(attrXmlns);
		
		Element execElement = oozieXmlDoc.createElement("exec");
		execElement.appendChild(oozieXmlDoc.createTextNode(fileNames[0]));
		shellElement.appendChild(execElement);
		
		Element fileElement = oozieXmlDoc.createElement("file");
		fileElement.appendChild(oozieXmlDoc.createTextNode(fileNames[0]));
		shellElement.appendChild(fileElement);
		
		action.appendChild(shellElement);
		
	}

	@Override
	public String[] getFileExtensions() throws RemoteException {
		return new String[] { ".sh" };
	}
}
