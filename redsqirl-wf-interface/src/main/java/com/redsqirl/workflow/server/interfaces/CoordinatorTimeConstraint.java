package com.redsqirl.workflow.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface CoordinatorTimeConstraint extends Remote{

	int getFrequency() throws RemoteException;
	
	void setFrequency(int frequency) throws RemoteException;
	
	String getFrequencyStr() throws RemoteException;
	
	String getUnit() throws RemoteException;
	
	void setUnit(String unit) throws RemoteException;
	
	void write(Document doc, Element parent) throws RemoteException;
	
	void read(Element parent) throws RemoteException;
}
