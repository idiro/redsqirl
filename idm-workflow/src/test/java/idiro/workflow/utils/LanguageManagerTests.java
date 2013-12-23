package idiro.workflow.utils;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

public class LanguageManagerTests {

	private Logger logger = Logger.getLogger("LanguageManagerTests");

	@Test
	public void LanguageManagerTest() {
		List<String> vals = new LinkedList<String>();
		vals.add("test");
		vals.add("test1");
		vals.add("test2");

		logger.info(LanguageManager.getText("AppendListInteraction.setValues",
				new Object[] { vals.toString() }));
	}
}
