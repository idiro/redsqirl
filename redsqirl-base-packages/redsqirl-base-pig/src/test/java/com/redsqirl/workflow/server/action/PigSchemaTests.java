package com.redsqirl.workflow.server.action;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.junit.Test;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.InputInteraction;
import com.redsqirl.workflow.server.ListInteraction;
import com.redsqirl.workflow.server.OozieManager;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interaction.PigFilterInteraction;
import com.redsqirl.workflow.server.interaction.PigOrderInteraction;
import com.redsqirl.workflow.server.interaction.PigTableSelectInteraction;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;

public class PigSchemaTests {

	static Logger logger = Logger.getLogger(PigSchemaTests.class);

	
	public static DataFlowElement createPigWithSrc(
			Workflow w,
			DataFlowElement src,
			HDFSInterface hInt) throws RemoteException, Exception{
		String error = null;
		String idHS = w.addElement((new PigSelect()).getName());
		logger.debug("Pig select: "+idHS);
		
		PigSelect pig = (PigSelect) w.getElement(idHS);
		
		logger.info(PigCompressSource.out_name+" "+src.getComponentId());
		logger.debug(PigSelect.key_input+" "+idHS);
		
		error = w.addLink(
				PigCompressSource.out_name, src.getComponentId(), 
				PigSelect.key_input, idHS);
		assertTrue("pig select add link: "+error,error == null);
		
		updatePig(w,pig,hInt);
		
		
		logger.debug("HS update out...");
		error = pig.updateOut();
		assertTrue("pig select update: "+error,error == null);
		logger.debug("Features "+pig.getDFEOutput().get(PigSelect.key_output).getFields());
		
		pig.getDFEOutput().get(PigSelect.key_output).generatePath(
				System.getProperty("user.name"), 
				pig.getComponentId(), 
				PigSelect.key_output);
		
		
		return pig;
	}
	
	public static void updatePig(
			Workflow w,
			PigSelect pig,
			HDFSInterface hInt) throws RemoteException, Exception{
		
		logger.info("update pig...");
		
		logger.info("got dfe");
		PigFilterInteraction ci = pig.getCondInt();
		logger.info("update pig... get condition");
		pig.update(ci);
		logger.info("update pig...update");
		Tree<String> cond = ci.getTree()
				.getFirstChild("editor").getFirstChild("output");
		logger.info("update pig...get condition tree");
		cond.add("VALUE < 10");
		logger.info("update pig...add to condition tree");
		
		logger.info("update pig... update");
		PigTableSelectInteraction tsi = pig.gettSelInt();
		logger.info("update pig... get table select interaction");
		
		pig.update(tsi);
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			rowId.add(PigTableSelectInteraction.table_field_title).add("ID");
			rowId.add(PigTableSelectInteraction.table_op_title).add("ID");
			rowId.add(PigTableSelectInteraction.table_type_title).add("STRING");
			rowId = out.add("row");
			rowId.add(PigTableSelectInteraction.table_field_title).add("VALUE");
			rowId.add(PigTableSelectInteraction.table_op_title).add("VALUE");
			rowId.add(PigTableSelectInteraction.table_type_title).add("INT");
		}

		PigOrderInteraction oi = pig.getOrderInt();
		pig.update(oi);
		List<String> values = new ArrayList<String>();
		values.add("ID");
		oi.setValues(values);
		
		ListInteraction ot = (ListInteraction) pig.getInteraction(PigElement.key_order_type);
		pig.update(oi);
		ot.setValue("ASCENDING");
		
		InputInteraction pl = (InputInteraction) pig.getInteraction(PigElement.key_parallel);
		pig.update(pl);
		pl.setValue("1");
		
		InputInteraction dl = (InputInteraction) pig.getInteraction(PigElement.key_delimiter);
		pig.update(dl);
		dl.setValue(",");
		
		ListInteraction st = (ListInteraction) pig.getInteraction(PigElement.key_outputType);
		pig.update(st);
		st.setValue(new MapRedTextType().getTypeName());
		
		logger.info("HS update out...");
		String error = pig.updateOut();
		assertTrue("pig select update: "+error,error == null);
	}
	
	
	
	@Test
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
			PigSelect pig = (PigSelect)createPigWithSrc(w,src,hInt);

			pig.getDFEOutput().get(PigSelect.key_output).setSavingState(SavingState.RECORDED);
			pig.getDFEOutput().get(PigSelect.key_output).setPath(new_path2);
			
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
		    
		    String idSource2 = w.addElement((new PigTextSource()).getName());
			PigTextSource src2 = (PigTextSource)w.getElement(idSource2);
			
			src2.update(src2.getInteraction(PigTextSource.key_dataset));
			Tree<String> dataSetTree = src2.getInteraction(PigTextSource.key_dataset).getTree();
			dataSetTree.getFirstChild("browse").getFirstChild("output").add("path").add(new_path2);
			dataSetTree.getFirstChild("browse").getFirstChild("output").add("property").add(MapRedTextType.key_delimiter).add(",");
			
			error = src2.updateOut();

			assertTrue("source update: "+error,error == null);
			
			assertTrue("number of fields in source should be 2 instead of " + 
					src2.getDFEOutput().get(PigTextSource.out_name).getFields().getSize(),
					src2.getDFEOutput().get(PigTextSource.out_name).getFields().getSize() == 2);
			
			assertTrue("field list " + 
					src2.getDFEOutput().get(PigTextSource.out_name).getFields().getFieldNames(),
					src2.getDFEOutput().get(PigTextSource.out_name).getFields().getFieldNames().contains("ID"));
			assertTrue("field list " + 
					src2.getDFEOutput().get(PigTextSource.out_name).getFields().getFieldNames(),
					src2.getDFEOutput().get(PigTextSource.out_name).getFields().getFieldNames().contains("VALUE"));
		    
		    logger.info(wc.getJobInfo(jobId));
		    error = wc.getJobInfo(jobId).toString();
		    assertTrue(error, error.contains("SUCCEEDED"));
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
}
