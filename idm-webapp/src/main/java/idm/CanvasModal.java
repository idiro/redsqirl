package idm;

import idiro.utils.Tree;
import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEPage;
import idiro.workflow.server.interfaces.DataFlow;
import idiro.workflow.server.interfaces.DataFlowElement;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.ajax4jsf.model.KeepAlive;
import org.apache.log4j.Logger;

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
	private Map<String, String> nameValue = new HashMap<String, String>();
	private ArrayList<ItemList> listGrid = new ArrayList<ItemList>();
	private ArrayList<ItemList> listFeature = new ArrayList<ItemList>();
	private String pathBrowser = "";
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


	/** getKeyAsListNameValue
	 * 
	 * Method to retrieve the list of files
	 * 
	 * @return List<String>
	 * @author Igor.Souza
	 */
	public List<String> getKeyAsListNameValue(){
		return new ArrayList<String>(nameValue.keySet());
	}

	/** nextPage
	 * 
	 * Methods to control the sequence of screens
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void nextPage() {

		logger.info("nextPage ");
		
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
		
	}
	
	/** previous
	 * 
	 * Methods to control the sequence of screens
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void previousPage() {
		
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
		
		setNameWorkflow("canvas1");

		try {
			
			if(getDfi() == null){
				setDfi(getworkFlowInterface());
			}
			if(getDf() == null){
				setDf(dfi.getWorkflow(getNameWorkflow()));
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

			DataFlowElement dfe = getDf().getElement(getNameElement());

			setListPage(dfe.getPageList());

			setListPageSize(getListPage().size());
			
			//initialise the position of list
			setListPosition(0);
			
			//retrieves the correct page
			setPage(getListPage().get(getListPosition()));
			
			setPageTitle(getPage().getTitle());
			
			setPageLegend(getPage().getLegend());
			
			//logger.info(" -> " + getPage().getTitle() + " -- " + getPage().getLegend());
			
			setDynamicFormList(new ArrayList<DynamicForm>());
			
			for (DFEInteraction dfeInteraction : getPage().getInteractions()) {
				
				DynamicForm dynamicF = new DynamicForm();
				dfe.update(dfeInteraction);
				
				//logger.info(dfeInteraction.getName() + " -- " + dfeInteraction.getLegend() + " -- " + dfeInteraction.getDisplay());
				//logger.info(dfeInteraction.getColumn() + " -- " + dfeInteraction.getPlaceInColumn());
				
				dynamicF.setName(dfeInteraction.getName());
				dynamicF.setLegend(dfeInteraction.getLegend());
				dynamicF.setDisplayType(dfeInteraction.getDisplay());
				
				if(dfeInteraction.getDisplay().equals(DisplayType.list)){
					
					List<SelectItem> selectItems = new ArrayList<SelectItem>();
					List<Tree<String>> list = dfeInteraction.getTree().getFirstChild("list").getChildren("value");
					for (Tree<String> tree : list) {
						//logger.info(dfeInteraction.getName() + " " + tree.getFirstChild().getHead());
						selectItems.add(new SelectItem(tree.getHead(), tree.getHead()));
					}
					dynamicF.setListOptions(selectItems);
					
				}
				
				getDynamicFormList().add(dynamicF);
				
			}
			
			
			
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


		if(getListGrid() != null && getListGrid().isEmpty()){
			for (int i = 0; i < 4; i++) {

				ItemList itemList = new ItemList("name"+i);
				getNameValue().put("label"+i, "value"+i);
				itemList.setSelected(false);
				itemList.setTypeTableInteraction("3");
				itemList.setNameValue(getNameValue());


				getListGrid().add(itemList);
			}
		}

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


		ItemList itemList = new ItemList();
		itemList.setNameValue(getNameValue());
		itemList.setSelected(false);
		itemList.setTypeTableInteraction("2");

		getListGrid().add(itemList);

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


		ItemList itemList = new ItemList();
		itemList.setNameValue(getNameValue());
		itemList.setSelected(false);

		getListGrid().add(itemList);

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

		for (Iterator<ItemList> iterator = getListGrid().iterator(); iterator.hasNext();) {
			ItemList itemList = (ItemList) iterator.next();

			if(itemList.isSelected()){
				iterator.remove();
			}
		}

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
	 * Method to chage path for the Browser screen
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void changePathBrowser() throws RemoteException {

		logger.info("changePathBrowser");

		//getHDFS().select(getPathBrowser(), 10);
		//getHiveInterface().select(getPathBrowser(), 10);

		for (int i = 0; i < 4; i++) {

			ItemList itemList = new ItemList();
			getNameValue().put("label"+i, "value"+i);
			itemList.setSelected(false);
			itemList.setNameValue(getNameValue());

			getListFeature().add(itemList);
		}

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

		setNameWorkflow("canvas1");

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

		String nameElement = params.get("paramNameElement");
		setNameElement(nameElement);

		String paramGroupID = params.get("paramGroupID");
		setGroupID(paramGroupID);

		try {

			DataFlowInterface dfi = getworkFlowInterface();

			DataFlow df = dfi.getWorkflow(getNameWorkflow());

			String idElement = df.addElement(getNameElement());

			setNameElement(idElement);

			logger.info("add element id " + getNameElement());

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

		try {

			DataFlowInterface dfi = getworkFlowInterface();

			DataFlow df = dfi.getWorkflow(getNameWorkflow());

			/*DataFlowElement dfeObjA = df.getElement("idElementA");
			DataFlowElement dfeObjB = df.getElement("idElementB");

			dfeObjB.getInput();
			dfeObjA.getDFEOutput();

			df.addLink("", "wlwmwntOut", "", "elementIN");*/

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

	public Map<String, String> getNameValue() {
		return nameValue;
	}

	public void setNameValue(Map<String, String> nameValue) {
		this.nameValue = nameValue;
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
	
}