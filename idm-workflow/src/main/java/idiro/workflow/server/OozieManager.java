package idiro.workflow.server;

import idiro.hadoop.NameNodeVar;
import idiro.utils.LocalFileSystem;
import idiro.workflow.server.interfaces.DataFlow;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.server.interfaces.JobManager;
import idiro.workflow.server.interfaces.OozieXmlCreator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;
import org.apache.oozie.client.WorkflowAction;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 * Oozie Interface with IDM.
 * @author etienne
 *
 */
public class OozieManager extends UnicastRemoteObject implements JobManager{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2909899568049533678L;

	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(OozieManager.class);


	private static OozieManager instance = null;

	private OozieClient oc = null;
	public final String xmlns;


	public static final String prop_namenode = "namenode",
			prop_jobtracker = "jobtracker",
			prop_queue = "queue",
			prop_user = "user.name",
			prop_libpath = "oozie.libpath";


	public static final String oozie_mode_default = "default";

	/**
	 * Constructor.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * 
	 */
	private OozieManager() throws FileNotFoundException, IOException {
		super();
		Properties prop = WorkflowPrefManager.getSysProperties();
		oc = new OozieClient(prop.getProperty(
				WorkflowPrefManager.sys_oozie
				));
		xmlns = prop.getProperty(
				WorkflowPrefManager.sys_oozie_xmlns
				);

	}

	/**
	 * 
	 * @return Returns the single allowed instance of ProcessRunner
	 */
	public static OozieManager getInstance() {
		if(instance == null){
			try{
				instance = new OozieManager();
			}catch(FileNotFoundException e){
				logger.error("No configuration file found to initialise oozie");
			}catch(IOException e){
				logger.error("IOException when attempting to read oozie properties");
			}
		}
		return instance;
	}

	/**
	 * @return
	 * @see org.apache.oozie.client.OozieClient#createConfiguration()
	 */
	public Properties createConfiguration() {
		return oc.createConfiguration();
	}

	/**
	 * @param jobId
	 * @throws OozieClientException
	 * @see org.apache.oozie.client.OozieClient#kill(java.lang.String)
	 */
	public void kill(String jobId) throws OozieClientException {
		oc.kill(jobId);
	}

	/**
	 * @param jobId
	 * @throws OozieClientException
	 * @see org.apache.oozie.client.OozieClient#resume(java.lang.String)
	 */
	public void resume(String jobId) throws OozieClientException {
		oc.resume(jobId);
	}

	/**
	 * @param conf
	 * @return
	 * @throws OozieClientException
	 * @see org.apache.oozie.client.OozieClient#run(java.util.Properties)
	 */
	public String run(Properties conf) throws OozieClientException {
		return oc.run(conf);
	}

	/**
	 * @param conf
	 * @return
	 * @throws OozieClientException
	 * @see org.apache.oozie.client.OozieClient#submit(java.util.Properties)
	 */
	public String submit(Properties conf) throws OozieClientException {
		return oc.submit(conf);
	}

	/**
	 * @param jobId
	 * @throws OozieClientException
	 * @see org.apache.oozie.client.OozieClient#suspend(java.lang.String)
	 */
	public void suspend(String jobId) throws OozieClientException {
		oc.suspend(jobId);
	}


	public String run(DataFlow df) throws Exception{
		return run(df,df.getElement());
	}

	public String run(DataFlow df, List<DataFlowElement> list) throws Exception{
		String jobId = null;
		String error = null;
		File parentDir = new File(WorkflowPrefManager.pathOozieJob.get()+"/"+df.getName());
		String hdfsWfPath = WorkflowPrefManager.hdfsPathOozieJobs.get()+"/"+df.getName();
		if(!parentDir.exists()){
			parentDir.mkdirs();
		}else{
			try {
				LocalFileSystem.delete(parentDir);
				parentDir.mkdir();
			} catch (IOException e) {
				error = e.getMessage();
			}

		}

		if(error == null){
			//Creating xml

			OozieXmlCreator xmlCreator = null;
			xmlCreator = new OozieXmlForkJoinPaired();
			error = xmlCreator.createXml(df, list, parentDir);

			if(error == null){
				try{
					writeWorkflowProp(new File(parentDir,"job.properties"),
							hdfsWfPath);
				}catch(Exception e){
					error = "Fail to generate the properties "+
							e.getMessage();
				}
			}

		}

		if(error == null){
			logger.debug("copy from "+
					parentDir.getAbsolutePath()+" to "+hdfsWfPath);
			try{
				FileSystem fs = NameNodeVar.getFS();
				Path wCur = new Path(hdfsWfPath);
				if(fs.exists(wCur)){
					fs.delete(wCur,true);
				}
				fs.copyFromLocalFile(
						false,
						true,
						new Path(parentDir.getAbsolutePath()), 
						wCur);
			}catch(Exception e){
				error = "Fail to copy dependencies "+e.getMessage();
			}
		}


		if(error == null){
			// create a workflow job configuration and set the workflow application path
			String wfPath = WorkflowPrefManager.getSysProperty(
					WorkflowPrefManager.sys_namenode)+
					hdfsWfPath;
			logger.debug("Workflow path: "+wfPath);
			Properties conf = addProperties(oc.createConfiguration(),
					defaultMap(hdfsWfPath));


			try {
				jobId = oc.run(conf);
				logger.debug("Workflow job submitted succesfully");
			} catch (OozieClientException e) {
				error = "Fail to launch the job with oozie "+e.getMessage();
			}

		}
		if(error != null){
			logger.debug(error);
			throw new Exception(error);
		}

		return jobId;
	}
	
