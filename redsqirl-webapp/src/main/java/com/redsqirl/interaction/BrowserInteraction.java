package com.redsqirl.interaction;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import com.redsqirl.CanvasModalOutputTab;
import com.redsqirl.FileSystemBean;
import com.redsqirl.dynamictable.SelectHeaderType;
import com.redsqirl.useful.MessageUseful;
import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.enumeration.FieldType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;

public class BrowserInteraction extends CanvasModalInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3020683683280306022L;

	/**
	 * List of the field name
	 */
	private List<String> listFields;
	private List<SelectHeaderType> listFieldsType;
	private List<SelectItem> fieldTypes;
	private String headerFieldsType;
	private String selectHeaderEditor;

	/**
	 * List of the properties
	 */
	private List<SelectItem> listProperties;

	private String updatableHeader;

	private DataFlowElement dfe;
	/**
	 * Object that will manage the output and display it. 
	 * It is supposed that a browser element has only one output.
	 */
	private CanvasModalOutputTab modalOutput;

	public BrowserInteraction(DFEInteraction dfeInter,DataFlowElement dfe,CanvasModalOutputTab outputTab) throws RemoteException {
		super(dfeInter);
		this.modalOutput = outputTab;
		this.dfe = dfe;
		//outputTab.resetNameOutput();
		//outputTab.updateDFEOutputTable();
	}

	@Override
	public void readInteraction() throws RemoteException {
		setTree();
		// clean the map
		listFields = new LinkedList<String>();
		listProperties = new LinkedList<SelectItem>();
		updatableHeader = "false";
		//logger.info(printTree(tree));
		try{
			if (tree.getFirstChild("browse")
					.getFirstChild("output").getFirstChild("path") != null) {
				setPath(tree
						.getFirstChild("browse")
						.getFirstChild("output").getFirstChild("path")
						.getFirstChild().getHead());
				logger.info("path mount " + getPath());
				if (!getPath().startsWith("/")) {
					setPath("/" + getPath());
				}
			}
		}catch(Exception e){
			logger.info("Exception: "+e.getMessage());
			setPath(null);
		}

		//set properties
		try{
			if (tree.getFirstChild("browse")
					.getFirstChild("output").getChildren("property") != null) {
				List<Tree<String>> props = tree.getFirstChild("browse")
						.getFirstChild("output").getFirstChild("property").getSubTreeList();
				if (props != null) {
					logger.info("properties not null: " + props.size());
					for (Tree<String> tree : props) {
						listProperties.add(new SelectItem(tree
								.getFirstChild().getHead(),tree.getHead()));
					}
				}
			}
			
		}catch(Exception e){
			logger.info("Exception: "+e.getMessage());
		}

		//set fields
		try{
			List<Tree<String>> field = tree.getFirstChild("browse")
					.getFirstChild("output").getChildren("field");
			if (field != null && !field.isEmpty()) {
				logger.info("fields not null: " + field.size());
				for (Tree<String> tree : field) {
					String name = tree.getFirstChild("name").getFirstChild().getHead();
					String type = tree.getFirstChild("type").getFirstChild().getHead();
					listFields.add(name+" "+type);
				}
			}
		}catch(Exception e){
			logger.info("Exception: "+e.getMessage());
		}

		if(modalOutput != null){
			modalOutput.resetNameOutput();
			modalOutput.updateDFEOutputTable();
			try{
				updatableHeader = Boolean.toString(dfe.getDFEOutput().get(modalOutput.getNameOutput()).getHeaderEditorOnBrowser());
			}catch(Exception e){
				logger.error("Unexpected error: "+e,e);
			}
		}
	}

	@Override
	public void writeInteraction() throws RemoteException {
		inter.getTree().getFirstChild("browse").getFirstChild("output").removeAllChildren();
		inter.getTree().getFirstChild("browse").getFirstChild("output").add("path").add(getPath());
		inter.getTree().getFirstChild("browse").getFirstChild("output").add("name").add("");
		
		inter.getTree().getFirstChild("browse").getFirstChild("output").remove("property");
		inter.getTree().getFirstChild("browse").getFirstChild("output").remove("field");
		Tree<String> myProperty = inter.getTree().getFirstChild("browse").getFirstChild("output").add("property");
		
		for (SelectItem item : listProperties) {
			logger.info("Add property: " + item.getLabel() + ": " + item.getValue());
			myProperty.add(item.getLabel()).add(item.getValue().toString());
		}

		for (String nameValue : listFields) {
			Tree<String> myField = inter.getTree().getFirstChild("browse").getFirstChild("output").add("field");
			String value[] = nameValue.trim().split("\\s+");
			myField.add("name").add(value[0]);
			myField.add("type").add(value[1]);
		}
	}

	@Override
	public void setUnchanged() {
		try {
			// Check path
			String oldPath = tree.getFirstChild("browse")
					.getFirstChild("output").getFirstChild("path")
					.getFirstChild().getHead();
			logger.debug("Comparaison path: " + oldPath + " , "
					+ getPath());
			unchanged = getPath().equals(
					oldPath);

			// Check properties
			if (unchanged) {
				for (SelectItem itemList : listProperties) {
					String key = itemList.getLabel();
					logger.trace("Comparaison property "
							+ key
							+ ": "
							+ itemList.getValue()
							+ " , "
							+ tree
							.getFirstChild("browse")
							.getFirstChild("output")
							.getFirstChild("property")
							.getFirstChild(key).getFirstChild()
							.getHead());
					unchanged &= tree
							.getFirstChild("browse")
							.getFirstChild("output")
							.getFirstChild("property")
							.getFirstChild(key).getFirstChild()
							.getHead().equals(itemList.getValue());
				}
			}

			// Check fields
			if (unchanged) {
				List<Tree<String>> oldFieldsList = tree
						.getFirstChild("browse")
						.getFirstChild("output").getChildren("field");
				logger.debug("comparaison fields: "
						+ oldFieldsList.size() + " , "
						+ listFields.size());
				if (unchanged &= oldFieldsList.size() == listFields.size()) {
					Iterator<Tree<String>> oldFieldIt = oldFieldsList
							.iterator();
					for (String nameValue : listFields) {
						Tree<String> field = oldFieldIt.next();
						String value[] = nameValue.split(" ");
						logger.trace("Comparaison field: "
								+ field.getFirstChild("name")
								.getFirstChild().getHead()
								+ " , "
								+ value[0]
										+ " | type "
										+ field.getFirstChild("type")
										.getFirstChild().getHead()
										+ " , " + value[1]);

						if (field.getFirstChild("name")
								.getFirstChild().getHead()
								.equals(value[0])) {
							unchanged &= field
									.getFirstChild("type")
									.getFirstChild().getHead()
									.equals(value[1]);
						} else {
							unchanged = false;
						}
					}

				}
			}
		} catch (Exception e) {
			unchanged = false;
		}
	}

	public void openHeaderEditor(){

		logger.info("openHeaderEditor");

		fieldTypes = new ArrayList<SelectItem>();
		for (FieldType type : FieldType.values()) {
			fieldTypes.add(new SelectItem(type.toString(), type.toString().toUpperCase() ));
		}
		
		setSelectHeaderEditor("D");

		if(getGridTitles() != null){

			listFieldsType = new ArrayList<SelectHeaderType>();
			StringBuffer header = new StringBuffer();
			for (String line : getGridColumnIds()) {
				logger.info(line);
				SelectHeaderType selectHeaderType = new SelectHeaderType();
				String[] values = line.trim().split("\\s+");
				selectHeaderType.setName(values[0].trim());
				selectHeaderType.setType(values[1].trim().toUpperCase());
				listFieldsType.add(selectHeaderType);
				header.append(","+values[0] + " " + values[1].toUpperCase());
			}

			setHeaderFieldsType(header.substring(1));

		}

	}

	public void headerEditor(){

		logger.info("headerEditor");

		String error = null;
		
		if(getSelectHeaderEditor() != null){
			listFields = new ArrayList<String>();
			if(getSelectHeaderEditor().equalsIgnoreCase("U")){

				String[] values = getHeaderFieldsType().split(",");
				if(values.length == getGridTitles().size()){
					for (int i = 0; i < values.length; i++) {
						
						if(error == null){
							String[] nameType = values[i].trim().split("\\s+");
							if(nameType.length == 1){
								listFields.add(nameType[0].trim() + " " + getListFieldsType().get(i).getType());
							}else if(nameType.length == 2){
								listFields.add(values[i].trim());
							}else if(nameType.length > 2){
								error = getMessageResources("msg_error_size_header");
							}
							
						}
						
					}
				}else{
					error = getMessageResources("msg_error_size_header");
				}

			}else{

				for (SelectHeaderType selectHeaderType : listFieldsType) {
					StringBuffer result = new StringBuffer();
					result.append(selectHeaderType.getName());
					result.append(" ");
					result.append(selectHeaderType.getType());
					logger.info(result);
					listFields.add(result.toString());
				}

			}
		}else{
			error = getMessageResources("msg_error_selected_header");
		}

		if(error != null){
			logger.info(error);
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

	}

	public void mountHeaderEditor(){

		logger.info("mountHeaderEditor");

		if(getSelectHeaderEditor() != null && getSelectHeaderEditor().equalsIgnoreCase("U")){

			StringBuffer header = new StringBuffer();
			for (SelectHeaderType selectHeaderType : listFieldsType) {
				header.append(","+selectHeaderType.getName() + " " + selectHeaderType.getType().toUpperCase());
			}
			setHeaderFieldsType(header.substring(1));

			logger.info("U");

		}else{

			String[] lines = getHeaderFieldsType().trim().split(",");
			if(lines.length == getGridTitles().size()){
				listFieldsType = new ArrayList<SelectHeaderType>();
				for (String line : lines) {
					SelectHeaderType selectHeaderType = new SelectHeaderType();
					String[] values = line.split("\\s+");
					selectHeaderType.setName(values[0].trim());
					if(values.length == 2){
						selectHeaderType.setType(values[1].trim().toUpperCase());
					}else{
						selectHeaderType.setType("");
					}
					listFieldsType.add(selectHeaderType);
				}
			}
			
			logger.info("D");

		}

	}

	/**
	 * @return the listFields
	 */
	public final List<String> getListFields() {
		return listFields;
	}

	/**
	 * @param listFields the listFields to set
	 */
	public final void setListFields(List<String> listFields) {
		this.listFields = listFields;
	}

	/**
	 * @return the listProperties
	 */
	public final List<SelectItem> getListProperties() {
		return listProperties;
	}

	/**
	 * @param listProperties the listProperties to set
	 */
	public final void setListProperties(List<SelectItem> listProperties) {
		this.listProperties = listProperties;
	}

	/**
	 * @return
	 * @see com.redsqirl.CanvasModalOutputTab#getFileSystem()
	 */
	public final FileSystemBean getFileSystem() {
		return modalOutput.getFileSystem();
	}

	/**
	 * @return
	 * @see com.redsqirl.CanvasModalOutputTab#getPath()
	 */
	public String getPath() {
		return modalOutput.getPath();
	}

	/**
	 * @param path
	 * @see com.redsqirl.CanvasModalOutputTab#setPath(java.lang.String)
	 */
	public void setPath(String path) {
		modalOutput.setPath(path);
	}

	/**
	 * @return
	 * @see com.redsqirl.CanvasModalOutputTab#getTitles()
	 */
	public List<String> getGridTitles() {
		return modalOutput != null ? modalOutput.getTitles():null;
	}
	
	public List<String> getGridColumnIds(){
		return modalOutput != null ? modalOutput.getGrid().getColumnIds():null;
	}

	/**
	 * @return
	 * @see com.redsqirl.CanvasModalOutputTab#getRows()
	 */
	public List<String[]> getGridRows() {
		return modalOutput != null ? modalOutput.getRows():null;
	}

	public List<SelectItem> getFieldTypes() {
		return fieldTypes;
	}

	public void setFieldTypes(List<SelectItem> fieldTypes) {
		this.fieldTypes = fieldTypes;
	}

	public List<SelectHeaderType> getListFieldsType() {
		return listFieldsType;
	}

	public void setListFieldsType(List<SelectHeaderType> listFieldsType) {
		this.listFieldsType = listFieldsType;
	}

	public String getHeaderFieldsType() {
		return headerFieldsType;
	}

	public void setHeaderFieldsType(String headerFieldsType) {
		this.headerFieldsType = headerFieldsType;
	}

	public String getSelectHeaderEditor() {
		return selectHeaderEditor;
	}

	public void setSelectHeaderEditor(String selectHeaderEditor) {
		this.selectHeaderEditor = selectHeaderEditor;
	}

	public String getUpdatableHeader() {
		return updatableHeader;
	}

	public void setUpdatableHeader(String updatableHeader) {
		this.updatableHeader = updatableHeader;
	}

}