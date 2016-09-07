package com.redsqirl.workflow.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.idiro.utils.RandomString;
import com.redsqirl.workflow.server.action.SyncSinkFilter;
import com.redsqirl.workflow.server.enumeration.PathType;
import com.redsqirl.workflow.server.enumeration.SavingState;
import com.redsqirl.workflow.server.interfaces.CoordinatorTimeConstraint;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.DataFlowCoordinator;
import com.redsqirl.workflow.server.interfaces.DataFlowCoordinatorVariables;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class WorkflowCoordinator extends UnicastRemoteObject implements DataFlowCoordinator{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2983015342985143960L;
	private static Logger logger = Logger.getLogger(WorkflowCoordinator.class);
	
	protected CoordinatorTimeConstraint timeCondition = new WfCoordTimeConstraint();
	protected String name;
	protected List<DataFlowElement> elements = new LinkedList<DataFlowElement>();
	protected WfCoordVariables variables = new WfCoordVariables();
	protected Date executionTime = null;
	
	public class WfDefaultConstraint implements DataFlowCoordinator.DefaultConstraint{
		protected CoordinatorTimeConstraint constraint;
		protected int offset;
		
		public WfDefaultConstraint(CoordinatorTimeConstraint constraint, int offset) {
			super();
			this.constraint = constraint;
			this.offset = offset;
		}
		public final CoordinatorTimeConstraint getConstraint() {
			return constraint;
		}
		public final void setConstraint(CoordinatorTimeConstraint constraint) {
			this.constraint = constraint;
		}
		public final int getOffset() {
			return offset;
		}
		public final void setOffset(int offset) {
			this.offset = offset;
		}
	}
	
	public WorkflowCoordinator() throws RemoteException {
		super();
	}
	
	public WorkflowCoordinator(String name) throws RemoteException {
		super();
		this.name = name;
	}
	
	
	public String saveInXml(Document doc, Element rootElement) throws RemoteException{
		String error = null;
		{
			Element elName = doc.createElement("name");
			elName.appendChild(doc.createTextNode(name));
			rootElement.appendChild(elName);
		}
		{
			Element elTimeConstraint = doc.createElement("time-condition");
			timeCondition.write(doc, elTimeConstraint);
			rootElement.appendChild(elTimeConstraint);
		}
		try{
			Element elTime = doc.createElement("execution-time");
			elTime.appendChild(doc.createTextNode( Long.valueOf(executionTime.getTime()).toString()));
			rootElement.appendChild(elTime);
		}catch(Exception e){}
		
		if(error == null){
			Element elConfiguration = doc.createElement("configuration");
			error = variables.saveInXml(doc, elConfiguration);
			rootElement.appendChild(elConfiguration);
		}
		
		if(error == null){
			Element elComp = doc.createElement("components");
			error = saveElements(doc,elComp);
			rootElement.appendChild(elComp);
		}
		
		return error;
	}
	
	
	public String saveElements(Document doc, Element rootElement) throws RemoteException {
		String error = null;
		Iterator<DataFlowElement> it = elements.iterator();
		while (it.hasNext() && error == null) {
			DataflowAction cur = (DataflowAction) it.next();
			logger.debug("write: " + cur.getComponentId());

			Element component = doc.createElement("component");

			// attribute
			logger.debug("add attributes...");
			Attr attrId = doc.createAttribute("id");
			attrId.setValue(cur.componentId);
			component.setAttributeNode(attrId);

			logger.debug("name: " + cur.getName());
			Attr attrName = doc.createAttribute("name");
			attrName.setValue(cur.getName());
			component.setAttributeNode(attrName);

			// Comment
			logger.debug("add positions...");
			Element commentEl = doc.createElement("comment");
			commentEl.appendChild(doc.createTextNode(cur.getComment()));
			component.appendChild(commentEl);

			//Oozie Action Id
			Element oozieActionNamesEl = doc.createElement("oozeactionnames");
			Iterator<String> itOozieElements = cur.getLastRunOozieElementNames().iterator();
			while(itOozieElements.hasNext()){
				String curOozieElement = itOozieElements.next();
				Element oozieActionNameEl = doc.createElement("oozeactionname");
				oozieActionNameEl.appendChild(doc.createTextNode(curOozieElement));
				oozieActionNamesEl.appendChild(oozieActionNameEl);
			}
			component.appendChild(oozieActionNamesEl);

			// Position
			logger.debug("add positions...");
			Element position = doc.createElement("position");
			Element x = doc.createElement("x");
			x.appendChild(doc.createTextNode(String.valueOf(cur.getPosition().x)));
			position.appendChild(x);
			Element y = doc.createElement("y");
			y.appendChild(doc.createTextNode(String.valueOf(cur.getPosition().y)));
			position.appendChild(y);
			component.appendChild(position);

			// Saving data
			Map<String, DFEOutput> saveMap = cur.getDFEOutput();
			if (saveMap != null) {
				logger.debug("find state of the outputs...");
				Iterator<String> itStr = saveMap.keySet().iterator();
				while (itStr.hasNext()) {
					String outName = itStr.next();
					if (saveMap.get(outName) != null) {
						logger.debug("save data named " + outName);
						Element data = doc.createElement("data");

						Attr attrDataName = doc.createAttribute("name");
						attrDataName.setValue(outName);
						data.setAttributeNode(attrDataName);

						Attr attrTypeName = doc.createAttribute("typename");
						attrTypeName.setValue(saveMap.get(outName)
								.getTypeName());
						data.setAttributeNode(attrTypeName);

						logger.debug("Enter in write...");
						saveMap.get(outName).write(doc, data);

						component.appendChild(data);
					}
				}
			}

			// Input
			logger.debug("add inputs...");
			Element inputs = doc.createElement("inputs");
			Map<String, List<DataFlowElement>> inComp = cur.getInputComponent();
			if (inComp != null) {
				logger.debug("inputs not null");
				Iterator<String> itS = inComp.keySet().iterator();
				logger.debug("inputs size " + inComp.size());
				while (itS.hasNext()) {
					String inputName = itS.next();
					logger.debug("save " + inputName + "...");
					if (inComp.get(inputName) != null) {
						Iterator<DataFlowElement> wa = inComp.get(inputName)
								.iterator();
						while (wa.hasNext()) {
							Element input = doc.createElement("input");
							String inId = wa.next().getComponentId();
							logger.debug("add " + inputName + " " + inId);

							Attr attrNameEl = doc.createAttribute("name");
							attrNameEl.setValue(inputName);
							input.setAttributeNode(attrNameEl);

							Element id = doc.createElement("id");
							id.appendChild(doc.createTextNode(inId));
							input.appendChild(id);

							inputs.appendChild(input);
						}
					}
				}
			}
			component.appendChild(inputs);

			// Output
			logger.debug("add outputs...");
			Element outputs = doc.createElement("outputs");
			Map<String, List<DataFlowElement>> outComp = cur
					.getOutputComponent();
			if (outComp != null) {
				logger.debug("outputs not null");
				Iterator<String> itS = outComp.keySet().iterator();
				logger.debug("outputs size " + outComp.size());
				while (itS.hasNext()) {
					String outputName = itS.next();
					logger.debug("save " + outputName + "...");
					Iterator<DataFlowElement> wa = outComp.get(outputName)
							.iterator();
					logger.debug(2);
					while (wa.hasNext()) {
						logger.debug(3);
						Element output = doc.createElement("output");
						logger.debug(31);
						String outId = wa.next().getComponentId();
						logger.debug("add " + outputName + " " + outId);

						Attr attrNameEl = doc.createAttribute("name");
						attrNameEl.setValue(outputName);
						output.setAttributeNode(attrNameEl);

						Element id = doc.createElement("id");
						id.appendChild(doc.createTextNode(outId));
						output.appendChild(id);

						outputs.appendChild(output);
					}
					logger.debug(4);

				}
			}
			component.appendChild(outputs);

			// Element
			Element interactions = doc.createElement("interactions");
			error = cur.writeValuesXml(doc, interactions);
			component.appendChild(interactions);

			rootElement.appendChild(component);
		}
		return error;
	}
	
	public void readInMeta(Document doc, Element parent){
		try{
			name = parent.getElementsByTagName("name").item(0)
					.getChildNodes().item(0).getNodeValue();
		}catch(Exception e){
			name = RandomString.getRandomName(8);
		}
		try{
			timeCondition.read((Element) parent.getElementsByTagName("time-condition").item(0));
		}catch(Exception e){
		}
		try{
			executionTime = new Date(Long.valueOf(parent.getElementsByTagName("execution-time").item(0)
					.getChildNodes().item(0).getNodeValue()));
		}catch(Exception e){
		}
		
		try{
			variables.readInXml(doc, (Element) parent.getElementsByTagName("configuration").item(0));
		}catch(Exception e){
		}
	}
	
	public String readInXml(Document doc, Element parent, DataFlow df) throws Exception{
		String error = null;
		readInMeta(doc, parent);
		
		if(error == null){
			error = readElements(doc,parent,df);
		}
		return error;
	}
	
	public String readElements(Document doc, Element parent, DataFlow df) throws Exception{
		String error = null;
		Workflow wf = (Workflow) df;
		
		NodeList compList = parent.getElementsByTagName("component");
		// Init element
		for (int temp = 0; temp < compList.getLength() && error == null; ++temp) {

			Node compCur = compList.item(temp);

			String name = compCur.getAttributes().getNamedItem("name")
					.getNodeValue();

			String id = compCur.getAttributes().getNamedItem("id")
					.getNodeValue();

			String compComment = "";
			try {
				compComment = ((Element) compCur)
						.getElementsByTagName("comment").item(0)
						.getChildNodes().item(0).getNodeValue();
			} catch (Exception e) {
			}
			

			int x = Integer.valueOf(((Element) (((Element) compCur)
					.getElementsByTagName("position").item(0)))
					.getElementsByTagName("x").item(0).getChildNodes().item(0)
					.getNodeValue());
			int y = Integer.valueOf(((Element) (((Element) compCur)
					.getElementsByTagName("position").item(0)))
					.getElementsByTagName("y").item(0).getChildNodes().item(0)
					.getNodeValue());
			logger.debug("create new Action: " + name + " " + id + ": (" + x
					+ "," + y + ")");
			wf.addElement(name, id,this);

			getElement(id).setPosition(x, y);
			getElement(id).setComment(compComment);
			
			Set<String> lastRunOozieElements = new LinkedHashSet<String>();
			try{
				NodeList compOozieElements= ((Element) ((Element) compCur).getElementsByTagName("oozeactionnames").item(0))
						.getElementsByTagName("oozeactionname");
				for (int oozieElIdx = 0; oozieElIdx < compOozieElements.getLength() && error == null; ++oozieElIdx) {
					Node compOozieCur = compOozieElements.item(oozieElIdx);
					lastRunOozieElements.add(compOozieCur.getChildNodes().item(0).getNodeValue());
				}
			}catch(Exception e){
				try {
					lastRunOozieElements.add(((Element) compCur)
							.getElementsByTagName("oozeactionid").item(0)
							.getChildNodes().item(0).getNodeValue());
				} catch (Exception e2) {
				}
			}
			getElement(id).setLastRunOozieElementNames(lastRunOozieElements);
			
			error = getElement(id).readValuesXml(
					((Element) compCur).getElementsByTagName("interactions")
							.item(0));

		}

		return error;
	}
	
	public String readInXmlLinks(Document doc, Element parent, DataFlow df, List<String> pathInUse,boolean runs) throws Exception{

		// Link and data
		String error = null;
		String warn = null;
		NodeList compList = parent.getElementsByTagName("component");
		logger.debug("loads links...");
		for (int temp = 0; temp < compList.getLength() && error == null; ++temp) {

			Node compCur = compList.item(temp);
			String compId = compCur.getAttributes().getNamedItem("id")
					.getNodeValue();
			DataFlowElement el = getElement(compId);
			Map<String, List<DataFlowElement>> elInputComponent = el
					.getInputComponent();
			Map<String, List<DataFlowElement>> elOutputComponent = el
					.getOutputComponent();

			logger.debug(compId + ": input...");
			NodeList inList = ((Element) ((Element) compCur)
					.getElementsByTagName("inputs").item(0)).getElementsByTagName("input");
			if (inList != null) {
				for (int index = 0; index < inList.getLength() && error == null; index++) {
					logger.debug(compId + ": input index " + index);
					Node inCur = inList.item(index);
					String nameIn = inCur.getAttributes().getNamedItem("name")
							.getNodeValue();
					String id = ((Element) inCur).getElementsByTagName("id")
							.item(0).getChildNodes().item(0).getNodeValue();

					String locWarn = el.addInputComponent(nameIn, df.getElement(id));
					if (locWarn != null) {
						logger.warn(locWarn);
						locWarn = null;
						List<DataFlowElement> lwa = elInputComponent
								.get(nameIn);
						if (lwa == null) {
							lwa = new LinkedList<DataFlowElement>();
							elInputComponent.put(nameIn, lwa);
						}
						lwa.add(getElement(id));
					}
				}
			}

			// Save element
			logger.debug("loads dataset: " + compId);
			Map<String, DFEOutput> mapOutput = el.getDFEOutput();
			NodeList dataList = ((Element) compCur)
					.getElementsByTagName("data");
			for (int ind = 0; ind < dataList.getLength() && error == null; ++ind) {
				Node dataCur = dataList.item(ind);

				String dataName = dataCur.getAttributes().getNamedItem("name")
						.getNodeValue();
				String typeName = dataCur.getAttributes()
						.getNamedItem("typename").getNodeValue();
				DFEOutput cur = DataOutput.getOutput(typeName);
				if (cur != null) {
					mapOutput.put(dataName, cur);
					logger.debug("loads state dataset: " + dataName);
					cur.read((Element) dataCur);
					if (!SavingState.RECORDED.equals(cur.getSavingState())){
						if(cur.getPath() == null || 
							!cur.isPathAutoGeneratedForUser(compId, dataName)){
							cur.generatePath(compId,dataName);
						}else if(pathInUse.contains(mapOutput.get(dataName).getPath())) {
							if(runs){
								warn = LanguageManagerWF.getText("workflow.run_pathconflict");
							}else{
								cur.generatePath(compId,dataName);
							}
						}
						
					}
				} else {
					error = LanguageManagerWF.getText(
							"workflow.read_unknownType",
							new Object[] { typeName });
					error = "Unknown typename " + typeName;
				}

			}

			logger.debug(compId + ": output...");
			NodeList outList = ((Element) ((Element) compCur)
					.getElementsByTagName("outputs").item(0)).getElementsByTagName("output");
			if (outList != null) {
				for (int index = 0; index < outList.getLength()
						&& error == null; index++) {
					try {
						logger.debug(compId + ": output index " + index);
						Node outCur = outList.item(index);

						String nameOut = outCur.getAttributes()
								.getNamedItem("name").getNodeValue();
						String id = ((Element) outCur)
								.getElementsByTagName("id").item(0)
								.getChildNodes().item(0).getNodeValue();

						String locWarn = el.addOutputComponent(nameOut, df.getElement(id));
						if (locWarn != null) {
							logger.warn(locWarn);
							locWarn = null;
							List<DataFlowElement> lwa = elOutputComponent
									.get(nameOut);
							if (lwa == null) {
								lwa = new LinkedList<DataFlowElement>();
								elOutputComponent.put(nameOut, lwa);
							}
							lwa.add(getElement(id));
						}
					} catch (Exception e) {
						error = LanguageManagerWF
								.getText("workflow.read_failLoadOut");
						logger.error(error,e);
					}
				}
			}

		}
		
		if(error == null){
			error = warn;
		}
		
		return error;
	}
	

	@Override
	public List<DataFlowElement> getElements() throws RemoteException {
		return elements;
	}
	
	@Override
	public List<String> getComponentIds() throws RemoteException{
		List<String> ans = new LinkedList<String>();
		Iterator<DataFlowElement> it = elements.iterator();
		while(it.hasNext()){
			ans.add(it.next().getComponentId());
		}
		return ans;
	}
	
	/**
	 * Get the WorkflowAction corresponding to the componentId.
	 * 
	 * @param componentId
	 *            the componentId @see {@link DataflowAction#componentId}
	 * @return a WorkflowAction object or null
	 * @throws RemoteException
	 */
	public DataFlowElement getElement(String componentId)
			throws RemoteException {
		Iterator<DataFlowElement> it = elements.iterator();
		DataFlowElement ans = null;
		while (it.hasNext() && ans == null) {
			ans = it.next();
			if (!ans.getComponentId().equals(componentId)) {
				ans = null;
			}
		}
		if (ans == null) {
			logger.debug("Component " + componentId + " not found");
		}
		return ans;
	}

	@Override
	public String addElement(DataFlowElement dfe) throws RemoteException {
		//If error or not linked
		elements.add(dfe);
		dfe.setCoordinatorName(name);
		return null;
	}
	
	@Override
	public void merge(DataFlowCoordinator coord) throws RemoteException{
		Iterator<DataFlowElement> it = coord.getElements().iterator();
		while(it.hasNext()){
			addElement(it.next());
		}
		variables.addAll(coord.getVariables());
	}
	
	@Override
	public DataFlowCoordinator split(List<DataFlowElement> dfe) throws RemoteException {
		DataFlowCoordinator dfC = new WorkflowCoordinator(dfe.get(0).getComponentId());
		dfC.getVariables().addAll(variables);
		Iterator<DataFlowElement> it = dfe.iterator();
		while(it.hasNext()){
			DataFlowElement cur = it.next();
			dfC.addElement(cur);
			elements.remove(cur);
		}
		return dfC;
	}

	@Override
	public String removeElement(DataFlowElement dfe) throws RemoteException {
		elements.remove(dfe);
		return null;
	}

	@Override
	public String getName() throws RemoteException {
		return name;
	}

	@Override
	public void setName(String name) throws RemoteException {
		this.name = name;
		Iterator<DataFlowElement> it = elements.iterator();
		while(it.hasNext()){
			it.next().setCoordinatorName(name);
		}
	}

	@Override
	public CoordinatorTimeConstraint getTimeCondition() throws RemoteException {
		return timeCondition;
	}
	
	public DefaultConstraint getDefaultTimeConstraint(DataFlow df) throws RemoteException {
		Iterator<DataFlowElement> itDfe = elements.iterator();
		CoordinatorTimeConstraint minCT = null;
		int offset = 0;
		while(itDfe.hasNext()){
			DataFlowElement cur = itDfe.next();
			
			logger.debug("Element "+cur.getComponentId());
			Iterator<DFEOutput> itOutputs = cur.getDFEOutput().values().iterator();
			while(itOutputs.hasNext()){
				DFEOutput datasetCur = itOutputs.next();
				CoordinatorTimeConstraint curTimeConstraint = null;
				int tmpOffset = 0;
				if(PathType.TEMPLATE.equals(datasetCur.getPathType())){
					curTimeConstraint = datasetCur.getFrequency();
				}else if(PathType.MATERIALIZED.equals(datasetCur.getPathType())){
					List<DataFlowElement> inputsDfe = cur.getAllInputComponent();
					if(inputsDfe.isEmpty()){
						curTimeConstraint = datasetCur.getFrequency();
					}else if(!inputsDfe.get(0).getCoordinatorName().equals(getName())){
						curTimeConstraint = df.getCoordinator(inputsDfe.get(0).getCoordinatorName()).getTimeCondition();
						if(curTimeConstraint.getUnit() == null){
							DefaultConstraint prevConstraint = df.getCoordinator(inputsDfe.get(0).getCoordinatorName()).getDefaultTimeConstraint(df);
							curTimeConstraint = prevConstraint.getConstraint();
							if(prevConstraint.getOffset() > 0){
								tmpOffset = prevConstraint.getOffset();
							}
						}
						tmpOffset += datasetCur.getNumberMaterializedPath() - 1;
					}
				}
				if(curTimeConstraint != null){
					if(minCT == null){
						minCT = curTimeConstraint;
						offset = tmpOffset;
					}else{
						minCT = WfCoordTimeConstraint.getMostFrequent(minCT,curTimeConstraint);
						if(minCT == curTimeConstraint){
							offset = tmpOffset;
						}
					}
				}
			}
			
		}
		return new WfDefaultConstraint(minCT,offset);
	}

	@Override
	public DataFlowCoordinatorVariables getVariables() throws RemoteException{
		return variables;
	}

	public final Date getExecutionTime() {
		return executionTime;
	}

	public final void setExecutionTime(Date executionTime) {
		this.executionTime = executionTime;
	}

}
