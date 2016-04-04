/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.redsqirl.auth.AuthorizationListener;
import com.redsqirl.dynamictable.SelectHeaderType;
import com.redsqirl.dynamictable.SelectableRow;
import com.redsqirl.dynamictable.SelectableRowFooter;
import com.redsqirl.dynamictable.SelectableTable;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.ElementManager;
import com.redsqirl.workflow.utils.ModelInt;
import com.redsqirl.workflow.utils.PackageManager;

public class ConfigureTabsBean extends BaseBean implements Serializable {


	private static final long serialVersionUID = 4626482566525824607L;
	private static final String workflowNameTmp = "wf-footer-123";

	private static Logger logger = Logger.getLogger(ConfigureTabsBean.class);

	protected ElementManager em = null;
	private List<String> tabs;
	private SelectableTable tableGrid = new SelectableTable();
	private Map<String, List<String[]>> tabsMap;
	private List<SelectItem> listPackages;
	private List<SelectHeaderType> listActions;
	private String selectedPackage;
	private Integer selectedTab;
	private Map<String,String> mapActionPackage;


	private ElementManager getEM() throws RemoteException{
		if(em == null){
			DataFlow wf = getworkFlowInterface().getWorkflow(workflowNameTmp);
			if(wf == null){
				getworkFlowInterface().addWorkflow(workflowNameTmp);
				wf = getworkFlowInterface().getWorkflow(workflowNameTmp);
			}
			em = wf.getElementManager();
			getworkFlowInterface().removeWorkflow(workflowNameTmp);
		}
		return em;
	}

