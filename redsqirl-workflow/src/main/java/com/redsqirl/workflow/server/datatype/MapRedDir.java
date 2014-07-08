package com.redsqirl.workflow.server.datatype;



import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.idiro.hadoop.NameNodeVar;
import com.redsqirl.utils.FeatureList;
import com.redsqirl.utils.OrderedFeatureList;
import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.enumeration.FeatureType;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public abstract class MapRedDir extends DataOutput{


	/**
	 * 
	 */
	private static final long serialVersionUID = 3497308078096391496L;
	
	/** HDFS Interface */
	protected static HDFSInterface hdfsInt;

	/** Header Key */
	public final static String key_header = "header";
	

	protected static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	protected static SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	protected static SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");


	protected List<FeatureType> featuresNumberHierarchicalOrder = new LinkedList<FeatureType>();

	protected List<FeatureType> featuresStrHierarchicalOrder = new LinkedList<FeatureType>();

	protected List<FeatureType> featuresDateHierarchicalOrder = new LinkedList<FeatureType>();
	
	public MapRedDir() throws RemoteException{
		super();
		init();
	}
	
	public MapRedDir(FeatureList features) throws RemoteException {
		super(features);
		init();
	}
	
	private void init() throws RemoteException{
		if (hdfsInt == null) {
			hdfsInt = new HDFSInterface();
		}
		
		addProperty(key_header, "");

		featuresNumberHierarchicalOrder.add(FeatureType.INT);
		featuresNumberHierarchicalOrder.add(FeatureType.LONG);
		featuresNumberHierarchicalOrder.add(FeatureType.FLOAT);
		featuresNumberHierarchicalOrder.add(FeatureType.DOUBLE);
		featuresNumberHierarchicalOrder.add(FeatureType.CATEGORY);
		featuresNumberHierarchicalOrder.add(FeatureType.STRING);

		featuresStrHierarchicalOrder.add(FeatureType.CHAR);
		featuresStrHierarchicalOrder.add(FeatureType.CATEGORY);
		featuresStrHierarchicalOrder.add(FeatureType.STRING);
		featuresStrHierarchicalOrder.add(FeatureType.CATEGORY);

		featuresDateHierarchicalOrder.add(FeatureType.DATE);
		featuresDateHierarchicalOrder.add(FeatureType.DATETIME);
		featuresDateHierarchicalOrder.add(FeatureType.TIMESTAMP);
		featuresDateHierarchicalOrder.add(FeatureType.CATEGORY);
		featuresDateHierarchicalOrder.add(FeatureType.STRING);
	}
	

	/**
	 * Get the DataBrowser
	 * 
	 * @return {@link idiro.workflow.server.enumeration.DataBrowser}
	 * @throws RemoteException
	 */
	@Override
	public String getBrowser() throws RemoteException {
		return hdfsInt.getBrowserName();
	}


	@Override
	public boolean isPathExists() throws RemoteException {
		boolean ok = false;
		if (getPath() != null) {
			logger.info("checking if path exists: " + getPath().toString());
			int again = 10;
			FileSystem fs = null;
			while (again > 0) {
				try {
					fs = NameNodeVar.getFS();
					logger.debug("Attempt " + (11 - again) + ": existence "
							+ getPath());
					ok = fs.exists(new Path(getPath()));
					again = 0;
				} catch (Exception e) {
					logger.error(e);
					--again;
				}
				try {
					// fs.close();
				} catch (Exception e) {
					logger.error(e);
				}
				if (again > 0) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e1) {
						logger.error(e1);
					}
				}
			}
		}
		return ok;
	}
	
	/**
	 * Move the current path to a new one
	 * 
	 * @param newPath
	 * @throws RemoteException
	 */
	@Override
	public void moveTo(String newPath) throws RemoteException {
		if (isPathExists()) {
			hdfsInt.move(getPath(), newPath);
		}
		setPath(newPath);
	}

	/**
	 * Copy the current path to a new one
	 * 
	 * @param newPath
	 * @throws RemoteException
	 * 
	 */
	@Override
	public void copyTo(String newPath) throws RemoteException {
		if (isPathExists()) {
			hdfsInt.copy(getPath(), newPath);
		}
		setPath(newPath);
	}

	/**
	 * Remove the current path from hdfs
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String remove() throws RemoteException {
		return hdfsInt.delete(getPath());
	}

	@Override
	public boolean oozieRemove(Document oozieDoc, Element action,
			File localDirectory, String pathFromOozieDir,
			String fileNameWithoutExtension) throws RemoteException {
		Element fs = oozieDoc.createElement("fs");
		action.appendChild(fs);

		Element rm = oozieDoc.createElement("delete");
		rm.setAttribute("path", "${" + OozieManager.prop_namenode + "}"
				+ getPath());
		fs.appendChild(rm);

		return true;
	}
	

	/**
	 * Is name a variable
	 * 
	 * @param name
	 * @return <code>true</code> if name matches structure of a variable name
	 *         (contains characters with numbers) and has a maximum else
	 *         <code>false</code>
	 */
	public boolean isVariableName(String name) {
		String regex = "[a-zA-Z]([a-zA-Z0-9_]{0,29})";
		return name.matches(regex);
	}
	

	public List<String> selectLine(int maxToRead) throws RemoteException {
		List<String> ans = null;
		if (isPathValid() == null && isPathExists()) {
			try {
				FileSystem fs = NameNodeVar.getFS();
				FileStatus[] stat = fs.listStatus(new Path(getPath()),
						new PathFilter() {

					@Override
					public boolean accept(Path arg0) {
						return !arg0.getName().startsWith("_");
					}
				});
				ans = new ArrayList<String>(maxToRead);
				for (int i = 0; i < stat.length; ++i) {
					ans.addAll(hdfsInt.select(stat[i].getPath().toString(),
							",",
							(maxToRead / stat.length) + 1));
				}
				try {
					// fs.close();
				} catch (Exception e) {
					logger.error("Fail to close FileSystem: " + e);
				}
			} catch (IOException e) {
				String error = "Unexpected error: " + e.getMessage();
				logger.error(error);
				ans = null;
			}
		}
		return ans;
	}
	

	/**
	 * Set the features list of the data set from the header
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	protected String setFeaturesFromHeader() throws RemoteException {

		logger.info("setFeaturesFromHeader()");

		String header = getProperty(key_header);
		String error = null;

		if (header != null && !header.isEmpty()) {

			String newLabels[] = header.split(",");

			logger.info("setFeaturesFromHeader features " + features);

			if (header.trim().endsWith(",")) {
				error = LanguageManagerWF
						.getText("mapredtexttype.setheaders.wronglabels");
			}

			FeatureList newFL = new OrderedFeatureList();

			try {

				if (newLabels[0].trim().split("\\s+").length > 1) {

					logger.info("setFeaturesFromHeader if ");

					for (int j = 0; j < newLabels.length && error == null; j++) {
						String label = newLabels[j].trim();
						String[] nameType = label.split("\\s+");
						if (nameType.length != 2) {
							error = LanguageManagerWF
									.getText("mapredtexttype.setheaders.wrongpair");
						} else {
							logger.info("nameType[1] " + nameType[0] + " "
									+ nameType[1]);

							if (isVariableName(nameType[0])) {

								try {
									FeatureType ft = FeatureType
											.valueOf(nameType[1].toUpperCase());
									if (ft == null) {
										error = LanguageManagerWF
												.getText(
														"mapredtexttype.msg_error_type_new_header",
														new Object[] { nameType[1] });
									} else {
										newFL.addFeature(nameType[0], ft);
									}
								} catch (Exception e) {
									logger.error(e);
									error = LanguageManagerWF
											.getText(
													"mapredtexttype.msg_error_type_new_header",
													new Object[] { nameType[1] });
								}

							} else {
								error = LanguageManagerWF.getText(
										"mapredtexttype.msg_error_name_header",
										new Object[] { nameType[0] });
							}

						}
					}

				} else {

					logger.info("setFeaturesFromHeader else ");

					logger.info("setFeaturesFromHeader else error  " + error);
					// logger.info("setFeaturesFromHeader else features "+
					// features);

					if (error == null && features != null
							&& features.getFeaturesNames() != null) {
						Iterator<String> it = features.getFeaturesNames()
								.iterator();
						int j = 0;
						while (it.hasNext() && error == null) {
							String featName = it.next();
							logger.info("getFeatureType featName " + featName);

							if (isVariableName(newLabels[j].trim())) {
								newFL.addFeature(newLabels[j].trim(),
										features.getFeatureType(featName));
							} else {
								error = LanguageManagerWF.getText(
										"mapredtexttype.msg_error_name_header",
										new Object[] { newLabels[j].trim() });
								break;
							}

							++j;
						}
					}

				}
			} catch (Exception e) {
				logger.error(e);
				error = LanguageManagerWF
						.getText("mapredtexttype.setheaders.typeunknown");
			}

			if (error == null && !newFL.getFeaturesNames().isEmpty()) {
				setFeatures(newFL);
			}
		}

		logger.info("setFeaturesFromHeader-error " + error);

		return error;
	}
	

	/**
	 * Generate a column name
	 * 
	 * @param columnIndex
	 * @return name
	 */
	private String generateColumnName(int columnIndex) {
		return "FIELD"+(columnIndex+1);
	}
	
	/**
	 * Generate a features list from the data in the current path
	 * 
	 * @return FeatureList
	 * @throws RemoteException
	 */
	protected FeatureList generateFeaturesMap(String delimiter) throws RemoteException {

		logger.info("generateFeaturesMap --");

		FeatureList fl = new OrderedFeatureList();
		try {
			List<String> lines = this.selectLine(2000);
			Map<String,Set<String>> valueMap = new LinkedHashMap<String,Set<String>>();
			Map<String,Integer> nbValueMap = new LinkedHashMap<String,Integer>();
			logger.info(lines);
			if (lines != null) {							
				logger.info("key_delimiter: "
					+ Pattern
					.quote(delimiter));
				for (String line : lines) {
					logger.info("line: " + line);
					boolean full = true;
					if (!line.trim().isEmpty()) {
						int cont = 0;
						for (String s : line.split(Pattern
								.quote(delimiter))) {

							String nameColumn = generateColumnName(cont++);
							if(!valueMap.containsKey(nameColumn)){
								valueMap.put(nameColumn, new LinkedHashSet<String>());
								nbValueMap.put(nameColumn, 0);
							}

							if(valueMap.get(nameColumn).size() < 101){
								full = false;
								valueMap.get(nameColumn).add(s.trim());
								nbValueMap.put(nameColumn,nbValueMap.get(nameColumn)+1);
							}

						}
					}
					if(full){
						break;
					}
				}
				
				Iterator<String> valueIt = valueMap.keySet().iterator();
				while(valueIt.hasNext()){
					String cat = valueIt.next();
					fl.addFeature(cat,getType(valueMap.get(cat),nbValueMap.get(cat)));
				}

			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return fl;

	}
	

	public FeatureType getType(Set<String> exValue, int numberOfValues){
		FeatureType typeAns = null; 
		boolean restart = false;
		do{
			restart = false;
			Iterator<String> featureValueIt = exValue.iterator();
			while(featureValueIt.hasNext() && !restart){
				String featureValue = featureValueIt.next();
				FeatureType typeCur = getType(featureValue);
				logger.info("Value: "+featureValue);
				logger.info("Type ans: "+typeAns);
				logger.info("Type cur: "+typeCur);
				if(typeAns == null){
					typeAns = typeCur;
				}else if(canCast(typeCur,typeAns)){
					//Nothing to do
				}else if(canCast(typeAns,typeCur)){
					//Get the higher type
					typeAns = typeCur;
				}else{
					logger.info("Have to reset the type");
					//Not the good type
					if(typeCur.equals(FeatureType.CHAR) && typeAns.equals(FeatureType.INT)){
						logger.info("Test integer");
						try {
							Integer.valueOf(featureValue);
						} catch (Exception e) {
							typeAns = FeatureType.STRING;
						}
					}else if(typeAns.equals(FeatureType.CHAR) && typeCur.equals(FeatureType.INT)){
						logger.info("Set to int and start again");
						typeAns = FeatureType.INT;
						restart = true;
					}else{
						logger.info("Set to string");
						typeAns = FeatureType.STRING;
					}
				}
			}
			logger.info(restart);
		}while(restart);

		if(typeAns.equals(FeatureType.STRING)){
			int nbValues = exValue.size();
			logger.info(nbValues+" / "+numberOfValues);
			if(nbValues < 101 && nbValues * 100 /numberOfValues < 5){
				typeAns = FeatureType.CATEGORY;
			}
		}
		
		return typeAns;
	}

	/**
	 * Get the FeatureType of
	 * 
	 * @param expr
	 *            to get FeatureType of
	 * @return {@link com.redsqirl.workflow.server.enumeration.FeatureType}
	 */
	public static FeatureType getType(String expr) {

		FeatureType type = null;
		if (expr.equalsIgnoreCase("TRUE") || expr.equalsIgnoreCase("FALSE")) {
			type = FeatureType.BOOLEAN;
		} else {
			if(expr.length() == 1){
				type = FeatureType.CHAR;
			}

			try {
				Integer.valueOf(expr);
				type = FeatureType.INT;
			} catch (Exception e) {
			}
			if (type == null) {
				try {
					Long.valueOf(expr);
					type = FeatureType.LONG;
				} catch (Exception e) {
				}
			}
			if (type == null) {
				try {
					Float.valueOf(expr);
					type = FeatureType.FLOAT;
				} catch (Exception e) {
				}
			}
			if (type == null) {
				try {
					Double.valueOf(expr);
					type = FeatureType.DOUBLE;
				} catch (Exception e) {
				}
			}
			if (type == null && expr.length() < 11) {
				try {
					dateFormat.parse(expr);
					type = FeatureType.DATE;
				} catch (Exception e) {
				}
			}

			if (type == null && expr.length() < 20) {
				try {
					datetimeFormat.parse(expr);
					type = FeatureType.DATETIME;
				} catch (Exception e) {
				}
			}

			if (type == null) {
				try {
					timestampFormat.parse(expr);
					type = FeatureType.TIMESTAMP;
				} catch (Exception e) {
				}
			}

			if (type == null) {
				type = FeatureType.STRING;
			}
		}
		return type;
	}
	

	/**
	 * Check if a feature can be converted from one type to another
	 * 
	 * @param from
	 * @param to
	 * @return <code>true</code> the cast is possible else <code>false</code>
	 */
	public boolean canCast(FeatureType from, FeatureType to) {
		if (from.equals(to)) {
			return true;
		}

		if (from.equals(FeatureType.BOOLEAN)) {
			if (to.equals(FeatureType.STRING) || to.equals(FeatureType.CATEGORY)) {
				return true;
			}
			return false;
		} else{
			int fromNumberIdx = featuresNumberHierarchicalOrder.indexOf(from);
			int toNumberIdx = featuresNumberHierarchicalOrder.lastIndexOf(to);
			if( fromNumberIdx != -1 && toNumberIdx != -1 && fromNumberIdx <= toNumberIdx){
				return true;
			}
			int fromStrIdx = featuresStrHierarchicalOrder.indexOf(from);
			int toStrIdx = featuresStrHierarchicalOrder.lastIndexOf(to);
			if( fromStrIdx != -1 && toStrIdx != -1 && fromStrIdx <= toStrIdx){
				return true;
			}
			int fromDateIdx = featuresDateHierarchicalOrder.indexOf(from);
			int toDateIdx = featuresDateHierarchicalOrder.lastIndexOf(to);
			if( fromDateIdx != -1 && toDateIdx != -1 && fromDateIdx <= toDateIdx){
				return true;
			}
		}
		return false;
	}

}
