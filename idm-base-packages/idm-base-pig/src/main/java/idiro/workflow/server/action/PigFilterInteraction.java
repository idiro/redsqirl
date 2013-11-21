package idiro.workflow.server.action;

import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.action.utils.PigDictionary;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

/**
 * Interaction for storing/checking Pig Latin filter condition.
 * 
 * @author marcos
 *
 */
public class PigFilterInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6688812502383438930L;


	private PigElement el;

	public PigFilterInteraction(String name, String legend,
			int column, int placeInColumn, PigElement el, String key_in)
					throws RemoteException {
		super(name, legend, DisplayType.helpTextEditor, column, placeInColumn);
		this.el = el;
	}

	@Override
	public String check(){
		String msg = null;
		try{
			Tree<String> cond = getTree()
					.getFirstChild("editor").getFirstChild("output")
					.getFirstChild();


			if(cond != null){
				String condition = cond.getHead();
				if(!condition.isEmpty()){
					logger.debug("Condition: "+ condition);
					String type = null;
					if(! (type = PigDictionary.getInstance().getReturnType(
							condition,
							el.getInFeatures()
							)).equalsIgnoreCase("boolean")){
						msg = "The condition have to return a boolean not a "+type;
						logger.info(msg);
					}
				}
			}
		}catch(Exception e){
			msg = "Fail to calculate the type of the conditon "+e.getMessage();
			logger.error(msg);

		}
		return msg;
	}

	public void update() throws RemoteException {
		try {
			Tree<String> output;
			if (tree.getSubTreeList().isEmpty()) {
				output = new TreeNonUnique<String>("output");
			} else {
				output = tree.getFirstChild("editor").getFirstChild("output");
				tree.remove("editor");
			}
			logger.info("trying to get editor");
			Tree<String> base = PigDictionary.generateEditor(PigDictionary
					.getInstance().createConditionHelpMenu(), el
					.getInFeatures());
			logger.info("editor ok");
			base.add(output);
			tree.add(base);
		} catch (Exception ec) {
			logger.info("There was an error updating " + getName());
		}
	}

	public String getQueryPiece(String relationName) throws RemoteException{
		logger.debug("for each...");
		String where = "";
		if(getTree().getFirstChild("editor")
				.getFirstChild("output").getSubTreeList().size() > 0){
			where = getTree().getFirstChild("editor")
					.getFirstChild("output").getFirstChild().getHead();
		}

		String whereIn = getInputWhere();
		if(!where.isEmpty()){
			if(!whereIn.isEmpty()){
				where = "("+where+") AND ("+whereIn+")";
			}
			where = "FILTER " + relationName + " BY "+where;
		}else if(!whereIn.isEmpty()){
			where = "FILTER " + relationName + " BY "+whereIn;
		}
		return where;
	}

	public String getInputWhere() throws RemoteException{
		String where = "";
		List<DFEOutput> out = el.getDFEInput().get(PigElement.key_input);
		Iterator<DFEOutput> it = out.iterator();
		while(it.hasNext()){
			DFEOutput cur = it.next();
			String where_loc = cur.getProperty("where");
			if(where_loc != null){
				if(where.isEmpty()){
					where = where_loc;
				}else{
					where = " AND "+where_loc;
				}
			}
		}
		return where;
	}
	
//	public String getInputWhere(String relationName) throws RemoteException{
//		String where = "";
//		List<DFEOutput> out = el.getDFEInput().get(PigElement.key_input);
//		Iterator<DFEOutput> it = out.iterator();
//		HDFSInterface hi = new HDFSInterface();
//		while(it.hasNext() && where.isEmpty()){
//			DFEOutput cur = it.next();
//			if(hi.getRelation(cur.getPath()).equalsIgnoreCase(relationName)){
//				where = cur.getProperty("where");
//				if(where == null){
//					where = "";
//				}
//			}
//		}
//		return where;
//	}

}