	/** openCanvasConfigureTabsBean
	 * 
	 * Methods Used in AuthorizationListener.java to open the canvas.xhtml
	 * 
	 * @see AuthorizationListener.java
	 * @author Igor.Souza
	 */
	public void openCanvasConfigureTabsBean() {

		try{
			ElementManager em = getEM();
			tabsMap = em.getRelativeMenu(getCurrentPage());

			setTabs(new LinkedList<String>(getTabsMap().keySet()));

			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			String user = (String) session.getAttribute("username");
			PackageManager pckManager = new PackageManager();
			mapActionPackage = pckManager.getPackageOfActions(user);

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
	 * @throws RemoteException 
	 */
	public void openConfigureTabsBean() throws RemoteException {

		setTableGrid(new SelectableTable());
		for (String name : getTabsMap().keySet()) {

			List<SelectHeaderType> ans = new ArrayList<SelectHeaderType>();
			List<String[]> l = getTabsMap().get(name);
			for (String[] value : l) {

				if(value[0].startsWith(">")){
					String superAction[] = value[0].split(">");
					SelectHeaderType sht = new SelectHeaderType(superAction[1] , superAction[2], true);
					ans.add(sht);
				}else{
					SelectHeaderType sht = new SelectHeaderType(mapActionPackage.get(value[0]) , value[0]);
					ans.add(sht);
				}

			}
			SelectableRowFooter str = new SelectableRowFooter(ans);
			str.setNameTab(name);
			getTableGrid().getRows().add(str);
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
				getTabsMap().remove(selectableRow.getNameTab());
			}
		}
	}

	public String[] getNotificationUser() throws RemoteException{
		getEM();
		Collection<String> notif = em.getPackageToNotify(); 
		return notif.toArray(new String[notif.size()]);
	}

	public void updateFooterWithNewPackages() throws RemoteException{
		getEM();
		em.addPackageToFooter(em.getPackageToNotify());
		openCanvasConfigureTabsBean();
	}

	public void updateNewPackageAsNotified() throws RemoteException{
		getEM();
		em.packageNotified(em.getPackageToNotify());
	}


	/** createTab
	 * 
	 * Methods to add one line in the table. this line represent on tab in the footer
	 * 
	 * @author Igor.Souza
	 */
	public void createTab() throws RemoteException, Exception {
		getTableGrid().getRows().add(new SelectableRowFooter(new ArrayList<SelectHeaderType>()));
	}

	/** saveTabs
	 * 
	 * Methods to save the list of tabs names
	 * 
	 * @author Igor.Souza
	 * @throws RemoteException 
	 */
	public void saveTabs() throws RemoteException {

		String error = checkSaveTabs();

		if(error == null){

			Map<String,List<String>> mapMenu = new LinkedHashMap<String,List<String>>();
			getEM();
			for (Iterator<SelectableRow> iterator = tableGrid.getRows().iterator(); iterator.hasNext();) {
				SelectableRowFooter s = (SelectableRowFooter) iterator.next();
				List<String> temp = new ArrayList<String>();
				for (int i = 0; i < s.getSelectedActions().size(); ++i) {
					if(s.getSelectedActions().get(i).isSuperAction()){
						temp.add(">" + s.getSelectedActions().get(i).getName() + ">" + s.getSelectedActions().get(i).getType());
					}else{
						temp.add(s.getSelectedActions().get(i).getType());
					}
				}
				mapMenu.put(s.getNameTab() , temp);
			}

			em.saveMenu(mapMenu);
			tabsMap = em.getRelativeMenu(getCurrentPage());
			setTabs(new LinkedList<String>(getTabsMap().keySet()));

		}else{
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			usageRecordLog().addError("ERROR SAVETABS", error);
		}

	}

	public String checkSaveTabs() {

		String error = null;
		String regex = "[a-zA-Z]([a-zA-Z0-9\\.\\-_]*[a-zA-Z0-9])*";

		//The field Name can not be blank
		for (SelectableRow selectableRow : tableGrid.getRows()) {
			if(selectableRow.getNameTab().equals("") || selectableRow.getNameTab().isEmpty()){
				error = getMessageResources("msg_error_save_footer");
				break;
			}

			//The field Name can not contain special character.
			if (!selectableRow.getNameTab().matches(regex)) {
				error = getMessageResources("msg_error_save_footer_name");
				break;
			}

			//The field Name already exists
			for (SelectableRow selectableRow2 : getTableGrid().getRows()) {
				if(!selectableRow.equals(selectableRow2) && selectableRow.getNameTab().equalsIgnoreCase(selectableRow2.getNameTab())){
					error = getMessageResources("msg_error_save_footer_same");
					break;
				}
			}

		}

		return error;
	}

	/** openActionsPanel
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
		if(selectedTab != null){
			setSelectedTab(Integer.parseInt(selectedTab));

			listPackages = new ArrayList<SelectItem>();
			listPackages.add(new SelectItem("all", "all"));
			listPackages.add(new SelectItem("core", "core"));

			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			String user = (String) session.getAttribute("username");

			PackageManager pckManager = new PackageManager();
			for (String name : pckManager.getAvailablePackageNames(user)) {
				SelectItem s = new SelectItem(name, name);
				listPackages.add(s);
			}

			for (ModelInt modelInt : getModelManager().getAvailableModels(user)) {
				SelectItem s = new SelectItem(modelInt.getName(), modelInt.getName());
				listPackages.add(s);
			}

			if(listPackages != null && !listPackages.isEmpty()){
				setSelectedPackage(listPackages.get(0).getLabel());
				retrieveActions(getSelectedPackage());
			}

			calculateListSelectedActions();

		}

	}

	/** calculateListSelectedActions
	 * 
	 * Method to create the list of selected actions for each tab
	 * 
	 * @author Igor.Souza
	 * @throws RemoteException 
	 */
	public void calculateListSelectedActions() throws RemoteException {

		SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());
		for (SelectHeaderType selectHeaderType : s.getSelectedActions()) {
			if(selectHeaderType.getType().startsWith(">")){
				String superAction[] = selectHeaderType.getType().split(">");
				selectHeaderType.setName(superAction[1]);
				selectHeaderType.setType(superAction[2]);
			}
			for (Iterator<SelectHeaderType> iterator = listActions.iterator(); iterator.hasNext();) {
				SelectHeaderType actions = (SelectHeaderType) iterator.next();
				if(actions.getName().equals(selectHeaderType.getName()) && actions.getType().equals(selectHeaderType.getType())){
					iterator.remove();
				}
			}
		}

	}

