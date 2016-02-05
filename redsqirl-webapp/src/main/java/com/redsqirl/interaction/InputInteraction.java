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

package com.redsqirl.interaction;


import java.rmi.RemoteException;

import com.redsqirl.workflow.server.interfaces.DFEInteraction;

/**
 * 
 * @author etienne
 *
 */
public class InputInteraction extends CanvasModalInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8667811028700533217L;

	/**
	 * The value in the input interaction 
	 */
	private String inputValue;

	/**
	 * The regex value
	 */
	private String inputRegex;

	public InputInteraction(DFEInteraction dfeInter) throws RemoteException {
		super(dfeInter);
	}

	@Override
	public void readInteraction() throws RemoteException {
		try{
			inputValue = inter.getTree().getFirstChild("input").getFirstChild("output").getFirstChild().getHead();
		}catch(Exception e){
			inputValue = "";
		}
		try{
			inputRegex = inter.getTree().getFirstChild("input").getFirstChild("regex").getFirstChild().getHead();
		}catch(Exception e){
			inputRegex = "";
		}
	}

	@Override
	public void writeInteraction() throws RemoteException {
		inter.getTree().getFirstChild("input")
		.getFirstChild("output").removeAllChildren();
		inter.getTree().getFirstChild("input")
		.getFirstChild("output")
		.add(inputValue);

	}

	public void setUnchanged(){
		try {
			unchanged = inputValue.equals(
					inter.getTree().getFirstChild("input")
					.getFirstChild("output")
					.getFirstChild().getHead());
		} catch (Exception e) {
			unchanged = false;
		}
	}

	/**
	 * @return the inputValue
	 */
	public String getInputValue() {
		return inputValue;
	}

	/**
	 * @param inputValue the inputValue to set
	 */
	public void setInputValue(String inputValue) {
		this.inputValue = inputValue;
	}

	/**
	 * @return the inputRegex
	 */
	public String getInputRegex() {
		return inputRegex;
	}

	/**
	 * @param inputRegex the inputRegex to set
	 */
	public void setInputRegex(String inputRegex) {
		this.inputRegex = inputRegex;
	}

}