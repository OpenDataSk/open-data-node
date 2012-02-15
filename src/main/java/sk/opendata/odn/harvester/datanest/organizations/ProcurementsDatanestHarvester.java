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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.quartz.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.model.Currency;
import sk.opendata.odn.model.ProcurementRecord;
import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.sesame.SesameBackend;
import sk.opendata.odn.serialization.OdnSerializationException;
import sk.opendata.odn.serialization.rdf.ProcurementRdfSerializer;
import au.com.bytecode.opencsv.CSVReader;

/**
 * This class contains stuff related to scraper of Vestník Verejného
 * Obstarávania published by Aliancia Fair-Play at
 * http://datanest.fair-play.sk/datasets/2 .
 */
public class ProcurementsDatanestHarvester extends
		AbstractDatanestHarvester<ProcurementRecord> implements Job {

	public final static String KEY_DATANEST_PROCUREMENTS_URL = "datanest.procurements.url";
	public final static String KEY_DATANEST_PROCUREMENTS_SEZAME_REPO_NAME = "datanest.procurements.sesame_repo_name";
	
	public final static String SC_MISSING_CURRENCY = "missing currency";
	public final static String SC_MISSING_CURRENCY_FOR_NON_ZERO_PRICE = "missing currency (for price which is non-zero)";
	public final static String SC_UNKNOWN_CURRENCY = "unknown currency: ";
	
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

	
	public ProcurementsDatanestHarvester() throws IOException,
			RepositoryConfigException, RepositoryException,
			ParserConfigurationException, TransformerConfigurationException {
		
		super();
		
		ProcurementRdfSerializer rdfSerializer = new ProcurementRdfSerializer(
				SesameBackend.getInstance(),
				datanestProperties
						.getProperty(KEY_DATANEST_PROCUREMENTS_SEZAME_REPO_NAME));
		serializers.add(rdfSerializer);
	}
	
	@Override
	public ProcurementRecord scrapOneRecord(String[] row) throws ParseException {
		ProcurementRecord record = new ProcurementRecord();
		
		record.setId("procurement_" + row[ATTR_INDEX_ID]);
		record.setDatanestId(row[ATTR_INDEX_ID]);
		record.setNote(row[ATTR_INDEX_NOTE]);
		record.setYear(row[ATTR_INDEX_YEAR]);
		record.setBulletinId(row[ATTR_INDEX_BULLETIN_ID]);
		record.setProcurementId(row[ATTR_INDEX_PROCUREMENT_ID]);
		record.setProcurementSubject(row[ATTR_INDEX_PROCUREMENT_SUBJECT]);
		record.setPrice(Float.valueOf(row[ATTR_INDEX_PRICE]));
		
		if (!row[ATTR_INDEX_CURRENCY].isEmpty()) {
			try {
				Currency currency = Currency.parse(row[ATTR_INDEX_CURRENCY]);
				record.setCurrency(currency);
			}
			catch (IllegalArgumentException e) {
				// unknown currencies
				record.addScrapNote(SC_UNKNOWN_CURRENCY + row[ATTR_INDEX_CURRENCY]);
			}
		}
		else {
			// sometimes the currency is not filled in the source (so far only
			// for cases where the price was 0)
			if (record.getPrice() == 0)
				record.addScrapNote(SC_MISSING_CURRENCY);
			else
				record.addScrapNote(SC_MISSING_CURRENCY_FOR_NON_ZERO_PRICE);
		}
		
		record.setVatIncluded(Boolean.valueOf(row[ATTR_INDEX_IS_VAT_INCLUDED]));
		record.setCustomerIco(row[ATTR_INDEX_CUSTOMER_ICO]);
		record.setSupplierIco(row[ATTR_INDEX_SUPPLIER_ICO]);
		
		logger.debug("scrapped record of: " + record.getDatanestId());
		
		return record;
	}
	
	@Override
	public void update() throws OdnHarvesterException,
			OdnSerializationException, OdnRepositoryException {
		
		OdnHarvesterException odnHarvestgerException = null;
		
		try {
			URL csvUrl = new URL(datanestProperties.getProperty(KEY_DATANEST_PROCUREMENTS_URL));
			logger.debug("going to load data from " + csvUrl.toExternalForm());
			
			// "open" the CSV dump
			CSVReader csvReader = new CSVReader(
					new BufferedReader(
							new InputStreamReader(
									csvUrl.openStream())));
		    
			Vector<ProcurementRecord> records = new Vector<ProcurementRecord>();
			
			// TODO: check the header - for now we simply skip it
			csvReader.readNext();
			
			// read the rows
			String[] row;
			int debugProcessOnlyNItems = Integer.valueOf(datanestProperties.getProperty(KEY_DEBUG_PROCESS_ONLY_N_ITEMS));
		    while ((row = csvReader.readNext()) != null) {
		    	try {
			        records.add(scrapOneRecord(row));
		    	}
		    	catch (IllegalArgumentException e) {
					logger.warn("illegal argument exception", e);
					logger.warn("skipping following record: "
							+ Arrays.deepToString(row));
		    	} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        
		        if (debugProcessOnlyNItems > 0 &&
		        		records.size() > debugProcessOnlyNItems)
		        	break;
		    }
		    
		    // store the results
		    store(records);
		    
		// TODO: If there wont be any more specialized error handling here
		// in the future, try catching only 'Exception' to simplify the
		// code.
		} catch (MalformedURLException e) {
			logger.error("malformed URL exception", e);
			odnHarvestgerException = new OdnHarvesterException(e.getMessage(), e);
		} catch (IOException e) {
			logger.error("IO exception", e);
			odnHarvestgerException = new OdnHarvesterException(e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			logger.error("illegal argument exception", e);
			odnHarvestgerException = new OdnHarvesterException(e.getMessage(), e);
		}

		if (odnHarvestgerException != null)
			throw odnHarvestgerException;
	}

}
