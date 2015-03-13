package idiro.workflow.server.interaction;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.redsqirl.workflow.server.AppendListInteraction;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.interfaces.DFEOutput;

public class DemoTable extends TableInteraction {
	//HDFS interface used in the update
	private HDFSInterface hInt;

	public DemoTable(String id, String name, String legend, int column,
			int placeInColumn) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		//initialize the tree
		if (tree.isEmpty()) {
			tree.add("table").add("columns");
		}
		//initialize the hdfs
		hInt = new HDFSInterface();
	}
	
	
	//update the table takes the input ,the list interacion and the append list
	public void update(DFEOutput in, ListInteraction list,
			AppendListInteraction append) throws RemoteException {
		if (list != null && list.getValue() != null) {
			//create a column from the value of the list interaction
			addColumn(list.getValue(), null, null, null);
		}
		if (append.getValues() != null && !append.getValues().isEmpty()) {
			Iterator<String> vals = append.getValues().iterator();
			while (vals.hasNext()) {
				//create a column from the values of the append list interaction
				addColumn(vals.next(), null, null, null);
			}
		}
		//get the input path
		String path = in.getPath();
		//get the input feats
		Iterator<String> feats = in.getFields().getFieldNames().iterator();
		//a map to hold feats and positions
		Map<String, Integer> featsAndPos = new HashMap<String, Integer>();
		int pos = 0;
		// get the columns that we generated from the list and append list interaction
		List<String> columns = getColumnNames();
		while (feats.hasNext()) {
			String feaName = feats.next();
			if(columns.contains(feaName)){
				//add acceptable features and their positions
				featsAndPos.put(feats.next(), pos);
			}
			++pos;
		}
		//list of maps that are used for rows
		List<Map<String, String>> newRows = new LinkedList<Map<String, String>>();
		//get the delimiter for the input
		String delimiter = in.getProperties().get(MapRedTextType.key_delimiter);

		//get the data from the input as a list of the rows
		Iterator<String> result = hInt.select(path, delimiter, 10).iterator();
		Map<String, String> row = null;
		while (result.hasNext()) {
			String entireRow = result.next();
			if (entireRow != null & !entireRow.isEmpty()) {
				//split the row on the delimiter
				String[] rowvals = entireRow.split(delimiter);
				row = new HashMap<String, String>();
				Iterator<String> keysIt = featsAndPos.keySet().iterator();
				while (keysIt.hasNext()) {
					String key = keysIt.next();
					//add a cell associated with the current acceptable column/input feature
					row.put(key, rowvals[featsAndPos.get(key)]);
				}
			}
			//add o to the list of rows
			newRows.add(row);
			//reset the row
			row = null;
		}
		//add the rows generated to he interaction under the title 'display'
		updateGenerator("display", newRows);

	}

}
