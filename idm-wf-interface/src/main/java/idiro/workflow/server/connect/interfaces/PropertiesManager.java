package idiro.workflow.server.connect.interfaces;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Properties;

public interface PropertiesManager extends Remote {
	
	
	public Properties getSysProperties() throws RemoteException;
	
	public void storeSysProperties(Properties prop) throws RemoteException, IOException;
	
	public Properties getSysLangProperties() throws RemoteException;;

	public Properties getUserProperties() throws RemoteException;
	
	public void storeUserProperties(Properties prop) throws RemoteException, IOException;

	public String getSysProperty(String key) throws RemoteException;

	public String getSysProperty(String key, String defaultValue) throws RemoteException;

	public String getUserProperty(String key) throws RemoteException;

	public String getUserProperty(String key, String defaultValue) throws RemoteException;
}
