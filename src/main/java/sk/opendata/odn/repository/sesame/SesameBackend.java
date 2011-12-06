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
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.nativerdf.NativeStore;
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
 * <li>so we have a little problem with "repository" being a little bit confusing
 *   (Open Data Node repository consists of one or more Sesame repositories)</li>
 * <li>so, to avoid some of the confusion, this class is called "back-end"
 *   instead of "repository"</li>
 * </ul> 
 */
public class SesameBackend implements OdnRepositoryInterface<RdfData> {
	
	public final static String SESAME_REPOSITORY_PROPERTIES_NAME = "/repo-sesame.properties";
	public final static String KEY_REPO_NAMES = "sesame.repositories";
	public final static String KEY_SESAME_DATA_DIR = "sesame.data_dir";
	public final static String KEY_DEBUG_DUMP_RDF = "sesame.debug.dump_rdf";
	public final static String PREFIX_KEY_REPO = "sesame.repo.";
	public final static String SUFFIX_KEY_DATA_SUBDIR = ".data_subdir";
	
	private static Logger logger = LoggerFactory.getLogger(SesameBackend.class);
	private ApplicationProperties srProperties = null;
	private File sesameDataDir = null;
//	private RepositoryManager repositoryManager = null;
	private Hashtable<String, Repository> sesameRepos = null;
	
	private static SesameBackend instance = null;
	
	
	private SesameBackend() throws IOException, RepositoryConfigException, RepositoryException {
		// load properties
		srProperties = ApplicationProperties.getInstance(SESAME_REPOSITORY_PROPERTIES_NAME);
		
		sesameDataDir = new File(srProperties.getProperty(KEY_SESAME_DATA_DIR));
		initDataDir(sesameDataDir);
		
		// initialize repository manager
//		repositoryManager = new LocalRepositoryManager(sesameDataDir);
		
		// initialize repositories
		sesameRepos = new Hashtable<String, Repository>();
		String repositories = srProperties.getProperty(KEY_REPO_NAMES);
		for (String repoName : repositories.split(",")) {
			logger.debug("initializeing repository " + repoName);
			initRepo(repoName);
		}
	}
	
	public static SesameBackend getInstance() throws RepositoryConfigException,
			RepositoryException, IOException {
		
		if (instance == null)
			instance = new SesameBackend();
		
		return instance;
	}
	
	/**
	 * Make sure that the directory we're going to use as store for Sesame repositories
	 * does exists and is usable.
	 * 
	 * @param dataDir directory we want to store the Sesame repositories into
	 */
	public void initDataDir(File dataDir) {
		if (!dataDir.exists()) {
			if (!dataDir.mkdir())
				throw new IllegalArgumentException("failed to create directory"
						+ dataDir.toString());
		}
		else {
			if (!dataDir.isDirectory())
				throw new IllegalArgumentException(dataDir.toString()
						+ " already exists but is not a directory");
			if (!dataDir.canRead() || !dataDir.canWrite())
				throw new IllegalArgumentException(dataDir.toString()
						+ " not both readable and writable");
		}
	}
	
//	private boolean repoExists(File repoDataDir) throws RepositoryConfigException, RepositoryException {
//		Collection<Repository> repos = repositoryManager.getAllRepositories();
//		
//		for (Repository repo : repos)
//			if (repoDataDir.equals(repo.getDataDir().getAbsolutePath()))
//				return true;
//		
//		return false;
//	}
	
	public void initRepo(String repoName) throws RepositoryConfigException,
			RepositoryException {

		String repoSubDir = srProperties.getProperty(PREFIX_KEY_REPO + repoName
				+ SUFFIX_KEY_DATA_SUBDIR);
		File repoDataDir = new File(sesameDataDir, repoSubDir);
		
//		if (repoExists(repoDataDir))
//			// nothing to initialize, just proceed
//			return;
		
		// TODO: indexes and other configuration parameters (as needed)
		Repository repo = new SailRepository(new NativeStore(repoDataDir));
		repo.initialize();
		
		sesameRepos.put(repoName, repo);
		logger.info("repository '" + repoName + "' initialized (" + repoDataDir + ")");
	}
	
	public Repository getRepo(String repoName) {
		return sesameRepos.get(repoName);
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
	 */
	public void store(String repoName, RdfData records, String... contexts)
			throws IllegalArgumentException, OdnRepositoryException {

		Repository repo = getRepo(repoName);
		if (repo == null)
			throw new IllegalArgumentException(repoName + " does not exists");
		
		RepositoryConnection connection = null;
		OdnRepositoryException odnRepoException = null;
		
		URI[] convertedContexts = null;
		if (contexts.length > 0) {
			ValueFactory valueFactory = repo.getValueFactory();
			convertedContexts = new URI[contexts.length];
			int index = 0;
			for (String context : contexts) {
				convertedContexts[index++] = valueFactory.createURI(context);
			}
		}
		
		try {
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
				
				// why we duplicate the 'clear()' and 'add()' statents:
				// 'getStatements(null, null, null, true);' is not the same as
				// 'getStatements(null, null, null, true, (Resource)null);' -
				// see
				// http://www.openrdf.org/doc/sesame2/2.3.2/users/userguide.html#d0e1218
				connection.add(new StringReader(records.getRdfData()),
						records.getRdfBaseURI(), RDFFormat.RDFXML, convertedContexts);
			}
			else {
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
		}
		finally {
			if (connection != null)
				try {
					connection.close();
				} catch (RepositoryException e) {
					logger.error("repository exception in 'finally' statement", e);
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
