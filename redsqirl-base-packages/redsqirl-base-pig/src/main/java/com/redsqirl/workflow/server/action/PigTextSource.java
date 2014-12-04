package com.redsqirl.workflow.server.action;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.AppendListInteraction;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.datatype.MapRedCtrlATextType;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.oozie.PigAction;
import com.redsqirl.workflow.utils.PigLanguageManager;

/**
 * Action that read a Text Map Reduce Directory.
 * 
 * @author etienne
 * 
 */
public class PigTextSource extends AbstractSource {

	private static final long serialVersionUID = 7519928238030041208L;
	
	private static Logger logger = Logger.getLogger(PigTextSource.class);
	
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
	public PigTextSource() throws RemoteException {
		super(new PigAction());
			
		initializeDataTypeInteraction();
		initializeDataSubtypeInteraction();
		addSourcePage();
		browser.setTextTip(PigLanguageManager.getText("pig.test_source_browser_interaction.header_help"));
		//browser.setEditableHeader(true);
		auditInt= new AppendListInteraction(key_audit,
			  	PigLanguageManager.getText("pig.audit_interaction.title"),
			  	PigLanguageManager.getText("pig.audit_interaction.legend"),
			  	1, 0);
		List<String> auditIntVal = new LinkedList<String>();
		auditIntVal.add(PigLanguageManager.getText("pig.audit_interaction_doaudit"));
		auditInt.setPossibleValues(auditIntVal);
		auditInt.setDisplayCheckBox(true);
		auditInt.setReplaceDisable(true);
		
		String pigParallel = WorkflowPrefManager.getUserProperty(
				WorkflowPrefManager.user_pig_parallel,
				WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_pig_parallel,
						"1"));
		
		parallelInt = new InputInteraction(
				"parallel",
				PigLanguageManager.getText("pig.parallel_interaction.title"),
				PigLanguageManager.getText("pig.parallel_interaction.legend"), 
				2, 0);
		parallelInt.setRegex("^\\d+$");
		parallelInt.setValue(pigParallel);
		
		getSourcePage().addInteraction(auditInt);
		getSourcePage().addInteraction(parallelInt);
		
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
		return "pig_text_source";
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
					
//					Iterator<String> it = getDFEInput().get(key_input).get(0)
//							.getFeatures().getFeaturesNames().iterator();
					
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
	
	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		logger.info("Write queries in file: "+files[0].getAbsolutePath());
		boolean ok = true;
		
		int doAudit = auditInt.getValues().size();
		if(doAudit > 0){
			String toWrite = (new AuditGenerator()).getQuery(
					output.get(out_name), 
					output.get(audit_out_name), 
					parallelInt.getValue());
			ok = toWrite != null;
			if(ok){
				logger.info("Content of "+files[0].getName()+": "+toWrite);
				try {
					FileWriter fw = new FileWriter(files[0]);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(toWrite);	
					bw.close();
	
				} catch (IOException e) {
					ok = false;
					logger.error("Fail to write into the file "+files[0].getAbsolutePath());
				}
			}
		}
		
		return ok;
	}
}
