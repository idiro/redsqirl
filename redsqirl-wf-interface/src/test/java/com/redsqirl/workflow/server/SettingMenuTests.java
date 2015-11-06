package com.redsqirl.workflow.server;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.junit.Test;

import com.redsqirl.workflow.settings.SettingMenu;

public class SettingMenuTests {
	
	Logger logger = Logger.getLogger(SettingMenuTests.class);
	
	public String getJson(){
		return "{"+
						"    \"settings\":"+
						"    ["+
						"	{"+
						"	    \"property\":\"spark_home\","+
						"	    \"scope\":\"system\","+
						"	    \"default\":\"/home/hadoop/spark\","+
						"	    \"type\":\"STRING\","+
						"	    \"Validator\":\"com.redsqirl.mypackage.MyClass.class\""+
						"	},"+
						"	{"+
						"	    \"property\":\"spark_master\","+
						"	    \"scope\":\"user\","+
						"	    \"default\":\"yarn-cluster2\""+
						"	}"+
						"    ],"+
						"    \"tabs\":"+
						"    ["+
						"	{"+
						"	    \"template_name\":\"host\","+
						"	    \"settings\":"+
						"	    ["+
						"		{"+
						"		    \"property\":\"spark_master\","+
						"		    \"scope\":\"any\","+
						"		    \"optional\":false,"+
						"		    \"default\":\"yarn-cluster\""+
						"		},"+
						"		{"+
						"		    \"property\":\"spark_test\","+
						"		    \"scope\":\"any\","+
						"		    \"optional\":false,"+
						"		    \"default\":\"yarn-cluster\""+
						"		}"+
						"	    ]"+
						"	},"+
						"        {"+
						"	    \"name\":\"test\","+
						"	    \"settings\":"+
						"	    ["+
						"		{"+
						"		    \"property\":\"spark_master\","+
						"		    \"scope\":\"any\","+
						"		    \"optional\":false,"+
						"		    \"default\":\"yarn-cluster\""+
						"		}"+
						"	    ]"+
						"	}"+
						"    ]"+
						"}";
	}
	
	@Test
	public void basic(){
		try{
		JSONObject obj = new JSONObject(getJson());
		SettingMenu sm = new SettingMenu("test", obj);
		assertTrue("Spark Home is "+sm.getPropertyValue("spark_home"), 
				"/home/hadoop/spark".equals(sm.getPropertyValue("spark_home")));

		assertTrue("1: Spark Master is "+sm.getPropertyValue("test.spark_master"), 
				"yarn-cluster".equals(sm.getPropertyValue("test.spark_master")));
		assertTrue("2: Spark Master is "+sm.getPropertyValue("spark_master"), 
				"yarn".equals(sm.getPropertyValue("spark_master")));
		}catch(Exception e){
			logger.error(e,e);
		}
		
	}
}
