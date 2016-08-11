package com.redsqirl.workflow.server.oozie;

import java.rmi.RemoteException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.OozieUniqueActionAbs;

public class DistcpAction extends OozieUniqueActionAbs{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5119314850496590566L;

	private static Logger logger = Logger.getLogger(DistcpAction.class);
	
	protected String input,output; 

	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public DistcpAction() throws RemoteException {
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

		Element distcp = oozieXmlDoc.createElement("distcp");
		Attr attrXmlns = oozieXmlDoc.createAttribute("xmlns");
		attrXmlns.setValue("uri:oozie:distcp-action:0.2");
		distcp.setAttributeNode(attrXmlns);
		

		defaultParam(
				oozieXmlDoc, 
				distcp);
		
		{
			Element paramEl = oozieXmlDoc.createElement("arg");
			paramEl.appendChild(oozieXmlDoc.createTextNode(input));
			distcp.appendChild(paramEl);
		}
		{
			Element paramEl = oozieXmlDoc.createElement("arg");
			paramEl.appendChild(oozieXmlDoc.createTextNode(output));
			distcp.appendChild(paramEl);
		}
		
		action.appendChild(distcp);

	}
	
	
	/**
	 * Get the file name extensions for Hive actions 
	 * @return extension
	 */
	@Override
	public String[] getFileExtensions() {
		return new String[]{};
	}

	public final String getInput() {
		return input;
	}

	public final void setInput(String input) {
		this.input = input;
	}

	public final String getOutput() {
		return output;
	}

	public final void setOutput(String output) {
		this.output = output;
	}
	
}
