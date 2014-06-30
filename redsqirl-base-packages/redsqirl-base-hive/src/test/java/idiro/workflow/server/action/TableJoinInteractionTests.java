package idiro.workflow.server.action;

import static org.junit.Assert.assertTrue;
import idiro.workflow.server.action.utils.TestUtils;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.action.HiveJoin;
import com.redsqirl.workflow.server.action.HiveSource;
import com.redsqirl.workflow.server.action.HiveTableJoinInteraction;
import com.redsqirl.workflow.server.action.Source;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;

public class TableJoinInteractionTests {

	Logger logger = Logger.getLogger(getClass());
	
	Map<String,String> getColumns(){
		Map<String,String> ans = new HashMap<String,String>();
		ans.put(HiveInterface.key_columns,"ID STRING, VALUE INT");
		return ans;
	}
	
	public DataFlowElement getSource(String path) throws RemoteException{
		HiveInterface hInt = new HiveInterface();
		
		hInt.delete(path);
		assertTrue("create "+path,
				hInt.create(path, getColumns()) == null
				);
		
		HiveSource src = new HiveSource();

		src.update(src.getInteraction(Source.key_dataset));
		Tree<String> dataSetTree = src.getInteraction(Source.key_dataset).getTree();
		dataSetTree.getFirstChild("browse").getFirstChild("output").add("path").add(path);

		Tree<String> feat1 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat1.add("name").add("ID");
		feat1.add("type").add("STRING");

		Tree<String> feat2 = dataSetTree.getFirstChild("browse")
				.getFirstChild("output").add("feature");
		feat2.add("name").add("VALUE");
		feat2.add("type").add("INT");
		
		String error = src.updateOut();
		assertTrue("source update: "+error,error == null);
		
		return src;
	}
	
	@Test
	public void basic(){
		TestUtils.logTestTitle(getClass().getName()+"#basic");
		String error = null;
		try{
			String new_path1 = TestUtils.getTablePath(1);
			String new_path2 = TestUtils.getTablePath(2);
			DataFlowElement src1 = getSource(new_path1);
			DataFlowElement src2 = getSource(new_path2);
			HiveJoin hs = new HiveJoin();
			src1.setComponentId("a1");
			src2.setComponentId("a2");
			hs.setComponentId("a3");
			//src1
			error = src1.addOutputComponent(Source.out_name, hs);
			assertTrue("source add output: "+error,error == null);
			error = hs.addInputComponent(HiveJoin.key_input, src1);
			assertTrue("hive select add input: "+error,error == null);
			//src2
			error = src2.addOutputComponent(Source.out_name, hs);
			assertTrue("source add output: "+error,error == null);
			error = hs.addInputComponent(HiveJoin.key_input, src2);
			assertTrue("hive select add input: "+error,error == null);

			String alias1 = null;
			String alias2 = null;
			Iterator<String> itAlias = hs.getAliases().keySet().iterator();
			while(itAlias.hasNext()){
				String swp = itAlias.next();
				if(hs.getAliases().get(swp).getPath().equals(new_path1)){
					alias1 = swp;
				}else{
					alias2 = swp;
				}
			}
			
			logger.debug(hs.getDFEInput());
			
			hs.getFilterInt().update();
			hs.getJrInt().update();
			logger.debug("base update...");
			//hs.getJoinTypeInt().update();
			HiveTableJoinInteraction tji = hs.gettJoinInt();
			hs.update(tji);
			logger.debug("tabje join interaction updated...");
			{
				error = tji.check();
				assertTrue("Should at least have one entry",error != null);
			}
			{
				Tree<String> out = tji.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(HiveTableJoinInteraction.table_op_title).add(alias1+".ID");
				rowId.add(HiveTableJoinInteraction.table_feat_title).add("ID");
				rowId.add(HiveTableJoinInteraction.table_type_title).add("STRING");
				logger.debug("5");
				rowId = out.add("row");
				rowId.add(HiveTableJoinInteraction.table_op_title).add(alias2+".VAL");
				rowId.add(HiveTableJoinInteraction.table_feat_title).add("ID");
				rowId.add(HiveTableJoinInteraction.table_type_title).add("STRING");
				error = tji.check();
				assertTrue("check "+error,error != null);
				out.remove("row");
			}
			{
				Tree<String> out = tji.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(HiveTableJoinInteraction.table_op_title).add(alias1+".ID");
				rowId.add(HiveTableJoinInteraction.table_feat_title).add("ID");
				rowId.add(HiveTableJoinInteraction.table_type_title).add("STRING");
				logger.debug("5");
				rowId = out.add("row");
				rowId.add(HiveTableJoinInteraction.table_op_title).add("a3.VALUE");
				rowId.add(HiveTableJoinInteraction.table_feat_title).add("ID");
				rowId.add(HiveTableJoinInteraction.table_type_title).add("STRING");
				error = tji.check();
				assertTrue("check "+error,error != null);
				out.remove("row");
			}
			{
				Tree<String> out = tji.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(HiveTableJoinInteraction.table_op_title).add(alias1+".ID");
				rowId.add(HiveTableJoinInteraction.table_feat_title).add("ID");
				rowId.add(HiveTableJoinInteraction.table_type_title).add("STRING");
				error = tji.check();
				assertTrue("check "+error,error == null);
				out.remove("row");
			}
			{
				Tree<String> out = tji.getTree().getFirstChild("table");
				logger.debug("3");
				Tree<String> rowId = out.add("row");
				logger.debug("4");
				rowId.add(HiveTableJoinInteraction.table_op_title).add(alias1+".ID");
				rowId.add(HiveTableJoinInteraction.table_feat_title).add("ID");
				rowId.add(HiveTableJoinInteraction.table_type_title).add("STRING");
				logger.debug("5");
				rowId = out.add("row");
				rowId.add(HiveTableJoinInteraction.table_op_title).add(alias2+".VALUE");
				rowId.add(HiveTableJoinInteraction.table_feat_title).add("VALUE");
				rowId.add(HiveTableJoinInteraction.table_type_title).add("INT");
				error = tji.check();
				assertTrue("check "+error,error == null);
				out.remove("row");
			}
			
			
			
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			assertTrue(e.getMessage(),false);
		}
	}
}
