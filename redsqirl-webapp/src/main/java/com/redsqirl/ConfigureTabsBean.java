package com.redsqirl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

}
