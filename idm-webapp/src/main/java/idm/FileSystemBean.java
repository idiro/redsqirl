package idm;

import idiro.workflow.server.connect.interfaces.DataStore;
import idiro.workflow.server.connect.interfaces.DataStore.ParamProperty;
import idm.useful.MessageUseful;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

public class FileSystemBean extends BaseBean implements Serializable{

	private static Logger logger = Logger.getLogger(FileSystemBean.class);

	private String path;
	private String name;
	private String newName;
	private ArrayList<ItemList> listGrid = new ArrayList<ItemList>();
	private List<String> nameCreateFields = new ArrayList<String>();
	private ItemList item;
	private String fileContent;
	private boolean file;

	private Map<String, String> nameValue = new HashMap<String, String>();
	private Map<String, String> nameHelp = new HashMap<String, String>();
	private List<Entry> fieldsInitNeededTitleKey;
	
	private DataStore dataStore;
	
	private List<String[]> selectedFiles;


	/** openCanvasScreen
	 * 
	 * Methods to generating screen
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	@PostConstruct
	public void openCanvasScreen() {

	}

	/** openCanvasScreen
	 * 
	 * Method that is executed when the screen
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	@PreDestroy
	public void closeCanvasScreen() {

	}

	/** mountTable
	 * 
	 * Method to mount the file system grid
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void mountTable(DataStore hInt) throws RemoteException {
		logger.info("Started mounting table");
		setListGrid(new ArrayList<ItemList>());
		
		setPath(hInt.getPath());

		Map<String, Map<String, String>> mapSSH = hInt.getChildrenProperties();
		Map<String, ParamProperty> paramProperties = hInt.getParamProperties();
		for (String path : mapSSH.keySet()) {

			String[] aux = path.split("/");
			String name = aux[aux.length-1];

			ItemList itemList = new ItemList(name);
			Map<String, String> nv = new HashMap<String, String>();
			Map<String, String> nve = new HashMap<String, String>();
			Map<String, Boolean> nc = new HashMap<String, Boolean>();
			Map<String, Boolean> vlb = new HashMap<String, Boolean>();

			for (String properties : paramProperties.keySet()) {

				if(!paramProperties.get(properties).editOnly() &&
					!paramProperties.get(properties).createOnly()){
					nv.put(properties, getFormatedString(properties, mapSSH.get(path).get(properties)));
				}
				
				if (paramProperties.get(properties).editOnly()){
					nve.put(properties, getFormatedString(properties, mapSSH.get(path).get(properties)));
				}
				
				nc.put(properties, paramProperties.get(properties).isConst());
				vlb.put(properties, mapSSH.get(path).get(properties) != null && mapSSH.get(path).get(properties).contains("/n"));

			}

			itemList.setNameValue(nv);
			itemList.setNameValueEdit(nve);
			itemList.setNameIsConst(nc);
			itemList.setValueHasLineBreak(vlb);
			
			setNameValue(nv);
			itemList.setSelected(false);
			getListGrid().add(itemList);

		}
		
		for (String properties : paramProperties.keySet()) {
			nameHelp.put(properties, paramProperties.get(properties).getHelp());
			if (paramProperties.get(properties).createOnly()){
				nameCreateFields.add(properties);
			}
		}
		logger.info("Finished mounting table");
	}
	
	public String getFormatedString(String property, String value){
		return value;
	}

	/** getKeyAsListNameValue
	 * 
	 * Method to retrieve the list of files
	 * 
	 * @return List<String>
	 * @author Igor.Souza
	 */
	public List<String> getKeyAsListNameValue(){
		return new ArrayList<String>(nameValue.keySet());
	}

