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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.enumeration.TimeTemplate;

/**
 * Time Constraint object used for datasets and jobs.
 * 
 * @author etienne
 *
 */
public interface CoordinatorTimeConstraint extends Remote{
	
	/**
	 * Get the frequency of a job.
	 * The frequency is always related to a unit
	 * @return The number of unit before a new occurence.
	 * @throws RemoteException
	 */
	int getFrequency() throws RemoteException;
	
	/**
	 * Set the frequency
	 * @param frequency
	 * @throws RemoteException
	 */
	void setFrequency(int frequency) throws RemoteException;
	
	/**
	 * Field to set a cron schedule, not working yet.
	 * @return an empty string.
	 * @throws RemoteException
	 */
	String getFrequencyStr() throws RemoteException;
	
	/**
	 * Get the frequency unit.
	 * Null means it is not a repeatable job. 
	 * @return The frequency time unit
	 * @throws RemoteException
	 * @see TimeTemplate
	 */
	TimeTemplate getUnit() throws RemoteException;
	
	/**
	 * Set the frequency unit.
	 * Null means it is not a repeatable job.
	 * @param unit
	 * @throws RemoteException
	 */
	void setUnit(TimeTemplate unit) throws RemoteException;
	
	/**
	 * Write into a xml the coordinator constraint.
	 * @param doc
	 * @param parent
	 * @throws RemoteException
	 */
	void write(Document doc, Element parent) throws RemoteException;
	
	/**
	 * Read into a xml the coordinator constraint.
	 * @param parent
	 * @throws RemoteException
	 */
	void read(Element parent) throws RemoteException;
	
	/**
	 * Get the frequency in an oozie format.
	 * @return Builds the coordinator function out of unit and frequency.
	 * @throws RemoteException
	 * @see #getFrequency()
	 * @see #getUnit()
	 */
	String getOozieFreq() throws RemoteException;
	
	/**
	 * Get the frequency of the object in minutes
	 * @return Translate unit and frequency to an approximate in minute.
	 * @throws RemoteException
	 * @see #getFrequency()
	 * @see #getUnit()
	 */
	int getFreqInMinutes() throws RemoteException;
	
	/**
	 * Field only used for a path. Get the initial instance of the dataset
	 * @return The first instance.
	 * @see com.redsqirl.workflow.server.interfaces.DFEOutput
	 * @throws RemoteException
	 */
	Date getInitialInstance() throws RemoteException;
	
	/**
	 * Set the initial instance of a dataset
	 * @param initialInstance
	 * @throws RemoteException
	 */
	void setInitialInstance(Date initialInstance) throws RemoteException;
	
	
	/**
	 * Get the time after now. 
	 * @param executionTime
	 * @param offset Number of frequency unit to wait before starting the coordinator
	 * @return The closest execution date in the past.
	 * @throws RemoteException
	 */
	Date getTimeAfterReference(Date executionTime,int offset) throws RemoteException;
	
	/**
	 * Get the time after the reference time.
	 * 
	 *  Get the time after the reference time, that execute at the executionTime and with the known periodicity
	 * @param referenceTime
	 * @param executionTime
	 * @param offset
	 * @return The closest execution date before a given date.
	 * @throws RemoteException
	 */
	public Date getTimeAfterReference(Date referenceTime, Date executionTime,int offset) throws RemoteException;
	
	/**
	 * Get the time before the reference time.
	 * 
	 * Get the time before the reference time, that execute at the executionTime and with the known periodicity.
	 * @param referenceTime
	 * @param executionTime
	 * @param offset
	 * @return The closest execution date before a given date.
	 * @throws RemoteException
	 */
	public Date getTimeBeforeReference(Date referenceTime, Date executionTime,int offset) throws RemoteException;
	
	/**
	 * Get the end time of the coordinator.
	 * By default the number of iteration is constant. If a coordinator starts later, the offset will compensate.
	 * @param startDate
	 * @param offset Number of frequency unit to wait before starting the coordinator
	 * @return 10 - offset frequency from the startDate
	 * @throws RemoteException
	 */
	Date getDefaultEndTime(Date startDate,int offset) throws RemoteException;
}
