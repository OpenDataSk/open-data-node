package sk.opendata.odn.harvester.datanest.organizations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.quartz.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.model.Currency;
import sk.opendata.odn.model.PoliticalPartyDonationRecord;
import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.sesame.SesameBackend;
import sk.opendata.odn.serialization.rdf.PoliticalPartyDonationRdfSerializer;
import au.com.bytecode.opencsv.CSVReader;

/**
 * This class contains stuff related to scraper of Sponzori politických strán
 * published by Aliancia Fair-Play at http://datanest.fair-play.sk/datasets/32 .
 */
public class PoliticalPartyDonationsDatanestHarvester extends
		AbstractDatanestHarvester<PoliticalPartyDonationRecord> implements Job {

	public final static String KEY_DATANEST_PPD_URL = "datanest.political_party_donors.url";
	public final static String KEY_DATANEST_PPD_SEZAME_REPO_NAME = "datanest.political_party_donors.sesame_repo_name";
	
	private final static int ATTR_INDEX_ID = 0;
	private final static int ATTR_INDEX_DONOR_NAME = 1;
	private final static int ATTR_INDEX_DONOR_SURNAME = 2;
	private final static int ATTR_INDEX_DONOR_TITLE = 3;
	private final static int ATTR_INDEX_DONOR_COMPANY = 4;
	private final static int ATTR_INDEX_DONOR_ICO = 5;
	private final static int ATTR_INDEX_DONATION_VALUE = 6;
	private final static int ATTR_INDEX_DONATION_CURRENCY = 7;
	private final static int ATTR_INDEX_DONOR_ADDRESS = 8;
	private final static int ATTR_INDEX_DONOR_PSC = 9;
	private final static int ATTR_INDEX_DONOR_CITY = 10;
	private final static int ATTR_INDEX_RECIPIENT_PARTY = 11;
	private final static int ATTR_INDEX_YEAR = 12;
	private final static int ATTR_INDEX_ACCEPT_DATE = 13;
	private final static int ATTR_INDEX_NOTE = 14;
	
	private static Logger logger = LoggerFactory.getLogger(PoliticalPartyDonationsDatanestHarvester.class);
	private PoliticalPartyDonationRdfSerializer serializer = null;

	
	public PoliticalPartyDonationsDatanestHarvester() throws IOException,
			RepositoryConfigException, RepositoryException,
			ParserConfigurationException, TransformerConfigurationException {
		
		super();
		
		serializer = new PoliticalPartyDonationRdfSerializer(
				SesameBackend.getInstance(),
				datanestProperties
						.getProperty(KEY_DATANEST_PPD_SEZAME_REPO_NAME));
	}
	
	@Override
	public PoliticalPartyDonationRecord scrapOneRecord(String[] row) throws ParseException {
		PoliticalPartyDonationRecord record = new PoliticalPartyDonationRecord();
		
		record.setId(row[ATTR_INDEX_ID]);
		if (!row[ATTR_INDEX_DONOR_NAME].isEmpty())
			record.setDonorName(row[ATTR_INDEX_DONOR_NAME]);
		if (!row[ATTR_INDEX_DONOR_SURNAME].isEmpty())
			record.setDonorSurname(row[ATTR_INDEX_DONOR_SURNAME]);
		if (!row[ATTR_INDEX_DONOR_TITLE].isEmpty())
			record.setDonorTitle(row[ATTR_INDEX_DONOR_TITLE]);
		if (!row[ATTR_INDEX_DONOR_COMPANY].isEmpty())
			record.setDonorCompany(row[ATTR_INDEX_DONOR_COMPANY]);
		if (!row[ATTR_INDEX_DONOR_ICO].isEmpty())
			record.setDonorIco(row[ATTR_INDEX_DONOR_ICO]);
		if (!row[ATTR_INDEX_DONATION_VALUE].isEmpty())
			record.setDonationValue(Float.valueOf(row[ATTR_INDEX_DONATION_VALUE]));
		Currency currency = Currency.parse(row[ATTR_INDEX_DONATION_CURRENCY]);
		record.setDonationCurrency(currency);
		record.setDonorAddress(row[ATTR_INDEX_DONOR_ADDRESS]);
		if (!row[ATTR_INDEX_DONOR_PSC].isEmpty())
			record.setDonorPsc(row[ATTR_INDEX_DONOR_PSC]);
		if (!row[ATTR_INDEX_DONOR_CITY].isEmpty())
			record.setDonorCity(row[ATTR_INDEX_DONOR_CITY]);
		record.setRecipientParty(row[ATTR_INDEX_RECIPIENT_PARTY]);
		record.setYear(row[ATTR_INDEX_YEAR]);
		if (!row[ATTR_INDEX_ACCEPT_DATE].isEmpty()) {
			Date acceptDate = sdf.parse(row[ATTR_INDEX_ACCEPT_DATE]);
			record.setAcceptDate(acceptDate);
		}
		if (!row[ATTR_INDEX_NOTE].isEmpty())
			record.setNote(row[ATTR_INDEX_NOTE]);
		
		logger.debug("scrapped record of: " + record.getId());
		
		return record;
	}
	
	@Override
	public void update() throws IOException, ParseException,
			RepositoryConfigException, RepositoryException,
			TransformerException, IllegalArgumentException, OdnRepositoryException {
		
		URL csvUrl = new URL(datanestProperties.getProperty(KEY_DATANEST_PPD_URL));
		logger.debug("going to load data from " + csvUrl.toExternalForm());
		
		// "open" the CSV dump
		CSVReader csvReader = new CSVReader(
				new BufferedReader(
						new InputStreamReader(
								csvUrl.openStream())));
	    
		records = new Vector<PoliticalPartyDonationRecord>();
		
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
