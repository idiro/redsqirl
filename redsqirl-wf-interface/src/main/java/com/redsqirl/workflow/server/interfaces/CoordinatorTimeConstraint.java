package com.redsqirl.workflow.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.enumeration.TimeTemplate;

public interface CoordinatorTimeConstraint extends Remote{

	int getFrequency() throws RemoteException;
	
	void setFrequency(int frequency) throws RemoteException;
	
	String getFrequencyStr() throws RemoteException;
	
	TimeTemplate getUnit() throws RemoteException;
	
	void setUnit(TimeTemplate unit) throws RemoteException;
	
	void write(Document doc, Element parent) throws RemoteException;
	
	void read(Element parent) throws RemoteException;
	
	String getOozieFreq() throws RemoteException;
	
	int getFreqInMinutes() throws RemoteException;
	
	Date getInitialInstance() throws RemoteException;
	
	void setInitialInstance(Date initialInstance) throws RemoteException;
	
	Date getStartTime(Date executionTime) throws RemoteException;
	
	Date getDefaultEndTime(Date startDate) throws RemoteException;
}
