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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.NumberUtils;
import org.apache.log4j.Logger;
import org.richfaces.event.DropEvent;

import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.connect.interfaces.SSHDataStoreArray;

/** SshBean
 * 
 * Class to screen control of the File System SSH
 * 
 * @author Igor.Souza
 */
public class SshBean extends FileSystemBean implements Serializable{

	private static Logger logger = Logger.getLogger(SshBean.class);

	private List<Entry<String, String>> fieldsInitNeededNewSsh = new ArrayList<Entry<String, String>>();
	private List<Entry<String, String>> fieldsInitNeededTitleKey = new ArrayList<Entry<String, String>>();
	private List<String> tabs;
	private boolean selectedSaveSsh;
	private String host;
	private String port;
	private String password;
	private String selectedTab;
	private String tableState = new String();
	private SSHDataStoreArray dsa;
	private boolean selectedpassword;
	private Map<String, String> mapPaths = new HashMap<String, String>();

	/** openCanvasScreen
	 * 
	 * Methods to generating screen
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws Exception 
	 */
	//@PostConstruct
	public void openCanvasScreen() {

		logger.info("openCanvasScreen sshbean");

		try {

			dsa = getSSHDataStoreArray();

			for(Map<String, String> map : dsa.getKnownStoreDetails()){
				dsa.addStore(map);
			}

			tabs = new ArrayList<String>();
			mapPaths = new HashMap<String, String>();
			for (Entry<String, DataStore> e : dsa.getStores().entrySet()){
				tabs.add(e.getKey());
				mapPaths.put(e.getKey(), e.getValue().getPath());
			}

			if (!tabs.isEmpty()){
				setSelectedTab(tabs.get(0));
				setDataStore(dsa.getStore(selectedTab));
				setPath(dsa.getStore(selectedTab).getPath());

				if(getTableGrid() != null && getTableGrid().getRows() != null && getTableGrid().getRows().isEmpty()){
					mountTable();
				}
			}

			setFieldsInitNeededNewSsh(mapToList(dsa.getFieldsInitNeeded()));
			setFieldsInitNeededTitleKey(mapToList(dsa.getFieldsInitNeeded()));

			for (Entry<String, String> entry : getFieldsInitNeededNewSsh()) {
				entry.setValue("");
			}

		}catch(Exception e){
			logger.error(e);
			getBundleMessage("error.mount.table");
		}

	}

	/** openNewSsh
	 * 
	 * Method to create the screen with necessary fields to configure a new file system
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws Exception 
	 */
	public void openNewSsh() throws Exception{

		logger.info("openNewSsh");
		setHost("");
		setPort("");
		setSelectedSaveSsh(false);

	}

	/** confirmNewSsh
	 * 
	 * Method to execute the connection
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws Exception 
	 */
	public void confirmNewSsh() {

		try{

			String error = null;
			logger.info("confirmNewSsh");

			Map<String, String> values = new HashMap<String, String>();

			if(getHost() != null && !"".equals(getHost())){
				values.put("host name", getHost());
			}else{
				error = "Error trying to add store with empty Host Name";
			}

			if(error == null && getPort() != null && !"".equals(getPort())){
				if(NumberUtils.isNumber(getPort())){
					values.put("port", getPort());
				}else{
					error = "Error trying to add store with wrong Port number";
				}
			}

			if(error == null && getPassword() != null && !"".equals(getPassword())){
				values.put("password", getPassword());

				setSelectedSaveSsh(false);

				try{
					dsa.addStore(values);
				}catch (Exception e){
					error = "Error trying to add store with Password "+e.getMessage();
					logger.error(error);
				}

			}else{

				if(error == null){

					if(isSelectedSaveSsh()){
						error = dsa.addKnownStore(values);
					}else{
						try{
							dsa.addStore(values);
						}catch (Exception e){
							error = "Error trying to add store "+e.getMessage();
							logger.error(error);
						}
					}

				}

			}

			setSelectedpassword(false);

			if(error == null){
				error = dsa.initKnownStores();

				tabs = new ArrayList<String>();
				mapPaths = new HashMap<String, String>();
				for(Entry<String, DataStore> e : dsa.getStores().entrySet()){
					tabs.add(e.getKey());
					mapPaths.put(e.getKey(), e.getValue().getPath());
				}

				setSelectedTab(tabs.get(0));
				setDataStore(dsa.getStores().get(selectedTab));
				setPath(dsa.getStores().get(selectedTab).getPath());

				mountTable();
			}else{

				for(Map<String, String> map : dsa.getKnownStoreDetails()){
					if (map.get("host name").equals(getHost())){
						dsa.removeKnownStore(map);
					}
				}
				dsa.removeStore(getHost());

			}

			if(error != null){
				MessageUseful.addErrorMessage(error);
				HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
				request.setAttribute("msnError", "msnError");
				usageRecordLog().addError("ERROR NEWSSH", error);
			}

			usageRecordLog().addSuccess("NEWSSH");

		} catch (Exception e) {
			logger.error(e,e);
		}

	}
	
