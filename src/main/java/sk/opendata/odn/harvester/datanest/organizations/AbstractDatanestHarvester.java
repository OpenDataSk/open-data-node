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
import java.util.List;
import java.util.Vector;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.model.AbstractRecord;
import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.solr.SolrItem;
import sk.opendata.odn.repository.solr.SolrRepository;
import sk.opendata.odn.serialization.AbstractSerializer;
import sk.opendata.odn.serialization.OdnSerializationException;
import sk.opendata.odn.utils.ApplicationProperties;

/**
 * Stuff common to all Datanest harvesters.
 * 
 * Note in regards to "primary repository": After we harvest the data, we store it into multiple
 * repositories to serve for multiple purposes. In current architecture, it means Jackrabbit
 * as primary document store (with full data: original record, harvested and enhanced record, ...)
 * and secondary stores SOLR (for full-text search) and Sesame (for RDF and SPARQL).
 * 
 * @param <RecordType>
 *            type of individual record into which the harvested data are stored into
 */
public abstract class AbstractDatanestHarvester<RecordType extends AbstractRecord> {

	public final static String DATANEST_PROPERTIES_NAME = "/datanest.properties";
	public final static String KEY_DEBUG_PROCESS_ONLY_N_ITEMS = "datanest.debug.process_only_n_items";

	public final static String DATANEST_DATE_FORMAT = "yyyy-MM-dd";

	private static Logger logger = LoggerFactory.getLogger(AbstractDatanestHarvester.class);
	protected final static SimpleDateFormat sdf = new SimpleDateFormat(DATANEST_DATE_FORMAT);
	
	protected ApplicationProperties datanestProperties = null;
	protected Vector<AbstractSerializer<RecordType, ?, ?>> serializers = null;
	
	private SolrRepository primaryRepository = null;	// TODO: for now just a hack as SolrRepository is use instead of Jackrabbit
	
	
	/**
	 * @param primaryRepository repository where we store primary copy of our harvested data
	 * @throws IOException
	 */
	public AbstractDatanestHarvester(SolrRepository primaryRepository)
			throws IOException {
		
		datanestProperties = ApplicationProperties.getInstance(DATANEST_PROPERTIES_NAME);
		
		serializers = new Vector<AbstractSerializer<RecordType, ?, ?>>();
		
		this.primaryRepository = primaryRepository;
	}
	
	abstract public RecordType scrapOneRecord(String[] row) throws ParseException;
	
	/**
	 * @param record
	 *            newly downloaded record
	 * @return {@code true} if given record is different than our current copy
	 *         of the record with same ID
	 *
	 * @throws IllegalArgumentException
	 *             when some of the given arguments is not valid
	 * @throws OdnRepositoryException
	 *             when retrieve operation fails
	 * @throws OdnSerializationException
	 *             when conversion into SOLR beans fails
	 */
	protected boolean updatedSinceLastHarvest(RecordType record) throws IllegalArgumentException, OdnRepositoryException, OdnSerializationException {
		SolrItem ourCurrentCopyOfRecord = primaryRepository.retrieve(record.getId());
		if (ourCurrentCopyOfRecord == null)
			// no record with such ID in DB => yup, update
			return true;
		
		SolrItem freshDownloadOfRecord = SolrItem.createSolrItem(record);
		
		if (ourCurrentCopyOfRecord.compareTo(freshDownloadOfRecord) == 0)
			return false;

		return true;
	}
	
	public abstract void update() throws OdnHarvesterException,
			OdnSerializationException, OdnRepositoryException;
	
	/**
	 * Loop through all serializer and pass given records to them. Serializers
	 * will serialize the records and store them.
	 * 
	 * @param records
	 *            list of records to serialize and store
	 * 
	 * @throws IllegalArgumentException
	 *             if repository with given name does not exists
	 * @throws OdnSerializationException
	 *             when serialization fails
	 * @throws OdnRepositoryException
	 *             when we fail to store given data into repository
	 */
	protected void store(List<RecordType> records) throws IllegalArgumentException,
			OdnSerializationException, OdnRepositoryException {
	    
		for (AbstractSerializer<RecordType, ?, ?> serializer : serializers)
	    	serializer.store(records);
	}
	
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
