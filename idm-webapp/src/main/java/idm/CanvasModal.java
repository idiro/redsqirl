package idm;

import idiro.utils.Tree;
import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.connect.interfaces.DataStore.ParamProperty;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.interfaces.DFEPage;
import idiro.workflow.server.interfaces.DataFlow;
import idiro.workflow.server.interfaces.DataFlowElement;
import idm.useful.MessageUseful;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.ajax4jsf.model.KeepAlive;
import org.apache.log4j.Logger;
import org.json.JSONArray;

/** CanvasModal
 * 
 * Class to class control of the dynamic form
 * 
 * @author Igor.Souza
 */
@KeepAlive
public class CanvasModal extends BaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(CanvasModal.class);

	private String list = "";
	private List<SelectItem> listItens = new ArrayList<SelectItem>();
	private List<Entry> listFunctions = new ArrayList<Entry>();
	private List<Entry> listFields = new ArrayList<Entry>();
	private List<Entry> listFunctionsType = new ArrayList<Entry>();
	private String command = "";
	private String nameWorkflow;
	private String nameElement;
	private String groupID;
	private Entry entry;
	private DataFlowInterface dfi;
	private DataFlow df;
	private List<DFEPage> listPage = new ArrayList<DFEPage>();
	private int listPosition;
	private DFEPage page;
	private String pageTitle;
	private String pageLegend;
	private String lastPage = "N";
	private String firstPage = "S";
	private int listPageSize;
	private List<DynamicForm> dynamicFormList = new ArrayList<DynamicForm>();
