package idm;


import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.interfaces.DataFlow;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

public class CanvasBean extends BaseBean implements Serializable{

	
	private static Logger logger = Logger.getLogger(CanvasBean.class);
	private int countObj;
	
	
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
		
		setCountObj(0);
		
		DataFlowInterface dfi;
		try {
			
			dfi = getworkFlowInterface();
			if(dfi != null && dfi.getWorkflow("canvas1") == null){
				dfi.addWorkflow("canvas1");
				logger.info("add new Workflow canvas1");
			}
			
		} catch (RemoteException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
	}
	
	public List<String[]> getHelpItens() throws Exception{
		DataFlow wf = getworkFlowInterface().getWorkflow("canvas1");
		wf.loadMenu();
		
		List<String[]> helpList = new ArrayList<String[]>();
		for (String[] e : wf.getAllWA()){
			helpList.add(new String[]{e[0], e[2]});
		}
		return helpList;
	}

	public int getCountObj() {
		return countObj;
	}

	public void setCountObj(int countObj) {
		this.countObj = countObj;
	}
	
	
	
}