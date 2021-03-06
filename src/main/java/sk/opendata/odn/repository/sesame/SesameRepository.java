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

package sk.opendata.odn.repository.sesame;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.OdnRepositoryStoreInterface;
import sk.opendata.odn.utils.ApplicationProperties;

/**
 * Sesame repository for Open Data Node.
 */
public class SesameRepository implements OdnRepositoryStoreInterface<RdfData> {

	public final static String SESAME_REPOSITORY_PROPERTIES_NAME = "/repo-sesame.properties";
	public final static String PREFIX_KEY_REPO = "sesame.repo.";
	public final static String KEY_SERVER = PREFIX_KEY_REPO + "server";
	public final static String KEY_REPO_ENABLED = PREFIX_KEY_REPO + "enabled";
	public final static String KEY_ID = PREFIX_KEY_REPO + "id";
	public final static String PREFIX_KEY_CONTEXTS = "sesame.contexts.";
	public final static String PREFIX_KEY_RDF_DUMP = "sesame.rdf_dump.";

	private static Logger logger = LoggerFactory.getLogger(SesameRepository.class);
	private ApplicationProperties srProperties = null;
	private boolean enabled = false;
	private HTTPRepository sesameRepo = null;

	private static SesameRepository instance = null;

	/**
	 * Initialize Sesame back-end.
	 * 
	 * @throws IOException
	 *             when error occurs while loading properties
	 */
	private SesameRepository() throws IOException {
		// load properties
		srProperties = ApplicationProperties
				.getInstance(SESAME_REPOSITORY_PROPERTIES_NAME);
		enabled = Boolean.valueOf(srProperties.getProperty(KEY_REPO_ENABLED));
		if (!enabled)
			logger.info("Sesame repository disabled");
	}

	/**
	 * Get the instance of Sesame repository singleton.
	 * 
	 * @return instance of Sesame repository
	 * @throws IOException
	 *             when error occurs while loading properties
	 */
	public static SesameRepository getInstance() throws IOException {

		if (instance == null)
			instance = new SesameRepository();

		return instance;
	}

	/**
	 * Initialize Sesame repository.
	 * 
	 * @return instance of proxy for a remote repository on a Sesame server
	 * @throws RepositoryException
	 *             when initialization fails
	 */
	private HTTPRepository initRepo() throws RepositoryException {

		enabled = Boolean.valueOf(srProperties.getProperty(KEY_REPO_ENABLED));
		String repoServer = srProperties.getProperty(KEY_SERVER);
		String repoID = srProperties.getProperty(KEY_ID);

		// TODO: indexes and other configuration parameters (as needed)
		HTTPRepository repo = new HTTPRepository(repoServer, repoID);
		repo.initialize();

		sesameRepo = repo;
		logger.info("Sesame repository initialized ('" + repoServer
				+ "', '" + repoID + "')");

		return repo;
	}

	private synchronized HTTPRepository getRepo() throws RepositoryException {

		HTTPRepository repo = sesameRepo;

		if (repo == null)
			repo = initRepo();

		return repo;
	}

	private void rdfDump(String fn, String rdfData) {
		try {
			File dumpFile = new File(fn);
			BufferedWriter out = new BufferedWriter(new FileWriter(dumpFile));
			out.write(rdfData);
			out.close();
			logger.info("RDF dump saved to file " + dumpFile);
		} catch (IOException e) {
			logger.error("IO exception", e);
		}
	}

	private URI[] determineRdfContexts(String rdfContextsKey, ValueFactory valueFactory) {
		if (rdfContextsKey == null || rdfContextsKey.isEmpty())
			// null/empty key name => null property => no contexts
			return null;
		
		String rdfContextsProperty = srProperties.getProperty(PREFIX_KEY_CONTEXTS + rdfContextsKey);
		if (rdfContextsProperty == null || rdfContextsProperty.isEmpty())
			// null/empty property => no contexts
			return null;
		
		String[] rdfContextsStrings = rdfContextsProperty.split(",");
		if (rdfContextsStrings.length <= 0)
			// empty array => no contexts
			return null;
		
		URI[] result = new URI[rdfContextsStrings.length];
		int index = 0;
		for (String context : rdfContextsStrings) {
			result[index++] = valueFactory.createURI(context);
		}
		
		return result;
	}
	
	/**
	 * Store given record into Sesame repository with given name.
	 * 
	 * @param records
	 *            records to store (in RDF format with additional info)
	 * 
	 * @throws IllegalArgumentException
	 *             if repository with given name does not exists
	 * @throws OdnRepositoryException
	 *             when error occurs while connecting to the Sesame repository
	 *             or when Sesame "add" operation fails
	 * @throws RepositoryException
	 *             when initialization fails
	 */
	@Override
	public void store(RdfData records)
			throws IllegalArgumentException, OdnRepositoryException {

		if (!enabled)
			// disabled => do not store anything
			return;
		
		OdnRepositoryException odnRepoException = null;
		RepositoryConnection connection = null;

		try {
			HTTPRepository repo = getRepo();
			if (repo == null)
				throw new IllegalArgumentException("Sesame repository not found");

			URI[] contexts = determineRdfContexts(records.getPropKey(),
					repo.getValueFactory());
			String rdfDumpFn = srProperties.getProperty(PREFIX_KEY_RDF_DUMP
					+ records.getPropKey());

			connection = repo.getConnection();

			if (contexts != null && contexts.length > 0) {
				// why we duplicate the 'add()' statements:
				// 'getStatements(null, null, null, true);' is not the same as
				// 'getStatements(null, null, null, true, (Resource)null);' -
				// see
				// http://www.openrdf.org/doc/sesame2/2.3.2/users/userguide.html#d0e1218
				connection.add(new StringReader(records.getRdfData()),
						records.getRdfBaseURI(), RDFFormat.RDFXML,
						contexts);
			} else {
				connection.add(new StringReader(records.getRdfData()),
						records.getRdfBaseURI(), RDFFormat.RDFXML);
			}

			logger.info("pushed " + records.getRdfData().length()
					+ " characters of RDF into the Sesame repository");

			if (rdfDumpFn != null && !rdfDumpFn.isEmpty())
				rdfDump(rdfDumpFn, records.getRdfData());
		} catch (RepositoryException e) {
			logger.error("repository exception", e);
			odnRepoException = new OdnRepositoryException(e.getMessage(), e);
		} catch (RDFParseException e) {
			logger.error("RDF parser exception", e);
			odnRepoException = new OdnRepositoryException(e.getMessage(), e);
		} catch (IOException e) {
			logger.error("IO exception", e);
			odnRepoException = new OdnRepositoryException(e.getMessage(), e);
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (RepositoryException e) {
					logger.error("repository exception in 'finally' statement",
							e);
				}
		}

		if (odnRepoException != null)
			throw odnRepoException;
	}

	@Override
	public void shutDown() throws OdnRepositoryException {
		RepositoryException repoException = null;
		
		try {
			if (sesameRepo != null)
				sesameRepo.shutDown();
		} catch (RepositoryException e) {
			logger.error("repository exception", e);
			// we're going to re-throw first exception we encounter
			repoException = e;
		}
		
		sesameRepo = null;
		instance = null;
		
		if (repoException != null)
			throw new OdnRepositoryException(repoException.getMessage(), repoException);
	}

}
