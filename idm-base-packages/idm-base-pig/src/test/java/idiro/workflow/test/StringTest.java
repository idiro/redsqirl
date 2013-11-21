package idiro.workflow.test;

import org.apache.log4j.Logger;
import org.junit.Test;

public class StringTest {
	
	private Logger logger = Logger.getLogger(getClass());
	
	@Test
	public void SplitTest(){
		String a = "a45_source.A";
		String[] result = a.split("\\.");
		logger.info(result.length);
	}
	@Test
	public void ReplaceTest(){
		String a = "a45_source.A";
		String result = a.replace(".","::");
		logger.info(result);
	}
	

}
