package com.redsqirl.workflow.server.action;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.datatype.MapRedCompressedType;
import com.redsqirl.workflow.server.datatype.MapRedCtrlATextType;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.oozie.PigAction;
import com.redsqirl.workflow.utils.PigLanguageManager;

public class PigAudit extends DataflowAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4652143460618748778L;

	private static Logger logger = Logger.getLogger(PigAudit.class);
	
	/**
	 * Names of different elements
	 */
	public static final String key_output = "", key_input = "in";

	
	public InputInteraction parallelInt;
	/**
	 * entries
	 */
	protected static Map<String, DFELinkProperty> input;

	public PigAudit() throws RemoteException {
		super(new PigAction());
		init();

		Page page1 = addPage(
				PigLanguageManager.getText("pig.audit_page1.title"),
				PigLanguageManager.getText("pig.audit_page1.legend"), 1);

		String pigParallel = WorkflowPrefManager.getUserProperty(
				WorkflowPrefManager.user_pig_parallel,
				WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_pig_parallel,
						"1"));
		
		parallelInt = new InputInteraction(
				"parallel",
				PigLanguageManager.getText("pig.parallel_interaction.title"),
				PigLanguageManager.getText("pig.parallel_interaction.legend"), 
				0, 0);
		parallelInt.setRegex("^\\d+$");
		parallelInt.setValue(pigParallel);
		
		page1.addInteraction(parallelInt);
	}

	/**
	 * Initiate the object
	 * 
	 * @throws RemoteException
	 */
	protected void init() throws RemoteException {
		if (input == null) {
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(MapRedCompressedType.class, 1, 1));
			input = in;
		}

	}

	public String getName() throws RemoteException {
		return "pig_audit";
	}

	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

	public String updateOut() throws RemoteException {
		if (output.get(key_output) == null) {
			output.put(key_output, new MapRedCtrlATextType());
		}
		try {
			FieldList fl = new OrderedFieldList();
			fl.addField("Legend", FieldType.STRING);
			Iterator<String> it = getDFEInput().get(key_input).get(0)
					.getFields().getFieldNames().iterator();
			while (it.hasNext()) {
				fl.addField("AUDIT_" + it.next(), FieldType.STRING);
			}
			output.get(key_output).setFields(fl);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	

	

	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		logger.info("Write queries in file: "+files[0].getAbsolutePath());
		String toWrite = (new AuditGenerator()).getQuery(
				getDFEInput().get(key_input).get(0), 
				getDFEOutput().get(key_output), 
				parallelInt.getValue());
		boolean ok = toWrite != null;
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
		
		return ok;
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {

	}
}
