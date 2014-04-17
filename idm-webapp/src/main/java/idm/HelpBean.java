package idm;

import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.interfaces.DataFlow;

import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

public class HelpBean extends BaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(HelpBean.class);
	private List<String[]> helpItens;

	public void calcHelpItens(){

		logger.info("calcHelpItens");

		try {

			if (getworkFlowInterface().getWorkflow("canvas-1") == null) {
				getworkFlowInterface().addWorkflow("canvas-1");
			}

			String relPath = ((HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest())
					.getRequestURI();

			logger.info(relPath);
			File f = null;
			Map<String, List<String[]>> newList = null;
			logger.info(relPath);
			DataFlow wf = getworkFlowInterface().getWorkflow("canvas-1");

			List<Integer> pos = new ArrayList<Integer>();
			for (int i = 0; i < relPath.length(); i++) {

				if (relPath.charAt(i) == '/') {
					pos.add(i);
				}
			}
			relPath = relPath.substring(0, pos.get(pos.size() - 1));
			logger.info(relPath);
			try {
				f = new File(
						WorkflowPrefManager
						.getSysProperty(WorkflowPrefManager.sys_tomcat_path)
						+ relPath);
				if (!f.exists()) {
					logger.info(relPath.substring(pos.get(1)));
					f = new File(
							WorkflowPrefManager
							.getSysProperty(WorkflowPrefManager.sys_tomcat_path)
							+ relPath.substring(pos.get(1)));
				}
				newList = wf.loadMenu(f);
				logger.info(f.getAbsolutePath());
			} catch (Exception e) {
				logger.info("E");
			}
			Iterator<String> it = newList.keySet().iterator();
			while (it.hasNext()) {
				List<String[]> tab = newList.get(it.next());
				for (String[] s : tab) {
					logger.info(s[0] + " , " + s[1] + " , " + s[2]);
				}
			}

			Iterator<String> keysIt = newList.keySet().iterator();

			List<String[]> helpList = new ArrayList<String[]>();

			while (keysIt.hasNext()) {
				String key = keysIt.next();
				List<String[]> menuItem = newList.get(key);
				for (String[] e : menuItem) {
					String name = WordUtils.capitalizeFully(e[0].replace("_", " "));
					helpList.add(new String[] { name, e[2],e[0] });
				}
			}
			setHelpItens(helpList);

		} catch (RemoteException e) {
			logger.error(e);
		}

	}

	public List<String[]> getHelpItens() {
		return helpItens;
	}

	public void setHelpItens(List<String[]> helpItens) {
		this.helpItens = helpItens;
	}

}