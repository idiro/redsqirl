package com.redsqirl.workflow.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.interfaces.CoordinatorTimeConstraint;

public class WfCoordTimeConstraint extends UnicastRemoteObject implements CoordinatorTimeConstraint{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9222678978810373856L;
	
	int frequency;
	String frequencyStr;
	String unit;

	protected WfCoordTimeConstraint() throws RemoteException {
		super();
	}
	
	@Override
	public int getFrequency() {
		return frequency;
	}

	@Override
	public String getFrequencyStr() {
		return frequencyStr;
	}
	
	@Override
	public void setFrequency(int frequency) throws RemoteException{
		this.frequency = frequency;
	}

	@Override
	public String getUnit() {
		return unit;
	}
	
	public void setUnit(String unit) throws RemoteException{
		this.unit = unit;
	}

	@Override
	public void write(Document doc, Element parent) throws RemoteException {
		{
			Element freq = doc.createElement("frequency");
			freq.appendChild(doc.createTextNode(Integer.toString(frequency)));
			parent.appendChild(freq);
		}
		{
			Element freqStr = doc.createElement("frequencyStr");
			freqStr.appendChild(doc.createTextNode(frequencyStr));
			parent.appendChild(freqStr);
		}
		{
			Element unitEl = doc.createElement("unit");
			unitEl.appendChild(doc.createTextNode(unit));
			parent.appendChild(unitEl);
		}
	}

	@Override
	public void read(Element parent) throws RemoteException {
		try{
			frequency = Integer.getInteger(parent.getElementsByTagName("frequency").item(0)
					.getChildNodes().item(0).getNodeValue());
		}catch(Exception e){}
		try{
			frequencyStr = parent.getElementsByTagName("frequencyStr").item(0)
					.getChildNodes().item(0).getNodeValue();
		}catch(Exception e){}
		try{
			unit = parent.getElementsByTagName("unit").item(0)
					.getChildNodes().item(0).getNodeValue();
		}catch(Exception e){}
	}

}
