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

package sk.opendata.odn.repository.jackrabbit;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.naming.NamingException;

import org.apache.jackrabbit.rmi.repository.URLRemoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.repository.OdnRepositoryException;
import sk.opendata.odn.repository.OdnRepositoryRetrieveInterface;
import sk.opendata.odn.repository.OdnRepositoryStoreInterface;
import sk.opendata.odn.utils.ApplicationProperties;

/**
 * Jackrabbit repository for Open Data Node.
 */
public class JackrabbitRepository implements OdnRepositoryStoreInterface<List<JackrabbitItem>>, OdnRepositoryRetrieveInterface<JackrabbitItem> {

	public final static String JACKRABBIT_REPOSITORY_PROPERTIES_NAME = "/repo-jackrabbit.properties";
	//public final static String KEY_DEBUG_DUMP_RDF = "sesame.debug.dump_rdf";
	public final static String KEY_REPO_URL = "jackrabbit.repo.url";
	//public final static String KEY_ID = PREFIX_KEY_REPO + "id";
	//public final static String PREFIX_KEY_CONTEXTS = PREFIX_KEY_REPO + "contexts.";

	private static Logger logger = LoggerFactory.getLogger(JackrabbitRepository.class);
	private ApplicationProperties srProperties = null;
	private Session jackrabbitSession = null;

	private static JackrabbitRepository instance = null;

	/**
	 * Initialize Jackrabbit back-end.
	 * 
	 * @throws IOException
	 *             when error occurs while loading properties
	 */
	private JackrabbitRepository() throws IOException {
		// load properties
		srProperties = ApplicationProperties
				.getInstance(JACKRABBIT_REPOSITORY_PROPERTIES_NAME);
	}

	/**
	 * Get the instance of Jackrabbit repository singleton.
	 * 
	 * @return instance of Jackrabbit repository
	 * @throws IOException
	 *             when error occurs while loading properties
	 */
	public static JackrabbitRepository getInstance() throws IOException {

		if (instance == null)
			instance = new JackrabbitRepository();
		
		// debug: just to test the session log in
		// status: it works but uses local repo created in homedir (derby.log, repository and repository.xml)
		// TODO: convince it to use remote Jackrabbit, by default on localhost, and configure it in my ~/.odn/ to use either VM or OHIO
		try {
			instance.getRepo();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		}

		return instance;
	}

	/**
	 * Initialize Jackrabbit repository.
	 * 
	 * @return instance of session object providing read and write access to content
	 * @throws RepositoryException when repository initialization fails
	 * @throws NamingException when repository lookup fails
	 * @throws MalformedURLException ...
	 */
	private Session initRepo() throws RepositoryException, NamingException, MalformedURLException {

		String repoUrl = srProperties.getProperty(KEY_REPO_URL);
		//String repoID = srProperties.getProperty(KEY_ID);

		// hopefully final: repository via environment lookup, thus it can be RMI, ...
		// simpler RMI method
		// note: this one works, firewall on target machine (the one running Jackrabbit) have to be disabled
		Repository repository = new URLRemoteRepository(repoUrl);
		Session session = null;
		// TODO: use naming
		//InitialContext context = new InitialContext();
		//Context environment = (Context)context.lookup("java:comp/env");
		//Repository repository = (Repository)environment.lookup("jcr/odnRepository");
		//Session session = null;
		
		try {
			session = repository.login();
			
			jackrabbitSession = session;
			
			String user = session.getUserID();
			String name = repository.getDescriptor(Repository.REP_NAME_DESC);
			logger.info("Jackrabbit repository initialized (user: '" + user
					+ "', name: '" + name + "')");
		}
		finally {
			if (session != null)
				session.logout();
		}

		return jackrabbitSession;
	}

	// TODO: rename to 'getSession()' - it is a private method so no need to maintain some common naming with other Repository classes, even more so if it is confusing naming in this case
	private synchronized Session getRepo() throws RepositoryException, NamingException, MalformedURLException {

		Session repo = jackrabbitSession;

		if (repo == null)
			repo = initRepo();

		return repo;
	}

	/* TODO: remove
	private void debugRdfDump(String rdfData) {
		try {
			File dumpFile = File.createTempFile("odn-", ".rdf");
			BufferedWriter out = new BufferedWriter(new FileWriter(dumpFile));
			out.write(rdfData);
			out.close();
			logger.info("RDF dump saved to file " + dumpFile);
		} catch (IOException e) {
			logger.error("IO exception", e);
		}
	}
	*/

	/* TODO: remove
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
	*/
	
	/**
	 * Store given record into Jackrabbit repository with given name.
	 * 
	 * @param records
	 *            records to store (in RDF format with additional info)
	 * 
	 * @throws IllegalArgumentException
	 *             if repository with given name does not exists
	 * @throws OdnRepositoryException
	 *             when error occurs while connecting to the XXX Sesame repository
	 *             or when XXX Sesame "add" operation fails
	 * @throws RepositoryException
	 *             when initialization fails
	 */
	@Override
	public void store(List<JackrabbitItem> records)
			throws IllegalArgumentException, OdnRepositoryException {

		OdnRepositoryException odnRepoException = null;
		Session session = null;

		try {
			session = getRepo();
			if (session == null)
				throw new IllegalArgumentException("Jackrabbit repository not found");

			/* TODO:
			URI[] contexts = determineRdfContexts(records.getRdfContextsKey(),
					repo.getValueFactory());

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

			if (Boolean.valueOf(srProperties.getProperty(KEY_DEBUG_DUMP_RDF)))
				debugRdfDump(records.getRdfData());
			*/
		} catch (RepositoryException e) {
			logger.error("repository exception", e);
			odnRepoException = new OdnRepositoryException(e.getMessage(), e);
		} catch (NamingException e) {
			logger.error("naming exception", e);
			odnRepoException = new OdnRepositoryException(e.getMessage(), e);
		} catch (MalformedURLException e) {
			logger.error("malformed URL exception", e);
			odnRepoException = new OdnRepositoryException(e.getMessage(), e);
		} finally {
			if (session != null)
				session.logout();
		}

		if (odnRepoException != null)
			throw odnRepoException;
	}

	@Override
	public JackrabbitItem retrieve(String id) throws IllegalArgumentException,
			OdnRepositoryException {
		
		JackrabbitItem result = null;

		/* TODO:
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "id:" + id);

		try {
			QueryResponse response = solrServer.query(params);
			List<SolrItem> records = response.getBeans(SolrItem.class);
			
			// having multiple records with same ID in the repository is an ERROR
			if (records.size() > 1)
				throw new OdnRepositoryException("unable to retrieve record ("
						+ records.size() + " items found)");
			
			// one match or no match are expected, so return the match or return {@code null}
			if (records.size() == 1)
				result = records.get(0);
		} catch (SolrServerException e) {
			logger.error("SOLR server exception", e);
			throw new OdnRepositoryException(e.getMessage(), e);
		}
		*/

		return result;
	}

	@Override
	public void shutDown() throws OdnRepositoryException {
		if (jackrabbitSession != null)
			jackrabbitSession.logout();
		
		jackrabbitSession = null;
		instance = null;
	}

}
