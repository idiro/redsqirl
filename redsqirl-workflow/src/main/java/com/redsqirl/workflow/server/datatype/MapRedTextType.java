package com.redsqirl.workflow.server.datatype;



import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.idiro.utils.RandomString;
import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Map-Reduce Text output type. Output given when an algorithm return a text
 * format map-reduce directory.
 * 
 * @author etienne
 * 
 */
public class MapRedTextType extends MapRedDir {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8260229620701006942L;
	/** Delimiter Key */
	public final static String key_delimiter = "delimiter";
	
	private static Logger logger = Logger.getLogger(MapRedTextType.class);


	/**
	 * Default Constructor
	 * 
	 * @throws RemoteException
	 */
	public MapRedTextType() throws RemoteException {
		super();
	}

	/**
	 * Constructor with FieldList
	 * 
	 * @param fields
	 * @throws RemoteException
	 */
	public MapRedTextType(FieldList fields) throws RemoteException {
		super(fields);
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
	
	@Override
	public String[] getExtensions() throws RemoteException {
		return new String[]{"*.mrtxt"};
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
		return "/user/" + userName + "/tmp/redsqirl_" + component + "_" + outputName
				+ "_" + RandomString.getRandomName(8)+".mrtxt";
	}

	
	/**
	 * Check if the path is a valid path
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String isPathValid() throws RemoteException {
		List<String> shouldNotHaveExt = new LinkedList<String>();
		shouldNotHaveExt.add(".bz");
		shouldNotHaveExt.add(".bz2");
		return isPathValid(shouldNotHaveExt,null);
	}

	/**
	 * I
	 */
	@Override
	public boolean isPathAutoGeneratedForUser(String userName,
			String component, String outputName) throws RemoteException {
		return getPath().startsWith(
				"/user/" + userName + "/tmp/redsqirl_" + component + "_"
						+ outputName + "_") && getPath().endsWith(".mrtxt");
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
			String l = it.next();
			if(l != null && ! l.isEmpty()){
				String[] line = l.split(
						Pattern.quote(getChar(getProperty(key_delimiter))), -1);
				List<String> fieldNames = getFields().getFieldNames();
				if (fieldNames.size() == line.length) {
					Map<String, String> cur = new LinkedHashMap<String, String>();
					for (int i = 0; i < line.length; ++i) {
						cur.put(fieldNames.get(i), line[i]);
					}
					ans.add(cur);
				} else {
					logger.error("The line size (" + line.length
							+ ") is not compatible to the number of fields ("
							+ fieldNames.size() + "). " + "The splitter is '"
							+ getChar(getProperty(key_delimiter)) + "'.");
					logger.error("Error line: " + l);
					ans = null;
					break;
				}
			}
		}
		return ans;
	}


	/**
	 * Set the FieldList for the data set
	 * 
	 * @param fl
	 * 
	 */
	@Override
	public void setFields(FieldList fl) {
		logger.info("setFields :");
		super.setFields(fl);
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
			setFields(null);
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

				FieldList fl = generateFieldsMap(getChar(getProperty(key_delimiter)));
				
				String error = null;
				String header = getProperty(key_header);
				if (header != null && !header.isEmpty()) {
					logger.info("setFieldsFromHeader --");
					error = setFieldsFromHeader();
					if (error != null) {
						if(checkCompatibility(fl,fields) != null){
							fields = fl;
						}
						throw new RemoteException(error);
					}
				} else {
					if(checkCompatibility(fl,fields) != null){
						fields = fl;
					}
					String myHeader = fl.mountStringHeader();
					addProperty(key_header, myHeader);
					logger.debug(fields.getFieldNames());
					logger.debug(fl.getFieldNames());
				}

				if (fields.getSize() != fl.getSize()) {
					if (header != null && !header.isEmpty()) {
						error = LanguageManagerWF
								.getText("mapredtexttype.setheaders.wronglabels");
					}
					fields = fl;
				} else {
					error = checkCompatibility(fl,fields);
					if (error != null) {
						fields = fl;						
						throw new RemoteException(error);
					}
				}

			}
		}
	}
	
	private String checkCompatibility(FieldList from, FieldList to) throws RemoteException{
		Iterator<String> flIt = from.getFieldNames().iterator();
		Iterator<String> fieldIt = to.getFieldNames()
				.iterator();
		String error = null;
		boolean ok = true;
		int i = 1;
		while (flIt.hasNext() && ok) {
			String nf = flIt.next();
			if (!fieldIt.hasNext()){
				ok = false;
				error = LanguageManagerWF.getText(
						"mapredtexttype.msg_error_number_fields");
			}
			else{
				String of = fieldIt.next();
				logger.info("types field " + i + ": "
						+ from.getFieldType(nf) + " , "
						+ to.getFieldType(of));
				ok &= canCast(from.getFieldType(nf),
						to.getFieldType(of));
				if (!ok) {
					error = LanguageManagerWF.getText(
							"mapredtexttype.msg_error_cannot_cast",
							new Object[] { from.getFieldType(nf),
									to.getFieldType(of) });
				}
				++i;
			}
		}
		return error;
	}

	/**
	 * Compare the current path , FieldList , properties to others
	 * 
	 * @param path
	 * @param fl
	 * @param props
	 * @return <code>true</code> if items are equal else <code>false</code>
	 */
	@Override
	public boolean compare(String path, FieldList fl,
			Map<String, String> props) {
		logger.debug("Comparaison MapRed:");
		logger.debug(this.getPath() + " " + path);
		try {
			logger.debug(fields.getFieldNames() + " "
					+ fl.getFieldNames());
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

		return !(this.getPath() == null || fields == null) && compProps
				&& (this.getPath().equals(path) && fields.equals(fl));
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
		
		String result = null;
		
		if (c != null){
			result = String.valueOf(c);
		}
		
		return result;
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
		return "MediumSlateBlue";
	}

}
