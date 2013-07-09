package idiro.workflow.server.action;

import idiro.utils.Tree;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * 
 * @author marcos
 *
 */
public class PigGroupInteraction extends UserInteraction{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 539841111561345129L;

	protected static Logger logger = Logger.getLogger(PigGroupInteraction.class);

	public PigGroupInteraction(String name, String legend, DisplayType display,
			int column, int placeInColumn) throws RemoteException {
		super(name, legend, display, column, placeInColumn);
	}
	
	public void update(DFEOutput in) throws RemoteException{
		Tree<String> list = null;
		if(tree.getSubTreeList().isEmpty()){
			list = tree.add("applist");
			list.add("output");
		}else{
			list = tree.getFirstChild("applist"); 
			list.remove("value");
		}
		Tree<String> value = list.add("value");
		Iterator<String> it = in.getFeatures().keySet().iterator();
		while(it.hasNext()){
			value.add(it.next());
		}
	}
	
	public String getQueryPiece(String relationName) throws RemoteException{
		logger.debug("group...");
		String groupby = "";
		
		if(getTree()
				.getFirstChild("applist")
				.getFirstChild("output").getSubTreeList().size() > 0){
			Iterator<Tree<String>> gIt = getTree()
					.getFirstChild("applist")
					.getFirstChild("output").getChildren("value").iterator();
			if(gIt.hasNext()){
				groupby = gIt.next().getFirstChild().getHead();
			}
			while(gIt.hasNext()){
				groupby += ","+gIt.next().getFirstChild().getHead();
			}
			if(!groupby.isEmpty()){
				groupby = "GROUP "+relationName+" BY ("+groupby+")";
			}
		}
		return groupby;
	}
}
