package com.redsqirl.workflow.server.datatype;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.utils.FeatureList;
import com.redsqirl.utils.OrderedFeatureList;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.datatype.HiveTypePartition;
import com.redsqirl.workflow.server.enumeration.FeatureType;
import com.redsqirl.workflow.test.TestUtils;

public class HiveTypePartitionTests {
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

	FeatureList getFeatures() throws RemoteException {
		FeatureList ans = new OrderedFeatureList();
		ans.addFeature("ID", FeatureType.STRING);
		ans.addFeature("VALUE", FeatureType.INT);
		return ans;
	}

	FeatureList getFeaturesWPart() throws RemoteException {
		FeatureList ans = new OrderedFeatureList();
		ans.addFeature("ID", FeatureType.STRING);
		ans.addFeature("VALUE", FeatureType.INT);
		ans.addFeature("COUNTRY", FeatureType.STRING);
		ans.addFeature("DT", FeatureType.STRING);
		return ans;
	}

	String getParts() {
		return "COUNTRY='Ireland',DT='20120201'";
	}

	@Test
	public void getPartitionsFilterTest() {
		TestUtils.logTestTitle("HiveTypeTests#getPartitionsFilterTest");
		try {
			HiveInterface hInt = new HiveInterface();
			HiveTypePartition part = new HiveTypePartition();
			
			String new_path1 = TestUtils.getTablePath(1);
			hInt.delete(new_path1);
			String newPart1 = new_path1+"/"+getParts();
			hInt.create(newPart1, getPartitions());
			hInt.goTo(new_path1);
			Map<String, Map<String, String>> results = hInt.getChildrenProperties();
			logger.info("results "+results.toString());
			int size = results.size();
			assertTrue("test 1 size : "+size , size == 2);
			logger.info("partitions "+hInt.getPartitions(new_path1, new ArrayList<String>()));
			boolean error = hInt.exists(new_path1+"/country=Ireland");
			assertTrue("Error "+error + " "+new_path1+"/country=Ireland",error);
			
			hInt.goTo(new_path1+"/country=Ireland");
			logger.info("current path "+hInt.getPath());
			results = hInt.getChildrenProperties();
			logger.info("results "+results.toString());
			size = results.size();
			assertTrue("test 2 size : "+size , size == 1);
			
			hInt.goTo(new_path1+"/dt=20120201");
			results = hInt.getChildrenProperties();
			logger.info("results "+results.toString());
			size = results.size();
			assertTrue("test 3 size : "+size , size == 1);
					
		} catch (Exception e) {
			logger.error(e.getMessage());
			for (StackTraceElement s : e.getStackTrace()) {
				logger.info(s.getFileName() + " : " + s.getLineNumber() + " : "
						+ s.getMethodName());
			}
			assertTrue(e.getMessage(), false);
		}
	}

}
