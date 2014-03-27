package idiro.workflow.server.datatype;

import idiro.utils.FeatureList;
import idiro.workflow.utils.LanguageManagerWF;

import java.rmi.RemoteException;

/**
 * Hive Output Action Type.
 * Optionally, if the action create a new partition, 
 * the following actions will use only the new partition.
 *  
 * @author etienne
 *
 */
public class HiveTypePartition extends HiveType{

	/**
	 * 
	 */
	private static final long serialVersionUID = 937674007867999596L;
	/**
	 * Partition Key
	 */
	public static final String usePartition ="partitoned" ;
	
	/**
	 * Default Constructor
	 * @throws RemoteException
	 */
	public HiveTypePartition() throws RemoteException {
		super();
		addProperty(usePartition, "true");
	}
	/**
	 * Add a property 
	 * @param key
	 * @param value
	 * 
	 */
	@Override
	public void addProperty(String key ,String value){
		if(usePartition.equals(key)){
			if(value != null && value.trim().equalsIgnoreCase("true")){
				super.addProperty(key, "true");
			}else{
				super.addProperty(key, "false");
			}
		}
	}
	/**
	 * Constructor with FeatureList
	 * @param features
	 * @throws RemoteException
	 */
	public HiveTypePartition(FeatureList features)
			throws RemoteException {
		super(features);
	}
	
	/**
	 * Get the type name
	 * @return type name
	 * @throws RemoteException
	 */
	@Override
	public String getTypeName() throws RemoteException {
		return "Hive Partition";
	}
	/**
	 * Get the Colour for the type
	 * @return Colour
	 */
	@Override
	protected String getDefaultColor(){
		return "SkyBlue"; 
	}
	/**
	 * Get the where part of the statement for partitions
	 * @return where statement
	 */
	public String getWhere(){
		String[] where = hInt.getTableAndPartitions(getPath());
		String ans= "";
		if(where.length > 1){
			ans = "( "+where[1];
			for(int i = 2; i < where.length;++i){
				ans = " AND "+where[i];
			}
			ans = ") ";
		}
		return ans;
	}
	/**
	 * Check if the path is valid (may contain partition)
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String isPathValid() throws RemoteException {
		logger.info("hive partition is path valid");
		String error=super.isPathValid();
		if(getPath() == null){
			error = LanguageManagerWF.getText("hivetype.ispathvalid.pathnull");
		}
		if(error == null){
			if (isPathExists()) {
				return hInt.isPathValid(getPath(), features,
						true);
			} else {
				String regex = "[a-zA-Z_]([A-Za-z0-9_]+)";
				if (!hInt.getTableAndPartitions(getPath())[0].matches(regex)) {
					error = LanguageManagerWF
							.getText("hivetype.ispathvalid.invalid");
				}
			}
		}
		return error;
	}
}
