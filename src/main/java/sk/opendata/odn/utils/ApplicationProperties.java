package sk.opendata.odn.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.opendata.odn.harvester.datanest.organizations.AbstractDatanestHarvester;

public class ApplicationProperties extends Properties {
	
	private static final long serialVersionUID = 4684911806029642651L;

	public final String ODN_PROPERTY_SUBDIR = ".odn";
	
	private static Logger logger = LoggerFactory.getLogger(AbstractDatanestHarvester.class);
	
	
	/**
	 * Load properties from given file name.
	 * 
	 * Properties load procedure:
	 * 
	 * <ol>
	 * <li>{@code propertiesFileName} is loaded from resources bundled with
	 * application</li>
	 * <li>if file {@code propertiesFileName} exists in subdirectory
	 * {@code .odn} in user's home directory (as specified by {@code user.home}
	 * system property), properties are loaded from that file too and they are
	 * then added to the properties loaded from the bundled resources</li>
	 * <li>the resulting set of properties are then present in the instance of
	 * this class</li>
	 * </ol>
	 * 
	 * This allows for:
	 * 
	 * <ul>
	 * <li>all the necessary properties be specified in bundled property file</li>
	 * <li>user/administrator of the application can override some or all
	 * properties in the file
	 * {@code &lt;user.home&gt;/.odn/&lt;propertiesFileName&gt;}
	 * </ul>
	 * 
	 * TODO: As of now, we're using this as 'new
	 * ApplicationProperties("/file.properties")' => clarify the usage if
	 * staring '/'!
	 * 
	 * @param propertiesFileName
	 *            properties file name
	 * @throws IOException
	 *             when loading of the properties from bundle or file fails
	 */
	public ApplicationProperties(String propertiesFileName) throws IOException {
		super();
		
		// load the default properties from the bundle
		this.load(getClass().getResourceAsStream(propertiesFileName));
		logger.info("loaded default properties from '" + propertiesFileName + "' from the bundle");
		
		// load the properties from the user home directory and - if found and
		// properly loaded - use them to override properties from the bundle
		File homeSubdirFn = new File(System.getProperty("user.home"),
				ODN_PROPERTY_SUBDIR);
		File overrideFn = new File(homeSubdirFn, propertiesFileName);
		if (overrideFn.exists() && overrideFn.isFile() && overrideFn.canRead()) {
			FileInputStream in = new FileInputStream(overrideFn);
			Properties overrides = new Properties();
			overrides.load(in);
			
			this.putAll(overrides);
			logger.info("user properties loaded from '" + overrideFn.getAbsolutePath() + "'");
		}
	}

}
