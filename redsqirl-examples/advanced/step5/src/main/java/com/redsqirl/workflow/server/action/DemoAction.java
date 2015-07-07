package com.redsqirl.workflow.server.action;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.action.PigTypeConvert;
import com.redsqirl.workflow.server.datatype.MapRedCompressedType;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.oozie.PigAction;

public abstract class DemoAction extends DataflowAction {

	
	private static Logger logger = Logger.getLogger(DemoAction.class);
	protected InputInteraction delimiterOutputInt;
	protected ListInteraction savetypeOutputInt;
	private Map<String, DFELinkProperty> input;
	
	public final static String key_output = "",
	key_input = "in",
	key_delimiter = "delimiter",
	key_condition = "condition",
	key_outputType = "output_type",
	default_delimiter = "\001",
	key_featureTable = "features";

	private int bagNum;

	public DemoAction() throws RemoteException {
		super(new PigAction());
		init(1,1);

		delimiterOutputInt = new InputInteraction(
				key_delimiter,
				"Pig Delimiter",
				"Set the delimiter character for the  ", 
				0, 0);
		delimiterOutputInt.setRegex("^(#\\d{1,3}|.)?$");
		delimiterOutputInt.setValue("#1");


		savetypeOutputInt = new ListInteraction(
				key_outputType,
				"Pig Output Type",
				"Set the Pig output type of the action", 1, 0);
		savetypeOutputInt.setDisplayRadioButton(true);
		List<String> saveTypePos = new LinkedList<String>();
		saveTypePos.add( new MapRedTextType().getTypeName());
		saveTypePos.add( new MapRedCompressedType().getTypeName());
		savetypeOutputInt.setPossibleValues(saveTypePos);
		savetypeOutputInt.setValue(new MapRedTextType().getTypeName());
	}
	
	public String updateOut() throws RemoteException {
		String error = checkIntegrationUserVariables();
		if(error == null){
			FieldList new_features = getNewFeatures();
			if(output.get(key_output) == null){
				output.put(key_output, new MapRedTextType());
			}
			output.get(key_output).setFields(new_features);
			output.get(key_output).addProperty(MapRedTextType.key_delimiter, delimiterOutputInt.getValue());
		}
		return error;
	}

	protected void init(int nbInMin, int nbInMax) throws RemoteException{
		if(input == null){
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(MapRedCompressedType.class, nbInMin, nbInMax));
			input = in;
		}
	}

	public abstract String getQuery() throws RemoteException;
	public abstract FieldList getNewFeatures() throws RemoteException;
	public abstract FieldList getInFeatures() throws RemoteException;

	public String getRemoveQueryPiece(String out) throws RemoteException {
		logger.debug("create remove...");
		return "rmf " + out;
	}

	public String getLoadQueryPiece(DFEOutput out) throws RemoteException {
		logger.debug("create load...");

		String delimiter = getPigDelimiter(out.getProperty(MapRedTextType.key_delimiter));
		if (delimiter == null) {
			delimiter = "\001";
		}

		String function = getLoadStoreFuncion(out, delimiter);
		String createSelect = "LOAD '" + out.getPath() + "' USING " + function
				+ " as (";

		Iterator<String> it = out.getFields().getFieldNames().iterator();
		logger.info("attribute list size : " + out.getFields().getSize());
		while (it.hasNext()) {
			String e = it.next();
			createSelect += e
					+ ":"
					+ PigTypeConvert.getPigType(out.getFields()
							.getFieldType(e));
			if (it.hasNext()) {
				createSelect += ", ";
			}
		}
		createSelect += ")";

		return createSelect;
	}

	/**
	 *   * Get the delimiter to be used in Pig format
	 *	 * 
	 *	     * @return delimiter
	 *		 */
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

	public String getStoreQueryPiece(DFEOutput out, String relationName)
			throws RemoteException {
		String delimiter = getPigDelimiter(out.getProperty(MapRedTextType.key_delimiter));

		String function = getStoreFunction(delimiter);
		logger.info(function);
		return "STORE " + relationName + " INTO '" + out.getPath() + "' USING "
				+ function + ";";
	}


	public String getStoreFunction(String delimiter) throws RemoteException {
		String type = "";
		String function = "";
		if (delimiter == null || delimiter.equalsIgnoreCase("")) {
			delimiter = "|";
		}
		try {
			type = savetypeOutputInt.getTree().getFirstChild("list")
					.getFirstChild("output").getFirstChild().getHead();
			logger.info("type: " + type);
			function = "PigStorage('" + delimiter + "')";
			logger.info("Storing via " + function);
			return function;
		} catch (Exception e) {
			logger.error("There was an error getting the output type");
		}
		return null;
	}

	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		logger.info("Write queries in file: "+files[0].getAbsolutePath());
		String toWrite = getQuery();
		boolean ok = toWrite != null;
		if(ok){
			logger.info("Content of "+files[0].getName()+": "+toWrite);
			try {
				FileWriter fw = new FileWriter(files[0]);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(toWrite);	
				bw.close();

			} catch (IOException e) {
				ok = false;
				logger.error("Fail to write into the file "+files[0].getAbsolutePath());
			}
		}
		
		return ok;
	}

	private String getLoadStoreFuncion(DFEOutput out, String delimiter)
			throws RemoteException {
		String function = null;
		function = "PigStorage('" + delimiter + "')";
		return function;
	}

	/**
	 * Get the Current Name of the bag
	 * 
	 * @return current Name
	 */
	protected String getCurrentName() {
		return "A" + bagNum;
	}
	protected String getNextName() {
		bagNum++;
		return "A" + bagNum;
	}
	
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

}
