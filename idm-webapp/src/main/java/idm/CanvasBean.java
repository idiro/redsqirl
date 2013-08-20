package idm;


import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.interfaces.DataFlow;
import idiro.workflow.server.interfaces.DataFlowElement;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.json.JSONArray;

public class CanvasBean extends BaseBean implements Serializable{

	
	private static Logger logger = Logger.getLogger(CanvasBean.class);
	private int countObj;
	private int countWf;
	private Entry entry;
	private String nameWorkflow;
	private DataFlow df;
//	private Map<String, String> idMap = new HashMap<String, String>();
	
	public void doNew(){

		logger.info("doNew");

	}

	public void doOpen(){

		logger.info("doOpen");

	}

	/** openCanvas
	 * 
	 * Methods to mount the first canvas
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	@PostConstruct
	public void openCanvas() {
		
		setCountObj(0);
		setNameWorkflow("canvas"+countWf);
		
		DataFlowInterface dfi;
		try {
			
			dfi = getworkFlowInterface();
			if(dfi != null && dfi.getWorkflow(getNameWorkflow()) == null){
				dfi.addWorkflow(getNameWorkflow());
				setDf(dfi.getWorkflow(getNameWorkflow()));
				logger.info("add new Workflow "+getNameWorkflow());
			}
			
		} catch (RemoteException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
	}
	
	public List<String[]> getHelpItens() throws Exception{
		if (getDf() == null){
			openCanvas();
		}
		DataFlow wf = getDf();
		wf.loadMenu();
		
		List<String[]> helpList = new ArrayList<String[]>();
		for (String[] e : wf.getAllWA()){
			helpList.add(new String[]{e[0], e[2]});
		}
		return helpList;
	}
	
	/** addElement
	 * 
	 * Method for add Element on canvas. set the new idElement on the element
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void addElement() {

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

		String nameElement = params.get("paramNameElement");

		String paramGroupID = params.get("paramGroupID");

		String posX = params.get("paramPosX");
		String posY = params.get("paramPosY");
		try {

			DataFlow df = getDf();
			
			String idElement = df.addElement(nameElement);
			
			df.getElement(idElement).setPosition(Double.valueOf(posX).intValue(), Double.valueOf(posY).intValue());

			setEntry(new Entry(idElement, paramGroupID));
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*public void updateValue(ActionEvent event){

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String nameElement = params.get("param1");

		logger.info("-> " + nameElement);
		setNameElement(nameElement);

		addElement();

	}*/

	/** addLink
	 * 
	 * Method for add Link for two elements
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void addLink() {
		
//		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
//		String idElementA = params.get("paramOutId");
//		String idElementB = params.get("paramInId");
//
//		try {
//
//			DataFlowInterface dfi = getworkFlowInterface();
//
//			DataFlow df = dfi.getWorkflow(getNameWorkflow());
//
//			DataFlowElement dfeObjA = df.getElement(getIdMap().get(idElementA));
//			DataFlowElement dfeObjB = df.getElement(getIdMap().get(idElementB));
//
//			
//			dfeObjB.getInput();
//			dfeObjA.getDFEOutput();
//			
//			df.addLink("output1", dfeObjA.getComponentId(), "input1", dfeObjB.getComponentId());
//
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

	}
	
//	/** addLink
//	 * 
//	 * Method for add Link for two elements
//	 * 
//	 * @return 
//	 * @author Igor.Souza
//	 */
//	public void getLinkPossibilities() {
//		
//		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
//		String idElementA = params.get("paramOutId");
//		String idElementB = params.get("paramInId");
//
//		List<String> result = new ArrayList<String>();
//		try {
//
//			DataFlowInterface dfi = getworkFlowInterface();
//
//			DataFlow df = dfi.getWorkflow(getNameWorkflow());
//
//			DataFlowElement dfeObjA = df.getElement(getIdMap().get(idElementA));
//			dfeObjA.
//			dfeObjA.updateOut();
//			DataFlowElement dfeObjB = df.getElement(getIdMap().get(idElementB));
//			
//			for (Map.Entry<String, DFELinkProperty> entryOutput : dfeObjB.getInput().entrySet()){
//				for (Map.Entry<String, DFEOutput> entryInput : dfeObjA.getDFEOutput().entrySet()){
//					if (entryOutput.getValue().check(entryInput.getValue().getClass())){
//						result.add(entryOutput.getKey()+" -> "+entryInput.getKey());
//					}
//				}
//			}
//
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}

	/** removeLink
	 * 
	 * Method for remove Link for two elements
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void removeLink() {

		try {

			DataFlow df = getDf();
			
			df.removeLink("", "wlwmwntOut", "", "elementIN");

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/** load
	 * 
	 * Method to save a workflow
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void load() {
		
		closeWorkflow();
		
		String path = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("pathFile");
		
		logger.info("load "+path);
		
		setNameWorkflow(path);

		DataFlowInterface dfi;
		try {
			dfi = getworkFlowInterface();
			
			dfi.addWorkflow(getNameWorkflow());
			DataFlow df = dfi.getWorkflow(getNameWorkflow());
			df.getElement();
			logger.info(df.read(path));
			df.getElement();
			setDf(df);
		} catch (Exception e) {
			logger.info("Error saving workflow");
			e.printStackTrace();
		}
	}
	
	/** save
	 * 
	 * Method to save the workflow
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void save() {

		String path = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("pathFile");
		
		try {
			logger.info("save workflow in "+path);
			String msg = getDf().save(path);
			logger.info(msg);
		} catch (Exception e) {
			logger.info("Error saving workflow");
			e.printStackTrace();
		}
	}
	
	/** closeWorkflow
	 * 
	 * Method to close a workflow
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException 
	 */
	public void closeWorkflow() {
		
		logger.info("closeWorkflow");
		
		countWf++;
		setNameWorkflow("canvas"+countWf);
		try {
			getworkFlowInterface().addWorkflow(getNameWorkflow());
			setDf(getworkFlowInterface().getWorkflow(getNameWorkflow()));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public int getCountObj() {
		return countObj;
	}

	public void setCountObj(int countObj) {
		this.countObj = countObj;
	}
	
	public Entry getEntry() {
		return entry;
	}

	public void setEntry(Entry entry) {
		this.entry = entry;
	}
	
	public DataFlow getDf() {
		return df;
	}

	public void setDf(DataFlow df) {
		this.df = df;
	}
	
	public String getNameWorkflow() {
		return nameWorkflow;
	}

	public void setNameWorkflow(String nameWorkflow) {
		this.nameWorkflow = nameWorkflow;
	}
	
//	public Map<String, String> getIdMap() {
//	return idMap;
//}
//
//public void setIdMap(Map<String, String> idMap) {
//	this.idMap = idMap;
//}
	
	public String getPositions() throws Exception{
		JSONArray json = new JSONArray();
		for (DataFlowElement e : getDf().getElement()){
			json.put(new Object[]{e.getComponentId(), e.getName(), e.getImage(), e.getX(), e.getY()});
		}
		return json.toString();
	}
	
}