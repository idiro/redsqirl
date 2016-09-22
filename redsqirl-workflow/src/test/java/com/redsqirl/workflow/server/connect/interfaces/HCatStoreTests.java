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

package com.redsqirl.workflow.server.connect.interfaces;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.connect.hcat.HCatStore;
import com.redsqirl.workflow.server.connect.jdbc.JdbcStore;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.enumeration.PathType;
import com.redsqirl.workflow.test.TestUtils;

public class HCatStoreTests {

	static Logger logger = Logger.getLogger(HCatStoreTests.class);
	public static int resultExists = 0;
	public static int resultExecute = 0;
	public static int resultExecuteQuery = 0;


	String getColumns() {
		return "ID STRING, VALUE INT";
	}
	
	String getPartitions() {
		return "COUNTRY STRING, DT STRING";
	}

	String getPartition() {
		return "DT STRING";
	}

	
	 @Test
	public void basic() {
		TestUtils.logTestTitle("HCatStoreTests#basic");
		try {

			HCatStore hInt = new HCatStore();

			String new_path1 = "/"+TestUtils.getName(1);
			hInt.delete(new_path1);

			assertTrue("create " + new_path1,
					hInt.createTable(HCatStore.getDatabaseTableAndPartition(new_path1)[1], getColumns())== null);

			String new_path2 = "/"+TestUtils.getName(2);
			hInt.delete(new_path2);
			assertTrue("copy to " + new_path2,
					hInt.copy(new_path1, new_path2) == null);

			assertTrue("copy to " + new_path2,
					hInt.copy(new_path1, new_path2) != null);

			String new_path3 = "/"+TestUtils.getName(3);
			hInt.delete(new_path3);
			assertTrue("move to " + new_path3,
					hInt.move(new_path1, new_path3) == null);

			hInt.goTo(new_path2);
			assertTrue("getPath", hInt.getPath().equals(new_path2));

			hInt.goTo(new_path3);
			assertTrue("getPath", hInt.getPath().equals(new_path3));

			hInt.goPrevious();
			assertTrue("getPath", hInt.getPath().equals(new_path2));

			assertTrue("delete " + new_path2, hInt.delete(new_path2) == null);
			assertTrue("delete " + new_path3, hInt.delete(new_path3) == null);
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(e.getMessage(), false);
		}
	}

