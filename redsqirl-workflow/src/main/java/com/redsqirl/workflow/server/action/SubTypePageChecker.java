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

package com.redsqirl.workflow.server.action;

import java.rmi.RemoteException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DFEPage;
import com.redsqirl.workflow.server.interfaces.PageChecker;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class SubTypePageChecker implements PageChecker{

	/**
	 * 
	 */
	private static final long serialVersionUID = 820602597286132154L;
	private static Logger logger = Logger.getLogger(SubTypePageChecker.class);
	
	private DataflowAction act;
	private String outputName;
	private ListInteraction subTypeInteraction;
	private SavingState savingStateNew = SavingState.RECORDED;
	
	public SubTypePageChecker(DataflowAction act, String outputName, ListInteraction subTypeInteraction){
		this.act = act;
		this.outputName = outputName;
		this.subTypeInteraction = subTypeInteraction;
	}

	@Override
	public String check(DFEPage page) throws RemoteException {
		String error = null;
		try {

			// Get the subtype
			String subtype = subTypeInteraction.getValue();
			logger.debug("output type : " + subtype);

			logger.debug("Getting CheckDirectory output type ");
			DFEOutput outNew = DataOutput.getOutput(subtype);
			Map<String,DFEOutput> output = act.getDFEOutput();
			// Set the instance as output if necessary
			if (outNew != null) {
				if (output.get(outputName) == null
						|| !output.get(outputName).getTypeName()
						.equalsIgnoreCase(subtype)) {
					logger.debug("output set");
					output.put(outputName, (DFEOutput) outNew);
					// Set the Output as RECORDED ALWAYS
					output.get(outputName).setSavingState(
							savingStateNew);
				}
			}

		} catch (Exception e) {
			error = LanguageManagerWF.getText("source.outputnull",
					new Object[] { e.getMessage() });
		}
		return error;
	}
	
	public final ListInteraction getSubTypeInteraction() {
		return subTypeInteraction;
	}

	public final String getOutputName() {
		return outputName;
	}

	public final void setOutputName(String outputName) {
		this.outputName = outputName;
	}

	public final SavingState getSavingStateNew() {
		return savingStateNew;
	}

	public final void setSavingStateNew(SavingState savingStateNew) {
		this.savingStateNew = savingStateNew;
	}

}
