package idiro.workflow.server.action;

import idiro.workflow.server.datatype.MapRedTextType;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

/**
 * Action that read a Text Map Reduce Directory.
 * 
 * @author etienne
 * 
 */
public class PigTextSource extends AbstractSource {

	private static final long serialVersionUID = 7519928238030041208L;

	/**
	 * Constructor containing the pages, page checks and interaction
	 * Initialization
	 * 
	 * @throws RemoteException
	 */
	public PigTextSource() throws RemoteException {
		super();
		
		initializeDataTypeInteraction();
		initializeDataSubtypeInteraction();
		addSourcePage();
		
		MapRedTextType type = new MapRedTextType();

		List<String> posValuesSubType = new LinkedList<String>();
		posValuesSubType.add(type.getTypeName());
		dataSubtype.setPossibleValues(posValuesSubType);
		
		dataType.setValue("HDFS");
		dataSubtype.setValue(type.getTypeName());
		checkSubType();
	}

	/**
	 * Get the name of the Action
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException {
		return "pig_text_source";
	}
}
