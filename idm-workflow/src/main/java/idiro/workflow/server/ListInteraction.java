package idiro.workflow.server;

import idiro.workflow.server.enumeration.DisplayType;

import java.rmi.RemoteException;
import java.util.List;

public class ListInteraction extends UserInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -410867993847056485L;

	public ListInteraction(String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(name, legend, DisplayType.list, column, placeInColumn);
	}
	

	public String getValue(){
		String ans = null;
		if(display == DisplayType.list){
			try{
				ans = getTree().getFirstChild("list").getFirstChild("output").getFirstChild().getHead();
			}catch(Exception e){
				logger.error("Tree structure incorrect");
			}
		}
		return ans;
	}
	
	public List<String> getPossibleValues(){
		return getPossibleValuesFromList();
	}

}
