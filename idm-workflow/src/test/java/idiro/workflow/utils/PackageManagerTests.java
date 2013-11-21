package idiro.workflow.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import idiro.workflow.test.SetupEnvironmentTest;
import idiro.workflow.test.TestUtils;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Test;

public class PackageManagerTests{

	Logger logger = Logger.getLogger(getClass());

	public File createPackage(String packName) throws IOException{
		File parent = SetupEnvironmentTest.testDirOut;
		File pack = new File(parent,packName);
		File help = new File(pack,"help");
		File images = new File(pack,"images");
		File lib = new File(pack,"lib");
		
		File actions = new File(pack,"actions.txt");
		File image1 = new File(images,"img_"+packName+".png");
		File help1 = new File(help,"help_"+packName+".html");
		File lib1 = new File(lib,"lib_"+packName+".jar");

		pack.mkdir();
		help.mkdir();
		images.mkdir();
		lib.mkdir();
		actions.createNewFile();
		image1.createNewFile();
		help1.createNewFile();
		lib1.createNewFile();
		return pack;
	}
	
	public File createMixPackage(String packName1,String packName2) throws IOException{
		File parent = SetupEnvironmentTest.testDirOut;
		File pack = new File(parent,packName1);
		File help = new File(pack,"help");
		File images = new File(pack,"images");
		File lib = new File(pack,"lib");
		
		File actions = new File(pack,"actions.txt");
		File image1 = new File(images,"img_"+packName1+".png");
		File help1 = new File(help,"help_"+packName2+".html");
		File lib1 = new File(lib,"lib_"+packName1+".jar");

		pack.mkdir();
		help.mkdir();
		images.mkdir();
		lib.mkdir();
		actions.createNewFile();
		image1.createNewFile();
		help1.createNewFile();
		lib1.createNewFile();
		return pack;
	}


	@Test
	public void basic(){
		TestUtils.logTestTitle("PackageManagerTests#basic");
		String error = null;
		try{
			String packName = "pack1";
			String packName2 = "pack2";
			String packName3 = "pack3";
			File pack2 = createPackage(packName2);
			File pack = createPackage(packName);
			File pack3 = createMixPackage(packName3,packName);
			
			PackageManager pkm = new PackageManager();
			pkm.removePackage(false, new String[]{packName});
			pkm.removePackage(false, new String[]{packName2});
			pkm.removePackage(false, new String[]{packName3});
			
			logger.debug("Add packages...");
			assertTrue(
					"Fail to add "+packName,
					pkm.addPackage(false, new String[]{pack.getAbsolutePath()}));
			
			assertFalse(
					"Success to add "+packName,
					pkm.addPackage(false, new String[]{pack.getAbsolutePath()}));
			

			assertFalse(
					"Success to add "+packName3,
					pkm.addPackage(false, new String[]{pack3.getAbsolutePath()}));
			
			assertTrue(
					"Success to add "+packName2,
					pkm.addPackage(false, new String[]{pack2.getAbsolutePath()}));
			
			logger.debug("Remove packages....");
			assertTrue(
					"Fail to remove "+packName,
					pkm.removePackage(false, new String[]{packName}));
			
			assertFalse(
					"Fail to remove "+packName,
					pkm.removePackage(false, new String[]{packName}));
			
			assertTrue(
					"Fail to remove "+packName2,
					pkm.removePackage(false, new String[]{packName2}));
			
		}catch(Exception e){
			error = "Unexpected exception "+e.getMessage();
			logger.error(error);
			assertTrue(error,false);
		}

	}

}
