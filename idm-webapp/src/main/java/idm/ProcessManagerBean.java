package idm;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

public class ProcessManagerBean extends BaseBean implements Serializable{
	
	private static Logger logger = Logger.getLogger(ProcessManagerBean.class);
	
	private List<String[]> processesGrid;
	private String oozieUrl = "";
	
	public List<String[]> getProcessesGrid() throws Exception{
		logger.info("getProcessesGrid");
		if (processesGrid == null){
			if (getOozie() != null){
				processesGrid = getOozie().getJobs();
				logger.info("getProcessesGrid-loading");
			}
			else{
				processesGrid = new ArrayList<String[]>();
			}
		}
		return processesGrid;
	}
	
	public void setProcessesGrid(List<String[]> processesGrid) {
		this.processesGrid = processesGrid;
	}
	
	public void updateProcessesGrid() throws RemoteException, Exception{
		logger.info("updateProcessesGrid");
		if (getOozie() != null){
			processesGrid = getOozie().getJobs();
		}
	}

	public String getOozieUrl() throws Exception{
		return getOozie() != null ? getOozie().getUrl() : oozieUrl;
	}
	
	public void setOozieUrl(String oozieUrl) {
		this.oozieUrl = oozieUrl;
	}

	public void killProcess() throws Exception{
		String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
		getOozie().kill(id);
	}
	
	public void suspendProcess() throws Exception{
		String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
		getOozie().suspend(id);
	}

	public void resumeProcess() throws Exception{
		String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
		getOozie().resume(id);
	}
	
}