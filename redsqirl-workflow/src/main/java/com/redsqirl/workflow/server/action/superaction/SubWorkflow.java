package com.redsqirl.workflow.server.action.superaction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.idiro.Log;
import com.idiro.ProjectID;
import com.idiro.hadoop.NameNodeVar;
import com.idiro.utils.RandomString;
import com.idiro.utils.XmlUtils;
import com.redsqirl.keymanager.ciphers.Decrypter;
import com.redsqirl.keymanager.ciphers.KeyCipher;
import com.redsqirl.workflow.server.DataOutput;
import com.redsqirl.workflow.server.Workflow;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.server.interfaces.SubDataFlow;
import com.redsqirl.workflow.server.interfaces.SuperElement;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * 
 * @author etienne
 *
 */
public class SubWorkflow extends Workflow implements SubDataFlow{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5755165800054576213L;

	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(SubWorkflow.class);


	private Map<LinkedList<String>, DFEOutput> tmpOutput = new LinkedHashMap<LinkedList<String>,DFEOutput>();
	private Map<String,DFEOutput> outputSuperAction = new LinkedHashMap<String,DFEOutput>();
	private Map<String, DFELinkProperty> inputSuperAction = new LinkedHashMap<String, DFELinkProperty>();
	
	/**
	 * Null: all privilege
	 * False: runnable only
	 * True: need a license key and runnable only
	 */
	private Boolean privilege;

	public SubWorkflow() throws RemoteException {
		super();
	}


	public SubWorkflow(String name) throws RemoteException {
		super(name);
	}

	/**
	 * Save on HDFS
	 */
	public String save(String filePath) throws RemoteException{
		return save(filePath,privilege);
	}
	
	public String save(String filePath,Boolean newPrivilege) throws RemoteException{
		String error = null;
		try{
			String[] path = filePath.split("/");
			String fileName = path[path.length - 1];
			String tempPath = WorkflowPrefManager.getPathuserpref() + "/tmp/"
					+ fileName + "_" + RandomString.getRandomName(4);

			logger.info(filePath);
			error = saveXmlOnLocal(new File(tempPath),newPrivilege);

			if(error == null){
				if(!filePath.endsWith(".srs")){
					filePath = filePath+".srs";
				}
				FileSystem fs = NameNodeVar.getFS();
				fs.moveFromLocalFile(new Path(tempPath), new Path(filePath));
			}
		}catch(Exception e){
			error = "Exception while saving a Super Action";
			logger.error(error,e);
		}

		return error;
	}

	public String saveLocal(File f,Boolean newPrivilege){
		String error = null;
		try{
			error = check();

			if(error != null){
				error = "Cannot install a faulty super action";
			}else{
				error = saveXmlOnLocal(f,newPrivilege);
			}

		}catch(Exception e){
			error = "Exception while installing a Super Action";
			logger.error(error,e);
		}

		return error;
	}
	
	@Override
	public String getBackupName(String path) throws RemoteException{
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		if (getName() != null && !getName().isEmpty()) {
			path += "/" + getName() + "-" + dateFormat.format(date) + ".srs";
		} else {
			path += "/redsqirl-backup-" + dateFormat.format(date) + ".srs";
		}
		return path;
	}
	
	public String buildHelpFileContent() throws RemoteException{
		String ans = "";
		ans +="<!DOCTYPE html>\n";
		ans +="<html>\n";
	    ans +="<head>\n";
	    ans +="\t<title>"+getName()+" Help</title>\n";
		ans +="</head>\n";
		ans +="<body>\n";
		ans += getComment()+"\n\n";
		ans +="</body>\n";
		ans +="</html>";
		return ans;
	}

