package idm;

import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEPage;
import idiro.workflow.server.interfaces.DataFlowElement;
import idm.interaction.CanvasModalInteraction;
import idm.interaction.EditorInteraction;
import idm.interaction.SelectedEditor;
import idm.interaction.TableInteraction;
import idm.useful.MessageUseful;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.ajax4jsf.model.KeepAlive;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

/**
 * Class to control the canvas modal The canvas Modal is the window that
 * configure a DataFlowElement
 * 
 * @author Igor.Souza
 */
@KeepAlive
public class CanvasModal extends BaseBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6582760940477306907L;

	private static Logger logger = Logger.getLogger(CanvasModal.class);

	/**
	 * The main canvas
	 */
	private CanvasBean canvasBean;

	/**
	 * Don't open the modal window after changing the ID.
	 */
	private boolean loadMainWindow;

	/**
	 * Front end element Id
	 */
	private String idGroup;

	/**
	 * Back-end elementId to be after calling changeIdElement. The true back-end
	 * id is componentId.
	 */
	private String elementId;

	/**
	 * The dataflow element
	 */
	private DataFlowElement dfe;

	/**
	 * The current page number (from 0)
	 */
	private int listPosition;

	/**
	 * The title of the modal
	 */
	private String canvasTitle;

	/**
	 * Path of the image associated with the modal
	 */
	private String pathImage;

	/**
	 * The title of the current wizard page
	 */
	private String pageTitle;

	/**
	 * The legend associated with the current wizard page
	 */
	private String pageLegend;

	/**
	 * Last wizard page flag ('N' or 'Y')
	 */
	private String lastPage = "N";

	/**
	 * First wizard page flag ('N' or 'Y')
	 */
	private String firstPage = "Y";

	/**
	 * Number total of wizard pages for this element
	 */
	private int listPageSize;

	/**
	 * Current error message
	 */
	private String errorMsg;

	/**
	 * The selected editor (currently opened and used by the user)
	 */
	private SelectedEditor selEditor;

	/**
	 * The selected table (either configuration with the wizards or output tab)
	 */
	private String selectedTab;

	/**
	 * True if it is a source node and hence there is no output tab
	 */
	private boolean sourceNode;

	/**
	 * Update the element when closing.
	 */
	private boolean elementToUpdate = false;

	/**
	 * List of the current interaction displayed
	 */
	private List<CanvasModalInteraction> inters = null;

	/**
	 * List of the table column titles
	 * To repeat this element is necessary because of JSF limitations.
	 * Due to this limitation only one table interaction can be used per
	 * page.
	 */
	private List<String> tablesColumnTitle = null;

	/**
	 * The output tab object
	 */
	private CanvasModalOutputTab outputTab = null;


	/**
	 * List of the FileSystem available for configuring an output.
	 */
	private static Map<String,FileSystemBean> datastores;


	public CanvasModal() throws RemoteException{
		DataFlowInterface dfi = getworkFlowInterface();
		datastores = new LinkedHashMap<String,FileSystemBean>();
		Iterator<String> storeName = dfi.getBrowsersName().iterator();
		while(storeName.hasNext()){
			String name = storeName.next();
			FileSystemBean newFS = new FileSystemBean();
			newFS.setDataStore(dfi.getBrowser(name));
			newFS.mountTable();
			datastores.put(name, newFS);
		}
	}

	/**
	 * Open Canvas Modal
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void openCanvasModal() throws RemoteException {
		logger.info("openCanvasModal");
		FacesContext context = FacesContext.getCurrentInstance();
		canvasBean = (CanvasBean) context.getApplication()
				.evaluateExpressionGet(context, "#{canvasBean}",
						CanvasBean.class);

		HttpServletRequest request = (HttpServletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest();

		// set the first tab for obj
		String selTab = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("paramSelectedTab");
		if (selTab == null || selTab.isEmpty()
				|| selTab.equalsIgnoreCase("undefined")) {
			selTab = "confTabCM";
		}
		logger.info("selected tab: " + selTab);
		setSelectedTab(selTab);

		// Don't update the element if you it close immediately
		elementToUpdate = false;

		// Get the image
		pathImage = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("paramPathImage");

		// Get the Element
		idGroup = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("paramGroupId");
		try {
			dfe = getworkFlowInterface().getWorkflow(
					canvasBean.getNameWorkflow()).getElement(
							canvasBean.getIdMap().get(canvasBean.getNameWorkflow())
							.get(idGroup));
			logger.info("Get element dfe");
		} catch (RemoteException e) {
			logger.error(e.getMessage());
		}

		String paramLMW = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("paramLoadMainWindow");
		if(paramLMW != null){
			loadMainWindow = !paramLMW.equalsIgnoreCase("false");
		}else{
			loadMainWindow = true;
		}

		if (dfe == null) {
			logger.error("The element is null!");
		}else{
			elementId = getComponentId();
			logger.info("Element id: "+elementId);
			if (loadMainWindow) {
				logger.info("load Main window");
				try {

					// validate if you can open or not the dynamic form of the
					// object
					String error = dfe.checkIn();

					logger.info("error " + error);

					if (error != null) {
						MessageUseful.addErrorMessage(error);
						request.setAttribute("msnError", "msnError");
					} else {

						// mount output tab
						outputTab = new CanvasModalOutputTab(datastores,dfe);
						Iterator<DFEInteraction> iterIt = dfe.getInteractions()
								.iterator();
						sourceNode = false;
						while (iterIt.hasNext() && !sourceNode) {
							sourceNode = iterIt.next().getDisplay()
									.equals(DisplayType.browser);
						}

						if (sourceNode) {
							outputTab.setShowOutputForm("N");
						}
						outputTab.mountOutputForm(!sourceNode);


						listPageSize = getPageList().size();

						// initialise the position of list
						setListPosition(0);

						// retrieves the correct page
						setCanvasTitle(WordUtils.capitalizeFully(dfe.getName().replace("_", " ")));

						mountInteractionForm();

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
			}
		}
	}

	/**
	 * When Closing Canvas Modal need to update Output element
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	protected String updateOutputElement() throws RemoteException {
		String error = null;
		try {
			logger.info("update Output Element ");
			if (elementToUpdate) {
				logger.info("Remove element data");
				dfe.cleanThisAndAllElementAfter();
			}
			error = dfe.updateOut();
			if (error != null) {
				MessageUseful.addErrorMessage(error);
			}
		} catch (Exception e) {
		}
		return error;
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

				logger.info(getPageList().size());

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

				mountInteractionForm();
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

		mountInteractionForm();

	}

	/**
	 * mountInteractionForm
	 * 
	 * Method to mount the page of Interaction
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	protected void mountInteractionForm() throws RemoteException {
		logger.info("mountInteractionForm ");
		try {

			int page = getListPosition();
			dfe.update(page);

			setPageTitle(getPage().getTitle());
			setPageLegend(getPage().getLegend());

			inters = new LinkedList<CanvasModalInteraction>();
			for (DFEInteraction dfeInteraction : getPage().getInteractions()) {
				CanvasModalInteraction cmi = CanvasModalInteraction.getNew(dfeInteraction,dfe,outputTab); 
				inters.add(cmi);
				if(cmi.getTable() != null){
					tablesColumnTitle = cmi.getTable().getTableGridColumns();
				}
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
	protected String checkNextPage() throws RemoteException {

		StringBuffer error = new StringBuffer();
		for (int i = 0; i < inters.size(); i++) {

			CanvasModalInteraction cmInter = inters.get(i);

			boolean interactionChanged = !cmInter.isUnchanged();
			logger.info(cmInter.getId()+": "+interactionChanged);
			if (interactionChanged) {
				cmInter.writeInteraction();
			}

			String e = cmInter.getInter().check();
			logger.info("error interaction -> " + e);
			if (e != null) {
				error.append(e);
				error.append(System.getProperty("line.separator"));
			}
		}

		String e = getPageList().get(getListPosition()).checkPage();
		if(e == null && error.length() == 0 && sourceNode){
			for (int i = 0; i < inters.size(); i++) {
				CanvasModalInteraction cmInter = inters.get(i);

				logger.info("error interaction -> " + e);
				if(!cmInter.isUnchanged() && 
						cmInter.getDisplayType().toString().equals(DisplayType.browser.toString())){
					logger.info("read back browser...");
					cmInter.readInteraction();
				}
			}
		}

		logger.info("error page -> " + e);
		if (e != null) {
			error.append(e);
			error.append(System.getProperty("line.separator"));
		} else {
			// Update output only if it is the last page
			// or an output already exist
			if (getListPageSize() - 1 == getListPosition()) {
				e = updateOutputElement();
				if (e != null) {
					error.append(e);
					error.append(System.getProperty("line.separator"));
				}
				outputTab.mountOutputForm(!sourceNode);

			}
		}
		return error.toString();
	}

	/**
	 * Open a Text Editor Modal
	 * 
	 * @throws RemoteException
	 */
	public void openHelpTextEditorModal() throws RemoteException {

		String idInteraction = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap()
				.get("idInteraction");

		logger.info("interaction: " + idInteraction);
		int indexOf = getPage().getInteractions().indexOf(
				getPage().getInteraction(idInteraction));

		CanvasModalInteraction cmInt = inters.get(indexOf);
		if (cmInt instanceof TableInteraction) {
			logger.info("Table interaction");
			Integer rowKey = Integer.valueOf(FacesContext.getCurrentInstance()
					.getExternalContext().getRequestParameterMap()
					.get("rowKey"));
			String column = FacesContext.getCurrentInstance()
					.getExternalContext().getRequestParameterMap()
					.get("column");

			logger.info("row: " + rowKey);
			logger.info("column: " + column);
			selEditor = new SelectedEditor((TableInteraction) cmInt, column,
					rowKey);
		} else if (cmInt instanceof EditorInteraction) {
			logger.info("Editor interaction");
			selEditor = new SelectedEditor((EditorInteraction) cmInt);
		} else {
			MessageUseful
			.addErrorMessage("Object should be a Table or an Editor");
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

	}

	protected DFEPage getPage() throws RemoteException {
		return dfe != null ? dfe.getPageList().get(listPosition) : null;
	}

	/**
	 * @return
	 * @throws RemoteException
	 * @see idiro.workflow.server.interfaces.DataFlowElement#getPageList()
	 */
	protected List<DFEPage> getPageList() throws RemoteException {
		return dfe != null ? dfe.getPageList() : null;
	}

	/**
	 * @return the listPosition
	 */
	public final int getListPosition() {
		return listPosition;
	}

	/**
	 * @param listPosition
	 *            the listPosition to set
	 */
	public final void setListPosition(int listPosition) {
		this.listPosition = listPosition;
	}

	/**
	 * @return the pageTitle
	 */
	public final String getCanvasTitle() {
		return canvasTitle;
	}

	/**
	 * @param pageTitle
	 *            the pageTitle to set
	 */
	public final void setCanvasTitle(String pageTitle) {
		this.canvasTitle = pageTitle;
	}

	/**
	 * @return the pageLegend
	 */
	public final String getPageLegend() {
		return pageLegend;
	}

	/**
	 * @param pageLegend
	 *            the pageLegend to set
	 */
	public final void setPageLegend(String pageLegend) {
		this.pageLegend = pageLegend;
	}

	/**
	 * @return the lastPage
	 */
	public final String getLastPage() {
		return lastPage;
	}

	/**
	 * @param lastPage
	 *            the lastPage to set
	 */
	public final void setLastPage(String lastPage) {
		this.lastPage = lastPage;
	}

	/**
	 * @return the firstPage
	 */
	public final String getFirstPage() {
		return firstPage;
	}

	/**
	 * @param firstPage
	 *            the firstPage to set
	 */
	public final void setFirstPage(String firstPage) {
		this.firstPage = firstPage;
	}

	/**
	 * @return the listPageSize
	 */
	public final int getListPageSize() {
		return listPageSize;
	}

	/**
	 * @return the errorMsg
	 */
	public final String getErrorMsg() {
		return errorMsg;
	}

	/**
	 * @param errorMsg
	 *            the errorMsg to set
	 */
	public final void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	/**
	 * @return the inters
	 */
	public final List<CanvasModalInteraction> getInters() {
		return inters;
	}

	/**
	 * @return the selEditor
	 */
	public SelectedEditor getSelEditor() {
		return selEditor;
	}

	/**
	 * @param selEditor
	 *            the selEditor to set
	 */
	public void setSelEditor(SelectedEditor selEditor) {
		this.selEditor = selEditor;
	}

	/**
	 * @return the selectedTab
	 */
	public String getSelectedTab() {
		return selectedTab;
	}

	/**
	 * @param selectedTab
	 *            the selectedTab to set
	 */
	public void setSelectedTab(String selectedTab) {
		this.selectedTab = selectedTab;
	}

	/**
	 * @return the pageTitle
	 */
	public String getPageTitle() {
		return pageTitle;
	}

	/**
	 * @param pageTitle
	 *            the pageTitle to set
	 */
	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	/**
	 * @return the imagePath
	 */
	public String getPathImage() {
		return pathImage;
	}

	/**
	 * @param pathImage
	 *            the imagePath to set
	 */
	public void setPathImage(String pathImage) {
		this.pathImage = pathImage;
	}

	/**
	 * @return the loadMainWindow
	 */
	public final boolean isLoadMainWindow() {
		return loadMainWindow;
	}

	/**
	 * @param loadMainWindow
	 *            the loadMainWindow to set
	 */
	public final void setLoadMainWindow(boolean loadMainWindow) {
		this.loadMainWindow = loadMainWindow;
	}

	/**
	 * @return the idGroup
	 */
	public final String getIdGroup() {
		return idGroup;
	}

	public void setIdGroup(String idGroup) {
		this.idGroup = idGroup;
	}

	/**
	 * @return the outputTab
	 */
	public final CanvasModalOutputTab getOutputTab() {
		return outputTab;
	}

	/**
	 * @return the isSourceNode
	 */
	public final boolean isSourceNode() {
		return sourceNode;
	}

	/**
	 * @return
	 * @throws RemoteException
	 * @see idiro.workflow.server.interfaces.DataFlowElement#getComponentId()
	 */
	public String getComponentId() throws RemoteException {
		return dfe != null ? dfe.getComponentId() : null;
	}

	/**
	 * @return the elementId
	 */
	public String getElementId() {
		return elementId;
	}

	/**
	 * @param elementId
	 *            the elementId to set
	 */
	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	/**
	 * @return the tablesColumnTitle
	 */
	public final List<String> getTablesColumnTitle() {
		return tablesColumnTitle;
	}

}
