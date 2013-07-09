package idiro.workflow.server.connect.interfaces;

import static org.junit.Assert.assertTrue;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.test.TestUtils;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

public class HDFSInterfaceTests {

	Logger logger = Logger.getLogger(getClass());


	@Test
	public void basic(){
		TestUtils.logTestTitle("HDFSInterfaceTests#basic");
		try{
			HDFSInterface hInt = new HDFSInterface();
			
			String new_path0 = hInt.getPath()+"/test_idm_0";
			String new_path0_test = new_path0+"/test";
			assertTrue("create "+new_path0_test,
					hInt.create(new_path0_test, new HashMap<String,String>()) == null
					);
			
			String new_path1 = hInt.getPath()+"/test_idm_1"; 
			assertTrue("create "+new_path1,
					hInt.create(new_path1, new HashMap<String,String>()) == null
					);
			
			String new_path2 = hInt.getPath()+"/test_idm_2";
			assertTrue("copy to "+new_path2,
					hInt.copy(new_path1, new_path2) == null);
			
			String new_path3 = hInt.getPath()+"/test_idm_3";
			assertTrue("move to "+new_path3,
					hInt.move(new_path1, new_path3) == null);
			
			hInt.goTo(new_path2);
			assertTrue("getPath",
					hInt.getPath().equals(new_path2));
			
			hInt.goTo(new_path3);
			assertTrue("getPath",
					hInt.getPath().equals(new_path3));
			
			hInt.goPrevious();
			assertTrue("getPath",
					hInt.getPath().equals(new_path2));
			
			assertTrue("delete "+new_path0,
					hInt.delete(new_path0) == null);
			assertTrue("delete "+new_path1,
					hInt.delete(new_path1) != null);
			assertTrue("delete "+new_path2,
					hInt.delete(new_path2) == null);
			assertTrue("delete "+new_path3,
					hInt.delete(new_path3) == null);
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
	
	@Test
	public void chmod(){
		TestUtils.logTestTitle("HDFSInterfaceTests#basic");
		try{
			HDFSInterface hInt = new HDFSInterface();
			String new_path1 = hInt.getPath()+"/test_idm_1"; 
			assertTrue("create "+new_path1,
					hInt.create(new_path1, new HashMap<String,String>()) == null
					);
			
			Map<String,String> chmod = new HashMap<String,String>();
			chmod.put(HDFSInterface.key_permission,"770");
			chmod.put(HDFSInterface.key_recursive,"true");
			assertTrue("chmod "+new_path1,
					hInt.changeProperties(new_path1, chmod) == null
					);
			
			Map<String,String> chgp = new HashMap<String,String>();
			chgp.put(HDFSInterface.key_group,"hadoop");
			chgp.put(HDFSInterface.key_recursive,"true");
			//chgp.put(HDFSInterface.key_owner, "hadoop");
			assertTrue("chown "+new_path1,
					hInt.changeProperties(new_path1, chgp) == null
					);
			
			assertTrue("delete "+new_path1,
					hInt.delete(new_path1) == null);
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
}
