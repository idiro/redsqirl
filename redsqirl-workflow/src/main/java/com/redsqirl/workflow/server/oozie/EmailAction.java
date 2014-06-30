package com.redsqirl.workflow.server.oozie;

import java.rmi.RemoteException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.OozieActionAbs;


public class EmailAction extends OozieActionAbs{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7136772929442974523L;
	
	private String destinatary;
	private String cc;
	private String subjectText;
	private String bodyText;

	public EmailAction() throws RemoteException {
		super();
	}

	@Override
	public void createOozieElement(Document oozieXmlDoc, Element action,
			String[] fileNames) throws RemoteException {
		
		
		Element emailElement = oozieXmlDoc.createElement("email");
		Attr attrXmlns = oozieXmlDoc.createAttribute("xmlns");
		attrXmlns.setValue("uri:oozie:email-action:0.1");
		emailElement.setAttributeNode(attrXmlns);
		
		Element toElement = oozieXmlDoc.createElement("to");
		toElement.appendChild(oozieXmlDoc.createTextNode(destinatary));
		emailElement.appendChild(toElement);
		
		if (cc != null && !cc.isEmpty()){
			Element ccElement = oozieXmlDoc.createElement("cc");
			ccElement.appendChild(oozieXmlDoc.createTextNode(cc));
			emailElement.appendChild(ccElement);
		}
		
		Element subjectElement = oozieXmlDoc.createElement("subject");
		subjectElement.appendChild(oozieXmlDoc.createTextNode(subjectText));
		emailElement.appendChild(subjectElement);
		
		Element bodyElement = oozieXmlDoc.createElement("body");
		bodyElement.appendChild(oozieXmlDoc.createTextNode(bodyText));
		emailElement.appendChild(bodyElement);
		
		action.appendChild(emailElement);
		
	}

	@Override
	public String[] getFileExtensions() throws RemoteException {
		return new String[] { ".xml" };
	}

	public void setDestinatary(String destinatary) {
		this.destinatary = destinatary;
	}

	public void setSubjectText(String subjectText) {
		this.subjectText = subjectText;
	}

	public void setBodyText(String bodyText) {
		this.bodyText = bodyText;
	}
	
	public void setCc(String cc) {
		this.cc = cc;
	}
}
