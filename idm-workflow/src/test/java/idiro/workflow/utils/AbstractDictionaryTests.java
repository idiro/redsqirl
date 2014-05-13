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
										"(() ? () : ())",
										"BOOLEAN,ANY,ANY",
										"ANY",
										"@function: (TEST) ? (EXPRESSION1) : (EXPRESSION2)@short:Returns one of two expressions depending on a condition@param:TEST Any Boolean expression@param:EXPRESSION1 An expression returned if test is true@param:EXPRESSION2 An expression returned if test is false@example: (TRUE) ? ('VALUE1') : ('VALUE2') returns 'VALUE1'"}
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
