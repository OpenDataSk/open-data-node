/* Copyright (C) 2011 Peter Hanecak <hanecak@opendata.sk>
 *
 * This file is part of Open Data Node.
 *
 * Open Data Node is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Open Data Node is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open Data Node.  If not, see <http://www.gnu.org/licenses/>.
 */

package sk.opendata.odn;

import java.io.IOException;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.sesame.SesameBackend;
import sk.opendata.odn.repository.solr.SolrBackend;
import sk.opendata.odn.ui.AdminHomePage;
import sk.opendata.odn.ui.HomePage;

/**
 * Open Data Mode "management console" ... sort of.
 * 
 * @see wicket.myproject.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{    
	
	private static Logger logger = LoggerFactory.getLogger(WicketApplication.class);
	
	private SesameBackend sesameBackend = null;
	private SolrBackend solrBackend = null;
	private Scheduler scheduler = null;
    
	/**
     * Constructor
     */
	public WicketApplication() {
		try {
			// initialize friendly URLs for certain pages
			mountBookmarkablePage("admin", AdminHomePage.class);
			
			// initialize repositories
			sesameBackend = SesameBackend.getInstance();
			solrBackend = SolrBackend.getInstance();
			
			// initialize job scheduler
			logger.debug("initializing job scheduler ...");
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			logger.error("scheduler exception", e);
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
			solrBackend.shutDown();
		} catch (SchedulerException e) {
			logger.error("scheduler exception", e);
			// TODO is it a "good practice" to pass that also up to Wicket?
		} catch (OdnRepositoryException e) {
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
