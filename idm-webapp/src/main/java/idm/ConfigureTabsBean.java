package idm;

import idiro.workflow.server.interfaces.DataFlow;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

public class ConfigureTabsBean extends BaseBean implements Serializable{

	private DataFlow wf;
	protected Map<String,List<String[]>> menuWA;
	private String tabName;
	private String selected;

	@PostConstruct
	public void openCanvasScreen() throws RemoteException {
		if (getworkFlowInterface().getWorkflow("canvas0") == null){
			getworkFlowInterface().addWorkflow("canvas0");
		}
		wf = getworkFlowInterface().getWorkflow("canvas0");
		wf.loadMenu();
	}

	public List<String> getTabs(){
		return new ArrayList<String>(getMenuWA().keySet());
	}



	public List<SelectItem> getMenuActions() throws RemoteException, Exception{
		List<SelectItem> result = new ArrayList<SelectItem>();
		for (Entry<String, String> e : wf.getAllWANameWithClassName().entrySet()){
			result.add(new SelectItem(e.getKey(), e.getKey()));
		}
		return result;
	}

	public Map<String,List<String[]>> getMenuWA(){
		if (menuWA == null){
			try {
				menuWA = wf.getMenuWA();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return menuWA;
	}

	public void setItems(String[] items){
		if (selected != null){
			List<String[]> temp = new ArrayList<String[]>();
			for (int i=0; i < items.length; ++i){
				temp.add(new String[]{items[i]});
			}
			getMenuWA().put(selected, temp);
		}
	}

	public String[] getItems() {  
		String selectedTab = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selected");
		String[] items = new String[]{};
		if (getMenuWA().containsKey(selectedTab)){
			items = new String[getMenuWA().get(selectedTab).size()];

			for (int i = 0; i < items.length; ++i){
				items[i] = getMenuWA().get(selectedTab).get(i)[0];
			}
		}
		return items;  
	} 

	public void deleteTab(){
		getMenuWA().remove(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selected"));
		setTabName("");
	}

	public void createTab(){
		getMenuWA().put(tabName, new ArrayList<String[]>());
		setTabName("");
	}

	public void changeTabName(){
		String oldTabName = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("oldName");
		List<String[]> list = getMenuWA().get(oldTabName);
		getMenuWA().remove(oldTabName);
		getMenuWA().put(selected, list);
	}

	public String getTabName() {
		return tabName;
	}

	public void setTabName(String tabName) {
		this.tabName = tabName;
	}

	public void saveTabs() throws RemoteException{
		wf.setMenuWA(getMenuWA());
		selected = null;
		setTabName("");
		wf.saveMenu();
	}

	public void setSelected(){
		selected = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selected");
	}

	public void setSelected(String selected){
		this.selected = selected;
	}

	public String getSelected(){
		return selected;
	}

	public void cancelChanges(){
		menuWA = null;
		selected = null;
		setTabName("");
	}

	public List<Entry<String, List<String[]>>> getMenuWAList() throws IOException {
		List<Entry<String, List<String[]>>> list = new ArrayList<Entry<String, List<String[]>>>();
		for (Entry<String, List<String[]>> e : getMenuWA().entrySet()){
			getMenuWA().get(e.getKey());
			list.add(e);
		}
		return list;
	}

}