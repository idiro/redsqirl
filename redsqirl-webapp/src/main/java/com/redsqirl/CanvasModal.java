/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl;


import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.ajax4jsf.model.KeepAlive;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

import com.redsqirl.interaction.BrowserInteraction;
import com.redsqirl.interaction.CanvasModalInteraction;
import com.redsqirl.interaction.EditorInteraction;
import com.redsqirl.interaction.SelectedEditor;
import com.redsqirl.interaction.TableInteraction;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.workflow.server.connect.interfaces.DataFlowInterface;
import com.redsqirl.workflow.server.enumeration.DisplayType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFEPage;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;

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
	
	private String pageToGoTo;

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
	 * The text tip associated with the current wizard page
	 */
	private String pageTextTip;

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
	private List<CanvasModalInteraction> inters = new LinkedList<CanvasModalInteraction>();
	private CanvasModalInteraction canvasModalInteractionTableInteractionPanel;
	private CanvasModalInteraction canvasModalInteractionHeaderEditor;

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
	private Map<String,FileSystemBean> datastores;

	/**
	 * Set the comment of an element in change id
	 */
	private String elementComment;
	
	private List<SelectItem> reachablePages = null;

	public CanvasModal() throws RemoteException{
		
		logger.info("CanvasModal");
		
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
	public String[] getOpenCanvasModal() throws RemoteException {
		logger.info("openCanvasModal");
		
		String error = null;
		
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
		//logger.info("selected tab: " + selTab);
		setSelectedTab(selTab);

		// Don't update the element if you it close immediately
		elementToUpdate = false;

		// Get the image
		pathImage = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("paramPathImage");

		// Get the Element
		idGroup = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("paramGroupId");

		Integer pageNb = 0;
		String pageNbParam = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("paramPageNb");
		if(pageNbParam != null && !pageNbParam.isEmpty() && !pageNbParam.equalsIgnoreCase("undefined")){
			try {
				pageNb = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext()
						.getRequestParameterMap().get("paramPageNb"));
			} catch (NumberFormatException e) {
				pageNb = 0;
				logger.warn("Page nb issue: "+e.getMessage(),e);
			}
		}

		try {
			dfe = canvasBean.getDf().getElement(canvasBean.getIdElement(idGroup));
			//logger.info("Get element dfe");
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		}

		String paramLMW = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("paramLoadMainWindow");
		if(paramLMW != null){
			loadMainWindow = !paramLMW.equalsIgnoreCase("false");
		}else{
			loadMainWindow = true;
		}

		if (dfe == null) {
			logger.info("The element is null!");
			error = getMessageResources("msg_error_oops");
		}else{
			elementId = getComponentId();
			//logger.info("Element id: "+elementId);
			elementComment = dfe.getComment();
			if (loadMainWindow) {
				//logger.info("load Main window");
				try {

					// validate if you can open or not the dynamic form of the object
					error = dfe.checkIn();

					//logger.info("error " + error);

					if (error == null) {

						// mount output tab
						outputTab = new CanvasModalOutputTab(datastores,dfe);
						Iterator<DFEInteraction> iterIt = dfe.getInteractions()
								.iterator();
						sourceNode = false;
						while (iterIt.hasNext() && !sourceNode) {
							sourceNode = iterIt.next().getDisplay()
									.equals(DisplayType.browser);
						}

						if (sourceNode && dfe.getDFEOutput().size() <= 1) {
							outputTab.setShowOutputForm("N");
						}

						listPageSize = getPageList().size();

						//initialise the position of list and check all pages before
						setListPosition(pageNb);
						String e = null;
						if(pageNb > 0){
							int i = -1;
							while(e == null && i < Math.min(pageNb ,listPageSize)){
								++i;
								dfe.update(i);
								e = getPageList().get(i).checkPage();
							}
							setListPosition(i);
						}
						//specific error message to validation object
						if (e != null) {
							logger.info("openCanvasModal checkpages before " + e);
							MessageUseful.addErrorMessage(e);
							request.setAttribute("msnErrorPage", "msnErrorPage");
							usageRecordLog().addError("ERROR OPENCANVASMODAL", e);
						}

						// retrieves the correct page
						setCanvasTitle(WordUtils.capitalizeFully(dfe.getName().replace("_", " "))+": "+elementId);

						if(listPageSize > 0){
							mountInteractionForm();
						}else{
							updateOutputElement();
						}

						outputTab.mountOutputForm(!sourceNode || dfe.getDFEOutput().size() > 1);

						checkFirstPage();

						//logger.info("List size " + getListPageSize());

						checkLastPage();

					}


				} catch (RemoteException e) {
					logger.error(e,e);
				} catch (Exception e) {
					logger.error(e,e);
				}
			}
			
		}
		
		//logger.info("listPage:"+ Integer.toString(getListPageSize()) + " getIdGroup:" + getIdGroup()+ " getCurElId:"+getCurElId()+ " getCurElComment:"+getCurElComment());
		boolean loadOutputTab = false;
		try{
			loadOutputTab = loadMainWindow && ((canvasBean.getWorkflowType().equals("W") && (getOutputTab().getShowOutputForm() != null && getOutputTab().getShowOutputForm().equals("Y")) || getListPageSize() > 0));
		}catch(Exception e){		
		}
		
		displayErrorMessage(error, "OPENCANVASMODAL");
		
		String[] ans = new String[]{Boolean.toString(loadOutputTab),
				Integer.toString(pageNb),
				getIdGroup(),
				getCurElId(),
				getCurElComment(),
				Boolean.toString(loadMainWindow)
				};
		
		logger.info(ans[0]+", "+ans[1]+", "+ans[2]+", "+ans[3]+", "+ans[4]+", "+ans[5]);
		
		return ans;
	}
	
	public void closeCanvasModal() throws RemoteException {
		logger.info("closeCanvasModal");
		loadMainWindow = false;
		outputTab = new CanvasModalOutputTab(datastores);
	}

	public void changeTitle(){
		
		logger.info("changeTitle");
		String error = null;
		try {
			DataFlowElement dfe = canvasBean.getDf().getElement(
							canvasBean.getIdElement(idGroup));
			if(dfe != null){
				setCanvasTitle(WordUtils.capitalizeFully(dfe.getName().replace("_", " "))+": "+dfe.getComponentId());
			}else{
				logger.error("Element is null!");
				error = getMessageResources("msg_error_oops");
			}
		} catch (Exception e) {
			error = e.getMessage();
			logger.error(e,e);
		}
		
		displayErrorMessage(error, "CHANGETITLE");
	}

	/**
	 * When Closing Canvas Modal need to update Output element
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	protected String updateOutputElement() throws RemoteException {
		
		logger.info("update Output Element ");
		
		String error = null;
		try {
			
			if (elementToUpdate) {
				logger.info("Remove element data");
				dfe.cleanThisAndAllElementAfter();
			}
			try{
				error = dfe.updateOut();
			}catch(Exception e){
				logger.error(e,e);
				error = "Unexpected program error while checking this action.";
			}
		} catch (Exception e) {
		}
		
		return error;
	}
	
	protected void updateReachablePage(){
		
		logger.info("updateReachablePage");
		
		reachablePages = new LinkedList<SelectItem>();
		try{
			for(int i = 0; i <= getListPosition();++i){
				reachablePages.add(new SelectItem(Integer.valueOf(i),
						Integer.valueOf(i+1)+": "+getPageList().get(i).getTitle(),"",false));
			}
			int i = getListPosition()+1;
			String error = null;
			while( i < getListPageSize() && error == null){
				if( (error = getPageList().get(i).checkPage()) == null){
					reachablePages.add(new SelectItem(Integer.valueOf(i),
							Integer.valueOf(i+1)+": "+getPageList().get(i).getTitle(),"",false));
					++i;
				}
			}
			while( i < getListPageSize()){
				reachablePages.add(new SelectItem(Integer.valueOf(i),
						Integer.valueOf(i+1)+": "+getPageList().get(i).getTitle(),"",true));
				++i;
			}
		}catch(Exception e){
			logger.error(e,e);
		}
		pageToGoTo = String.valueOf(getListPosition());
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

		logger.info("applyPage");

		String error = checkNextPage();
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		if (error != null && error.length() > 1) {
			setErrorMsg(error);
		}else{
			//Done in checkNextPage if it is the last
			if (getListPageSize() - 1 != getListPosition()) {
				updateOutputElement();
				outputTab.mountOutputForm(!sourceNode || dfe.getDFEOutput().size() > 1);
			}
			MessageUseful.addInfoMessage(getMessageResources("success_message"));
			request.setAttribute("msnSuccess", "msnSuccess");
			setErrorMsg("");
		}
		displayErrorMessage(error, "APPLYPAGE");
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
		String error = null;
		try {

			error = checkNextPage();
			if (error == null || error.length() <= 1) {
				error = null;
				logger.info("check nextPage Ok ");

				setListPosition(getListPosition() + 1);

				logger.info("getListPosition()+1 " + getListPosition());

				logger.info(getPageList().size());

				checkFirstPage();

				checkLastPage();

				mountInteractionForm();
			}

		} catch (Exception e) {
			error = e.getMessage();
			logger.error(error,e);
		}

		displayErrorMessage(error, "NEXTPAGE");
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

		setListPosition(getListPosition() - 1);

		checkFirstPage();

		checkLastPage();

		mountInteractionForm();

		usageRecordLog().addSuccess("PREVIOUSPAGE");
	}
	

	public void goToPage(){
		
		logger.info("goToPage");
		String error = null;
		try {
			error = checkNextPage();
			int pageNb = Integer.valueOf(pageToGoTo);
			if( pageNb != getListPosition() && (error == null || error.isEmpty())){
				//logger.info("Go to page: "+pageNb);
				error = null;
				if(pageNb > getListPosition()){
					int i = getListPosition();
					while(error == null && i < Math.min(pageNb ,listPageSize)){
						++i;
						dfe.update(i);
						error = getPageList().get(i).checkPage();
					}
					setListPosition(i);
				}else{
					setListPosition(pageNb);
				}
			}
			
			if (error != null) {
				pageToGoTo = String.valueOf(getListPosition());
			} else {

				logger.info("check nextPage Ok ");

				checkFirstPage();

				checkLastPage();

				mountInteractionForm();
			}

		} catch (Exception e) {
			error = getMessageResources("msg_error_oops");
			logger.error(error,e);
		}
		
		displayErrorMessage(error, "GOTOPAGE");
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
		String error = null;
		try {

			int page = getListPosition();
			dfe.update(page);

			setPageTitle(getPage().getTitle());
			setPageLegend(getPage().getLegend());
			setPageTextTip(getPage().getTextTip());

			inters = new LinkedList<CanvasModalInteraction>();
			for (DFEInteraction dfeInteraction : getPage().getInteractions()) {
				CanvasModalInteraction cmi = CanvasModalInteraction.getNew(dfeInteraction,dfe,outputTab); 
				inters.add(cmi);
				if(cmi.getTable() != null){
					tablesColumnTitle = cmi.getTable().getTableGridColumns();
				}
			}

			updateReachablePage();
		} catch (Exception e) {
			error = e.getMessage();
			logger.error(e.getMessage(),e);
		}
		
		displayErrorMessage(error, "MOUNTINTERACTIONFORM");

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
		String error = null;
		try{
			logger.info("Check page : "+getListPosition());
			for (int i = 0; i < inters.size(); i++) {
				CanvasModalInteraction cmInter = inters.get(i);
				cmInter.setUnchanged();
				boolean interactionChanged = !cmInter.isUnchanged();
				logger.info(cmInter.getId()+": "+interactionChanged);
				if (interactionChanged) {
					elementToUpdate = true;
					logger.info("write interaction in "+cmInter.getId());
					cmInter.writeInteraction();
				}
			}
			logger.info("check page...");
			error = getPageList().get(getListPosition()).checkPage();
			//if(e == null && sourceNode){
			if(sourceNode){
				for (int i = 0; i < inters.size(); i++) {
					CanvasModalInteraction cmInter = inters.get(i);
					if(cmInter.getDisplayType().toString().equals(DisplayType.browser.toString()) && !cmInter.isUnchanged()){
						logger.info("read back browser...");
						cmInter.readInteraction();
						//logger.info("read back browser: "+cmInter.printTree(cmInter.getTree()));
					}
				}
			}
			if (error == null) {
				// Update output only if it is the last page
				// or an output already exist
				if (getListPageSize() - 1 == getListPosition()) {
					logger.info("updateOutputElement...");
					error = updateOutputElement();
					outputTab.mountOutputForm(!sourceNode || dfe.getDFEOutput().size() > 1);
				}
			}
			logger.info("error page -> " + error);
		}catch(Exception e){
			logger.error(e,e);
			error = getMessageResources("msg_error_oops");
		}
		return error;
	}

	/**
	 * Open a Text Editor Modal
	 * 
	 * @throws RemoteException
	 */
	public void openHelpTextEditorModal() throws RemoteException {
		logger.info("openHelpTextEditorModal");
		String error = null;
		String idInteraction = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("idInteraction");
		if(idInteraction != null){
			logger.info("interaction: " + idInteraction);
			int indexOf = getPage().getInteractions().indexOf(getPage().getInteraction(idInteraction));
			CanvasModalInteraction cmInt = inters.get(indexOf);
			if (cmInt instanceof TableInteraction) {
				logger.info("Table interaction");
				Integer rowKey = Integer.valueOf(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rowKey"));
				String column = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("column");
				logger.info("row: " + rowKey);
				logger.info("column: " + column);
				selEditor = new SelectedEditor((TableInteraction) cmInt, column, rowKey);
			} else if (cmInt instanceof EditorInteraction) {
				logger.info("Editor interaction");
				selEditor = new SelectedEditor((EditorInteraction) cmInt);
			} else {
				logger.info("openHelpTextEditorModal error ");
				error = getMessageResources("msg_error_selEditor");
			}
		}else{
			error = getMessageResources("msg_error_oops");
			logger.info("openHelpTextEditorModal error idInteraction = NULL ");
		}
		
		displayErrorMessage(error, "TEXTEDITOR");
	}

	public void openTableInteractionPanel() throws RemoteException{
		logger.info("openTableInteractionPanel");
		String error = null;
		String idInteraction = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("idInteraction");
		if(idInteraction != null){
			logger.info("interaction: " + idInteraction);
			int indexOf = getPage().getInteractions().indexOf(getPage().getInteraction(idInteraction));
			CanvasModalInteraction cmInt = inters.get(indexOf);
			if (cmInt instanceof TableInteraction) {
				logger.info("Table interaction");
				((TableInteraction) cmInt).mountTableInteractionPanel();
				setCanvasModalInteractionTableInteractionPanel(cmInt);
			}
		}else{
			error = getMessageResources("msg_error_oops");
			logger.info("openTableInteractionPanel error idInteraction = NULL ");
		}
		
		displayErrorMessage(error, "TABLEINTERACTION");
	}
	
	public void openHeaderEditorPanel() throws RemoteException{
		logger.info("openHeaderEditorPanel");
		String error = null;
		String idInteraction = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("idInteraction");
		if(idInteraction != null){
			logger.info("interaction: " + idInteraction);
			int indexOf = getPage().getInteractions().indexOf(getPage().getInteraction(idInteraction));
			CanvasModalInteraction cmInt = inters.get(indexOf);
			if (cmInt instanceof BrowserInteraction) {
				logger.info("Header Editor");
				((BrowserInteraction) cmInt).openHeaderEditor();
				setCanvasModalInteractionHeaderEditor(cmInt);
			}
		}else{
			logger.info("openHeaderEditorPanel error idInteraction = NULL ");
			error = getMessageResources("msg_error_oops");
		}
		
		displayErrorMessage(error, "HEADEREDITOR");
	}

	public void checkFirstPage() {
		if (getListPosition() == 0) {
			setFirstPage("Y");
		} else {
			setFirstPage("N");
		}
	}

	public void checkLastPage() {
		if (getListPageSize() - 1 > getListPosition()) {
			setLastPage("N");
		} else {
			setLastPage("Y");
		}
	}

	protected DFEPage getPage() throws RemoteException {
		return dfe != null ? dfe.getPageList().get(listPosition) : null;
	}

	/**
	 * @return
	 * @throws RemoteException
	 * @see com.redsqirl.workflow.server.interfaces.DataFlowElement#getPageList()
	 */
	protected List<DFEPage> getPageList() throws RemoteException {
		return dfe != null ? dfe.getPageList() : null;
	}
	
	public final String getCurElId() throws RemoteException{
		return dfe!= null ? dfe.getComponentId():null;
	}
	
	public final String getCurElComment() throws RemoteException{
		return dfe!= null ? dfe.getComment() : null;
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
	 * @see com.redsqirl.workflow.server.interfaces.DataFlowElement#getComponentId()
	 */
	public String getComponentId() throws RemoteException {
		return dfe != null ? dfe.getComponentId() : null;
	}

	/**
	 * @return the tablesColumnTitle
	 */
	public final List<String> getTablesColumnTitle() {
		return tablesColumnTitle;
	}

	public String getPageTextTip() {
		return pageTextTip;
	}

	public void setPageTextTip(String pageTextTip) {
		this.pageTextTip = pageTextTip;
	}

	/**
	 * @return the elementComment
	 */
	public String getElementComment() {
		return elementComment;
	}

	/**
	 * @param elementComment the elementComment to set
	 */
	public void setElementComment(String elementComment) {
		this.elementComment = elementComment;
	}

	public CanvasModalInteraction getCanvasModalInteractionTableInteractionPanel() {
		return canvasModalInteractionTableInteractionPanel;
	}

	public void setCanvasModalInteractionTableInteractionPanel(
			CanvasModalInteraction canvasModalInteractionTableInteractionPanel) {
		this.canvasModalInteractionTableInteractionPanel = canvasModalInteractionTableInteractionPanel;
	}

	/**
	 * @return the reachablePage
	 */
	public final List<SelectItem> getReachablePages() {
		return reachablePages;
	}

	/**
	 * @return the pageToGoTo
	 */
	public String getPageToGoTo() {
		return pageToGoTo;
	}

	/**
	 * @param pageToGoTo the pageToGoTo to set
	 */
	public void setPageToGoTo(String pageToGoTo) {
		this.pageToGoTo = pageToGoTo;
	}
	
	public final int getReachablePagesSize(){
		return reachablePages == null ? 0 : reachablePages.size();
	}

	/**
	 * @return the elementToUpdate
	 */
	public final boolean isElementToUpdate() {
		return elementToUpdate;
	}

	public CanvasModalInteraction getCanvasModalInteractionHeaderEditor() {
		return canvasModalInteractionHeaderEditor;
	}

	public void setCanvasModalInteractionHeaderEditor(
			CanvasModalInteraction canvasModalInteractionHeaderEditor) {
		this.canvasModalInteractionHeaderEditor = canvasModalInteractionHeaderEditor;
	}

}