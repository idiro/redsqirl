package idiro.workflow.server;

import idiro.hadoop.NameNodeVar;
import idiro.utils.RandomString;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.interfaces.DataFlow;
import idiro.workflow.server.interfaces.DataFlowElement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class that manages a workflow.
 * 
 * A workflow is a DAG graph of process.
 * Each process can be an input or output of another.
 * 
 * The class is done with a GUI back-end in mind, several
 * options are there to be interfaced.
 * 
 * @author etienne
 *
 */
public class Workflow extends UnicastRemoteObject implements DataFlow{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3290769501278834001L;

	/**
	 * The logger.
	 */
	private static Logger logger = Logger.getLogger(Workflow.class);

	/**
	 * Avoid to call reflexive method again and again
	 */
	protected static Map<String,String> flowElement = new LinkedHashMap<String,String>(); 


	/**
	 * Menu of action,
	 * each tab title is link to a list of action name (@see {@link DataflowAction#getName()})
	 */
	protected Map<String,List<String[]>> menuWA;

	/**
	 * The current Action in the workflow
	 */
	protected LinkedList<DataFlowElement> element = new LinkedList<DataFlowElement>();


	protected String name,
	oozieJobId;

	protected boolean saved = false;

	public Workflow() throws RemoteException{
		super();
	}

	public Workflow(String name) throws RemoteException{
		super();
		this.name = name;
	}

