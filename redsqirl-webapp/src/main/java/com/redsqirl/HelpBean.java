package com.redsqirl;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

import com.idiro.utils.LocalFileSystem;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.server.interfaces.ElementManager;

public class HelpBean extends BaseBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7223013564153599958L;
	private static final String workflowNameTmp = "calcHelpItens"; 
	private static Logger logger = Logger.getLogger(HelpBean.class);
	private Map<String,Map<String,String[]>> helpHtml = null;
	private String fieldSearch;
	private List<String[]> result = new ArrayList<String[]>();
	private List<String> listHelp = new ArrayList<String>();
	protected ElementManager em = null;
	private Map<String, List<String[]>> helpAns;
	private Map<String, List<String[]>> helpHtmlSA;


	private ElementManager getEM() throws RemoteException{
		if(em == null){
			DataFlow wf = getworkFlowInterface().getWorkflow(workflowNameTmp);
			if(wf == null){
				getworkFlowInterface().addWorkflow(workflowNameTmp);
				wf = getworkFlowInterface().getWorkflow(workflowNameTmp);
			}
			em = wf.getElementManager();
			getworkFlowInterface().removeWorkflow(workflowNameTmp);
		}
		return em;
	}
	
	public void calcHelpItens(){

		logger.info("calcHelpItens");

		try {
			getEM();
			mountRelativeHelp();
			mountRelativeHelpSuperAction();
		} catch (RemoteException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error(e);
		}

	}
	
	public void refreshRelativeHelp() throws Exception{
		logger.info("refreshRelativeHelp");
		getEM();
		mountRelativeHelpSuperAction();
	}

	protected void mountRelativeHelp() throws Exception{

		Map<String,String[]> helpRel = new LinkedHashMap<String, String[]>();
		helpAns = new LinkedHashMap<String, List<String[]>>();
		
		//helpHtml = new LinkedList<String[]>();
		try {
			
			helpHtml = em.getRelativeHelp(getCurrentPage());
			for (String packageName : helpHtml.keySet()) {
				helpRel = helpHtml.get(packageName);
				List<String[]> l = new ArrayList<String[]>();
				for (String action : helpRel.keySet()) {
					l.add(helpRel.get(action));
				}
				
				Collections.sort(l, new Comparator<String[]>() {

					@Override
					public int compare(String[] o1, String[] o2) {
						return o1[0].compareTo(o2[0]);
					}
				});
				
				helpAns.put(packageName, l);
			}
			
			
			/*Iterator<String> it = helpRel.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				String[] helpArray = new String[]{
						key, 
						WordUtils.capitalizeFully(key.replace("_", " ")),
						helpRel.get(key)[0],
						helpRel.get(key)[1]};

				helpHtml.add(helpArray);
				listHelp.add(key);
			}
			Collections.sort(helpHtml, new Comparator<String[]>() {

				@Override
				public int compare(String[] o1, String[] o2) {
					return o1[0].compareTo(o2[0]);
				}
			});*/
			
		} catch (Exception e) {
			logger.error(e,e);
		}

	}

	protected void mountRelativeHelpSuperAction() throws Exception{

		Map<String, Map<String, String[]>> helpRel = null;
		helpHtmlSA = new LinkedHashMap<String, List<String[]>>();
		Map<String,String[]> helpSA = new LinkedHashMap<String, String[]>();
		
		try {
			helpRel = em.getRelativeHelpSuperAction(getCurrentPage());
			
			for (String modelName : helpRel.keySet()) {
				helpSA = helpRel.get(modelName);
				List<String[]> l = new ArrayList<String[]>();
				for (String superAction : helpSA.keySet()) {
					l.add(helpSA.get(superAction));
				}
				
				Collections.sort(l, new Comparator<String[]>() {

					@Override
					public int compare(String[] o1, String[] o2) {
						return o1[0].compareTo(o2[0]);
					}
				});
				
				helpHtmlSA.put(modelName, l);
			}
			
			/*Iterator<String> it = helpRel.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				
				String name = "";
				if(key != null && key.startsWith(">")){
					String[] superAction = key.split(">");
					name = superAction[2];
				}else{
					name = key;
				}
				
				String[] helpArray = new String[]{
						key, 
						WordUtils.capitalizeFully(name.replace("_", " ")),
						helpRel.get(key)[0],
						helpRel.get(key)[1]};

				helpHtmlSA.add(helpArray);
				listHelp.add(key);
			}
			Collections.sort(helpHtmlSA, new Comparator<String[]>() {

				@Override
				public int compare(String[] o1, String[] o2) {
					return o1[0].compareTo(o2[0]);
				}
			});*/
			
		} catch (Exception e) {
			logger.error(e,e);
		}

	}

	public void helpSearch() throws Exception{

		logger.info("helpSearch");

		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		String user = (String) session.getAttribute("username");
		String indexResultPath = WorkflowPrefManager.getPathUserPref(user)+"/lucene/index";
		File indexDir = new File(indexResultPath);
		if(indexDir.isDirectory() && indexDir.list().length > 0 ){
			
			int hits = 100;
			SimpleSearcher searcher = new SimpleSearcher();
			if(getFieldSearch() != null && !"".equals(getFieldSearch())){
				logger.info("search " + getFieldSearch());
				List<String> list = searcher.searchIndex(indexDir, getFieldSearch().trim(), hits);
				mountListResult(list);
			}else{
				setResult(null);
			}
			
		}else{
			setResult(null);
		}

	}

	public void mountListResult(List<String> list) throws Exception{
		String newName ="";
		File currentPage = getCurrentPage();
		List<String[]> newList = new ArrayList<String[]>();
		for (String fileName : list) {
			File file = new File(fileName);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				if(line.contains("<title>")){
					newName = line.replaceAll("<title>", "").replaceAll("</title>", "");
				}
			}
			br.close();
			String aux[] = fileName.split("/");
			String id = aux[aux.length-1].replaceAll("\\.html", "");
			if(listHelp != null && listHelp.contains(id)){
				id = "help_"+id;
			}
			newList.add(new String[]{LocalFileSystem.relativize(currentPage, fileName) ,newName.trim(), id });
			logger.info("result name: " + newName);
		}
		setResult(newList);
	}

	/**
	 * @return the helpHtml
	 */
	public final Map<String,Map<String,String[]>> getHelpHtml() {
		return helpHtml;
	}

	public void setHelpHtml(Map<String, Map<String, String[]>> helpHtml) {
		this.helpHtml = helpHtml;
	}
	
	public final List<String[]> getHelpHtmlList() {
		List<String[]> result = new ArrayList<String[]>();
		for (String name : helpHtml.keySet()) {
			result.add(new String[] { name , WordUtils.capitalizeFully(name.replace("_", " ")) });
		}
		
		Collections.sort(result, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				return o1[0].compareTo(o2[0]);
			}
		});
		
		return result;
	}
	
	public final List<String[]> getHelpHtmlSuperActionList() {
		List<String[]> result = new ArrayList<String[]>();
		for (String name : helpHtmlSA.keySet()) {
			result.add(new String[] { name , WordUtils.capitalizeFully(name.replace("_", " ")) });
		}
		
		Collections.sort(result, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				return o1[0].compareTo(o2[0]);
			}
		});
		
		return result;
	}

	public final int getHelpSize(){
		Map<String,String[]> helpRel = new LinkedHashMap<String, String[]>();
		int count = 0;
		for (String packageName : helpHtml.keySet()) {
			helpRel = helpHtml.get(packageName);
			List<String[]> l = new ArrayList<String[]>();
			for (String action : helpRel.keySet()) {
				count++;
			}
		}
		return count;
	}
	
	public final int getHelpSuperActionSize(){
		List<String[]> helpRel = new ArrayList<String[]>();
		int count = 0;
		for (String modelName : helpHtmlSA.keySet()) {
			helpRel = helpHtmlSA.get(modelName);
			for (String[] action : helpRel) {
				count++;
			}
		}
		return count;
	}

	public String getFieldSearch() {
		return fieldSearch;
	}

	public void setFieldSearch(String fieldSearch) {
		this.fieldSearch = fieldSearch;
	}

	public List<String[]> getResult() {
		return result;
	}

	public void setResult(List<String[]> result) {
		this.result = result;
	}

	public List<String> getListHelp() {
		return listHelp;
	}

	public void setListHelp(List<String> listHelp) {
		this.listHelp = listHelp;
	}

	public Map<String, List<String[]>> getHelpAns() {
		return helpAns;
	}

	public void setHelpAns(Map<String, List<String[]>> helpAns) {
		this.helpAns = helpAns;
	}

	public Map<String, List<String[]>> getHelpHtmlSA() {
		return helpHtmlSA;
	}

	public void setHelpHtmlSA(Map<String, List<String[]>> helpHtmlSA) {
		this.helpHtmlSA = helpHtmlSA;
	}

}