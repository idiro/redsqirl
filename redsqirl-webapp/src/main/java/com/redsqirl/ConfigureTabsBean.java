package com.redsqirl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.redsqirl.auth.AuthorizationListener;
import com.redsqirl.auth.UserInfoBean;
import com.redsqirl.dynamictable.SelectHeaderType;
import com.redsqirl.dynamictable.SelectableRow;
import com.redsqirl.dynamictable.SelectableTable;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.utils.PackageManager;

public class ConfigureTabsBean extends BaseBean implements Serializable {


	private static final long serialVersionUID = 4626482566525824607L;

	private static Logger logger = Logger.getLogger(ConfigureTabsBean.class);

	private String workflowNameTmp = "wf-footer-123";
	private Map<String, List<String[]>> tabsMap;
	private SelectableTable tableGrid = new SelectableTable();
	private LinkedList<String> columnIds;
	private List<String> tabs;
	private List<SelectItem> listPackages;
	private List<SelectHeaderType> listActions;
	private List<SelectHeaderType> listSelectedActions;
	private String selectedPackage;


	/** openCanvasConfigureTabsBean
	 * 
	 * Methods Used in AuthorizationListener.java to open the canvas.xhtml
	 * 
	 * @see AuthorizationListener.java - canvas.xhtml
	 * @author Igor.Souza
	 */
	public void openCanvasConfigureTabsBean() {

		try{

			if (getworkFlowInterface().getWorkflow(workflowNameTmp) == null) {
				getworkFlowInterface().addWorkflow(workflowNameTmp);
			}

			DataFlow wf = getworkFlowInterface().getWorkflow(workflowNameTmp);
			wf.loadMenu();
			tabsMap = wf.getRelativeMenu(getCurrentPage());
			getworkFlowInterface().removeWorkflow(workflowNameTmp);

			setTabs(new LinkedList<String>(getTabsMap().keySet()));


		} catch (Exception e) {
			logger.error("Error openConfigureTabsBean " + e,e);
		}

	}

	/** openConfigureTabsBean
	 * 
	 * Methods Used called to open the configure footer popup
	 * 
	 * @see canvas.xhtml
	 * @author Igor.Souza
	 */
	public void openConfigureTabsBean() {

		setColumnIds(new LinkedList<String>());
		getColumnIds().add("Name");
		setTableGrid(new SelectableTable(columnIds));
		for (String name : getTabsMap().keySet()) {
			String[] value = new String[1];
			value[0] = name;
			getTableGrid().getRows().add(new SelectableRow(value));
		}

	}

	/** deleteTab
	 * 
	 * Methods to remove one line in the table. Remove one tab in the footer
	 * 
	 * @author Igor.Souza
	 */
	public void deleteTab() {
		for (Iterator<SelectableRow> iterator = getTableGrid().getRows().iterator(); iterator.hasNext();) {
			SelectableRow selectableRow = (SelectableRow) iterator.next();
			if(selectableRow.isSelected()){
				iterator.remove();
				getTabsMap().remove(selectableRow.getRow()[0]);
			}
		}
	}

	/** createTab
	 * 
	 * Methods to add one line in the table. this line represent on tab in the footer
	 * 
	 * @author Igor.Souza
	 */
	public void createTab() throws RemoteException, Exception {
		String[] value = new String[1];
		value[0] = "";
		getTableGrid().getRows().add(new SelectableRow(value));
	}

	/** saveTabs
	 * 
	 * Methods to save the list of tabs names
	 * 
	 * @author Igor.Souza
	 */
	public void saveTabs() {

	}

	/** cancelChanges
	 * 
	 * Methods to just close the configure footer
	 * 
	 * @author Igor.Souza
	 */
	public void cancelChanges() {
		tabsMap = null;
	}

	/** cancelChanges
	 * 
	 * open the settings configure the actions
	 * 
	 * @param selected - id of tabsMap came from .xhtml
	 * @see configureFooterTabs.xhtml
	 * @author Igor.Souza
	 * @throws RemoteException 
	 */
	public void openActionsPanel() throws RemoteException {

		logger.info("openActionsPanel");
		String selectedTab = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selected");

		listPackages = new ArrayList<SelectItem>();
		listPackages.add(new SelectItem("All", "All"));
		listPackages.add(new SelectItem("core", "core"));

		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		String user = (String) session.getAttribute("username");

		PackageManager pckManager = new PackageManager();
		for (String name : pckManager.getPackageNames(user)) {
			SelectItem s = new SelectItem(name, name);
			listPackages.add(s);
		}
		for (String name : pckManager.getPackageNames(null)) {
			SelectItem s = new SelectItem(name, name);
			listPackages.add(s);
		}
		if(listPackages != null && !listPackages.isEmpty()){
			setSelectedPackage(listPackages.get(0).getLabel());
			retrieveActions(getSelectedPackage());
		}

	}
	
