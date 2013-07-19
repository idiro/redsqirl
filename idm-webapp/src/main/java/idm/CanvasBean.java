package idm;


import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.interfaces.DataFlow;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

public class CanvasBean extends BaseBean {

	
	private static Logger logger = Logger.getLogger(CanvasBean.class);
	
	
	
	
	
	
	public void doNew(){

		logger.info("doNew");

	}

	public void doOpen(){

		logger.info("doOpen");

	}

	/** openCanvas
	 * 
	 * Methods to mount the first canvas
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	@PostConstruct
	public void openCanvas() {
		
		DataFlowInterface dfi;
		try {
			
			dfi = getworkFlowInterface();
			if(dfi != null && dfi.getWorkflow("canvas1") == null){
				dfi.addWorkflow("canvas1");
			}
			
		} catch (RemoteException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
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