	/**
	 * Load the icon menu.
	 * 
	 * The icon menu is read from a directory.
	 * All the directory are tab, and
	 * each line in each file is an action.
	 * The files can be commented by '#' on the
	 * beginning of each line.
	 *  
	 * @return null if ok, or all the error found
	 * 
	 */
	public String loadMenu(){
		String error = "";
		File menuDir = new File(WorkflowPrefManager.pathIconMenu.get());
		File[] children = menuDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return !pathname.getName().startsWith(".");
			}
		});
		menuWA = new LinkedHashMap<String, List<String[]> >();

		Map<String, String> nameWithClass;
		try {
			nameWithClass = getAllWANameWithClassName();

			for(int i = 0; i < children.length;++i){
				if(children[i].isFile()){
					LinkedList<String[]> new_list = new LinkedList<String[]>();
					BufferedReader br = new BufferedReader(new FileReader(children[i]));
					String line;
					while((line = br.readLine()) != null){
						try{
							if(!line.isEmpty() && !line.startsWith("#")){
								if(nameWithClass.get(line) != null){
									DataFlowElement dfe = (DataFlowElement) 
											Class.forName(nameWithClass.get(line)).newInstance();

									String[] parameters = new String[3];
									parameters[0] = line;
									parameters[1] = dfe.getImage();
									parameters[2] = dfe.getHelp();
									new_list.add(parameters);
								}else{
									logger.warn("unknown workflow action '"+line+"'");
								}
							}
						}catch(Exception e){
							error +="Fail to load class "+line+"\n";
						}
					}
					br.close();
					menuWA.put(children[i].getName(), new_list);
				}
			}

		} catch (Exception e) {
			error +="Fail to load classes or read menu files\n";
		}

		if(error.isEmpty()){
			error = null;
		}else{
			logger.error(error);
		}

		return error;
	}

	/**
	 * Save the icon menu.
	 * 
	 * @return null if ok, or all the error found
	 * 
	 */
	public String saveMenu(){

		String error = "";

		File menuDir = new File(WorkflowPrefManager.pathIconMenu.get());

		try {
			FileUtils.cleanDirectory(menuDir);

			for (Entry<String,List<String[]>> e : menuWA.entrySet()){
				File file = new File(menuDir.getAbsolutePath()+"/"+e.getKey());

				PrintWriter s = new PrintWriter(file);
				for (String[] string : e.getValue()){
					s.println(string[0]);
				}
				s.close();
			}
		} catch (Exception e) {
			error +="Fail to write menu files\n";
		}

		if(error.isEmpty()){
			error = null;
		}else{
			logger.error(error);
		}

		return error;
	}

	/**
	 * Check if a workflow is correct or not.
	 * Returns a string with a description of the error 
	 * if it is not correct.
	 * @return the error.
	 * @throws RemoteException 
	 */
	public String check() throws RemoteException{
		String error = "";
		//Need to check that we have a DAG
		try{
			topoligicalSort();
		}catch(Exception e){
			return e.getMessage();
		}


		//Need to check element one per one
		// We don't check an element that depends on an element that fails
		Iterator<DataFlowElement> iconIt = element.iterator();
		List<DataFlowElement> listToNotCheck = new LinkedList<DataFlowElement>();
		while(iconIt.hasNext()){
			DataFlowElement wa = iconIt.next();
			boolean toCheck = true;
			Iterator<DataFlowElement> noCheckIt = listToNotCheck.iterator();
			List<DataFlowElement> curAllInput = wa.getAllInputComponent(); 
			while(noCheckIt.hasNext() && toCheck){
				toCheck = curAllInput.contains(noCheckIt.next());
			}
			if(!toCheck){
				listToNotCheck.add(wa);
			}else{
				String locError = wa.checkEntry();
				if(locError != null){
					error += wa.getComponentId()+"\t"+ locError+"\n";
					listToNotCheck.add(wa);
				}else{
					wa.updateOut();
				}
			}
		}

		if(error.isEmpty()){
			error = null;
		}
		return error;
	}

	/**
	 * Run a workflow
	 * @return
	 * @throws Exception 
	 */
	@Override
	public String run() throws RemoteException{
		return run(getIds(element));
	}


	public String run(List<String> dataFlowElement) throws RemoteException{
		String error = check();

		LinkedList<DataFlowElement> elsIn = new LinkedList<DataFlowElement>();
		if(dataFlowElement.size() < element.size()){
			Iterator<DataFlowElement> itIn = getEls(dataFlowElement).iterator();
			while(itIn.hasNext()){
				DataFlowElement cur = itIn.next();
				elsIn = getAllWithoutDuplicate(elsIn, getItAndAllElementsNeeded(cur));
			}
		}else{
			elsIn.addAll(getEls(dataFlowElement));
		}

		//Run only what have not been calculated in the workflow.
		List<DataFlowElement> toRun = new LinkedList<DataFlowElement>();
		Iterator<DataFlowElement> itE = elsIn.descendingIterator();
		while(itE.hasNext() && error == null){
			DataFlowElement cur = itE.next();
			if(!toRun.contains(cur)){
				boolean haveTobeRun = false;
				List<DataFlowElement> outAllComp = cur.getAllOutputComponent();
				Collection<DFEOutput> outData = cur.getDFEOutput().values();
				Map<String,List<DataFlowElement>> outComp = cur.getOutputComponent();
				if(outAllComp.size() == 0){
					//Check if one element buffered/recorded exist or not
					//if all elements are temporary and not exist calculate the element
					Iterator<DFEOutput> itOutData = outData.iterator();
					int nbTemporary = 0;
					while(itOutData.hasNext() && !haveTobeRun){
						DFEOutput outC = itOutData.next();
						if( (outC.getSavingState() != SavingState.TEMPORARY )&&
								!outC.isPathExists()){
							haveTobeRun = true;
						}else if(outC.getSavingState() == SavingState.TEMPORARY &&
								!outC.isPathExists()){
							++nbTemporary;
						}
					}
					if(nbTemporary == outData.size()){
						haveTobeRun = true;
					}

				}else{
					//Check if among the output several elements some are recorded/buffered and does not exist
					Iterator<DFEOutput> itOutData = outData.iterator();
					while(itOutData.hasNext() && !haveTobeRun){
						DFEOutput outC = itOutData.next();
						if( (outC.getSavingState() != SavingState.TEMPORARY )&&
								!outC.isPathExists()){
							haveTobeRun = true;
						}
					}
					if(!haveTobeRun){
						//Check if among the output several elements to run are in the list
						//Check if it is true the corresponded outputs is saved or not
						Iterator<String> searchOutIt = outComp.keySet().iterator();
						while(searchOutIt.hasNext() && !haveTobeRun){
							boolean foundOne = false;
							String searchOut = searchOutIt.next();
							Iterator<DataFlowElement> outCIt = outComp.get(searchOut).iterator();
							while(outCIt.hasNext() && !foundOne){
								foundOne = toRun.contains(outCIt.next());
							}
							if(foundOne ){
								haveTobeRun = !cur.getDFEOutput().get(searchOut).isPathExists();
							}
						}
					}
				}
				//Never run an element that have no action
				if(cur.getOozieAction() == null){
					haveTobeRun = false;
				}
				if(haveTobeRun){
					//If this element have to be run
					//if one element exist and one recorded/buffered does not send an error
					cur.cleanDataOut();
					boolean errorToSend = false;
					Iterator<DFEOutput> itOutData = outData.iterator();
					while(itOutData.hasNext() && !haveTobeRun){
						DFEOutput outC = itOutData.next();
						errorToSend = outC.isPathExists();
					}
					if(errorToSend){
						error = cur.getComponentId()
								+": Element have to be run but one or several elements are recorded";
					}else{
						toRun.add(cur);
					}
				}

			}
		}

		if(error == null && toRun.isEmpty()){
			error = "Everything is up to date.";
		}

		if(error == null){
			try {
				setOozieJobId(OozieManager.getInstance().run(this,
						toRun));
			} catch (Exception e) {
				error = e.getMessage();
			}
		}

		if(error != null){
			logger.error(error);
		}

		return error;
	}

	public String cleanProject() throws RemoteException{
		String err = "";
		Iterator<DataFlowElement> it = element.iterator();
		while(it.hasNext()){
			DataFlowElement cur = it.next();
			String curErr = cur.cleanDataOut();
			if(curErr != null){
				err = err + "Error in the element "+cur.getComponentId()+": \n"
						+curErr+"\n";
			}
		}
		if(err.isEmpty()){
			err = null;
		}
		return err;
	}

	/**
	 * Null if it is not running, or the status if it runs
	 * @return
	 */
	public boolean isrunning(){
		OozieClient wc = OozieManager.getInstance().getOc();
		boolean running = false;
		try{
			if(oozieJobId != null && 
					wc.getJobInfo(oozieJobId).getStatus() == 
					org.apache.oozie.client.WorkflowJob.Status.RUNNING){
				running = true;
			}
		}catch(Exception e){
			logger.error(e.getMessage());
		}
		return running;
	}

	/**
	 * Save the xml part of a workflow @see {@link Workflow#save(Path)}
	 * @param file the xml file to write in.
	 * @return null if OK, or a description of the error.
	 */
	public String save(final String filePath){
		String error = null;
		try{
			String[] path = filePath.split("/");
			String fileName = path[path.length-1];
			String tempPath = WorkflowPrefManager.pathUserPref.get()+"/tmp/"+fileName;
			File file = new File(tempPath);
			logger.debug("Save xml: "+file.getAbsolutePath());
			file.getParentFile().mkdirs();
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("workflow");
			doc.appendChild(rootElement);

			Element jobId = doc.createElement("job-id");
			String jobIdContent = getOozieJobId();
			if(jobIdContent == null){
				jobIdContent = "";
			}
			jobId.appendChild(doc.createTextNode(jobIdContent));
			rootElement.appendChild(jobId);

			Iterator<DataFlowElement> it = element.iterator();
			while(it.hasNext() && error == null){
				DataflowAction cur = (DataflowAction) it.next();
				logger.debug("write: "+cur.getComponentId());

				Element component = doc.createElement("component");

				//attribute				
				logger.debug("add attributes...");
				Attr attrId = doc.createAttribute("id");
				attrId.setValue(cur.componentId);
				component.setAttributeNode(attrId);

				logger.debug("name: "+cur.getName());
				Attr attrName = doc.createAttribute("name");
				attrName.setValue(cur.getName());
				component.setAttributeNode(attrName);

				//Position
				logger.debug("add positions...");
				Element position = doc.createElement("position");
				Element x = doc.createElement("x");
				x.appendChild(doc.createTextNode(String.valueOf(cur.getPosition().x)));
				position.appendChild(x);
				Element y = doc.createElement("y");
				y.appendChild(doc.createTextNode(String.valueOf(cur.getPosition().y)));
				position.appendChild(y);
				component.appendChild(position);

				//Saving data
				Map<String,DFEOutput> saveMap = cur.getDFEOutput();
				if(saveMap != null){
					logger.debug("find state of the outputs...");
					Iterator<String> itStr = saveMap.keySet().iterator();
					while(itStr.hasNext()){
						String outName = itStr.next();
						if(saveMap.get(outName) != null){
							logger.debug("save data named "+outName);
							Element data = doc.createElement("data");

							Attr attrDataName = doc.createAttribute("name");
							attrDataName.setValue(outName);
							data.setAttributeNode(attrDataName);
							logger.debug("Enter in write...");
							saveMap.get(outName).write(doc,data);

							component.appendChild(data);
						}
					}
				}

				//Input
				logger.debug("add inputs...");
				Element inputs = doc.createElement("inputs");
				Map<String,List<DataFlowElement>> inComp = cur.getInputComponent();
				if(inComp != null){
					logger.debug("inputs not null");
					Iterator<String> itS = inComp.keySet().iterator();
					logger.debug("inputs size "+inComp.size());
					while(itS.hasNext()){
						String inputName = itS.next();
						logger.debug("save "+inputName+"...");
						if(inComp.get(inputName) != null){
							Iterator<DataFlowElement> wa = inComp.get(inputName).iterator();
							while(wa.hasNext()){
								Element input = doc.createElement("input");
								String inId = wa.next().getComponentId();
								logger.debug("add "+inputName+" "+ inId);

								Element nameEl = doc.createElement("name");
								nameEl.appendChild(doc.createTextNode(inputName));
								input.appendChild(nameEl);

								Element id = doc.createElement("id");
								id.appendChild(doc.createTextNode(inId));
								input.appendChild(id);

								inputs.appendChild(input);
							}
						}
					}
				}
				component.appendChild(inputs);

				//Output
				logger.debug("add outputs...");
				Element outputs = doc.createElement("outputs");
				Map<String,List<DataFlowElement>> outComp = cur.getOutputComponent();
				if(outComp != null){
					logger.debug("outputs not null");
					Iterator<String> itS = outComp.keySet().iterator();
					logger.debug("outputs size "+outComp.size());
					while(itS.hasNext()){
						String outputName = itS.next();
						logger.debug("save "+outputName+"...");
						Iterator<DataFlowElement> wa = outComp.get(outputName).iterator();
						logger.debug(2);
						while(wa.hasNext()){
							logger.debug(3);
							Element output = doc.createElement("output");
							logger.debug(31);
							String outId = wa.next().getComponentId();
							logger.debug("add "+outputName+" "+ outId);
							Element nameEl = doc.createElement("name");
							nameEl.appendChild(doc.createTextNode(outputName));
							output.appendChild(nameEl);

							Element id = doc.createElement("id");
							id.appendChild(doc.createTextNode(outId));
							output.appendChild(id);

							outputs.appendChild(output);
						}
						logger.debug(4);

					}
				}
				component.appendChild(outputs);

				//Element
				Element interactions = doc.createElement("interactions");
				error = cur.writeValuesXml(doc, interactions);
				component.appendChild(interactions);


				rootElement.appendChild(component);
			}
			if(error == null){
				logger.debug("write the file...");
				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(file);
				transformer.transform(source, result);

				FileSystem fs = NameNodeVar.getFS();
				fs.moveFromLocalFile(new Path(tempPath), new Path(filePath));
				fs.close();

				saved = true;
				logger.debug("file saved successfully");
			}
		} catch (Exception e) {
			error = "Fail to save the xml file"+e;

			logger.error(error);
			logger.error(e.getMessage());
		}

		return error;
	}

	public void cleanUpBackup() throws IOException{
		String path = WorkflowPrefManager.getUserProperty(WorkflowPrefManager.user_backup);
		String numberBackup = WorkflowPrefManager.getUserProperty(WorkflowPrefManager.user_nb_backup);
		int nbBackup = 25;
		if(numberBackup != null){
			try{
				nbBackup = Integer.valueOf(numberBackup);
				if(nbBackup < 0){
					nbBackup = 25;
				}
			}catch(Exception e){}
		}

		FileSystem fs = NameNodeVar.getFS();
		//FileStatus stat = fs.getFileStatus(new Path(path));
		FileStatus[] fsA = fs.listStatus(new Path(path), new PathFilter() {

			@Override
			public boolean accept(Path arg0) {
				return arg0.getName().matches(".*[0-9]{14}.xml$");
			}
		});
		if(fsA.length > nbBackup){
			int numberToRemove = fsA.length - nbBackup;
			Map<Long,Path> pathToRemove = new HashMap<Long,Path>();
			for(FileStatus stat: fsA){
				if(pathToRemove.size() < numberToRemove){
					pathToRemove.put(stat.getModificationTime(), stat.getPath());
				}else{
					Iterator<Long> it = pathToRemove.keySet().iterator();
					Long min = it.next();
					while(it.hasNext()){
						Long cur = it.next();
						if(min > cur ){
							cur = min;
						}
					}
					if(min > stat.getModificationTime()){
						pathToRemove.remove(min);
						pathToRemove.put(stat.getModificationTime(), stat.getPath());
					}
				}
			}
			for(Path pathDel: pathToRemove.values()){
				fs.delete(pathDel,false);
			}
		}
		fs.close();
	}

	public void backup() throws RemoteException{
		String path = WorkflowPrefManager.getUserProperty(WorkflowPrefManager.user_backup);
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		if(path == null || path.isEmpty()){
			path = "/user/"+System.getProperty("user.name")+"/idm-backup";
		}
		try{
			FileSystem fs = NameNodeVar.getFS();
			fs.mkdirs(new Path(path));
			fs.close();
		}catch(Exception e){
			logger.warn(e.getMessage());
			logger.warn("Fail creating backup directory");
		}
		if(getName() != null && !getName().isEmpty()){
			path += "/"+getName()+"-"+dateFormat.format(date)+".xml";
		}else{
			path += "/idm-backup-"+dateFormat.format(date)+".xml";
		}
		String error = save(path);

		try{
			if(error != null){
				FileSystem fs = NameNodeVar.getFS();
				fs.delete(new Path(path),false);
				fs.close();
			}
			cleanUpBackup();
		}catch(Exception e){
			logger.warn(e.getMessage());
			logger.warn("Fail cleaning up backup directory");
		}

	}

	public boolean isSaved(){
		return saved;
	}

	/**
	 * Reads the xml part of a workflow @see {@link Workflow#read(Path)}
	 * @param file the xml file to read from.
	 * @return null if OK, or a description of the error.
	 */
	public String read(String filePath){
		String error = null;
		element.clear();

		try {
			String[] path = filePath.split("/");
			String fileName = path[path.length-1];
			String userName = System.getProperty("user.name");
			String tempPath = WorkflowPrefManager.pathUserPref.get()+"/tmp";
			FileSystem fs = NameNodeVar.getFS();
			fs.copyToLocalFile(new Path(filePath), new Path(tempPath));

			File xmlFile = new File(tempPath+"/"+fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();


			Node jobId = doc.getElementsByTagName("job-id").item(0);
			try{
				String jobIdContent = jobId.getChildNodes().item(0).getNodeValue();
				if(!jobIdContent.isEmpty()){
					setOozieJobId(jobIdContent);
				}
			}catch(Exception e){}

			//Needs to do two reading,
			//for the element and there id
			//for link all the element
			logger.debug("loads elements...");
			NodeList compList = doc.getElementsByTagName("component");
			//Init element
			for (int temp = 0; temp < compList.getLength(); ++temp) {

				Node compCur = compList.item(temp);

				String name = compCur.getAttributes().getNamedItem("name").getNodeValue();
				String id = compCur.getAttributes().getNamedItem("id").getNodeValue();


				int x = Integer.valueOf(
						((Element) (
								((Element)compCur).getElementsByTagName("position").item(0)
								)).getElementsByTagName("x").item(0).getChildNodes().item(0).getNodeValue()
						);
				int y = Integer.valueOf(
						((Element) (
								((Element)compCur).getElementsByTagName("position").item(0)
								)).getElementsByTagName("y").item(0).getChildNodes().item(0).getNodeValue()
						);
				logger.debug("create new Action: "+name+" "+id+": ("+x+","+y+")");
				addElement(name,id);
				getElement(id).setPosition(x,y);
				getElement(id).readValuesXml(((Element) compCur).getElementsByTagName("interactions").item(0));
			}

			//Link
			logger.debug("loads links...");
			for (int temp = 0; temp < compList.getLength(); ++temp) {

				Node compCur = compList.item(temp);
				String compId = compCur.getAttributes().getNamedItem("id").getNodeValue();

				logger.debug(compId+": input...");
				NodeList inList = ((Element) compCur).getElementsByTagName("inputs").item(0).getChildNodes();
				if(inList != null){
					for (int index = 0; index < inList.getLength(); index++) {
						logger.debug(compId+": input index "+index);
						Node inCur = inList.item(index);
						String nameIn = ((Element) inCur).getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();
						String id = ((Element) inCur).getElementsByTagName("id").item(0).getChildNodes().item(0).getNodeValue();

						getElement(compId).addInputComponent(nameIn, getElement(id));
					}
				}

				logger.debug(compId+": output...");
				NodeList outList = ((Element) compCur).getElementsByTagName("outputs").item(0).getChildNodes();
				if (getElement(compId).getDFEOutput() == null || getElement(compId).getDFEOutput().isEmpty()){
					getElement(compId).updateOut();
				}
				if(outList != null){
					for (int index = 0; index < outList.getLength(); index++) {
						try{
							logger.debug(compId+": output index "+index);
							Node outCur = outList.item(index);

							String nameOut = ((Element) outCur).getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();
							String id = ((Element) outCur).getElementsByTagName("id").item(0).getChildNodes().item(0).getNodeValue();

							getElement(compId).addOutputComponent(nameOut, getElement(id));
						}
						catch (Exception e){
							logger.error("Fail to load output");
							error = "Fail to load output";
						}
					}
				}

			}

			//Saved Element
			logger.debug("loads saved states...");
			for (int temp = 0; temp < compList.getLength(); ++temp) {

				Node compCur = compList.item(temp);
				String id = compCur.getAttributes().getNamedItem("id").getNodeValue();

				//Save element
				Map<String,DFEOutput> mapOutput = getElement(id).getDFEOutput();
				NodeList dataList = ((Element)compCur).getElementsByTagName("data");
				for(int ind = 0; ind < dataList.getLength(); ++ind){
					Node dataCur = dataList.item(ind);

					String dataName =  dataCur.getAttributes().getNamedItem("name").getNodeValue();
					mapOutput.get(dataName).read((Element)dataCur);
					if(mapOutput.get(dataName).getSavingState() != SavingState.RECORDED &&
							mapOutput.get(dataName).getPath() == null){
						mapOutput.get(dataName).generatePath(userName, id, dataName);
					}
				}
			}
			saved = true;

			//clean temporary files
			String tempPathCrc = WorkflowPrefManager.pathUserPref.get()+"/tmp/."+fileName+".crc";
			File tempCrc = new File(tempPathCrc);
			tempCrc.delete();
			xmlFile.delete();

		} catch (Exception e) {
			logger.error("Fail to read the xml file");
			error = "Fail to read the xml file";
		}

		return error;
	}

	/**
	 * Do sort of the workflow.
	 * 
	 * If the sort is successful, it is a DAG
	 * @return null if OK, or a description of the error.
	 * @throws RemoteException 
	 */
	public String topoligicalSort() throws RemoteException{
		String error = null;
		LinkedList<DataFlowElement> newList = new LinkedList<DataFlowElement>();

		LinkedList<DataFlowElement> queueList = new LinkedList<DataFlowElement>();
		Iterator<DataFlowElement> iconIt = element.iterator();
		while(iconIt.hasNext()){
			DataFlowElement cur = iconIt.next();
			if(cur.getInputComponent().values().size() == 0){
				queueList.add(cur);
			}
		}

		while(!queueList.isEmpty()){
			newList.add(queueList.removeFirst());
			iconIt = element.iterator();
			while(iconIt.hasNext()){
				DataFlowElement cur = iconIt.next();
				if(!newList.contains(cur) && !queueList.contains(cur)){
					Iterator<List<DataFlowElement>> it = 
							cur.getInputComponent().values().iterator();
					boolean allThere = true;
					while(it.hasNext() && allThere){
						allThere = newList.containsAll(it.next());
					}

					if(allThere){
						queueList.add(cur);
					}
				}
			}
		}
		if(newList.size() < element.size()){
			error = "The graph contains at least one cycle";
		}else{
			element = newList;
		}


		return error;
	}

	public String changeElementId(String oldId, String newId) throws RemoteException{
		String err = null;
		String regex = "[a-zA-Z]([A-Za-z0-9_]{0,15})";
		boolean found = false;
		if(!newId.matches(regex)){
			err = "The new id does not matches a name ("+regex+").";
		}
		if(! oldId.equals(newId)){
			Iterator<DataFlowElement> itA = element.iterator();
			while(itA.hasNext()&&!found){
				found = itA.next().getComponentId().equals(newId);
			}
			if(found){
				err = "The id '"+newId+"' is already used.";
			}else{
				DataFlowElement el = getElement(oldId);
				if(el == null){
					err = "Id '"+oldId+"' unknown.";
				}else{
					el.setComponentId(newId);
				}
			}
		}
		return err;
	}

	/**
	 * Add a WorkflowAction in the Workflow.
	 * The element is at the end of the workingWA list
	 * @param waName the name of the action @see {@link DataflowAction#getName()}
	 * @return null if OK, or a description of the error.
	 * @throws Exception 
	 */
	public String addElement(String waName) throws Exception{
		boolean found = false;
		String newId = null;
		int length = (int) (Math.log10(element.size()+1) + 2);

		while(newId == null){
			newId = "a" + RandomString.getRandomName(length,"1234567890");
			Iterator<DataFlowElement> itA = element.iterator();
			found = false;
			while(itA.hasNext()&&!found){
				found = itA.next().getComponentId().equals(newId);
			}
			if(found){
				newId = null;
			}

		}
		logger.debug("Attempt to add an element: "+waName+", "+newId);

		return addElement(waName,newId);
	}

	public String removeElement(String componentId) throws RemoteException, Exception{
		String error = null;
		DataFlowElement dfe = getElement(componentId);
		dfe.cleanThisAndAllElementAfter();
		for (Entry<String,List<DFEOutput>> dfeInput : dfe.getDFEInput().entrySet()){
			for (DataFlowElement inputComponent : dfe.getInputComponent().get(dfeInput.getKey())){
				for (Entry<String, List<DataFlowElement>> outputComponent : inputComponent.getOutputComponent().entrySet()){
					if (outputComponent.getValue().contains(dfe)){
						logger.info("Remove1 - "+outputComponent.getKey()+" "+inputComponent.getComponentId()+" "+dfeInput.getKey()+" "+dfe.getComponentId());
						error = this.removeLink(outputComponent.getKey(), inputComponent.getComponentId(), dfeInput.getKey(), dfe.getComponentId(), true);
					}

				}
			}
		}

		if (error != null && dfe.getDFEOutput() != null){
			for (Entry<String,DFEOutput> dfeOutput : dfe.getDFEOutput().entrySet()){
				for (DataFlowElement outputComponent : dfe.getOutputComponent().get(dfeOutput.getKey())){
					for (Entry<String, List<DataFlowElement>> inputComponent : outputComponent.getInputComponent().entrySet()){
						if (inputComponent.getValue().contains(dfe)){
							logger.info("Remove2 - "+dfeOutput.getKey()+" "+dfe.getComponentId()+" "+inputComponent.getKey()+" "+outputComponent.getComponentId());
							error = this.removeLink(dfeOutput.getKey(), dfe.getComponentId(), inputComponent.getKey(), outputComponent.getComponentId(), true);
						}

					}
				}
			}
		}

		element.remove(element.indexOf(dfe));
		return error;
	}

	/**
	 * Add a WorkflowAction in the Workflow.
	 * The element is at the end of the workingWA list
	 * @param waName the name of the action @see {@link DataflowAction#getName()}
	 * @param componentId the id of the new component.
	 * @return null if OK, or a description of the error.
	 * @throws Exception 
	 */
	protected String addElement(String waName, String componentId) throws Exception{
		String error = null;
		Map<String,String> namesWithClassName = null;
		try{
			namesWithClassName = getAllWANameWithClassName();
		}catch(Exception e){
			//This should not happend if the workflow has been initialised corretly
			error = "Fail to generate the WorkflowAction list: "+e.getMessage();
		}
		if(error == null){
			if( namesWithClassName.get(waName) == null){
				error = "Action '"+waName+"' does not exist";
			}else{
				try {
					logger.debug("initiate the action "+waName+" "+namesWithClassName.get(waName));
					DataFlowElement new_wa = (DataFlowElement) Class.forName(
							namesWithClassName.get(waName)).newInstance();
					logger.debug("set the componentId...");
					new_wa.setComponentId(componentId);
					logger.debug("Add the element to the list...");
					element.add( new_wa);
				} catch (Exception e) {
					logger.debug("exception...");
					error = e.getMessage();
					logger.debug(error);
				}
			}
		}
		if(error != null){
			logger.error(error);
			throw new Exception(error);
		}else{
			logger.debug("Add action: "+waName+" componentId: "+componentId);
		}
		return componentId;
	}

	/**
	 * Get the WorkflowAction corresponding to the componentId.
	 * 
	 * @param componentId the componentId @see {@link DataflowAction#componentId}
	 * @return a WorkflowAction object or null 
	 * @throws RemoteException 
	 */
	public DataFlowElement getElement(String componentId) throws RemoteException{
		Iterator<DataFlowElement> it = element.iterator();
		DataFlowElement ans = null;
		while(it.hasNext() && ans == null){
			ans = it.next();
			if(!ans.getComponentId().equals(componentId)){
				ans = null;
			}
		}
		if(ans == null){
			logger.debug("Component "+componentId+" not found");
		}
		return ans;
	}

	/**
	 * Remove a link.
	 * If the link creation imply a topological error it cancel it.
	 * To understand the nomenclature: out --> in
	 * 
	 * @param inName relation between the edge and the output vertex
	 * @param componentIdIn the output vertex id 
	 * @param outName relation between the edge and the input vertex
	 * @param componentIdOut the input vertex id 
	 * @return  null if OK, or a description of the error.
	 * @throws RemoteException 
	 */
	public String removeLink(String outName,
			String componentIdOut,
			String inName,
			String componentIdIn
			) throws RemoteException{
		return removeLink(outName,
				componentIdOut,
				inName,
				componentIdIn,
				false);
	}

	/**
	 * Add a link.
	 * If the link creation imply a topological error it cancel it.
	 * To understand the nomenclature: out --> in
	 * 
	 * @param inName relation between the edge and the output vertex
	 * @param componentIdIn the output vertex id 
	 * @param outName relation between the edge and the input vertex
	 * @param componentIdOut the input vertex id 
	 * @return null if OK, or a description of the error.
	 * @throws RemoteException 
	 */
	public String addLink(
			String outName,
			String componentIdOut,
			String inName,
			String componentIdIn
			) throws RemoteException{
		return addLink( 
				outName,
				componentIdOut,
				inName,
				componentIdIn,
				false);
	}

	/**
	 * Remove a link.
	 * To understand the nomenclature: out --> in
	 * 
	 * @param inName relation between the edge and the output vertex
	 * @param componentIdIn the output vertex id 
	 * @param outName relation between the edge and the input vertex
	 * @param componentIdOut the input vertex id 
	 * @param force if false cancel the action if it implies a topological error 
	 * @return  null if OK, or a description of the error.
	 * @throws RemoteException 
	 */
	public String removeLink( 
			String outName,
			String componentIdOut,
			String inName,
			String componentIdIn,
			boolean force) throws RemoteException{
		String error = null;
		DataFlowElement out = getElement(componentIdOut);
		DataFlowElement in = getElement(componentIdIn);

		if(out == null || in == null){
			error = "One of the element to link does not exist";
		}else{
			in.cleanThisAndAllElementAfter();
			out.removeOutputComponent(outName, in);
			error = in.removeInputComponent(inName, out);
			if(!force && error == null){
				error = topoligicalSort();
				if(error != null){
					addLink(outName,componentIdOut,inName,componentIdIn,true);
				}
			}
		}
		if(error != null){
			logger.debug("Error when removing link "+error);
		}
		return error;
	}

	/**
	 * Add a link.
	 * If the link creation imply a topological error it cancel it.
	 * To understand the nomenclature: out --> in
	 * 
	 * @param inName relation between the edge and the output vertex
	 * @param componentIdIn the output vertex id 
	 * @param outName relation between the edge and the input vertex
	 * @param componentIdOut the input vertex id 
	 * @param force if false cancel the action if it implies a topological error
	 * @return null if OK, or a description of the error.
	 * @throws RemoteException 
	 */
	public String addLink( 
			String outName,
			String componentIdOut,
			String inName,
			String componentIdIn,
			boolean force) throws RemoteException{

		String error = null;
		DataFlowElement out = getElement(componentIdOut);
		DataFlowElement in = getElement(componentIdIn);
		if(out == null || in == null){
			error = "One of the element to link does not exist";
		}else if(in.getInput().get(inName) == null){
			error = "The entry name "+inName+" does not exist in input";
		}else if(out.getDFEOutput().get(outName) == null){
			error = "The entry name "+outName+" does not exist in output";
		}else{
			if(force){
				out.addOutputComponent(outName, in);
				error = in.addInputComponent(inName, out);
			}else{
				if( ! in.getInput().get(inName).check(
						out.getDFEOutput().get(outName))
						){
					error = "The type of the edge is not compatible";
				}else{
					out.addOutputComponent(outName, in);
					error = in.addInputComponent(inName, out);
					if(error == null){
						error = topoligicalSort();
					}
					if(error != null){
						removeLink(outName,componentIdOut,inName,componentIdIn,true);
					}
				}
			}
		}
		if(error != null){
			logger.debug("Error when add link "+error);
		}
		return error;
	}

	public boolean check( 
			String outName,
			String componentIdOut,
			String inName,
			String componentIdIn) throws RemoteException{

		String error = null;
		DataFlowElement out = getElement(componentIdOut);
		DataFlowElement in = getElement(componentIdIn);
		if(out == null || in == null){
			error = "One of the element to link does not exist";
		}else if(in.getInput().get(inName) == null){
			error = "The entry name "+inName+" does not exist in input";
		}else if(out.getDFEOutput().get(outName) == null){
			error = "The entry name "+outName+" does not exist in output";
		}else if( ! in.getInput().get(inName).check(out.getDFEOutput().get(outName))){
			error = "The type of the edge is not compatible";
		}
		if(error != null){
			return false;
		}
		return true;
	}

	/**
	 * Get all the WorkflowAction available in the jars file.
	 * 
	 * To find the jars, the method use 
	 * @see {@link WorkflowPrefManager#getNonAbstractClassesFromSuperClass(String)}.
	 * 
	 * @return the dictionary: key name @see {@link DataflowAction#getName()} ; value the canonical class name.
	 * @throws Exception if one action cannot be load
	 */
	public Map<String,String> getAllWANameWithClassName() throws RemoteException, Exception{
		logger.debug("get all the Workflow actions");
		if(flowElement.isEmpty()){

			Iterator<String> actionClassName = 
					WorkflowPrefManager.getInstance().getNonAbstractClassesFromSuperClass(
							DataflowAction.class.getCanonicalName()).iterator();

			while(actionClassName.hasNext()){
				String className = actionClassName.next();
				try{
					DataflowAction wa = (DataflowAction) Class.forName(className).newInstance();
					flowElement.put(wa.getName(),className);
				}catch(Exception e){
					logger.error("Error instanciating class : "+className);
				}


			}
			logger.debug("WorkflowAction found : "+flowElement.toString());
		}
		return flowElement;
	}

	/**
	 * Get all the WorkflowAction available in the jars file.
	 * 
	 * To find the jars, the method use 
	 * @see {@link WorkflowPrefManager#getNonAbstractClassesFromSuperClass(String)}.
	 * 
	 * @return an array containing the name, image and help of the action
	 * @throws Exception if one action cannot be load
	 */
	public List<String[]> getAllWA() throws RemoteException{
		logger.debug("get all the Workflow actions");
		List<String[]> result = new LinkedList<String[]>();

		Iterator<String> actionClassName = 
				WorkflowPrefManager.getInstance().getNonAbstractClassesFromSuperClass(
						DataflowAction.class.getCanonicalName()).iterator();

		while(actionClassName.hasNext()){
			String className = actionClassName.next();
			DataflowAction wa;
			try {
				wa = (DataflowAction) Class.forName(className).newInstance();
				result.add(new String[]{wa.getName(), wa.getImage(), wa.getHelp()});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * @return the workingWA
	 */
	public List<DataFlowElement> getElement() {
		return element;
	}

	/**
	 * @return the last element of workingWA.
	 */
	public DataFlowElement getLastElement() {
		return element.getLast();
	}

	/**
	 * @return the menuWA
	 */
	public Map<String, List<String[]>> getMenuWA() {
		return menuWA;
	}

	/**
	 * @param menuWA the menuWA to set
	 */
	public void setMenuWA(Map<String, List<String[]>> menuWA) throws RemoteException{
		this.menuWA = menuWA;
	}

	public List<String> getComponentIds() throws RemoteException {
		List<String> ans = new LinkedList<String>();
		Iterator<DataFlowElement> it = element.iterator();
		while(it.hasNext()){
			ans.add(it.next().getComponentId());
		}
		return ans;
	}

	@Override
	public String getName() throws RemoteException {
		return name;
	}

	@Override
	public void setName(String name) throws RemoteException {
		this.name = name;
	}

	@Override
	public String getOozieJobId() throws RemoteException {
		return oozieJobId;
	}

	public void setOozieJobId(String oozieJobId){
		this.oozieJobId = oozieJobId;
	}

	protected List<String> getIds(List<DataFlowElement> els) throws RemoteException{
		List<String> ans = new ArrayList<String>(els.size());
		Iterator<DataFlowElement> it = els.iterator();
		while(it.hasNext()){
			ans.add(it.next().getComponentId());
		}
		return ans;
	}

	protected List<DataFlowElement> getEls(List<String> ids) throws RemoteException{
		if(ids == null){
			return new ArrayList<DataFlowElement>();
		}else{
			List<DataFlowElement> ans = new ArrayList<DataFlowElement>(ids.size());
			Iterator<String> it = ids.iterator();
			while(it.hasNext()){
				ans.add(getElement(it.next()));
			}
			return ans;
		}
	}

	protected LinkedList<DataFlowElement> getItAndAllElementsNeeded(DataFlowElement el) throws RemoteException{
		LinkedList<DataFlowElement> ans = new LinkedList<DataFlowElement>();
		ans.add(el);
		Iterator<DataFlowElement> it = el.getAllInputComponent().iterator();
		while(it.hasNext()){
			Iterator<DataFlowElement> itCur = getItAndAllElementsNeeded(it.next()).iterator();
			while(itCur.hasNext()){
				DataFlowElement cans = itCur.next();
				if(!ans.contains(cans)){
					ans.add(cans);
				}
			}
		}
		return ans;
	}

	protected LinkedList<DataFlowElement> getAllWithoutDuplicate(List<DataFlowElement> l1,
			List<DataFlowElement> l2){
		LinkedList<DataFlowElement> ans = new LinkedList<DataFlowElement>();
		ans.addAll(l1);
		Iterator<DataFlowElement> itCur = l2.iterator();
		while(itCur.hasNext()){
			DataFlowElement cans = itCur.next();
			if(!ans.contains(cans)){
				ans.add(cans);
			}
		}
		return ans;
	}

}
