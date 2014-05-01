package idiro.workflow.server.action.test;


import static org.junit.Assert.*;
import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.workflow.server.action.utils.HiveDictionary;
import idiro.workflow.server.enumeration.FeatureType;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

public class HiveDictionaryTests {
	private Logger logger =Logger.getLogger(getClass());
	//@Test
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
	
	@Test
	public void testDistinct() throws Exception{
		HiveDictionary dictionary = HiveDictionary.getInstance();

		Set<String> featureAggreg = new HashSet<String>();
		featureAggreg.add("A");
		
		FeatureList features = new OrderedFeatureList();
		features.addFeature("A", FeatureType.STRING);
		features.addFeature("B", FeatureType.STRING);
		features.addFeature("C", FeatureType.FLOAT);
		features.addFeature("D", FeatureType.FLOAT);
		
		String expression = "DISTINCT(A)";
		assertTrue(dictionary.getReturnType(expression, features).equals("STRING"));
		
		String expression2 = "DISTINCT(C + D)";
		assertTrue(dictionary.getReturnType(expression2, features).equals("NUMBER"));
		
		String expression3 = "DISTINCT(UPPER(A))";
		assertTrue(dictionary.getReturnType(expression3, features).equals("STRING"));
		
		String expression4 = "COUNT(DISTINCT(B))";
		assertTrue(dictionary.getReturnType(expression4, features, featureAggreg).equals("BIGINT"));
	}
}
