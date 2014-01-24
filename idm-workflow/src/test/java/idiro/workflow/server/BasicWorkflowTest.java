package idiro.workflow.server;

import static org.junit.Assert.assertTrue;
import idiro.utils.Tree;
import idiro.workflow.server.action.Convert;
import idiro.workflow.server.action.ConvertTests;
import idiro.workflow.server.action.Source;
import idiro.workflow.server.action.SourceTests;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DataFlow;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.test.TestUtils;

import org.apache.log4j.Logger;

public class BasicWorkflowTest{
	private Logger logger = Logger.getLogger(getClass());
	private DataFlow dfIn,dfOut;
	
	public BasicWorkflowTest(DataFlow dfIn, DataFlow dfOut){
		this.dfIn = dfIn;
		this.dfOut = dfOut;
	}
	
	public void linkCreationDeletion(){
		HiveInterface hiveInt = null;
		HDFSInterface hdfsInt = null;
		
		String new_path1 =TestUtils.getTablePath(1);
		String new_path2 = TestUtils.getPath(2);
		String file_path = TestUtils.getPath(3);
		String error = null;
		try{
			hiveInt = new HiveInterface();
			hdfsInt = new HDFSInterface();
			
			hiveInt.delete(new_path1);
			hdfsInt.delete(new_path2);
			hdfsInt.delete(file_path);
			
			DataFlowElement src = SourceTests.createSrc_ID_VALUE(dfIn,hiveInt,new_path1);
			String source = src.getComponentId();
			Convert conv = (Convert )ConvertTests.createConvertWithSrc(dfIn,src);
			String convert = conv.getComponentId();
			conv.getDFEOutput().get(Convert.key_output).setSavingState(SavingState.RECORDED);
			conv.getDFEOutput().get(Convert.key_output).setPath(new_path2);
			
			
			assertTrue("link out not created",
					dfIn.getElement(source).getOutputComponent().get(Source.out_name)
					.contains(dfIn.getElement(convert)));
			assertTrue("link in not created",
					dfIn.getElement(convert).getInputComponent().get(Convert.key_input)
					.contains(dfIn.getElement(source)));
			
			logger.debug("sort...");
			error = dfIn.topoligicalSort();
			assertTrue(error,error == null);

			logger.debug("remove link...");
			error = dfIn.removeLink(Source.out_name, source,Convert.key_input,convert);
			assertTrue(error, error == null);
			assertTrue("link out not created",
					!dfIn.getElement(source).getOutputComponent().get(Source.out_name)
					.contains(dfIn.getElement(convert)));
			assertTrue("link in not created",
					!dfIn.getElement(convert).getInputComponent().get(Convert.key_input)
					.contains(dfIn.getElement(source)));
			
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
		try{
			hdfsInt.delete(file_path);
			hiveInt.delete(new_path1);
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
	
	
	public void readSaveElementDeletion(){

		HiveInterface hiveInt = null;
		HDFSInterface hdfsInt = null;
		
		String new_path1 =TestUtils.getTablePath(1);
		String new_path2 = TestUtils.getPath(2);
		String file_path = TestUtils.getPath(3);
		String error = null;
		try{
			hiveInt = new HiveInterface();
			hdfsInt = new HDFSInterface();
			
			hiveInt.delete(new_path1);
			hdfsInt.delete(new_path2);
			hdfsInt.delete(file_path);
			
			DataFlowElement src = SourceTests.createSrc_ID_VALUE(dfIn,hiveInt,new_path1);
			String source = src.getComponentId();
			Convert conv = (Convert )ConvertTests.createConvertWithSrc(dfIn,src);
			String convert = conv.getComponentId();
			conv.getDFEOutput().get(Convert.key_output).setSavingState(SavingState.RECORDED);
			conv.getDFEOutput().get(Convert.key_output).setPath(new_path2);
			
			Tree<String> forTreeIn = conv.getFormats().getTree();
			Tree<String> cpiTreeIn = conv.getCpi().getTree();
			
			logger.debug("save workflow...");
			error = dfIn.save(file_path);
			assertTrue(error,error == null);
			
			logger.debug("read workflow...");
			error = dfOut.read(file_path);
			assertTrue(error,error == null);
			
			logger.debug("check...");
			assertTrue("Element not found after saving",dfOut.getElement(convert) != null);

			Tree<String> forTreeOut = ((Convert)dfOut.getElement(convert)).getFormats().getTree();
			Tree<String> cpiTreeOut = ((Convert)dfOut.getElement(convert)).getCpi().getTree();
			logger.debug(forTreeIn.toString());
			logger.debug(forTreeOut.toString());
			assertTrue("The format Tree has been modified during the save/read process", 
					forTreeIn.equals(forTreeOut));
			logger.debug(cpiTreeIn.toString());
			logger.debug(cpiTreeOut.toString());
			assertTrue("The cpi Tree has been modified during the save/read process", 
					cpiTreeIn.equals(cpiTreeOut));
			

			assertTrue("After saving link out not created",
					dfOut.getElement(source).getOutputComponent().get(Source.out_name)
					.contains(dfOut.getElement(convert)));
			assertTrue("After saving link in not created",
					dfOut.getElement(convert).getInputComponent().get(Convert.key_input)
					.contains(dfOut.getElement(source)));
			
			
			logger.debug("remove Element convert...");
			DataFlowElement cvOut = dfOut.getElement(convert);
			error = dfOut.removeElement(convert);
			assertTrue("Element convert found after deleting",dfOut.getElement(convert) == null);
			assertTrue("After element deletion, link not deleted",
					!dfOut.getElement(source).getOutputComponent().get(Source.out_name)
					.contains(cvOut));
			
			logger.debug("remove Element source...");
			error = dfIn.removeElement(source);
			assertTrue("Element source found after deleting",dfIn.getElement(source) == null);
			assertTrue("After element deletion, link not deleted",
					!dfIn.getElement(convert).getInputComponent().get(Convert.key_input)
					.contains(src));
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			assertTrue(false);
		}
		try{
			hdfsInt.delete(file_path);
			hiveInt.delete(new_path1);
		}catch(Exception e){
			logger.error(e.getMessage());
			assertTrue(e.getMessage(),false);
		}
	}
}