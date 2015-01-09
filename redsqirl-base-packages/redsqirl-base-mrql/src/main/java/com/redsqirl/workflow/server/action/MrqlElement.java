package com.redsqirl.workflow.server.action;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.datatype.MapRedCompressedType;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.interaction.MrqlGroupInteraction;
import com.redsqirl.workflow.server.interaction.MrqlOrderInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.oozie.MrqlAction;
import com.redsqirl.workflow.utils.MrqlLanguageManager;

/**
 * Common functionalities for a Mrql action.
 * A Mrql action support as input and output 
 * 
 * @author marcos
 *
 */
public abstract class MrqlElement extends SqlElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1651299366774317959L;
	
	private static Logger logger = Logger.getLogger(MrqlElement.class);
	
								/**
								 * Output Key
								 */
	public static final String key_output = "",
			/**Input Key*/
			key_input = "in",
			/**Delimiter Key*/
			key_delimiter="delimiter",
			/**Condition Key*/
			key_condition = "condition",
			/**Output Type Key*/
			key_outputType = "output_type",
			/**Default Delimiter*/
			default_delimiter = new String(new char[]{'\001'}),
			/**Field Key for table*/
			key_fieldTable = "field",
			/**Order Key*/
			key_order = "order",
			/**Order Type Key*/
			key_order_type = "order_type",
			/**Audit Key */
			key_audit="audit";
			
	
	/**Input Interaction for delimiter*/
	protected InputInteraction delimiterOutputInt;
	/**List Interaction for save output type*/
	protected ListInteraction savetypeOutputInt;
	/**Group Interaction*/
	public MrqlGroupInteraction groupingInt;
	
	/**
	 * Order Interaction
	 */
	protected MrqlOrderInteraction orderInt;
	
	/**
	 * Order Type Interaction
	 */
	protected ListInteraction orderTypeInt;
	
	/**Map of inputs*/
	protected Map<String, DFELinkProperty> input;
	/**
	 * Count of named bags
	 */
	private int nameCont;
	/**
	 * Constructor
	 * @param nbInMin
	 * @param nbInMax
	 * @param placeDelimiterInPage
	 * @throws RemoteException
	 */
	public MrqlElement( int nbInMin, int nbInMax,int placeDelimiterInPage) throws RemoteException {
		super(new MrqlAction());
		init(nbInMin,nbInMax);
		
		orderInt = new MrqlOrderInteraction(
				key_order, 
				MrqlLanguageManager.getText("mrql.order_interaction.title"), 
				MrqlLanguageManager.getText("mrql.order_interaction.legend"), 
				0, 0, this);
		
		orderTypeInt = new ListInteraction(
				key_order_type, 
				MrqlLanguageManager.getText("mrql.order_type_interaction.title"), 
				MrqlLanguageManager.getText("mrql.order_type_interaction.title"), 
				1, 0);
		
		orderTypeInt.setDisplayRadioButton(true);
		List<String> values = new ArrayList<String>();
		values.add("ASCENDING");
		values.add("DESCENDING");
		orderTypeInt.setPossibleValues(values);
		orderTypeInt.setReplaceDisable(true);
		
		delimiterOutputInt = new InputInteraction(
				key_delimiter,
				MrqlLanguageManager.getText("mrql.delimiter_interaction.title"),
				MrqlLanguageManager.getText("mrql.delimiter_interaction.legend"), 
				placeDelimiterInPage+1, 0);
		delimiterOutputInt.setRegex("^(#\\d{1,3}|.)?$");
		delimiterOutputInt.setValue("#1");


		savetypeOutputInt = new ListInteraction(
				key_outputType,
				MrqlLanguageManager.getText("mrql.outputtype_interaction.title"),
				MrqlLanguageManager.getText("mrql.outputtype_interaction.legend"), placeDelimiterInPage+2, 0);
		savetypeOutputInt.setDisplayRadioButton(true);
		List<String> saveTypePos = new LinkedList<String>();
		saveTypePos.add( new MapRedTextType().getTypeName());
		saveTypePos.add( new MapRedCompressedType().getTypeName());
		savetypeOutputInt.setPossibleValues(saveTypePos);
		savetypeOutputInt.setValue(new MapRedCompressedType().getTypeName());
		savetypeOutputInt.setReplaceDisable(true);
	}
	/**
	 * Initialise the element
	 * @param nbInMin
	 * @param nbInMax
	 * @throws RemoteException
	 */
	protected void init(int nbInMin, int nbInMax) throws RemoteException{
		if(input == null){
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(MapRedCompressedType.class, nbInMin, nbInMax));
			input = in;
		}
	}
	
