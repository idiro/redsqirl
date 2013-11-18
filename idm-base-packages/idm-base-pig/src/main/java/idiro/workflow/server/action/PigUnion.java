package idiro.workflow.server.action;

import idiro.utils.OrderedFeatureList;
import idiro.utils.FeatureList;
import idiro.workflow.server.DataProperty;
import idiro.workflow.server.Page;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.datatype.MapRedTextType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Action to do a union statement in Pig Latin.
 * @author marcos
 *
 */
public class PigUnion  extends PigElement{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2971963679008329394L;


	public static final String key_featureTable = "Features";

	private Page page1;

	private PigTableUnionInteraction tUnionSelInt;


	public PigUnion() throws RemoteException {
		super(2,2,Integer.MAX_VALUE);

		page1 = addPage("Operations",
				"Union operations",
				1);
		
		tUnionSelInt = new PigTableUnionInteraction(
				key_featureTable,
				"Please specify the operations to be executed for each feature",
				0,
				0,
				this);

		page1.addInteraction(tUnionSelInt);
		
//		addOutputPage();

	}

	public void init() throws RemoteException{
		if(input == null){
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(MapRedTextType.class, 2, Integer.MAX_VALUE));
			input = in;
		}
	}

//	@Override
	public String getName() throws RemoteException {
		return "pig_union";
	}

//	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		List<DFEOutput> in = getDFEInput().get(key_input);
		if(in.size() > 1){
			if(interaction == tUnionSelInt){
				tUnionSelInt.update(in);
			}else if(interaction == dataSubtypeInt){
				updateDataSubTypeInt();
			}
		}
		
	}

	public String getQuery() throws RemoteException{

		HDFSInterface hInt = new HDFSInterface();
		String query = null;
		if(getDFEInput() != null){
			//Output
			DFEOutput out = output.values().iterator().next();
			
			String remove = getRemoveQueryPiece(out.getPath())+"\n\n";
			
			String load = "";
			for (DFEOutput in : getDFEInput().get(key_input)){
				load += hInt.getRelation(in.getPath()) + " = "+getLoadQueryPiece(in) + ";\n";
			}
			load += "\n";

			String select = tUnionSelInt.getQueryPiece(out)+"\n\n";

			String store = getStoreQueryPiece(out, getCurrentName());
			
			if(select.isEmpty()){
				logger.debug("Nothing to select");
			}else{
				query = remove;
				
				query += load;
				
				query += select;
				
				query += store;
			}
		}

		return query;
	}

	@Override
	public FeatureList getInFeatures() throws RemoteException{
		FeatureList ans = 
				new OrderedFeatureList();
		HDFSInterface hInt = new HDFSInterface();
		List<DFEOutput> lOut = getDFEInput().get(PigUnion.key_input);
		Iterator<DFEOutput> it = lOut.iterator();
		while(it.hasNext()){
			DFEOutput out = it.next();
			String relationName = hInt.getRelation(out.getPath());
			FeatureList mapRelation = out.getFeatures();
			Iterator<String> itFeat = mapRelation.getFeaturesNames().iterator();
			while(itFeat.hasNext()){
				String cur = itFeat.next();
				ans.addFeature(relationName+"."+cur, mapRelation.getFeatureType(cur));
			}
		}
		return ans; 
	}

	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return tUnionSelInt.getNewFeatures();
	}

	/**
	 * @return the tUnionSelInt
	 */
	public final PigTableUnionInteraction gettUnionSelInt() {
		return tUnionSelInt;
	}


}
