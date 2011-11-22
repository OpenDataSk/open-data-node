package sk.opendata.odn;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Open Data Mode "management console" ... sort of.
 * 
 * @see wicket.myproject.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{    
	
	private static Logger logger = LoggerFactory.getLogger(WicketApplication.class);
	
	private Scheduler scheduler = null;
    
	/**
     * Constructor
     */
	public WicketApplication() {
		// initialize job scheduler
		logger.debug("initializing job scheduler ...");
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			logger.error("scheduler exception", e);
			// TODO is it a "good practice" to pass that also up to Wicket?
		}
	}
	
	/**
	 * Destructor
	 */
	@Override
	protected void onDestroy() {
		// shut down job scheduler
		logger.debug("shuting down job scheduler ...");
		try {
			scheduler.shutdown();
		} catch (SchedulerException e) {
			logger.error("scheduler exception", e);
			// TODO is it a "good practice" to pass that also up to Wicket?
		}
	}

	/**
	 * @see wicket.Application#getHomePage()
	 */
	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}

}
