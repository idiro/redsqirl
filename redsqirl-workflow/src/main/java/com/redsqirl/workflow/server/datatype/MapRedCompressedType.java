package com.redsqirl.workflow.server.datatype;



import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.LineReader;

import com.idiro.hadoop.NameNodeVar;
import com.idiro.hadoop.checker.HdfsFileChecker;
import com.idiro.utils.RandomString;
import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.enumeration.FieldType;

/**
 * Class to read files that are stored in MapReduce Directories and are stored
 * as Binary format
 * 
 * @author keith
 * 
 */
public class MapRedCompressedType extends MapRedTextType {

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
	public MapRedCompressedType() throws RemoteException {
		super();
	}

	/**
	 * Constructor with FieldList
	 * 
	 * @param fields
	 * @throws RemoteException
	 */
	public MapRedCompressedType(FieldList fields) throws RemoteException {
		super(fields);
	}

	/**
	 * Get the Type name
	 */
	@Override
	public String getTypeName() throws RemoteException {
		return "COMPRESSED MAP-REDUCE DIRECTORY";
	}
	
	@Override
	public String[] getExtensions() throws RemoteException {
		return new String[]{"*.bz", "*.bz2"};
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
				+ "_" + RandomString.getRandomName(8)+".bz2";
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
}
