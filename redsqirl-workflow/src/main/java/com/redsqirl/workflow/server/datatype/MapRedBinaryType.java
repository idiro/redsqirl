package com.redsqirl.workflow.server.datatype;



import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.log4j.Logger;

import com.idiro.hadoop.NameNodeVar;
import com.idiro.utils.RandomString;
import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Class to read files that are stored in MapReduce Directories and are stored
 * as Binary format
 * 
 * @author keith
 * 
 */
public class MapRedBinaryType extends MapRedTextType {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6720621203419913600L;
	
	private static Logger logger = Logger.getLogger(MapRedBinaryType.class);
	
	/**
	 * Delimier
	 */
	public static final String delim = "\001";

	/**
	 * Default Constructor
	 * 
	 * @throws RemoteException
	 */
	public MapRedBinaryType() throws RemoteException {
		super();
	}

	/**
	 * Constructor with FieldList
	 * 
	 * @param fields
	 * @throws RemoteException
	 */
	public MapRedBinaryType(FieldList fields) throws RemoteException {
		super(fields);
	}

	/**
	 * Get the Type name
	 */
	@Override
	public String getTypeName() throws RemoteException {
		return "BINARY MAP-REDUCE DIRECTORY";
	}
	
	@Override
	public String[] getExtensions() throws RemoteException {
		return new String[]{"*.mrbin"};
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
				+ "_" + RandomString.getRandomName(8)+".mrbin";
	}
	
	/**
	 * Get the Colour of the type
	 * 
	 * @return colour
	 * 
	 */
	protected String getDefaultColor() {
		return "Coral";
	}

	/**
	 * Select data from the current path
	 * 
	 * @param maxToRead
	 * @throws RemoteException
	 */
	@Override
	public List<Map<String,String>> select(int maxToRead) throws RemoteException {
		List<Map<String,String>> ans = new LinkedList<Map<String,String>>();
		Iterator<String> it = selectLine(maxToRead).iterator();
		while(it.hasNext()){
			String[] line = it.next().split(delim);
			List<String> fieldNames = getFields().getFieldNames(); 
			if(fieldNames.size() == line.length){
				Map<String,String> cur = new LinkedHashMap<String,String>();
				for(int i = 0; i < line.length; ++i){
					cur.put(getFields().getFieldNames().get(i),line[i]);
				}
				ans.add(cur);
			}else{
				ans = null;
				break;
			}
		}
		return ans;
	}
	
	@Override
	public List<String> selectLine(int maxToRead) throws RemoteException {
		List<String> ans = null;

		if (getFields() != null) {
			if (isPathValid() == null && isPathExists()) {
				try {
					final FileSystem fs = NameNodeVar.getFS();
					FileStatus[] stat = fs.listStatus(new Path(getPath()),
							new PathFilter() {

								@Override
								public boolean accept(Path arg0) {
									return !arg0.getName().startsWith("_");
								}
							});
					ans = new ArrayList<String>(maxToRead);

					logger.info("stat length : " + stat.length);
					if (maxToRead / stat.length < 1) {
						maxToRead = (int) Math.ceil((maxToRead / stat.length));
					}
					for (int i = 0; i < stat.length; ++i) {
						logger.info("header : "
								+ getChar(getProperty(key_header)));

						ans.addAll(hdfsInt.selectSeq(stat[i].getPath()
								.toString(), delim, maxToRead, getFields()));
					}
				} catch (IOException e) {
					String error = "Unexpected IOException error: " + e.getMessage();
					logger.error(error);
					ans = null;
				}catch (Exception e1){
					String error = "Unexpected Exception error: " + e1.getMessage();
					logger.error(error);
					ans = null;
					
				}
			}
		}
		return ans;
	}

	/**
	 * Set the path for the Binary Type
	 * 
	 * @param path
	 * @throws RemoteException
	 */
	@Override
	public void setPath(String path) throws RemoteException {
		logger.info("setting bin type path : "
				+ this.getClass().getCanonicalName() + " , " + path);

		String oldPath = getPath();
		fields = new OrderedFieldList();
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

				// FieldList fl = generateFieldsMap();

				String error = null;
				String header = getProperty(key_header);
				logger.info("header :  " + header);
				if (header != null && !header.isEmpty()) {
					logger.info("setFieldsFromHeader --");
					error = setFieldsFromHeader();
					if (error != null) {
						throw new RemoteException(error);
					}
				} else {
					if (fields != null) {
						logger.debug(fields.getFieldNames());
						// logger.debug(fl.getFieldsNames());
					} else {
						// fields = fl;
					}
				}

				if (fields.getSize() != fields.getSize()) {

					Iterator<String> flIt = fields.getFieldNames()
							.iterator();
					Iterator<String> fieldIt = fields.getFieldNames()
							.iterator();
					boolean ok = true;
					int i = 1;
					while (flIt.hasNext() && ok) {
						String nf = flIt.next();
						String of = fieldIt.next();
						logger.info("types field " + i + ": "
								+ fields.getFieldType(nf) + " , "
								+ fields.getFieldType(of));
						// ok &= canCast(fields.getFieldType(nf),
						// fields.getFieldType(of));
						if (!ok) {
							error = LanguageManagerWF.getText(
									"mapredtexttype.msg_error_cannot_cast",
									new Object[] { fields.getFieldType(nf),
											fields.getFieldType(of) });
						}
						++i;
					}
					if (!ok) {
						// fields = fl;
						if (error != null) {
							throw new RemoteException(error);
						}
					}
				}

			}
		}

	}

	/**
	 * Set the field names from the header
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	protected String setFieldsFromHeader() throws RemoteException {

		logger.info("setFieldsFromHeader()");

		String header = getProperty(key_header);
		String error = null;

		if (header != null && !header.isEmpty()) {

			String newLabels[] = header.split(",");

			logger.info("setFieldsFromHeader fields "
					+ fields.getFieldNames());

			if (header.trim().endsWith(",")) {
				error = LanguageManagerWF
						.getText("mapredtexttype.setheaders.wronglabels");
			}

			FieldList newFL = new OrderedFieldList();

			try {

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
								FieldType ft = FieldType
										.valueOf(nameType[1].toUpperCase());
								if (ft == null) {
									error = LanguageManagerWF
											.getText(
													"mapredtexttype.msg_error_type_new_header",
													new Object[] { nameType[1] });
								} else {
									logger.info("adding new field");
									newFL.addField(nameType[0], ft);
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

			} catch (Exception e) {
				logger.error(e);
				error = LanguageManagerWF
						.getText("mapredtexttype.setheaders.typeunknown");
			}

			if (error == null && !newFL.getFieldNames().isEmpty()) {
				setFields(newFL);
			}
		}

		logger.info("setFieldsFromHeader-error " + error);

		return error;
	}

}
