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
							SavingState.RECORDED);
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

}