package idm.auth;

import java.rmi.registry.Registry;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

public class SessionListener implements HttpSessionListener{

	private static Logger logger = Logger.getLogger(SessionListener.class);

	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent arg0) {
		logger.info("before session destroy");
		HttpSession session = arg0.getSession();
		ServletContext sc = session.getServletContext();
		Map<String, HttpSession> sessionLoginMap = (Map<String, HttpSession>) sc.getAttribute("sessionLoginMap");

		try{
			ServerProcess th = (ServerProcess) session.getAttribute("serverThread");
			if (th != null){
				logger.info("kill serverThread");
				th.kill(session);
			}
		}catch(Exception e){
			logger.warn("Fail to kill server process thread");
		}

		//Disconnect ssh connection
		try{
			FacesContext context = FacesContext.getCurrentInstance();
			UserInfoBean uib = (UserInfoBean) context.getApplication()
					.evaluateExpressionGet(context, "#{userInfoBean}",
							UserInfoBean.class);
			uib.sshDisconnect();
		}catch(Exception e){
			logger.warn("Fail to disconnect from the ssh session");
		}


		String userName = (String) session.getAttribute("username");
		try {
			Registry registry = (Registry) sc.getAttribute("registry");
			for (String name : registry.list()){
				if (name.startsWith(userName+"@")){
					registry.unbind(name);
				}
			}
		}catch (Exception e) {
			logger.warn("Fail to remove object from registry");
		}

		if(sessionLoginMap != null){
			sessionLoginMap.remove(userName);
		}else{
			logger.warn("No "+userName+" to remove from session login map.");
		}

	}

}