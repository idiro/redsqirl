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
