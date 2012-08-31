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

package sk.opendata.odn.harvester.datanest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.harvester.OdnHarvesterException;
import sk.opendata.odn.model.AbstractRecord;
import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.jackrabbit.JackrabbitItem;
import sk.opendata.odn.repository.jackrabbit.JackrabbitRepository;
import sk.opendata.odn.serialization.AbstractSerializer;
import sk.opendata.odn.serialization.OdnSerializationException;
import sk.opendata.odn.utils.ApplicationProperties;
import au.com.bytecode.opencsv.CSVReader;

/**
 * Stuff common to all Datanest harvesters.
 * 
 * What is a common harvesting work-flow:
 * 1) download the original document(s) from the main source of data
 * 2) store the copy of original(s) in primary repository
 *    why: to have audit trail, our own copy, possibly to run next steps
 *    from this own copy instead of downloading (possibly unchanged)
 *    document repeatedly
 * 3) extract data
 * 4) enhance data: clean-up, correction, correlation, possibly production
 *    of new data via math/logic, ...
 * 5) serialize data into format(s) suitable for ODN back-end repository(ies)
 * 6) store the data into ODN back-end repository(ies)
 * 
 * Note in regards to "primary repository": After we harvest the data, we store
 * it into multiple repositories to serve for multiple purposes. In current
 * architecture, it means Jackrabbit as primary document store (with full data:
 * original record, harvested and enhanced record, ...) and secondary stores
 * SOLR (for full-text search) and Sesame (for RDF and SPARQL).
 * 
 * TODO: Enhance the documentation and separate stuff common to all Datansets
 * harvesters from stuff common to all harvesters.
 * 
 * @param <RecordType>
 *            type of individual record into which the harvested data are stored
 *            into
 */
public abstract class AbstractDatanestHarvester<RecordType extends AbstractRecord> {

	public final static String DATANEST_PROPERTIES_NAME = "/datanest.properties";
	public final static String KEY_DATANEST_BATCH_SIZE = "datanest.batch_size";
	public final static String KEY_DEBUG_PROCESS_ONLY_N_ITEMS = "datanest.debug.process_only_n_items";

	public final static String DATANEST_DATE_FORMAT = "yyyy-MM-dd";

	private static Logger logger = LoggerFactory.getLogger(AbstractDatanestHarvester.class);
	protected final static SimpleDateFormat sdf = new SimpleDateFormat(DATANEST_DATE_FORMAT);
	
	protected ApplicationProperties datanestProperties = null;
	protected Vector<AbstractSerializer<RecordType, ?, ?>> serializers = null;
	
