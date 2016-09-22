/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

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
	String host1 = "dev3.local.net";
	String host2 = "dev.local.net";
	
	@Test
	public void check_save(){
		TestUtils.logTestTitle("HDFSInterfaceArrayTests#save");
		
		try{
			SSHInterfaceArray arr = SSHInterfaceArray.getInstance();
			
			arr.resetKnownStores();
			assertTrue("No host should be there",arr.getKnownStoreDetails().isEmpty());
			
			Map<String,String> host = new LinkedHashMap<String,String>();
			host.put(SSHInterfaceArray.hostName,host1);
			host.put(SSHInterfaceArray.port,"22");
			
			logger.info("key needed: "+arr.getFieldsInitNeeded().toString());
			logger.info("host: "+host.toString());
			assertTrue("map are not contained", host.keySet().containsAll(arr.getFieldsInitNeeded().keySet()));
			
			assertTrue("Fail to add localhost",arr.addKnownStore(host) == null);
			assertTrue("Does not contains dev3.local.net",
					checkInStore(arr,host1)
					);
			assertTrue("localhost is normally already inserted",arr.addKnownStore(host) != null);
			
			Map<String,String> mapHost2 = new LinkedHashMap<String,String>();
			mapHost2.put(SSHInterfaceArray.hostName,host2);
			mapHost2.put(SSHInterfaceArray.port,"22");
			
			logger.info("add dev3.local.net...");
			
			assertTrue("Fail to add dev3.local.net server",arr.addKnownStore(mapHost2) == null);
			assertTrue("List should still contain dev3.local.net",
					checkInStore(arr,host1));
			assertTrue("List should contain dev.local.net", 
					checkInStore(arr,host2)
					);
			
			Map<String,String> removeHost2 = new LinkedHashMap<String,String>();
			removeHost2.put(SSHInterfaceArray.hostName, host2);
			
			logger.info("remove dev3.local.net...");
			assertTrue("Fail to remove dev.local.net", arr.removeKnownStore(removeHost2) == null);
			
			assertTrue("List should still contain dev3.local.net",
					checkInStore(arr,host1)
					);
			
			assertTrue("List should not contain dev3.local.net anymore", 
					!checkInStore(arr,host2));
			
			
			logger.info("remove localhost...");
			arr.initKnownStores();
			Map<String,DataStore> l = arr.getStores();
			assertTrue(l.size() == 1);
			l.get(host1).close();
			
			Map<String,String> removeHost = new LinkedHashMap<String,String>();
			removeHost.put(SSHInterfaceArray.hostName, host1);
			
			assertTrue("Fail to remove dev3.local.net",arr.removeKnownStore(removeHost) == null);
			assertTrue("List should be empty",arr.getKnownStoreDetails().isEmpty());
		}catch(Exception e){
			logger.error("Exception "+e.getMessage(),e);
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
