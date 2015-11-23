package com.redsqirl.workflow.server.connect.interfaces;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.server.connect.SSHInterfaceArray;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.connect.interfaces.DataStoreArray;
import com.redsqirl.workflow.test.TestUtils;

public class SSHInterfaceArrayTests {

	Logger logger = Logger.getLogger(getClass());
	
	@Test
	public void check_save(){
		TestUtils.logTestTitle("HDFSInterfaceArrayTests#save");
		
		try{
			SSHInterfaceArray arr = SSHInterfaceArray.getInstance();
			
			arr.resetKnownStores();
			assertTrue("No host should be there",arr.getKnownStoreDetails().isEmpty());
			
			Map<String,String> host = new LinkedHashMap<String,String>();
			host.put(SSHInterfaceArray.hostName,"namenode");
			host.put(SSHInterfaceArray.port,"22");
			
			logger.info("key needed: "+arr.getFieldsInitNeeded().toString());
			logger.info("host: "+host.toString());
			assertTrue("map are not contained", host.keySet().containsAll(arr.getFieldsInitNeeded().keySet()));
			
			assertTrue("Fail to add localhost",arr.addKnownStore(host) == null);
			assertTrue("Does not contains namenode",
					checkInStore(arr,"namenode")
					);
			assertTrue("localhost is normally already inserted",arr.addKnownStore(host) != null);
			
			Map<String,String> host2 = new LinkedHashMap<String,String>();
			host2.put(SSHInterfaceArray.hostName,"datanode2");
			host2.put(SSHInterfaceArray.port,"22");
			
			logger.info("add namenode...");
			
			assertTrue("Fail to add namenode server",arr.addKnownStore(host2) == null);
			assertTrue("List should still contain namenode",
					checkInStore(arr,"namenode"));
			assertTrue("List should contain datanode2", 
					checkInStore(arr,"datanode2")
					);
			
			Map<String,String> removeHost2 = new LinkedHashMap<String,String>();
			removeHost2.put(SSHInterfaceArray.hostName, "datanode2");
			
			logger.info("remove namenode...");
			assertTrue("Fail to remove datanode2", arr.removeKnownStore(removeHost2) == null);
			
			assertTrue("List should still contain namenode",
					checkInStore(arr,"namenode")
					);
			
			assertTrue("List should not contain namenode anymore", 
					!checkInStore(arr,"datanode2"));
			
			
			logger.info("remove localhost...");
			arr.initKnownStores();
			Map<String,DataStore> l = arr.getStores();
			assertTrue(l.size() == 1);
			l.get("namenode").close();
			
			Map<String,String> removeHost = new LinkedHashMap<String,String>();
			removeHost.put(SSHInterfaceArray.hostName, "namenode");
			
			assertTrue("Fail to remove namenode",arr.removeKnownStore(removeHost) == null);
			assertTrue("List should be empty",arr.getKnownStoreDetails().isEmpty());
		}catch(Exception e){
			logger.error("Exception "+e.getMessage());
			assertTrue(e.getMessage(),false);
		}
		
	}
	
	boolean checkInStore(DataStoreArray dsa, String host) throws RemoteException{
		boolean found = false;
		
		Iterator<Map<String,String>> it = dsa.getKnownStoreDetails().iterator();
		while(it.hasNext() && !found){
			found = host.equals(it.next().get(SSHInterfaceArray.hostName));
			
		}
		return found;
	}
	
}
