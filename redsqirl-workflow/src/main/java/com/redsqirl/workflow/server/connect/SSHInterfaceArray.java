package com.redsqirl.workflow.server.connect;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.connect.interfaces.DataStoreArray;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class SSHInterfaceArray extends UnicastRemoteObject implements DataStoreArray {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**Map of Fields that are needed*/
	protected static Map<String,String> initFieldsInit;
	/**Map of Fields to remove*/
	protected static Map<String,String> fieldsToRemove;
	/**
	 * Host name Key
	 */
	public static final String hostName = "host name",
			/**Port Key*/
			port = "port";
	
	/**
	 * Map of available DataStores
	 */
	protected Map<String,DataStore> stores = new LinkedHashMap<String,DataStore>();
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public SSHInterfaceArray() throws RemoteException {
		super();
		if(initFieldsInit == null){
			initFieldsInit = new LinkedHashMap<String,String>();
			initFieldsInit.put(hostName,"Host name known on the local network. It can be an IP address.");
			initFieldsInit.put(port,"Port to connect to, by default it is 22. This field have to be a number");
			
			fieldsToRemove = new LinkedHashMap<String,String>();
			fieldsToRemove.put(hostName,
					"Host name known on the local network. It can be an IP address.");
		}
	}


	/**
	 * Get a map of Fields that are needed
	 * @return Map of Field
	 * @throws RemoteException
	 */
	@Override
	public Map<String,String> getFieldsInitNeeded() throws RemoteException {
		return initFieldsInit;
	}


	/**
	 * Get a map of fields to remove
	 * @return Map of Fields
	 * @throws RemoteException
	 */
	@Override
	public Map<String,String> getFieldsToRemove() throws RemoteException {
		return fieldsToRemove;
	}

	/**
	 * Add a DataStore to to the list of DataStores
	 * @param field containing hostname and port
	 * @return hostname
	 * @throws RemoteException 
	 * @throws Exception
	 */
	@Override
	public String addStore(Map<String, String> fields)
			throws RemoteException, Exception {
		
		String host = fields.get(hostName);
		
		if(stores.containsKey(host)){
			throw new Exception("A connection with "+host+"already exists");
		}
		
		String p = fields.get(port);
		int pInt = 22;
		
		if(p != null && !p.trim().isEmpty()){
			try{
				pInt = Integer.valueOf(p.trim());
			}catch(NumberFormatException e){
				throw new Exception("Error the port given is not a number");
			}
		}
		
		SSHInterface sshInt = new SSHInterface(host, pInt);
		stores.put(host,sshInt);
		
		return host;
	}

	/**
	 * Remove a DataStore from the list of active Stores
	 * @param name
	 * @return Error Message
	 * @throws Remote Exception
	 */
	@Override
	public String removeStore(String name) throws RemoteException {
		String error = null;
		DataStore ds = stores.get(name);
		if(ds != null){
			ds.close();
			stores.remove(name);
		}else{
			error = LanguageManagerWF.getText("sshinterfacearray.removestorefail",new Object[]{name});
		}
		
		return error; 
	}
	/**
	 * Get the details of the Know Hosts
	 * @return List of Details for Known Stores
	 * @throws RemoteException
	 */
	@Override
	public List<Map<String, String>> getKnownStoreDetails()
			throws RemoteException {
		List<Map<String,String>> ans = new LinkedList<Map<String,String>>();
		Iterator<String> it = SSHInterface.getKnownHost().iterator();
		while(it.hasNext()){
			Map<String,String> cur = new LinkedHashMap<String,String>();
			cur.put(hostName, it.next());
			ans.add(cur);
		}
		
		return ans;
	}


	/**
	 * Initialize the Stores with KnownHosts
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String initKnownStores() throws RemoteException {
		String error = null;
		try {
			
			stores.putAll(SSHInterface.getHosts());
		} catch (Exception e) {
			error = e.getMessage();
		}
		return error;
	}


	/**
	 * Get the Stores available 
	 * @return Map of DataStores
	 * @throws Exception
	 * @throws {@link RemoteException}
	 */
	@Override
	public Map<String,DataStore> getStores() throws Exception, RemoteException {
		return stores;
	}


	/**
	 * Add a host to the Known Hosts List
	 * @param fields
	 * @return Error Message
	 * @throws RemoteException
	 *
	 */
	@Override
	public String addKnownStore(Map<String, String> fields)
			throws RemoteException {
		
		String error = null;
		String host = fields.get(hostName);
		String p = fields.get(port);
		int pInt = 22;
		
		if(p != null && !p.trim().isEmpty()){
			try{
				pInt = Integer.valueOf(p.trim());
			}catch(NumberFormatException e){
				error = LanguageManagerWF.getText("sshinterfacearray.porthostfail");
			}
		}
		if(error == null){
			error = SSHInterface.addKnownHost(host, pInt);
		}
		
		return error;
	}



	@Override
	public String removeKnownStore(Map<String, String> fields)
			throws RemoteException {
		return SSHInterface.removeKnownHost(fields.get(hostName));
	}



	@Override
	public void resetKnownStores() throws RemoteException {
		SSHInterface.resetKnownHost();
	}

}