//	private Map<String, String> idMap = new HashMap<String, String>();

	private Map<String, String> nameValueFeature = new HashMap<String, String>();
	private Map<String, String> nameValueListGrid = new HashMap<String, String>();
	private String pathBrowser = "";
	private ArrayList<ItemList> listGrid = new ArrayList<ItemList>();
	private ArrayList<ItemList> listFeature = new ArrayList<ItemList>();
	private DataFlowElement dfe;
	

	/** getKeyAsListNameValue
	 * 
	 * Method to retrieve the list of files
	 * 
	 * @return List<String>
	 * @author Igor.Souza
	 */
	public List<String> getKeyAsListNameValueListGrid(){
		return new ArrayList<String>(nameValueListGrid.keySet());
	}
	
	public List<String> getKeyAsListNameValueFeature(){
		return new ArrayList<String>(nameValueFeature.keySet());
	}


	/** nextPage
	 * 
	 * Methods to control the sequence of screens
	 * 
	 * @return 
	 * @author Igor.Souza
	 * @throws RemoteException 
	 */
	public void nextPage() throws RemoteException {

		logger.info("nextPage ");

		String error = checkNextPage();
		if(error.length() > 1){
			MessageUseful.addErrorMessage(error);
		}else{
			
			setListPosition(getListPosition()+1);

			setPage(getListPage().get(getListPosition()));

			if(getListPageSize() -1 > getListPosition()){
				setLastPage("N");
			}else{
				setLastPage("S");
			}

			if(getListPosition() == 0){
				setFirstPage("S");
			}else{
				setFirstPage("N");
			}

			mountInteractionForm();
			
		}

	}

	/** checkNextPage
	 * 
	 * Method to check the fields before change to the next page
	 * 
	 * @return 
	 * @author Igor.Souza
	 * @throws RemoteException 
	 */
	public String checkNextPage() throws RemoteException {

		StringBuffer error = new StringBuffer();
		
		for (int i = 0; i < getDynamicFormList().size(); i++) {
			
			DynamicForm dynamicF = getDynamicFormList().get(i);
			DFEInteraction dfi = getPage().getInteractions().get(i);
			
			if(dynamicF.getDisplayType().equals(DisplayType.list)){

				logger.info("value -> " + dynamicF.getSelectedListOptions());
				dynamicF.getTree().getFirstChild("list").getFirstChild("output").removeAllChildren();
				dynamicF.getTree().getFirstChild("list").getFirstChild("output").add(dynamicF.getSelectedListOptions());
				
				dfi.getTree().getFirstChild("list").getFirstChild("output").removeAllChildren();
				dfi.getTree().getFirstChild("list").getFirstChild("output").add(dynamicF.getSelectedListOptions());
				
			}
			
			getPage().getInteractions().set(i, dfi);
			String e = dfi.check();
			logger.info("error interaction -> " + e);
			if(e != null){
				error.append(e);
				error.append(System.getProperty("line.separator"));
			}
		}
		
		String e = getPage().checkPage();
		logger.info("error page -> " + e);
		if(e != null){
			error.append(e);
			error.append(System.getProperty("line.separator"));
		}
		
		getDfe().getPageList().set(getListPosition(), getPage());
		
		return error.toString();
	}

	/** previous
	 * 
	 * Methods to control the sequence of screens
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException 
	 */
	public void previousPage() throws RemoteException {

		logger.info("previousPage ");

		setListPosition(getListPosition()-1);

		setPage(getListPage().get(getListPosition()));

		if(getListPageSize() -1 > getListPosition()){
			setLastPage("N");
		}else{
			setLastPage("S");
		}

		if(getListPosition() == 0){
			setFirstPage("S");
		}else{
			setFirstPage("N");
		}

		mountInteractionForm();

	}

	/** close
	 * 
	 * Methods to control the sequence of screens
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void close() {

		logger.info("close ");

	}

	/** start
	 * 
	 * Methods to start the control of sequence of screens
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	@PostConstruct
	public void start() {
		logger.info("starCanvasModal");
		setNameWorkflow("canvas1");
		
		try {

			if(getDfi() == null){
				setDfi(getworkFlowInterface());
			}
			if(getDf() == null){
				setDf(getDfi().getWorkflow(getNameWorkflow()));
			}

		} catch (RemoteException e) {
			logger.error(e.getMessage());
		}


	}

	/** openTextEditor
	 * 
	 * Methods to mount the dynamic form
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	//@PostConstruct
	public void openCanvasModal() {

		//setNameWorkflow("canvas1");

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

		String nameElement = params.get("paramNameElement");
		setNameElement(nameElement);

		logger.info("open element id " + getNameElement());

		try {

			//DataFlowInterface dfi =  getworkFlowInterface();

			//DataFlow df = dfi.getWorkflow(getNameWorkflow());

			//setNameElement("Source");
			//String idElement = df.addElement(getNameElement());

			setDfe(getDf().getElement(getNameElement()));

			setListPage(getDfe().getPageList());

			setListPageSize(getListPage().size());

			//initialise the position of list
			setListPosition(0);

			//retrieves the correct page
			setPage(getListPage().get(getListPosition()));

			setPageTitle(getPage().getTitle());

			setPageLegend(getPage().getLegend());

			//logger.info(" -> " + getPage().getTitle() + " -- " + getPage().getLegend());


			mountInteractionForm();



			/*

			for (DFEPage dfePage : dfe.getPageList()) {

				dfePage.getTitle();
				dfePage.getLegend();
				dfePage.getNbColumn();

				//on next button
				dfePage.checkPage();

				for (DFEInteraction dfeInteraction : dfePage.getInteractions()) {

					//before open
					dfe.update(dfeInteraction);

					dfeInteraction.getColumn();
					dfeInteraction.getPlaceInColumn();
					dfeInteraction.getName();
					dfeInteraction.getLegend();
					DisplayType display = dfeInteraction.getDisplay();

					//update tree add new tree
					//on click button if necessary
					dfeInteraction.check();

					//List<Tree<String>> lis =  dfeInteraction.getTree().getFirstChild("help").getChildren("submenu");
					//lis.get(0).getFirstChild("name").getFirstChild().getHead();

					//logger.info(display);
					//logger.info(dfeInteraction.getTree());

				}
			}*/

			//update the output
			//dfe.updateOut();

			setFirstPage("S");

			logger.info("List size " + getListPage().size());

			if(getListPageSize() -1 > getListPosition()){
				setLastPage("N");
			}else{
				setLastPage("S");
			}


		} catch (RemoteException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e);
			logger.error(e.getMessage());
		}
		//openTextEditor();

	}

	/** mountInteractionForm
	 * 
	 * Method to mount the new list of Interaction
	 * 
	 * @return 
	 * @author Igor.Souza
	 * @throws RemoteException 
	 */
	public void mountInteractionForm() throws RemoteException {

		setDynamicFormList(new ArrayList<DynamicForm>());

		for (DFEInteraction dfeInteraction : getPage().getInteractions()) {

			DynamicForm dynamicF = new DynamicForm();
			getDfe().update(dfeInteraction);

			//logger.info(dfeInteraction.getName() + " -- " + dfeInteraction.getLegend() + " -- " + dfeInteraction.getDisplay());
			//logger.info(dfeInteraction.getColumn() + " -- " + dfeInteraction.getPlaceInColumn());

			logger.info("type  " + dfeInteraction.getDisplay());
			
			dynamicF.setName(dfeInteraction.getName());
			dynamicF.setLegend(dfeInteraction.getLegend());
			dynamicF.setDisplayType(dfeInteraction.getDisplay());
			dynamicF.setTree(dfeInteraction.getTree());

			if(dfeInteraction.getDisplay().equals(DisplayType.list)){

				List<SelectItem> selectItems = new ArrayList<SelectItem>();
				List<Tree<String>> list = dfeInteraction.getTree().getFirstChild("list").getChildren("value");
				for (Tree<String> tree : list) {
					//logger.info(dfeInteraction.getName() + " " + tree.getHead());
					selectItems.add(new SelectItem(tree.getFirstChild().getHead(), tree.getFirstChild().getHead()));
				}
				dynamicF.setListOptions(selectItems);

				if(dfeInteraction.getTree().getFirstChild("list").getFirstChild("output").getFirstChild() != null){
					String value =  dfeInteraction.getTree().getFirstChild("list").getFirstChild("output").getFirstChild().getHead();
					logger.info("value default -> " + value);
					dynamicF.setSelectedListOptions(value);
				}

			}else if(dfeInteraction.getDisplay().equals(DisplayType.appendList)){

				List<SelectItem> selectItems = new ArrayList<SelectItem>();
				List<Tree<String>> list = dfeInteraction.getTree().getFirstChild("applist").getChildren("value");
				for (Tree<String> tree : list) {
					selectItems.add(new SelectItem(tree.getFirstChild().getHead(), tree.getFirstChild().getHead()));
				}
				dynamicF.setListOptions(selectItems);

				if(selectItems.size() > 10){
					dynamicF.setComboBox("Y");
				}else{
					dynamicF.setComboBox("N");
				}

			}else if(dfeInteraction.getDisplay().equals(DisplayType.browser)){

				String dataTypeName = dfeInteraction.getTree().getFirstChild("browse").getFirstChild("type").getFirstChild().getHead();
				logger.info("dataTypeName " + dataTypeName);
				dynamicF.setDataTypeName(dataTypeName);
				String subtypeName = dfeInteraction.getTree().getFirstChild("browse").getFirstChild("subtype").getFirstChild().getHead();
				logger.info("subtypeName " + subtypeName);
				dynamicF.setSubtypeName(subtypeName);
				
				if(dfeInteraction.getTree().getFirstChild("browse").getFirstChild("output").getFirstChild("path") != null){
					String mypath = dfeInteraction.getTree().getFirstChild("browse").getFirstChild("output").getFirstChild("path").getFirstChild().getHead();
					dynamicF.setPathBrowser(mypath);
				}
				
				if(dfeInteraction.getTree().getFirstChild("browse").getFirstChild("output").getChildren("feature") != null){
					List<Tree<String>> listFeature = dfeInteraction.getTree().getFirstChild("browse").getFirstChild("output").getChildren("feature");
					List<ItemList> listObj = new ArrayList<ItemList>();
					Map<String, String> nameValueFeature = new HashMap<String, String>();
					for (Tree<String> tree : listFeature) {
						ItemList item = new ItemList(tree.getFirstChild("name").getFirstChild().getHead());
						logger.info("value map feature -> " + tree.getFirstChild("name").getFirstChild().getHead() + "  " + tree.getFirstChild("type").getFirstChild().getHead());
						nameValueFeature.put(tree.getFirstChild("name").getFirstChild().getHead(), tree.getFirstChild("type").getFirstChild().getHead());
						item.setNameValue(nameValueFeature);
						item.setSelected(false);
						listObj.add(item);
					}
					setNameValueFeature(nameValueFeature);
					dynamicF.setListFeature(listObj);
				}
				
				if(dfeInteraction.getTree().getFirstChild("browse").getFirstChild("output").getChildren("property") != null){
					List<Tree<String>> listProp = dfeInteraction.getTree().getFirstChild("browse").getFirstChild("output").getChildren("property");
					List<ItemList> listObj = new ArrayList<ItemList>();
					Map<String, String> nameValueListGrid = new HashMap<String, String>();
					for (Tree<String> tree : listProp) {
						ItemList item = new ItemList(tree.getFirstChild().getHead());
						nameValueListGrid.put(tree.getFirstChild().getHead(), tree.getFirstChild().getFirstChild().getHead());
						logger.info("value map Property -> " + tree.getFirstChild().getHead() + "  " + tree.getFirstChild().getFirstChild().getHead());
						item.setNameValue(nameValueListGrid);
						item.setSelected(false);
						listObj.add(item);
					}
					setNameValueListGrid(nameValueListGrid);
					dynamicF.setListGrid(listObj);
				}
				
			}else if(dfeInteraction.getDisplay().equals(DisplayType.helpTextEditor)){

			}else if(dfeInteraction.getDisplay().equals(DisplayType.table)){

			}

			getDynamicFormList().add(dynamicF);

		}


	}


	/** endDynamicForm
	 * 
	 * Methods to process the dynamic form
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void endDynamicForm() {

		logger.info("endDynamicForm ");

	}

	/** openTextEditor
	 * 
	 * Methods to mount screen dynamic form
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void openTextEditor() {

		//logger.info("openModal");

		List<SelectItem> selectItems = new ArrayList<SelectItem>();

		for (int i = 0; i < 5; i++) {
			ItemList l = new ItemList("nome"+i);
			selectItems.add(new SelectItem(l.getName(), l.getName()));
		}

		setListItens(selectItems);

		if(getListFields().isEmpty()){
			Entry e1 = new Entry("Type 1", "Field 1");
			Entry e2 = new Entry("Type 2", "Field 2");
			Entry e3 = new Entry("Type 3", "Field 3");
			getListFields().add(e1);
			getListFields().add(e2);
			getListFields().add(e3);
		}

		if(getListFunctions().isEmpty()){
			Entry e4 = new Entry("Function 1", "Return 1");
			Entry e5 = new Entry("Function 2", "Return 2");
			Entry e6 = new Entry("Function 3", "Return 3");
			getListFunctions().add(e4);
			getListFunctions().add(e5);
			getListFunctions().add(e6);
		}

		if(getListFunctionsType().isEmpty()){
			Entry e7 = new Entry("Label 1", "value 1");
			Entry e8 = new Entry("Label 2", "value 2");
			getListFunctionsType().add(e7);
			getListFunctionsType().add(e8);
		}


		/*if(getListGrid() != null && getListGrid().isEmpty()){
			for (int i = 0; i < 4; i++) {

				ItemList itemList = new ItemList("name"+i);
				getNameValue().put("label"+i, "value"+i);
				itemList.setSelected(false);
				itemList.setTypeTableInteraction("3");
				itemList.setNameValue(getNameValue());


				getListGrid().add(itemList);
			}
		}*/

	}

	/** openTableInteraction
	 * 
	 * Methods to mount screen Table Interaction
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void openTableInteraction() {

	}

	/** openBrowser
	 * 
	 * Methods to mount screen Browser
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void openBrowser() {
		
		logger.info("openBrowser");

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String positionElement = params.get("numberDynamic");
		
		logger.info("positionElement " + positionElement);
		DynamicForm dynamicForm = getDynamicFormList().get(Integer.parseInt(positionElement));
		
		if(dynamicForm.getDataTypeName().equalsIgnoreCase("hive")){
			
			
		} else if(dynamicForm.getDataTypeName().equalsIgnoreCase("hdfs")){
			
			
		}
		

	}

	/** confirm
	 * 
	 * Methods to confirm this action.
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void confirm() {

		logger.info("confirm");


	}

	/** cancel
	 * 
	 * Methods to cancel this action
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void cancel() {

		logger.info("cancel");

	}

	/** checkTextEditor
	 * 
	 * Methods to Check if the entry are correct or not for this action.
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void checkTextEditor() {

		logger.info("checkTextEditor");

	}

	/** changeFunctionsTextEditor
	 * 
	 * Methods to retrieve the new Functions
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void changeFunctionsTextEditor(){

		logger.info("changeFunctions");

		setListFunctions(new ArrayList<Entry>());

		Entry e1 = new Entry("Function 8", "Return 8");
		Entry e2 = new Entry("Function 9", "Return 9");
		Entry e3 = new Entry("Function 0", "Return 0");
		getListFunctions().add(e1);
		getListFunctions().add(e2);
		getListFunctions().add(e3);

	}

	/** tableInteractionAddNewLine
	 * 
	 * Methods to add a new line on table editor
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void tableInteractionAddNewLine() {

		logger.info("tableInteractionAddNewLine");


		/*ItemList itemList = new ItemList();
		itemList.setNameValue(getNameValue());
		itemList.setSelected(false);
		itemList.setTypeTableInteraction("2");

		getListGrid().add(itemList);*/

	}

	/** tableInteractionGenerationLines
	 * 
	 * Methods to add a several lines on table editor
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void tableInteractionGenerationLines() {

		logger.info("tableInteractionGenerationLines");


		/*ItemList itemList = new ItemList();
		itemList.setNameValue(getNameValue());
		itemList.setSelected(false);

		getListGrid().add(itemList);*/

	}

	/** tableInteractionDeleteLine
	 * 
	 * Methods to remove selected lines from table editor
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void tableInteractionDeleteLine() {

		logger.info("tableInteractionDeleteLine");

		/*for (Iterator<ItemList> iterator = getListGrid().iterator(); iterator.hasNext();) {
			ItemList itemList = (ItemList) iterator.next();

			if(itemList.isSelected()){
				iterator.remove();
			}
		}*/

	}

	/** confirmTableInteraction
	 * 
	 * Method for validating and close the table interaction 
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void confirmTableInteraction() {

		logger.info("confirmTableInteraction");


	}

	/** changePathBrowser
	 * 
	 * Method to change path for the Browser screen
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void changePathBrowser() throws RemoteException {

		logger.info("changePathBrowser");

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String positionElement = params.get("numberDynamic");
		
		logger.info("positionElement " + positionElement);
		DynamicForm dynamicForm = getDynamicFormList().get(Integer.parseInt(positionElement));

		if(dynamicForm.getDataTypeName().equalsIgnoreCase("Hive")){
			
			getDfe().getDFEOutput().get("source").setPath(dynamicForm.getPathBrowser());
			String error = getDfe().getDFEOutput().get("source").isPathValid();
			logger.info("error hive " + error);
			
			Map<String, String> map = getHiveInterface().getProperties(dynamicForm.getPathBrowser());
			logger.info("map " + map);
			
			//getDfe().getDFEOutput().get("source").setFeatures();
			
		}else if(dynamicForm.getDataTypeName().equalsIgnoreCase("HDFS")){
			
			getDfe().getDFEOutput().get("source").setPath(dynamicForm.getPathBrowser());
			String error = getDfe().getDFEOutput().get("source").isPathValid();
			logger.info("error hdfs " + error);
			
		}
		
		getDfe().getDFEOutput().get("source").select(10);
		getDfe().getDFEOutput().get("source").getProperties();
		getDfe().getDFEOutput().get("source").getFeatures();
		
		//getHDFS().select(getPathBrowser(), 10);
		//getHiveInterface().select(getPathBrowser(), 10);
		
	}

	/** confirmBrowser
	 * 
	 * Method for validating and close the browser
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void confirmBrowser() {

		logger.info("confirmBrowser");


	}

	/** addElement
	 * 
	 * Method for add Element on canvas. set the new idElement on the element
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void addElement() {
//		setNameWorkflow("canvas1");

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

		String nameElement = params.get("paramNameElement");
		setNameElement(nameElement);

		String paramGroupID = params.get("paramGroupID");
		setGroupID(paramGroupID);

		String posX = params.get("paramPosX");
		String posY = params.get("paramPosY");
		try {

//			DataFlowInterface dfi = getworkFlowInterface();

//			DataFlow df = dfi.getWorkflow(getNameWorkflow());

			DataFlow df = getDf();
			
			String idElement = df.addElement(getNameElement());
			df.getElement(idElement).setPosition(Double.valueOf(posX).intValue(), Double.valueOf(posY).intValue());

			setNameElement(idElement);
//			getIdMap().put(paramGroupID, idElement);

			logger.info("add element id " + getNameElement()+" in workflow "+df.getName());

			setEntry(new Entry(getNameElement(), getGroupID()));

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

			DataFlowInterface dfi = getworkFlowInterface();

			DataFlow df = dfi.getWorkflow(getNameWorkflow());

			df.removeLink("", "wlwmwntOut", "", "elementIN");

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
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
//			dfi = getworkFlowInterface();
//			
//			DataFlow df = dfi.getWorkflow(getNameWorkflow());
			logger.info("save workflow "+getDf().getName()+" in "+path);
			logger.info(getDf().save(path));
		} catch (Exception e) {
			logger.info("Error saving workflow");
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
	
	/** closeWorkflow
	 * 
	 * Method to close a workflow
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void closeWorkflow() {
		
		logger.info("closeWorkflow");
		
		setDf(null);
//		setIdMap(new HashMap<String, String>());
	}


	public List<SelectItem> getListItens() {
		return listItens;
	}

	public void setListItens(List<SelectItem> listItens) {
		this.listItens = listItens;
	}

	public String getList() {
		return list;
	}

	public void setList(String list) {
		this.list = list;
	}

	public List<Entry> getListFunctions() {
		return listFunctions;
	}

	public void setListFunctions(List<Entry> listFunctions) {
		this.listFunctions = listFunctions;
	}

	public List<Entry> getListFields() {
		return listFields;
	}

	public void setListFields(List<Entry> listFields) {
		this.listFields = listFields;
	}

	public List<Entry> getListFunctionsType() {
		return listFunctionsType;
	}

	public void setListFunctionsType(List<Entry> listFunctionsType) {
		this.listFunctionsType = listFunctionsType;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
	
	public ArrayList<ItemList> getListGrid() {
		return listGrid;
	}

	public void setListGrid(ArrayList<ItemList> listGrid) {
		this.listGrid = listGrid;
	}

	public String getPathBrowser() {
		return pathBrowser;
	}

	public void setPathBrowser(String pathBrowser) {
		this.pathBrowser = pathBrowser;
	}

	public ArrayList<ItemList> getListFeature() {
		return listFeature;
	}

	public void setListFeature(ArrayList<ItemList> listFeature) {
		this.listFeature = listFeature;
	}

	public String getNameWorkflow() {
		return nameWorkflow;
	}

	public void setNameWorkflow(String nameWorkflow) {
		this.nameWorkflow = nameWorkflow;
	}

	public String getNameElement() {
		return nameElement;
	}

	public void setNameElement(String nameElement) {
		this.nameElement = nameElement;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}
	
	public Entry getEntry() {
		return entry;
	}

	public void setEntry(Entry entry) {
		this.entry = entry;
	}

	public DataFlowInterface getDfi() {
		return dfi;
	}

	public void setDfi(DataFlowInterface dfi) {
		this.dfi = dfi;
	}

	public DataFlow getDf() {
		return df;
	}

	public void setDf(DataFlow df) {
		this.df = df;
	}

	public List<DFEPage> getListPage() {
		return listPage;
	}

	public void setListPage(List<DFEPage> listPage) {
		this.listPage = listPage;
	}

	public int getListPosition() {
		return listPosition;
	}

	public void setListPosition(int listPosition) {
		this.listPosition = listPosition;
	}

	public DFEPage getPage() {
		return page;
	}

	public void setPage(DFEPage page) {
		this.page = page;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public String getPageLegend() {
		return pageLegend;
	}

	public void setPageLegend(String pageLegend) {
		this.pageLegend = pageLegend;
	}

	public String getLastPage() {
		return lastPage;
	}

	public void setLastPage(String lastPage) {
		this.lastPage = lastPage;
	}

	public String getFirstPage() {
		return firstPage;
	}

	public void setFirstPage(String firstPage) {
		this.firstPage = firstPage;
	}

	public int getListPageSize() {
		return listPageSize;
	}

	public void setListPageSize(int listPageSize) {
		this.listPageSize = listPageSize;
	}

	public List<DynamicForm> getDynamicFormList() {
		return dynamicFormList;
	}

	public void setDynamicFormList(List<DynamicForm> dynamicFormList) {
		this.dynamicFormList = dynamicFormList;
	}

	public DataFlowElement getDfe() {
		return dfe;
	}

	public void setDfe(DataFlowElement dfe) {
		this.dfe = dfe;
	}

	public Map<String, String> getNameValueFeature() {
		return nameValueFeature;
	}

	public void setNameValueFeature(Map<String, String> nameValueFeature) {
		this.nameValueFeature = nameValueFeature;
	}

	public Map<String, String> getNameValueListGrid() {
		return nameValueListGrid;
	}

	public void setNameValueListGrid(Map<String, String> nameValueListGrid) {
		this.nameValueListGrid = nameValueListGrid;
	}

	
//	public Map<String, String> getIdMap() {
//		return idMap;
//	}
//
//	public void setIdMap(Map<String, String> idMap) {
//		this.idMap = idMap;
//	}

	public String getPositions() throws Exception{
		JSONArray json = new JSONArray();
		for (DataFlowElement e : getDf().getElement()){
			json.put(new Object[]{e.getComponentId(), e.getName(), e.getImage(), e.getX(), e.getY()});
		}
		return json.toString();
	}
}