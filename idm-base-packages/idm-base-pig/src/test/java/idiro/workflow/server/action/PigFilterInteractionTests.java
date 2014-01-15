package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.workflow.server.Workflow;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEPage;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

public class PigFilterInteractionTests {

	Logger logger = Logger.getLogger(getClass());
	
	Map<String,String> getProperties(){
		Map<String,String> ans = new HashMap<String,String>();
		return ans;
	}
	
	public DataFlowElement getSource( Workflow w) throws Exception{
		HDFSInterface hInt = new HDFSInterface();
		String new_path1 = TestUtils.getPath(1);
		
		hInt.delete(new_path1);
		assertTrue("create "+new_path1,
				hInt.create(new_path1, getProperties()) == null
				);
		
		String idSource = w.addElement((new Source()).getName());
		Source src = (Source)w.getElement(idSource);
		

		src.update(src.getInteraction(Source.key_datatype));
		Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype).getTree();
		dataTypeTree.getFirstChild("list").getFirstChild("output").add("HDFS");
		
		src.update(src.getInteraction(Source.key_datasubtype));
		Tree<String> dataSubTypeTree = src.getInteraction(Source.key_datasubtype).getTree();
		dataSubTypeTree.getFirstChild("list").getFirstChild("output").add(new MapRedTextType().getTypeName());

		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset).getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path").add(new_path1);
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("property").add("delimiter").add("#1");
		
		Tree<String> feat1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat1.add("name").add("ID");
		feat1.add("type").add("STRING");

		Tree<String> feat2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat2.add("name").add("VALUE");
		feat2.add("type").add("INT");
		
		Iterator<DFEPage> itPage = src.getPageList().iterator();
		while(itPage.hasNext()){
			String error = itPage.next().checkPage();
			assertTrue("error page: "+error,error == null);
		}
		
		String error = src.updateOut();
		assertTrue("source update: "+error,error == null);
		
		FeatureList fl = new OrderedFeatureList();
		fl.addFeature("ID", FeatureType.STRING);
		fl.addFeature("VALUE", FeatureType.INT);
		src.getDFEOutput().get(Source.out_name).setFeatures(fl);
		
		assertTrue("number of features in source should be 2 instead of " + 
				src.getDFEOutput().get(Source.out_name).getFeatures().getSize(),
				src.getDFEOutput().get(Source.out_name).getFeatures().getSize() == 2);
		
		
		return src;
	}
	
	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			Workflow w = new Workflow("workflow1_"+getClass().getName());
			DataFlowElement src = getSource(w);
			
			String idHs = w.addElement((new PigSelect()).getName());
			PigSelect hs = (PigSelect)w.getElement(idHs);
			
			error = w.addLink(
					Source.out_name, src.getComponentId(), 
					PigSelect.key_input, idHs);
			assertTrue("pig select link: "+error,error == null);
			
			PigFilterInteraction ci = hs.getCondInt();
			
			logger.debug(hs.getDFEInput());
			hs.update(ci);
			Tree<String> cond = ci.getTree()
					.getFirstChild("editor");
			cond.getFirstChild("output").add("VAL < 10");
			
			logger.info(hs.getDFEInput().get(PigElement.key_input).get(0).getFeatures().getFeaturesNames().toString());
			error = ci.check();
			
			assertTrue("check1: VAL does not exist",error != null);
			
			cond.remove("output");
			cond.add("output").add("VALUE < 10");
			error = ci.check();
			assertTrue("check2: "+error,error == null);
			
			
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
}
