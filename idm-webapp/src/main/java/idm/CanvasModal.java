package idm;

import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEPage;
import idiro.workflow.server.interfaces.DataFlow;
import idiro.workflow.server.interfaces.DataFlowElement;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

/** CanvasModal
 * 
 * Class to class control of the dynamic form
 * 
 * @author Igor.Souza
 */
public class CanvasModal extends BaseBean {

	private static Logger logger = Logger.getLogger(CanvasModal.class);

	private String list = "";
	private List<SelectItem> listItens = new ArrayList<SelectItem>();
	private List<Entry> listFunctions = new ArrayList<Entry>();
	private List<Entry> listFields = new ArrayList<Entry>();
	private List<Entry> listFunctionsType = new ArrayList<Entry>();
	private String command = "";


	/** next
	 * 
	 * Methods to control the sequence of screens
	 * 
	 * @return String - to Navigation
	 * @author Igor.Souza
	 */
	public String next() {

		logger.info("next ");

		return "next";
	}

	public String close() {

		logger.info("close ");

		return "close";
	}

	/** openTextEditor
	 * 
	 * Methods to mount screen dynamic form
	 * 
	 * @return 
	 * @author Igor.Souza
	 */
	@PostConstruct
	public void openTextEditor() {

		logger.info("openModal");

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
		
		
		
		
		try {
			
			DataFlowInterface dfi =  getworkFlowInterface();
			dfi.addWorkflow("canvas1");
			DataFlow df = dfi.getWorkflow("canvas1");
			
			//logger.info(df.getAllWANameWithClassName());
			
			String idElement = df.addElement("Source");
			DataFlowElement dfe = df.getElement(idElement);
			
			for (DFEPage dfePage : dfe.getPageList()) {
				
				dfePage.getTitle();
				dfePage.getNbColumn();
				
				for (DFEInteraction dfeInteraction : dfePage.getInteractions()) {
					
					DisplayType display = dfeInteraction.getDisplay();
					dfeInteraction.getTree();
					
					logger.info(display);
					logger.info(dfeInteraction.getTree());
					
				}
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		
		
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

		
		logger.info(getCommand());
		
		
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

	/** check
	 * 
	 * Methods to Check if the entry are correct or not for this action.
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void check() {

		logger.info("check");

	}

	/** completeFunction
	 * 
	 * Methods to complete the field
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	/*public void completeFunction(){

		logger.info("completeFunction");

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String nameFunction = params.get("nameFunctions");

		logger.info("Command: " + getCommand());
		logger.info("Function: " + nameFunction);
		setCommand(getCommand() + " " + nameFunction);

	}*/

	/** completeFilds
	 * 
	 * Methods to complete the field
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	/*public void completeFilds(){

		logger.info("completeFilds");

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String nameFild = params.get("nameFilds");

		logger.info(getCommand() + " " + nameFild);
		setCommand(getCommand() + " " + nameFild);

	}*/

	/** changeFunctions
	 * 
	 * Methods to retrieve the new Functions
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void  changeFunctions(){

		logger.info("changeFunctions");

		setListFunctions(new ArrayList<Entry>());
		
		Entry e1 = new Entry("Function 8", "Return 8");
		Entry e2 = new Entry("Function 9", "Return 9");
		Entry e3 = new Entry("Function 0", "Return 0");
		getListFunctions().add(e1);
		getListFunctions().add(e2);
		getListFunctions().add(e3);

	}

	public void apply(){

	}

	public void reset(){

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

}