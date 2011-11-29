package sk.opendata.odn.harvester.datanest.organizations;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.repository.OdnRepositoryException;

/**
 * Stuff common to all Datanest harvesters.
 */
public abstract class AbstractDatanestHarvester {

	public final static String DATANEST_PROPERTIES_NAME = "/datanest.properties";
	public final static String KEY_DEBUG_PROCESS_ONLY_N_ITEMS = "datanest.debug.process_only_n_items";

	private static Logger logger = LoggerFactory.getLogger(AbstractDatanestHarvester.class);
	
	protected Properties datanestProperties = null;
	
	
	public AbstractDatanestHarvester() throws IOException {
		datanestProperties = new Properties();
		datanestProperties.load(getClass().getResourceAsStream(DATANEST_PROPERTIES_NAME));
	}
	
	public abstract void update() throws IOException, ParseException,
			RepositoryConfigException, RepositoryException,
			TransformerException, IllegalArgumentException,
			OdnRepositoryException;
	
	/**
	 * Method invoked by QUARTZ scheduler to launch this job.
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// TODO: implement the fetching of source data, "enhancer" and storage into the Sesame
		JobKey jobKey = context.getJobDetail().getKey();
		logger.info("scheduled job says: " + jobKey + " executing at " + new Date());
		
		// TODO: contemplate catching simply 'Exception' thus reducing the
		// amount of repetitive 'catch' statements
		try {
			update();
		} catch (IOException e) {
			logger.error("IO exception", e);
		} catch (ParseException e) {
			logger.error("parse exception", e);
		} catch (RepositoryConfigException e) {
			logger.error("repository config exception", e);
		} catch (RepositoryException e) {
			logger.error("repository exception", e);
		} catch (TransformerException e) {
			logger.error("XML transformation exception", e);
		} catch (IllegalArgumentException e) {
			logger.error("illegal argument exception", e);
		} catch (OdnRepositoryException e) {
			logger.error("repository exception", e);
		}
	}
}