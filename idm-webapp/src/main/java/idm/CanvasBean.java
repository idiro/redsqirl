package idm;


import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.interfaces.DataFlow;
import idiro.workflow.server.interfaces.DataFlowElement;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.json.JSONArray;

public class CanvasBean extends BaseBean implements Serializable{

	
	private static Logger logger = Logger.getLogger(CanvasBean.class);
	private List<SelectItem> linkPossibilities = new ArrayList<SelectItem>();
	private String selectedLink;
	private int countObj;
	private int countWf;
	private String nameWorkflow;
	private DataFlow df;
	private String paramOutId;
	private String paramInId;
	private String paramNameLink;
	private String[] result;

	
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
		getworkFlowInterface().addWorkflow("wf help");
		
		DataFlow wf = getworkFlowInterface().getWorkflow("wf help");
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

		try {
			DataFlow df = getDf();
			String idElement = df.addElement(nameElement);
			
			setResult(new String[]{idElement, paramGroupID});
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/** removeElement
	 * 
	 * Method to remove Element on canvas.
	 * 
	 * @return 
	 * @author Marcos.Freitas
	 */
	public void removeElement() {

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

		String paramGroupID = params.get("paramGroupID");

		try {

			DataFlow df = getDf();
			logger.info("Remove element "+paramGroupID);
			df.removeElement(paramGroupID);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/** updatePosition
	 * 
	 * Method for update the position of an Element
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void updatePosition() {
		logger.info("updatePosition");
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

		String paramGroupID = params.get("paramGroupID");
		logger.info(paramGroupID);
		String posX = params.get("paramPosX");
		String posY = params.get("paramPosY");
		try {
			
			DataFlow df = getDf();
			df.getElement(paramGroupID).setPosition(Double.valueOf(posX).intValue(), Double.valueOf(posY).intValue());

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/** addLink
	 * 
	 * Method for add Link for two elements
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void addLink() {
		logger.info("addLink");
		String idElementA = getParamOutId();
		String idElementB = getParamInId();
		
		String nameElementA = getSelectedLink().split(" -> ")[0];
		String nameElementB = getSelectedLink().split(" -> ")[1];
		
		logger.info("AddLink A: "+idElementA+" - "+nameElementA);
		logger.info("AddLink B: "+idElementB+" - "+nameElementB);

		try {

			DataFlowInterface dfi = getworkFlowInterface();

			DataFlow df = dfi.getWorkflow(getNameWorkflow());

			DataFlowElement dfeObjA = df.getElement(idElementA);
			DataFlowElement dfeObjB = df.getElement(idElementB);

			df.addLink(nameElementA, dfeObjA.getComponentId(), nameElementB, dfeObjB.getComponentId());
			
			setResult(new String[]{getParamNameLink(), nameElementA, nameElementB});


		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void updateLinkPossibilities() {
		
		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String idElementA = params.get("paramOutId");
		String idElementB = params.get("paramInId");
		
		try {
			linkPossibilities = new ArrayList<SelectItem>();
			DataFlowInterface dfi = getworkFlowInterface();
			DataFlow df = dfi.getWorkflow(getNameWorkflow());

			DataFlowElement dfeObjA = df.getElement(idElementA);
			DataFlowElement dfeObjB = df.getElement(idElementB);
			
			for (Map.Entry<String, DFELinkProperty> entryInput : dfeObjB.getInput().entrySet()){
				for (Map.Entry<String, DFEOutput> entryOutput : dfeObjA.getDFEOutput().entrySet()){
					if (df.check(entryOutput.getKey(), dfeObjA.getComponentId(), entryInput.getKey(), dfeObjB.getComponentId())){
						linkPossibilities.add(new SelectItem(entryOutput.getKey()+" -> "+entryInput.getKey()));
					}
				}
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/** removeLink
	 * 
	 * Method for remove Link for two elements
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void removeLink() {
		logger.info("Remove link");

		try {
			Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
			String idElementA = params.get("paramOutId");
			String idElementB = params.get("paramInId");
			String nameElementA = params.get("paramOutName");
			String nameElementB = params.get("paramInName");
			
			getDf().removeLink(nameElementA, idElementA, nameElementB, idElementB);

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
		logger.info("save");

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
	
	public String getParamOutId() {
		return paramOutId;
	}

	public void setParamOutId(String paramOutId) {
		this.paramOutId = paramOutId;
	}

	public String getParamInId() {
		return paramInId;
	}

	public void setParamInId(String paramInId) {
		this.paramInId = paramInId;
	}
	
	public String getParamNameLink() {
		return paramNameLink;
	}

	public void setParamNameLink(String paramNameLink) {
		this.paramNameLink = paramNameLink;
	}
	
	public String[] getResult() {
		return result;
	}

	public void setResult(String[] result) {
		this.result = result;
	}
	
	public List<SelectItem> getLinkPossibilities() {
		return linkPossibilities;
	}

	public void setLinkPossibilities(List<SelectItem> linkPossibilities) {
		this.linkPossibilities = linkPossibilities;
	}
	
	public String getSelectedLink() {
		return selectedLink;
	}

	public void setSelectedLink(String selectedLink) {
		this.selectedLink = selectedLink;
	}

	public String[] getPositions() throws Exception{
		JSONArray jsonElements = new JSONArray();
		for (DataFlowElement e : getDf().getElement()){
			jsonElements.put(new Object[]{e.getComponentId(), e.getName(), e.getImage(), e.getX(), e.getY()});
		}
		
		JSONArray jsonLinks = new JSONArray();
		for (DataFlowElement e : getDf().getElement()){
			for (Map.Entry<String, List<DataFlowElement>> entry : e.getInputComponent().entrySet()){
				for (DataFlowElement dfe : entry.getValue()){
					jsonLinks.put(new Object[]{dfe.getComponentId(), e.getComponentId()});
				}
			}
		}
		
		return new String[]{jsonElements.toString(), jsonLinks.toString()};
	}
}