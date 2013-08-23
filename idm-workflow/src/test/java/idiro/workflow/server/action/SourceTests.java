package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.interfaces.DFEPage;
import idiro.workflow.test.TestUtils;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

public class SourceTests {

	Logger logger = Logger.getLogger(getClass());

	Map<String,String> getColumns(){
		Map<String,String> ans = new HashMap<String,String>();
		ans.put(HiveInterface.key_columns,"ID STRING, VALUE INT");
		return ans;
	}

	@Test
	public void basic(){
		TestUtils.logTestTitle("SourceTests#basic");
		try{
			HiveInterface hInt = new HiveInterface();
			String new_path1 = "/"+TestUtils.getTableName(1); 
			hInt.delete(new_path1);
			assertTrue("create "+new_path1,
					hInt.create(new_path1, getColumns()) == null
					);


			Source src = new Source();
			assertTrue("check1",
					src.getPageList().get(0).checkPage() != null
					);

			src.update(src.getInteraction(Source.key_datatype));
			Tree<String> dataTypeTree = src.getInteraction(Source.key_datatype).getTree();
			dataTypeTree.getFirstChild("list").getFirstChild("output").add("Hive");
			assertTrue("check2",
					src.getPageList().get(0).checkPage() == null
					);
			
			DFEPage page2 = src.getPageList().get(1); 
			assertTrue("check3"+page2.checkPage(),
					page2.checkPage() == null
					);

			DFEPage page3 = src.getPageList().get(2); 
			src.update(src.getInteraction(Source.key_dataset));
			assertTrue("check4",
					page3.checkPage() != null
					);
			Tree<String> dataSetTree = src.getInteraction(Source.key_dataset).getTree();
			dataSetTree.getFirstChild("browse").getFirstChild("output").add("path").add(new_path1);

			Tree<String> feat1 = dataSetTree.getFirstChild("browse")
					.getFirstChild("output").add("feature");
			feat1.add("name").add("ID");
			feat1.add("type").add("STRING");

			Tree<String> feat2 = dataSetTree.getFirstChild("browse")
					.getFirstChild("output").add("feature");
			feat2.add("name").add("VALUE");
			feat2.add("type").add("INT");

			String error = page3.checkPage(); 
			assertTrue("check5: "+error,
					 error == null
					);

			hInt.delete(new_path1);
			assertTrue("check6",
					page3.checkPage() != null
					);

		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}


}