	protected String saveXmlOnLocal(File file, Boolean privilege) throws RemoteException{
		String error = null;
		String pattern= "sa_[a-z0-9]*";
		logger.info(name+ "  "+name.matches(pattern));
		logger.info(name+ "  "+name.startsWith("sa_"));
		if(!name.matches(pattern) || !name.startsWith("sa_")){
			error = "The super action name should be alpha-numeric only and start by 'sa_'. '"+name+"'" ;
		}else{

		}

		if(error == null){
			setName(name);
			try {
				logger.debug("Save xml: " + file.getAbsolutePath());
				file.getParentFile().mkdirs();
				Document doc = null;
				try{
					doc = saveInXML();
					addOutputNamesInXml(doc);
					addSecurityInXml(doc,privilege);
				}catch(IOException e){
					error = e.getMessage();
				}

				if (error == null) {
					logger.debug("write the file...");
					// write the content into xml file
					logger.info("Check Null text nodes...");
					XmlUtils.checkForNullTextNodes(doc.getDocumentElement(), "");
					TransformerFactory transformerFactory = TransformerFactory
							.newInstance();
					Transformer transformer = transformerFactory.newTransformer();
					DOMSource source = new DOMSource(doc);
					StreamResult result = new StreamResult(file);
					logger.debug(4);
					transformer.transform(source, result);
					logger.debug(5);

					saved = true;
					logger.debug("file saved successfully");
				}
			} catch (Exception e) {
				error = LanguageManagerWF.getText("workflow.writeXml",
						new Object[] { e.getMessage() });
				logger.error(error);
				for (int i = 0; i < 6 && i < e.getStackTrace().length; ++i) {
					logger.error(e.getStackTrace()[i].toString());
				}
				try {
					logger.info("Attempt to delete " + file.getAbsolutePath());
					file.delete();
				} catch (Exception e1) {
				}
			}
		}
		Log.flushAllLogs();

		return error;
	}

	protected void addOutputNamesInXml(Document doc) throws RemoteException{
		Element root = doc.getDocumentElement();
		SubWorkflowOutput saOut = new SubWorkflowOutput();
		Iterator<DataFlowElement> itDfe = getElement().iterator();
		Element globOutEl = doc.createElement("sa_global_outputs");
		
		logger.debug("Index all the outputs");
		while(itDfe.hasNext()){
			DataFlowElement dfe = itDfe.next();
			if(dfe.getName().equals(saOut.getName())){
				try{
					String idOutput = dfe.getComponentId();
					DataFlowElement lastRunnableEl =  dfe.getAllInputComponent().get(0);
					logger.debug("Index the output "+idOutput+" (element "+lastRunnableEl.getComponentId()+")");
					Map<String,List<DataFlowElement>> mapContainingOutput = lastRunnableEl.getOutputComponent();
					boolean found = false;
					Iterator<String> elMapKeyIt = mapContainingOutput.keySet().iterator();
					String cur = null;
					logger.debug("search output among "+mapContainingOutput.keySet());
					while(!found && elMapKeyIt.hasNext()){
						cur = elMapKeyIt.next();
						found = mapContainingOutput.get(cur).contains(dfe);
					}
					if(found){
						Element outEl = doc.createElement("global_output");
						Attr attrElId = doc.createAttribute("element");
						attrElId.setValue(lastRunnableEl.getComponentId());
						outEl.setAttributeNode(attrElId);

						Attr attrOutId = doc.createAttribute("output");
						attrOutId.setValue(cur);
						outEl.setAttributeNode(attrOutId);

						Attr attrName = doc.createAttribute("name");
						attrName.setValue(idOutput);
						outEl.setAttributeNode(attrName);
						
						globOutEl.appendChild(outEl);
					}else{
						logger.warn("Output dataset for "+idOutput + " not found.");
					}
				}catch(Exception e){
					logger.error("Not expected exception: "+e,e);
				}
			}
		}
		root.appendChild(globOutEl);
	}
	
	protected void addSecurityInXml(Document doc,Boolean privs) throws RemoteException{
		Element root = doc.getDocumentElement();
		Element security = doc.createElement("security");
		
		Element privilegeEl = doc.createElement("privilege");
		if(privs == null){
			privilegeEl.appendChild(doc.createTextNode("editable"));
		}else if(!privs){
			privilegeEl.appendChild(doc.createTextNode("runnable"));
		}else{
			privilegeEl.appendChild(doc.createTextNode("licensed"));
		}
		security.appendChild(privilegeEl);
		
		
		root.appendChild(security);
		
	}
	
	public File getInstalledMainFile(){
		File xmlFile = new File(WorkflowPrefManager.getPathUserSuperAction(System.getProperty("user.name")),name);
		logger.debug("User path to search "+name+": "+xmlFile.getPath());
		if(!xmlFile.exists()){
			xmlFile = new File(WorkflowPrefManager.getPathSysSuperAction(),name);
			logger.debug("System path to search "+name+": "+xmlFile.getPath());
		}
		return xmlFile;
	}
	
