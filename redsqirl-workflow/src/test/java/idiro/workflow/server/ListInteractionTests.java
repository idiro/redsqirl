package idiro.workflow.server;

import static org.junit.Assert.assertTrue;
import idiro.workflow.test.TestUtils;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.server.ListInteraction;

public class ListInteractionTests {


	protected Logger logger = Logger.getLogger(getClass());

	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass()+"#basic");
		try{
			ListInteraction li = new ListInteraction(
					"id", "name", "legend", 0, 0);
			assertTrue("get Id ", li.getId().equals("id"));
			assertTrue("get Name ", li.getName().equals("name"));
			assertTrue("get Id ", li.getLegend().equals("legend"));
			
			assertTrue("get Value 1", li.getValue() == null);
			assertTrue("get Possible values", li.getPossibleValues().isEmpty());
			li.setValue("my_value");
			assertTrue("get Value 2", li.getValue() == null);
			
			List<String> posValues = new LinkedList<String>();
			posValues.add("my_value");
			li.setPossibleValues(posValues);
			li.setValue("my_value");
			assertTrue("get Value 3", li.getValue().equals("my_value"));
			assertTrue("check 1",li.check() == null);
			
			posValues.remove("my_value");
			posValues.add("val");
			li.setPossibleValues(posValues);
			assertTrue("check 2",li.check() != null);
			
			assertTrue("eq possible values",posValues.equals(li.getPossibleValues()));
		}catch(Exception e){
			logger.error(e);
			assertTrue("Exception thrown: "+e.getMessage(),false);
		}
	}
}
