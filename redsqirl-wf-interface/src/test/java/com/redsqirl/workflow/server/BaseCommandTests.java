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
import com.redsqirl.workflow.utils.RedSqirlPackage;
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
			RedSqirlPackage pckSys = new RedSqirlPackage(f,user);
			String propertyPath = f.getAbsolutePath();
			String pck = pckSys.getPackageProperty(RedSqirlPackage.property_name)
					+"-"
					+pckSys.getPackageProperty(RedSqirlPackage.property_version);
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
