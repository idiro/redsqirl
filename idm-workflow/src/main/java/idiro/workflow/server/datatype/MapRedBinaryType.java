package idiro.workflow.server.datatype;

import idiro.hadoop.NameNodeVar;
import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.utils.LanguageManagerWF;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

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
	 * Constructor with FeatureList
	 * 
	 * @param features
	 * @throws RemoteException
	 */
	public MapRedBinaryType(FeatureList features) throws RemoteException {
		super(features);
	}

	/**
	 * Get the Type name
	 */
	@Override
	public String getTypeName() throws RemoteException {
		return "BINARY MAP-REDUCE DIRECTORY";
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
	public List<String> select(int maxToRead) throws RemoteException {
		List<String> ans = null;

		if (getFeatures() != null) {
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
								.toString(), delim, maxToRead, getFeatures()));
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
		features = new OrderedFeatureList();
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

				// FeatureList fl = generateFeaturesMap();

				String error = null;
				String header = getProperty(key_header);
				logger.info("header :  " + header);
				if (header != null && !header.isEmpty()) {
					logger.info("setFeaturesFromHeader --");
					error = setFeaturesFromHeader();
					if (error != null) {
						throw new RemoteException(error);
					}
				} else {
					if (features != null) {
						logger.debug(features.getFeaturesNames());
						// logger.debug(fl.getFeaturesNames());
					} else {
						// features = fl;
					}
				}

				if (features.getSize() != features.getSize()) {

					Iterator<String> flIt = features.getFeaturesNames()
							.iterator();
					Iterator<String> featIt = features.getFeaturesNames()
							.iterator();
					boolean ok = true;
					int i = 1;
					while (flIt.hasNext() && ok) {
						String nf = flIt.next();
						String of = featIt.next();
						logger.info("types feat " + i + ": "
								+ features.getFeatureType(nf) + " , "
								+ features.getFeatureType(of));
						// ok &= canCast(features.getFeatureType(nf),
						// features.getFeatureType(of));
						if (!ok) {
							error = LanguageManagerWF.getText(
									"mapredtexttype.msg_error_cannot_cast",
									new Object[] { features.getFeatureType(nf),
											features.getFeatureType(of) });
						}
						++i;
					}
					if (!ok) {
						// features = fl;
						if (error != null) {
							throw new RemoteException(error);
						}
					}
				}

			}
		}

	}

	/**
	 * Set the feature names from the header
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

			logger.info("setFeaturesFromHeader features "
					+ features.getFeaturesNames());

			if (header.trim().endsWith(",")) {
				error = LanguageManagerWF
						.getText("mapredtexttype.setheaders.wronglabels");
			}

			FeatureList newFL = new OrderedFeatureList();

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
								FeatureType ft = FeatureType
										.valueOf(nameType[1].toUpperCase());
								if (ft == null) {
									error = LanguageManagerWF
											.getText(
													"mapredtexttype.msg_error_type_new_header",
													new Object[] { nameType[1] });
								} else {
									logger.info("adding new feat");
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

}
