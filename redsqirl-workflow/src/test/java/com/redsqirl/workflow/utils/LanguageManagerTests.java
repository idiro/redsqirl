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

package com.redsqirl.workflow.utils;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.utils.LanguageManagerWF;

public class LanguageManagerTests {

	private Logger logger = Logger.getLogger("LanguageManagerTests");

	@Test
	public void LanguageManagerTest() {
		List<String> vals = new LinkedList<String>();
		vals.add("test");
		vals.add("test1");
		vals.add("test2");
		
		String error = LanguageManagerWF.getText("workflow.cleanProject",
				new Object[] { vals.get(0) , vals.get(1) , vals.get(2) });
		
		error = error + LanguageManagerWF.getText("workflow.cleanProject",
				new Object[] { "", vals.get(1) , vals.get(2) });
		
		logger.info(error);
	}
}
