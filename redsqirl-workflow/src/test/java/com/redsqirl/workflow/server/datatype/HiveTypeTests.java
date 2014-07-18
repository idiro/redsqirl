package com.redsqirl.workflow.server.datatype;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.connect.interfaces.HiveInterfaceTests;
import com.redsqirl.workflow.server.datatype.HiveType;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.test.TestUtils;

public class HiveTypeTests {

	Logger logger = Logger.getLogger(getClass());

	Map<String, String> getColumns() {
		Map<String, String> ans = new HashMap<String, String>();
		ans.put(HiveInterface.key_columns, "ID STRING, VALUE INT");
		return ans;
	}

	Map<String, String> getPartitions() {
		Map<String, String> ans = new HashMap<String, String>();
		ans.put(HiveInterface.key_columns, "ID STRING, VALUE INT");
		ans.put(HiveInterface.key_partitions, "COUNTRY STRING, DT STRING");
		return ans;
	}

	FieldList getFields() throws RemoteException {
		FieldList ans = new OrderedFieldList();
		ans.addField("ID", FieldType.STRING);
		ans.addField("VALUE", FieldType.INT);
		return ans;
	}

	FieldList getFieldWPart() throws RemoteException {
		FieldList ans = new OrderedFieldList();
		ans.addField("ID", FieldType.STRING);
		ans.addField("VALUE", FieldType.INT);
		ans.addField("COUNTRY", FieldType.STRING);
		ans.addField("DT", FieldType.STRING);
		return ans;
	}

	String getParts() {
		return "/COUNTRY='Ireland'/DT='20120201'";
	}

	@Test
	public void basic() {
		TestUtils.logTestTitle("HiveTypeTests#basic");
		try {
			HiveInterface hInt = new HiveInterface();
			Map<String, String> columns = getColumns();

			String new_path1 = TestUtils.getTablePath(1);
			hInt.delete(new_path1);
			String path1_part = new_path1 + "/SIZE=1";
			assertTrue("create " + new_path1,
					hInt.create(new_path1, columns) == null);

			logger.info("init Hive type...");
			HiveType ht = new HiveType();
			logger.info("set fields...");
			ht.setFields(getFields());
			logger.info("set path...");
			ht.setPath(new_path1);

			assertTrue("Exists " + new_path1, ht.isPathExists());

			assertTrue("1) Valid " + new_path1, ht.isPathValid() == null);

			assertTrue("Remove " + new_path1, ht.remove() == null);

			assertFalse("Exists " + new_path1, ht.isPathExists());

			String partitions = hInt.getTypesPartitons(path1_part);
			logger.info("Partitions : " + partitions);
			columns.put(HiveInterface.key_partitions, partitions);
			assertTrue("create " + path1_part,
					hInt.create(path1_part, columns) == null);
			ht.setPath(path1_part);
			String error = ht.isPathValid();
			assertTrue("2) Valid " + path1_part, error != null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			for (StackTraceElement s : e.getStackTrace()) {
				logger.info(s.getFileName() + " : " + s.getLineNumber() + " : "
						+ s.getMethodName());
			}
			assertTrue(e.getMessage(), false);
		}
	}

	@Test
	public void partitions() {
		TestUtils.logTestTitle("HiveTypeTests#partitions");
		try {
			HiveInterface hInt = new HiveInterface();

			String new_path1 = TestUtils.getTablePath(1)
					+ getParts();

			hInt.delete(TestUtils.getTablePath(1));
			
			assertTrue("create " + new_path1,
					hInt.create(new_path1, getPartitions()) == null);

			logger.info("init Hive type...");
			HiveType ht = new HiveType();
			logger.info("set fields...");
			ht.setFields(getFields());
			logger.info("set path...");
			ht.setPath(new_path1);

			assertTrue("Exists " + new_path1, ht.isPathExists());

			assertTrue("1) Valid " + new_path1, ht.isPathValid() != null);

			assertTrue("Remove " + new_path1, ht.remove() == null);

			assertFalse("Not Exists Anymore" + new_path1, ht.isPathExists());

			assertTrue("2) Valid " + new_path1, ht.isPathValid() != null);

			assertTrue("Remove " + TestUtils.getTablePath(1),
					hInt.delete(TestUtils.getTablePath(1)) == null);

			assertTrue("Valid " + new_path1, ht.isPathValid() != null);
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(e.getMessage(), false);
		}
	}

}
