package idiro.workflow.server.action;

import idiro.workflow.server.DataOutput;
import idiro.workflow.server.enumeration.DataBrowser;

import java.io.File;
import java.rmi.RemoteException;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class OutputActionTest extends DataOutput{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1198299149815581123L;


	public OutputActionTest() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public void generatePath(String userName, String component,
			String outputName) throws RemoteException {
		setPath("");
	}

	@Override
	public String isPathValid() throws RemoteException {
		return null;
	}

	@Override
	public boolean isPathAutoGeneratedForUser(String userName,
			String component, String outputName) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPathExists() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String remove() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean oozieRemove(Document oozieDoc, Element action,
			File localDirectory, String pathFromOozie, String fileNameWithoutExtension)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> select(int maxToRead) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getTypeName() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public DataBrowser getBrowser() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
