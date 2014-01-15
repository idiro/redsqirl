package idiro.workflow.server.action;

import idiro.utils.Tree;
import idiro.workflow.server.EditorInteraction;
import idiro.workflow.server.action.utils.PigDictionary;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Interaction for storing/checking Pig Latin filter condition.
 * 
 * @author marcos
 * 
 */
public class PigFilterInteraction extends EditorInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6688812502383438930L;

	private PigElement el;

	public PigFilterInteraction(int column,
			int placeInColumn, PigElement el)
					throws RemoteException {
		super(PigElement.key_condition, 
				"Please specify the condition of the select", 
				column, 
				placeInColumn);
		this.el = el;
	}

	@Override
	public String check() {
		String msg = null;
		try {

			String condition = getValue();
			if (condition != null && !condition.isEmpty()) {
				logger.debug("Condition: " + condition
						+ " features list size : "
						+ el.getInFeatures().getSize());
				String type = null;
				Set<String> aggregation = null;
				if(el.groupingInt != null){
					aggregation = el.groupingInt.getAggregationFeatures(el.getDFEInput().get(PigElement.key_input).get(0));
					logger.info("aggregation set size : "+ aggregation.size());
				}
				type = PigDictionary.getInstance().getReturnType(
						condition, el.getInFeatures(),aggregation);
				if (!type.equalsIgnoreCase("boolean")) {
					msg = "The condition have to return a boolean not a "
							+ type;
					logger.info(msg);

				}
			}
		} catch (Exception e) {
			msg = "Fail to calculate the type of the conditon "
					+ e.getMessage();
			logger.error(msg);

		}
		return msg;
	}

	public void update() throws RemoteException {

		try {
			String output = getValue();
			tree.remove("editor");

			Tree<String> base = null;
			if (el.groupingInt != null) {
				base = PigDictionary.generateEditor(PigDictionary.getInstance()
						.createGroupSelectHelpMenu(), el.getInFeatures());

			} else {

				base = PigDictionary.generateEditor(PigDictionary.getInstance()
						.createConditionHelpMenu(), el.getInFeatures());
			}
			logger.info("editor ok");
			base.add("output").add(output);
			tree.add(base);
		} catch (Exception ec) {
			logger.info("There was an error updating " + getName());
		}
	}

	public String getQueryPiece(String relationName) throws RemoteException {
		logger.debug("for each...");
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
			where = "FILTER " + relationName + " BY " + where;
		} else if (!whereIn.isEmpty()) {
			where = "FILTER " + relationName + " BY " + whereIn;
		}
		return where;
	}
	public String getQueryPieceGroup(String relationName) throws RemoteException {
		logger.debug("for each...");
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
			where = "FILTER " + relationName + " BY " + where;
		} 
		return where;
	}

	public String getInputWhere() throws RemoteException {
		String where = "";
		List<DFEOutput> out = el.getDFEInput().get(PigElement.key_input);
		Iterator<DFEOutput> it = out.iterator();
		while (it.hasNext()) {
			DFEOutput cur = it.next();
			String where_loc = cur.getProperty("where");
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
}
