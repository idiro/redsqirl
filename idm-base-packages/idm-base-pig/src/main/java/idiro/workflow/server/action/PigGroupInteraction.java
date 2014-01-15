package idiro.workflow.server.action;

import idiro.utils.Tree;
import idiro.workflow.server.AppendListInteraction;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.apache.log4j.Logger;


/**
 * 
 * @author marcos
 *
 */
public class PigGroupInteraction extends AppendListInteraction{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 539841111561345129L;

	protected static Logger logger = Logger.getLogger(PigGroupInteraction.class);

	public PigGroupInteraction(String name, String legend, DisplayType display,
			int column, int placeInColumn) throws RemoteException {
		super(name, legend, column, placeInColumn);
	}
	
	public void update(DFEOutput in) throws RemoteException{
		Tree<String> list = null;
		if(tree.getSubTreeList().isEmpty()){
			list = tree.add("applist");
			list.add("output");
		}else{
			list = tree.getFirstChild("applist"); 
			list.remove("values");
		}
		Tree<String> values = list.add("values");
		Tree<String> display = list.add("display");
		display.add("checkbox");
		Iterator<String> it = in.getFeatures().getFeaturesNames().iterator();
		while(it.hasNext()){
			values.add("value").add(it.next());
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
			if(!groupby.isEmpty() || !groupby.equalsIgnoreCase("")){
				groupby = "GROUP "+relationName+" BY ("+groupby+")";
			} 
		}else{
			groupby = "GROUP "+relationName+" ALL";
			
		}
		return groupby;
	}
	
	public Set<String> getAggregationFeatures(DFEOutput in) throws RemoteException{
		Set<String> aggregationFeatures = new HashSet<String>();
		
		in.getFeatures().getFeaturesNames();
		if(in.getFeatures().getFeaturesNames().size() > 0){
			Iterator<String> gIt =in.getFeatures().getFeaturesNames().iterator();
			while (gIt.hasNext()){
				aggregationFeatures.add(gIt.next());
			}
		}
	
		return aggregationFeatures;
	}
}
