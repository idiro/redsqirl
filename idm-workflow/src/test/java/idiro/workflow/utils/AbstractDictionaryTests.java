package idiro.workflow.utils;

import static org.junit.Assert.*;

import java.util.HashMap;

import idiro.workflow.server.action.AbstractDictionary;

import org.apache.log4j.Logger;
import org.junit.Test;

public class AbstractDictionaryTests {
	
	private Logger logger = Logger.getLogger("AbstractDictionaryTests");
	
	@Test
	public void abstractDisctionaryTest(){
		AbstractDictionary dict = new AbstractDictionary() {
			
			@Override
			protected void loadDefaultFunctions() {
				functionsMap = new HashMap<String, String[][]>();
				functionsMap
				.put("conditionalOperator",
							new String[][] {
								new String[] {
										">=",
										"ANY,ANY",
										"BOOLEAN",
										"@function:>=@short:Greater or equal@param:Any value@param:Any value@description:Compare the left value to the right and checks if the left value is greater or equal to the right@example:5>=1 returns TRUE" },
				});
				
			}
			
			@Override
			protected String getNameFile() {
				return "functionsAbsrtact";
			}
		};
		String help = dict.getFunctionsMap().get("conditionalOperator")[0][3];
		logger.info(help);
		String convertHelp = dict.convertStringtoHelp(help);
		logger.info(convertHelp);
		assertTrue(convertHelp.contains("div"));
	}
	
}
