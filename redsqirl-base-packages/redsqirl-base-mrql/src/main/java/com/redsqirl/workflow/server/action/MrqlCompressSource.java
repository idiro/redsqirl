package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.AppendListInteraction;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.datatype.MapRedCompressedType;
import com.redsqirl.workflow.server.datatype.MapRedCtrlATextType;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.oozie.MrqlAction;
import com.redsqirl.workflow.utils.MrqlLanguageManager;

/**
 * Action that read a Text Map Reduce Directory.
 * 
 * @author marcos
 * 
 */
public class MrqlCompressSource extends AbstractSource {

	private static final long serialVersionUID = 7519928238030041208L;
	
	private static Logger logger = Logger.getLogger(MrqlCompressSource.class);
	
	/**
	 * Audit output name
	 */
	public static final String audit_out_name = "audit",
							   key_audit="audit";
	
	/**
	 * Audit Interaction
	 */
	protected AppendListInteraction auditInt;
	
	/**
	 * Parallel Interaction
	 */
	public InputInteraction parallelInt;
	
	/**
	 * Constructor containing the pages, page checks and interaction
	 * Initialization
	 * 
	 * @throws RemoteException
	 */
	public MrqlCompressSource() throws RemoteException {
		super(new MrqlAction());
		
		initializeDataTypeInteraction();
		initializeDataSubtypeInteraction();
		addSourcePage();
		
		auditInt= new AppendListInteraction(key_audit,
			  	MrqlLanguageManager.getText("mrql.audit_interaction.title"),
			  	MrqlLanguageManager.getText("mrql.audit_interaction.legend"), 1, 0);
		List<String> auditIntVal = new LinkedList<String>();
		auditIntVal.add(MrqlLanguageManager.getText("mrql.audit_interaction_doaudit"));
		auditInt.setPossibleValues(auditIntVal);
		auditInt.setDisplayCheckBox(true);
		
		getSourcePage().addInteraction(auditInt);
		getSourcePage().addInteraction(parallelInt);
		
		MapRedCompressedType type = new MapRedCompressedType();

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
		return "mrql_compress_source";
	}
	
	
	/**
	 * Update the output
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String updateOut() throws RemoteException {
		
		String error = super.updateOut();
		if (error == null){
			
			int doAudit = auditInt.getValues().size();
			if(doAudit > 0){
			
				if (output.get(audit_out_name) == null) {
					output.put(audit_out_name, new MapRedCtrlATextType());
				}
				
				try {
					FieldList fl = new OrderedFieldList();
					fl.addField("Legend", FieldType.STRING);
					
					Iterator<String> it = output.get(out_name).getFields()
							.getFieldNames().iterator();
					
					while (it.hasNext()) {
						fl.addField("AUDIT_" + it.next(), FieldType.STRING);
					}
					output.get(audit_out_name).setFields(fl);
				} catch (Exception e) {
					error = e.getMessage();
					logger.error(e.getMessage(), e);
				}
				
			}
			else if (output.get(audit_out_name) != null){
				output.remove(audit_out_name);
			}
		}
		return error;
		
	}
}
