package idiro.workflow.server.datatype;

import idiro.hadoop.NameNodeVar;
import idiro.hadoop.checker.HdfsFileChecker;
import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.RandomString;
import idiro.workflow.server.DataOutput;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.enumeration.DataBrowser;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.utils.LanguageManagerWF;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

	/**
	 * Default Constructor
	 * 
	 * @throws RemoteException
	 */
	public MapRedTextType() throws RemoteException {
		super();
		if (hdfsInt == null) {
			hdfsInt = new HDFSInterface();
		}
		addProperty(key_header, "");
	}

	/**
	 * Constructor with FeatureList
	 * 
	 * @param features
	 * @throws RemoteException
	 */
	public MapRedTextType(FeatureList features) throws RemoteException {
		super(features);
		if (hdfsInt == null) {
			hdfsInt = new HDFSInterface();
		}
		addProperty(key_header, "");
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
	public DataBrowser getBrowser() throws RemoteException {
		return DataBrowser.HDFS;
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
				hCh.setPath(new Path(getPath()).getParent());
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
	@Override
	public List<String> select(int maxToRead) throws RemoteException {
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
	 *         (contains characters with numbers) and has a maximum else <code>false</code>
	 */
	public boolean isVariableName(String name) {
		String regex = "[a-zA-Z]([a-zA-Z0-9_]{0,29})";
		return name.matches(regex);
	}
	/**
	 * Set the features list of the data set from the header
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
	 * @return FeatureList
	 * @throws RemoteException
	 */
	private FeatureList generateFeaturesMap() throws RemoteException {

		logger.info("generateFeaturesMap --");

		FeatureList fl = new OrderedFeatureList();
		try {
			List<String> lines = this.select(10);
			logger.info(lines);
			if (lines != null) {
				for (String line : lines) {
					if (!line.trim().isEmpty()) {
						int cont = 0;

						for (String s : line.split(Pattern
								.quote(getChar(getProperty(key_delimiter))))) {

							String nameColumn = generateColumnName(cont++);

							logger.info("line: " + line);
							logger.info("key_delimiter: "
									+ Pattern
											.quote(getChar(getProperty(key_delimiter))));
							logger.info("s: " + s);
							logger.info("new nameColumn: " + nameColumn);

							FeatureType type = getType(s.trim());
							if (fl.containsFeature(nameColumn)) {
								if (!canCast(type,
										fl.getFeatureType(nameColumn))) {
									fl.addFeature(nameColumn, type);
								}
							} else {
								fl.addFeature(nameColumn, type);
							}

						}

					}
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return fl;

	}
	/**
	 * Get a Default delimiter from text
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
	 * Check if a feature can be converted from one type to another
	 * @param from
	 * @param to
	 * @return <code>true</code> the cast is possible else <code>false</code>
	 */
	private boolean canCast(FeatureType from, FeatureType to) {
		if (from.equals(to)) {
			return true;
		}

		List<FeatureType> features = new ArrayList<FeatureType>();
		features.add(FeatureType.INT);
		features.add(FeatureType.LONG);
		features.add(FeatureType.FLOAT);
		features.add(FeatureType.DOUBLE);
		features.add(FeatureType.STRING);

		if (from.equals(FeatureType.BOOLEAN)) {
			if (to.equals(FeatureType.STRING)) {
				return true;
			}
			return false;
		} else if (features.indexOf(from) <= features.indexOf(to)) {
			return true;
		}
		return false;
	}
	/**
	 * Add a property to the dataset
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
				List<String> list = select(1);

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
	 * @param columnIndex
	 * @return name
	 */
	private String generateColumnName(int columnIndex) {
		if (columnIndex > 25) {
			return generateColumnName(((columnIndex) / 26) - 1)
					+ generateColumnName(((columnIndex) % 26));
		} else
			return String.valueOf((char) (columnIndex + 65));
	}

	/**
	 * Get the character from an ascii value
	 * 
	 * @param asciiCode
	 * @return character
	 */
	protected String getChar(String asciiCode) {
		String result = null;
		if (asciiCode != null && asciiCode.startsWith("#")
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
