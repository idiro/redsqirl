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

import com.redsqirl.workflow.server.EditorInteraction;
import com.redsqirl.workflow.test.TestUtils;

public class EditorInteractionTests {

	protected Logger logger = Logger.getLogger(getClass());

	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass()+"#basic");
		try{
			EditorInteraction ed = new EditorInteraction(
					"id", "name", "legend", 0, 0);
			assertTrue("get Id ", ed.getId().equals("id"));
			assertTrue("get Name ", ed.getName().equals("name"));
			assertTrue("get Id ", ed.getLegend().equals("legend"));
			
			assertTrue("get Value 1", ed.getValue().isEmpty());

			ed.setValue("my_value");
			assertTrue("get Value 2", ed.getValue().equals("my_value"));

		}catch(Exception e){
			logger.error(e);
			assertTrue("Exception thrown: "+e.getMessage(),false);
		}
	}
	
}
