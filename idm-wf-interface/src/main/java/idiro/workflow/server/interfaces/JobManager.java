package idiro.workflow.server.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;
/**
 * Manager for jobs to be launched 
 * @author keith
 *
 */
public interface JobManager extends Remote{
	/**
	 * Kill a job
	 * @param jobId
	 * @throws RemoteException
	 * @throws Exception
	 */
	public void kill(String jobId) throws RemoteException, Exception;
	/**
	 * Resume a job that is suspended
	 * @param jobId
	 * @throws RemoteException
	 * @throws Exception
	 */
	public void resume(String jobId) throws RemoteException, Exception;
	/**
	 * Run a job using specified properties
	 * @param conf
	 * @return Job ID
	 * @throws RemoteException
	 * @throws Exception
	 */
	public String run(Properties conf) throws RemoteException, Exception;
	/**
	 * Submit a job with specific properties
	 * @param conf
	 * @return Job ID
	 * @throws RemoteException
	 * @throws Exception
	 */
	public String submit(Properties conf) throws RemoteException, Exception;
	/**
	 * Suspend a job
	 * @param jobId
	 * @throws RemoteException
	 * @throws Exception
	 */
	public void suspend(String jobId) throws RemoteException, Exception;
	/**
	 * Run a job with a specified DataFlow
	 * @param w
	 * @return job ID
	 * @throws RemoteException
	 * @throws Exception
	 */
	public String run(DataFlow w)throws RemoteException, Exception;
	/**
	 * Run a job with specified DataFlow and List of DataFlowElements
	 * @param df
	 * @param list
	 * @return Job ID
	 * @throws RemoteException
	 * @throws Exception
	 */
	public String run(DataFlow df, List<DataFlowElement> list) throws RemoteException, Exception;
	/**
	 * Get the URL for the Console
	 * @param df
	 * @return URL in String format
	 * @throws RemoteException
	 * @throws Exception
	 */
	public String getConsoleUrl(DataFlow df) throws RemoteException, Exception;
	/**
	 * Get the Console URL specific for a job
	 * @param df
	 * @param e
	 * @return URL String formatted
	 * @throws RemoteException
	 * @throws Exception
	 */
	public String getConsoleElementUrl(DataFlow df,DataFlowElement e) 
			throws RemoteException, Exception;
	/**
	 * Get a List of jobs that are in the Job Manager
	 * @return List of Arrays oj Job IDs
	 * @throws RemoteException
	 * @throws Exception
	 */
	public List<String[]> getJobs() throws RemoteException, Exception;
	/**
	 * Get the URL for the Job Manager
	 * @return URL in String format
	 * @throws RemoteException
	 */
	public String getUrl() throws RemoteException;
	/**
	 * Get the status of an Element from the JobManager
	 * @param df
	 * @param e
	 * @return Status String (SUSPENDED , RUNNING , TERMINATED)
	 * @throws RemoteException
	 * @throws Exception
	 */
	public String getElementStatus(DataFlow df, DataFlowElement e) throws RemoteException, Exception;

}
