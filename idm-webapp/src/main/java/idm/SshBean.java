package idm;

import idiro.workflow.server.connect.interfaces.DataStoreArray;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;

/** HiveBean
 * 
 * Class to screen control of the File System SSH
 * 
 * @author Igor.Souza
 */
public class SshBean extends BaseBean {


	private static Logger logger = Logger.getLogger(SshBean.class);

	private List<Entry> fieldsInitNeededNewSsh = new ArrayList<Entry>();
	private List<Entry> fieldsInitNeededTitleKey = new ArrayList<Entry>();

	private boolean selectedSaveSsh;

	/** openCanvasScreen
	 * 
	 * Methods to generating screen
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	@PostConstruct
	public void openCanvasScreen() {

		try {

			if(getFieldsInitNeededNewSsh().isEmpty()){
				openNewSsh();
			}

		} catch (RemoteException e) {
			logger.error(e);
		}

	}

	/** openNewSsh
	 * 
	 * Method to create the screen with necessary fields to configure a new file system
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void openNewSsh() throws RemoteException{

		DataStoreArray arr = getDataStoreArray();

		setFieldsInitNeededNewSsh(mapToList(arr.getFieldsInitNeeded()));
		setFieldsInitNeededTitleKey(mapToList(arr.getFieldsInitNeeded()));

		for (Entry entry : getFieldsInitNeededNewSsh()) {
			entry.setValue("");
		}



	}

	/** confirmNewSsh
	 * 
	 * Method to execute the connection
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void confirmNewSsh() throws RemoteException{


	}

	public List<Entry> getFieldsInitNeededNewSsh() {
		return fieldsInitNeededNewSsh;
	}

	public void setFieldsInitNeededNewSsh(List<Entry> fieldsInitNeededNewSsh) {
		this.fieldsInitNeededNewSsh = fieldsInitNeededNewSsh;
	}

	public List<Entry> getFieldsInitNeededTitleKey() {
		return fieldsInitNeededTitleKey;
	}

	public void setFieldsInitNeededTitleKey(List<Entry> fieldsInitNeededTitleKey) {
		this.fieldsInitNeededTitleKey = fieldsInitNeededTitleKey;
	}

	public boolean isSelectedSaveSsh() {
		return selectedSaveSsh;
	}

	public void setSelectedSaveSsh(boolean selectedSaveSsh) {
		this.selectedSaveSsh = selectedSaveSsh;
	}



}