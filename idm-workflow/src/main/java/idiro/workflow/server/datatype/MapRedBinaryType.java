package idiro.workflow.server.datatype;

import idiro.workflow.server.enumeration.FeatureType;

import java.rmi.RemoteException;
import java.util.Map;

public class MapRedBinaryType extends MapRedTextType {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6720621203419913600L;
		
	
	public MapRedBinaryType() throws RemoteException {
		super();
	}

	public MapRedBinaryType(Map<String, FeatureType> features)
			throws RemoteException {
		super(features);
	}

	@Override
	public String getTypeName() throws RemoteException {
		return "BINARY MAP-REDUCE DIRECTORY";
	}

}
