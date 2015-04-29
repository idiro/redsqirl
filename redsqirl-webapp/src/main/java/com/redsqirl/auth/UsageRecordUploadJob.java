package com.redsqirl.auth;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.Base64;

public class UsageRecordUploadJob implements Job {

	private static Logger logger = Logger.getLogger(UsageRecordUploadJob.class);
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		logger.info("execute job");
		
		String error = null;
		
		UsageRecordWriter usageRecordLog = new UsageRecordWriter();
		
		try{
		
			String file = convertFileToString(new File(usageRecordLog.getZipPath()));
			String fileName = usageRecordLog.getZipPath().substring(usageRecordLog.getZipPath().lastIndexOf("/"));
			
			String uri = getRepoServer()+"rest/uploadusagerecord";

			JSONObject object = new JSONObject();
			object.put("file", file);
			object.put("fileName", fileName);

			Client client = Client.create();
			WebResource webResource = client.resource(uri);

			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, object.toString());
			String ansServer = response.getEntity(String.class);

			logger.info(ansServer);

			try{
				JSONObject pckObj = new JSONObject(ansServer);
				error = pckObj.getString("error");
			} catch (JSONException e){
				e.printStackTrace();
			}
			
			if (error != null){
				logger.error(error);
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public String getRepoServer(){
		String pckServer = WorkflowPrefManager.getPckManagerUri();
		if(!pckServer.endsWith("/")){
			pckServer+="/";
		}
		return pckServer;
	}
	
	//Convert my file to a Base64 String
    private String convertFileToString(File file) throws IOException{
        byte[] bytes = FileUtils.readFileToByteArray(file);
        return new String(Base64.encode(bytes));
    }

}