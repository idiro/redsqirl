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

import static org.junit.Assert.*;

import java.util.HashMap;


import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.server.action.AbstractDictionary;

public class AbstractDictionaryTests {
	
	private Logger logger = Logger.getLogger("AbstractDictionaryTests");
	
	@Test
	public void abstractDisctionaryTest(){
		AbstractDictionary dict = new AbstractDictionary() {
			
			@Override
			protected void loadDefaultFunctions() {
				functionsMap = new HashMap<String, String[][]>();
				functionsMap
				.put("conditionalOperator",
							new String[][] {
								new String[] {
										">=",
										"ANY,ANY",
										"BOOLEAN",
										"@function:>=@short:Greater or equal@param:Any value@param:Any value@description:Compare the left value to the right and checks if the left value is greater or equal to the right@example:5>=1 returns TRUE" },
				});
				
			}
			
			@Override
			protected String getNameFile() {
				return "functionsAbsrtact";
			}
		};
		String help = dict.getFunctionsMap().get("conditionalOperator")[0][3];
		logger.info(help);
		String convertHelp = dict.convertStringtoHelp(help);
		logger.info(convertHelp);
		assertTrue(convertHelp.contains("div"));
	}
	
}
