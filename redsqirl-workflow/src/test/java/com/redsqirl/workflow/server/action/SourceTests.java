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

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.utils.OrderedFieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.connect.HDFSInterface;
import com.redsqirl.workflow.server.datatype.MapRedTextType;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.test.TestUtils;

public class SourceTests {

	Logger logger = Logger.getLogger(getClass());
	static Logger logger2 = Logger.getLogger(SourceTests.class);

	static String getColumns() {
		return "ID STRING, VALUE INT";
	}
	
	public static DataFlowElement createSrc_ID_VALUE(DataFlow w,
			HDFSInterface hInt, String new_path1) throws RemoteException,
			Exception {

		String idSource = w.addElement((new Source()).getName());
		Source src = (Source) w.getElement(idSource);
		
		Map<String,String> prop = new LinkedHashMap<String,String>();
		prop.put(HDFSInterface.key_type,"directory");
		assertTrue("create " + new_path1,
				hInt.create(new_path1, prop) == null);

		src.update(src.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype)
				.getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add(hInt.getBrowserName());
		src.page1.checkPage();

		src.update(src.getInteraction(Source.key_datasubtype));
		Tree<String> dataSubTypeTree = src.getInteraction(
				Source.key_datasubtype).getTree();
		dataSubTypeTree.getFirstChild("list").getFirstChild("output")
				.add(new MapRedTextType().getTypeName());
		src.page2.checkPage();

		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset)
				.getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path")
				.add(new_path1);

		Tree<String> field1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		field1.add("name").add("ID");
		field1.add("type").add("STRING");

		Tree<String> field2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("field");
		field2.add("name").add("VALUE");
		field2.add("type").add("INT");

		String error = src.checkEntry(null);
		assertTrue("source update: " + error, error == null);
		error = src.updateOut();
		assertTrue("source update: " + error, error == null);
	
		//FIXME this is a temp fix
		OrderedFieldList fl = new OrderedFieldList();
		fl.addField("id",FieldType.STRING);
		fl.addField("value",FieldType.INT);
		src.getDFEOutput().get(new Source().getOut_name()).setFields(fl);
		

		assertTrue("number of fields in source should be 2 instead of "
				+ src.getDFEOutput().get(new Source().getOut_name()).getFields()
						.getSize(), src.getDFEOutput().get(new Source().getOut_name())
				.getFields().getSize() == 2);

		assertTrue("field list "
				+ src.getDFEOutput().get(new Source().getOut_name()).getFields()
						.getFieldNames(),
				src.getDFEOutput().get(new Source().getOut_name()).getFields()
						.getFieldNames().contains("id"));
		assertTrue("field list "
				+ src.getDFEOutput().get(new Source().getOut_name()).getFields()
						.getFieldNames(),
				src.getDFEOutput().get(new Source().getOut_name()).getFields()
						.getFieldNames().contains("value"));

		return src;
	}
	
	@Test
	public void basicHDFS() {
		TestUtils.logTestTitle("SourceTests#basicHDFS");
		try {
			HDFSInterface hInt = new HDFSInterface();
			String new_path1 = TestUtils.getPath(12);
			hInt.delete(new_path1);
			
			//assertTrue("create " + new_path1, hInt.create(new_path1, getColumns()) == null);
			
			TestUtils.createStringIntIntfile(new Path(new_path1));
			
			Source src = new Source();
			String error = "";
			src.update(src.getInteraction(Source.key_datatype));
			Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype).getTree();
			dataTypeTree.getFirstChild("list").getFirstChild("output").add(hInt.getBrowserName());
			src.page1.checkPage();

			src.update(src.getInteraction(Source.key_datasubtype));
			Tree<String> dataSubTypeTree = src.getInteraction(Source.key_datasubtype).getTree();
			dataSubTypeTree.getFirstChild("list").getFirstChild("output").add(new MapRedTextType().getTypeName());
			src.page2.checkPage();
			
			src.update(src.getInteraction(Source.key_dataset));
			Tree<String> dataSetTree = src.getInteraction(Source.key_dataset).getTree();
			dataSetTree.getFirstChild("browse").getFirstChild("output").add("path").add(new_path1);
			//dataSetTree.getFirstChild("browse").getFirstChild("output").add("field");
			
				//.add(MapRedTextType.key_header)
				//.add("A INT , B INT , C INT");
			
			Tree<String> field1 = dataSetTree.getFirstChild("browse").getFirstChild("output").add("field");
			field1.add("name").add("A");
			field1.add("type").add("STRING");

			Tree<String> field2 = dataSetTree.getFirstChild("browse").getFirstChild("output").add("field");
			field2.add("name").add("B");
			field2.add("type").add("INT");
			
			Tree<String> field3 = dataSetTree.getFirstChild("browse").getFirstChild("output").add("field");
			field3.add("name").add("C");
			field3.add("type").add("INT");
			
			src.update(src.getInteraction(Source.key_dataset));

			error = null;
			error = src.checkEntry(null);
			assertTrue("source update: " + error, error == null);
			error = src.updateOut();
			assertTrue("error update out : " + error, error == null);

//			WorkflowPrefManager.resetSys();
//			WorkflowPrefManager.resetUser();
		} catch (Exception e) {
			logger.error(e,e);
			assertTrue(e.toString(), false);
		}
	}
	
	
	//@Test
	public void getHelpTest() throws RemoteException {
		try {
			Source src = new Source();
			logger.info(src.getHelp());
			logger.info(src.getImage());
		} catch (Exception e) {
			logger.error(e,e);
			assertTrue(e.getMessage(), false);
		}
	}

}
