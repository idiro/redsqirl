package idiro.workflow.server;

import idiro.utils.Tree;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.utils.LanguageManagerWF;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

public class ListInteraction extends UserInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -410867993847056485L;

	public ListInteraction(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, DisplayType.list, column, placeInColumn);
		init();
	}
	
	protected void init() throws RemoteException{
		Tree<String> list= null;
		if(tree.isEmpty()){
			list = tree.add("list");
			list.add("values");
			list.add("output");
		}
	}
	

	public void setDisplayRadioButton(boolean radioButton) throws RemoteException{
		if(radioButton){
			if(tree.getFirstChild("list").getFirstChild("display") != null){
				tree.getFirstChild("list").getFirstChild("display").removeAllChildren();
				tree.getFirstChild("list").getFirstChild("display").add("checkbox");
			}else{
				tree.getFirstChild("list").add("display").add("checkbox");
			}
		}else{
			if(tree.getFirstChild("list").getFirstChild("display") != null){
				tree.getFirstChild("list").remove("display");
			}
		}
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
	
	public void setPossibleValues(List<String> values) throws RemoteException{
		Tree<String> vals = tree.getFirstChild("list").getFirstChild("values");
		vals.removeAllChildren();
		Iterator<String> it = values.iterator();
		while(it.hasNext()){
			vals.add("value").add(it.next());
		}
	}
	
	public String setValue(String value)  throws RemoteException{
		String error = null;
		Tree<String> output =  tree.getFirstChild("list").getFirstChild("output");
		if(getPossibleValues().contains(value)){
			output.removeAllChildren();
			output.add(value);
		}else{
			error = LanguageManagerWF.getText("listinteractions.setvalue");
		}
		return error;
	}

}
