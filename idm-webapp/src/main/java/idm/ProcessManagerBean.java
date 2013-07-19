package idm;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

public class ProcessManagerBean extends BaseBean{
	
	private List<String[]> processesGrid = new ArrayList<String[]>();
	private String oozieUrl = "";
	
	public List<String[]> getProcessesGrid() throws Exception{
		return getOozie() != null ? getOozie().getJobs() : processesGrid;
	}
	
	public void setProcessesGrid(List<String[]> processesGrid) {
		this.processesGrid = processesGrid;
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