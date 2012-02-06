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
import java.util.Hashtable;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.OdnRepositoryInterface;
import sk.opendata.odn.utils.ApplicationProperties;

/**
 * Sesame backend/repository for Open Data Node.
 * 
 * "Little" terminology note:
 * 
 * <ul>
 * <li>in Open Data Node architecture, this (this class) is a repository</li>
 * <li>to achieve this, we're using multiple "Sesame repositories"</li>
 * <li>so we have a little problem with "repository" being a little bit
 * confusing (Open Data Node repository consists of one or more Sesame
 * repositories)</li>
 * <li>so, to avoid some of the confusion, this class is called "back-end"
 * instead of "repository"</li>
 * </ul>
 */
public class SesameBackend implements OdnRepositoryInterface<RdfData> {

	public final static String SESAME_REPOSITORY_PROPERTIES_NAME = "/repo-sesame.properties";
	public final static String KEY_DEBUG_DUMP_RDF = "sesame.debug.dump_rdf";
	public final static String PREFIX_KEY_REPO = "sesame.repo.";
	public final static String SUFFIX_KEY_SERVER = ".server";
	public final static String SUFFIX_KEY_ID = ".id";

	private static Logger logger = LoggerFactory.getLogger(SesameBackend.class);
	private ApplicationProperties srProperties = null;
	private Hashtable<String, HTTPRepository> sesameRepos = null;

	private static SesameBackend instance = null;

	/**
	 * Initialize Sesame back-end.
	 * 
	 * @throws IOException
	 *             when error occurs while loading properties
	 */
	private SesameBackend() throws IOException {
		// load properties
		srProperties = ApplicationProperties
				.getInstance(SESAME_REPOSITORY_PROPERTIES_NAME);

		// initialize repositories
		sesameRepos = new Hashtable<String, HTTPRepository>();
	}

	/**
	 * Get the instance of Sesame back-end singleton.
	 * 
	 * @return instance of Sesame backend
	 * @throws IOException
	 *             when error occurs while loading properties
	 */
	public static SesameBackend getInstance() throws IOException {

		if (instance == null)
			instance = new SesameBackend();

		return instance;
	}

	/**
	 * Initialize repository with given name.
	 * 
	 * Note: Do not confuse repository name (which is internal to the Open Data
	 * Node and is used in as key in our repository properties) and Sesame
	 * repository ID (which is relevant to the Sesame installation and can be
	 * found in our properties as value).
	 * 
	 * @param repoName
	 *            repository name
	 * @return instance of HTTP
	 * @throws RepositoryException
	 *             when initialization fails
	 */
	private HTTPRepository initRepo(String repoName) throws RepositoryException {

		String repoServer = srProperties.getProperty(PREFIX_KEY_REPO + repoName
				+ SUFFIX_KEY_SERVER);
		String repoID = srProperties.getProperty(PREFIX_KEY_REPO + repoName
				+ SUFFIX_KEY_ID);

		// TODO: indexes and other configuration parameters (as needed)
		HTTPRepository repo = new HTTPRepository(repoServer, repoID);
		repo.initialize();

		sesameRepos.put(repoName, repo);
		logger.info("repository '" + repoName + "' initialized ('" + repoServer
				+ "', '" + repoID + "')");

		return repo;
	}

	public HTTPRepository getRepo(String repoName) throws RepositoryException {

		HTTPRepository repo = sesameRepos.get(repoName);

		if (repo == null)
			repo = initRepo(repoName);

		return repo;
	}

	private void debugRdfDump(String repoName, String rdfData) {
		try {
			File dumpFile = File.createTempFile(repoName + "-", ".rdf");
			BufferedWriter out = new BufferedWriter(new FileWriter(dumpFile));
			out.write(rdfData);
			out.close();
			logger.info("RDF dump saved to file " + dumpFile);
		} catch (IOException e) {
			logger.error("IO exception", e);
		}
	}

	/**
	 * Store given record into Sesame repository with given name.
	 * 
	 * @param repoName
	 *            name of Sesame repository to store into
	 * @param records
	 *            records to store (in RDF format with additional info)
	 * @param contexts
	 *            the context for RDF statements used for the statements in the
	 *            repository
	 * 
	 * @throws IllegalArgumentException
	 *             if repository with given name does not exists
	 * @throws OdnRepositoryException
	 *             when error occurs while connecting to the Sesame repository
	 *             or when Sesame "add" operation fails
	 * @throws RepositoryException
	 *             when initialization fails
	 */
	public void store(String repoName, RdfData records, String... contexts)
			throws IllegalArgumentException, OdnRepositoryException {

		OdnRepositoryException odnRepoException = null;
		RepositoryConnection connection = null;

		try {
			HTTPRepository repo = getRepo(repoName);
			if (repo == null)
				throw new IllegalArgumentException(repoName
						+ " does not exists");

			URI[] convertedContexts = null;
			if (contexts.length > 0) {
				ValueFactory valueFactory = repo.getValueFactory();
				convertedContexts = new URI[contexts.length];
				int index = 0;
				for (String context : contexts) {
					convertedContexts[index++] = valueFactory
							.createURI(context);
				}
			}

			connection = repo.getConnection();

			// As of now, the "update" consist of fresh "whole at once" copy of
			// the new data loaded into the repository. Thus, we need to remove
			// existing data from the repository before loading the new data so
			// as to prevent old, stale data to be left in the repository (like
			// items which were valid yesterday, but then deemed "bad" or
			// whatever and deleted).
			// Note: Yes, that is costly and we want to fix that later on.
			// FIXME: Implement proper "update" procedure.
			if (contexts.length > 0) {
				connection.clear(convertedContexts);

				// why we duplicate the 'clear()' and 'add()' statements:
				// 'getStatements(null, null, null, true);' is not the same as
				// 'getStatements(null, null, null, true, (Resource)null);' -
				// see
				// http://www.openrdf.org/doc/sesame2/2.3.2/users/userguide.html#d0e1218
				connection.add(new StringReader(records.getRdfData()),
						records.getRdfBaseURI(), RDFFormat.RDFXML,
						convertedContexts);
			} else {
				// CRUDE HACK, FIXME: If we use contexts for the "all"
				// repository to distinguish statements in terms of where they
				// came from so that we can do a proper clean-up before
				// "update", I'm then not able yet to make a proper query on top
				// of statements from different contexts. Thus for now I'm not
				// using contexts and for "all" repository I'm not doing the
				// automatic clean-up, which means that "Clean" needs to be done
				// on the repo manually!!!
				if (!repoName.equals("all"))
					connection.clear();

				connection.add(new StringReader(records.getRdfData()),
						records.getRdfBaseURI(), RDFFormat.RDFXML);
			}

			logger.info("pushed " + records.getRdfData().length()
					+ " characters of RDF into the Sesame repository '"
					+ repoName + "'");

			if (Boolean.valueOf(srProperties.getProperty(KEY_DEBUG_DUMP_RDF)))
				debugRdfDump(repoName, records.getRdfData());
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

	public void shutDown() throws RepositoryException {
		for (Repository repo : sesameRepos.values())
			repo.shutDown();
	}

}
