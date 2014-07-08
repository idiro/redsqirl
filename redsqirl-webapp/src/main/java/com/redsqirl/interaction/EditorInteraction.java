package com.redsqirl.interaction;


import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import com.redsqirl.dynamictable.UnselectableTable;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;

/**
 * Make Editor interaction available to client.
 * @author etienne
 *
 */
public class EditorInteraction extends CanvasModalInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9115060433093718102L;
	
	private EditorFromTree edit;
	
	public EditorInteraction(DFEInteraction dfeInter) throws RemoteException {
		super(dfeInter);
		/*logger.info("tree: "+printTree(dfeInter.getTree().getFirstChild("editor")
				.getFirstChild("keywords")));*/
	}

	@Override
	public void writeInteraction() throws RemoteException {
		inter.getTree().getFirstChild("editor").getFirstChild("output")
				.removeAllChildren();
		inter.getTree().getFirstChild("editor").getFirstChild("output")
				.add(getTextEditorValue());
	}

	@Override
	public void setUnchanged() {
		try {
			unchanged = getTextEditorValue().equals(inter.getTree()
					.getFirstChild("editor").getFirstChild("output")
					.getFirstChild().getHead());
		} catch (Exception e) {
			unchanged = false;
		}
	}

	@Override
	public void readInteraction() throws RemoteException {
		edit = new EditorFromTree(inter.getTree());
		edit.readInteraction();
	}


	/**
	 * @return
	 * @see com.redsqirl.interaction.EditorFromTree#getTextEditorValue()
	 */
	public final String getTextEditorValue() {
		return edit.getTextEditorValue();
	}

	/**
	 * @param textEditorValue
	 * @see com.redsqirl.interaction.EditorFromTree#setTextEditorValue(java.lang.String)
	 */
	public final void setTextEditorValue(String textEditorValue) {
		edit.setTextEditorValue(textEditorValue);
	}

	/**
	 * @return
	 * @see com.redsqirl.interaction.EditorFromTree#getTextEditorFields()
	 */
	public final UnselectableTable getTextEditorFields() {
		return edit.getTextEditorFields();
	}

	/**
	 * @param textEditorFields
	 * @see com.redsqirl.interaction.EditorFromTree#setTextEditorFields(java.util.Map)
	 */
	public final void setTextEditorFields(UnselectableTable textEditorFields) {
		edit.setTextEditorFields(textEditorFields);
	}

	/**
	 * @return
	 * @see com.redsqirl.interaction.EditorFromTree#getTextEditorFunctionMenu()
	 */
	public final List<String> getTextEditorFunctionMenu() {
		return edit.getTextEditorFunctionMenu();
	}

	/**
	 * @param textEditorFunctionMenu
	 * @see com.redsqirl.interaction.EditorFromTree#setTextEditorFunctionMenu(java.util.List)
	 */
	public final void setTextEditorFunctionMenu(
			List<String> textEditorFunctionMenu) {
		edit.setTextEditorFunctionMenu(textEditorFunctionMenu);
	}

	/**
	 * @return
	 * @see com.redsqirl.interaction.EditorFromTree#getTextEditorOperationMenu()
	 */
	public final List<String> getTextEditorOperationMenu() {
		return edit.getTextEditorOperationMenu();
	}

	/**
	 * @param textEditorOperationMenu
	 * @see com.redsqirl.interaction.EditorFromTree#setTextEditorOperationMenu(java.util.List)
	 */
	public final void setTextEditorOperationMenu(
			List<String> textEditorOperationMenu) {
		edit.setTextEditorOperationMenu(textEditorOperationMenu);
	}

	/**
	 * @return
	 * @see com.redsqirl.interaction.EditorFromTree#getTextEditorFunctions()
	 */
	public final Map<String, List<String[]>> getTextEditorFunctions() {
		return edit.getTextEditorFunctions();
	}

	/**
	 * @param textEditorFunctions
	 * @see com.redsqirl.interaction.EditorFromTree#setTextEditorFunctions(java.util.Map)
	 */
	public final void setTextEditorFunctions(
			Map<String, List<String[]>> textEditorFunctions) {
		edit.setTextEditorFunctions(textEditorFunctions);
	}

	/**
	 * @return
	 * @see com.redsqirl.interaction.EditorFromTree#getTextEditorOperations()
	 */
	public final Map<String, List<String[]>> getTextEditorOperations() {
		return edit.getTextEditorOperations();
	}

	/**
	 * @param textEditorOperations
	 * @see com.redsqirl.interaction.EditorFromTree#setTextEditorOperations(java.util.Map)
	 */
	public final void setTextEditorOperations(
			Map<String, List<String[]>> textEditorOperations) {
		edit.setTextEditorOperations(textEditorOperations);
	}

	/**
	 * @return
	 * @see com.redsqirl.interaction.EditorFromTree#getOpenPopup()
	 */
	public final String getOpenPopup() {
		return edit.getOpenPopup();
	}

	/**
	 * @param openPopup
	 * @see com.redsqirl.interaction.EditorFromTree#setOpenPopup(java.lang.String)
	 */
	public final void setOpenPopup(String openPopup) {
		edit.setOpenPopup(openPopup);
	}

	/**
	 * @return the edit
	 */
	public final EditorFromTree getEdit() {
		return edit;
	}

}
