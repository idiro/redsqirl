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

package com.redsqirl.workflow.server.action.superaction;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.idiro.utils.RandomString;
import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.DataflowAction;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.action.AbstractSource;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.server.interfaces.OozieSubWorkflowAction;
import com.redsqirl.workflow.server.interfaces.SuperElement;
import com.redsqirl.workflow.server.oozie.SubWorkflowAction;
import com.redsqirl.workflow.utils.LanguageManagerWF;
import com.redsqirl.workflow.utils.RedSqirlModel;

/**
 * SuperAction class, read the given superaction and initialise the
 * needed value for running a subworkflow.
 * @author etienne
 *
 */
public class SuperAction extends DataflowAction implements SuperElement{

	private static final long serialVersionUID = 4061277134086282971L;

	private static Logger logger = Logger.getLogger(SuperAction.class);
	
	private Map<String, DFELinkProperty> input = new LinkedHashMap<String, DFELinkProperty>();
	private Map<LinkedList<String>, DFEOutput> tmpOutput = new LinkedHashMap<LinkedList<String>,DFEOutput>();
	private Set<String> superElementDependencies = null;
	private Map<String,String> subWorkflowVariables = null;
	private String name = null;
	private String errorInstall = null;
	private Boolean privilege = null;
	private Page page;
	private SuperActionVariableTable variablesTable;
	
	public SuperAction() throws RemoteException {
		super(new SubWorkflowAction());
		setName("superaction");
	}

	public SuperAction(String name) throws RemoteException {
		super(new SubWorkflowAction());
		setName(name);
		logger.debug("Read the metadata of the sub-workflow...");
		readMetadataSuperElement();
	}
	
	public SuperAction(String name, boolean readPrivilege) throws RemoteException {
		super(new SubWorkflowAction());
		setName(name);
		if(readPrivilege){
			logger.debug("Read the metadata of the sub-workflow...");
			readPrivilegeSuperElement();
		}
	}
	public void readPrivilegeSuperElement(){
		try{
			SubWorkflow saw = new SubWorkflow(name);
			errorInstall = saw.readPrivilege();
			if(errorInstall == null){
				privilege = saw.getPrivilege();
			}else{
				logger.info("Error when reading the metadata: "+errorInstall);
			}
		}catch(Exception e){
			logger.error("Fail to read Super Action Meta data: "+e,e);
		}
	}
	
	public void readMetadataSuperElement(){
		try{
			SubWorkflow saw = new SubWorkflow(name);
			errorInstall = saw.readMetaData();
			if(errorInstall == null){
				logger.info("Set input and output of the action");
				input = saw.getInputSuperAction();
				tmpOutput = saw.getTmpOutput();
				output = saw.getOutputSuperAction();
				privilege = saw.getPrivilege();
				superElementDependencies = saw.getSuperElementDependencies();
				subWorkflowVariables = saw.getCoordinatorVariables();
				
				if(page == null && subWorkflowVariables != null && !subWorkflowVariables.isEmpty()){
					page = addPage(LanguageManagerWF.getText("superaction_page.title"),
							LanguageManagerWF.getText("superaction_page.legend"), 1);
					
					variablesTable = new SuperActionVariableTable();
					
					page.addInteraction(variablesTable);
				}
				
				if(variablesTable != null){
					variablesTable.updateColumnConstraint(subWorkflowVariables);
				}
			}else{
				logger.info("Error when reading the metadata: "+errorInstall);
			}
		}catch(Exception e){
			logger.error("Fail to read Super Action Meta data: "+e,e);
		}
	}

	@Override
	public void setName(String name) throws RemoteException{
		if(name == null){
			logger.debug("No name: Clear input and output...");
			input.clear();
			output.clear();
		}else if(!name.equals(this.name)){
			logger.debug("Set the name...");
			this.name  = name;
		}
	}

