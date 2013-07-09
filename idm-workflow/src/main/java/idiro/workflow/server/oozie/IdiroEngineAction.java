package idiro.workflow.server.oozie;

import idiro.workflow.server.OozieActionAbs;
import idiro.workflow.server.OozieManager;

import java.rmi.RemoteException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Write an idiro engine action into an oozie xml file.
 * @author etienne
 *
 */
public class IdiroEngineAction extends OozieActionAbs {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2656343380409195545L;
	
	public IdiroEngineAction() throws RemoteException {
		super();
	}

	@Override
	public void createOozieElement(Document oozieXmlDoc, Element action,
			String[] fileNames) throws RemoteException {

		Element java = oozieXmlDoc.createElement("java");

		defaultParam(oozieXmlDoc, java);

		Element mainClass = oozieXmlDoc.createElement("main-class");
		mainClass.appendChild(oozieXmlDoc.createTextNode("${idiroEngineMain}"));
		java.appendChild(mainClass);

		Element arg1 =  oozieXmlDoc.createElement("arg");
		arg1.appendChild(oozieXmlDoc.createTextNode(OozieManager.prop_namenode));
		java.appendChild(arg1);

		Element arg2 = oozieXmlDoc.createElement("arg");
		arg2.appendChild(oozieXmlDoc.createTextNode(fileNames[0]));
		java.appendChild(arg2);

		action.appendChild(java);
	}

	@Override
	public String[] getFileExtensions() {
		return new String[]{".xml"};
	}

}
