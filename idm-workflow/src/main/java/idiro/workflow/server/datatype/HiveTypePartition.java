package idiro.workflow.server.datatype;

import idiro.utils.FeatureList;

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

	public static final String usePartition ="partitoned" ;
	
	public HiveTypePartition() throws RemoteException {
		super();
	}
	
	public HiveTypePartition(FeatureList features)
			throws RemoteException {
		super(features);
	}
	

	@Override
	public String getTypeName() throws RemoteException {
		return "Hive Partition";
	}
	
	@Override
	protected String getDefaultColor(){
		return "SkyBlue"; 
	}
}
