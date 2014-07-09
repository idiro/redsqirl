package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.action.AuditGenerator;
import com.redsqirl.workflow.server.action.PigAudit;
import com.redsqirl.workflow.server.action.PigBinarySource;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.datatype.MapRedCtrlATextType;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;

public class PigAuditTests {


	static Logger logger = Logger.getLogger(PigAuditTests.class);

	
	public static DataFlowElement createPigWithSrc(
			Workflow w,
			DataFlowElement src,
			HDFSInterface hInt) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement((new PigAudit()).getName());
		logger.debug("Pig audit: "+idHS);
		
		PigAudit pig = (PigAudit) w.getElement(idHS);
		
		logger.info(PigBinarySource.out_name+" "+src.getComponentId());
		logger.debug(PigAudit.key_input+" "+idHS);
		
		error = w.addLink(
				PigBinarySource.out_name, src.getComponentId(), 
				PigAudit.key_input, idHS);
		assertTrue("pig select add link: "+error,error == null);
		
		updatePig(w,pig);
		
		
		logger.debug("HS update out...");
		error = pig.updateOut();
		assertTrue("pig select update: "+error,error == null);
		logger.debug("Fields "+pig.getDFEOutput().get(PigAudit.key_output).getFields());
		
		pig.getDFEOutput().get(PigAudit.key_output).generatePath(
				System.getProperty("user.name"), 
				pig.getComponentId(), 
				PigAudit.key_output);
		
		
		return pig;
	}
	
	
	public static void updatePig(
			Workflow w,
			PigAudit pig) throws RemoteException, Exception{

		logger.info("HS update out...");
		String error = pig.updateOut();
		assertTrue("pig select update: "+error,error == null);
	}
	
	
	
	//@Test
	public void basic(){
		
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			HDFSInterface hInt = new HDFSInterface();
			String new_path1 = TestUtils.getPath(1);
			String new_path2 = TestUtils.getPath(2); 
			
			hInt.delete(new_path1);
			hInt.delete(new_path2);
			
			DataFlowElement src = PigTestUtils.createSrc_ID_VALUE(w,hInt,new_path1);
			PigAudit pig = (PigAudit)createPigWithSrc(w,src,hInt);

			pig.getDFEOutput().get(PigAudit.key_output).setSavingState(SavingState.RECORDED);
			pig.getDFEOutput().get(PigAudit.key_output).setPath(new_path2);
			
			logger.info("run...");
			OozieClient wc = OozieManager.getInstance().getOc();
			logger.info("Got Oozie Client");
			error = w.run();
			assertTrue("Job submition failed: "+error, error == null);
			String jobId = w.getOozieJobId();
			if(jobId == null){
				assertTrue("jobId cannot be null", false);
			}
			logger.info(jobId);
			
			// wait until the workflow job finishes printing the status every 10 seconds
		    while(
		    		wc.getJobInfo(jobId).getStatus() == 
		    		org.apache.oozie.client.WorkflowJob.Status.RUNNING) {
		    	System.out.println("Workflow job running ...");
		    	logger.info("Workflow job running ...");
		        Thread.sleep(10 * 1000);
		    }
		    logger.info("Workflow job completed ...");
		    logger.info(wc.getJobInfo(jobId));
		    error = wc.getJobInfo(jobId).toString();
		    assertTrue(error, error.contains("SUCCEEDED"));
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
	
	//@Test
	public void readAudit(){
		TestUtils.logTestTitle(getClass().getName()+"#readAudit");
		String error = null;
		try{
			HDFSInterface hInt = new HDFSInterface();
			String new_path1 = TestUtils.getPath(1);
			hInt.delete(new_path1);
			
			MapRedCtrlATextType output = new MapRedCtrlATextType();
			FieldList fl = new OrderedFieldList();
			fl.addField("Legend", FieldType.STRING);
			fl.addField("AUDIT_ID", FieldType.STRING);
			fl.addField("AUDIT_VALUE", FieldType.STRING);
			
			output.setFields(fl);
			output.setPath(new_path1);
			PigTestUtils.createDistinctValueAuditFile(new Path(new_path1));
			
			AuditGenerator ag = new AuditGenerator();
			Map<String,List<String> > agMap = ag.readDistinctValuesAudit(null,output);
			logger.info(agMap.toString());
			
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			assertTrue(e.getMessage(),false);
		}
	}
	
	@Test
	public void testGenerator(){
		TestUtils.logTestTitle(getClass().getName()+"#readAudit");
		String error = null;
		try{
			HDFSInterface hInt = new HDFSInterface();
			String new_path1 = TestUtils.getPath(1);
			hInt.delete(new_path1);
			
			MapRedCtrlATextType output = new MapRedCtrlATextType();
			FieldList fl = new OrderedFieldList();
			fl.addField("Legend", FieldType.STRING);
			fl.addField("AUDIT_ID", FieldType.STRING);
			fl.addField("AUDIT_VALUE", FieldType.STRING);
			
			output.setFields(fl);
			output.setPath(new_path1);
			PigTestUtils.createDistinctValueAuditFile(new Path(new_path1));
			
			AuditGenerator ag = new AuditGenerator();
			Map<String,List<String> > agMap = ag.readDistinctValuesAudit(null,output);
			logger.info(agMap.toString());
			if (agMap != null) {
				Iterator<String> it = agMap.keySet().iterator();
				while (it.hasNext()) {
					String field = it.next();
					List<Map<String, String>> rowCaseWhen = new LinkedList<Map<String, String>>();
					List<Map<String, String>> rowCaseWhenElse = new LinkedList<Map<String, String>>();
					List<Map<String, String>> rowAllCaseWhen = new LinkedList<Map<String, String>>();
					Iterator<String> itVals = agMap.get(field).iterator();
					String allCase = "";
					while (itVals.hasNext()) {
						String valCur = itVals.next();
						String code = "WHEN ("+ field+" == '"+valCur+"') THEN () ";
						allCase +=code;
						Map<String, String> rowWhen = new LinkedHashMap<String, String>();
						rowWhen.put("table_op_title", "CASE "+code+" END");
						rowWhen.put("table_field_title", valCur);
						rowWhen.put("table_type_title", "STRING");
						rowCaseWhen.add(rowWhen);

						Map<String, String> rowWhenElse = new LinkedHashMap<String, String>();
						rowWhenElse.put("table_op_title", "CASE "+code+" ELSE () END");
						rowWhenElse.put("table_field_title", valCur);
						rowWhenElse.put("table_type_title", "STRING");
						rowCaseWhenElse.add(rowWhenElse);
					}
					Map<String,String> row = new LinkedHashMap<String, String>();
					row.put("table_op_title", "CASE "+allCase+" END");
					row.put("table_field_title", "_SWITCH");
					row.put("table_type_title", "STRING");
					rowAllCaseWhen.add(row);
					logger.info(rowCaseWhen);
					logger.info(rowAllCaseWhen);
					logger.info(rowCaseWhenElse);
				}
			}
			
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			assertTrue(e.getMessage(),false);
		}
	}
}
