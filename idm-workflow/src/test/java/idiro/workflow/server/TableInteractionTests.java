package idiro.workflow.server;

import static org.junit.Assert.assertTrue;
import idiro.workflow.test.TestUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

public class TableInteractionTests {

	protected Logger logger = Logger.getLogger(getClass());

	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass()+"#basic");
		try{
			TableInteraction ta = new TableInteraction(
					"id", "name", "legend", 0, 0);
			assertTrue("get Id ", ta.getId().equals("id"));
			assertTrue("get Name ", ta.getName().equals("name"));
			assertTrue("get Id ", ta.getLegend().equals("legend"));
			
			ta.addColumn("name", 1, null,null, null);
			
			assertTrue("get Value 1", ta.getValues().isEmpty());
			
			Map<String,String> row = new LinkedHashMap<String,String>();
			row.put("name", "a");
			ta.addRow(row);
			assertTrue("check 1", ta.check() == null);
			assertTrue("get Value 2", ta.getValues().size() == 1);
			ta.addRow(row);
			assertTrue("check 2", ta.check() != null);
			ta.setValues(null);
			row.put("name", "1");
			ta.addRow(row);
			
			assertTrue("check 3", ta.check() == null);
			ta.updateColumnConstraint("name", "[a-z]([a-z0-9_]*)",1, null);
			assertTrue("check 4", ta.check() != null);
			
		}catch(Exception e){
			logger.error(e);
			assertTrue("Exception thrown: "+e.getMessage(),false);
		}
	}
}
