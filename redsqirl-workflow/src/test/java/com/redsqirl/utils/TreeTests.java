package com.redsqirl.utils;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.utils.Tree;
import com.redsqirl.utils.TreeNonUnique;
import com.redsqirl.workflow.server.enumeration.FeatureType;

public class TreeTests {

	private Logger logger = Logger.getLogger(getClass());
	
	@Test
	public void TreeTestBasic() throws RemoteException{
		
		Tree<String> input = new TreeNonUnique<String>("table");
		Tree<String> columns = new TreeNonUnique<String>("columns");
		input.add(columns);

		String table_op_title = "Operations";
		String table_feat_title= "Feature";
		String table_type_title= "Type";
		// operation
		columns.add("column").add("title").add(table_op_title);

		// Feature name
		Tree<String> newFeatureName = new TreeNonUnique<String>("column");
		columns.add(newFeatureName);
		newFeatureName.add("title").add(table_feat_title);

		Tree<String> constraintFeat = new TreeNonUnique<String>("constraint");
		newFeatureName.add(constraintFeat);
		constraintFeat.add("count").add("1");

		// Type
		Tree<String> newType = new TreeNonUnique<String>("column");
		columns.add(newType);
		newType.add("title").add(table_type_title);

		Tree<String> constraintType = new TreeNonUnique<String>("constraint");
		newType.add(constraintType);

		Tree<String> valsType = new TreeNonUnique<String>("values");
		constraintType.add(valsType);

		valsType.add("value").add(FeatureType.BOOLEAN.name());
		valsType.add("value").add(FeatureType.INT.name());
		valsType.add("value").add(FeatureType.DOUBLE.name());
		valsType.add("value").add(FeatureType.FLOAT.name());
		valsType.add("value").add("BIGINT");
		
		assertTrue("Input head incorrect",input.getHead().equals("table"));
		assertTrue("Input child incorrect",input.getFirstChild().getHead().equals("columns"));
		assertTrue("Input size",input.getSubTreeList().size() == 1);
		assertTrue("Input size 2",input.getFirstChild().getSubTreeList().size() == 3); 
		logger.info(input.getFirstChild().getHead());
	}
	
	@Test
	
	public void TreeDepth() throws RemoteException{
		
		Tree<String> tree = new TreeNonUnique<String>("test"); 
		tree.add("testl1");
		assertTrue("Search incorrect.",tree.getFirstChild("test2") == null);
		
	}

}
