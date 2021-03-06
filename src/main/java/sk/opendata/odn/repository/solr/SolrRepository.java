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
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.OdnRepositoryStoreInterface;
import sk.opendata.odn.utils.ApplicationProperties;

/**
 * SOLR repository for Open Data Node.
 */
public class SolrRepository implements OdnRepositoryStoreInterface<List<SolrItem>> {

	public final static String SOLR_REPOSITORY_PROPERTIES_NAME = "/repo-solr.properties";
	public final static String KEY_DEBUG_DUMP = "solr.debug.dump";
	public final static String KEY_REPO_URL = "solr.repo.url";
	public final static String KEY_REPO_ENABLED = "solr.repo.enabled";

	private static Logger logger = LoggerFactory.getLogger(SolrRepository.class);
	private ApplicationProperties srProperties = null;
	private boolean enabled = false;
	private SolrServer solrServer = null;

	private static SolrRepository instance = null;

	/**
	 * Initialize SOLR back-end.
	 * 
	 * @throws IOException
	 *             when error occurs while loading properties
	 */
	private SolrRepository() throws IOException {
		// load properties
		srProperties = ApplicationProperties
				.getInstance(SOLR_REPOSITORY_PROPERTIES_NAME);

		// initialize SOLR server connection
		enabled = Boolean.valueOf(srProperties.getProperty(KEY_REPO_ENABLED));
		if (enabled) {
			String solrServerUrl = srProperties.getProperty(KEY_REPO_URL);
			solrServer = new HttpSolrServer(solrServerUrl);
			logger.info("SOLR index '" + solrServerUrl + "' initialized");
		}
		else
			logger.info("SOLR index disabled");
	}

	/**
	 * Get the instance of SOLR repository singleton.
	 * 
	 * @return instance of SOLR repository
	 * @throws IOException
	 *             when error occurs while loading properties
	 */
	public static SolrRepository getInstance() throws IOException {

		if (instance == null)
			instance = new SolrRepository();

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
		
		if (!enabled)
			// disabled => do not store anything
			return;
		
		OdnRepositoryException odnRepoException = null;

		try {
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
		}

		if (odnRepoException != null)
			throw odnRepoException;
	}

	@Override
	public void shutDown() throws OdnRepositoryException {
		solrServer = null;
		instance = null;
	}

	/**
	 * Purpose of this is to provide access to data stored in SOLR repository for search GUI.
	 * 
	 * TODO: Clean-up this setter. It might not be a good idea to give away reference to that object.
	 * 
	 * @return SOLR server instance used by this repository
	 */
	public SolrServer getSolrServer() {
		return solrServer;
	}
	
}
