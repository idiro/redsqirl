package idiro.workflow.server.datatype;

import idiro.utils.FeatureList;

import java.rmi.RemoteException;

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
	protected String getDefaultColor() {
		return "Coral";
	}

}
