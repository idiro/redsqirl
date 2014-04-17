package idm.interaction;

import idiro.utils.Tree;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

public class EditorFromTree {

	static protected Logger logger = Logger.getLogger(EditorFromTree.class);
	
	private Tree<String> tree;
	
	private String textEditorValue;
	private Map<String, String> textEditorFields;
	private List<SelectItem> textEditorFunctionMenu;
	private List<SelectItem> textEditorOperationMenu;
	private Map<String, List<String[]>> textEditorFunctions;
	private Map<String, List<String[]>> textEditorOperations;
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
		textEditorFields = new LinkedHashMap<String, String>();
		try {
			list = tree.getFirstChild("editor")
					.getFirstChild("keywords").getFirstChild("features")
					.getSubTreeList();
		} catch (Exception e) {
			list = null;
		}
		if (list != null && !list.isEmpty()) {
			// logger.info("list not null: "+list.toString());
			for (Tree<String> tree : list) {
				textEditorFields.put(tree.getFirstChild("name").getFirstChild()
						.getHead(), tree.getFirstChild("type").getFirstChild()
						.getHead());
			}
		}

		// Get the functions and operations
		textEditorFunctionMenu = new LinkedList<SelectItem>();
		textEditorOperationMenu = new LinkedList<SelectItem>();
		textEditorFunctions = new HashMap<String, List<String[]>>();
		textEditorOperations = new HashMap<String, List<String[]>>();
		try {
			list = tree.getFirstChild("editor")
					.getFirstChild("help").getSubTreeList();
		} catch (Exception e) {
			list = null;
		}
		if (list != null && !list.isEmpty()) {
			// logger.info("list not null: " + list.toString());
			for (Tree<String> tree : list) {
				logger.info("list value " + tree.getHead());
				Map<String, List<String[]>> cur = null;
				List<SelectItem> curL = null;
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
					curL.add(new SelectItem(menuName, menuName));
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
	public final Map<String, String> getTextEditorFields() {
		return textEditorFields;
	}

	/**
	 * @param textEditorFields the textEditorFields to set
	 */
	public final void setTextEditorFields(Map<String, String> textEditorFields) {
		this.textEditorFields = textEditorFields;
	}

	/**
	 * @return the textEditorFunctionMenu
	 */
	public final List<SelectItem> getTextEditorFunctionMenu() {
		return textEditorFunctionMenu;
	}

	/**
	 * @param textEditorFunctionMenu the textEditorFunctionMenu to set
	 */
	public final void setTextEditorFunctionMenu(
			List<SelectItem> textEditorFunctionMenu) {
		this.textEditorFunctionMenu = textEditorFunctionMenu;
	}

	/**
	 * @return the textEditorOperationMenu
	 */
	public final List<SelectItem> getTextEditorOperationMenu() {
		return textEditorOperationMenu;
	}

	/**
	 * @param textEditorOperationMenu the textEditorOperationMenu to set
	 */
	public final void setTextEditorOperationMenu(
			List<SelectItem> textEditorOperationMenu) {
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

}
