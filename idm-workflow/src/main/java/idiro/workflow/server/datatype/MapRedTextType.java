package idiro.workflow.server.datatype;

import idiro.hadoop.NameNodeVar;
import idiro.hadoop.checker.HdfsFileChecker;
import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.RandomString;
import idiro.workflow.server.DataOutput;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.utils.LanguageManagerWF;

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

/**
 * Map-Reduce Text output type. Output given when an algorithm return a text
 * format map-reduce directory.
 * 
 * @author etienne
 * 
 */
public class MapRedTextType extends DataOutput {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8260229620701006942L;
	/** Delimiter Key */
	public final static String key_delimiter = "delimiter";
	/** Header Key */
	public final static String key_header = "header";
	/** HDFS Interface */
	protected static HDFSInterface hdfsInt;


	protected static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	protected static SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	protected static SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");


	protected List<FeatureType> featuresNumberHierarchicalOrder = new ArrayList<FeatureType>();

	protected List<FeatureType> featuresStrHierarchicalOrder = new ArrayList<FeatureType>();

	protected List<FeatureType> featuresDateHierarchicalOrder = new ArrayList<FeatureType>();

	/**
	 * Default Constructor
	 * 
	 * @throws RemoteException
	 */
	public MapRedTextType() throws RemoteException {
		super();
		init();
	}

	/**
	 * Constructor with FeatureList
	 * 
	 * @param features
	 * @throws RemoteException
	 */
	public MapRedTextType(FeatureList features) throws RemoteException {
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

		featuresDateHierarchicalOrder.add(FeatureType.DATE);
		featuresDateHierarchicalOrder.add(FeatureType.DATETIME);
		featuresDateHierarchicalOrder.add(FeatureType.TIMESTAMP);
		featuresDateHierarchicalOrder.add(FeatureType.CATEGORY);
		featuresDateHierarchicalOrder.add(FeatureType.STRING);
	}

	/**
	 * Get the type name
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	@Override
	public String getTypeName() throws RemoteException {
		return "TEXT MAP-REDUCE DIRECTORY";
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

	/**
	 * Generate a path and set it as current path
	 * 
	 * @param userName
	 * @param component
	 * @param outputName
	 * @throws RemoteException
	 */
	@Override
	public void generatePath(String userName, String component,
			String outputName) throws RemoteException {
		setPath(generatePathStr(userName, component, outputName));
	}

