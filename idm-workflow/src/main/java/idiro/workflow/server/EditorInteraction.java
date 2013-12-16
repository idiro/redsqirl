package idiro.workflow.server;

import idiro.utils.FeatureList;
import idiro.utils.Tree;
import idiro.workflow.server.enumeration.DisplayType;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map;

public class EditorInteraction extends UserInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -834634281289412942L;

	public EditorInteraction(String name, String legend,
			int column, int placeInColumn) throws RemoteException {
		super(name, legend, DisplayType.helpTextEditor, column, placeInColumn);
	}
	

	public void setValue(String value)  throws RemoteException{
		init();
		Tree<String> output =  tree.getFirstChild("editor").getFirstChild("output");
		output.removeAllChildren();
		output.add(value);
	}
	
	public String getValue(){
		String ans = null;
		if(display == DisplayType.helpTextEditor){
			try{
				ans = getTree().getFirstChild("editor").getFirstChild("output").getFirstChild().getHead();
			}catch(Exception e){
				logger.error("Tree structure incorrect");
			}
		}
		return ans;
	}
	
	public void init() throws RemoteException{
		Tree<String> editor= null;
		if(tree.isEmpty()){
			editor = tree.add("editor");
			editor.add("output");
			editor.add("keywords");
			//editor.add("help");
		}
	}
	
	public void addFeatures(FeatureList fl) throws RemoteException{
		init();
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
	
	public void addFeatures(Map<String,String> fl) throws RemoteException{
		init();
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
	
	public void removeFeatures() throws RemoteException{
		init();
		tree.getFirstChild("editor").getFirstChild("keywords").remove("features");
	}
	
	public void addHelpMenu(Tree<String> submenu) throws RemoteException{
		init();
		Tree<String> help = tree.getFirstChild("editor").getFirstChild("help");
		if(help == null){
			help = tree.getFirstChild("editor").add("help");
		}
		help.add(submenu);
	}
	
	public void removeHelpMenu() throws RemoteException{
		init();
		tree.getFirstChild("editor").remove("help");
	}

}
