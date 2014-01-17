package idiro.workflow.server.action;

import idiro.workflow.server.TableInteraction;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Specify the relationship between joined tables.
 * The order is important as it will be the same 
 * in the SQL query.
 * 
 * @author etienne
 *
 */
public class ConvertPropertiesInteraction extends TableInteraction{

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
		super(name, legend, column, placeInColumn);
		this.cv = cv;
	}


	public void update() throws RemoteException{
		//Map<String, String> properties = cv.getDFEOutput().get(Convert.key_output).getProperties();
		logger.debug("enter here");
		tree.getFirstChild("table").remove("columns");
		getRootTable();
		//logger.info(tree);
	}

	public Map<String,String> getProperties() throws RemoteException{
		Map<String,String> prop = new LinkedHashMap<String,String>();
		
		List<Map<String,String>> lRow = getValues();
		
		if(lRow != null && !lRow.isEmpty()){
			Iterator<Map<String,String>> rowIt = lRow.iterator();
			while(rowIt.hasNext()){
				Map<String,String> cur = rowIt.next();
				prop.put(cur.get(table_property_title), cur.get(table_value_title));
			}
		}

		return prop;
	}


	protected void getRootTable() throws RemoteException{
		logger.info(4);
		//Table
		addColumn(
				table_property_title, 
				null, 
				null, 
				null);
		Set<String> props = null;
		if(cv.getDFEOutput().get(Convert.key_output).getProperties() != null &&
				!cv.getDFEOutput().get(Convert.key_output).getProperties().isEmpty()){
			props = cv.getDFEOutput().get(Convert.key_output).getProperties().keySet();
		}
		
		addColumn(
				table_value_title, 
				1, 
				props, 
				null);
		
	}



}
