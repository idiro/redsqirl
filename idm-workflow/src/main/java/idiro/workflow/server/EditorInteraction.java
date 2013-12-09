package idiro.workflow.server;

import idiro.workflow.server.enumeration.DisplayType;

import java.rmi.RemoteException;

public class EditorInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -834634281289412942L;

	public EditorInteraction(String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(name, legend, DisplayType.helpTextEditor, column, placeInColumn);
	}
	

	public String getValue(){
		String ans = null;
		if(display == DisplayType.helpTextEditor){
			try{
				ans = getTree().getFirstChild("editor").getFirstChild("output").getFirstChild().getHead();
			}catch(Exception e){
				logger.error("Tree structure incorrect");
			}
		}
		return ans;
	}
	
	

}