	@Override 
	public String regeneratePaths(Boolean copy,boolean force)  throws RemoteException{
		super.regeneratePaths(copy,force);
		Iterator<Entry<LinkedList<String>,DFEOutput> > it = tmpOutput.entrySet().iterator();
		while(it.hasNext()){
			Entry<LinkedList<String>,DFEOutput> cur = it.next();
			
			logger.debug("sa regeneratePaths");
			logger.debug("1 " + copy);
			logger.debug("2 " + getComponentId());
			logger.debug("3 " + cur.getKey().getLast());
			
			cur.getValue().regeneratePath(
					copy, 
					getComponentId(),
					cur.getKey().getLast());
		}
		return null;
	}

	public void generateTmpPathsIfNull() throws RemoteException{
		Iterator<Entry<LinkedList<String>,DFEOutput> > it = tmpOutput.entrySet().iterator();
		while(it.hasNext()){
			Entry<LinkedList<String>,DFEOutput> cur = it.next();
			LinkedList<String> curKey = cur.getKey();
			if(!SavingState.RECORDED.equals(cur.getValue())){
				cur.getValue().setSavingState(SavingState.TEMPORARY);
				if ( cur.getValue().getPath() == null
						|| !cur.getValue()
						.isPathAutoGeneratedForUser(
								curKey.get(curKey.size()-2), 
								curKey.get(curKey.size()-1))) {
					
					
					logger.debug("generateTmpPathsIfNull");
					logger.debug("1: " + getComponentId());
					logger.debug("2: " + cur.getKey().getLast());
					
					cur.getValue().generatePath(getComponentId(),	cur.getKey().getLast());
				}
			}
		}
	}

