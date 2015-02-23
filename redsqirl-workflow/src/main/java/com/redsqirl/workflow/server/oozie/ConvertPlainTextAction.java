package com.redsqirl.workflow.server.oozie;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.idiro.hadoop.NameNodeVar;
import com.redsqirl.workflow.server.OozieActionAbs;
import com.redsqirl.workflow.server.action.ConvertPlainText;


public class ConvertPlainTextAction extends OozieActionAbs{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7136772929442974523L;
	
	private String path;
	private String pathOut;
	
	public ConvertPlainTextAction() throws RemoteException {
		super();
	}

	@Override
	public void createOozieElement(Document oozieXmlDoc, Element action,
			String[] fileNames) throws RemoteException {
		
		List<String> pathList = new ArrayList<String>();
		
		String filesConcatenate = "";
		
		FileSystem fs;
		try {
			fs = NameNodeVar.getFS();
			FileStatus[] stat = fs.listStatus(new Path(path),
					new PathFilter() {

				@Override
				public boolean accept(Path arg0) {
					return !arg0.getName().startsWith("_") && !arg0.getName().startsWith(".");
				}
			});
			for (int i = 0; i < stat.length; ++i) {
				if (!stat[i].isDir()) {
					String file = stat[i].getPath().toString().replace(fs.getUri().toString(), "");
					System.out.println(file);
					pathList.add(file);
					filesConcatenate += file + " ";
				}
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		pathList.size();
		
		Element shellElement = oozieXmlDoc.createElement("shell");
		defaultParam(oozieXmlDoc, shellElement);
		
		Attr attrXmlns = oozieXmlDoc.createAttribute("xmlns");
		attrXmlns.setValue("uri:oozie:shell-action:0.1");
		shellElement.setAttributeNode(attrXmlns);
		
		String command = "hadoop fs -cat " + filesConcatenate + " | hadoop fs -put - " + pathOut;
		
		Element execElement = oozieXmlDoc.createElement("exec");
		execElement.appendChild(oozieXmlDoc.createTextNode(command));
		shellElement.appendChild(execElement);
		
		action.appendChild(shellElement);
		
	}

	@Override
	public String[] getFileExtensions() throws RemoteException {
		return new String[] { ".xml" };
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPathOut() {
		return pathOut;
	}

	public void setPathOut(String pathOut) {
		this.pathOut = pathOut;
	}
}
