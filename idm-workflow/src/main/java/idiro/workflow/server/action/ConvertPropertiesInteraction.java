package idiro.workflow.server.action;

import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.enumeration.DisplayType;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Specify the relationship between joined tables.
 * The order is important as it will be the same 
 * in the SQL query.
 * 
 * @author etienne
 *
 */
public class ConvertPropertiesInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7384667815452362352L;

	private Convert cv;

	public static final String table_property_title = "Property",
			table_value_title = "Value";

	public ConvertPropertiesInteraction(String name, String legend,
			int column, int placeInColumn, Convert cv)
					throws RemoteException {
		super(name, legend, DisplayType.table, column, placeInColumn);
		this.cv = cv;
	}

	@Override
	public String check() throws RemoteException{
		String msg = null;

		return msg;
	}


	public void update() throws RemoteException{
		//Map<String, String> properties = cv.getDFEOutput().get(Convert.key_output).getProperties();
		logger.info("enter here");
		logger.info(1);
		if(tree.getSubTreeList().isEmpty()){
			logger.info(2);
			tree.add("table").add(getRootTable());		
		}else{
			logger.info(3);
			tree.getFirstChild("table").remove("columns");
			tree.getFirstChild("table").add(getRootTable());
			logger.info(100);
		}
		logger.info(tree);
	}

	public Map<String,String> getProperties() throws RemoteException{
		Map<String,String> prop = new LinkedHashMap<String,String>();
		List<Tree<String>>lRow;
		try{
			lRow = getTree()
					.getFirstChild("table").getChildren("row");
		}catch(Exception e){
			logger.warn("Try to get property when not initialized");
			return prop;
		}

		if(lRow != null && !lRow.isEmpty()){
			Iterator<Tree<String>> rowIt = lRow.iterator();
			while(rowIt.hasNext()){
				Tree<String> cur = rowIt.next();
				String property = cur.getFirstChild(table_property_title).getFirstChild().getHead();
				String value = cur.getFirstChild(table_value_title).getFirstChild().getHead();
				prop.put(property, value);
			}
		}

		return prop;
	}


	protected Tree<String> getRootTable() throws RemoteException{
		logger.info(4);
		//Table
		Tree<String> columns = new TreeNonUnique<String>("columns");
		logger.info(5);
		//Feature name
		Tree<String> propertyCol = columns.add("column");
		propertyCol.add("title").add(table_property_title);
		logger.info(6);
		Tree<String> valueCol = columns.add("column"); 
		valueCol.add("title").add(table_value_title);
		logger.info(7);
		Tree<String> constraintProperty= propertyCol.add("constraint");
		constraintProperty.add("count").add(Integer.toString(1));
		logger.info(8);
		if(cv.getDFEOutput().get(Convert.key_output).getProperties() != null &&
				!cv.getDFEOutput().get(Convert.key_output).getProperties().isEmpty()){
			logger.info(9);
			Tree<String> valsProperties = constraintProperty.add("values");
			logger.info(10);
			Iterator<String> it = cv.getDFEOutput().get(Convert.key_output).getProperties().keySet().iterator();
			logger.info(11);
			while(it.hasNext()){
				logger.info(12);
				valsProperties.add("value").add(it.next());
			}
		}
		logger.info(13);
		return columns;
	}



}
