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
		return getStartTime(new Date(),executionTime,offset);
	}
	
	public Date getStartTime(Date referenceTime, Date executionTime,int offset) throws RemoteException{
		Date tmpDate = null;
		Calendar cl = new GregorianCalendar(
				TimeZone.getTimeZone(
						WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_oozie_user_timezone)));
		if(unit == null || frequency == 0){
			throw new RemoteException("This coordinator is not periodic please configure it.");
		}
		if(executionTime == null){
			cl.setTime(referenceTime);
			tmpDate = cl.getTime();
		}else{
			tmpDate = executionTime;
			while(true){
				long search = (tmpDate.getTime() - referenceTime.getTime())/60000;
				Date newDate = addToDate(tmpDate, (int)((search < 0 ? 1:-1) * frequency), unit);
				long newSearch = (newDate.getTime() - referenceTime.getTime());
				if(newSearch*search < 1 || search == 0){
					if(newSearch > 1){
						tmpDate  = newDate;
					}
					break;
				}
				tmpDate = newDate;
			}
			cl.setTime(tmpDate);
		}
		if( unit != null && offset > 0){
			tmpDate = addToDate(tmpDate, frequency * offset, unit);
		}
		
		return tmpDate;
	}
	
	public static Date addToDate(Date myDate, int offset, TimeTemplate unit) throws RemoteException{
		Calendar cl = new GregorianCalendar(
				TimeZone.getTimeZone(
						WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_oozie_processing_timezone)));
		cl.setTime(myDate);
		switch(unit){
		case DAY:
			cl.add(Calendar.HOUR, 24 * offset);
			break;
		case HOUR:
			cl.add(Calendar.HOUR, offset);
			break;
		case MINUTE:
			cl.add(Calendar.MINUTE, offset);
			break;
		case MONTH:
			cl.add(Calendar.MONTH, offset);
			break;
		case YEAR:
			cl.add(Calendar.YEAR, offset);
			break;
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
			cl.setTime(addToDate(startDate, frequency * (numberDefaultIteration - offset), unit));
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
	
	public String toString(){
		return "Every "+frequency+" "+unit+". Initial Instance: "+initialInstance;
	}

}
