package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.DataOutput;
import idiro.workflow.server.DataProperty;
import idiro.workflow.server.DataflowAction;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.datatype.MapRedBinaryType;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.enumeration.DataBrowser;
import idiro.workflow.server.enumeration.DisplayType;
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

	protected UserInteraction delimiterOutputInt;
	protected UserInteraction dataSubtypeInt;
	protected UserInteraction typeOutputInt;
	protected UserInteraction savetypeOutputInt;
	public PigGroupInteraction groupingInt;

	protected Map<String, DFELinkProperty> input;
	protected Map<String, DFEOutput> output;

	protected int minNbOfPage;
	
	private int nameCont;

	public PigElement(int minNbOfPage, int nbInMin, int nbInMax) throws RemoteException {
		super(new PigAction());
		init(nbInMin,nbInMax);
		this.minNbOfPage = minNbOfPage;
//		delimiterOutputInt = new UserInteraction("Delimiter", "Setting output delimiter", DisplayType.list, 0, 0);
//		
//		savetypeOutputInt = new UserInteraction(
//				"Output Type",
//				"Setting the output type",
//						DisplayType.list,
//						0,
//						0);

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
		logger.debug("Write queries in file: "+files[0].getAbsolutePath());
		String toWrite = getQuery();
		boolean ok = toWrite != null;
		if(ok){
			logger.debug("Content of "+files[0].getName()+": "+toWrite);
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
		
		logger.debug("Write properties in file: "+files[1].getAbsolutePath());
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

//	@Override
	public String updateOut() throws RemoteException {
		String error = checkIntegrationUserVariables();
		if(error == null){
			FeatureList new_features = getNewFeatures();
			if(output == null){
				output = new LinkedHashMap<String, DFEOutput>();
				output.put(key_output, new MapRedTextType());
			}else{
				/*if(output.get(key_output) instanceof MapRedTextType){
					output.clear();
					output.put(key_output, new MapRedTextType());
					logger.info("setting output if instance exists"+output.get(key_output).getPath());
				}*/
			}
			output.get(key_output).setFeatures(new_features);
		}
		return error;
	}

//	@Override
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

//	@Override
	public Map<String, DFEOutput> getDFEOutput() throws RemoteException {
		return output;
	}


	/**
	 * @return the delimiterOutputInt
	 */
	public UserInteraction getDelimiterOutputInt() {
		return delimiterOutputInt;
	}

	public void updateOutputType() throws RemoteException, InstantiationException, IllegalAccessException{
		Tree<String> list= null;
		if(savetypeOutputInt.getTree().isEmpty()){
			list = savetypeOutputInt.getTree().add("list");
			String k = MapRedTextType.class.newInstance().getTypeName();
			list.add("output").add(k);
			Tree<String> values = list.add("values");
			values.add("value").add(k);
			values.add("value").add(MapRedBinaryType.class.newInstance().getTypeName());
		}
		logger.info("output tree : "+((TreeNonUnique<String>) list).toString());
	}
	
	public void updateDelimiterOutputInt() throws RemoteException{
		logger.debug("updating default delimiter");
		Tree<String> list = null;
		delimiterOutputInt.setTree(new TreeNonUnique<String>(componentId));
		Tree<String>tree = delimiterOutputInt.getTree();
		if(tree.isEmpty()){
			list = delimiterOutputInt.getTree().add("list");
			list.add("output").add(default_delimiter);
			Tree<String> value = list.add("values");
			value.add("value").add("\001");
			value.add("value").add(",");
			value.add("value").add("|");
			value.add("value").add(";");
			logger.debug("delimiters added");
			
		}
		logger.debug("updating default delimiter complete");
	}
	
	/**
	 * @return the delimiterOutputInt
	 */
	public final UserInteraction getDataSubtypeInt() {
		return dataSubtypeInt;
	}

	public void updateDataSubTypeInt() throws RemoteException{
		Tree<String> treeDatasubtype = dataSubtypeInt.getTree();
		Tree<String> list = null;
		if(treeDatasubtype.getSubTreeList().isEmpty()){
			list = treeDatasubtype.add("list");
			list.add("output");

			Tree<String> value = list.add("value");
			
			Iterator<String> dataOutputClassName = 
					WorkflowPrefManager.getInstance().getNonAbstractClassesFromSuperClass(
							DataOutput.class.getCanonicalName()).iterator();
				
			while(dataOutputClassName.hasNext()){
				String className = dataOutputClassName.next();
				DataOutput wa = null;
				try {
					wa = (DataOutput) Class.forName(className).newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if (wa.getBrowser().equals(DataBrowser.HDFS)){
					value.add(DataOutput.class.getSimpleName());
				}
			}
		}
	}
	
	public void addOrRemoveOutPage() throws RemoteException{
//		logger.info("getting parts");
//		List<Tree<String>> parts = dataSubtypeInt.getTree()
//				.getFirstChild("table").getChildren("row");
//		if(parts.isEmpty()){
//			if(pageList.size() > minNbOfPage){
//				typeOutputInt = null;
//				pageList.remove(pageList.size()-1);
//			}
//		}else if(pageList.size() == minNbOfPage){
//			Page page = addPage("Output selection",
//					"",
//					1);
//
//			typeOutputInt = new UserInteraction(
//					key_outputType,
//					"Specify Partition only, if you want to use "+
//							"only the newly created partition in the next actions",
//							DisplayType.list,
//							0,
//							0);
//
//			page.addInteraction(typeOutputInt);
//		}
	}
	
	
//	protected void addOutputPage() throws RemoteException{
//		Page page = addPage("Output",
//				"Output_Options",
//				1);
//		
//		delimiterOutputInt = new UserInteraction(
//				"Output_Delimiter",
//				"Please specify the delimiter for the output file.",
//				DisplayType.list,
//				0,
//				0);
//		
//		page.addInteraction(delimiterOutputInt);
//		
//		dataSubtypeInt = new UserInteraction(
//				"Data_subtype",
//				"Please specify a data subtype",
//				DisplayType.list,
//				0,
//				1); 
//
//		page.addInteraction(dataSubtypeInt);
//	}
	
	public String getRemoveQueryPiece(String out) throws RemoteException{
		logger.debug("create remove...");
		return "rmf "+out;
	}
	
	public String getLoadQueryPiece(DFEOutput out) throws RemoteException{
		logger.debug("create load...");
		
		String delimiter = out.getProperty(MapRedTextType.key_delimiter_char);
		
		if (delimiter == null){
			delimiter = default_delimiter;
		}
		
		String function = getLoadStoreFuncion(out, delimiter);
		String createSelect = "LOAD '" + out.getPath() + "' USING "+function+" as (";
		
		Iterator<String> it = out.getFeatures().getFeaturesNames().iterator();
		while (it.hasNext()){
			String e = it.next();
			createSelect += e+":"+out.getFeatures().getFeatureType(e);
			if (it.hasNext()){
				createSelect += ", ";
			}
		}
		createSelect +=")";

		return createSelect;
	}
	
	public String getStoreQueryPiece(DFEOutput out, String relationName) throws RemoteException{
		String delimiter = default_delimiter;
		try{
			delimiter = delimiterOutputInt.getTree().getFirstChild("list").getFirstChild("output").getFirstChild().getHead();
		}
		catch(Exception e){
			logger.debug("Delimiter not set, using default delimiter");
		}
		
		String type = null;
		DFEOutput typeOutput = null;
//		try{
//			type = dataSubtypeInt.getTree().getFirstChild("list").getFirstChild("output").getFirstChild().getHead();
//			
//			Iterator<String> dataOutputClassName = 
//					WorkflowPrefManager.getInstance().getNonAbstractClassesFromSuperClass(
//							DataOutput.class.getCanonicalName()).iterator();
//			
//			Class<?> klass = null;
//			while (dataOutputClassName.hasNext()){
//				String className = dataOutputClassName.next();
//				String[] classNameArray = className.split("\\.");
//				if (classNameArray[classNameArray.length-1].equals(type)){
//					klass = Class.forName(className);
//					break;
//				}
//			}
//			
//			typeOutput = (DFEOutput)(klass.newInstance());
//		}
//		catch(Exception e){
//			logger.debug("Output type not set, using type from source");
//		}
//		if (typeOutput == null){
//			typeOutput = out;
//		}
		
//		String function = getLoadStoreFuncion(typeOutput, delimiter);
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
			logger.info("Storeing via "+function);
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
}
