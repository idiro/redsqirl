package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import com.redsqirl.workflow.server.action.AbstractSource;
import com.redsqirl.workflow.server.datatype.HiveType;

/**
 * Action that read a Hive table.
 * 
 * @author etienne
 * 
 */
public class HiveSource extends AbstractSource {

	private static final long serialVersionUID = 7519928238030041208L;

	/**
	 * Constructor containing the pages, page checks and interaction
	 * Initialization
	 * 
	 * @throws RemoteException
	 */
	public HiveSource() throws RemoteException {
		super(null);
		
		initializeDataTypeInteraction();
		initializeDataSubtypeInteraction();
		addSourcePage();
		
		HiveType type = new HiveType();
		
		List<String> posValuesSubType = new LinkedList<String>();
		posValuesSubType.add(type.getTypeName());
		dataSubtype.setPossibleValues(posValuesSubType);

		dataType.setValue(type.getBrowser());
		dataSubtype.setValue(type.getTypeName());
		checkSubType();
	}

	/**
	 * Get the name of the Action
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	@Override
	public String getName() throws RemoteException {
		return "hive_source";
	}
}
