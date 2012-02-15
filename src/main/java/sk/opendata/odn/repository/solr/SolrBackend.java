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

package sk.opendata.odn.repository.solr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.OdnRepositoryInterface;
import sk.opendata.odn.utils.ApplicationProperties;

/**
 * SOLR backend/repository for Open Data Node.
 * 
 * "Little" terminology note:
 * 
 * <ul>
 * <li>in Open Data Node architecture, this (this class) is a repository</li>
 * <li>in SOLR, it is called "index"</li>
 * <li>to achieve this, we're using "SOLR index"</li>
 * <li>but to be in-line with naming chosen for Sesame, "repository" is used</li>
 * </ul>
 */
public class SolrBackend implements OdnRepositoryInterface<List<SolrItem>> {

	public final static String SOLR_REPOSITORY_PROPERTIES_NAME = "/repo-solr.properties";
	public final static String KEY_DEBUG_DUMP = "solr.debug.dump";
	public final static String KEY_REPO_URL = "solr.repo.url";

	private static Logger logger = LoggerFactory.getLogger(SolrBackend.class);
	private ApplicationProperties srProperties = null;
	private SolrServer solrServer = null;

	private static SolrBackend instance = null;

	/**
	 * Initialize SOLR back-end.
	 * 
	 * @throws IOException
	 *             when error occurs while loading properties
	 */
	private SolrBackend() throws IOException {
		// load properties
		srProperties = ApplicationProperties
				.getInstance(SOLR_REPOSITORY_PROPERTIES_NAME);

		// initialize SOLR server connection
		String solrServerUrl = srProperties.getProperty(KEY_REPO_URL);
		solrServer = new CommonsHttpSolrServer(solrServerUrl);
		logger.info("index '" + solrServerUrl + "' initialized");
	}

	/**
	 * Get the instance of SOLR back-end singleton.
	 * 
	 * @return instance of SOLR backend
	 * @throws IOException
	 *             when error occurs while loading properties
	 */
	public static SolrBackend getInstance() throws IOException {

		if (instance == null)
			instance = new SolrBackend();

		return instance;
	}

	private void debugDump(List<SolrItem> solrItems) {
		try {
			File dumpFile = File.createTempFile("solr-", ".txt");
			BufferedWriter out = new BufferedWriter(new FileWriter(dumpFile));
			for (SolrItem solrItem : solrItems) {
				out.write(solrItem.toString());
				out.write('\n');
			}
			out.close();
			logger.info("dump saved to file " + dumpFile);
		} catch (IOException e) {
			logger.error("IO exception", e);
		}
	}

	/**
	 * Store given record into SOLR index with given name.
	 * 
	 * @param records
	 *            records to store (as beans)
	 * 
	 * @throws IllegalArgumentException
	 *             if repository with given name does not exists
	 * @throws OdnRepositoryException
	 *             when error occurs while connecting to the SOLR index
	 *             or when SOLR XXX "add" XXX operation fails
	 * @throws RepositoryException
	 *             when initialization fails
	 */
	@Override
	public void store(List<SolrItem> records)
			throws IllegalArgumentException, OdnRepositoryException {
		
		OdnRepositoryException odnRepoException = null;

		try {
			// As of now, the "update" consist of fresh "whole at once" copy of
			// the new data loaded into the repository. Thus, we need to remove
			// existing data from the repository before loading the new data so
			// as to prevent old, stale data to be left in the repository (like
			// items which were valid yesterday, but then deemed "bad" or
			// whatever and deleted).
			// Note: Yes, that is costly and we want to fix that later on.
			// FIXME: Implement proper "update" procedure. For now disabled as
			// we're pushing multiple data sets into one index meaning that if
			// we left this here, insertion of 2nd data set will mean deletion
			// of 1st etc. Workaround: Clean the index manualy if necessary.
			//solrServer.deleteByQuery("*:*");	// CAUTION: deletes everything!
			
			solrServer.addBeans(records);
			solrServer.commit();

			logger.info("pushed " + records.size()
					+ " documents of into the SOLR index");

			if (Boolean.valueOf(srProperties.getProperty(KEY_DEBUG_DUMP)))
				debugDump(records);
		} catch (SolrServerException e) {
			logger.error("SOLR server exception", e);
			odnRepoException = new OdnRepositoryException(e.getMessage(), e);
		} catch (IOException e) {
			logger.error("SOLR server exception", e);
			odnRepoException = new OdnRepositoryException(e.getMessage(), e);
//		} finally {
//			if (connection != null)
//				try {
//					connection.close();
//				} catch (RepositoryException e) {
//					logger.error("repository exception in 'finally' statement",
//							e);
//				}
		}

		if (odnRepoException != null)
			throw odnRepoException;
	}

}
