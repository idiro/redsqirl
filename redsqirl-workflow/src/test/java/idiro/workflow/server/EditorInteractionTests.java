package idiro.workflow.server;

import static org.junit.Assert.assertTrue;
import idiro.workflow.test.TestUtils;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.server.EditorInteraction;

public class EditorInteractionTests {

	protected Logger logger = Logger.getLogger(getClass());

	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass()+"#basic");
		try{
			EditorInteraction ed = new EditorInteraction(
					"id", "name", "legend", 0, 0);
			assertTrue("get Id ", ed.getId().equals("id"));
			assertTrue("get Name ", ed.getName().equals("name"));
			assertTrue("get Id ", ed.getLegend().equals("legend"));
			
			assertTrue("get Value 1", ed.getValue().isEmpty());

			ed.setValue("my_value");
			assertTrue("get Value 2", ed.getValue().equals("my_value"));

		}catch(Exception e){
			logger.error(e);
			assertTrue("Exception thrown: "+e.getMessage(),false);
		}
	}
	
}
