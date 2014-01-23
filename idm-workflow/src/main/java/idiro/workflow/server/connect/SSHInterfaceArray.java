package idiro.workflow.server.connect;

import idiro.workflow.server.connect.interfaces.DataStore;
import idiro.workflow.server.connect.interfaces.DataStoreArray;
import idiro.workflow.utils.LanguageManagerWF;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SSHInterfaceArray extends UnicastRemoteObject implements DataStoreArray {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected static Map<String,String> initFieldsInit;
	protected static Map<String,String> fieldsToRemove;
	
	public static final String hostName = "host name",
			port = "port";
	
	
	protected Map<String,DataStore> stores = new LinkedHashMap<String,DataStore>();
	
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



	@Override
	public Map<String,String> getFieldsInitNeeded() throws RemoteException {
		return initFieldsInit;
	}



	@Override
	public Map<String,String> getFieldsToRemove() throws RemoteException {
		return fieldsToRemove;
	}


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



	@Override
	public Map<String,DataStore> getStores() throws Exception, RemoteException {
		return stores;
	}



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