	/** deleteFile
	 * 
	 * Method to delete the selected file in the file system screen
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void deleteFile() throws RemoteException{

		for (Iterator<ItemList> i = getListGrid().iterator(); i.hasNext();) {
			ItemList item = i.next();

			if(item.isSelected()){

				String directory = generatePath(getDataStore().getPath(), item.getName());
				
				logger.info("Delete -"+directory);

				getDataStore().delete(directory);
				i.remove();

			}
		}

	}

	/** changePath
	 * 
	 * Method that retrieves the path is typed on the screen and the update of the grid
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void changePath() throws RemoteException{

		if(getDataStore().goTo(getPath())){
			getDataStore().getPath();
			mountTable(getDataStore());
		}else{
			getBundleMessage("error.invalid.path");
		}

	}

	/** selectFile
	 * 
	 * Method to navigate through the file system using the directory selected
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void selectFile(){

		try {

			Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
			String name = params.get("nameFile");

			String path = generatePath(getPath(), name);
			
			if(getDataStore().goTo(path)){
				setPath(path);
				mountTable(getDataStore());
			}else{
				logger.error("Error this is not a directory");
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			MessageUseful.addErrorMessage(" ");
		}

	}
	
	/** open
	 * 
	 * Method to preview the contents of a file
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void openFile() throws RemoteException{
		String name = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("nameFile");
		String path = generatePath(getPath(), name);
		getDataStore().goTo(path);
		List<String> contents = getDataStore().select(" | ", 10);
		fileContent = "";
		for (String s : contents){
			fileContent += s+"<br/>";
		}
		getDataStore().goPrevious();
	}
	
	/** verifyIfIsFile
	 * 
	 * Method to verify if the selected path is a file or a directory
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void verifyIfIsFile() throws RemoteException{
		String name = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("nameFile");
		getDataStore().goTo(generatePath(getDataStore().getPath(), name));
		file = getDataStore().getChildrenProperties().isEmpty();
		getDataStore().goPrevious();
	}
	
	/** copyFileBefore
	 * 
	 * Method to execute before opening the screen to copy a file
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void copyFileBefore() throws RemoteException{
		mountSelectedFilesList();
	}

	/** copyFileAfter
	 * 
	 * Method to run the file copy
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void copyFileAfter() throws RemoteException{
		logger.info("copy file after");
		ItemList itemSelect = null;
		for (Iterator<ItemList> i = getListGrid().iterator(); i.hasNext();) {
			ItemList item = i.next();

			if(item.isSelectedDestination()){
				itemSelect = item;
				break;
			}
		}

		for (String[] s : selectedFiles) {
			logger.info("copy "+s[0] + "/" + s[1] + " to " + getDataStore().getPath() + "/" + itemSelect.getName());
			getDataStore().copy(s[0] + "/" + s[1], getDataStore().getPath() + "/" + itemSelect.getName()+ "/" + s[1]);
		}
		
		mountTable(getDataStore());
	}

	/** addFileBefore
	 * 
	 * Method to execute before opening the screen to add a file
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void addFileBefore() throws RemoteException{
	}

	/** addFileAfter
	 * 
	 * Method to run the add file
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void addFileAfter() throws RemoteException{
		String newDirectory = generatePath(getDataStore().getPath(), getNewName());
		
		Map<String, String> properties = new HashMap<String, String>();
		for (Entry<String, String> e : nameValue.entrySet()){
			if (e.getValue() != null && !e.getValue().isEmpty()){
				properties.put(e.getKey(), e.getValue());
			}
		}
		getDataStore().create(newDirectory, properties);
		mountTable(getDataStore());
	}

	/** editFileBefore
	 * 
	 * Method to execute before opening the screen to edit a file
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void editFileBefore() throws RemoteException{

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("nameFileEdit");

		setName(name);
		setNewName(name);

		setItem(getItemByName(name));

	}

	/** getItemByName
	 * 
	 * Method to retrieve the selected file by its name
	 * 
	 * @return ItemList
	 * @author Igor.Souza
	 */
	public ItemList getItemByName(String name) throws RemoteException{

		for (ItemList it : getListGrid()){
			if(it.getName().equals(name)){
				return it;
			}
		}

		return null;
	}

