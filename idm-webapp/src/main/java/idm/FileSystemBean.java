package idm;

import idiro.workflow.server.connect.interfaces.DataStore;
import idiro.workflow.server.connect.interfaces.DataStore.ParamProperty;
import idm.auth.UserInfoBean;
import idm.dynamictable.SelectableRow;
import idm.dynamictable.SelectableTable;
import idm.useful.MessageUseful;

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

import org.apache.log4j.Logger;

public class FileSystemBean extends BaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(FileSystemBean.class);
	private static int nbCreate = 0;
	
	/*private Map<String, String> nameHelp = new LinkedHashMap<String, String>();
	private List<String> nameCreateFields = new ArrayList<String>();
	private Map<String, String> nameValue = new LinkedHashMap<String, String>();
	private String path;
	private EditFileSystem item;
	private ArrayList<EditFileSystem> listGrid = new ArrayList<EditFileSystem>();*/

	/*
	private List<Entry<String, String>> fieldsInitNeededTitleKey;
	private ArrayList<ItemList> listHeaderGrid = new ArrayList<ItemList>();
	 */
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

	/**
	 * mountTable
	 * 
	 * Method to mount the file system grid
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void mountTable() throws RemoteException {
		DataStore hInt = getDataStore();
		logger.info("Started mounting table");
		setPath(hInt.getPath());

		Map<String, Map<String, String>> mapSSH = hInt.getChildrenProperties();
		setPropsParam(hInt.getParamProperties());

		if (mapSSH != null) {
			//Set list features
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

			updateTable();

		}

		FacesContext context = FacesContext.getCurrentInstance();
		UserInfoBean userInfoBean = (UserInfoBean) context.getApplication()
				.evaluateExpressionGet(context, "#{userInfoBean}",
						UserInfoBean.class);

		if (userInfoBean.getCurrentValue() < 96) {
			userInfoBean.setCurrentValue(userInfoBean.getCurrentValue() + 5);
		}

		logger.info("Finished mounting table");
	}

	public void updateTable() throws RemoteException{

		setPath(getDataStore().getPath());
		Map<String, Map<String, String>> mapSSH = getDataStore().getChildrenProperties();
		//Fill rows
		if (mapSSH != null) {
			setAllProps(new LinkedList<Map<String,String>>());
			getTableGrid().getRows().clear();
			for (String path : mapSSH.keySet()) {

				String[] aux = path.split("/");
				String name = aux[aux.length - 1];

				Map<String, String> allProperties = new LinkedHashMap<String, String>();
				allProperties.put("name", name);
				allProperties.putAll(mapSSH.get(path));
				getTableGrid().add(allProperties);
				getAllProps().add(allProperties);

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
			updateTable();
		}

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
			
			logger.info(""+ getPropsParam());
			logger.info(""+ getTableGrid().getTitles());
			logger.info(""+ getEditProps());
			logger.info(""+ getCreateProps());
			
			updateTable();
		} else {
			getBundleMessage("error.invalid.path");
		}

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

			Map<String, String> params = FacesContext.getCurrentInstance()
					.getExternalContext().getRequestParameterMap();
			String name = params.get("nameFile");

			String path = generatePath(getPath(), name);

			if (getDataStore().goTo(path)) {
				setPath(path);
				updateTable();
			} else {
				logger.error("Error this is not a directory");
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
		String path = generatePath(getPath(), name);
		getDataStore().goTo(path);
		List<String> contents = getDataStore().select(" | ", 10);
		fileContent = "";
		for (String s : contents) {
			fileContent += s + "<br/>";
		}
		getDataStore().goPrevious();
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
		verifyIfIsFile(name);
	}

	public void verifyIfIsFile(String name) throws RemoteException {
		getDataStore().goTo(generatePath(getDataStore().getPath(), name));
		file = getDataStore().getChildrenProperties() == null;
		getDataStore().goPrevious();
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
		logger.info("copy file after");
		SelectableRow itemSelect = null;
		for (Iterator<SelectableRow> i = getTableGrid().getRows().iterator(); i.hasNext();) {
			SelectableRow item = i.next();

			if (item.isSelected()) {
				itemSelect = item;
				break;
			}
		}

		for (String[] s : selectedFiles) {
			if (itemSelect != null) {
				logger.info("move " + s[0] + "/" + s[1] + " to " + getDataStore().getPath() + "/" + itemSelect.getRow()[0]);
				getDataStore().move(s[0] + "/" + s[1], getDataStore().getPath() + "/" + itemSelect.getRow()[0] + "/" + s[1]);
			} else {
				logger.info("move " + s[0] + "/" + s[1] + " to " + getDataStore().getPath());
				getDataStore().move(s[0] + "/" + s[1], getDataStore().getPath() + "/" + s[1]);
			}
		}

		updateTable();
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

		updateTable();

	}

	/**
	 * getItemByName
	 * 
	 * Method to retrieve the selected file by its name
	 * 
	 * @return ItemList
	 * @author Igor.Souza
	 */
	/*public EditFileSystem getItemByName(String name) throws RemoteException {

		logger.info("getItemByName");

		for (EditFileSystem it : getListGrid()) {
			if (it.getName().equals(name)) {
				return it;
			}
		}

		logger.info("getItemByName return null");
		return null;
	}*/

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
			String error = getDataStore().changeProperties(getDataStore().getPath() + "/" + getName(), 
					prop);
			logger.info("change properties error : " + error);
		} catch (Exception e) {
			logger.error("Error change properties : " + e.getMessage());
			MessageUseful.addErrorMessage("Fail to update properties of " + getDataStore().getPath() + "/" + newName + " to " + getTableGrid().getRow(0));

		}
		if(!newName.equals(name)){
			logger.info("Rename " + getDataStore().getPath() + "/" + getName() + " to " + getDataStore().getPath() + "/" + newName);
			getDataStore().move(getDataStore().getPath() + "/" + getName(),	getDataStore().getPath() + "/" + newName);
		}

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

		SelectableRow itemSelect = null;
		for (Iterator<SelectableRow> i = getTableGrid().getRows().iterator(); i.hasNext();) {
			SelectableRow item = (SelectableRow) i.next();

			if (item.isSelected()) {
				itemSelect = item;
				break;
			}
		}

		for (String[] s : selectedFiles) {
			if (itemSelect != null) {
				logger.info("move " + s[0] + "/" + s[1] + " to " + getDataStore().getPath() + "/" + getTableGrid().getRows().get(0));
				getDataStore().move(s[0] + "/" + s[1], getDataStore().getPath() + "/" + getTableGrid().getRows().get(0) + "/" + s[1]);
			} else {
				logger.info("move " + s[0] + "/" + s[1] + " to " + getDataStore().getPath());
				getDataStore().move(s[0] + "/" + s[1], getDataStore().getPath() + "/" + s[1]);
			}
		}
		updateTable();
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
		updateTable();

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
		updateTable();

	}

	private void mountSelectedFilesList() throws RemoteException {
		selectedFiles = new ArrayList<String[]>();
		for (SelectableRow i : getTableGrid().getRows()) {
			if (i.isSelected()) {
				selectedFiles.add(new String[] { getDataStore().getPath(), i.getRow()[0] });
			}
		}
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

		logger.info("goUp: " + getPath());

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
					updateTable();
				} else {
					getBundleMessage("error.invalid.path");
				}
			}
		}

	}

	/**
	 * createNewFolder
	 * 
	 * Method to create a default folder to save
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void createNewFolder() throws RemoteException {

		String newPath = "/user/" + System.getProperty("user.name") + "/idm-save";

		if (getDataStore().goTo(newPath)) {
			updateTable();
		} else {
			getDataStore().create(newPath, new LinkedHashMap<String, String>());
			if (getDataStore().goTo(newPath)) {
				updateTable();
			} else {
				getBundleMessage("error.invalid.path");
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
		return getDataStore().canDelete();
	}

	public String getCanCreate() throws RemoteException {
		return getDataStore().canCreate();
	}

	/*

	public ArrayList<EditFileSystem> getListGrid() {
		return listGrid;
	}

	public void setListGrid(ArrayList<EditFileSystem> listGrid) {
		this.listGrid = listGrid;
	}

	public EditFileSystem getItem() {
		return item;
	}

	public void setItem(EditFileSystem item) {
		this.item = item;
	}

	public Map<String, String> getNameValue() {
		return nameValue;
	}

	public void setNameValue(Map<String, String> nameValue) {
		this.nameValue = nameValue;
	}

	public Map<String, String> getNameHelp() {
		return nameHelp;
	}

	public void setNameHelp(Map<String, String> nameHelp) {
		this.nameHelp = nameHelp;
	}

	public List<String> getNameCreateFields() {
		return nameCreateFields;
	}

	public void setNameCreateFields(List<String> nameCreateFields) {
		this.nameCreateFields = nameCreateFields;
	}

	 */



	/*
	public ArrayList<ItemList> getListHeaderGrid() {
		return listHeaderGrid;
	}

	public void setListHeaderGrid(ArrayList<ItemList> listHeaderGrid) {
		this.listHeaderGrid = listHeaderGrid;
	}

	public List<Entry<String, String>> getFieldsInitNeededTitleKey() {
		return fieldsInitNeededTitleKey;
	}

	public void setFieldsInitNeededTitleKey(
			List<Entry<String, String>> fieldsInitNeededTitleKey) {
		this.fieldsInitNeededTitleKey = fieldsInitNeededTitleKey;
	}
	 */


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

}