	public void retrieveActions() throws RemoteException {
		String selectedPackage = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedPackage");
		retrieveActions(selectedPackage);
	}

	public void retrieveActions(String selectedPackage) throws RemoteException {

		if(selectedPackage != null){
			setSelectedPackage(selectedPackage);

			FacesContext context = FacesContext.getCurrentInstance();
			UserInfoBean userInfoBean = (UserInfoBean) context.getApplication().evaluateExpressionGet(context, "#{userInfoBean}", UserInfoBean.class);
			logger.info("User: " + userInfoBean.getUserName());

			PackageManager pckManager = new PackageManager();
			listActions = new ArrayList<SelectHeaderType>();
			
			if(selectedPackage.equals("All")){
				List<String> ansList = pckManager.getActions(userInfoBean.getUserName());
				for (String action : ansList) {
					SelectHeaderType selectHeaderType = new SelectHeaderType(selectedPackage, action);
					listActions.add(selectHeaderType);
				}
			}else{
				Map<String,List<String>> map = pckManager.getActionsPerPackage(userInfoBean.getUserName());
				List<String> ansList = map.get(selectedPackage);
				if(ansList != null && !ansList.isEmpty()){
					for (String action : ansList) {
						SelectHeaderType selectHeaderType = new SelectHeaderType(selectedPackage, action);
						listActions.add(selectHeaderType);
					}
				}
			}

		}

	}

	public void cancelActionChanges() {
		tabsMap = null;
	}

	public void saveActions() {

	}
	
	public void selectAll(){
		
	}
	
	public void select(){
		
	}
	
	public void unselect(){
		
	}
	
	public void unselectAll(){
		
	}

	/** getMenuWAList
	 * 
	 * Methods to return the list of tabs. Used in canvas.xhtml to Iterator the tabs of the footer 
	 * 
	 * @see canvas.xhtml
	 * @return List<Entry<String, List<String[]>>>
	 * @author Igor.Souza
	 */
	public List<Entry<String, List<String[]>>> getTabsMapList(){
		List<Entry<String, List<String[]>>> list = new ArrayList<Entry<String, List<String[]>>>();
		for (Entry<String, List<String[]>> e : getTabsMap().entrySet()) {
			list.add(e);
		}
		return list;
	}


	public Map<String, List<String[]>> getTabsMap() {
		return tabsMap;
	}

	public void setTabsMap(Map<String, List<String[]>> tabsMap) {
		this.tabsMap = tabsMap;
	}

	public LinkedList<String> getColumnIds() {
		return columnIds;
	}

	public void setColumnIds(LinkedList<String> columnIds) {
		this.columnIds = columnIds;
	}

	public SelectableTable getTableGrid() {
		return tableGrid;
	}

	public void setTableGrid(SelectableTable tableGrid) {
		this.tableGrid = tableGrid;
	}

	public List<String> getTabs() {
		return tabs;
	}

	public void setTabs(List<String> tabs) {
		this.tabs = tabs;
	}

	public List<SelectItem> getListPackages() {
		return listPackages;
	}

	public void setListPackages(List<SelectItem> listPackages) {
		this.listPackages = listPackages;
	}

	public String getSelectedPackage() {
		return selectedPackage;
	}

	public void setSelectedPackage(String selectedPackage) {
		this.selectedPackage = selectedPackage;
	}

	public List<SelectHeaderType> getListActions() {
		return listActions;
	}

	public void setListActions(List<SelectHeaderType> listActions) {
		this.listActions = listActions;
	}

	public List<SelectHeaderType> getListSelectedActions() {
		return listSelectedActions;
	}

	public void setListSelectedActions(List<SelectHeaderType> listSelectedActions) {
		this.listSelectedActions = listSelectedActions;
	}

	

