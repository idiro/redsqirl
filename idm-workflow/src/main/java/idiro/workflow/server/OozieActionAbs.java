package idiro.workflow.server;

import idiro.workflow.server.interfaces.OozieAction;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

	
	protected OozieActionAbs() throws RemoteException {
		super();
	}

	public void defaultParam(Document oozieXmlDoc, Element subAction){
		defaultParam(oozieXmlDoc, subAction,null);
	}
	
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

		Element confName = oozieXmlDoc.createElement("name");
		confName.appendChild(oozieXmlDoc.createTextNode("mapred.job.queue.name"));
		Element confValue = oozieXmlDoc.createElement("value");
		confValue.appendChild(oozieXmlDoc.createTextNode(
				getVar(OozieManager.prop_queue)));

		Element property = oozieXmlDoc.createElement("property");
		property.appendChild(confName);
		property.appendChild(confValue);

		Element configuration = oozieXmlDoc.createElement("configuration");
		configuration.appendChild(property);
		
		
//		property.appendChild(confName);
//		property.appendChild(confValue);
//		configuration.appendChild(property);
		subAction.appendChild(configuration);
	}
	
	public String getVar(String varName){
		return "${"+varName+"}";
	}
	
}
