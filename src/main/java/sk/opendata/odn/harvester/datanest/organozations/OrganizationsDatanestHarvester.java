package sk.opendata.odn.harvester.datanest.organozations;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.OpenDataNode;

public class OrganizationsDatanestHarvester implements Job {

	private static Logger logger = LoggerFactory.getLogger(OpenDataNode.class);

	public void execute(JobExecutionContext context) throws JobExecutionException {
		// TODO: implement the fetching of source data, "enhancer" and storage into the Sesame
		JobKey jobKey = context.getJobDetail().getKey();
		logger.info("SimpleJob says: " + jobKey + " executing at " + new Date());
	}

}