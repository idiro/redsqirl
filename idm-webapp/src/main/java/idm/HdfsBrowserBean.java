package idm;

import idiro.workflow.server.connect.interfaces.DataStore;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

/** HdfsBean
 * 
 * Class to screen control of the File System Hadoop
 * 
 * @author Igor.Souza
 */
public class HdfsBrowserBean extends HdfsBean {

	private static Logger logger = Logger.getLogger(HdfsBrowserBean.class);
	
	@Override
	public DataStore getRmiHDFS() throws RemoteException{
		return getHDFSBrowser();
	}
	
}