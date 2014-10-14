package com.redsqirl;

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
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.redsqirl.auth.UserInfoBean;
import com.redsqirl.dynamictable.SelectableRow;
import com.redsqirl.dynamictable.SelectableRowFooter;
import com.redsqirl.dynamictable.SelectableTable;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.utils.SuperActionManager;

public class ConfigureTabsBean extends BaseBean implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 4626482566525824607L;

	protected Map<String, List<String[]>> menuWA;
	private Map<String,String> allWANameWithClassName = null;
	private LinkedList<String> menuActions;
	private List<String> tabs;
	private LinkedList<String> columnIds;
	private LinkedList<String> target;
	private SelectableTable tableGrid = new SelectableTable();
	private Integer index;
	private String showTab = "N";

	private String workflowNameTmp = "wf-footer-123";
	
	/**
	 * Value to give when index is null
	 */
	private SelectableRowFooter menuNull = new SelectableRowFooter(
			new String[3], getMenuActions());

	private static Logger logger = Logger.getLogger(ConfigureTabsBean.class);


	public ConfigureTabsBean(){
	}

	//@PostConstruct
	public void openCanvasScreen() {

		if(menuWA != null){
			showTab = "Y";
		}else{
			try {
				if (getworkFlowInterface().getWorkflow(workflowNameTmp) == null) {
					getworkFlowInterface().addWorkflow(workflowNameTmp);
				}
				
				DataFlow wf = getworkFlowInterface().getWorkflow(workflowNameTmp);
				wf.loadMenu();
				menuWA = wf.getRelativeMenu(getCurrentPage());
				if(allWANameWithClassName == null){
					allWANameWithClassName = wf.getAllWANameWithClassName();
					logger.info(allWANameWithClassName.keySet());
				}
				getworkFlowInterface().removeWorkflow(workflowNameTmp);

				mountMenuActions();
				
				menuNull = new SelectableRowFooter(
						new String[3], getMenuActions());
				setTabs(new LinkedList<String>(getMenuWA().keySet()));

				setColumnIds(new LinkedList<String>());
				getColumnIds().add("Name");
				setTableGrid(new SelectableTable(columnIds));

				for (String name : getMenuWA().keySet()) {
					String[] value = new String[1];
					value[0] = name;
					retrieveItems(name);
					getTableGrid().getRows().add(new SelectableRowFooter(value, getMenuActions(), getTarget()));
				}

				if(getMenuWA().isEmpty()){
					setIndex(null);
				}else{
					setIndex(0);
				}


			} catch (RemoteException e1) {
				e1.printStackTrace();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void mountMenuActions() throws RemoteException, Exception {
		
		logger.info("mountMenuActions");
		
		LinkedList<String> result = new LinkedList<String>();
		for (Entry<String, String> e : allWANameWithClassName.entrySet()) {
			result.add(e.getKey());
		}
		
		FacesContext context = FacesContext.getCurrentInstance();
		UserInfoBean userInfoBean = (UserInfoBean) context.getApplication().evaluateExpressionGet(context, "#{userInfoBean}", UserInfoBean.class);
		logger.info("User: " + userInfoBean.getUserName());
		
		List<String> listSuperAction = new SuperActionManager().getAvailableSuperActions(userInfoBean.getUserName());
		for (String name : listSuperAction) {
			result.add(name);
		}

		setMenuActions(result);
	}

	public Map<String, List<String[]>> getMenuWA() {
		if (menuWA == null) {
			openCanvasScreen();
		}
		return menuWA;
	}

	public void retrieveItems() throws RemoteException, Exception {
		logger.info("retrieveItems");
		String selectedTab = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selected");
		if(selectedTab != null){
			if(selectedTab.equals("new")){
				if(getTableGrid().getRows().size() > 0){
					setIndex(getTableGrid().getRows().size()-1);
				}
			}else{
				setIndex(Integer.parseInt(selectedTab));
			}
		}
	}

	public SelectableRowFooter getCurrentFooterMenu(){
		if(index != null && tableGrid.getRows().size() > index){
			return (SelectableRowFooter) tableGrid.getRows().get(index);
		}else{
			setIndex(null);
			return menuNull;
		}
	}

	public void retrieveItems(String selectedTab) throws RemoteException, Exception {

		String[] items = new String[] {};
		if (getMenuWA().containsKey(selectedTab)) {

			mountMenuActions();

			items = new String[getMenuWA().get(selectedTab).size()];
			target = new LinkedList<String>();

			for (int i = 0; i < items.length; ++i) {
				items[i] = getMenuWA().get(selectedTab).get(i)[0];
				target.add(items[i]);
				if(getMenuActions() != null && getMenuActions().contains(items[i])){
					getMenuActions().remove(items[i]);
				}
			}
		}
		setTarget(target);
	}

	public void deleteTab() {
		for (Iterator<SelectableRow> iterator = getTableGrid().getRows().iterator(); iterator.hasNext();) {
			SelectableRow selectableRow = (SelectableRow) iterator.next();
			if(selectableRow.isSelected()){
				iterator.remove();
			}
		}
		setIndex(null);
	}

	public void createTab() throws RemoteException, Exception {
		mountMenuActions();
		String[] value = new String[1];
		value[0] = "";
		getTableGrid().getRows().add(new SelectableRowFooter(value, getMenuActions()));
	}

	public String checkSaveTabs() {

		String error = null;
		String regex = "[a-zA-Z]([a-zA-Z0-9_]*)";

		//The field Name can not be blank
		for (SelectableRow selectableRow : getTableGrid().getRows()) {
			if(selectableRow.getRow()[0].equals("") || selectableRow.getRow()[0].isEmpty()){
				error = getMessageResources("msg_error_save_footer");
				break;
			}

			//The field Name can not contain special character.
			if (!selectableRow.getRow()[0].matches(regex)) {
				error = getMessageResources("msg_error_save_footer_name");
				break;
			}

			//The field Name already exists
			for (SelectableRow selectableRow2 : getTableGrid().getRows()) {
				if(!selectableRow.equals(selectableRow2) && selectableRow.getRow()[0].equalsIgnoreCase(selectableRow2.getRow()[0])){
					error = getMessageResources("msg_error_save_footer_same");
					break;
				}
			}

		}

		return error;
	}

	public void saveTabs() {

		String error = checkSaveTabs();

		if(error == null){

			try {
				if (getworkFlowInterface().getWorkflow(workflowNameTmp) == null) {
					getworkFlowInterface().addWorkflow(workflowNameTmp);
				}
				DataFlow wf = getworkFlowInterface().getWorkflow(workflowNameTmp);
				Map<String,List<String>> mapMenu = new LinkedHashMap<String,List<String>>();

				for (SelectableRow selectableRow : getTableGrid().getRows()) {

					List<String[]> temp = new ArrayList<String[]>();
					for (int i = 0; i < ((SelectableRowFooter) selectableRow).getTarget().size(); ++i) {
						temp.add(new String[] { ((SelectableRowFooter) selectableRow).getTarget().get(i) });
					}
					getMenuWA().put(selectableRow.getRow()[0], temp);

					List<String> l = new LinkedList<String>();
					
					List<String[]> menuList = getMenuWA().get(selectableRow.getRow()[0]);
					for (String[] menuName : menuList) {
						l.add(menuName[0]);
					}
					mapMenu.put(selectableRow.getRow()[0],l);
				}

				wf.loadMenu(mapMenu);
				wf.saveMenu();
				menuWA = wf.getRelativeMenu(getCurrentPage());
				getworkFlowInterface().removeWorkflow(workflowNameTmp);
				setTabs(new LinkedList<String>(getMenuWA().keySet()));

				showTab = "N";
			} catch (RemoteException e1) {
				e1.printStackTrace();

			} catch (Exception e) {
				e.printStackTrace();
			};
		}else{
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

	}

	public void cancelChanges() {
		menuWA = null;
	}

	public List<Entry<String, List<String[]>>> getMenuWAList() throws IOException {
		List<Entry<String, List<String[]>>> list = new ArrayList<Entry<String, List<String[]>>>();
		for (Entry<String, List<String[]>> e : getMenuWA().entrySet()) {
			getMenuWA().get(e.getKey());
			list.add(e);
		}
		return list;
	}

	public LinkedList<String> getMenuActions() {
		return menuActions;
	}

	public void setMenuActions(LinkedList<String> menuActions) {
		this.menuActions = menuActions;
	}

	public SelectableTable getTableGrid() {
		return tableGrid;
	}

	public void setTableGrid(SelectableTable tableGrid) {
		this.tableGrid = tableGrid;
	}

	public LinkedList<String> getColumnIds() {
		return columnIds;
	}

	public void setColumnIds(LinkedList<String> columnIds) {
		this.columnIds = columnIds;
	}

	public LinkedList<String> getTarget() {
		return target;
	}

	public void setTarget(LinkedList<String> target) {
		this.target = target;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public List<String> getTabs() {
		return tabs;
	}

	public void setTabs(List<String> tabs) {
		this.tabs = tabs;
	}

	/**
	 * @return the showTab
	 */
	public String getShowTab() {
		return showTab;
	}

	/**
	 * @param showTab the showTab to set
	 */
	public void setShowTab(String showTab) {
		this.showTab = showTab;
	}

}