//	public Map<String,List<String>> getDistinctValues() throws RemoteException{
//		Map<String, List<String>> ans = null;
//		List<DataFlowElement> lin = getInputComponent().get(key_input);
//		if(lin != null && lin.size() > 0){
//			if(groupingInt != null){
//				ans = (new AuditGenerator()).readDistinctValuesAudit(getAliases().keySet().iterator().next(),lin.get(0).getDFEOutput().get(key_output_audit));
//			}else{
//				ans = (new AuditGenerator()).readDistinctValuesAudit(null,lin.get(0).getDFEOutput().get(key_output_audit));
//			}
//		}
//		
//		return ans;
//	}
	

	/**
	 * Get the Query for the action
	 * @return query
	 * @throws RemoteException
	 */
	public abstract String getQuery() throws RemoteException;
	/**
	 * Get the Input Field
	 * @return input FieldList
	 * @throws RemoteException
	 */
	public abstract FieldList getInFields() throws RemoteException;
	/**
	 * Get the Input Relations
	 * @return Set of Input relations
	 * @throws RemoteException
	 */
	public Set<String> getInRelations() throws RemoteException{
		Set<String> ans = new LinkedHashSet<String>();
		HDFSInterface hInt = new HDFSInterface();
		List<DFEOutput> lOut = getDFEInput().get(key_input);
		Iterator<DFEOutput> it = lOut.iterator();
		while(it.hasNext()){
			ans.add(hInt.getRelation(it.next().getPath()));
		}
		return ans; 
	}
	/**
	 * Write the Oozie Action Files
	 * @param files
	 * @return <code>true</code> if write the oozie files was ok else <code>false</code>
	 * @throws RemoteException
	 */
	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		logger.info("Write queries in file: " + files[0].getAbsolutePath());
		String toWrite = getQuery();
		boolean ok = toWrite != null;
		if (ok) {

			logger.info("Content of " + files[0].getName() + ": " + toWrite);
			try {
				FileWriter fw = new FileWriter(files[0]);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(toWrite);
				bw.close();

			} catch (IOException e) {
				ok = false;
				logger.error("Fail to write into the file "
						+ files[0].getAbsolutePath());
			}
		}
		
		logger.info("Write properties in file: "+files[1].getName());
		toWrite = "#!/bin/bash" + System.getProperty("line.separator");
		toWrite = "echo \"File: $1 \""  + System.getProperty("line.separator");
		toWrite += "/home/hadoop/mrql-0.9.2-incubating-src/bin/mrql.bsp -nodes 2 -dist $1";

		ok = toWrite != null;
		if(ok){
			try {
				logger.debug("Content of "+files[1]+": "+toWrite);
				FileWriter fw = new FileWriter(files[1]);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(toWrite);	
				bw.close();
			} catch (IOException e) {
				ok = false;
				logger.error("Fail to write into the file "+files[1].getAbsolutePath(),e);
			}
		}
		return ok;
	}
	/**
	 * Get the properties of the input
	 * @param out
	 * @return properties String
	 * @throws RemoteException
	 */
	public String getProperties(DFEOutput out) throws RemoteException{
		String properties = "";

		properties += "number_fields="+out.getFields().getSize()+"\n";

		int cont = 0;
		for (String name : out.getFields().getFieldNames()){
			properties += "field"+cont+"_name="+name+"\n";
			properties += "field"+cont+"_value="+out.getFields().getFieldType(name)+"\n";
		}

		return properties;
	}
	/**
	 * Update the output of the action
	 */
	public String updateOut() throws RemoteException {
		String error = checkIntegrationUserVariables();
		logger.info("Error in updae out : "+error);
		if(error == null){
			FieldList new_field = getNewFields();
			String type = savetypeOutputInt.getValue();
			DFEOutput out = output.get(key_output);
			logger.info("new fields "+new_field.getFieldNames());
			logger.info("output type : "+type);
			
			if(out != null && !type.equalsIgnoreCase(out.getTypeName())){
				output.remove(key_output).clean();
			}
			
			if(output.get(key_output) == null){
				if(type.equalsIgnoreCase(new MapRedTextType().getTypeName())){
					output.put(key_output, new MapRedTextType());
				}else if(type.equalsIgnoreCase(new MapRedCompressedType().getTypeName())){
					output.put(key_output, new MapRedCompressedType());
				}
			}
			
			output.get(key_output).setFields(new_field);
			output.get(key_output).addProperty(MapRedTextType.key_delimiter, delimiterOutputInt.getValue());
		}
		return error;
	}

	/**
	 * Get the input map
	 * @return Map of input
	 * @throws RemoteException
	 */
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}
	
	/**
	 * Get the remove query piece of the query
	 * @param out
	 * @return query
	 * @throws RemoteException
	 */
	public String getRemoveQueryPiece(String out) throws RemoteException{
		logger.debug("create remove...");
		return "rmf "+out;
	}
	/**
	 * Get the load query piece for the query
	 * @param out
	 * @return query
	 * @throws RemoteException
	 */
	public String getLoadQueryPiece(DFEOutput out) throws RemoteException{
		logger.debug("create load...");

		String delimiter = out.getProperty(MapRedTextType.key_delimiter);
		delimiter = ((MapRedTextType)out).getPigDelimiter();
		if (delimiter == null){
			delimiter = default_delimiter;
		}

//		String function = getLoadStoreFuncion(out, delimiter);
		String createSelect = "source(line,'" + out.getPath() + "', '" + delimiter + "',";
		
//		if (function != null){
//			createSelect += " USING "+function;
//		}
		createSelect += " type(<";

		Iterator<String> it = out.getFields().getFieldNames().iterator();
		logger.info("attribute list size : "+out.getFields().getSize());
		while (it.hasNext()){
			String e = it.next();
			createSelect += e+":"+MrqlTypeConvert.getMrqlType(out.getFields().getFieldType(e));
			if (it.hasNext()){
				createSelect += ", ";
			}
		}
		createSelect +=">))";

		return createSelect;
	}

	public String getStoreQueryPiece(DFEOutput out, String relationName) throws RemoteException{
//		MapRedTextType output = (MapRedTextType) getDFEOutput().get(key_output); 
//		String delimiter = output.getPigDelimiter();

//		String function = getStoreFunction(delimiter);
//		logger.info(function);
		
		String query = "STORE '"+out.getPath() + "' FROM " + relationName+ ";";
				
		return query;
	}

