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

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.test.TestUtils;

public class InputInteractionTests {

	protected Logger logger = Logger.getLogger(getClass());

	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass()+"#basic");
		try{
			InputInteraction in = new InputInteraction(
					"id", "name", "legend", 0, 0);
			assertTrue("get Id ", in.getId().equals("id"));
			assertTrue("get Name ", in.getName().equals("name"));
			assertTrue("get Id ", in.getLegend().equals("legend"));
			
			assertTrue("get Value 1", in.getValue() == null);

			in.setValue("my_value");
			assertTrue("get Value 2", in.getValue().equals("my_value"));
			
			in.setRegex("^(#\\d{1,3}|.)?$");
			
			assertTrue("check 1", in.check() != null);
			
			in.setValue("my");
			assertTrue("get Value 3", in.getValue() == null || !in.getValue().equals("my"));
			
			in.setValue("#1");
			assertTrue("get Value 4", in.getValue().equals("#1"));
		}catch(Exception e){
			logger.error(e);
			assertTrue("Exception thrown: "+e.getMessage(),false);
		}
	}
}
