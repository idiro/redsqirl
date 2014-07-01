package com.redsqirl.workflow.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.test.SetupEnvironmentTest;
import com.redsqirl.workflow.test.TestUtils;
import com.redsqirl.workflow.utils.PackageManager;

public class PackageManagerTests{

	Logger logger = Logger.getLogger(getClass());

	String user = System.getProperty("user.name");
	
	public File createPackage(String packName) throws IOException{
		File parent = SetupEnvironmentTest.testDirOut;
		File pack = new File(parent,packName);
		File help = new File(pack,"help");
		File images = new File(pack,"images");
		File lib = new File(pack,"lib");
		
		File actions = new File(pack,"actions.txt");
		File propF = new File(pack,"package.properties");
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

		Properties prop = new Properties();
		prop.setProperty("packageName", packName);
		prop.setProperty("version", "0.1-SNAPSHOT");
		prop.store(new FileWriter(propF), "");
		
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
			pkm.removePackage(user, new String[]{packName});
			pkm.removePackage(user, new String[]{packName2});
			pkm.removePackage(user, new String[]{packName3});
			
			logger.debug("Add packages...");
			assertTrue(
					"Fail to add "+packName,
					pkm.addPackage(user, new String[]{pack.getAbsolutePath()}) == null);
			
			assertFalse(
					"Success to add "+packName,
					pkm.addPackage(user, new String[]{pack.getAbsolutePath()}) == null);	

			assertFalse(
					"Success to add "+packName3,
					pkm.addPackage(user, new String[]{pack3.getAbsolutePath()}) == null);
			
			assertTrue(
					"Success to add "+packName2,
					pkm.addPackage(user, new String[]{pack2.getAbsolutePath()}) == null);
			
			logger.debug("Remove packages....");
			assertTrue(
					"Fail to remove "+packName,
					pkm.removePackage(user, new String[]{packName}) == null);
			
			assertFalse(
					"Fail to remove "+packName,
					pkm.removePackage(user, new String[]{packName}) == null);
			
			assertTrue(
					"Fail to remove "+packName2,
					pkm.removePackage(user, new String[]{packName2}) == null);
			
		}catch(Exception e){
			error = "Unexpected exception "+e.getMessage();
			logger.error(error);
			assertTrue(error,false);
		}

	}

}