	/**
	 * Gernate a path given values
	 * 
	 * @param userName
	 * @param component
	 * @param outputName
	 * @return generated path
	 * @throws RemoteException
	 */
	@Override
	public String generatePathStr(String userName, String component,
			String outputName) throws RemoteException {
		return "/user/" + userName + "/tmp/idm_" + component + "_" + outputName
				+ "_" + RandomString.getRandomName(8);
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
	 * Check if the path is a valid path
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String isPathValid() throws RemoteException {
		String error = null;
		HdfsFileChecker hCh = new HdfsFileChecker(getPath());
		if (!hCh.isInitialized() || hCh.isFile()) {
			error = LanguageManagerWF.getText("mapredtexttype.dirisfile");
		} else {
			FileSystem fs;
			try {
				fs = NameNodeVar.getFS();
				hCh.setPath(new Path(getPath()));
				if (!hCh.isDirectory()) {
					error = LanguageManagerWF.getText("mapredtexttype.nodir");
				}
				FileStatus[] stat = fs.listStatus(new Path(getPath()),
						new PathFilter() {

					@Override
					public boolean accept(Path arg0) {
						return !arg0.getName().startsWith("_");
					}
				});
				for (int i = 0; i < stat.length && error == null; ++i) {
					if (stat[i].isDir()) {
						error = LanguageManagerWF.getText(
								"mapredtexttype.notmrdir",
								new Object[] { getPath() });
					} else {
						try {
							hdfsInt.select(stat[i].getPath().toString(),"", 1);
						} catch (Exception e) {
							error = LanguageManagerWF
									.getText("mapredtexttype.notmrdir");
						}
					}
				}
				try {
					// fs.close();
				} catch (Exception e) {
					logger.error("Fail to close FileSystem: " + e);
				}
			} catch (IOException e) {

				error = LanguageManagerWF.getText("unexpectedexception",
						new Object[] { e.getMessage() });

				logger.error(error);
			}

		}
		// hCh.close();
		return error;
	}

	/**
	 * I
	 */
	@Override
	public boolean isPathAutoGeneratedForUser(String userName,
			String component, String outputName) throws RemoteException {
		return getPath().startsWith(
				"/user/" + userName + "/tmp/idm_" + component + "_"
						+ outputName + "_");
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
	 * Select data from the current path
	 * 
	 * @param maxToRead
	 *            limit
	 * @return List of rows returned
	 * @throws RemoteException
	 */
	public List<Map<String,String>> select(int maxToRead) throws RemoteException {
		List<Map<String,String>> ans = new LinkedList<Map<String,String>>();
		Iterator<String> it = selectLine(maxToRead).iterator();
		while(it.hasNext()){
			String[] line = it.next().split(Pattern.quote(getChar(getProperty(key_delimiter))));
			List<String> featureNames = getFeatures().getFeaturesNames(); 
			if(featureNames.size() == line.length){
				Map<String,String> cur = new LinkedHashMap<String,String>();
				for(int i = 0; i < line.length; ++i){
					cur.put(featureNames.get(i),line[i]);
				}
				ans.add(cur);
			}else{
				ans = null;
				break;
			}
		}
		return ans;
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
							getChar(getProperty(key_delimiter)),
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

	/**
	 * Set the features list of the data set from the header
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	private String setFeaturesFromHeader() throws RemoteException {

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
	 * Set the FeatureList for the data set
	 * 
	 * @param fl
	 * 
	 */
	@Override
	public void setFeatures(FeatureList fl) {
		logger.info("setFeatures :");
		super.setFeatures(fl);
	}

	/**
	 * Generate a features list from the data in the current path
	 * 
	 * @return FeatureList
	 * @throws RemoteException
	 */
	private FeatureList generateFeaturesMap() throws RemoteException {

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
					.quote(getChar(getProperty(key_delimiter))));
				for (String line : lines) {
					logger.info("line: " + line);
					boolean full = true;
					if (!line.trim().isEmpty()) {
						int cont = 0;
						for (String s : line.split(Pattern
								.quote(getChar(getProperty(key_delimiter))))) {

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
			int toNumberIdx = featuresNumberHierarchicalOrder.indexOf(to);
			if( fromNumberIdx != -1 && toNumberIdx != -1 && fromNumberIdx <= toNumberIdx){
				return true;
			}
			int fromStrIdx = featuresStrHierarchicalOrder.indexOf(from);
			int toStrIdx = featuresStrHierarchicalOrder.indexOf(to);
			if( fromStrIdx != -1 && toStrIdx != -1 && fromStrIdx <= toStrIdx){
				return true;
			}
			int fromDateIdx = featuresDateHierarchicalOrder.indexOf(from);
			int toDateIdx = featuresDateHierarchicalOrder.indexOf(to);
			if( fromDateIdx != -1 && toDateIdx != -1 && fromDateIdx <= toDateIdx){
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the FeatureType of
	 * 
	 * @param expr
	 *            to get FeatureType of
	 * @return {@link idiro.workflow.server.enumeration.FeatureType}
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
	 * Get a Default delimiter from text
	 * 
	 * @param text
	 * @return delimiter
	 */
	private String getDefaultDelimiter(String text) {
		if (text.contains("\001")) {
			return "#1";
		} else if (text.contains("\002")) {
			return "#2";
		} else if (text.contains("|")) {
			return "#124";
		}
		return "#1";
	}

	/**
	 * Add a property to the dataset
	 * 
	 * @param key
	 * @param value
	 */
	@Override
	public void addProperty(String key, String value) {

		if (key.equals(key_delimiter) && value.length() == 1) {
			value = "#" + String.valueOf((int) value.charAt(0));
		}
		super.addProperty(key, value);
	}

	/**
	 * Set the path
	 * 
	 * @param path
	 * @throws RemoteException
	 */
	@Override
	public void setPath(String path) throws RemoteException {
		String oldPath = getPath();

		if (path == null) {
			super.setPath(path);
			setFeatures(null);
			return;
		}

		if (!path.equalsIgnoreCase(oldPath)) {

			super.setPath(path);

			logger.info("setPath() " + path);
			if (isPathExists()) {
				List<String> list = selectLine(1);

				if (list != null && !list.isEmpty()) {
					String text = list.get(0);

					if (getProperty(key_delimiter) == null) {
						String delimiter = getDefaultDelimiter(text);

						logger.info("delimiter -> " + delimiter);

						super.addProperty(key_delimiter, delimiter);
					}

				}

				FeatureList fl = generateFeaturesMap();

				String error = null;
				String header = getProperty(key_header);
				if (header != null && !header.isEmpty()) {
					logger.info("setFeaturesFromHeader --");
					error = setFeaturesFromHeader();
					if (error != null) {
						throw new RemoteException(error);
					}
				} else {
					if (features != null) {
						logger.debug(features.getFeaturesNames());
						logger.debug(fl.getFeaturesNames());
					} else {
						features = fl;
					}
				}

				if (features.getSize() != fl.getSize()) {
					if (header != null && !header.isEmpty()) {
						error = LanguageManagerWF
								.getText("mapredtexttype.setheaders.wronglabels");
					}
					features = fl;
				} else {
					Iterator<String> flIt = fl.getFeaturesNames().iterator();
					Iterator<String> featIt = features.getFeaturesNames()
							.iterator();
					boolean ok = true;
					int i = 1;
					while (flIt.hasNext() && ok) {
						String nf = flIt.next();
						String of = featIt.next();
						logger.info("types feat " + i + ": "
								+ fl.getFeatureType(nf) + " , "
								+ features.getFeatureType(of));
						ok &= canCast(fl.getFeatureType(nf),
								features.getFeatureType(of));
						if (!ok) {
							error = LanguageManagerWF.getText(
									"mapredtexttype.msg_error_cannot_cast",
									new Object[] { fl.getFeatureType(nf),
											features.getFeatureType(of) });
						}
						++i;
					}
					if (!ok) {
						features = fl;
						if (error != null) {
							throw new RemoteException(error);
						}
					}
				}

			}
		}

	}

	/**
	 * Compare the current path , FeatureList , properties to others
	 * 
	 * @param path
	 * @param fl
	 * @param props
	 * @return <code>true</code> if items are equal else <code>false</code>
	 */
	@Override
	public boolean compare(String path, FeatureList fl,
			Map<String, String> props) {
		logger.debug("Comparaison MapRed:");
		logger.debug(this.getPath() + " " + path);
		try {
			logger.debug(features.getFeaturesNames() + " "
					+ fl.getFeaturesNames());
		} catch (Exception e) {
		}
		logger.debug(dataProperty + " " + props);

		String delimNew = props.get(key_delimiter);
		if (delimNew != null && delimNew.length() == 1) {
			delimNew = "#" + String.valueOf((int) delimNew.charAt(0));
		}

		boolean compProps = false;
		if (dataProperty != null) {
			String headOld = dataProperty.get(key_header), headNew = props
					.get(key_header), delimOld = dataProperty
					.get(key_delimiter);
			if (headNew == null) {
				compProps = headOld == null;
			} else {
				compProps = headNew.equals(headOld);
			}
			if (compProps) {
				if (delimNew == null) {
					compProps = delimNew == null;
				} else {
					compProps = delimNew.equals(delimOld);
				}
			}
		} else if (props.isEmpty()) {
			compProps = true;
		}

		return !(this.getPath() == null || features == null) && compProps
				&& (this.getPath().equals(path) && features.equals(fl));
	}

	/**
	 * Generate a column name
	 * 
	 * @param columnIndex
	 * @return name
	 */
	private String generateColumnName(int columnIndex) {
		return "COL"+(columnIndex+1);
	}

	/**
	 * Get the character from an ascii value
	 * 
	 * @param asciiCode
	 * @return character
	 */
	protected String getChar(String asciiCode) {
		String result = null;
		if(asciiCode == null){
			//default
			result = "|";
		}else if (asciiCode.startsWith("#")
				&& asciiCode.length() > 1) {
			result = String.valueOf(Character.toChars(Integer.valueOf(asciiCode
					.substring(1))));
		} else {
			result = asciiCode;
		}
		return result;
	}

	/**
	 * Get the delimiter in octal format
	 * 
	 * @return delimiter
	 */
	public String getOctalDelimiter() {
		String asciiCode = getProperty(key_delimiter);
		String result = null;
		if (asciiCode != null && asciiCode.startsWith("#")
				&& asciiCode.length() > 1) {
			result = Integer.toOctalString(Integer.valueOf(asciiCode
					.substring(1)));
			if (result.length() == 2) {
				result = "\\0" + result;
			} else {
				result = "\\" + result;
			}
		}
		return result;
	}

	/**
	 * Get the delimiter to be used in Pig format
	 * 
	 * @return delimiter
	 */
	public String getPigDelimiter() {
		String asciiCode = getProperty(key_delimiter);
		Character c = null;
		if (asciiCode == null) {
			c = '|';
		} else if (asciiCode != null && asciiCode.startsWith("#")
				&& asciiCode.length() > 1) {
			int i = Integer.valueOf(asciiCode.substring(1));
			c = new Character((char) i);
		} else if (asciiCode.length() == 1) {
			c = asciiCode.charAt(0);
		}
		return new String() + c;
	}

	/**
	 * Get the delimiter in either octal or decimal notation
	 * 
	 * @return
	 */
	public String getDelimiterOrOctal() {
		String octal = getOctalDelimiter();
		return octal != null ? octal
				: getProperty(MapRedTextType.key_delimiter);
	}

	@Override
	protected String getDefaultColor() {
		return "Chocolate";
	}

}
