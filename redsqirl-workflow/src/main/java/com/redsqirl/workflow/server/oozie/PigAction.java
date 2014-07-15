package com.redsqirl.workflow.server.oozie;


import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.OozieActionAbs;

/**
 * Write a pig action into an oozie xml file.
 * @author etienne
 *
 */
public class PigAction extends OozieActionAbs {

	/**
	 * 
	 */
	private static final long serialVersionUID = 233700291606047641L;
	
	private Logger logger = Logger.getLogger(getClass());
	
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public PigAction() throws RemoteException {
		super();
	}
	
	/**
	 * Create an element for a Pig Action in Oozie file
	 * @param oozieXmlDoc
	 * @param action
	 * @param fileNames
	 * @throws RemoteException
	 */
	@Override
	public void createOozieElement(Document oozieXmlDoc, Element action, String[] fileNames) throws RemoteException {
		
		logger.info("createOozieElement ");
		
		Element pig = oozieXmlDoc.createElement("pig");
		defaultParam(oozieXmlDoc, pig);
		
		logger.info("createOozieElement 1");
		
		Element script = oozieXmlDoc.createElement("script");
		script.appendChild(oozieXmlDoc.createTextNode(fileNames[0]));
		
		logger.info("createOozieElement 2");
		
		pig.appendChild(script);
		action.appendChild(pig);
		
		logger.info("createOozieElement 3");
		
	}
	
	/**
	 * Get the file extensions needed for a pig action
	 * @return extensions
	 */
	@Override
	public String[] getFileExtensions() {
		return new String[]{".pig", ".properties"};
	}

}