package idm;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.richfaces.model.Ordering;

public class ProcessManagerBean extends BaseBean implements Serializable{
	
	private static Logger logger = Logger.getLogger(ProcessManagerBean.class);
	
	private List<String[]> processesGrid;
	private String oozieUrl = "";
	
	private Ordering[] sortingOrder = new Ordering[5];
	private Object filterValue = "";
	
	private String tableState = new String();
	
	public List<String[]> retrievesProcessesGrid() {
		logger.info("getProcessesGrid");
		if (processesGrid == null){
			try {
				if (getOozie() != null){
					processesGrid = getOozie().getJobs();
					logger.info("getProcessesGrid-loading");
				}
				else{
					processesGrid = new ArrayList<String[]>();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return processesGrid;
	}
	
	public void setProcessesGrid(List<String[]> processesGrid) {
		this.processesGrid = processesGrid;
	}
	
	public List<String[]> getProcessesGrid() {
		return processesGrid;
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

	public Ordering[] getSortingOrder() {
		return sortingOrder;
	}

	public void setSortingOrder(Ordering[] sortingOrder) {
		this.sortingOrder = sortingOrder;
	}

	public Object getFilterValue() {
		return filterValue;
	}

	public void setFilterValue(Object filterValue) {
		this.filterValue = filterValue;
	}

	public String getTableState() {
		return tableState;
	}

	public void setTableState(String tableState) {
		this.tableState = tableState;
	}
	
}