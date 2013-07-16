package idiro.workflow.server.datatype;

import idiro.hadoop.NameNodeVar;
import idiro.hadoop.checker.HdfsFileChecker;
import idiro.utils.RandomString;
import idiro.workflow.server.DataOutput;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.enumeration.DataBrowser;
import idiro.workflow.server.enumeration.FeatureType;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Data File Output Action Type.
 * Return a data file character delimited.
 * @author etienne
 *
 */
public class DataFileType extends DataOutput{

	public final static String key_header = "header",
			key_delimiter = "delimiter";

	protected static HDFSInterface hdfsInt;
	/**
	 * 
	 */
	private static final long serialVersionUID = -3691211283409701170L;

	public DataFileType() throws RemoteException {
		super();
		if(hdfsInt == null){
			hdfsInt = new HDFSInterface();
		}
	}

	public DataFileType(Map<String,FeatureType> features) throws RemoteException{
		super(features);
		if(hdfsInt == null){
			hdfsInt = new HDFSInterface();
		}
	}
	

	@Override
	public String getTypeName() throws RemoteException {
		return "DATA FILE";
	}

	@Override
	public DataBrowser getBrowser() throws RemoteException {
		return DataBrowser.HDFS;
	}

	@Override
	public String remove() throws RemoteException {
		return hdfsInt.delete(getPath());
	}

	@Override
	public boolean oozieRemove(Document doc, Element parent,
			File directoryToWrite, String pathFromOozieDir,
			String fileNameWithoutExtension) throws RemoteException {
		Element fs = doc.createElement("fs");
		parent.appendChild(fs);

		Element rm = doc.createElement("delete");
		rm.setAttribute("path", getPath());
		fs.appendChild(rm);

		return true;
	}

	@Override
	public List<String> select(int maxToRead) throws RemoteException {
		List<String> ans = hdfsInt.select(getPath(),maxToRead);
		if(dataProperty.containsKey(key_header)&& dataProperty.get(key_header).equalsIgnoreCase("true")){
			ans.remove(0);
		}
		return ans;
	}

	@Override
	public String isPathValid() throws RemoteException {
		String error = null;
		HdfsFileChecker hCh = new HdfsFileChecker(getPath());
		if(!hCh.isInitialized() || 
			hCh.isDirectory()){
			error = "The file is a directory";
		}else{
			FileSystem fs;
			try {
				fs = NameNodeVar.getFS();
				hCh.setPath(new Path(getPath()).getParent());
				if(!hCh.isDirectory()){
					error = "The parent of the file does not exists";
				}
				fs.close();
			} catch (IOException e) {
				error = "Unexpected error: "+e.getMessage();
				logger.error(error);
			}
			
		}
		hCh.close();
		return error;
	}
	
	@Override
	public void generatePath(String userName,
			String component, 
			String outputName) throws RemoteException {
		setPath("/user/"+userName+
				"/tmp/idm_"+
				component+"_"+
				outputName+"_"+
				RandomString.getRandomName(8)+".dat");
	}

	@Override
	public boolean isPathAutoGeneratedForUser(String userName,
			String component, String outputName) throws RemoteException {
		return getPath().startsWith("/user/"+userName+
				"/tmp/idm_"+
				component+"_"+
				outputName+"_");
	}

	@Override
	public boolean isPathExists() throws RemoteException {
		boolean ok = false;
		HdfsFileChecker hCh = new HdfsFileChecker(getPath());
		if(hCh.isFile()){
			ok = true;
		}
		hCh.close();
		return ok;
	}

}