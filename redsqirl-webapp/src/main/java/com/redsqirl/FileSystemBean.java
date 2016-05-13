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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.redsqirl.auth.UserInfoBean;
import com.redsqirl.dynamictable.SelectableRow;
import com.redsqirl.dynamictable.SelectableTable;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.connect.interfaces.DataStore.ParamProperty;

public class FileSystemBean extends BaseBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1685249123539175115L;

	private static Logger logger = Logger.getLogger(FileSystemBean.class);
	private static int nbCreate = 0;


	private List<SelectItem> listExtensions;
	private List<String> listExtensionsString;
	private boolean allowDirectories;
	private boolean allowOnlyDirectories = false;
	private String extensionsSelected;
	private String openOutputData;


	private boolean file;
	private String name;
	private String path;
	private List<String[]> selectedFiles;
	private String fileContent;

	/**
	 * The list of rows of the grid file system
	 */
	private SelectableTable tableGrid = new SelectableTable();

	private DataStore dataStore;
	private List<Map<String,String>> allProps;
	private List<String> editProps;
	private List<String> createProps;
	private Map<String, ParamProperty> propsParam; 
	private Integer currentFileIndex;
	private LinkedHashMap<String, String> newProp;

	/**
	 * Have the same xhtml page for copy and move.
	 * 'C' for showing copy and 'M' for showing move.
	 */
	private String showCopyMove;



	public FileSystemBean(){
		logger.info("Create FileSystem: "+ (++nbCreate));
	}

	/**
	 * openCanvasScreen
	 * 
	 * Methods to generating screen
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void openCanvasScreen() {

	}

	/** closeCanvasScreen
	 * 
	 * Method that is executed when the screen
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void closeCanvasScreen() {

	}

	public void updateParamProperties() throws RemoteException{
		DataStore hInt = getDataStore();
		setPropsParam(hInt.getParamProperties());

		//Set list field
		LinkedList<String> titles = new LinkedList<String>();
		LinkedList<String> editProps = new LinkedList<String>();
		LinkedList<String> createProps = new LinkedList<String>();
		for (String properties : propsParam.keySet()) {

			if (!propsParam.get(properties).editOnly() && !propsParam.get(properties).createOnly()) {
				titles.add(properties);
				editProps.add(properties);
			}else if (propsParam.get(properties).editOnly()) {
				editProps.add(properties);
			}else if (propsParam.get(properties).createOnly()) {
				createProps.add(properties);
			}

		}

		setTableGrid(new SelectableTable(titles));
		setEditProps(editProps);
		setCreateProps(createProps);
	}
	/**
	 * mountTable
	 * 
	 * Method to mount the file system grid
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void mountTable() throws RemoteException {
		logger.info("Started mounting table");

		FacesContext context = FacesContext.getCurrentInstance();
		UserInfoBean userInfoBean = (UserInfoBean) context.getApplication()
				.evaluateExpressionGet(context, "#{userInfoBean}",
						UserInfoBean.class);

		logger.info("update progressbar");
		userInfoBean.setValueProgressBar(Math.min(100, userInfoBean.getValueProgressBar()+5));


		updateTable(false);

		logger.info("update progressbar");
		userInfoBean.setValueProgressBar(Math.min(100, userInfoBean.getValueProgressBar()+5));


		logger.info("Finished mounting table");
	}

	protected void updateTable(boolean refresh) throws RemoteException{
		String oldPath = getPath();
		String error = null;
		setPath(getDataStore().getPath());
		updateParamProperties();
		
		//Fill rows
		try{
			if (oldPath == null || !oldPath.equals(getPath()) || getAllProps() == null || getAllProps().isEmpty() || 
					(getAllProps() != null && (getTableGrid().getRows() == null || getTableGrid().getRows().isEmpty())) || refresh ){
				
				Map<String, Map<String, String>> mapSSH = getDataStore().getChildrenProperties(refresh);
				if(mapSSH != null){
					setAllProps(new LinkedList<Map<String,String>>());
					getTableGrid().getRows().clear();
					for (String path : mapSSH.keySet()) {
						String[] aux = path.split("/");
						String childName = aux[aux.length - 1];
						Map<String, String> allProperties = new LinkedHashMap<String, String>();
						allProperties.put("name", childName);
						allProperties.putAll(mapSSH.get(path));
						getTableGrid().add(allProperties);
						getAllProps().add(allProperties);
					}
				}
			}
			
			updateSelection();
			
		}catch(Exception e){
			error = e.getMessage();
		}
		
		if(error != null){
			setPath(oldPath);
		}
		displayErrorMessage(error, "UPDATETABLE");
	}
	
	private void updateSelection(){
		if(getAllProps() != null){

			String regex = null;
			if(extensionsSelected != null && !extensionsSelected.isEmpty()){
				logger.info("extension select: "+extensionsSelected);
				regex = extensionsSelected.replaceAll(Pattern.quote("."), Matcher.quoteReplacement("\\.")).replaceAll(Pattern.quote("*"), ".*");
				logger.info("Regex: "+regex);
			}
			
			
			for(int i=0;i < getAllProps().size();++i){
				String childName = getAllProps().get(i).get("name");
				getTableGrid().getRows().get(i).setSelected(false);
				getTableGrid().getRows().get(i).setDisableSelect(false);
				try{
					if(getAllProps().get(i).get("type").equalsIgnoreCase("connection") ||
							(getAllProps().get(i).get("type").equalsIgnoreCase("directory") && !isAllowDirectories()) ||
							(!getAllProps().get(i).get("type").equalsIgnoreCase("directory") && isAllowOnlyDirectories()) ||
							(openOutputData != null && openOutputData.equals("Y") && !getAllProps().get(i).get("type").equalsIgnoreCase("directory"))
							) {
						getTableGrid().getRows().get(i).setDisableSelect(false);
					}else{
						if(regex != null){
							getTableGrid().getRows().get(i).setDisableSelect(childName.matches(regex));
						}else{
							getTableGrid().getRows().get(i).setDisableSelect(true);
						}
					}
				}catch(Exception e){
					//No Directory Types
				}
			}
		}
	}


	/**
	 * deleteFile
	 * 
	 * Method to delete the selected file in the file system screen
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void deleteFile() throws RemoteException {

		List<Integer> posToDell = tableGrid.getAllSelected();
		if(posToDell != null){
			for (int i = 0; i < posToDell.size(); i++) {
				int pos = posToDell.get(i);
				String directory = generatePath(getDataStore().getPath(), allProps.get(pos).get("name"));
				logger.info("Delete -" + directory);
				getDataStore().delete(directory);
			}
			
			//Force the refresh
			setPath(null);
			updateTable(true);
		}

		usageRecordLog().addSuccess("DELETEFILE");
	}

	/**
	 * changePath
	 * 
	 * Method that retrieves the path is typed on the screen and the update of
	 * the grid
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void changePath() throws RemoteException {
		logger.info("changePath: " + getPath());
		if (getDataStore().goTo(getPath())) {
			//Force the refresh
			setPath(null);
			updateTable(false);
		} else {
			getBundleMessage("error.invalid.path");
		}

	}
	
	public void refreshPath() throws RemoteException {
		setPath(null);
		updateTable(true);
	}

	/**
	 * selectFile
	 * 
	 * Method to navigate through the file system using the directory selected
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void selectFile() {

		try {

			Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
			String name = params.get("nameFile");

			logger.info("selectFile " + getPath() + " - " + name);

			String newPath = generatePath(getPath(), name);

			logger.info("selectFile newPath " + newPath);

			if (getDataStore().goTo(newPath)) {
				//setPath(newPath);
				updateTable(false);
				logger.info("selectFile updateTable");

			} else {
				logger.error("Error this is not a directory");
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			MessageUseful.addErrorMessage(" ");
		}

	}
	
	public void selectFileHistory() {

		try {

			Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
			String name = params.get("nameFile");

			logger.info("selectFileHistory " + name);

			if (getDataStore().goTo(name)) {
				updateTable(false);
				logger.info("selectFileHistory updateTable");
			} else {
				logger.error("Error: " + name + " this is not a directory");
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			MessageUseful.addErrorMessage(" ");
		}

	}


	/**
	 * open
	 * 
	 * Method to preview the contents of a file
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void openFile() throws RemoteException {
		String name = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("nameFile");

		logger.info("openFile " + name);

		String path = generatePath(getPath(), name);

		try{
			List<String> contents = getDataStore().displaySelect(path,200);
			fileContent = "";
			for (String s : contents) {
				fileContent += s + System.getProperty("line.separator");
			}
		}catch(Exception e){
			logger.info("Exception happened, probably no permissions to view this file");
		}

		logger.info("openFile fileContent " + fileContent);
	}

	/**
	 * verifyIfIsFile
	 * 
	 * Method to verify if the selected path is a file or a directory
	 * 
	 * @return
	 * @author Igor.Souza
	 */

	public void verifyIfIsFile() throws RemoteException {
		String name = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("nameFile");
		logger.info("verifying "+name);
		verifyIfIsFile(name);
	}

	public void verifyIfIsFile(String name) throws RemoteException {
		getDataStore().goTo(generatePath(getDataStore().getPath(), name));
		try{
			file = getDataStore().getChildrenProperties(true) == null;
		}catch(Exception e){
			file = true;
		}
		logger.info("verifying is "+name + " a file "+file);
		getDataStore().goPrevious();
	}

	/**
	 * mountSelectedFilesList
	 * 
	 * Method to mount one list with the selecteds files
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	private void mountSelectedFilesList() throws RemoteException {
		selectedFiles = new ArrayList<String[]>();
		int i =0;
		for (SelectableRow row : getTableGrid().getRows()) {
			if (row.isSelected()) {
				selectedFiles.add(new String[] { getDataStore().getPath(), getAllProps().get(i).get("name")});
				logger.info("mountSelectedFilesList " + getAllProps().get(i).get("name"));
			}
			i++;
		}
	}

	/**
	 * copyMoveFile
	 * 
	 * Method to copy or move a file
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void copyMoveFile(String action) throws RemoteException {

		logger.info("copyMoveFile");

		Integer indexDes = null;
		for (int i = 0; i < getTableGrid().getRows().size(); i++) {
			SelectableRow item = (SelectableRow)getTableGrid().getRows().get(i);
			if (item.isSelected()) {
				indexDes = i;
				break;
			}
		}

		String error = null;
		String destination = getDataStore().getPath();
		if(indexDes != null){
			destination += "/" + getAllProps().get(indexDes).get("name");
		}
		for (String[] s : selectedFiles) {
			if(destination.startsWith((s[0] + "/" + s[1]))){
				error = getMessageResources("error_target_sorce");
				break;
			}
		}

		if(error == null){
			for (String[] s : selectedFiles) {
				logger.info(action + " " + s[0] + "/" + s[1] + " to " + destination);
				if(action.equals("copy")){
					error = getDataStore().copy(s[0] + "/" + s[1], destination + "/" + s[1]);
				}else{
					error = getDataStore().move(s[0] + "/" + s[1], destination + "/" + s[1]);
				}
			}
		}

		if(error != null){
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			usageRecordLog().addError("ERROR COPYMOVEFILE", error);
		}

		updateTable(true);
		
		usageRecordLog().addSuccess("COPYMOVEFILE");
	}

	/**
	 * copyFileBefore
	 * 
	 * Method to execute before opening the screen to copy a file
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void copyFileBefore() throws RemoteException {
		mountSelectedFilesList();
	}

	/**
	 * copyFileAfter
	 * 
	 * Method to run the file copy
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void copyFileAfter() throws RemoteException {
		copyMoveFile("copy");
	}

	/**
	 * moveFileBefore
	 * 
	 * Method to execute before opening the screen to move a file
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void moveFileBefore() throws RemoteException {
		mountSelectedFilesList();
	}

	/**
	 * moveFileAfter
	 * 
	 * Method to move a file
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void moveFileAfter() throws RemoteException {
		copyMoveFile("move");
	}

	/**
	 * addFileBefore
	 * 
	 * Method to execute before opening the screen to add a file
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void addFileBefore() throws RemoteException {

		logger.info("addFileBefore");
		
		newProp = new LinkedHashMap<String, String>();
		Iterator<String> it = createProps.iterator();
		while (it.hasNext()) {
			String key = it.next();
			newProp.put(key, null);
		}

	}

	/**
	 * addFileAfter
	 * 
	 * Method to run the add file
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void addFileAfter() throws RemoteException {

		logger.info("addFileAfter");

		setName(newProp.get("name"));

		String newDirectory = generatePath(getDataStore().getPath(), getName());
		getDataStore().create(newDirectory, getNewProp());

		//Force the refresh
		setPath(null);
		updateTable(true);
	}

	/**
	 * editFileBefore
	 * 
	 * Method to execute before opening the screen to edit a file
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void editFileBefore() throws RemoteException {

		logger.info("editFileBefore");

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String index = params.get("indexFileEdit");

		logger.info("index " + index);

		if(index != null){
			setCurrentFileIndex(Integer.parseInt(index));
			setName(allProps.get(getCurrentFileIndex()).get("name"));
		}

	}

	/**
	 * editFileAfter
	 * 
	 * Method to edit a file
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void editFileAfter() throws RemoteException {

		Map<String,String> prop = new LinkedHashMap<String, String>();
		Iterator<String> it = editProps.iterator();
		while (it.hasNext()) {
			String key = it.next();
			prop.put(key, allProps.get(getCurrentFileIndex()).get(key));
		}
		String newName = allProps.get(getCurrentFileIndex()).get("name");

		try {
			String error = getDataStore().changeProperties(getDataStore().getPath() + "/" + getName(), prop);
			logger.info("change properties error : " + error);
		} catch (Exception e) {
			logger.error("Error change properties : " + e.getMessage());
			MessageUseful.addErrorMessage("Fail to update properties of " + getDataStore().getPath() + "/" + newName + " to " + getName());
		}
		if(!newName.equals(name)){
			logger.info("Rename " + getDataStore().getPath() + "/" + getName() + " to " + getDataStore().getPath() + "/" + newName);
			getDataStore().move(getDataStore().getPath() + "/" + getName(),	getDataStore().getPath() + "/" + newName);
		}
		
		updateTable(true);

	}

	/**
	 * goPrevious
	 * 
	 * Method to navigate the file system. updates the grid with the information
	 * from the previous directory
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void goPrevious() throws RemoteException {

		getDataStore().goPrevious();
		updateTable(false);

	}

	/**
	 * goNext
	 * 
	 * Method to navigate the file system. updates the grid with information
	 * from the directory later
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void goNext() throws RemoteException {

		getDataStore().goNext();
		updateTable(false);

	}

	/**
	 * goUp
	 * 
	 * Method to navigate the file system.
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void goUp() throws RemoteException {

		logger.info("goUp: " + getDataStore().getPath());

		if (getPath() != null) {
			if (getPath().length() > 1) {
				String newPath = "";
				if (getPath().endsWith("/")) {
					newPath = getPath().substring(0, getPath().length() - 1);
				}
				newPath = getPath().substring(0, getPath().lastIndexOf('/'));

				if (newPath.length() < 1) {
					newPath = "/";
				}

				logger.info("newPath" + newPath);

				if (getDataStore().goTo(newPath)) {
					updateTable(false);
				} else {
					getBundleMessage("error.invalid.path");
				}
			}
		}

	}

	private String generatePath(String path, String name) {
		String resultPath = path;
		if (!resultPath.endsWith("/")) {
			resultPath += "/";
		}
		resultPath += name;
		return resultPath;
	}

	public String getCanCopy() throws RemoteException {
		return getDataStore().canCopy();
	}

	public String getCanMove() throws RemoteException {
		return getDataStore().canMove();
	}

	public String getCanDelete() throws RemoteException {
		return getDataStore() != null ? getDataStore().canDelete() : "NULL";
	}

	public String getCanCreate() throws RemoteException {
		return getDataStore() != null ? getDataStore().canCreate() : "NULL";
	}

	public SelectableTable getTableGrid() {
		return tableGrid;
	}

	public List<Map<String, String>> getAllProps() {
		return allProps;
	}

	public List<String> getEditProps() {
		return editProps;
	}

	public List<String> getCreateProps() {
		return createProps;
	}

	public void setTableGrid(SelectableTable tableGrid) {
		this.tableGrid = tableGrid;
	}

	public void setAllProps(List<Map<String, String>> allProps) {
		this.allProps = allProps;
	}

	public void setEditProps(List<String> editProps) {
		this.editProps = editProps;
	}

	public void setCreateProps(List<String> createProps) {
		this.createProps = createProps;
	}

	public DataStore getDataStore() {
		return dataStore;
	}

	public void setDataStore(DataStore dataStore) {
		this.dataStore = dataStore;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isFile() {
		return file;
	}

	public void setFile(boolean file) {
		this.file = file;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String[]> getSelectedFiles() {
		return selectedFiles;
	}

	public void setSelectedFiles(List<String[]> selectedFiles) {
		this.selectedFiles = selectedFiles;
	}

	public Map<String, ParamProperty> getPropsParam() {
		return propsParam;
	}

	public void setPropsParam(Map<String, ParamProperty> propsParam) {
		this.propsParam = propsParam;
	}

	public String getFileContent() {
		return fileContent;
	}

	public void setFileContent(String content) {
		fileContent = content;
	}

	public Integer getCurrentFileIndex() {
		return currentFileIndex;
	}

	public void setCurrentFileIndex(Integer currentFileIndex) {
		this.currentFileIndex = currentFileIndex;
	}

	public LinkedHashMap<String, String> getNewProp() {
		return newProp;
	}

	public void setNewProp(LinkedHashMap<String, String> newProp) {
		this.newProp = newProp;
	}

	public String getShowCopyMove() {
		return showCopyMove;
	}

	public void setShowCopyMove(String showCopyMove) {
		this.showCopyMove = showCopyMove;
	}

	public List<SelectItem> getListExtensions() {
		return listExtensions;
	}

	public String getExtensionsSelected() {
		return extensionsSelected;
	}

	public void setListExtensions(List<SelectItem> listExtensions) {
		this.listExtensions = listExtensions;
	}

	public void setExtensionsSelected(String extensionsSelected) {
		this.extensionsSelected = extensionsSelected;
	}

	public String getOpenOutputData() {
		return openOutputData;
	}

	public void setOpenOutputData(String openOutputData) {
		this.openOutputData = openOutputData;
	}

	public boolean isAllowDirectories() {
		return allowDirectories;
	}

	public void setAllowDirectories(boolean allowDirectories) {
		this.allowDirectories = allowDirectories;
	}

	public List<String> getListExtensionsString() {
		return listExtensionsString;
	}

	public void setListExtensionsString(List<String> listExtensionsString) {
		this.listExtensionsString = listExtensionsString;
	}

	public boolean isAllowOnlyDirectories() {
		return allowOnlyDirectories;
	}

	public void setAllowOnlyDirectories(boolean allowOnlyDirectories) {
		this.allowOnlyDirectories = allowOnlyDirectories;
	}
	
}