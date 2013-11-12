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

import org.apache.log4j.Logger;

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

			setDataStore(getDataStoreArray().getStores().get("dev"));

			if(getListGrid().isEmpty()){

				mountTable(getDataStore());
			}
			
			DataStoreArray arr = getDataStoreArray();

			setFieldsInitNeededNewSsh(mapToList(arr.getFieldsInitNeeded()));
			setFieldsInitNeededTitleKey(mapToList(arr.getFieldsInitNeeded()));

			for (Entry entry : getFieldsInitNeededNewSsh()) {
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
		
//		Map<String, String> values = new HashMap<String, String>();
//		values.put("host name", "dev");
//		values.put("port", "");
//		for (Entry<String, String> e : getFieldsInitNeededNewSsh()){
//			logger.info(e.getKey()+" - "+e.getValue());
//			values.put(e.getKey(), "dev");
//		}
		
//		logger.info(getDataStoreArray().addKnownStore(values));
//		logger.info(getDataStoreArray().addStore(values));


		logger.info(getDataStoreArray().initKnownStores());
		logger.info("Stores");
		for (Entry<String, DataStore> e : getDataStoreArray().getStores().entrySet()){
			logger.info(e.getKey()+" - "+e.getValue());
		}
		logger.info("Known Stores");
		for (Map<String, String> e2 : getDataStoreArray().getKnownStoreDetails()){
			for (Entry<String, String> e : e2.entrySet()){
				logger.info(e.getKey()+" - "+e.getValue());
			}
		}
		
	}
	
	public void aaa(){
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
		
		if (tabs == null){
			logger.info("creatingTabs");
			tabs = new ArrayList<String>();
			tabs.add("tab1");
			tabs.add("tab2");
		}
		logger.info("getTabs:"+tabs.size());
		return tabs;
	}
	
	public void addTab(){
		logger.info("addTab");
		tabs.add("tab3");
	}



}