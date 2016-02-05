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


public class ConvertFileTextTests {
	/*
	static Logger logger = Logger.getLogger(ConvertPlainTextTests.class);

	public static DataFlowElement createPigWithSrc(Workflow w,
			DataFlowElement src, HDFSInterface hInt) throws RemoteException,
			Exception {
		
		String error = null;
		String idHS = w.addElement((new ConvertPlainText()).getName());
		logger.debug("Pig convert plain file: " + idHS);

		ConvertPlainText pig = (ConvertPlainText) w.getElement(idHS);

		logger.info(PigCompressSource.out_name + " " + src.getComponentId());
		logger.debug(PigSelect.key_input + " " + idHS);

		error = w.addLink(PigCompressSource.out_name, src.getComponentId(),	PigSelect.key_input, idHS);
		assertTrue("pig select add link: " + error, error == null);

		updatePig(w, pig, hInt);

		logger.debug("HS update out...");
		error = pig.updateOut();
		
		pig.getDFEOutput().get(PigSelect.key_output);
		
		assertTrue("pig select update: " + error, error == null);
		logger.debug("Features " + pig.getDFEOutput().get(PigSelect.key_output).getFields());

		pig.getDFEOutput().get(PigSelect.key_output).generatePath(System.getProperty("user.name"), pig.getComponentId(), PigSelect.key_output);

		return pig;
	}

	public static void updatePig(Workflow w, ConvertPlainText pig, HDFSInterface hInt) throws RemoteException, Exception {

		logger.info("update pig...");

//		pig.pigsample.update();
//		pig.pigsample.setValue("0.75");

//		PigOrderInteraction oi = pig.getOrderInt();
//		pig.update(oi);
//		List<String> values = new ArrayList<String>();
//		values.add("ID");
//		oi.setValues(values);

//		ListInteraction ot = (ListInteraction) pig.getInteraction(PigElement.key_order_type);
//		pig.update(oi);
//		ot.setValue("ASCENDING");

//		InputInteraction pl = (InputInteraction) pig.getInteraction(PigElement.key_parallel);
//		pig.update(pl);
//		pl.setValue("1");

		logger.info("HS update out...");
		String error = pig.updateOut();
		assertTrue("pig select update: " + error, error == null);
	}

	@Test
	public void basic() {

		TestUtils.logTestTitle(getClass().getName() + "#basic");
		String error = null;
		try {
			Workflow w = new Workflow("workflow1_" + getClass().getName());
			HDFSInterface hInt = new HDFSInterface();
			String new_path1 = TestUtils.getPath(1);
			String new_path2 = TestUtils.getPath(2);

			hInt.delete(new_path1);

			DataFlowElement src = PigTestUtils.createSrc_ID_VALUE(w, hInt, new_path1);
			ConvertPlainText pig = (ConvertPlainText) createPigWithSrc(w, src, hInt);

			pig.getDFEOutput().get(PigSelect.key_output).setSavingState(SavingState.RECORDED);
			pig.getDFEOutput().get(PigSelect.key_output).setPath(new_path2);

			pig.getDFEOutput().get(PigSelect.key_output);
			
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
			while(wc.getJobInfo(jobId).getStatus() == org.apache.oozie.client.WorkflowJob.Status.RUNNING) {
				System.out.println("Workflow job running ...");
				logger.info("Workflow job running ...");
				Thread.sleep(10 * 1000);
			}
			logger.info("Workflow job completed ...");
			logger.info(wc.getJobInfo(jobId));
			error = wc.getJobInfo(jobId).toString();
			assertTrue(error, error.contains("SUCCEEDED"));
			WorkflowPrefManager.resetSys();
			WorkflowPrefManager.resetUser();
			logger.info(WorkflowPrefManager.pathSysHome);
		} catch (Exception e) {
			logger.error(e.getMessage());
			assertTrue("caught exception : "+e.getMessage(), false);
		}
	}
	*/
}