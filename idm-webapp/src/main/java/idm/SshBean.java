package idm;

import idiro.workflow.server.connect.interfaces.DataStore;
import idiro.workflow.server.connect.interfaces.DataStoreArray;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.richfaces.event.DropEvent;

/** HiveBean
 * 
 * Class to screen control of the File System SSH
 * 
 * @author Igor.Souza
 */
public class SshBean extends FileSystemBean implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(SshBean.class);

	private List<Entry<String, String>> fieldsInitNeededNewSsh = new ArrayList<Entry<String, String>>();
	private List<Entry<String, String>> fieldsInitNeededTitleKey = new ArrayList<Entry<String, String>>();
	
	private List<String> tabs;

	private boolean selectedSaveSsh;
	
	private String host;
	private String port;
	
	private String selectedTab;

	/** openCanvasScreen
	 * 
	 * Methods to generating screen
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws Exception 
	 */
	@PostConstruct
	public void openCanvasScreen() {
		
		try {
			
			logger.info(getDataStoreArray().initKnownStores());
			
			tabs = new ArrayList<String>();
			for (Entry<String, DataStore> e : getDataStoreArray().getStores().entrySet()){
				tabs.add(e.getKey());
			}

			if (!tabs.isEmpty()){
				setSelectedTab(tabs.get(0));
				setDataStore(getDataStoreArray().getStores().get(selectedTab));
				
				if(getListGrid().isEmpty()){
					mountTable(getDataStore());
				}
			}
			
			DataStoreArray arr = getDataStoreArray();

			setFieldsInitNeededNewSsh(mapToList(arr.getFieldsInitNeeded()));
			setFieldsInitNeededTitleKey(mapToList(arr.getFieldsInitNeeded()));

			for (Entry<String, String> entry : getFieldsInitNeededNewSsh()) {
				entry.setValue("");
			}
			
			

		}catch(Exception e){
			logger.error(e);
			getBundleMessage("error.mount.table");
		}

//		try {
//
//			if(getFieldsInitNeededNewSsh().isEmpty()){
//				openNewSsh();
//			}
//
//		} catch (Exception e) {
//			logger.error(e);
//		}

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

		
	}

	/** confirmNewSsh
	 * 
	 * Method to execute the connection
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws Exception 
	 */
	public void confirmNewSsh() throws Exception{
		
		logger.info("confirmNewSsh");
		
		Map<String, String> values = new HashMap<String, String>();
		values.put("host name", getHost());
		values.put("port", getPort());
		logger.info("host name: "+getHost());
		logger.info("port: "+getPort());
		
		logger.info(isSelectedSaveSsh());
		
		if (isSelectedSaveSsh()){
			logger.info(getDataStoreArray().addKnownStore(values));
		}
		else{
			logger.info(getDataStoreArray().addStore(values));
		}
		
		logger.info(getDataStoreArray().initKnownStores());
		
		tabs = new ArrayList<String>();
		for (Entry<String, DataStore> e : getDataStoreArray().getStores().entrySet()){
			tabs.add(e.getKey());
		}
	}
	
	public void changeTab() throws RemoteException, Exception{
		
		logger.info("changeTab");
		
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("nameTab");
		
		logger.info("changeTab: "+name);
		setSelectedTab(name);
		setDataStore(getDataStoreArray().getStores().get(name));
		
		setPath(getDataStore().getPath());
		logger.info("path: "+getPath());

		mountTable(getDataStore());
		
	}
	
	public void processDrop(DropEvent dropEvent) throws RemoteException { 
		logger.info("processDrop");
		
		FacesContext context = FacesContext.getCurrentInstance();
		String file = context.getExternalContext().getRequestParameterMap().get("file");
		String path = context.getExternalContext().getRequestParameterMap().get("path");
		
		logger.info("copy from "+path+"/"+file+" to "+getSelectedTab()+":"+getPath()+"/"+file);
		
		try{
			getHDFS().copyToRemote(path+"/"+file, getPath()+"/"+file, getSelectedTab());
			mountTable(getDataStore());
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
		logger.info("getTabs:"+tabs.size());
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
}