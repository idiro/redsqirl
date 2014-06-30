package com.redsqirl.workflow.server.action;

import java.rmi.RemoteException;

import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.utils.PigLanguageManager;

/**
 * Interaction to set the sample size of the dataset 
 * @author keith
 *
 */
public class PigSampleInteraction extends InputInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4482001628403863894L;
	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException
	 */
	public PigSampleInteraction(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
	}
	/**
	 * Check the interaction for errors
	 * @return Error Message
	 * 
	 */
	@Override
	public String check(){
		String error = null;
		try{
			String value = getValue();
			double sample = Double.valueOf(value);
			if(sample < 0 || sample > 1){
				error = PigLanguageManager.getText("pig.sample_interaction.not_inrange");
			}
		}catch(Exception e){
			error = PigLanguageManager.getText("pig.sample_interaction.not_double");
		}
		return error;
	}
	/**
	 * Update the interaction 
	 * @throws RemoteException
	 */
	public void update() throws RemoteException{
		setRegex("[\\-\\+]?[0-9]*(\\.[0-9]+)?");
		setValue("0.7");
	}
	/**
	 * Get the query piece for the interaction
	 * @param relation
	 * @return query
	 * @throws RemoteException
	 */
	public String getQueryPiece(String relation) throws RemoteException{
		String query="";
		if(check()==null){
			query = "SAMPLE "+relation +" "+getValue()+";";
		}
		return query;
	}

}
