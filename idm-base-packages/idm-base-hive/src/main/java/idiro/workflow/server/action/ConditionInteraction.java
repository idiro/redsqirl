package idiro.workflow.server.action;

import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.action.utils.HiveDictionary;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.datatype.HiveTypeWithWhere;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

/**
 * Interaction for storing/checking HiveQL conditions.
 * 
 * @author etienne
 *
 */
public class ConditionInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6688812502383438930L;


	private HiveElement el;

	public ConditionInteraction(String name, String legend,
			int column, int placeInColumn, HiveElement el, String key_in)
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
					if(! (type = HiveDictionary.getReturnType(
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


	public void update() throws RemoteException{
		Tree<String> output;
		if(tree.getSubTreeList().isEmpty()){
			output = new TreeNonUnique<String>("output");
		}else{
			output = tree.getFirstChild("editor").getFirstChild("output");
			tree.remove("editor");
		}
		Tree<String> base = HiveDictionary.generateEditor(HiveDictionary.createConditionHelpMenu(),
				el.getInFeatures());
		base.add(output);
		tree.add(base);
	}

	public String getQueryPiece() throws RemoteException{
		logger.debug("where...");
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
			where = " WHERE "+where;
		}else if(!whereIn.isEmpty()){
			where = "WHERE "+whereIn;
		}
		return where;
	}

	public String getInputWhere() throws RemoteException{
		String where = "";
		List<DFEOutput> out = el.getDFEInput().get(HiveElement.key_input);
		Iterator<DFEOutput> it = out.iterator();
		while(it.hasNext()){
			DFEOutput cur = it.next();
			String where_loc = cur.getProperty(HiveTypeWithWhere.key_where);
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
	
	public String getInputWhere(String tableName) throws RemoteException{
		String where = "";
		List<DFEOutput> out = el.getDFEInput().get(HiveElement.key_input);
		Iterator<DFEOutput> it = out.iterator();
		HiveInterface hi = new HiveInterface();
		while(it.hasNext() && where.isEmpty()){
			DFEOutput cur = it.next();
			if(hi.getTableAndPartitions(cur.getPath())[0].equalsIgnoreCase(tableName)){
				where = cur.getProperty(HiveTypeWithWhere.key_where);
				if(where == null){
					where = "";
				}
			}
		}
		return where;
	}

}
