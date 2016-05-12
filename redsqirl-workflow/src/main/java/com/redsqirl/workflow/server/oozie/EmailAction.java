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

import com.redsqirl.workflow.server.OozieUniqueActionAbs;


public class EmailAction extends OozieUniqueActionAbs{
	
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
		createOozieElement(oozieXmlDoc, action,
				destinatary,
				cc,
				subjectText,
				bodyText);
	}
	
	public static void createOozieElement(Document oozieXmlDoc, Element action,
			String to,
			String cc,
			String subject,
			String body){
		Element emailElement = oozieXmlDoc.createElement("email");
		Attr attrXmlns = oozieXmlDoc.createAttribute("xmlns");
		attrXmlns.setValue("uri:oozie:email-action:0.1");
		emailElement.setAttributeNode(attrXmlns);
		
		Element toElement = oozieXmlDoc.createElement("to");
		toElement.appendChild(oozieXmlDoc.createTextNode(to));
		emailElement.appendChild(toElement);
		
		if (cc != null && !cc.isEmpty()){
			Element ccElement = oozieXmlDoc.createElement("cc");
			ccElement.appendChild(oozieXmlDoc.createTextNode(cc));
			emailElement.appendChild(ccElement);
		}
		
		Element subjectElement = oozieXmlDoc.createElement("subject");
		subjectElement.appendChild(oozieXmlDoc.createTextNode(subject));
		emailElement.appendChild(subjectElement);
		
		Element bodyElement = oozieXmlDoc.createElement("body");
		bodyElement.appendChild(oozieXmlDoc.createTextNode(body));
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
