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

package com.redsqirl.workflow.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
/**
 * Manager for jobs to be launched 
 * @author keith
 *
 */
public interface JobManager extends Remote{
	/**
	 * Kill a job
	 * @param jobId
	 * @throws RemoteException
	 */
	public void kill(String jobId) throws RemoteException;
	
	/**
	 * Kill a coordinator materialized job
	 * @param jobId
	 * @throws RemoteException
	 */
	public void kill(String jobId, String rangeType, String scope) throws RemoteException;
	
	/**
	 * Resume a job that is suspended
	 * @param jobId
	 * @throws RemoteException
	 */
	public void resume(String jobId) throws RemoteException;
	/**
	 * Run a job using specified properties
	 * @param conf
	 * @return Job ID
	 * @throws RemoteException
	 * @throws Exception
	 */
	public String run(Properties conf) throws RemoteException;
	/**
	 * Submit a job with specific properties
	 * @param conf
	 * @return Job ID
	 * @throws RemoteException
	 * @throws Exception
	 */
	public String submit(Properties conf) throws RemoteException;
	/**
	 * Suspend a job
	 * @param jobId
	 * @throws RemoteException
	 * @throws Exception
	 */
	public void suspend(String jobId) throws RemoteException;
	
	/**
	 * Get the Console URL for a job
	 * @param jobId The oozie job id
	 * @return Get URL for the given Oozie job.
	 * @throws RemoteException
	 */
	String getConsoleUrl(String jobId) throws RemoteException;
	
	/**
	 *
	 * @param jobId
	 * @param rerunType
	 * @param scope
	 * @param refresh
	 * @param noCleanup
	 * @throws RemoteException
	 */
	public void reRunCoord(String jobId, String rerunType, String scope, boolean refresh,
			boolean noCleanup) throws RemoteException;

	/**
	 * 
	 * @param jobId
	 * @param coordScope
	 * @param dateScope
	 * @param refresh
	 * @param noCleanup
	 * @throws RemoteException
	 */
	public void reRunBundle(String jobId, String coordScope, String dateScope, boolean refresh, boolean noCleanup) throws RemoteException;
	
	/**
	 * Run a job with specified DataFlow and List of DataFlowElements
	 * @param df
	 * @param list
	 * @param startTime
	 * @param endTime
	 * 
	 * @return Job ID
	 * @throws RemoteException
	 * @throws Exception
	 */
	public String run(DataFlow df, List<RunnableElement> list, Date startTime, Date endTime) throws RemoteException, Exception;
	/**
	 * Get the URL for the Console
	 * @param df
	 * @return URL in String format
	 * @throws RemoteException
	 * @throws Exception
	 */
	public String getConsoleUrl(DataFlow df) throws RemoteException, Exception;
	/**
	 * Get the Console URL specific for a job
	 * @param df
	 * @param e
	 * @return URL String formatted
	 * @throws RemoteException
	 * @throws Exception
	 */
	public String getConsoleElementUrl(DataFlow df,DataFlowElement e) 
			throws RemoteException, Exception;
	/**
	 * Get a List of jobs that are in the Job Manager
	 * @return List of Arrays oj Job IDs
	 * @throws RemoteException
	 * @throws Exception
	 */
	public List<String[]> getJobs() throws RemoteException, Exception;
	/**
	 * Get the URL for the Job Manager
	 * @return URL in String format
	 * @throws RemoteException
	 */
	public String getUrl() throws RemoteException;
	/**
	 * Get the status of an Element from the JobManager
	 * @param df
	 * @param e
	 * @return Status String (SUSPENDED , RUNNING , TERMINATED)
	 * @throws RemoteException
	 * @throws Exception
	 */
	public String getElementStatus(DataFlow df, DataFlowElement e) throws RemoteException, Exception;
	
	/**
	 * Get the job info of an Oozie Bundle job (data pipeline)
	 * @param jobId
	 * @return a JSON with coordinators info
	 * @throws RemoteException
	 */
	public String getBundleJobInfo(String jobId) throws RemoteException;

	/**
	 * Get the number of element to run in this workflow
	 * @param df
	 * @return The number of element to run in this workflow
	 * @throws RemoteException
	 * @throws Exception
	 */
	public int getNbElement(DataFlow df)throws RemoteException, Exception;

	/**
	 * Get the list of actions that are running
	 * @param df
	 * @return The list of actions that are running
	 * @throws RemoteException
	 * @throws Exception
	 */
	public List<String> getElementsRunning(DataFlow df)throws RemoteException, Exception;

	/**
	 * Get the list of actions that are done
	 * @param df
	 * @return The list of actions that are done
	 * @throws RemoteException
	 * @throws Exception
	 */
	public List<String> getElementsDone(DataFlow df)throws RemoteException, Exception;

	/**
	 * Return true if the last dataflowjob exists
	 * @param df
	 * @return True if the last dataflowjob exists
	 * @throws RemoteException
	 */
	public boolean jobExists(DataFlow df) throws RemoteException;
	
}