package com.redsqirl.interaction;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.redsqirl.CanvasModalOutputTab;
import com.redsqirl.utils.Tree;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DataFlowElement;

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
	 * The text tip associated with the interaction.
	 */
	protected String textTip;
	
	/**
	 * The copied over tree
	 */
	protected Tree<String> tree;
	
	/**
	 * Constructor
	 * @param dfeInter the interaction used
	 * @throws RemoteException
	 */
	public CanvasModalInteraction(DFEInteraction dfeInter) throws RemoteException{
		this.inter = dfeInter;
		this.name = inter.getName();
		this.legend = inter.getLegend();
		this.textTip = inter.getTextTip();
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
		ans.readInteraction();
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
	
	@SuppressWarnings("unchecked")
	public void setTree(){
		try {
			Object ans = null;
			//Check if T is instance of Serializeble other throw CloneNotSupportedException
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			//Serialize it
			out.writeObject(inter.getTree());
			byte[] bytes = bos.toByteArray();
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
			//Deserialize it
			ans = ois.readObject();
			tree = (Tree<String>)ans;
		} catch (Exception e) {
			logger.error("Fail to copy the tree over: "+e.getMessage(),e);
		}
	}
	
	public Tree<String> getTree(){
		return tree;
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
	 * @see com.redsqirl.workflow.server.interfaces.DFEInteraction#getId()
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

	public String getTextTip() {
		return textTip;
	}

	public void setTextTip(String textTip) {
		this.textTip = textTip;
	}
	
}