package com.redsqirl.useful;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.WordUtils;

import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.server.interfaces.DFEOutput;

public class WorkflowHelpUtils {

	
	public static String generateHelp(String wfName, Map<String,DFEOutput> inputs,Map<String,DFEOutput> outputs) throws RemoteException{
		return generateHelp(wfName, "",inputs,outputs);
	}
	
	public static String generateHelp(String wfName, String description,Map<String,DFEOutput> inputs,Map<String,DFEOutput> outputs) throws RemoteException{
		String help = "<h1>"+WordUtils.capitalizeFully(wfName.replace("_", " "))+"</h1>";
		if(description != null && !description.isEmpty()){
			help+="<p>"+description+"</p>";
		}
		
		help+="<p>"+wfName+" takes ";
		if(inputs.isEmpty()){
			help+="no inputs";
		}else{
			help+=inputs.size()+" input";
			if(inputs.size() > 1){
				help+="s";
			}
		}
		help +=" and ";
		if(outputs.isEmpty()){
			help+="no outputs";
		}else{
			help+=inputs.size()+" output";
			if(outputs.size() > 1){
				help+="s";
			}
		}
		
		help+=".</p>";

		String tableCellStyle=" style='border: 1px solid;	border-collapse: collapse;' ";
		if(!inputs.isEmpty()){
			help +="<h2>Inputs</h2>";
			for(Entry<String,DFEOutput> input : inputs.entrySet()){
				help+="<h3>"+input.getKey()+"</h3>";
				help+=input.getKey()+" is a <b>"+input.getValue().getTypeName()+"</b> dataset.";
				FieldList fl = input.getValue().getFields();
				if(fl != null && !fl.getFieldNames().isEmpty()){
					help+="<table"+tableCellStyle+">";
					help+="<tr><td"+tableCellStyle+">Field Name</td><td"+tableCellStyle+">Type</td><td"+tableCellStyle+">Description</td></tr>";
					for(String fieldName : fl.getFieldNames()){
						help+="<tr><td"+tableCellStyle+">"+fieldName+"</td><td"+tableCellStyle+">"+fl.getFieldType(fieldName)+"</td><td"+tableCellStyle+"></td></tr>";
					}
					help+="</table>";
				}
			}
		}
		
		if(!outputs.isEmpty()){
			help +="<h2>Outputs</h2>";
			for(Entry<String,DFEOutput> output : outputs.entrySet()){
				help+="<h3>"+output.getKey()+"</h3>";
				help+=output.getKey()+" is a <b>"+output.getValue().getTypeName()+"</b> dataset.";
				FieldList fl = output.getValue().getFields();
				if(fl != null && !fl.getFieldNames().isEmpty()){
					help+="<table"+tableCellStyle+">";
					help+="<tr><td"+tableCellStyle+">Field Name</td><td"+tableCellStyle+">Type</td><td"+tableCellStyle+">Description</td></tr>";
					for(String fieldName : fl.getFieldNames()){
						help+="<tr><td"+tableCellStyle+">"+fieldName+"</td><td"+tableCellStyle+">"+fl.getFieldType(fieldName)+"</td><td"+tableCellStyle+"></td></tr>";
					}
					help+="</table>";
				}
			}
		}
		
		return help;
	}
}
