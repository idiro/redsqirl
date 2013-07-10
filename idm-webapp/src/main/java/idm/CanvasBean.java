package idm;


import idiro.workflow.server.interfaces.DataFlow;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class CanvasBean extends BaseBean {

	
	private static Logger logger = Logger.getLogger(CanvasBean.class);
	
	
	
	
	
	
	public void doNew(){

		logger.info("doNew");

	}

	public void doOpen(){

		logger.info("doOpen");

	}

	
	
	
	
	
	
	
	public List<String[]> getHelpItens() throws Exception{
		getworkFlowInterface().addWorkflow("new wf help2");
		DataFlow wf = getworkFlowInterface().getWorkflow("new wf help2");
		wf.loadMenu();
		
		List<String[]> helpList = new ArrayList<String[]>();
		for (String[] e : wf.getAllWA()){
			helpList.add(new String[]{e[0], e[2]});
		}
		return helpList;
	}
	
}
