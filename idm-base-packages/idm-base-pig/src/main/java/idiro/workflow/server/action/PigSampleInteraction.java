package idiro.workflow.server.action;

import java.rmi.RemoteException;

import idiro.workflow.server.InputInteraction;
import idiro.workflow.utils.PigLanguageManager;

public class PigSampleInteraction extends InputInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4482001628403863894L;

	public PigSampleInteraction(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
	}
	
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
