package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.workflow.server.DataProperty;
import idiro.workflow.server.DataflowAction;
import idiro.workflow.server.ListInteraction;
import idiro.workflow.server.Page;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.datatype.HiveType;
import idiro.workflow.server.datatype.HiveTypeWithWhere;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEInteractionChecker;
import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.oozie.HiveAction;
import idiro.workflow.utils.LanguageManagerWF;

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

public class Convert extends DataflowAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6342079587552236953L;

	private static Map<String, DFELinkProperty> input = null;

	/**
	 * Choose the Output Format
	 */
	private Page page1,
	/**
	 * Change data properties
	 */
	page2;

	private ListInteraction formats;

	private ConvertPropertiesInteraction cpi;

	public static final String key_output = "out",
			key_input = "in",
			key_formats = "format",
			key_properties= "data_set_properties";

	public Convert() throws RemoteException {
		super(new HiveAction());
		init();

		page1 = addPage(LanguageManagerWF.getText("convert.page1.title"),
				LanguageManagerWF.getText("convert.page1.legend"),
				1);

		formats = new ListInteraction(
				key_formats,
				LanguageManagerWF.getText("convert.formats_interaction.title"),
				LanguageManagerWF.getText("convert.formats_interaction.legend"),
				0,
				0);
		
		formats.setDisplayRadioButton(true);
		formats.setChecker(new DFEInteractionChecker(){

			@Override
			public String check(DFEInteraction interaction)
					throws RemoteException {
				logger.info("Initialise convert");
				DFEOutput in = getDFEInput().get(key_input).get(0);
				FeatureList new_features = new OrderedFeatureList();

				FeatureList in_feat = in.getFeatures();
				Iterator<String> it = in_feat.getFeaturesNames().iterator();
				while(it.hasNext()){
					String name = it.next();
					new_features.addFeature(name, in_feat.getFeatureType(name));
				}


				String convert = interaction.getTree().getFirstChild("list").getFirstChild("output").getFirstChild().getHead();

				if(convert.equalsIgnoreCase((new MapRedTextType()).getTypeName()) && (
						output.get(key_output) == null ||
						!output.get(key_output).getTypeName().equalsIgnoreCase(convert))
						){
					output.put(key_output, new MapRedTextType());
					output.get(key_output).addProperty(MapRedTextType.key_delimiter, "|");
				}else if(convert.equalsIgnoreCase((new HiveType()).getTypeName())  && (
						output.get(key_output) == null ||
						!output.get(key_output).getTypeName().equalsIgnoreCase(convert))
						){
					output.put(key_output, new HiveType());
				}
				output.get(key_output).setFeatures(new_features);
				return null;
			}

		});

		page1.addInteraction(formats);

		page2 = addPage("Data Format Properties",
				"Change data properties",
				1);
		cpi = new ConvertPropertiesInteraction(
				key_properties,
				LanguageManagerWF.getText("convert.props_interaction.title"),
				LanguageManagerWF.getText("convert.props_interaction.legend"),
				0,
				0,
				this);

		page2.addInteraction(cpi);

	}

	protected static void init() throws RemoteException{
		if(input == null){
			List<Class<? extends DFEOutput>> l = new LinkedList<Class<? extends DFEOutput>>();
			l.add(HiveTypeWithWhere.class);
			l.add(MapRedTextType.class);
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(l, 1, 1));
			input = in;
		}

	}

	protected void updateFormat() throws RemoteException{
		List<String> values = new LinkedList<String>();
		DFEOutput in = getDFEInput().get(key_input).get(0);
		if(in.getClass().equals(MapRedTextType.class)){
			values.add((new HiveType()).getTypeName());
		}else{
			values.add((new MapRedTextType()).getTypeName());
		}
		formats.setPossibleValues(values);
	}

	@Override
	public String getName() throws RemoteException {
		return "Convert";
	}

	@Override
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

	@Override
	public String updateOut() throws RemoteException {
		logger.info("Initialize update out");
		String error = checkIntegrationUserVariables();
		if(error == null){
			DFEOutput in = getDFEInput().get(key_input).get(0);
			FeatureList new_features = new OrderedFeatureList();

			FeatureList in_feat = in.getFeatures();
			Iterator<String> it = in_feat.getFeaturesNames().iterator();
			while(it.hasNext()){
				String name = it.next();
				new_features.addFeature(name, in_feat.getFeatureType(name));
			}

			/*if(in.getClass().equals(MapRedTextType.class)){
				output.put(key_output, new HiveType());
			}else{
				output.put(key_output, new MapRedTextType());
			}*/
			output.get(key_output).setFeatures(new_features);
			Map<String,String> properties = cpi.getProperties();
			if(properties != null && !properties.isEmpty()){
				Iterator<String> itP = properties.keySet().iterator();
				while(itP.hasNext()){
					String propKey = itP.next();
					output.get(key_output).addProperty(propKey, properties.get(propKey));
				}
			}
		}
		return error;
	}

	public String importInHive() throws RemoteException{
		HiveInterface hi = new HiveInterface();
		MapRedTextType in = (MapRedTextType) getDFEInput().get(key_input).get(0);
		DFEOutput out = getDFEOutput().get(key_output);
		logger.debug(out.getPath());
		String table_out = hi.getTableAndPartitions(out.getPath())[0];
		String table_ext = table_out+"_ext";

		String create_out = "CREATE TABLE IF NOT EXISTS  "+table_out +"(";
		String create_ext = "CREATE EXTERNAL TABLE IF NOT EXISTS  "+ table_ext+"(";
		Iterator<String> itFeat = out.getFeatures().getFeaturesNames().iterator();
		while(itFeat.hasNext()){
			String name = itFeat.next();
			String type = HiveTypeConvert.getHiveType(out.getFeatures().getFeatureType(name));
			create_out += name+" "+type+",";
			create_ext += name+" "+type+",";
		}
		create_out = create_out.substring(0,create_out.length()-1);
		create_out +=");\n\n";
		create_ext = create_ext.substring(0,create_ext.length()-1);
		create_ext +=")\n";
		create_ext +="ROW FORMAT DELIMITED\n";
		String delimiter = in.getDelimiterOrOctal();
		create_ext +="FIELDS TERMINATED BY '"+delimiter+"'\n";
		create_ext +="STORED AS TEXTFILE\n";
		create_ext +="LOCATION '"+in.getPath()+"';\n\n";

		String select = "FROM "+table_ext+"\n";
		select +="INSERT OVERWRITE TABLE "+table_out+"\n";
		select += "SELECT *;\n\n";
		String drop_ext = "DROP TABLE "+table_ext+";\n";
		logger.debug(create_out+create_ext+select+drop_ext);
		return create_out+create_ext+select+drop_ext;
	}

	public String importInMapRed() throws RemoteException{
		HiveInterface hi = new HiveInterface();
		DFEOutput in = getDFEInput().get(key_input).get(0);
		MapRedTextType out = (MapRedTextType) getDFEOutput().get(key_output);
		String delimiter = out.getDelimiterOrOctal();
		String table_ext = 
				out.getPath().substring(1).split("/")[out.getPath().substring(1).split("/").length-1]
						+"_"+System.getProperty( "user.name" )+"_ext";
		String create_ext = "CREATE EXTERNAL TABLE IF NOT EXISTS "+table_ext+"(";
		Iterator<String> itFeat = out.getFeatures().getFeaturesNames().iterator();
		while(itFeat.hasNext()){
			logger.debug(7);
			String name = itFeat.next();
			String type = HiveTypeConvert.getHiveType(out.getFeatures().getFeatureType(name));
			create_ext += name+" "+type+",";
		}
		create_ext = create_ext.substring(0,create_ext.length()-1);
		create_ext +=")\n";
		create_ext += "ROW FORMAT DELIMITED\n";
		create_ext += "FIELDS TERMINATED BY '"+delimiter+"'\n";
		create_ext += "STORED AS TEXTFILE\n";
		create_ext += "LOCATION '"+out.getPath()+"';\n\n";
		
		String select = "INSERT OVERWRITE TABLE "+table_ext+"\n";
		select += "select * from "+hi.getTableAndPartitions(in.getPath())[0];
		String where = in.getProperty(HiveTypeWithWhere.key_where);
		if(where != null && !where.isEmpty()){
			select += " where "+where;
		}
		select += ";\n\n";
		String drop_ext = "DROP TABLE "+table_ext+";\n";

		logger.debug(create_ext+select+drop_ext);
		return create_ext+select+drop_ext;
	}

	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		logger.debug("Write queries in file: "+files[0].getAbsolutePath());
		String toWrite = null;
		if(output.get(key_output).getClass().equals(MapRedTextType.class)){
			toWrite = importInMapRed();
		}else{
			toWrite = importInHive();
		}

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
		return ok;
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		String interId = interaction.getId();
		if(interId.equals(key_formats)){
			updateFormat();
		}else if(interId.equals(key_properties)){
			cpi.update();
		}
	}

	// Override default static methods
	@Override
	public String getHelp() throws RemoteException {
		return "../help/" + getName().toLowerCase() + ".html";
	}

	@Override
	public String getImage() throws RemoteException {
		return "../image/" + getName().toLowerCase() + ".gif";
	}

}
