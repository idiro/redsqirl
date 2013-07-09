package idiro.workflow.server.connect;

import idiro.workflow.server.connect.interfaces.DataStore;
import idiro.workflow.server.enumeration.FeatureType;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class DSParamProperty 
extends UnicastRemoteObject
implements DataStore.ParamProperty{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String help;
	protected boolean constant,
						editOnly;
	
	protected FeatureType type;
	/**
	 * Create the parameter of a property 
	 * @param help
	 * @param constant
	 * @throws RemoteException
	 */
	public DSParamProperty(String help, boolean constant, boolean editOnly) throws RemoteException {
		super();
		init(help,constant,editOnly,FeatureType.STRING);
	}
	
	/**
	 * Create the parameter of a property 
	 * @param help
	 * @param constant
	 * @throws RemoteException
	 */
	public DSParamProperty(String help, boolean constant, boolean editOnly, FeatureType type) throws RemoteException {
		super();
		init(help,constant,editOnly,type);
	}
	
	private void init(String help, boolean constant, boolean editOnly, FeatureType type){
		this.help = help;
		this.constant = constant;
		this.editOnly = editOnly;
		this.type = type;
	}
	
	
	@Override
	public String getHelp() throws RemoteException {
		return help;
	}

	@Override
	public boolean isConst() throws RemoteException {
		return constant;
	}


	@Override
	public boolean editOnly() throws RemoteException {
		return editOnly;
	}


	@Override
	public FeatureType type() throws RemoteException {
		return type;
	}

}
