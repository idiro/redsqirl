package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.oozie.MrqlAction;
import com.redsqirl.workflow.utils.MrqlLanguageManager;

/**
 * Action that read a Text Map Reduce Directory.
 * 
 * @author etienne
 * 
 */
public class MrqlTextSource extends AbstractSource {

	private static final long serialVersionUID = 7519928238030041208L;
	
	private static Logger logger = Logger.getLogger(MrqlTextSource.class);
	
	/**
	 * Audit output name
	 */
	public static final String audit_out_name = "audit",
							   key_audit="audit";
	
	/**
	 * Constructor containing the pages, page checks and interaction
	 * Initialization
	 * 
	 * @throws RemoteException
	 */
	public MrqlTextSource() throws RemoteException {
		super(new MrqlAction());
			
		initializeDataTypeInteraction();
		initializeDataSubtypeInteraction();
		addSourcePage();
		
		logger.info("MrqlTextSource - addSourcePage ");
		
		browser.setTextTip(MrqlLanguageManager.getText("mrql.test_source_browser_interaction.header_help"));
		//browser.setEditableHeader(true);
		
		MapRedTextType type = new MapRedTextType();

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
	public String getName() throws RemoteException {
		return "mrql_text_source";
	}
}
