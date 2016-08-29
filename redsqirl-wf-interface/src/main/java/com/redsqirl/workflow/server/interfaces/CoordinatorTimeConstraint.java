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
	 * @return
	 * @throws RemoteException
	 */
	int getFrequency() throws RemoteException;
	
	/**
	 * Set the frequency
	 * @param frequency
	 * @throws RemoteException
	 */
	void setFrequency(int frequency) throws RemoteException;
	
	
	String getFrequencyStr() throws RemoteException;
	
	/**
	 * Get the frequency unit.
	 * Null means it is not a repeatable job. 
	 * @return
	 * @throws RemoteException
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
	 * Get the frequency in an oozie format
	 * @return
	 * @throws RemoteException
	 */
	String getOozieFreq() throws RemoteException;
	
	/**
	 * Get the frequency of the object in minutes
	 * @return
	 * @throws RemoteException
	 */
	int getFreqInMinutes() throws RemoteException;
	
	/**
	 * Get the initial instance of the dataset
	 * @return
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
	 * Get the start time of the coordinator after now. 
	 * @param executionTime
	 * @param offset Number of frequency unit to wait before starting the coordinator
	 * @return
	 * @throws RemoteException
	 */
	Date getStartTime(Date executionTime,int offset) throws RemoteException;
	
	/**
	 * Get the end time of the coordinator.
	 * By default the number of iteration is constant. If a coordinator starts later, the offset will compensate.
	 * @param startDate
	 * @param offset Number of frequency unit to wait before starting the coordinator
	 * @return
	 * @throws RemoteException
	 */
	Date getDefaultEndTime(Date startDate,int offset) throws RemoteException;
}
