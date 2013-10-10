package idm.auth;

import java.rmi.registry.Registry;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

public class SessionListener implements HttpSessionListener{
	
	private static Logger logger = Logger.getLogger(UserInfoBean.class);
	
	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent arg0) {
		logger.info("before session destroy");
		HttpSession session = arg0.getSession();
		ServletContext sc = session.getServletContext();
		Map<String, HttpSession> sessionLoginMap = (Map<String, HttpSession>) sc.getAttribute("sessionLoginMap");

		String userName = (String) session.getAttribute("username");
		if(sessionLoginMap != null){
			sessionLoginMap.remove(userName);
		}
		
//		session.removeAttribute("wfm");
//		session.removeAttribute("hive");
//		session.removeAttribute("ssharray");
//		session.removeAttribute("oozie");
//		session.removeAttribute("hdfs");
		
		Registry registry = (Registry) sc.getAttribute("registry");
		try {
			for (String name : registry.list()){
				if (name.startsWith(userName+"@")){
					registry.unbind(name);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		ServerThread th = (ServerThread) session.getAttribute("serverThread");
		if (th != null){
			logger.info("kill serverThread");
			th.kill();
		}
//		session.removeAttribute("serverThread");
	}

}
