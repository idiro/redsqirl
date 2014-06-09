package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.workflow.server.datatype.MapRedCtrlATextType;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class AuditGenerator {

	private static Logger logger = Logger.getLogger(AuditGenerator.class);

	/**
	 * Get the remove query piece of the query
	 * 
	 * @param out
	 * @return query
	 * @throws RemoteException
	 */
	public String getRemoveQueryPiece(String out) throws RemoteException {
		logger.debug("create remove...");
		return "rmf " + out;
	}

	/**
	 * Get the load query piece for the query
	 * 
	 * @param out
	 * @return query
	 * @throws RemoteException
	 */
	public String getLoadQueryPiece(DFEOutput out) throws RemoteException {
		logger.debug("create load...");

		String delimiter = out.getProperty(MapRedTextType.key_delimiter);
		delimiter = ((MapRedTextType) out).getPigDelimiter();
		if (delimiter == null) {
			delimiter = new String(new char[] { '\001' });
		}

		String function = getLoadStoreFuncion(out, delimiter);
		String createSelect = "LOAD '" + out.getPath() + "' USING " + function
				+ " as (";

		Iterator<String> it = out.getFeatures().getFeaturesNames().iterator();
		logger.info("attribute list size : " + out.getFeatures().getSize());
		while (it.hasNext()) {
			String e = it.next();
			createSelect += e
					+ ":"
					+ PigTypeConvert.getPigType(out.getFeatures()
							.getFeatureType(e));
			if (it.hasNext()) {
				createSelect += ", ";
			}
		}
		createSelect += ")";

		return createSelect;
	}

	public String getStoreQueryPiece(DFEOutput out, String relationName)
			throws RemoteException {
		String delimiter = new String(new char[] { '\001' });

		String function = "PigStorage('" + delimiter + "')";
		logger.info(function);
		return "STORE " + relationName + " INTO '" + out.getPath() + "' USING "
				+ function + ";";
	}

	/**
	 * Get the function to load or store the data
	 * 
	 * @param out
	 * @param delimiter
	 * @return function
	 * @throws RemoteException
	 */
	private String getLoadStoreFuncion(DFEOutput out, String delimiter)
			throws RemoteException {
		String function = null;
		if (out.getTypeName().equals("TEXT MAP-REDUCE DIRECTORY")) {
			function = "PigStorage('" + delimiter + "')";
		} else if (out.getTypeName().equals("BINARY MAP-REDUCE DIRECTORY")) {
			function = "BinStorage()";
		}
		return function;
	}

	public String getQuery(DFEOutput in, DFEOutput audit_out, Integer parallel)throws RemoteException {
		return getQuery(in,audit_out,parallel,null);
	}
	public String getQuery(DFEOutput in, DFEOutput audit_out, Integer parallel,String loader)
			throws RemoteException {
		String query = null;
		if (in != null) {
			logger.debug("In and out...");
			// Output

			String remove = getRemoveQueryPiece(audit_out.getPath()) + "\n\n";
			String load = "";
			if(loader == null){
				loader = "DATA_TO_AUDIT";
				load = loader + " = " + getLoadQueryPiece(in) + ";\n\n";
			}

			FeatureList fl = in.getFeatures();
			Set<String> stringFeats = new LinkedHashSet<String>();
			Set<String> categoryFeats = new LinkedHashSet<String>();
			Set<String> numericFeats = new LinkedHashSet<String>();
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
			String select = "G_ALL = group " + loader + " ALL PARALLEL "
					+ parallel + ";\n";

			select += "GLOB_TMP = FOREACH G_ALL{\n";
			Iterator<String> stringFeatsIt = stringFeats.iterator();
			while (stringFeatsIt.hasNext()) {
				String name = stringFeatsIt.next();
				select += "\t" + name + "_col = " + loader + "." + name + ";\n";
				select += "\t" + name + "_vals = DISTINCT " + name + "_col;\n";
			}
			select += "\tGENERATE\n";

			// Range
			select += "\t\t('Range'";
			flNames = fl.getFeaturesNames().iterator();
			while (flNames.hasNext()) {
				String name = flNames.next();
				select += ",\n\t\t\tCONCAT( (CHARARRAY) MIN(" + loader + "."
						+ name + "), " + "CONCAT(' - ', (CHARARRAY) MAX("
						+ loader + "." + name + ")))";
			}
			select += "\n\t\t),\n";

			// Not null values
			select += "\t\t('Not null values'";
			flNames = fl.getFeaturesNames().iterator();
			while (flNames.hasNext()) {
				String name = flNames.next();
				select += ",\n\t\t\tCOUNT(" + loader + "." + name + ")";
			}
			select += "\n\t\t),\n";
			// Null values
			select += "\t\t('Null values'";
			flNames = fl.getFeaturesNames().iterator();
			while (flNames.hasNext()) {
				String name = flNames.next();
				select += ",\n\t\t\tCOUNT_STAR(" + loader + ") - COUNT("
						+ loader + "." + name + ") ";
			}
			select += "\n\t\t),\n";

			// Average
			select += "\t\t('Average'";
			flNames = fl.getFeaturesNames().iterator();
			while (flNames.hasNext()) {
				String name = flNames.next();
				if (numericFeats.contains(name)) {
					select += ",\n\t\t\tAVG(" + loader + "." + name + ")";
				} else {
					select += ",\n\t\t\tNULL";
				}
			}
			select += "\n\t\t),\n";

			// Count Distinct
			select += "\t\t('Count distinct values'";
			flNames = fl.getFeaturesNames().iterator();
			while (flNames.hasNext()) {
				String name = flNames.next();
				if (stringFeats.contains(name)) {
					select += ",\n\t\t\tCOUNT_STAR(" + name + "_vals)";
				} else {
					select += ",\n\t\t\tNULL";
				}
			}
			select += "\n\t\t),\n";

			// Distinct
			select += "\t\t('Distinct values'";
			flNames = fl.getFeaturesNames().iterator();
			while (flNames.hasNext()) {
				String name = flNames.next();
				if (categoryFeats.contains(name)) {
					select += ",\n\t\t\t" + name + "_vals";
				} else {
					select += ",\n\t\t\tNULL";
				}
			}
			select += "\n\t\t);\n";
			select += "}\n\n";
			select += "GLOB_OUT = FOREACH GLOB_TMP generate FLATTEN(TOBAG(*));\n\n";

			String store = getStoreQueryPiece(audit_out, "GLOB_OUT");

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

	public Map<String, List<String>> readDistinctValuesAudit(String alias,
			DFEOutput auditOutput) throws RemoteException {
		Map<String, List<String>> ans = null;
		if (auditOutput != null && auditOutput instanceof MapRedCtrlATextType) {
			ans = new LinkedHashMap<String, List<String>>();
			if (auditOutput.isPathExists() && auditOutput.isPathValid() == null) {
				List<Map<String, String>> values = auditOutput.select(10);
				logger.info(values);
				Iterator<Map<String, String>> itVals = values.iterator();
				boolean found = false;
				while (itVals.hasNext() && !found) {
					Map<String, String> cur = itVals.next();
					if (cur.get("Legend").contains("Distinct values")) {
						found = true;
						Iterator<String> itAudit = cur.keySet().iterator();
						while (itAudit.hasNext()) {
							String nameAudit = itAudit.next();
							logger.info("Values for " + nameAudit + ": "
									+ cur.get(nameAudit));
							if (nameAudit.startsWith("AUDIT_")
									&& !cur.get(nameAudit).isEmpty()) {
								String feat = nameAudit.substring(6);
								List<String> vals = new LinkedList<String>();
								String valsStr = cur.get(nameAudit).substring(
										1, cur.get(nameAudit).length() - 1);
								String[] valSplit = valsStr.split("\\),\\(");
								if (valSplit.length == 1) {
									vals.add(valsStr.substring(1,
											cur.get(nameAudit).length() - 1));
								} else {
									vals.add(valSplit[0].substring(1));
									for (int i = 1; i < valSplit.length - 1; ++i) {
										vals.add(valSplit[i]);
									}
									vals.add(valSplit[valSplit.length - 1]
											.substring(
													0,
													valSplit[valSplit.length - 1]
															.length() - 1));
								}
								logger.info("Values for " + feat + " in list: "
										+ vals);
								if (alias == null || alias.isEmpty()) {
									ans.put(feat, vals);
								} else {
									ans.put(alias + "." + feat, vals);
								}
							}
						}
					}
				}
			}
		}
		return ans;
	}

	public Map<String, Double[]> readRangeValuesAudit(String alias,
			DFEOutput auditOutput) throws RemoteException {
		Map<String, Double[]> ans = null;
		if (auditOutput != null && auditOutput instanceof MapRedCtrlATextType) {
			ans = new LinkedHashMap<String, Double[]>();
			if (auditOutput.isPathExists() && auditOutput.isPathValid() == null) {
				List<Map<String, String>> values = auditOutput.select(10);
				logger.info(values);
				Iterator<Map<String, String>> itVals = values.iterator();
				boolean found = false;
				while (itVals.hasNext() && !found) {
					Map<String, String> cur = itVals.next();
					if (cur.get("Legend").contains("Range")) {
						found = true;
						Iterator<String> itAudit = cur.keySet().iterator();
						while (itAudit.hasNext()) {
							String nameAudit = itAudit.next();
							logger.info("Values for " + nameAudit + ": "
									+ cur.get(nameAudit));
							if (nameAudit.startsWith("AUDIT_")
									&& !cur.get(nameAudit).isEmpty()) {
								String feat = nameAudit.substring(6);

								try {
									Double[] vals = new Double[2];
									String[] valSplit = cur.get(nameAudit)
											.split(" - ");
									vals[0] = Double
											.valueOf(valSplit[0].trim());
									vals[1] = Double
											.valueOf(valSplit[1].trim());

									logger.info("Values for " + feat
											+ " in list: " + vals[0] + " , "
											+ vals[1]);
									if (alias == null || alias.isEmpty()) {
										ans.put(feat, vals);
									} else {
										ans.put(alias + "." + feat, vals);
									}
								} catch (NumberFormatException e) {
									logger.debug(feat + " is not a number");
								}
							}
						}
					}
				}
			}
		}
		return ans;
	}
}
