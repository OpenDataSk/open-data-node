package sk.opendata.odn;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenDataNode {
	
	private static Logger logger = LoggerFactory.getLogger(OpenDataNode.class);
	private static boolean keepRunning = true;

	public static void main(String[] args) {
		Scheduler scheduler = null;
		
		// add shutdown hook so that we can properly de-initialize when we get
		// Ctrl+C or something like that
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				logger.info("shutdown hook ...");
				keepRunning = false;
			}
		}));
		
		try {
			// initialize job scheduler
			logger.info("initializing scheduler ...");
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
			
			// keep running, let the scheduled jobs do their stuff
			logger.info("running ...");
			while (keepRunning)
				Thread.sleep(0);
		} catch (SchedulerException e) {
			logger.error("scheduler exception", e);
		} catch (InterruptedException e) {
			logger.error("interrupted exception", e);
		}
		
		try {
			// de-initialize
			// FIXME: this portion of the code seems to never run
			logger.info("shuting down ...");
			scheduler.shutdown();
		} catch (SchedulerException e) {
			logger.error("scheduler exception", e);
		}
	}
}