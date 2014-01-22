package idiro.workflow.server;

import idiro.utils.Tree;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.utils.LanguageManagerWF;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class AppendListInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6059678937933664413L;

	public AppendListInteraction(String name, String legend,
			int column, int placeInColumn)
					throws RemoteException {
		super(name, legend, DisplayType.appendList, column, placeInColumn);
		init();
	}

	protected void init() throws RemoteException{
		Tree<String> list= null;
		if(tree.isEmpty()){
			list = tree.add("applist");
			list.add("values");
			list.add("output");
		}
	}
	
	public void setDisplayCheckBox(boolean checkBox) throws RemoteException{
		if(checkBox){
			if(tree.getFirstChild("applist").getFirstChild("display") != null){
				tree.getFirstChild("applist").getFirstChild("display").removeAllChildren();
				tree.getFirstChild("applist").getFirstChild("display").add("");
			}else{
				tree.getFirstChild("applist").add("display").add("checkbox");
			}
		}else{
			if(tree.getFirstChild("applist").getFirstChild("display") != null){
				tree.getFirstChild("applist").remove("display");
			}
		}
	}

	public List<String> getPossibleValues(){
		return getPossibleValuesFromList();
	}
	
	public void setPossibleValues(List<String> values) throws RemoteException{
		Tree<String> vals = tree.getFirstChild("applist").getFirstChild("values");
		vals.removeAllChildren();
		Iterator<String> it = values.iterator();
		while(it.hasNext()){
			vals.add("value").add(it.next());
		}
	}
	

	public List<String> getValues() throws RemoteException{
		List<String> values = null;
		values = new LinkedList<String>();
		List<Tree<String>> lRow = null;
		Iterator<Tree<String>> rows = null;
		try{
			lRow = getTree()
					.getFirstChild("applist").getFirstChild("output").getChildren("value");
			rows = lRow.iterator();
			while(rows.hasNext()){
				values.add(rows.next().getFirstChild().getHead());
			}
		}catch(Exception e){
			values = null;
			logger.error(LanguageManagerWF.getText("UserInteraction.treeIncorrect"));
		}
		return values;
	}
	
	public String setValues(List<String> values)  throws RemoteException{
		String error = null;
		Tree<String> output =  tree.getFirstChild("applist").getFirstChild("output");
		if(getPossibleValues().containsAll(values)){
			output.removeAllChildren();
			Iterator<String> it = values.iterator();
			while(it.hasNext()){
				output.add("value").add(it.next());
			}
		}else{
			error = LanguageManagerWF.getText("AppendListInteraction.setValues", new Object[]{values.toString()});
		}
		return error;
	}

}
