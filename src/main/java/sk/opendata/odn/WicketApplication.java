package sk.opendata.odn;

import java.io.IOException;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.repository.sesame.SesameBackend;

/**
 * Open Data Mode "management console" ... sort of.
 * 
 * @see wicket.myproject.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{    
	
	private static Logger logger = LoggerFactory.getLogger(WicketApplication.class);
	
	private SesameBackend sesameBackend = null;
	private Scheduler scheduler = null;
    
	/**
     * Constructor
     */
	public WicketApplication() {
		try {
			// initialize repository
			sesameBackend = SesameBackend.getInstance();
			
			// initialize job scheduler
			logger.debug("initializing job scheduler ...");
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
			
			// TODO: we need to somehow pass the 'sesameBackend' to the scheduled jobs
		} catch (SchedulerException e) {
			logger.error("scheduler exception", e);
			// TODO is it a "good practice" to pass that also up to Wicket?
		} catch (RepositoryConfigException e) {
			logger.error("repository config exception", e);
			// TODO is it a "good practice" to pass that also up to Wicket?
		} catch (RepositoryException e) {
			logger.error("repository exception", e);
			// TODO is it a "good practice" to pass that also up to Wicket?
		} catch (IOException e) {
			logger.error("IO exception", e);
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
			sesameBackend.shutDown();
		} catch (SchedulerException e) {
			logger.error("scheduler exception", e);
			// TODO is it a "good practice" to pass that also up to Wicket?
		} catch (RepositoryException e) {
			logger.error("repository exception", e);
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
