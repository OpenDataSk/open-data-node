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
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.quartz.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.model.Currency;
import sk.opendata.odn.model.PoliticalPartyDonationRecord;
import sk.opendata.odn.repository.sesame.SesameRepository;
import sk.opendata.odn.repository.solr.SolrRepository;
import sk.opendata.odn.serialization.rdf.PoliticalPartyDonationRdfSerializer;
import sk.opendata.odn.serialization.solr.SolrSerializer;
import sk.opendata.odn.utils.PscUtil;

/**
 * This class contains stuff related to scraper of Sponzori politických strán
 * published by Aliancia Fair-Play at http://datanest.fair-play.sk/datasets/32 .
 */
public class PoliticalPartyDonationsDatanestHarvester extends
		AbstractDatanestHarvester<PoliticalPartyDonationRecord> implements Job {

	public final static String KEY_DATANEST_PPD_URL_KEY = "datanest.political_party_donors.url";
	
	protected final static int ATTR_INDEX_ID = 0;
	protected final static int ATTR_INDEX_DONOR_NAME = 1;
	protected final static int ATTR_INDEX_DONOR_SURNAME = 2;
	protected final static int ATTR_INDEX_DONOR_TITLE = 3;
	protected final static int ATTR_INDEX_DONOR_COMPANY = 4;
	protected final static int ATTR_INDEX_DONOR_ICO = 5;
	protected final static int ATTR_INDEX_DONATION_VALUE = 6;
	protected final static int ATTR_INDEX_DONATION_CURRENCY = 7;
	protected final static int ATTR_INDEX_DONOR_ADDRESS = 8;
	protected final static int ATTR_INDEX_DONOR_PSC = 9;
	protected final static int ATTR_INDEX_DONOR_CITY = 10;
	protected final static int ATTR_INDEX_RECIPIENT_PARTY = 11;
	protected final static int ATTR_INDEX_YEAR = 12;
	protected final static int ATTR_INDEX_ACCEPT_DATE = 13;
	protected final static int ATTR_INDEX_NOTE = 14;
	
	private static Logger logger = LoggerFactory.getLogger(PoliticalPartyDonationsDatanestHarvester.class);

	
	public PoliticalPartyDonationsDatanestHarvester() throws IOException,
			RepositoryConfigException, RepositoryException,
			ParserConfigurationException, TransformerConfigurationException {
		
		super(KEY_DATANEST_PPD_URL_KEY);
		
		PoliticalPartyDonationRdfSerializer rdfSerializer = new PoliticalPartyDonationRdfSerializer(
				SesameRepository.getInstance());
		addSerializer(rdfSerializer);

		SolrSerializer<PoliticalPartyDonationRecord> solrSerializer = new SolrSerializer<PoliticalPartyDonationRecord>(
				SolrRepository.getInstance());
		addSerializer(solrSerializer);
	}
	
	@Override
	public PoliticalPartyDonationRecord scrapOneRecord(String[] row) throws ParseException {
		PoliticalPartyDonationRecord record = new PoliticalPartyDonationRecord();
		
		record.setId("donation_" + row[ATTR_INDEX_ID]);
		record.setDatanestId(row[ATTR_INDEX_ID]);
		if (!row[ATTR_INDEX_DONOR_NAME].isEmpty())
			record.setDonorName(row[ATTR_INDEX_DONOR_NAME]);
		if (!row[ATTR_INDEX_DONOR_SURNAME].isEmpty())
			record.setDonorSurname(row[ATTR_INDEX_DONOR_SURNAME]);
		if (!row[ATTR_INDEX_DONOR_TITLE].isEmpty())
			record.setDonorTitle(row[ATTR_INDEX_DONOR_TITLE]);
		if (!row[ATTR_INDEX_DONOR_COMPANY].isEmpty())
			record.setName(row[ATTR_INDEX_DONOR_COMPANY]);
		if (!row[ATTR_INDEX_DONOR_ICO].isEmpty())
			record.setIco(row[ATTR_INDEX_DONOR_ICO]);
		if (!row[ATTR_INDEX_DONATION_VALUE].isEmpty())
			record.setDonationValue(Float.valueOf(row[ATTR_INDEX_DONATION_VALUE]));
		Currency currency = Currency.UNDEFINED;
		// note: Some "non cash" donations have empty string filled in column
		// currency so we use "UNDEFINED" for those.
		if (!row[ATTR_INDEX_DONATION_CURRENCY].isEmpty())
			currency = Currency.parse(row[ATTR_INDEX_DONATION_CURRENCY]);
		record.setCurrency(currency);
		record.setDonorAddress(row[ATTR_INDEX_DONOR_ADDRESS]);
		if (!row[ATTR_INDEX_DONOR_PSC].isEmpty())
			record.setDonorPsc(PscUtil.normalize(row[ATTR_INDEX_DONOR_PSC]));
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
		
		logger.debug("scrapped record of: " + record.getDatanestId());
		
		return record;
	}

}
