package com.redsqirl.workflow.server.action;


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

import com.redsqirl.utils.FeatureList;
import com.redsqirl.utils.OrderedFeatureList;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.action.utils.HiveDictionary;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.datatype.HiveType;
import com.redsqirl.workflow.server.datatype.HiveTypePartition;
import com.redsqirl.workflow.server.enumeration.FeatureType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.oozie.HiveAction;

public class HiveAudit extends DataflowAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5397211998850948742L;

	/**
	 * Names of different elements
	 */
	public static final String key_output = "", key_input = "in";

	/**
	 * entries
	 */
	protected static Map<String, DFELinkProperty> input;

	public HiveAudit() throws RemoteException {
		super(new HiveAction());
		init();
	}

	/**
	 * Initiate the object
	 * 
	 * @throws RemoteException
	 */
	protected void init() throws RemoteException {
		if (input == null) {
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(HiveTypePartition.class, 1, 1));
			input = in;
		}

	}

	@Override
	public String getName() throws RemoteException {
		return "hive_audit";
	}

	@Override
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

	@Override
	public String updateOut() throws RemoteException {
		if (output.get(key_output) == null) {
			output.put(key_output, new HiveType());
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
	 * Get the query piece for the union
	 * 
	 * @param out
	 * @param conditions
	 * @return query piece
	 * @throws RemoteException
	 */
	public String getQueryPiece(String fromtablenameWithWhere) throws RemoteException {
		logger.debug("select...");
		HiveInterface hi = new HiveInterface();
		String select = "";
		FeatureList features = output.get(key_output).getFeatures();
		Iterator<String> it = features.getFeaturesNames().iterator();
		if (it.hasNext()) {
			String featName = it.next();
			select = "SELECT " + featName + " AS " + featName;
		}
		while (it.hasNext()) {
			String featName = it.next();
			select += ",\n      " + featName + " AS " + featName;
		}
		select += "\nFROM (\n";

		// Do feature list depending of the type.
		Set<String> stringFeats = new LinkedHashSet<String>();
		Set<String> categoryFeats = new LinkedHashSet<String>();
		Set<String> numericFeats = new LinkedHashSet<String>();
		FeatureList fl = getDFEInput().get(key_input).get(0).getFeatures();
		Iterator<String> flNames = fl.getFeaturesNames().iterator();
		while (flNames.hasNext()) {
			String name = flNames.next();
			switch (fl.getFeatureType(name)) {
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

		// Range
		select += "\tSELECT 'Range' AS LEGEND";
		flNames = fl.getFeaturesNames().iterator();
		while (flNames.hasNext()) {
			String name = flNames.next();
			select += ",\n\t\tCONCAT( MIN(" + name + "), ' - ', MAX(" + name
					+ ")) AS AUDIT_" + name;
		}
		select += "\n\t " + fromtablenameWithWhere + "\n";

		select += "UNION ALL\n";

		// Not null values
		select += "\tSELECT 'Count not null values' AS LEGEND";
		flNames = fl.getFeaturesNames().iterator();
		while (flNames.hasNext()) {
			String name = flNames.next();
			select += ",\n\t\tCOUNT_" + name + " AS AUDIT_" + name;
		}
		select += "\n\tFROM (";
		select += "\t\tSELECT ";
		flNames = fl.getFeaturesNames().iterator();
		if (flNames.hasNext()) {
			String name = flNames.next();
			select += "\n\t\t\tCOUNT(" + name + ") AS COUNT_" + name;
		}
		while (flNames.hasNext()) {
			String name = flNames.next();
			select += ",\n\t\t\tCOUNT(" + name + ") AS COUNT_" + name;
		}
		select += "\n\t\t " + fromtablenameWithWhere + " ) DT_NOTNULL\n";

		select += "UNION ALL\n";

		// Null values
		select += "\tSELECT 'Count null values' AS LEGEND";
		flNames = fl.getFeaturesNames().iterator();
		while (flNames.hasNext()) {
			String name = flNames.next();
			select += ",\n\t\tCOUNT_" + name + " AS AUDIT_" + name;
		}
		select += "\n\tFROM (";
		select += "\t\tSELECT ";
		flNames = fl.getFeaturesNames().iterator();
		if (flNames.hasNext()) {
			String name = flNames.next();
			select += "\n\t\t\tCOUNT(*) - COUNT(" + name + ") AS COUNT_" + name;
		}
		while (flNames.hasNext()) {
			String name = flNames.next();
			select += ",\n\t\t\tCOUNT(*) - COUNT(" + name + ") AS COUNT_"
					+ name;
		}
		select += "\n\t\t " + fromtablenameWithWhere + " ) DT_NULL\n";
		// Average
		it = numericFeats.iterator();
		if (it.hasNext()) {

			select += "UNION ALL\n";

			select += "\tSELECT 'Average' AS LEGEND";
			flNames = fl.getFeaturesNames().iterator();
			while (flNames.hasNext()) {
				String name = flNames.next();
				if (numericFeats.contains(name)) {
					select += ",\n\t\tAVG_" + name + " AS AUDIT_" + name;
				} else {
					select += ",\n\t\tNULL AS AUDIT_" + name;
				}

			}
			select += "\n\tFROM (";
			select += "\t\tSELECT ";
			
			String name = it.next();
			select += "\n\t\t\tAVG(" + name + ") AS AVG_" + name;
		while (it.hasNext()) {
			name = it.next();
			select += ",\n\t\t\tAVG(" + name + ") AS AVG_" + name;
		}
		select += "\n\t\t " + fromtablenameWithWhere + " ) DT_AVG\n";
		}

		// Count Distinct
		it = stringFeats.iterator();
		if (it.hasNext()) {

			select += "UNION ALL\n";

			select += "\tSELECT 'Count distinct values' AS LEGEND";
			flNames = fl.getFeaturesNames().iterator();
			while (flNames.hasNext()) {
				String name = flNames.next();
				if (stringFeats.contains(name)) {
					select += ",\n\t\tCNTD_" + name + " AS AUDIT_" + name;
				} else {
					select += ",\n\t\tNULL AS AUDIT_" + name;
				}

			}
			
			select += "\n\tFROM (";
			select += "\t\tSELECT ";
			String name = it.next();
			select += "\n\t\t\tCOUNT(DISTINCT(" + name + ")) AS CNTD_" + name;
			while (it.hasNext()) {
				name = it.next();
				select += ",\n\t\t\tCOUNT(DISTINCT(" + name + ")) AS CNTD_"
						+ name;
			}
			select += "\n\t\t " + fromtablenameWithWhere + " ) DT_CNTD\n";
		}


		// Distinct
		// Cannot cast an array to a string, just give up on this for the minute
		/*
		it = categoryFeats.iterator();
		if (it.hasNext()) {

			select += "UNION ALL\n";

			select += "\tSELECT 'Distinct values' AS LEGEND";
			flNames = fl.getFeaturesNames().iterator();
			while (flNames.hasNext()) {
				String name = flNames.next();
				if (categoryFeats.contains(name)) {
					select += ",\n\t\tDSTNCT_" + name + " AS AUDIT_" + name;
				} else {
					select += ",\n\t\tNULL AS AUDIT_" + name;
				}

			}
			
			select += "\n\tFROM (";
			select += "\t\tSELECT ";
			String name = it.next();
			select += "\n\t\t\tREPLACE( COLLECT_SET(" + name
					+ "), '\002', ', ') AS DSTNCT_" + name;
			while (it.hasNext()) {
				name = it.next();
				select += ",\n\t\t\tREPLACE( COLLECT_SET(" + name
					+ "), '\002', ', ') AS DSTNCT_" + name;
			}
			select += "\n\t\t " + fromtablenameWithWhere + " ) DT_DSTNCT\n";
		}
		*/
		return select+"\n) DT_ALL";
	}

	/**
	 * Get the create query piece that gets the features to be used in the
	 * create statement
	 * 
	 * @param out
	 * @return query piece
	 * @throws RemoteException
	 */
	public String getCreateQueryPiece() throws RemoteException {
		logger.debug("create features...");
		String createSelect = "";
		FeatureList features = output.get(key_output).getFeatures();
		Iterator<String> it = features.getFeaturesNames().iterator();
		if (it.hasNext()) {
			String featName = it.next();
			String type = HiveDictionary.getHiveType(features
					.getFeatureType(featName));
			createSelect = "(" + featName + " " + type;
		}
		while (it.hasNext()) {
			String featName = it.next();
			String type = HiveDictionary.getHiveType(features
					.getFeatureType(featName));
			createSelect += "," + featName + " " + type;
		}
		createSelect += ")";

		return createSelect;
	}
	
	/**
	 * Create the where statement for when there is a partition
	 * @return where
	 * @throws RemoteException
	 */

	public String getInputWhere() throws RemoteException {
		String where = "";
		List<DFEOutput> out = getDFEInput().get(key_input);
		Iterator<DFEOutput> it = out.iterator();
		while (it.hasNext()) {
			DFEOutput cur = it.next();
			String prop = cur.getProperty(HiveTypePartition.usePartition);
			if(prop != null && prop.equals("true")){
				String where_loc = ((HiveTypePartition) cur).getWhere();
				if (where_loc != null && !where.isEmpty()) {
					if (where.isEmpty()) {
						where = where_loc;
					} else {
						where = " AND " + where_loc;
					}
				}
			}
		}
		return where;
	}

	public String getQuery() throws RemoteException {

		HiveInterface hInt = new HiveInterface();
		String query = null;
		if (getDFEInput() != null) {
			DFEOutput in = getDFEInput().get(key_input).get(0);
			logger.info("In and out...");
			// Input
			String[] tableAndPartsIn = hInt.getTableAndPartitions(in.getPath());
			logger.info("table and parts ");
			String tableIn = tableAndPartsIn[0];
			logger.info("table In");
			// Output
			DFEOutput out = output.get(key_output);
			logger.info("ouput " + output.size());
			logger.info(out.getFeatures().getFeaturesNames().toString());
			logger.info("path : " + out.getPath());
			String[] tableOutArray = hInt.getTableAndPartitions(out.getPath());
			logger.info("paths : " + tableOutArray);
			String tableOut = tableOutArray[0];
			logger.info("table ouput : " + tableOut);

			String insert = "INSERT OVERWRITE TABLE " + tableOut;
			logger.info("insert : " + insert);
			String from = " FROM " + tableIn;
			String where = getInputWhere();
			if(!where.isEmpty()){
				from += " WHERE "+where; 
			}
			logger.info("from : " + from);
			String create = "CREATE TABLE IF NOT EXISTS " + tableOut;
			logger.info("create : " + create);

			String select = getQueryPiece(from);
			logger.info("select : " + select);

			String createSelect = getCreateQueryPiece();
			logger.info("create select : " + createSelect);

			// end partition code

			if (select.isEmpty()) {
				logger.debug("Nothing to select");
			} else {
				query = create + "\n" + createSelect + ";\n\n";

				query += insert + "\n" + select + "\n"+ ";";
			}
		}

		return query;
	}

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

		return ok;
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
	}
}
