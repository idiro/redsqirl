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

package com.redsqirl.workflow.server.connect;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.idiro.utils.LocalFileSystem;
import com.idiro.utils.RandomString;
import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.WorkflowCoordinator;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.action.SyncSink;
import com.redsqirl.workflow.server.action.SyncSource;
import com.redsqirl.workflow.server.action.SyncSourceFilter;
import com.redsqirl.workflow.server.action.superaction.SubWorkflow;
import com.redsqirl.workflow.server.action.superaction.SubWorkflowInput;
import com.redsqirl.workflow.server.action.superaction.SubWorkflowOutput;
import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;
import com.redsqirl.workflow.server.connect.interfaces.DataStore;
import com.redsqirl.workflow.server.enumeration.PathType;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.DataFlowCoordinator;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Interface to store/add/retrieve work flow through a map system.
 * 
 * @author etienne
 *
 */
public class WorkflowInterface extends UnicastRemoteObject implements DataFlowInterface{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5892963334534803842L;
	/**
	 * Instance of WorkflowInterface
	 */
	private static WorkflowInterface instance;
	/**
	 * Is Interface Initialized
	 */
	private static boolean init = false;
	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(WorkflowInterface.class);
	/**
	 * Map of workflows
	 */
	private Map<String,DataFlow> wf = new LinkedHashMap<String,DataFlow>();

	/**
	 * Map of workflow clones
	 */
	private List<String> wfClones = new LinkedList<String>();


	/**
	 * Map of datastores
	 */
	private Map<String,DataStore> datastores;
	
	private Map<String,Set<String>> typesPerDataStore;

	protected Map<String, String> mapCanvasToOpen;
	
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	private WorkflowInterface() throws RemoteException{
		super();
		datastores = WorkflowInterface.getAllClassDataStore();
		typesPerDataStore = WorkflowInterface.getAllTypesPerDataStore();
	}
	
	private static List<String> getDataOutputClassName(){
		File outputClassFile = new File(WorkflowPrefManager.getPathOutputClasses());
		List<String> dataoutputClassName = new LinkedList<String>();
		if(outputClassFile.exists()){
			try{
				BufferedReader br = new BufferedReader(new FileReader(outputClassFile));
				String line = null;
				while((line = br.readLine()) != null){
					dataoutputClassName.add(line);
				}
				br.close();
			}catch(Exception e){
				logger.error("Error while reading class file",e);
				outputClassFile.delete();
			}
		}

		if(!outputClassFile.exists()){
			dataoutputClassName = DataOutput.getAllClassDataOutput();
			try{
				BufferedWriter bw = new BufferedWriter(new FileWriter(outputClassFile));
				Iterator<String> dataoutputClassNameIt = dataoutputClassName.iterator();
				while(dataoutputClassNameIt.hasNext()){
					bw.write(dataoutputClassNameIt.next());
					bw.newLine();
				}
				bw.close();
				//Everyone can remove this file
				outputClassFile.setReadable(true, false);
				outputClassFile.setWritable(true, false);
			}catch(Exception e){
				logger.error("Error while writing class file",e);
				outputClassFile.delete();
			}

		}
		return dataoutputClassName;
	}

	private static Map<String,Set<String>> getAllTypesPerDataStore(){
		 Map<String,Set<String>> ans = new LinkedHashMap<String,Set<String>>();
		 List<String> dataoutputClassName = getDataOutputClassName();

		 Iterator<String> dataoutputClassNameIt = dataoutputClassName.iterator();
		 while(dataoutputClassNameIt.hasNext()){
			 String className = dataoutputClassNameIt.next();
			 try {
				 DataOutput outNew = (DataOutput) Class.forName(className).newInstance();
				 String browserName = outNew.getBrowserName();
				 String typeName = outNew.getTypeName(); 
				 if(!ans.containsKey(browserName)){
					 ans.put(browserName, new LinkedHashSet<String>());
				 }
				 ans.get(browserName).add(typeName);
			 } catch (Exception e) {
				 logger.error(e,e);
			 }
		 }
		 return ans;
	}
	
