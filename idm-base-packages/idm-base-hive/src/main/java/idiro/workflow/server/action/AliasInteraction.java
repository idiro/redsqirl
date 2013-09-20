package idiro.workflow.server.action;

import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.enumeration.DisplayType;

import java.rmi.RemoteException;

/**
 * Interaction to save the alias of the output table.
 * @author etienne
 *
 */
public class AliasInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3287280857763805444L;
	private static String regex = "[a-zA-Z_]([A-Za-z0-9_]+)";
	

	public AliasInteraction(String name, String legend,
			int column, int placeInColumn)
					throws RemoteException {
		super(name, legend, DisplayType.helpTextEditor, column, placeInColumn);
	}

	@Override
	public String check(){
		String msg = null;
		try{
			Tree<String> alias = getTree()
					.getFirstChild("editor").getFirstChild("output")
					.getFirstChild();


			if(alias != null){
				if(!alias.getHead().matches(regex)){
					msg = "'"+alias.getHead()+"' is not a valid alias";
				}
			}else{
				msg = "Giving an alias is mandatory";
			}
		}catch(Exception e){
			msg = "Fail to access to the tree "+e.getMessage();
			logger.error(msg);

		}
		return msg;
	}


	public void update() throws RemoteException{
		Tree<String> output;
		if(tree.getSubTreeList().isEmpty()){
			output = new TreeNonUnique<String>("output");
		}else{
			output = tree.getFirstChild("editor").getFirstChild("output");
			tree.remove("editor");
		}
		Tree<String> editor = new TreeNonUnique<String>("editor");
		editor.add("keywords");
		editor.add("help");
		editor.add(output);
		tree.add(editor);
	}
	
	public String getAlias(){
		String alias = null;
		try{
			alias = getTree()
					.getFirstChild("editor").getFirstChild("output")
					.getFirstChild().getHead();
		}catch(Exception e){}
		return alias;
	}
	
	
}
