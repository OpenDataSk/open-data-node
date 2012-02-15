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
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.quartz.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.model.OrganizationRecord;
import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.sesame.SesameBackend;
import sk.opendata.odn.repository.solr.SolrBackend;
import sk.opendata.odn.serialization.rdf.OrganizationRdfSerializer;
import sk.opendata.odn.serialization.solr.OrganizationSolrSerializer;
import au.com.bytecode.opencsv.CSVReader;

/**
 * This class contains stuff related to scraper of Register Organizacii
 * published by Aliancia Fair-Play at http://datanest.fair-play.sk/datasets/1 .
 */
public class OrganizationsDatanestHarvester extends
		AbstractDatanestHarvester<OrganizationRecord> implements Job {

	public final static String KEY_DATANEST_ORGANIZATIONS_URL = "datanest.organizations.url";
	public final static String KEY_DATANEST_ORGANIZATIONS_SEZAME_REPO_NAME = "datanest.organizations.sesame_repo_name";
	
	private final static int ATTR_INDEX_ID = 0;
	protected final static int ATTR_INDEX_NAME = 1;
	protected final static int ATTR_INDEX_SEAT = 3;
	protected final static int ATTR_INDEX_LEGAL_FORM = 4;
	protected final static int ATTR_INDEX_ICO = 2;
	protected final static int ATTR_INDEX_DATE_FROM = 6;
	protected final static int ATTR_INDEX_DATE_TO = 7;
	protected final static int ATTR_INDEX_SOURCE = 13;
	
	private static Logger logger = LoggerFactory.getLogger(OrganizationsDatanestHarvester.class);
	// TODO: transform that into list of serializers
	private OrganizationRdfSerializer rdfSerializer = null;
	private OrganizationSolrSerializer solrSerializer = null;

	
	public OrganizationsDatanestHarvester() throws IOException,
			RepositoryConfigException, RepositoryException,
			ParserConfigurationException, TransformerConfigurationException {
		
		super();
		
		rdfSerializer = new OrganizationRdfSerializer(SesameBackend.getInstance(),
				datanestProperties.getProperty(
						KEY_DATANEST_ORGANIZATIONS_SEZAME_REPO_NAME));
		
		solrSerializer = new OrganizationSolrSerializer(SolrBackend.getInstance());
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
	
	@Override
	public void update() throws OdnHarvesterException, OdnRepositoryException {
		
		OdnHarvesterException odnHarvestgerException = null;
		
		try {
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
		    rdfSerializer.store(records);
		    solrSerializer.store(records);
		    
		// TODO: If there wont be any more specialized error handling here
		// in the future, try catching only 'Exception' to simplify the
		// code.
		} catch (MalformedURLException e) {
			logger.error("malformed URL exception", e);
			odnHarvestgerException = new OdnHarvesterException(e.getMessage(), e);
		} catch (IOException e) {
			logger.error("IO exception", e);
			odnHarvestgerException = new OdnHarvesterException(e.getMessage(), e);
		} catch (ParseException e) {
			logger.error("parse exception", e);
			odnHarvestgerException = new OdnHarvesterException(e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			logger.error("illegal argument exception", e);
			odnHarvestgerException = new OdnHarvesterException(e.getMessage(), e);
		} catch (TransformerException e) {
			logger.error("transformer exception", e);
			odnHarvestgerException = new OdnHarvesterException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			logger.error("illegal access exception", e);
			odnHarvestgerException = new OdnHarvesterException(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			logger.error("invocation target exception", e);
			odnHarvestgerException = new OdnHarvesterException(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			logger.error("no such method exception", e);
			odnHarvestgerException = new OdnHarvesterException(e.getMessage(), e);
		}

		if (odnHarvestgerException != null)
			throw odnHarvestgerException;
	}

}