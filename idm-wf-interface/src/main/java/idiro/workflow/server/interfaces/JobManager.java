package idiro.workflow.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;

public interface JobManager extends Remote{

	public void kill(String jobId) throws RemoteException, Exception;

	public void resume(String jobId) throws RemoteException, Exception;

	public String run(Properties conf) throws RemoteException, Exception;

	public String submit(Properties conf) throws RemoteException, Exception;

	public void suspend(String jobId) throws RemoteException, Exception;
	
	public String run(DataFlow w)throws RemoteException, Exception;

	public String run(DataFlow df, List<DataFlowElement> list) throws RemoteException, Exception;
	
	public String getConsoleUrl(DataFlow df) throws RemoteException, Exception;
	
	public String getConsoleElementUrl(DataFlow df,DataFlowElement e) 
			throws RemoteException, Exception;
	
	public List<String[]> getJobs() throws RemoteException, Exception;
	
	public String getUrl() throws RemoteException;

}
