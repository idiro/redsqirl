package com.redsqirl.workflow.server.oozie;



import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.idiro.hadoop.NameNodeVar;
import com.redsqirl.workflow.server.OozieActionAbs;
import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.WorkflowPrefManager;

/**
 * Write an idiro engine action into an oozie xml file.
 * 
 * @author etienne
 * 
 */
public class SqirlNutcrackerAction extends OozieActionAbs {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2656343380409195545L;
	/**
	 * Set of paths to remove
	 */
	private Set<String> pathToRemove = new HashSet<String>();
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public SqirlNutcrackerAction() throws RemoteException {
		super();
	}
	/**
	 * Create an element for Idiro Engine Action in the Oozie action file
	 * @param oozieXmlDoc
	 * @param action
	 * @param fileNames
	 * @throws RemoteException
	 */
	@Override
	public void createOozieElement(Document oozieXmlDoc, Element action,
			String[] fileNames) throws RemoteException {

		Element java = oozieXmlDoc.createElement("java");

		defaultParam(oozieXmlDoc, java);
		String path = WorkflowPrefManager
				.getSysProperty(WorkflowPrefManager.sys_nutcracker_path);
		try {
			FileSystem fs = NameNodeVar.getFS();
			if (fs.isDirectory(new Path(path))) {

				FileStatus[] fileStatus = fs.listStatus(new Path(path));

				Element property = oozieXmlDoc.createElement("property");
				Element confName = oozieXmlDoc.createElement("name");
				confName.appendChild(oozieXmlDoc
						.createTextNode("oozie.launcher.fs.hdfs.impl.disable.cache"));
				Element confValue = oozieXmlDoc.createElement("value");
				confValue.appendChild(oozieXmlDoc.createTextNode("true"));
				property.appendChild(confName);
				property.appendChild(confValue);
				Element configuration = (Element) java.getElementsByTagName(
						"configuration").item(0);
				configuration.appendChild(property);
				
				property = oozieXmlDoc.createElement("property");
				confName = oozieXmlDoc.createElement("name");
				confValue = oozieXmlDoc.createElement("value");
				confName.appendChild(oozieXmlDoc
						.createTextNode("oozie.launcher.oozie.libpath"));
				confValue.appendChild(oozieXmlDoc
						.createTextNode(path));
				
//				for (int i = 0; i < fileStatus.length; ++i) {
//					if (fileStatus[i].getPath().getName().contains(".jar")) {
//						String name = fileStatus[i].getPath().toString();
//						confValue.appendChild(oozieXmlDoc.createTextNode(name+":"));
//					}
//				}
				property.appendChild(confName);
				property.appendChild(confValue);
				configuration.appendChild(property);

				// Iterator<String> it = pathToRemove.iterator();
				// if(it.hasNext()){
				// Element prepare = oozieXmlDoc.createElement("prepare");
				// while(it.hasNext()){
				// Element delete = oozieXmlDoc.createElement("delete");
				// delete.setAttribute("path", it.next());
				// prepare.appendChild(delete);
				// }
				// java.appendChild(prepare);
				// }

				// <prepare>
				// <delete path="[PATH]"/>
				// </prepare>
				Element mainClass = oozieXmlDoc.createElement("main-class");
				mainClass.appendChild(oozieXmlDoc
						.createTextNode("com.sqirlnutcracker.SqirlNutcrackerMain"));
				java.appendChild(mainClass);

				Element javaopts = oozieXmlDoc.createElement("java-opts");
				javaopts.appendChild(oozieXmlDoc.createTextNode("-Duser.name="
						+ System.getProperty("user.name")));
				java.appendChild(javaopts);

				Element arg1 = oozieXmlDoc.createElement("arg");
				arg1.appendChild(oozieXmlDoc.createTextNode("${"
						+ OozieManager.prop_namenode + "}"));
				java.appendChild(arg1);

				/*Element arg3 = oozieXmlDoc.createElement("arg");
				arg3.appendChild(oozieXmlDoc.createTextNode("-nologinit"));
				java.appendChild(arg3);*/

				Element arg2 = oozieXmlDoc.createElement("arg");
				String[] filename = fileNames[0].split("/");
				arg2.appendChild(oozieXmlDoc
						.createTextNode(filename[filename.length - 1]));
				java.appendChild(arg2);

				Element file = oozieXmlDoc.createElement("file");
				file.appendChild(oozieXmlDoc.createTextNode(fileNames[0]));
				java.appendChild(file);

				

				action.appendChild(java);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	/**
	 * Get the file extensions for Idiro Engine action
	 * @return extensions
	 */
	@Override
	public String[] getFileExtensions() {
		return new String[] { ".xml" };
	}
	/**
	 * Get a set of paths to remove
	 * @return Set of paths
	 */
	public Set<String> getPathToRemove() {
		return pathToRemove;
	}
	/**
	 * Set the list of paths to remove 
	 * @param pathToRemove
	 */
	public void setPathToRemove(Set<String> pathToRemove) {
		this.pathToRemove = pathToRemove;
	}
	/**
	 * Add a path to remove 
	 * @param e
	 * @return <code>true</code> if path remove was successful 
	 */
	public boolean addRemovePath(String e) {

		return pathToRemove.add(e);
	}
	/**
	 * Clear the path to remove
	 */
	public void clearPathToRemove() {
		pathToRemove.clear();
	}

}
