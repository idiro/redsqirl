package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import com.redsqirl.workflow.server.action.AbstractSource;
import com.redsqirl.workflow.server.datatype.HiveTypePartition;

/**
 * Action that read a Hive partition.
 * 
 * @author etienne
 * 
 */
public class HivePartitionSource extends AbstractSource {

	private static final long serialVersionUID = 7519928238030041208L;

	/**
	 * Constructor containing the pages, page checks and interaction
	 * Initialization
	 * 
	 * @throws RemoteException
	 */
	public HivePartitionSource() throws RemoteException {
		super();
		
		initializeDataTypeInteraction();
		initializeDataSubtypeInteraction();
		addSourcePage();
		
		HiveTypePartition type = new HiveTypePartition();
		
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
		return "hive_partition_source";
	}
}
