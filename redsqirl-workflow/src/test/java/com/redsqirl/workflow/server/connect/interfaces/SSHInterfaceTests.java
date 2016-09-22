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

import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.server.connect.SSHInterface;
import com.redsqirl.workflow.test.TestUtils;

/**
 * To Run these test, we suppose that a RSA key
 * is set up with localhost
 * @author etienne
 *
 */
public class SSHInterfaceTests {
	Logger logger = Logger.getLogger(getClass());


	@Test
	public void basic(){
		TestUtils.logTestTitle("HDFSInterfaceTests#basic");
		try{
			SSHInterface hInt = new SSHInterface("dev3.local.net",22);
			
			logger.debug("We are in: "+hInt.getPath());
			
			String new_path0 = hInt.getPath()+"/"+TestUtils.getName(0);
			assertTrue("create "+new_path0,
					hInt.create(new_path0, new HashMap<String,String>()) == null
					);
			
			String new_path1 = hInt.getPath()+"/"+TestUtils.getName(1); 
			assertTrue("create "+new_path1,
					hInt.create(new_path1, new HashMap<String,String>()) == null
					);
			
			String new_path2 = hInt.getPath()+"/"+TestUtils.getName(2);
			assertTrue("move to "+new_path2,
					hInt.move(new_path1, new_path2) == null);
			
			String new_path3 = new_path2+"/"+TestUtils.getName(3);
			assertTrue("create "+new_path3,
					hInt.create(new_path3, new HashMap<String,String>()) == null
					);
			
			assertTrue("Fail to go to "+new_path2, hInt.goTo(new_path0));
			assertTrue("getPath: "+hInt.getPath(),
					hInt.getPath().equals(new_path0));
			
			assertTrue("Fail to go to "+new_path2, hInt.goTo(new_path2));
			
			assertTrue("getPath: "+hInt.getPath(),
					hInt.getPath().equals(new_path2));
			
			Set<String> child = hInt.getChildrenProperties(true).keySet();
			assertTrue("number of children should be one instead of "+child.toString(),child.size()==1);
			assertTrue("Child is "+new_path3+" instead of "+child.toString(),child.contains(new_path3));
			
			hInt.goPrevious();
			assertTrue("getPath: "+hInt.getPath(),
					hInt.getPath().equals(new_path0));
			
			hInt.goNext();
			assertTrue("getPath: "+hInt.getPath(),
					hInt.getPath().equals(new_path2));
			
			assertTrue("delete "+new_path0,
					hInt.delete(new_path0) == null);
			assertTrue("delete "+new_path2,
					hInt.delete(new_path2) == null);
			
			hInt.close();
		}catch(Exception e){
			logger.error(e,e);
			assertTrue("error : "+e.getMessage(),false);
		}
	}
	
	
	@Test
	public void check_save(){
		TestUtils.logTestTitle("HDFSInterfaceTests#save");
		try{
			logger.info("1");
			SSHInterface.resetKnownHost();
			logger.info("2");
			assertTrue("No host should be there",SSHInterface.getKnownHost().isEmpty());
			logger.info("3");
			assertTrue("Fail to add localhost",SSHInterface.addKnownHost("dev.local.net", 22) == null);
			logger.info("4");
			assertTrue("Does not contains localhost",SSHInterface.getKnownHost().contains("dev.local.net"));
			logger.info("5");
			assertTrue("localhost is normally already inserted",SSHInterface.addKnownHost("dev.local.net", 22) != null);
			logger.info("6");
			assertTrue("Fail to add dev3.local.net server",SSHInterface.addKnownHost("dev3.local.net", 22) == null);
			logger.info("7");
			assertTrue("List should still contain localhost",SSHInterface.getKnownHost().contains("dev.local.net"));
			logger.info("8");
			assertTrue("List should contain dev3.local.net", SSHInterface.getKnownHost().contains("dev3.local.net"));
			logger.info("9");
			assertTrue("Fail to remove dev3.local.net",SSHInterface.removeKnownHost("dev3.local.net") == null);
			logger.info("10");
			assertTrue("List should still contain localhost",SSHInterface.getKnownHost().contains("dev.local.net"));
			logger.info("11");
			assertTrue("List should not contain dev3.local.net anymore", !SSHInterface.getKnownHost().contains("dev3.local.net"));
			logger.info("12");
			
			Set<String> l = SSHInterface.getKnownHost();
			logger.info("13");
			assertTrue(l.size() == 1);
			logger.info("14");
			SSHInterface.getHosts().get("dev.local.net").close();
			logger.info("15");
			assertTrue("Fail to remove localhost",SSHInterface.removeKnownHost("dev.local.net") == null);
			logger.info("16");
			assertTrue("List should be empty",SSHInterface.getKnownHost().isEmpty());
		}catch(Exception e){
			logger.error(e,e);
			assertTrue("error : "+e.getMessage(),false);
		}
		
	}
	
}
