package idiro.workflow.server.action;

import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.EditorInteraction;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.action.utils.HiveDictionary;
import idiro.workflow.server.datatype.HiveTypePartition;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.utils.HiveLanguageManager;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Interaction for storing/checking HiveQL conditions.
 * 
 * @author etienne
 * 
 */
public class HiveFilterInteraction extends EditorInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6688812502383438930L;

	private HiveElement el;

	public HiveFilterInteraction(int column,
			int placeInColumn, HiveElement el)
			throws RemoteException {
		super(HiveElement.key_condition, HiveLanguageManager
				.getText("hive.filter_interaction.title"), HiveLanguageManager
				.getText("hive.filter_interaction.legend"), column,
				placeInColumn);
		this.el = el;
	}

	@Override
	public String check() {
		String msg = null;
		try {

			String condition = getValue();
			logger.info("condition : "+condition);
			if (condition != null && !condition.isEmpty()) {
				logger.debug("Condition: " + condition
						+ " features list size : "
						+ el.getInFeatures().getSize());
				String type = null;
				Set<String> aggregation = new HashSet<String>();
				if(el.groupingInt != null){
					aggregation = el.groupingInt.getAggregationFeatures(el.getDFEInput().get(HiveElement.key_input).get(0));
					logger.info("aggregation set size : "+ aggregation.size());
				}
				type = HiveDictionary.getInstance().getReturnType(
						condition, el.getInFeatures(),aggregation);
				logger.info("return type : "+type);
				if (!type.equalsIgnoreCase("boolean")) {
					msg = HiveLanguageManager.getText("hive.filter_interaction.checkerror",new String[]{type});
					logger.info(msg);

				}
			}
		} catch (Exception e) {
			msg = HiveLanguageManager.getText("hive.filter_interaction.checkexception");
			logger.error(msg);

		}
		logger.info("the msg : "+msg);
		return msg;
	}

	public void update() throws RemoteException {
		try {
			String output = getValue();
			tree.remove("editor");

			Tree<String> base = HiveDictionary.generateEditor(HiveDictionary.getInstance()
					.createConditionHelpMenu(), el.getInFeatures()).getTree();
//			logger.debug(base);
			tree.add(base.getFirstChild("editor"));
			setValue(output);
			logger.debug("set value");
		} catch (Exception ec) {
			logger.info("There was an error updating " + getName());
			logger.info("error : "+ ec.getMessage());
		}
	}

	public String getQueryPiece() throws RemoteException {
		logger.info("where...");
		String where = "";
		if (getTree().getFirstChild("editor").getFirstChild("output")
				.getSubTreeList().size() > 0) {
			where = getTree().getFirstChild("editor").getFirstChild("output")
					.getFirstChild().getHead();
		}

		String whereIn = getInputWhere();
		if (!where.isEmpty()) {
			if (!whereIn.isEmpty()) {
				where = "(" + where + ") AND (" + whereIn + ")";
			}
			where = " WHERE " + where;
		} else if (!whereIn.isEmpty()) {
			where = "WHERE " + whereIn;
		}
		return where;
	}

	public String getInputWhere() throws RemoteException {
		String where = "";
		List<DFEOutput> out = el.getDFEInput().get(HiveElement.key_input);
		Iterator<DFEOutput> it = out.iterator();
		while (it.hasNext()) {
			DFEOutput cur = it.next();
			String where_loc = cur.getProperty(HiveTypePartition.key_partitions);
			if (where_loc != null) {
				if (where.isEmpty()) {
					where = where_loc;
				} else {
					where = " AND " + where_loc;
				}
			}
		}
		return where;
	}

	public String getInputWhere(String alias) throws RemoteException {
		String where = "";
		DFEOutput out = el.getAliases().get(alias);
		if (out != null) {
			where = out.getProperty(HiveTypePartition.key_partitions);
			if (where == null) {
				where = "";
			}
		}

		return where;
	}

}
