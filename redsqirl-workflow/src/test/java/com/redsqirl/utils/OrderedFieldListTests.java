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

package com.redsqirl.utils;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.test.TestUtils;

public class OrderedFieldListTests{

	Logger logger = Logger.getLogger(getClass());

	private List<Object[]> list;
	
	@Before
	public void init(){
		list = new ArrayList<Object[]>();
		list.add(new Object[]{"A", FieldType.BOOLEAN});
		list.add(new Object[]{"B", FieldType.INT});
		list.add(new Object[]{"C", FieldType.STRING});
	}

	@Test
	public void basic(){
		TestUtils.logTestTitle("PackageManagerTests#basic");
		String error = null;
		try{
			
			OrderedFieldList fieldList = new OrderedFieldList();
			fieldList.addField((String) list.get(0)[0], (FieldType) list.get(0)[1]);
			fieldList.addField((String) list.get(1)[0], (FieldType) list.get(1)[1]);
			fieldList.addField((String) list.get(2)[0], (FieldType) list.get(2)[1]);
			
			assertTrue("Wrong size", fieldList.getSize() == 3);
			
			int cont = 0;
			for (String fieldName : fieldList.getFieldNames()){
				assertTrue("Wrong order", fieldName.equals(list.get(cont++)[0]));
			}
			
			cont = 0;
			for (Object[] obj : list){
				assertTrue("Field not found", fieldList.containsField((String) obj[0]));
			}
			
			cont = 0;
			for (Object[] obj : list){
				assertTrue("Wrong field type", fieldList.getFieldType((String) obj[0]).equals(obj[1]));
			}
			
		}catch(Exception e){
			error = "Unexpected exception "+e.getMessage();
			logger.error(error);
			assertTrue(error,false);
		}

	}

}