//	/**
//	 * Get the store part of the query
//	 * @param delimiter
//	 * @return query
//	 * @throws RemoteException
//	 */
//	public String getStoreFunction(String delimiter) throws RemoteException{
//		String type = "";
//		String function = null;
//		if(delimiter==null || delimiter.equalsIgnoreCase("")){
//			delimiter ="|";
//		}
//		logger.info("delim is : "+delimiter);
//		try{
//			
//			type = savetypeOutputInt.getTree().getFirstChild("list").getFirstChild("output").getFirstChild().getHead();
//			logger.info("type: "+type);
//			if(type.equalsIgnoreCase("TEXT MAP-REDUCE DIRECTORY")||type.equalsIgnoreCase("COMPRESSED MAP-REDUCE DIRECTORY")){
//
//				function = "PigStorage('"+delimiter+"')";//+"','-schema')";//TODO Schema IS a problem, '-schema'); Didnt Have propblem ?
//			}
//			if (type.equalsIgnoreCase("BINARY MAP-REDUCE DIRECTORY")){
//				function = "BinStorage()";
//			}
//			logger.info("Storing via "+function);
//			return function;
//		}catch (Exception e){
//			logger.error("There was an error getting the output type");
//		}
//		return null;
//
//	}
//	/**
//	 * Get the function to load or store the data
//	 * @param out
//	 * @param delimiter
//	 * @return function
//	 * @throws RemoteException
//	 */
//	private String getLoadStoreFuncion(DFEOutput out, String delimiter) throws RemoteException{
//		String function = null;
//		if (out.getTypeName().equals("TEXT MAP-REDUCE DIRECTORY") ||
//				out.getTypeName().equals("COMPRESSED MAP-REDUCE DIRECTORY")){
//			function = "PigStorage('"+delimiter+"')";
//		}
//		else if (out.getTypeName().equals("BINARY MAP-REDUCE DIRECTORY")){
//			function = "BinStorage()";
//		}
//		return function;
//	}
	/**
	 * Get the Current Name of the bag
	 * @return current Name
	 */
	public String getCurrentName(){
		return "A"+nameCont;
	}
	/**
	 * Get the Next Name in the bag
	 * @return next Name
	 */
	public String getNextName(){
		nameCont++;
		return "A"+nameCont;
	}
	
	/**
	 * Get the Previous Name in the bag
	 * @return next Name
	 */
	public String getPreviousName(){
		return "A"+(nameCont-1);
	}
	/**
	 * Get the grouping interaction
	 * @return groupingInt
	 */
	public MrqlGroupInteraction getGroupingInt() {
		return groupingInt;
	}
	
	/**
	 * Get the ordering interaction
	 * @return groupingInt
	 */
	public MrqlOrderInteraction getOrderInt() {
		return orderInt;
	}
	
	/**
	 * Get the ordering interaction
	 * @return groupingInt
	 */
	public ListInteraction getSaveTypeInt() {
		return savetypeOutputInt;
	}
}
