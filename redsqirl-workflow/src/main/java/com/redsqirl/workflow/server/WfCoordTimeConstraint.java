package com.redsqirl.workflow.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.redsqirl.workflow.server.enumeration.TimeTemplate;
import com.redsqirl.workflow.server.interfaces.CoordinatorTimeConstraint;

public class WfCoordTimeConstraint extends UnicastRemoteObject implements CoordinatorTimeConstraint{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9222678978810373856L;
	public static final int numberDefaultIteration = 10;
	
	int frequency;
	String frequencyStr = "";
	TimeTemplate unit = null;
	Date initialInstance = null;

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
	public TimeTemplate getUnit() {
		return unit;
	}
	
	public void setUnit(TimeTemplate unit) throws RemoteException{
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
		try{
			Element unitEl = doc.createElement("unit");
			unitEl.appendChild(doc.createTextNode(unit.toString()));
			parent.appendChild(unitEl);
		}catch(Exception e){}
	}

	@Override
	public void read(Element parent) throws RemoteException {
		try{
			frequency = Integer.valueOf(parent.getElementsByTagName("frequency").item(0)
					.getChildNodes().item(0).getNodeValue());
		}catch(Exception e){}
		try{
			frequencyStr = parent.getElementsByTagName("frequencyStr").item(0)
					.getChildNodes().item(0).getNodeValue();
		}catch(Exception e){}
		try{
			unit = TimeTemplate.valueOf(parent.getElementsByTagName("unit").item(0)
					.getChildNodes().item(0).getNodeValue());
		}catch(Exception e){}
	}
	
	@Override
	public String getOozieFreq() throws RemoteException{
		String ans = null;
		if(unit != null){
			ans = "${coord:"+unit.toString().toLowerCase()+"s("+frequency+")}";
		}
		return ans;
	}
	
	public static final CoordinatorTimeConstraint getMostFrequent(CoordinatorTimeConstraint tc1, CoordinatorTimeConstraint tc2) throws RemoteException{
		return tc2.getFreqInMinutes() == 0 || (tc1.getFreqInMinutes() < tc2.getFreqInMinutes() && tc1.getFreqInMinutes() != 0) ? tc1 : tc2;
	}
	
	public Date getStartTime(Date executionTime,int offset) throws RemoteException{
		Date tmpDate = null;
		Date now = new Date();
		Calendar cl = new GregorianCalendar(
				TimeZone.getTimeZone(
						WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_oozie_user_timezone)));
		if(executionTime == null){
			cl.setTime(now);
			tmpDate = cl.getTime();
		}else{
			tmpDate = executionTime;
			cl.setTime(tmpDate);
			while(true){
				long search = (tmpDate.getTime() - now.getTime())/60000;
				switch(unit){
				case DAY:
					cl.add(Calendar.HOUR, (int)((search < 0 ? 1:-1)* 24 * frequency));
					break;
				case HOUR:
					cl.add(Calendar.HOUR, (int)((search < 0 ? 1:-1) * frequency));
					break;
				case MINUTE:
					cl.add(Calendar.MINUTE, (int)((search < 0 ? 1:-1) * frequency));
					break;
				case MONTH:
					cl.add(Calendar.MONTH, (int)((search < 0 ? 1:-1)* frequency));
					break;
				case YEAR:
					cl.add(Calendar.YEAR, (int)((search < 0 ? 1:-1)* frequency));
					break;

				}
				long newSearch = (cl.getTime().getTime() - now.getTime());
				if(newSearch*search < 1){
					if(newSearch > 1){
						tmpDate  = cl.getTime();
					}
					break;
				}
			}
			cl.setTime(tmpDate);
		}
		if( unit != null && offset > 0){
			switch(unit){
			case DAY:
				cl.add(Calendar.HOUR, 24 * frequency * offset);
				break;
			case HOUR:
				cl.add(Calendar.HOUR, frequency * offset);
				break;
			case MINUTE:
				cl.add(Calendar.MINUTE, frequency * offset);
				break;
			case MONTH:
				cl.add(Calendar.MONTH, frequency * offset);
				break;
			case YEAR:
				cl.add(Calendar.YEAR, frequency * offset);
				break;

			}
		}
		
		return cl.getTime();
	}
	
	public Date getDefaultEndTime(Date startDate,int offset) throws RemoteException{
		Calendar cl = new GregorianCalendar(
				TimeZone.getTimeZone(
						WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_oozie_processing_timezone)));
		cl.setTime(startDate);
		if(unit == null){
			cl.add(Calendar.MINUTE, 3);
		}else{
			switch(unit){
			case DAY:
				cl.add(Calendar.HOUR, 24 * frequency * (numberDefaultIteration - offset));
				break;
			case HOUR:
				cl.add(Calendar.HOUR, frequency * (numberDefaultIteration - offset));
				break;
			case MINUTE:
				cl.add(Calendar.MINUTE, frequency * (numberDefaultIteration - offset));
				break;
			case MONTH:
				cl.add(Calendar.MONTH, frequency * (numberDefaultIteration - offset));
				break;
			case YEAR:
				cl.add(Calendar.YEAR, frequency * (numberDefaultIteration - offset));
				break;
			}
		}
		return cl.getTime();
	}
	
	public int getFreqInMinutes(){
		int ans = 0;
		if(unit != null){
			switch(unit){
			case DAY:
				ans = 1440;
				break;
			case HOUR:
				ans = 60;
				break;
			case MINUTE:
				ans = 1;
				break;
			case MONTH:
				ans = 43200;
				break;
			case YEAR:
				ans = 525600;
				break;
			default:
				break;

			}
		}
		return ans*getFrequency();
	}

	@Override
	public Date getInitialInstance() throws RemoteException {
		return initialInstance;
	}

	@Override
	public void setInitialInstance(Date initialInstance) throws RemoteException {
		this.initialInstance = initialInstance;
	}

}
