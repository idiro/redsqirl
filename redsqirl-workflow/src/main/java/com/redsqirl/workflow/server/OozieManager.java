/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl.workflow.server;



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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.log4j.Logger;
import org.apache.oozie.client.AuthOozieClient;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;
import org.apache.oozie.client.WorkflowAction;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.idiro.hadoop.NameNodeVar;
import com.idiro.utils.LocalFileSystem;
import com.idiro.utils.RandomString;
import com.redsqirl.workflow.server.connect.WorkflowInterface;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.server.interfaces.JobManager;
import com.redsqirl.workflow.server.interfaces.OozieXmlCreator;
import com.redsqirl.workflow.server.interfaces.RunnableElement;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Oozie Interface with Red Sqirl.
 * 
 * @author etienne
 * 
 */
public class OozieManager extends UnicastRemoteObject implements JobManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2909899568049533678L;

	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(OozieManager.class);

	/**
	 * Instance of Oozie Manager
	 */
	private static OozieManager instance = null;
	/** Oozie Client */
	private OozieClient oc = null;
	/** Namenode property key */
	public static final String prop_namenode = "namenode",
			/** JobTracker link property key */
			prop_jobtracker = "jobtracker",
			/** Queue for namenode property key */
			prop_launcher_queue = "default_launcher_queue",
			/** Default running job queue */
			prop_action_queue = "default_action_queue",
			/** User Name property key */
			prop_user = "user_name",
			/** Library Path for Oozie property key */
			prop_libpath = "oozie.libpath",
			/** Main Workflow path property key */
			prop_workflowpath = "pathWorkflow";

	public static final String oozie_mode_default = "default";

	/**
	 * Constructor.
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * 
	 */
	private OozieManager() throws FileNotFoundException, IOException {
		super();
		String secEnable = WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_enable_security);
		if(secEnable != null && secEnable.equalsIgnoreCase("true")){
			oc = new AuthOozieClient(WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_oozie));
		}else{
			oc = new OozieClient(WorkflowPrefManager.getProperty(WorkflowPrefManager.sys_oozie));
		}
	}

	/**
	 * Get an Instance of OozieManager
	 * 
	 * @return Returns the single allowed instance of ProcessRunner
	 */
	public static OozieManager getInstance() {
		if (instance == null) {
			try {
				instance = new OozieManager();
			} catch (FileNotFoundException e) {
				logger.error("No configuration file found to initialise oozie");
			} catch (IOException e) {
				logger.error("IOException when attempting to read oozie properties");
			}
		}
		return instance;
	}

	/**
	 * Create Configuration for Oozie Client
	 * 
	 * @return Protperties for Oozie Client
	 * @see org.apache.oozie.client.OozieClient#createConfiguration()
	 */
	public Properties createConfiguration() {
		return oc.createConfiguration();
	}

	/**
	 * Kill a job that is in not Terminated in Oozie
	 * 
	 * @param jobId
	 * @throws Exception
	 * @see org.apache.oozie.client.OozieClient#kill(java.lang.String)
	 */
	public void kill(String jobId) throws Exception {
		try{
			oc.kill(jobId);
		}catch(OozieClientException e ){
			logger.warn(e,e);
			throw new Exception(e);
		}
	}

	/**
	 * Resume a suspended job that is in Oozie
	 * 
	 * @param jobId
	 * @throws Exception
	 * @see org.apache.oozie.client.OozieClient#resume(java.lang.String)
	 */
	public void resume(String jobId) throws Exception {
		try{
			oc.resume(jobId);
		}catch(OozieClientException e ){
			logger.warn(e,e);
			throw new Exception(e);
		}
	}

	/**
	 * Run a Job
	 * 
	 * @param conf
	 *            properties for the job
	 * @return ID of the job that is running
	 * @throws Exception
	 * @see org.apache.oozie.client.OozieClient#run(java.util.Properties)
	 */
	public String run(Properties conf) throws Exception {
		try{
			return oc.run(conf);
		}catch(OozieClientException e ){
			logger.warn(e,e);
			throw new Exception(e);
		}
	}

	/**
	 * Submit a job to Oozie
	 * 
	 * @param conf
	 *            properties of the Job to be submitted
	 * @return Id of the Job
	 * @throws Exception
	 * @see org.apache.oozie.client.OozieClient#submit(java.util.Properties)
	 */
	public String submit(Properties conf) throws Exception {
		try{
			return oc.submit(conf);
		}catch(OozieClientException e ){
			logger.warn(e,e);
			throw new Exception(e);
		}
	}

	/**
	 * Suspend a Job that is Running in Oozie
	 * 
	 * @param jobId
	 *            of job to suspend
	 * @throws Exception
	 * @see org.apache.oozie.client.OozieClient#suspend(java.lang.String)
	 */
	public void suspend(String jobId) throws Exception {
		try{
			oc.suspend(jobId);
		}catch(OozieClientException e ){
			logger.warn(e,e);
			throw new Exception(e);
		}
	}

	/**
	 * Clean the directory where the Job details are stored
	 * 
	 * @param nameWf
	 * @throws RemoteException
	 */
	public void cleanJobDirectory(final String nameWf) throws RemoteException {
		Path hdfsWfPath = new Path(WorkflowPrefManager.getHDFSPathJobs());
		FileSystem fs = null;
		int numberToKeep = WorkflowPrefManager.getNbOozieDirToKeep();
		try {
			fs = NameNodeVar.getFS();
			FileStatus[] children = fs.listStatus(hdfsWfPath,
					new PathFilter() {

				@Override
				public boolean accept(Path arg0) {
					return arg0.getName().startsWith(nameWf + "_");
				}
			});
			Arrays.sort(children, 0, children.length,
					new Comparator<FileStatus>() {

				@Override
				public int compare(FileStatus arg0, FileStatus arg1) {
					return (int) ((arg0.getModificationTime() - arg1
							.getModificationTime()) / 10000);
				}
			});
			for (int i = 0; i < children.length - numberToKeep; ++i) {
				fs.delete(children[i].getPath(), true);
			}
		} catch (Exception e1) {
			logger.error(e1);
		}
	}

	/**
	 * Get a name for a directory to store all the jobs files and configuration
	 * 
	 * @param df
	 * @return The name for a directory to store all the jobs files and configuration
	 * @throws RemoteException
	 */
	protected String buildFileName(DataFlow df) throws RemoteException {
		final String nameWf = df.getName();
		if(nameWf == null){
			logger.warn("The workflow to run has no name");
			df.setName(RandomString.getRandomName(8));
		}
		String ans = null;
		Path hdfsWfPath = new Path(WorkflowPrefManager.getHDFSPathJobs());
		FileSystem fs = null;
		int number = -1;
		try {
			fs = NameNodeVar.getFS();
			FileStatus[] children = fs.listStatus(hdfsWfPath,
					new PathFilter() {

				@Override
				public boolean accept(Path arg0) {
					if (arg0.getName().startsWith(nameWf)) {
						try {
							@SuppressWarnings("unused")
							int i = Integer.valueOf(arg0.getName()
									.substring(nameWf.length() + 1));
							return true;
						} catch (Exception e) {
						}
					}
					return false;
				}
			});
			
			if(children != null && children.length > 0){
				for (FileStatus child : children) {
					number = Math.max(
							number,
							Integer.valueOf(child.getPath().getName()
									.substring(nameWf.length() + 1)));
				}
			}
		} catch (Exception e) {
			logger.error(e,e);
		}
		ans = nameWf + "_" + (number + 1);

		return ans;
	}

	/**
	 * Run a job with A list of actions and a DataFlow
	 * 
	 * @param df
	 *            DataFlow to be run with
	 * @param list
	 *            of actions to run
	 * @return ID of the Job
	 * @throws Exception
	 */
	public String run(DataFlow df, List<RunnableElement> list) throws Exception {

		logger.debug("run");

		String jobId = null;
		String error = null;
		final String nameWF = df.getName();
		String fileName = buildFileName(df);
		File parentDir = new File(WorkflowPrefManager.getPathooziejob() + "/"
				+ fileName);
		String hdfsWfPath = WorkflowPrefManager.getHDFSPathJobs() + "/"
				+ fileName;
		if (!parentDir.exists()) {
			parentDir.mkdirs();
		} else {
			try {
				LocalFileSystem.delete(parentDir);
				parentDir.mkdir();
			} catch (IOException e) {
				error = e.getMessage();
			}

		}

		if (error == null) {
			// Creating xml

			OozieXmlCreator xmlCreator = null;
			xmlCreator = new OozieXmlForkJoinPaired();

			logger.debug("run df " + df);
			logger.debug("run list " + list);
			logger.debug("run parentDir " + parentDir);

			error = xmlCreator.createXml(df, list, parentDir);

			if (error == null) {
				try {
					writeWorkflowProp(new File(parentDir, "job.properties"),
							hdfsWfPath);
				} catch (Exception e) {
					error = LanguageManagerWF.getText(
							"ooziemanager.createproperties",
							new Object[] { e.getMessage() });
				}
			}

		}

		if (error == null) {
			logger.debug("copy from " + parentDir.getAbsolutePath() + " to "
					+ hdfsWfPath);
			FileSystem fs = null;
			try {
				fs = NameNodeVar.getFS();
				Path wCur = new Path(hdfsWfPath);
				fs.copyFromLocalFile(false, true,
							new Path(parentDir.getAbsolutePath()), wCur);
			} catch (Exception e) {
				logger.error(e,e);
				error = LanguageManagerWF.getText(
						"ooziemanager.copydependencies",
						new Object[] { e.getMessage() });
			}

			try {
				LocalFileSystem.delete(parentDir);
			} catch (Exception e1) {
				logger.error("Fail to remove local directory: " + e1);
			}
		}

		if (error == null) {
			// create a workflow job configuration and set the workflow application path
			String wfPath = WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_namenode) + hdfsWfPath;
			logger.debug("Workflow path: " + wfPath);
			Properties conf = addProperties(oc.createConfiguration(), defaultMap(hdfsWfPath));
			/* //TODO uncomment to run the workflow
			try {
				jobId = oc.run(conf);
				logger.debug("Workflow job submitted succesfully");
			} catch (OozieClientException e) {
				logger.error(e,e);
				error = LanguageManagerWF.getText("ooziemanager.launchjob",
						new Object[] { e.getMessage() });
			}*/
		}

		if (error != null) {
			logger.debug(error);
			throw new Exception(error);
		}

		
		new Thread() {
			public void run() {
				try {
					cleanJobDirectory(nameWF);
				} catch (Exception e) {
					logger.warn("Fail clean oozie directory for job " + nameWF);
				}
			}  
		}.start();

		return jobId;
	}

	/**
	 * Get the Console URL for Oozie
	 * 
	 * @return URL of the workflow
	 * @param df
	 *            to get URL of specific DataFlow
	 * @throws RemoteException
	 * @throws Exception
	 * 
	 */

	public String getConsoleUrl(DataFlow df) throws RemoteException, Exception {
		String console = null;
		String jobId = df.getOozieJobId();

		if (jobId != null) {
			console = oc.getJobInfo(jobId).getConsoleUrl();
		}
		return console;
	}

	public String getElementStatus(DataFlow df, DataFlowElement dfe)
			throws RemoteException, Exception {
		String status = null;
		String jobId = df.getOozieJobId();

		if (jobId != null) {
			
			Set<String> actionNames = dfe.getLastRunOozieElementNames();
			int found = 0;
			Iterator<String> it = actionNames.iterator();
			boolean curFound = true;
			while(it.hasNext() && curFound){
				String cur = it.next();
				curFound = false;
				for (WorkflowAction wfa : oc.getJobInfo(jobId).getActions()) {
					if (cur.equals(wfa.getName())) {
						status = wfa.getStatus().toString();
						curFound = true;
						++found;
						break;
					}
				}
			}
			if(found == 0){
				status = "UNKNOWN";
			}
		}
		return status;
	}

	public int getNbElement(DataFlow df)throws RemoteException, Exception {
		return df.getNbOozieRunningActions();
	}

	public List<String> getElementsRunning(DataFlow df)throws RemoteException, Exception {
		List<String> ans = new LinkedList<String>();
		String jobId = df.getOozieJobId();
		if (jobId != null) {
			Iterator<DataFlowElement> it = WorkflowInterface.getInstance().getWorkflow(df.getName()).getElement().iterator();
			List<WorkflowAction> lWA = oc.getJobInfo(jobId).getActions();
			while(it.hasNext()){
				DataFlowElement curEl = it.next();
				Iterator<String> itOozieAction = curEl.getLastRunOozieElementNames().iterator();
				boolean end = false;
				while(itOozieAction.hasNext() && !end){
					String curOozieAct = itOozieAction.next();
					for (WorkflowAction wfa : lWA){
						if(wfa.getName().equals(curOozieAct)){ 
							if(WorkflowAction.Status.RUNNING.equals(wfa.getStatus())){
								ans.add(curEl.getComponentId());
								end = true;
							}
							break;
						}
					}
				}
			}
		}
		return ans;
	}
	
	public boolean jobExists(DataFlow df){
		boolean ans = false;
		try{
			String jobId = df.getOozieJobId();
			if (jobId != null) {
				ans = oc.getJobInfo(jobId) != null;
			}
		}catch(Exception e){
			logger.error(e,e);
		}
		return ans;
	}

	public List<String> getElementsDone(DataFlow df)throws RemoteException, Exception {
		List<String> ans = new LinkedList<String>();
		String jobId = df.getOozieJobId();
		if (jobId != null) {
			Iterator<DataFlowElement> it = WorkflowInterface.getInstance().getWorkflow(df.getName()).getElement().iterator();
			List<WorkflowAction> lWA = oc.getJobInfo(jobId).getActions();
			while(it.hasNext()){
				DataFlowElement curEl = it.next();
				Set<String> lastrunOozieElementNames = curEl.getLastRunOozieElementNames();
				int nbOkEl = 0;
				boolean end = false;
				if(lastrunOozieElementNames != null){
					Iterator<String> itOozieAction = lastrunOozieElementNames.iterator();
					while(itOozieAction.hasNext() && !end){
						String curOozieAct = itOozieAction.next();
						for (WorkflowAction wfa : lWA){
							if(wfa.getName().equals(curOozieAct)){ 
								if(WorkflowAction.Status.OK.equals(wfa.getStatus())){
									++nbOkEl;
								}else{
									end = true;
								}
								break;
							}
						}
					}
				}
				if(!end && nbOkEl > 0
						&& nbOkEl == lastrunOozieElementNames.size()){
					ans.add(curEl.getComponentId());
				}
			}
		}
		return ans;
	}

	public String getConsoleElementUrl(DataFlow df, DataFlowElement e)
			throws RemoteException, Exception {
		String found = null;
		String jobId = df.getOozieJobId();
		if (jobId != null) {
			Iterator<WorkflowAction> it = oc.getJobInfo(jobId).getActions()
					.iterator();
			Set<String> actionEls = e.getLastRunOozieElementNames();
			while (it.hasNext() && found == null) {
				WorkflowAction cur = it.next();
				if(actionEls.contains(cur.getName())){
					if(actionEls.size() > 1){
						found = getConsoleUrl(df);
					}else{
						found = cur.getConsoleUrl();
					}
					 
				}
			}
		}
		return found;
	}

	/**
	 * Get the OozieClietn
	 * 
	 * @return the oc
	 */
	public final OozieClient getOc() {
		return oc;
	}

	/**
	 * Get Default properties
	 * 
	 * @param hdfsWfPath
	 * @return Map with properties for Job
	 */

	protected Map<String, String> defaultMap(String hdfsWfPath) {
		Map<String, String> properties = new HashMap<String, String>(5);
		Properties propSys = WorkflowPrefManager.getSysProperties();
		properties.put(prop_jobtracker,
				propSys.getProperty(WorkflowPrefManager.sys_jobtracker));
		properties.put(prop_namenode,
				propSys.getProperty(WorkflowPrefManager.sys_namenode));
		properties.put(prop_launcher_queue,
				propSys.getProperty(WorkflowPrefManager.sys_oozie_launcher_queue));
		properties.put(prop_action_queue,
				propSys.getProperty(WorkflowPrefManager.sys_oozie_action_queue));
		properties.put(prop_workflowpath,
				propSys.getProperty(WorkflowPrefManager.sys_namenode)
				+ hdfsWfPath);
		properties.put(prop_user,System.getProperty("user.name"));
		properties.put(OozieClient.APP_PATH,
				propSys.getProperty(WorkflowPrefManager.sys_namenode)
				+ hdfsWfPath);
		properties.put("oozie.use.system.libpath", "true");

		return properties;
	}

	/**
	 * Write properties for the workflow
	 * 
	 * @param workflowPropWriter
	 * @param hdfsWfPath
	 * @throws Exception
	 */
	protected void writeWorkflowProp(File workflowPropWriter, String hdfsWfPath)
			throws Exception {
		Map<String, String> properties = defaultMap(hdfsWfPath);

		BufferedWriter bf = new BufferedWriter(new FileWriter(
				workflowPropWriter));
		bf.write("-Duser.name=" + System.getProperty("user.name") + "\n");
		Iterator<String> itKey = properties.keySet().iterator();
		while (itKey.hasNext()) {
			String key = itKey.next();
			bf.write(key + "=" + properties.get(key) + "\n");
		}
		bf.close();
	}

	/**
	 * Add a property to the configuration
	 * 
	 * @param prop
	 * @param map
	 * @return Updated Properties
	 */
	protected Properties addProperties(Properties prop, Map<String, String> map) {
		Iterator<String> itKey = map.keySet().iterator();
		while (itKey.hasNext()) {
			String key = itKey.next();
			if(key != null && map.get(key) != null){
				prop.setProperty(key, map.get(key));
			}
		}
		return prop;
	}

	/**
	 * Get a List of Jobs that are in the Oozie Console
	 * 
	 * @return List of Jobs and status that are in the Console
	 */
	public List<String[]> getJobs() {
		List<String[]> listGrid = new ArrayList<String[]>();
		String str = oc.getOozieUrl() + "v1/jobs";
		try {
			URL url = new URL(str);
			URLConnection urlc = url.openConnection();
			BufferedReader bfr = new BufferedReader(new InputStreamReader(
					urlc.getInputStream()));

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
	public String getUrl() throws RemoteException {
		return oc.getOozieUrl();
	}

}
