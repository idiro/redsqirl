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

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.datatype.HiveType;
import com.redsqirl.workflow.server.datatype.HiveTypePartition;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.oozie.HiveAction;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class Convert extends DataflowAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6342079587552236953L;
	/**
	 * Map of inputs
	 */

	private static Map<String, DFELinkProperty> input = null;

	/**
	 * Choose the Output Format
	 */
	private Page page1,
	/**
	 * Change data properties
	 */
	page2;
	/**
	 * Formats interaction
	 */
	private ListInteraction formats;
	/**
	 * Convert properties Interaction
	 */
	private ConvertPropertiesInteraction cpi;

	public static final String
	/** Key outputs */
	key_output = "",
	/** Key inputs */
	key_input = "in",
	/** Key formats */
	key_formats = "format",
	/** Key data set properties */
	key_properties = "data_set_properties";

	/**
	 * Constructor
	 * 
	 * @throws RemoteException
	 */
	public Convert() throws RemoteException {
		super(new HiveAction());
		init();

		page1 = addPage(LanguageManagerWF.getText("convert.page1.title"),
				LanguageManagerWF.getText("convert.page1.legend"), 1);

		formats = new ListInteraction(
				key_formats,
				LanguageManagerWF.getText("convert.formats_interaction.title"),
				LanguageManagerWF.getText("convert.formats_interaction.legend"),
				0, 0);

		formats.setDisplayRadioButton(true);

		page1.addInteraction(formats);

		page2 = addPage("Data Format Properties", "Change data properties", 1);
		cpi = new ConvertPropertiesInteraction(key_properties,
				LanguageManagerWF.getText("convert.props_interaction.title"),
				LanguageManagerWF.getText("convert.props_interaction.legend"),
				0, 0, this);

		page2.addInteraction(cpi);

	}

	/**
	 * Initialize the action
	 * 
	 * @throws RemoteException
	 */
	protected static void init() throws RemoteException {
		if (input == null) {
			List<Class<? extends DFEOutput>> l = new LinkedList<Class<? extends DFEOutput>>();
			l.add(HiveTypePartition.class);
			l.add(MapRedTextType.class);
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(l, 1, 1));
			input = in;
		}

	}

	/**
	 * Update the format Interaction
	 * 
	 * @throws RemoteException
	 */
	protected void updateFormat() throws RemoteException {
		List<String> values = new LinkedList<String>();
		DFEOutput in = getDFEInput().get(key_input).get(0);
		if (in.getClass().equals(MapRedTextType.class)) {
			values.add((new HiveType()).getTypeName());
		} else {
			values.add((new MapRedTextType()).getTypeName());
		}
		formats.setPossibleValues(values);
		formats.setValue(values.get(0));
	}

	/**
	 * Get the Action name
	 * 
	 * @return name
	 */
	@Override
	public String getName() throws RemoteException {
		return "convert";
	}

	/**
	 * Get the map of inputs
	 * 
	 * @return Map of Inputs
	 * @throws RemoteException
	 */
	@Override
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

	/**
	 * Update the output of the action on initialization
	 * 
	 * @throws RemoteException
	 */
	public void updateOutput() throws RemoteException {
		logger.info("Initialise convert");
		DFEOutput in = getDFEInput().get(key_input).get(0);
		FieldList new_fields = new OrderedFieldList();

		FieldList in_field = in.getFields();
		Iterator<String> it = in_field.getFieldNames().iterator();
		while (it.hasNext()) {
			String name = it.next();
			new_fields.addField(name, in_field.getFieldType(name));
		}

		String convert = formats.getValue();
		if (convert == null) {
			logger.error("Format interaction has not been initialized!");
			return;
		} else if (convert.equalsIgnoreCase((new MapRedTextType())
				.getTypeName())
				&& (output.get(key_output) == null || !output.get(key_output)
						.getTypeName().equalsIgnoreCase(convert))) {
			output.put(key_output, new MapRedTextType());
			output.get(key_output).addProperty(MapRedTextType.key_delimiter,
					"|");
		} else if (convert.equalsIgnoreCase((new HiveType()).getTypeName())
				&& (output.get(key_output) == null || !output.get(key_output)
						.getTypeName().equalsIgnoreCase(convert))) {
			output.put(key_output, new HiveType());
		}
		output.get(key_output).setFields(new_fields);
	}
	/**
	 * Update the output of the action
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String updateOut() throws RemoteException {
		logger.info("Initialize update out");
		String error = checkIntegrationUserVariables();
		if (error == null && output.get(key_output) == null) {
			error = LanguageManagerWF.getText("convert.output_null");
		}
		if (error == null) {
			DFEOutput in = getDFEInput().get(key_input).get(0);
			FieldList new_fields = new OrderedFieldList();

			FieldList in_field = in.getFields();
			Iterator<String> it = in_field.getFieldNames().iterator();
			while (it.hasNext()) {
				String name = it.next();
				new_fields.addField(name, in_field.getFieldType(name));
			}

			output.get(key_output).setFields(new_fields);
			Map<String, String> properties = cpi.getProperties();
			if (properties != null && !properties.isEmpty()) {
				Iterator<String> itP = properties.keySet().iterator();
				while (itP.hasNext()) {
					String propKey = itP.next();
					output.get(key_output).addProperty(propKey,
							properties.get(propKey));
				}
			}
		}
		return error;
	}
	/**
	 * Import Data into Hive
	 * @return execution
	 * @throws RemoteException
	 */
	public String importInHive() throws RemoteException {
		HiveInterface hi = new HiveInterface();
		MapRedTextType in = (MapRedTextType) getDFEInput().get(key_input)
				.get(0);
		DFEOutput out = getDFEOutput().get(key_output);
		logger.debug(out.getPath());
		String table_out = hi.getTableAndPartitions(out.getPath())[0];
		String table_ext = table_out + "_ext";

		String create_out = "CREATE TABLE IF NOT EXISTS  " + table_out + "(";
		String create_ext = "CREATE EXTERNAL TABLE IF NOT EXISTS  " + table_ext
				+ "(";
		Iterator<String> itField = out.getFields().getFieldNames()
				.iterator();
		while (itField.hasNext()) {
			String name = itField.next();
			String type = HiveTypeConvert.getHiveType(out.getFields()
					.getFieldType(name));
			create_out += name + " " + type + ",";
			create_ext += name + " " + type + ",";
		}
		create_out = create_out.substring(0, create_out.length() - 1);
		create_out += ");\n\n";
		create_ext = create_ext.substring(0, create_ext.length() - 1);
		create_ext += ")\n";
		create_ext += "ROW FORMAT DELIMITED\n";
		String delimiter = in.getDelimiterOrOctal();
		create_ext += "FIELDS TERMINATED BY '" + delimiter + "'\n";
		create_ext += "STORED AS TEXTFILE\n";
		create_ext += "LOCATION '" + in.getPath() + "';\n\n";

		String select = "FROM " + table_ext + "\n";
		select += "INSERT OVERWRITE TABLE " + table_out + "\n";
		select += "SELECT *;\n\n";
		String drop_ext = "DROP TABLE " + table_ext + ";\n";
		logger.debug(create_out + create_ext + select + drop_ext);
		return create_out + create_ext + select + drop_ext;
	}

	/**
	 * Import the file into MapReduce Directory
	 * 
	 * @return execution
	 * @throws RemoteException
	 */
	public String importInMapRed() throws RemoteException {
		HiveInterface hi = new HiveInterface();
		DFEOutput in = getDFEInput().get(key_input).get(0);
		MapRedTextType out = (MapRedTextType) getDFEOutput().get(key_output);
		String delimiter = out.getDelimiterOrOctal();
		String table_ext = out.getPath().substring(1).split("/")[out.getPath()
				.substring(1).split("/").length - 1]
				+ "_" + System.getProperty("user.name") + "_ext";
		String create_ext = "CREATE EXTERNAL TABLE IF NOT EXISTS " + table_ext
				+ "(";
		Iterator<String> itField = out.getFields().getFieldNames()
				.iterator();
		while (itField.hasNext()) {
			logger.debug(7);
			String name = itField.next();
			String type = HiveTypeConvert.getHiveType(out.getFields()
					.getFieldType(name));
			create_ext += name + " " + type + ",";
		}
		create_ext = create_ext.substring(0, create_ext.length() - 1);
		create_ext += ")\n";
		create_ext += "ROW FORMAT DELIMITED\n";
		create_ext += "FIELDS TERMINATED BY '" + delimiter + "'\n";
		create_ext += "STORED AS TEXTFILE\n";
		create_ext += "LOCATION '" + out.getPath() + "';\n\n";

		String select = "INSERT OVERWRITE TABLE " + table_ext + "\n";
		select += "select * from " + hi.getTableAndPartitions(in.getPath())[0];
		String where = in.getProperty(HiveTypePartition.usePartition);
		if (where != null && !where.isEmpty()) {
			select += " where " + where;
		}
		select += ";\n\n";
		String drop_ext = "DROP TABLE " + table_ext + ";\n";

		logger.debug(create_ext + select + drop_ext);
		return create_ext + select + drop_ext;
	}

	/**
	 * Write files needed to run the Oozie action
	 * 
	 * @param files
	 * @return <code>true</code> if actions were written else <code>false</code>
	 * @throws RemoteException
	 */
	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		logger.debug("Write queries in file: " + files[0].getAbsolutePath());
		String toWrite = null;
		if (output.get(key_output).getClass().equals(MapRedTextType.class)) {
			toWrite = importInMapRed();
		} else {
			toWrite = importInHive();
		}

		boolean ok = toWrite != null;
		if (ok) {

			logger.debug("Content of " + files[0].getName() + ": " + toWrite);
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
		return ok;
	}

	/**
	 * Update the interactions in the action
	 * 
	 * @param interaction
	 * @throws RemoteException
	 * 
	 */
	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		String interId = interaction.getId();
		if (interId.equals(key_formats)) {
			updateFormat();
		} else if (interId.equals(key_properties)) {
			updateOutput();
			if (getDFEOutput().get(key_output) != null) {
				cpi.update();
			}
		}
	}

	// Override default static methods
	/**
	 * Get path to help
	 * 
	 * @return path
	 * @throws RemoteException
	 */
	@Override
	public String getHelp() throws RemoteException {
		String absolutePath = "";
		String helpFile = "/help/" + getName().toLowerCase() + ".html";
		String path = WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_tomcat_path);
		List<String> files = listFilesRecursively(path);
		for (String file : files) {
			if (file.contains(helpFile)) {
				absolutePath = file;
				break;
			}
		}
		String ans = "";
		if (absolutePath.contains(path)) {
			ans = absolutePath.substring(path.length());
		}
		logger.debug("Source help absPath : " + absolutePath);
		logger.debug("Source help Path : " + path);
		logger.debug("Source help ans : " + ans);
		// absolutePath
		return absolutePath;
	}

	/**
	 * Get the path to the Image
	 * 
	 * @return path
	 * @throws RemoteException
	 */
	@Override
	public String getImage() throws RemoteException {
		String absolutePath = "";
		String imageFile = "/image/" + getName().toLowerCase() + ".gif";
		String path = WorkflowPrefManager
						.getSysProperty(WorkflowPrefManager.sys_tomcat_path);
		List<String> files = listFilesRecursively(path);
		for (String file : files) {
			if (file.contains(imageFile)) {
				absolutePath = file;
				break;
			}
		}
		String ans = "";
		if (absolutePath.contains(path)) {
			ans = absolutePath.substring(path.length());
		}
		logger.debug("Source image abs Path : " + absolutePath);
		logger.debug("Source image Path : " + path);
		logger.debug("Source image ans : " + ans);

		return absolutePath;
	}

	/**
	 * Get the Formats Interaction
	 * 
	 * @return the formats
	 */
	public final ListInteraction getFormats() {
		return formats;
	}

	/**
	 * Get the ConvertPropertiesInteraction
	 * 
	 * @return cpi
	 */
	public final ConvertPropertiesInteraction getCpi() {
		return cpi;
	}

}
