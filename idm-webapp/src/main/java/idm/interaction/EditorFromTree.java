package idm.interaction;

import idiro.utils.Tree;
import idm.dynamictable.UnselectableTable;
import idm.useful.ListStringArrayComparator;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Setup an editor from a Tree object.
 * 
 * This class is used for a text editor and for a table interaction.
 * 
 * @author etienne
 *
 */
public class EditorFromTree implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3420978034024666649L;

	static protected Logger logger = Logger.getLogger(EditorFromTree.class);
	
	private Tree<String> tree;
	
	/**
	 * What is inside the text editor
	 */
	private String textEditorValue;
	/**
	 * The fields available associated with their type.
	 */
	private UnselectableTable textEditorFields;
	
	/**
	 * The Function menu
	 */
	private List<String> textEditorFunctionMenu;
	
	
	/**
	 * The operation menu
	 */
	private List<String> textEditorOperationMenu;
	
	/**
	 * The functions available for each function category
	 */
	private Map<String, List<String[]>> textEditorFunctions;
	/**
	 * The operations available for each operation category
	 */
	private Map<String, List<String[]>> textEditorOperations;
	
	/**
	 * Flag "Y" if it is necessary to open a popup or "N".
	 */
	private String openPopup;


	public EditorFromTree(Tree<String> tree) {
		this.tree = tree;
	}

	public void readInteraction() throws RemoteException {
		if (tree.getFirstChild("editor").getFirstChild("output")
				.getFirstChild() != null) {
			textEditorValue =  "";
			try{
			textEditorValue = tree.getFirstChild("editor")
					.getFirstChild("output").getFirstChild().getHead();
			}catch(Exception e){}
		}
		List<Tree<String>> list = null;

		// Get the features
		LinkedList<String> header = new LinkedList<String>();
		header.add("Name");
		header.add("Info");
		textEditorFields = new UnselectableTable(header);
		try {
			list = tree.getFirstChild("editor")
					.getFirstChild("keywords").getSubTreeList();
		} catch (Exception e) {
			list = null;
		}
		if (list != null && !list.isEmpty()) {
			
			for (Tree<String> tree : list) {
				textEditorFields.add(new String[]{
						tree.getFirstChild("name").getFirstChild().getHead(),
						tree.getFirstChild("info").getFirstChild().getHead()});
			}
			logger.info("features: "+textEditorFields.toString());
		}else{
			logger.info("no features...");
		}

		// Get the functions and operations
		textEditorFunctionMenu = new LinkedList<String>();
		textEditorOperationMenu = new LinkedList<String>();
		textEditorFunctions = new HashMap<String, List<String[]>>();
		textEditorOperations = new HashMap<String, List<String[]>>();
		try {
			list = tree.getFirstChild("editor")
					.getFirstChild("help").getSubTreeList();
		} catch (Exception e) {
			list = null;
		}
		if (list != null && !list.isEmpty()) {
			openPopup = "Y";
			// logger.info("list not null: " + list.toString());
			for (Tree<String> tree : list) {
				logger.info("list value " + tree.getHead());
				Map<String, List<String[]>> cur = null;
				List<String> curL = null;
				String menuName = null;
				if (tree.getHead().startsWith("operation_")) {
					cur = textEditorOperations;
					curL = textEditorOperationMenu;
					menuName = tree.getHead().split("_")[1];
				} else {
					cur = textEditorFunctions;
					curL = textEditorFunctionMenu;
					menuName = tree.getHead();
				}
				if (!cur.containsKey(menuName)) {
					// Add one menu
					cur.put(menuName, new LinkedList<String[]>());
					curL.add(menuName);
				}

				// Add the content
				for (Tree<String> tree2 : tree.getSubTreeList()) {

					String nameFunction = tree2.getFirstChild("name")
							.getFirstChild() != null ? tree2
							.getFirstChild("name").getFirstChild().getHead()
							: "";
					String inputFunction = tree2.getFirstChild("input")
							.getFirstChild() != null ? tree2
							.getFirstChild("input").getFirstChild().getHead()
							: "";
					String returnFunction = tree2.getFirstChild("return")
							.getFirstChild() != null ? tree2
							.getFirstChild("return").getFirstChild().getHead()
							: "";
					String helpFunction = tree2.getFirstChild("help")
							.getFirstChild() != null ? tree2
							.getFirstChild("help").getFirstChild().getHead()
							: "";

					cur.get(menuName).add(
							new String[] { nameFunction, inputFunction,
									returnFunction, helpFunction });
				}
			}

		} else {
			openPopup = "N";
		}
		
		Collections.sort(textEditorFunctionMenu);
		Collections.sort(textEditorOperationMenu);
		for (String key : textEditorFunctions.keySet()) {
			List<String[]> lfunc = textEditorFunctions.get(key);
			Collections.sort(lfunc, new ListStringArrayComparator());
		}
		for (String key : textEditorOperations.keySet()) {
			List<String[]> lop = textEditorOperations.get(key);
			Collections.sort(lop, new ListStringArrayComparator());
		}

	}

	/**
	 * @return the textEditorValue
	 */
	public final String getTextEditorValue() {
		return textEditorValue;
	}

	/**
	 * @param textEditorValue the textEditorValue to set
	 */
	public final void setTextEditorValue(String textEditorValue) {
		this.textEditorValue = textEditorValue;
	}

	/**
	 * @return the textEditorFields
	 */
	public final UnselectableTable getTextEditorFields() {
		return textEditorFields;
	}

	/**
	 * @param textEditorFields the textEditorFields to set
	 */
	public final void setTextEditorFields(UnselectableTable textEditorFields) {
		this.textEditorFields = textEditorFields;
	}

	/**
	 * @return the textEditorFunctionMenu
	 */
	public final List<String> getTextEditorFunctionMenu() {
		return textEditorFunctionMenu;
	}

	/**
	 * @param textEditorFunctionMenu the textEditorFunctionMenu to set
	 */
	public final void setTextEditorFunctionMenu(
			List<String> textEditorFunctionMenu) {
		this.textEditorFunctionMenu = textEditorFunctionMenu;
	}

	/**
	 * @return the textEditorOperationMenu
	 */
	public final List<String> getTextEditorOperationMenu() {
		return textEditorOperationMenu;
	}

	/**
	 * @param textEditorOperationMenu the textEditorOperationMenu to set
	 */
	public final void setTextEditorOperationMenu(
			List<String> textEditorOperationMenu) {
		this.textEditorOperationMenu = textEditorOperationMenu;
	}

	/**
	 * @return the textEditorFunctions
	 */
	public final Map<String, List<String[]>> getTextEditorFunctions() {
		return textEditorFunctions;
	}

	/**
	 * @param textEditorFunctions the textEditorFunctions to set
	 */
	public final void setTextEditorFunctions(
			Map<String, List<String[]>> textEditorFunctions) {
		this.textEditorFunctions = textEditorFunctions;
	}

	/**
	 * @return the textEditorOperations
	 */
	public final Map<String, List<String[]>> getTextEditorOperations() {
		return textEditorOperations;
	}

	/**
	 * @param textEditorOperations the textEditorOperations to set
	 */
	public final void setTextEditorOperations(
			Map<String, List<String[]>> textEditorOperations) {
		this.textEditorOperations = textEditorOperations;
	}

	/**
	 * @return the openPopup
	 */
	public final String getOpenPopup() {
		return openPopup;
	}

	/**
	 * @param openPopup the openPopup to set
	 */
	public final void setOpenPopup(String openPopup) {
		this.openPopup = openPopup;
	}

	/**
	 * @return
	 * @see idm.dynamictable.UnselectableTable#getRows()
	 */
	public List<String[]> getTextEditorFieldsRows() {
		return textEditorFields.getRows();
	}

	/**
	 * @return
	 * @see idm.dynamictable.UnselectableTable#getTitles()
	 */
	public List<String> getTextEditorFieldsTitles() {
		return textEditorFields.getTitles();
	}

}
