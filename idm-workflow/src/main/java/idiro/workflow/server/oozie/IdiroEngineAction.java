package idiro.workflow.server.oozie;

import idiro.hadoop.NameNodeVar;
import idiro.workflow.server.OozieActionAbs;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.WorkflowPrefManager;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Write an idiro engine action into an oozie xml file.
 * 
 * @author etienne
 * 
 */
public class IdiroEngineAction extends OozieActionAbs {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2656343380409195545L;

	private Set<String> pathToRemove = new HashSet<String>();

	public IdiroEngineAction() throws RemoteException {
		super();
	}

	@Override
	public void createOozieElement(Document oozieXmlDoc, Element action,
			String[] fileNames) throws RemoteException {

		Element java = oozieXmlDoc.createElement("java");

		defaultParam(oozieXmlDoc, java);
		String path = WorkflowPrefManager
				.getSysProperty(WorkflowPrefManager.sys_idiroEngine_path);
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
						.createTextNode("idiro.xmlm.IdiroEngineMain"));
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

	@Override
	public String[] getFileExtensions() {
		return new String[] { ".xml" };
	}

	public Set<String> getPathToRemove() {
		return pathToRemove;
	}

	public void setPathToRemove(Set<String> pathToRemove) {
		this.pathToRemove = pathToRemove;
	}

	public boolean addRemovePath(String e) {

		return pathToRemove.add(e);
	}

	public void clearPathToRemove() {
		pathToRemove.clear();
	}

}
