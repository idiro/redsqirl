package idm.interaction;

import idiro.workflow.server.interfaces.DFEInteraction;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

public class EditorInteraction extends CanvasModalInteraction {

	private EditorFromTree edit;
	
	public EditorInteraction(DFEInteraction dfeInter) throws RemoteException {
		super(dfeInter);
		edit = new EditorFromTree(dfeInter.getTree());
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
		edit.readInteraction();
	}

	/**
	 * @return
	 * @see idm.interaction.EditorFromTree#getTextEditorValue()
	 */
	public final String getTextEditorValue() {
		return edit.getTextEditorValue();
	}

	/**
	 * @param textEditorValue
	 * @see idm.interaction.EditorFromTree#setTextEditorValue(java.lang.String)
	 */
	public final void setTextEditorValue(String textEditorValue) {
		edit.setTextEditorValue(textEditorValue);
	}

	/**
	 * @return
	 * @see idm.interaction.EditorFromTree#getTextEditorFields()
	 */
	public final Map<String, String> getTextEditorFields() {
		return edit.getTextEditorFields();
	}

	/**
	 * @param textEditorFields
	 * @see idm.interaction.EditorFromTree#setTextEditorFields(java.util.Map)
	 */
	public final void setTextEditorFields(Map<String, String> textEditorFields) {
		edit.setTextEditorFields(textEditorFields);
	}

	/**
	 * @return
	 * @see idm.interaction.EditorFromTree#getTextEditorFunctionMenu()
	 */
	public final List<SelectItem> getTextEditorFunctionMenu() {
		return edit.getTextEditorFunctionMenu();
	}

	/**
	 * @param textEditorFunctionMenu
	 * @see idm.interaction.EditorFromTree#setTextEditorFunctionMenu(java.util.List)
	 */
	public final void setTextEditorFunctionMenu(
			List<SelectItem> textEditorFunctionMenu) {
		edit.setTextEditorFunctionMenu(textEditorFunctionMenu);
	}

	/**
	 * @return
	 * @see idm.interaction.EditorFromTree#getTextEditorOperationMenu()
	 */
	public final List<SelectItem> getTextEditorOperationMenu() {
		return edit.getTextEditorOperationMenu();
	}

	/**
	 * @param textEditorOperationMenu
	 * @see idm.interaction.EditorFromTree#setTextEditorOperationMenu(java.util.List)
	 */
	public final void setTextEditorOperationMenu(
			List<SelectItem> textEditorOperationMenu) {
		edit.setTextEditorOperationMenu(textEditorOperationMenu);
	}

	/**
	 * @return
	 * @see idm.interaction.EditorFromTree#getTextEditorFunctions()
	 */
	public final Map<String, List<String[]>> getTextEditorFunctions() {
		return edit.getTextEditorFunctions();
	}

	/**
	 * @param textEditorFunctions
	 * @see idm.interaction.EditorFromTree#setTextEditorFunctions(java.util.Map)
	 */
	public final void setTextEditorFunctions(
			Map<String, List<String[]>> textEditorFunctions) {
		edit.setTextEditorFunctions(textEditorFunctions);
	}

	/**
	 * @return
	 * @see idm.interaction.EditorFromTree#getTextEditorOperations()
	 */
	public final Map<String, List<String[]>> getTextEditorOperations() {
		return edit.getTextEditorOperations();
	}

	/**
	 * @param textEditorOperations
	 * @see idm.interaction.EditorFromTree#setTextEditorOperations(java.util.Map)
	 */
	public final void setTextEditorOperations(
			Map<String, List<String[]>> textEditorOperations) {
		edit.setTextEditorOperations(textEditorOperations);
	}

	/**
	 * @return
	 * @see idm.interaction.EditorFromTree#getOpenPopup()
	 */
	public final String getOpenPopup() {
		return edit.getOpenPopup();
	}

	/**
	 * @param openPopup
	 * @see idm.interaction.EditorFromTree#setOpenPopup(java.lang.String)
	 */
	public final void setOpenPopup(String openPopup) {
		edit.setOpenPopup(openPopup);
	}

}
