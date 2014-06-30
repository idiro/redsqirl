package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.utils.PigLanguageManager;

public class PigTableValueBinningInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6690396651848509446L;

	private PigValueBinning pvb;

	public static final String table_name_title = PigLanguageManager
			.getTextWithoutSpace("pig.table_value_interaction.name_column"),
	/** Feature Column Title */
	table_min_title = PigLanguageManager
			.getTextWithoutSpace("pig.table_value_interaction.min_column");

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @param hs
	 * @throws RemoteException
	 */
	public PigTableValueBinningInteraction(String id, String name,
			String legend, int column, int placeInColumn, PigValueBinning pvb)
			throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.pvb = pvb;
		createColumns();
	}

	/**
	 * Create Columns for the interaction
	 * 
	 * @throws RemoteException
	 */
	protected void createColumns() throws RemoteException {

		addColumn(table_name_title, 1, null, null);

		addColumn(table_min_title, 1, "([-+]?[0-9]*\\.?[0-9]+|^$)", null, null);
	}
	
	@Override
	public String check() throws RemoteException {
		String msg = super.check();

		if (msg == null) {
			List<Map<String,String>> out = getValues();
			if(out.size() < 2){
				msg = PigLanguageManager
						.getText("pig.table_value_interaction.msg_size");;
			}else{
				Iterator<Map<String,String>> it = out.iterator();
				if(!it.next().get(table_min_title).isEmpty()){
					msg =  PigLanguageManager
							.getText("pig.table_value_interaction.msg_firstempty");
				}
				Double absMin = null;
				double maxSplit = -Double.MAX_VALUE;
				while(it.hasNext() && msg == null){
					Map<String,String> cur = it.next();
					double curSplit = Double.valueOf(cur.get(table_min_title));
					if(maxSplit > curSplit){
						msg = PigLanguageManager
								.getText("pig.table_value_interaction.msg_notsorted");
					}else{
						if(absMin == null){
							absMin = curSplit;
						}
						maxSplit = curSplit;
					}
				}
				if(msg == null){
					Double[] range = pvb.getMinMaxValues();
					if(range != null && (range[0] > absMin || range[1] < maxSplit)){
						msg = PigLanguageManager
								.getText("pig.table_value_interaction.msg_notinrange");
					}
				}
			}
		}
		
		return msg;
	}
	
	public String getQuery(String in, String feat) throws RemoteException {
		
		String select = "FOREACH " + in + " GENERATE ";
		boolean firstLine = true;
		Iterator<String> it = pvb.getInFeatures().getFeaturesNames().iterator();
		while(it.hasNext()){
			String name = it.next();
			if(firstLine){
				firstLine = false;
				select += name + " AS "+name;
			}else{
				select += ",\n       "+name + " AS "+name;
			}
		}
		
		String casewhen = ",\n       CASE ";
		String prevName = null;
		Iterator<Map<String,String>> vals = getValues().iterator();
		while(vals.hasNext()){
			Map<String,String> val = vals.next();
			if(prevName != null){
				casewhen += "\n         WHEN "+feat+ " < "+
						val.get(PigTableValueBinningInteraction.table_min_title)
						+" THEN '"+prevName+"' ";
			}
			prevName = val.get(PigTableValueBinningInteraction.table_name_title);
		}
		casewhen += "\n         ELSE '"+prevName+"'\n       END";
		
		select +=casewhen+" AS "+pvb.getNewFeatureName();

		logger.debug("select looks like : " + select);
		
		return select;
		
	}

	public void update() throws RemoteException {
		generate();
	}

	protected void generate() throws RemoteException {
		removeGenerators();

		for (int i = 2; i < 16; ++i) {
			generate(i);
		}
		for (int i = 20; i < 51; i += 5) {
			generate(i);
		}
		for (int i = 60; i < 101; i += 10) {
			generate(i);
		}
	}

	protected void generate(int numberBin) throws RemoteException {
		List<Map<String, String>> rows = new LinkedList<Map<String, String>>();
		Double[] range = pvb.getMinMaxValues();
		if (range != null) {
			double interval = (range[1] - range[0])/numberBin;
			Map<String, String> row = null;
			{
				row = new LinkedHashMap<String, String>();
				row.put(table_name_title, String.valueOf(1));
				row.put(table_min_title, "");
				rows.add(row);
			}
			for (int i = 1; i < numberBin; ++i) {
				double cur = range[0] + interval*i;
				row = new LinkedHashMap<String, String>();
				row.put(table_name_title, String.valueOf(i+1));
				row.put(table_min_title, String.valueOf(cur));
				rows.add(row);
			}
			updateGenerator(String.valueOf(numberBin), rows);
		}
	}
}
