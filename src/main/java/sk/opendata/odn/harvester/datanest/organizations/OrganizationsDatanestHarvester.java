package sk.opendata.odn.harvester.datanest.organizations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.model.OrganizationRecord;
import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.sesame.SesameBackend;
import sk.opendata.odn.serialization.rdf.OrganizationRdfSerializer;
import au.com.bytecode.opencsv.CSVReader;

/**
 * This class contains stuff related to scraper of Register Organizacii
 * published by Aliancia Fair-Play at http://datanest.fair-play.sk/datasets/1 .
 */
public class OrganizationsDatanestHarvester implements Job {

	public final static String DATANEST_PROPERTIES_NAME = "/datanest.properties";
	public final static String KEY_DATANEST_ORGANIZATIONS_URL = "datanest.organizations.url";
	public final static String KEY_DATANEST_ORGANIZATIONS_SEZAME_REPO_NAME = "datanest.organizations.sesame_repo_name";
	public final static String KEY_DEBUG_PROCESS_ONLY_N_ITEMS = "datanest.debug.process_only_n_items";
	
	private final static int ATTR_INDEX_ID = 0;
	private final static int ATTR_INDEX_NAME = 1;
	private final static int ATTR_INDEX_SEAT = 3;
	private final static int ATTR_INDEX_ICO = 2;
	private final static int ATTR_INDEX_DATE_FROM = 6;
	private final static int ATTR_INDEX_DATE_TO = 7;
	private final static int ATTR_INDEX_SOURCE = 13;

	private final static String DATANEST_DATE_FORMAT = "yyyy-MM-dd";
	
	private static Logger logger = LoggerFactory.getLogger(OrganizationsDatanestHarvester.class);
	private final static SimpleDateFormat sdf = new SimpleDateFormat(DATANEST_DATE_FORMAT);
	private Properties datanestProperties = null;
	private Vector<OrganizationRecord> records = null;
	private OrganizationRdfSerializer serializer = null;

	
	public OrganizationsDatanestHarvester() throws IOException,
			RepositoryConfigException, RepositoryException,
			ParserConfigurationException, TransformerConfigurationException {
		
		datanestProperties = new Properties();
		datanestProperties.load(getClass().getResourceAsStream(DATANEST_PROPERTIES_NAME));
		
		serializer = new OrganizationRdfSerializer(SesameBackend.getInstance(),
				datanestProperties.getProperty(
						KEY_DATANEST_ORGANIZATIONS_SEZAME_REPO_NAME));
	}
	
	private OrganizationRecord scrapOneRecord(String[] row) throws ParseException {
		OrganizationRecord record = new OrganizationRecord();
		
		record.setSource(row[ATTR_INDEX_SOURCE]);
		record.setName(StringEscapeUtils.escapeXml(row[ATTR_INDEX_NAME]));
		record.setSeat(row[ATTR_INDEX_SEAT]);
		record.setIco(row[ATTR_INDEX_ICO]);
		
		Date dateFrom = sdf.parse(row[ATTR_INDEX_DATE_FROM]);
		record.setDateFrom(dateFrom);
		
		if (!row[ATTR_INDEX_DATE_TO].isEmpty()) {
			Date dateTo = sdf.parse(row[ATTR_INDEX_DATE_TO]);
			record.setDateTo(dateTo);
		}
		
		logger.debug("scrapped record of: " + record.getName());
		
		return record;
	}
	
	private void update() throws IOException, ParseException,
			RepositoryConfigException, RepositoryException,
			TransformerException, IllegalArgumentException, OdnRepositoryException {
		
		URL csvUrl = new URL(datanestProperties.getProperty(KEY_DATANEST_ORGANIZATIONS_URL));
		logger.debug("going to load data from " + csvUrl.toExternalForm());
		
		// "open" the CSV dump
		CSVReader csvReader = new CSVReader(
				new BufferedReader(
						new InputStreamReader(
								csvUrl.openStream())));
	    
		records = new Vector<OrganizationRecord>();
		
		// TODO: check the header - for now we simply skip it
		csvReader.readNext();
		
		// read the rows
		String[] row;
		int debugProcessOnlyNItems = Integer.valueOf(datanestProperties.getProperty(KEY_DEBUG_PROCESS_ONLY_N_ITEMS));
	    while ((row = csvReader.readNext()) != null) {
	        records.add(scrapOneRecord(row));
	        
	        if (debugProcessOnlyNItems > 0 &&
	        		records.size() > debugProcessOnlyNItems)
	        	break;
	    }
	    
	    // store the results
	    serializer.store(records);
	}
	
	/**
	 * Method invoked by QUARTZ scheduler to launch this job.
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// TODO: implement the fetching of source data, "enhancer" and storage into the Sesame
		JobKey jobKey = context.getJobDetail().getKey();
		logger.info("scheduled job says: " + jobKey + " executing at " + new Date());
		
		// TODO: contemplate catching simply 'Exception' thus reducing the
		// amount of repetitive 'catch' statements
		try {
			update();
		} catch (IOException e) {
			logger.error("IO exception", e);
		} catch (ParseException e) {
			logger.error("parse exception", e);
		} catch (RepositoryConfigException e) {
			logger.error("repository config exception", e);
		} catch (RepositoryException e) {
			logger.error("repository exception", e);
		} catch (TransformerException e) {
			logger.error("XML transformation exception", e);
		} catch (IllegalArgumentException e) {
			logger.error("illegal argument exception", e);
		} catch (OdnRepositoryException e) {
			logger.error("repository exception", e);
		}
	}

}