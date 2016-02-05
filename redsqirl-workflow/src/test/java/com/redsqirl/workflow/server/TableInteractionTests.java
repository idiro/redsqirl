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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.test.TestUtils;

public class TableInteractionTests {

	protected Logger logger = Logger.getLogger(getClass());

	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass()+"#basic");
		try{
			TableInteraction ta = new TableInteraction(
					"id", "name", "legend", 0, 0);
			assertTrue("get Id ", ta.getId().equals("id"));
			assertTrue("get Name ", ta.getName().equals("name"));
			assertTrue("get Id ", ta.getLegend().equals("legend"));
			
			ta.addColumn("name", 1, null,null, null);
			
			assertTrue("get Value 1", ta.getValues().isEmpty());
			
			Map<String,String> row = new LinkedHashMap<String,String>();
			row.put("name", "a");
			ta.addRow(row);
			assertTrue("check 1", ta.check() == null);
			assertTrue("get Value 2", ta.getValues().size() == 1);
			ta.addRow(row);
			assertTrue("check 2", ta.check() != null);
			ta.setValues(null);
			row.put("name", "1");
			ta.addRow(row);
			
			assertTrue("check 3", ta.check() == null);
			ta.updateColumnConstraint("name", "[a-z]([a-z0-9_]*)",1, null);
			assertTrue("check 4", ta.check() != null);
			
		}catch(Exception e){
			logger.error(e);
			assertTrue("Exception thrown: "+e.getMessage(),false);
		}
	}
}