	/**
	 * Get a List of output classes for data to be held in
	 * 
	 * @return List output classes
	 */
	private static Map<String,DataStore> getAllClassDataStore() {

		//Get the browser name used in DataOutput
		logger.debug("Get the output class...");
		Set<String> browsersFromDataOut = new HashSet<String>();
		Set<String> browserClasses = new HashSet<String>();
		List<String> dataoutputClassName = getDataOutputClassName();

		Iterator<String> dataoutputClassNameIt = dataoutputClassName.iterator();
		while(dataoutputClassNameIt.hasNext()){
			String className = dataoutputClassNameIt.next();
			try {
				DataOutput outNew = (DataOutput) Class.forName(className).newInstance();
				logger.debug(outNew.getTypeName());
				browsersFromDataOut.add(outNew.getBrowserName());
				browserClasses.add(outNew.getBrowser().getClass().getCanonicalName());
			} catch (Exception e) {
				logger.error(e,e);
			}
		}

		//Return a map containing only the one used in DataOutput
		logger.debug("Get the store class...");
		Map<String,DataStore> ans = new LinkedHashMap<String,DataStore>();
		Iterator<String> datastoreClassName = browserClasses.iterator();
		while (datastoreClassName.hasNext()) {
			String className = datastoreClassName.next();
			try {
				DataStore outNew = (DataStore) Class.forName(className).newInstance();
				logger.debug(outNew.getBrowserName());
				if(browsersFromDataOut.contains(outNew.getBrowserName())){
					ans.put(outNew.getBrowserName(),outNew);
				}
			} catch (Exception e) {
			}

		}
		return ans;
	}

	public Set<String> getBrowsersName(){
		return new HashSet<String>(datastores.keySet());
	}

	public DataStore getBrowser(String browserName){
		return datastores.get(browserName);
	}