	private void setCorrectPath(){
		String path = mapPaths.get(getSelectedTab());
		logger.info(path);
		setPath(path);
	}

	public void changePathSsh() throws RemoteException {
		
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String nameTab = params.get("nameTab");
		String path = params.get("path");
		
		if(nameTab != null && nameTab.equalsIgnoreCase(getSelectedTab())){
			logger.info(path);
			setPath(path);
			changePath();
			mapPaths.put(getSelectedTab(), getPath());
		}
		
	}
	
	public void goPreviousSsh() throws RemoteException {
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String nameTab = params.get("nameTab");
		
		if(nameTab != null && nameTab.equalsIgnoreCase(getSelectedTab())){
			setCorrectPath();
			goPrevious();
			mapPaths.put(getSelectedTab(), getPath());
		}
	}
	
	public void goNextSsh() throws RemoteException {
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String nameTab = params.get("nameTab");
		
		if(nameTab != null && nameTab.equalsIgnoreCase(getSelectedTab())){
			setCorrectPath();
			goNext();
			mapPaths.put(getSelectedTab(), getPath());
		}
	}
	
	public void goUpSsh() throws RemoteException {
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String nameTab = params.get("nameTab");
		
		if(nameTab != null && nameTab.equalsIgnoreCase(getSelectedTab())){
			setCorrectPath();
			goUp();
			mapPaths.put(getSelectedTab(), getPath());
		}
	}
	
	public void selectFileSsh() {
		setCorrectPath();
		selectFile();
		mapPaths.put(getSelectedTab(), getPath());
	}

	public void changeTab() throws RemoteException, Exception{

		logger.info("changeTab");

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("nameTab");

		logger.info("changeTab: "+name);
		setSelectedTab(name);
		setDataStore(dsa.getStore(name));

		setPath(getDataStore().getPath());
		logger.info("path: "+getPath());
		mapPaths.put(getSelectedTab(), getPath());

		mountTable();
	}

	public void closeTab() throws Exception{

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("nameTab");

		logger.info("closeTab: "+name);

		dsa.initKnownStores();

		for(Map<String, String> map : dsa.getKnownStoreDetails()){
			if (map.get("host name").equals(name)){
				dsa.removeKnownStore(map);
				dsa.removeStore(name);
			}
		}

		if(dsa.getStores().containsKey(name)){
			dsa.removeStore(name);
		}

		tabs = new ArrayList<String>();
		mapPaths = new HashMap<String, String>();
		for(Entry<String, DataStore> e : dsa.getStores().entrySet()){
			tabs.add(e.getKey());
			mapPaths.put(e.getKey(), e.getValue().getPath());
		}

	}

	public void processDrop(DropEvent dropEvent) throws RemoteException {
		logger.info("processDrop");

		FacesContext context = FacesContext.getCurrentInstance();
		String file = context.getExternalContext().getRequestParameterMap().get("file");
		String path = context.getExternalContext().getRequestParameterMap().get("path");

		String pathTo = mapPaths.get(getSelectedTab());
		
		logger.warn("copy from "+path+"/"+file+" to "+getSelectedTab()+":"+pathTo+"/"+file);
		
		try{
			getHDFS().copyToRemote(path+"/"+file, pathTo+"/"+file, getSelectedTab());
			updateTable(true);
		}
		catch(Exception e){
			logger.info("", e);
		}
	}

	public List<Entry<String, String>> getFieldsInitNeededNewSsh() {
		return fieldsInitNeededNewSsh;
	}

	public void setFieldsInitNeededNewSsh(List<Entry<String, String>> fieldsInitNeededNewSsh) {
		this.fieldsInitNeededNewSsh = fieldsInitNeededNewSsh;
	}

	public List<Entry<String, String>> getFieldsInitNeededTitleKey() {
		return fieldsInitNeededTitleKey;
	}

	public void setFieldsInitNeededTitleKey(List<Entry<String, String>> fieldsInitNeededTitleKey) {
		this.fieldsInitNeededTitleKey = fieldsInitNeededTitleKey;
	}

	public boolean isSelectedSaveSsh() {
		return selectedSaveSsh;
	}

	public void setSelectedSaveSsh(boolean selectedSaveSsh) {
		this.selectedSaveSsh = selectedSaveSsh;
	}

	public List<String> getTabs(){
		//logger.info("getTabs:"+tabs.size());
		return tabs;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getSelectedTab() {
		return selectedTab;
	}

	public void setSelectedTab(String selectedTab) {
		this.selectedTab = selectedTab;
	}

	public String getTableState() {
		return tableState;
	}

	public void setTableState(String tableState) {
		this.tableState = tableState;
	}

	public SSHDataStoreArray getDsa() {
		return dsa;
	}

	public void setDsa(SSHDataStoreArray dsa) {
		this.dsa = dsa;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isSelectedpassword() {
		return selectedpassword;
	}

	public void setSelectedpassword(boolean selectedpassword) {
		this.selectedpassword = selectedpassword;
	}

	public Map<String, String> getMapPaths() {
		return mapPaths;
	}

	public void setMapPaths(Map<String, String> mapPaths) {
		this.mapPaths = mapPaths;
	}

}