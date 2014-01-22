package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.workflow.server.TableInteraction;
import idiro.workflow.server.action.utils.PigDictionary;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Interaction for selecting output of a union action.
 * The interaction is a table with for columns: 'Relation',
 * 'Operation', 'Feature name', 'Type'.
 * 
 * @author marcos
 *
 */
public class PigTableUnionInteraction extends TableInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4973968329944889374L;

	private PigUnion hu;

	public static final String table_relation_title = "Relation", 
			table_op_title = "Operation",
			table_feat_title = "Feature_name",
			table_type_title = "Type";

	public PigTableUnionInteraction(String name, String legend,
			int column, int placeInColumn, PigUnion hu)
					throws RemoteException {
		super(name, legend, column, placeInColumn);
		this.hu = hu;
		getRootTable();
	}


	@Override
	public String check() throws RemoteException{
		String msg = null;
		List<Map<String,String>> lRow = getValues();
		Iterator<Map<String,String>> rows;

		if(lRow.isEmpty()){
			msg = "A relation is composed of at least 1 column";
		}else{

			Map<String,List<Map<String,String> > > mapRelationRow = getSubQuery();
			FeatureList mapFeatType = getNewFeatures();

			//Check if we have the right number of list
			if(mapRelationRow.keySet().size() != hu.getAllInputComponent().size()){
				msg = "One or several input relation are missing in the query";
			}

			Iterator<String> itRelation = mapRelationRow.keySet().iterator();
			while(itRelation.hasNext() && msg == null){
				String relationName = itRelation.next();
				List<Map<String,String>> listRow = mapRelationRow.get(relationName);
				//Check if there is the same number of row for each input
				if(listRow.size() != lRow.size() / mapRelationRow.keySet().size()){
					msg = relationName+ " does not have the right number of rows compare to others";
				}

				Set<String> featuresTitle = new LinkedHashSet<String>();
				rows = listRow.iterator();
				while(rows.hasNext() && msg == null){
					//
					Map<String,String> row = rows.next();
					try{
						if( ! PigDictionary.check(
								row.get(table_type_title), 
								PigDictionary.getInstance().getReturnType(
										row.get(table_op_title),
										hu.getInFeatures())
								)){
							msg = "Error the type returned does not correspond for feature "+
									row.get(table_feat_title);
						}else{
							String featureName = row.get(table_feat_title)
									.toUpperCase();
							logger.info("is it contained in map : "+featureName);
							if(!mapFeatType.containsFeature(featureName)){
								msg = "Some Features are not implemented for every relation";
							}else{
								featuresTitle.add(featureName);
								if(!PigDictionary.getType(
										row.get(table_type_title))
										.equals(mapFeatType.getFeatureType(featureName)
												)){
									msg = "Type of "+featureName+ " inconsistant";
								}
							}
						}
					}catch(Exception e){
						msg = e.getMessage();
					}
				}


				if(msg == null && 
						listRow.size() !=
						featuresTitle.size()){
					msg = lRow.size()-featuresTitle.size()+
							" features has the same name, total "+lRow.size() +" featuresTitle "+featuresTitle.size();
					logger.debug(featuresTitle);
				}
			}
		}

		return msg;
	}

	public String checkExpression(String expression, String modifier)
			throws RemoteException {
		String error = null;
		try {
			if (PigDictionary.getInstance().getReturnType(
					expression,
					hu.getInFeatures()
					) == null) {
				error = "Expression does not have a return type";
			}
		} catch (Exception e) {
			error = "Error trying to get expression return type";
			logger.error(error, e);
		}
		return error;
	}


	public void update(List<DFEOutput> in) throws RemoteException{

		updateColumnConstraint(
				table_relation_title, 
				null,
				null,
				hu.getAliases().keySet());
		
		
		updateColumnConstraint(
				table_feat_title,
				"[a-zA-Z]([A-Za-z0-9_]{0,29})",
				hu.getAllInputComponent().size(),
				null
				);
		
		updateEditor(table_op_title,
				PigDictionary.generateEditor(PigDictionary.getInstance().createDefaultSelectHelpMenu(),hu.getInFeatures()));


		//Set the Generator
		List<Map<String,String>> copyRows = new LinkedList<Map<String,String>>();
		FeatureList firstIn = in.get(0).getFeatures();
		Iterator<String> featIt = firstIn.getFeaturesNames().iterator();
		while(featIt.hasNext()){
			String feature = featIt.next();
			FeatureType featureType = firstIn.getFeatureType(feature); 
			Iterator<DFEOutput> itIn = in.iterator();
			itIn.next();
			boolean found = true;
			while(itIn.hasNext() && found){
				DFEOutput cur = itIn.next();
				found = featureType.equals(cur.getFeatures().getFeatureType(feature));
			}
			if(found){
				Iterator<String> aliases = hu.getAliases().keySet().iterator();
				while(aliases.hasNext()){
					Map<String,String> curMap = new LinkedHashMap<String,String>();
					String alias = aliases.next();
					
					curMap.put(table_relation_title,alias); 
					curMap.put(table_op_title,alias+"."+feature);
					curMap.put(table_feat_title,feature);
					curMap.put(table_type_title,
							PigDictionary.getPigType(featureType)
							);
					
					copyRows.add(curMap);
				}
			}
		}
		updateGenerator("copy", copyRows);
	}


	protected void getRootTable() throws RemoteException{

		
		addColumn(
				table_relation_title, 
				null, 
				null, 
				null);
		
		addColumn(
				table_op_title,
				null,
				null,
				null
				);
		
		addColumn(
				table_feat_title,
				null,
				"[a-zA-Z]([A-Za-z0-9_]{0,29})",
				null,
				null
				);
		
		
		List<String> types = new LinkedList<String>();
		types.add(FeatureType.BOOLEAN.name());
		types.add(FeatureType.INT.name());
		types.add(FeatureType.DOUBLE.name());
		types.add(FeatureType.FLOAT.name());
		types.add(FeatureType.STRING.name());
		
		addColumn(
				table_type_title,
				null,
				types,
				null);
	}

	public FeatureList getNewFeatures() throws RemoteException{
		FeatureList new_features = new OrderedFeatureList();
		
		Map<String,List<Map<String,String> > > mapRelationRow = getSubQuery();

		Iterator<Map<String,String>> rowIt = mapRelationRow.get(mapRelationRow.keySet().iterator().next()).iterator();
		while(rowIt.hasNext()){
			Map<String,String> rowCur = rowIt.next();
			String name = rowCur.get(table_feat_title);
			String type = rowCur.get(table_type_title);
			new_features.addFeature(name, PigDictionary.getType(type));
		}
		return new_features;


	}

	public Map<String,List<Map<String,String>>> getSubQuery() throws RemoteException{
		Map<String,List<Map<String,String> > >  mapRelationRow = 
				new LinkedHashMap<String,List<Map<String,String> >>();
		
		List<Map<String,String>> lRow = getValues();
		Iterator<Map<String,String>> rows = lRow.iterator();

		while(rows.hasNext()){
			Map<String,String> row = rows.next();
			String relationName = row.get(table_relation_title);
			if(!mapRelationRow.containsKey(relationName)){
				
				List<Map<String,String>> list = new LinkedList<Map<String,String>>();
				mapRelationRow.put(relationName, list);
				logger.info("adding to "+relationName);
			}
			mapRelationRow.get(relationName).add(row);
		}

		return mapRelationRow;
	}

	public String getCreateQueryPiece(DFEOutput out) throws RemoteException{
		logger.debug("create features...");
		String createSelect = "";
		FeatureList features = getNewFeatures();
		Iterator<String> it = features.getFeaturesNames().iterator();
		if(it.hasNext()){
			String featName = it.next();
			String type = PigDictionary.getPigType(features.getFeatureType(featName));
			createSelect = "("+featName+" "+type;
		}
		while(it.hasNext()){
			String featName = it.next();
			String type = PigDictionary.getPigType(features.getFeatureType(featName));
			createSelect+=","+featName+" "+type;
		}
		createSelect +=")";

		return createSelect;
	}

	public String getQueryPiece(DFEOutput out) throws RemoteException{
		logger.debug("select...");
		String select = "";
		String union = "";

		Map<String,List<Map<String,String>>> subQuery = getSubQuery();
		Iterator<String> it = subQuery.keySet().iterator();
		while(it.hasNext()){
			String relationName = it.next();
			Iterator<Map<String,String>> itTree = subQuery.get(relationName).iterator();
			if(itTree.hasNext()){
				Map<String,String> featTree = itTree.next();
				String featName = featTree.get(table_feat_title);
				String op = featTree.get(table_op_title).replaceAll(Pattern.quote(relationName+"."), "");
				select += hu.getNextName() +" = FOREACH " + relationName + " GENERATE "+op+" AS "+featName;
			}
			while(itTree.hasNext()){
				Map<String,String> featTree = itTree.next();
				String featName = featTree.get(table_feat_title);
				String op = featTree.get(table_op_title).replaceAll(Pattern.quote(relationName+"."), "");;
				select +=", "+op+" AS "+featName;
			}
			select +=";\n\n";

			union += hu.getCurrentName();
			if (it.hasNext()){
				union += ", ";
			}
		}
		if (!union.isEmpty()){
			select += hu.getNextName()+" = UNION "+union+";";
		}
		return select;
	}
}
