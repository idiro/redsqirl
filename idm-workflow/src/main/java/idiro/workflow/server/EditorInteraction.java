package idiro.workflow.server;

import idiro.utils.FeatureList;
import idiro.utils.Tree;
import idiro.workflow.server.enumeration.DisplayType;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map;
/**
 * Template class for an editor class
 * @author keith
 *
 */
public class EditorInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -834634281289412942L;
	/**
	 * Constructor with the necessary params
	 * @param id of interaction
	 * @param name of interaction
	 * @param legend of the interaction
	 * @param column number to be placed in
	 * @param placeInColumn place in the column
	 * @throws RemoteException
	 */
	public EditorInteraction(String id, String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(id, name, legend, DisplayType.helpTextEditor, column, placeInColumn);
		init();
	}
	/**
	 * Initialize the interactions tree
	 * @throws RemoteException
	 */
	protected void init() throws RemoteException{
		Tree<String> editor= null;
		if(tree.isEmpty()){
			editor = tree.add("editor");
			editor.add("output");
			editor.add("keywords");
			//editor.add("help");
		}
	}
	/**
	 * Set the value of the interaction
	 * @param value
	 * @throws RemoteException
	 */
	public void setValue(String value)  throws RemoteException{
		logger.info("getting output of editor");
		Tree<String> output =tree.getFirstChild("editor").getFirstChild("output");
		output.removeAllChildren();
		output.add(value);
	}
	/**
	 * Get the value of the Interaction
	 * @return
	 * @throws RemoteException
	 */
	public String getValue() throws RemoteException{
		String ans = null;
		if(display == DisplayType.helpTextEditor){
			try{
				if(getTree().getFirstChild("editor").getFirstChild("output").getFirstChild() != null){
					ans = getTree().getFirstChild("editor").getFirstChild("output").getFirstChild().getHead();
				}else{
					ans = "";
				}
			}catch(Exception e){
				logger.error(getId()+": Tree structure incorrect");
			}
		}
		return ans;
	}
	
	/**
	 * Add a features list to the interaction
	 * @param fl
	 * @throws RemoteException
	 */
	public void addFeatures(FeatureList fl) throws RemoteException{
		Iterator<String> it = fl.getFeaturesNames().iterator();
		Tree<String> feats = tree.getFirstChild("editor").getFirstChild("keywords").getFirstChild("features");
		if(feats == null){
			feats = tree.getFirstChild("editor").getFirstChild("keywords").add("features");
		}
		while(it.hasNext()){
			String name = it.next();
			String type = fl.getFeatureType(name).name();
			Tree<String> feature = feats.add("feature");
			feature.add("name").add(name);
			feature.add("type").add(type);
		}
	}
	
	/**
	 * Add a Map of features to the interaction
	 * @param fl
	 * @throws RemoteException
	 */
	public void addFeatures(Map<String,String> fl) throws RemoteException{
		Iterator<String> it = fl.keySet().iterator();
		Tree<String> feats = tree.getFirstChild("editor").getFirstChild("keywords").getFirstChild("features");
		if(feats == null){
			feats = tree.getFirstChild("editor").getFirstChild("keywords").add("features");
		}
		while(it.hasNext()){
			String name = it.next();
			String type = fl.get(name);
			Tree<String> feature = feats.add("feature");
			feature.add("name").add(name);
			feature.add("type").add(type);
		}
		
	}
	/**
	 * Remove the features of the Interaction
	 * @throws RemoteException
	 */
	public void removeFeatures() throws RemoteException{
		tree.getFirstChild("editor").getFirstChild("keywords").remove("features");
	}
	/**
	 * Add a help Menu to the interaction
	 * @param submenu
	 * @throws RemoteException
	 */
	public void addHelpMenu(Tree<String> submenu) throws RemoteException{
		Tree<String> help = tree.getFirstChild("editor").getFirstChild("help");
		if(help == null){
			help = tree.getFirstChild("editor").add("help");
		}
		help.add(submenu);
	}
	/**
	 * Remove the help Menu
	 * @throws RemoteException
	 */
	public void removeHelpMenu() throws RemoteException{
		tree.getFirstChild("editor").remove("help");
	}

}