	/** editFileAfter
	 * 
	 * Method to edit a file
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void editFileAfter() throws RemoteException{

		for (Iterator<String> iterator = nameValue.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			getDataStore().changeProperty(getDataStore().getPath() + "/" + getName(), key, nameValue.get(key) );
		}

		getDataStore().changeProperties(getItem().getNameValue());

		getDataStore().move(getDataStore().getPath() + "/" + getName(), getDataStore().getPath() + "/" + getNewName());

	}

	/** moveFileBefore
	 * 
	 * Method to execute before opening the screen to move a file
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void moveFileBefore() throws RemoteException{
		mountSelectedFilesList();
	}

	/** moveFileAfter
	 * 
	 * Method to move a file
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void moveFileAfter() throws RemoteException{
		
		ItemList itemSelect = null;
		for (Iterator<ItemList> i = getListGrid().iterator(); i.hasNext();) {
			ItemList item = (ItemList) i.next();

			if(item.isSelectedDestination()){
				itemSelect = item;
				break;
			}
		}

		for (String[] s : selectedFiles) {
			logger.info("move "+s[0] + "/" + s[1] + " to " + getDataStore().getPath() + "/" + itemSelect.getName());
			getDataStore().move(s[0] + "/" + s[1], getDataStore().getPath() + "/" + itemSelect.getName()+ "/" + s[1]);
		}
		mountTable(getDataStore());
	}

	/** goPrevious
	 * 
	 * Method to navigate the file system. updates the grid with the information from the previous directory	
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void goPrevious() throws RemoteException{

		getDataStore().goPrevious();
		mountTable(getDataStore());

	}

	/** goNext
	 * 
	 * Method to navigate the file system. updates the grid with information from the directory later
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void goNext() throws RemoteException{

		getDataStore().goNext();
		mountTable(getDataStore());

	}
	
	private void mountSelectedFilesList() throws RemoteException{
		selectedFiles = new ArrayList<String[]>();
		for (ItemList i : getListGrid()) {
			if(i.isSelected()){
				selectedFiles.add(new String[]{getDataStore().getPath(), i.getName()});
			}
		}
	}
	
	public String getCanCopy() throws RemoteException{
		return getDataStore().canCopy();
	}
	
	public String getCanMove() throws RemoteException{
		return getDataStore().canMove();
	}
	
	public String getCanDelete() throws RemoteException{
		return getDataStore().canDelete();
	}
	
	public String getCanCreate() throws RemoteException{
		return getDataStore().canCreate();
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public ArrayList<ItemList> getListGrid() {
		return listGrid;
	}

	public void setListGrid(ArrayList<ItemList> listGrid) {
		this.listGrid = listGrid;
	}

	public ItemList getItem() {
		return item;
	}

	public void setItem(ItemList item) {
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

	public List<Entry> getFieldsInitNeededTitleKey() {
		return fieldsInitNeededTitleKey;
	}

	public void setFieldsInitNeededTitleKey(List<Entry> fieldsInitNeededTitleKey) {
		this.fieldsInitNeededTitleKey = fieldsInitNeededTitleKey;
	}

	public DataStore getDataStore() {
		return dataStore;
	}

	public void setDataStore(DataStore dataStore) {
		this.dataStore = dataStore;
	}
	
	public List<String> getNameCreateFields() {
		return nameCreateFields;
	}

	public void setNameCreateFields(List<String> nameCreateFields) {
		this.nameCreateFields = nameCreateFields;
	}
	
	public String getFileContent(){
		return fileContent;
	}
	
	public void setFileContent(String content){
		fileContent = content;
	}
	
	public boolean isFile(){
		return file;
	}
	
	public void setFile(boolean file){
		this.file = file;
	}
	
	private String generatePath(String path, String name){
		String resultPath = path;
		if (!resultPath.endsWith("/")){
			resultPath += "/";
		}
		resultPath += name;
		return resultPath;
	}
	
}