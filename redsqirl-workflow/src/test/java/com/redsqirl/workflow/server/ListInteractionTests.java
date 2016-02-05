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

package com.redsqirl.workflow.server;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.test.TestUtils;

public class ListInteractionTests {


	protected Logger logger = Logger.getLogger(getClass());

	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass()+"#basic");
		try{
			ListInteraction li = new ListInteraction(
					"id", "name", "legend", 0, 0);
			assertTrue("get Id ", li.getId().equals("id"));
			assertTrue("get Name ", li.getName().equals("name"));
			assertTrue("get Id ", li.getLegend().equals("legend"));
			
			assertTrue("get Value 1", li.getValue() == null);
			assertTrue("get Possible values", li.getPossibleValues().isEmpty());
			li.setValue("my_value");
			assertTrue("get Value 2", li.getValue() == null);
			
			List<String> posValues = new LinkedList<String>();
			posValues.add("my_value");
			li.setPossibleValues(posValues);
			li.setValue("my_value");
			assertTrue("get Value 3", li.getValue().equals("my_value"));
			assertTrue("check 1",li.check() == null);
			
			posValues.remove("my_value");
			posValues.add("val");
			li.setPossibleValues(posValues);
			assertTrue("check 2",li.check() != null);
			
			assertTrue("eq possible values",posValues.equals(li.getPossibleValues()));
		}catch(Exception e){
			logger.error(e);
			assertTrue("Exception thrown: "+e.getMessage(),false);
		}
	}
}
