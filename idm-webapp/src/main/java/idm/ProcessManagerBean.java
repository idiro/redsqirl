package idm;

import java.util.List;

import javax.faces.context.FacesContext;

public class ProcessManagerBean extends BaseBean{
	
	public List<String[]> getProcessesGrid() throws Exception{
		return getOozie().getJobs();
	}
	
	public String getOozieUrl() throws Exception{
		return getOozie().getUrl();
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
