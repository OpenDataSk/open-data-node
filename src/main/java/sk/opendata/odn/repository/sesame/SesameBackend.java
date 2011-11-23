package sk.opendata.odn.repository.sesame;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class SesameBackend {
	
	public final static String SESAME_REPOSITORY_PROPERTIES_NAME = "/repo-sesame.properties";
	public final static String KEY_REPO_NAMES = "sesame.repositories";
	public final static String KEY_SESAME_DATA_DIR = "sesame.data_dir";
	public final static String PREFIX_KEY_REPO = "sesame.repo.";
	public final static String SUFFIX_KEY_DATA_SUBDIR = ".data_subdir";
	
	private static Logger logger = LoggerFactory.getLogger(SesameBackend.class);
	private Properties srProperties = null;
	private File sesameDataDir = null;
//	private RepositoryManager repositoryManager = null;
	private Hashtable<String, Repository> sesameRepos = null;
	
	
	public SesameBackend() throws IOException, RepositoryConfigException, RepositoryException {
		// load properties
		srProperties = new Properties();
		srProperties.load(getClass().getResourceAsStream(SESAME_REPOSITORY_PROPERTIES_NAME));
		
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
	
	public void shutDown() throws RepositoryException {
		for (Repository repo : sesameRepos.values())
			repo.shutDown();
	}
	
}
