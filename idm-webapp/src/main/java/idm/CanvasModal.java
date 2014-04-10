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
import idm.useful.IdmEntry;
import idm.useful.MessageUseful;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.ajax4jsf.model.KeepAlive;
import org.apache.log4j.Logger;

/**
 * CanvasModal
 * 
 * Class to class control of the dynamic form
 * 
 * @author Igor.Souza
 */
@KeepAlive
public class CanvasModal extends BaseBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6582760940477306907L;

	public static final String sourceOutName = "";

	private static Logger logger = Logger.getLogger(CanvasModal.class);
	private CanvasBean canvasBean;
	private String list = "";
	private String listOp = "";
	private String selectedGenerator = "";
	private List<SelectItem> listItens = new ArrayList<SelectItem>();
	private List<SelectItem> listItensTable = new ArrayList<SelectItem>();
	private List<SelectItem> listItensTableOperation = new ArrayList<SelectItem>();
	private Map<String, List<SelectItem>> listConstraint = new HashMap<String, List<SelectItem>>();
	private List<String[]> listFunctions = new ArrayList<String[]>();
	private List<String[]> listOperation = new ArrayList<String[]>();
	private List<Entry<String, String>> listFields = new ArrayList<Entry<String, String>>();
	private Map<String, List<String[]>> functionsMap = new HashMap<String, List<String[]>>();
	private Map<String, List<String[]>> operationMap = new HashMap<String, List<String[]>>();
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
	private String firstPage = "Y";
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
	private DynamicForm dynamicFormDataOutput;
	private String columnEdit;
	private int rowEdit;
	private String errorMsg;
	private String pathImage;
	private String tabTitle;
	private String tabLegend;
	private List<String> tableInteractionsColumns = new ArrayList<String>();
	private List<String> browserNameFeatureColumns = new ArrayList<String>();
	private String selectedTab;
	private String showOutputForm;
	private String hiveHdfs;
	private Map<String, String> nameBrowserLabel1 = new HashMap<String, String>();
	private Map<String, String> nameBrowserLabel2 = new HashMap<String, String>();
	private String confirm = "N";
	private String openPopUp = "S";
	private List<String> listOutput = new ArrayList<String>();

	/**
	 * Update the element when closing.
	 */
	private boolean elementToUpdate = false;

	/**
	 * getKeyAsListNameValue
	 * 
	 * Method to retrieve the list of files
	 * 
	 * @return List<String>
	 * @author Igor.Souza
	 */
	public List<String> getKeyAsListNameValueListGrid() {
		return new ArrayList<String>(nameValueListGrid.keySet());
	}

	public List<String> getKeyAsListNameValueFeature() {
		return new ArrayList<String>(nameValueFeature.keySet());
	}

	public List<String> getTableInteractionColumns() {
		return tableInteractionsColumns;
	}

	/**
	 * applyPage
	 * 
	 * Methods to apply the page
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void applyPage() throws RemoteException {

		logger.info("applyPage ");

		String error = checkNextPage();
		if (error.length() > 1) {
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

		setErrorMsg(error);

	}

	/**
	 * nextPage
	 * 
	 * Methods to control the sequence of screens
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void nextPage() throws RemoteException {

		logger.info("nextPage ");

		try {

			String error = checkNextPage();
			if (error.length() > 1) {
				MessageUseful.addErrorMessage(error);
				HttpServletRequest request = (HttpServletRequest) FacesContext
						.getCurrentInstance().getExternalContext().getRequest();
				request.setAttribute("msnError", "msnError");
			} else {

				logger.info("check nextPage Ok ");

				setListPosition(getListPosition() + 1);

				logger.info("getListPosition()+1 " + getListPosition());

				logger.info(getListPage().size());

				setPage(getListPage().get(getListPosition()));

				if (getListPageSize() - 1 > getListPosition()) {
					setLastPage("N");
				} else {
					setLastPage("Y");
				}

				if (getListPosition() == 0) {
					setFirstPage("Y");
				} else {
					setFirstPage("N");
				}

				mountInteractionForm(getListPosition());
			}

		} catch (Exception e) {
			logger.error(e);
			MessageUseful
					.addErrorMessage(getMessageResources("msg_error_oops"));
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

	}

	/**
	 * checkNextPage
	 * 
	 * Method to check the fields before change to the next page
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public String checkNextPage() throws RemoteException {

		StringBuffer error = new StringBuffer();
		boolean updateTable = false;

		for (int i = 0; i < getDynamicFormList().size(); i++) {

			DynamicForm dynamicF = getDynamicFormList().get(i);
			DFEInteraction dfi = getPage().getInteractions().get(i);

			boolean interactionChanged = false;
			if (dynamicF.getDisplayType().equals(DisplayType.input)) {

				if (dynamicF.getInputValue() != null) {
					try {
						interactionChanged = !dynamicF.getInputValue().equals(
								dynamicF.getTree().getFirstChild("input")
										.getFirstChild("output")
										.getFirstChild().getHead());
					} catch (Exception e) {
						interactionChanged = true;
					}
					if (interactionChanged) {
						dynamicF.getTree().getFirstChild("input")
								.getFirstChild("output").removeAllChildren();
						dynamicF.getTree().getFirstChild("input")
								.getFirstChild("output")
								.add(dynamicF.getInputValue());
					}
				}

			} else if (dynamicF.getDisplayType().equals(DisplayType.list)) {

				logger.info("value list -> "
						+ dynamicF.getSelectedListOptions());
				try {
					interactionChanged = !dynamicF.getSelectedListOptions()
							.equals(dynamicF.getTree().getFirstChild("list")
									.getFirstChild("output").getFirstChild()
									.getHead());
				} catch (Exception e) {
					interactionChanged = true;
				}
				if (interactionChanged) {
					dynamicF.getTree().getFirstChild("list")
							.getFirstChild("output").removeAllChildren();
					dynamicF.getTree().getFirstChild("list")
							.getFirstChild("output")
							.add(dynamicF.getSelectedListOptions());
				}

			} else if (dynamicF.getDisplayType().equals(DisplayType.appendList)) {
				try {
					List<String> oldValues = new LinkedList<String>();
					Iterator<Tree<String>> it = dynamicF.getTree()
							.getFirstChild("applist").getFirstChild("output")
							.getChildren("value").iterator();
					while (it.hasNext()) {
						oldValues.add(it.next().getFirstChild().getHead());
					}
					interactionChanged = !dynamicF
							.getSelectedAppendListOptions().equals(oldValues);
				} catch (Exception e) {
					interactionChanged = true;
				}
				if (interactionChanged) {
					dynamicF.getTree().getFirstChild("applist")
							.getFirstChild("output").removeAllChildren();
					logger.info(dynamicF.getName() + "value list size-> "
							+ dynamicF.getSelectedAppendListOptions().size());
					for (String s : dynamicF.getSelectedAppendListOptions()) {
						logger.info("appendList seleted: " + s);
						dynamicF.getTree().getFirstChild("applist")
								.getFirstChild("output").add("value").add(s);
					}
				}
			} else if (dynamicF.getDisplayType().equals(DisplayType.browser)) {

				logger.info("Browser path -> " + dynamicF.getPathBrowser());
				updateTable = true;
				try {
					// Check path
					String oldPath = dynamicF.getTree().getFirstChild("browse")
							.getFirstChild("output").getFirstChild("path")
							.getFirstChild().getHead();
					logger.info("Comparaison path: " + oldPath + " , "
							+ dynamicF.getPathBrowser());
					interactionChanged = !dynamicF.getPathBrowser().equals(
							oldPath);

					// Check properties
					if (!interactionChanged) {
						for (ItemList itemList : dynamicF.getListGrid()) {
							String key = itemList.getProperty();
							logger.info("Comparaison property "
									+ key
									+ ": "
									+ itemList.getValue()
									+ " , "
									+ dynamicF.getTree()
											.getFirstChild("browse")
											.getFirstChild("output")
											.getFirstChild("property")
											.getFirstChild(key).getFirstChild()
											.getHead());
							interactionChanged |= !dynamicF.getTree()
									.getFirstChild("browse")
									.getFirstChild("output")
									.getFirstChild("property")
									.getFirstChild(key).getFirstChild()
									.getHead().equals(itemList.getValue());
						}
					}

					// Check features
					if (!interactionChanged) {
						List<Tree<String>> oldFeatureList = dynamicF.getTree()
								.getFirstChild("browse")
								.getFirstChild("output").getChildren("feature");
						logger.info("comparaison features: "
								+ oldFeatureList.size() + " , "
								+ getBrowserNameFeatureColumns().size());
						if (!(interactionChanged |= oldFeatureList.size() != getBrowserNameFeatureColumns()
								.size())) {
							Iterator<Tree<String>> oldFeatureIt = oldFeatureList
									.iterator();
							if (getHiveHdfs() != null
									&& getHiveHdfs().equalsIgnoreCase("hive")) {
								for (String nameValue : getBrowserNameFeatureColumns()) {
									Tree<String> feature = oldFeatureIt.next();
									String value[] = nameValue.split(" ");
									logger.info("Comparaison feature: "
											+ feature.getFirstChild("name")
													.getFirstChild().getHead()
											+ " , "
											+ value[0]
											+ " | type "
											+ feature.getFirstChild("type")
													.getFirstChild().getHead()
											+ " , " + value[1]);

									if (feature.getFirstChild("name")
											.getFirstChild().getHead()
											.equals(value[0])) {
										interactionChanged |= !feature
												.getFirstChild("type")
												.getFirstChild().getHead()
												.equals(value[1]);
									} else {
										interactionChanged = true;
									}
								}
							} else if (getHiveHdfs() != null
									&& getHiveHdfs().equalsIgnoreCase("hdfs")) {
								for (String nameValue : getBrowserNameFeatureColumns()) {
									Tree<String> feature = oldFeatureIt.next();
									logger.info("Comparaison feature: "
											+ feature.getFirstChild("name")
													.getFirstChild().getHead()
											+ " , "
											+ getNameBrowserLabel1().get(
													nameValue)
											+ " | type "
											+ feature.getFirstChild("type")
													.getFirstChild().getHead()
											+ " , "
											+ getNameBrowserLabel2().get(
													nameValue));
									if (feature
											.getFirstChild("name")
											.getFirstChild()
											.getHead()
											.equals(getNameBrowserLabel1().get(
													nameValue))) {
										interactionChanged |= !feature
												.getFirstChild("type")
												.getFirstChild()
												.getHead()
												.equals(getNameBrowserLabel2()
														.get(nameValue));
									} else {
										interactionChanged = true;
									}
								}
							}
						}
					}
				} catch (Exception e) {
					interactionChanged = true;
				}
				if (interactionChanged) {
					dynamicF.getTree().getFirstChild("browse")
							.getFirstChild("output").removeAllChildren();
					dynamicF.getTree().getFirstChild("browse")
							.getFirstChild("output").add("path")
							.add(dynamicF.getPathBrowser());

					Tree<String> myProperty = dynamicF.getTree()
							.getFirstChild("browse").getFirstChild("output")
							.add("property");
					for (ItemList itemList : dynamicF.getListGrid()) {
						logger.info("Add property: " + itemList.getProperty()
								+ ": " + itemList.getValue());
						myProperty.add(itemList.getProperty()).add(
								itemList.getValue());
						// getDfe().getDFEOutput().get(sourceOutName).addProperty(itemList.getProperty(),
						// itemList.getValue());
					}

					if (getHiveHdfs() != null
							&& getHiveHdfs().equalsIgnoreCase("hive")) {
						for (String nameValue : getBrowserNameFeatureColumns()) {
							Tree<String> myFeature = dynamicF.getTree()
									.getFirstChild("browse")
									.getFirstChild("output").add("feature");
							String value[] = nameValue.split(" ");
							myFeature.add("name").add(value[0]);
							myFeature.add("type").add(value[1]);
						}
					} else if (getHiveHdfs() != null
							&& getHiveHdfs().equalsIgnoreCase("hdfs")) {
						for (String nameValue : getBrowserNameFeatureColumns()) {
							Tree<String> myFeature = dynamicF.getTree()
									.getFirstChild("browse")
									.getFirstChild("output").add("feature");

							logger.info("nameValue " + nameValue);

							logger.info("update NameBrowserLabel = "
									+ getNameBrowserLabel1().get(nameValue)
									+ " -> "
									+ getNameBrowserLabel2().get(nameValue));
							logger.info(getNameBrowserLabel1());
							logger.info(getNameBrowserLabel2());

							myFeature.add("name").add(
									getNameBrowserLabel1().get(nameValue));
							myFeature.add("type").add(
									getNameBrowserLabel2().get(nameValue));

						}
					}
				}

			} else if (dynamicF.getDisplayType().equals(
					DisplayType.helpTextEditor)) {
				try {
					interactionChanged = !getCommand().equals(
							dynamicF.getTree().getFirstChild("editor")
									.getFirstChild("output").getFirstChild()
									.getHead());
				} catch (Exception e) {
					interactionChanged = true;
				}
				if (interactionChanged) {
					dynamicF.getTree().getFirstChild("editor")
							.getFirstChild("output").removeAllChildren();
					dynamicF.getTree().getFirstChild("editor")
							.getFirstChild("output").add(getCommand());
				}
			} else if (dynamicF.getDisplayType().equals(DisplayType.table)) {
				try {

					Iterator<Tree<String>> oldColumns = dynamicF.getTree()
							.getFirstChild("table").getChildren("row")
							.iterator();
					for (ItemList item : getListGrid()) {
						Tree<String> row = oldColumns.next();
						for (String column : getKeyAsListNameValueListGrid()) {
							String value = item.getNameValue().get(column);
							// logger.info("Comparaison "+column+": "+value+" , "+row.getFirstChild(column).getFirstChild().getHead());
							interactionChanged |= !row.getFirstChild(column)
									.getFirstChild().getHead().equals(value);
						}
					}
				} catch (Exception e) {
					interactionChanged = true;
				}
				if (interactionChanged) {
					dynamicF.getTree().getFirstChild("table").remove("row");

					for (ItemList item : getListGrid()) {
						Tree<String> row = dynamicF.getTree()
								.getFirstChild("table").add("row");
						logger.info("Table row");
						for (String column : getKeyAsListNameValueListGrid()) {
							String value = item.getNameValue().get(column);
							row.add(column).add(value);
							logger.info(column + " -> " + value);
						}
					}
				}
			}
			if (interactionChanged) {
				logger.info("Interaction "
						+ getPage().getInteractions().get(i).getName()
						+ " changed");
			}
			elementToUpdate |= interactionChanged;

			getPage().getInteractions().set(i, dfi);
			String e = dfi.check();
			logger.info("error interaction -> " + e);
			if (e != null) {
				error.append(e);
				error.append(System.getProperty("line.separator"));
			}
		}

		String e = getPage().checkPage();
		logger.info("error page -> " + e);
		if (e != null) {
			error.append(e);
			error.append(System.getProperty("line.separator"));
		} else {

			logger.info("updateTable " + updateTable);

			if (updateTable) {
				// Update table
				updateDFEOUtputTable(
						getDfe().getDFEOutput().get(sourceOutName),
						getDynamicFormBrowser());
			}
			// Update output only if it is the last page
			// or an output already exist
			if (getListPageSize() - 1 == getListPosition()) {
				e = close();
				if (e != null) {
					error.append(e);
					error.append(System.getProperty("line.separator"));
				}
				mountOutputForm();

			}
		}
		return error.toString();
	}

	/**
	 * previous
	 * 
	 * Methods to control the sequence of screens
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void previousPage() throws RemoteException {

		logger.info("previousPage ");
		// Save current page
		// checkNextPage();

		setListPosition(getListPosition() - 1);

		setPage(getListPage().get(getListPosition()));

		if (getListPageSize() - 1 > getListPosition()) {
			setLastPage("N");
		} else {
			setLastPage("Y");
		}

		if (getListPosition() == 0) {
			setFirstPage("Y");
		} else {
			setFirstPage("N");
		}

		mountInteractionForm(getListPosition());

	}

	/**
	 * close
	 * 
	 * Methods to control the sequence of screens
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public String close() throws RemoteException {
		String error = null;
		try {
			logger.info("close ");
			if (elementToUpdate) {
				logger.info("Remove element data");
				getDfe().cleanThisAndAllElementAfter();
			}
			error = getDfe().updateOut();
			if (error != null) {
				MessageUseful.addErrorMessage(error);
			}
		} catch (Exception e) {
		}
		return error;
	}

	public String printTree(Tree<String> tree) throws RemoteException {
		String ans = "";
		String head = tree.getHead();
		if (head != null) {
			ans = head.toString();
		}
		Iterator<Tree<String>> it = tree.getSubTreeList().iterator();
		while (it.hasNext()) {
			ans = ans + "\n\t" + printTree(it.next()).replaceAll("\n", "\n\t");
		}

		return ans;
	}

	/**
	 * start
	 * 
	 * Methods to start the control of sequence of screens
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void start() {
		FacesContext context = FacesContext.getCurrentInstance();
		canvasBean = (CanvasBean) context.getApplication()
				.evaluateExpressionGet(context, "#{canvasBean}",
						CanvasBean.class);

		String nameWf = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("paramNameWorkflow");
		setNameWorkflow(nameWf);

		try {

			setDfi(getworkFlowInterface());
			setDf(dfi.getWorkflow(getNameWorkflow()));

		} catch (RemoteException e) {
			logger.error(e.getMessage());
		}

	}

	/**
	 * openTextEditor
	 * 
	 * Methods to mount the dynamic form
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void openCanvasModal() throws RemoteException {

		start();
		logger.info("openCanvasModal " + getNameWorkflow());
		HttpServletRequest request = (HttpServletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest();
		request.removeAttribute("msnError");
		
		String selTab = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("paramSelectedTab");
		if(selTab == null || selTab.isEmpty()||selTab.equalsIgnoreCase("undefined")){
			selTab = "confTabCM";
		}
		logger.info("selected tab: "+selTab);

		// Don't update the element if you it close immediately
		elementToUpdate = false;

		// set the first tab for obj
		setSelectedTab(selTab);

		logger.info("open group id " + getGroupID());
		logger.info("size of map "
				+ canvasBean.getIdMap().get(getNameWorkflow()).size());

		setNameElement(canvasBean.getIdMap().get(getNameWorkflow())
				.get(getGroupID()));
		setDynamicFormDataOutput(null);

		logger.info("open element id " + getNameElement());

		if (getPathImage() != null && getNameElement() != null) {

			try {

				setDfe(getDf().getElement(getNameElement()));

				// validate if you can open or not the dynamic form of the
				// object
				String error = getDfe().checkIn();

				logger.info("error " + error);

				if (error != null) {

					MessageUseful.addErrorMessage(error);
					request.setAttribute("msnError", "msnError");

				} else {

					setListPage(getDfe().getPageList());

					setListPageSize(getListPage().size());

					// initialise the position of list
					setListPosition(0);

					// retrieves the correct page
					setPage(getListPage().get(getListPosition()));

					setPageTitle(getDfe().getName().replace("_", " "));

					mountInteractionForm(getListPosition());

					setFirstPage("Y");

					logger.info("List size " + getListPageSize());

					if (getListPageSize() - 1 > getListPosition()) {
						setLastPage("N");
					} else {
						setLastPage("Y");
					}

				}

			} catch (RemoteException e) {
				e.printStackTrace();
				logger.error(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage());
			}

			// mount output tab
			mountOutputForm();

		}

	}

	/**
	 * mountInteractionForm
	 * 
	 * Method to mount the new list of Interaction
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void mountInteractionForm(int page) throws RemoteException {

		logger.info("mountInteractionForm ");

		try {

			setDynamicFormList(new ArrayList<DynamicForm>());
			getDfe().update(page);

			for (DFEInteraction dfeInteraction : getDfe().getPageList()
					.get(page).getInteractions()) {

				DynamicForm dynamicF = new DynamicForm();

				/*
				 * logger.info("type " + dfeInteraction.getName() + " " +
				 * dfeInteraction.getDisplay() + " " +
				 * printTree(dfeInteraction.getTree()));
				 */

				setTabTitle(getPage().getTitle());
				setTabLegend(getPage().getLegend());

				dynamicF.setName(dfeInteraction.getName().replace("_", " "));
				dynamicF.setLegend(dfeInteraction.getLegend());
				dynamicF.setDisplayType(dfeInteraction.getDisplay());
				dynamicF.setTree(dfeInteraction.getTree());

				if (dfeInteraction.getDisplay().equals(DisplayType.input)) {

					String value = dfeInteraction.getTree()
							.getFirstChild("input").getFirstChild("output")
							.getFirstChild().getHead();
					dynamicF.setInputValue(value);
					String regex = dfeInteraction.getTree()
							.getFirstChild("input").getFirstChild("regex")
							.getFirstChild().getHead();
					dynamicF.setInputRegex(regex);

				} else if (dfeInteraction.getDisplay().equals(DisplayType.list)) {

					List<SelectItem> selectItems = new ArrayList<SelectItem>();
					Tree<String> dfetree = dfeInteraction.getTree();
					// logger.info("got tree");
					Tree<String> lists = dfetree.getFirstChild("list");
					// logger.info("got tree -> list");
					Tree<String> values = lists.getFirstChild("values");
					// logger.info("got tree -> list -> values");
					List<Tree<String>> list = values.getSubTreeList();
					// logger.info("got tree -> list -> values -> tree");

					// logger.info("list value " + list);

					if (list != null) {
						for (Tree<String> tree : list) {
							logger.info("list value "
									+ tree.getFirstChild().getHead());
							selectItems
									.add(new SelectItem(tree.getFirstChild()
											.getHead(), tree.getFirstChild()
											.getHead()));
						}
						dynamicF.setListOptions(selectItems);
						dynamicF.setSelectedListOptions(selectItems.get(0)
								.getLabel());
					}

					if (dfeInteraction.getTree().getFirstChild("list")
							.getFirstChild("output").getFirstChild() != null) {
						String value = dfeInteraction.getTree()
								.getFirstChild("list").getFirstChild("output")
								.getFirstChild().getHead();
						logger.info("value default -> " + value);
						dynamicF.setSelectedListOptions(value);
					}

					// check display type
					if (dfeInteraction.getTree().getFirstChild("list")
							.getFirstChild("display") != null
							&& dfeInteraction.getTree().getFirstChild("list")
									.getFirstChild("display").getFirstChild() != null) {
						String displayType = dfeInteraction.getTree()
								.getFirstChild("list").getFirstChild("display")
								.getFirstChild().getHead();
						if (displayType.equalsIgnoreCase("combobox")) {
							dynamicF.setComboBox("Y");
						} else {
							dynamicF.setComboBox("N");
						}
					} else {
						dynamicF.setComboBox("Y");
					}

				} else if (dfeInteraction.getDisplay().equals(
						DisplayType.appendList)) {

					logger.info("appendList");
					List<SelectItem> selectItems = new ArrayList<SelectItem>();
					if (dfeInteraction.getTree().getFirstChild("applist")
							.getFirstChild("values") != null) {
						List<Tree<String>> list = dfeInteraction.getTree()
								.getFirstChild("applist")
								.getFirstChild("values").getChildren("value");
						if (list != null) {
							logger.info("list not null: " + list.size());
							for (Tree<String> tree : list) {
								logger.info("list value "
										+ tree.getFirstChild().getHead());
								selectItems.add(new SelectItem(tree
										.getFirstChild().getHead(), tree
										.getFirstChild().getHead()));
							}
							dynamicF.setAppendListOptions(selectItems);

							/*
							 * if (selectItems.size() > 10) {
							 * dynamicF.setComboBox("Y"); } else {
							 * dynamicF.setComboBox("N"); }
							 */
						}
					}

					if (dfeInteraction.getTree().getFirstChild("applist")
							.getFirstChild("output") != null) {
						if (dfeInteraction.getTree().getFirstChild("applist")
								.getFirstChild("output").getChildren("value") != null) {
							List<Tree<String>> listOut = dfeInteraction
									.getTree().getFirstChild("applist")
									.getFirstChild("output")
									.getChildren("value");
							if (listOut != null) {
								List<String> listSelected = new ArrayList<String>();
								for (Tree<String> tree : listOut) {
									listSelected.add(tree.getFirstChild()
											.getHead());
								}
								dynamicF.setSelectedAppendListOptions(listSelected);
							}
						}
					}

					// check display type
					if (dfeInteraction.getTree().getFirstChild("applist")
							.getFirstChild("display") != null
							&& dfeInteraction.getTree()
									.getFirstChild("applist")
									.getFirstChild("display").getFirstChild() != null) {
						String displayType = dfeInteraction.getTree()
								.getFirstChild("applist")
								.getFirstChild("display").getFirstChild()
								.getHead();
						if (displayType.equalsIgnoreCase("combobox")) {
							dynamicF.setComboBox("Y");
						} else {
							dynamicF.setComboBox("N");
						}
					} else {
						dynamicF.setComboBox("Y");
					}

				} else if (dfeInteraction.getDisplay().equals(
						DisplayType.browser)) {

					// clean the map
					setListFeature(new ArrayList<ItemList>());
					setListGrid(new ArrayList<ItemList>());
					setBrowserNameFeatureColumns(new ArrayList<String>());

					String dataTypeName = dfeInteraction.getTree()
							.getFirstChild("browse").getFirstChild("type")
							.getFirstChild().getHead();
					logger.info("dataTypeName " + dataTypeName);
					dynamicF.setDataTypeName(dataTypeName);
					if (dataTypeName.equalsIgnoreCase("HDFS")) {
						String subtypeName = dfeInteraction.getTree()
								.getFirstChild("browse")
								.getFirstChild("subtype").getFirstChild()
								.getHead();
						logger.info("subtypeName " + subtypeName);
						dynamicF.setSubtypeName(subtypeName);
					}

					if (dfeInteraction.getTree().getFirstChild("browse")
							.getFirstChild("output").getFirstChild("path") != null) {
						String mypath = dfeInteraction.getTree()
								.getFirstChild("browse")
								.getFirstChild("output").getFirstChild("path")
								.getFirstChild().getHead();
						dynamicF.setPathBrowser(mypath);
						logger.info("path mount " + mypath);
						if (!mypath.startsWith("/")) {
							mypath = "/" + mypath;
						}
						setPathBrowser(mypath);
						setDynamicFormBrowser(dynamicF);
						changePathBrowser();
					}

				} else if (dfeInteraction.getDisplay().equals(
						DisplayType.helpTextEditor)) {
					setCommand("");

					if (dfeInteraction.getTree().getFirstChild("editor")
							.getFirstChild("output").getFirstChild() != null) {
						setCommand(dfeInteraction.getTree()
								.getFirstChild("editor")
								.getFirstChild("output").getFirstChild()
								.getHead());
					}

					mountHelpTextEditorInteraction(dfeInteraction.getTree());

				} else if (dfeInteraction.getDisplay()
						.equals(DisplayType.table)) {

					setListConstraint(new HashMap<String, List<SelectItem>>());

					Map<String, List<Map<String, String>>> map = new HashMap<String, List<Map<String, String>>>();
					List<SelectItem> listFields = new ArrayList<SelectItem>();

					if (dfeInteraction.getTree().getFirstChild("table")
							.getFirstChild("generator") != null) {
						List<Tree<String>> list = dfeInteraction.getTree()
								.getFirstChild("table")
								.getFirstChild("generator").getSubTreeList();
						if (list != null) {
							for (Tree<String> tree : list) {
								logger.info(printTree(tree));
								logger.info("list value "
										+ tree.getFirstChild().getHead());
								SelectItem e = new SelectItem(tree
										.getFirstChild("title").getFirstChild()
										.getHead(), tree.getFirstChild("title")
										.getFirstChild().getHead());
								listFields.add(e);
								map.put(tree.getFirstChild("title")
										.getFirstChild().getHead(),
										new ArrayList<Map<String, String>>());

								for (Tree<String> treeRows : tree
										.getChildren("row")) {
									Map<String, String> t = new HashMap<String, String>();
									for (Tree<String> treeFeat : treeRows
											.getSubTreeList()) {
										logger.info(printTree(treeFeat));
										t.put(treeFeat.getHead(), treeFeat
												.getFirstChild().getHead());
									}
									map.get(tree.getFirstChild("title")
											.getFirstChild().getHead()).add(t);
								}
							}
						}
					}
					setRowsMap(map);
					setListItens(listFields);

					if (!listFields.isEmpty()) {
						logger.info("link selected: "
								+ listFields.get(0).getValue().toString());
						setSelectedGenerator(listFields.get(0).getValue()
								.toString());
					} else {
						setSelectedGenerator(null);
					}

					Map<String, String> mapColumns = new HashMap<String, String>();
					List<Tree<String>> list2 = dfeInteraction.getTree()
							.getFirstChild("table").getFirstChild("columns")
							.getSubTreeList();
					tableInteractionsColumns = new ArrayList<String>();
					if (list2 != null) {
						for (Tree<String> tree : list2) {
							logger.info("list2 value " + tree.getHead());
							String aux = null;
							if (tree.getFirstChild("constraint") != null) {
								if (tree.getFirstChild("constraint")
										.findFirstChild("values") != null) {
									aux = "comboBox";
									mountTableInteractionConstraint(tree);
								} else {
									aux = "textField";
								}
							} else if (tree.getFirstChild("editor") != null) {
								aux = "editor";
								mountHelpTextEditorInteraction(tree);
							} else {
								aux = "textField";
							}
							String ans = "";
							if (tree.getHead() != null) {
								ans = tree.getHead().toString();
							}
							Iterator<Tree<String>> it = tree.getSubTreeList()
									.iterator();
							while (it.hasNext()) {
								ans = ans
										+ "\n\t"
										+ it.next().toString()
												.replaceAll("\n", "\n\t");
							}
							logger.info(aux);
							// logger.info(ans);
							mapColumns.put(tree.getFirstChild("title")
									.getFirstChild().getHead(), aux);
							tableInteractionsColumns.add(tree
									.getFirstChild("title").getFirstChild()
									.getHead());
						}
					}
					setColumnsMap(mapColumns);

					nameValueListGrid = new HashMap<String, String>();
					Map<String, String> columnsMap = new HashMap<String, String>();
					for (String column : getColumnsMap().keySet()) {
						columnsMap.put(column, column);
					}
					setNameValueListGrid(columnsMap);

					List<ItemList> listGrid = new ArrayList<ItemList>();
					if (dfeInteraction.getTree().getFirstChild("table")
							.getChildren("row") != null) {
						List<Tree<String>> list = dfeInteraction.getTree()
								.getFirstChild("table").getChildren("row");
						for (Tree<String> rows : list) {

							ItemList item = new ItemList();

							for (Tree<String> row : rows.getSubTreeList()) {
								item.getNameValue().put(row.getHead(),
										row.getFirstChild().getHead());
								logger.info(row.getHead() + " -> "
										+ row.getFirstChild().getHead());
							}
							listGrid.add(item);
						}
					}
					setListGrid(listGrid);
					setList(null);
				}

				getDynamicFormList().add(dynamicF);

			}

		} catch (Exception e) {
			logger.error(e);
			MessageUseful
					.addErrorMessage(getMessageResources("msg_error_oops"));
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

	}

	private void mountTableInteractionConstraint(Tree<String> dfeInteractionTree)
			throws RemoteException {
		List<SelectItem> listFields = new ArrayList<SelectItem>();
		if (dfeInteractionTree.getFirstChild("constraint").getFirstChild(
				"values") != null) {
			List<Tree<String>> list = dfeInteractionTree
					.getFirstChild("constraint").getFirstChild("values")
					.getSubTreeList();

			if (list != null) {
				// logger.info("list not null: " + list.toString());
				for (Tree<String> tree : list) {
					logger.info("list value " + tree.getFirstChild().getHead());
					listFields.add(new SelectItem(tree.getFirstChild()
							.getHead(), tree.getFirstChild().getHead()));
				}
			}
			getListConstraint().put(
					dfeInteractionTree.getFirstChild("title").getFirstChild()
							.getHead(), listFields);
		}
	}

	private void mountHelpTextEditorInteraction(Tree<String> dfeInteractionTree)
			throws RemoteException {

		List<Entry<String, String>> listFields = new ArrayList<Entry<String, String>>();
		List<Tree<String>> list = null;
		try {
			list = dfeInteractionTree.getFirstChild("editor")
					.getFirstChild("keywords").getSubTreeList();
		} catch (Exception e) {
			list = null;
		}
		if (list != null && !list.isEmpty()) {
			// logger.info("list not null: "+list.toString());
			for (Tree<String> tree : list) {
				// logger.info("list value " + tree.getFirstChild().getHead());
				Entry<String, String> e = new IdmEntry<String, String>(tree
						.getFirstChild("name").getFirstChild().getHead(), tree
						.getFirstChild("info").getFirstChild().getHead());
				listFields.add(e);
			}
		}
		setListFields(listFields);

		List<SelectItem> listCategories = new ArrayList<SelectItem>();
		List<SelectItem> listCategoriesOperation = new ArrayList<SelectItem>();
		try {
			list = dfeInteractionTree.getFirstChild("editor")
					.getFirstChild("help").getSubTreeList();
		} catch (Exception e) {
			list = null;
		}
		if (list != null) {
			// logger.info("list not null: " + list.toString());
			for (Tree<String> tree : list) {
				// logger.info("list value " + tree.getHead());
				if (tree.getHead().startsWith("operation_")) {
					String valueOperation[] = tree.getHead().split("_");
					// logger.info("list value startsWith: " +
					// valueOperation[1]);
					SelectItem e = new SelectItem(valueOperation[1],
							valueOperation[1]);
					listCategoriesOperation.add(e);
				} else {
					SelectItem e = new SelectItem(tree.getHead(),
							tree.getHead());
					listCategories.add(e);
				}
			}
		} else {
			// does not open pop up in text editor
			setOpenPopUp("N");
		}

		setListItensTable(listCategories);
		setListItensTableOperation(listCategoriesOperation);

		Map<String, List<String[]>> map = new HashMap<String, List<String[]>>();
		Map<String, List<String[]>> mapOp = new HashMap<String, List<String[]>>();
		try {
			list = dfeInteractionTree.getFirstChild("editor")
					.getFirstChild("help").getSubTreeList();
		} catch (Exception e) {
			list = null;
		}
		if (list != null && !list.isEmpty()) {
			// logger.info("list not null: " + list.toString());
			for (Tree<String> tree : list) {
				logger.info("list value " + tree.getHead());

				if (tree.getHead().startsWith("operation_")) {

					String valueOperation[] = tree.getHead().split("_");

					if (!mapOp.containsKey(valueOperation[1])) {
						mapOp.put(valueOperation[1], new ArrayList<String[]>());
					}

					for (Tree<String> tree2 : tree.getSubTreeList()) {

						String nameFunction = tree2.getFirstChild("name")
								.getFirstChild() != null ? tree2
								.getFirstChild("name").getFirstChild()
								.getHead() : "";
						String inputFunction = tree2.getFirstChild("input")
								.getFirstChild() != null ? tree2
								.getFirstChild("input").getFirstChild()
								.getHead() : "";
						String returnFunction = tree2.getFirstChild("return")
								.getFirstChild() != null ? tree2
								.getFirstChild("return").getFirstChild()
								.getHead() : "";
						String helpFunction = tree2.getFirstChild("help")
								.getFirstChild() != null ? tree2
								.getFirstChild("help").getFirstChild()
								.getHead() : "";

						mapOp.get(valueOperation[1]).add(
								new String[] { nameFunction, inputFunction,
										returnFunction, helpFunction });
					}

				} else {

					if (!map.containsKey(tree.getHead())) {
						map.put(tree.getHead(), new ArrayList<String[]>());
					}

					for (Tree<String> tree2 : tree.getSubTreeList()) {

						String nameFunction = tree2.getFirstChild("name")
								.getFirstChild() != null ? tree2
								.getFirstChild("name").getFirstChild()
								.getHead() : "";
						String inputFunction = tree2.getFirstChild("input")
								.getFirstChild() != null ? tree2
								.getFirstChild("input").getFirstChild()
								.getHead() : "";
						String returnFunction = tree2.getFirstChild("return")
								.getFirstChild() != null ? tree2
								.getFirstChild("return").getFirstChild()
								.getHead() : "";
						String helpFunction = tree2.getFirstChild("help")
								.getFirstChild() != null ? tree2
								.getFirstChild("help").getFirstChild()
								.getHead() : "";

						map.get(tree.getHead()).add(
								new String[] { nameFunction, inputFunction,
										returnFunction, helpFunction });
					}

				}

			}
		} else {
			// does not open pop up in text editor
			setOpenPopUp("N");
		}

		setFunctionsMap(map);
		setOperationMap(mapOp);

		if (getListItensTable() != null && !getListItensTable().isEmpty()) {

			// logger.info("list getListItensTable "
			// + getListItensTable().get(0).getLabel());

			setList(getListItensTable().get(0).getLabel());
			setListFunctions(getFunctionsMap().get(getList()));
		}

		if (getListItensTableOperation() != null
				&& !getListItensTableOperation().isEmpty()) {

			// logger.info("list getListItensTableOperation "
			// + getListItensTableOperation().get(0).getLabel());

			setListOp(getListItensTableOperation().get(0).getLabel());
			setListOperation(getOperationMap().get(getListOp()));
		}

	}

	/**
	 * endDynamicForm
	 * 
	 * Methods to process the dynamic form
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void endDynamicForm() throws RemoteException {

		logger.info("endDynamicForm ");

		applyPage();

	}

	/**
	 * openTableInteraction
	 * 
	 * Methods to mount screen Table Interaction
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void openTableInteraction() {

	}

	/**
	 * confirm
	 * 
	 * Methods to confirm this action.
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void confirm() throws RemoteException {

		logger.info("confirm");

		setConfirm("S");
		boolean success = checkTextEditor();

		if (success) {
			if (getColumnEdit() != null) {
				getListGrid().get(getRowEdit()).getNameValue()
						.put(getColumnEdit(), getCommandEdit());
			} else {
				setCommand(getCommandEdit());
			}
		}

	}

	/**
	 * cancel
	 * 
	 * Methods to cancel this action
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void cancel() {

		logger.info("cancel");

	}

	/**
	 * checkTextEditor
	 * 
	 * Methods to Check if the entry are correct or not for this action.
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public boolean checkTextEditor() throws RemoteException {

		logger.info("checkTextEditor");

		boolean result = false;

		if (getColumnEdit() != null) {
			for (int i = 0; i < getDynamicFormList().size(); i++) {

				DynamicForm dynamicF = getDynamicFormList().get(i);
				DFEInteraction dfi = getPage().getInteractions().get(i);

				if (dynamicF.getDisplayType().equals(DisplayType.table)) {

					logger.info("newCommand -> " + getCommandEdit());
					String e = dfi.checkExpression(getCommandEdit(), null);
					logger.info("error interaction ->  " + e);

					if (e != null && e.length() > 0) {
						MessageUseful.addErrorMessage(e);
						HttpServletRequest request = (HttpServletRequest) FacesContext
								.getCurrentInstance().getExternalContext()
								.getRequest();
						request.setAttribute("msnError", "msnError");
					} else {
						if (getConfirm() != null
								&& !getConfirm().equalsIgnoreCase("S")) {
							MessageUseful
									.addInfoMessage(getMessageResources("success_message"));
							HttpServletRequest request = (HttpServletRequest) FacesContext
									.getCurrentInstance().getExternalContext()
									.getRequest();
							request.setAttribute("msnError", "msnError");
						}
						result = true;
					}
				}
			}
		}

		else {
			for (int i = 0; i < getDynamicFormList().size(); i++) {

				DynamicForm dynamicF = getDynamicFormList().get(i);
				DFEInteraction dfi = getPage().getInteractions().get(i);

				if (dynamicF.getDisplayType()
						.equals(DisplayType.helpTextEditor)) {
					String oldCommand = null;
					if (dfi.getTree().getFirstChild("editor")
							.getFirstChild("output").getFirstChild() != null) {
						oldCommand = dfi.getTree().getFirstChild("editor")
								.getFirstChild("output").getFirstChild()
								.getHead();
					}

					logger.info("oldCommand -> " + oldCommand);
					logger.info("newCommand -> " + getCommandEdit());

					dfi.getTree().getFirstChild("editor")
							.getFirstChild("output").removeAllChildren();
					dfi.getTree().getFirstChild("editor")
							.getFirstChild("output")
							.add(getCommandEdit().trim());

					String e = dfi.check();

					logger.info("error interaction -> " + e);

					if (e != null && e.length() > 0) {
						MessageUseful.addErrorMessage(e);
						HttpServletRequest request = (HttpServletRequest) FacesContext
								.getCurrentInstance().getExternalContext()
								.getRequest();
						request.setAttribute("msnError", "msnError");
					} else {
						if (getConfirm() != null
								&& !getConfirm().equalsIgnoreCase("S")) {
							MessageUseful
									.addInfoMessage(getMessageResources("success_message"));
							HttpServletRequest request = (HttpServletRequest) FacesContext
									.getCurrentInstance().getExternalContext()
									.getRequest();
							request.setAttribute("msnError", "msnError");
						}
						result = true;
					}

					dfi.getTree().getFirstChild("editor")
							.getFirstChild("output").removeAllChildren();
					if (oldCommand != null) {
						dfi.getTree().getFirstChild("editor")
								.getFirstChild("output").add(oldCommand);
					}
				}
			}
		}

		setConfirm("N");

		return result;
	}

	/**
	 * changeFunctionsTextEditor
	 * 
	 * Methods to retrieve the new Functions
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void changeFunctionsTextEditor() {

		logger.info("changeFunctions: " + getList());

		setListFunctions(getFunctionsMap().get(getList()));
	}

	/**
	 * changeOperationTextEditor
	 * 
	 * Methods to retrieve the new Functions
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void changeOperationTextEditor() {

		logger.info("changeOperation: " + getListOp());

		setListOperation(getOperationMap().get(getListOp()));
	}

	/**
	 * tableInteractionAddNewLine
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
		logger.info("num columns: " + getColumnsMap().keySet().size());
		for (String column : getColumnsMap().keySet()) {
			type.put(column, getColumnsMap().get(column));
			value.put(column, null);
		}
		item.setTypeTableInteraction(type);
		item.setNameValue(value);

		getListGrid().add(item);
	}

	/**
	 * tableInteractionGenerationLines
	 * 
	 * Methods to add a several lines on table editor
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void tableInteractionGenerationLines() {

		logger.info("tableInteractionGenerationLines");

		List<ItemList> itemList = new ArrayList<ItemList>();
		if (getRowsMap().get(this.getSelectedGenerator()) != null) {
			for (Map<String, String> l : getRowsMap().get(
					this.getSelectedGenerator())) {

				ItemList item = new ItemList();

				Map<String, String> type = new HashMap<String, String>();
				Map<String, String> value = new HashMap<String, String>();

				for (String column : getColumnsMap().keySet()) {
					type.put(column, getColumnsMap().get(column));
					value.put(column, l.get(column));
				}
				item.setTypeTableInteraction(type);
				item.setNameValue(value);

				itemList.add(item);
			}
			getListGrid().addAll(itemList);
		}
	}

	/**
	 * tableInteractionDeleteLine
	 * 
	 * Methods to remove selected lines from table editor
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void tableInteractionDeleteLine() {

		logger.info("tableInteractionDeleteLine");

		for (Iterator<ItemList> iterator = getListGrid().iterator(); iterator
				.hasNext();) {
			ItemList itemList = (ItemList) iterator.next();

			if (itemList.isSelected()) {
				iterator.remove();
			}
		}

	}

	/**
	 * confirmTableInteraction
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

		String positionElement = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap()
				.get("numberDynamic");

		logger.info("positionElement " + positionElement);
		setDynamicFormBrowser(getDynamicFormList().get(
				Integer.parseInt(positionElement)));

		if (getDynamicFormBrowser().getDataTypeName().equalsIgnoreCase("hive")) {
			setHiveHdfs("hive");
		} else if (getDynamicFormBrowser().getDataTypeName().equalsIgnoreCase(
				"hdfs")) {
			setHiveHdfs("hdfs");
		}

	}

	/**
	 * changePathBrowser
	 * 
	 * Method to change path for the Browser screen
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void changePathBrowser() throws RemoteException {

		logger.info("changePathBrowser");

		String path = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("pathFile");

		if (path == null) {
			path = getPathBrowser();
			logger.info("getPathBrowser" + path);
		}

		logger.info("path: " + path);

		if (path != null) {

			DynamicForm dynamicForm = getDynamicFormBrowser();

			logger.info("pathFile " + path);

			getDfe().getDFEOutput().get(sourceOutName).setPath(path);
			//getDfe().getDFEOutput().get(sourceOutName).isPathExists();

			List<ItemList> listObjGrid = new ArrayList<ItemList>();
			Map<String, String> outputPropertiesMap = getDfe().getDFEOutput()
					.get(sourceOutName).getProperties();
			logger.info("outputPropertiesMap -> " + outputPropertiesMap);

			for (String value : outputPropertiesMap.keySet()) {

				ItemList item = new ItemList();
				item.setSelected(false);
				item.setProperty(value);
				item.setValue(outputPropertiesMap.get(value));

				listObjGrid.add(item);
			}
			dynamicForm.setListGrid(listObjGrid);
			setListGrid(listObjGrid);

			updateDFEOUtputTable(getDfe().getDFEOutput().get(sourceOutName),
					dynamicForm);
			dynamicForm.setPathBrowser(path);
			String msg = getDfe().getDFEOutput().get(sourceOutName).isPathValid();
			if(msg != null){
				MessageUseful.addErrorMessage(msg);
				HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
				request.setAttribute("msnError", "msnError");
			}
		}

	}

	public void displayOutput() throws RemoteException {
		String outputName = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap()
				.get("outputName");
		logger.info("display out: " + outputName);
		if (getDfe().getDFEOutput().get(outputName) != null) {
			if (dynamicFormDataOutput == null) {
				setDynamicFormDataOutput(new DynamicForm());
			}
			getDynamicFormDataOutput().setName(outputName);
			updateDFEOUtputTable(getDfe().getDFEOutput().get(outputName),
					getDynamicFormDataOutput());
		}
	}

	protected void updateDFEOUtputTable(DFEOutput dfeOut,
			DynamicForm dynamicForm) throws RemoteException {

		Map<String, String> nameValueFeature = new HashMap<String, String>();
		List<ItemList> listObj = new ArrayList<ItemList>();

		if (dfeOut.getFeatures() != null) {

			List<String> outputFeatureList = dfeOut.getFeatures()
					.getFeaturesNames();
			List<String> labels = new ArrayList<String>();

			Map<String, String> nameBrowserLabel1 = new HashMap<String, String>();
			Map<String, String> nameBrowserLabel2 = new HashMap<String, String>();

			setBrowserNameFeatureColumns(new ArrayList<String>());
			for (String outputFeature : outputFeatureList) {

				logger.info("outputFeatureNames " + outputFeature);

				FeatureType featureType = dfeOut.getFeatures().getFeatureType(
						outputFeature);

				logger.info("featureType " + featureType);

				labels.add(outputFeature + " " + featureType.toString());

				getBrowserNameFeatureColumns().add(
						outputFeature + " " + featureType.toString());

				nameBrowserLabel1.put(
						outputFeature + " " + featureType.toString(),
						outputFeature);
				nameBrowserLabel2.put(
						outputFeature + " " + featureType.toString(),
						featureType.toString());

			}

			setNameBrowserLabel1(nameBrowserLabel1);
			setNameBrowserLabel2(nameBrowserLabel2);

			List<String> outputLines = dfeOut.select(10);
			if (outputLines != null) {
				logger.info("outputLines " + outputLines);
			}

			if (outputLines != null) {
				for (String output : outputLines) {
					Map<String, String> nameValueFeatureItem = new HashMap<String, String>();
					if (output != null) {

						logger.info("Hive or Hdfs " + getHiveHdfs());

						if (getHiveHdfs() != null
								&& getHiveHdfs().equalsIgnoreCase("hive")) {

							String rows[] = output.split("'\001'");
							for (int i = 0; i < rows.length; i++) {
								logger.info("map to show " + labels.get(i)
										+ " " + rows[i]);
								nameValueFeature.put(labels.get(i), rows[i]);
								nameValueFeatureItem
										.put(labels.get(i), rows[i]);
							}
						} else {
							// String delimiter =
							// dfeOut.getProperty("delimiter");
							// if(delimiter == null || delimiter.isEmpty()){
							// delimiter = "\001";
							// }else if(delimiter.length() > 1){
							//
							// }
							String delimiter = null;
							try {
								delimiter = String.valueOf(Character
										.toChars(Integer.valueOf(dfeOut
												.getProperty("delimiter")
												.substring(1))));
							} catch (Exception e) {
								delimiter = dfeOut.getProperty("delimiter");
							}
							logger.info("delimiter " + delimiter);

							String rows[] = output.split(Pattern
									.quote(delimiter));
							for (int i = 0; i < rows.length; i++) {
								logger.info("map to show " + labels.get(i)
										+ " " + rows[i]);
								nameValueFeature.put(labels.get(i), rows[i]);
								nameValueFeatureItem
										.put(labels.get(i), rows[i]);
							}

						}

					}

					ItemList item = new ItemList();
					item.setSelected(false);
					item.setNameValue(nameValueFeatureItem);
					logger.info("new item ");
					listObj.add(item);

					logger.info("new nameValueFeature " + nameValueFeature);
					logger.info("new nameValueFeatureItem "
							+ nameValueFeatureItem);
					logger.info("getKeyAsListNameValueFeature "
							+ getKeyAsListNameValueFeature());

				}

				setNameValueFeature(nameValueFeature);
				dynamicForm.setListFeature(listObj);
				setListFeature(listObj);

			}
		}
	}

	/**
	 * confirmBrowser
	 * 
	 * Method for validating and close the browser
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void confirmBrowser() {

		logger.info("confirmBrowser");

	}

	public void mountOutputForm() throws RemoteException {

		logger.info("mountOutputForm");
		if (getDfe() != null && getDfe().getOozieAction() != null
				&& getDfe().getDFEOutput() != null
				&& !getDfe().getDFEOutput().isEmpty()) {
			setOutputFormList(new ArrayList<OutputForm>());

			for (Entry<String, DFEOutput> e : getDfe().getDFEOutput()
					.entrySet()) {
				OutputForm of = new OutputForm(e.getValue(), getDfe()
						.getComponentId(), e.getKey());

				List<SelectItem> outputList = new ArrayList<SelectItem>();
				for (SavingState s : SavingState.values()) {
					outputList.add(new SelectItem(s.toString(), s.toString()));
				}
				of.setSavingStateList(outputList);
				logger.info("saving state "
						+ e.getValue().getSavingState().toString());
				if (e.getValue().getSavingState() == SavingState.RECORDED) {
					int lastSlash = e.getValue().getPath().lastIndexOf('/');
					if (lastSlash != -1) {
						if (lastSlash == 0) {
							of.setPath("/");
						} else {
							of.setPath(e.getValue().getPath()
									.substring(0, lastSlash));
						}
						of.setFile(e.getValue().getPath()
								.substring(lastSlash + 1));
					}
				}

				getOutputFormList().add(of);
			}
		} else {
			setOutputFormList(new ArrayList<OutputForm>());
		}

		if (getOutputFormList().isEmpty()) {
			setShowOutputForm("N");
		} else {
			setShowOutputForm("Y");
		}

	}

	public void changePathOutputBrowser() throws RemoteException {

		logger.info("changePathOutputBrowser");

		String path = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("pathFile");
		logger.info("Output: " + getNameOutput() + " - path: " + path);

		for (OutputForm f : getOutputFormList()) {
			if (f.getName().equals(getNameOutput())) {
				f.setPath(path);
				getListOutput().add(path);
				logger.info("Output found: " + getNameOutput() + " - path: "
						+ path);
			}
		}

	}

	public void confirmOutput() throws RemoteException {
		logger.info("confirmOutput");

		String error = null;
		int i = 0;
		for (OutputForm f : getOutputFormList()) {

			if (getListOutput().size() == getOutputFormList().size()) {
				f.setPath(getListOutput().get(i));
			}

			logger.info("confirmOutput path " + f.getPath());

			error = f.updateDFEOutput();
			if (error != null) {
				logger.error(error);
				MessageUseful.addErrorMessage(error);
				HttpServletRequest request = (HttpServletRequest) FacesContext
						.getCurrentInstance().getExternalContext().getRequest();
				request.setAttribute("msnError", "msnError");
			}
			logger.info("output ok");

			i++;
		}

		if (error == null) {
			logger.info("close the rest at the same time...");
			error = close();
			if (error != null) {
				HttpServletRequest request = (HttpServletRequest) FacesContext
						.getCurrentInstance().getExternalContext().getRequest();
				request.setAttribute("msnError", "msnError");
			}
		}

	}

	public void openHelpTextEditorModal() {
		
		String command = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("command");

		String rowKey = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("rowKey");

		String column = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("column");

		logger.info("openHelpTextEditorModal ["+command+","+rowKey+","+column+"]");
		if (rowKey != null) {
			try{
				setRowEdit(Integer.valueOf(rowKey));
			}catch(Exception e){}
		}
		setColumnEdit(column);

		setCommandEdit(command);

		logger.info("row: " + rowKey);
		logger.info("column: " + column);
		logger.info("command: " + command);
	}

	/**
	 * openCanvas
	 * 
	 * Methods to clean the outputs from the obj
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void removeCleanOutput() throws RemoteException {

		String nameOutputClean = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap()
				.get("nameOutputClean");
		String error = null;
		for (OutputForm outputForm : getOutputFormList()) {

			if (outputForm.getName().equalsIgnoreCase(nameOutputClean)) {
				error = outputForm.getDfeOutput().remove();
			}

		}

		if (error != null) {
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

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

	public String getSelectedGenerator() {
		return selectedGenerator;
	}

	public void setSelectedGenerator(String selectedGenerator) {
		this.selectedGenerator = selectedGenerator;
	}

	public List<String[]> getListFunctions() {
		return listFunctions;
	}

	public void setListFunctions(List<String[]> listFunctions) {
		this.listFunctions = listFunctions;
	}

	public List<Entry<String, String>> getListFields() {
		return listFields;
	}

	public void setListFields(List<Entry<String, String>> listFields) {
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

	public boolean isRenderOutputTab() throws RemoteException {
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

	public String getSelectedTab() {
		return selectedTab;
	}

	public void setSelectedTab(String selectedTab) {
		this.selectedTab = selectedTab;
	}

	public CanvasBean getCanvasBean() {
		return canvasBean;
	}

	public void setCanvasBean(CanvasBean canvasBean) {
		this.canvasBean = canvasBean;
	}

	public List<String> getTableInteractionsColumns() {
		return tableInteractionsColumns;
	}

	public void setTableInteractionsColumns(
			List<String> tableInteractionsColumns) {
		this.tableInteractionsColumns = tableInteractionsColumns;
	}

	public String getShowOutputForm() {
		return showOutputForm;
	}

	public void setShowOutputForm(String showOutputForm) {
		this.showOutputForm = showOutputForm;
	}

	public List<String> getBrowserNameFeatureColumns() {
		return browserNameFeatureColumns;
	}

	public void setBrowserNameFeatureColumns(
			List<String> browserNameFeatureColumns) {
		this.browserNameFeatureColumns = browserNameFeatureColumns;
	}

	public String getHiveHdfs() {
		return hiveHdfs;
	}

	public void setHiveHdfs(String hiveHdfs) {
		this.hiveHdfs = hiveHdfs;
	}

	public Map<String, String> getNameBrowserLabel1() {
		return nameBrowserLabel1;
	}

	public Map<String, String> getNameBrowserLabel2() {
		return nameBrowserLabel2;
	}

	public void setNameBrowserLabel1(Map<String, String> nameBrowserLabel1) {
		this.nameBrowserLabel1 = nameBrowserLabel1;
	}

	public void setNameBrowserLabel2(Map<String, String> nameBrowserLabel2) {
		this.nameBrowserLabel2 = nameBrowserLabel2;
	}

	public List<String[]> getListOperation() {
		return listOperation;
	}

	public void setListOperation(List<String[]> listOperation) {
		this.listOperation = listOperation;
	}

	public List<SelectItem> getListItensTableOperation() {
		return listItensTableOperation;
	}

	public void setListItensTableOperation(
			List<SelectItem> listItensTableOperation) {
		this.listItensTableOperation = listItensTableOperation;
	}

	public String getListOp() {
		return listOp;
	}

	public void setListOp(String listOp) {
		this.listOp = listOp;
	}

	public Map<String, List<String[]>> getOperationMap() {
		return operationMap;
	}

	public void setOperationMap(Map<String, List<String[]>> operationMap) {
		this.operationMap = operationMap;
	}

	public String getConfirm() {
		return confirm;
	}

	public void setConfirm(String confirm) {
		this.confirm = confirm;
	}

	/**
	 * @return the dynamicFormDataOutput
	 */
	public DynamicForm getDynamicFormDataOutput() {
		return dynamicFormDataOutput;
	}

	/**
	 * @param dynamicFormDataOutput
	 *            the dynamicFormDataOutput to set
	 */
	public void setDynamicFormDataOutput(DynamicForm dynamicFormDataOutput) {
		this.dynamicFormDataOutput = dynamicFormDataOutput;
	}

	public String getOpenPopUp() {
		return openPopUp;
	}

	public void setOpenPopUp(String openPopUp) {
		this.openPopUp = openPopUp;
	}

	public List<String> getListOutput() {
		return listOutput;
	}

	public void setListOutput(List<String> listOutput) {
		this.listOutput = listOutput;
	}

}
