package idiro.workflow.server.oozie;

import idiro.workflow.server.OozieActionAbs;
import idiro.workflow.server.WorkflowPrefManager;

import java.rmi.RemoteException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Write a hive action into an oozie xml.
 *
 * @author etienne
 *
 */
public class HiveAction extends OozieActionAbs{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5119314850496590566L;
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public HiveAction() throws RemoteException {
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

		Element hive = oozieXmlDoc.createElement("hive");
		Attr attrXmlns = oozieXmlDoc.createAttribute("xmlns");
		attrXmlns.setValue("uri:oozie:hive-action:0.2");
		hive.setAttributeNode(attrXmlns);
		
		defaultParam(
				oozieXmlDoc, 
				hive,
				WorkflowPrefManager.getSysProperty(
				WorkflowPrefManager.sys_hive_xml));
		
		Element confName = oozieXmlDoc.createElement("name");
		confName.appendChild(oozieXmlDoc.createTextNode("oozie.hive.defaults"));
		Element confValue = oozieXmlDoc.createElement("value");
		confValue.appendChild(oozieXmlDoc.createTextNode(
				WorkflowPrefManager.getSysProperty(
						WorkflowPrefManager.sys_hive_default_xml)));
		
		Element property = oozieXmlDoc.createElement("property");
		property.appendChild(confName);
		property.appendChild(confValue);

		Element configuration = (Element) hive.getElementsByTagName("configuration").item(0);
		configuration.appendChild(property);
		
		Element script = oozieXmlDoc.createElement("script");
		script.appendChild(oozieXmlDoc.createTextNode(fileNames[0]));
		hive.appendChild(script);
		
		action.appendChild(hive);

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