	/**
	 * Add a Workflow to the list
	 * @param name
	 * @return Error Message
	 */
	public String addWorkflow(String name){
		String error = null;
		if(!wf.containsKey(name)){
			try {
				wf.put(name, new Workflow());
			} catch (Exception e) {
				error = e.getMessage();
			}
		}else{
			error=LanguageManagerWF.getText("workflowinterface.addWorkflow_workflowexists",new Object[]{name});
		}
		if(error != null){
			logger.error(error);
		}
		return error;
	}
	/**
	 * Rename a workflow
	 * @param oldName
	 * @param newName
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String renameWorkflow(String oldName, String newName)
			throws RemoteException {
		String error = null;
		if(!wf.containsKey(oldName)){
			error = "Workflow "+oldName+" does not exists";
		}else{
			if(!oldName.equals(newName)){
				if(wf.containsKey(newName)){
					error = "Workflow "+newName+" already exists";
				}else{
					DataFlow cur = wf.get(oldName);
					cur.setName(newName);
					wf.remove(oldName);
					wf.put(newName, cur);
				}
			}
		}
		return error;
	}

	/**
	 * Clone a data flow
	 * @param wfName The data flow to clone
	 * @return The id of the clone
	 */
	@Override
	public String cloneDataFlow(String wfName){
		String cloneId = generateNewCloneId();
		if(wf.containsKey(wfName)){
			try {
				if(((Workflow) wf.get(wfName)).cloneToFile(cloneId)){
					wfClones.add(cloneId);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
		return cloneId;
	}

	@Override
	public void eraseClone(String cloneId){
		wfClones.remove(cloneId);
	}

	protected String generateNewCloneId(){
		boolean found = false;
		String newId = null;
		int length = 10;

		while (newId == null) {
			newId = RandomString.getRandomNameStartByLetter(length);
			Iterator<String> itA = wfClones.iterator();
			found = false;
			while (itA.hasNext() && !found) {
				found = itA.next().equals(newId);
			}
			if (found) {
				newId = null;
			}

		}
		return newId;
	}

	/**
	 * Copy a subset of a workflow into another.
	 * @param cloneId The id of the workflow to copy from 
	 * @param elements The element ids to copy
	 * @param wfName The id of the workflow to copy to
	 */
	public String copy(String cloneId, List<String> elements, String wfName){

		String error = null;

		if(wfClones.contains(cloneId) && 
				elements != null && 
				!elements.isEmpty() && 
				wf.containsKey(wfName)){
			try {
				Workflow from = (Workflow) readCloneFile(cloneId);
				DataFlow to  = wf.get(wfName);
				Workflow cloneFrom = (Workflow) from.clone();

				//Check SubWorkflow
				boolean check = true;
				if(!(to instanceof SubDataFlow)){
					Iterator<DataFlowElement> cloneElIt = cloneFrom.getElement().iterator();
					while(cloneElIt.hasNext() && check){
						DataFlowElement curEl = cloneElIt.next();
						if(elements.contains(curEl.getComponentId()) &&
								( curEl instanceof SubWorkflowInput ||
										curEl instanceof SubWorkflowOutput
										)){
							check = false;
							error = LanguageManagerWF.getText("copy_subDataFlow_to_workflow");
						}
					}
				}else{
					Iterator<DataFlowElement> cloneElIt = cloneFrom.getElement().iterator();
					while(cloneElIt.hasNext() && check){
						DataFlowElement curEl = cloneElIt.next();
						if(elements.contains(curEl.getComponentId()) &&
								( curEl instanceof SyncSourceFilter ||
										curEl instanceof SyncSource ||
										curEl instanceof SyncSink
										)){
							check = false;
							error = LanguageManagerWF.getText("copy_workflow_to_subDataFlow");
						}else{
							Iterator<DFEOutput> itOut = curEl.getDFEOutput().values().iterator();
							while(itOut.hasNext()){
								DFEOutput curOut = itOut.next();
								if(PathType.TEMPLATE.equals(curOut.getPathType())||
										PathType.MATERIALIZED.equals(curOut.getPathType())){
									check = false;
									error = LanguageManagerWF.getText("copy_workflow_to_subDataFlow");
								}
							}
						}
					}
				}

				if(check){

					//Copy
					Iterator<DataFlowElement> cloneElIt = cloneFrom.getElement().iterator();
					List<String> toDelete = new LinkedList<String>();
					while(cloneElIt.hasNext()){
						DataFlowElement curEl = cloneElIt.next();
						if(!elements.contains(curEl.getComponentId())){
							toDelete.add(curEl.getComponentId());
						}else{
							String newName = null;
							while(newName == null){
								newName = to.generateNewId();
								logger.debug("new name: "+newName+" in "+ to.getComponentIds()+ " for "+cloneFrom.getComponentIds());
								if(cloneFrom.getElement(newName) != null){
									newName = null;
								}
							}
							String oldName = curEl.getComponentId();
							cloneFrom.changeElementId(oldName,newName);
							
							cloneFrom.replaceInAllElements(cloneFrom.getComponentIds(),
									"([_ \\W]|^)("+Pattern.quote(oldName)+")([_ \\W]|$)", "$1"+newName+"$3",true);
							curEl.setPosition(curEl.getX()+75, curEl.getY()+75);
						}
					}
					Iterator<String> itDel = toDelete.iterator();
					while(itDel.hasNext()){
						cloneFrom.removeElement(itDel.next());
					}
					cloneFrom.regeneratePaths(null);
					Map<String,String> coordinators = new HashMap<String,String>();
					Iterator<DataFlowElement> copyElIt = cloneFrom.getElement().iterator();
					while(copyElIt.hasNext()){
						DataFlowElement cur = copyElIt.next();
						if(coordinators.containsKey(cur.getCoordinatorName())){
							to.addElement(cur,coordinators.get(cur.getCoordinatorName()));
						}else{
							coordinators.put(cur.getCoordinatorName(), cur.getComponentId());
							to.addElement(cur,cur.getComponentId());
						}
					}

				}

			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}

		return error;
	}
	/**
	 * Read the clone file
	 * @param cloneId
	 * @return Bytes version of Workflow
	 */
	public Object readCloneFile(String cloneId){
		Object result = null;
		try {
			// Check if T is instance of Serializeble other throw
			// CloneNotSupportedException
			String path = WorkflowPrefManager.getPathClonefolder()+"/"
					+ cloneId;

			File file = new File(path);
			FileInputStream fin = null;
			// create FileInputStream object
			fin = new FileInputStream(file);

			byte fileContent[] = new byte[(int) file.length()];

			// Reads up to certain bytes of data from this input stream into
			// an array of bytes.
			fin.read(fileContent);

			ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(fileContent));
			// Deserialize it
			result = ois.readObject();
			fin.close();
			ois.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			result = null;
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
			result = null;
		}
		return result;
	}

	public void removeClone(String cloneId){
		String path = WorkflowPrefManager.getPathClonefolder()+"/"+cloneId;
		new File(path).delete();
		wfClones.remove(cloneId);
	}

	/**
	 * Remove a Workflow
	 * @param name
	 * 
	 */
	public void removeWorkflow(String name){
		wf.remove(name);
	}

	/**
	 * Get a Workflow by Name
	 * @param name
	 * @return Workflow
	 */
	public DataFlow getWorkflow(String name){
		return wf.get(name);
	}
	
	public void setWorkflowPath(String name, String path){
		try {
			wf.get(name).setPath(path);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}



	@Override
	public String addSubWorkflow(String name) throws RemoteException {
		String error = null;
		if(!wf.containsKey(name)){
			try {
				wf.put(name, new SubWorkflow());
			} catch (Exception e) {
				error = e.getMessage();
			}
		}else{
			error=LanguageManagerWF.getText("workflowinterface.addWorkflow_workflowexists",new Object[]{name});
		}
		if(error != null){
			logger.error(error);
		}
		return error;
	}


	@Override
	public SubDataFlow getSubWorkflow(String name)
			throws RemoteException {
		return (SubDataFlow) wf.get(name);
	}
	
	@Override
	public DataFlow getNewWorkflow() throws RemoteException{
		return new Workflow();
	}
	
	@Override
	public SubDataFlow getNewSubWorkflow() throws RemoteException{
		return new SubWorkflow();
	}

	/**
	 * Get an Instance of the interface
	 * @return instance
	 */
	public static WorkflowInterface getInstance() {
		if(!init){
			try {
				instance = new WorkflowInterface();
				init = true;
			} catch (RemoteException e) {
				logger.error("RemoteException");
				logger.error(e.getMessage());
			}

		}
		return instance;
	}
	/**
	 * Backup all workflows that are open
	 */
	public void backupAll() throws RemoteException{
		logger.debug ("backupAllWorkflowsToOpen ");

		Map<String, String> mapCanvasToOpen = new LinkedHashMap<String, String>();

		Iterator<String> itWorkflow = wf.keySet().iterator();
		while(itWorkflow.hasNext()){
			String workflowNameCur = itWorkflow.next();
			logger.debug("backup "+workflowNameCur);
			try {
				if(!wf.get(workflowNameCur).getElement().isEmpty()){
					wf.get(workflowNameCur).setName(workflowNameCur);
					wf.get(workflowNameCur).setPath(wf.get(workflowNameCur).backup());

					if(wf.get(workflowNameCur).getPath() != null){
						
						logger.debug("backup path check " + wf.get(workflowNameCur).getPath());
						
						mapCanvasToOpen.put(workflowNameCur, wf.get(workflowNameCur).getPath());
					}else{
						String path = wf.get(workflowNameCur).backupAllWorkflowsBeforeClose();
						mapCanvasToOpen.put(workflowNameCur, path);
					}
				}
			} catch (Exception e) {
				logger.warn("Error backing up workflow "+workflowNameCur);
			}
		}

		saveMapCanvasToOpen(mapCanvasToOpen);
	}

	protected String saveMapCanvasToOpen(Map<String, String> mapCanvasToOpen) throws RemoteException {
		logger.debug ("saveMapCanvasToOpen ");
		
		String error = null;
		
		File path = new File(WorkflowPrefManager.getPathuserpref()+"/.loadCanvas.txt");
		logger.debug ("saveMapCanvasToOpen path " + path);
		
		try {
			
			FileWriter fw = new FileWriter(path);
			
			BufferedWriter bw = new BufferedWriter(fw);
			for (Entry<String,String> e : mapCanvasToOpen.entrySet()) {
				bw.write(e.getKey() + "," + e.getValue()+"\n");
			}
            bw.close();
            fw.close();

		} catch (Exception e) {
			error = LanguageManagerWF.getText("workflow.saveMapCanvasToOpen");
		}

		return error;
	}

	public List<String[]> getLastBackedUp() throws RemoteException {
		
		List<String[]> result = new LinkedList<String[]>();

		File path = new File(WorkflowPrefManager.getPathuserpref()+"/"+".loadCanvas.txt");
		if(path.exists()){
			try {

				BufferedReader br = new BufferedReader(new FileReader(path));
				String line;
				while ((line = br.readLine()) != null) {
					String[] ans = line.split(",");
					result.add(ans);
				}
				br.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * Close all workflows
	 * @throws RemoteExeption
	 */
	public void autoCleanAll() throws RemoteException{
		Iterator<String> itWorkflow = wf.keySet().iterator();
		while(itWorkflow.hasNext()){
			String workflowNameCur = itWorkflow.next();
			wf.get(workflowNameCur).close();
		}
	}
	/**
	 * Shutdown the server
	 */
	public void shutdown() throws RemoteException{
        try {
            File[] clones = new File(WorkflowPrefManager.getPathClonefolder()).listFiles();
            for(File clone:clones){
                try{
                    LocalFileSystem.delete(clone);
                }catch(Exception e){
                    logger.warn("Fail to clean clone temporary files.");
                }
            }
        } catch (Exception e) {
            logger.error("Fail to clean temporary files.",e);
        }
        try{
            Iterator<DataStore> it = datastores.values().iterator();
            while(it.hasNext()){
                try{
                    it.next().close();
                }catch(Exception e){
                    logger.warn("Fail to close a datastore: "+e,e);
                }
            }
        } catch (Exception e) {
            logger.error("Fail to close datastores.",e);
        }
        ServerMain.shutdown();
    }

	/**
	 * @return the datastores
	 */
	private Map<String,DataStore> getDatastores() {
		return datastores;
	}

	public void replaceWFByClone(String id, String wfName,boolean keepClone) throws RemoteException{

		if(wfClones.contains(id) && wf.containsKey(wfName)){
			wf.remove(wfName);
			wf.put(wfName,(Workflow)readCloneFile(id));
			if(!keepClone){
				removeClone(id);
			}
		}

	}

	public void copy(String id, String wfName) throws RemoteException{

		if(wfClones.contains(id) && wf.containsKey(wfName)){
			wf.remove(wfName);
			removeClone(id);
			wf.put(wfName,(Workflow)readCloneFile(id));
		}

	}
	
	public void removeAllTmpInType(String type) throws RemoteException{
		removeAllTmp(null,type);
	}
	
	public void removeAllTmpInBrowser(String browserName) throws RemoteException{
		removeAllTmp(browserName,null);
	}
	
	public void removeAllTmp() throws RemoteException{
		removeAllTmp(null,null);
	}
	
	protected void removeAllTmp(String browserName, String typeName) throws RemoteException{
		List<String> dataoutputClassName = getDataOutputClassName();
		Iterator<String> dataoutputClassNameIt = dataoutputClassName.iterator();
		while(dataoutputClassNameIt.hasNext()){
			String className = dataoutputClassNameIt.next();
			try {
				DataOutput outNew = (DataOutput) Class.forName(className).newInstance();
				logger.debug(outNew.getTypeName());
				if( (browserName == null || browserName.equals(outNew.getBrowserName())) &&
					(typeName == null || typeName.equals(outNew.getTypeName())) ){
					outNew.removeAllDataUnderGeneratePath();
				}
			} catch (Exception e) {
				logger.error(e,e);
			}
		}
		removeCacheFromType(browserName,typeName);
	}
	
	protected void removeCacheFromType(String browserName,String typeName) throws RemoteException{
		Iterator<DataFlow> it = wf.values().iterator();
		while(it.hasNext()){
			DataFlow curWf = it.next();
			Iterator<DataFlowElement> itDfe = curWf.getElement().iterator();
			while(itDfe.hasNext()){
				Iterator<DFEOutput> itOut = itDfe.next().getDFEOutput().values().iterator();
				while(itOut.hasNext()){
					DFEOutput curOut = itOut.next();
					if(!SavingState.RECORDED.equals(curOut.getSavingState()) &&
							(browserName == null || browserName.equals(curOut.getBrowserName())) &&
							(typeName == null || typeName.equals(curOut.getTypeName())) ){
						curOut.clearCache();
					}
				}
			}
		}
	}

	public Map<String,Set<String>> getTypesPerDataStore() {
		return this.typesPerDataStore;
	}

}