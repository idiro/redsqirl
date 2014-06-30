package idiro.workflow.server.connect.interfaces;

import static org.junit.Assert.assertTrue;
import idiro.workflow.test.TestUtils;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.server.connect.HDFSInterface;

public class HDFSInterfaceTests {

	Logger logger = Logger.getLogger(getClass());

	// @Test
	public void basic() {
		TestUtils.logTestTitle("HDFSInterfaceTests#basic");
		try {
			HDFSInterface hInt = new HDFSInterface();

			String new_path0 = hInt.getPath() + TestUtils.getTablePath(0);
			String new_path0_test = new_path0 + "/test";
			assertTrue(
					"create " + new_path0_test,
					hInt.create(new_path0_test, new HashMap<String, String>()) == null);

			String new_path1 = hInt.getPath() + TestUtils.getTablePath(1);
			assertTrue(
					"create " + new_path1,
					hInt.create(new_path1, new HashMap<String, String>()) == null);

			String new_path2 = hInt.getPath() + TestUtils.getTablePath(2);
			assertTrue("copy to " + new_path2,
					hInt.copy(new_path1, new_path2) == null);

			String new_path3 = hInt.getPath() + TestUtils.getTablePath(3);
			assertTrue("move to " + new_path3,
					hInt.move(new_path1, new_path3) == null);

			hInt.goTo(new_path2);
			assertTrue("getPath", hInt.getPath().equals(new_path2));

			hInt.goTo(new_path3);
			assertTrue("getPath", hInt.getPath().equals(new_path3));

			hInt.goPrevious();
			assertTrue("getPath", hInt.getPath().equals(new_path2));

			assertTrue("delete " + new_path0, hInt.delete(new_path0) == null);
			assertTrue("delete " + new_path1, hInt.delete(new_path1) != null);
			assertTrue("delete " + new_path2, hInt.delete(new_path2) == null);
			assertTrue("delete " + new_path3, hInt.delete(new_path3) == null);
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue(e.getMessage(), false);
		}
	}

	@Test
	public void chmod() {
		TestUtils.logTestTitle("HDFSInterfaceTests#chmod");
		try {
			HDFSInterface hInt = new HDFSInterface();
			logger.info("interface made");
			String new_path1 = hInt.getPath() + TestUtils.getTablePath(1);
//			 String new_path1 = "/user/keith/t2.rs";
			assertTrue(
					"create " + new_path1,
					hInt.create(new_path1, new HashMap<String, String>()) == null);

			Map<String, String> chmod = new HashMap<String, String>();
			chmod.put(HDFSInterface.key_permission, "770");
			chmod.put(HDFSInterface.key_recursive, "true");
			assertTrue("chmod " + new_path1,
					hInt.changeProperties(new_path1, chmod) == null);
			logger.info(hInt.getProperties(new_path1));
			chmod.put(HDFSInterface.key_permission, "755");
			chmod.put(HDFSInterface.key_recursive, "true");
			assertTrue("chmod " + new_path1,
					hInt.changeProperties(new_path1, chmod) == null);
			logger.info(hInt.getProperties(new_path1));
			Map<String, String> chgp = new HashMap<String, String>();
			chgp.put(HDFSInterface.key_group, "hadoop");
			chgp.put(HDFSInterface.key_recursive, "true");
			// chgp.put(HDFSInterface.key_owner, "hadoop");
			assertTrue("chown " + new_path1,
					hInt.changeProperties(new_path1, chgp) == null);

			logger.info(hInt.getProperties(new_path1));
			assertTrue("delete " + new_path1, hInt.delete(new_path1) == null);
		} catch (Exception e) {
			logger.error(e.getMessage());
			StackTraceElement[] errs = e.getStackTrace();
			for (StackTraceElement er : errs) {
				logger.error(er.getFileName() + " , " + er.getLineNumber());
			}
			assertTrue(e.getMessage(), false);
		}
	}
	
	@Test
	public void select(){
		TestUtils.logTestTitle("HDFSInterfaceTests#chmod");
		try{
			HDFSInterface hInt = new HDFSInterface();
			
		}catch (Exception e){
			logger.error(e.getMessage());
			StackTraceElement[] errs = e.getStackTrace();
			for (StackTraceElement er : errs) {
				logger.error(er.getFileName() + " , " + er.getLineNumber());
			}
			assertTrue(e.getMessage(), false);
		}
	}
}
