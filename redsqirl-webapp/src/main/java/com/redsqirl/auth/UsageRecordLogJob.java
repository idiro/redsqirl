package com.redsqirl.auth;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.redsqirl.workflow.server.WorkflowPrefManager;

public class UsageRecordLogJob implements Job {

	private static Logger logger = Logger.getLogger(UsageRecordLogJob.class);

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		logger.info("execute job");

		try{

			UsageRecordWriter usageRecordLog = new UsageRecordWriter();

			String fileZipName = usageRecordLog.getZipPath();
			File fileName = usageRecordLog.getPreviousFile();
			zip(fileName, fileZipName);

			//fileName.delete();
			if(!"FALSE".equalsIgnoreCase(WorkflowPrefManager.getSysProperty(WorkflowPrefManager.core_settings_data_usage))){
				executeUpload();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void zip(File inputFile, String compressedFile) {
		try {
			ZipFile zipFile = new ZipFile(compressedFile);
			ZipParameters parameters = new ZipParameters();

			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);

			zipFile.addFile(inputFile, parameters);

		} catch (Exception e) {
			logger.error(e,e);
		}
	}

	public void executeUpload() {

		Random random = new Random();
		int Low = 1;
		int High = 10;
		int r = random.nextInt(High-Low) + Low;

		Calendar startTime = Calendar.getInstance();
		startTime.set(java.util.Calendar.MINUTE, r);
		startTime.set(java.util.Calendar.SECOND, r);
		startTime.set(java.util.Calendar.MILLISECOND, r);

		try{

			JobDetail job = JobBuilder.newJob(UsageRecordUploadJob.class).withIdentity("usageRecordUploadJob"+r).build();
			Trigger trigger = TriggerBuilder.newTrigger().startAt(startTime.getTime()).withSchedule(SimpleScheduleBuilder.simpleSchedule()).build();

			SchedulerFactory schFactory = new StdSchedulerFactory();
			Scheduler sch = schFactory.getScheduler();
			sch.start();
			sch.scheduleJob(job, trigger);

		} catch (SchedulerException e) {
			e.printStackTrace();
		}

	}

}