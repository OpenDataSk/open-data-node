package sk.opendata.odn.harvester.datanest.organizations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.quartz.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.model.ProcurementRecord;
import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.sesame.SesameBackend;
import sk.opendata.odn.serialization.rdf.ProcurementRdfSerializer;
import au.com.bytecode.opencsv.CSVReader;

/**
 * This class contains stuff related to scraper of Vestník Verejného
 * Obstarávania published by Aliancia Fair-Play at
 * http://datanest.fair-play.sk/datasets/2 .
 */
public class ProcurementsDatanestHarvester extends AbstractDatanestHarvester
		implements Job {

	public final static String KEY_DATANEST_PROCUREMENTS_URL = "datanest.procurements.url";
	public final static String KEY_DATANEST_PROCUREMENTS_SEZAME_REPO_NAME = "datanest.procurements.sesame_repo_name";
	
	private final static int ATTR_INDEX_ID = 0;
	private final static int ATTR_INDEX_NOTE = 5;
	private final static int ATTR_INDEX_YEAR = 6;
	private final static int ATTR_INDEX_BULLETIN_ID = 7;
	private final static int ATTR_INDEX_PROCUREMENT_ID = 8;
	private final static int ATTR_INDEX_PROCUREMENT_SUBJECT = 9;
	private final static int ATTR_INDEX_PRICE = 10;
	private final static int ATTR_INDEX_CURRENCY = 11;
	private final static int ATTR_INDEX_IS_VAT_INCLUDED = 12;
	private final static int ATTR_INDEX_CUSTOMER_ICO = 15;
	private final static int ATTR_INDEX_SUPPLIER_ICO = 17;
	
	private static Logger logger = LoggerFactory.getLogger(ProcurementsDatanestHarvester.class);
	private Vector<ProcurementRecord> records = null;
	private ProcurementRdfSerializer serializer = null;

	
	public ProcurementsDatanestHarvester() throws IOException,
			RepositoryConfigException, RepositoryException,
			ParserConfigurationException, TransformerConfigurationException {
		
		super();
		
		serializer = new ProcurementRdfSerializer(SesameBackend.getInstance(),
				datanestProperties.getProperty(
						KEY_DATANEST_PROCUREMENTS_SEZAME_REPO_NAME));
	}
	
	private ProcurementRecord scrapOneRecord(String[] row) throws ParseException {
		ProcurementRecord record = new ProcurementRecord();
		
		record.setId(row[ATTR_INDEX_ID]);
		record.setNote(row[ATTR_INDEX_NOTE]);
		record.setYear(row[ATTR_INDEX_YEAR]);
		record.setBulletinId(row[ATTR_INDEX_BULLETIN_ID]);
		record.setProcurementId(row[ATTR_INDEX_PROCUREMENT_ID]);
		record.setProcurementSubject(row[ATTR_INDEX_PROCUREMENT_SUBJECT]);
		record.setPrice(Float.valueOf(row[ATTR_INDEX_PRICE]));
		record.setCurrency(row[ATTR_INDEX_CURRENCY]);
		record.setVatIncluded(Boolean.valueOf(row[ATTR_INDEX_IS_VAT_INCLUDED]));
		record.setCustomerIco(row[ATTR_INDEX_CUSTOMER_ICO]);
		record.setSupplierIco(row[ATTR_INDEX_SUPPLIER_ICO]);
		
		logger.debug("scrapped record of: " + record.getId());
		
		return record;
	}
	
	@Override
	public void update() throws IOException, ParseException,
			RepositoryConfigException, RepositoryException,
			TransformerException, IllegalArgumentException, OdnRepositoryException {
		
		URL csvUrl = new URL(datanestProperties.getProperty(KEY_DATANEST_PROCUREMENTS_URL));
		logger.debug("going to load data from " + csvUrl.toExternalForm());
		
		// "open" the CSV dump
		CSVReader csvReader = new CSVReader(
				new BufferedReader(
						new InputStreamReader(
								csvUrl.openStream())));
	    
		records = new Vector<ProcurementRecord>();
		
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

}
