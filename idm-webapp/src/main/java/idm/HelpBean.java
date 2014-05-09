package idm;

import idiro.workflow.server.interfaces.DataFlow;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

public class HelpBean extends BaseBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7223013564153599958L;
	
	
	private static Logger logger = Logger.getLogger(HelpBean.class);
	private static List<String[]> helpHtml = null;

	public void calcHelpItens(){

		logger.info("calcHelpItens");

		try {

			if (getworkFlowInterface().getWorkflow("canvas-1") == null) {
				getworkFlowInterface().addWorkflow("canvas-1");
			}

			DataFlow wf = getworkFlowInterface().getWorkflow("canvas-1");
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

		} catch (RemoteException e) {
			logger.error(e);
		}

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

}