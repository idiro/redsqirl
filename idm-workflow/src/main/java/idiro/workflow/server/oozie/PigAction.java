package idiro.workflow.server.oozie;

import idiro.workflow.server.OozieActionAbs;

import java.rmi.RemoteException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

	public PigAction() throws RemoteException {
		super();
	}

	@Override
	public void createOozieElement(Document oozieXmlDoc, Element action,
			String[] fileNames) throws RemoteException {
		Element pig = oozieXmlDoc.createElement("pig");

		defaultParam(oozieXmlDoc, pig);

		Element script = oozieXmlDoc.createElement("script");
		script.appendChild(oozieXmlDoc.createTextNode(fileNames[0]));
		pig.appendChild(script);

		action.appendChild(pig);
	}

	@Override
	public String[] getFileExtensions() {
		return new String[]{"pig"};
	}

}