	private JackrabbitRepository primaryRepository = null;
	
	
	/**
	 * @throws IOException
	 */
	public AbstractDatanestHarvester()
			throws IOException {
		
		datanestProperties = ApplicationProperties.getInstance(DATANEST_PROPERTIES_NAME);
		
		serializers = new Vector<AbstractSerializer<RecordType, ?, ?>>();
		
		this.primaryRepository = JackrabbitRepository.getInstance();
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
	protected UpdatedSinceLastHarvestResults updatedSinceLastHarvest(
			RecordType record) throws IllegalArgumentException,
			OdnRepositoryException, OdnSerializationException {
		
		JackrabbitItem ourCurrentCopyOfRecord = primaryRepository.retrieve(record
				.getId());
		if (ourCurrentCopyOfRecord == null)
			return UpdatedSinceLastHarvestResults.NEW_RECORD;

		JackrabbitItem freshDownloadOfRecord = JackrabbitItem
				.createJackrabbitItem(record);

		if (ourCurrentCopyOfRecord.compareTo(freshDownloadOfRecord) == 0)
			return UpdatedSinceLastHarvestResults.RECORD_UNCHANGED;

		return UpdatedSinceLastHarvestResults.RECORD_UPDATED;
	}
	
	/**
	 * Update our data using data harvested from source.
	 * 
	 * @throws OdnHarvesterException
	 *             when some harvesting error occurs
	 * @throws OdnSerializationException
	 *             when some serialization error occurs
	 * @throws OdnRepositoryException
	 *             when some repository error occurs
	 */
	public abstract void update() throws OdnHarvesterException,
			OdnSerializationException, OdnRepositoryException;

	/**
	 * Most common implementation of harvesting code in our current harvesters.
	 * 
	 * @param urlKey
	 *            property key used to retrieve source URL
	 * 
	 * @throws OdnHarvesterException
	 *             when some harvesting error occurs
	 * @throws OdnSerializationException
	 *             when some serialization error occurs
	 * @throws OdnRepositoryException
	 *             when some repository error occurs
	 */
	protected void genericUpdate(String urlKey) throws OdnHarvesterException,
			OdnSerializationException, OdnRepositoryException {

		logger.info("harvesting started (" + urlKey + ")");

		// sort of performance counters
		long timeStart = Calendar.getInstance().getTimeInMillis();
		long timeCurrent = -1;
		long recordCounter = 0;
		long unchangedRecordCounter = 0;
		
		OdnHarvesterException odnHarvesterException = null;

		try {
			URL csvUrl = new URL(
					datanestProperties.getProperty(urlKey));
			logger.debug("going to load data from " + csvUrl.toExternalForm());
			
			// TODO: insert following:
			// a) just download the data to local temporary file
			// b) store that content using 'storeOriginalData()'
			// c) pass that temporary file to CSVReader
			// Later on we can enhance the /admin page of ODN with some
			// harvesting options like "use latest raw data (from Jackrabbit"
			// (to avoid downloading the data repeatedly - usefull from when
			// the data did not chage at the source but we've updated harvesting code)
			
			URLConnection csvConnection = csvUrl.openConnection();
			csvConnection.setRequestProperty("User-Agent",
							"Open Data Node (http://opendata.sk/liferay/open-data-node)");

			// "open" the CSV dump
			CSVReader csvReader = new CSVReader(new BufferedReader(
					new InputStreamReader(csvConnection.getInputStream())));
			// TODO: If we store also the original copy of the data (say in
			// Jacrabbit) and perform a "diff" on that and previous version we can:
			// a) determine also removed records (which current implementation
			//    does not know to do)
			// b) determine new and updated records without downloading records
			//    for all IDs ...
			// c) ... instead noting only changed records in say vectors and
			//    processing only those (thus saving a LOT of resources assuming
			//    changes and additions are small and infrequent)

			Vector<RecordType> records = new Vector<RecordType>();

			// TODO: check the header - for now we simply skip it
			csvReader.readNext();

			// read the rows
			String[] row;
			int batchSize = Integer.valueOf(datanestProperties.getProperty(KEY_DATANEST_BATCH_SIZE));
			int debugProcessOnlyNItems = Integer.valueOf(datanestProperties
					.getProperty(KEY_DEBUG_PROCESS_ONLY_N_ITEMS));
			while ((row = csvReader.readNext()) != null) {
				try {
					RecordType record = scrapOneRecord(row);
					recordCounter++;

					// determine whether it changed since last harvesting ...
					UpdatedSinceLastHarvestResults updated = updatedSinceLastHarvest(record);
					if (updated == UpdatedSinceLastHarvestResults.RECORD_UNCHANGED) {
						// it did not => nothing to do, just maintain the count
						unchangedRecordCounter++;
					}
					else {
						// clean-up data related to old record, if necessary
						if (updated == UpdatedSinceLastHarvestResults.RECORD_UPDATED)
							throw new OdnHarvesterException(
									"clean-up of old records not implemented yet (record ID: "
											+ record.getId() + ")");

						// add new data
						records.add(record);
					}
				} catch (ParseException e) {
					logger.warn("parse exception", e);
					logger.warn("skipping following record: "
							+ Arrays.deepToString(row));
				}
		    	
		    	if (records.size() >= batchSize) {
		    		store(records);
		    		
		    		// report current harvesting status
					timeCurrent = Calendar.getInstance().getTimeInMillis();
					float harvestingSpeed = 1000f * (float) recordCounter
							/ (float) (timeCurrent - timeStart);
					logger.info("harvested " + recordCounter + " records ("
							+ harvestingSpeed + "/s) so far ...");
		    		
		    		records.clear();
		    	}

		    	if (debugProcessOnlyNItems > 0 &&
		    			recordCounter >= debugProcessOnlyNItems)
					break;
			}

			// store the results
			store(records);
			
			csvReader.close();

		// TODO: If there wont be any more specialized error handling here
		// in the future, try catching only 'Exception' to simplify the
		// code.
		} catch (MalformedURLException e) {
			logger.error("malformed URL exception", e);
			odnHarvesterException = new OdnHarvesterException(e.getMessage(), e);
		} catch (IOException e) {
			logger.error("IO exception", e);
			odnHarvesterException = new OdnHarvesterException(e.getMessage(), e);
		}

		if (odnHarvesterException != null)
			throw odnHarvesterException;

		logger.info("harvesting finished (" + urlKey + ")");
		
		// report final harvesting status
		timeCurrent = Calendar.getInstance().getTimeInMillis();
		float harvestingSpeed = 1000f * (float) recordCounter
				/ (float) (timeCurrent - timeStart);
		logger.info("harvested " + recordCounter + " records in "
				+ (float) (timeCurrent - timeStart) / 1000f + " seconds ("
				+ harvestingSpeed + "/s, " + unchangedRecordCounter
				+ " records not changed)");
	}

	protected void storeOriginalData() {
		// TODO: get the data as downloaded and store then into dedicated node in Jackrabbit, say /raw/datanest/ppd/2012/08/22/hh/mm
	}
	
	/**
	 * Loop through all serializers and pass given records to them. Serializers
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
	    
		if (records.size() <= 0)
			// nothing to store so why bother?
			return;
		
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
