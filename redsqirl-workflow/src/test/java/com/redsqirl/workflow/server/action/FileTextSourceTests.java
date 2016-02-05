/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl.workflow.server.action;


public class FileTextSourceTests {
	/*
	static Logger logger = Logger.getLogger(FileTextSourceTests.class);
	
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
				FileTextSource.no_header_out_name, src.getComponentId(), 
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
		
		logger.info("update pig... update");
		PigTableSelectInteraction tsi = pig.gettSelInt();
		logger.info("update pig... get table select interaction");
		
		pig.update(tsi);
		{
			Tree<String> out = tsi.getTree().getFirstChild("table");
			Tree<String> rowId = out.add("row");
			rowId.add(PigTableSelectInteraction.table_feat_title).add("VALUEP1");
			rowId.add(PigTableSelectInteraction.table_op_title).add("VALUEP1");
			rowId.add(PigTableSelectInteraction.table_type_title).add("STRING");
//			rowId = out.add("row");
//			rowId.add(PigTableSelectInteraction.table_feat_title).add("VALUE_STRING2");
//			rowId.add(PigTableSelectInteraction.table_op_title).add("VALUE_STRING2");
//			rowId.add(PigTableSelectInteraction.table_type_title).add("STRING");
		}

		PigOrderInteraction oi = pig.getOrderInt();
		pig.update(oi);
		
		ListInteraction ot = (ListInteraction) pig.getInteraction(PigElement.key_order_type);
		pig.update(ot);
		ot.setValue("ASCENDING");
		
		InputInteraction pl = (InputInteraction) pig.getInteraction(PigElement.key_parallel);
		pig.update(pl);
		pl.setValue("1");
		
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
			
			String new_path1 = TestUtils.getPath(4);
			String new_path2 = TestUtils.getPath(5); 
			
			hInt.delete(new_path1);
			hInt.delete(new_path2);

			String idSource = w.addElement((new FileTextSource()).getName());
			FileTextSource src = (FileTextSource)w.getElement(idSource);
			
			
			src.update(src.getInteraction(PigTextSource.key_dataset));
			Tree<String> dataSetTree = src.getInteraction(PigTextSource.key_dataset).getTree();
			dataSetTree.getFirstChild("browse").getFirstChild("output").add("path").add("/user/marcos/plain_text1");
			dataSetTree.getFirstChild("browse").getFirstChild("output").add("property").add(MapRedPlainTextType.key_delimiter).add(",");
			
			src.update(src.getInteraction(PigTextSource.key_datasubtype));
			ListInteraction subtypeInt = (ListInteraction)src.getInteraction(PigTextSource.key_datasubtype);
			subtypeInt.setValue(new MapRedPlainTextHeaderType().getTypeName());
			
			src.updateOut();
			src.getDFEOutput().get(FileTextSource.out_name);
			src.getDFEOutput().get(FileTextSource.no_header_out_name);
			
			src.getDFEOutput();
			
			PigSelect pig = (PigSelect)createPigWithSrc(w,src,hInt);

			pig.getDFEOutput().get(PigSelect.key_output).setSavingState(SavingState.RECORDED);
			pig.getDFEOutput().get(PigSelect.key_output).setPath(new_path2);
			
			logger.info("run...");
			error = w.run();
			assertTrue("Job submition failed: "+error, error == null);
			String jobId = w.getOozieJobId();
			OozieClient wc = OozieManager.getInstance().getOc();
			
			// wait until the workflow job finishes printing the status every 10 seconds
		    while(wc.getJobInfo(jobId).getStatus() == org.apache.oozie.client.WorkflowJob.Status.RUNNING) {
		        System.out.println("Workflow job running ...");
		        Thread.sleep(10 * 1000);
		    }
		    logger.info("Workflow job completed ...");
		    error = wc.getJobInfo(jobId).toString();
		    logger.debug(error);
		    assertTrue(error, error.contains("SUCCEEDED"));
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	*/
	
}