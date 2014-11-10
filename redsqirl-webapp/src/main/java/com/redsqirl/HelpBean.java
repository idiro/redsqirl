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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

import com.idiro.utils.LocalFileSystem;
import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.interfaces.DataFlow;

public class HelpBean extends BaseBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7223013564153599958L;

	private static Logger logger = Logger.getLogger(HelpBean.class);
	private List<String[]> helpHtml = null;
	private String fieldSearch;
	private List<String[]> result = new ArrayList<String[]>();
	private List<String> listHelp = new ArrayList<String>();
	private List<String[]> helpHtmlSuperAction = null;

	public void calcHelpItens(){

		logger.info("calcHelpItens");

		try {

			if (getworkFlowInterface().getWorkflow("canvas-1") == null) {
				getworkFlowInterface().addWorkflow("canvas-1");
			}

			DataFlow wf = getworkFlowInterface().getWorkflow("canvas-1");

			mountRelativeHelp(wf);
			
			mountRelativeHelpSuperAction(wf);

		} catch (RemoteException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error(e);
		}

	}
	
	public void refreshRelativeHelp() throws Exception{
		String canvas1 = "canvas-1";
		boolean toRemove = false;
		DataFlow wf = getworkFlowInterface().getWorkflow(canvas1);
		if(wf == null){
			toRemove = true;
			getworkFlowInterface().addWorkflow(canvas1);
			wf = getworkFlowInterface().getWorkflow(canvas1);
		}
		mountRelativeHelpSuperAction(wf);
		if(toRemove){
			getworkFlowInterface().removeWorkflow(canvas1);
		}
	}

	public void mountRelativeHelp(DataFlow wf) throws Exception{

		Map<String,String[]> helpRel = null;
		helpHtml = new LinkedList<String[]>();
		try {
			helpRel = wf.getRelativeHelp(getCurrentPage());
			Iterator<String> it = helpRel.keySet().iterator();
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
			});
		} catch (Exception e) {
			logger.info("E");
		}

	}

	public void mountRelativeHelpSuperAction(DataFlow wf) throws Exception{

		Map<String,String[]> helpRel = null;
		List<String[]> helpHtmlSA = new LinkedList<String[]>();
		try {
			helpRel = wf.getRelativeHelpSuperAction(getCurrentPage());
			Iterator<String> it = helpRel.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				String[] helpArray = new String[]{
						key, 
						WordUtils.capitalizeFully(key.replace("_", " ")),
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
			});
		} catch (Exception e) {
			logger.info("E");
		}
		
		setHelpHtmlSuperAction(helpHtmlSA);

	}

	public void helpSearch() throws Exception{

		logger.info("helpSearch");

		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		String user = (String) session.getAttribute("username");
		String indexResultPath = WorkflowPrefManager.getPathUserPref(user)+"/lucene/index";
		File indexDir = new File(indexResultPath);
		int hits = 100;

		SimpleSearcher searcher = new SimpleSearcher();
		if(getFieldSearch() != null && !"".equals(getFieldSearch())){
			logger.info("search " + getFieldSearch());
			List<String> list = searcher.searchIndex(indexDir, getFieldSearch().trim(), hits);
			mountListResult(list);
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
	public final List<String[]> getHelpHtml() {
		return helpHtml;
	}

	public final int getHelpSize(){
		return helpHtml.size();
	}
	
	public final int getHelpSuperActionSize(){
		return helpHtmlSuperAction.size();
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

	public List<String[]> getHelpHtmlSuperAction() {
		return helpHtmlSuperAction;
	}

	public void setHelpHtmlSuperAction(List<String[]> helpHtmlSuperAction) {
		this.helpHtmlSuperAction = helpHtmlSuperAction;
	}

}