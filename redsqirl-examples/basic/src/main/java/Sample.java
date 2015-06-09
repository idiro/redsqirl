import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.action.PigTypeConvert;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.oozie.PigAction;


public class Sample extends DataflowAction{
	
	private static Logger logger = Logger.getLogger(Sample.class);
	
	// Page that holds interactions, title and legend
	private Page page;
	//Map of inputs for the action
	private Map<String, DFELinkProperty> input;
	//interaction that is an input interaction
	public InputInteraction sample;

	public Sample() throws RemoteException {
		//set the oozie action type
		super(new PigAction());
		//check if input is null
		if (input == null) {
			//initialize input map and add an entry
			input = new LinkedHashMap<String, DFELinkProperty>();
			input.put("in", new DataProperty(MapRedTextType.class, 1, 1));
		}
		//add a new page with title and legend and number of columns
		page = addPage("Sample", "Sample a dataset", 1);
		//initialize the interaction with id, title , legend , position in column and column number
		sample = new InputInteraction("samlpeinput", "Sample Interaction",
				"Set the rate to sample the dataset", 0, 1);
		//set the regex for checking the input
		sample.setRegex("[\\-\\+]?[0-9]*(\\.[0-9]+)?");
		//set default value for sample
		sample.setValue("0.7");
		//add interaction to the page
		page.addInteraction(sample);
	}

	@Override
	public String getName() throws RemoteException {
		return "sample";
	}

	@Override
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

	@Override
	public String updateOut() throws RemoteException {
		//check interactions for errors
		String error = checkIntegrationUserVariables();
		if (error == null) {
			//get the input features list
			FieldList new_features = getDFEInput().get("in").get(0)
					.getFields();
			if (output.get("out") == null) {
				//add a new output to the map
				output.put("out", new MapRedTextType());
			}
			//set the features of the output
			output.get("out").setFields(new_features);
			//add a delimiter to the output
			output.get("out").addProperty(MapRedTextType.key_delimiter, "|");
		}
		return error;
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
	}

	public String getQuery() throws RemoteException {
		//Create a query string
		String query = null;
		//check if the input is null
		if (getDFEInput() != null) {
			//get the input for the action
			DFEOutput in = getDFEInput().get("in").get(0);
			// Output
			//get the output for the action
			DFEOutput out = output.values().iterator().next();
			//set the bag for loading the data
			String loader = "READ";
			//set the load query piece method is explaned below
			String load = loader + " = LOAD " + getLoadQueryPiece(in) + ";\n\n";
			//set the sample query piece by getting the value from the sample interaction
			String sampletxt = "SAMPLEDATA" + " = SAMPLE " + loader + " "
					+ sample.getValue() + ";\n";
			//store the results from the sample into the output path 
			String store = "STORE SAMPLEDATA INTO '" + out.getPath()
					+ "' USING PigStorage('|');";
			//build the query string
			if (sampletxt != null || !sampletxt.isEmpty()) {
				query = load;
				query += sampletxt;
				query += store;
			}
		}
		return query;

	}

	public String getLoadQueryPiece(DFEOutput in) throws RemoteException {
		//get the delimiter
		String delimiter = getPigDelimiter(in.getProperty(MapRedTextType.key_delimiter));
		//if delimiter is empty assign it one
		if (delimiter == null) {
			delimiter = "\001";
		}
		//set the pig storage function
		String function = "PigStorage('" + delimiter + "')";
		// set the path to load from with the function
		String createSelect = "'" + in.getPath() + "' USING " + function
				+ " as (";
		// get iterator of features from the input
		Iterator<String> it = in.getFields().getFieldNames().iterator();
		//iterate over each feature and add them to the 'load as piece'
		while (it.hasNext()) {
			String e = it.next();
			// get the pig type of the feature , may not be string or bigint but
			// chararray or int
			createSelect += e
					+ ":"
					+ PigTypeConvert.getPigType(in.getFields()
							.getFieldType(e));
			if (it.hasNext()) {
				createSelect += ", ";
			}
		}
		//close the query piece
		createSelect += ")";
		//return query piece
		return createSelect;    
	}

	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		//get the query
		String toWrite = getQuery();
		//check if the query is null
		boolean ok = toWrite != null;
		if (ok) {
			logger.info("Content of " + files[0].getName() + ": " + toWrite);
			try {
				//open a file writer with the file given
				FileWriter fw = new FileWriter(files[0]);
				//open a buffer writer with the file writer 
				BufferedWriter bw = new BufferedWriter(fw);
				//write the query to file
				bw.write(toWrite);
				//close the file
				bw.close();

			} catch (IOException e) {
				//set the boolean to false
				ok = false;
				logger.error("Fail to write into the file "
						+ files[0].getAbsolutePath());
			}
		}
		return false;
	}
	
	/**
	 * Get the delimiter to be used in Pig format
	 * 
	 * @return delimiter
	 */
	public static String getPigDelimiter(String asciiCode) {
		String result = null;
		if (asciiCode == null || asciiCode.isEmpty()) {
			result = "|";
		} else if (asciiCode.length() == 1) {
			result = asciiCode;
		}else if (asciiCode.startsWith("#")) {
			result = String.valueOf( (char) ((int)Integer.valueOf(asciiCode.substring(1))));
		} 
		
		return result;
	}
}
