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

		ServerProcess th = (ServerProcess) session.getAttribute("serverThread");
		if (th != null){
			logger.info("kill serverThread");
			th.kill(session);
		}
		
		//Disconnect ssh connection
		FacesContext context = FacesContext.getCurrentInstance();
		UserInfoBean uib = (UserInfoBean) context.getApplication()
				.evaluateExpressionGet(context, "#{userInfoBean}",
						UserInfoBean.class);
		uib.sshDisconnect();
		
		String userName = (String) session.getAttribute("username");

		try {
			Registry registry = (Registry) sc.getAttribute("registry");
			for (String name : registry.list()){
				if (name.startsWith(userName+"@")){
					registry.unbind(name);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		if(sessionLoginMap != null){
			sessionLoginMap.remove(userName);
		}
		
//		session.removeAttribute("serverThread");
	}

}