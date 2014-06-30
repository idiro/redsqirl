package idiro.workflow.utils;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.workflow.utils.LanguageManagerWF;

public class LanguageManagerTests {

	private Logger logger = Logger.getLogger("LanguageManagerTests");

	@Test
	public void LanguageManagerTest() {
		List<String> vals = new LinkedList<String>();
		vals.add("test");
		vals.add("test1");
		vals.add("test2");
		
		String error = LanguageManagerWF.getText("workflow.cleanProject",
				new Object[] { vals.get(0) , vals.get(1) , vals.get(2) });
		
		error = error + LanguageManagerWF.getText("workflow.cleanProject",
				new Object[] { "", vals.get(1) , vals.get(2) });
		
		logger.info(error);
	}
}
