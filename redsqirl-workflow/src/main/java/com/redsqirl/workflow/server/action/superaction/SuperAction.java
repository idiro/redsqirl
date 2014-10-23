package com.redsqirl.workflow.server.action.superaction;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

/**
 * SuperAction class, read the given superaction and initialise the
 * needed value for running a subworkflow.
 * @author etienne
 *
 */
public class SuperAction extends DataflowAction implements SuperElement{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4061277134086282971L;

	private static Logger logger = Logger.getLogger(SuperAction.class);


	private Map<String, DFELinkProperty> input = new LinkedHashMap<String, DFELinkProperty>();

	private Map<LinkedList<String>, DFEOutput> tmpOutput = new LinkedHashMap<LinkedList<String>,DFEOutput>();

	private String name = null;
	private String errorInit = null;

	private Boolean privilege = null;
	
	public SuperAction() throws RemoteException {
		super(new SubWorkflowAction());
		name = "superaction";
	}

	public SuperAction(String name) throws RemoteException {
		super(new SubWorkflowAction());
		setName(name);
	}

	private void readMetadataSuperAction(){
		try{
			SubWorkflow saw = new SubWorkflow(name);
			errorInit = saw.readMetaData();
			if(errorInit == null){
				logger.debug("Set input and output of the action");
				input = saw.getInputSuperAction();
				tmpOutput = saw.getTmpOutput();
				generateTmpPathsIfNull();
				output = saw.getOutputSuperAction();
				privilege = saw.getPrivilege();
			}else{
				logger.debug("Error when reading the metadata: "+errorInit);
			}
		}catch(Exception e){
			logger.error("Fail to read Super Action Meta data: "+e,e);
		}
	}

	public void setName(String name) throws RemoteException{
		if(name == null){
			logger.debug("No name: Clear input and output...");
			input.clear();
			output.clear();
		}else if(!name.equals(this.name)){
			logger.debug("Set the name...");
			this.name  = name;
			logger.debug("Read the metadata of the sub-workflow...");
			readMetadataSuperAction();
		}
	}

	@Override 
	public String regeneratePaths(Boolean copy,boolean force)  throws RemoteException{
		super.regeneratePaths(copy,force);
		String userName = System.getProperty("user.name");
		Iterator<Entry<LinkedList<String>,DFEOutput> > it = tmpOutput.entrySet().iterator();
		while(it.hasNext()){
			Entry<LinkedList<String>,DFEOutput> cur = it.next();
			if(force){
				cur.getValue().setSavingState(SavingState.TEMPORARY);
			}
			cur.getValue().regeneratePath(
					copy, 
					userName, 
					getComponentId(),
					cur.getKey().getLast());
		}
		return null;
	}

	public void generateTmpPathsIfNull() throws RemoteException{
		Iterator<Entry<LinkedList<String>,DFEOutput> > it = tmpOutput.entrySet().iterator();
		String userName = System.getProperty("user.name");
		while(it.hasNext()){
			Entry<LinkedList<String>,DFEOutput> cur = it.next();
			LinkedList<String> curKey = cur.getKey();
			if(!SavingState.RECORDED.equals(cur.getValue())){
				cur.getValue().setSavingState(SavingState.TEMPORARY);
				if ( cur.getValue().getPath() == null
						|| !cur.getValue()
						.isPathAutoGeneratedForUser(userName,
								curKey.get(curKey.size()-2), 
								curKey.get(curKey.size()-1))) {
					cur.getValue().generatePath(userName,
							getComponentId(),
							cur.getKey().getLast());
				}
			}
		}
	}

	/**
	 * Get the path to the Image
	 * 
	 * @return path
	 * @throws RemoteException
	 */
	@Override
	public String getImage() throws RemoteException {
		String absolutePath = "";
		String imageFile = "/image/superaction.gif";
		String path = WorkflowPrefManager
				.getSysProperty(WorkflowPrefManager.sys_tomcat_path);
		List<String> files = listFilesRecursively(path);
		for (String file : files) {
			if (file.contains(imageFile)) {
				absolutePath = file;
				break;
			}
		}
		
		if(logger.isDebugEnabled()){
			String ans = "";
			if (absolutePath.contains(path)) {
				ans = absolutePath.substring(path.length());
			}
			logger.debug("SuperAction image abs Path : " + absolutePath);
			logger.debug("SuperAction image Path : " + path);
			logger.debug("SuperAction image ans : " + ans);
		}
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

			try {

				String queueStr = ((Element) cur)
						.getElementsByTagName("stack").item(0)
						.getChildNodes().item(0).getNodeValue();

				String[] queueArr = queueStr.split(",");
				Element outputEl = (Element) ((Element) cur)
						.getElementsByTagName("output").item(0);
				String outputType = 
						outputEl.getAttributes().getNamedItem("typename").getNodeValue();

				DataOutput out = DataOutput.getOutput(outputType);
				out.read(outputEl);

				LinkedList<String> queueCur = new LinkedList<String>();
				for(int j = 0; j < queueArr.length;++j){
					queueCur.add(queueArr[j]);
				}

			} catch (Exception e) {
				error = LanguageManagerWF.getText(
						"dataflowaction.readvaluesxml",
						new Object[] { componentId });
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
	 * @param fw
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

				Element curEl = doc.createElement("tmpoutput");

				Element stackEl = doc.createElement("stack");
				String stack = "";
				Iterator<String> itStack = cur.getKey().iterator();
				while(itStack.hasNext()){
					stack +=itStack.next()+",";
				}
				stack.substring(0,stack.length()-1);
				stackEl.appendChild(doc.createTextNode(stack));
				curEl.appendChild(stackEl);

				Element outputEl = doc.createElement("output");
				Attr attrType = doc.createAttribute("typename");
				attrType.setValue(cur.getValue().getTypeName());
				outputEl.setAttributeNode(attrType);
				cur.getValue().write(doc, outputEl);


				parent.appendChild(curEl);
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
		String error = errorInit != null? errorInit : checkIntegrationUserVariables();
		if(error == null){
			((OozieSubWorkflowAction) oozieAction).setSuperElement(this);
		}
		return error; 
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
				DFEOutput out = saWf.getElement(curOutName).getDFEInput().get(SubWorkflowOutput.input_name).get(0);
				if(SavingState.TEMPORARY.equals(getDFEOutput().get(curOutName).getSavingState())){
					out.setSavingState(SavingState.BUFFERED);
				}else{
					out.setSavingState(getDFEOutput().get(curOutName).getSavingState());
				}
				out.setPath(getDFEOutput().get(curOutName).getPath());
			}
			
			//Edit input paths
			Iterator<String> itIn = input.keySet().iterator();
			while(itIn.hasNext()){
				String curInName = itIn.next();
				DFEOutput in = saWf.getElement(curInName).getDFEOutput().get(AbstractSource.out_name);
				in.setSavingState(SavingState.RECORDED);
				in.setPath(getDFEInput().get(curInName).get(0).getPath());
			}

			saWf.updateSuperActionTmpOutputs(tmpOutput);

			swAct.setWfId("sa_"+getComponentId()+"_"+RandomString.getRandomName(8));
			swAct.setSubWf(saWf);
		}
		if(error != null){
			logger.warn("Fail set up Oozie action: "+error);
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
		return null;
	}

}