	public String getHelp() throws RemoteException {
		String[] modelWA = RedSqirlModel.getModelAndSW(getName());
		String fname = modelWA[0]+"/"+modelWA[1] + ".html";
		String relativePath = WorkflowPrefManager.getPathuserhelppref() + "/"
				+ fname;
		File f = new File(WorkflowPrefManager.getSysProperty(
				WorkflowPrefManager.sys_install_package, WorkflowPrefManager
						.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat))
				+ relativePath);
		if (!f.exists()) {
			relativePath = WorkflowPrefManager.getPathSysHelpPref() + "/"
					+ fname;
			f = new File(
					WorkflowPrefManager.getSysProperty(
							WorkflowPrefManager.sys_install_package, WorkflowPrefManager
									.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat))
							+ relativePath);
		}
		String absolutePath = f.getAbsoluteFile().getAbsolutePath();
		logger.debug("help absolutePath : "+absolutePath);
		logger.debug("help relPath : "+relativePath);
		
		return absolutePath;
	}
	
	/**
	 * Get the path to the Image
	 * 
	 * @return path
	 * @throws RemoteException
	 */
	@Override
	public String getImage() throws RemoteException {
		String fname = RedSqirlModel.getModelAndSW(getName())[0] + ".gif";
		String relativePath = WorkflowPrefManager.getPathuserimagepref() + "/model/"
				+ fname;
		File f = new File(WorkflowPrefManager.getSysProperty(
				WorkflowPrefManager.sys_install_package, WorkflowPrefManager
						.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat))
				+ relativePath);
		if (!f.exists()) {
			relativePath = WorkflowPrefManager.getPathsysimagepref() + "/model/"
					+ fname;
			f = new File(
					WorkflowPrefManager.getSysProperty(
							WorkflowPrefManager.sys_install_package, WorkflowPrefManager
									.getSysProperty(WorkflowPrefManager.sys_tomcat_path, WorkflowPrefManager.defaultTomcat))
							+ relativePath);
		}
		
		if(!f.exists()){
			f = new File(RedSqirlModel.getDefaultImage());
		}
		String absolutePath = f.getAbsoluteFile().getAbsolutePath();
		logger.debug("image absolutePath : "+absolutePath);
		logger.debug("image relPath : "+relativePath);
		return absolutePath;
	}

	/**
	 * Read the values for a Node stored in the XML
	 * 
	 * @param n
	 *            Node to read XML for
	 * @return Error Message
	 */
	public String readValuesXml(Node n) {
		String error = null;

		NodeList nl = n.getChildNodes();
		for (int i = 0; i < nl.getLength(); ++i) {
			Node cur = nl.item(i);
			if(cur.getNodeType() == Node.ELEMENT_NODE){
				try {
					Node outputNode = ((Element) cur).getElementsByTagName("output").item(0);					
					if(outputNode != null && outputNode.getNodeType() == Node.ELEMENT_NODE){
						Element outputEl = (Element) outputNode;
						String outputType = outputEl.getAttributes().getNamedItem("typename").getNodeValue();

						DataOutput out = DataOutput.getOutput(outputType);
						if(out == null){
							error = LanguageManagerWF.getText("dataoutput.unknown", new Object[] { outputType });
							break;
						}
						out.read(outputEl);


						try{
							String queueStr = ((Element) cur).getElementsByTagName("stack").item(0).getChildNodes().item(0).getNodeValue();
							String[] queueArr = queueStr.split(",");
							LinkedList<String> queueCur = new LinkedList<String>();
							for(int j = 0; j < queueArr.length;++j){
								queueCur.add(queueArr[j]);
							}
						}catch(Exception e){
							logger.warn(e,e);
						}

					}

				} catch (Exception e) {
					//error = LanguageManagerWF.getText("dataflowaction.readvaluesxml", new Object[] { componentId });
					logger.warn("error "+e,e);
				}
			}
		}

		if (error != null) {
			logger.error(error);
		}
		return error;
	}

	/**
	 * Writes values for this action.
	 * 
	 * @param doc
	 * @param parent
	 * @return null if OK, or a description of the error.
	 * @throws RemoteException
	 */
	public String writeValuesXml(Document doc, Node parent)
			throws RemoteException {
		String error = null;
		try {
			Iterator<Entry<LinkedList<String>,DFEOutput> > it = tmpOutput.entrySet().iterator();
			while(it.hasNext()){
				Entry<LinkedList<String>,DFEOutput> cur = it.next();
				if(cur.getValue() != null && cur.getValue().getTypeName() != null){
					Element curEl = doc.createElement("tmpoutput");

					Element stackEl = doc.createElement("stack");
					String stack = "";
					Iterator<String> itStack = cur.getKey().iterator();
					while(itStack.hasNext()){
						stack +=itStack.next()+",";
					}
					stack = stack.substring(0,stack.length()-1);
					stackEl.appendChild(doc.createTextNode(stack));
					curEl.appendChild(stackEl);

					Element outputEl = doc.createElement("output");
					Attr attrType = doc.createAttribute("typename");
					attrType.setValue(cur.getValue().getTypeName());
					outputEl.setAttributeNode(attrType);
					cur.getValue().write(doc, outputEl);
					curEl.appendChild(outputEl);


					parent.appendChild(curEl);
				}
			}

		} catch (DOMException dme) {
			error = LanguageManagerWF.getText(
					"dataflowaction.writevaluesxml_domexception",
					new Object[] { dme.getMessage() });
		} catch (Exception e) {
			error = LanguageManagerWF.getText("dataflowaction.writevaluesxml",
					new Object[] { e.getMessage() });
		}

		if (error != null) {
			logger.error(error);
		}

		return error;
	}

	@Override
	public String getName() throws RemoteException {
		return name;
	}
	
	@Override
	public String cleanDataOut() throws RemoteException {
		String err = super.cleanDataOut();
		if(err != null){
			err = "";
			if (tmpOutput != null) {
				Iterator<DFEOutput> it = tmpOutput.values().iterator();
				while (it.hasNext()) {
					DFEOutput cur = it.next();
					if (cur != null) {
						String curErr = cur.clean();
						if (curErr != null) {
							err = err + curErr + "\n";
						}
					}
				}
			}
			if (err.isEmpty()) {
				err = null;
			}
		}
		return err;
	}

	@Override
	public Map<String, DFELinkProperty> getInput() throws RemoteException {
		return input;
	}

	@Override
	public String updateOut() throws RemoteException {
		if(errorInstall == null){
			((OozieSubWorkflowAction) oozieAction).setSuperElement(this);
			if(variablesTable != null){
				variablesTable.updateOozieAction((SubWorkflowAction) oozieAction, subWorkflowVariables);
			}
		}
		return errorInstall; 
	}

	@Override
	public boolean writeOozieActionFiles(File[] files) throws RemoteException {
		return true;
	}
	
	public void updateOozieSubWorkflowAction() throws RemoteException{
		SubWorkflowAction swAct = (SubWorkflowAction) getOozieAction();

		SubWorkflow saWf = new SubWorkflow(name);
		String error = saWf.readFromLocal(saWf.getInstalledMainFile());
		if(error != null){
			logger.error("Fail to read superaction "+name+": "+error);
			throw new RemoteException(error);
		}else{
			logger.debug("Workflow elements: "+saWf.getComponentIds());

			//Edit tmp paths
			Iterator<Entry<LinkedList<String>,DFEOutput>> it = tmpOutput.entrySet().iterator();
			while(it.hasNext()){
				Entry<LinkedList<String>,DFEOutput> cur = it.next();
				LinkedList<String> curKey = cur.getKey();
				if(curKey.size() == 2){
					DataFlowElement dfe = saWf.getElement(curKey.getFirst());
					if(dfe != null){
						dfe.getDFEOutput().put(curKey.getLast(), cur.getValue());
					}
				}
			}

			//Edit output paths
			Iterator<String> itOut = getDFEOutput().keySet().iterator();
			while(itOut.hasNext()){
				String curOutName = itOut.next();
				try{
					DFEOutput out = saWf.getElement(curOutName).getDFEInput().get(SubWorkflowOutput.input_name).get(0);
					out.setSavingState(SavingState.RECORDED);
					out.setPath(getDFEOutput().get(curOutName).getPath());
				}catch(NullPointerException e){
					error = LanguageManagerWF.getText("superaction.needrefresh", new Object[]{name,componentId,curOutName});
					logger.warn(error,e);
					throw new RemoteException(error);
				}
			}
			
			//Edit input paths
			Iterator<String> itIn = input.keySet().iterator();
			while(itIn.hasNext()){
				String curInName = itIn.next();
				DFEOutput in = saWf.getElement(curInName).getDFEOutput().get(SubWorkflowInput.out_name);
				in.setSavingState(SavingState.RECORDED);
				in.setPath(getDFEInput().get(curInName).get(0).getPath());
				Map<String, String> wfProp=  getDFEInput().get(curInName).get(0).getProperties();
				for (Entry<String,String> cur: wfProp.entrySet()) {
					in.addProperty(cur.getKey(), cur.getValue());
				}
			}

			saWf.updateSuperActionTmpOutputs(tmpOutput);
			
			Iterator<DataFlowElement> itDfe = saWf.getElement().iterator();
			while(itDfe.hasNext()){
				DataFlowElement cur = itDfe.next();
				cur.updateOut();
			}

			swAct.setWfId("sa_"+getComponentId()+"_"+RandomString.getRandomName(8));
			swAct.setSubWf(saWf);
		}
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
	}

	/**
	 * @return the tmpOutput
	 */
	public final Map<LinkedList<String>, DFEOutput> getTmpOutput() {
		return tmpOutput;
	}

	/**
	 * @param tmpOutput the tmpOutput to set
	 */
	public final void setTmpOutput(Map<LinkedList<String>, DFEOutput> tmpOutput) {
		this.tmpOutput = tmpOutput;
	}

	@Override
	public Boolean getPrivilege() throws RemoteException {
		return privilege;
	}

	@Override
	public Set<String> getSuperElementDependencies() {
		return superElementDependencies;
	}

	public void setDependencies(Set<String> dependencies) {
		this.superElementDependencies = dependencies;
	}
	
	@Override
	public String getErrorInstall() throws RemoteException {
		return errorInstall;
	}

}
