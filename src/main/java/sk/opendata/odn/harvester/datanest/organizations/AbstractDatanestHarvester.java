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

package sk.opendata.odn.harvester.datanest.organizations;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.serialization.OdnSerializationException;
import sk.opendata.odn.utils.ApplicationProperties;

/**
 * Stuff common to all Datanest harvesters.
 * 
 * @param <RecordType>
 *            type of individual record into which the harvested data are stored into
 */
public abstract class AbstractDatanestHarvester<RecordType> {

	public final static String DATANEST_PROPERTIES_NAME = "/datanest.properties";
	public final static String KEY_DEBUG_PROCESS_ONLY_N_ITEMS = "datanest.debug.process_only_n_items";

	public final static String DATANEST_DATE_FORMAT = "yyyy-MM-dd";

	private static Logger logger = LoggerFactory.getLogger(AbstractDatanestHarvester.class);
	protected final static SimpleDateFormat sdf = new SimpleDateFormat(DATANEST_DATE_FORMAT);
	
	protected ApplicationProperties datanestProperties = null;
	protected Vector<RecordType> records = null;
	
	
	public AbstractDatanestHarvester() throws IOException {
		datanestProperties = ApplicationProperties.getInstance(DATANEST_PROPERTIES_NAME);
	}
	
	abstract public RecordType scrapOneRecord(String[] row) throws ParseException;
	
	public abstract void update() throws OdnHarvesterException,
			OdnSerializationException, OdnRepositoryException;
	
	/**
	 * Method invoked by QUARTZ scheduler to launch this job.
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobKey jobKey = context.getJobDetail().getKey();
		logger.info("scheduled job says: " + jobKey + " executing at " + new Date());
		
		try {
			update();
		} catch (OdnHarvesterException e) {
			logger.error("harvester exception", e);
		} catch (OdnSerializationException e) {
			logger.error("serialization exception", e);
		} catch (OdnRepositoryException e) {
			logger.error("repository exception", e);
		}
	}
}
