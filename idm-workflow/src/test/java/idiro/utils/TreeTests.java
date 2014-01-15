package idiro.utils;

import idiro.workflow.server.enumeration.FeatureType;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

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
		
		logger.info(""+((TreeNonUnique<String>) input).toString());
		logger.info(input.getHead());
		logger.info(input.getFirstChild().getHead());
	}
	
	@Test
	
	public void TreeDepth() throws RemoteException{
		
		Tree<String> tree = new TreeNonUnique<String>("test"); 
		
		tree.add("testl1");
		logger.info(""+((TreeNonUnique<String>) tree).toString());
		
		logger.info(tree.getFirstChild("test2").isEmpty());
		
		
		
	}
	@Test
	public void ListTest(){
		List<String> list = new LinkedList<String>();
		list.add("A");
		list.add("B");
		list.add("C");
		
		Set<String> set = new HashSet();
		set.add("A");
		
		logger.info(list.containsAll(set));
	}

}
