package com.redsqirl.workflow.server.interaction;

import java.rmi.RemoteException;

import com.redsqirl.workflow.server.InputInteraction;

public class SampleInteraction extends InputInteraction{

	public SampleInteraction(String id, String name, String legend, int column,
			int placeInColumn) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
	}
	
	public String check(){
		String error = null;
		try{
			String value = getValue();
			if(!value.matches(getRegex())){
				error = "Sample rate is note in range";
			}
		}catch(Exception e){
			error = "An error occured when checking the value";
		}
		return error;
	}
	
	public void update() throws RemoteException{
		setRegex("[\\-\\+]?[0-9]*(\\.[0-9]+)?");
		setValue("0.7");
	}
	
	public String getQueryPiece(String relation) throws RemoteException{
		String query="";
		if(check()==null){
			query = "SAMPLE "+relation +" "+getValue()+";";
		}
		return query;
	}

}
