package idiro.workflow.server.connect.interfaces;

import static org.junit.Assert.assertTrue;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.test.TestUtils;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

public class HiveInterfaceTests {

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
			assertTrue("1) exists : "+new_partitions,berror);

			String new_path3 = TestUtils.getTablePath(3);
			new_path3 += "/SIZE=9";
			partition.put(HiveInterface.key_partitions,
					hInt.getTypesPartitons(new_path3));
			
			error = hInt.create(new_path3, partition);
			assertTrue("create : " + new_path3 + " , "+error, error == null);
			logger.info("path : "+new_path3);
			logger.info("partitions : "+hInt.getPartitions(new_path3, new ArrayList<String>()));
			boolean exists = hInt.exists(new_path3);
			assertTrue("2) exists "+new_path3,exists);
			
			error = hInt.delete(new_path1);
			assertTrue("delete " + new_path1 + " , "+error, error == null);
			error = hInt.delete(new_path2);
			assertTrue("delete " + new_path2+ " , "+error, error == null);
			error = hInt.delete(new_path3);
			assertTrue("delete " + new_path3+ " , "+error, error == null);

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
}
