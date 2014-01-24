package idiro.workflow.server.action.test;


import idiro.workflow.server.action.utils.HiveDictionary;

import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

public class HiveDictionaryTests {
	private Logger logger =Logger.getLogger(getClass());
	@Test
	public void DictionaryTestLoad(){
		HiveDictionary dictionary = HiveDictionary.getInstance();
//		dictionary.loadDefaultFunctions();
		Set<String> keys = dictionary.getFunctionsMap().keySet();
		
		for(String key : keys){
			String[][] values =dictionary.getFunctionsMap().get(key);
			logger.info(key+ " "+ values.length);
			for(String[] functions :values){
				for(String functionStrings : functions){
					if(functionStrings.contains("@")){
						
						logger.debug(dictionary.convertStringtoHelp(functionStrings));
					}
				}
			}
		}
	}
}
