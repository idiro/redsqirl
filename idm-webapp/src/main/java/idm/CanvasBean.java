package idm;


import idm.BaseBean;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import idiro.workflow.server.interfaces.DataFlow;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.model.SelectItem;

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
