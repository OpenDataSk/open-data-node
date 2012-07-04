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

import java.io.IOException;
import java.text.ParseException;

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
import sk.opendata.odn.repository.sesame.SesameRepository;
import sk.opendata.odn.repository.solr.SolrRepository;
import sk.opendata.odn.serialization.OdnSerializationException;
import sk.opendata.odn.serialization.rdf.ProcurementRdfSerializer;
import sk.opendata.odn.serialization.solr.SolrSerializer;

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
	public final static String SC_MISSING_PRICE = "missing price";
	
	protected final static int ATTR_INDEX_ID = 0;
	protected final static int ATTR_INDEX_NOTE = 5;
	protected final static int ATTR_INDEX_YEAR = 6;
	protected final static int ATTR_INDEX_BULLETIN_ID = 7;
	protected final static int ATTR_INDEX_PROCUREMENT_ID = 8;
	protected final static int ATTR_INDEX_PROCUREMENT_SUBJECT = 9;
	protected final static int ATTR_INDEX_PRICE = 10;
	protected final static int ATTR_INDEX_CURRENCY = 11;
	protected final static int ATTR_INDEX_IS_VAT_INCLUDED = 12;
	protected final static int ATTR_INDEX_CUSTOMER_ICO = 15;
	protected final static int ATTR_INDEX_SUPPLIER_ICO = 17;
	
	private static Logger logger = LoggerFactory.getLogger(ProcurementsDatanestHarvester.class);

	
	public ProcurementsDatanestHarvester() throws IOException,
			RepositoryConfigException, RepositoryException,
			ParserConfigurationException, TransformerConfigurationException {
		
		super(SolrRepository.getInstance());	// TODO: fix that by using Jackrabbit
		
		ProcurementRdfSerializer rdfSerializer = new ProcurementRdfSerializer(
				SesameRepository.getInstance(),
				datanestProperties
						.getProperty(KEY_DATANEST_PROCUREMENTS_SEZAME_REPO_NAME));
		serializers.add(rdfSerializer);

		SolrSerializer<ProcurementRecord> solrSerializer = new SolrSerializer<ProcurementRecord>(
				SolrRepository.getInstance());
		serializers.add(solrSerializer);
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
		
		if (row[ATTR_INDEX_PRICE].isEmpty())
			// some entries (like ID 49338, from
			// http://www.e-vestnik.sk/EVestnik/Detail/29531) have empty string
			// for price
			record.addScrapNote(SC_MISSING_PRICE);
		else
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
			record.setCurrency(Currency.UNDEFINED);
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
	
		genericUpdate(KEY_DATANEST_PROCUREMENTS_URL);
	}

}
