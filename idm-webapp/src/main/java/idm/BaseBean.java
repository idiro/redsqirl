package idm;

import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.connect.interfaces.DataStore;
import idiro.workflow.server.connect.interfaces.DataStoreArray;
import idiro.workflow.server.connect.interfaces.PckManager;
import idiro.workflow.server.interfaces.JobManager;
import idm.useful.IdmEntry;
import idm.useful.MessageUseful;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/** BaseBean
 * 
 * Class to the methods common to all Beans
 * 
 * @author Igor.Souza
 */
public class BaseBean {


	
	/** getBundleMessage
	 * 
	 * Methods to put message on the screen. Retrieves the message from aplication.properties
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public static void getBundleMessage(String msg){

		FacesContext facesContext = FacesContext.getCurrentInstance();
		String messageBundleName = facesContext.getApplication().getMessageBundle();
		Locale locale = facesContext.getViewRoot().getLocale();
		ResourceBundle bundle = ResourceBundle.getBundle(messageBundleName, locale);
		MessageUseful.addErrorMessage(bundle.getString(msg));

	}
	
	/** getMessageResources
	 * 
	 * Methods retrieve message . Retrieves the message from aplication.properties
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public String getMessageResources(String msg){

		FacesContext facesContext = FacesContext.getCurrentInstance();
		String messageBundleName = facesContext.getApplication().getMessageBundle();
		Locale locale = facesContext.getViewRoot().getLocale();
		ResourceBundle bundle = ResourceBundle.getBundle(messageBundleName, locale);
		return bundle.getString(msg);

	}
	
	/** mapToList
	 * 
	 * Methods to transform the map into a list of the Entry object(value, key). Object used to display dynamic fields
	 * 
	 * @return List<Entry>
	 * @author Igor.Souza
	 */
	public <K, V> List<Entry<K,V>> mapToList(Map<K,V> map) {
		List<Entry<K,V>> list = new ArrayList<Entry<K,V>>();
		for(K key: map.keySet()) {
			list.add(new IdmEntry<K,V>(key, map.get(key)));
		}
		return list;        
	}

	/** getworkFlowInterface
	 * 
	 * Methods to retrieve the object DataFlowInterface from context
	 * 
	 * @return DataFlowInterface
	 * @author Igor.Souza
	 */
	public DataFlowInterface getworkFlowInterface() throws RemoteException{

		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);

		return (DataFlowInterface) session.getAttribute("wfm");
	}

	/** getHiveInterface
	 * 
	 * Methods to retrieve the object DataStore hive from context
	 * 
	 * @return DataStore
	 * @author Igor.Souza
	 */
	public DataStore getHiveInterface() throws RemoteException{

		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);

		return (DataStore) session.getAttribute("hive");
	}

	/** getDataStoreArray
	 * 
	 * Methods to retrieve the object DataStoreArray from context
	 * 
	 * @return DataStoreArray
	 * @author Igor.Souza
	 */
	public DataStoreArray getDataStoreArray() throws RemoteException{

		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);

		return (DataStoreArray) session.getAttribute("ssharray");
	}
	
	/** getHDFS
	 * 
	 * Methods to retrieve the object DataStore HDFS from context
	 * 
	 * @return DataStore
	 * @author Igor.Souza
	 */
	public DataStore getHDFS() throws RemoteException{

		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);

		return (DataStore) session.getAttribute("hdfs");
	}

	/** getOozie
	 * 
	 * Methods to retrieve the object JobManager Oozie from context
	 * 
	 * @return JobManager
	 * @author Igor.Souza
	 */
	public JobManager getOozie() throws RemoteException{

		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);

		return (JobManager) session.getAttribute("oozie");
	}

	/** getPckManager
	 * 
	 * Methods to retrieve the object Package Manager from context
	 * 
	 * @return JobManager
	 * @author Igor.Souza
	 */
	public PckManager getPckMng() throws RemoteException{

		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);

		return (PckManager) session.getAttribute("pckmng");
	}
}