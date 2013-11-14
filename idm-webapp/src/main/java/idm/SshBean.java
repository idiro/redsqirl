package idm;

import idiro.hadoop.NameNodeVar;
import idiro.workflow.server.connect.interfaces.DataStore;
import idiro.workflow.server.connect.interfaces.DataStoreArray;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.richfaces.event.DropEvent;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

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
				selectedTab = tabs.get(0);
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
		
		setDataStore(getDataStoreArray().getStores().get(name));
		
		setPath(getDataStore().getPath());
		logger.info("path: "+getPath());
//		if(getListGrid().isEmpty()){

			mountTable(getDataStore());
//		}
		
	}
	
	public void processDrop(DropEvent dropEvent) throws RemoteException { 
		logger.info("processDrop");
		
		FacesContext context = FacesContext.getCurrentInstance();
		String file = context.getExternalContext().getRequestParameterMap().get("file");
		String path = context.getExternalContext().getRequestParameterMap().get("path");
		String type = context.getExternalContext().getRequestParameterMap().get("type");
		
		logger.info("copy from "+type+":"+path+"/"+file+" to "+getPath());
		
		try{
			getHDFS().copyToRemote(path+"/"+file, getPath()+"/"+file, selectedTab);
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
	
//	public void copyFile() throws Exception{
//		FileSystem fs = NameNodeVar.getFS();
//		
//		FSDataOutputStream out = fs.create(new Path("path"));
//		
//		InputStream in = new BufferedInputStream(new FileInputStream(
//	        new File("source")));
//
//	    byte[] b = new byte[1024];
//	    int numBytes = 0;
//	    while ((numBytes = in.read(b)) > 0) {
//	        out.write(b, 0, numBytes);
//	    }
//
//	    // Close all the file descripters
//	    in.close();
//	    out.close();
//	    fs.close();
//	}
	
	
	
//	public InputStream copy(String rfile){
//
//		Properties config = new Properties(); 
//		config.put("StrictHostKeyChecking", "no");
//
//		JSch shell = new JSch();
//		Session session = shell.getSession("user", "localhost");
//		session.setPassword("password");
//		session.setConfig(config);
//		logger.info("session config set");
//		session.connect();
//
//		FileOutputStream fos=null;
//
//		// exec 'scp -f rfile' remotely
//		String command="scp -f "+rfile;
//		Channel channel=session.openChannel("exec");
//		((ChannelExec)channel).setCommand(command);
//
//		// get I/O streams for remote scp
//		OutputStream out=channel.getOutputStream();
//		InputStream in=channel.getInputStream();
//
//		channel.connect();
//
//		byte[] buf=new byte[1024];
//
//		// send '\0'
//		buf[0]=0; out.write(buf, 0, 1); out.flush();
//
//		while(true){
//			int c=checkAck(in);
//			if(c!='C'){
//				break;
//			}
//
//			// read '0644 '
//			in.read(buf, 0, 5);
//
//			long filesize=0L;
//			while(true){
//				if(in.read(buf, 0, 1)<0){
//					// error
//					break; 
//				}
//				if(buf[0]==' ')break;
//				filesize=filesize*10L+(long)(buf[0]-'0');
//			}
//
//			String file=null;
//			for(int i=0;;i++){
//				in.read(buf, i, 1);
//				if(buf[i]==(byte)0x0a){
//					file=new String(buf, 0, i);
//					break;
//				}
//			}
//
//			//System.out.println("filesize="+filesize+", file="+file);
//
//			// send '\0'
//			buf[0]=0; out.write(buf, 0, 1); out.flush();
//
//			// read a content of lfile
//			fos=new FileOutputStream(prefix==null ? lfile : prefix+file);
//			int foo;
//			while(true){
//				if(buf.length<filesize) foo=buf.length;
//				else foo=(int)filesize;
//				foo=in.read(buf, 0, foo);
//				if(foo<0){
//					// error 
//					break;
//				}
//				fos.write(buf, 0, foo);
//				filesize-=foo;
//				if(filesize==0L) break;
//			}
//			fos.close();
//			fos=null;
//
//			if(checkAck(in)!=0){
//				System.exit(0);
//			}
//
//			// send '\0'
//			buf[0]=0; out.write(buf, 0, 1); out.flush();
//		}
//
//		session.disconnect();
//	}
//
//	static int checkAck(InputStream in) throws IOException{
//		int b=in.read();
//		// b may be 0 for success,
//		//          1 for error,
//		//          2 for fatal error,
//		//          -1
//		if(b==0) return b;
//		if(b==-1) return b;
//	
//		if(b==1 || b==2){
//			StringBuffer sb=new StringBuffer();
//			int c;
//			do {
//				c=in.read();
//				sb.append((char)c);
//			}
//			while(c!='\n');
//			if(b==1){ // error
//				System.out.print(sb.toString());
//			}
//			if(b==2){ // fatal error
//				System.out.print(sb.toString());
//			}
//		}
//		return b;
//	}
}