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

import org.apache.commons.lang3.StringEscapeUtils;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.quartz.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.model.OrganizationRecord;
import sk.opendata.odn.repository.sesame.SesameRepository;
import sk.opendata.odn.repository.solr.SolrRepository;
import sk.opendata.odn.serialization.rdf.OrganizationRdfSerializer;
import sk.opendata.odn.serialization.solr.SolrSerializer;

/**
 * This class contains stuff related to scraper of Register Organizacii
 * published by Aliancia Fair-Play at http://datanest.fair-play.sk/datasets/1 .
 */
public class OrganizationsDatanestHarvester extends
		AbstractDatanestHarvester<OrganizationRecord> implements Job {

	public final static String KEY_DATANEST_ORGANIZATIONS_URL_KEY = "datanest.organizations.url";
	
	protected final static int ATTR_INDEX_ID = 0;
	protected final static int ATTR_INDEX_NAME = 2;
	protected final static int ATTR_INDEX_SEAT = 4;
	protected final static int ATTR_INDEX_LEGAL_FORM = 5;
	protected final static int ATTR_INDEX_ICO = 3;
	protected final static int ATTR_INDEX_DATE_FROM = 7;
	protected final static int ATTR_INDEX_DATE_TO = 8;
	protected final static int ATTR_INDEX_SOURCE = 14;
	
	private static Logger logger = LoggerFactory.getLogger(OrganizationsDatanestHarvester.class);

	
	public OrganizationsDatanestHarvester() throws IOException,
			RepositoryConfigException, RepositoryException,
			ParserConfigurationException, TransformerConfigurationException {
		
		super(KEY_DATANEST_ORGANIZATIONS_URL_KEY);
		
		OrganizationRdfSerializer rdfSerializer = new OrganizationRdfSerializer(
				SesameRepository.getInstance());
		addSerializer(rdfSerializer);

		SolrSerializer<OrganizationRecord> solrSerializer = new SolrSerializer<OrganizationRecord>(
				SolrRepository.getInstance());
		addSerializer(solrSerializer);
	}
	
	@Override
	public OrganizationRecord scrapOneRecord(String[] row) throws ParseException {
		OrganizationRecord record = new OrganizationRecord();
		
		record.setId("org_" + row[ATTR_INDEX_ID]);
		record.setDatanestId(row[ATTR_INDEX_ID]);
		record.setSource(row[ATTR_INDEX_SOURCE]);
		record.setName(StringEscapeUtils.escapeXml(row[ATTR_INDEX_NAME]));
		record.setLegalForm(row[ATTR_INDEX_LEGAL_FORM]);
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

}