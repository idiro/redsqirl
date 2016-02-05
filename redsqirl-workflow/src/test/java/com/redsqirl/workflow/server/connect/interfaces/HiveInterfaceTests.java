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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.test.TestUtils;

public class HiveInterfaceTests {

	static Logger logger = Logger.getLogger(HiveInterfaceTests.class);
	public static int resultExists = 0;
	public static int resultExecute = 0;
	public static int resultExecuteQuery = 0;

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

	Map<String, String> getPartition() {
		Map<String, String> ans = new HashMap<String, String>();
		ans.put(HiveInterface.key_columns, "ID STRING, VALUE INT");
		ans.put(HiveInterface.key_partitions, "DT STRING");
		return ans;
	}

	 @Test
	public void basic() {
		TestUtils.logTestTitle("HiveInterfaceTests#basic");
		try {

			HiveInterface hInt = new HiveInterface();
			Map<String, String> columns = getColumns();

			String new_path1 = TestUtils.getTablePath(1);
			hInt.delete(new_path1);

			assertTrue("create " + new_path1,
					hInt.create(new_path1, columns) == null);

			String new_path2 = TestUtils.getTablePath(2);
			hInt.delete(new_path2);
			assertTrue("copy to " + new_path2,
					hInt.copy(new_path1, new_path2) == null);

			assertTrue("copy to " + new_path2,
					hInt.copy(new_path1, new_path2) != null);

			String new_path3 = TestUtils.getTablePath(3);
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
		TestUtils.logTestTitle("HiveInterfaceTests#partitionMgmt");
		try {
			String error = "";
			HiveInterface hInt = new HiveInterface();
			Map<String, String> partition = getPartition();
			Map<String, String> partitions = getPartitions();

			String new_path1 = TestUtils.getTablePath(1);
			hInt.delete(new_path1);

			error = hInt.create(new_path1, partition);
			assertTrue("create " + new_path1 + " , " + error, error == null);
			logger.debug("create 1 : " + new_path1);

			String new_partition = new_path1 + "/DT='20120102'";
			error = hInt.create(new_partition, new HashMap<String, String>());
			assertTrue("create " + new_partition + " , " + error, error == null);
			logger.debug("create 2 : " + new_partition);

			error = hInt.create(new_partition, new HashMap<String, String>());
			assertTrue("create " + new_partition + " , " + error, error != null);
			logger.debug("create 3 : " + new_partition);

			String new_path2 = TestUtils.getTablePath(2);
			hInt.delete(new_path2);

			String new_partitions = new_path2
					+ "/COUNTRY='Ireland'/DT='20120102'";
			error = hInt.create(new_partitions, partitions);
			assertTrue("create " + new_partitions + " , " + error,
					error == null);
			logger.debug("create 4 : " + new_partitions);

			List<String> part = new ArrayList<String>();
			List<String> list = hInt.getPartitions(new_partitions, part);
			assertTrue("partitions empty : " + list.toString(), !list.isEmpty());
			assertTrue("partitions " + list.toString(), list.size() == 2);
			boolean berror = hInt.exists(new_partitions);
			assertTrue("1) exists : " + new_partitions, berror);

			String new_path3 = TestUtils.getTablePath(3);
			new_path3 += "/SIZE=9";
			partition.put(HiveInterface.key_partitions,
					hInt.getTypesPartitons(new_path3));

			error = hInt.create(new_path3, partition);
			assertTrue("create : " + new_path3 + " , " + error, error == null);
			logger.info("path : " + new_path3);
			logger.info("partitions : "
					+ hInt.getPartitions(new_path3, new ArrayList<String>()));
			boolean exists = hInt.exists(new_path3);
			assertTrue("2) exists " + new_path3, exists);

			error = hInt.delete(new_path1);
			assertTrue("delete " + new_path1 + " , " + error, error == null);
			error = hInt.delete(new_path2);
			assertTrue("delete " + new_path2 + " , " + error, error == null);
			
			error = hInt.delete(new_path3);
			assertTrue("delete " + new_path3 + " , " + error, error == null);

		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(e.getMessage(), false);
		}
	}

	 @Test
	public void getTypesFromPathTest() throws RemoteException {
		TestUtils.logTestTitle("HiveInterfaceTests#getTypesFromPathTest");
		HiveInterface hInt = new HiveInterface();
		String new_path1 = TestUtils.getTablePath(1);
		new_path1 += "/COUNTRY='Ireland'/DT='20120102'/PRICE=5.0/SIZE=7";
		// new_path1+="/DT='20120102'";
		logger.info("result : " + hInt.getTypesPartitons(new_path1));
	}

	 @Test
	public void selectPartitionTest() throws SQLException {
		try {
			HiveInterface hInt = new HiveInterface();
			String path_1 = TestUtils.getTablePath(1);
			String part_path = path_1 + "/COUNTRY='Ireland'/DT='20120204'";
			logger.info("execute : " + hInt.getExecute());
			hInt.delete(path_1);
			String error = hInt.create(part_path, getPartitions());
			assertTrue("create error " + error, error == null);
			hInt.goTo(part_path);
			List<String> result = hInt.select("\001", 5);
			logger.info("result : " + result.toString());
			hInt.delete(path_1);
//			WorkflowPrefManager.resetSys();
//			WorkflowPrefManager.resetUser();

		} catch (RemoteException e) {
			e.printStackTrace();
			logger.info("error in this test " + e.getMessage());
		}
	}

	 @Test
	public void getDescriptionTest() throws RemoteException {
		HiveInterface hInt = new HiveInterface();
		String path1 = TestUtils.getTablePath(1);

		logger.info(hInt.delete(path1));
		logger.info(hInt.create(path1 + "/DT='20120102'", getPartition()));
		logger.info(hInt.exists(path1));
		hInt.goTo(path1);
		logger.info(hInt.getPath());
		assertTrue(hInt.getPath() != "/");
		Map<String, Map<String, String>> map = hInt.getChildrenProperties();
		Iterator<String> it = map.keySet().iterator();

		while (it.hasNext()) {
			String key = it.next();
			Map<String, String> desc = map.get(key);
			Iterator<String> it2 = desc.keySet().iterator();
			while (it2.hasNext()) {
				String key2 = it2.next();
				logger.info(key2 + " , " + desc.get(key2));
			}

		}

	}

	 @Test
	public void selectPathPartition() {
		try {
			HiveInterface hInt = new HiveInterface();
			String path = "/keith_part/id=my_id";
			hInt.goTo(path);
			logger.info(hInt.getDescription("keith_part"));
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
			HiveInterface hInt = new HiveInterface();
			String path = "/keith_part/id=my_id";
			// String path = "/keith_part";
			FieldList fl = new OrderedFieldList();
			fl.addField("a", FieldType.INT);
			fl.addField("b", FieldType.INT);
			fl.addField("weight", FieldType.INT);
			fl.addField("id", FieldType.STRING);
			logger.info(hInt.isPathValid(path, fl, true));

		} catch (RemoteException e) {
			e.printStackTrace();
			assertTrue("error : " + e.getMessage(), false);
		}

	}

	@Test
	public void deleteTest() {
		try {
			HiveInterface hInt = new HiveInterface();
			String path = TestUtils.getTablePath(1);
			String pathPart = path + "/DT='20100204'";
			String error = "";
			error = hInt.delete(path);
			logger.info("error delete "+error);
			error = hInt.create(pathPart, getPartition());
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

		HiveInterface hInt = new HiveInterface();
		String path1 = TestUtils.getTablePath(25);

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

		boolean ok = false;
		int executeVal = HiveInterface.getExecute();
		int doARefreshcount = HiveInterface.getDoARefreshcount();
		if (executeVal == 0 && doARefreshcount == 0) {
			ok = true;
		}
		assertTrue("result was not equal to size for exist " + resultExists
				+ ", " + size, resultExists == size);
		assertTrue("result was not equal to size for execute " + resultExecute
				+ ", " + size, resultExecute == size);
		assertTrue("result was not equal to size for executeQuery "
				+ resultExecuteQuery + ", " + size, resultExecuteQuery == size);
		assertTrue("HiveInterface Execute and doARefresh not empty : "
				+ executeVal + " , " + doARefreshcount, ok);
	}

	public class HiveThreadexist implements Runnable {
		String path;
		HiveInterface hInt;

		public HiveThreadexist(String p) {
			path = p;
		}

		@Override
		public void run() {
			try {
				hInt = new HiveInterface();
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
				if (HiveInterface.execute(query)) {
					++resultExecute;
				}
			} catch (SQLException e) {
				e.printStackTrace();
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
				if (HiveInterface.executeQuery(query) != null) {
					++resultExecuteQuery;
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}
}