	public String getConsoleUrl(DataFlow df) throws RemoteException, Exception{
		String console = null;
		String jobId = df.getOozieJobId();
		if(jobId != null){
			console = oc.getJobInfo(jobId).getConsoleUrl();
		}
		return console;
	}
	
	public String getConsoleElementUrl(DataFlow df,DataFlowElement e) throws RemoteException, Exception{
		String found = null;
		String jobId = df.getOozieJobId();
		if(jobId != null){
			Iterator<WorkflowAction> it = oc.getJobInfo(jobId).getActions().iterator();
			OozieXmlCreator xmlC = new OozieXmlForkJoinPaired(); 
			while(it.hasNext() && found == null){
				WorkflowAction cur = it.next();
				if(xmlC.getNameAction(e).equals(cur.getName())){
					found = cur.getConsoleUrl();
				}
			}
		}
		return found;
	}

	/**
	 * @return the oc
	 */
	public final OozieClient getOc() {
		return oc;
	}
	
	
	protected Map<String,String> defaultMap(String hdfsWfPath){
		Map<String,String> properties = new HashMap<String,String>(5);
		Properties propSys = WorkflowPrefManager.getSysProperties();
		properties.put(prop_jobtracker, 
				propSys.getProperty(WorkflowPrefManager.sys_jobtracker));
		properties.put(prop_namenode, 
				propSys.getProperty(WorkflowPrefManager.sys_namenode));
		properties.put(prop_queue, 
				propSys.getProperty(WorkflowPrefManager.sys_oozie_queue));
//		properties.put(prop_libpath, 
//				propSys.getProperty(WorkflowPrefManager.sys_idiroEngine_path));
		properties.put(OozieClient.APP_PATH,
				propSys.getProperty(WorkflowPrefManager.sys_namenode)+
				hdfsWfPath);
		properties.put("oozie.use.system.libpath","true");
		
		return properties;
	}

	protected void writeWorkflowProp(
			File workflowPropWriter,
			String hdfsWfPath) throws Exception{
		Map<String,String> properties = defaultMap(hdfsWfPath);

		BufferedWriter bf = new BufferedWriter( 
				new FileWriter(workflowPropWriter));
		bf.write("-Duser.name="+System.getProperty("user.name")+"\n");
		Iterator<String> itKey = properties.keySet().iterator();
		while(itKey.hasNext()){
			String key = itKey.next();
			bf.write(key+"="+properties.get(key)+"\n");
		}
		bf.close();
	}
	
	protected Properties addProperties(Properties prop,Map<String,String> map){
		Iterator<String> itKey = map.keySet().iterator();
		while(itKey.hasNext()){
			String key = itKey.next();
			prop.setProperty(key, map.get(key));
		}
		return prop;
	}
	
	/**
	 * 
	 * @return Returns an array containing the jobs
	 */
	public List<String[]> getJobs() {
		List<String[]> listGrid = new ArrayList<String[]>();
		String str = oc.getOozieUrl()+"v1/jobs";
	    try {
	        URL url = new URL(str);
	        URLConnection urlc = url.openConnection();
	        BufferedReader bfr = new BufferedReader(new InputStreamReader(urlc.getInputStream()));

	        String line;
	        final StringBuilder builder = new StringBuilder(2048);

	        while ((line = bfr.readLine()) != null) {
	            builder.append(line);
	        }
	        // convert response to JSON array
	        final JSONObject jso = new JSONObject(builder.toString());
	        JSONArray jsa = jso.getJSONArray("workflows");
	        // extract out data of interest
	        for (int i = 0; i < jsa.length(); i++) {
	            final JSONObject jo = (JSONObject) jsa.get(i);
	            String[] result = new String[5];
	            result[0] = jo.getString("id");
	            result[1] = jo.getString("user"); 
	            result[2] = jo.getString("appName"); 
	            result[3] = jo.getString("status"); 
	            result[4] = jo.getString("createdTime"); 
	            listGrid.add(result);
	        }
	        
	    } catch (Exception e) {
	    	logger.error(e.getMessage());
	    }
	    return listGrid;
	}
	
	/**
	 * 
	 * @return Returns a String with the Oozie URL
	 */
	public String getUrl() throws RemoteException{
		return oc.getOozieUrl();
	}
}
