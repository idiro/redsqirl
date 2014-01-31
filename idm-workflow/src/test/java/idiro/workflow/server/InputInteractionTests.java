package idiro.workflow.server;

import static org.junit.Assert.assertTrue;
import idiro.workflow.test.TestUtils;

import org.apache.log4j.Logger;
import org.junit.Test;

public class InputInteractionTests {

	protected Logger logger = Logger.getLogger(getClass());

	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass()+"#basic");
		try{
			InputInteraction in = new InputInteraction(
					"id", "name", "legend", 0, 0);
			assertTrue("get Id ", in.getId().equals("id"));
			assertTrue("get Name ", in.getName().equals("name"));
			assertTrue("get Id ", in.getLegend().equals("legend"));
			
			assertTrue("get Value 1", in.getValue() == null);

			in.setValue("my_value");
			assertTrue("get Value 2", in.getValue().equals("my_value"));
			
			in.setRegex("^(#\\d{1,3}|.)?$");
			
			assertTrue("check 1", in.check() != null);
			
			in.setValue("my");
			assertTrue("get Value 3", in.getValue() == null || !in.getValue().equals("my"));
			
			in.setValue("#1");
			assertTrue("get Value 4", in.getValue().equals("#1"));
		}catch(Exception e){
			logger.error(e);
			assertTrue("Exception thrown: "+e.getMessage(),false);
		}
	}
}
