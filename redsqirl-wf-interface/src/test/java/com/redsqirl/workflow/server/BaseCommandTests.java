package com.redsqirl.workflow.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.idiro.ProjectID;
import com.redsqirl.workflow.utils.PackageManager;

public class BaseCommandTests {
	
	private Logger logger = Logger.getLogger(getClass());
	
	@Test
	public void baseCommandCheck() throws FileNotFoundException, IOException{
		String user = System.getProperty("user.name");
		logger.info(WorkflowPrefManager.getPathUserPackagePref(user));
		PackageManager pm = new PackageManager();
		String pathUserLib = WorkflowPrefManager.getPathUserPackagePref(user);
		String pathSysPackage = WorkflowPrefManager.getPathsyspackagepref();
		logger.info(pathSysPackage);
		File path = new File(pathUserLib);
		File[] list = path.listFiles();
		List<String> packages = new ArrayList<String>();
		for (File f : list){
			String propertyPath = f.getAbsolutePath();
			String pck = pm.getPackageProperties(propertyPath)
					.getProperty(PackageManager.property_name)
					+"-"+ pm.getPackageProperties(propertyPath).getProperty(
							PackageManager.property_version);
			pck = pck.replaceAll("[^A-Za-z0-9 ]", "").toLowerCase();
//			logger.info(pck);
			packages.add(pck);
			
		}
		
		Properties properties = new Properties();
		File licenseFile = new File(WorkflowPrefManager.getPathSystemLicence());
		properties.load(new FileInputStream(licenseFile));
		try{
		BaseCommand bc = new BaseCommand();
		logger.info(bc.getPackageClasspath(WorkflowPrefManager.getPathUserPackagePref(user),
				WorkflowPrefManager.getPathsyspackagepref(), properties, user,"redsqirl01snapshot"));
		} catch (Exception e){
			logger.error("error ",e);
		}
		// pm.getPackageClasspath();
//		BaseCommand.
		
	}

}
