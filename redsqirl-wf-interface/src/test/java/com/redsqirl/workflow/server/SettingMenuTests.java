package com.redsqirl.workflow.server;

import static org.junit.Assert.assertTrue;

import java.util.Properties;

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
				"	    \"name\":\"fix_prop\","+
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

			assertTrue("1: Spark Master is "+sm.getPropertyValue("fix_prop.spark_master"), 
					"yarn-cluster".equals(sm.getPropertyValue("fix_prop.spark_master")));

			assertTrue("2: Spark Master is "+sm.getPropertyValue("spark_master"), 
					"yarn".equals(sm.getPropertyValue("spark_master")));
			
			Properties prop = WorkflowPrefManager.getUserProperties();
			String propTemplate = "test.host.t1.spark_master";
			prop.put(propTemplate, "blah");
			WorkflowPrefManager.storeUserProperties(prop);
			
			sm = new SettingMenu("test", obj);
			String newValue = sm.getPropertyValue("host.t1.spark_master");
			assertTrue("3: Spark Master is "+newValue, 
					"blah".equals(newValue));
			
			prop.remove(propTemplate);
			
			//assertTrue("template ", "yarn-cluster-blabla".equals(sm.getMenu().get("host").getPropertyValue("spark_master")));

		}catch(Exception e){
			logger.error(e,e);
			assertTrue("Fail with exception: "+e.getMessage(),false);
		}

	}

}