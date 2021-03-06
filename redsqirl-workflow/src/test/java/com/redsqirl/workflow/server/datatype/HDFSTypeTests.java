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

package com.redsqirl.workflow.server.datatype;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.server.connect.HDFSInterface;

public class HDFSTypeTests {

	private Logger logger = Logger.getLogger(getClass());
	private Map<String, String> valuesMap= new HashMap<String, String>();
	String text_file = "/user/"+System.getProperty("user.name")+"/unit_test/text_file.mrtxt/";
	String ctrla_file = "/user/"+System.getProperty("user.name")+"/unit_test/ctrla_text_file";

	@Test
	public void MapRedTextTypeTest() throws RemoteException {
		HDFSInterface fs = null;
		try {
			fs = new HDFSInterface();
			PigTestUtils.createStringIntString_text_file(new Path(text_file));

			valuesMap = new HashMap<String, String>();
			valuesMap.put("FIELD1", "A");
			valuesMap.put("FIELD2", "1");
			valuesMap.put("FIELD3", "A");

			MapRedTextType bin = new MapRedTextType();
			bin.setPath(text_file);

			assertTrue("size should be 3 and is "+bin.getFields().getSize(), bin.getFields().getSize() == 3);

			List<String> firstLine = bin.selectLine(1);
			logger.info("firstLine " + firstLine.toString());
			logger.info("firstLine size " + firstLine.size());

			assertTrue("line size "+firstLine.size(), firstLine.size() == 1);
			assertTrue("line "+firstLine.get(0), firstLine.get(0).equals("A|1|A"));

			List<Map<String, String>> first = bin.select(1);
			logger.info(first.toString());
			assertTrue("first size "+first.size(), first.size() == 1);

			for (Entry<String, String> e : valuesMap.entrySet()){
				assertTrue("check entry of value map "+first.get(0).get(e.getKey()),first.get(0).get(e.getKey()).equals(e.getValue()));
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			fs.delete(text_file);
		}
	}

	String text_file_dir = "/user/"+System.getProperty("user.name")+"/unit_test/text_file.mrtxt/";

	@Test
	public void MapRedTextDirTest() throws RemoteException {
		HDFSInterface fs = null;
		try {
			fs = new HDFSInterface();
			PigTestUtils.createHFDSdir(text_file_dir);
			for(int i = 0 ; i < 10; i++){
				PigTestUtils.writeContent(new Path(text_file_dir),"part-000"+String.valueOf(i),"A"+String.valueOf(i)+"|"+String.valueOf(i)+"|B"+String.valueOf(i)+"\n");
			}

			valuesMap = new HashMap<String, String>();
			valuesMap.put("FIELD1", "A0");
			valuesMap.put("FIELD2", "0");
			valuesMap.put("FIELD3", "B0");

			MapRedTextType bin = new MapRedTextType();
			logger.info("Setting path for file "+text_file_dir);
			bin.setPath(text_file_dir);

			assertTrue("size should be 3 and is "+bin.getFields().getSize() , bin.getFields().getSize() == 3);

			List<String> firstLine = bin.selectLine(1);
			logger.info("firstLine " + firstLine.toString());
			logger.info("firstLine.get(0) " + firstLine.get(0));

			assertTrue("lineLine size "+firstLine.size(),firstLine.size() == 1);
			assertTrue("line "+firstLine.get(0),firstLine.get(0).equals("A0|0|B0"));

			List<Map<String, String>> first = bin.select(1);
			logger.info(first.toString()+" "+first.size());
			assertTrue("line size "+first.size(),first.size() == 1);

			for (Entry<String, String> e : valuesMap.entrySet()){
				assertTrue("check entry of value map "+first.get(0).get(e.getKey()), first.get(0).get(e.getKey()).equals(e.getValue()));
			}

		} catch (Exception e) {
			logger.error("Error",e);
			assertTrue(false);
		} finally {
			fs.delete(text_file_dir);
			//fs.delete(text_file);
		}
	}

	@Test
	public void MapRedCtrlATextTypeTest() throws RemoteException {
		HDFSInterface fs = null;
		try {
			fs = new HDFSInterface();
			PigTestUtils.createStringIntString_ctrl_a_file(new Path(ctrla_file));

			valuesMap= new HashMap<String, String>();
			valuesMap.put("FIELD1", "A");
			valuesMap.put("FIELD2", "1");
			valuesMap.put("FIELD3", "A");

			MapRedCtrlATextType bin = new MapRedCtrlATextType();
			bin.setPath(ctrla_file);

			assertTrue("size should be 3 and is "+bin.getFields().getSize(), bin.getFields().getSize() == 3);

			List<String> firstLine = bin.selectLine(1);
			logger.info(firstLine.toString());
			assertTrue(firstLine.size() == 1);
			assertTrue(firstLine.get(0).equals("A\0011\001A"));

			List<Map<String, String>> first = bin.select(1);
			logger.info(first.toString());
			assertTrue(first.get(0).size() == 3);

			for (Entry<String, String> e : valuesMap.entrySet()){
				assertTrue(first.get(0).get(e.getKey()).equals(e.getValue()));
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			fs.delete(ctrla_file);
		}
	}

}