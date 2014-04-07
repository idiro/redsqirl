package idm;

import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.interfaces.DataFlow;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class ConfigureTabsBean extends BaseBean implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 4626482566525824607L;
	
	
	protected Map<String, List<String[]>> menuWA;
	private String tabName;
	private String selected;

	private static Logger logger = Logger.getLogger(ConfigureTabsBean.class);
	private static Map<String,String> allWANameWithClassName = null;

	//@PostConstruct
	public void openCanvasScreen()  {
		try {
			if (getworkFlowInterface().getWorkflow("canvas0") == null) {
				getworkFlowInterface().addWorkflow("canvas0");
				DataFlow wf = getworkFlowInterface().getWorkflow("canvas0");
				wf.loadMenu();
				menuWA = wf.loadMenu(getCurrentPage());
				if(allWANameWithClassName == null){
					allWANameWithClassName = wf.getAllWANameWithClassName();
				}
				getworkFlowInterface().removeWorkflow("canvas0");
			}
		} catch (RemoteException e1) {
			e1.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public File getCurrentPage(){
		String currentPage = ((HttpServletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest())
				.getRequestURI();
		File f = null;
		//		logger.info(currentPage);
		if (currentPage != null && !currentPage.isEmpty()) {
			List<Integer> pos = new ArrayList<Integer>();
			for (int i = 0; i < currentPage.length(); i++) {

				if (currentPage.charAt(i) == '/') {
					pos.add(i);
				}
			}
			currentPage = currentPage.substring(0, pos.get(pos.size() - 1));
			try {
				f = new File(
						WorkflowPrefManager
						.getSysProperty(WorkflowPrefManager.sys_tomcat_path)
						+ currentPage);
				if (!f.exists()) {
					f = new File(
							WorkflowPrefManager
							.getSysProperty(WorkflowPrefManager.sys_tomcat_path)
							+ currentPage.substring(pos.get(1)));
				}
				logger.info(f.getAbsolutePath());
			} catch (Exception e) {
				//				logger.info("E");
			}
			
		}

		return f;
	}

	public List<String> getTabs() {
		return new ArrayList<String>(getMenuWA().keySet());
	}

	public List<SelectItem> getMenuActions() throws RemoteException, Exception {
		List<SelectItem> result = new ArrayList<SelectItem>();
		if (allWANameWithClassName == null) {
			openCanvasScreen();
		}
		for (Entry<String, String> e : allWANameWithClassName
				.entrySet()) {
			result.add(new SelectItem(e.getKey(), e.getKey()));
		}
		return result;
	}

	public Map<String, List<String[]>> getMenuWA() {
		if (menuWA == null) {
			openCanvasScreen();
		}
		return menuWA;
	}

	public void setItems(String[] items) {
		if (selected != null) {
			List<String[]> temp = new ArrayList<String[]>();
			for (int i = 0; i < items.length; ++i) {
				temp.add(new String[] { items[i] });
			}
			getMenuWA().put(selected, temp);
		}
	}

	public String[] getItems() {
		String selectedTab = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("selected");
		String[] items = new String[] {};
		if (getMenuWA().containsKey(selectedTab)) {
			items = new String[getMenuWA().get(selectedTab).size()];

			for (int i = 0; i < items.length; ++i) {
				items[i] = getMenuWA().get(selectedTab).get(i)[0];
			}
		}
		return items;
	}

	public void deleteTab() {
		getMenuWA().remove(
				FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("selected"));
		setTabName("");
	}

	public void createTab() {
		getMenuWA().put(tabName, new ArrayList<String[]>());
		setTabName("");
	}

	public void changeTabName() {
		String oldTabName = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("oldName");
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

	public void saveTabs() throws RemoteException {
		try {
			if (getworkFlowInterface().getWorkflow("canvas0") == null) {
				getworkFlowInterface().addWorkflow("canvas0");
				DataFlow wf = getworkFlowInterface().getWorkflow("canvas0");
				Map<String,List<String>> mapMenu = new LinkedHashMap<String,List<String>>();
				for(Entry<String, List<String[]>> cur: getMenuWA().entrySet()){
					List<String> l = new LinkedList<String>();
					Iterator<String[]> it = cur.getValue().iterator();
					while(it.hasNext()){
						l.add(it.next()[0]);
					}
					mapMenu.put(cur.getKey(),l);
				}
				wf.loadMenu(mapMenu);
				selected = null;
				setTabName("");
				wf.saveMenu();
				menuWA = wf.loadMenu(getCurrentPage());
				getworkFlowInterface().removeWorkflow("canvas0");
			}
		} catch (RemoteException e1) {
			e1.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setSelected() {
		selected = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("selected");
	}

	public void setSelected(String selected) {
		this.selected = selected;
	}

	public String getSelected() {
		return selected;
	}

	public void cancelChanges() {
		menuWA = null;
		selected = null;
		setTabName("");
	}

	public List<Entry<String, List<String[]>>> getMenuWAList()
			throws IOException {
		List<Entry<String, List<String[]>>> list = new ArrayList<Entry<String, List<String[]>>>();
		for (Entry<String, List<String[]>> e : getMenuWA().entrySet()) {
			getMenuWA().get(e.getKey());
			list.add(e);
		}
		return list;
	}
}
