package idm;

import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.connect.interfaces.DataStore;
import idiro.workflow.server.connect.interfaces.DataStoreArray;
import idiro.workflow.server.connect.interfaces.PropertiesManager;
import idiro.workflow.server.interfaces.JobManager;
import idm.useful.IdmEntry;
import idm.useful.MessageUseful;

import java.io.File;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;


/** BaseBean
 * 
 * Class to the methods common to all Beans
 * 
 * @author Igor.Souza
 */
public class BaseBean {


	private static Logger bb_logger = Logger.getLogger(BaseBean.class);
	
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
	 * Methods retrieve message. Retrieves the message from aplication.properties
	 * 
	 * @return string message
	 * @author Igor.Souza
	 */
	public String getMessageResources(String msg){

		FacesContext facesContext = FacesContext.getCurrentInstance();
		String messageBundleName = facesContext.getApplication().getMessageBundle();
		Locale locale = facesContext.getViewRoot().getLocale();
		ResourceBundle bundle = ResourceBundle.getBundle(messageBundleName, locale);
		return bundle.getString(msg);
	}
	
	/** getMessageResourcesWithParameter
	 * 
	 * Methods retrieve message. Retrieves the message from aplication.properties with parameters
	 * 
	 * @return string message
	 * @author Igor.Souza
	 */
	public static String getMessageResourcesWithParameter(String msgid, Object[] args){
		FacesContext facesContext = FacesContext.getCurrentInstance();
		String messageBundleName = facesContext.getApplication().getMessageBundle();
		Locale locale = facesContext.getViewRoot().getLocale();
		ResourceBundle bundle = ResourceBundle.getBundle(messageBundleName, locale);
		return MessageFormat.format(bundle.getString(msgid), args);
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
	
	/** getHDFSBrowser
	 * 
	 * Methods to retrieve the object DataStore HDFS from context
	 * 
	 * @return DataStore
	 * @author Igor.Souza
	 */
	public DataStore getHDFSBrowser() throws RemoteException{

		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);

		return (DataStore) session.getAttribute("hdfsbrowser");
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
	
	/** getPrefs
	 * 
	 * Methods to retrieve the object prefs from context
	 * 
	 * @return JobManager
	 * @author Igor.Souza
	 */
	public PropertiesManager getPrefs() throws RemoteException{

		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);

		return (PropertiesManager) session.getAttribute("prefs");
	}
	

	public File getCurrentPage(){
		String currentPage = ((HttpServletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest())
				.getRequestURI();
		File f = null;
		//		logger.info(currentPage);
		if (currentPage != null && !currentPage.isEmpty()) {
			List<Integer> pos = new ArrayList<Integer>();
			for (int i = 0; i < currentPage.length(); i++) {

				if (currentPage.charAt(i) == '/') {
					pos.add(i);
				}
			}
			currentPage = currentPage.substring(0, pos.get(pos.size() - 1));
			try {
				f = new File(
						WorkflowPrefManager
						.getSysProperty(WorkflowPrefManager.sys_tomcat_path)
						+ currentPage);
				if (!f.exists()) {
					f = new File(
							WorkflowPrefManager
							.getSysProperty(WorkflowPrefManager.sys_tomcat_path)
							+ currentPage.substring(pos.get(1)));
				}
				bb_logger.info(f.getAbsolutePath());
			} catch (Exception e) {
				//				logger.info("E");
			}
			
		}

		return f;
	}
}