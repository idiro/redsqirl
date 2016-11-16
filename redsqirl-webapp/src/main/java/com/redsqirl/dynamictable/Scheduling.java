/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl.dynamictable;

import java.io.Serializable;
import java.util.List;

public class Scheduling implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7291616326471561973L;
	
	private String nameScheduling = "DOES NOT EXIST";
	private String jobId = "DOES NOT EXIST";
	private String lastActionScheduling = "DOES NOT EXIST";
	private String nextActionScheduling = "DOES NOT EXIST";
	private String actionsScheduling = "DOES NOT EXIST";
	private String okScheduling = "0";
	private String skippedScheduling = "0";
	private String errorsScheduling = "0";
	private String runningScheduling = "0";
	private List<String[]> listJobsScheduling;
	private String statusScheduling = "DOES NOT EXIST";
	private boolean selected;
	
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
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getStatusScheduling() {
		return statusScheduling;
	}
	public void setStatusScheduling(String statusScheduling) {
		this.statusScheduling = statusScheduling;
	}

	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
}