package idm;

import idiro.utils.Tree;
import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DFEInteraction;
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

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

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
	private List<SelectItem> listItensTable = new ArrayList<SelectItem>();
	private Map<String, List<SelectItem>> listConstraint = new HashMap<String, List<SelectItem>>();
	private List<String[]> listFunctions = new ArrayList<String[]>();
	private List<Entry> listFields = new ArrayList<Entry>();
	private Map<String, List<String[]>> functionsMap = new HashMap<String, List<String[]>>();
	private Map<String, List<Map<String, String>>> rowsMap = new HashMap<String, List<Map<String, String>>>();
	private Map<String, String> columnsMap = new HashMap<String, String>();
	private String command = "";
	private String commandEdit = "";
	private String nameWorkflow;
	private String nameElement;
	private String groupID;
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
	private List<OutputForm> outputFormList = new ArrayList<OutputForm>();
	private String nameOutput;

	private Map<String, String> nameValueFeature = new HashMap<String, String>();
	private Map<String, String> nameValueListGrid = new HashMap<String, String>();
	private String pathBrowser = "";
	private List<ItemList> listGrid = new ArrayList<ItemList>();
	private List<ItemList> listFeature = new ArrayList<ItemList>();
	private DataFlowElement dfe;
	private DynamicForm DynamicFormBrowser;

	private String columnEdit;
	private int rowEdit;
	private String errorMsg;
	private String pathImage;

	private String tabTitle;
	private String tabLegend;
	
	private List<String> tableInteractionsColumns = new ArrayList<String>();

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
	
	public List<String> getTableInteractionColumns(){
		return tableInteractionsColumns;
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
			HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
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



			} else if(dynamicF.getDisplayType().equals(DisplayType.appendList)){

				dynamicF.getTree().getFirstChild("applist").getFirstChild("output").removeAllChildren();
				logger.info(dynamicF.getName()+ "value list size-> " + dynamicF.getSelectedAppendListOptions().size());
				for (String s : dynamicF.getSelectedAppendListOptions()){
					logger.info("appendList seleted: "+s);
					dynamicF.getTree().getFirstChild("applist").getFirstChild("output").add("value").add(s);
				}


			} else if(dynamicF.getDisplayType().equals(DisplayType.browser)){

				logger.info("Browser path -> " + dynamicF.getPathBrowser());
				dynamicF.getTree().getFirstChild("browse").getFirstChild("output").removeAllChildren();
				dynamicF.getTree().getFirstChild("browse").getFirstChild("output").add("path").add(dynamicF.getPathBrowser());


				for (String nameValue : getKeyAsListNameValueFeature()) {
					Tree<String> myFeature = dynamicF.getTree().getFirstChild("browse").getFirstChild("output").add("feature");
					String value[] = nameValue.split(" ");
					myFeature.add("name").add(value[0]);
					myFeature.add("type").add(value[1]);
				}

				getDfe().updateOut();

			}else if(dynamicF.getDisplayType().equals(DisplayType.helpTextEditor)){
				dynamicF.getTree().getFirstChild("editor").getFirstChild("output").removeAllChildren();
				dynamicF.getTree().getFirstChild("editor").getFirstChild("output").add(getCommand());
			}else if(dynamicF.getDisplayType().equals(DisplayType.table)){

				dynamicF.getTree().getFirstChild("table").remove("row");

				for (ItemList item : getListGrid()){
					Tree<String> row = dynamicF.getTree().getFirstChild("table").add("row");
					logger.info("Table row");
					for (String column : getKeyAsListNameValueListGrid()){
						String value = item.getNameValue().get(column);
						row.add(column).add(value);
						logger.info(column+" -> "+value);
					}
				}
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
		
		checkNextPage();

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
	public void start() {
		String nameWf = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("paramNameWorkflow");
		setNameWorkflow(nameWf);

		try {

			setDfi(getworkFlowInterface());
			setDf(dfi.getWorkflow(getNameWorkflow()));

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
	 * @throws RemoteException 
	 */
	public void openCanvasModal() throws RemoteException {
		HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
		request.removeAttribute("msnError");
		
		start();

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

		String pathImage =  params.get("paramPathImage");
		String nameElement = params.get("paramNameElement");
		setNameElement(nameElement);
		setPathImage(pathImage);

		logger.info("open element id " + getNameElement());

		if(pathImage != null && nameElement != null){

			try {

				setDfe(getDf().getElement(getNameElement()));

				//validate if you can open or not the dynamic form of the object
				String error = getDfe().checkIn();
				
				logger.info("error " + error);
				
				if(error != null){

					MessageUseful.addErrorMessage(error);
					request.setAttribute("msnError", "msnError");

				}else{


					setListPage(getDfe().getPageList());

					setListPageSize(getListPage().size());

					//initialise the position of list
					setListPosition(0);

					//retrieves the correct page
					setPage(getListPage().get(getListPosition()));

					setPageTitle(getDfe().getName().replace("_"," "));

					// setPageLegend(getPage().getLegend());

					mountInteractionForm();

					setFirstPage("S");

					logger.info("List size " + getListPage().size());

					if(getListPageSize() -1 > getListPosition()){
						setLastPage("N");
					}else{
						setLastPage("S");
					}


				}

			} catch (RemoteException e) {
				logger.error(e.getMessage());
			} catch (Exception e) {
				logger.error(e);
				logger.error(e.getMessage());
			}

		}

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

		logger.info("mountInteractionForm ");

		setDynamicFormList(new ArrayList<DynamicForm>());


		for (DFEInteraction dfeInteraction : getPage().getInteractions()) {

			DynamicForm dynamicF = new DynamicForm();

			logger.info("type " + dfeInteraction.getName());

			getDfe().update(dfeInteraction);

			logger.info("type " + dfeInteraction.getName() + " " + dfeInteraction.getDisplay() + " " + dfeInteraction.getTree());

			setTabTitle(getPage().getTitle());
			setTabLegend(getPage().getLegend());

			dynamicF.setName(dfeInteraction.getName().replace("_", " "));
			dynamicF.setLegend(dfeInteraction.getLegend());
			dynamicF.setDisplayType(dfeInteraction.getDisplay());
			dynamicF.setTree(dfeInteraction.getTree());

			if(dfeInteraction.getDisplay().equals(DisplayType.list)){

				List<SelectItem> selectItems = new ArrayList<SelectItem>();
				List<Tree<String>> list = dfeInteraction.getTree().getFirstChild("list").getFirstChild("values").getSubTreeList();

				logger.info("list value " + list);

				if(list != null){
					for (Tree<String> tree : list) {
						logger.info("list value " + tree.getFirstChild().getHead());
						selectItems.add(new SelectItem(tree.getFirstChild().getHead(), tree.getFirstChild().getHead()));
					}
					dynamicF.setListOptions(selectItems);
				}

				if(dfeInteraction.getTree().getFirstChild("list").getFirstChild("output").getFirstChild() != null){
					String value =  dfeInteraction.getTree().getFirstChild("list").getFirstChild("output").getFirstChild().getHead();
					logger.info("value default -> " + value);
					dynamicF.setSelectedListOptions(value);
				}

			}else if(dfeInteraction.getDisplay().equals(DisplayType.appendList)){

				logger.info("appendList");
				List<SelectItem> selectItems = new ArrayList<SelectItem>();
				if (dfeInteraction.getTree().getFirstChild("applist").getFirstChild("values") != null){
					List<Tree<String>> list = dfeInteraction.getTree().getFirstChild("applist").getFirstChild("values").getChildren("value");
					if(list != null){
						logger.info("list not null: "+list.size());
						for (Tree<String> tree : list) {
							logger.info("list value " + tree.getFirstChild().getHead());
							selectItems.add(new SelectItem(tree.getFirstChild().getHead(), tree.getFirstChild().getHead()));
						}
						dynamicF.setAppendListOptions(selectItems);

						if(selectItems.size() > 10){
							dynamicF.setComboBox("Y");
						}else{
							dynamicF.setComboBox("N");
						}
					}
				}

				if (dfeInteraction.getTree().getFirstChild("applist").getFirstChild("output") != null){
					if(dfeInteraction.getTree().getFirstChild("applist").getFirstChild("output").getChildren("value") != null){
						List<Tree<String>> listOut = dfeInteraction.getTree().getFirstChild("applist").getFirstChild("output").getChildren("value");
						if(listOut != null){
							List<String> listSelected = new ArrayList<String>();
							for (Tree<String> tree : listOut) {
								listSelected.add(tree.getFirstChild().getHead());
							}
							dynamicF.setSelectedAppendListOptions(listSelected);
						}
					}
				}

			}else if(dfeInteraction.getDisplay().equals(DisplayType.browser)){

				String dataTypeName = dfeInteraction.getTree().getFirstChild("browse").getFirstChild("type").getFirstChild().getHead();
				logger.info("dataTypeName " + dataTypeName);
				dynamicF.setDataTypeName(dataTypeName);
				if (dataTypeName.equalsIgnoreCase("HDFS")){
					String subtypeName = dfeInteraction.getTree().getFirstChild("browse").getFirstChild("subtype").getFirstChild().getHead();
					logger.info("subtypeName " + subtypeName);
					dynamicF.setSubtypeName(subtypeName);
				}

				if(dfeInteraction.getTree().getFirstChild("browse").getFirstChild("output").getFirstChild("path") != null){
					String mypath = dfeInteraction.getTree().getFirstChild("browse").getFirstChild("output").getFirstChild("path").getFirstChild().getHead();
					dynamicF.setPathBrowser(mypath);
					logger.info("path mount " + mypath);
					setPathBrowser("/"+mypath);
					setDynamicFormBrowser(dynamicF);
					changePathBrowser();
				}


			}else if(dfeInteraction.getDisplay().equals(DisplayType.helpTextEditor)){
				setCommand("");
				if (dfeInteraction.getTree().getFirstChild("editor").getFirstChild("output").getFirstChild() != null){
					setCommand(dfeInteraction.getTree().getFirstChild("editor").getFirstChild("output").getFirstChild().getHead());
				}

				mountHelpTextEditorInteraction(dfeInteraction.getTree());

			}else if(dfeInteraction.getDisplay().equals(DisplayType.table)){

				setListConstraint(new HashMap<String, List<SelectItem>>());

				Map<String, List<Map<String, String>>> map = new HashMap<String, List<Map<String, String>>>();
				List<SelectItem> listFields = new ArrayList<SelectItem>();

				if (dfeInteraction.getTree().getFirstChild("table").getFirstChild("generator") != null){
					List<Tree<String>> list = dfeInteraction.getTree().getFirstChild("table").getFirstChild("generator").getSubTreeList();
					if(list != null){
						for (Tree<String> tree : list) {
							logger.info("list value " + tree.getFirstChild().getHead());
							SelectItem e = new SelectItem(tree.getFirstChild("title").getFirstChild().getHead(),
									tree.getFirstChild("title").getFirstChild().getHead());
							listFields.add(e);
							map.put(tree.getFirstChild("title").getFirstChild().getHead(), new ArrayList<Map<String, String>>());

							for (Tree<String> treeRows : tree.getChildren("row")){
								Map<String, String> t = new HashMap<String, String>();
								for (Tree<String> treeFeat : treeRows.getSubTreeList()){
									t.put(treeFeat.getHead(), treeFeat.getFirstChild().getHead());
								}
								map.get(tree.getFirstChild("title").getFirstChild().getHead()).add(t);
							}
						}
					}
				}
				setRowsMap(map);
				setListItens(listFields);

				Map<String, String> mapColumns = new HashMap<String, String>();
				List<Tree<String>> list2 = dfeInteraction.getTree().getFirstChild("table").getFirstChild("columns").getSubTreeList();
				tableInteractionsColumns = new ArrayList<String>();
				if(list2 != null){
					for (Tree<String> tree : list2) {
						logger.info("list2 value " + tree.getHead());
						String aux = null;
						if (tree.getFirstChild("constraint") != null){
							if (tree.getFirstChild("constraint").findFirstChild("values") != null){
								aux = "comboBox";
								mountTableInteractionConstraint(tree);
							}
							else if (tree.getFirstChild("constraint").findFirstChild("count") != null){
								aux = "textField";
							}
						}else if (tree.getFirstChild("editor") != null){
							aux = "editor";
							mountHelpTextEditorInteraction(tree);
						}

						mapColumns.put(tree.getFirstChild("title").getFirstChild().getHead(), aux);
						tableInteractionsColumns.add(tree.getFirstChild("title").getFirstChild().getHead());
					}
				}
				setColumnsMap(mapColumns);

				nameValueListGrid = new HashMap<String, String>();
				Map<String, String> columnsMap = new HashMap<String, String>();
				for (String column : getColumnsMap().keySet()){
					columnsMap.put(column,column);
				}
				setNameValueListGrid(columnsMap);

				List<ItemList> listGrid = new ArrayList<ItemList>();
				if (dfeInteraction.getTree().getFirstChild("table").getChildren("row") != null){
					List<Tree<String>> list = dfeInteraction.getTree().getFirstChild("table").getChildren("row");
					for (Tree<String> rows : list) {

						ItemList item = new ItemList();

						for (Tree<String> row : rows.getSubTreeList()){
							item.getNameValue().put(row.getHead(), row.getFirstChild().getHead());
							logger.info(row.getHead()+" -> "+row.getFirstChild().getHead());
						}
						listGrid.add(item);
					}
				}
				setListGrid(listGrid);
				setList(null);
			}

			getDynamicFormList().add(dynamicF);

		}
	}

	private void mountTableInteractionConstraint(Tree<String> dfeInteractionTree) throws RemoteException{
		List<SelectItem> listFields = new ArrayList<SelectItem>();
		if (dfeInteractionTree.getFirstChild("constraint").getFirstChild("values") != null){
			List<Tree<String>> list = dfeInteractionTree.getFirstChild("constraint").getFirstChild("values").getSubTreeList();

			if(list != null){
				logger.info("list not null: "+list.toString());
				for (Tree<String> tree : list) {
					logger.info("list value " + tree.getFirstChild().getHead());
					listFields.add(new SelectItem(tree.getFirstChild().getHead(), tree.getFirstChild().getHead()));
				}
			}
			getListConstraint().put(dfeInteractionTree.getFirstChild("title").getFirstChild().getHead(), listFields);
		}
	}



	private void mountHelpTextEditorInteraction(Tree<String> dfeInteractionTree) throws RemoteException{
		List<Entry> listFields = new ArrayList<Entry>();
		List<Tree<String>> list = dfeInteractionTree.getFirstChild("editor").getFirstChild("keywords").getSubTreeList();
		if(list != null){
			logger.info("list not null: "+list.toString());
			for (Tree<String> tree : list) {
				logger.info("list value " + tree.getFirstChild().getHead());
				Entry e = new Entry(tree.getFirstChild("name").getFirstChild().getHead(),
						tree.getFirstChild("info").getFirstChild().getHead());
				listFields.add(e);
			}
		}
		setListFields(listFields);


		List<SelectItem> listCategories = new ArrayList<SelectItem>();
		list = dfeInteractionTree.getFirstChild("editor").getFirstChild("help").getSubTreeList();
		if(list != null){
			logger.info("list not null: "+list.toString());
			for (Tree<String> tree : list) {
				logger.info("list value " + tree.getHead());
				SelectItem e = new SelectItem(tree.getHead(),
						tree.getHead());
				listCategories.add(e);
			}
		}
		setListItensTable(listCategories);


		Map<String, List<String[]>> map = new HashMap<String, List<String[]>>();
		list = dfeInteractionTree.getFirstChild("editor").getFirstChild("help").getSubTreeList();
		if(list != null){
			logger.info("list not null: "+list.toString());
			for (Tree<String> tree : list) {
				logger.info("list value " + tree.getHead());
				if (!map.containsKey(tree.getHead())){
					map.put(tree.getHead(), new ArrayList<String[]>());
				}

				for (Tree<String> tree2 : tree.getSubTreeList()){

					String nameFunction = tree2.getFirstChild("name").getFirstChild() != null ? 
							tree2.getFirstChild("name").getFirstChild().getHead() : "";
							String inputFunction = tree2.getFirstChild("input").getFirstChild() != null ?
									tree2.getFirstChild("input").getFirstChild().getHead() : "";
									String returnFunction = tree2.getFirstChild("return").getFirstChild() != null ?
											tree2.getFirstChild("return").getFirstChild().getHead() : "";

											map.get(tree.getHead()).add(new String[]{nameFunction,
													inputFunction,
													returnFunction});
				}
			}
		}

		setFunctionsMap(map);
	}

	/** endDynamicForm
	 * 
	 * Methods to process the dynamic form
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException 
	 */
	public void endDynamicForm() throws RemoteException {

		logger.info("endDynamicForm ");

		mountOutputForm();

		String error = checkNextPage();
		if(error.length() > 1){
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");

		}
		setErrorMsg(error);

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

	/** confirm
	 * 
	 * Methods to confirm this action.
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	public void confirm() {

		logger.info("confirm");

		if (getColumnEdit() != null){
			getListGrid().get(getRowEdit()).getNameValue().put(getColumnEdit(), getCommandEdit());
		}
		else{
			setCommand(getCommandEdit());
		}


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
	 * @throws RemoteException 
	 */
	public void checkTextEditor() throws RemoteException {

		logger.info("checkTextEditor");

		for (int i = 0; i < getDynamicFormList().size(); i++) {

			DynamicForm dynamicF = getDynamicFormList().get(i);
			DFEInteraction dfi = getPage().getInteractions().get(i);

			if(dynamicF.getDisplayType().equals(DisplayType.helpTextEditor)){
				String oldCommand = null;
				if (dfi.getTree().getFirstChild("editor").getFirstChild("output").getFirstChild() != null){
					oldCommand = dfi.getTree().getFirstChild("editor").getFirstChild("output").getFirstChild().getHead();
				}

				logger.info("oldCommand -> " + oldCommand);

				dfi.getTree().getFirstChild("editor").getFirstChild("output").removeAllChildren();
				dfi.getTree().getFirstChild("editor").getFirstChild("output").add(getCommandEdit());

				String e = dfi.check();

				logger.info("error interaction -> " + e);

				if(e != null && e.length() > 0){
					MessageUseful.addErrorMessage(e);
					HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
					request.setAttribute("msnError", "msnError");
				}

				dfi.getTree().getFirstChild("editor").getFirstChild("output").removeAllChildren();
				if (oldCommand != null){
					dfi.getTree().getFirstChild("editor").getFirstChild("output").add(oldCommand);
				}
			}
		}
	}

	/** changeFunctionsTextEditor
	 * 
	 * Methods to retrieve the new Functions
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void changeFunctionsTextEditor(){

		logger.info("changeFunctions: "+getList());

		setListFunctions(getFunctionsMap().get(getList()));
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

		ItemList item = new ItemList();

		Map<String, String> type = new HashMap<String, String>();
		Map<String, String> value = new HashMap<String, String>();
		logger.info("num columns: "+getColumnsMap().keySet().size());
		for (String column : getColumnsMap().keySet()){
			type.put(column, getColumnsMap().get(column));
			value.put(column, null);
		}
		item.setTypeTableInteraction(type);
		item.setNameValue(value);

		getListGrid().add(item);
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

		List<ItemList> itemList = new ArrayList<ItemList>();
		for (Map<String, String> l : getRowsMap().get(this.getList())){

			ItemList item = new ItemList();

			Map<String, String> type = new HashMap<String, String>();
			Map<String, String> value = new HashMap<String, String>();

			for (String column : getColumnsMap().keySet()){
				type.put(column, getColumnsMap().get(column));
				value.put(column, l.get(column));
			}
			item.setTypeTableInteraction(type);
			item.setNameValue(value);

			itemList.add(item);
		}
		setListGrid(itemList);
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

	public void changePathBrowserBefore() throws RemoteException {

		String positionElement = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("numberDynamic");

		logger.info("positionElement " + positionElement);
		setDynamicFormBrowser(getDynamicFormList().get(Integer.parseInt(positionElement)));

		if(getDynamicFormBrowser().getDataTypeName().equalsIgnoreCase("hive")){


		} else if(getDynamicFormBrowser().getDataTypeName().equalsIgnoreCase("hdfs")){


		}


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

		String path = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("pathFile");
		if(path == null){
			path = getPathBrowser();
			logger.info("getPathBrowser" + path);
		}

		if(path != null){

			DynamicForm dynamicForm = getDynamicFormBrowser();
			String name = path.substring(1);

			logger.info("pathFile " + name);

			getDfe().getDFEOutput().get("source").setPath(name);
			getDfe().getDFEOutput().get("source").isPathExists();

			List<String> outputLines = getDfe().getDFEOutput().get("source").select(10);
			logger.info("outputLines " + outputLines);

			Map<String, String> outputPropertiesMap = getDfe().getDFEOutput().get("source").getProperties();
			logger.info("outputPropertiesMap " + outputPropertiesMap);

			Map<String, String> nameValueFeature = new HashMap<String, String>();
			List<ItemList> listObj = new ArrayList<ItemList>();
			List<String> outputFeatureList  = getDfe().getDFEOutput().get("source").getFeatures().getFeaturesNames();

			List<String> labels = new ArrayList<String>();

			for (String outputFeature : outputFeatureList) {

				logger.info("outputFeatureNames " + outputFeature);

				FeatureType featureType = getDfe().getDFEOutput().get("source").getFeatures().getFeatureType(outputFeature);

				logger.info("featureType " + featureType);

				labels.add(outputFeature + " " + featureType.toString());

			}

			if(outputLines != null){
				for (String output : outputLines) {
					Map<String, String> nameValueFeatureItem = new HashMap<String, String>();
					if(output != null){
						String rows[] = output.split("'\001'");
						for (int i = 0; i < rows.length; i++) {
							logger.info("map to show " + labels.get(i) + " " + rows[i]);
							nameValueFeature.put(labels.get(i), rows[i]);
							nameValueFeatureItem.put(labels.get(i), rows[i]);
						}
					}
					ItemList item = new ItemList();
					item.setSelected(false);
					item.setNameValue(nameValueFeatureItem);
					logger.info("new item ");
					listObj.add(item);

					logger.info("new nameValueFeature " + nameValueFeature);
					logger.info("new nameValueFeatureItem " + nameValueFeatureItem);
					logger.info("getKeyAsListNameValueFeature " + getKeyAsListNameValueFeature());

				}
				setNameValueFeature(nameValueFeature);
				dynamicForm.setListFeature(listObj);
				setListFeature(listObj);
			}
			dynamicForm.setPathBrowser(name);

			getDfe().updateOut();
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

	public void mountOutputForm() throws RemoteException{
		logger.info("mountOutputForm");
		if (getDfe().getDFEOutput() == null){
			getDfe().updateOut();
		}
		else if (!getDfe().getDFEOutput().isEmpty()){
			setOutputFormList(new ArrayList<OutputForm>());
			for (Map.Entry<String, DFEOutput> e : getDfe().getDFEOutput().entrySet()){
				OutputForm of = new OutputForm();
				of.setName(e.getKey());

				List<SelectItem> outputList = new ArrayList<SelectItem>();
				for (SavingState s : SavingState.values()){
					outputList.add(new SelectItem(s.toString(), s.toString()));
				}
				of.setSavingStateList(outputList);
				of.setDfeOutput(e.getValue());
				of.setComponentId(getDfe().getComponentId());
				of.setSavingState(e.getValue().getSavingState().toString());
				logger.info("saving state "+e.getValue().getSavingState().toString());
				of.setPath(e.getValue().getPath());

				getOutputFormList().add(of);
			}
		}else{
			setOutputFormList(new ArrayList<OutputForm>());
		}
	}

	public void changePathOutputBrowser() throws RemoteException {

		logger.info("changePathOutputBrowser");
		String path = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("pathFile");
		logger.info("Output: "+getNameOutput()+" - path: "+path);
		for (OutputForm f : getOutputFormList()){
			if (f.getName().equals(getNameOutput())){
				f.setPath(path);
				logger.info("Output found: "+getNameOutput()+" - path: "+path);
			}
		}
	}

	public void confirmOutput() throws RemoteException{
		logger.info("confirmOutput");

		for (OutputForm f : getOutputFormList()){
			String error = f.updateDFEOutput();
			if (error != null){
				logger.error(error);
				MessageUseful.addErrorMessage(error);
				HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
				request.setAttribute("msnError", "msnError");
			}
			logger.info("output ok");
		}
	}

	public void openHelpTextEditorModal(){
		logger.info("openHelpTextEditorModal");
		String command = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("command");

		String rowKey = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("rowKey");

		String column = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("column");

		if (rowKey != null){
			setRowEdit(Integer.valueOf(rowKey));
		}
		setColumnEdit(column);

		setCommandEdit(command);
		setList(null);
	}

	public List<SelectItem> getListItens() {
		return listItens;
	}

	public void setListItens(List<SelectItem> listItens) {
		this.listItens = listItens;
	}

	public List<SelectItem> getListItensTable() {
		return listItensTable;
	}

	public void setListItensTable(List<SelectItem> listItens) {
		this.listItensTable = listItens;
	}

	public String getList() {
		return list;
	}

	public void setList(String list) {
		this.list = list;
	}

	public List<String[]> getListFunctions() {
		return listFunctions;
	}

	public void setListFunctions(List<String[]> listFunctions) {
		this.listFunctions = listFunctions;
	}

	public List<Entry> getListFields() {
		return listFields;
	}

	public void setListFields(List<Entry> listFields) {
		this.listFields = listFields;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public List<ItemList> getListGrid() {
		return listGrid;
	}

	public List<ItemList> getListFeature() {
		return listFeature;
	}

	public void setListGrid(List<ItemList> listGrid) {
		this.listGrid = listGrid;
	}

	public void setListFeature(List<ItemList> listFeature) {
		this.listFeature = listFeature;
	}

	public String getPathBrowser() {
		return pathBrowser;
	}

	public void setPathBrowser(String pathBrowser) {
		this.pathBrowser = pathBrowser;
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

	public DynamicForm getDynamicFormBrowser() {
		return DynamicFormBrowser;
	}

	public void setDynamicFormBrowser(DynamicForm dynamicFormBrowser) {
		DynamicFormBrowser = dynamicFormBrowser;
	}

	public List<OutputForm> getOutputFormList() {
		return outputFormList;
	}

	public void setOutputFormList(List<OutputForm> outputFormList) {
		this.outputFormList = outputFormList;
	}

	public String getNameOutput() {
		return nameOutput;
	}

	public void setNameOutput(String nameOutput) {
		this.nameOutput = nameOutput;
	}

	public boolean isRenderOutputTab() throws RemoteException{
		return getDfe() != null && getDfe().getOozieAction() != null;
	}

	public Map<String, List<String[]>> getFunctionsMap() {
		return functionsMap;
	}

	public void setFunctionsMap(Map<String, List<String[]>> functionsMap) {
		this.functionsMap = functionsMap;
	}

	public Map<String, String> getColumnsMap() {
		return columnsMap;
	}

	public void setColumnsMap(Map<String, String> columnsMap) {
		this.columnsMap = columnsMap;
	}

	public Map<String, List<Map<String, String>>> getRowsMap() {
		return rowsMap;
	}

	public void setRowsMap(Map<String, List<Map<String, String>>> rowsMap) {
		this.rowsMap = rowsMap;
	}

	public String getCommandEdit() {
		return commandEdit;
	}

	public void setCommandEdit(String commandEdit) {
		this.commandEdit = commandEdit;
	}

	public Map<String, List<SelectItem>> getListConstraint() {
		return listConstraint;
	}

	public void setListConstraint(Map<String, List<SelectItem>> listConstraint) {
		this.listConstraint = listConstraint;
	}

	public String getColumnEdit() {
		return columnEdit;
	}

	public void setColumnEdit(String columnEdit) {
		this.columnEdit = columnEdit;
	}

	public int getRowEdit() {
		return rowEdit;
	}

	public void setRowEdit(int rowEdit) {
		this.rowEdit = rowEdit;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getPathImage() {
		return pathImage;
	}

	public void setPathImage(String pathImage) {
		this.pathImage = pathImage;
	}

	public String getTabTitle() {
		return tabTitle;
	}

	public String getTabLegend() {
		return tabLegend;
	}

	public void setTabTitle(String tabTitle) {
		this.tabTitle = tabTitle;
	}

	public void setTabLegend(String tabLegend) {
		this.tabLegend = tabLegend;
	}

}