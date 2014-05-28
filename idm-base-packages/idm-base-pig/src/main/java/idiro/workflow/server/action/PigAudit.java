package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.workflow.server.DataProperty;
import idiro.workflow.server.DataflowAction;
import idiro.workflow.server.InputInteraction;
import idiro.workflow.server.Page;
import idiro.workflow.server.datatype.MapRedBinaryType;
import idiro.workflow.server.datatype.MapRedCtrlATextType;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.oozie.PigAction;
import idiro.workflow.utils.PigLanguageManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class PigAudit extends DataflowAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4652143460618748778L;

	/**
	 * Names of different elements
	 */
	public static final String key_output = "", key_input = "in";

	
	public InputInteraction nbParallel;
	/**
	 * entries
	 */
	protected static Map<String, DFELinkProperty> input;

	public PigAudit() throws RemoteException {
		super(new PigAction());
		init();

		Page page1 = addPage(
				PigLanguageManager.getText("pig.audit_page1.title"),
				PigLanguageManager.getText("pig.audit_page1.legend"), 1);

		nbParallel = new InputInteraction(
				"parallel",
				PigLanguageManager
						.getText("pig.table_parallel_interaction.title"),
				PigLanguageManager
						.getText("pig.table_parallel_interaction.legend"),
				0, 0);

		nbParallel.setValue("3");
		nbParallel.setRegex("^\\d{1,4}?$");
		page1.addInteraction(nbParallel);
	}

	/**
	 * Initiate the object
	 * 
	 * @throws RemoteException
	 */
	protected void init() throws RemoteException {
		if (input == null) {
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(MapRedBinaryType.class, 1, 1));
			input = in;
		}

	}

	public String getName() throws RemoteException {
		return "pig_audit";
	}

	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

	public String updateOut() throws RemoteException {
		if (output.get(key_output) == null) {
			output.put(key_output, new MapRedCtrlATextType());
		}
		try {
			FeatureList fl = new OrderedFeatureList();
			fl.addFeature("Legend", FeatureType.STRING);
			Iterator<String> it = getDFEInput().get(key_input).get(0)
					.getFeatures().getFeaturesNames().iterator();
			while (it.hasNext()) {
				fl.addFeature("AUDIT_" + it.next(), FeatureType.STRING);
			}
			output.get(key_output).setFeatures(fl);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
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
			delimiter = new String(new char[]{'\001'});
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
		String delimiter = new String(new char[]{'\001'});

		String function = "PigStorage('"+delimiter+"')";
		logger.info(function);
		return "STORE "+relationName+" INTO '" + out.getPath() + "' USING "+function+";";
	}
	
	/**
	 * Get the function to load or store the data
	 * @param out
	 * @param delimiter
	 * @return function
	 * @throws RemoteException
	 */
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
	
	public String getQuery() throws RemoteException{
		String query = null;
		if (getDFEInput() != null) {
			DFEOutput in = getDFEInput().get(key_input).get(0);
			logger.debug("In and out...");
			// Output
			DFEOutput out = output.values().iterator().next();

			String loader = "";
			Iterator<String> aliases = getAliases().keySet().iterator();
			if (aliases.hasNext()) {
				loader = aliases.next();
			}

			String remove = getRemoveQueryPiece(out.getPath()) + "\n\n";

			String load = loader + " = " + getLoadQueryPiece(in) + ";\n\n";

			FeatureList fl = in.getFeatures();
			Set<String> stringFeats = new LinkedHashSet<String>();
			Set<String> categoryFeats = new LinkedHashSet<String>();
			Set<String> numericFeats = new LinkedHashSet<String>();
			Iterator<String> flNames = fl.getFeaturesNames().iterator();
			while(flNames.hasNext()){
				String name = flNames.next();
				switch(fl.getFeatureType(name)){
				case CHAR:
				case STRING:
					stringFeats.add(name);
					break;
				case CATEGORY:
					stringFeats.add(name);
					categoryFeats.add(name);
					break;
				case DOUBLE:
				case FLOAT:
				case LONG:
				case INT:
					numericFeats.add(name);
					break;
				default:
					break;
				}
			}
			String parallel = nbParallel.getValue();
			String select = "G_ALL = group "+loader+" ALL PARALLEL "+parallel+";\n";
			
			select += "GLOB_TMP = FOREACH G_ALL{\n";
			Iterator<String> stringFeatsIt = stringFeats.iterator();
			while(stringFeatsIt.hasNext()){
				String name = stringFeatsIt.next();
				select +="\t"+name+"_col = "+loader+"."+name+";\n";
				select +="\t"+name+"_vals = DISTINCT "+name+"_col;\n";
			}
			select +="\tGENERATE\n";
			
			//Range
			select +="\t\t('Range'";
			flNames = fl.getFeaturesNames().iterator();
			while(flNames.hasNext()){
				String name = flNames.next();
				select +=",\n\t\t\tCONCAT( (CHARARRAY) MIN("+loader+"."+name+"), "
									+"CONCAT(' - ', (CHARARRAY) MAX("+loader+"."+name+")))";
			}
			select +="\n\t\t),\n";
			
			//Not null values
			select +="\t\t('Not null values'";
			flNames = fl.getFeaturesNames().iterator();
			while(flNames.hasNext()){
				String name = flNames.next();
				select +=",\n\t\t\tCOUNT("+loader+"."+name+")";
			}
			select +="\n\t\t),\n";
			//Null values
			select +="\t\t('Null values'";
			flNames = fl.getFeaturesNames().iterator();
			while(flNames.hasNext()){
				String name = flNames.next();
				select +=",\n\t\t\tCOUNT_STAR("+loader+") - COUNT("+loader+"."+name+") ";
			}
			select +="\n\t\t),\n";
			
			//Average
			select +="\t\t('Average'";
			flNames = fl.getFeaturesNames().iterator();
			while(flNames.hasNext()){
				String name = flNames.next();
				if(numericFeats.contains(name)){
					select +=",\n\t\t\tAVG("+loader+"."+name+")";
				}else{
					select +=",\n\t\t\tNULL";
				}
			}
			select +="\n\t\t),\n";
			
			//Count Distinct
			select +="\t\t('Count distinct values'";
			flNames = fl.getFeaturesNames().iterator();
			while(flNames.hasNext()){
				String name = flNames.next();
				if(stringFeats.contains(name)){
					select +=",\n\t\t\tCOUNT_STAR("+name+"_vals)";
				}else{
					select +=",\n\t\t\tNULL";
				}
			}
			select +="\n\t\t),\n";
			
			//Distinct
			select +="\t\t('Distinct values'";
			flNames = fl.getFeaturesNames().iterator();
			while(flNames.hasNext()){
				String name = flNames.next();
				if(categoryFeats.contains(name)){
					select +=",\n\t\t\t"+name+"_vals";
				}else{
					select +=",\n\t\t\tNULL";
				}
			}
			select +="\n\t\t);\n";
			select +="}\n\n";
			select +="GLOB_OUT = FOREACH GLOB_TMP generate FLATTEN(TOBAG(*));\n\n";
			
			String store = getStoreQueryPiece(out, "GLOB_OUT");

			if (select.isEmpty()) {
				logger.debug("Nothing to select");
			} else {
				query = remove;
				query += load;
				query += select;
				query += store;
			}
		}
		logger.info(query);
		return query;
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

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {

	}
}
