package com.redsqirl.utils;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.redsqirl.utils.OrderedFeatureList;
import com.redsqirl.workflow.server.enumeration.FeatureType;
import com.redsqirl.workflow.test.TestUtils;

public class OrderedFeatureListTests{

	Logger logger = Logger.getLogger(getClass());

	private List<Object[]> list;
	
	@Before
	public void init(){
		list = new ArrayList<Object[]>();
		list.add(new Object[]{"A", FeatureType.BOOLEAN});
		list.add(new Object[]{"B", FeatureType.INT});
		list.add(new Object[]{"C", FeatureType.STRING});
	}

	@Test
	public void basic(){
		TestUtils.logTestTitle("PackageManagerTests#basic");
		String error = null;
		try{
			
			OrderedFeatureList featureList = new OrderedFeatureList();
			featureList.addFeature((String) list.get(0)[0], (FeatureType) list.get(0)[1]);
			featureList.addFeature((String) list.get(1)[0], (FeatureType) list.get(1)[1]);
			featureList.addFeature((String) list.get(2)[0], (FeatureType) list.get(2)[1]);
			
			assertTrue("Wrong size", featureList.getSize() == 3);
			
			int cont = 0;
			for (String featureName : featureList.getFeaturesNames()){
				assertTrue("Wrong order", featureName.equals(list.get(cont++)[0]));
			}
			
			cont = 0;
			for (Object[] obj : list){
				assertTrue("Feature not found", featureList.containsFeature((String) obj[0]));
			}
			
			cont = 0;
			for (Object[] obj : list){
				assertTrue("Wrong feature type", featureList.getFeatureType((String) obj[0]).equals(obj[1]));
			}
			
		}catch(Exception e){
			error = "Unexpected exception "+e.getMessage();
			logger.error(error);
			assertTrue(error,false);
		}

	}

}
