package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.workflow.server.DataProperty;
import idiro.workflow.server.DataflowAction;
import idiro.workflow.server.InputInteraction;
import idiro.workflow.server.ListInteraction;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.datatype.MapRedBinaryType;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.oozie.PigAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Common functionalities for a Pig action.
 * A Pig action support as input and output 
 * 
 * @author marcos
 *
 */
public abstract class PigElement extends DataflowAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1651299366774317959L;

	public static final String key_output = "out",
			key_input = "in",
			key_condition = "Condition",
			key_outputType = "Output_Type",
			default_delimiter = "\001";

	protected InputInteraction delimiterOutputInt;
	protected ListInteraction savetypeOutputInt;
	public PigGroupInteraction groupingInt;

	protected Map<String, DFELinkProperty> input;
	protected Map<String, DFEOutput> output = new LinkedHashMap<String, DFEOutput>();

	private String alias = "";
	private int nameCont;

	public PigElement( int nbInMin, int nbInMax,int placeDelimiterInPage) throws RemoteException {
		super(new PigAction());
		init(nbInMin,nbInMax);

		delimiterOutputInt = new InputInteraction("Delimiter",
				"Setting output delimiter", placeDelimiterInPage, 0);
		delimiterOutputInt.setRegex("^(#\\d{1,3}|.)?$");
		delimiterOutputInt.setValue("#1");


		savetypeOutputInt = new ListInteraction(key_outputType,
				"Setting the output type", placeDelimiterInPage+1, 0);
		savetypeOutputInt.setDisplayRadioButton(true);
		List<String> saveTypePos = new LinkedList<String>();
		saveTypePos.add( new MapRedTextType().getTypeName());
		saveTypePos.add( new MapRedBinaryType().getTypeName());
		savetypeOutputInt.setPossibleValues(saveTypePos);
		savetypeOutputInt.setValue(new MapRedTextType().getTypeName());
	}

	protected void init(int nbInMin, int nbInMax) throws RemoteException{
		if(input == null){
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(MapRedBinaryType.class, nbInMin, nbInMax));
			input = in;
		}
	}

	public abstract String getQuery() throws RemoteException;

	public abstract FeatureList getInFeatures() throws RemoteException;

	public abstract FeatureList getNewFeatures() throws RemoteException;

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

		logger.info("Write properties in file: "+files[1].getName());
		toWrite = getProperties(output.values().iterator().next());
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
				logger.error("Fail to write into the file "+files[1].getAbsolutePath());
			}
		}
		return ok;
	}

	public String getProperties(DFEOutput out) throws RemoteException{
		String properties = "";

		properties += "number_features="+out.getFeatures().getSize()+"\n";

		int cont = 0;
		for (String name : out.getFeatures().getFeaturesNames()){
			properties += "feature"+cont+"_name="+name+"\n";
			properties += "feature"+cont+"_value="+out.getFeatures().getFeatureType(name)+"\n";
		}

		return properties;
	}

	public String updateOut() throws RemoteException {
		String error = checkIntegrationUserVariables();
		if(error == null){
			FeatureList new_features = getNewFeatures();
			if(output.get(key_output) == null){
				output.put(key_output, new MapRedTextType());
			}
			output.get(key_output).setFeatures(new_features);
			output.get(key_output).addProperty(MapRedTextType.key_delimiter, delimiterOutputInt.getValue());
		}
		return error;
	}


	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}


	public Map<String, DFEOutput> getDFEOutput() throws RemoteException {
		return output;
	}

	public String getRemoveQueryPiece(String out) throws RemoteException{
		logger.debug("create remove...");
		return "rmf "+out;
	}

	public String getLoadQueryPiece(DFEOutput out) throws RemoteException{
		logger.debug("create load...");

		String delimiter = out.getProperty(MapRedTextType.key_delimiter);
		delimiter = ((MapRedTextType)out).getPigDelimiter();
		if (delimiter == null){
			delimiter = default_delimiter;
		}

		String function = getLoadStoreFuncion(out, delimiter);
		String createSelect = "LOAD '" + out.getPath() + "' USING "+function+" as (";

		Iterator<String> it = out.getFeatures().getFeaturesNames().iterator();
		logger.info("attribute list size : "+out.getFeatures().getSize());
		while (it.hasNext()){
			String e = it.next();
			createSelect += e+":"+PigTypeConvert.getPigType(out.getFeatures().getFeatureType(e));
			if (it.hasNext()){
				createSelect += ", ";
			}
		}
		createSelect +=")";

		return createSelect;
	}

	public String getStoreQueryPiece(DFEOutput out, String relationName) throws RemoteException{
		MapRedTextType output = (MapRedTextType) getDFEOutput().get(key_output); 
		String delimiter = output.getPigDelimiter();

		String function = getStoreFunction(delimiter);
		logger.info(function);
		return "STORE "+relationName+" INTO '" + out.getPath() + "' USING "+function+";";
	}


	public String getStoreFunction(String delimiter) throws RemoteException{
		String type = "";
		String function = "";
		if(delimiter==null || delimiter.equalsIgnoreCase("")){
			delimiter ="|";
		}
		try{
			type = savetypeOutputInt.getTree().getFirstChild("list").getFirstChild("output").getFirstChild().getHead();
			logger.info("type: "+type);
			if(type.equalsIgnoreCase("TEXT MAP-REDUCE DIRECTORY")){
				function = "PigStorage('"+delimiter+"')";
			}
			if (type.equalsIgnoreCase("BINARY MAP-REDUCE DIRECTORY")){
				function = "BinStorage()";
			}
			logger.info("Storing via "+function);
			return function;
		}catch (Exception e){
			logger.error("There was an error getting the output type");
		}
		return null;

	}
	private String getLoadStoreFuncion(DFEOutput out, String delimiter) throws RemoteException{
		String function = null;
		if (out.getTypeName().equals("TEXT MAP-REDUCE DIRECTORY")){
			function = "PigStorage('"+delimiter+"')";
		}
		else if (out.getTypeName().equals("BINARY MAP-REDUCE DIRECTORY")){
			function = "BinStorage()";
		}
		return function;
	}

	protected String getCurrentName(){
		return "A"+nameCont;
	}

	protected String getNextName(){
		nameCont++;
		return "A"+nameCont;
	}

	public PigGroupInteraction getGroupingInt() {
		return groupingInt;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
}
