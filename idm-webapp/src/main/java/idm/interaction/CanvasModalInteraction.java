package idm.interaction;

import idiro.utils.Tree;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DataFlowElement;
import idm.CanvasModalOutputTab;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * Class that controls an interaction within a page.
 * For each wizard page of Canvas modal, one or several
 * user interaction are available to the user. Essentially
 * this class read and write from the back end.
 * @author etienne
 *
 */
public abstract class CanvasModalInteraction implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 6266349903239331092L;

	static protected Logger logger = Logger.getLogger(CanvasModalInteraction.class);
	
	/**
	 * The back-end interaction
	 */
	protected DFEInteraction inter;
	
	/**
	 * true if the object in memory and the
	 * back-end objects represent the same thing.
	 */
	protected boolean unchanged;
	
	/**
	 * The name of the interaction that will be presented as title on the front end
	 */
	protected String name;
	
	/**
	 * The legend associated with the interaction.
	 */
	protected String legend;
	
	/**
	 * Constructor
	 * @param dfeInter the interaction used
	 * @throws RemoteException
	 */
	public CanvasModalInteraction(DFEInteraction dfeInter) throws RemoteException{
		this.inter = dfeInter;
		this.name = inter.getName();
		this.legend = inter.getLegend();
		readInteraction();
	}
	
	/**
	 * Create a CanvasModalInteraction depending on its DisplayType
	 * @param dfe The DataFlowElement from which the interaction comes from
	 * @param dfeInter The interaction
	 * @return the new CanvasModalInteraction object
	 * @throws RemoteException
	 */
	public static CanvasModalInteraction getNew(
			DFEInteraction dfeInter,
			DataFlowElement dfe,
			CanvasModalOutputTab outputTab) throws RemoteException{
		CanvasModalInteraction ans = null;
		switch(dfeInter.getDisplay()){
		case appendList:
			ans = new AppendListInteraction(dfeInter);
			break;
		case browser:
			ans = new BrowserInteraction(dfeInter,dfe,outputTab);
			break;
		case helpTextEditor:
			ans = new EditorInteraction(dfeInter);
			break;
		case input:
			ans = new InputInteraction(dfeInter);
			break;
		case list:
			ans = new ListInteraction(dfeInter);
			break;
		case table:
			ans = new TableInteraction(dfeInter);
			break;
		default:
			break;
			
		}
		return ans;
	}

	public AppendListInteraction getAppendList(){
		return this instanceof AppendListInteraction ? (AppendListInteraction) this:null;
	}

	public BrowserInteraction getBrowser(){
		return this instanceof BrowserInteraction ? (BrowserInteraction) this:null;
	}
	
	public EditorInteraction getEditor(){
		return this instanceof EditorInteraction ? (EditorInteraction) this:null;
	}
	
	public InputInteraction getInput(){
		return this instanceof InputInteraction ? (InputInteraction) this:null;
	}

	public ListInteraction getList(){
		return this instanceof ListInteraction ? (ListInteraction) this:null;
	}

	public TableInteraction getTable(){
		return this instanceof TableInteraction ? (TableInteraction) this:null;
	}
	
	
	/**
	 * Read the back-end object and make the data available to the client
	 * @throws RemoteException
	 */
	public abstract void readInteraction() throws RemoteException;
	
	/**
	 * Write the data to the back-end object
	 * @throws RemoteException
	 */
	public abstract void writeInteraction() throws RemoteException;

	/**
	 * Set the unchanged class field
	 */
	public abstract void setUnchanged();
	

	/**
	 * Utility function to print a tree
	 * @param tree
	 * @return
	 * @throws RemoteException
	 */
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
	 * @return the inter
	 */
	public DFEInteraction getInter() {
		return inter;
	}

	/**
	 * @return the unchanged
	 */
	public boolean isUnchanged() {
		return unchanged;
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the legend
	 */
	public final String getLegend() {
		return legend;
	}

	/**
	 * @param legend the legend to set
	 */
	public final void setLegend(String legend) {
		this.legend = legend;
	}

	/**
	 * @param unchanged the unchanged to set
	 */
	public final void setUnchanged(boolean unchanged) {
		this.unchanged = unchanged;
	}

	/**
	 * @return
	 * @throws RemoteException
	 * @see idiro.workflow.server.interfaces.DFEInteraction#getId()
	 */
	public String getId() throws RemoteException {
		return inter.getId();
	}
	
	/**
	 * @return
	 * @throws RemoteException
	 */
	public String getDisplayType() throws RemoteException{
	    return inter.getDisplay().toString();
	}
}
