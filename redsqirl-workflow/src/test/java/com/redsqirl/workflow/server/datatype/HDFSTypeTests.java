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
import com.redsqirl.workflow.test.SetupEnvironmentTest;

public class HDFSTypeTests {

	private Logger logger = Logger.getLogger(getClass());
	
	private Map<String, String> valuesMap= new HashMap<String, String>();
	
	String text_file = "/user/"+System.getProperty("user.name")+"/unit_test/text_file";
	String binary_file = "/user/"+System.getProperty("user.name")+"/unit_test/binary_file4";
	String ctrla_file = "/user/"+System.getProperty("user.name")+"/unit_test/ctrla_text_file";

	@Test
	public void MapRedBinTypeTest() throws RemoteException {
//		HDFSInterface fs = null;
		try {
			
//			fs = new HDFSInterface();
//			fs.copyFromRemote(SetupEnvironmentTest.class.getResource( "/binary_file/part-m-00000" ).getFile(), binary_file, "localhost");
			
			MapRedBinaryType bin = new MapRedBinaryType();
			bin.setPath(binary_file);
			bin.isPathExists();
//			assertTrue(bin.getFields().getSize() == 1);
			
			List<String> firstLine = bin.selectLine(100);
			logger.info(firstLine.toString());
//			assertTrue(firstLine.size() == 1);
//			assertTrue(firstLine.get(0).equals("line"));
			
			List<Map<String, String>> first = bin.select(100);
			logger.info(first.toString());
//			assertTrue(first.size() == 1);
			
			for (Entry<String, String> e : valuesMap.entrySet()){
//				assertTrue(first.get(0).get(e.getKey()).equals(e.getValue()));
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
//			fs.delete(binary_file);
		}
	}
	
//	@Test
	public void MapRedTextTypeTest() throws RemoteException {
		HDFSInterface fs = null;
		try {
			fs = new HDFSInterface();
			PigTestUtils.createStringIntString_text_file(new Path(text_file));
			
			valuesMap= new HashMap<String, String>();
			valuesMap.put("FIELD1", "A");
			valuesMap.put("FIELD2", "1");
			valuesMap.put("FIELD3", "A");
			
			MapRedTextType bin = new MapRedTextType();
			bin.setPath(text_file);
			
			assertTrue(bin.getFields().getSize() == 3);
			
			List<String> firstLine = bin.selectLine(0);
			logger.info(firstLine.toString());
			assertTrue(firstLine.size() == 1);
			assertTrue(firstLine.get(0).equals("A|1|A"));
			
			List<Map<String, String>> first = bin.select(0);
			logger.info(first.toString());
			assertTrue(first.size() == 1);
			
			for (Entry<String, String> e : valuesMap.entrySet()){
				assertTrue(first.get(0).get(e.getKey()).equals(e.getValue()));
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			fs.delete(text_file);
		}
	}
	
//	@Test
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
			
			assertTrue(bin.getFields().getSize() == 3);
			
			List<String> firstLine = bin.selectLine(0);
			logger.info(firstLine.toString());
			assertTrue(firstLine.size() == 1);
			assertTrue(firstLine.get(0).equals("A\0011\001A"));
			
			List<Map<String, String>> first = bin.select(0);
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