	public void retrieveActions() throws RemoteException {
		String selectedPackage = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedPackage");
		retrieveActions(selectedPackage);
		calculateListSelectedActions();
	}

	/** retrieveActions
	 * 
	 * Method to create the list of actions for each package
	 * 
	 * @param selectedPackage
	 * @author Igor.Souza
	 * @throws RemoteException 
	 */
	public void retrieveActions(String selectedPackage) throws RemoteException {

		if(selectedPackage != null){
			setSelectedPackage(selectedPackage);

			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			String user = (String) session.getAttribute("username");

			PackageManager pckManager = new PackageManager();
			listActions = new ArrayList<SelectHeaderType>();

			if(selectedPackage.equals("all")){
				for (String name : pckManager.getCoreActions()) {
					SelectHeaderType s = new SelectHeaderType("core", name, false);
					listActions.add(s);
				}
				Map<String,List<String>> map = pckManager.getActionsPerPackage(user);
				for (String key : map.keySet()) {
					List<String> ansList = map.get(key);
					if(ansList != null && !ansList.isEmpty()){
						for (String action : ansList) {
							SelectHeaderType selectHeaderType = new SelectHeaderType(key, action, false);
							listActions.add(selectHeaderType);
						}
					}
				}
				for (ModelInt modelInt : getModelManager().getAvailableModels(user)) {
					for (String superAction : modelInt.getPublicSubWorkflowNames()) {
						SelectHeaderType selectHeaderType = new SelectHeaderType(modelInt.getName(), superAction, true);
						listActions.add(selectHeaderType);
					}
				}
			}else if(selectedPackage.equals("core")){
				for (String name : pckManager.getCoreActions()) {
					SelectHeaderType s = new SelectHeaderType("core", name, false);
					listActions.add(s);
				}
			}else{

				Map<String,List<String>> map = pckManager.getActionsPerPackage(user);
				List<String> ansList = map.get(selectedPackage);
				if(ansList != null && !ansList.isEmpty()){
					for (String action : ansList) {
						SelectHeaderType selectHeaderType = new SelectHeaderType(selectedPackage, action, false);
						listActions.add(selectHeaderType);
					}
				}else{

					for (ModelInt modelInt : getModelManager().getAvailableModels(user)) {
						if(modelInt.getName().equals(selectedPackage)){
							for (String superAction : modelInt.getPublicSubWorkflowNames()) {
								SelectHeaderType selectHeaderType = new SelectHeaderType(selectedPackage, superAction, true);
								listActions.add(selectHeaderType);
							}
						}
					}

				}

			}

		}

	}

