package idiro.workflow.server.datatype;

import idiro.hadoop.NameNodeVar;
import idiro.utils.FeatureList;
import idiro.workflow.server.DataOutput;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.connect.HDFSInterface;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class MapRedDir extends DataOutput{


	/**
	 * 
	 */
	private static final long serialVersionUID = 3497308078096391496L;
	
	
	/** HDFS Interface */
	protected static HDFSInterface hdfsInt;
	
	public MapRedDir() throws RemoteException{
		super();
		init();
	}
	
	public MapRedDir(FeatureList features) throws RemoteException {
		super(features);
		init();
	}
	
	private void init() throws RemoteException{
		if (hdfsInt == null) {
			hdfsInt = new HDFSInterface();
		}
	}
	

	/**
	 * Get the DataBrowser
	 * 
	 * @return {@link idiro.workflow.server.enumeration.DataBrowser}
	 * @throws RemoteException
	 */
	@Override
	public String getBrowser() throws RemoteException {
		return hdfsInt.getBrowserName();
	}


	@Override
	public boolean isPathExists() throws RemoteException {
		boolean ok = false;
		if (getPath() != null) {
			logger.info("checking if path exists: " + getPath().toString());
			int again = 10;
			FileSystem fs = null;
			while (again > 0) {
				try {
					fs = NameNodeVar.getFS();
					logger.debug("Attempt " + (11 - again) + ": existence "
							+ getPath());
					ok = fs.exists(new Path(getPath()));
					again = 0;
				} catch (Exception e) {
					logger.error(e);
					--again;
				}
				try {
					// fs.close();
				} catch (Exception e) {
					logger.error(e);
				}
				if (again > 0) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e1) {
						logger.error(e1);
					}
				}
			}
		}
		return ok;
	}
	
	/**
	 * Move the current path to a new one
	 * 
	 * @param newPath
	 * @throws RemoteException
	 */
	@Override
	public void moveTo(String newPath) throws RemoteException {
		if (isPathExists()) {
			hdfsInt.move(getPath(), newPath);
		}
		setPath(newPath);
	}

	/**
	 * Copy the current path to a new one
	 * 
	 * @param newPath
	 * @throws RemoteException
	 * 
	 */
	@Override
	public void copyTo(String newPath) throws RemoteException {
		if (isPathExists()) {
			hdfsInt.copy(getPath(), newPath);
		}
		setPath(newPath);
	}

	/**
	 * Remove the current path from hdfs
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String remove() throws RemoteException {
		return hdfsInt.delete(getPath());
	}

	@Override
	public boolean oozieRemove(Document oozieDoc, Element action,
			File localDirectory, String pathFromOozieDir,
			String fileNameWithoutExtension) throws RemoteException {
		Element fs = oozieDoc.createElement("fs");
		action.appendChild(fs);

		Element rm = oozieDoc.createElement("delete");
		rm.setAttribute("path", "${" + OozieManager.prop_namenode + "}"
				+ getPath());
		fs.appendChild(rm);

		return true;
	}
	

	/**
	 * Is name a variable
	 * 
	 * @param name
	 * @return <code>true</code> if name matches structure of a variable name
	 *         (contains characters with numbers) and has a maximum else
	 *         <code>false</code>
	 */
	public boolean isVariableName(String name) {
		String regex = "[a-zA-Z]([a-zA-Z0-9_]{0,29})";
		return name.matches(regex);
	}
	

	public List<String> selectLine(int maxToRead) throws RemoteException {
		List<String> ans = null;
		if (isPathValid() == null && isPathExists()) {
			try {
				FileSystem fs = NameNodeVar.getFS();
				FileStatus[] stat = fs.listStatus(new Path(getPath()),
						new PathFilter() {

					@Override
					public boolean accept(Path arg0) {
						return !arg0.getName().startsWith("_");
					}
				});
				ans = new ArrayList<String>(maxToRead);
				for (int i = 0; i < stat.length; ++i) {
					ans.addAll(hdfsInt.select(stat[i].getPath().toString(),
							",",
							(maxToRead / stat.length) + 1));
				}
				try {
					// fs.close();
				} catch (Exception e) {
					logger.error("Fail to close FileSystem: " + e);
				}
			} catch (IOException e) {
				String error = "Unexpected error: " + e.getMessage();
				logger.error(error);
				ans = null;
			}
		}
		return ans;
	}
}
