package idiro.workflow.server.datatype;

import idiro.hadoop.NameNodeVar;
import idiro.utils.FeatureList;
import idiro.workflow.server.enumeration.DataBrowser;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

public class MapRedBinaryType extends MapRedTextType {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6720621203419913600L;
		
	
	public MapRedBinaryType() throws RemoteException {
		super();
	}

	public MapRedBinaryType(FeatureList features)
			throws RemoteException {
		super(features);
	}

	@Override
	public String getTypeName() throws RemoteException {
		return "BINARY MAP-REDUCE DIRECTORY";
	}
	
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

					for (int i = 0; i < stat.length; ++i) {
						ans.addAll(hdfsInt.selectSeq(stat[i].getPath()
								.toString(),
								getChar(getProperty(key_delimiter)),
								(maxToRead / stat.length) + 1, getFeatures()));
					}
				} catch (IOException e) {
					String error = "Unexpected error: " + e.getMessage();
					logger.error(error);
					ans = null;
				}
			}
		}
		return ans;
	}
}