	/** cancelActions
	 * 
	 * re calculate the list of selected actions for the selected tab 
	 * 
	 * @author Igor.Souza
	 */
	public void cancelActions() throws RemoteException {
		if(getSelectedTab()!= null){
			SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());
			if(s != null){
				List<String[]> l = getTabsMap().get(s.getNameTab());
				if(l != null){
					List<SelectHeaderType> ans = new ArrayList<SelectHeaderType>();
					for (String[] value : l) {
						SelectHeaderType sht = new SelectHeaderType(mapActionPackage.get(value[0]) , value[0]);
						ans.add(sht);
					}
					s.setSelectedActions(ans);
				}
			}
		}
		setSelectedTab(null);
	}

	public void selectAll(){
		SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());

		for (Iterator<SelectHeaderType> iterator = listActions.iterator(); iterator.hasNext();) {
			SelectHeaderType actions = (SelectHeaderType) iterator.next();
			s.getSelectedActions().add(actions);
			iterator.remove();
		}

	}

	public void select(){
		SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());

		for (Iterator<SelectHeaderType> iterator = listActions.iterator(); iterator.hasNext();) {
			SelectHeaderType actions = (SelectHeaderType) iterator.next();
			if(actions.isSelected()){
				logger.info(actions.getName());
				logger.info(actions.getType());
				s.getSelectedActions().add(actions);
				iterator.remove();
			}
		}

	}

	public void unselect(){
		SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());

		for (Iterator<SelectHeaderType> iterator = s.getSelectedActions().iterator(); iterator.hasNext();) {
			SelectHeaderType actions = (SelectHeaderType) iterator.next();
			if(actions.isSelected()){
				logger.info(actions.getName());
				logger.info(actions.getType());
				if(actions.getName().equals(getSelectedPackage()) || getSelectedPackage().equals("all")){
					listActions.add(actions);
					iterator.remove();
				}else{
					iterator.remove();
				}
			}
		}

	}

	public void unselectAll(){
		SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());

		for (Iterator<SelectHeaderType> iterator = s.getSelectedActions().iterator(); iterator.hasNext();) {
			SelectHeaderType actions = (SelectHeaderType) iterator.next();
			if(actions.getName().equals(getSelectedPackage()) || getSelectedPackage().equals("all")){
				listActions.add(actions);
				iterator.remove();
			}else{
				iterator.remove();
			}
		}

	}

	public List<Integer> getAllSelected(){
		SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());
		List<Integer> listSelected = new ArrayList<Integer>();
		for (int i = 0; i < s.getSelectedActions().size(); i++) {
			SelectHeaderType actions = s.getSelectedActions().get(i);
			if(actions.isSelected()){
				listSelected.add(i);
			}
		}
		return listSelected;
	}

	public void goUp() {
		SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());
		List<SelectHeaderType> list = s.getSelectedActions();
		List<Integer> listSelected = getAllSelected();
		for (int i = 0; i < listSelected.size(); i++) {
			int index = listSelected.get(i);
			if(index > 0 && index != i){
				list.add(index-1, list.get(index));
				list.remove(index+1);
			}
		}
		tableGrid.getRows().set(getSelectedTab(), s);
	}

	public void goDown() {
		SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());
		List<SelectHeaderType> list = s.getSelectedActions();
		List<Integer> listSelected = getAllSelected();
		for (int i = listSelected.size()-1; i >=0 ; i--) {
			int index = listSelected.get(i);
			logger.info("go down: "+list.size()+" "+index+" "+listSelected.size()+" "+i);
			if( list.size() - index != listSelected.size() - i){
				if(index < list.size()-2){
					list.add(index+2, list.get(index));
					list.remove(index);
				}else{
					list.add(list.get(index));
					list.remove(index);
				}
			}
		}
	}

	public void goFirst() {
		SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());
		List<SelectHeaderType> list = s.getSelectedActions();
		List<Integer> listSelected = getAllSelected();
		for (int i = 0; i < listSelected.size(); i++) {
			int index = listSelected.get(i);
			list.add(i, list.get(index));
			list.remove(index+1);
		}
	}

	public void goLast() {
		SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());
		List<SelectHeaderType> list = s.getSelectedActions();
		List<Integer> listSelected = getAllSelected();
		for (int i = 0; i < listSelected.size(); i++) {
			int index = listSelected.get(i);
			list.add(list.get(index-i));
			list.remove(index-i);
		}
	}

	/** getTabSelectedActions
	 * 
	 * Methods to return the list of selected actions. Used in configureFooterActionsTabs.xhtml to Iterator the tabs 
	 * 
	 * @see configureFooterActionsTabs.xhtml
	 * @return List<SelectHeaderType>
	 * @author Igor.Souza
	 */
	public List<SelectHeaderType> getTabSelectedActions(){
		if(getSelectedTab() != null && getSelectedTab() < tableGrid.getRows().size()){
			SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());
			return s.getSelectedActions();
		}
		return new ArrayList<SelectHeaderType>();
	}

	/** getTabsMapList
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

	public Integer getSelectedTab() {
		return selectedTab;
	}

	public void setSelectedTab(Integer selectedTab) {
		this.selectedTab = selectedTab;
	}

	public Map<String, String> getMapActionPackage() {
		return mapActionPackage;
	}

	public void setMapActionPackage(Map<String, String> mapActionPackage) {
		this.mapActionPackage = mapActionPackage;
	}

}