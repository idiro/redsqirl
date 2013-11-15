package idm;

import java.rmi.RemoteException;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.richfaces.event.DropEvent;

/** HdfsBean
 * 
 * Class to screen control of the File System Hadoop
 * 
 * @author Igor.Souza
 */
public class HdfsBean extends FileSystemBean {

	private static Logger logger = Logger.getLogger(HdfsBean.class);


	/** openCanvasScreen
	 * 
	 * Methods to generating screen
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	@PostConstruct
	public void openCanvasScreen() {
		logger.info("HdfsOpenCanvasScreen");
		try {

			setDataStore(getHDFS());

			if(getListGrid().isEmpty()){

				mountTable(getDataStore());
			}

		}catch(Exception e){
			logger.error(e);
			getBundleMessage("error.mount.table");
		}

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
	
	public void addFileAfter() throws RemoteException{
		setNameValue(new HashMap<String, String>());
		super.addFileAfter();
	}
	
	public void processDrop(DropEvent dropEvent) throws RemoteException { 
		logger.info("processDrop");
		
		FacesContext context = FacesContext.getCurrentInstance();
		String file = context.getExternalContext().getRequestParameterMap().get("file");
		String path = context.getExternalContext().getRequestParameterMap().get("path");
		String server = context.getExternalContext().getRequestParameterMap().get("server");
		
		logger.info("copy from "+path+"/"+file+" to "+server+":"+getPath()+"/"+file);
		
		try{
			getHDFS().copyFromRemote(path+"/"+file, getPath()+"/"+file, server);
			mountTable(getDataStore());
		}
		catch(Exception e){
			logger.info("", e);
		}
	} 

}