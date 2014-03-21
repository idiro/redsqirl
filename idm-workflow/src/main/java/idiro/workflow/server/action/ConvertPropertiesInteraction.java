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
	/**
	 * Convert Action
	 */
	private Convert cv;
								/**Properties title Key*/
	public static final String table_property_title = "Property",
			/**Title Value Key*/
			table_value_title = "Value";
	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @param cv
	 * @throws RemoteException
	 */
	public ConvertPropertiesInteraction(String id, String name, String legend,
			int column, int placeInColumn, Convert cv)
					throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.cv = cv;
		addColumn(
				table_property_title, 
				null, 
				null, 
				null);
		addColumn(
				table_value_title, 
				1, 
				null, 
				null);
	}

	/**
	 * Update the Interaction
	 * @throws RemoteException
	 */
	public void update() throws RemoteException{
		logger.debug("generate columns of convert properties table");
		
		Set<String> props = null;
		if(cv.getDFEOutput().get(Convert.key_output).getProperties() != null &&
				!cv.getDFEOutput().get(Convert.key_output).getProperties().isEmpty()){
			props = cv.getDFEOutput().get(Convert.key_output).getProperties().keySet();
		}
		updateColumnConstraint(
				table_value_title, 
				null, 
				1, 
				props);
	}
	/**
	 * Get the properties of the Interaction to hold
	 * @return Map of properties
	 * @throws RemoteException
	 */
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

}
