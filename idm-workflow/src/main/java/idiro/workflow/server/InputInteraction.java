package idiro.workflow.server;

import idiro.utils.Tree;
import idiro.workflow.server.enumeration.DisplayType;

import java.rmi.RemoteException;

public class InputInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7192633417256406554L;

	public InputInteraction(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, DisplayType.input, column, placeInColumn);
		init();
	}

	protected void init() throws RemoteException{
		Tree<String> input = null;
		if(tree.isEmpty()){
			input = tree.add("input");
			input.add("output");
			input.add("regex");
		}
	}

	public String getValue(){
		String ans = null;
		try{
			if(getTree().getFirstChild("input").getFirstChild("output").getFirstChild() != null){
				ans = getTree().getFirstChild("input").getFirstChild("output").getFirstChild().getHead();
			}
		}catch(Exception e){
			logger.error("Tree structure incorrect");
		}
		return ans;
	}
	
	public String getRegex(){
		String ans = null;
		try{
			if(getTree().getFirstChild("input").getFirstChild("regex").getFirstChild() != null ){
				ans = getTree().getFirstChild("input").getFirstChild("regex").getFirstChild().getHead();
			}
		}catch(Exception e){
			logger.error("Tree structure incorrect");
		}
		return ans;
	}

	public void setValue(String value) throws RemoteException{
		getTree().getFirstChild("input").getFirstChild("output").removeAllChildren();
		getTree().getFirstChild("input").getFirstChild("output").add(value);
	}
	
	public void setRegex(String regex) throws RemoteException{
		getTree().getFirstChild("input").getFirstChild("regex").removeAllChildren();
		getTree().getFirstChild("input").getFirstChild("regex").add(regex);
	}

}
