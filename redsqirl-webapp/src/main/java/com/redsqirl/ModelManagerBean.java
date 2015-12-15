package com.redsqirl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;

import com.idiro.utils.LocalFileSystem;
import com.redsqirl.analyticsStore.RedSqirlModule;
import com.redsqirl.auth.UserInfoBean;
import com.redsqirl.dynamictable.SelectableTable;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;
import com.redsqirl.workflow.utils.ModelInstaller;
import com.redsqirl.workflow.utils.ModelInt;
import com.redsqirl.workflow.utils.RedSqirlModel;

public class ModelManagerBean extends BaseBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6845493504303529213L;

	private static  Logger logger = Logger.getLogger(ModelManagerBean.class);

	private String name = "";
	private String comment = null;
	private String version = null;

	private String currentSubworkflowName = "";

	private String privilege = "";
	private String pathHDFS = "";
	private boolean admin = false;
	private String exists ;
	private String copyMove;
	private RedSqirlModule model = null;
	private List<RedSqirlModule> systemModels;
	private List<RedSqirlModule> userModels;

	/**
	 * The list of rows of the grid file system
	 */
	private SelectableTable subWorkflowFromModel = new SelectableTable();
	private SelectableTable modelGrid = new SelectableTable();

	private String asSystem = "";

	// For selection uninstall
	private List<SelectItem> uninstallUserSa = new ArrayList<SelectItem>(),
			uninstallSysSa = new ArrayList<SelectItem>();
	private String modelScope;

	private ModelInstaller modelInstaller;

	private String userName;

	private ModelInt rsModel;

	private HashSet<String> subWFNamesToCopy;

	
	public ModelManagerBean(){
		try {
			modelInstaller = new ModelInstaller(getModelManager());
			userName = getUserInfoBean().getUserName();
		} catch (RemoteException e) {
			logger.error("Fail to create "+this.getClass()+": "+e,e);
		}
	}
	
	public void installCurrentSubWorkflow() throws RemoteException {

		logger.info("subWorkflow actual name  " + currentSubworkflowName);
		DataFlowInterface dfi = getworkFlowInterface();
		SubDataFlow swa = dfi.getSubWorkflow(currentSubworkflowName);
		boolean system = asSystem.equals("System");
		
		logger.info("privilege : '" + privilege + "'");
		Boolean privilegeVal = null;
		if (privilege.equals("edit")) {

		} else if (privilege.equals("run")) {
			privilegeVal = new Boolean(false);
		} else if (privilege.equals("license")) {
			privilegeVal = new Boolean(true);
		}
		logger.info(privilege + " + " + privilegeVal);
		
		String nameWithModel = currentSubworkflowName;
		if(!currentSubworkflowName.contains(">")){
			nameWithModel = ">default>"+currentSubworkflowName;
		}

		swa.setName(nameWithModel);
		
		String username = system ? null : getUserInfoBean().getUserName();
		String modelName = RedSqirlModel.getModelAndSW(nameWithModel)[0];
		String error = null;
		ModelInt modelCur = null;
		if(system){
			modelCur = getModelManager().getSysModel(modelName);
		}else{
			modelCur = getModelManager().getUserModel(username,modelName);
		}
		modelInstaller.uninstallSA(modelCur,swa.getName());
		
		error = modelInstaller.installSA(modelCur, swa, privilegeVal);

		if (error != null && !error.isEmpty()) {
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			logger.info(" " + error);
			usageRecordLog().addError("ERROR INSTALLSUBWORKFLOW", error);
		} else {
			MessageUseful.addInfoMessage("Install Success for " + swa.getName());
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnSuccess", "msnSuccess");
			usageRecordLog().addSuccess("INSTALLSUBWORKFLOW");
		}
	}
	
	public void checkExistenceCurrentSubWorkflow() throws RemoteException{
		DataFlowInterface dfi = getworkFlowInterface();
		SubDataFlow swa = dfi.getSubWorkflow(currentSubworkflowName);
		boolean system = asSystem.equals("System");
		String username = system ? null : getUserInfoBean().getUserName();
		logger.info(system);
		logger.info(username);
		logger.info(currentSubworkflowName);
		logger.info(swa==null);
		
		String subWfToCheck = currentSubworkflowName; 
		if(!currentSubworkflowName.contains(">")){
			subWfToCheck = ">default>"+currentSubworkflowName;
		}
		
		if(!getModelManager().getAvailableSuperActions(username).contains(swa.getName())){
			exists = "true";
		}else{
			exists = "false";			
		}
	}
	
	public void exportModel() throws RemoteException {
		String hdfsPath = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("hdfsPath");

		DataFlowInterface dfi = getworkFlowInterface();
		logger.info("privilege : '" + privilege + "'");
		Boolean privilegeVal = null;
		if (privilege.equals("edit")) {
		} else if (privilege.equals("run")) {
			privilegeVal = new Boolean(false);
		} else if (privilege.equals("license")) {
			privilegeVal = new Boolean(true);
		}
		logger.info("export model: "+hdfsPath+", "+privilegeVal);
		String error = exportModel(rsModel, hdfsPath,privilegeVal);
		displayErrorMessage(error, "EXPORTMODEL");
	}
	
	protected String exportModel(ModelInt model, String hdfsDirectory,Boolean privilege) throws RemoteException{
		String error = null;
		
		List<SubDataFlow> l = new LinkedList<SubDataFlow>();
		Iterator<String> it = model.getSubWorkflowNames().iterator();
		while(it.hasNext()){
			String cur = it.next();
			SubDataFlow sdf = getworkFlowInterface().getNewSubWorkflow();
			sdf.setName(cur);
			sdf.read(cur);
			l.add(sdf);
		}
		File tmpFile = modelInstaller.exportModel(model, l, privilege);
		Map<String,String> propHdfs = getHDFS().getProperties(hdfsDirectory+"/"+tmpFile.getName()); 
		if( propHdfs != null && !propHdfs.isEmpty()){
			error = "File "+hdfsDirectory+"/"+tmpFile.getName()+" already exists.";
		}else{
			logger.info("Copy "+tmpFile.getAbsolutePath()+" to "+hdfsDirectory+"/"+tmpFile.getName());
			error = getHDFS().copyFromLocal(tmpFile.getAbsolutePath(), hdfsDirectory+"/"+tmpFile.getName());
		}
		tmpFile.delete();
		
		
		return error;
	}
	
	public void importModel() throws RemoteException {
		String hdfsPath = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("hdfsPath");
		String error = importModelPriv(hdfsPath,modelScope);
		displayErrorMessage(error, "IMPORTMODEL");
		if("system".equalsIgnoreCase(modelScope)){
			calcSystemModels();
		}else{
			calcUserModels();
		}
	}
	
	private String importModelPriv(String pathHdfs, String scope) throws RemoteException {
		String error = null;
		logger.info("path '" + pathHdfs + "'");
		if (pathHdfs == null || pathHdfs.isEmpty()) {
			error = "Path to get SubWorkflow is Empty";
		} else if(pathHdfs.lastIndexOf('-') == -1 ){
			error = "File should have the format {name}-{version}.zip";
		}else {
			
			String[] path = pathHdfs.split("/");
			String tmpPath = WorkflowPrefManager.getPathtmpfolder()+"/"+path[path.length-1];
			File tmpFile = new File(tmpPath);
			String modelName = path[path.length-1].substring(0, path[path.length-1].lastIndexOf('-'));
			logger.info("Copy "+pathHdfs+" "+tmpPath);
			getHDFS().copyToLocal(pathHdfs, tmpPath);
			ModelInt model = null;
			if("system".equalsIgnoreCase(scope)){
				model = getModelManager().getSysModel(modelName);
			}else{
				model = getModelManager().getUserModel(getUserInfoBean().getUserName(), modelName);
			}
			model.importModel(tmpFile);
			tmpFile.delete();
			List<SubDataFlow> l = new LinkedList<SubDataFlow>();
			Iterator<String> it = model.getSubWorkflowNames().iterator();
			while(it.hasNext()){
				String cur = it.next();
				SubDataFlow sdf = getworkFlowInterface().getNewSubWorkflow();
				sdf.setName(cur);
				sdf.read(cur);
				l.add(sdf);
			}
			modelInstaller.installModelWebappFiles(model, l);
		}
		return error;
	}
	
	public void removeSystemModel() throws RemoteException{
		logger.info("rm sys model");

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		if(isAdmin() && name != null){
			modelInstaller.uninstallModel(getModelManager().getSysModel(name));
			calcSystemModels();
		}
		usageRecordLog().addSuccess("REMOVESYSTEMMODEL");
	}

	public void removeUserModel() throws RemoteException{
		logger.info("rm user model");

		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		if(isUserAllowInstall() && name != null){
			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			String user = (String) session.getAttribute("username");
			modelInstaller.uninstallModel(getModelManager().getUserModel(user,name));
			calcUserModels();
		}
		usageRecordLog().addSuccess("REMOVEUSERMODEL");
	}

	
	public void createSysModel() throws RemoteException {
		logger.info("create sys model");
		String newModelName = name;
		if(isAdmin() && newModelName != null){
			ModelInt mInt = getModelManager().getSysModel(newModelName);
			if(!mInt.getFile().exists() ){
				mInt.createModelDir();
				calcSystemModels();
				usageRecordLog().addSuccess("CREATESYSTEMMODEL");
			}else{
				MessageUseful.addErrorMessage("Model "+name +" already exists.");
				HttpServletRequest request = (HttpServletRequest) FacesContext
						.getCurrentInstance().getExternalContext()
						.getRequest();
				request.setAttribute("msnError", "msnError");
			}
		}
	}

	
	public void createUserModel() throws RemoteException {
		logger.info("create user model");
		//Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String newModelName = name;
		if(isUserAllowInstall() && newModelName != null){
			ModelInt mInt = getModelManager().getUserModel(userName,newModelName);
			if(!mInt.getFile().exists() ){
				mInt.createModelDir();
				calcUserModels();
				usageRecordLog().addSuccess("CREATEUSERMODEL");
			}else{
				MessageUseful.addErrorMessage("Model "+name +" already exists.");
				HttpServletRequest request = (HttpServletRequest) FacesContext
						.getCurrentInstance().getExternalContext()
						.getRequest();
				request.setAttribute("msnError", "msnError");
			}
		}
	}


	public void mountSubName() {
		String val = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("subWorkflowName");
		setAsSystem("User");
		this.currentSubworkflowName = val;
		setName(val);
		setPrivilege("edit");
	}



	public void refreshModelList() throws RemoteException {
		logger.info("refreshModelList");
		getAdminValue();
		calcSystemModels();
		calcUserModels();
	}

	
	public void deleteSubWorkflow() throws RemoteException{

		List<Integer> posToDell = subWorkflowFromModel.getAllSelected();
		if(posToDell != null){
			for (int i = 0; i < posToDell.size(); i++) {
				int pos = posToDell.get(i);
				String subWFName = subWorkflowFromModel.getRow(pos).get(getMessageResources("label_name"));
				rsModel.delete(subWFName);
			}
		}

		usageRecordLog().addSuccess("DELETESUBWORKFLOW");
	}
	
	public void renameModel() throws RemoteException {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String newModelName = params.get("modelName");
		String error = null;
		ModelInt newModel = null;
		if(rsModel.isSystem()){
			 newModel = getModelManager().getSysModel(newModelName);
		}else{
			 newModel = getModelManager().getUserModel(userName,newModelName);
		}
		
		if(!newModel.getFile().exists() ){
			newModel.createModelDir();
			error = copyMoveSubWorkflow(newModel, rsModel.getSubWorkflowNames(),true,false);
			if(error == null){
				newModel.setComment(rsModel.getComment());
				newModel.setVersion(rsModel.getVersion());
				try{
					LocalFileSystem.delete(rsModel.getFile());
				}catch(Exception e){
					logger.warn("Fail to remove directory "+rsModel.getName());
				}
				rsModel = newModel;
				name = newModelName;
				if(rsModel.isSystem()){
					calcSystemModels();
				}else{
					calcUserModels();
				}
			}
		}else{
			error = "Model already exists.";
		}
		displayErrorMessage(error, "RENAMEMODEL");
	}


	public void listenerImageFile(UploadEvent event) throws Exception {
		String error = null;
		UploadItem item = event.getUploadItem();
		try{
			
			File osFile = new File("/tmp/"+name+".gif");
			FileOutputStream os = new FileOutputStream(osFile);
			os.write(item.getData());
			os.close();
			rsModel.setImage(osFile);
			osFile.delete();
			refreshModelList();
		}catch(Exception e){
			error = "Fail to upload the image: "+e.getMessage();
			logger.warn(error,e);
		}
		displayErrorMessage(error, "CHANGEIMAGE");
	}

	
	public void copyBefore(){
		logger.info("copy before");
		copyMove = "C";
		copyMoveBefore();
	}

	public void moveBefore(){
		logger.info("move before");
		copyMove = "M";
		copyMoveBefore();
	}
	
	private void displayErrorMessage(String error, String usageRecordField){

		if(error != null){
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			logger.info(error);
			usageRecordLog().addError(usageRecordField, error);
		}else{
			usageRecordLog().addSuccess(usageRecordField);
		}
	}
	
	protected void copyMoveBefore() {
		logger.info("copy move before");
		String error = null;
		List<Integer> posToCopyMove = subWorkflowFromModel.getAllSelected();
		if(posToCopyMove.isEmpty()){
			error = "No subworkflows to copy/move";
		}else{
			subWFNamesToCopy = new HashSet<String>(posToCopyMove.size());
			for (int i = 0; i < posToCopyMove.size(); i++) {
				int pos = posToCopyMove.get(i);
				subWFNamesToCopy.add(subWorkflowFromModel.getRow(pos).get(getMessageResources("label_name")));
			}
			logger.info("Subworkflows to copy/move: "+subWFNamesToCopy.toString());
		}
		displayErrorMessage(error,"ERROR COPYMOVEBEFORE");
	}
	
	public void addPublic() throws RemoteException{
		logger.info("add public");
		String error = null;
		List<Integer> addToPublic = subWorkflowFromModel.getAllSelected();
		if(addToPublic.isEmpty()){
			error = "No subworkflow selected.";
		}else{
			logger.info("1");
			for (int i = 0; i < addToPublic.size(); i++) {
				logger.info("2");
				int pos = addToPublic.get(i);
				logger.info(pos);
				logger.info("Add "+pos+" to public: "+subWorkflowFromModel.getRow(pos).get(getMessageResources("label_name")));
				rsModel.removeFromPrivate(subWorkflowFromModel.getRow(pos).get(getMessageResources("label_name")));
			}
			updateSubWorkflowFromModel();
		}
		displayErrorMessage(error,"ERROR ADDPUBLIC");
	}
	
	public void addPrivate() throws RemoteException{
		logger.info("add private");
		String error = null;
		List<Integer> addToPrivate = subWorkflowFromModel.getAllSelected();
		if(addToPrivate.isEmpty()){
			error = "No subworkflow selected.";
		}else{
			for (int i = 0; i < addToPrivate.size(); i++) {
				int pos = addToPrivate.get(i);
				logger.info("Add "+pos+" to private: "+subWorkflowFromModel.getRow(pos).get(getMessageResources("label_name")));
				rsModel.addToPrivate(subWorkflowFromModel.getRow(pos).get(getMessageResources("label_name")));
			}
			updateSubWorkflowFromModel();
		}
		displayErrorMessage(error,"ERROR ADDPRIVATE");
	}
	
	public void copyMoveSubWorkflow() throws RemoteException{
		logger.info("copy move subworkflow");
		String error =  null;
		Map<String,String> modelToSelected = modelGrid.getRow(modelGrid.getAllSelected().get(0));
		logger.info(modelToSelected);
		String user = modelToSelected.get(getMessageResources("label_scope"));
		String modelToStr = modelToSelected.get(getMessageResources("label_name"));
		ModelInt modelTo = null;
		if(modelToStr == null){
			logger.warn("model not selected");
		}
		if(user.equals("system")){
			modelTo = getModelManager().getSysModel(modelToStr);
		}else{
			modelTo = getModelManager().getUserModel(userName,modelToStr);
		}
		logger.info("Subworkflows to copy/move: "+subWFNamesToCopy.toString());
		error = copyMoveSubWorkflow(modelTo,subWFNamesToCopy,"M".equals(copyMove),false);
		if(error != null){
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			logger.info(" " + error);
			usageRecordLog().addError("ERROR COPYSUBWORKFLOW", error);
		}
	}
	
	protected String copyMoveSubWorkflow(ModelInt modelTo, Set<String> subWFNames,boolean remove,boolean force) throws RemoteException{
		String error = null;
		if(modelTo == null){
			error = "Model not found.";
		}else if(subWFNames== null || subWFNames.isEmpty()){
			error = "No subworkflow selected to copy.";
		}else{
			
			if(remove && !force){
				Set<String> dependent = 
						getModelManager().getSubWorkflowFullNameDependentOn(userName, subWFNames);
				if(!dependent.isEmpty()){
					error = "The following super actions will be broken: "+dependent.toString();
				}
			}
			
			if(error == null){
				DataFlowInterface dfi = getworkFlowInterface();
				List<SubDataFlow> subDF = new ArrayList<SubDataFlow>(subWFNames.size());
				for (String subWFName: subWFNames) {
					subWFNames.add(subWFName);;
					SubDataFlow sdCur = dfi.getNewSubWorkflow();
					sdCur.setName(rsModel.getFullName(subWFName));
					sdCur.readFromLocal(new File(rsModel.getFile(),subWFName));
					logger.info(subWFName+": "+new File(rsModel.getFile(),subWFName)+" "+sdCur.getName());
					subDF.add(sdCur);
				}
				error = changeSWofModels(rsModel,modelTo,subWFNames,subDF,remove);
			}
		}
		return error;
	}
	

	protected String changeSWofModels(
			ModelInt modelFrom, ModelInt modelTo,
			Collection<String> subDataFlowNames,
			Collection<SubDataFlow> subDataFlows,
			boolean removeOld) throws RemoteException {
		String error = null;
		String modelFromName = modelFrom.getName();
		String modelToName = modelTo.getName();
		Set<String> modelToExistingModels = modelTo.getSubWorkflowNames();
		Iterator<SubDataFlow> itSubDF = subDataFlows.iterator();
		while(itSubDF.hasNext() && error == null){
			SubDataFlow cur = itSubDF.next();
			String subDataFlowName = cur.getName(); 
			//Check Model exists in output
			if(modelToExistingModels.contains(subDataFlowName)){
				error = subDataFlowName+" already exists in "+modelToName+".";
			}
		}
		
		if(error == null){
			try {
				Iterator<SubDataFlow> it = subDataFlows.iterator();
				while(it.hasNext()){
					SubDataFlow subDataFlow = it.next();
					String subDataFlowPrevFullName = subDataFlow.getName(); 
					String subDataFlowName = subDataFlowPrevFullName.substring(modelFromName.length()+2);
					subDataFlow.setName(">"+modelToName+">"+subDataFlowName);
					Iterator<String> itchangeNames =  subDataFlowNames.iterator();
					while(itchangeNames.hasNext()){
						String oldName = itchangeNames.next();
						String newName = ">"+modelToName +">"+ oldName;
						subDataFlow.renameSA(">"+modelFromName+">"+oldName, newName);
					}
					modelInstaller.installSA(modelTo, subDataFlow, subDataFlow.getPrivilege());
					if(removeOld){
						modelInstaller.uninstallSA(modelFrom, subDataFlowPrevFullName);
					}
				}
			} catch (Exception e) {
				error = "Unexpected exception.";
				logger.error(e,e);
			}
		}
		
		return error;
	}
	
	public void toggleEditable() throws RemoteException{
		logger.info("updateModel");
		recordModel();
		rsModel.setEditable(!rsModel.isEditable());
		getModel().setEditable(rsModel.isEditable());
	}
	
	public void recordModel()  throws RemoteException{
		String modelIndex = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("index");
		String modelScope = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("scope");
		if(modelScope.equalsIgnoreCase("system")){
			setModel(getSystemModels().get(Integer.valueOf(modelIndex)));
			rsModel = getModelManager().getSysModel(model.getName());
		}else{
			setModel(getUserModels().get(Integer.valueOf(modelIndex)));
			rsModel = getModelManager().getUserModel(userName,model.getName());
		}
	}
	
	public void updateModel() throws RemoteException{
		logger.info("updateModel");
		recordModel();
		updateModelPriv();
	}
	
	private void updateModelPriv() throws RemoteException{
		name = rsModel.getName();
		comment = rsModel.getComment();
		version = rsModel.getVersion();
		
		
		updateSubWorkflowFromModel();
	}
	
	public void updateModelComment() throws RemoteException{
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		comment = params.get("modelComment");
		logger.info("updateModelComment: "+comment);
		rsModel.setComment(comment);
		if(rsModel.isSystem()){
			calcSystemModels();
		}else{
			calcUserModels();
		}
	}

	public void updateModelVersion() throws RemoteException{
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String error = null;
		String regex="[0-9]+(\\.{0,1}[0-9]+)*";
		String modelVersion = params.get("modelVersion");
		if(!modelVersion.matches(regex)){
			error = "A version should only include number with optional dots between them.";
		}else{
			version = modelVersion;
			logger.info("updateModelVersion: "+version);
			rsModel.setVersion(version);
			if(rsModel.isSystem()){
				calcSystemModels();
			}else{
				calcUserModels();
			}
		}
		displayErrorMessage(error, "UPDATEMODELVERSION");
	}
	
	public void updateSubWorkflowFromModel() throws RemoteException{
		subWorkflowFromModel = new SelectableTable();
		Iterator<String> it = rsModel.getSubWorkflowNames().iterator();
		Set<String> publicSW = rsModel.getPublicSubWorkflowNames();
		logger.info("Public subworkflows: "+publicSW.toString());
		Map<String, Set<String>> depends = rsModel.getDependenciesPerSubWorkflows();
		String name = getMessageResources("label_name");
		String access = getMessageResources("label_access_sa");
		String dependencies = getMessageResources("label_dep");
		List<String> lName = new LinkedList<String>();
		lName.add(name);
		lName.add(access);
		lName.add(dependencies);
		subWorkflowFromModel.setColumnIds(lName);
		while(it.hasNext()){
			String curName = it.next();
			Map<String,String> cur = new LinkedHashMap<String,String>();
			cur.put(name, curName);
			cur.put(access, publicSW.contains(curName) ? "public" : "private");
			String curDep = "";
			if(depends.containsKey(curName)){
				String curDepListStr = depends.get(curName).toString(); 
				curDep = curDepListStr.substring(1,curDepListStr.length()-1);
			}
			cur.put(dependencies, curDep);
			subWorkflowFromModel.add(cur);
		}
	}
	
	public void beforeRenameSubWorkfow(){
		logger.info("beforeRenameSubWorkfow");
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		currentSubworkflowName = params.get("subWFName"); 
	}
	
	public void renameSubWorkflow() throws RemoteException{
		logger.info("renameSubWorkflow");
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		renameSubWorkflow(currentSubworkflowName, params.get("subWFNewName"));
		updateSubWorkflowFromModel();
	}
	
	protected void renameSubWorkflow(String oldNameSubWf, String newNameSubWf) throws RemoteException{
		logger.info("rename "+oldNameSubWf+" to "+newNameSubWf);
		DataFlowInterface dfi = getworkFlowInterface();
		SubDataFlow sdf = dfi.getNewSubWorkflow();
		sdf.setName(rsModel.getFullName(oldNameSubWf));
		sdf.readFromLocal(new File(rsModel.getFile(),oldNameSubWf));
		sdf.setName(rsModel.getFullName(newNameSubWf));
		
		modelInstaller.installSA(rsModel, sdf, sdf.getPrivilege());
		modelInstaller.uninstallSA(rsModel, rsModel.getFullName(oldNameSubWf));
	}
	
	private void updateModelGrid() throws RemoteException{
		modelGrid = new SelectableTable();
		String scope = getMessageResources("label_scope");
		String name = getMessageResources("label_name");
		List<String> lName = new LinkedList<String>();
		lName.add(scope);
		lName.add(name);
		
		modelGrid.setColumnIds(lName);
		List<RedSqirlModule> userRS = getUserModels(); 
		if(isUserAllowInstall() && userRS != null){
			Iterator<RedSqirlModule> it = userRS.iterator();
			while(it.hasNext()){
				Map<String,String> cur = new LinkedHashMap<String,String>();
				cur.put(scope, "user");
				cur.put(name, it.next().getName());
				logger.info(cur.toString());
				modelGrid.add(cur);
			}
		}
		List<RedSqirlModule> adminRS = getSystemModels();
		if(isAdmin() && adminRS != null){
			Iterator<RedSqirlModule> it = adminRS.iterator();
			while(it.hasNext()){
				Map<String,String> cur = new LinkedHashMap<String,String>();
				cur.put(scope, "system");
				cur.put(name, it.next().getName());
				logger.info(cur.toString());
				modelGrid.add(cur);
			}
		}
	}
	
	public void storeScope(){
		logger.info("store scope");
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		asSystem = params.get("scope");
	}

	public void calcSystemModels() throws RemoteException{
		logger.info("sys model");
		setSystemModels(calcModel(getModelManager().getSysModels()));
		updateModelGrid();
	}

	public void calcUserModels() throws RemoteException{
		logger.info("user models");
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		String user = (String) session.getAttribute("username");
		setUserModels(calcModel(getModelManager().getUserModels(user)));
		updateModelGrid();
	}
	
	
	private List<RedSqirlModule> calcModel(Iterable<ModelInt> modelIterable) throws RemoteException{
		Iterator<ModelInt> it = modelIterable.iterator();
		List<RedSqirlModule> result = new LinkedList<RedSqirlModule>();
		while(it.hasNext()){
			ModelInt model = it.next();
			RedSqirlModule rdm = new RedSqirlModule();
			rdm.setImage(LocalFileSystem.relativize(getCurrentPage(), model.getTomcatImage().getAbsolutePath()));
			rdm.setName(model.getName());
			rdm.setVersionName(model.getVersion());
			rdm.setVersionNote(model.getComment());
			rdm.setEditable(model.isEditable());
			result.add(rdm);
		}
		return result;
	}

	private UserInfoBean getUserInfoBean() {
		FacesContext context = FacesContext.getCurrentInstance();
		UserInfoBean userInfoBean = (UserInfoBean) context.getApplication()
				.evaluateExpressionGet(context, "#{userInfoBean}",
						UserInfoBean.class);

		return userInfoBean;
	}
	

	public String getName() {
		return name;
	}

	public void setName(String name) {

		this.name = name;
	}

	
	/**
	 * @param admin
	 *            the admin to set
	 */
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	
	public boolean getAdmin() {
		return admin;
	}

	public void getAdminValue() {
		admin = false;
		try {
			logger.info("is admin");

			String user = getUserInfoBean().getUserName();
			String[] admins = WorkflowPrefManager.getSysAdminUser();
			if (admins != null) {
				for (String cur : admins) {
					admin = admin || cur.equals(user);
					logger.debug("admin user: " + cur);
				}
			}
		} catch (Exception e) {
			logger.warn("Exception in isAdmin: " + e.getMessage());
		}
		logger.info("is admin " + admin);
	}
	
	/**
	 * @return the asSystem
	 */
	public String getAsSystem() {
		return asSystem;
	}

	/**
	 * @param asSystem
	 *            the asSystem to set
	 * @throws RemoteException 
	 */
	public void setAsSystem(String asSystem){
		this.asSystem = asSystem;
	}
	
	
	/**
	 * @return the uninstallUserSa
	 */
	public List<SelectItem> getUninstallUserSa() {
		return uninstallUserSa;
	}

	/**
	 * @param uninstallUserSa
	 *            the uninstallUserSa to set
	 */
	public void setUninstallUserSa(List<SelectItem> uninstallUserSa) {
		this.uninstallUserSa = uninstallUserSa;
	}

	/**
	 * @return the uninstallSysSa
	 */
	public List<SelectItem> getUninstallSysSa() {
		return uninstallSysSa;
	}

	/**
	 * @param uninstallSysSa
	 *            the uninstallSysSa to set
	 */
	public void setUninstallSysSa(List<SelectItem> uninstallSysSa) {
		this.uninstallSysSa = uninstallSysSa;
	}

	public String getPathHDFS() {
		return pathHDFS;
	}

	public void setPathHDFS(String pathHDFS) {
		this.pathHDFS = pathHDFS;
	}

	public String getPrivilege() {
		return privilege;
	}

	public void setPrivilege(String privilege) {
		this.privilege = privilege;
	}

	public String getExists() {
		return exists;
	}

	public void setExists(String exists) {
		this.exists = exists;
	}

	/**
	 * @return the systemModels
	 */
	public List<RedSqirlModule> getSystemModels() {
		return systemModels;
	}

	/**
	 * @param systemModels the systemModels to set
	 */
	public void setSystemModels(List<RedSqirlModule> systemModels) {
		this.systemModels = systemModels;
	}

	/**
	 * @return the userModels
	 */
	public List<RedSqirlModule> getUserModels() {
		return userModels;
	}

	/**
	 * @param userModels the userModels to set
	 */
	public void setUserModels(List<RedSqirlModule> userModels) {
		this.userModels = userModels;
	}

	/**
	 * @return the model
	 */
	public RedSqirlModule getModel() {
		return model;
	}

	/**
	 * @param model the model to set
	 */
	public void setModel(RedSqirlModule model) {
		this.model = model;
	}

	/**
	 * @return the subWorkflowFromModel
	 */
	public SelectableTable getSubWorkflowFromModel() {
		return subWorkflowFromModel;
	}

	/**
	 * @param subWorkflowFromModel the subWorkflowFromModel to set
	 */
	public void setSubWorkflowFromModel(SelectableTable subWorkflowFromModel) {
		this.subWorkflowFromModel = subWorkflowFromModel;
	}

	/**
	 * @return the modelGrid
	 */
	public SelectableTable getModelGrid() {
		return modelGrid;
	}

	/**
	 * @param modelGrid the modelGrid to set
	 */
	public void setModelGrid(SelectableTable modelGrid) {
		this.modelGrid = modelGrid;
	}

	/**
	 * @return the copyMove
	 */
	public String getCopyMove() {
		return copyMove;
	}

	/**
	 * @param copyMove the copyMove to set
	 */
	public void setCopyMove(String copyMove) {
		this.copyMove = copyMove;
	}
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCurrentSubworkflowName() {
		return currentSubworkflowName;
	}

	public void setCurrentSubworkflowName(String currentSubworkflowName) {
		this.currentSubworkflowName = currentSubworkflowName;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
}