	 @Test
	public void partitionMgmt() {
		TestUtils.logTestTitle("HCatStoreTests#partitionMgmt");
		try {
			String error = "";
			HCatStore hInt = new HCatStore();
			String partition = getPartition();
			String partitions = getPartitions();

			String new_path1 = "/"+TestUtils.getName(1);
			hInt.delete(new_path1);

			error = hInt.createTable(HCatStore.getDatabaseTableAndPartition(new_path1)[1], getColumns(),partition);
			assertTrue("create " + new_path1 + " , " + error, error == null);
			logger.debug("create 1 : " + new_path1);

			String new_partition = new_path1 + "/DT='20120102'";
			error = hInt.create(new_partition, new HashMap<String, String>());
			assertTrue("create " + new_partition + " , " + error, error == null);
			logger.debug("create 2 : " + new_partition);

			error = hInt.create(new_partition, new HashMap<String, String>());
			assertTrue("create " + new_partition + " , " + error, error != null);
			logger.debug("create 3 : " + new_partition);

			String new_path2 = "/"+TestUtils.getName(2);
			hInt.delete(new_path2);

			error = hInt.createTable(HCatStore.getDatabaseTableAndPartition(new_path2)[1], getColumns(),partitions);
			assertTrue("create " + partitions + " , " + error,
					error == null);
			logger.debug("create 4 : " + partitions);

			List<String> list = Arrays.asList(hInt.getDescription(HCatStore.getDatabaseTableAndPartition(new_path2)).get(JdbcStore.key_partition).split(","));
			assertTrue("partitions empty : " + list.toString(), !list.isEmpty());
			assertTrue("partitions " + list.toString(), list.size() == 2);
			

			error = hInt.delete(new_path1);
			assertTrue("delete " + new_path1 + " , " + error, error == null);
			error = hInt.delete(new_path2);
			assertTrue("delete " + new_path2 + " , " + error, error == null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(e.getMessage(), false);
		}
	}

	 @Test
	public void selectPathPartition() {
		try {
			HCatStore hInt = new HCatStore();
			String path = "/keith_part/id=my_id";
			hInt.goTo(path);
			logger.info(hInt.getDescription(HCatStore.getDatabaseTableAndPartition(path)));
			hInt.select(path, "\001", 1);
		} catch (RemoteException e) {
			logger.error(e,e);
			assertTrue("error : " + e.getMessage(), false);
		} catch (Exception e) {
			logger.error(e,e);
			assertTrue("error : " + e.getMessage(), false);
		}

	}

	@Test
	public void isValidPathTest() {
		try {
			HCatStore hInt = new HCatStore();
			String path = "/keith_part/id=my_id";
			// String path = "/keith_part";
			FieldList fl = new OrderedFieldList();
			fl.addField("a", FieldType.INT);
			fl.addField("b", FieldType.INT);
			fl.addField("weight", FieldType.INT);
			fl.addField("id", FieldType.STRING);
			logger.info(hInt.isPathValid(path, fl,PathType.REAL));

		} catch (RemoteException e) {
			e.printStackTrace();
			assertTrue("error : " + e.getMessage(), false);
		}

	}

	@Test
	public void deleteTest() throws SQLException {
		try {
			HCatStore hInt = new HCatStore();
			String path = "/"+TestUtils.getName(1);
			String pathPart = path + "/DT='20100204'";
			String error = "";
			error = hInt.delete(path);
			logger.info("error delete "+error);
			error = hInt.createTable(HCatStore.getDatabaseTableAndPartition(path)[1], getColumns());
			assertTrue("Error creating " + pathPart + " : " + error,
					error == null || error.isEmpty());
			
			String replacedPath = pathPart.replace("\"", "");
			
			error = hInt.delete(replacedPath);
			assertTrue("Error deleting " + pathPart + " : " + error,
					error == null || error.isEmpty());

		} catch (RemoteException e) {
			assertTrue("Error : " + e.getMessage(), false);
		}

	}

	// @Test
	public void interfaceConcurrency() throws RemoteException {
		TestUtils.logTestTitle("interfaceConcurrency");

		HCatStore hInt = new HCatStore();
		String path1 = "/"+TestUtils.getName(25);

		int size = 15;
		Thread[] exists = new Thread[size];
		Thread[] execute = new Thread[size];
		Thread[] executeQuery = new Thread[size];

		// logger.info("Init count down latch..");
		logger.info("Created Latch no creating arrays");
		for (int i = 0; i < size; ++i) {

			exists[i] = new Thread(new HiveThreadexist(path1));
			execute[i] = new Thread(new HiveThreadExecute("SHOW TABLES"));
			executeQuery[i] = new Thread(new HiveThreadExecuteQuery(
					"SHOW TABLES"));
			exists[i].start();
			execute[i].start();
			executeQuery[i].start();

		}

		logger.info("Latch countdown");

		boolean end = false;
		int count = 0;
		int countMax = 1000;
		while (!end && count < countMax) {
			if (count % 100 == 0) {
				logger.info("await thread : " + count);
			}
			end = true;
			for (int i = 0; i < size; ++i) {
				end &= !(exists[i].isAlive() || executeQuery[i].isAlive() || execute[i]
						.isAlive());
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			++count;
		}
		if (!end) {
			logger.info("wait too long");
			for (int i = 0; i < size; ++i) {
				exists[i].destroy();
				execute[i].destroy();
				executeQuery[i].destroy();
			}
			assertTrue("thread destroyed", false);
		}

		assertTrue("result was not equal to size for exist " + resultExists
				+ ", " + size, resultExists == size);
		assertTrue("result was not equal to size for execute " + resultExecute
				+ ", " + size, resultExecute == size);
		assertTrue("result was not equal to size for executeQuery "
				+ resultExecuteQuery + ", " + size, resultExecuteQuery == size);
	}

	public class HiveThreadexist implements Runnable {
		String path;
		HCatStore hInt;

		public HiveThreadexist(String p) {
			path = p;
		}

		@Override
		public void run() {
			try {
				hInt = new HCatStore();
				if (!hInt.exists(path)) {
					++resultExists;
				}
			} catch (RemoteException e) {
				logger.info("exception");
				e.printStackTrace();
			}
		}

	}

	public class HiveThreadExecute implements Runnable {
		String query;

		public HiveThreadExecute(String p) {
			query = p;
		}

		@Override
		public void run() {
			try {
				if (HCatStore.getHiveConnection().execute(query)) {
					++resultExecute;
				}
			} catch (Exception e) {
				logger.error(e,e);
			}
		}
	}

	public class HiveThreadExecuteQuery implements Runnable {
		String query;

		public HiveThreadExecuteQuery(String p) {
			query = p;
		}

		@Override
		public void run() {
			try {
				if (HCatStore.getHiveConnection().executeQuery(query) != null) {
					++resultExecuteQuery;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
}
