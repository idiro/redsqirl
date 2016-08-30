package com.redsqirl.dynamictable;

import java.io.Serializable;
import java.util.List;

public class Scheduling implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7291616326471561973L;
	
	private String nameScheduling;
	private String jobId;
	private String lastActionScheduling;
	private String nextActionScheduling;
	private String actionsScheduling;
	private String okScheduling;
	private String skippedScheduling;
	private String errorsScheduling;
	private String runningScheduling;
	private List<String[]> listJobsScheduling;
	
	public Scheduling() {
		super();
	}
	
	public String getLastActionScheduling() {
		return lastActionScheduling;
	}
	public void setLastActionScheduling(String lastActionScheduling) {
		this.lastActionScheduling = lastActionScheduling;
	}
	public String getNextActionScheduling() {
		return nextActionScheduling;
	}
	public void setNextActionScheduling(String nextActionScheduling) {
		this.nextActionScheduling = nextActionScheduling;
	}
	public String getActionsScheduling() {
		return actionsScheduling;
	}
	public void setActionsScheduling(String actionsScheduling) {
		this.actionsScheduling = actionsScheduling;
	}
	public String getOkScheduling() {
		return okScheduling;
	}
	public void setOkScheduling(String okScheduling) {
		this.okScheduling = okScheduling;
	}
	public String getSkippedScheduling() {
		return skippedScheduling;
	}
	public void setSkippedScheduling(String skippedScheduling) {
		this.skippedScheduling = skippedScheduling;
	}
	public String getErrorsScheduling() {
		return errorsScheduling;
	}
	public void setErrorsScheduling(String errorsScheduling) {
		this.errorsScheduling = errorsScheduling;
	}
	public String getRunningScheduling() {
		return runningScheduling;
	}
	public void setRunningScheduling(String runningScheduling) {
		this.runningScheduling = runningScheduling;
	}
	public List<String[]> getListJobsScheduling() {
		return listJobsScheduling;
	}
	public void setListJobsScheduling(List<String[]> listJobsScheduling) {
		this.listJobsScheduling = listJobsScheduling;
	}
	public String getNameScheduling() {
		return nameScheduling;
	}
	public void setNameScheduling(String nameScheduling) {
		this.nameScheduling = nameScheduling;
	}

	/**
	 * @return the jobId
	 */
	public String getJobId() {
		return jobId;
	}

	/**
	 * @param jobId the jobId to set
	 */
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	
}