	@Override
	public String read(String filePath){
		String error = null;
		boolean valid = true;
		
		Boolean licensed = getPrivilege();
		String swLicense= null;
		
		
		String softwareLicenseKey = ProjectID.get().trim().replaceAll("[^A-Za-z0-9]", "").toLowerCase();
		String softwareLicense = null;
		
		String name = null;
		try {
			name = getName();
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		
		if(licensed !=null && licensed){
			File licenseP = new File(
					WorkflowPrefManager.getPathSystemLicence());
			Properties props = new Properties();
			String licenseKey=null ;
			if (licenseP.exists()) {
				try {
					licenseKey = name;
					props.load(new FileInputStream(licenseP));
					licenseKey = licenseKey.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
					swLicense =  props.getProperty(licenseKey);
					softwareLicense = props.getProperty(softwareLicenseKey);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				logger.info(props.toString());
				
				logger.info(licenseKey);
			}
			
			if(swLicense ==null){
					return "There was an error when trying to run "+name;
			}else{
				Decrypter dec = new Decrypter();
				dec.decrypt_key_module(swLicense);

				Map<String,String> keyModule = new HashMap<String,String>();
				keyModule.put(dec.userName,System.getProperty("user.name"));
				keyModule.put(dec.name,name);
				keyModule.put(dec.license,softwareLicense);
				
				valid = dec.validateAllValuesModule(keyModule);
			}
			
		}
			
		if(!valid){
			error = "License is invalid for "+name;
			return error;
		}
			
		//check license 
		//return error if license invalid
		
		return super.read(filePath);
	}

	public String readMetaData() throws Exception{

		tmpOutput = new LinkedHashMap<LinkedList<String>,DFEOutput>();
		outputSuperAction = new LinkedHashMap<String,DFEOutput>();
		inputSuperAction = new LinkedHashMap<String, DFELinkProperty>();

		String error = null;
		
		File xmlFile = getInstalledMainFile();
		
		if(!xmlFile.exists()){
			return "Super Action "+name+" not found";
		}

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xmlFile);

		doc.getDocumentElement().normalize();
		Element docEL = doc.getDocumentElement();
		// Needs to do two reading,
		// for the element and there id
		// for link all the element
		logger.debug("loads elements...");

		SubWorkflowInput saIn = new SubWorkflowInput();
		Map<String,String> globOut = new LinkedHashMap<String,String>();

		
		NodeList compList = doc.getElementsByTagName("sa_global_outputs");
		String security = doc.getElementsByTagName("security").item(0).getChildNodes().item(0).getTextContent();
		logger.info("Security "+security);
		
		if(security.equals("editable")){
			this.privilege = null;
		}else if(security.equals("runnable")){
			this.privilege = new Boolean(false);
		}else if (security.equals("licensed")){
			this.privilege = new Boolean(true);
		}
		
		logger.info(getName()+" privilege '"+this.privilege+"'");
		
		for (int temp = 0; temp < compList.getLength() && error == null; ++temp) {

			Node compCur = compList.item(temp);

			NodeList dataList = ((Element) compCur)
					.getElementsByTagName("global_output");

			for (int ind = 0; ind < dataList.getLength() && error == null; ++ind) {
				Node outputCur = dataList.item(ind);

				String elId = outputCur.getAttributes().getNamedItem("element")
						.getNodeValue();
				String elOutput = outputCur.getAttributes().getNamedItem("output")
						.getNodeValue();
				String elName = outputCur.getAttributes().getNamedItem("name")
						.getNodeValue();
				globOut.put(elId+","+elOutput,elName);
			}

		}


		// Init element
		compList = doc.getElementsByTagName("component");
		for (int temp = 0; temp < compList.getLength() && error == null; ++temp) {

			Node compCur = compList.item(temp);

			String name = compCur.getAttributes().getNamedItem("name")
					.getNodeValue();

			String id = compCur.getAttributes().getNamedItem("id")
					.getNodeValue();
			if(name.startsWith("sa_")){
				addElement(name, id);
				SuperAction saCur = (SuperAction) getElement(id);
				Map<LinkedList<String>, DFEOutput> saTmpOutput = saCur.getTmpOutput();
				Iterator<Entry<LinkedList<String>,DFEOutput>> itSaCur = saTmpOutput.entrySet().iterator();
				while(itSaCur.hasNext()){
					Entry<LinkedList<String>,DFEOutput> curTmpOut = itSaCur.next();
					LinkedList<String> newList = new LinkedList<String>();
					newList.addAll(curTmpOut.getKey());
					newList.addFirst(id);
					tmpOutput.put(newList, curTmpOut.getValue());
				}
				removeElement(id);
			}else if(name.equals(saIn.getName())){
				addElement(name, id);
				SubWorkflowInput inCur = (SubWorkflowInput) getElement(id); 
				error = inCur.readValuesXml(
						((Element) compCur)
						.getElementsByTagName("interactions").item(0));
				if(error == null){
					error = inCur.updateOut();
				}
				if(error == null){
					inputSuperAction.put(id, inCur.getSubWorkflowInput());
				}

			}
		}

		// Link and data
		for (int temp = 0; temp < compList.getLength() && error == null; ++temp) {

			Node compCur = compList.item(temp);
			String compId = compCur.getAttributes().getNamedItem("id")
					.getNodeValue();


			// Save element
			logger.debug("loads dataset: " + compId);
			NodeList dataList = ((Element) compCur)
					.getElementsByTagName("data");
			for (int ind = 0; ind < dataList.getLength() && error == null; ++ind) {
				Node dataCur = dataList.item(ind);

				String dataName = dataCur.getAttributes()
						.getNamedItem("name").getNodeValue();
				String typeName = dataCur.getAttributes()
						.getNamedItem("typename").getNodeValue();
				DFEOutput cur = DataOutput.getOutput(typeName);
				if (cur != null) {
					logger.debug("loads state dataset: " + dataName);
					cur.read((Element) dataCur);
				} else {
					error = LanguageManagerWF.getText(
							"workflow.read_unknownType",
							new Object[] { typeName });
					error = "Unknown typename " + typeName;
				}
				if(!getComponentIds().contains(compId)){
					//Not input
					if(globOut.keySet().contains(compId+","+dataName)){
						//Is glob output
						outputSuperAction.put(globOut.get(compId+","+dataName), cur);
					}else{
						//Is tmp data
						LinkedList<String> newL = new LinkedList<String>();
						newL.add(compId);
						newL.add(dataName);
						tmpOutput.put(newL, cur);
					}
				}
			}

		}


		return error;
	}

	public void updateSuperActionTmpOutputs(Map<LinkedList<String>,DFEOutput> saOutputs) throws RemoteException{

		Iterator<Entry<LinkedList<String>,DFEOutput>> it = saOutputs.entrySet().iterator();
		Map<String,Map<LinkedList<String>,DFEOutput>> ans = 
				new LinkedHashMap<String,Map<LinkedList<String>,DFEOutput>>();
		while(it.hasNext()){
			Entry<LinkedList<String>,DFEOutput> cur = it.next();
			LinkedList<String> curKey = cur.getKey();
			if(curKey.size() > 2){
				String act = curKey.getFirst();
				if(!ans.containsKey(act)){
					ans.put(act,new LinkedHashMap<LinkedList<String>,DFEOutput>());
				}
				LinkedList<String> newList = new LinkedList<String>();
				newList.addAll(curKey);
				newList.removeFirst();
				ans.get(act).put(newList, cur.getValue());
			}
		}
		Iterator<String> itStr = ans.keySet().iterator();
		while(itStr.hasNext()){
			String cur = itStr.next();
			((SuperElement) getElement(cur)).setTmpOutput(ans.get(cur));
		}

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


	/**
	 * @return the outputSuperAction
	 */
	public final Map<String, DFEOutput> getOutputSuperAction() {
		return outputSuperAction;
	}


	/**
	 * @param outputSuperAction the outputSuperAction to set
	 */
	public final void setOutputSuperAction(Map<String, DFEOutput> outputSuperAction) {
		this.outputSuperAction = outputSuperAction;
	}


	/**
	 * @return the inputSuperAction
	 */
	public final Map<String, DFELinkProperty> getInputSuperAction() {
		return inputSuperAction;
	}


	/**
	 * @param inputSuperAction the inputSuperAction to set
	 */
	public final void setInputSuperAction(
			Map<String, DFELinkProperty> inputSuperAction) {
		this.inputSuperAction = inputSuperAction;
	}


	/**
	 * @return the privilege
	 */
	public final Boolean getPrivilege() {
		return privilege;
	}

}