	/*
	protected Map<String, List<String[]>> menuWA;
	private Map<String,String> allWANameWithClassName = null;
	private LinkedHashSet<String> menuActions;
	private List<String> tabs;
	private LinkedList<String> columnIds;
	private LinkedList<String> target;
	private SelectableTable tableGrid = new SelectableTable();
	private SelectableTable tableGridOld = new SelectableTable();
	private Integer index;
	private String showTab = "N";
	private String workflowNameTmp = "wf-footer-123";
	private List<SelectItem> packageList;
	private String selectedPackage;

	 *//**
	 * Value to give when index is null
	 *//*
	private SelectableRowFooter menuNull = new SelectableRowFooter(new String[3], getMenuActionsAsList());




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
				logger.info("Load menu...");
				wf.loadMenu();
				logger.info("Load relative menu...");
				menuWA = wf.getRelativeMenu(getCurrentPage());
				logger.info("Load Action classes");
				if(allWANameWithClassName == null){
					allWANameWithClassName = wf.getAllWANameWithClassName();
					logger.info(allWANameWithClassName.keySet());
				}
				getworkFlowInterface().removeWorkflow(workflowNameTmp);




				logger.info("Mount package list");
				HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
				String user = (String) session.getAttribute("username");

				packageList = new ArrayList<SelectItem>(); 

				PackageManager pckManager = new PackageManager();
				for (String name : pckManager.getPackageNames(user)) {
					SelectItem s = new SelectItem(name, name);
					packageList.add(s);
				}
				for (String name : pckManager.getPackageNames(null)) {
					SelectItem s = new SelectItem(name, name);
					packageList.add(s);
				}

				if(!packageList.isEmpty()){
					setSelectedPackage(packageList.get(0).getLabel());
				}


				logger.info("Mount action");
				mountMenuActions();

				if(getMenuWA().isEmpty()){
					setIndex(null);
				}else{
					setIndex(0);
				}
				logger.info("end...");

			} catch (RemoteException e1) {
				e1.printStackTrace();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void selectPackage() throws RemoteException, Exception {
		if(getSelectedPackage() != null){

			FacesContext context = FacesContext.getCurrentInstance();
			UserInfoBean userInfoBean = (UserInfoBean) context.getApplication().evaluateExpressionGet(context, "#{userInfoBean}", UserInfoBean.class);
			logger.info("User: " + userInfoBean.getUserName());

			PackageManager pckManager = new PackageManager();
			Map<String,List<String>> map = pckManager.getActionsPerPackage(userInfoBean.getUserName());
			((SelectableRowFooter) tableGrid.getRows().get(index)).setActions(map.get(getSelectedPackage()));

		}
	}

	public void mountMenuActions() throws RemoteException, Exception {

		logger.info("mountMenuActions");

		LinkedList<String> result = new LinkedList<String>();

		FacesContext context = FacesContext.getCurrentInstance();
		UserInfoBean userInfoBean = (UserInfoBean) context.getApplication().evaluateExpressionGet(context, "#{userInfoBean}", UserInfoBean.class);
		logger.info("User: " + userInfoBean.getUserName());

		if(getSelectedPackage() != null){

			PackageManager pckManager = new PackageManager();

			Map<String,List<String>> map = pckManager.getActionsPerPackage(userInfoBean.getUserName());
			result = new LinkedList<String>();
			result.addAll(map.get(getSelectedPackage()));

			//list of super action
			//List<String> listSuperAction = getSuperActionManager().getAvailableSuperActions(userInfoBean.getUserName());

			//list of normal action
			List<String> listAction = new ArrayList<String>();
			for (Iterator<Entry<String, String>> iterator = allWANameWithClassName.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, String> e = iterator.next();
				listAction.add(e.getKey());
				result.add(e.getKey());
			}

			for (String name : listSuperAction) {
				if(!result.contains(name)){
					result.add(name);
				}
			}

		}

		menuActions = new LinkedHashSet<String>();
		menuActions.addAll(result);

		//logger.info("Available Actions: "+result);
		menuNull = new SelectableRowFooter(new String[3], getMenuActionsAsList());

		//Action Menu names
		setTabs(new LinkedList<String>(getMenuWA().keySet()));
		setColumnIds(new LinkedList<String>());
		getColumnIds().add("Name");


		//save old
		tableGridOld = new SelectableTable();
		tableGridOld.setColumnIds(tableGrid.getColumnIds());
		tableGridOld.setTitles(tableGrid.getTitles());
		tableGridOld.setRows(tableGrid.getRows());
		tableGridOld.setRowNumber(tableGrid.getRowNumber());
		tableGridOld.setName(tableGrid.getName());


		setTableGrid(new SelectableTable(columnIds));

		//Set All the menus
		for (String name : getMenuWA().keySet()) {
			menuActions = new LinkedHashSet<String>();
			menuActions.addAll(result);

			String[] value = new String[1];
			value[0] = name;

			retrieveItems(name);

			checkOldValues(name);

			getTableGrid().getRows().add(new SelectableRowFooter(value, getMenuActionsAsList(), getTarget()));
			//logger.info("menu "+name+": "+getMenuActions()+", "+getTarget());
		}
		menuActions.addAll(result);
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

			//mountMenuActions();

			items = new String[getMenuWA().get(selectedTab).size()];
			target = new LinkedList<String>();

			for (int i = 0; i < items.length; ++i) {
				items[i] = getMenuWA().get(selectedTab).get(i)[0];
				//if(getMenuActions().contains(items[i])){
				target.add(items[i]);
				//}
				if(getMenuActions() != null && getMenuActions().contains(items[i])){
					getMenuActions().remove(items[i]);
				}
			}
		}
		setTarget(target);
	}

	public void checkOldValues(String selectedTab){

		for (int i = 0; i < tableGridOld.getRows().size(); i++) {
			SelectableRowFooter selectableRowFooter = (SelectableRowFooter) tableGridOld.getRows().get(i);
			if(selectableRowFooter.getRow()[0].equals(selectedTab)){

				if(getMenuActions().size() != selectableRowFooter.getActions().size()){
					for (String nameAction : selectableRowFooter.getActions()) {
						if(!getMenuActions().contains(nameAction)){
							getMenuActions().add(nameAction);
							if(getTarget().contains(nameAction)){
								getTarget().remove(nameAction);
							}
						}
					}
				}

				if(getTarget().size() != selectableRowFooter.getTarget().size()){
					for (String nameTargetAction : selectableRowFooter.getTarget()) {
						if(!getTarget().contains(nameTargetAction)){
							getTarget().add(nameTargetAction);
							if(getMenuActions().contains(nameTargetAction)){
								getMenuActions().remove(nameTargetAction);
							}
						}
					}
				}
			}
		}

	}

	public void deleteTab() {
		for (Iterator<SelectableRow> iterator = getTableGrid().getRows().iterator(); iterator.hasNext();) {
			SelectableRow selectableRow = (SelectableRow) iterator.next();
			if(selectableRow.isSelected()){
				iterator.remove();
				getMenuWA().remove(selectableRow.getRow()[0]);
			}
		}

		setIndex(null);
	}

	public void createTab() throws RemoteException, Exception {

		LinkedList<String> result = new LinkedList<String>();
		FacesContext context = FacesContext.getCurrentInstance();
		UserInfoBean userInfoBean = (UserInfoBean) context.getApplication().evaluateExpressionGet(context, "#{userInfoBean}", UserInfoBean.class);
		//list of super action
		List<String> listSuperAction = getSuperActionManager().getAvailableSuperActions(userInfoBean.getUserName());
		//list of normal action
		for (Iterator<Entry<String, String>> iterator = allWANameWithClassName.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, String> e = iterator.next();
			result.add(e.getKey());
		}
		for (String name : listSuperAction) {
			if(!result.contains(name)){
				result.add(name);
			}
		}

		String[] value = new String[1];
		value[0] = "";
		getTableGrid().getRows().add(new SelectableRowFooter(value, result));
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

				//showTab = "N";

				tableGrid.unselectAll();

				if(getMenuWA().isEmpty()){
					setIndex(null);
				}else{
					setIndex(0);
				}

			} catch (RemoteException e1) {
				e1.printStackTrace();

			} catch (Exception e) {
				e.printStackTrace();
			};
		}else{
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			usageRecordLog().addError("ERROR SAVETABS", error);
		}

		usageRecordLog().addSuccess("SAVETABS");
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

	public LinkedList<String> getMenuActionsAsList() {
		LinkedList<String> l = new LinkedList<String>();
		if(menuActions != null){
			l.addAll(menuActions);
		}
		return l;
	}

	public LinkedHashSet<String> getMenuActions() {
		return menuActions;
	}

	public void setMenuActions(LinkedHashSet<String> menuActions) {
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

	  *//**
	  * @return the showTab
	  *//*
	public String getShowTab() {
		return showTab;
	}

	   *//**
	   * @param showTab the showTab to set
	   *//*
	public void setShowTab(String showTab) {
		this.showTab = showTab;
	}

	public SelectableTable getTableGridOld() {
		return tableGridOld;
	}

	public void setTableGridOld(SelectableTable tableGridOld) {
		this.tableGridOld = tableGridOld;
	}

	public List<SelectItem> getPackageList() {
		return packageList;
	}

	public void setPackageList(List<SelectItem> packageList) {
		this.packageList = packageList;
	}

	public String getSelectedPackage() {
		return selectedPackage;
	}

	public void setSelectedPackage(String selectedPackage) {
		this.selectedPackage = selectedPackage;
	}*/

}