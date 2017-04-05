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

package com.redsqirl.workflow.test;


import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.idiro.Log;
import com.idiro.ProjectID;
import com.idiro.hadoop.NameNodeVar;
import com.redsqirl.utils.OrderedFieldListTests;
import com.redsqirl.utils.TreeTests;
import com.redsqirl.workflow.server.AppendListInteractionTests;
import com.redsqirl.workflow.server.EditorInteractionTests;
import com.redsqirl.workflow.server.InputInteractionTests;
import com.redsqirl.workflow.server.ListInteractionTests;
import com.redsqirl.workflow.server.OozieDagTests;
import com.redsqirl.workflow.server.OozieManagerTests;
import com.redsqirl.workflow.server.TableInteractionTests;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.WorkflowProcessesManagerTests;
import com.redsqirl.workflow.server.WorkflowTests;
import com.redsqirl.workflow.server.action.ActionTests;
import com.redsqirl.workflow.server.action.SendEmailTests;
import com.redsqirl.workflow.server.action.SourceTests;
import com.redsqirl.workflow.server.action.superaction.SubWorkflowTests;
import com.redsqirl.workflow.server.action.superaction.SuperActionTests;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.connect.interfaces.HDFSInterfaceTests;
import com.redsqirl.workflow.server.connect.interfaces.SSHInterfaceArrayTests;
import com.redsqirl.workflow.server.connect.interfaces.SSHInterfaceTests;
import com.redsqirl.workflow.server.connect.interfaces.WorkflowInterfaceTests;
import com.redsqirl.workflow.server.datatype.HDFSTypeTests;
import com.redsqirl.workflow.utils.AbstractDictionaryTests;
import com.redsqirl.workflow.utils.JdbcDictionaryTests;
import com.redsqirl.workflow.utils.PackageManagerTests;


@RunWith(Suite.class)
@SuiteClasses({
//	ActionTests.class,
//	SourceTests.class,
//	WorkflowTests.class,
//	HDFSInterfaceTests.class,
//	SSHInterfaceTests.class,
//	SSHInterfaceArrayTests.class,
//	WorkflowProcessesManagerTests.class,
//	OozieManagerTests.class,
//	OozieDagTests.class,
//	OrderedFieldListTests.class,
//	TreeTests.class,
//	InputInteractionTests.class,
//	AppendListInteractionTests.class,
//	ListInteractionTests.class,
//	EditorInteractionTests.class,
//	TableInteractionTests.class,
//	AbstractDictionaryTests.class,
//	SendEmailTests.class,
//	WorkflowInterfaceTests.class,
//	SubWorkflowTests.class,
	
//	//FIXME To update PackageManagerTests.class,
//	//FIXME SuperActionTests.class,
//	//FIXME Hard-coded properties GenericConfFileTests.class,
	
	//HDFSTypeTests.class,
	//SuperActionTests.class
	//PackageManagerTests.class
	
	
	JdbcDictionaryTests.class
	
})
public class SetupEnvironmentTest {

	static Logger logger = null;
	static public File testDirOut = null;
	static public String pathSaveWorkflow = null;

	@BeforeClass
	public static void init() throws Exception{
		String log4jFile = SetupEnvironmentTest.class.getResource( "/log4j.properties" ).getFile();

		ProjectID.getInstance().setName("IdiroWorkflowServerTest");
		ProjectID.getInstance().setVersion("0.01");
		System.out.println(ProjectID.get());

		Log log = new Log();
		log.put(log4jFile);

		logger = Logger.getLogger(SetupEnvironmentTest.class);
		logger.debug("Log4j initialised");
		
		NameNodeVar.set(WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_namenode));
		NameNodeVar.setJobTracker(WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_jobtracker));
		
		logger.info("Namenode: " + NameNodeVar.get());
		
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}

	@AfterClass
	public static void end(){
		logger.info("Clean testing environment...");
		HDFSInterface hdfsInt = null;
		try {
			hdfsInt = new HDFSInterface();
			for(int i = 0; i < 4; ++i){
				hdfsInt.delete(TestUtils.getPath(i));
			}
		}catch (Exception e) {
			logger.error("something went wrong : " + e.getMessage());
		}

		WorkflowPrefManager.resetSys();
		WorkflowPrefManager.resetUser();
		logger.info(WorkflowPrefManager.pathSysHome);
	}
	
}